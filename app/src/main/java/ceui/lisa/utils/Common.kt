package ceui.lisa.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ceui.lisa.R
import ceui.lisa.activities.MainActivity
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.activities.UActivity
import ceui.lisa.database.UserEntity
import ceui.lisa.download.FileCreator
import ceui.lisa.file.LegacyFile
import ceui.lisa.file.SAFile
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.UserContainer
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.Utils
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringChain
import com.hjq.toast.ToastUtils
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import okhttp3.MediaType
import okhttp3.Response
import okio.Buffer
import okio.BufferedSource
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.charset.UnsupportedCharsetException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList
import java.util.Random

object Common {
    private val safeReplacer =
        arrayOf(
            arrayOf("|", "%7c"),
            arrayOf("\\", "%5c"),
            arrayOf("?", "%3f"),
            arrayOf("*", "\u22c6"),
            arrayOf("<", "%3c"),
            arrayOf("\"", "%22"),
            arrayOf(":", "%3a"),
            arrayOf(">", "%3e"),
            arrayOf("/", "%2f"),
        )

    @JvmStatic
    fun isNumeric(str: String): Boolean {
        for (i in str.length - 1 downTo 0) {
            if (!Character.isDigit(str[i])) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun isEmpty(list: List<*>?): Boolean {
        return list == null || list.isEmpty()
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        val imm =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive && activity.currentFocus != null) {
            if (activity.currentFocus!!.windowToken != null) {
                imm.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS,
                )
            }
        }
    }

