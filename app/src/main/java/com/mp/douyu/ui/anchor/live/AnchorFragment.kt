package com.mp.douyu.ui.anchor.live

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.Glide
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.mp.douyu.*
import com.mp.douyu.adapter.LiveAudienceInfoAdapter
import com.mp.douyu.adapter.LiveRoomChatAdapter
import com.mp.douyu.base.MBaseActivity
import com.mp.douyu.bean.*
import com.mp.douyu.dialog.GiftListDialog
import com.mp.douyu.dialog.TransferDialog
import com.mp.douyu.im.ImManager
import com.mp.douyu.ui.anchor.AnchorViewModel
import com.mp.douyu.ui.anchor.live.vip.LiveVipListActivity
import com.mp.douyu.ui.home.recommended.HomeRecommendedChessActivity
import com.mp.douyu.ui.mine.IWantExtensionActivity
import com.mp.douyu.ui.mine.MineViewModel
import com.mp.douyu.ui.mine.walnut.ChargeCenterActivity
import com.mp.douyu.ui.square.RelationViewModel
import com.mp.douyu.ui.video.comment.CommentViewModel
import com.mp.douyu.utils.DisplayUtils
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo
import com.tencent.imsdk.v2.V2TIMMessage
import com.tencent.imsdk.v2.V2TIMUserInfo
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.tencent.liteav.demo.beauty.model.ItemInfo
import com.tencent.liteav.demo.beauty.model.TabInfo
import com.tencent.liteav.demo.beauty.view.BeautyPanel
import com.tencent.rtmp.*
import io.alterac.blurkit.BlurKit
import kotlinx.android.synthetic.main.layout_live_medal_list.*
import kotlinx.android.synthetic.main.layout_live_pusher_info.*
import kotlinx.android.synthetic.main.layout_live_pusher_info.iv_anchor_head_icon
import kotlinx.android.synthetic.main.layout_live_right.*
import kotlinx.android.synthetic.main.live_fragment_anchor.*
import kotlinx.android.synthetic.main.live_fragment_anchor.cl_gift
import kotlinx.android.synthetic.main.live_fragment_anchor.container
import kotlinx.android.synthetic.main.live_fragment_anchor.iv_gift
import kotlinx.android.synthetic.main.live_fragment_anchor.keyboard
import kotlinx.android.synthetic.main.live_fragment_anchor.recycle_chat
import kotlinx.android.synthetic.main.live_fragment_anchor.tv_gift_num
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import java.util.HashMap
import kotlin.random.Random


class AnchorFragment(var bean: LiveStreamingBean? = null) : BaseLiveFragment() {
    private var currentPage: Int = 1
    private var pageSize: Int = 10
    private var currentPosition = -1;

    var danmakuContext: DanmakuContext? = null
    var danmakuParser: BaseDanmakuParser? = null
    var listAvatars: MutableList<CommonUserBean> = arrayListOf()
    var isOpenDanmaku: Boolean = false
    val mChatAdapter by lazy {
        LiveRoomChatAdapter({}, context).apply {
//            add(ChatMsgBean(itemViewType = LiveRoomChatAdapter.TYPE_TIP))
        }
    }
    val mAudienceAvatar by lazy {
        LiveAudienceInfoAdapter({}, context).apply {
            initializeWidth(data, recycle_audience, 40, 3)
        }
    }

    companion object {
        const val LIVE_DATA = "data"
        fun newInstance(bean: LiveStreamingBean): AnchorFragment {
            val fragment = AnchorFragment()
            val bundle = Bundle()
            bundle.putParcelable(LIVE_DATA, bean)
            fragment.arguments = bundle
            return fragment
        }

    }


    private fun follow(position: Int) {
//        anchorViewModel.followAnchor(hashMapOf("anchor_id" to "${mAdapter.data[position].id}"))
    }

    private fun unFollow(position: Int) {
//        anchorViewModel.cancelFollowAnchor(hashMapOf("anchor_id" to "${mAdapter.data[position].id}"))
    }

    override val layoutId: Int
        get() = R.layout.live_fragment_anchor

    override fun initView() {
    }

    override fun onVisible() {
        super.onVisible()


    }

    override fun onInvisible() {
        super.onInvisible()
    }

