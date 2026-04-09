package ceui.lisa.fragments

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.activities.BaseActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.NAdapter
import ceui.lisa.cache.Cache
import ceui.lisa.core.BaseRepo
import ceui.lisa.databinding.FragmentNovelSeriesBinding
import ceui.lisa.download.IllustDownload
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.Callback
import ceui.lisa.model.ListNovelOfSeries
import ceui.lisa.models.NovelBean
import ceui.lisa.models.NovelDetail
import ceui.lisa.models.NovelSeriesItem
import ceui.lisa.models.UserBean
import ceui.lisa.models.WebNovel
import ceui.lisa.repo.NovelSeriesDetailRepo
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.TreeMap

class FragmentNovelSeriesDetail :
    NetListFragment<FragmentNovelSeriesBinding, ListNovelOfSeries, NovelBean>() {

    private var seriesID = 0

    companion object {
        @JvmStatic
        fun newInstance(seriesID: Int): FragmentNovelSeriesDetail {
            val args = Bundle()
            args.putInt(Params.ID, seriesID)
            val fragment = FragmentNovelSeriesDetail()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_novel_series
    }

    override fun initToolbar(toolbar: Toolbar) {
        super.initToolbar(toolbar)
        toolbar.inflateMenu(R.menu.novel_series_download)
        toolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                if (item.itemId == R.id.batch_download) {
                    for (novelBean in allItems) {
                        if (novelBean.isLocalSaved()) {
                            saveNovelToDownload(
                                novelBean,
                                Cache.get().getModel(
                                    Params.NOVEL_KEY + novelBean.id,
                                    NovelDetail::class.java,
                                )!!,
                            )
                        } else {
                            Retro.getAppApi()
                                .getNovelDetailV2(Shaft.sUserModel.access_token, novelBean.id.toLong())
                                .enqueue(object : retrofit2.Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>,
                                    ) {
                                        object : WebNovelParser(response) {
                                            override fun onNovelPrepared(
                                                novelDetail: NovelDetail,
                                                webNovel: WebNovel,
                                            ) {
                                                saveNovelToDownload(novelBean, novelDetail)
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    }
                                })
                        }
                    }
                } else if (item.itemId == R.id.batch_download_as_one) {
                    val seriesResponse = mResponse ?: return true
                    val taskContainer = TreeMap<Long, String>()
                    val lineSeparator = System.lineSeparator()
                    for (novelBean in allItems) {
                        if (novelBean.isLocalSaved()) {
                            val sb = lineSeparator + novelBean.title + " - " + novelBean.id + lineSeparator +
                                Cache.get().getModel(
                                    Params.NOVEL_KEY + novelBean.id,
                                    NovelDetail::class.java,
                                )!!.novel_text
                            taskContainer[novelBean.id.toLong()] = sb
                            if (taskContainer.size == allItems.size) {
                                val content = taskContainer.values.joinToString(lineSeparator)
                                saveNovelSeriesToDownload(seriesResponse.novel_series_detail!!, content)
                            }
                        } else {
                            Retro.getAppApi()
                                .getNovelDetailV2(Shaft.sUserModel.access_token, novelBean.id.toLong())
                                .enqueue(object : retrofit2.Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>,
                                    ) {
                                        object : WebNovelParser(response) {
                                            override fun onNovelPrepared(
                                                novelDetail: NovelDetail,
                                                webNovel: WebNovel,
                                            ) {
                                                val sb = lineSeparator + novelBean.title + " - " + novelBean.id + lineSeparator +
                                                    novelDetail.novel_text
                                                taskContainer[novelBean.id.toLong()] = sb
                                                if (taskContainer.size == allItems.size) {
                                                    val content = taskContainer.values.joinToString(lineSeparator)
                                                    saveNovelSeriesToDownload(seriesResponse.novel_series_detail!!, content)
                                                }
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    }
                                })
                        }
                    }
                }
                return true
            }
        })
    }

    override fun initBundle(bundle: Bundle) {
        seriesID = bundle.getInt(Params.ID)
    }

    override fun adapter(): BaseAdapter<*, out ViewDataBinding> = NAdapter(allItems, mContext, true)

    override fun repository(): BaseRepo = NovelSeriesDetailRepo(seriesID)

    override fun getToolbarTitle(): String = "小说系列"

    override fun onResponse(listNovelOfSeries: ListNovelOfSeries) {
        try {
            baseBind.cardPixiv.visibility = View.VISIBLE
            baseBind.seriesTitle.text = String.format("系列名称：%s", listNovelOfSeries.novel_series_detail?.title)
            baseBind.seriesDescription.setHtml(listNovelOfSeries.novel_series_detail?.display_text.orEmpty())
            val minute = (listNovelOfSeries.novel_series_detail?.total_character_count ?: 0) / 500.0
            baseBind.seriesDetail.text = String.format(
                getString(R.string.how_many_novels),
                listNovelOfSeries.novel_series_detail?.content_count,
                listNovelOfSeries.novel_series_detail?.total_character_count,
                Math.floor(minute / 60).toInt(),
                minute.toInt() % 60,
            )
            if (listNovelOfSeries.list.isNotEmpty()) {
                val bean = listNovelOfSeries.list[0]
                val userBean = bean.user
                initUser(userBean)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initUser(userBean: UserBean) {
        val seeUser = View.OnClickListener {
            Common.showUser(mContext, userBean)
        }
        baseBind.userHead.setOnClickListener(seeUser)
        baseBind.userName.setOnClickListener(seeUser)
        baseBind.userName.text = userBean.name
        Glide.with(mContext).load(GlideUtil.getHead(userBean)).into(baseBind.userHead)
        if (userBean.isIs_followed()) {
            baseBind.postLikeUser.text = "取消关注"
        } else {
            baseBind.postLikeUser.text = "添加关注"
        }

        baseBind.postLikeUser.setOnClickListener {
            if (userBean.isIs_followed()) {
                baseBind.postLikeUser.text = "添加关注"
                userBean.setIs_followed(false)
                PixivOperate.postUnFollowUser(userBean.id)
            } else {
                baseBind.postLikeUser.text = "取消关注"
                userBean.setIs_followed(true)
                PixivOperate.postFollowUser(userBean.id, Params.TYPE_PUBLIC)
            }
        }
        baseBind.postLikeUser.setOnLongClickListener {
            if (!userBean.isIs_followed()) {
                baseBind.postLikeUser.text = "取消关注"
                userBean.setIs_followed(true)
                PixivOperate.postFollowUser(userBean.id, Params.TYPE_PRIVATE)
            }
            true
        }
    }

    private fun saveNovelToDownload(novelBean: NovelBean, novelDetail: NovelDetail) {
        IllustDownload.downloadNovel(
            mContext as BaseActivity<*>,
            novelBean,
            novelDetail,
            Callback<Uri> {
                Common.showToast(getString(R.string.string_279), 2)
            },
        )
    }

    private fun saveNovelSeriesToDownload(novelSeriesItem: NovelSeriesItem, content: String) {
        IllustDownload.downloadNovel(
            mContext as BaseActivity<*>,
            novelSeriesItem,
            content,
            Callback<Uri> {
                Common.showToast(getString(R.string.string_279), 2)
            },
        )
    }
}
