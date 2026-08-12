package com.uniinformation.dynamic.aw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintWorkOrderFromQuotation extends BiActionHandler implements JxActionListener {
	boolean needRefresh = false;
	BiResult woBr = null;
	RpcClient rpc = null;
	Hashtable<Integer,String> cfmQuoSet;
	ChnftrParser mainparser = null;
	public PrintWorkOrderFromQuotation() {
		super(null);
	}
	public PrintWorkOrderFromQuotation(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		return new ReturnMsg(false,"Batch Print not supported");
	}
	
	@Override
	public void actionPerformed(JxField field) {
		
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult br = jxf.getBr();
		/*
		if(
				(!"Confirmed".equals(br.getCellString("inv_quostatus")))
				|| StringUtils.isBlank(br.getCellString("inv_jobno"))
				) {
			field.getJxForm().messageBox("Please confirm quotation to print");
			return;
		}
		*/
		int invrg = br.getCellInt("inv_rg");
		needRefresh = false;
		cfmQuoSet = new Hashtable<Integer,String>();
		if(woBr == null) woBr = br.getSessionHelper().getBiSchema().getViewByName("aw.WorkOrder").newBiResult(br.getSessionHelper().getLoginId(), null, null, br.getSessionHelper());
		woBr.clearCondition();
		woBr.clearOrderBy();
		woBr.addCustomCondition("jm_jobno = '"+ br.getCellString("inv_jobno") + "'");
		woBr.addOrderByColumnList("jm_rev", false);
		woBr.query();
		if(woBr.getRecordCount() < 1) {
			field.getJxForm().messageBox("Workorder Not Created");
			return;
		}
		RpcClient rpc = jxf.getRpcClient();
		try {
			mainparser = new ChnftrParser((InputStream)null, "-p14");
			ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
			rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
			Value val = rpc.callSegment("printer_autoselect",
					new VectorUtil()
					.addElement(1)
					.toVector()
				);
			val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(br.getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			for(int i=0;i<woBr.getRecordCount();i++) {
				woBr.fetchOneRecV(i);
				val = rpc.callSegment("erpv4_print_wo",
						new VectorUtil()
						.addElement(woBr.getCell("jm_rg").getInt())
						.addElement("CHNPRINT")
						.addElement("VARIABLE")
						.addElement("A3P")
						.addElement("NORMAL")
						.addElement("LPTRAW")
						.toVector()
					);
				if(val != null && val.toString().startsWith("OK")) {
					String fname = val.toString().substring(4);
					InputStream is = jxf.erpFileInputStream(fname);
					ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ps.print(bos);
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					mainparser.loadTemplateStream(bis);
				} else break;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mainparser.print(bos);
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ZkUtil.printFromStream(bis, "application/pdf", br.getSessionHelper());
		} catch (Exception ex) {
			UniLog.log(ex);
			field.getJxForm().messageBox("Print Error " + ex.toString());
		}
		rpc.close();
	}

	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(p_br.getSessionHelper().hasAccessRight("#cfmwo"));
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			String qs = p_br.getCellString("inv_quostatus");
			if(qs.equals("Void")) return(true);
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
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
