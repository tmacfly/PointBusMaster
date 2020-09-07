package com.fly.ui.subscriber;

/**
 * @Author: wangxiang
 * @CreateDate: 2020/8/4 9:30 PM
 * @Description:
 */
public class SimpleSubscribeInfo {
    private String methodName;
    private String eventName;
    private String threadMode;

    public SimpleSubscribeInfo(String methodName, String eventName,String threadMode) {
        this.methodName = methodName;
        this.eventName = eventName;
        this.threadMode = threadMode;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getThreadMode() {
        return threadMode;
    }
}
