package ceui.lisa.utils

import android.text.TextUtils
import ceui.lisa.model.EmojiItem

object Emoji {

    @JvmStatic
    fun main(args: Array<String>) {
        println(111)
    }

    private const val EMOJI_1 = "(normal)"
    private const val EMOJI_2 = "(surprise)"
    private const val EMOJI_3 = "(serious)"
    private const val EMOJI_4 = "(heaven)"
    private const val EMOJI_5 = "(happy)"
    private const val EMOJI_6 = "(excited)"
    private const val EMOJI_7 = "(sing)"
    private const val EMOJI_8 = "(cry)"
    private const val EMOJI_9 = "(normal2)"
    private const val EMOJI_10 = "(shame2)"
    private const val EMOJI_11 = "(love2)"
    private const val EMOJI_12 = "(interesting2)"
    private const val EMOJI_13 = "(blush2)"
    private const val EMOJI_14 = "(fire2)"
    private const val EMOJI_15 = "(angry2)"
    private const val EMOJI_16 = "(shine2)"
    private const val EMOJI_17 = "(panic2)"
    private const val EMOJI_18 = "(normal3)"
    private const val EMOJI_19 = "(satisfaction3)"
    private const val EMOJI_20 = "(surprise3)"
    private const val EMOJI_21 = "(smile3)"
    private const val EMOJI_22 = "(shock3)"
    private const val EMOJI_23 = "(gaze3)"
    private const val EMOJI_24 = "(wink3)"
    private const val EMOJI_25 = "(happy3)"
    private const val EMOJI_26 = "(excited3)"
    private const val EMOJI_27 = "(love3)"
    private const val EMOJI_28 = "(normal4)"
    private const val EMOJI_29 = "(surprise4)"
    private const val EMOJI_30 = "(serious4)"
    private const val EMOJI_31 = "(love4)"
    private const val EMOJI_32 = "(shine4)"
    private const val EMOJI_33 = "(sweat4)"
    private const val EMOJI_34 = "(shame4)"
    private const val EMOJI_35 = "(sleep4)"
    private const val EMOJI_36 = "(heart)"
    private const val EMOJI_37 = "(teardrop)"
    private const val EMOJI_38 = "(star)"

    private val NAMES =
        arrayOf(
            EMOJI_1,
            EMOJI_2,
            EMOJI_3,
            EMOJI_4,
            EMOJI_5,
            EMOJI_6,
            EMOJI_7,
            EMOJI_8,
            EMOJI_9,
            EMOJI_10,
            EMOJI_11,
            EMOJI_12,
            EMOJI_13,
            EMOJI_14,
            EMOJI_15,
            EMOJI_16,
            EMOJI_17,
            EMOJI_18,
            EMOJI_19,
            EMOJI_20,
            EMOJI_21,
            EMOJI_22,
            EMOJI_23,
            EMOJI_24,
            EMOJI_25,
            EMOJI_26,
            EMOJI_27,
            EMOJI_28,
            EMOJI_29,
            EMOJI_30,
            EMOJI_31,
            EMOJI_32,
            EMOJI_33,
            EMOJI_34,
            EMOJI_35,
            EMOJI_36,
            EMOJI_37,
            EMOJI_38,
        )

    private const val HEAD = "<img class=\"_2sgsdWB\" width=\"24\" height=\"24\" src=\""
    private const val OFF = "\">"

    private val map = HashMap<String, String>()

    private val RESOURCE =
        arrayOf(
            "101.png",
            "102.png",
            "103.png",
            "104.png",
            "105.png",
            "106.png",
            "107.png",
            "108.png",
            "201.png",
            "202.png",
            "203.png",
            "204.png",
            "205.png",
            "206.png",
            "207.png",
            "208.png",
            "209.png",
            "301.png",
            "302.png",
            "303.png",
            "304.png",
            "305.png",
            "306.png",
            "307.png",
            "308.png",
            "309.png",
            "310.png",
            "401.png",
            "402.png",
            "403.png",
            "404.png",
            "405.png",
            "406.png",
            "407.png",
            "408.png",
            "501.png",
            "502.png",
            "503.png",
        )

    @JvmField
    var emojiDic: CharDicWithArr

