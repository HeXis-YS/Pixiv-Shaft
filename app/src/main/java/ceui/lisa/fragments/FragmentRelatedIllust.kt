package ceui.lisa.fragments

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.ViewDataBinding
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.feature.FeatureEntity
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.repo.RelatedIllustRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate

class FragmentRelatedIllust : NetListFragment<FragmentBaseListBinding, ListIllust, IllustsBean>() {
    private var illustID = 0
    private var title = ""

    companion object {
        @JvmStatic
        fun newInstance(id: Int, title: String?): FragmentRelatedIllust {
            val args = Bundle()
            args.putInt(Params.ILLUST_ID, id)
            args.putString(Params.ILLUST_TITLE, title)
            val fragment = FragmentRelatedIllust()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        illustID = bundle.getInt(Params.ILLUST_ID)
        title = bundle.getString(Params.ILLUST_TITLE).orEmpty()
    }

    override fun initView() {
        super.initView()
        baseBind.toolbar.inflateMenu(R.menu.local_save)
        baseBind.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_bookmark) {
                val entity = FeatureEntity()
                entity.uuid = illustID.toString() + "相关作品"
                entity.dataType = "相关作品"
                entity.illustID = illustID
                entity.illustTitle = title
                entity.illustJson = Common.cutToJson(allItems)
                entity.dateTime = System.currentTimeMillis()
                PixivOperate.insertFeature(entity)
                Common.showToast("已收藏到精华")
                true
            } else {
                false
            }
        }
    }

    override fun initRecyclerView() {
        staggerRecyclerView()
    }

    override fun adapter(): BaseAdapter<*, out ViewDataBinding> = IAdapter(allItems, mContext)

    override fun repository(): RemoteRepo<ListIllust> = RelatedIllustRepo(illustID)

    override fun getToolbarTitle(): String = title + getString(R.string.string_231)
}
