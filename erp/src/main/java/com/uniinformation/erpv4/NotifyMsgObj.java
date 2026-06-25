package com.uniinformation.erpv4;

public class NotifyMsgObj {
	public static enum Level { norm, warn, err };
	final public String msg;
	final public Level level;
	public NotifyMsgObj(String msg, Level level) {
		this.msg = msg;
		this.level = level;
	}
	public String toString() {
		return this.msg;
	}

}
