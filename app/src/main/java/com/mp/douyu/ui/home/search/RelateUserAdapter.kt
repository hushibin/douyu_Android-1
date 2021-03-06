package com.mp.douyu.ui.home.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.iyao.recyclerviewhelper.adapter.CacheViewHolder
import com.iyao.recyclerviewhelper.adapter.CachedAutoRefreshAdapter
import com.mp.douyu.R
import com.mp.douyu.bean.SearchUserBean

class RelateUserAdapter(private val context: Context) : CachedAutoRefreshAdapter<SearchUserBean>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CacheViewHolder {
        return CacheViewHolder(LayoutInflater.from(context).inflate(R.layout.item_search_user,parent,false))
    }

    override fun onBindViewHolder(holder: CacheViewHolder, position: Int) {
    }

}
