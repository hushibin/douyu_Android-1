package com.mp.douyu.ui.video

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.Jzvd
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.mp.douyu.GlobeStatusViewHolder
import com.mp.douyu.R
import com.mp.douyu.adapter.ShortVideoAdapter
import com.mp.douyu.base.MBaseActivity
import com.mp.douyu.base.VisibilityFragment
import com.mp.douyu.bean.ShortVideoBean
import com.mp.douyu.dialog.CommentBottomDialog
import com.mp.douyu.provider.TokenProvider
import com.mp.douyu.relation.RelationManager
import com.mp.douyu.ui.guide.GuideFragment
import com.mp.douyu.ui.login_register.LoginActivity
import com.mp.douyu.ui.mine.IWantExtensionActivity
import com.mp.douyu.ui.mine.space.UserSpaceActivity
import com.mp.douyu.ui.square.RelationViewModel
import com.mp.douyu.view.OnViewPagerListener
import com.mp.douyu.view.ViewPagerLayoutManager
import kotlinx.android.synthetic.main.video_fragment_short_video.*
import kotlinx.android.synthetic.main.video_fragment_short_video.refreshLayout
import kotlinx.android.synthetic.main.video_recycle_item_short_video.view.*

class ShortVideoFragment : VisibilityFragment() {
    private var currentPage: Int = 1
    private var pageSize: Int = 10
    private var currentPosition = -1;

    companion object {
        const val TYPE = "type"
        const val TYPE_RECOMMEND = 0
        const val TYPE_HOT = 1
        fun newInstance(type: Int): ShortVideoFragment {
            val fragment = ShortVideoFragment()
            val bundle = Bundle()
            bundle.putInt(GuideFragment.TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }

    val type by lazy {
        arguments?.getInt(TYPE, TYPE_RECOMMEND)
    }
    val mAdapter by lazy {
        ShortVideoAdapter({}, context).apply {
            setListener(object : ShortVideoAdapter.OnShortVideoListener {
                override fun onLikeClick(position: Int) {
                    if (GlobeStatusViewHolder.isNotNeedLogin(activity as MBaseActivity)) {
                        currentPosition = position
                        when (get(position).is_like_short_video) {
                            0 -> likeShortVideo(position)
                            else -> unLikeShortVideo(position)
                        }
                    }
                }

                override fun onCommentClick(id: Int?) {
                    XPopup.Builder(activity).moveUpToKeyboard(false) //????????????????????????????????????????????????????????????
                        .asCustom(CommentBottomDialog(activity!!, id)).show()
//                    CommentDialog(id).show(childFragmentManager, "comment")
                }

                override fun onHeaderClick(position: Int) {
                    startActivityWithTransition(UserSpaceActivity.open(activity!!,
                        get(position).user?.id))
                }

                override fun onRepostClick(position: Int) {
                    //??????
                    if (GlobeStatusViewHolder.isNotNeedLogin(activity as MBaseActivity)) {
                        if (GlobeStatusViewHolder.isNotNeedLogin(activity as MBaseActivity)) {
                            startActivityWithTransition(IWantExtensionActivity.open(context))
                        }
                    }
                }

                override fun onFollow(position: Int) {
                    if (GlobeStatusViewHolder.isNotNeedLogin(activity as MBaseActivity)) {
                        currentPosition = position
                        when (get(position).is_follow) {
                            0 -> follow(position)
                            else -> unFollow(position)
                        }
                    }
                }

            })
        }

    }

    private fun follow(position: Int) {
        currentPosition = position
        videoViewModel.follow(hashMapOf("to_id" to "${mAdapter.data[position].user?.id}"))
    }

    private fun unFollow(position: Int) {
        currentPosition = position
        videoViewModel.cancelFollow(hashMapOf("to_id" to "${mAdapter.data[position].user?.id}"))
    }

    private fun likeShortVideo(position: Int) {
        currentPosition = position
        videoViewModel.likeVideo(hashMapOf("video_id" to "${mAdapter.data[position].id}"))
    }

    private fun unLikeShortVideo(position: Int) {
        currentPosition = position
        videoViewModel.unlikeVideo(hashMapOf("video_id" to "${mAdapter.data[position].id}"))
    }

    val mViewPagerLayoutManager by lazy {
        ViewPagerLayoutManager(activity, OrientationHelper.VERTICAL).apply {
            setOnViewPagerListener(object : OnViewPagerListener {
                override fun onInitComplete() {
                    Log.e(TAG, "onInitComplete")
                    //?????????????????????
                    autoPlayVideo(0)
                }

                override fun onPageRelease(isNext: Boolean, position: Int) {
                    Log.e(TAG, "????????????:position=${position},?????????:isNext=${isNext}")
                    if (currentPosition == position) {
                        Jzvd.releaseAllVideos()
                    }
                }

                override fun onPageSelected(position: Int, isBottom: Boolean) {
                    Log.e(TAG, "????????????:positon=${position},????????????????????????:isBottom=${isBottom}")
                    if (currentPosition != position) {
                        currentPosition = position
                        //????????????????????????
                        videoViewModel.getPlayState(hashMapOf("video_id" to "${mAdapter[position].id}"))
//                        autoPlayVideo(position)
                    }
                }
            })
        }
    }
    override val layoutId: Int
        get() = R.layout.video_fragment_short_video

    override fun initView() {
    }

    override fun onVisible() {
        Log.e(TAG, "onVisible")
        super.onVisible()
    }

    override fun onInvisible() {
        super.onInvisible()
        Log.e(TAG, "onInvisible")
        Jzvd.releaseAllVideos()
    }

    override fun onVisibleFirst() {
        recyclerView.apply {
            adapter = mAdapter
            layoutManager = mViewPagerLayoutManager
            addOnChildAttachStateChangeListener(object :
                RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewDetachedFromWindow(view: View) {
                    Log.e(TAG, "onChildViewDetachedFromWindow")
                    if (jz_player != null && Jzvd.CURRENT_JZVD != null && jz_player.jzDataSource != null && jz_player.jzDataSource.containsTheUrl(
                            Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)
                    ) {
                        if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                            Jzvd.releaseAllVideos()
                        }
                    }
                }

                override fun onChildViewAttachedToWindow(view: View) {

                }
            })
        }
        refreshLayout.apply {
            setRefreshHeader(ClassicsHeader(context))
            setRefreshFooter(ClassicsFooter(context))
            setOnRefreshListener {
                currentPage = 1
                loadData()
            }
            setOnLoadMoreListener { refreshlayout ->
                currentPage++
                loadData()
            }

        }
        loadData()

    }

