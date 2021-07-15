package com.swbg.mlivestreaming.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.swbg.mlivestreaming.R
import com.swbg.mlivestreaming.base.MBaseFragment
import com.swbg.mlivestreaming.bean.ActivityBean
import kotlinx.android.synthetic.main.fragment_activity.*
import kotlinx.android.synthetic.main.title_bar_simple.*

class ActivityFragment : MBaseFragment() {
    private var currentPage: Int = 1

    override val layoutId: Int
        get() = R.layout.fragment_activity

    override fun initView() {
        ibReturn.visibility = View.GONE
        iftTitle.text = "优惠活动"
        titleBar.setBackgroundResource(R.color.color00000000)
        rv_activity.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(context)
        }

        refreshLayout.setRefreshHeader(ClassicsHeader(context))
        refreshLayout.setRefreshFooter(ClassicsFooter(context))
        refreshLayout.setOnRefreshListener {
            currentPage = 1
            loadBegin()
        }
        refreshLayout.setOnLoadMoreListener { refreshlayout ->
            currentPage += 1
            loadBegin()
        }
        loadBegin()
    }

    private val homeViewModel by getViewModel(ActivityViewModel::class.java) {
        _getActivityList.observe(it, Observer {
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()
            it?.let {
                if (currentPage == 1) {
                    it.data?.let { it1 -> mAdapter.refresh(it1, null) }
                } else {
                    it.data?.let { it1 -> mAdapter.addAll(it1) }
                }
            }
        })
    }


    private fun loadBegin() {
        homeViewModel.getActivityList(hashMapOf(
            "size" to "10",
            "page" to "${currentPage}"))
    }



    private val mAdapter by lazy {
        ActivityAdapter(context)
    }
}