    init {
        map.clear()
        map[EMOJI_1] = HEAD + "101.png" + OFF
        map[EMOJI_2] = HEAD + "102.png" + OFF
        map[EMOJI_3] = HEAD + "103.png" + OFF
        map[EMOJI_4] = HEAD + "104.png" + OFF
        map[EMOJI_5] = HEAD + "105.png" + OFF
        map[EMOJI_6] = HEAD + "106.png" + OFF
        map[EMOJI_7] = HEAD + "107.png" + OFF
        map[EMOJI_8] = HEAD + "108.png" + OFF
        map[EMOJI_9] = HEAD + "201.png" + OFF
        map[EMOJI_10] = HEAD + "202.png" + OFF
        map[EMOJI_11] = HEAD + "203.png" + OFF
        map[EMOJI_12] = HEAD + "204.png" + OFF
        map[EMOJI_13] = HEAD + "205.png" + OFF
        map[EMOJI_14] = HEAD + "206.png" + OFF
        map[EMOJI_15] = HEAD + "207.png" + OFF
        map[EMOJI_16] = HEAD + "208.png" + OFF
        map[EMOJI_17] = HEAD + "209.png" + OFF
        map[EMOJI_18] = HEAD + "301.png" + OFF
        map[EMOJI_19] = HEAD + "302.png" + OFF
        map[EMOJI_20] = HEAD + "303.png" + OFF
        map[EMOJI_21] = HEAD + "304.png" + OFF
        map[EMOJI_22] = HEAD + "305.png" + OFF
        map[EMOJI_23] = HEAD + "306.png" + OFF
        map[EMOJI_24] = HEAD + "307.png" + OFF
        map[EMOJI_25] = HEAD + "308.png" + OFF
        map[EMOJI_26] = HEAD + "309.png" + OFF
        map[EMOJI_27] = HEAD + "310.png" + OFF
        map[EMOJI_28] = HEAD + "401.png" + OFF
        map[EMOJI_29] = HEAD + "402.png" + OFF
        map[EMOJI_30] = HEAD + "403.png" + OFF
        map[EMOJI_31] = HEAD + "404.png" + OFF
        map[EMOJI_32] = HEAD + "405.png" + OFF
        map[EMOJI_33] = HEAD + "406.png" + OFF
        map[EMOJI_34] = HEAD + "407.png" + OFF
        map[EMOJI_35] = HEAD + "408.png" + OFF
        map[EMOJI_36] = HEAD + "501.png" + OFF
        map[EMOJI_37] = HEAD + "502.png" + OFF
        map[EMOJI_38] = HEAD + "503.png" + OFF
        emojiDic = CharDicWithArr(map.entries)
    }

    @JvmStatic
    fun hasEmoji(origin: String?): Boolean {
        Common.showLog("hasEmoji hasEmoji")
        if (TextUtils.isEmpty(origin)) {
            return false
        }
        return emojiDic.containsBy(origin!!)
    }

    @JvmStatic
    fun transform(origin: String?): String? {
        if (origin == null) {
            return null
        }
        val res = StringBuilder(origin)
        var range: RangeAndTarget?
        while (emojiDic.containsByReturnTarget(res).also { range = it } != null) {
            val matched = range!!
            res.replace(matched.start, matched.end, matched.target)
        }
        return res.toString()
    }

    class RangeAndTarget() {
        @JvmField
        var start = 0

        @JvmField
        var end = 0

        @JvmField
        var target: String? = null

        constructor(start: Int, end: Int, target: String?) : this() {
            this.start = start
            this.end = end
            this.target = target
        }
    }

    @JvmStatic
    fun replace(origin: String, emoji: String): String {
        val after = map[emoji]
        if (!TextUtils.isEmpty(after)) {
            return origin.replace(emoji, after!!)
        }
        return origin.replace(emoji, "")
    }

    @JvmStatic
    fun getEmojis(): List<EmojiItem> {
        val result = ArrayList<EmojiItem>()
        for (i in NAMES.indices) {
            result.add(EmojiItem(NAMES[i], RESOURCE[i]))
        }
        return result
    }

    class CharDicWithArr() {
        var root: Node = Node()

        constructor(list: List<String>) : this() {
            generateNodeByStringList(list)
        }

        constructor(list: Array<String>) : this() {
            generateNodeByStringList(list)
        }

        constructor(list: Set<Map.Entry<String, String>>) : this() {
            generateNodeByStringList(list)
        }

        fun generateNodeByStringList(list: List<String>) {
            for (f in list) {
                var ro = root
                for (c in f.toCharArray()) {
                    if (c.code >= arrLen) {
                        System.err.println(" 不是 ascii ")
                        break
                    }
                    if (ro.sons[c.code] == null) {
                        ro.sons[c.code] = Node()
                    }
                    ro = ro.sons[c.code]!!
                }
                ro.isEnd = true
            }
        }

        fun generateNodeByStringList(list: Array<String>) {
            for (f in list) {
                var ro = root
                for (c in f.toCharArray()) {
                    if (c.code >= arrLen) {
                        System.err.println(" 不是 ascii ")
                        break
                    }
                    if (ro.sons[c.code] == null) {
                        ro.sons[c.code] = Node()
                    }
                    ro = ro.sons[c.code]!!
                }
                ro.isEnd = true
            }
        }

        fun generateNodeByStringList(list: Set<Map.Entry<String, String>>) {
            for (kv in list) {
                val f = kv.key
                var ro = root
                for (c in f.toCharArray()) {
                    if (c.code >= arrLen) {
                        System.err.println(" 不是 ascii ")
                        break
                    }
                    if (ro.sons[c.code] == null) {
                        ro.sons[c.code] = Node()
                    }
                    ro = ro.sons[c.code]!!
                }
                ro.isEnd = true
                ro.forReplace = kv.value
            }
        }

        fun containsBy(s: String): Boolean {
            var i = 0
            var left: Int
            while (i < s.length) {
                left = i
                var ro = root
                while (left < s.length && s[left].code < arrLen) {
                    ro = ro.sons[s[left].code] ?: break
                    left++
                    if (ro.isEnd) {
                        return true
                    }
                }
                i++
            }
            return false
        }

