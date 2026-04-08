package ceui.lisa.fragments

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.scwang.smart.refresh.header.FalsifyFooter
import com.scwang.smart.refresh.header.FalsifyHeader
import ceui.lisa.R
import ceui.lisa.activities.Shaft
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.core.RxRun
import ceui.lisa.core.RxRunnable
import ceui.lisa.database.AppDatabase
import ceui.lisa.database.UserEntity
import ceui.lisa.databinding.FragmentLocalUserBinding
import ceui.lisa.databinding.RecyLocalUserBinding
import ceui.lisa.http.NullCtrl
import ceui.lisa.models.UserModel
import ceui.lisa.utils.Common
import ceui.lisa.utils.GlideUtil
import ceui.lisa.utils.Local
import ceui.lisa.utils.Params

class FragmentLocalUsers : BaseFragment<FragmentLocalUserBinding>() {
    private var allItems: List<UserModel> = ArrayList()

    override fun initLayout() {
        mLayoutID = R.layout.fragment_local_user
    }

    override fun initView() {
        baseBind.toolbar.toolbarTitle.setText(R.string.string_251)
        baseBind.toolbar.toolbar.setNavigationOnClickListener { mActivity.finish() }
        baseBind.addUser.setOnClickListener {
            TemplateActivity.startLogin(mContext)
        }
        baseBind.refreshLayout.setRefreshFooter(FalsifyFooter(mContext))
        baseBind.refreshLayout.setRefreshHeader(FalsifyHeader(mContext))
    }

    override fun initData() {
        RxRun.runOn(object : RxRunnable<List<UserModel>>() {
            override fun execute(): List<UserModel> {
                val temp: List<UserEntity> = AppDatabase.downloadDao(mContext).getAllUser()
                val result = ArrayList<UserModel>()
                for (entity in temp) {
                    result.add(Shaft.sGson.fromJson(entity.userGson, UserModel::class.java))
                }
                allItems = result
                return result
            }
        }, object : NullCtrl<List<UserModel>>() {
            override fun success(userModels: List<UserModel>) {
                if (userModels.isNotEmpty()) {
                    for (userModel in userModels) {
                        bindData(userModel)
                    }
                }
            }
        })
    }

    private fun bindData(userModel: UserModel) {
        val binding = DataBindingUtil.inflate<RecyLocalUserBinding>(
            LayoutInflater.from(mContext),
            R.layout.recy_local_user,
            null,
            false,
        )
        binding.userName.text = String.format(
            "%s (%s)",
            userModel.user.name,
            userModel.user.account,
        )
        binding.loginTime.text = if (TextUtils.isEmpty(userModel.user.mail_address)) {
            "未绑定邮箱"
        } else {
            userModel.user.mail_address
        }
        Glide.with(mContext).load(GlideUtil.getHead(userModel.user)).into(binding.userHead)
        if (Shaft.sUserModel != null &&
            Shaft.sUserModel.user != null &&
            userModel.user.id == Shaft.sUserModel.user.id
        ) {
            binding.currentUser.visibility = View.VISIBLE
        } else {
            binding.currentUser.visibility = View.GONE
        }
        binding.exportUser.setOnClickListener {
            userModel.local_user = Params.USER_KEY
            val userJson = Shaft.sGson.toJson(userModel)
            Common.copy(mContext, userJson, false)
            Common.showToast("已导出到剪切板", 2)
        }
        binding.root.setOnClickListener {
            Local.saveUser(userModel)
            Shaft.sUserModel = userModel
            Common.restart(true)
            mActivity.finish()
        }
        binding.root.setOnLongClickListener {
            Common.copy(mContext, userModel.user.account.toString())
            true
        }
        baseBind.userList.addView(binding.root)
    }
}
