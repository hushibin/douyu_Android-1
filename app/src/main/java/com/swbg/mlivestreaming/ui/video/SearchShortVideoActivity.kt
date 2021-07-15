package com.swbg.mlivestreaming.ui.video

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import com.swbg.mlivestreaming.R
import com.swbg.mlivestreaming.base.MBaseActivity
import com.swbg.mlivestreaming.bean.DynamicBean
import com.swbg.mlivestreaming.bean.ShortVideoBean
import com.swbg.mlivestreaming.inTransaction
import kotlinx.android.synthetic.main.square_fragment_dymaic_comment.*

class SearchShortVideoActivity:MBaseActivity() {
    var fragment:SearchShortVideoFragment? =null
    override val contentViewLayoutId: Int
        get() = R.layout.activity_user_sapce

    override fun initView() {
        val data = intent.getParcelableArrayListExtra<ShortVideoBean>(VIDEO_DATA)
        val position =intent.getIntExtra(INITIAL_POSITION,0)
        fragment = SearchShortVideoFragment.newInstance(data,position)
        supportFragmentManager.inTransaction {
            add(R.id.container, fragment!!)
        }
//    ActivityUtils.add(supportFragmentManager,R.id.container,SpaceFragment(),"user_space")
    }
    companion object {
        const val VIDEO_DATA = "data"
        const val INITIAL_POSITION = "position"
        fun open(context:Context?, data: ArrayList<ShortVideoBean>,position: Int = 0):Intent{
            return Intent(context,SearchShortVideoActivity::class.java).apply {
                putExtra(VIDEO_DATA,data)
                putExtra(INITIAL_POSITION,position)

            }
        }
    }

}