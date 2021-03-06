package com.mp.douyu.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.core.BubbleAttachPopupView;
import com.mp.douyu.R;

/**
 * Description: 自定义气泡Attach弹窗
 * Create by lxj, at 2019/3/13
 */
public class CustomBubbleAttachPopup extends BubbleAttachPopupView {
    public CustomBubbleAttachPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.custom_bubble_attach_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        bubbleContainer.setBackgroundResource(0);
        final TextView tv = findViewById(R.id.tv);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tv.setText(tv.getText() + "\n 啊哈哈哈啊哈");
//                tv.setText("\n 啊哈哈哈啊哈");
                dismiss();
            }
        });
    }

}