    private fun loadData() {
        when (type) {
            TYPE_RECOMMEND -> videoViewModel.getRecommendVideoData(hashMapOf("list_rows" to "${pageSize}",
                "page" to "${currentPage}"))
            TYPE_HOT -> videoViewModel.getPoplarVideoData(hashMapOf("list_rows" to "${pageSize}",
                "page" to "${currentPage}"))
        }

    }

    override fun onVisibleExceptFirst() {
        super.onVisibleExceptFirst()
        relationViewModel.getAllFollowsData(hashMapOf())
        relationViewModel.getAllShortVideoLikesData(hashMapOf())
    }


    private val videoViewModel by getViewModel(ShortVideoViewModel::class.java) {
        popularVideoData.observe(it, Observer {
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()
            it?.let {
                it.data?.let { it1 ->
                    if (it1.size < pageSize) refreshLayout.finishLoadMoreWithNoMoreData()
                    if (currentPage == 1) {
                        mAdapter.clear()
                    }
                    it1.forEach { shortVideoBean ->
                        setActionRelationData(shortVideoBean)
                    }
                    mAdapter.addAll(it1)
                }
            }
        })
        recommendVideoData.observe(it, Observer {
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()
            it?.let {
                it.data.let { it1 ->
                    if (it1.size < pageSize) refreshLayout.finishLoadMoreWithNoMoreData()
                    if (currentPage == 1) {
                        mAdapter.clear()
                    }
                    it1.forEach { shortVideoBean ->
                        setActionRelationData(shortVideoBean)
                    }
                    mAdapter.addAll(it1)
                }

            }
        })

        _likeVideoResult.observe(it, Observer {
            it?.let {
//                if (it) {
//                mAdapter[currentPosition].is_like_short_video = 1
//                mAdapter.notifyItemChanged(currentPosition)
//                }
                relationViewModel.getAllShortVideoLikesData(hashMapOf())
            }
        })
        _unlikeVideoResult.observe(it, Observer {
            it?.let {
//                if (it) {
                relationViewModel.getAllShortVideoLikesData(hashMapOf())
//                mAdapter[currentPosition].is_like_short_video = 0
//                mAdapter.notifyItemChanged(currentPosition)
//                }
            }
        })

        _followResult.observe(it, Observer {
            it?.let {
                relationViewModel.getAllFollowsData(hashMapOf())
//                mAdapter[currentPosition].is_follow = 1
//                mAdapter.notifyItemChanged(currentPosition)

            }
        })
        _cancelFollowResult.observe(it, Observer {
            it?.let {
                relationViewModel.getAllFollowsData(hashMapOf())
//                mAdapter[currentPosition].is_follow = 0
//                mAdapter.notifyItemChanged(currentPosition)
            }
        })
        _playStateData.observe(it, Observer {
            it?.let { message ->
                if ("??????????????????" == message) {
                    //??????????????????
                    if (!TokenProvider.get().hasToken()) {
                        (activity as MBaseActivity).startActivityWithTransition(LoginActivity.open(activity))
                    }
                } else if ("????????????" == message) {
                    //??????
                    autoPlayVideo(0)
                } else {
                    //??????
                    autoPlayVideo(0)
                }
            }
        })

    }
    private val relationViewModel by getViewModel(RelationViewModel::class.java) {
        shortVideoLikesData.observe(it, Observer {
            it?.let { it1 ->
                RelationManager.instance.shortVideoLikes = it1.data.toMutableList()
                for ((index, shortVideoBean) in mAdapter.data.withIndex()) {
                    val initialIsLike = shortVideoBean.is_like_short_video
                    val initialIsFollow = shortVideoBean.is_follow
                    if (it1.data.isEmpty()) shortVideoBean.is_like_short_video = 0
                    else setActionRelationData(shortVideoBean)
                    if (initialIsLike != shortVideoBean.is_like_short_video || initialIsFollow != shortVideoBean.is_follow) {
                        mAdapter.notifyItemChanged(index)
                    }
                }
            }
        })
        followsData.observe(it, Observer {
            it?.let { it1 ->
                RelationManager.instance.follows = it1.toMutableList()
                for ((index, shortVideoBean) in mAdapter.data.withIndex()) {
                    val initialIsLike = shortVideoBean.is_like_short_video
                    val initialIsFollow = shortVideoBean.is_follow
                    if (it1.isEmpty()) shortVideoBean.is_follow = 0
                    else setActionRelationData(shortVideoBean)
                    if (initialIsLike != shortVideoBean.is_like_short_video || initialIsFollow != shortVideoBean.is_follow) {
                        mAdapter.notifyItemChanged(index)
                    }
                }
            }
        })
    }

    private fun setActionRelationData(shortVideoBean: ShortVideoBean) {
        //????????????????????????
        for (userBean in RelationManager.instance.follows) {
            if (userBean.id == shortVideoBean.user?.id) {
                shortVideoBean.is_follow = 1
                break
            } else {
                shortVideoBean.is_follow = 0
            }
        }
        //??????????????????????????????
        for (likeShortVideoBean in RelationManager.instance.shortVideoLikes) {
            if (likeShortVideoBean.pivot?.video_id == shortVideoBean.id) {
                shortVideoBean.is_like_short_video = 1
                break
            } else {
                shortVideoBean.is_like_short_video = 0
            }
        }
    }

    private fun autoPlayVideo(position: Int) {
        recyclerView?.apply {
            getChildAt(0)?.apply {
                jz_player?.apply {
                    startVideoAfterPreloading()
                }
            }
        }
    }

}

