package ceui.lisa.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.R
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.adapters.ArticleAdapter
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyArticalBinding
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ListArticle
import ceui.lisa.models.SpotlightArticlesBean
import ceui.lisa.repo.PivisionRepo
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.LinearItemDecoration

class FragmentPivision : NetListFragment<FragmentBaseListBinding, ListArticle, SpotlightArticlesBean>() {
    private var dataType: String? = null

    companion object {
        @JvmStatic
        fun newInstance(dataType: String?): FragmentPivision {
            val args = Bundle()
            args.putString(Params.DATA_TYPE, dataType)
            val fragment = FragmentPivision()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        dataType = bundle.getString(Params.DATA_TYPE)
    }

    override fun getToolbarTitle(): String = getString(R.string.pixiv_special)

    override fun repository(): RemoteRepo<ListArticle> {
        return object : PivisionRepo(dataType, false) {
            override fun localData(): Boolean = false
        }
    }

    override fun adapter(): BaseAdapter<SpotlightArticlesBean, RecyArticalBinding> {
        return ArticleAdapter(allItems, mContext).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                TemplateActivity.startWeb(
                    mContext,
                    getString(R.string.pixiv_special),
                    allItems[position].article_url,
                    true
                )
            }
        })
    }

    override fun showToolbar(): Boolean = false

    override fun initRecyclerView() {
        mRecyclerView.layoutManager = LinearLayoutManager(mContext)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addItemDecoration(LinearItemDecoration(DensityUtil.dp2px(16.0f)))
    }
}
