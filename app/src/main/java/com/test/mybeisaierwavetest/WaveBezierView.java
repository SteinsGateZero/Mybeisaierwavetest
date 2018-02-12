package com.test.mybeisaierwavetest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by SteinsGateZero on 2018/1/8.
 */

public class WaveBezierView extends View {
    private Paint mPaint = new Paint(), mPaintMore = new Paint();//画笔
    private Path mPath;//路径
    private PointF controlPoint1, controlPoint2, controlPoint3, controlPoint4;//波峰与波谷坐标点
    private ValueAnimator animator, animatorh;//属性动画
    private PointF startp1, startp2, startp3;//x轴上依次的坐标点
    private PointF endp1, endp2;//x轴上依次的坐标点
    private int duringtime = 2000;//属性动画播放时间
    private float mWidth, mHeight, waveHeight;//控件宽，控件高，水位
    private float waveTop = 40f;//波峰值
    private float waveDeep = 40f;//波谷值
    private Boolean isInit = false;//初始化判定
    private Boolean isRunning = false;//运行判定
    private float waveHeightBefore;//变化值之前的水位
    private float arcRa = 0;//圆半径
    private Boolean iscircle = true;//是否是圆形图案
    private Boolean antiAlias = true;//是否开启抗锯齿
    public String MAINCOLOR_DEF = "#0000AA", NEXTCOLOR_DEF = "#0000FF";//默认颜色
    private int mainColor = Color.parseColor(MAINCOLOR_DEF), nextColor = Color.parseColor(NEXTCOLOR_DEF);//颜色
    private int percent = 5;//波的水位百分比，0~10,这里为50%

    public WaveBezierView(Context context) {
        super(context);
    }

    public WaveBezierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WaveBezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = getMeasuredWidth();//获得控件宽度
        mHeight = getMeasuredHeight();//获得控件宽度
        if (mWidth > mHeight) {//若要裁剪为圆形，以最短的长度为直径
            arcRa = mHeight / 2;
            if (iscircle) {
                mWidth = mHeight;
            }
        } else {
            arcRa = mWidth / 2;
            if (iscircle) {
                mHeight = mWidth;
            }
        }
        setWavePercent(percent);
        if (!isInit) {
            init();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setWavePercent(int percent) {
        this.percent = percent;
        waveHeightBefore = waveHeight;//保存变化前水位
        waveHeight = mHeight / 10f * percent;//计算变化后水位
        refreshData();
    }

    //是否是圆形
    public void isCircle(Boolean iscircle) {
        this.iscircle = iscircle;
    }

    //是否开启抗锯齿
    public void setAntiAlias(Boolean antiAlias) {
        this.antiAlias = antiAlias;
        mPaint.setAntiAlias(antiAlias);
        mPaintMore.setAntiAlias(antiAlias);
    }

    //设置主波颜色
    public void setMainWaveColor(int color) {
        mainColor = color;
        mPaint.setColor(color);
    }

    //设置副波颜色
    public void setSecondaryWaveColor(int color) {
        nextColor = color;
        mPaintMore.setColor(color);
    }

    private void refreshData() {
        if (isInit) {
            animatorh = ValueAnimator.ofFloat(mHeight - waveHeightBefore, mHeight - waveHeight);//设置变化高度范围
            animatorh.setDuration(duringtime);
            animatorh.setInterpolator(new DecelerateInterpolator());//控制动画的变化速率
            animatorh.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    startp1.y = (float) animation.getAnimatedValue();
                    //整个波向Y轴水平增减高度
                    endp1 = new PointF(startp1.x + mWidth / 2f, startp1.y);
                    startp2 = new PointF(endp1.x + mWidth / 2f, startp1.y);
                    endp2 = new PointF(startp2.x + mWidth / 2f, startp1.y);
                    startp3 = new PointF(endp2.x + mWidth / 2f, startp1.y);
                    controlPoint1 = new PointF(startp1.x + mWidth / 4f, startp1.y + waveTop);
                    controlPoint2 = new PointF(endp1.x + mWidth / 4f, startp1.y - waveDeep);
                    controlPoint3 = new PointF(startp2.x + mWidth / 4f, startp1.y + waveTop);
                    controlPoint4 = new PointF(endp2.x + mWidth / 4f, startp1.y - waveDeep);
                    //invalidate();//请求重新draw()
                }

            });
            animatorh.start();//开始动画
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (iscircle) {//判断是否定义为圆形
            mPath.reset();//重置路径
            mPath.addCircle(arcRa, arcRa, arcRa, Path.Direction.CW);//画以(arcRa,arcRa),半径为arcRa的顺时针的圆
            canvas.clipPath(mPath);//裁剪
        }
        mPath.reset();//重置路径
        //第一条浅蓝色被遮挡的波(描述同第二条深蓝色波，只是比深蓝色波提前了画了4分之一个周期)
        mPath.moveTo(startp1.x - mWidth / 4, startp1.y);//移动到开始坐标点
        //第一个完整周期波形
        mPath.quadTo(controlPoint1.x - mWidth / 4, controlPoint1.y, endp1.x - mWidth / 4f, endp1.y);//二阶贝塞尔曲线，正半波（含波峰）
        mPath.quadTo(controlPoint2.x - mWidth / 4, controlPoint2.y, startp2.x - mWidth / 4f, startp2.y);//二阶贝塞尔曲线，后半波（含波谷）
        //第二个完整周期波形
        mPath.quadTo(controlPoint3.x - mWidth / 4, controlPoint3.y, endp2.x - mWidth / 4f, endp2.y);
        mPath.quadTo(controlPoint4.x - mWidth / 4, controlPoint4.y, startp3.x - mWidth / 4f, startp3.y);
        //封闭路径
        mPath.lineTo(startp3.x - mWidth / 4, mHeight);
        mPath.lineTo(startp1.x - mWidth / 4, mHeight);
        mPath.lineTo(startp1.x - mWidth / 4, startp1.y);
        canvas.drawPath(mPath, mPaintMore);//开始绘制

