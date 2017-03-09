package com.pl.voiceAnimation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import static com.pl.voiceAnimation.VoiceAnimationUnit.SET_VALUE_ANIMATION_MAX_FRAMES;

//import android.util.Log;


/**
 * Created by penglu on 2016/5/26.
 */
public class VoiceAnimator extends ViewGroup {

    private static final String TAG="VoiceAnimator";
    private static final int VALUE_SETED =10010;
    private static final int VALUE_RESET=10086;
    private static final int VALUE_CHANGING=10000;

    private static final int DEFAULT_SET_VALUE_ANIMATION_FRAMES_INTERVAL=40;//ms
    private static final int DEFAULT_SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP=5;//ms


    private int SET_VALUE_ANIMATION_FRAMES_INTERVAL=DEFAULT_SET_VALUE_ANIMATION_FRAMES_INTERVAL;//ms
    private int SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP=DEFAULT_SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP;//ms


    private static final int DEFAULT_COUNT=4;
    private static final int DEFAULT_DOTSCOLOR_RES= R.array.dotsColors;
    private static final int DEFAULT_DOTS_MAX_HEIGHT_RES=R.array.dotsMaxHeight;
    private static final float DEFAULT_DOTS_MAX_HEIGHT=100;//px
    private static final float DEFAULT_DOTS_MIN_HEIGHT=20;//px
    private static final float DEFAULT_DOTS_WIDTH=20;//px
    private static final float DEFAULT_DOTS_MARGIN=20;//px
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
    private RectF backgroundRect;
    private AnimationMode animationMode=AnimationMode.ANIMATION;

