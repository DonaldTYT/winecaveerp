package com.uniinformation.utils;

public class ThreadControlNoThreadAvailableException extends Exception {
   public ThreadControlNoThreadAvailableException() {
	   super();
	}
   public ThreadControlNoThreadAvailableException(String p_message) {
	   super(p_message);
	}
}
