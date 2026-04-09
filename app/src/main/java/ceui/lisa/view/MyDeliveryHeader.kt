package ceui.lisa.view

import android.content.Context
import ceui.lisa.R
import ceui.lisa.activities.Shaft

class MyDeliveryHeader(context: Context) : DeliveryHeader(context) {

    companion object {
        init {
            DeliveryHeader.setCloudColor(Shaft.getContext().resources.getColor(R.color.delivery_header_cloud))
        }

        @JvmStatic
        fun changeCloudColor(context: Context) {
            DeliveryHeader.setCloudColor(context.resources.getColor(R.color.delivery_header_cloud))
        }
    }
}
