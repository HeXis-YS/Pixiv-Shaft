package ceui.lisa.fragments

import android.text.TextUtils
import android.view.View
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.database.UserEntity
import ceui.lisa.databinding.FragmentEditAccountBinding
import ceui.lisa.http.NullCtrl
import ceui.lisa.http.Retro
import ceui.lisa.models.AccountEditResponse
import ceui.lisa.models.UserState
import ceui.lisa.utils.Common
import ceui.lisa.utils.Local
import ceui.lisa.utils.PixivOperate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FragmentEditAccount : BaseFragment<FragmentEditAccountBinding>() {
    private var canChangePixivID = false
    private var hasPassword = false

    override fun initLayout() {
        mLayoutID = R.layout.fragment_edit_account
    }

    override fun initData() {
        if (!Shaft.hasLoginUser()) {
            Common.showToast("你还没有登录")
            mActivity.finish()
            return
        }
        baseBind.toolbar.toolbarTitle.setText(R.string.string_250)
        baseBind.toolbar.toolbar.setNavigationOnClickListener { finish() }
        Retro.getAppApi().getAccountState(Shaft.sUserModel.access_token)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NullCtrl<UserState>() {
                override fun success(userState: UserState) {
                    if (userState.user_state != null) {
                        canChangePixivID = userState.user_state.isCan_change_pixiv_id
                        baseBind.pixivId.isEnabled = canChangePixivID
                        hasPassword = userState.user_state.isHas_password
                        baseBind.userOldPassword.visibility =
                            if (hasPassword) View.VISIBLE else View.GONE
                    }
                }
            })
        if (!TextUtils.isEmpty(Shaft.sUserModel.user.mail_address)) {
            baseBind.emailAddress.setText(Shaft.sUserModel.user.mail_address)
        }
        baseBind.pixivId.setText(Shaft.sUserModel.user.account)
        baseBind.pixivId.isEnabled = false
        baseBind.submit.setOnClickListener { submit() }
    }

    private fun submit() {
        if (hasPassword && TextUtils.isEmpty(baseBind.userOldPassword.text.toString())) {
            Common.showToast("更新账号信息需要输入当前密码")
            return
        }
        val currentPassword = baseBind.userOldPassword.text.toString()
        if (canChangePixivID) {
            if (TextUtils.isEmpty(baseBind.pixivId.text.toString())) {
                Common.showToast("pixiv ID不能为空")
                return
            }
            if (TextUtils.isEmpty(baseBind.userNewPassword.text.toString())) {
                Common.showToast("新密码不能为空")
                return
            }
            val isPixivIdNotChanged = baseBind.pixivId.text.toString() == Shaft.sUserModel.user.account
            val isPasswordNotChanged = baseBind.userNewPassword.text.toString() == currentPassword
            if (TextUtils.isEmpty(baseBind.emailAddress.text.toString())) {
                if (isPixivIdNotChanged && isPasswordNotChanged) {
                    Common.showToast("你还没有做任何修改")
                } else if (isPixivIdNotChanged && !isPasswordNotChanged) {
                    Common.showToast("正在修改密码")
                    Retro.getSignApi().changePassword(
                        Shaft.sUserModel.access_token,
                        currentPassword,
                        baseBind.userNewPassword.text.toString(),
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                Shaft.sUserModel.user.password =
                                    baseBind.userNewPassword.text.toString()
                                saveUser()
                                mActivity.finish()
                                Common.showToast("密码修改成功")
                            }
                        })
                } else if (!isPixivIdNotChanged && isPasswordNotChanged) {
                    Common.showToast("正在修改PixivID")
                    Retro.getSignApi().changePixivID(
                        Shaft.sUserModel.access_token,
                        baseBind.pixivId.text.toString(),
                        currentPassword,
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                Shaft.sUserModel.user.account = baseBind.pixivId.text.toString()
                                saveUser()
                                mActivity.finish()
                                Common.showToast("PixivID修改成功")
                            }
                        })
                } else if (!isPixivIdNotChanged && !isPasswordNotChanged) {
                    Common.showToast("正在修改PixivID 和密码")
                    Retro.getSignApi().changePasswordPixivID(
                        Shaft.sUserModel.access_token,
                        baseBind.pixivId.text.toString(),
                        currentPassword,
                        baseBind.userNewPassword.text.toString(),
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                Shaft.sUserModel.user.account = baseBind.pixivId.text.toString()
                                Shaft.sUserModel.user.password =
                                    baseBind.userNewPassword.text.toString()
                                saveUser()
                                mActivity.finish()
                                Common.showToast("PixivID 和密码修改成功")
                            }
                        })
                }
            } else {
                if (TextUtils.isEmpty(baseBind.pixivId.text.toString())) {
                    Common.showToast("pixiv ID不能为空")
                    return
                }
                if (TextUtils.isEmpty(baseBind.userNewPassword.text.toString())) {
                    Common.showToast("新密码不能为空")
                    return
                }

                if (isPixivIdNotChanged && isPasswordNotChanged) {
                    Retro.getSignApi().changeEmail(
                        Shaft.sUserModel.access_token,
                        baseBind.emailAddress.text.toString(),
                        currentPassword,
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                mActivity.finish()
                                Common.showToast("验证邮件发送成功！", true)
                            }
                        })
                } else if (!isPixivIdNotChanged && isPasswordNotChanged) {
                    Retro.getSignApi().changeEmailAndPixivID(
                        Shaft.sUserModel.access_token,
                        baseBind.emailAddress.text.toString(),
                        baseBind.pixivId.text.toString(),
                        currentPassword,
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                Shaft.sUserModel.user.account = baseBind.pixivId.text.toString()
                                saveUser()
                                mActivity.finish()
                                Common.showToast("验证邮件发送成功！", true)
                            }
                        })
                } else if (isPixivIdNotChanged && !isPasswordNotChanged) {
                    Retro.getSignApi().changeEmailAndPassword(
                        Shaft.sUserModel.access_token,
                        baseBind.emailAddress.text.toString(),
                        currentPassword,
                        baseBind.userNewPassword.text.toString(),
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                Shaft.sUserModel.user.password =
                                    baseBind.userNewPassword.text.toString()
                                saveUser()
                                mActivity.finish()
                                Common.showToast("验证邮件发送成功！", true)
                            }
                        })
                } else if (!isPixivIdNotChanged && !isPasswordNotChanged) {
                    Retro.getSignApi().edit(
                        Shaft.sUserModel.access_token,
                        baseBind.emailAddress.text.toString(),
                        baseBind.pixivId.text.toString(),
                        currentPassword,
                        baseBind.userNewPassword.text.toString(),
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                Shaft.sUserModel.user.password =
                                    baseBind.userNewPassword.text.toString()
                                Shaft.sUserModel.user.account = baseBind.pixivId.text.toString()
                                saveUser()
                                mActivity.finish()
                                Common.showToast("验证邮件发送成功！", true)
                            }
                        })
                }
            }
        } else {
            if (TextUtils.isEmpty(baseBind.userNewPassword.text.toString())) {
                Common.showToast("新密码不能为空")
                return
            }
            val isPasswordNotChanged = baseBind.userNewPassword.text.toString() == currentPassword
            if (TextUtils.isEmpty(baseBind.emailAddress.text.toString())) {
                if (isPasswordNotChanged) {
                    Common.showToast("你还没有做任何修改")
                } else {
                    Common.showToast("正在修改密码")
                    Retro.getSignApi().changePassword(
                        Shaft.sUserModel.access_token,
                        currentPassword,
                        baseBind.userNewPassword.text.toString(),
                    )
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : NullCtrl<AccountEditResponse>() {
                            override fun success(accountEditResponse: AccountEditResponse) {
                                Shaft.sUserModel.user.password =
                                    baseBind.userNewPassword.text.toString()
                                saveUser()
                                mActivity.finish()
                                Common.showToast("密码修改成功")
                            }
                        })
                }
            } else {
                val isEmailNotChanged =
                    baseBind.emailAddress.text.toString() == Shaft.sUserModel.user.mail_address
                if (isEmailNotChanged) {
                    if (isPasswordNotChanged) {
                        Common.showToast("你还没有做任何修改")
                    } else {
                        Common.showToast("正在修改密码")
                        Retro.getSignApi().changePassword(
                            Shaft.sUserModel.access_token,
                            currentPassword,
                            baseBind.userNewPassword.text.toString(),
                        )
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : NullCtrl<AccountEditResponse>() {
                                override fun success(accountEditResponse: AccountEditResponse) {
                                    Shaft.sUserModel.user.password =
                                        baseBind.userNewPassword.text.toString()
                                    saveUser()
                                    mActivity.finish()
                                    Common.showToast("密码修改成功")
                                }
                            })
                    }
                } else {
                    if (isPasswordNotChanged) {
                        Retro.getSignApi().changeEmail(
                            Shaft.sUserModel.access_token,
                            baseBind.emailAddress.text.toString(),
                            currentPassword,
                        )
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : NullCtrl<AccountEditResponse>() {
                                override fun success(accountEditResponse: AccountEditResponse) {
                                    mActivity.finish()
                                    Common.showToast("验证邮件发送成功！", true)
                                }
                            })
                    } else {
                        Retro.getSignApi().changeEmailAndPassword(
                            Shaft.sUserModel.access_token,
                            baseBind.emailAddress.text.toString(),
                            currentPassword,
                            baseBind.userNewPassword.text.toString(),
                        )
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : NullCtrl<AccountEditResponse>() {
                                override fun success(accountEditResponse: AccountEditResponse) {
                                    Shaft.sUserModel.user.password =
                                        baseBind.userNewPassword.text.toString()
                                    saveUser()
                                    mActivity.finish()
                                    Common.showToast("验证邮件发送成功！", true)
                                }
                            })
                    }
                }
            }
        }
    }

    private fun saveUser() {
        Local.saveUser(Shaft.sUserModel)
        val userEntity = UserEntity()
        userEntity.loginTime = System.currentTimeMillis()
        userEntity.userID = Shaft.sUserModel.user.id
        userEntity.userGson = Shaft.sGson.toJson(Shaft.sUserModel)
        PixivOperate.insertUser(userEntity)
    }
}