        fun containsByReturnRange(s: CharSequence): IntArray? {
            var i = 0
            var left: Int
            while (i < s.length) {
                left = i
                var ro = root
                while (left < s.length && s[left].code < arrLen) {
                    ro = ro.sons[s[left].code] ?: break
                    left++
                    if (ro.isEnd) {
                        return intArrayOf(i, left)
                    }
                }
                i++
            }
            return null
        }

        fun containsByReturnTarget(s: CharSequence): RangeAndTarget? {
            var i = 0
            var left: Int
            while (i < s.length) {
                left = i
                var ro = root
                while (left < s.length && s[left].code < arrLen) {
                    ro = ro.sons[s[left].code] ?: break
                    left++
                    if (ro.isEnd) {
                        return RangeAndTarget(i, left, ro.forReplace)
                    }
                }
                i++
            }
            return null
        }

        private fun containsByWithPrefix(prefix: String, s: String): Boolean {
            var left = -1
            val length = prefix.length
            while (true) {
                left = s.indexOf(prefix, left + 1)
                if (left < 0) {
                    break
                }
                left += length
                var ro = root
                var cursor = left
                while (cursor < s.length && s[cursor].code < arrLen) {
                    ro = ro.sons[s[cursor].code] ?: break
                    cursor++
                    if (ro.isEnd) {
                        return true
                    }
                }
            }
            return false
        }

        class Node {
            @JvmField
            var sons: Array<Node?> = arrayOfNulls(arrLen)

            @JvmField
            var isEnd = false

            @JvmField
            var forReplace: String? = null

            @JvmField
            var length = 0
        }

        companion object {
            @JvmField
            var arrLen = 128

            @JvmField
            var dicList =
                arrayListOf(
                    "normal",
                    "surprise",
                    "serious",
                    "heaven",
                    "happy",
                    "excited",
                    "sing",
                    "cry",
                    "normal2",
                    "shame2",
                    "love2",
                    "interesting2",
                    "blush2",
                    "fire2",
                    "angry2",
                    "shine2",
                    "panic2",
                    "normal3",
                    "satisfaction3",
                    "surprise3",
                    "smile3",
                    "shock3",
                    "gaze3",
                    "wink3",
                    "happy3",
                    "excited3",
                    "love3",
                    "normal4",
                    "surprise4",
                    "serious4",
                    "love4",
                    "shine4",
                    "sweat4",
                    "shame4",
                    "sleep4",
                    "heart",
                    "teardrop",
                    "star",
                    "st你好ar",
                )

            @JvmStatic
            fun main(args: Array<String>) {
                字典树检测表情_测试一()
                字典树检测表情_测试二()
            }

            private fun 字典树检测表情_测试二() {
                println("================ 字典树检测表情_测试二 ")
                val dicListFix = ArrayList<String>()
                for (s in dicList) {
                    dicListFix.add("($s)")
                }
                val dic = CharDicWithArr(dicListFix)

                println(dic.containsBy("465456456(fsdf)5645").toString() + " , 预期 : false ")
                println(dic.containsBy("465456456(love4)9347457").toString() + " , 预期 : true ")
                println(dic.containsBy("4654564{5}6(lov)9347{heart}457").toString() + " , 预期 : false ")
                println(dic.containsBy("46545(lovvvv)64{5}6(love4)9347(heart)").toString() + " , 预期 : true ")
                println(dic.containsBy("46545(lovvvv)64{5}6(love4)9347(heart)457").toString() + " , 预期 : true ")
                println(dic.containsBy("").toString() + " , 预期 : false ")
                println(dic.containsBy("(love4)").toString() + " , 预期 : true ")
                println(dic.containsBy("(lov5756756757)").toString() + " , 预期 : false ")
            }

            private fun 字典树检测表情_测试一() {
                println("================ 字典树检测表情_测试一 ")
                val dic = CharDicWithArr(dicList)

                println(dic.containsByWithPrefix("(", "(love4)").toString() + " , 预期 : true ")
                println(dic.containsByWithPrefix("(", "465456456(fsdf)5645").toString() + " , 预期 : false ")
                println(dic.containsByWithPrefix("(", "465456456(love4)9347457").toString() + " , 预期 : true ")
                println(dic.containsByWithPrefix("{", "4654564{5}6(love4)9347{heart}457").toString() + " , 预期 : true ")
                println(dic.containsByWithPrefix("{", "46545(lovvvv)64{5}6(love4)9347(heart)").toString() + " , 预期 : false ")
                println(dic.containsByWithPrefix("(", "46545(lovvvv)64{5}6(love4)9347(heart)457").toString() + " , 预期 : true ")
                println(dic.containsByWithPrefix("(", "").toString() + " , 预期 : false ")
                println(dic.containsByWithPrefix("(", "(").toString() + " , 预期 : false ")
                println(dic.containsByWithPrefix("(", "(love4)").toString() + " , 预期 : true ")
                println(dic.containsByWithPrefix("(", "(lov5756756757)").toString() + " , 预期 : false ")
            }
        }
    }
}