        //第二条深蓝色波
        mPath.reset();//重置路径
        mPath.moveTo(startp1.x, startp1.y);//移动到开始坐标点
        //第一个完整周期波形
        mPath.quadTo(controlPoint1.x, controlPoint1.y, endp1.x, endp1.y);//二阶贝塞尔曲线，正半波（含波峰）
        mPath.quadTo(controlPoint2.x, controlPoint2.y, startp2.x, startp2.y);//二阶贝塞尔曲线，后半波（含波谷）
        //第二个完整周期波形
        mPath.quadTo(controlPoint3.x, controlPoint3.y, endp2.x, endp2.y);//二阶贝塞尔曲线，正半波（含波峰）
        mPath.quadTo(controlPoint4.x, controlPoint4.y, startp3.x, startp3.y);//二阶贝塞尔曲线，后半波（含波谷）
        //封闭路径
        mPath.lineTo(startp3.x, mHeight);
        mPath.lineTo(startp1.x, mHeight);
        mPath.lineTo(startp1.x, startp1.y);
        canvas.drawPath(mPath, mPaint);//开始绘制
    }

    private void reset() {
        startp1 = new PointF(-mWidth, mHeight - waveHeight);//从左至右x轴上开始绘制的初始坐标点
        endp1 = new PointF(startp1.x + mWidth / 2f, startp1.y);//2分之一周期上波与x轴的交点
        startp2 = new PointF(endp1.x + mWidth / 2f, startp1.y);//一周期上的波与x轴的交点
        endp2 = new PointF(startp2.x + mWidth / 2f, startp1.y);//第二周期的2分之一周期上波与x轴的交点
        startp3 = new PointF(endp2.x + mWidth / 2f, startp1.y);//第二周期的一周期上的波与x轴的交点
        controlPoint1 = new PointF(startp1.x + mWidth / 4f, startp1.y + waveTop);//第一周期的波峰
        controlPoint2 = new PointF(endp1.x + mWidth / 4f, startp1.y - waveDeep);//第一周期的波谷
        controlPoint3 = new PointF(startp2.x + mWidth / 4f, startp1.y + waveTop);//第二周期的波峰
        controlPoint4 = new PointF(endp2.x + mWidth / 4f, startp1.y - waveDeep);//第二周期的波谷
    }

    private void init() {
        mPath = new Path();

        mPaint.setColor(mainColor);//设置颜色
        mPaint.setAntiAlias(antiAlias);//抗锯齿(性能影响)
        mPaint.setStyle(Paint.Style.FILL);
        // mPaint.setAlpha(70);
        //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));//设置遮罩

        mPaintMore.setAntiAlias(antiAlias);//抗锯齿
        mPaintMore.setStyle(Paint.Style.FILL);
        mPaintMore.setColor(nextColor);//设置颜色
        //mPaintMore.setAlpha(50);
        //mPaintMore.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));//设置遮罩

        animator = ValueAnimator.ofFloat(-mWidth, 0);//设置属性值变化范围，一个波长的长度
        animator.setDuration(duringtime);//设置动画时间
        animator.setInterpolator(new LinearInterpolator());//控制动画的变化速率
        reset();//重置坐标点
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isRunning) {
                    startp1.x = (float) animation.getAnimatedValue();//开始绘制的初始坐标点不断向X轴正方向移动
                    //整个波向X轴正方向平移startp1.x个值
                    endp1 = new PointF(startp1.x + mWidth / 2f, startp1.y);
                    startp2 = new PointF(endp1.x + mWidth / 2f, startp1.y);
                    endp2 = new PointF(startp2.x + mWidth / 2f, startp1.y);
                    startp3 = new PointF(endp2.x + mWidth / 2f, startp1.y);
                    controlPoint1 = new PointF(startp1.x + mWidth / 4f, startp1.y + waveTop);
                    controlPoint2 = new PointF(endp1.x + mWidth / 4f, startp1.y - waveDeep);
                    controlPoint3 = new PointF(startp2.x + mWidth / 4f, startp1.y + waveTop);
                    controlPoint4 = new PointF(endp2.x + mWidth / 4f, startp1.y - waveDeep);
                    invalidate();
                }
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);//重复次数无限次
        isRunning = true;
        animator.start();//开始动画
        isInit = true;

    }
}
