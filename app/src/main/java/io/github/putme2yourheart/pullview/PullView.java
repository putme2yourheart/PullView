package io.github.putme2yourheart.pullview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by Frank on 2016/8/23 0023.
 * 向上推动的view
 */
public class PullView extends RelativeLayout {
    private Context mContext;
    //滑动管理器
    private Scroller mScroller;

    private int mScreenHeight = 0;
    //第一次按下距离
    private int mFirstDownX = 0;
    private int mFirstDownY = 0;

    //是否滑动完成
    private boolean mFinishFlag = false;

    private ImageView mImgView;

    private OnFinishListener mOnFinishListener;

    public PullView(Context context) {
        super(context);
        mContext = context;
        setupView();
    }

    public PullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupView();
    }

    private void setupView() {
        // 常速效果的Interpolator
        Interpolator polator = new AccelerateInterpolator();
        mScroller = new Scroller(mContext, polator);
        // 获取屏幕分辨率
        WindowManager wm = (WindowManager) (mContext.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;

        // 设置成透明背景,不然会看到底层布局
        this.setBackgroundColor(Color.argb(0, 0, 0, 0));
        mImgView = new ImageView(mContext);
        mImgView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mImgView.setScaleType(ImageView.ScaleType.FIT_XY);// 填充整个屏幕
        mImgView.setImageResource(R.drawable.bg);
        // 添加背景
        addView(mImgView);
    }

    // 设置推动门背景
    public void setBgImage(int id) {
        mImgView.setImageResource(id);
    }

    // 设置推动门背景
    public void setBgImage(Drawable drawable) {
        mImgView.setImageDrawable(drawable);
    }

    // 推动门的动画
    public void startAccelerateInterpolator(int startY, int dy, int duration) {
        mScroller.startScroll(0, startY, 0, dy, duration);
        invalidate();
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int mCurryY;
        //偏移量
        int mScrollY;
        // 降噪
        int mCurryX;
        int mScrollX;

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mFirstDownX = (int) event.getX();
                mFirstDownY = (int) event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                mCurryY = (int) event.getY();
                mScrollY = mCurryY - mFirstDownY;
                // 只准上滑有效
                if (mScrollY < 0) {
                    scrollTo(0, -mScrollY);
                }
                break;
            case MotionEvent.ACTION_UP:
                mCurryX = (int) event.getX();
                mScrollX = mCurryX - mFirstDownX;

                mCurryY = (int) event.getY();
                mScrollY = mCurryY - mFirstDownY;

                if (mScrollY < 0) {
                    // 降噪处理，向上的位移大于左右滑动的位移
                    if (Math.abs(mScrollY) > mScreenHeight / 5 && Math.abs(mScrollX) < Math.abs(mScrollY)) {

                        // 向上滑动超过1/5屏幕高的时候 开启向上消失动画
                        startAccelerateInterpolator(this.getScrollY(), mScreenHeight - this.getScrollY(), 250);
                        mFinishFlag = true;

                    } else {
                        // 向上滑动未超过1/5屏幕高的时候 开启向下动画
                        startAccelerateInterpolator(this.getScrollY(), -this.getScrollY(), 250);

                    }
                }

                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 更新界面
            postInvalidate();
        } else {
            if (mFinishFlag) {
                this.setVisibility(View.GONE);

                // 如果设置了OnFinish接收，调用
                if (mOnFinishListener != null) {
                    mOnFinishListener.onFinish();
                }
            }
        }
    }

}
