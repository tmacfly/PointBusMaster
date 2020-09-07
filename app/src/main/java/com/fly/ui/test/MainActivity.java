package com.fly.ui.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fly.ThreadMode;
import com.fly.annotation.PointSubscribe;
import com.fly.bus.PointBus;
import com.fly.ui.event.TestEvent;
import com.fly.ui.myapplication.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PointBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
    }

    public void onTestClick(View view){
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    @PointSubscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(TestEvent testEvent) {
        Toast.makeText(this,testEvent.msg,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PointBus.getDefault().unRegister(this);
    }
}