package ceui.lisa.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapterWithStar
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyIllustStaggerBinding
import ceui.lisa.feature.FeatureEntity
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.notification.BaseReceiver
import ceui.lisa.notification.FilterReceiver
import ceui.lisa.repo.LikeIllustRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate

class FragmentLikeIllust : NetListFragment<FragmentBaseListBinding, ListIllust, IllustsBean>() {
    private var userID = 0
    private var starType = ""
    private var tag = ""
    private var showToolbar = false
    private var filterReceiver: BroadcastReceiver? = null

    companion object {
        @JvmStatic
        fun newInstance(userID: Int, starType: String): FragmentLikeIllust {
            return newInstance(userID, starType, false)
        }

        @JvmStatic
        fun newInstance(
            userID: Int,
            starType: String,
            paramShowToolbar: Boolean,
        ): FragmentLikeIllust {
            val args = Bundle()
            args.putInt(Params.USER_ID, userID)
            args.putString(Params.STAR_TYPE, starType)
            args.putBoolean(Params.FLAG, paramShowToolbar)
            val fragment = FragmentLikeIllust()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView() {
        super.initView()
        baseBind.toolbar.inflateMenu(R.menu.local_save)
        baseBind.toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.action_bookmark) {
                    val entity = FeatureEntity()
                    entity.uuid = "${userID}插画/漫画收藏"
                    entity.isShowToolbar = showToolbar
                    entity.dataType = "插画/漫画收藏"
                    entity.illustJson = Common.cutToJson(allItems)
                    entity.userID = userID
                    entity.starType = starType
                    entity.dateTime = System.currentTimeMillis()
                    PixivOperate.insertFeature(entity)
                    Common.showToast("已收藏到精华")
                    return true
                }
                return false
            }
        })
    }

    override fun initBundle(bundle: Bundle) {
        userID = bundle.getInt(Params.USER_ID)
        starType = bundle.getString(Params.STAR_TYPE).orEmpty()
        showToolbar = bundle.getBoolean(Params.FLAG)
    }

    override fun repository(): RemoteRepo<ListIllust> = LikeIllustRepo(userID, starType, tag)

    override fun adapter(): BaseAdapter<IllustsBean, RecyIllustStaggerBinding> {
        val isOwnPage = Shaft.sUserModel.user.userId == userID
        return IAdapterWithStar(allItems, mContext).setHideStarIcon(
            isOwnPage && Shaft.sSettings.isHideStarButtonAtMyCollection,
        )
    }

    override fun onAdapterPrepared() {
        super.onAdapterPrepared()
        val intentFilter = IntentFilter()
        filterReceiver = FilterReceiver(object : BaseReceiver.CallBack {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras
                if (bundle != null) {
                    val type = bundle.getString(Params.STAR_TYPE)
                    if (starType == type) {
                        tag = bundle.getString(Params.CONTENT).orEmpty()
                        (mRemoteRepo as LikeIllustRepo).tag = tag
                        baseBind.refreshLayout.autoRefresh()
                    }
                }
            }
        })
        intentFilter.addAction(Params.FILTER_ILLUST)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(filterReceiver!!, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (filterReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(filterReceiver!!)
        }
    }

    override fun initRecyclerView() {
        staggerRecyclerView()
    }

    override fun showToolbar(): Boolean = showToolbar

    override fun getToolbarTitle(): String {
        return if (showToolbar) getString(R.string.string_164) else super.getToolbarTitle()
    }
}
