package com.swbg.mlivestreaming.ui.mine.about

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import com.swbg.mlivestreaming.BuildConfig
import com.swbg.mlivestreaming.BuildConfig.VERSION_NAME
import com.swbg.mlivestreaming.R
import com.swbg.mlivestreaming.base.MBaseActivity
import com.swbg.mlivestreaming.provider.StoredUserSources
import com.swbg.mlivestreaming.singleClick
import com.swbg.mlivestreaming.ui.mine.MineViewModel
import com.swbg.mlivestreaming.utils.ActivityUtils
import com.swbg.mlivestreaming.utils.AppUtils
import kotlinx.android.synthetic.main.activity_about_us.*
import kotlinx.android.synthetic.main.title_bar_simple.*

class AboutUsActivity : MBaseActivity() {
    override val contentViewLayoutId: Int
        get() = R.layout.activity_about_us

    override fun initView() {
        iftTitle.text = "关于我们"
        ibReturn.singleClick {
            onBackPressed()
        }

        val settingData = StoredUserSources.getSettingData()
        tv_name.text = "${AppUtils.getAppName(this) ?: "${BuildConfig.APP_NAME_}"}$VERSION_NAME"
        tv_version.text = VERSION_NAME
        tv_office_group.text = settingData?.group
        tv_office_group.singleClick {
            settingData?.group?.let {
                ActivityUtils.jumpToWebView(it, this)
            }
        }
        tv3.singleClick {
            mineViewModel.getVersionUpdate()
        }
        mineViewModel.getVersionUpdate()
    }

    private val mineViewModel by getViewModel(MineViewModel::class.java) {
        _verionUpdate.observe(it, Observer {
            it?.let {
                VERSION_NAME.split(".").mapIndexed { index, s ->
                }
//                ToastUtils.showToast(getString(R.string.already_newest_version),true)
            }
        })
    }

    companion object {
        fun open(context: Context?): Intent {
            return Intent(context, AboutUsActivity::class.java)
        }
    }
}
