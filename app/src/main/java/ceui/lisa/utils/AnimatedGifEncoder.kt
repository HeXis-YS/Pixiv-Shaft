package ceui.lisa.utils

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.graphics.Paint
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max

class AnimatedGifEncoder {

    protected var width = 0
    protected var height = 0
    protected var x = 0
    protected var y = 0
    protected var transparentColor = -1
    protected var transIndex = 0
    protected var repeatCount = -1
    protected var delayTime = 0
    protected var started = false
    protected var out: OutputStream? = null
    protected var image: Bitmap? = null
    protected var pixels: ByteArray? = null
    protected var indexedPixels: ByteArray? = null
    protected var colorDepth = 0
    protected var colorTab: ByteArray? = null
    protected var usedEntry = BooleanArray(256)
    protected var palSize = 7
    protected var disposeCode = -1
    protected var closeStream = false
    protected var firstFrame = true
    protected var sizeSet = false
    protected var sample = 10

    fun setDelay(ms: Int) {
        delayTime = ms / 10
    }

    fun setDispose(code: Int) {
        if (code >= 0) {
            disposeCode = code
        }
    }

    fun setRepeat(iter: Int) {
        if (iter >= 0) {
            repeatCount = iter
        }
    }

    fun setTransparent(c: Int) {
        transparentColor = c
    }

    fun addFrame(im: Bitmap?): Boolean {
        if (im == null || !started) {
            return false
        }
        var ok = true
        try {
            if (!sizeSet) {
                setSize(im.width, im.height)
            }
            image?.recycle()
            image = im
            getImagePixels()
            analyzePixels()
            if (firstFrame) {
                writeLSD()
                writePalette()
                if (repeatCount >= 0) {
                    writeNetscapeExt()
                }
            }
            writeGraphicCtrlExt()
            writeImageDesc()
            if (!firstFrame) {
                writePalette()
            }
            writePixels()
            firstFrame = false
        } catch (_: IOException) {
            ok = false
        }
        return ok
    }

    fun finish(): Boolean {
        if (!started) {
            return false
        }
        var ok = true
        started = false
        try {
            out!!.write(0x3b)
            out!!.flush()
            if (closeStream) {
                out!!.close()
            }
        } catch (_: IOException) {
            ok = false
        }

        transIndex = 0
        out = null
        image = null
        pixels = null
        indexedPixels = null
        colorTab = null
        closeStream = false
        firstFrame = true

        return ok
    }

    fun setFrameRate(fps: Float) {
        if (fps != 0f) {
            delayTime = (100 / fps).toInt()
        }
    }

    fun setQuality(quality: Int) {
        sample = if (quality < 1) 1 else quality
    }

    fun setSize(w: Int, h: Int) {
        width = if (w < 1) 320 else w
        height = if (h < 1) 240 else h
        sizeSet = true
    }

