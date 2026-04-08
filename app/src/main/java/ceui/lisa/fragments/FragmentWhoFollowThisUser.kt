package ceui.lisa.fragments

import android.os.Bundle
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.UAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyUserPreviewBinding
import ceui.lisa.model.ListUser
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.repo.WhoFollowThisUserRepo
import ceui.lisa.utils.Params

class FragmentWhoFollowThisUser : NetListFragment<FragmentBaseListBinding, ListUser, UserPreviewsBean>() {
    private var userID = 0

    companion object {
        @JvmStatic
        fun newInstance(userId: Int): FragmentWhoFollowThisUser {
            val args = Bundle()
            args.putInt(Params.USER_ID, userId)
            val fragment = FragmentWhoFollowThisUser()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        userID = bundle.getInt(Params.USER_ID)
    }

    override fun repository(): RemoteRepo<ListUser> = WhoFollowThisUserRepo(userID)

    override fun adapter(): BaseAdapter<UserPreviewsBean, RecyUserPreviewBinding> = UAdapter(allItems, mContext)

    override fun getToolbarTitle(): String = getString(R.string.string_264)
}
