package com.fly.ui.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fly.ThreadMode;
import com.fly.annotation.PointSubscribe;
import com.fly.bus.PointBus;
import com.fly.ui.event.TestEvent;
import com.fly.ui.myapplication.R;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void testPost() {
        PointBus.getDefault().post(this, new TestEvent("from TestActivity"), "com.fly.ui.test.MainActivity");
    }

    public void onTestClick(View view){
        testPost();
    }

    @PointSubscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(TestEvent testEvent) {
        Toast.makeText(this,testEvent.msg,Toast.LENGTH_LONG).show();
    }
}