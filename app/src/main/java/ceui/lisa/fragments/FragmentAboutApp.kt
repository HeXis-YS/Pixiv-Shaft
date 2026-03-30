package ceui.lisa.fragments

import android.content.Intent
import android.net.Uri
import ceui.lisa.R
import ceui.lisa.databinding.FragmentAboutBinding
import ceui.lisa.utils.Common
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class FragmentAboutApp : SwipeFragment<FragmentAboutBinding>() {

    override fun initLayout() {
        mLayoutID = R.layout.fragment_about
    }

    override fun getSmartRefreshLayout(): SmartRefreshLayout {
        return baseBind.refreshLayout
    }

    override fun initData() {
        baseBind.toolbar.setNavigationOnClickListener { mActivity.finish() }

        baseBind.appVersion.text = "%s (%s) "
            .format(Common.getAppVersionName(mContext), Common.getAppVersionCode(mContext))

        baseBind.faq.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/CeuiLiSA/Pixiv-Shaft/blob/classic/FAQ.md")))
        }

        baseBind.projectWebsite.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/CeuiLiSA/Pixiv-Shaft")))
        }
    }
}
