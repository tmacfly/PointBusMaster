package com.fly.poster;

import android.os.Handler;
import android.os.Looper;

import com.fly.bus.PointBus;

import java.lang.reflect.Method;

/**
 * @Author: wangxiang
 * @CreateDate: 2020/8/18 4:40 PM
 * @Description:
 */
public class MainPoster extends Handler implements Poster {

    public MainPoster(Looper looper) {
        super(looper);
    }

    @Override
    public void enqueue(final Method method, final Object subscriber, final Object event) {
        PointBus.getDefault().getExecutorService().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    method.invoke(subscriber, event);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
        );
    }
}
