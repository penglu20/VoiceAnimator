package com.pl.voiceAnimation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by penglu on 2016/5/26.
 */
public class VoiceAnimator extends ViewGroup {

    private static final String TAG="VoiceAnimator";
    private static final int VALUE_SETED =10010;
    private static final int VALUE_RESET=10086;
    private static final int VALUE_CHANGING=10000;

    private static final int SET_VALUE_ANIMATION_FRAMES_INTERVAL=40;
    private static final int SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP=5;

    private static final int DEFAULT_COUNT=4;
    private static final int DEFAULT_DOTSCOLOR_RES= R.array.dotsColors;
    private static final int DEFAULT_DOTS_MAX_HEIGHT_RES=R.array.dotsMaxHeight;
    private static final float DEFAULT_DOTS_MAX_HEIGHT=100;
    private static final float DEFAULT_DOTS_MIN_HEIGHT=20;
    private static final float DEFAULT_DOTS_WIDTH=20;
    private static final float DEFAULT_DOTS_MARGIN=20;
    private static final int DEFAULT_BACKGROUND_COLOR=0x000000;

    public enum AnimationMode{
        STABLE_MAX(0),STABLE_MIN(1),STABLE_HALF(2),ANIMATION(3);
        AnimationMode(int key){
            this.key=key;
        }
        public int key;
    }

    private int dotsCount;
    private int[] dotsColors;
    private float[] dotsMaxHeight;
    private float dotsMinHeight;
    private float dotsWidth;
    private float dotsMargin;
    private int backgroundColor;
    private RectF backgroundRect;
    private Paint backgroundPaint;
    private AnimationMode animationMode=AnimationMode.ANIMATION;

    private float totalHeight;
    private VoiceAnimationUnite[] VoiceAnimationUnites;
    private int changeStep =0;


    private long lastSetValueTime;
    private int setValueInterval;


    private Context mContext;

    public VoiceAnimator(Context context) {
        super(context);
        init(context, null,0,0);
    }

