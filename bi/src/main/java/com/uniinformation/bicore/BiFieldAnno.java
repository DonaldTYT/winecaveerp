package com.uniinformation.bicore;

import java.lang.annotation.*;
/***
 * For testing only
 * Similar to zk @Wire annotation
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BiFieldAnno {
    public String value() default "";
}