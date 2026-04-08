package ceui.lisa.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.activities.VActivity
import ceui.lisa.adapters.LAdapter
import ceui.lisa.core.Container
import ceui.lisa.core.PageData
import ceui.lisa.databinding.FragmentLikeIllustHorizontalBinding
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ListIllust
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.UserDetailResponse
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.LinearItemHorizontalDecoration
import com.github.ybq.android.spinkit.style.Wave
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator

class FragmentLikeIllustHorizontal : BaseFragment<FragmentLikeIllustHorizontalBinding>() {
    private val allItems = ArrayList<IllustsBean>()
    private var mUserDetailResponse: UserDetailResponse? = null
    private lateinit var mAdapter: LAdapter
    private var type = 0

    companion object {
        @JvmStatic
        fun newInstance(userDetailResponse: UserDetailResponse, pType: Int): FragmentLikeIllustHorizontal {
            val args = Bundle()
            args.putSerializable(Params.CONTENT, userDetailResponse)
            args.putInt(Params.DATA_TYPE, pType)
            val fragment = FragmentLikeIllustHorizontal()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        mUserDetailResponse = bundle.getSerializable(Params.CONTENT) as UserDetailResponse
        type = bundle.getInt(Params.DATA_TYPE)
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_like_illust_horizontal
    }

    override fun initView() {
        val wave = Wave()
        wave.setColor(androidx.appcompat.R.attr.colorPrimary)
        baseBind.progress.indeterminateDrawable = wave
        baseBind.recyclerView.addItemDecoration(LinearItemHorizontalDecoration(DensityUtil.dp2px(8.0f)))
        val landingAnimator = FadeInLeftAnimator()
        landingAnimator.addDuration = ListFragment.animateDuration
        landingAnimator.removeDuration = ListFragment.animateDuration
        landingAnimator.moveDuration = ListFragment.animateDuration
        landingAnimator.changeDuration = ListFragment.animateDuration
        baseBind.recyclerView.itemAnimator = landingAnimator
        val manager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        baseBind.recyclerView.layoutManager = manager
        baseBind.recyclerView.setHasFixedSize(true)
        PagerSnapHelper().attachToRecyclerView(baseBind.recyclerView)
        mAdapter = LAdapter(allItems, mContext)
        mAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                val pageData = PageData(allItems)
                Container.get().addPageToMap(pageData)

                val intent = Intent(mContext, VActivity::class.java)
                intent.putExtra(Params.POSITION, position)
                intent.putExtra(Params.PAGE_UUID, pageData.getUUID())
                mContext.startActivity(intent)
            }
        })
        baseBind.recyclerView.adapter = mAdapter
        val layoutParams = baseBind.recyclerView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height =
            mAdapter.getImageSize() + mContext.resources.getDimensionPixelSize(R.dimen.sixteen_dp)
        baseBind.recyclerView.layoutParams = layoutParams
        if (type == 1) {
            baseBind.title.text = "插画/漫画收藏"
            baseBind.howMany.text = String.format(
                getString(R.string.how_many_illust_works),
                mUserDetailResponse!!.profile.total_illust_bookmarks_public
            )
        } else if (type == 2) {
            baseBind.title.text = "插画作品"
            baseBind.howMany.text = String.format(
                getString(R.string.how_many_illust_works),
                mUserDetailResponse!!.profile.total_illusts
            )
        } else if (type == 3) {
            baseBind.title.text = "漫画作品"
            baseBind.howMany.text = String.format(
                getString(R.string.how_many_illust_works),
                mUserDetailResponse!!.profile.total_manga
            )
        }
        baseBind.howMany.setOnClickListener {
            val intent = Intent(mContext, TemplateActivity::class.java)
            intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, baseBind.title.text.toString())
            intent.putExtra(Params.USER_ID, mUserDetailResponse!!.user.id)
            startActivity(intent)
        }
    }

    override fun initData() {
        val api: Observable<ListIllust>? = if (type == 1) {
            Retro.getAppApi().getUserLikeIllust(
                Shaft.sUserModel.access_token,
                mUserDetailResponse!!.user.id,
                Params.TYPE_PUBLIC
            )
        } else if (type == 2) {
            Retro.getAppApi().getUserSubmitIllust(
                Shaft.sUserModel.access_token,
                mUserDetailResponse!!.user.id,
                Params.TYPE_ILLUST
            )
        } else if (type == 3) {
            Retro.getAppApi().getUserSubmitIllust(
                Shaft.sUserModel.access_token,
                mUserDetailResponse!!.user.id,
                Params.TYPE_MANGA
            )
        } else {
            null
        }

        api?.subscribeOn(Schedulers.newThread())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : NullCtrl<ListIllust>() {
                override fun success(listIllust: ListIllust) {
                    allItems.clear()
                    if (listIllust.list.size > 10) {
                        allItems.addAll(listIllust.list.subList(0, 10))
                    } else {
                        allItems.addAll(listIllust.list)
                    }
                    mAdapter.notifyItemRangeInserted(0, allItems.size)
                }

                override fun must(isSuccess: Boolean) {
                    baseBind.progress.visibility = View.INVISIBLE
                }
            })
    }
}