    fun setPosition(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun start(os: OutputStream?): Boolean {
        if (os == null) {
            return false
        }
        var ok = true
        closeStream = false
        out = os
        try {
            writeString("GIF89a")
        } catch (_: IOException) {
            ok = false
        }
        started = ok
        return started
    }

    protected fun analyzePixels() {
        val len = pixels!!.size
        val nPix = len / 3
        indexedPixels = ByteArray(nPix)
        val nq = NeuQuant(pixels!!, len, sample)
        colorTab = nq.process()
        var i = 0
        while (i < colorTab!!.size) {
            val temp = colorTab!![i]
            colorTab!![i] = colorTab!![i + 2]
            colorTab!![i + 2] = temp
            usedEntry[i / 3] = false
            i += 3
        }
        var k = 0
        for (index in 0 until nPix) {
            val mapped = nq.map(pixels!![k++].toInt() and 0xff, pixels!![k++].toInt() and 0xff, pixels!![k++].toInt() and 0xff)
            usedEntry[mapped] = true
            indexedPixels!![index] = mapped.toByte()
        }
        pixels = null
        colorDepth = 8
        palSize = 7
        if (transparentColor != -1) {
            transIndex = findClosest(transparentColor)
        }
    }

    protected fun findClosest(c: Int): Int {
        if (colorTab == null) {
            return -1
        }
        val r = c shr 16 and 0xff
        val g = c shr 8 and 0xff
        val b = c and 0xff
        var minPos = 0
        var dmin = 256 * 256 * 256
        var i = 0
        while (i < colorTab!!.size) {
            val dr = r - (colorTab!![i++].toInt() and 0xff)
            val dg = g - (colorTab!![i++].toInt() and 0xff)
            val db = b - (colorTab!![i].toInt() and 0xff)
            val d = dr * dr + dg * dg + db * db
            val index = i / 3
            if (usedEntry[index] && d < dmin) {
                dmin = d
                minPos = index
            }
            i++
        }
        return minPos
    }

    protected fun getImagePixels() {
        val current = image!!
        val w = current.width
        val h = current.height
        if (w != width || h != height) {
            val temp = Bitmap.createBitmap(width, height, Config.RGB_565)
            val canvas = Canvas(temp)
            canvas.drawBitmap(current, 0f, 0f, Paint())
            image = temp
        }
        val data = getImageData(image!!)
        pixels = ByteArray(data.size * 3)
        for (i in data.indices) {
            val td = data[i]
            var tind = i * 3
            pixels!![tind++] = (td and 0xFF).toByte()
            pixels!![tind++] = (td shr 8 and 0xFF).toByte()
            pixels!![tind] = (td shr 16 and 0xFF).toByte()
        }
    }

    protected fun getImageData(img: Bitmap): IntArray {
        val w = img.width
        val h = img.height
        val data = IntArray(w * h)
        img.getPixels(data, 0, w, 0, 0, w, h)
        return data
    }

    @Throws(IOException::class)
    protected fun writeGraphicCtrlExt() {
        out!!.write(0x21)
        out!!.write(0xf9)
        out!!.write(4)
        val transp: Int
        var disp = 0
        if (transparentColor == -1) {
            transp = 0
            disp = 0
        } else {
            transp = 1
            disp = 2
        }
        if (disposeCode >= 0) {
            disp = disposeCode and 7
        }
        disp = disp shl 2

        out!!.write(0 or disp or 0 or transp)
        writeShort(delayTime)
        out!!.write(transIndex)
        out!!.write(0)
    }

    @Throws(IOException::class)
    protected fun writeImageDesc() {
        out!!.write(0x2c)
        writeShort(x)
        writeShort(y)
        writeShort(width)
        writeShort(height)
        if (firstFrame) {
            out!!.write(0)
        } else {
            out!!.write(0x80 or 0 or 0 or 0 or palSize)
        }
    }

    @Throws(IOException::class)
    protected fun writeLSD() {
        writeShort(width)
        writeShort(height)
        out!!.write(0x80 or 0x70 or 0x00 or palSize)
        out!!.write(0)
        out!!.write(0)
    }

    @Throws(IOException::class)
    protected fun writeNetscapeExt() {
        out!!.write(0x21)
        out!!.write(0xff)
        out!!.write(11)
        writeString("NETSCAPE2.0")
        out!!.write(3)
        out!!.write(1)
        writeShort(repeatCount)
        out!!.write(0)
    }

    @Throws(IOException::class)
    protected fun writePalette() {
        out!!.write(colorTab!!, 0, colorTab!!.size)
        val n = 3 * 256 - colorTab!!.size
        repeat(n) {
            out!!.write(0)
        }
    }

    @Throws(IOException::class)
    protected fun writePixels() {
        val encoder = LZWEncoder(width, height, indexedPixels!!, colorDepth)
        encoder.encode(out!!)
    }

    @Throws(IOException::class)
    protected fun writeShort(value: Int) {
        out!!.write(value and 0xff)
        out!!.write(value shr 8 and 0xff)
    }

    @Throws(IOException::class)
    protected fun writeString(s: String) {
        for (i in s.indices) {
            out!!.write(s[i].code.toByte().toInt())
        }
    }
}

class NeuQuant(thepic: ByteArray, len: Int, sample: Int) {

    protected var alphadec = 0
    protected var thepicture: ByteArray = thepic
    protected var lengthcount = len
    protected var samplefac = sample
    protected var network: Array<IntArray> = Array(netsize) { IntArray(4) }
    protected var netindex = IntArray(256)
    protected var bias = IntArray(netsize)
    protected var freq = IntArray(netsize)
    protected var radpower = IntArray(initrad)

    init {
        for (i in 0 until netsize) {
            val p = network[i]
            p[0] = (i shl (netbiasshift + 8)) / netsize
            p[1] = p[0]
            p[2] = p[0]
            freq[i] = intbias / netsize
            bias[i] = 0
        }
    }

    fun colorMap(): ByteArray {
        val map = ByteArray(3 * netsize)
        val index = IntArray(netsize)
        for (i in 0 until netsize) {
            index[network[i][3]] = i
        }
        var k = 0
        for (i in 0 until netsize) {
            val j = index[i]
            map[k++] = network[j][0].toByte()
            map[k++] = network[j][1].toByte()
            map[k++] = network[j][2].toByte()
        }
        return map
    }

