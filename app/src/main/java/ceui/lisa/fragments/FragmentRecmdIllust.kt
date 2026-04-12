package ceui.lisa.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ceui.lisa.refresh.header.FalsifyFooter
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapterWithHeadView
import ceui.lisa.core.RemoteRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.IllustRecmdEntity
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyIllustStaggerBinding
import ceui.lisa.helper.IllustNovelFilter
import ceui.lisa.helper.StaggeredManager
import ceui.lisa.http.NullCtrl
import ceui.lisa.model.ListIllust
import ceui.lisa.model.RecmdIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.notification.BaseReceiver
import ceui.lisa.notification.CallBackReceiver
import ceui.lisa.repo.RecmdIllustRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.SpacesItemWithHeadDecoration
import ceui.lisa.viewmodel.BaseModel
import ceui.lisa.viewmodel.RecmdModel
import ceui.loxia.ObjectPool

class FragmentRecmdIllust : NetListFragment<FragmentBaseListBinding, RecmdIllust, IllustsBean>() {
    private var dataType = ""
    private var localData: List<IllustRecmdEntity>? = null
    private var relatedReceiver: BroadcastReceiver? = null

    companion object {
        @JvmStatic
        fun newInstance(dataType: String): FragmentRecmdIllust {
            val args = Bundle()
            args.putString(Params.DATA_TYPE, dataType)
            val fragment = FragmentRecmdIllust()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        dataType = bundle.getString(Params.DATA_TYPE).orEmpty()
    }

    override fun modelClass(): Class<out BaseModel<IllustsBean>> = RecmdModel::class.java

    override fun repository(): RemoteRepo<ListIllust> = RecmdIllustRepo(dataType)

    override fun adapter(): BaseAdapter<IllustsBean, RecyIllustStaggerBinding> {
        return IAdapterWithHeadView(allItems, mContext, dataType)
            .setShowRelated(Shaft.sSettings.isShowRelatedWhenStar())
    }

    override fun onAdapterPrepared() {
        super.onAdapterPrepared()
        val intentFilter = IntentFilter()
        relatedReceiver = CallBackReceiver(object : BaseReceiver.CallBack {
            override fun onReceive(context: Context, intent: Intent) {
                val bundle = intent.extras ?: return
                val index = bundle.getInt(Params.INDEX)
                @Suppress("DEPRECATION")
                val listIllust = bundle.getSerializable(Params.CONTENT) as? ListIllust ?: return
                val relatedList = listIllust.list
                if (Common.isEmpty(relatedList) || !isAdded) {
                    return
                }
                val temp = ArrayList<IllustsBean>()
                for (i in relatedList.indices) {
                    relatedList[i].isRelated = true
                    if (i < 5) {
                        temp.add(relatedList[i])
                    } else {
                        break
                    }
                }
                if (!Common.isEmpty(temp) && index < allItems.size) {
                    mModel.load(temp, index)
                    mAdapter.notifyItemRangeInserted(index + 1, temp.size)
                    mAdapter.notifyItemRangeChanged(index + 1, allItems.size - index - 1)
                }
            }
        })
        intentFilter.addAction(Params.FRAGMENT_ADD_RELATED_DATA)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(relatedReceiver!!, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (relatedReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(relatedReceiver!!)
        }
    }

    override fun initRecyclerView() {
        val layoutManager = StaggeredManager(
            Shaft.sSettings.lineCount,
            StaggeredGridLayoutManager.VERTICAL,
        )
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        baseBind.recyclerView.layoutManager = layoutManager
        baseBind.recyclerView.addItemDecoration(
            SpacesItemWithHeadDecoration(DensityUtil.dp2px(8.0f)),
        )
    }

    override fun getToolbarTitle(): String = getString(R.string.recommend) + dataType

    override fun showToolbar(): Boolean = getString(R.string.type_manga) == dataType

    override fun onFirstLoaded(illustsBeans: List<IllustsBean>) {
        val rankList = (mModel as RecmdModel).getRankList() as MutableList<IllustsBean>
        rankList.clear()
        RxRun.runOn(object : RxRunnable<Void>() {
            override fun execute(): Void {
                if (allItems.size >= 20) {
                    for (i in 0 until 20) {
                        insertViewHistory(allItems[i])
                    }
                } else {
                    for (i in allItems.indices) {
                        insertViewHistory(allItems[i])
                    }
                }
                @Suppress("NULL_FOR_NONNULL_TYPE")
                return null as Void
            }
        }, TryCatchObserverImpl())
        val response = mResponse ?: return
        response.ranking_illusts?.forEach { illustsBean ->
            ObjectPool.updateIllust(illustsBean)
        }
        rankList.addAll(response.ranking_illusts.orEmpty())
        (mAdapter as IAdapterWithHeadView).setHeadData(rankList)
        mModel.tidyAppViewModel(rankList)
    }

    private fun insertViewHistory(illustsBean: IllustsBean) {
        val illustRecmdEntity = IllustRecmdEntity()
        illustRecmdEntity.illustID = illustsBean.id
        illustRecmdEntity.illustJson = Shaft.sGson.toJson(illustsBean)
        illustRecmdEntity.time = System.currentTimeMillis()
        AppDatabase.recmdDao(Shaft.getContext()).insert(illustRecmdEntity)
    }

    override fun showDataBase() {
        if (Common.isEmpty(localData)) {
            return
        }
        RxRun.runOn(object : RxRunnable<List<IllustsBean>>() {
            override fun execute(): List<IllustsBean> {
                Thread.sleep(100)
                val temp = ArrayList<IllustsBean>()
                for (entity in localData.orEmpty()) {
                    val illustsBean = Shaft.sGson.fromJson(entity.illustJson, IllustsBean::class.java)
                    if (!IllustNovelFilter.judge(illustsBean)) {
                        temp.add(illustsBean)
                    }
                }
                return temp
            }
        }, object : NullCtrl<List<IllustsBean>>() {
            override fun success(illustsBeans: List<IllustsBean>) {
                allItems.addAll(illustsBeans)
                illustsBeans.forEach { illustsBean ->
                    ObjectPool.updateIllust(illustsBean)
                }
                val rankList = (mModel as RecmdModel).getRankList() as MutableList<IllustsBean>
                rankList.addAll(illustsBeans)
                mModel.tidyAppViewModel(illustsBeans)
                (mAdapter as IAdapterWithHeadView).setHeadData(rankList)
                mAdapter.notifyItemRangeInserted(mAdapter.headerSize(), allItems.size)
            }

            override fun must(isSuccess: Boolean) {
                baseBind.refreshLayout.finishRefresh(isSuccess)
                baseBind.refreshLayout.setRefreshFooter(FalsifyFooter(mContext))
            }
        })
    }
}