    override fun onVisibleFirst() {
        super.onVisibleFirst()
        bean = arguments?.getParcelable<LiveStreamingBean>(LIVE_DATA)?.apply {
            Glide.with(context!!).load(BuildConfig.IMAGE_BASE_URL + image).circleCrop()
                .placeholder(R.mipmap.default_avatar).into(iv_anchor_head_icon)
            tv_anchor_name.text = user?.nickname
            tv_hot_num.text = "${hot}"
            tv_medal_num.text = "${gold}"
            startPusher(this.push_url)
        }
        iv_follow_status.visibility = View.GONE
        recycle_chat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mChatAdapter
        }
        recycle_audience.apply {

            adapter = mAudienceAvatar
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        }
        initBeautyListener()
        initKeyboard(keyboard)
        loadData()
        initOnClick()
        initDanmaku(danmaku)
        mineViewModel.getUserAvatar()
    }

    private fun initBeautyListener() {
        beauty_panel.setOnBeautyListener(object : BeautyPanel.OnBeautyListener {
            override fun onClick(tabInfo: TabInfo?, tabPosition: Int, itemInfo: ItemInfo?, itemPosition: Int): Boolean {
                return false
            }

            override fun onTabChange(tabInfo: TabInfo?, position: Int) {

            }

            override fun onLevelChanged(tabInfo: TabInfo?, tabPosition: Int, itemInfo: ItemInfo?, itemPosition: Int, beautyLevel: Int): Boolean {
                return false
            }

            override fun onClose(): Boolean {
                beauty_panel.visibility = View.GONE
                tool_bar.visibility = View.VISIBLE
                return true
            }
        })
    }


    private fun initOnClick() {

        btn_message_input.singleClick {
            onSendClick()
        }
        anchor_btn_flash.singleClick {

        }
        switch_cam.singleClick {
            if (mLivePusher != null) {
                mLivePusher.switchCamera()
            }
        }
        beauty_btn.singleClick {
            if (beauty_panel.isShown) {
                beauty_panel.visibility = View.GONE
                tool_bar.visibility = View.VISIBLE
            } else {
                beauty_panel.visibility = View.VISIBLE
                tool_bar.visibility = View.GONE
            }
        }
        btn_audio_ctrl.singleClick {

        }
        btn_close.singleClick {
            onCloseClick()
        }
        iv_recharge.singleClick {
            onRechargeClick()
        }
        iv_yp.singleClick {
            onHookClick()
        }
        iv_right_share.singleClick {
            onShareClick()
        }
        iv_bet.singleClick {
            onBetGameClick()
        }
        layout_live_medal_list.singleClick {
            onRankListClick()
        }
        layout_live_pusher_info.singleClick {
            onHeaderClick()
        }
        iv_back.singleClick {
            stopDialog.show()
            //??????
        }
        iv_danmaku.singleClick {
            iv_danmaku.isSelected = !iv_danmaku.isSelected
            isOpenDanmaku = iv_danmaku.isSelected
            if (isOpenDanmaku) {
                danmaku.show()
            } else {
                danmaku.hide()
            }
        }
        iv_share.singleClick {
            onShareClick()
        }
    }
    val stopDialog by lazy {
        XPopup.Builder(context).asConfirm("??????","?????????????",object :OnConfirmListener{
            override fun onConfirm() {
                var status = 1
                var groupId = "${bean?.GroupId}"
//                liveViewModel.startLive(null, null,null, groupId, status)
                liveViewModel.stopLive(null,null,groupId,status)
                sendLiveTextMsg("@#???????????????")
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        stopPusher()
    }


    private fun startPusher(pushUrl: String?) {
        val rtmpURL = pushUrl //?????????????????? rtmp ????????????
        val ret = mLivePusher.startPusher(rtmpURL?.trim())
        if (ret == -5) {
            Log.e(TAG, "startRTMPPush: license ????????????")
        }
    }

    private fun stopPusher() {
        mLivePusher.apply {
            stopPusher()
            stopCameraPreview(true)//???????????????????????????????????????????????????????????????????????????
        }
    }


    private fun loadData() {
        /*  liveViewModel.getRecommendVideoData(hashMapOf("list_rows" to "${pageSize}",
              "page" to "${currentPage}"))*/
    }

    override fun onVisibleExceptFirst() {
        super.onVisibleExceptFirst()
    }

    override fun sendLiveTextMsg(text: String) {
        ImManager.instance.sendLiveTextMessage(text,
            "${bean?.GroupId}",
            V2TIMMessage.V2TIM_PRIORITY_NORMAL,
            object : V2TIMValueCallback<V2TIMMessage> {
                override fun onSuccess(p0: V2TIMMessage?) {
                    Log.e(TAG, "?????????????????? :${p0?.message.toString()}")
                    p0?.textElem?.text?.let { fetchMsg("???",it) }
                }

                override fun onError(p0: Int, p1: String?) {
                    Log.e(TAG, "?????????????????? :code=${p0}, msg=${p1}")
                    fetchMsg("???",text)
                }
            })
    }
    private fun fetchMsg(nickname:String,textStr: String) {
        var content: String? = null
        if (textStr?.contains("{") && textStr.contains("}")) {
            val messageBean = GsonUtils.fromJson(textStr, MessageBean::class.java)
            when (messageBean.type) {
                "??????" -> {
                    val giftBean = GsonUtils.fromJson(messageBean.msg, GiftBean::class.java)
                    content = "?????????${messageBean.num}???${giftBean.title}"
                    showGiftAnim( SendGiftBean(num = messageBean.num,giftBean = giftBean))
                }
                "??????" -> {
                    content = messageBean.msg
                }

            }
        } else {
            if (textStr == "@#???????????????") {
                //?????????????????????
                finishView()
                return
            }
            content = textStr
        }
        addMessage(ChatMsgBean(nickname = nickname, content = content), true)
    }
    fun onRankListClick() {
        //?????????
        liveViewModel.getRanksData(hashMapOf("anchor_id" to "${bean?.id}"))
    }

    fun onRechargeClick() {
        //??????
        if (GlobeStatusViewHolder.isNotNeedLogin(activity as MBaseActivity)) {
            startActivityWithTransition(ChargeCenterActivity.open(context))
        }
    }

    fun onBetClick(id: Int?) {
        //??????
        XPopup.Builder(activity).asCustom(TransferDialog(activity!!)).show()
    }

    fun onHookClick() {
        //???pao

    }

    fun onBetGameClick() {
        //??????
        context?.let { it1 ->
            HomeRecommendedChessActivity.open(it1, 0)
        }?.let { it2 -> startActivityWithTransition(it2) }
    }

    fun onGiftClick() {
        //??????
        //??????RenderScript?????????????????????????????????bitmap???iv1???????????????View?????????????????????ViewGroup???25??????????????????2???????????????????????????????????????????????????OOM
        var bt = BlurKit.getInstance().blur(container, 25 )
        XPopup.Builder(activity)
            .asCustom(GiftListDialog(context =activity!!, bitmap = bt, listener = object : GiftListDialog.OnGiftListener {
                override fun sendGift(bean: SendGiftBean) {
                }
            })).show()
    }

    fun onShareClick() {
        //??????
        if (GlobeStatusViewHolder.isNotNeedLogin(activity as MBaseActivity)) {
            startActivityWithTransition(IWantExtensionActivity.open(context))
        }
    }

    fun onDanmakuClick(isOpen: Boolean, position: Int) {
        //????????????/??????
    }

    fun onSendClick() {
        //????????????????????????
        keyboard.visibility = View.VISIBLE
        keyboard.popKeyboard()
    }

    fun onVipClick(position: Int) {
        //??????vip
        startActivityWithTransition(LiveVipListActivity.open(activity))
    }

    fun onCloseClick() {
        //??????
        finishView()
    }

    fun onHeaderClick() {
        //????????????
//        XPopup.Builder(activity).asCustom(AnchorInfoDialog(activity!!, get(position))).show()
    }

    fun onFollow(position: Int) {
        currentPosition = position
//        when (get(position).isFollowLive) {
//            0 -> follow(position)
//            else -> unFollow(position)
//        }
    }

    private fun showGiftAnim(bean: SendGiftBean) {
        if (bean.giftBean?.image == null) {
            Log.e(TAG, "?????? url ??? null")
            return
        }
        cl_gift.visibility = View.VISIBLE
        if (bean.giftBean?.animation?.contains("http") == false) {
            bean.giftBean?.animation = BuildConfig.IMAGE_BASE_URL + bean.giftBean?.animation
        }
        Glide.with(context!!).load(bean.giftBean?.animation).into(iv_gift)
        tv_gift_num.text = "x${bean.num}"
        var duration = bean.giftBean?.duration
        if (duration == 0L) {
            duration = 2L
        }
        val l = duration?.times(1000)

        Handler().postDelayed(Runnable {
            cl_gift.visibility = View.GONE
        }, l!!)
    }

    override fun onMemberLeave(groupID: String?, member: V2TIMGroupMemberInfo?) {
        super.onMemberLeave(groupID, member)
        if (groupID != null && groupID == "${bean?.GroupId}") {
            addMessage(ChatMsgBean(nickname = member?.nickName, content = "???????????????"))
        }
    }

    override fun onMemberEnter(groupID: String?, memberList: MutableList<V2TIMGroupMemberInfo>?) {
        super.onMemberEnter(groupID, memberList)
        if (groupID != null && groupID == "${bean?.GroupId}") {
            memberList?.forEach { member ->
                addMessage(ChatMsgBean(nickname = member.nickName, content = "???????????????"))
            }
        }
    }

    override fun onRecvC2CTextMessage(msgID: String?, sender: V2TIMUserInfo?, text: String?) {
        super.onRecvC2CTextMessage(msgID, sender, text)
    }

    override fun onRecvC2CCustomMessage(msgID: String?, sender: V2TIMUserInfo?, customData: ByteArray?) {
        super.onRecvC2CCustomMessage(msgID, sender, customData)
    }

    override fun onRecvGroupCustomMessage(msgID: String?, groupID: String?, sender: V2TIMGroupMemberInfo?, customData: ByteArray?) {
        super.onRecvGroupCustomMessage(msgID, groupID, sender, customData)
    }

    override fun onRecvGroupTextMessage(msgID: String?, groupID: String?, sender: V2TIMGroupMemberInfo?, text: String?) {
        super.onRecvGroupTextMessage(msgID, groupID, sender, text)
        if (groupID != null && groupID == "${bean?.GroupId}") {
            fetchMsg(sender?.nickName!!,text!!)
        }
    }

    private fun addMessage(msgBean: ChatMsgBean, isSelf: Boolean = false) {
        if (isOpenDanmaku) {
            //????????????
            addDanmaku(danmaku, text = msgBean.content, isSelf = isSelf)
        } else {
            mChatAdapter?.add(msgBean)
            recycle_chat?.smoothScrollToPosition(mChatAdapter.itemCount-1)
        }

    }

    private val liveViewModel by getViewModel(LiveViewModel::class.java) {

        _likeVideoResult.observe(it, Observer {
            it?.let {
//                if (it) {
//                mAdapter[currentPosition].is_like_short_video = 1
//                mAdapter.notifyItemChanged(currentPosition)
//                }
            }
        })
        _unlikeVideoResult.observe(it, Observer {
            it?.let {
//                if (it) {
//                mAdapter[currentPosition].is_like_short_video = 0
//                mAdapter.notifyItemChanged(currentPosition)
//                }
            }
        })

        _followResult.observe(it, Observer {
            it?.let {
//                mAdapter[currentPosition].is_follow = 1
//                mAdapter.notifyItemChanged(currentPosition)

            }
        })
        _cancelFollowResult.observe(it, Observer {
            it?.let {
//                mAdapter[currentPosition].is_follow = 0
//                mAdapter.notifyItemChanged(currentPosition)
            }
        })
        _stopLiveData.observe(it, Observer {
            it?.let {
                //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//                this@AnchorFragment.finishView()

            }
        })

    }
    val mLivePusher: TXLivePusher by lazy {
        TXLivePusher(activity).apply {
            // ?????????????????????????????? config ???????????????
            config = TXLivePushConfig()
            startCameraPreview(anchor_video_view)
            setPushListener(object : ITXLivePushListener {
                override fun onNetStatus(p0: Bundle?) {
                    Log.e(TAG, "onNetStatus ")
                }

                override fun onPushEvent(event: Int, p1: Bundle?) {
                    if (event === TXLiveConstants.PUSH_EVT_PUSH_BEGIN) {
                        Log.e(TAG, "???????????? event:${event}")
                    } else if (event === TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL) {
                        val msg = "[LivePusher] ????????????[?????????????????????] event:${event}"
                        Log.e(TAG, msg)
                    } else if (event === TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL) {
                        val msg = "[LivePusher] ????????????[?????????????????????] event:${event}"
                        Log.e(TAG, msg)
                    } else if (event === TXLiveConstants.PUSH_ERR_NET_DISCONNECT || event === TXLiveConstants.PUSH_ERR_INVALID_ADDRESS) {
                        val msg = "[LivePusher] ????????????[????????????] event:${event}"
                        Log.e(TAG, msg)
                    } else if (event === TXLiveConstants.PUSH_ERR_SCREEN_CAPTURE_START_FAILED) {
                        val msg = "[LivePusher] ????????????[??????????????????] event:${event}"
                        Log.e(TAG, msg)
                    }
                }
            })
        }
    }

    private val commentViewModel by getViewModel(CommentViewModel::class.java) {

    }
    private val anchorViewModel by getViewModel(AnchorViewModel::class.java) {
        _followAnchorResult.observe(it, Observer {
            relationViewModel.getAllFollowLiveListData(hashMapOf())
        })
        _cancelFollowAnchorResult.observe(it, Observer {
            relationViewModel.getAllFollowLiveListData(hashMapOf())
        })

    }
    private val relationViewModel by getViewModel(RelationViewModel::class.java) {
        followLiveListData.observe(it, Observer {
            it?.let {
                it.data.let { it1 ->


                }
            }
        })

    }
    private val mineViewModel by getViewModel(MineViewModel::class.java) {
        _getUserAvatar.observe(it, Observer {
            it?.let {
                it.male?.let {
                    for (el in it) {
                        listAvatars.add(CommonUserBean(el.url))
                    }
                }
                it.female?.let {
                    for (el in it) {
                        listAvatars.add(CommonUserBean(el.url))
                    }
                }
                var randomList: MutableList<CommonUserBean> = randomAvatarList(listAvatars)
                mAudienceAvatar.refresh(randomList, null)
            }
        })

    }

    private fun randomAvatarList(list: MutableList<CommonUserBean>): MutableList<CommonUserBean> {
        var randomList: MutableList<CommonUserBean> = arrayListOf()
        for (index in 0..3) {
            var nextInt = Random.nextInt(list.size)
            randomList.add(list[nextInt])
        }
        return randomList
    }


    /**
     * ??????????????????
     *
     * @param text   ????????????
     * @param isSelf ?????????????????????
     */
    fun addDanmaku(danmakuView: DanmakuView, text: String?, isSelf: Boolean) {
        danmakuContext?.setCacheStuffer(SpannedCacheStuffer(), null)
        val danmaku: BaseDanmaku? =
            danmakuContext?.mDanmakuFactory?.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)
        if (danmaku == null || danmakuView == null) {
            return;
        }
        danmaku.text = text
        danmaku.priority = 3
        danmaku.isLive = false
        danmaku.time = danmakuView.getCurrentTime() + 1200
        danmaku.textSize = DisplayUtils.dp2px(if (isSelf) 20f else 12f).toFloat()
        danmaku.textColor = if (isSelf) Color.BLUE else Color.WHITE
        danmaku.textShadowColor = Color.GRAY
        danmaku.underlineColor = if (isSelf) Color.GREEN else Color.TRANSPARENT
        danmaku.borderColor = if (isSelf) Color.GREEN else Color.TRANSPARENT
        danmakuView?.addDanmaku(danmaku)
    }

    fun initDanmaku(danmakuView: DanmakuView) {
        val overlappingEnablePair = HashMap<Int, Boolean>()
        overlappingEnablePair[BaseDanmaku.TYPE_SCROLL_RL] = true
        overlappingEnablePair[BaseDanmaku.TYPE_FIX_TOP] = true
        val maxLinesPair = HashMap<Int, Int>()
        maxLinesPair[BaseDanmaku.TYPE_SCROLL_RL] = 5 // ????????????????????????5???,?????????????????????????????????

        danmakuContext = DanmakuContext.create()
        danmakuContext?.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
            ?.setDuplicateMergingEnabled(false)?.setScrollSpeedFactor(1.2f)?.setScaleTextSize(1.2f)
            ?.setMaximumLines(maxLinesPair)?.preventOverlapping(overlappingEnablePair)
            ?.setDanmakuMargin(40)
        danmakuParser = object : BaseDanmakuParser() {
            override fun parse(): IDanmakus {
                return Danmakus()
            }
        }
        danmakuView.setCallback(object : DrawHandler.Callback {
            override fun prepared() {
                danmakuView.start()
            }

            override fun updateTimer(timer: DanmakuTimer) {}
            override fun danmakuShown(danmaku: BaseDanmaku) {}
            override fun drawingFinished() {}
        })

        danmakuView.setCallback(object : DrawHandler.Callback {
            override fun drawingFinished() {

            }

            override fun danmakuShown(danmaku: BaseDanmaku?) {
            }

            override fun prepared() {
                //???????????????????????????????????????????????????
                danmakuView.start()
            }

            override fun updateTimer(timer: DanmakuTimer?) {
            }
        })
        danmakuView.prepare(danmakuParser, danmakuContext)
//            danmakuView.showFPS(BuildConfig.DEBUG)
        danmakuView.enableDanmakuDrawingCache(true)
    }
}

