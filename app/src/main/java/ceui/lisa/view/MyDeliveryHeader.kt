package ceui.lisa.view

import android.content.Context
import ceui.lisa.R
import ceui.lisa.activities.Shaft

class MyDeliveryHeader(context: Context) : DeliveryHeader(context) {

    companion object {
        init {
            cloudColors[0] = Shaft.getContext().resources.getColor(R.color.delivery_header_cloud)
        }

        @JvmStatic
        fun changeCloudColor(context: Context) {
            cloudColors[0] = context.resources.getColor(R.color.delivery_header_cloud)
        }
    }
}