    fun inxbuild() {
        var previouscol = 0
        var startpos = 0
        for (i in 0 until netsize) {
            val p = network[i]
            var smallpos = i
            var smallval = p[1]
            for (j in i + 1 until netsize) {
                val q = network[j]
                if (q[1] < smallval) {
                    smallpos = j
                    smallval = q[1]
                }
            }
            val q = network[smallpos]
            if (i != smallpos) {
                var j = q[0]
                q[0] = p[0]
                p[0] = j
                j = q[1]
                q[1] = p[1]
                p[1] = j
                j = q[2]
                q[2] = p[2]
                p[2] = j
                j = q[3]
                q[3] = p[3]
                p[3] = j
            }
            if (smallval != previouscol) {
                netindex[previouscol] = startpos + i shr 1
                for (j in previouscol + 1 until smallval) {
                    netindex[j] = i
                }
                previouscol = smallval
                startpos = i
            }
        }
        netindex[previouscol] = startpos + maxnetpos shr 1
        for (j in previouscol + 1 until 256) {
            netindex[j] = maxnetpos
        }
    }

    fun learn() {
        if (lengthcount < minpicturebytes) {
            samplefac = 1
        }
        alphadec = 30 + (samplefac - 1) / 3
        val p = thepicture
        var pix = 0
        val lim = lengthcount
        val samplepixels = lengthcount / (3 * samplefac)
        var delta = samplepixels / ncycles
        var alpha = initalpha
        var radius = initradius

        var rad = radius shr radiusbiasshift
        if (rad <= 1) {
            rad = 0
        }
        for (i in 0 until rad) {
            radpower[i] = alpha * (((rad * rad - i * i) * radbias) / (rad * rad))
        }

        val step =
            if (lengthcount < minpicturebytes) {
                3
            } else if (lengthcount % prime1 != 0) {
                3 * prime1
            } else if (lengthcount % prime2 != 0) {
                3 * prime2
            } else if (lengthcount % prime3 != 0) {
                3 * prime3
            } else {
                3 * prime4
            }

        var i = 0
        while (i < samplepixels) {
            val b = (p[pix].toInt() and 0xff) shl netbiasshift
            val g = (p[pix + 1].toInt() and 0xff) shl netbiasshift
            val r = (p[pix + 2].toInt() and 0xff) shl netbiasshift
            val j = contest(b, g, r)

            altersingle(alpha, j, b, g, r)
            if (rad != 0) {
                alterneigh(rad, j, b, g, r)
            }

            pix += step
            if (pix >= lim) {
                pix -= lengthcount
            }

            i++
            if (delta == 0) {
                delta = 1
            }
            if (i % delta == 0) {
                alpha -= alpha / alphadec
                radius -= radius / radiusdec
                rad = radius shr radiusbiasshift
                if (rad <= 1) {
                    rad = 0
                }
                for (j in 0 until rad) {
                    radpower[j] = alpha * (((rad * rad - j * j) * radbias) / (rad * rad))
                }
            }
        }
    }

