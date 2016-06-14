package com.pl.voiceAnimation.demo;

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
    Button start_500_100;
    Button start_loading_12;
    Button start_loading_24;
    Button start_loading_36;
    Button start_loading_default;
    TextView ints;

    int[] data={

            13,13,12,12,12,12,13,13,13,21,
            23,16,13,15,13,13,14,13,15,12,
            14,14,23,16,16,16,14,13,17,19,
            15,15,14,14,19,17,15,14,14,13,
            12,15,17,16,21,17,20,27,18,20,
            29,22,23,28,27,28,27,32,33,30,
            34,31,26,18,18,23,33,34,34,28,
            31,31,22,31,29,30,34,32,20,15,
            13,17,12,16,21,31,34,34,34,34,
            33,25,25,12,12,11,33,34,13,12
    };

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this,"animation set value end!",Toast.LENGTH_SHORT).show();
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
        start_500_100= (Button) findViewById(R.id.start_500_100);

        start_loading_12= (Button) findViewById(R.id.start_loading_12);
        start_loading_24= (Button) findViewById(R.id.start_loading_24);
        start_loading_36= (Button) findViewById(R.id.start_loading_36);

        start_loading_default= (Button) findViewById(R.id.start_loading_default);

        ints= (TextView) findViewById(R.id.ints);

        StringBuilder sb=new StringBuilder();
        for (int i=0;i<data.length;i++){
            if (i!=1&&i%5==1){
                sb.append("\n");
            }
            sb.append(data[i]-10);
            sb.append("\t,\t");
        }
        ints.setText(sb.toString());

        start_25_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i]-10)/20f);
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i]-10)/20f);
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i]-10)/20f);
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i]-10)/20f);
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
        start_500_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random random=new Random();
                        for (int i =0;i<data.length;i++) {
                            voiceAnimator.setValue((data[i]-10)/20f);
                            try {
                                Thread.sleep(500);
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

    }
}
