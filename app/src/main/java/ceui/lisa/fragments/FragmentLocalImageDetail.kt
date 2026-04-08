package ceui.lisa.fragments

import android.os.Bundle
import android.text.TextUtils
import ceui.lisa.R
import ceui.lisa.databinding.FragmentImageDetailLocalBinding
import ceui.lisa.utils.Params
import xyz.zpayh.hdimage.state.ScaleType

class FragmentLocalImageDetail : BaseFragment<FragmentImageDetailLocalBinding>() {
    private var filePath: String? = null

    companion object {
        @JvmStatic
        fun newInstance(filePath: String?): FragmentLocalImageDetail {
            val args = Bundle()
            args.putString(Params.FILE_PATH, filePath)
            val fragment = FragmentLocalImageDetail()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initBundle(bundle: Bundle) {
        filePath = bundle.getString(Params.FILE_PATH)
    }

    override fun initLayout() {
        mLayoutID = R.layout.fragment_image_detail_local
    }

    override fun initView() {
        if (!TextUtils.isEmpty(filePath) && filePath!!.contains(".zip")) {
            baseBind.illustImage.setScaleType(ScaleType.CENTER_CROP)
            baseBind.illustImage.setImageURI("res:///${R.mipmap.zip}")
        } else {
            baseBind.illustImage.setImageURI(filePath)
        }
    }
}