    fun map(b: Int, g: Int, r: Int): Int {
        var bestd = 1000
        var best = -1
        var i = netindex[g]
        var j = i - 1

        while (i < netsize || j >= 0) {
            if (i < netsize) {
                val p = network[i]
                var dist = p[1] - g
                if (dist >= bestd) {
                    i = netsize
                } else {
                    i++
                    if (dist < 0) {
                        dist = -dist
                    }
                    var a = p[0] - b
                    if (a < 0) {
                        a = -a
                    }
                    dist += a
                    if (dist < bestd) {
                        a = p[2] - r
                        if (a < 0) {
                            a = -a
                        }
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3]
                        }
                    }
                }
            }
            if (j >= 0) {
                val p = network[j]
                var dist = g - p[1]
                if (dist >= bestd) {
                    j = -1
                } else {
                    j--
                    if (dist < 0) {
                        dist = -dist
                    }
                    var a = p[0] - b
                    if (a < 0) {
                        a = -a
                    }
                    dist += a
                    if (dist < bestd) {
                        a = p[2] - r
                        if (a < 0) {
                            a = -a
                        }
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3]
                        }
                    }
                }
            }
        }
        return best
    }

    fun process(): ByteArray {
        learn()
        unbiasnet()
        inxbuild()
        return colorMap()
    }

    fun unbiasnet() {
        for (i in 0 until netsize) {
            network[i][0] = network[i][0] shr netbiasshift
            network[i][1] = network[i][1] shr netbiasshift
            network[i][2] = network[i][2] shr netbiasshift
            network[i][3] = i
        }
    }

    protected fun alterneigh(rad: Int, i: Int, b: Int, g: Int, r: Int) {
        var lo = i - rad
        if (lo < -1) {
            lo = -1
        }
        var hi = i + rad
        if (hi > netsize) {
            hi = netsize
        }

        var j = i + 1
        var k = i - 1
        var m = 1
        while (j < hi || k > lo) {
            val a = radpower[m++]
            if (j < hi) {
                val p = network[j++]
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias
                    p[1] -= (a * (p[1] - g)) / alpharadbias
                    p[2] -= (a * (p[2] - r)) / alpharadbias
                } catch (_: Exception) {
                }
            }
            if (k > lo) {
                val p = network[k--]
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias
                    p[1] -= (a * (p[1] - g)) / alpharadbias
                    p[2] -= (a * (p[2] - r)) / alpharadbias
                } catch (_: Exception) {
                }
            }
        }
    }

    protected fun altersingle(alpha: Int, i: Int, b: Int, g: Int, r: Int) {
        val n = network[i]
        n[0] -= (alpha * (n[0] - b)) / initalpha
        n[1] -= (alpha * (n[1] - g)) / initalpha
        n[2] -= (alpha * (n[2] - r)) / initalpha
    }

    protected fun contest(b: Int, g: Int, r: Int): Int {
        var bestd = Int.MAX_VALUE
        var bestbiasd = bestd
        var bestpos = -1
        var bestbiaspos = bestpos

        for (i in 0 until netsize) {
            val n = network[i]
            var dist = n[0] - b
            if (dist < 0) {
                dist = -dist
            }
            var a = n[1] - g
            if (a < 0) {
                a = -a
            }
            dist += a
            a = n[2] - r
            if (a < 0) {
                a = -a
            }
            dist += a
            if (dist < bestd) {
                bestd = dist
                bestpos = i
            }
            val biasdist = dist - (bias[i] shr (intbiasshift - netbiasshift))
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist
                bestbiaspos = i
            }
            val betafreq = freq[i] shr betashift
            freq[i] -= betafreq
            bias[i] += betafreq shl gammashift
        }
        freq[bestpos] += beta
        bias[bestpos] -= betagamma
        return bestbiaspos
    }

    companion object {
        protected const val netsize = 256
        protected const val prime1 = 499
        protected const val prime2 = 491
        protected const val prime3 = 487
        protected const val prime4 = 503
        protected const val minpicturebytes = 3 * prime4
        protected const val maxnetpos = netsize - 1
        protected const val netbiasshift = 4
        protected const val ncycles = 100
        protected const val intbiasshift = 16
        protected const val intbias = 1 shl intbiasshift
        protected const val gammashift = 10
        protected const val gamma = 1 shl gammashift
        protected const val betashift = 10
        protected const val beta = intbias shr betashift
        protected const val betagamma = intbias shl (gammashift - betashift)
        protected const val initrad = netsize shr 3
        protected const val radiusbiasshift = 6
        protected const val radiusbias = 1 shl radiusbiasshift
        protected const val initradius = initrad * radiusbias
        protected const val radiusdec = 30
        protected const val alphabiasshift = 10
        protected const val initalpha = 1 shl alphabiasshift
        protected const val radbiasshift = 8
        protected const val radbias = 1 shl radbiasshift
        protected const val alpharadbshift = alphabiasshift + radbiasshift
        protected const val alpharadbias = 1 shl alpharadbshift
    }
}

class LZWEncoder(width: Int, height: Int, pixels: ByteArray, colorDepth: Int) {

    private val imgW = width
    private val imgH = height
    private val pixAry = pixels
    private val initCodeSize = max(2, colorDepth)
    private var remaining = 0
    private var curPixel = 0

    var nBits = 0
    var maxbits = BITS
    var maxcode = 0
    var maxmaxcode = 1 shl BITS
    var htab = IntArray(HSIZE)
    var codetab = IntArray(HSIZE)
    var hsize = HSIZE
    var freeEnt = 0
    var clearFlg = false
    var gInitBits = 0
    var clearCode = 0
    var eofCode = 0
    var curAccum = 0
    var curBits = 0
    var masks =
        intArrayOf(
            0x0000,
            0x0001,
            0x0003,
            0x0007,
            0x000F,
            0x001F,
            0x003F,
            0x007F,
            0x00FF,
            0x01FF,
            0x03FF,
            0x07FF,
            0x0FFF,
            0x1FFF,
            0x3FFF,
            0x7FFF,
            0xFFFF,
        )
    var aCount = 0
    var accum = ByteArray(256)

