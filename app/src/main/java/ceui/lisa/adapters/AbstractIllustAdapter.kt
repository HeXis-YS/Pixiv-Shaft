package ceui.lisa.adapters

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import ceui.lisa.activities.ImageDetailActivity
import ceui.lisa.models.IllustsBean

abstract class AbstractIllustAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    @JvmField
    protected var allIllust: IllustsBean? = null

    @JvmField
    protected var mContext: Context? = null

    @JvmField
    protected var imageSize = 0

    @JvmField
    protected var isForceOriginal = false

    override fun getItemCount(): Int {
        return allIllust!!.page_count
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, ImageDetailActivity::class.java)
            intent.putExtra("illust", allIllust)
            intent.putExtra("dataType", "二级详情")
            intent.putExtra("index", position)
            mContext!!.startActivity(intent)
        }
    }
}
