package ceui.lisa.fragments

import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.viewbinding.ViewBinding
import androidx.fragment.app.FragmentTransaction
import ceui.lisa.R
import ceui.lisa.activities.MainActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.IAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.IllustRecmdEntity
import ceui.lisa.databinding.FragmentNewRightBinding
import ceui.lisa.http.NullCtrl
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.repo.RightRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.view.OnCheckChangeListener
import ceui.lisa.viewmodel.BaseModel
import ceui.lisa.viewmodel.DynamicIllustModel

class FragmentRight : NetListFragment<FragmentNewRightBinding, ListIllust, IllustsBean>() {
    private var headerFragment: FragmentRecmdUserHorizontal? = null
    private var restrict = Params.TYPE_ALL

    override fun initLayout() {
        mLayoutID = R.layout.fragment_new_right
    }

    override fun modelClass(): Class<out BaseModel<*>> = DynamicIllustModel::class.java

    override fun adapter(): BaseAdapter<*, out ViewBinding> = IAdapter(allItems, mContext)

    override fun initView() {
        super.initView()

        val headParams: ViewGroup.LayoutParams = baseBind.head.layoutParams
        headParams.height = Shaft.statusHeight
        baseBind.head.layoutParams = headParams

        baseBind.toolbar.inflateMenu(R.menu.fragment_left)
        baseBind.toolbar.setNavigationOnClickListener {
            if (mActivity is MainActivity) {
                (mActivity as MainActivity).getDrawer().openDrawer(GravityCompat.START, true)
            }
        }
        baseBind.toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.action_search) {
                    TemplateActivity.startSearch(mContext)
                    return true
                }
                return false
            }
        })
        baseBind.seeMore.setOnClickListener {
            val fragment = headerFragment
            if (fragment != null && fragment.allItems != null && fragment.allItems.size > 0) {
                TemplateActivity.startRecmdUser(
                    mContext,
                    fragment.allItems,
                    fragment.mRemoteRepo.getNextUrl(),
                )
            } else {
                TemplateActivity.startRecmdUser(mContext)
            }
        }
        baseBind.glareLayout.setListener(object : OnCheckChangeListener {
            private val types = arrayOf(Params.TYPE_ALL, Params.TYPE_PUBLIC, Params.TYPE_PRIVATE)

            override fun onSelect(index: Int, view: View) {
                Common.showLog("glareLayout onSelect $index")
                if (index < types.size) {
                    restrict = types[index]
                }
                (mRemoteRepo as RightRepo).restrict = restrict
                forceRefresh()
            }

            override fun onReselect(index: Int, view: View) {
                Common.showLog("glareLayout onReselect $index")
                forceRefresh()
            }
        })
        baseBind.dynamicTitleLayout.setOnClickListener {
            scrollToTop()
        }
    }

    override fun repository(): BaseRepo = RightRepo(restrict)

    override fun initRecyclerView() {
        staggerRecyclerView()
    }

    override fun lazyData() {
        super.lazyData()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        val recmdUser = FragmentRecmdUserHorizontal()
        headerFragment = recmdUser
        transaction.add(R.id.user_recmd_fragment, recmdUser, "FragmentRecmdUserHorizontal")
        transaction.commitNowAllowingStateLoss()
        baseBind.refreshLayout.autoRefresh()
    }

    override fun autoRefresh(): Boolean = false

    override fun showDataBase() {
        RxRun.runOn(object : RxRunnable<List<IllustsBean>>() {
            override fun execute(): List<IllustsBean> {
                Thread.sleep(100)
                val entities: List<IllustRecmdEntity> = AppDatabase.recmdDao(mContext).getAll()
                val temp = ArrayList<IllustsBean>()
                for (entity in entities) {
                    val illustsBean = Shaft.sGson.fromJson(entity.illustJson, IllustsBean::class.java)
                    temp.add(illustsBean)
                }
                return temp
            }
        }, object : NullCtrl<List<IllustsBean>>() {
            override fun success(illustsBeans: List<IllustsBean>) {
                allItems.addAll(illustsBeans)
                mAdapter.notifyItemRangeInserted(mAdapter.headerSize(), allItems.size)
            }

            override fun must(isSuccess: Boolean) {
                baseBind.refreshLayout.finishRefresh(isSuccess)
                baseBind.refreshLayout.setEnableLoadMore(false)
            }
        })
    }

    override fun forceRefresh() {
        emptyRela.visibility = View.INVISIBLE
        super.forceRefresh()
    }
}
