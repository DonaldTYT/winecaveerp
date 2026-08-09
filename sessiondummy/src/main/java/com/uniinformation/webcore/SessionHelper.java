package com.uniinformation.webcore;

import java.io.InputStream;
import java.util.function.Supplier;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SessionHelper {

    public static enum EVENT_TYPE {
        APPLICATION
    }

    public static <T extends SessionHelper> T getSessionHelperDummy(
            String p_iniAgent,
            String p_loginid,
            ServletContext p_svc,
            Supplier<T> newClassCb) {
        return null;
    }

    public static synchronized SessionHelper getSessionHelper(
            HttpServletRequest p_request,
            HttpServletResponse p_response,
            boolean p_requireNew,
            Supplier<SessionHelper> newClassCb) {
        return null;
    }

    public ServletContext getSvc() {
        return null;
    }

    public abstract String getURLParam(String p_key);

    public abstract InputStream openResourceAsStream(String p_path);

    public abstract String getWebContentRealPath(String p_path, boolean p_withSeparator);

    public abstract Object lookupEventQueue(
            String p_name,
            EVENT_TYPE p_type,
            boolean p_autoCreate);

    public abstract void publishEventQueue(
            Object p_que,
            String p_eventStr,
            Object p_data);
}
