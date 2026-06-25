package com.uniinformation.cell;

import java.lang.instrument.Instrumentation;

import com.kyoko.common.CoreLog;

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
		CoreLog.log("TestCell");
		Double dd = 0.0;
		CoreLog.log("Double Size = "+getObjectSize(dd));
		
	}
}
