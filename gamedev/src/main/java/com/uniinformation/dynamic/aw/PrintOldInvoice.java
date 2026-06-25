package com.uniinformation.dynamic.aw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.zkoss.zk.ui.Executions;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultInvoiceBase;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
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

public class PrintOldInvoice  extends BiActionHandler implements JxActionListener {

	public PrintOldInvoice() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public PrintOldInvoice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResultInvoiceBase  br = (BiResultInvoiceBase) jxf.getBr();
		int invrg = br.getCellInt("invh_rg");
		RpcClient rpc = jxf.getRpcClient();
		ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
		rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
		Value val = rpc.callSegment("printer_autoselect",
					new VectorUtil()
					.addElement(1)
					.toVector()
				);
		//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement( br.getCellString("invh_cocode"))
				.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),br.getCellString("invh_cocode")))
				.toVector()
				);
		val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(br.getSessionHelper().getWebContentRealPath("images", true)) .toVector());
		val = rpc.callSegment("artway_print_invoice",
					new VectorUtil()
					.addElement(invrg)
					.addElement("CHNPRINT")
					.addElement("VARIABLE")
					.addElement("A4P")
					.addElement("NORMAL")
					.addElement("LPTRAW")
					.toVector()
				);
		rpc.close();
		if(val != null && val.toString().startsWith("OK")) {
			String fname = val.toString().substring(4);
			UniLog.log("Print invoice got " + fname);
			try {
				InputStream is = jxf.erpFileInputStream(fname);
				ChnftrParser ps = new ChnftrParser(is,""); // print as A3 , always two pages
//				ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ps.print(bos);
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
//		Messagebox.show("Print Old Invoice ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
//				new EventListener() {
//				   public void onEvent(Event evt) throws Exception {
//				    	if (((Integer)evt.getData()) == Messagebox.YES){
//				    	} else{
//				    		return;
//				    	}
//				   }
//				}
//			)k";
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
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
	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(true);
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			if(!p_br.getSessionHelper().hasAccessRight("#prtinv")) {
				return(true);
			}
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
	
	public ReturnMsg isRunnable(BiResult br,boolean isBatch) {
		return(ReturnMsg.defaultOk);
	}
}
