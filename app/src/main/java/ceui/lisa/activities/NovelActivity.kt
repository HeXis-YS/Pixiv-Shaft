package ceui.lisa.activities

import android.os.Bundle
import android.os.Handler
import android.view.View
import ceui.lisa.R
import ceui.lisa.databinding.ActivityNovelBinding
import ceui.lisa.models.NovelDetail
import ceui.lisa.page.PageLoader
import ceui.lisa.page.PageView
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params

class NovelActivity : BaseActivity<ActivityNovelBinding>() {
    private var mNovelDetail: NovelDetail? = null
    private var mPageLoader: PageLoader? = null

    override fun initBundle(bundle: Bundle) {
        mNovelDetail = bundle.getSerializable(Params.NOVEL_DETAIL) as NovelDetail?
    }

    override fun initLayout(): Int = R.layout.activity_novel

    override fun initView() {
        baseBind.pageView.setTouchListener(object : PageView.TouchListener {
            override fun onTouch(): Boolean = true

            override fun center() {
            }

            override fun allowPrePage(): Boolean = true

            override fun prePage() {
            }

            override fun allowNextPage(): Boolean = true

            override fun nextPage() {
            }

            override fun cancel() {
            }
        })
        mPageLoader = baseBind.pageView.getPageLoader(mNovelDetail)
        mPageLoader!!.init()
        mPageLoader!!.dataInitSuccess()
        Common.showLog("drawContent initData ")

        Handler().postDelayed({
            baseBind.pageView.drawCurPage(false)
        }, 500L)
    }

    override fun initData() {
    }

    private fun showOrHideOperation() {
        if (baseBind.operateLl.visibility == View.VISIBLE) {
            baseBind.operateLl.visibility = View.GONE
        } else {
            baseBind.operateLl.visibility = View.VISIBLE
        }
    }
}
