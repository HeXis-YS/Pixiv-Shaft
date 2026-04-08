package ceui.lisa.http

import android.text.TextUtils
import ceui.lisa.activities.Shaft
import ceui.lisa.core.TryCatchObserver
import ceui.lisa.models.Error500
import ceui.lisa.models.Error500Obj
import ceui.lisa.models.ErrorResponse
import ceui.lisa.models.ErrorResponse2
import ceui.lisa.utils.Common
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import java.io.IOException

abstract class ErrorCtrl<T : Any> : TryCatchObserver<T>() {
    override fun subscribe(d: Disposable) {
    }

    override fun error(e: Throwable) {
        if (e is HttpException) {
            try {
                val responseString = e.response()?.errorBody()?.string()
                if (!TextUtils.isEmpty(responseString) &&
                    responseString!!.contains("{") &&
                    responseString.contains("}") &&
                    responseString.contains(":")
                ) {
                    if (responseString.contains("validation_errors") || e.code() == 500) {
                        if (responseString.contains("body\":{")) {
                            val response = Shaft.sGson.fromJson(responseString, Error500Obj::class.java)
                            if (response != null && response.body != null) {
                                if (!TextUtils.isEmpty(response.body.validation_errors.mail_address)) {
                                    Common.showToast(response.body.validation_errors.mail_address)
                                } else if (!TextUtils.isEmpty(response.body.validation_errors.pixiv_id)) {
                                    Common.showToast(response.body.validation_errors.pixiv_id)
                                }
                            } else {
                                Common.showToast(e.toString())
                            }
                        } else {
                            val response = Shaft.sGson.fromJson(responseString, Error500::class.java)
                            if (response != null) {
                                if (!TextUtils.isEmpty(response.message)) {
                                    Common.showToast(response.message)
                                }
                            } else {
                                Common.showToast(e.toString())
                            }
                        }
                    } else if (responseString.contains("invalid_grant") ||
                        responseString.contains("invalid_request")
                    ) {
                        val response = Shaft.sGson.fromJson(responseString, ErrorResponse2::class.java)
                        if (response != null) {
                            if (response.errors != null && response.errors.system != null) {
                                if (!TextUtils.isEmpty(response.errors.system.message)) {
                                    Common.showToast(response.errors.system.message)
                                }
                            }
                        }
                    } else {
                        val response = Shaft.sGson.fromJson(responseString, ErrorResponse::class.java)
                        if (response != null) {
                            if (response.body != null &&
                                response.body.validation_errors != null
                            ) {
                                if (!TextUtils.isEmpty(response.body.validation_errors.mail_address)) {
                                    Common.showToast(response.body.validation_errors.mail_address, true)
                                } else if (!TextUtils.isEmpty(response.body.validation_errors.pixiv_id)) {
                                    Common.showToast(response.body.validation_errors.pixiv_id)
                                }
                            } else {
                                if (response.errors != null) {
                                    Common.showToast(response.errors.system.message, true)
                                }
                                if (response.error != null) {
                                    if (!TextUtils.isEmpty(response.error.message)) {
                                        Common.showToast(response.error.message, true)
                                    } else if (!TextUtils.isEmpty(response.error.reason)) {
                                        Common.showToast(response.error.reason, true)
                                    } else if (!TextUtils.isEmpty(response.error.user_message)) {
                                        Common.showToast(response.error.user_message, true)
                                    } else if (response.error.user_message_details != null &&
                                        !TextUtils.isEmpty(response.error.user_message_details.profile_image)
                                    ) {
                                        Common.showToast(response.error.user_message_details.profile_image, true)
                                    }
                                }
                            }
                        } else {
                            Common.showToast(e.toString())
                        }
                    }
                } else {
                    Common.showToast(e.toString())
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    override fun complete() {
    }
}
