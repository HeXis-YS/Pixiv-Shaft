package ceui.lisa.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.BookedTagAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyBookTagBinding
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ListTag
import ceui.lisa.models.TagsBean
import ceui.lisa.repo.BookedTagRepo
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.LinearItemDecoration

class FragmentBookedTag : NetListFragment<FragmentBaseListBinding, ListTag, TagsBean>() {
    private var starType = ""
    private var type = 0

    companion object {
        @JvmStatic
        fun newInstance(starType: String?): FragmentBookedTag {
            val args = Bundle()
            args.putString(Params.STAR_TYPE, starType)
            val fragment = FragmentBookedTag()
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun newInstance(type: Int, starType: String?): FragmentBookedTag {
            val args = Bundle()
            args.putInt(Params.DATA_TYPE, type)
            args.putString(Params.STAR_TYPE, starType)
            val fragment = FragmentBookedTag()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        starType = bundle.getString(Params.STAR_TYPE).orEmpty()
        type = bundle.getInt(Params.DATA_TYPE, 0)
    }

    override fun repository(): RemoteRepo<ListTag> = BookedTagRepo(type, starType)

    override fun adapter(): BaseAdapter<TagsBean, RecyBookTagBinding> {
        return BookedTagAdapter(allItems, mContext, false).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                val intent = Intent(if (type == 1) Params.FILTER_NOVEL else Params.FILTER_ILLUST)
                intent.putExtra(Params.CONTENT, allItems[position].name)
                intent.putExtra(Params.STAR_TYPE, starType)
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent)
                mActivity.finish()
            }
        })
    }

    override fun getToolbarTitle(): String = getString(R.string.string_244)

    override fun initRecyclerView() {
        baseBind.recyclerView.layoutManager = LinearLayoutManager(mContext)
        baseBind.recyclerView.addItemDecoration(LinearItemDecoration(DensityUtil.dp2px(16.0f)))
    }

    override fun onFirstLoaded(tagsBeans: List<TagsBean>) {
        val all = TagsBean()
        all.count = -1
        all.name = ""
        allItems.add(0, all)

        val unSeparated = TagsBean()
        unSeparated.count = -1
        unSeparated.name = "未分類"
        allItems.add(0, unSeparated)
    }
}
