package ceui.lisa.utils

import android.text.TextUtils
import com.bumptech.glide.load.model.GlideUrl
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.UserBean

object GlideUtil {

    @JvmField
    val DEFAULT_HEAD_IMAGE = "https://s.pximg.net/common/images/no_profile.png"

    @JvmStatic
    fun getMediumImg(illustsBean: IllustsBean?): GlideUrl {
        return GlideUrlChild(illustsBean!!.image_urls.medium)
    }

    @JvmStatic
    fun getUrl(url: String?): GlideUrl {
        return GlideUrlChild(url)
    }

    @JvmStatic
    fun getLargeImage(illustsBean: IllustsBean?): GlideUrl {
        return GlideUrlChild(illustsBean!!.image_urls.large)
    }

    @JvmStatic
    fun getHead(userBean: UserBean?): GlideUrl? {
        if (userBean == null) {
            return null
        }
        if (userBean.profile_image_urls == null) {
            return null
        }
        val image = userBean.profile_image_urls.maxImage
        return if (TextUtils.equals(image, DEFAULT_HEAD_IMAGE)) {
            GlideUrlChild(image)
        } else {
            GlideUrlChild(userBean.profile_image_urls.maxImage)
        }
    }

    @JvmStatic
    fun getSquare(illustsBean: IllustsBean?): GlideUrl {
        return GlideUrlChild(illustsBean!!.image_urls.square_medium)
    }

    @JvmStatic
    fun getLargeImage(illustsBean: IllustsBean?, i: Int): GlideUrl {
        Common.showLog("getLargeImage 11 ")
        return if (illustsBean!!.page_count == 1) {
            getLargeImage(illustsBean)
        } else {
            GlideUrlChild(illustsBean.meta_pages[i].image_urls.large)
        }
    }

    @JvmStatic
    fun getOriginalImage(illustsBean: IllustsBean?, i: Int): GlideUrl {
        return if (illustsBean!!.page_count == 1) {
            GlideUrlChild(illustsBean.meta_single_page.original_image_url)
        } else {
            GlideUrlChild(illustsBean.meta_pages[i].image_urls.original)
        }
    }
}
