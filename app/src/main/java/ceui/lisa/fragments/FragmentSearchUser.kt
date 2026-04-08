package ceui.lisa.fragments

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import ceui.lisa.activities.TemplateActivity
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.UAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyUserPreviewBinding
import ceui.lisa.model.ListUser
import ceui.lisa.models.UserPreviewsBean
import ceui.lisa.repo.SearchUserRepo
import ceui.lisa.utils.Params
import ceui.lisa.viewmodel.SearchModel

class FragmentSearchUser : NetListFragment<FragmentBaseListBinding, ListUser, UserPreviewsBean>() {
    private var word: String? = null
    private lateinit var searchModel: SearchModel

    companion object {
        @JvmStatic
        fun newInstance(word: String?): FragmentSearchUser {
            val args = Bundle()
            args.putString(Params.KEY_WORD, word)
            val fragment = FragmentSearchUser()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchModel.nowGo.observe(viewLifecycleOwner) {
            val keyWord = searchModel.keyword.value
            if (!TextUtils.isEmpty(keyWord)) {
                (mRemoteRepo as SearchUserRepo).update(keyWord!!)
                mRefreshLayout.autoRefresh()
            }
        }
    }

    override fun initModel() {
        searchModel = ViewModelProvider(requireActivity())[SearchModel::class.java]
        super.initModel()
    }

    override fun initBundle(bundle: Bundle) {
        word = bundle.getString(Params.KEY_WORD)
    }

    override fun repository(): RemoteRepo<ListUser> = SearchUserRepo(word)

    override fun adapter(): BaseAdapter<UserPreviewsBean, RecyUserPreviewBinding> = UAdapter(allItems, mContext)

    override fun showToolbar(): Boolean {
        val activity: Activity? = activity
        return activity is TemplateActivity
    }
}
