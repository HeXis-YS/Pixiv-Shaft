package ceui.lisa.cache

import android.content.SharedPreferences
import ceui.lisa.activities.Shaft

class SfOperator : IOperate {

    override fun <T : Any> getModel(key: String, pClass: Class<T>): T? {
        val value = Shaft.sPreferences.getString(key, "")
        return Shaft.sGson.fromJson(value, pClass)
    }

    override fun <T> saveModel(ket: String, pT: T) {
        val editor: SharedPreferences.Editor = Shaft.sPreferences.edit()
        editor.putString(ket, Shaft.sGson.toJson(pT))
        editor.apply()
    }

    override fun clearAll() {}

    override fun clear(key: String) {
        val editor: SharedPreferences.Editor = Shaft.sPreferences.edit()
        editor.putString(key, "")
        editor.apply()
    }
}
