package ceui.lisa.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import ceui.lisa.activities.SearchActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.TagAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyTagGridBinding
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ListTrendingtag
import ceui.lisa.repo.HotTagRepo
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.TagItemDecoration

class FragmentHotTag : NetListFragment<FragmentBaseListBinding, ListTrendingtag, ListTrendingtag.TrendTagsBean>() {
    private var contentType = ""

    companion object {
        @JvmStatic
        fun newInstance(type: String?): FragmentHotTag {
            val args = Bundle()
            args.putString(Params.CONTENT_TYPE, type)
            val fragment = FragmentHotTag()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        contentType = bundle.getString(Params.CONTENT_TYPE).orEmpty()
    }

    override fun initRecyclerView() {
        val manager = GridLayoutManager(mContext, 3)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 3 else 1
            }
        }
        baseBind.recyclerView.layoutManager = manager
        baseBind.recyclerView.addItemDecoration(TagItemDecoration(3, DensityUtil.dp2px(1.0f), false))
    }

    override fun repository(): RemoteRepo<ListTrendingtag> = HotTagRepo(contentType)

    override fun adapter(): BaseAdapter<ListTrendingtag.TrendTagsBean, RecyTagGridBinding> {
        return TagAdapter(allItems, mContext).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                val intent = Intent(mContext, SearchActivity::class.java)
                intent.putExtra(Params.KEY_WORD, allItems[position].getTag())
                intent.putExtra(Params.INDEX, if (Params.TYPE_ILLUST == contentType) 0 else 1)
                startActivity(intent)
            }
        })
    }

    override fun showToolbar(): Boolean = false
}
