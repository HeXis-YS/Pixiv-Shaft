package ceui.lisa.adapters

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.Nullable
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.databinding.RecyColorBinding
import ceui.lisa.interfaces.OnItemClickListener
import ceui.lisa.model.ColorItem
import ceui.lisa.utils.Common
import ceui.lisa.utils.Local
import com.blankj.utilcode.util.StringUtils.getString

class ColorAdapter(
    @Nullable targetList: List<ColorItem>?,
    context: Context,
) : BaseAdapter<ColorItem, RecyColorBinding>(targetList, context) {

    init {
        handleClick()
    }

    override fun initLayout() {
        mLayoutID = R.layout.recy_color
    }

    override fun bindData(target: ColorItem, bindView: ViewHolder<RecyColorBinding>, position: Int) {
        bindView.baseBind.card.setCardBackgroundColor(Color.parseColor(target.color))
        if (target.isSelect) {
            bindView.baseBind.name.text =
                String.format("%s" + getString(R.string.theme_nowUsing), target.name)
        } else {
            bindView.baseBind.name.text = target.name
        }
        bindView.baseBind.value.text = target.color
        bindView.itemView.setOnClickListener { v: View ->
            mOnItemClickListener?.onItemClick(v, position, 0)
        }
    }

    private fun handleClick() {
        setOnItemClickListener(
            OnItemClickListener { _, position, _ ->
                if (position == Shaft.sSettings.themeIndex) {
                    return@OnItemClickListener
                }

                Shaft.sSettings.themeIndex = position
                Local.setSettings(Shaft.sSettings)
                Common.restart()
                Common.showToast(getString(R.string.string_428), 2)
            },
        )
    }
}
