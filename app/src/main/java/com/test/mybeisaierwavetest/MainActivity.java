package com.test.mybeisaierwavetest;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    WaveFunctionView bezierView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bezierView = findViewById(R.id.BezierView);
        init();
    }

    public void init() {
        bezierView.postDelayed(new Runnable() {
            @Override
            public void run() {
                bezierView.ChangeWaveLevel(1);
            }
        }, 3100);

        bezierView.postDelayed(new Runnable() {
            @Override
            public void run() {
                bezierView.ChangeWaveLevel(8);
            }
        }, 6200);


        bezierView.postDelayed(new Runnable() {
            @Override
            public void run() {
                bezierView.ChangeWaveLevel(3);
            }
        }, 9200);
    }
}
