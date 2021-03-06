package com.mp.douyu.ui.home.nv_info

import android.os.Bundle
import com.mp.douyu.R
import com.mp.douyu.base.MBaseFragment
import kotlinx.android.synthetic.main.fragment_nv_info_introduce.*

class NvInfoIntroduceFragment : MBaseFragment() {
    override val layoutId: Int
        get() = R.layout.fragment_nv_info_introduce

    override fun initView() {
    }

    fun setContent(s: String?) {
        tv_content.text = s
    }

    companion object {
        const val LIST_TYPE = "list_type"
        fun newInstance(datas: String?): NvInfoIntroduceFragment {
            val fragment = NvInfoIntroduceFragment()
            val bundle = Bundle()
            bundle.putString(LIST_TYPE, datas)
            fragment.arguments = bundle
            return fragment
        }
    }

}