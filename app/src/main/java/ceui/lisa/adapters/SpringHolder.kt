package ceui.lisa.adapters

import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringConfig
import com.facebook.rebound.SpringSystem
import ceui.lisa.databinding.RecyViewHistoryBinding

class SpringHolder(bindView: RecyViewHistoryBinding) : ViewHolder<RecyViewHistoryBinding>(bindView) {

    @JvmField
    val spring: Spring

    init {
        val springSystem = SpringSystem.create()
        spring = springSystem.createSpring()
        spring.springConfig = SpringConfig.fromOrigamiTensionAndFriction(40.0, 5.0)
        spring.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                itemView.translationX = spring.currentValue.toFloat()
            }
        })
    }
}
