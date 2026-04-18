package ceui.lisa.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.NAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyNovelBinding
import ceui.lisa.model.ListNovel
import ceui.lisa.models.NovelBean
import ceui.lisa.notification.BaseReceiver
import ceui.lisa.notification.FilterReceiver
import ceui.lisa.repo.LikeNovelRepo
import ceui.lisa.utils.Params

class FragmentLikeNovel : NetListFragment<FragmentBaseListBinding, ListNovel, NovelBean>() {
    private var userID = 0
    private var starType: String? = null
    private var tag = ""
    private var showToolbar = false
    private var filterReceiver: BroadcastReceiver? = null

    companion object {
        @JvmStatic
        fun newInstance(userID: Int, starType: String?, paramShowToolbar: Boolean): FragmentLikeNovel {
            val args = Bundle()
            args.putInt(Params.USER_ID, userID)
            args.putString(Params.STAR_TYPE, starType)
            args.putBoolean(Params.FLAG, paramShowToolbar)
            val fragment = FragmentLikeNovel()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        userID = bundle.getInt(Params.USER_ID)
        starType = bundle.getString(Params.STAR_TYPE)
        showToolbar = bundle.getBoolean(Params.FLAG)
    }

    override fun repository(): RemoteRepo<ListNovel> = LikeNovelRepo(userID, starType, tag)

    override fun adapter(): BaseAdapter<NovelBean, RecyNovelBinding> = NAdapter(allItems, mContext)

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
                        (mRemoteRepo as LikeNovelRepo).tag = tag
                        autoRefresh()
                    }
                }
            }
        })
        intentFilter.addAction(Params.FILTER_NOVEL)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(filterReceiver!!, intentFilter)
    }

    override fun showToolbar(): Boolean = showToolbar

    override fun getToolbarTitle(): String =
        if (showToolbar) getString(R.string.string_192) else super.getToolbarTitle()
}
