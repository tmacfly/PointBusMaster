package com.fly.annotation;


import com.fly.ThreadMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface PointSubscribe {
    ThreadMode threadMode() default ThreadMode.POSTING;
}