    @JvmStatic
    fun logOut(context: Context, deleteUser: Boolean) {
        if (Shaft.sUserModel != null) {
            Shaft.sUserModel.user.setIs_login(false)
            Local.saveUser(Shaft.sUserModel)
            if (deleteUser) {
                val userEntity = UserEntity()
                userEntity.userID = Shaft.sUserModel.userId
                PixivOperate.deleteUser(userEntity)
            }
            val intent = TemplateActivity.newLoginIntent(context)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK,
            )
            context.startActivity(intent)
        }
    }

    @JvmStatic
    fun <T> showLog(t: T) {
        Log.d("==SHAFT== log ==> ", t.toString())
    }

    @JvmStatic
    fun <T> showToast(t: T) {
        ToastUtils.show(t)
    }

    @JvmStatic
    fun showToast(id: Int) {
        ToastUtils.show(id)
    }

    @JvmStatic
    fun <T> showToast(t: T, type: Int) {
        ToastUtils.show(t)
    }

    @JvmStatic
    fun getAppVersionCode(context: Context): String {
        var versioncode = 0
        try {
            val pm = context.packageManager
            val pi: PackageInfo = pm.getPackageInfo(context.packageName, 0)
            versioncode = pi.versionCode
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return "$versioncode"
    }

    @JvmStatic
    fun getAppVersionName(context: Context): String? {
        var versionName: String? = null
        try {
            val pm = context.packageManager
            val pi: PackageInfo = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
        } catch (e: Exception) {
            Log.e("VersionInfo", "Exception", e)
        }
        return versionName
    }

    @JvmStatic
    fun <T> showToast(t: T, isLong: Boolean) {
        ToastUtils.show(t)
    }

    @JvmStatic
    fun copy(context: Context, s: String) {
        ClipBoardUtils.putTextIntoClipboard(context, s, true)
    }

    @JvmStatic
    fun copy(context: Context, s: String, hasHint: Boolean) {
        ClipBoardUtils.putTextIntoClipboard(context, s, hasHint)
    }

    @JvmStatic
    fun checkEmpty(before: String?): String {
        return if (TextUtils.isEmpty(before)) {
            Shaft.getContext().getString(R.string.no_info)
        } else {
            before!!
        }
    }

    @JvmStatic
    fun checkEmpty(before: EditText?): String {
        return if (before != null &&
            before.text != null &&
            !TextUtils.isEmpty(before.text.toString())
        ) {
            before.text.toString()
        } else {
            ""
        }
    }

    @JvmStatic
    fun animate(linearLayout: LinearLayout) {
        val springChain = SpringChain.create(40, 8, 60, 10)
        val childCount = linearLayout.childCount
        for (i in 0 until childCount) {
            val view = linearLayout.getChildAt(i)
            springChain.addSpring(
                object : SimpleSpringListener() {
                    override fun onSpringUpdate(spring: Spring) {
                        view.translationX = spring.currentValue.toFloat()
                    }
                },
            )
        }

        val springs = springChain.allSprings
        for (spring in springs) {
            spring.currentValue = 400.0
        }
        springChain.setControlSpringIndex(0).controlSpring.endValue = 0.0
    }

    @JvmStatic
    fun createDialog(context: Context) {
        val qmuiDialog =
            QMUIDialog.MessageDialogBuilder(context)
                .setTitle(context.getString(R.string.string_188))
                .setMessage(context.getString(R.string.dont_catch_me))
                .setSkinManager(QMUISkinManager.defaultInstance(context))
                .addAction(
                    context.getString(R.string.string_189),
                    object : QMUIDialogAction.ActionListener {
                        override fun onClick(dialog: QMUIDialog, index: Int) {
                            Shaft.getMMKV().encode(Params.SHOW_DIALOG, false)
                            dialog.dismiss()
                        }
                    },
                )
                .addAction(
                    context.getString(R.string.string_190),
                    object : QMUIDialogAction.ActionListener {
                        override fun onClick(dialog: QMUIDialog, index: Int) {
                            Shaft.getMMKV().encode(Params.SHOW_DIALOG, true)
                            dialog.dismiss()
                        }
                    },
                )
                .create()
        val window: Window? = qmuiDialog.window
        if (window != null) {
            window.setWindowAnimations(R.style.dialog_animation_scale)
        }
        qmuiDialog.show()
    }

    @JvmStatic
    fun getResponseBody(response: Response): String {
        val utf8 = StandardCharsets.UTF_8
        val responseBody = response.body ?: return ""
        val source: BufferedSource = responseBody.source()
        try {
            source.request(Long.MAX_VALUE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val buffer: Buffer = source.buffer
        var charset: Charset = utf8
        val contentType: MediaType? = responseBody.contentType()
        if (contentType != null) {
            try {
                charset = contentType.charset(utf8) ?: utf8
            } catch (e: UnsupportedCharsetException) {
                e.printStackTrace()
            }
        }
        return buffer.clone().readString(charset)
    }

    @JvmStatic
    fun showUser(context: Context, userContainer: UserContainer) {
        val intent = Intent(context, UActivity::class.java)
        intent.putExtra(Params.USER_ID, userContainer.userId)
        context.startActivity(intent)
    }

    @JvmStatic
    fun <T> cutToJson(from: List<T>?): String {
        if (isEmpty(from)) {
            return ""
        }

        return if (from!!.size > 5) {
            val temp: MutableList<T> = ArrayList()
            for (i in 0..4) {
                temp.add(from[i])
            }
            Shaft.sGson.toJson(temp)
        } else {
            Shaft.sGson.toJson(from)
        }
    }

    @JvmStatic
    fun isAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    @JvmStatic
    fun restart() {
        restart(false)
    }

    @JvmStatic
    fun restart(refreshDrawerHeader: Boolean) {
        val intent = MainActivity.newIntent(Utils.getApp(), refreshDrawerHeader)
        intent.component = ComponentName(Utils.getApp(), MainActivity::class.java.name)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK,
        )
        Utils.getApp().startActivity(intent)
    }

    @JvmStatic
    fun flatRandom(left: Int, right: Int): Int {
        val r = Random()
        return r.nextInt(right - left) + left
    }

    @JvmStatic
    fun flatRandom(right: Int): Int {
        return flatRandom(0, right)
    }

    @JvmStatic
    fun resolveThemeAttribute(context: Context?, resId: Int): Int {
        if (context == null) {
            return 0
        }
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }

    @JvmStatic
    fun removeFSReservedChars(s: String): String {
        var result = s
        try {
            for (strings in safeReplacer) {
                result = result.replace(strings[0], strings[1])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @JvmStatic
    fun isIllustDownloaded(illust: IllustsBean?): Boolean {
        if (illust == null) {
            return false
        }
        try {
            return if (illust.page_count == 1) {
                if (Shaft.sSettings.getDownloadWay() == 1) {
                    SAFile.isFileExists(Shaft.getContext(), illust)
                } else {
                    FileCreator.isExist(illust, 0)
                }
            } else {
                val range = 0 until (illust.page_count - 1)
                if (Shaft.sSettings.getDownloadWay() == 1) {
                    range.all { index -> SAFile.isFileExists(Shaft.getContext(), illust, index) }
                } else {
                    range.all { index -> FileCreator.isExist(illust, index) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @JvmStatic
    fun isIllustDownloaded(illust: IllustsBean?, index: Int): Boolean {
        if (illust == null) {
            return false
        }
        try {
            return if (illust.page_count == 1) {
                if (Shaft.sSettings.getDownloadWay() == 1) {
                    SAFile.isFileExists(Shaft.getContext(), illust)
                } else {
                    FileCreator.isExist(illust, 0)
                }
            } else {
                if (Shaft.sSettings.getDownloadWay() == 1) {
                    SAFile.isFileExists(Shaft.getContext(), illust, index)
                } else {
                    FileCreator.isExist(illust, index)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @JvmStatic
    fun getLocalYYYYMMDDHHMMString(source: String): String {
        return try {
            ZonedDateTime
                .parse(source)
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        } catch (e: Exception) {
            e.printStackTrace()
            source.substring(0, 16)
        }
    }

    @JvmStatic
    fun getLocalYYYYMMDDHHMMSSString(source: String): String {
        return try {
            ZonedDateTime
                .parse(source)
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } catch (e: Exception) {
            e.printStackTrace()
            source
        }
    }

    @JvmStatic
    fun getLocalYYYYMMDDHHMMSSFileString(source: String): String {
        return try {
            ZonedDateTime
                .parse(source)
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        } catch (e: Exception) {
            e.printStackTrace()
            source
        }
    }

    @JvmStatic
    fun getNovelTextColor(): Int {
        val color = Shaft.sSettings.getNovelHolderTextColor()
        return if (color == 0) {
            ContextCompat.getColor(Shaft.getContext(), R.color.white)
        } else {
            color
        }
    }

    @JvmStatic
    fun isFileSizeOkToReverseSearch(uri: Uri, maxImageSize: Long): Boolean {
        val cursor: Cursor =
            Shaft.getContext().contentResolver.query(uri, null, null, null, null) ?: return false
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        val ret = cursor.getLong(sizeIndex) <= maxImageSize
        cursor.close()
        return ret
    }

    @JvmStatic
    fun copyUriToImageCacheFolder(uri: Uri): File? {
        var `is`: InputStream? = null
        return try {
            `is` = Utils.getApp().contentResolver.openInputStream(uri)
            val file =
                File(
                    LegacyFile.imageCacheFolder(Utils.getApp()),
                    System.currentTimeMillis().toString(),
                )
            FileIOUtils.writeFileFromIS(file.absolutePath, `is`)
            file
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @JvmStatic
    fun copyBitmapToImageCacheFolder(bitmap: Bitmap?, fileName: String): Uri? {
        if (bitmap == null) {
            return null
        }
        try {
            val cachePath = File(Utils.getApp().externalCacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
            return FileProvider.getUriForFile(Utils.getApp(), "ceui.lisa.pixiv.provider", file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
