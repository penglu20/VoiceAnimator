package com.pl.voiceAnimation.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pl.voiceAnimation.VoiceAnimator;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    VoiceAnimator voiceAnimator;
    Button start_25_100;
    Button start_50_100;
    Button start_100_100;
    Button start_300_100;
    Button start_loading_12;
    Button start_loading_24;
    Button start_loading_36;
    Button start_loading_default;
    Button show_setter;
    TextView ints;

    int[] data={
            0,0,0,0,1,0,0,0,18,19,
            21,18,9,9,16,20,18,11,17,13,
            17,12,16,16,20,16,5,1,0,4,
            16,17,9,16,20,11,6,16,16,11,
            6,14,16,8,5,13,13,6,2,16,
            18,12,7,13,15,13,4,1,18,15,
            7,3,14,13,6,4,12,10,15,12,
            1,1,0,0,0,0,0,1,0,0
    };

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            Toast.makeText(MainActivity.this,"animation set value end!",Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        voiceAnimator= (VoiceAnimator) findViewById(R.id.voiceAnimator);


        start_25_100= (Button) findViewById(R.id.start_25_100);
        start_50_100= (Button) findViewById(R.id.start_50_100);
        start_100_100= (Button) findViewById(R.id.start_100_100);
        start_300_100= (Button) findViewById(R.id.start_300_100);

        start_loading_12= (Button) findViewById(R.id.start_loading_12);
        start_loading_24= (Button) findViewById(R.id.start_loading_24);
        start_loading_36= (Button) findViewById(R.id.start_loading_36);

        start_loading_default= (Button) findViewById(R.id.start_loading_default);
        show_setter= (Button) findViewById(R.id.show_setter);

        ints= (TextView) findViewById(R.id.ints);

        StringBuilder sb=new StringBuilder();
        sb.append("测试数据：\n");
        for (int i=0;i<data.length;i++){
            if (i!=1&&i%5==1){
                sb.append("\n");
            }
            sb.append(data[i]);
            sb.append("\t,\t");
        }
        ints.setText(sb.toString());

        start_25_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.setAnimationMode(VoiceAnimator.AnimationMode.ANIMATION);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i])/20f);
                            try {
                                Thread.sleep(25);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        });
        start_50_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.setAnimationMode(VoiceAnimator.AnimationMode.ANIMATION);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i])/20f);
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        });
        start_100_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.setAnimationMode(VoiceAnimator.AnimationMode.ANIMATION);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i])/20f);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        });
        start_300_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.setValueInterval(300);
                voiceAnimator.setAnimationMode(VoiceAnimator.AnimationMode.ANIMATION);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i])/20f);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(0);
                    }
                }).start();
            }
        });

        start_loading_12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.startLoading(12);
            }
        });
        start_loading_24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.startLoading(24);
            }
        });

        start_loading_36.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.startLoading(36);
            }
        });
        start_loading_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.startLoading();
            }
        });
        show_setter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAnimator.setDotsCount(5);
                voiceAnimator.setDotsColors(new int[]{Color.parseColor("#ff0000"),Color.parseColor("#ffff00"),Color.parseColor("#00ff00"),Color.parseColor("#0000ff"),Color.parseColor("#00ffff")
                });
                voiceAnimator.setDotsMaxHeight(new float[]{64,48,96,56,72});
                voiceAnimator.startLoading();

                voiceAnimator.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        voiceAnimator.setBackgroundColor(Color.parseColor("#556677"));
                        voiceAnimator.setDotsWidth(40);
                        voiceAnimator.setDotsMargin(40);
                        voiceAnimator.setAnimationMode(VoiceAnimator.AnimationMode.STABLE_HALF);
                    }
                }, 2000);
            }
        });
        voiceAnimator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"voiceAnimator onClick",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
