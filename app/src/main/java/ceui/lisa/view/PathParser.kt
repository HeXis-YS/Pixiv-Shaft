package ceui.lisa.view

import android.graphics.Matrix
import android.graphics.Path
import android.os.Build
import androidx.annotation.NonNull
import java.util.ArrayList
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Suppress("WeakerAccess", "SameParameterValue", "PointlessArithmeticExpression")
object PathParser {

    private const val TAG = "PathParser"

    private fun copyOfRange(@NonNull original: FloatArray, start: Int, end: Int): FloatArray {
        val originalLength = original.size
        val resultLength = end - start
        val copyLength = min(resultLength, originalLength - start)
        val result = FloatArray(resultLength)
        System.arraycopy(original, start, result, 0, copyLength)
        return result
    }

    @JvmStatic
    fun transformScale(
        ratioWidth: Float,
        ratioHeight: Float,
        originPaths: List<Path>,
        orginSvgs: List<String>?,
    ): List<Path> {
        val matrix = Matrix()
        matrix.setScale(ratioWidth, ratioHeight)
        val paths = ArrayList<Path>()
        if (Build.VERSION.SDK_INT > 16) {
            for (path in originPaths) {
                val nPath = Path()
                path.transform(matrix, nPath)
                paths.add(nPath)
            }
        } else {
            for (svgPath in orginSvgs.orEmpty()) {
                val path = Path()
                val nodes = createNodesFromPathData(svgPath)!!
                transformScaleNodes(ratioWidth, ratioHeight, nodes)
                PathDataNode.nodesToPath(nodes, path)
                paths.add(path)
            }
        }
        return paths
    }

    private fun transformScaleNodes(ratioWidth: Float, ratioHeight: Float, node: Array<PathDataNode>) {
        for (aNode in node) {
            transformScaleCommand(ratioWidth, ratioHeight, aNode.type, aNode.params)
        }
    }

    private fun transformScaleCommand(ratioWidth: Float, ratioHeight: Float, cmd: Char, `val`: FloatArray) {
        var inc = 2
        when (cmd) {
            'z', 'Z' -> Unit
            'm', 'M', 'l', 'L', 't', 'T' -> inc = 2
            'h', 'H', 'v', 'V' -> inc = 1
            'c', 'C' -> inc = 6
            's', 'S', 'q', 'Q' -> inc = 4
            'a', 'A' -> inc = 7
        }
        var k = 0
        while (k < `val`.size) {
            when (cmd) {
                'm', 'M', 'l', 'L', 't', 'T' -> {
                    `val`[k] *= ratioWidth
                    `val`[k + 1] *= ratioHeight
                }

                'h', 'H' -> `val`[k] *= ratioWidth
                'v', 'V' -> `val`[k] *= ratioHeight
                'c', 'C' -> {
                    `val`[k] *= ratioWidth
                    `val`[k + 1] *= ratioHeight
                    `val`[k + 2] *= ratioWidth
                    `val`[k + 3] *= ratioHeight
                    `val`[k + 4] *= ratioWidth
                    `val`[k + 5] *= ratioHeight
                }

                's', 'S', 'q', 'Q' -> {
                    `val`[k] *= ratioWidth
                    `val`[k + 1] *= ratioHeight
                    `val`[k + 2] *= ratioWidth
                    `val`[k + 3] *= ratioHeight
                    if (cmd == 'q' || cmd == 'Q') {
                        // Keep the original Java fallthrough semantics exactly.
                        `val`[k] *= ratioWidth
                        `val`[k + 1] *= ratioHeight
                        `val`[k + 5] *= ratioWidth
                        `val`[k + 6] *= ratioHeight
                    }
                }

                'a', 'A' -> {
                    `val`[k] *= ratioWidth
                    `val`[k + 1] *= ratioHeight
                    `val`[k + 5] *= ratioWidth
                    `val`[k + 6] *= ratioHeight
                }
            }
            k += inc
        }
    }

    @JvmStatic
    fun createPathFromPathData(pathData: String?): Path? {
        val path = Path()
        val nodes = createNodesFromPathData(pathData)
        if (nodes != null) {
            try {
                PathDataNode.nodesToPath(nodes, path)
            } catch (e: RuntimeException) {
                throw RuntimeException("Error in parsing $pathData", e)
            }
            return path
        }
        return null
    }

