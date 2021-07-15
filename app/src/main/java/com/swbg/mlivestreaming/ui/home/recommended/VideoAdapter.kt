package com.swbg.mlivestreaming.ui.home.recommended

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.iyao.recyclerviewhelper.adapter.CacheViewHolder
import com.iyao.recyclerviewhelper.adapter.CachedAutoRefreshAdapter
import com.swbg.mlivestreaming.R
import com.swbg.mlivestreaming.bean.VideoBean
import com.swbg.mlivestreaming.loadGlobleVideo
import com.swbg.mlivestreaming.singleClick
import com.swbg.mlivestreaming.ui.home.play.VideoPlayActivity
import kotlinx.android.synthetic.main.item_video.view.*

class VideoAdapter(private val listener: () -> Unit, var context: Context?) :
    CachedAutoRefreshAdapter<VideoBean>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CacheViewHolder {
        return CacheViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_video, parent, false)).apply {
            itemView.apply {}
        }
    }

    override fun onBindViewHolder(holder: CacheViewHolder, position: Int) {
        val videoBean = get(position)
        holder.itemView.apply {
            loadGlobleVideo(context,videoBean.faceImageUrl,iv_video)
//            Glide.with(context).load(videoBean.faceImageUrl).error(R.drawable.drawable_video_error_bg).into(iv_video)
            tv_video_title.text = videoBean.videoTitle ?: ""
            singleClick {
                listener.invoke()
                context.startActivity(VideoPlayActivity.open(context,videoBean.id))
            }
        }
    }

}
