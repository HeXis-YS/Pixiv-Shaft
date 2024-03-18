package ceui.lisa.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import ceui.lisa.R
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.databinding.FragmentAboutBinding
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog.MenuDialogBuilder
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

        run {
            baseBind.faq.setOnClickListener{
                val intent = Intent(mContext, TemplateActivity::class.java)
                intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, "Markdown")
                intent.putExtra(Params.URL, "FAQ.md")
                startActivity(intent)
            }
        }

        run {
            baseBind.pixivProblem.setOnClickListener {
                val intent = Intent(mContext, TemplateActivity::class.java)
                intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, "网页链接")
                intent.putExtra(Params.URL, "https://app.pixiv.help/hc/zh-cn")
                intent.putExtra(Params.TITLE, getString(R.string.pixiv_problem))
                startActivity(intent)
            }
            baseBind.pixivUseDetail.setOnClickListener {
                val intent = Intent(mContext, TemplateActivity::class.java)
                intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, "网页链接")
                intent.putExtra(Params.URL, "https://www.pixiv.net/terms/?page=term&appname=pixiv_ios")
                intent.putExtra(Params.TITLE, getString(R.string.pixiv_use_detail))
                startActivity(intent)
            }
            baseBind.pixivPrivacy.setOnClickListener {
                val intent = Intent(mContext, TemplateActivity::class.java)
                intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, "网页链接")
                intent.putExtra(Params.URL,"https://www.pixiv.net/terms/?page=privacy&appname=pixiv_ios")
                intent.putExtra(Params.TITLE, getString(R.string.privacy))
                startActivity(intent)
            }
        }

        run {
            baseBind.dontCatchMe.setOnClickListener {
                Common.createDialog(context)
            }

            baseBind.projectWebsite.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse("https://github.com/CeuiLiSA/Pixiv-Shaft")
                startActivity(intent)
            }
        }
    }
}