    @JvmStatic
    fun createNodesFromPathData(pathData: String?): Array<PathDataNode>? {
        if (pathData == null) {
            return null
        }
        var start = 0
        var end = 1
        val list = ArrayList<PathDataNode>()
        while (end < pathData.length) {
            end = nextStart(pathData, end)
            val s = pathData.substring(start, end).trim()
            if (s.isNotEmpty()) {
                val `val` = getFloats(s)
                addNode(list, s[0], `val`)
            }
            start = end
            end++
        }
        if (end - start == 1 && start < pathData.length) {
            addNode(list, pathData[start], FloatArray(0))
        }
        return list.toTypedArray()
    }

    private fun nextStart(s: String, end: Int): Int {
        var index = end
        while (index < s.length) {
            val c = s[index]
            if ((((c - 'A') * (c - 'Z') <= 0) || ((c - 'a') * (c - 'z') <= 0)) &&
                c != 'e' && c != 'E'
            ) {
                return index
            }
            index++
        }
        return index
    }

    private fun addNode(list: MutableList<PathDataNode>, cmd: Char, `val`: FloatArray) {
        list.add(PathDataNode(cmd, `val`))
    }

    private class ExtractFloatResult {
        var mEndPosition = 0
        var mEndWithNegOrDot = false
    }

    private fun getFloats(s: String): FloatArray {
        if (s[0] == 'z' || s[0] == 'Z') {
            return FloatArray(0)
        }
        try {
            val results = FloatArray(s.length)
            var count = 0
            var startPosition = 1
            val result = ExtractFloatResult()
            val totalLength = s.length
            while (startPosition < totalLength) {
                extract(s, startPosition, result)
                val endPosition = result.mEndPosition
                if (startPosition < endPosition) {
                    results[count++] = s.substring(startPosition, endPosition).toFloat()
                }
                startPosition =
                    if (result.mEndWithNegOrDot) {
                        endPosition
                    } else {
                        endPosition + 1
                    }
            }
            return copyOfRange(results, 0, count)
        } catch (e: NumberFormatException) {
            throw RuntimeException("error in parsing \"$s\"", e)
        }
    }

    private fun extract(s: String, start: Int, result: ExtractFloatResult) {
        var currentIndex = start
        var foundSeparator = false
        result.mEndWithNegOrDot = false
        var secondDot = false
        var isExponential = false
        while (currentIndex < s.length) {
            val isPrevExponential = isExponential
            isExponential = false
            when (val currentChar = s[currentIndex]) {
                ' ', ',' -> foundSeparator = true
                '-' -> {
                    if (currentIndex != start && !isPrevExponential) {
                        foundSeparator = true
                        result.mEndWithNegOrDot = true
                    }
                }

                '.' -> {
                    if (!secondDot) {
                        secondDot = true
                    } else {
                        foundSeparator = true
                        result.mEndWithNegOrDot = true
                    }
                }

                'e', 'E' -> isExponential = true
            }
            if (foundSeparator) {
                break
            }
            currentIndex++
        }
        result.mEndPosition = currentIndex
    }

