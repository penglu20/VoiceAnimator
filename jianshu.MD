谷歌App的语音搜索功能估计很多人都没用过，没用过的也没必要去用它了，因为实际上就类似手机百度，360手机搜索，是一款类浏览器产品，没有太多实用价值。

不过不得不说的是，它的动画做得相当精致，如果要用一个词来形容，就是————灵动。先给大家看看效果：


<!--![录音效果，每100ms用setValue()传一个值](https://raw.githubusercontent.com/l465659833/VoiceAnimation/master/art/setValue_100.gif)-->

<!--![startLoading()效果](https://raw.githubusercontent.com/l465659833/VoiceAnimation/master/art/startLoading.gif)-->

动图无法完全展现这个动画的细微精妙之处，想仔细研究的同学可以自行下载，不过接着往下看，我们会来模拟实现这个效果的。

### 背景

首先介绍一下语音动画的一些背景，使用过讯飞语音识别sdk（或者其他语音识别）的人都应该有相关经验，
在开始录音以后，讯飞会通过回调函数返回一小段时间内声音的平均大小。我们使用这个代表声音大小的值，就可以绘制出各种各样的动画，给用户清晰的反馈。


仔细观察不难发现，这个动画中包含了几个特点：

1. 波浪的效果，前面的点比后面的点先涨先落
2. 每个点本身都具有一定的延滞性，在到达一定的高度以后，不会立刻回落，而是停顿一小段时间以后才收缩。
3. 对变化比较敏感，如果持续大声说话，动画会在最高点处不断震颤，而不会死板的不动


我们先预想一下如何实现这些功能（以下称为VoiceAnimator）：

首先共通的部分是，按一定的时间间隔，将代表声音大小的值设置给VoiceAnimator，VoiceAnimator则根据这些值来绘制动画。

而动画的实现方式有：

1. 自定义View，通过一个线程来计算每个点的高度，然后统一绘制
2. 自定义View，通过多个线程分别计算每个点的高度，然后统一绘制
3. 自定义ViewGroup，每个点都用一个View来表示，通过属性动画来实现动画
4. 自定义ViewGroup，每个点都用一个View来表示，使用多个线程来手动绘制动画

其中1和3应该是最容易实现的，又是消耗资源比较少的方法，但是为了得到更好更可控的动画效果，我采用了**第4种方法**。下面我就结合源码介绍一下我是如何实现的。



### 源码
[VoiceAnimator](https://github.com/l465659833/VoiceAnimator)

自定义ViewGroup取名为VoiceAnimator，其子View叫VoiceAnimationUnit。

外部通过每隔一段时间调用VoiceAnimator.setValue()函数来启动动画效果。

### 实现VoiceAnimator
VoiceAnimator比较简单，主要是作为ViewGroup包裹住VoiceAnimationUnit，然后对作为单独点的VoiceAnimationUnit进行统一启动操作。

实现自定义ViewGroup比View需要多实现一个函数：

```java
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
```

onLayout函数的作用是计算出每一个点的位置，然后通过childView.layout(cl,ct,cr,cb)将VoiceAnimationUnit设置到这个位置上。



至于构造函数、attribute属性、onMeasure等基础的函数，可以参考我以前写的
[【造轮子系列】一个选择星期的工具——SweepSelect View](http://www.jianshu.com/p/eac00d958a63)



#### 先涨先落的关键函数setValue

```java
    private static final int SET_VALUE_ANIMATION_FRAMES_INTERVAL=40;//ms
    private static final int SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP=5;//ms
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

```

其中SET_VALUE_ANIMATION_FRAMES_INTERVAL和SET_VALUE_ANIMATION_FRAMES_INTERVAL_STEP的值是通过反复实验得到的，可以得到比较理想的动画效果。

从源码中就可以看出，其实只是通过调用VoiceAnimationUnit.setVaule()方法的时间间隔变化来调整动效中每个点的先涨先落，
而每个Unit自己来控制自身的动画效果。

### 实现VoiceAnimationUnit

这部分复杂一些，不但要包括快速上涨的动画，还包括缓慢回落的动画，用到了两个Handler进行计算。
#### 1. 加速增加的setValue函数


```
    private float targetValue;          // 上涨时的目标高度，范围（0,1）
    private float currentValue;         // 当前帧计算出的高度值，用于onDraw绘制，范围（0,1）
    private float lastValue;            // 回落时记录上一帧的高度值，范围（0,1）

    private HandlerThread valueHandlerThread=new HandlerThread(TAG);
    private Handler valueHandler=new Handler(valueHandlerThread.getLooper());//用于计算上涨的动画

    private static final int SET_VALUE_ANIMATION_MAX_FRAMES=10;
    private static final int SET_VALUE_ANIMATION_FRAMES_INTERVAL=10;

    private static final int STAY_INTERVAL=50;

    private static final int RESET_VALUE_ANIMATION_MAX_FRAMES=10;
    private static final int RESET_VALUE_ANIMATION_FRAMES_INTERVAL=10;

    private void removeResetMessages() {
        VoiceAnimationUnit.this.changeStep=0;
        drawHandler.removeMessages(VALUE_RESET_START);
        drawHandler.removeMessages(VALUE_RESETTING);
    }


    private void setCurrentValue(float value){
        Log.d(TAG,"setCurrentValue currentValue="+value);
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
        Log.d(TAG,"setValueInterval="+setValueInterval);
        if (targetValue<currentValue){
            Log.d(TAG,"Runnable targetValue<this.targetValue");
        }else {
            removeResetMessages();
        }
        this.targetValue=targetValue;
        valueHandler.post(new Runnable(){
            @Override
            public void run() {
                if (isLoading){
                    return;
                }
                final float lastValue=(Float.isInfinite(currentValue)||Float.isNaN(currentValue))?0:currentValue;
                final float targetValue= VoiceAnimationUnit.this.targetValue;

                Log.d(TAG,"Runnable start currentValue="+lastValue);
                Log.d(TAG,"Runnable start targetValue="+targetValue);
                removeResetMessages();
                float currentValue;
                int changeStep=0;
                while (changeStep <= SET_VALUE_ANIMATION_MAX_FRAMES&&!isLoading) {
                    if (targetValue<lastValue){
                        Log.d(TAG,"Runnable targetValue<this.targetValue");
                    }else {
                        removeResetMessages();
                    }
                    currentValue = lastValue + (targetValue - lastValue) * valueAddingInterpolator.
                            getInterpolation((float) changeStep / (float) SET_VALUE_ANIMATION_MAX_FRAMES);
                    Log.d(TAG,"Runnable currentValue=");
                    setCurrentValue(currentValue);
                    drawHandler.sendEmptyMessage(VALUE_CHANGING);
                    try {
                        Thread.sleep(Math.min(SET_VALUE_ANIMATION_FRAMES_INTERVAL,
                                (setValueInterval==0?SET_VALUE_ANIMATION_FRAMES_INTERVAL:(setValueInterval/SET_VALUE_ANIMATION_MAX_FRAMES))));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeStep++;
                }
                if (targetValue<lastValue){
                    Log.d(TAG,"Runnable targetValue<this.targetValue");
                }else {
                    removeResetMessages();
                }
                drawHandler.sendEmptyMessageDelayed(VALUE_RESET_START,
                        setValueInterval==0?STAY_INTERVAL: (long) ((setValueInterval * 0.4 + STAY_INTERVAL * 0.6) / 2));
            }
        });
    }

```
从源码中可以看出setValue()中将工作添加到valueHandler中进行，而valueHandler中则会进行SET_VALUE_ANIMATION_MAX_FRAMES次计算，
计算出每一帧的位置，然后进行绘制，每帧间隔SET_VALUE_ANIMATION_FRAMES_INTERVAL。

等SET_VALUE_ANIMATION_MAX_FRAMES次计算完成以后，将启动回落的过程。

#### 2. 减速减小的handler

```
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
```

这部分可以结合上面的部分看，其实drawHandler的功能主要就是间隔RESET_VALUE_ANIMATION_FRAMES_INTERVAL时间，就绘制一帧，形成回落的动画。

可能有人会有疑问：

1. 为什么上涨的过程是在整个Runnable中执行，而回落的过程则是通过sendEmptyMessage()实现的。
2. 上涨的过程在整个Runnable中执行，会不会导致多次调用setValue()以后，设置了更大的幅度值，但是Runnable上涨的幅度过小。


很简单

1. 因为回落必须能被打断，在回落的过程中setValue()被调用都要立刻停止回落，并重新上涨。
2. 没错，就是这样，但是这样做是为了实现在最高点处不断震颤的效果。不信的话可以尝试只取targetValue的最大值做动画，最终效果可能像一条死鱼一样在最高点不动。

### 小结

光看代码可能无法体会调整动画的痛苦，这其中的实现方式我做了好几次修改，才最终稳定到现在的版本。
其实目前这种实现方式肯定不是最优的，因为计算4个点的动画，就开启了4个子线程，再加上UI线程，一共用到了5个线程,动画过程中的cpu使用率达到8%左右。
而计算的东西其实是差不多的，只是由于延迟启动造成的时间差导致不能直接使用同一个线程的计算结果，做一些转换可能就能使用了。
所以想尝试的朋友可以试试前面说的方法————自定义View，通过一个线程来计算每个点的高度，然后统一绘制。