    @Throws(IOException::class)
    fun charOut(c: Byte, outs: OutputStream) {
        accum[aCount++] = c
        if (aCount >= 254) {
            flushChar(outs)
        }
    }

    @Throws(IOException::class)
    fun clBlock(outs: OutputStream) {
        clHash(hsize)
        freeEnt = clearCode + 2
        clearFlg = true
        output(clearCode, outs)
    }

    fun clHash(hsize: Int) {
        for (i in 0 until hsize) {
            htab[i] = -1
        }
    }

    @Throws(IOException::class)
    fun compress(initBits: Int, outs: OutputStream) {
        gInitBits = initBits
        clearFlg = false
        nBits = gInitBits
        maxcode = MAXCODE(nBits)

        clearCode = 1 shl (initBits - 1)
        eofCode = clearCode + 1
        freeEnt = clearCode + 2

        aCount = 0
        var ent = nextPixel()

        var hshift = 0
        var fcode = hsize
        while (fcode < 65536) {
            ++hshift
            fcode *= 2
        }
        hshift = 8 - hshift

        val hsizeReg = hsize
        clHash(hsizeReg)

        output(clearCode, outs)

        while (true) {
            val c = nextPixel()
            if (c == EOF) {
                break
            }
            fcode = (c shl maxbits) + ent
            var i = (c shl hshift) xor ent

            if (htab[i] == fcode) {
                ent = codetab[i]
                continue
            } else if (htab[i] >= 0) {
                var disp = hsizeReg - i
                if (i == 0) {
                    disp = 1
                }
                while (true) {
                    i -= disp
                    if (i < 0) {
                        i += hsizeReg
                    }
                    if (htab[i] == fcode) {
                        ent = codetab[i]
                        break
                    }
                    if (htab[i] < 0) {
                        output(ent, outs)
                        ent = c
                        if (freeEnt < maxmaxcode) {
                            codetab[i] = freeEnt++
                            htab[i] = fcode
                        } else {
                            clBlock(outs)
                        }
                        i = -1
                        break
                    }
                }
                if (i >= 0) {
                    continue
                }
                continue
            }

            output(ent, outs)
            ent = c
            if (freeEnt < maxmaxcode) {
                codetab[i] = freeEnt++
                htab[i] = fcode
            } else {
                clBlock(outs)
            }
        }
        output(ent, outs)
        output(eofCode, outs)
    }

    @Throws(IOException::class)
    fun encode(os: OutputStream) {
        os.write(initCodeSize)
        remaining = imgW * imgH
        curPixel = 0
        compress(initCodeSize + 1, os)
        os.write(0)
    }

    @Throws(IOException::class)
    fun flushChar(outs: OutputStream) {
        if (aCount > 0) {
            outs.write(aCount)
            outs.write(accum, 0, aCount)
            aCount = 0
        }
    }

    fun MAXCODE(nBits: Int): Int = (1 shl nBits) - 1

    private fun nextPixel(): Int {
        if (remaining == 0) {
            return EOF
        }
        --remaining
        val pix = pixAry[curPixel++]
        return pix.toInt() and 0xff
    }

    @Throws(IOException::class)
    fun output(code: Int, outs: OutputStream) {
        curAccum = curAccum and masks[curBits]

        if (curBits > 0) {
            curAccum = curAccum or (code shl curBits)
        } else {
            curAccum = code
        }

        curBits += nBits

        while (curBits >= 8) {
            charOut((curAccum and 0xff).toByte(), outs)
            curAccum = curAccum shr 8
            curBits -= 8
        }

        if (freeEnt > maxcode || clearFlg) {
            if (clearFlg) {
                nBits = gInitBits
                maxcode = MAXCODE(nBits)
                clearFlg = false
            } else {
                ++nBits
                maxcode = if (nBits == maxbits) maxmaxcode else MAXCODE(nBits)
            }
        }

        if (code == eofCode) {
            while (curBits > 0) {
                charOut((curAccum and 0xff).toByte(), outs)
                curAccum = curAccum shr 8
                curBits -= 8
            }
            flushChar(outs)
        }
    }

    companion object {
        private const val EOF = -1
        const val BITS = 12
        const val HSIZE = 5003
    }
}
