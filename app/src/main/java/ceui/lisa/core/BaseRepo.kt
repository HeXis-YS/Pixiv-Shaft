package ceui.lisa.core

import android.content.Context
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.header.ClassicsFooter
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshHeader
import ceui.lisa.activities.Shaft
import ceui.lisa.utils.Common

open class BaseRepo : DataView {
    init {
        Common.showLog("BaseRepo ${javaClass.simpleName} newInstance")
    }

    override fun hasNext(): Boolean {
        return true
    }

    override fun enableRefresh(): Boolean {
        return true
    }

    override fun getHeader(context: Context): RefreshHeader {
        return MaterialHeader(context)
    }

    override fun getFooter(context: Context): RefreshFooter {
        return ClassicsFooter(context)
    }

    override fun showNoDataHint(): Boolean {
        return true
    }

    override fun token(): String {
        return try {
            Shaft.sUserModel?.access_token ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun currentUserID(): Int {
        return try {
            Shaft.sUserModel?.user?.id ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun localData(): Boolean {
        return false
    }
}
