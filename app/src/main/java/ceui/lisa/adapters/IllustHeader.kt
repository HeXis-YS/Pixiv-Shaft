package ceui.lisa.adapters

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.activities.RankActivity
import ceui.lisa.activities.VActivity
import ceui.lisa.core.Container
import ceui.lisa.core.PageData
import ceui.lisa.databinding.RecyRecmdHeaderBinding
import ceui.lisa.models.IllustsBean
import ceui.lisa.utils.DensityUtil
import ceui.lisa.utils.Params
import ceui.lisa.view.LinearItemHorizontalDecoration

class IllustHeader(bindView: RecyRecmdHeaderBinding, private var type: String = "") :
    ViewHolder<RecyRecmdHeaderBinding>(bindView) {

    fun show(context: Context, illustsBeans: List<IllustsBean>) {
        baseBind.topRela.visibility = View.VISIBLE
        val animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 800L
        baseBind.topRela.startAnimation(animation)
        val adapter = RAdapter(illustsBeans, context)
        adapter.setOnItemClickListener { _, position, _ ->
            val pageData = PageData(illustsBeans)
            Container.get().addPageToMap(pageData)

            val intent = Intent(context, VActivity::class.java)
            intent.putExtra(Params.POSITION, position)
            intent.putExtra(Params.PAGE_UUID, pageData.getUUID())
            context.startActivity(intent)
        }
        baseBind.ranking.adapter = adapter
    }

    fun initView(context: Context) {
        baseBind.topRela.visibility = View.GONE
        baseBind.seeMore.setOnClickListener {
            val intent = Intent(context, RankActivity::class.java)
            intent.putExtra("dataType", type)
            context.startActivity(intent)
        }
        baseBind.ranking.addItemDecoration(LinearItemHorizontalDecoration(DensityUtil.dp2px(8.0f)))
        val manager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        baseBind.ranking.layoutManager = manager
        baseBind.ranking.setHasFixedSize(true)
    }
}
