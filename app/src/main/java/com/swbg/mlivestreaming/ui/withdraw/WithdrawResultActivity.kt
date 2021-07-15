package com.swbg.mlivestreaming.ui.withdraw

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import com.swbg.mlivestreaming.R
import com.swbg.mlivestreaming.base.MBaseActivity
import com.swbg.mlivestreaming.bean.AccountRes
import com.swbg.mlivestreaming.bean.WithdrawDetailRes
import com.swbg.mlivestreaming.event.LiveEvent
import com.swbg.mlivestreaming.singleClick
import com.swbg.mlivestreaming.utils.HideDataUtil
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_apply_result.*

class WithdrawResultActivity : MBaseActivity() {
    var withdrawRes:WithdrawDetailRes?=null
    var accountRes:AccountRes?=null
    var observable: Observable<LiveEvent>? = null
    override val contentViewLayoutId: Int
        get() = R.layout.activity_apply_result

    override fun initView() {
        withdrawRes = intent.getParcelableExtra<WithdrawDetailRes>(INTENT_DATA)
        accountRes = intent.getParcelableExtra<AccountRes>(INTENT_ACCOUNT)
        if (withdrawRes?.bank_card.isNullOrBlank()) {
            tv_account_title.text = "支付宝(${HideDataUtil.hidePhoneNo(accountRes?.alipay_account)})"
        } else {

        tv_account_title.text = "银行卡号(${HideDataUtil.hideCardNo(accountRes?.bank_card)})"
        }
        tv_withdraw_title.text ="${withdrawRes?.amount}"
        tv_withdraw.singleClick {
            finish()
        }
        ibReturn.singleClick {
            finish()
        }
    }


    companion object {
        const val INTENT_DATA = "data"
        const val INTENT_ACCOUNT = "account"
        fun open(context: Context?, withdrawResult: WithdrawDetailRes, accountRes: AccountRes?): Intent {
            return Intent(context, WithdrawResultActivity::class.java).apply {
                putExtra(INTENT_DATA,withdrawResult)
                putExtra(INTENT_ACCOUNT,accountRes)
            }
        }
    }

    private val anchorViewModel by getViewModel(AnchorViewModel::class.java) {
        _recordDetail.observe(it, Observer {recordDetail->




        })
    }


    override fun onDestroy() {
        super.onDestroy()

    }

}