    private float totalHeight;
    private VoiceAnimationUnit[] voiceAnimationUnits;


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
            }else if (typedArray.getIndex(i)==R.styleable.VoiceAnimator_dotColors){
                int colorRes=typedArray.getResourceId(R.styleable.VoiceAnimator_dotColors,DEFAULT_DOTSCOLOR_RES);
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
        removeAllViews();
        voiceAnimationUnits =new VoiceAnimationUnit[dotsCount];
        for (int i=0;i<dotsCount;i++){
            voiceAnimationUnits[i]=new VoiceAnimationUnit(mContext);
            voiceAnimationUnits[i].width=dotsWidth;
            voiceAnimationUnits[i].heightMax=dotsMaxHeight[i];
            voiceAnimationUnits[i].heightMin=dotsMinHeight;
            voiceAnimationUnits[i].color=dotsColors[i];
            voiceAnimationUnits[i].currentY =backgroundRect.height()/2;
            addView(voiceAnimationUnits[i]);
        }
    }

    private void countSize(float setWidth,float setHeight) {
        float width=dotsCount * dotsWidth + dotsMargin * (dotsCount + 1);
        if (setWidth>0||setHeight>0) {
            if (setWidth>0){
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
//        backgroundPaint=new Paint();
//        backgroundPaint.setAntiAlias(true);
//        backgroundPaint.setColor(backgroundColor);
        for (VoiceAnimationUnit VoiceAnimationUnit : voiceAnimationUnits){
            VoiceAnimationUnit.preparePaint();
        }
    }

    private void setCurrentValue(float value,int changeStep){
        if (voiceAnimationUnits ==null){
            return;
        }
        if (voiceAnimationUnits.length>changeStep) {
            if (voiceAnimationUnits[changeStep]!=null) {
                try {
                    voiceAnimationUnits[changeStep].setValue(value);//先涨先落的关键，voiceAnimationUnit随着changeStep递增依次启动
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setCurrentHeight(float height,int changeStep){
        if (voiceAnimationUnits ==null){
            return;
        }
        if (voiceAnimationUnits.length>changeStep) {
            if (voiceAnimationUnits[changeStep]!=null) {
                try {
                    voiceAnimationUnits[changeStep].setLoadingHeight(height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setStableMax(){
        for (VoiceAnimationUnit unit: voiceAnimationUnits){
            unit.showStableMax();
        }
    }

    private void setStableMin(){
        for (VoiceAnimationUnit unit: voiceAnimationUnits){
            unit.showStableMin();
        }
    }

    private void setStableHalf(){
        for (VoiceAnimationUnit unit: voiceAnimationUnits){
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


//        Log.d(TAG,"onmeasure"+this+": w="+width+",h="+height);
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
        if (voiceAnimationUnits ==null){
            prepareDots();
            preparePaints();
        }
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

    /**
     * 获取点的数量
     */
    public int getDotsCount() {
        return dotsCount;
    }

    /**
     * 设置点的数量,
     * 因为会根据点的数量加载点的颜色和最大高度，
     * 所以必须先调用{@link #setDotsColors(int[])}
     * 和{@link #setDotsMaxHeight(float[])}将其参数的长度与点的数量保存一致，
     * 否则可能导致出错
     * @param dotsCount 点的个数，
     */
    public void setDotsCount(int dotsCount) {
        this.dotsCount = dotsCount;
        voiceAnimationUnits =null;
        requestLayout();
        postInvalidate();
    }

    /**
     * 获取每个点的颜色，rgb颜色
     */
    public int[] getDotsColors() {
        return dotsColors;
    }

    /**
     * 设置每个点的颜色
     * @param dotsColors 每个点的颜色，rgb颜色，推荐使用Color.parseColor("#ff0000")获取
     */
    public void setDotsColors(int[] dotsColors) {
        this.dotsColors = dotsColors;
        voiceAnimationUnits =null;
        requestLayout();
        postInvalidate();
    }

    /**
     * 获取每个点的最大高度，单位px
     */
    public float[] getDotsMaxHeight() {
        return dotsMaxHeight;
    }


    /**
     * 设置每个点的最大高度
     * @param dotsMaxHeight 最大高度值，单位px
     */
    public void setDotsMaxHeight(float[] dotsMaxHeight) {
        this.dotsMaxHeight = dotsMaxHeight;
        voiceAnimationUnits =null;
        requestLayout();
        postInvalidate();
    }

    /**
     * 获取点的最小高度，单位px
     */
    public float getDotsMinHeight() {
        return dotsMinHeight;
    }

    /**
     * 设置点的最小高度
     * @param dotsMinHeight 最小高度值，单位px
     */
    public void setDotsMinHeight(float dotsMinHeight) {
        this.dotsMinHeight = dotsMinHeight;
        voiceAnimationUnits =null;
        requestLayout();
        postInvalidate();
    }

    /**
     * 获取每个点的宽度，单位px
     */
    public float getDotsWidth() {
        return dotsWidth;
    }


    /**
     * 设置每个点的宽度
     * @param dotsWidth 宽度值，单位px
     */
    public void setDotsWidth(float dotsWidth) {
        this.dotsWidth = dotsWidth;
        voiceAnimationUnits =null;
        requestLayout();
        postInvalidate();
    }

    /**
     * 获取每个点之间的间距，单位px
     */
    public float getDotsMargin() {
        return dotsMargin;
    }

    /**
     * 设置每个点之间的间距
     * @param dotsMargin 间距值，单位px
     */
    public void setDotsMargin(float dotsMargin) {
        this.dotsMargin = dotsMargin;
        voiceAnimationUnits =null;
        requestLayout();
        postInvalidate();
    }

    /**
     * 设置动画类型
     * @param mode
     */
    public void setAnimationMode(AnimationMode mode){
        animationMode=mode;
        postInvalidate();
    }

    /**
     * 获取动画类型
     */
    public AnimationMode getAnimationMode() {
        return animationMode;
    }



    /**
     * 设置当前动画的幅度值
     * @param targetValue 动画的幅度，范围（0,1）
     */
    public void setValue(final float targetValue){
        if (animationMode!=AnimationMode.ANIMATION){
            return;
        }
        if(valueHandler==null){
            return;
        }
        valueHandler.removeCallbacksAndMessages(null);
        valueHandler.post(new Runnable() {
            @Override
            public void run() {
                int changeStep=0;
                while(changeStep<dotsCount){
                    setCurrentValue(targetValue,changeStep);
                    drawHandler.sendEmptyMessage(VALUE_SETED);
                    try {
                        Thread.sleep(SET_VALUE_ANIMATION_FRAMES_INTERVAL-SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP*changeStep);//先涨先落的间隔越来越短
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeStep++;
                }
            }
        });
    }

    /**
     * 启动加载动画
     */
    public void startLoading(){
        float height=mContext.getResources().getDimension(R.dimen.height_12dp);
        startLoading(height);
    }

    /**
     * 启动加载动画
     * @param height 加载动画的幅度，单位px
     */
    public void startLoading(final float height){
        if(valueHandler==null){
            return;
        }
        valueHandler.removeCallbacksAndMessages(null);
        valueHandler.post(new Runnable() {
            @Override
            public void run() {
                int changeStep=0;
                while(changeStep<dotsCount){
                    setCurrentHeight(height,changeStep);
                    drawHandler.sendEmptyMessage(VALUE_SETED);
                    try {
                        Thread.sleep(SET_VALUE_ANIMATION_FRAMES_INTERVAL-SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP*changeStep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeStep++;
                }
            }
        });
    }


    /**
     * 设置间隔时间，根据间隔时间，调整动画的效果
     * ms<100时，不生效，因为会导致动画变形
     * @param ms
     */
    public void setValueInterval(int ms){
        if (ms<100){
            return;
        }
        SET_VALUE_ANIMATION_FRAMES_INTERVAL = (int) (ms*0.4);
        SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP = (int) (ms*0.05);

        VoiceAnimationUnit.SET_VALUE_ANIMATION_MAX_FRAMES = ms/VoiceAnimationUnit.SET_VALUE_ANIMATION_FRAMES_INTERVAL;
        VoiceAnimationUnit.RESET_VALUE_ANIMATION_MAX_FRAMES = (int) (ms/VoiceAnimationUnit.RESET_VALUE_ANIMATION_FRAMES_INTERVAL*1.3);
//        VoiceAnimationUnit.LOADING_ANIMATION_MAX_FRAMES = ms/VoiceAnimationUnit.LOADING_ANIMATION_FRAMES_INTERVAL*3;
        VoiceAnimationUnit.STAY_INTERVAL = (int) (ms/1.6);


    }

}
