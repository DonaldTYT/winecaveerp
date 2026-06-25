package com.uniinformation.zkf;

import java.io.InputStream;

import org.zkoss.zk.ui.Component;

import com.uniinformation.cell.CellCollection;
import com.uniinformation.webcore.SessionHelper;

public interface ZkCellActionInterface {
	void init(Component arg0);
	void afterCompose(CellCollection arg0);
	void beforeMapCollection(SessionHelper sh);
	void processActionByComposer(String p_eventName,Component p_target,boolean p_needResponse,InputStream p_upload)  throws Exception;
}
