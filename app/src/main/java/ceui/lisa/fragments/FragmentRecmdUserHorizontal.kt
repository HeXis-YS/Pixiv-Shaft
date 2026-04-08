package ceui.lisa.fragments

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.UActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.UserHAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.IllustRecmdEntity
import ceui.lisa.databinding.FragmentUserHorizontalBinding
import ceui.lisa.databinding.RecyUserPreviewHorizontalBinding
import ceui.lisa.http.NullCtrl
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ListUser
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.repo.RecmdUserRepo
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.LinearItemHorizontalDecoration
import jp.wasabeef.recyclerview.animators.BaseItemAnimator
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator

class FragmentRecmdUserHorizontal :
    NetListFragment<FragmentUserHorizontalBinding, ListUser, UserPreviewsBean>() {

    override fun initLayout() {
        mLayoutID = R.layout.fragment_user_horizontal
    }

    override fun adapter(): BaseAdapter<UserPreviewsBean, RecyUserPreviewHorizontalBinding> {
        return UserHAdapter(allItems, mContext).setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                val intent = Intent(mContext, UActivity::class.java)
                intent.putExtra(Params.USER_ID, allItems[position].user.id)
                startActivity(intent)
            }
        })
    }

    override fun repository(): BaseRepo = RecmdUserRepo(true)

    override fun animation(): BaseItemAnimator {
        val fade = FadeInLeftAnimator()
        fade.addDuration = animateDuration
        fade.removeDuration = animateDuration
        fade.moveDuration = animateDuration
        fade.changeDuration = animateDuration
        return fade
    }

    override fun onFirstLoaded(userPreviewsBeans: List<UserPreviewsBean>) {
        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMore(false)
    }

    override fun initRecyclerView() {
        baseBind.recyclerView.addItemDecoration(LinearItemHorizontalDecoration(DensityUtil.dp2px(12.0f)))
        val manager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        baseBind.recyclerView.layoutManager = manager
        baseBind.recyclerView.setHasFixedSize(true)
    }

    override fun showDataBase() {
        RxRun.runOn(object : RxRunnable<List<IllustsBean>>() {
            override fun execute(): List<IllustsBean> {
                val entities: List<IllustRecmdEntity> = AppDatabase.recmdDao(mContext).getAll()
                Thread.sleep(100)
                val temp = ArrayList<IllustsBean>()
                for (entity in entities) {
                    val illustsBean = Shaft.sGson.fromJson(entity.illustJson, IllustsBean::class.java)
                    temp.add(illustsBean)
                }
                return temp
            }
        }, object : NullCtrl<List<IllustsBean>>() {
            override fun success(illustsBeans: List<IllustsBean>) {
                for (illustsBean in illustsBeans) {
                    val userPreviewsBean = UserPreviewsBean()
                    userPreviewsBean.user = illustsBean.user
                    allItems.add(userPreviewsBean)
                }
                mAdapter.notifyItemRangeInserted(mAdapter.headerSize(), allItems.size)
            }

            override fun must(isSuccess: Boolean) {
                baseBind.refreshLayout.finishRefresh(isSuccess)
                baseBind.refreshLayout.setEnableRefresh(false)
                baseBind.refreshLayout.setEnableLoadMore(false)
            }
        })
    }
}
