package com.fly.bus;

/**
 * @Author: wangxiang
 * @CreateDate: 2020/8/4 9:30 PM
 * @Description:
 */
public class SubscribeInfo {
    private String methodName;
    private String eventName;
    private String threadMode;


    public SubscribeInfo(String methodName, String eventName, String threadMode) {
        this.methodName = methodName;
        this.eventName = eventName;
        this.threadMode = threadMode;
    }

    public String getThreadMode() {
        return threadMode;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getEventName() {
        return eventName;
    }

    @Override
    public String toString() {
        return "SubscribeInfo{" +
                "methodName='" + methodName + '\'' +
                ", eventName='" + eventName + '\'' +
                '}';
    }
}
