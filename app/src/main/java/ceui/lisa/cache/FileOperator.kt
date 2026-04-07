package ceui.lisa.cache

import android.util.Log
import com.blankj.utilcode.util.PathUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class FileOperator : IOperate {

    override fun <T : Any> getModel(key: String, pClass: Class<T>): T? {
        return try {
            val file = File(PathUtils.getInternalAppCachePath(), key)
            if (!file.exists()) {
                return null
            }

            ObjectInputStream(FileInputStream(file)).use { input ->
                pClass.cast(input.readObject())
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun <T> saveModel(ket: String, pT: T) {
        try {
            val file = File(PathUtils.getInternalAppCachePath(), ket)
            Log.d("file name ", file.path)
            ObjectOutputStream(FileOutputStream(file)).use { output ->
                output.writeObject(pT)
                output.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun clearAll() {}

    override fun clear(key: String) {}
}
