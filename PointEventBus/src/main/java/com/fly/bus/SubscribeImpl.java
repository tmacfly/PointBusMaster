package com.fly.bus;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: wangxiang
 * @CreateDate: 2020/8/12 8:54 PM
 * @Description:
 */
public interface SubscribeImpl {
    HashMap<String, List<SubscribeInfo>> getSubscribeInfo();
}
