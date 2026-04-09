package ceui.lisa.dialogs

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import ceui.lisa.R
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.core.TryCatchObserverImpl
import ceui.lisa.databinding.DialogMuteTagBinding
import ceui.lisa.helper.IllustNovelFilter
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.TagsBean
import ceui.lisa.utils.Common
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate

class MuteDialog : BaseDialog<DialogMuteTagBinding>() {

    private var mIllust: IllustsBean? = null
    private val selected = ArrayList<TagsBean>()
    private val muteNotEffect = ArrayList<Boolean>()

    companion object {
        @JvmStatic
        fun newInstance(illustsBean: IllustsBean): MuteDialog {
            val args = Bundle()
            args.putSerializable(Params.CONTENT, illustsBean)
            val fragment = MuteDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initLayout() {
        mLayoutID = R.layout.dialog_mute_tag
    }

    override fun initView(v: View) {
        RxRun.runOn(object : RxRunnable<List<TagsBean>>() {
            override fun execute(): List<TagsBean> {
                return IllustNovelFilter.getMutedTags()
            }
        }, object : TryCatchObserverImpl<List<TagsBean>>() {
            override fun next(muted: List<TagsBean>) {
                if (!isAdded) {
                    return
                }
                bindTagState(muted)
            }
        })
        val binding = baseBind!!
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.sure.setOnClickListener {
            if (selected.isNotEmpty()) {
                PixivOperate.muteTags(selected)
                Common.showToast(mContext!!.resources.getString(R.string.operate_success))
                dismiss()
            } else {
                Common.showToast(getString(R.string.string_165))
            }
        }
        binding.other.setOnClickListener {
            TemplateActivity.startMutedTags(mContext!!)
            dismiss()
        }
    }

    private fun bindTagState(muted: List<TagsBean>) {
        val currentIllust = mIllust!!
        val illustTags = currentIllust.tags
        val selectedIndex = HashSet<Int>()
        muteNotEffect.clear()
        selected.clear()
        for (i in illustTags.indices) {
            val tagsBean = illustTags[i]
            muteNotEffect.add(false)
            for (mutedBean in muted) {
                if (tagsBean.name == mutedBean.name) {
                    if (mutedBean.isEffective) {
                        selectedIndex.add(i)
                    } else {
                        muteNotEffect[i] = true
                    }
                    break
                }
            }
        }

        val adapter = object : TagAdapter<TagsBean>(illustTags) {
            override fun getView(parent: FlowLayout, position: Int, o: TagsBean): View {
                val view = View.inflate(mContext, R.layout.recy_single_tag_text, null)
                val tag = view.findViewById<TextView>(R.id.tag_title)
                tag.text = o.name
                if (muteNotEffect[position]) {
                    tag.setBackgroundResource(R.drawable.tag_stroke_checked_not_enable_bg)
                }
                return view
            }

            override fun onSelected(position: Int, view: View) {
                super.onSelected(position, view)
                (view as TextView).setTextColor(
                    Common.resolveThemeAttribute(
                        mContext,
                        androidx.appcompat.R.attr.colorPrimary,
                    ),
                )
                view.setBackgroundResource(R.drawable.tag_stroke_checked_bg)
                selected.add(currentIllust.tags[position])
            }

            override fun unSelected(position: Int, view: View) {
                super.unSelected(position, view)
                if (muteNotEffect[position]) {
                    view.setBackgroundResource(R.drawable.tag_stroke_checked_not_enable_bg)
                } else {
                    view.setBackgroundResource(R.drawable.tag_stroke_bg)
                }
                (view as TextView).setTextColor(resources.getColor(R.color.tag_text_unselect))
                selected.remove(currentIllust.tags[position])
            }
        }
        baseBind!!.tagLayout.adapter = adapter
        if (selectedIndex.isNotEmpty()) {
            adapter.setSelectedList(selectedIndex)
        }
    }

    override fun initData() = Unit

    override fun initBundle(bundle: Bundle) {
        mIllust = bundle.getSerializable(Params.CONTENT) as IllustsBean
    }
}
