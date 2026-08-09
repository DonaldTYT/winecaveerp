package com.uniinformation.webcore;

import java.io.InputStream;

public class ZkossSessionHelper extends SessionHelper {

    @Override
    public String getURLParam(String p_key) {
        return null;
    }

    @Override
    public InputStream openResourceAsStream(String p_path) {
        return null;
    }

    @Override
    public String getWebContentRealPath(String p_path, boolean p_withSeparator) {
        return null;
    }

    @Override
    public Object lookupEventQueue(String p_name, EVENT_TYPE p_type, boolean p_autoCreate) {
        return null;
    }

    @Override
    public void publishEventQueue(Object p_que, String p_eventStr, Object p_data) {
    }

    public static SessionHelper getSessionHelper() {
        return null;
    }

	public static void showMsg(String p_format, Object...p_args){
	}
	public static void showWarnMsg(String p_format, Object...p_args){
	}
	public static void showErrMsg(String p_format, Object...p_args){
	}
}
