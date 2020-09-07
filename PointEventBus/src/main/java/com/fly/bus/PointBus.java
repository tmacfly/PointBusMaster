package com.fly.bus;

import android.os.Looper;
import android.util.Log;

import com.fly.poster.BackgroundPoster;
import com.fly.poster.MainPoster;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

/**
 * @Author: wangxiang
 * @CreateDate: 2020/8/4 9:54 PM
 * @Description:
 */

public class PointBus {
    private static HashMap<String, List<Object>> registerMap = new HashMap<>();
    private static volatile PointBus defaultInstance;
    private static ExecutorService executorService;
    private static String[] classArray = {"com.fly.subscribe.MyEventBusIndex123", "com.fly.subscribe.MyEventBusIndex"};

    public static MainPoster getMainPoster() {
        if (mainPoster == null) {
            mainPoster = new MainPoster(Looper.getMainLooper());
        }
        return mainPoster;
    }

    public static BackgroundPoster getBackgroundPoster() {
        if (backgroundPoster == null) {
            backgroundPoster = new BackgroundPoster();
        }
        return backgroundPoster;
    }

    private static MainPoster mainPoster;
    private static BackgroundPoster backgroundPoster;

    private PointBus() {
        executorService = newCachedThreadPool();
        mainPoster = new MainPoster(Looper.getMainLooper());
        backgroundPoster = new BackgroundPoster();
    }

    public static PointBus getDefault() {
        PointBus instance = defaultInstance;
        if (instance == null) {
            synchronized (PointBus.class) {
                instance = PointBus.defaultInstance;
                if (instance == null) {
                    instance = PointBus.defaultInstance = new PointBus();
                }
            }
        }
        return instance;
    }

    public ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = newCachedThreadPool();
        }
        return executorService;
    }


    public void post(Object event, String className) {
        post(null, event, className);
    }

    public void post(Object sender, Object event, String className) {

        List<Object> subscribers = registerMap.get(className);
        if (subscribers == null || subscribers.size() == 0) {
            return;
        }
        for (Object subscriber : subscribers) {
            doEventAction(sender, subscriber, event, className);
        }
    }

    private synchronized void doEventAction(Object sender, Object subscriber, Object event, String className) {
        for (String classzName : classArray) {
            invokeEvent(sender, subscriber, event, className, classzName);
        }

    }

    private void invokeEvent(Object sender, Object subscriber, Object event, String className, String classzName) {
        try {
            Class<?> subscriberClass = subscriber.getClass();
            Class<?> senderClass = null;
            if (sender != null) {
                senderClass = sender.getClass();
            }
            Class<?> eventClass = event.getClass();
            String eventName = eventClass.getCanonicalName();
            Class classz = Class.forName(classzName);
            if(classz == null) return;
            SubscribeImpl subscribe = (SubscribeImpl) classz.newInstance();
            HashMap<String, List<SubscribeInfo>> listHashMap = subscribe.getSubscribeInfo();
            List<SubscribeInfo> infos = listHashMap.get(className);
            if (infos == null || infos.size() == 0) {
                return;
            }
            for (SubscribeInfo info : infos) {
                if (info.getEventName().equals(eventName)) {
                    //Log.i("myTag", "match method name  = " + info.getMethodName() + "   method event = " + info.getEventName());
                    Class classevent = Class.forName(eventName);
                    Method method = subscriberClass.getDeclaredMethod(info.getMethodName(), classevent);
                    if (info.getThreadMode().equals("MAIN")) {
                        getMainPoster().enqueue(method, subscriber, event);
                    } else if (info.getThreadMode().equals("BACKGROUND")) {
                        getBackgroundPoster().enqueue(method, subscriber, event);
                        //method.invoke(subscriber, event);
                    } else {
                        method.invoke(subscriber, event);
                    }
                    if (senderClass != null) {
                        Log.i("PointBus", "【Receiver】:  " + className + "  【Event】:  " + eventClass.getCanonicalName() + " 【Sender】:  " + senderClass.getCanonicalName());
                    } else {
                        Log.i("PointBus", "【Receiver】:  " + className + "  【Event】:  " + eventClass.getCanonicalName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        String className = subscriberClass.getCanonicalName();
        Log.i("myTag", "register class Name = " + className);
        List<Object> subscribers;
        if (registerMap.get(className) != null) {
            subscribers = registerMap.get(className);
            subscribers.add(subscriber);
        } else {
            subscribers = new ArrayList<>();
            subscribers.add(subscriber);
        }
        registerMap.put(className, subscribers);
    }

    public synchronized void unRegister(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        String className = subscriberClass.getCanonicalName();
        if (registerMap.get(className) != null) {
            List<Object> subscribers = registerMap.get(className);
            if (subscribers.contains(subscriber)) {
                subscribers.remove(subscriber);
            }
        }
    }
}
