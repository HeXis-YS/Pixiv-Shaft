package ceui.lisa.fragments

import android.os.Bundle
import android.view.MenuItem
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyIllustStaggerBinding
import ceui.lisa.feature.FeatureEntity
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.repo.UserMangaRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate

class FragmentUserManga : NetListFragment<FragmentBaseListBinding, ListIllust, IllustsBean>() {
    private var userID = 0
    private var showToolbar = false

    companion object {
        @JvmStatic
        fun newInstance(userID: Int, paramShowToolbar: Boolean): FragmentUserManga {
            val args = Bundle()
            args.putInt(Params.USER_ID, userID)
            args.putBoolean(Params.FLAG, paramShowToolbar)
            val fragment = FragmentUserManga()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        userID = bundle.getInt(Params.USER_ID)
        showToolbar = bundle.getBoolean(Params.FLAG)
    }

    override fun initView() {
        super.initView()
        baseBind.toolbar.inflateMenu(R.menu.local_save)
        baseBind.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_bookmark) {
                val entity = FeatureEntity()
                entity.uuid = userID.toString() + "漫画作品"
                entity.isShowToolbar = showToolbar
                entity.dataType = "漫画作品"
                entity.illustJson = Common.cutToJson(allItems)
                entity.userID = userID
                entity.dateTime = System.currentTimeMillis()
                PixivOperate.insertFeature(entity)
                Common.showToast("已收藏到精华")
                true
            } else {
                false
            }
        }
    }

    override fun repository(): RemoteRepo<ListIllust> = UserMangaRepo(userID)

    override fun adapter(): BaseAdapter<IllustsBean, RecyIllustStaggerBinding> =
        IAdapter(allItems, mContext)

    override fun showToolbar(): Boolean = showToolbar

    override fun getToolbarTitle(): String =
        if (showToolbar) getString(R.string.string_233) else super.getToolbarTitle()

    override fun initRecyclerView() {
        staggerRecyclerView()
    }
}
