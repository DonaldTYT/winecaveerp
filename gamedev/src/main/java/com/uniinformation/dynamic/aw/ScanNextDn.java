package com.uniinformation.dynamic.aw;

import org.zkoss.zk.ui.util.Clients;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ScanNextDn extends BiActionHandler implements JxActionListener {

	public ScanNextDn() {
		super(null);
	}
	public ScanNextDn(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub
//		Clients.evalJavaScript(
//					"if (window.ZkBiCamera) {" +
//					"  ZkBiCamera.open({" +
//					"    mode: 'scanner'," +
//					"    autoStopAfterScan: true," +
//					"    onScan: function(text) {" +
//					"      var loginId = zk.Widget.$('$loginId');" +
//					"      if (loginId) {" +
//					"        loginId.$n().value = text;" +
//					"        loginId.updateChange_();" +
//					"      }" +
//					"    }" +
//					"  });" +
//					"} else {" +
//					"  alert('Camera scanner script is not loaded.');" +
//					"}"
//				);
			Clients.evalJavaScript(
     					"if (window.ZkBiCamera) {" +
     					"  ZkBiCamera.open({" +
     					"    mode: 'scanner'," +
     					"    autoStopAfterScan: true," +
     					"    onScan: function(text) {" +
                        "       var zkComp = getMainComp();" +
                        "       if (zkComp !== null && typeof zAu !== 'undefined'){" + 
                        "          zAu.send(new zk.Event(zkComp, 'onBarcodeNotify', text, {toServer:true}));" +
                        "       }"+
     					"    }" +
     					"  });" +
     					"} else {" +
     					"  alert('Camera scanner script is not loaded.');" +
     					"}"
     				);
		/*
		ZkUtil.js("startWebcamScanner()");	
		*/
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		// TODO Auto-generated method stub
		return null;
	}

}
