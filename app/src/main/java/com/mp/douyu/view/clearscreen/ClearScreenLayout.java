package com.mp.douyu.view.clearscreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * 左滑 or 右滑清屏
 * Created by lmxjw3 on 2020/3/10.
 */
public class ClearScreenLayout extends FrameLayout {
    private int mDownX;     // 手指按下的x轴位置
    private int mDownY;     // 手指按下的y轴位置
    private int startX;     // 滑动开始时x轴偏移量
    private int translateX; // 当前x轴偏移量
    private int endX;       // 动画结束时x轴偏移量
    private boolean ifCleared; // 是否已清屏
    private VelocityTracker mVelocityTracker; // 计算滑动速度
    private ValueAnimator mAnimator; // 动画

    private boolean leftSlide = true; // true-左滑清屏 false-右滑清屏
    private OnSlideClearListener slideClearListener; // 清屏监听器
    private ArrayList<View> listClearViews = new ArrayList<>(); // 需要清除的View

    public ClearScreenLayout(Context context) {
        this(context, null);
    }

    public ClearScreenLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearScreenLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        View view = new View(getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setClickable(true);
        addView(view, 0);

        mVelocityTracker = VelocityTracker.obtain();
        mAnimator = ValueAnimator.ofFloat(0, 1.0f).setDuration(200);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                translateChild((int) (translateX + value * (endX - translateX)));
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (ifCleared && translateX == 0) {
                    if (slideClearListener != null) {
                        slideClearListener.onRestored();
                    }
                    ifCleared = !ifCleared;
                } else if (!ifCleared && Math.abs(translateX) == getWidth()) {
                    if (slideClearListener != null) {
                        slideClearListener.onCleared();
                    }
                    ifCleared = !ifCleared;
                }
            }
        });
    }

    public boolean isIfCleared() {
        return ifCleared;
    }

    public void setIfCleared(boolean ifCleared) {
        this.ifCleared = ifCleared;
    }

    /**
     * 滑动监听
     */
    public void setOnSlideListener(OnSlideClearListener slideListener) {
        this.slideClearListener = slideListener;
    }

    /**
     * 滑动方向（左滑 or 右滑）
     */
    public void setSlideDirection(SlideDirection direction) {
        leftSlide = direction == SlideDirection.LEFT;
    }

    /**
     * 添加需要清屏的view
     */
    public void addClearViews(View... views) {
        for (View cell : views) {
            if (!listClearViews.contains(cell)) {
                listClearViews.add(cell);
            }
        }
    }

    /**
     * 移除需要清屏的view
     */
    public void removeClearViews(View... views) {
        for (View cell : views) {
            listClearViews.remove(cell);
        }
    }

    /**
     * 移除所有需要清屏的view
     */
    public void removeAllClearViews() {
        listClearViews.clear();
    }

    /**
     * 清屏（有动画）
     */
    public void clearWithAnim() {
        if (ifCleared) {
            return;
        } else if (leftSlide) {
            endX = -getWidth();
        } else {
            endX = getWidth();
        }
        mAnimator.start();
    }

    /**
     * 清屏（无动画）
     */
    public void clearWithoutAnim() {
        if (ifCleared) {
            return;
        } else if (leftSlide) {
            endX = -getWidth();
        } else {
            endX = getWidth();
        }
        translateChild(endX);
        ifCleared = true;
    }

    /**
     * 还原（有动画）
     */
    public void restoreWithAnim() {
        if (!ifCleared) {
            return;
        }
        endX = 0;
        mAnimator.start();
    }

    /**
     * 还原（无动画）
     */
    public void restoreWithoutAnim() {
        if (!ifCleared) {
            return;
        }
        endX = 0;
        translateChild(endX);
        ifCleared = false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = (int) event.getX();
                    mDownY = (int) event.getY();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int endX = (int) event.getX();
                    int endY = (int) event.getY();
                    int distanceX = Math.abs(endX - mDownX);
                    int distanceY = Math.abs(endY - mDownY);
                    if (distanceX >= distanceY) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = x;
                    mDownY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mAnimator.isRunning() && Math.abs(x - mDownX) > Math.abs(y - mDownY)) {
                        startX = translateX;
                        if (x - mDownX < -10) {
                            if ((leftSlide && !ifCleared) || (!leftSlide && ifCleared)) {
                                return true;
                            }
                        } else if (x - mDownX > 10) {
                            if ((leftSlide && ifCleared) || (!leftSlide && !ifCleared)) {
                                return true;
                            }
                        }
                    }
                    if (isIfCleared()) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (isIfCleared()) {
                        return true;
                    }
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            mVelocityTracker.addMovement(event);
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final int offsetX = x - mDownX;
            final int offsetY = y - mDownY;
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    translateChild(startX + offsetX);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(offsetY)<20&&Math.abs(offsetX)<100) {
                        //点击事件
                        return performClick();
                    }
                    if (translateX != 0) {
                        mVelocityTracker.computeCurrentVelocity(10);
                        /*if (Math.abs(offsetY)<20&&Math.abs(offsetX)<20&&Math.abs(mVelocityTracker.getXVelocity()) < 10 && Math.abs(mVelocityTracker.getYVelocity()) < 10) {
                            //点击事件
                            return performClick();
                        }*/
                        if (Math.abs(offsetX) > getWidth() / 3 ||
                                (mVelocityTracker.getXVelocity() > 20 && !leftSlide && !ifCleared) ||
                                (mVelocityTracker.getXVelocity() > 20 && leftSlide && ifCleared) ||
                                (mVelocityTracker.getXVelocity() < -20 && !leftSlide && ifCleared) ||
                                (mVelocityTracker.getXVelocity() < -20 && leftSlide && !ifCleared)) {
                            if (ifCleared) {
                                endX = 0;
                            } else if (leftSlide) {
                                endX = -getWidth();
                            } else {
                                endX = getWidth();
                            }
                        } else {
                            endX = startX;
                        }
                    }
                    mAnimator.start();
                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private void translateChild(int translate) {
        if ((leftSlide && translate > 0) || ((!leftSlide && translate < 0))) {
            translate = 0;
        }
        translateX = translate;
        for (int i = 0; i < listClearViews.size(); i++) {
            listClearViews.get(i).setTranslationX(translate);
        }
    }

    public interface OnSlideClearListener {
        void onCleared();

        void onRestored();
    }
}