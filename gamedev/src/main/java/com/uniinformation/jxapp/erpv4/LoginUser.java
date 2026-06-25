package com.uniinformation.jxapp.erpv4;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.WordPressHelper;

public class LoginUser extends JxZkBiBase {
	@Override
	public void afterBind() {
		super.afterBind();
		Textbox tb = (Textbox) jxAdd("lgu_bpcode").getNativeObject();
		tb.setType("password");
		Boolean b = (Boolean) getSessionObject("STRAIGHTPASSWORD");
		if(b != null && b ){
			Component pc = tb.getParent();
			Hlayout hl = new Hlayout();
			tb.setParent(hl);
			Label lb = new Label("Must be mixture of both uppercase and lowercase letters and include at least one special character, e.g., ! @ # ?");
					
			hl.appendChild(lb);
			pc.appendChild(hl);
		}
		tb = (Textbox) jxAdd("lgu_pwd2").getNativeObject();
		tb.setType("password");
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		try {
			br.getCell("lgu_pwd2").set(br.getCellString("lgu_bpcode"));
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
	@Override
	protected ReturnMsg beforeUpdate(BiResult br)
	{
		if(!br.getCellString("lgu_pwd2").equals(br.getCellString("lgu_bpcode"))) {
			return(new ReturnMsg(false,"Password Not Match"));
		}
		return ReturnMsg.defaultOk;
	}
	@Override
	protected ReturnMsg beforeAdd(BiResult br)
	{
		if(!br.getCellString("lgu_pwd2").equals(br.getCellString("lgu_bpcode"))) {
			return(new ReturnMsg(false,"Password Not Match"));
		}
		/*
		if (sessionHelper.getWPLinkUser()) {
			WordPressHelper wph = new WordPressHelper(sessionHelper);
			wph.updateUser(p_login, p_email, p_password);  //for add or update account
			wph.deleteUser(p_login)  //for del account
			wph.triggerSync();  //for sync stock
		}
		*/
		return ReturnMsg.defaultOk;
	}
}
