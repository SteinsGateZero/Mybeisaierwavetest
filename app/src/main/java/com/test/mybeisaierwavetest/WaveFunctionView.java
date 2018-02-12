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
 * Created by SteinsGateZero on 2018/2/11.
 */

public class WaveFunctionView extends View {
    private Path mPath;//路径
    private Paint mPaint, mPaintMore;//画笔
    private PointF drawPoint, drawPoint2;//绘制点
    private ValueAnimator animator, animatorh;
    private float mWidth, mHeight, waveHeight;//控件宽，控件高，水位
    private float waveDeepmin = 8f;//最小的波峰与波谷
    private float waveDeepMax = 20f;//最大的波峰与波谷
    private float waveDeep = 8f;//波峰与波谷
    private float arcRa = 0;//圆半径
    private Boolean iscircle = true;//是否是圆形图案
    private Boolean antiAlias = true;//是否开启抗锯齿
    public String MAINCOLOR_DEF = "#0000AA", NEXTCOLOR_DEF = "#0000FF";//默认颜色
    private int mainColor = Color.parseColor(MAINCOLOR_DEF), nextColor = Color.parseColor(NEXTCOLOR_DEF);//颜色
    private Double anglenum = Math.PI / 180;
    private int count = 0;//绘制次数

    public WaveFunctionView(Context context) {
        super(context);
        init();
    }

    public WaveFunctionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WaveFunctionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;//获得控件宽度
        mHeight = h;//获得控件高度
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
        waveHeight = mHeight;//初始化开始水位
        ChangeWaveLevel(5);
        super.onSizeChanged(w, h, oldw, oldh);
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

    //设置被遮挡的副波颜色
    public void setSecondaryWaveColor(int color) {
        nextColor = color;
        mPaintMore.setColor(color);
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
        drawPoint.x = 0;//重置为0,从原点开始绘制
        Double rightperiod = Math.PI / 8 * count;//每次平移Math.PI/8个周期
        if (count == 16) {//每次平移Math.PI/8个周期,平移第16次,平移了一个完整的周期
            count = 0;//平移了一个完整周期归零重新开始计数
        } else {
            count++;
        }

        //在宽度以内绘制一条条竖线
        while (drawPoint.x < mWidth) {
            //第一条波的y坐标
            drawPoint.y = (float) (waveHeight - waveDeep * Math.sin(drawPoint.x * anglenum - rightperiod));
            //第二条波的y坐标，比第一条向右移动了Math.PI/2个周期
            drawPoint2.y = (float) (waveHeight - waveDeep * Math.sin(drawPoint.x * anglenum - rightperiod - Math.PI / 2));
            //先绘制被遮挡的副波的竖线
            canvas.drawLine(drawPoint.x, drawPoint2.y, drawPoint.x, mHeight, mPaintMore);
            //绘制最上面显示主波的竖线
            canvas.drawLine(drawPoint.x, drawPoint.y, drawPoint.x, mHeight, mPaint);
            //跳到下一个点继续
            drawPoint.x++;
        }
        //定时更新
        postInvalidateDelayed(17);
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(mainColor);//设置颜色
        mPaint.setAntiAlias(antiAlias);//抗锯齿(性能影响)
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(50);
        mPaintMore = new Paint();
        mPaintMore.setAntiAlias(antiAlias);//抗锯齿
        mPaintMore.setStyle(Paint.Style.FILL);
        mPaintMore.setColor(nextColor);//设置颜色
        mPaintMore.setAlpha(30);
        drawPoint = new PointF(0, 0);
        drawPoint2 = new PointF(0, 0);
    }

    public void ChangeWaveLevel(int percent) {
        animator = ValueAnimator.ofFloat(waveDeepmin, waveDeepMax);//设置属性值变化范围，最大波峰波谷与最小
        animator.setDuration(1000);//设置动画时间
        animator.setInterpolator(new LinearInterpolator());//控制动画的变化速率
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waveDeep = (float) animation.getAnimatedValue();
            }
        });
        animator.setRepeatMode(ValueAnimator.REVERSE);//往返模式
        animator.setRepeatCount(1);
        animatorh = ValueAnimator.ofFloat(waveHeight, mHeight * (10 - percent) / 10);//水位变化
        animatorh.setDuration(2000);//设置动画时间
        animatorh.setInterpolator(new DecelerateInterpolator());//控制动画的变化速率

        animatorh.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waveHeight = (float) animation.getAnimatedValue();
            }
        });
        animator.start();//开始动画
        animatorh.start();//开始动画
    }
}
