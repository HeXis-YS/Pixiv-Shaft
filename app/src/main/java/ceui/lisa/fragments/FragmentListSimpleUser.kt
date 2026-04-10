package ceui.lisa.fragments

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.SimpleUserAdapter
import ceui.lisa.core.BaseRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.model.ListSimpleUser
import ceui.lisa.models.IllustsBean
import ceui.lisa.models.UserBean
import ceui.lisa.repo.SimpleUserRepo
import ceui.lisa.utils.Params

class FragmentListSimpleUser : NetListFragment<FragmentBaseListBinding, ListSimpleUser, UserBean>() {
    private var illustsBean: IllustsBean? = null

    companion object {
        @JvmStatic
        fun newInstance(illustsBean: IllustsBean): FragmentListSimpleUser {
            val args = Bundle()
            args.putSerializable(Params.CONTENT, illustsBean)
            val fragment = FragmentListSimpleUser()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        illustsBean = bundle.getSerializable(Params.CONTENT) as IllustsBean
    }

    override fun adapter(): BaseAdapter<*, out ViewBinding> = SimpleUserAdapter(allItems, mContext)

    override fun repository(): BaseRepo = SimpleUserRepo(illustsBean!!.id)

    override fun getToolbarTitle(): String = "喜欢${illustsBean!!.title}的用户"
}
