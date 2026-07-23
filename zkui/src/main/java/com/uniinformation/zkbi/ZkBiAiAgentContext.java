package com.uniinformation.zkbi;

import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;

import com.uniinformation.webcore.SessionHelper;

/** Live page and capability context supplied to a ZK BI AI agent dialog. */
public interface ZkBiAiAgentContext {
    SessionHelper getAiHelpSessionHelper();

    Component getAiHelpParentComponent();

    /** Builds a fresh snapshot of the invoking composer's current state. */
    JSONObject getAiHelpContext() throws JSONException;

    /** Lists the page operations for which this live view can provide exact guidance. */
    JSONObject getAiHelpOperationCatalog() throws JSONException;

    /** Returns exact, read-only guidance for one operation in the current page state. */
    JSONObject getAiHelpOperationHelp(String operationId) throws JSONException;
}