    public VoiceAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,0,0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public VoiceAnimator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr,0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VoiceAnimator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private HandlerThread valueHandlerThread;
    private Handler valueHandler;
    private Handler drawHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case VALUE_SETED:
                    invalidate();
                    break;
                case VALUE_CHANGING:
                    break;
            }
        }
    };

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mContext=context;
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.VoiceAnimator);
        int length=typedArray.getIndexCount();
        for (int i=0;i<length;i++){
            if (typedArray.getIndex(i)==R.styleable.VoiceAnimator_dotsCount) {
                dotsCount = typedArray.getInt(R.styleable.VoiceAnimator_dotsCount, DEFAULT_COUNT);
            }else if(typedArray.getIndex(i)==R.styleable.VoiceAnimator_dotsMaxHeight){
                int heightRes=typedArray.getResourceId(R.styleable.VoiceAnimator_dotsMaxHeight,DEFAULT_DOTS_MAX_HEIGHT_RES);
                TypedArray heightArray=context.getResources().obtainTypedArray(heightRes);
                int heightArrayLength=heightArray.length();
                dotsMaxHeight=new float[heightArrayLength];
                for (int j=0;j<heightArrayLength;j++){
                    dotsMaxHeight[j]=heightArray.getDimension(j,DEFAULT_DOTS_MAX_HEIGHT);
                }
                heightArray.recycle();
            }else if(typedArray.getIndex(i)==R.styleable.VoiceAnimator_dotsMinHeight){
                dotsMinHeight=typedArray.getDimension(R.styleable.VoiceAnimator_dotsMinHeight,DEFAULT_DOTS_MIN_HEIGHT);
            }else if(typedArray.getIndex(i)==R.styleable.VoiceAnimator_dotsWidth){
                dotsWidth=typedArray.getDimension(R.styleable.VoiceAnimator_dotsWidth,DEFAULT_DOTS_WIDTH);
            }else if (typedArray.getIndex(i)==R.styleable.VoiceAnimator_dotsMargin){
                dotsMargin=typedArray.getDimension(R.styleable.VoiceAnimator_dotsMargin,DEFAULT_DOTS_MARGIN);
            }else if (typedArray.getIndex(i)==R.styleable.VoiceAnimator_animationMode){
                int key=typedArray.getInt(R.styleable.VoiceAnimator_animationMode, AnimationMode.ANIMATION.key);
                animationMode= AnimationMode.values()[key];
            }else if (typedArray.getIndex(i)==R.styleable.VoiceAnimator_backGroundColor){
                backgroundColor=typedArray.getColor(R.styleable.VoiceAnimator_backGroundColor,DEFAULT_BACKGROUND_COLOR);
            }else if (typedArray.getIndex(i)==R.styleable.VoiceAnimator_colorList){
                int colorRes=typedArray.getResourceId(R.styleable.VoiceAnimator_colorList,DEFAULT_DOTSCOLOR_RES);
                TypedArray colorArray=context.getResources().obtainTypedArray(colorRes);
                int colorArrayLength=colorArray.length();
                dotsColors=new int[colorArrayLength];
                for (int j=0;j<colorArrayLength;j++){
                    dotsColors[j]=colorArray.getInt(j,0);
                }
                colorArray.recycle();
            }
        }
        typedArray.recycle();


        countSize(-1,-1);
        setWillNotDraw(false);

    }


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


    private void prepareDots(){
        VoiceAnimationUnites=new VoiceAnimationUnite[dotsCount];
        for (int i=0;i<dotsCount;i++){
            VoiceAnimationUnites[i]=new VoiceAnimationUnite(mContext);
            VoiceAnimationUnites[i].width=dotsWidth;
            VoiceAnimationUnites[i].heightMax=dotsMaxHeight[i];
            VoiceAnimationUnites[i].heightMin=dotsMinHeight;
            VoiceAnimationUnites[i].color=dotsColors[i];
            VoiceAnimationUnites[i].currentY =backgroundRect.height()/2;
            addView(VoiceAnimationUnites[i]);
        }

    }

    private void countSize(float setWidth,float setHeight) {
        float width=dotsCount * dotsWidth + dotsMargin * (dotsCount + 1);
        if (setWidth>=0||setHeight>=0) {
            if (setWidth>=0){
                width=setWidth;
            }
            if (setHeight>=0) {
                totalHeight = setHeight;
            }
        }else {
            for (float height : dotsMaxHeight) {
                if (height > totalHeight) {
                    totalHeight = height;
                }
            }
            totalHeight += dotsMargin * 2;
        }
        float round = Math.max(width, totalHeight);
        backgroundRect = new RectF(0, 0, round, round);
        return;
    }

    private void preparePaints(){
        backgroundPaint=new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(backgroundColor);
        for (VoiceAnimationUnite VoiceAnimationUnite:VoiceAnimationUnites){
            VoiceAnimationUnite.preparePaint();
        }
    }

    private void setCurrentValue(float value){
        if (VoiceAnimationUnites==null){
            return;
        }
        if (VoiceAnimationUnites.length>changeStep) {
            if (VoiceAnimationUnites[changeStep]!=null) {
                try {
                    VoiceAnimationUnites[changeStep].setValue(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setCurrentHeight(float height){
        if (VoiceAnimationUnites==null){
            return;
        }
        if (VoiceAnimationUnites.length>changeStep) {
            if (VoiceAnimationUnites[changeStep]!=null) {
                try {
                    VoiceAnimationUnites[changeStep].setLoadingHeight(height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setStableMax(){
        for (VoiceAnimationUnite unit:VoiceAnimationUnites){
            unit.showStableMax();
        }
    }

    private void setStableMin(){
        for (VoiceAnimationUnite unit:VoiceAnimationUnites){
            unit.showStableMin();
        }
    }

    private void setStableHalf(){
        for (VoiceAnimationUnite unit:VoiceAnimationUnites){
            unit.showStableHalf();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int height=backgroundRect!=null? (int) backgroundRect.height() :0;
        int width=backgroundRect!=null? (int) backgroundRect.width() :0;

        int measuredWidth=getMeasuredWidth();
        int measuredHeight=getMeasuredHeight();

        measureChildren(widthMeasureSpec,heightMeasureSpec);
        if (heightMode==MeasureSpec.EXACTLY){
            height=MeasureSpec.getSize(heightMeasureSpec);
        }
        else if (height==MeasureSpec.AT_MOST){
            int atMostHeight=MeasureSpec.getSize(heightMeasureSpec);
            height= (int) Math.min(backgroundRect!=null?backgroundRect.height():atMostHeight,atMostHeight);
//        }else if (heightMeasureSpec==MeasureSpec.UNSPECIFIED){
//            height= backgroundRect!=null? (int) backgroundRect.height() :0;
        }

        if (widthMode==MeasureSpec.EXACTLY){
            width=MeasureSpec.getSize(widthMeasureSpec);
        }else if (widthMode==MeasureSpec.EXACTLY){
            int atMostWidth=MeasureSpec.getSize(widthMeasureSpec);
            width= (int) Math.min(backgroundRect!=null?backgroundRect.width():atMostWidth,atMostWidth);
//        }else if (widthMode==MeasureSpec.UNSPECIFIED){
//            width= backgroundRect!=null? (int) backgroundRect.width() :0;
        }
        heightMeasureSpec=MeasureSpec.makeMeasureSpec(height,heightMode);
        widthMeasureSpec=MeasureSpec.makeMeasureSpec(width,heightMode);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG,"onmeasure"+this+": w="+width+",h="+height);
        setMeasuredDimension(width,height);

        countSize(width,height);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount=getChildCount();
        float totalWidth= (dotsCount*dotsWidth+dotsMargin*(dotsCount +1));
        float backgroundWitdh= (int) backgroundRect.width();
        for (int i=0;i<childCount;i++){
            View childView=getChildAt(i);
            int cl,ct,cr,cb;
            cl= (int) ((backgroundWitdh-totalWidth)/2+dotsMargin*(i+1)+dotsWidth*(i));
            cr= (int) (cl+dotsWidth);
            ct=0;
            cb= (int) Math.max(backgroundRect.height(),totalHeight);
            childView.layout(cl,ct,cr,cb);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (VoiceAnimationUnites==null){
            prepareDots();
            preparePaints();
        }
        drawBackground(canvas);
        switch (animationMode){
            case STABLE_MAX:
                setStableMax();
                break;
            case STABLE_MIN:
                setStableMin();
                break;
            case STABLE_HALF:
                setStableHalf();
                break;
        }
        super.onDraw(canvas);
    }

    private void drawBackground(Canvas canvas){
        canvas.drawOval(backgroundRect,backgroundPaint);
    }

    public int getDotsCount() {
        return dotsCount;
    }

    public void setDotsCount(int dotsCount) {
        this.dotsCount = dotsCount;
    }

    public int[] getDotsColors() {
        return dotsColors;
    }

    public void setDotsColors(int[] dotsColors) {
        this.dotsColors = dotsColors;
    }

    public float[] getDotsMaxHeight() {
        return dotsMaxHeight;
    }

    public void setDotsMaxHeight(float[] dotsMaxHeight) {
        this.dotsMaxHeight = dotsMaxHeight;
    }

    public float getDotsMinHeight() {
        return dotsMinHeight;
    }

    public void setDotsMinHeight(float dotsMinHeight) {
        this.dotsMinHeight = dotsMinHeight;
    }

    public float getDotsWidth() {
        return dotsWidth;
    }

    public void setDotsWidth(float dotsWidth) {
        this.dotsWidth = dotsWidth;
    }

    public float getDotsMargin() {
        return dotsMargin;
    }

    public void setDotsMargin(float dotsMargin) {
        this.dotsMargin = dotsMargin;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setAnimationMode(AnimationMode mode){
        animationMode=mode;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public AnimationMode getAnimationMode() {
        return animationMode;
    }

    public void setValue(final float targetValue){
        if (animationMode!=AnimationMode.ANIMATION){
            return;
        }
        if (lastSetValueTime==0){
            long now=System.currentTimeMillis();
            setValueInterval=SET_VALUE_ANIMATION_FRAMES_INTERVAL*dotsCount;
            lastSetValueTime=now;
        }else {
            long now=System.currentTimeMillis();
            setValueInterval= (int) (now-lastSetValueTime);
            lastSetValueTime=now;
        }
        Log.d(TAG,"setValueInterval="+setValueInterval);
//        drawHandler.sendEmptyMessage(VALUE_SETED);
        if(valueHandler==null){
            return;
        }
        valueHandler.removeCallbacksAndMessages(null);
        valueHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeStep=0;
                while(changeStep<dotsCount){
                    setCurrentValue(targetValue);
                    drawHandler.sendEmptyMessage(VALUE_SETED);
                    try {
//                        Thread.sleep(Math.min(SET_VALUE_ANIMATION_FRAMES_INTERVAL,setValueInterval==0?SET_VALUE_ANIMATION_FRAMES_INTERVAL:(setValueInterval/dotsCount)));
                        Thread.sleep(SET_VALUE_ANIMATION_FRAMES_INTERVAL-SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP*changeStep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeStep++;
                }
            }
        },Math.min(SET_VALUE_ANIMATION_FRAMES_INTERVAL,setValueInterval/dotsCount));
    }

    public void startLoading(){
        float height=mContext.getResources().getDimension(R.dimen.height_12dp);
        startLoading(height);
    }

    public void startLoading(final float height){
        if(valueHandler==null){
            return;
        }
        valueHandler.removeCallbacksAndMessages(null);
        valueHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeStep=0;
                while(changeStep<dotsCount){
                    setCurrentHeight(height);
                    drawHandler.sendEmptyMessage(VALUE_SETED);
//                        sendEmptyMessageDelayed(VALUE_SETED,Math.min(SET_VALUE_ANIMATION_FRAMES_INTERVAL,setValueInterval==0?SET_VALUE_ANIMATION_FRAMES_INTERVAL:(setValueInterval/dotsCount)));
                    try {
//                        Thread.sleep(Math.min(SET_VALUE_ANIMATION_FRAMES_INTERVAL,setValueInterval==0?SET_VALUE_ANIMATION_FRAMES_INTERVAL:(setValueInterval/dotsCount)));
                        Thread.sleep(SET_VALUE_ANIMATION_FRAMES_INTERVAL-SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP*changeStep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeStep++;
                }
            }
        },SET_VALUE_ANIMATION_FRAMES_INTERVAL);
    }
}
