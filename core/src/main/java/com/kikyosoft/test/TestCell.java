package com.kikyosoft.test;

import java.lang.instrument.Instrumentation;

import com.kikyosoft.utils.LogUtil;

public class TestCell {
	    private static volatile Instrumentation globalInstrumentation;

	    public static void premain(final String agentArgs, final Instrumentation inst) {
	        globalInstrumentation = inst;
	    }

	    public static long getObjectSize(final Object object) {
	        if (globalInstrumentation == null) {
	            throw new IllegalStateException("Agent not initialized.");
	        }
	        return globalInstrumentation.getObjectSize(object);
	    }
	
	
	
	public static void main(String args[]){
		LogUtil.log("TestCell");
		Double dd = 0.0;
		LogUtil.log("Double Size = "+getObjectSize(dd));
		
	}
}
