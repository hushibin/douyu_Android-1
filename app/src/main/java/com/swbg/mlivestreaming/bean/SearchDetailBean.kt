package com.swbg.mlivestreaming.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import com.swbg.mlivestreaming.adapter.SearchDetailAdapter.Companion.ITEM_VIEW_ANCHOR
import com.swbg.mlivestreaming.adapter.SearchDetailAdapter.Companion.ITEM_VIEW_DYNAMIC
import com.swbg.mlivestreaming.adapter.SearchDetailAdapter.Companion.ITEM_VIEW_HEADER
import com.swbg.mlivestreaming.adapter.SearchDetailAdapter.Companion.ITEM_VIEW_VIDEO
import com.swbg.mlivestreaming.interfaces.ItemViewType
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class SearchDetailBean(
    var id:Int?=0,
    var title:String? =null,
    var isMore:Boolean? =false,
    var videos:MutableList<VideoBean> = arrayListOf(),
    var lives:MutableList<LiveBean> = arrayListOf(),
    var dynamicBean: DynamicBean? =null
):Parcelable,ItemViewType {
    override fun getItemViewType(): Int {
        if (title?.isNotBlank()==true) {
            return ITEM_VIEW_HEADER
        }else if (videos .isNotEmpty()) {
            return ITEM_VIEW_VIDEO
        }else if (lives.isNotEmpty()) {
            return ITEM_VIEW_ANCHOR
        }else if (dynamicBean != null) {
            return ITEM_VIEW_DYNAMIC
        }
    return ITEM_VIEW_DYNAMIC
    }
}