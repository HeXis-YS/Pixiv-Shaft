package ceui.lisa.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.adapters.NHAdapter
import ceui.lisa.databinding.FragmentLikeIllustHorizontalBinding
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ListNovel
import ceui.lisa.models.NovelBean
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.LinearItemHorizontalDecoration
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FragmentLikeNovelHorizontal : BaseFragment<FragmentLikeIllustHorizontalBinding>() {
    private val allItems = ArrayList<NovelBean>()
    private lateinit var mAdapter: NHAdapter
    private var type = 0
    private var userID = 0
    private var novelSize = 0

    companion object {
        @JvmStatic
        fun newInstance(pType: Int, userID: Int, novelSize: Int): FragmentLikeNovelHorizontal {
            val args = Bundle()
            args.putInt(Params.DATA_TYPE, pType)
            args.putInt(Params.USER_ID, userID)
            args.putInt(Params.SIZE, novelSize)
            val fragment = FragmentLikeNovelHorizontal()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        type = bundle.getInt(Params.DATA_TYPE)
        userID = bundle.getInt(Params.USER_ID)
        novelSize = bundle.getInt(Params.SIZE)
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_like_illust_horizontal
    }

    override fun initView() {
        baseBind.progress.visibility = View.INVISIBLE
        baseBind.rootParentView.visibility = View.GONE
        baseBind.recyclerView.addItemDecoration(LinearItemHorizontalDecoration(DensityUtil.dp2px(8.0f)))
        if (type == 1) {
            baseBind.title.setText(R.string.string_237)
            baseBind.howMany.text = String.format(getString(R.string.how_many_illust_works), novelSize)
            baseBind.howMany.setOnClickListener {
                val intent = Intent(mContext, TemplateActivity::class.java)
                intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, baseBind.title.text.toString())
                intent.putExtra(Params.USER_ID, userID)
                startActivity(intent)
            }
        } else if (type == 0) {
            baseBind.title.setText(R.string.string_166)
            baseBind.howMany.setText(R.string.string_167)
            baseBind.howMany.setOnClickListener {
                val intent = Intent(mContext, TemplateActivity::class.java)
                intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, baseBind.title.text.toString())
                intent.putExtra(Params.USER_ID, userID)
                startActivity(intent)
            }
        }
        val manager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        baseBind.recyclerView.layoutManager = manager
        baseBind.recyclerView.setHasFixedSize(true)
        mAdapter = NHAdapter(allItems, mContext)
        mAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int, viewType: Int) {
                TemplateActivity.startNovelDetail(mContext, allItems[position])
            }
        })
        PagerSnapHelper().attachToRecyclerView(baseBind.recyclerView)
        baseBind.recyclerView.adapter = mAdapter
        val layoutParams = baseBind.recyclerView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height =
            DensityUtil.dp2px(180.0f) + mContext.resources.getDimensionPixelSize(R.dimen.sixteen_dp)
        baseBind.recyclerView.layoutParams = layoutParams
    }

    override fun initData() {
        val api: Observable<ListNovel> = if (type == 0) {
            Retro.getAppApi().getUserLikeNovel(Shaft.sUserModel.access_token, userID, Params.TYPE_PUBLIC)
        } else {
            Retro.getAppApi().getUserSubmitNovel(Shaft.sUserModel.access_token, userID)
        }
        api.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NullCtrl<ListNovel>() {
                override fun success(listNovel: ListNovel) {
                    if (listNovel.list.size > 0) {
                        allItems.clear()
                        if (listNovel.list.size > 10) {
                            allItems.addAll(listNovel.list.subList(0, 10))
                        } else {
                            allItems.addAll(listNovel.list)
                        }
                        mAdapter.notifyItemRangeInserted(0, allItems.size)
                        baseBind.rootParentView.visibility = View.VISIBLE
                        val animation: Animation = AlphaAnimation(0.0f, 1.0f)
                        animation.duration = 800L
                        baseBind.rootParentView.startAnimation(animation)
                    }
                }
            })
    }
}
