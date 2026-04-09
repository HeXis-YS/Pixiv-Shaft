package ceui.lisa.adapters

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.bumptech.glide.Glide
import ceui.lisa.R
import ceui.lisa.activities.VActivity
import ceui.lisa.core.Container
import ceui.lisa.core.PageData
import ceui.lisa.databinding.RecyMultiDownloadBinding
import ceui.lisa.interfaces.Callback
import ceui.lisa.interfaces.MultiDownload
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Params

class MultiDownloadAdapter(
    targetList: List<IllustsBean>,
    context: Context,
) : BaseAdapter<IllustsBean, RecyMultiDownloadBinding>(targetList, context), MultiDownload {

    private var imageSize: Int =
        (mContext.resources.displayMetrics.widthPixels -
            mContext.resources.getDimensionPixelSize(R.dimen.two_dp)) / 3
    private var mCallback: Callback<Any?>? = null

    override fun initLayout() {
        mLayoutID = R.layout.recy_multi_download
    }

    override fun bindData(
        target: IllustsBean,
        bindView: ViewHolder<RecyMultiDownloadBinding>,
        position: Int,
    ) {
        val params: ViewGroup.LayoutParams = bindView.baseBind.illustImage.layoutParams
        params.height = imageSize
        params.width = imageSize
        bindView.baseBind.illustImage.layoutParams = params
        val illustsBean = allItems[position]
        val tag = bindView.itemView.getTag(R.id.tag_image_url)
        if (tag !is String || tag != illustsBean.image_urls.medium) {
            Glide.with(mContext)
                .load(GlideUtil.getMediumImg(illustsBean))
                .placeholder(R.color.light_bg)
                .into(bindView.baseBind.illustImage)

            bindView.itemView.setTag(R.id.tag_image_url, illustsBean.image_urls.medium)
        }

        bindView.baseBind.checkbox.setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener { _, isChecked ->
                illustsBean.isChecked = isChecked
                mCallback?.doSomething(null)
            },
        )

        bindView.baseBind.checkbox.isChecked = illustsBean.isChecked

        bindView.itemView.setOnClickListener { _: View ->
            val pageData = PageData(allItems)
            Container.get().addPageToMap(pageData)

            val intent = Intent(mContext, VActivity::class.java)
            intent.putExtra(Params.POSITION, position)
            intent.putExtra(Params.PAGE_UUID, pageData.getUUID())
            mContext.startActivity(intent)
        }
        val listener = mOnItemLongClickListener
        if (listener != null) {
            bindView.itemView.setOnLongClickListener { view ->
                listener.onItemLongClick(view, position, 0)
                true
            }
        }
    }

    override fun getContext(): Context {
        return mContext
    }

    override fun startDownload() {
        MultiDownload.startMultiDownload(this)
    }

    override fun getIllustList(): List<IllustsBean> {
        return allItems
    }

    fun setCallback(callback: Callback<Any?>?) {
        mCallback = callback
        if (mCallback != null) {
            mCallback!!.doSomething(null)
        }
    }
}
