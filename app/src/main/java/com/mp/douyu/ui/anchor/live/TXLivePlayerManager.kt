package com.mp.douyu.ui.anchor.live

import android.content.Context
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLivePlayer

class TXLivePlayerManager private constructor(context: Context) {
    companion object : SingletonHolder<TXLivePlayerManager, Context>(::TXLivePlayerManager)

    val livePlayer:TXLivePlayer by lazy {
        TXLivePlayer(context).apply {
            setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN)
        }
    }
}

open class SingletonHolder<out T, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }

    //对上述方法的一种更简洁的写法
    fun getInstance2(arg: A): T = instance ?: synchronized(this) {
        instance ?: creator!!(arg).apply {
            instance = this
        }
    }

}