package ceui.lisa.adapters

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.LinearLayoutManager
import ceui.lisa.activities.RankActivity
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.databinding.RecyRecmdHeaderBinding
import ceui.lisa.models.NovelBean
import ceui.lisa.utils.DensityUtil
import ceui.lisa.view.LinearItemHorizontalDecoration

class NovelHeader(bindView: RecyRecmdHeaderBinding) : ViewHolder<RecyRecmdHeaderBinding>(bindView) {

    fun show(context: Context, illustsBeans: List<NovelBean>) {
        baseBind.topRela.visibility = View.VISIBLE
        val animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = 800L
        baseBind.topRela.startAnimation(animation)
        val adapter = NHAdapter(illustsBeans, context)
        adapter.setOnItemClickListener { _, position, _ ->
            TemplateActivity.startNovelDetail(context, illustsBeans[position])
        }
        baseBind.ranking.adapter = adapter
    }

    fun initView(context: Context) {
        baseBind.topRela.visibility = View.GONE
        baseBind.seeMore.setOnClickListener {
            val intent = Intent(context, RankActivity::class.java)
            intent.putExtra("dataType", "小说")
            context.startActivity(intent)
        }
        baseBind.ranking.addItemDecoration(LinearItemHorizontalDecoration(DensityUtil.dp2px(8.0f)))
        val manager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        baseBind.ranking.layoutManager = manager
        baseBind.ranking.setHasFixedSize(true)
    }
}
