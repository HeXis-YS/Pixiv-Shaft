package ceui.lisa.utils

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import ceui.lisa.http.PixivHeaders
import java.util.HashMap

class GlideUrlChild : GlideUrl {

    constructor(url: String?) : this(url, formatHeader())

    constructor(url: String?, headers: Headers) : super(url!!, headers)

    companion object {
        @JvmStatic
        private fun formatHeader(): Headers {
            val pixivHeaders = PixivHeaders()
            val hashMap = HashMap<String, String>()
            hashMap[Params.MAP_KEY_SMALL] = Params.IMAGE_REFERER
            hashMap["x-client-time"] = pixivHeaders.xClientTime
            hashMap["x-client-hash"] = pixivHeaders.xClientHash
            hashMap[Params.USER_AGENT] = Params.PHONE_MODEL
            return Headers { hashMap }
        }
    }
}
