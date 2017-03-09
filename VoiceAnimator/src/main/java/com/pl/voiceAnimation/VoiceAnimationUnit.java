package com.pl.voiceAnimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
//import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by penglu on 2016/5/27.
 */
public class VoiceAnimationUnit extends View {
    private String TAG="VoiceAnimationUnite";

    private static final int HEIGHT_CHANGING =10010;
    private static final int VALUE_RESET_START=10087;
    private static final int VALUE_RESETTING =10086;
    private static final int VALUE_CHANGING=10000;

    static final int DEFAULT_SET_VALUE_ANIMATION_MAX_FRAMES=10;
    static final int SET_VALUE_ANIMATION_FRAMES_INTERVAL=10;

    static final int DEFAULT_STAY_INTERVAL=50;

    static final int DEFAULT_RESET_VALUE_ANIMATION_MAX_FRAMES=10;
    static final int RESET_VALUE_ANIMATION_FRAMES_INTERVAL=10;

    static final int DEFAULT_LOADING_ANIMATION_MAX_FRAMES=30;
    static final int LOADING_ANIMATION_FRAMES_INTERVAL=15;



    static int SET_VALUE_ANIMATION_MAX_FRAMES=  DEFAULT_SET_VALUE_ANIMATION_MAX_FRAMES;

    static int STAY_INTERVAL=   DEFAULT_STAY_INTERVAL;

    static int RESET_VALUE_ANIMATION_MAX_FRAMES=  DEFAULT_RESET_VALUE_ANIMATION_MAX_FRAMES;

    static int LOADING_ANIMATION_MAX_FRAMES=  DEFAULT_LOADING_ANIMATION_MAX_FRAMES;


    float width;
    float heightMax;
    float heightMin;
    int color;
    Paint paint;
    RectF rect;

    private float targetValue;
    private float currentValue;
    private float lastValue;
    private Interpolator valueAddingInterpolator =new AccelerateInterpolator();
    private Interpolator valueDecreasingInterpolator =new DecelerateInterpolator();

    private float targetHeight;
    private Interpolator heightInterpolator =new LinearInterpolator();


    private long lastSetValueTime;
    private int setValueInterval;
    int changeStep;
    //y坐标，用于loading动画，表示当前波动状态的的y坐标
    float currentY;

    private boolean isLoading;
    public VoiceAnimationUnit(Context context) {
        super(context);
    }

    private HandlerThread valueHandlerThread;
    private Handler valueHandler;

    private Handler drawHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case VALUE_CHANGING:
                    invalidate();
                    break;
                case VALUE_RESET_START:
                    lastValue=currentValue;
                    sendEmptyMessage(VALUE_RESETTING);
                case VALUE_RESETTING:
                    currentValue=lastValue-lastValue* valueDecreasingInterpolator.getInterpolation((float) changeStep/(float) RESET_VALUE_ANIMATION_MAX_FRAMES);
//                    Log.d(TAG,"handleMessage currentValue=");
                    setCurrentValue(currentValue);
                    invalidate();
                    changeStep++;
                    if (changeStep<=RESET_VALUE_ANIMATION_MAX_FRAMES){
                        sendEmptyMessageDelayed(VALUE_RESETTING,(Math.min(RESET_VALUE_ANIMATION_FRAMES_INTERVAL,
                                (setValueInterval==0?RESET_VALUE_ANIMATION_FRAMES_INTERVAL:(setValueInterval/RESET_VALUE_ANIMATION_MAX_FRAMES)))));
                    }else {
                        lastValue=0;
                        targetValue=0;
                    }
                    break;
                case HEIGHT_CHANGING:

