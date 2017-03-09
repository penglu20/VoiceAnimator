# VoiceAnimator

A custom view that imitate google app's voice animation.
实现仿谷歌的语音动画，效果见下图:


![录音效果，每50ms用setValue()传一个值](https://raw.githubusercontent.com/l465659833/VoiceAnimation/master/art/setValue_50.gif)

![录音效果，每100ms用setValue()传一个值](https://raw.githubusercontent.com/l465659833/VoiceAnimation/master/art/setValue_100.gif)

![startLoading()效果](https://raw.githubusercontent.com/l465659833/VoiceAnimation/master/art/startLoading.gif)


# 实现原理简介

[【造轮子系列】仿谷歌语音搜索动画——VoiceAnimation](http://www.siki.space/post/voice_animation_development.html)


#How To Use

add to your build.gradle files:

```
dependencies {
    compile 'com.pl:VoiceAnimator:0.2'
}
```



# Attributes


| attr 属性          | description 描述 | format 类型 |
|:---				 |:---|:---|
| dotsCount  	     | 动画中小圆点的个数 |integer|
| dotColors  	     | 每个小圆点的颜色 |reference数组引用|
| dotsMaxHeight	 	 | 每个小圆点的最大高度 |reference数组引用|
| dotsMinHeight 			 | 小圆点的最小高度 |dimension|
| dotsWidth 	 | 小圆点的宽度 |dimension|
| dotsMargin 	 | 小圆点之间的间距 |dimension|
| animationMode | 动画类型 |enum|

其中AnimationMode的取值有STABLE_MAX,STABLE_MIN,STABLE_HALF,ANIMATION

#Method
###1.  int getDotsCount() ;
获取点的数量


###2. void setDotsCount(int dotsCount) ;
设置点的数量,
  因为会根据点的数量加载点的颜色和最大高度，
  所以必须先调用{@link #setDotsColors(int[])}
  和{@link #setDotsMaxHeight(float[])}将其参数的长度与点的数量保存一致，
  否则可能导致出错
 dotsCount 点的数量
 

  
 
###3. int[] getDotsColors() ;
获取每个点的颜色，rgb颜色

  
   
 
###4. void setDotsColors(int[] dotsColors);
设置每个点的颜色  

dotsColors 每个点的颜色，rgb颜色，推荐使用Color.parseColor("#ff0000")获取

 
###5.float[] getDotsMaxHeight() ;
  获取每个点的最大高度，单位px




 
###6. void setDotsMaxHeight(float[] dotsMaxHeight);
  设置每个点的最大高度  
  
  dotsMaxHeight 最大高度值，单位px

  
  
 
###7. float getDotsMinHeight() ;
获取点的最小高度，单位px


 
###8. void setDotsMinHeight(float dotsMinHeight);
  设置点的最小高度  
  
   dotsMinHeight 最小高度值，单位px


 
###9. float getDotsWidth();
  获取每个点的宽度，单位px



 
###10. void setDotsWidth(float dotsWidth);
  设置每个点的宽度  
  
   dotsWidth 宽度值，单位px


 
###11. float getDotsMargin();
  获取每个点之间的间距，单位px


 
###12. void setDotsMargin(float dotsMargin);
  设置每个点之间的间距  
  
   dotsMargin 间距值，单位px


 
###13. void setAnimationMode(AnimationMode mode);
  设置动画类型

  
 
###14. AnimationMode getAnimationMode();
获取动画类型



 
###15. void setValue(final float targetValue);
  **设置当前动画的幅度值**  
  
  **使用setValue来不断传递声音强度，让动画动起来**  
  
   targetValue 动画的幅度，范围（0,1）

 
###16. void startLoading();
  **启动加载动画**


 
###17. void startLoading(final float height); 
 **启动加载动画**  
 
   height 加载动画的幅度，单位px


