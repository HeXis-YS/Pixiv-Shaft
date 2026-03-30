package ceui.lisa.utils

import android.util.Base64
import java.nio.charset.StandardCharsets

class Base64Util private constructor() {
    companion object {
        @JvmStatic
        fun encode(oldWord: String?): String {
            if (oldWord.isNullOrEmpty()) {
                return ""
            }
            return Base64.encodeToString(oldWord.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        }

        @JvmStatic
        fun decode(encodeWord: String?): String {
            if (encodeWord.isNullOrEmpty()) {
                return ""
            }
            return String(Base64.decode(encodeWord, Base64.NO_WRAP), StandardCharsets.UTF_8)
        }
    }
}
