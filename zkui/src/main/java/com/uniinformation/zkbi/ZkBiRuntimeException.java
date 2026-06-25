package com.uniinformation.zkbi;

public class ZkBiRuntimeException extends RuntimeException{
	private boolean ignoreFlag = false;
	public ZkBiRuntimeException() {
		super();
	}
	public ZkBiRuntimeException(String str) {
		super(str);
	}
	public ZkBiRuntimeException(Exception ex){
		super(ex);
	}
	public ZkBiRuntimeException setIgnore() {
		ignoreFlag = true;
		return this;
	}
	public boolean getIgnore() {
		return ignoreFlag;
	}
}
