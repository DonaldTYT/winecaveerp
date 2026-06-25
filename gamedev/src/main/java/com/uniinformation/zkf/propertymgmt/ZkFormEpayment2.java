package com.uniinformation.zkf.propertymgmt;

import javax.servlet.http.HttpServletRequest;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;

import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.QRCodeUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkFormEpayment2 extends ZkCellActionForm {
	@Wire
	Div btGroup1;
	@Wire
	Div btGroup2;
	@Wire
	Div barcodeS1;
	@Wire
	Image barcodeImg;
	
	TableRec devTr;
	TableRec typeTr;
	static public enum RUNSTATE {STATE_IDLE}
	
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				Component c = (Component)arg0.getTarget();
				UniLog.log("Event " + arg0.getName() + " Id " + c.getId());
				if(c.getId().equals("btPayResidental")) {
					if(btGroup2 != null) {
						if(btGroup1 != null) {
							btGroup1.setVisible(false);
						}
						btGroup2.setVisible(true);
					}
					
				}
				if(c.getId().equals("btExitGroup1")) {
					if(btGroup1 != null) {
						if(btGroup2 != null) {
							btGroup2.setVisible(false);
						}
						btGroup1.setVisible(true);
					}
					
				}
				if(c.getId().equals("btExitBarcodeS1")) {
					if(btGroup1 != null) {
						if(barcodeS1 != null) {
							barcodeS1.setVisible(false);
						}
						btGroup1.setVisible(true);
					}
					
				}
				if(c.getId().equals("btfloor01")) {
					if(barcodeS1 != null && barcodeImg != null) {
   		 				byte[] imgBytes = QRCodeUtil.createQRCode(
//   		 									"BindBcnToSession:"+sessionHelper.hashCode()
   		 									"http://www.hellovoice.com"
   		 									,500,500,"PNG");
   		 				barcodeImg.setSrc(Base64Util.convertToImgString(imgBytes, "PNG"));
						if(btGroup2 != null) {
							btGroup2.setVisible(false);
						}
						barcodeS1.setVisible(true);
					}
					
				}
			}
		};
//		customLoginUrl = "deviceLogin.html";
//		customLoginUrl = "http://www.hellovoice.com";
		super.doAfterCompose(arg0);
		String targetURL = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getRequestURL().toString();
		String  queryString = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getQueryString();
      	String deviceId = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getParameter("deviceid");
		if(queryString != null) targetURL += "?" + queryString;
		sessionHelper.setLogoutURL(targetURL);
		SelectUtil su = sessionHelper.getBiSchema().getSelectUtil();
		devTr = su.getQueryResult("select * from devicelogin,location where ldv_login = ? and lc_rg = ldv_lcrg" , 
					new Wherecl().appendArgument(deviceId)
					);
		devTr.setRecPointer(0);
		Erpv4Config.setDefaultLcrg(sessionHelper, devTr.getFieldInt("ldv_lcrg"));
		formCollection.getCell("ldv_cmpname").set(Erpv4Config.getCoName(sessionHelper, Erpv4Config.getDefaultCoCode(sessionHelper)));
		formCollection.getCell("ldv_locname").set(Erpv4Config.getLcDesc(sessionHelper, Erpv4Config.getDefaultLcrg(sessionHelper)));
		formCollection.getCell("ldv_title").set("自助繳費機");
		typeTr = su.getQueryResult("select unique col_a ptype from property where col_b = ? ",
					new Wherecl().appendArgument(devTr.getFieldString("lc_desc"))
				 );
		
	}
}