                    break;
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        valueHandlerThread=new HandlerThread(TAG);
        valueHandlerThread.start();
        valueHandler=new Handler(valueHandlerThread.getLooper());
    }

    @Override
    protected void onDetachedFromWindow() {
        if (valueHandlerThread!=null&&valueHandlerThread.isAlive()){
            valueHandlerThread.quit();
            valueHandlerThread=null;
            valueHandler=null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);

//        if (heightMode==MeasureSpec.AT_MOST){
//            int atMostHeight=MeasureSpec.getSize(heightMeasureSpec);
//            height= (int) Math.min(heightMax,atMostHeight);
//        }else if (heightMeasureSpec==MeasureSpec.UNSPECIFIED){
        height= (int) heightMax;
//        }
//
//        if (widthMode==MeasureSpec.AT_MOST){
//            int atMostWidth=MeasureSpec.getSize(widthMeasureSpec);
//            width= (int) Math.min(width,atMostWidth);
//        }else if (widthMode==MeasureSpec.UNSPECIFIED){
//        width= (int) (width);
//        }
        heightMeasureSpec=MeasureSpec.makeMeasureSpec(height,heightMode);
        widthMeasureSpec=MeasureSpec.makeMeasureSpec(width,widthMode);

//        Log.d(TAG,"onmeasure: w="+width+",h="+height);
        setMeasuredDimension(width,height);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    void preparePaint(){
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        TAG+=heightMax;
    }

    protected void onDraw(Canvas canvas){
        if (rect==null){
            rect=new RectF();
        }

        float height;
        if (isLoading){
            //loading模式
            height=heightMin;
        }else{
            height=(currentValue>=0f?currentValue:0f)*(heightMax-heightMin)+heightMin;
        }
        if (height>heightMax){
            height=heightMax;
        }
        if (height<heightMin){
            height=heightMin;
        }
        rect.left=0;
        rect.top= currentY -(height)/2;
        rect.right=width;
        rect.bottom=(height)/2+ currentY;
        canvas.drawRoundRect(rect,(width)/2,(width)/2,paint);
    }

    void drawLoading(Canvas canvas ,float height){
        //用于绘制加载动画和静音动画，根据height的幅度来变化
    }

    private void removeResetMessages() {
        VoiceAnimationUnit.this.changeStep=0;
        drawHandler.removeMessages(VALUE_RESET_START);
        drawHandler.removeMessages(VALUE_RESETTING);
    }


    private void setCurrentValue(float value){
//        Log.d(TAG,"setCurrentValue currentValue="+value);
        this.currentValue =value;
    }

    /**
     * 设置当前动画的幅度值
     * @param targetValue 动画的幅度，范围（0,1）
     */
    public void setValue(float targetValue){
        if (isLoading){
            return;
        }
        if (lastSetValueTime==0){
            long now=System.currentTimeMillis();
            setValueInterval=SET_VALUE_ANIMATION_FRAMES_INTERVAL*SET_VALUE_ANIMATION_MAX_FRAMES;
            lastSetValueTime=now;
        }else {
            long now=System.currentTimeMillis();
            setValueInterval= (int) (now-lastSetValueTime);
            lastSetValueTime=now;
        }
        if(valueHandler==null){
            return;
        }
//        Log.d(TAG,"setValueInterval="+setValueInterval);
        if (targetValue<currentValue){
//            Log.d(TAG,"Runnable targetValue<this.targetValue");
        }else {
            removeResetMessages();
        }
//        if (targetValue<this.targetValue){
//            Log.d(TAG,"targetValue<this.targetValue");
//            this.targetValue=targetValue;
//            drawHandler.sendEmptyMessageDelayed(VALUE_RESET_START,
//                    Math.min(STAY_INTERVAL,setValueInterval));
//            return;
//        }
        this.targetValue=targetValue;
//        valueHandler.removeCallbacksAndMessages(null);
        valueHandler.post(new Runnable(){
            @Override
            public void run() {
                if (isLoading){
                    return;
                }
                final float lastValue=(Float.isInfinite(currentValue)||Float.isNaN(currentValue))?0:currentValue;
                final float targetValue= VoiceAnimationUnit.this.targetValue;

//                Log.d(TAG,"Runnable start currentValue="+lastValue);
//                Log.d(TAG,"Runnable start targetValue="+targetValue);
                removeResetMessages();
                float currentValue;
                int changeStep=0;
                while (changeStep <= SET_VALUE_ANIMATION_MAX_FRAMES&&!isLoading) {
                    if (targetValue<lastValue){
//                        Log.d(TAG,"Runnable targetValue<this.targetValue");
                    }else {
                        removeResetMessages();
                    }
                    currentValue = lastValue + (targetValue - lastValue) * valueAddingInterpolator.
                            getInterpolation((float) changeStep / (float) SET_VALUE_ANIMATION_MAX_FRAMES);
//                    Log.d(TAG,"Runnable currentValue=");
                    setCurrentValue(currentValue);
                    drawHandler.sendEmptyMessage(VALUE_CHANGING);
                    try {
                        Thread.sleep(Math.min(SET_VALUE_ANIMATION_FRAMES_INTERVAL,
                                (setValueInterval==0?SET_VALUE_ANIMATION_FRAMES_INTERVAL:(setValueInterval/SET_VALUE_ANIMATION_MAX_FRAMES))));
//                            Thread.sleep(SET_VALUE_ANIMATION_FRAMES_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeStep++;
                }
                if (targetValue<lastValue){
//                    Log.d(TAG,"Runnable targetValue<this.targetValue");
                }else {
                    removeResetMessages();
                }
                drawHandler.sendEmptyMessageDelayed(VALUE_RESET_START,
                        setValueInterval==0?STAY_INTERVAL: (long) ((setValueInterval * 0.4 + STAY_INTERVAL * 0.6) / 2));
            }
        });
    }

    public void setLoadingHeight(float height){
        if (isLoading){
            return;
        }
        if(valueHandler==null){
            return;
        }
        removeResetMessages();
        targetHeight=height;
        valueHandler.post(new Runnable(){
            @Override
            public void run() {
                isLoading=true;
                removeResetMessages();
                float currentHeight= currentY;
                int changeStep=0;
                while (changeStep <= LOADING_ANIMATION_MAX_FRAMES) {
                    removeResetMessages();
                    if (changeStep<=LOADING_ANIMATION_MAX_FRAMES/3){
                        //前半段
                        currentY = currentHeight-targetHeight * heightInterpolator.
                                getInterpolation((float) changeStep*3 / (float) LOADING_ANIMATION_MAX_FRAMES);
                    }else if (changeStep>=LOADING_ANIMATION_MAX_FRAMES*2/3){
                        //后半段
                        currentY = currentHeight+targetHeight - targetHeight * valueDecreasingInterpolator.
                                getInterpolation((float) (changeStep-LOADING_ANIMATION_MAX_FRAMES*2/3)*3/ (float) LOADING_ANIMATION_MAX_FRAMES);
                    }else {
                        //中间
                        currentY = currentHeight-targetHeight+2*targetHeight * valueAddingInterpolator.
                                getInterpolation((float) (changeStep -LOADING_ANIMATION_MAX_FRAMES/3)*3/ (float) LOADING_ANIMATION_MAX_FRAMES);
                    }
                    drawHandler.sendEmptyMessage(VALUE_CHANGING);
                    try {
                        Thread.sleep(LOADING_ANIMATION_FRAMES_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeStep++;
                }
                targetValue=0;
                currentValue=0;
                currentY =currentHeight;
                isLoading=false;
                drawHandler.sendEmptyMessage(VALUE_CHANGING);
            }
        });
    }

    public void showStableMax(){
        removeResetMessages();
        valueHandler.removeCallbacksAndMessages(null);
        currentValue=1;
        targetValue=1;
        drawHandler.sendEmptyMessage(VALUE_CHANGING);
    }

    public void showStableMin(){
        removeResetMessages();
        valueHandler.removeCallbacksAndMessages(null);
        currentValue=0;
        targetValue=0;
        drawHandler.sendEmptyMessage(VALUE_CHANGING);
    }
    public void showStableHalf(){
        removeResetMessages();
        valueHandler.removeCallbacksAndMessages(null);
        currentValue=0.5f;
        targetValue=0.5f;
        drawHandler.sendEmptyMessage(VALUE_CHANGING);
    }
}
