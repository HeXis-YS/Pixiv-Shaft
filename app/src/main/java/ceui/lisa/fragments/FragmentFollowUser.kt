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
import ceui.lisa.repo.FollowUserRepo
import ceui.lisa.utils.Params

class FragmentFollowUser : NetListFragment<FragmentBaseListBinding, ListUser, UserPreviewsBean>() {
    private var userID = 0
    private var starType: String? = null
    private var shouldShowToolbar = false

    companion object {
        @JvmStatic
        fun newInstance(userID: Int, starType: String?, pShowToolbar: Boolean): FragmentFollowUser {
            val args = Bundle()
            args.putInt(Params.USER_ID, userID)
            args.putString(Params.STAR_TYPE, starType)
            args.putBoolean(Params.FLAG, pShowToolbar)
            val fragment = FragmentFollowUser()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        userID = bundle.getInt(Params.USER_ID)
        starType = bundle.getString(Params.STAR_TYPE)
        shouldShowToolbar = bundle.getBoolean(Params.FLAG)
    }

    override fun repository(): RemoteRepo<ListUser> = FollowUserRepo(userID, starType)

    override fun adapter(): BaseAdapter<UserPreviewsBean, RecyUserPreviewBinding> = UAdapter(allItems, mContext)

    override fun showToolbar(): Boolean = shouldShowToolbar

    override fun getToolbarTitle(): String = getString(R.string.string_232)
}