    class PathDataNode internal constructor(
        @JvmField var type: Char,
        @JvmField var params: FloatArray,
    ) {
        companion object {
            @JvmStatic
            fun nodesToPath(node: Array<PathDataNode>, path: Path) {
                val current = FloatArray(6)
                var previousCommand = 'm'
                for (aNode in node) {
                    addCommand(path, current, previousCommand, aNode.type, aNode.params)
                    previousCommand = aNode.type
                }
            }

            private fun addCommand(
                path: Path,
                current: FloatArray,
                previousCmd: Char,
                cmd: Char,
                `val`: FloatArray,
            ) {
                var inc = 2
                var currentX = current[0]
                var currentY = current[1]
                var ctrlPointX = current[2]
                var ctrlPointY = current[3]
                var currentSegmentStartX = current[4]
                var currentSegmentStartY = current[5]
                var reflectiveCtrlPointX: Float
                var reflectiveCtrlPointY: Float

                when (cmd) {
                    'z', 'Z' -> {
                        path.close()
                        currentX = currentSegmentStartX
                        currentY = currentSegmentStartY
                        ctrlPointX = currentSegmentStartX
                        ctrlPointY = currentSegmentStartY
                        path.moveTo(currentX, currentY)
                    }

                    'm', 'M', 'l', 'L', 't', 'T' -> inc = 2
                    'h', 'H', 'v', 'V' -> inc = 1
                    'c', 'C' -> inc = 6
                    's', 'S', 'q', 'Q' -> inc = 4
                    'a', 'A' -> inc = 7
                }

                var k = 0
                var previous = previousCmd
                while (k < `val`.size) {
                    when (cmd) {
                        'm' -> {
                            currentX += `val`[k]
                            currentY += `val`[k + 1]
                            if (k > 0) {
                                path.rLineTo(`val`[k], `val`[k + 1])
                            } else {
                                path.rMoveTo(`val`[k], `val`[k + 1])
                                currentSegmentStartX = currentX
                                currentSegmentStartY = currentY
                            }
                        }

                        'M' -> {
                            currentX = `val`[k]
                            currentY = `val`[k + 1]
                            if (k > 0) {
                                path.lineTo(`val`[k], `val`[k + 1])
                            } else {
                                path.moveTo(`val`[k], `val`[k + 1])
                                currentSegmentStartX = currentX
                                currentSegmentStartY = currentY
                            }
                        }

                        'l' -> {
                            path.rLineTo(`val`[k], `val`[k + 1])
                            currentX += `val`[k]
                            currentY += `val`[k + 1]
                        }

                        'L' -> {
                            path.lineTo(`val`[k], `val`[k + 1])
                            currentX = `val`[k]
                            currentY = `val`[k + 1]
                        }

                        'h' -> {
                            path.rLineTo(`val`[k], 0f)
                            currentX += `val`[k]
                        }

                        'H' -> {
                            path.lineTo(`val`[k], currentY)
                            currentX = `val`[k]
                        }

                        'v' -> {
                            path.rLineTo(0f, `val`[k])
                            currentY += `val`[k]
                        }

                        'V' -> {
                            path.lineTo(currentX, `val`[k])
                            currentY = `val`[k]
                        }

                        'c' -> {
                            path.rCubicTo(
                                `val`[k],
                                `val`[k + 1],
                                `val`[k + 2],
                                `val`[k + 3],
                                `val`[k + 4],
                                `val`[k + 5],
                            )
                            ctrlPointX = currentX + `val`[k + 2]
                            ctrlPointY = currentY + `val`[k + 3]
                            currentX += `val`[k + 4]
                            currentY += `val`[k + 5]
                        }

                        'C' -> {
                            path.cubicTo(
                                `val`[k],
                                `val`[k + 1],
                                `val`[k + 2],
                                `val`[k + 3],
                                `val`[k + 4],
                                `val`[k + 5],
                            )
                            currentX = `val`[k + 4]
                            currentY = `val`[k + 5]
                            ctrlPointX = `val`[k + 2]
                            ctrlPointY = `val`[k + 3]
                        }

                        's' -> {
                            reflectiveCtrlPointX = 0f
                            reflectiveCtrlPointY = 0f
                            if (previous == 'c' || previous == 's' || previous == 'C' || previous == 'S') {
                                reflectiveCtrlPointX = currentX - ctrlPointX
                                reflectiveCtrlPointY = currentY - ctrlPointY
                            }
                            path.rCubicTo(
                                reflectiveCtrlPointX,
                                reflectiveCtrlPointY,
                                `val`[k],
                                `val`[k + 1],
                                `val`[k + 2],
                                `val`[k + 3],
                            )
                            ctrlPointX = currentX + `val`[k]
                            ctrlPointY = currentY + `val`[k + 1]
                            currentX += `val`[k + 2]
                            currentY += `val`[k + 3]
                        }

                        'S' -> {
                            reflectiveCtrlPointX = currentX
                            reflectiveCtrlPointY = currentY
                            if (previous == 'c' || previous == 's' || previous == 'C' || previous == 'S') {
                                reflectiveCtrlPointX = 2 * currentX - ctrlPointX
                                reflectiveCtrlPointY = 2 * currentY - ctrlPointY
                            }
                            path.cubicTo(
                                reflectiveCtrlPointX,
                                reflectiveCtrlPointY,
                                `val`[k],
                                `val`[k + 1],
                                `val`[k + 2],
                                `val`[k + 3],
                            )
                            ctrlPointX = `val`[k]
                            ctrlPointY = `val`[k + 1]
                            currentX = `val`[k + 2]
                            currentY = `val`[k + 3]
                        }

                        'q' -> {
                            path.rQuadTo(`val`[k], `val`[k + 1], `val`[k + 2], `val`[k + 3])
                            ctrlPointX = currentX + `val`[k]
                            ctrlPointY = currentY + `val`[k + 1]
                            currentX += `val`[k + 2]
                            currentY += `val`[k + 3]
                        }

                        'Q' -> {
                            path.quadTo(`val`[k], `val`[k + 1], `val`[k + 2], `val`[k + 3])
                            ctrlPointX = `val`[k]
                            ctrlPointY = `val`[k + 1]
                            currentX = `val`[k + 2]
                            currentY = `val`[k + 3]
                        }

                        't' -> {
                            reflectiveCtrlPointX = 0f
                            reflectiveCtrlPointY = 0f
                            if (previous == 'q' || previous == 't' || previous == 'Q' || previous == 'T') {
                                reflectiveCtrlPointX = currentX - ctrlPointX
                                reflectiveCtrlPointY = currentY - ctrlPointY
                            }
                            path.rQuadTo(
                                reflectiveCtrlPointX,
                                reflectiveCtrlPointY,
                                `val`[k],
                                `val`[k + 1],
                            )
                            ctrlPointX = currentX + reflectiveCtrlPointX
                            ctrlPointY = currentY + reflectiveCtrlPointY
                            currentX += `val`[k]
                            currentY += `val`[k + 1]
                        }

                        'T' -> {
                            reflectiveCtrlPointX = currentX
                            reflectiveCtrlPointY = currentY
                            if (previous == 'q' || previous == 't' || previous == 'Q' || previous == 'T') {
                                reflectiveCtrlPointX = 2 * currentX - ctrlPointX
                                reflectiveCtrlPointY = 2 * currentY - ctrlPointY
                            }
                            path.quadTo(
                                reflectiveCtrlPointX,
                                reflectiveCtrlPointY,
                                `val`[k],
                                `val`[k + 1],
                            )
                            ctrlPointX = reflectiveCtrlPointX
                            ctrlPointY = reflectiveCtrlPointY
                            currentX = `val`[k]
                            currentY = `val`[k + 1]
                        }

                        'a' -> {
                            drawArc(
                                path,
                                currentX,
                                currentY,
                                `val`[k + 5] + currentX,
                                `val`[k + 6] + currentY,
                                `val`[k],
                                `val`[k + 1],
                                `val`[k + 2],
                                `val`[k + 3] != 0f,
                                `val`[k + 4] != 0f,
                            )
                            currentX += `val`[k + 5]
                            currentY += `val`[k + 6]
                            ctrlPointX = currentX
                            ctrlPointY = currentY
                        }

                        'A' -> {
                            drawArc(
                                path,
                                currentX,
                                currentY,
                                `val`[k + 5],
                                `val`[k + 6],
                                `val`[k],
                                `val`[k + 1],
                                `val`[k + 2],
                                `val`[k + 3] != 0f,
                                `val`[k + 4] != 0f,
                            )
                            currentX = `val`[k + 5]
                            currentY = `val`[k + 6]
                            ctrlPointX = currentX
                            ctrlPointY = currentY
                        }
                    }
                    previous = cmd
                    k += inc
                }
                current[0] = currentX
                current[1] = currentY
                current[2] = ctrlPointX
                current[3] = ctrlPointY
                current[4] = currentSegmentStartX
                current[5] = currentSegmentStartY
            }

            private fun drawArc(
                p: Path,
                x0: Float,
                y0: Float,
                x1: Float,
                y1: Float,
                a: Float,
                b: Float,
                theta: Float,
                isMoreThanHalf: Boolean,
                isPositiveArc: Boolean,
            ) {
                val thetaD = Math.toRadians(theta.toDouble())
                val cosTheta = cos(thetaD)
                val sinTheta = sin(thetaD)
                val x0p = (x0 * cosTheta + y0 * sinTheta) / a
                val y0p = (-x0 * sinTheta + y0 * cosTheta) / b
                val x1p = (x1 * cosTheta + y1 * sinTheta) / a
                val y1p = (-x1 * sinTheta + y1 * cosTheta) / b
                val dx = x0p - x1p
                val dy = y0p - y1p
                val xm = (x0p + x1p) / 2
                val ym = (y0p + y1p) / 2
                val dsq = dx * dx + dy * dy
                if (dsq == 0.0) {
                    return
                }
                val disc = 1.0 / dsq - 1.0 / 4.0
                if (disc < 0.0) {
                    val adjust = (sqrt(dsq) / 1.99999).toFloat()
                    drawArc(p, x0, y0, x1, y1, a * adjust, b * adjust, theta, isMoreThanHalf, isPositiveArc)
                    return
                }
                val s = sqrt(disc)
                val sdx = s * dx
                val sdy = s * dy
                var cx: Double
                var cy: Double
                if (isMoreThanHalf == isPositiveArc) {
                    cx = xm - sdy
                    cy = ym + sdx
                } else {
                    cx = xm + sdy
                    cy = ym - sdx
                }
                val eta0 = Math.atan2(y0p - cy, x0p - cx)
                val eta1 = Math.atan2(y1p - cy, x1p - cx)
                var sweep = eta1 - eta0
                if (isPositiveArc != (sweep >= 0)) {
                    sweep =
                        if (sweep > 0) {
                            sweep - 2 * Math.PI
                        } else {
                            sweep + 2 * Math.PI
                        }
                }
                cx *= a.toDouble()
                cy *= b.toDouble()
                val tcx = cx
                cx = cx * cosTheta - cy * sinTheta
                cy = tcx * sinTheta + cy * cosTheta
                arcToBezier(p, cx, cy, a.toDouble(), b.toDouble(), x0.toDouble(), y0.toDouble(), thetaD, eta0, sweep)
            }

            private fun arcToBezier(
                p: Path,
                cx: Double,
                cy: Double,
                a: Double,
                b: Double,
                e1x: Double,
                e1y: Double,
                theta: Double,
                start: Double,
                sweep: Double,
            ) {
                val numSegments = ceil(kotlin.math.abs(sweep * 4 / Math.PI)).toInt()
                var eta1 = start
                val cosTheta = cos(theta)
                val sinTheta = sin(theta)
                val cosEta1 = cos(eta1)
                val sinEta1 = sin(eta1)
                var currentE1x = e1x
                var currentE1y = e1y
                var ep1x = (-a * cosTheta * sinEta1) - (b * sinTheta * cosEta1)
                var ep1y = (-a * sinTheta * sinEta1) + (b * cosTheta * cosEta1)
                val anglePerSegment = sweep / numSegments
                for (i in 0 until numSegments) {
                    val eta2 = eta1 + anglePerSegment
                    val sinEta2 = sin(eta2)
                    val cosEta2 = cos(eta2)
                    val e2x = cx + (a * cosTheta * cosEta2) - (b * sinTheta * sinEta2)
                    val e2y = cy + (a * sinTheta * cosEta2) + (b * cosTheta * sinEta2)
                    val ep2x = -a * cosTheta * sinEta2 - b * sinTheta * cosEta2
                    val ep2y = -a * sinTheta * sinEta2 + b * cosTheta * cosEta2
                    val tanDiff2 = tan((eta2 - eta1) / 2)
                    val alpha = sin(eta2 - eta1) * (sqrt(4 + (3 * tanDiff2 * tanDiff2)) - 1) / 3
                    val q1x = currentE1x + alpha * ep1x
                    val q1y = currentE1y + alpha * ep1y
                    val q2x = e2x - alpha * ep2x
                    val q2y = e2y - alpha * ep2y
                    p.rLineTo(0f, 0f)
                    p.cubicTo(
                        q1x.toFloat(),
                        q1y.toFloat(),
                        q2x.toFloat(),
                        q2y.toFloat(),
                        e2x.toFloat(),
                        e2y.toFloat(),
                    )
                    eta1 = eta2
                    currentE1x = e2x
                    currentE1y = e2y
                    ep1x = ep2x
                    ep1y = ep2y
                }
            }
        }
    }
}
