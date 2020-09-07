package com.fly.poster;

import com.fly.bus.PointBus;
import java.lang.reflect.Method;

/**
 * @Author: wangxiang
 * @CreateDate: 2020/8/18 4:40 PM
 * @Description:
 */
public class BackgroundPoster implements Poster {

    @Override
    public void enqueue(final Method method, final Object subscriber, final Object event) {
        PointBus.getDefault().getExecutorService().execute(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(subscriber, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
