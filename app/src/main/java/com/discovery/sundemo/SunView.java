package com.discovery.sundemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Date;


/**
 * Created by ruanwenjiang
 * on 19-7-2
 * desc:一个描述日出到日落整个的自定义空间
 */


public class SunView extends View {
    private static final String TAG = "SunView";

    /*开始时间字符串*/
    private String mStartTime = "6:00";

    /*结束时间字符串*/
    private String mStopTime = "18:00";

    /*中间提示语*/
    private String mTimpText = "日出日落";

    /*字体颜色*/
    private String mFontColor = "#FFFFFF";

    /*线条颜色*/
    private String mLineColor;

    /*大背景颜色*/
    private String mBgColor = "#8D93E4";

    /*太阳颜色*/
    private String mSunColor = "#F6F412";

    /*画笔，这里我们始终使用同一支画笔就可以了*/
    private Paint mPaint;

    /*当前时间距离凌晨6点一共有几分钟*/
    private int mTime;

    private Handler mH;

    public SunView(Context context) {
        this(context, null);
    }

    public SunView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SunView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mH = new Handler(Looper.getMainLooper());
        mPaint = new Paint();
        Date date = new Date();
        int hours = date.getHours();
        int minutes = date.getMinutes();
        /**
         * 计算出当前时间距离凌晨6点有几分钟,后续需要根据这个时间计算出我们的太阳偏移了多少度
         * 这里我们规定从凌晨6点开始为0度，到18点一共180度
         */
        mTime = (hours - 6) * 60 + minutes;
        mH.postDelayed(new SunRunnable(), 60 * 1000);
    }


    private class SunRunnable implements Runnable {

        @Override
        public void run() {
            Date date = new Date();
            int hours = date.getHours();
            int minutes = date.getMinutes();
            mTime = (hours - 6) * 60 + minutes;//计算出当前时间距离凌晨6点有几分钟
            Log.d(TAG, "更新时间：" + mTime + "/分");
            post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
            mH.postDelayed(this, 60 * 1000);
        }
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置背景颜色
        canvas.drawColor(Color.parseColor(mBgColor));

        int width = getWidth();
        int height = getHeight();

        //底部横线偏移位置
        float lineOffest = height - (1 / 5f * height);

        float radius = Math.min(width / 2.5f, height / 2.5f);


        //绘制圆弧虚线
        mPaint.reset();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor(mFontColor));
        mPaint.setAlpha(125);
        mPaint.setStrokeWidth(2);
        mPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        canvas.drawCircle(width / 2f, lineOffest, radius, mPaint);


        //底部横线
        mPaint.reset();
        mPaint.setColor(Color.parseColor(mFontColor));
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(125);
        mPaint.setStrokeWidth(5);
        canvas.drawLine(0, lineOffest, width, lineOffest, mPaint);

        //绘制一个遮罩，罩住底部虚线
        mPaint.reset();
        mPaint.setColor(Color.parseColor(mBgColor));
        mPaint.setAntiAlias(true);
        RectF rectF = new RectF(0, lineOffest + 2.5f, width, height);
        canvas.drawRect(rectF, mPaint);


        //绘制文字
        mPaint.reset();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor(mFontColor));
        mPaint.setTextSize(radius / 5);
        float startMT = mPaint.measureText(mStartTime, 0, mStartTime.length());
        float stopMT = mPaint.measureText(mStopTime, 0, mStopTime.length());
        float timpMT = mPaint.measureText(mTimpText, 0, mTimpText.length());
        canvas.drawText(mStartTime, width / 2f - startMT / 2f-radius, lineOffest + radius / 5, mPaint);
        canvas.drawText(mStopTime, width / 2f - stopMT / 2f+radius, lineOffest + radius / 5, mPaint);
        canvas.drawText(mTimpText, width / 2f - timpMT / 2f, lineOffest - radius / 5, mPaint);

        //绘制小太阳
        drawSun(radius,width, height, canvas);
    }

    private void drawSun(float r,int width, int height, Canvas canvas) {
        mPaint.reset();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        /**
         * 1、由于我们的小太阳是需要跟随半圆移动的，所以小太阳的圆心就是圆上面的各个点了
         * 2、那么当我们把时间精确到分的时候，可以得出每分钟是1/4度，结合大圆的半径我们就可以根据三角函数的计算获取到每个点的坐标了
         */
        float sR = r / 10f;
        float pointX = 0;
        float pointY = 0;

        //此时太阳还没有升起来
        if (mTime <= 0 || mTime > 1080) {
            mSunColor = "#808080";
            pointX = width / 2f - r;
            pointY = height - (1 / 5f * height);
        }

        //此时太阳已经落山
        if (mTime >= 720 && mTime <= 1080) {
            mSunColor = "#808080";
            pointX = width / 2f + r;
            pointY = height - (1 / 5f * height);
        }

        if (mTime == 360) {
            mSunColor = "#FEFF00";
            pointX = width / 2f;
            pointY = height - (1 / 5f * height) - r;
        }

        //此时太阳应该再6-12点之间
        if (mTime > 0 && mTime < 360) {
            mSunColor = "#FEFF00";
            double angle = mTime * 0.25f;

            /**
             * 这里使用正余弦公式的时候我们需要将角度转弧度，公式=PI/ 180 * 角度
             */
            double pAngle = Math.PI / 180 * angle;
            double angleY = Math.sin(pAngle) * r;
            double angleX = Math.cos(pAngle) * r;
            pointX = (float) (width / 2f - angleX) + 0.5f;
            pointY = (float) (height - (1 / 5f * height) - angleY) + 0.5f;

        }

        //此时太阳应该放于12-18点之间
        if (mTime > 360 && mTime < 720) {
            mSunColor = "#FEFF00";
            double angle = 180 - mTime * 0.25f;
            double pAngle = Math.PI / 180 * angle;
            double angleY = Math.sin(pAngle) * r;
            double angleX = Math.cos(pAngle) * r;
            pointX = (float) (width / 2f + angleX) + 0.5f;
            pointY = (float) (height - (1 / 5f * height) - angleY) + 0.5f;
        }

        mPaint.setColor(Color.parseColor(mSunColor));
        canvas.drawCircle(pointX, pointY, sR, mPaint);

        //接下来就该绘制太阳的边线了
        mPaint.reset();
        mPaint.setColor(Color.parseColor(mSunColor));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);

        float pointOffest = sR / 4f;
        float lineLen = sR;

        //线条内端点所在圆的半径
        float lineInR = sR + pointOffest;

        //线条外端点所在圆的半径
        float lineOutR = sR + pointOffest + lineLen;

        //所有弧度计算都一致
        double lineAngle = Math.PI / 180 * 45;

        //计算太阳边线的所有点坐标
        float x1 = pointX;
        float y1 = pointY - lineInR;
        float x2 = pointX;
        float y2 = pointY - lineOutR;

        float x3 = (float) (Math.cos(lineAngle) * lineInR) + pointX;
        float y3 = pointY - (float) (Math.sin(lineAngle) * lineInR);
        float x4 = (float) (Math.cos(lineAngle) * lineOutR) + pointX;
        float y4 = pointY - (float) (Math.sin(lineAngle) * lineOutR);

        float x5 = pointX + lineInR;
        float y5 = pointY;
        float x6 = pointX + lineOutR;
        float y6 = pointY;

        float x7 = (float) (Math.cos(lineAngle) * lineInR) + pointX;
        float y7 = pointY + (float) (Math.sin(lineAngle) * lineInR);
        float x8 = (float) (Math.cos(lineAngle) * lineOutR) + pointX;
        float y8 = pointY + (float) (Math.sin(lineAngle) * lineOutR);

        float x9 = pointX;
        float y9 = pointY + lineInR;
        float x10 = pointX;
        float y10 = pointY + lineOutR;

        float x11 = pointX - (float) (Math.cos(lineAngle) * lineInR);
        float y11 = pointY + (float) (Math.sin(lineAngle) * lineInR);
        float x12 = pointX - (float) (Math.cos(lineAngle) * lineOutR);
        float y12 = pointY + (float) (Math.sin(lineAngle) * lineOutR);

        float x13 = pointX - lineInR;
        float y13 = pointY;
        float x14 = pointX - lineOutR;
        float y14 = pointY;

        float x15 = pointX - (float) (Math.cos(lineAngle) * lineInR);
        float y15 = pointY - (float) (Math.sin(lineAngle) * lineInR);
        float x16 = pointX - (float) (Math.cos(lineAngle) * lineOutR);
        float y16 = pointY - (float) (Math.sin(lineAngle) * lineOutR);

        float[] points = {x1, y1, x2, y2, x3, y3, x4, y4, x5, y5, x6, y6, x7, y7, x8, y8, x9, y9, x10, y10, x11, y11, x12, y12, x13, y13, x14, y14, x15, y15,
                x16, y16};
        canvas.drawLines(points, mPaint);

    }
}
