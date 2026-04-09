package ceui.lisa.activities

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import ceui.lisa.R
import ceui.lisa.database.UserEntity
import ceui.lisa.databinding.ActivityOutWakeBinding
import ceui.lisa.feature.PkceUtil
import ceui.lisa.fragments.FragmentLogin
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.interfaces.Callback
import ceui.lisa.models.UserModel
import ceui.lisa.utils.Common
import ceui.lisa.utils.Local
import ceui.lisa.utils.Params
import ceui.lisa.utils.PixivOperate
import com.qmuiteam.qmui.skin.QMUISkinManager
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OutWakeActivity : BaseActivity<ActivityOutWakeBinding>() {

    override fun initLayout(): Int {
        return R.layout.activity_out_wake
    }

    override fun hideStatusBar(): Boolean {
        return true
    }

    override fun initView() {
    }

    override fun initData() {
        val activityIntent = intent
        val uri = activityIntent?.data
        if (uri != null) {
            val scheme = uri.scheme
            if (!TextUtils.isEmpty(scheme)) {
                if (uri.path != null) {
                    if (uri.pathSegments.contains("artworks") || uri.pathSegments.contains("i")) {
                        if (isNetWorking) {
                            return
                        }
                        isNetWorking = true
                        val pathArray = uri.pathSegments
                        val illustID = pathArray[pathArray.size - 1]
                        if (!TextUtils.isEmpty(illustID)) {
                            PixivOperate.getIllustByID(
                                Shaft.sUserModel,
                                tryParseId(illustID),
                                mContext,
                                Callback { finish() },
                                null,
                            )
                            return
                        }
                    }

                    if (
                        uri.pathSegments.contains("novel") && !TextUtils.isEmpty(uri.getQueryParameter("id")) ||
                        uri.pathSegments.contains("n")
                    ) {
                        if (isNetWorking) {
                            return
                        }
                        isNetWorking = true
                        val novelId = if (uri.pathSegments.contains("novel") &&
                            !TextUtils.isEmpty(uri.getQueryParameter("id"))
                        ) {
                            uri.getQueryParameter("id")
                        } else {
                            val pathArray = uri.pathSegments
                            pathArray[pathArray.size - 1]
                        }
                        PixivOperate.getNovelByID(
                            Shaft.sUserModel,
                            tryParseId(novelId!!),
                            mContext,
                            Callback { finish() },
                        )
                        return
                    }

                    if (uri.pathSegments.contains("users") || uri.pathSegments.contains("u")) {
                        val pathArray = uri.pathSegments
                        val userID = pathArray[pathArray.size - 1]
                        if (!TextUtils.isEmpty(userID)) {
                            val userIntent = Intent(mContext, UActivity::class.java)
                            userIntent.putExtra(Params.USER_ID, userID.toInt())
                            startActivity(userIntent)
                            finish()
                            return
                        }
                    }
                }

                if (scheme!!.contains("http")) {
                    try {
                        val uriString = uri.toString()
                        if (uriString.lowercase().contains(PIXIV_IMAGE_HOST)) {
                            val index = uriString.lastIndexOf("/")
                            val end = uriString.substring(index + 1)
                            val idString = end.split("_")[0]

                            Common.showLog("end $end idString $idString")
                            PixivOperate.getIllustByID(
                                Shaft.sUserModel,
                                tryParseId(idString),
                                mContext,
                                Callback { finish() },
                                null,
                            )
                            return
                        } else if (uriString.lowercase().contains(HOST_ME)) {
                            startActivity(TemplateActivity.newWebIntent(mContext, HOST_ME, uriString))
                            finish()
                            return
                        } else if (uriString.lowercase().contains(HOST_PIXIVISION)) {
                            startActivity(
                                TemplateActivity.newWebIntent(
                                    mContext,
                                    getString(R.string.pixiv_special),
                                    uriString,
                                    true,
                                ),
                            )
                            finish()
                            return
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val illustID = uri.getQueryParameter("illust_id")
                    if (!TextUtils.isEmpty(illustID)) {
                        PixivOperate.getIllustByID(
                            Shaft.sUserModel,
                            tryParseId(illustID!!),
                            mContext,
                            Callback { finish() },
                            null,
                        )
                        return
                    }

                    val userID = uri.getQueryParameter("id")
                    if (!TextUtils.isEmpty(userID)) {
                        val userIntent = Intent(mContext, UActivity::class.java)
                        userIntent.putExtra(Params.USER_ID, userID!!.toInt())
                        startActivity(userIntent)
                        finish()
                        return
                    }
                }

                if (scheme.contains("pixiv") || scheme.contains("shaftintent")) {
                    val host = uri.host
                    if (!TextUtils.isEmpty(host)) {
                        val hostString = host!!
                        if (hostString == "account") {
                            Common.showToast(getString(R.string.trying_login))
                            val code = uri.getQueryParameter("code")
                            Retro.getAccountApi().newLogin(
                                FragmentLogin.CLIENT_ID,
                                FragmentLogin.CLIENT_SECRET,
                                FragmentLogin.AUTH_CODE,
                                code!!,
                                PkceUtil.pkceItem.verify,
                                FragmentLogin.CALL_BACK,
                                true,
                            ).subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : NullCtrl<UserModel>() {
                                    override fun success(userModel: UserModel) {
                                        Common.showLog(userModel.toString())
                                        Common.showToast("登录成功")

                                        userModel.user.isIs_login = true
                                        Local.saveUser(userModel)

                                        val userEntity = UserEntity()
                                        userEntity.loginTime = System.currentTimeMillis()
                                        userEntity.userID = userModel.user.id
                                        userEntity.userGson = Shaft.sGson.toJson(Local.getUser())

                                        PixivOperate.insertUser(userEntity)

                                        if (userModel.user.isR18Enabled || !userModel.user.isIs_mail_authorized) {
                                            mActivity.finish()
                                            Common.restart()
                                        } else {
                                            QMUIDialog.MessageDialogBuilder(mActivity)
                                                .setTitle(R.string.string_216)
                                                .setMessage(R.string.string_400)
                                                .setSkinManager(QMUISkinManager.defaultInstance(mContext))
                                                .addAction(
                                                    R.string.string_401,
                                                ) { dialog: QMUIDialog, _: Int ->
                                                    dialog.dismiss()
                                                    mActivity.finish()
                                                    Common.restart()
                                                }
                                                .addAction(
                                                    R.string.string_402,
                                                ) { dialog: QMUIDialog, _: Int ->
                                                    TemplateActivity.startWeb(
                                                        mContext,
                                                        null,
                                                        Params.URL_R18_SETTING,
                                                    )
                                                }
                                                .create()
                                                .show()
                                        }
                                    }
                                })
                            return
                        }

                        if (hostString.contains("users")) {
                            val path = uri.path
                            val userIntent = Intent(mContext, UActivity::class.java)
                            userIntent.putExtra(Params.USER_ID, path!!.substring(1).toInt())
                            startActivity(userIntent)
                            finish()
                            return
                        }

                        if (hostString.contains("illusts")) {
                            val path = uri.path
                            PixivOperate.getIllustByID(
                                Shaft.sUserModel,
                                tryParseId(path!!.substring(1)),
                                mContext,
                                Callback { finish() },
                                null,
                            )
                            return
                        }

                        if (hostString.contains("novels")) {
                            val path = uri.path
                            PixivOperate.getNovelByID(
                                Shaft.sUserModel,
                                tryParseId(path!!.substring(1)),
                                mContext,
                                Callback { finish() },
                            )
                            return
                        }
                    }
                }
            }
        }

        if (Shaft.sUserModel.user.isIs_login) {
            val mainIntent = Intent(mContext, MainActivity::class.java)
            mActivity.startActivity(mainIntent)
            mActivity.finish()
        } else {
            TemplateActivity.startLogin(mContext)
            finish()
        }
    }

    companion object {
        const val HOST_ME = "pixiv.me"
        const val HOST_PIXIVISION = "pixivision.net"
        private const val PIXIV_IMAGE_HOST = "i.pximg.net"

        @JvmField
        var isNetWorking = false
    }
}
