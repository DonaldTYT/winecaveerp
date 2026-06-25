package com.uniinformation.zkf;

import java.io.InputStream;

import org.zkoss.zk.ui.Component;

import com.google.gson.JsonObject;
import com.uniinformation.cell.CellCollection;
import com.kyoko.common.*;
import com.uniinformation.webcore.SessionHelper;

public interface ZkfAction {
	public ReturnMsg processAction(String p_id,SessionHelper p_sh,CellCollection p_col, JsonObject p_actionData, InputStream p_upload,Component p_target) throws Exception;
}
