package ceui.lisa.fragments

import android.os.Bundle
import ceui.lisa.R
import ceui.lisa.adapters.BaseAdapter
import ceui.lisa.adapters.NAdapter
import ceui.lisa.core.RemoteRepo
import ceui.lisa.databinding.FragmentBaseListBinding
import ceui.lisa.databinding.RecyNovelBinding
import ceui.lisa.model.ListNovel
import ceui.lisa.models.NovelBean
import ceui.lisa.repo.UserNovelRepo
import ceui.lisa.utils.Params

class FragmentUserNovel : NetListFragment<FragmentBaseListBinding, ListNovel, NovelBean>() {
    private var userID = 0

    companion object {
        @JvmStatic
        fun newInstance(userID: Int): FragmentUserNovel {
            val args = Bundle()
            args.putInt(Params.USER_ID, userID)
            val fragment = FragmentUserNovel()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        userID = bundle.getInt(Params.USER_ID)
    }

    override fun repository(): RemoteRepo<ListNovel> = UserNovelRepo(userID)

    override fun adapter(): BaseAdapter<NovelBean, RecyNovelBinding> = NAdapter(allItems, mContext)

    override fun getToolbarTitle(): String = getString(R.string.string_237)
}
