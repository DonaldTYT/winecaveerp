package com.uniinformation.dynamic.aw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

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
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ConfirmAndPrintOrder extends BiActionHandler implements JxActionListener {
	boolean needRefresh = false;
	BiResult quoBr = null;
	RpcClient rpc = null;
	Hashtable<Integer,String> cfmQuoSet;
	ChnftrParser mainparser = null;
	public ConfirmAndPrintOrder() {
		super(null);
	}
	public ConfirmAndPrintOrder(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		needRefresh = false;
		cfmQuoSet = new Hashtable<Integer,String>();
		if(cnt > 50) {
			return(new ReturnMsg(false,"Cannot Print more than 50 orders"));
		}
		try {
			mainparser = new ChnftrParser((InputStream)null, "-p14");
			rpc = biBase.getSessionHelper().getRpcClient();
		ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
		rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
		Value val = rpc.callSegment("printer_autoselect",
				new VectorUtil()
				.addElement(1)
				.toVector()
			);
		//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
		val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(p_result.getSessionHelper().getWebContentRealPath("images", true)) .toVector());
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Initialized Print Job Failed"));
		}
		return(ReturnMsg.defaultOk);		
	}
	
	ReturnMsg confirmOneQuotation(BiResult p_result, BiResult quoBr,int invrg) {
		if(invrg < 300590) return(ReturnMsg.defaultOk);
		String quoStatus = cfmQuoSet.get(invrg);
		if(quoStatus == null) {
			quoBr.clearCondition();
			quoBr.addCustomCondition("inv_rg = "+invrg);
			quoBr.query();
			if(quoBr.getRecordCount() == 1) {
				try {
					quoBr.loadOneRecV(0);
					String ss = quoBr.getCellString("inv_quostatus");
					if(ss.equals("Confirmed")) {
						cfmQuoSet.put(invrg, "Confirmed");
					} else if(ss.equals("Void")) {
						cfmQuoSet.put(invrg, "Void");
					} else {
						quoBr.fetchOneRecV(0);
						quoBr.getCell("inv_quostatus").set("Confirmed");
						ReturnMsg rtn = quoBr.updateCurrent();
						if(rtn == null || rtn.getStatus()) {
							cfmQuoSet.put(invrg, "Confirmed");
							needRefresh = true;
						} else {
							cfmQuoSet.put(invrg, rtn.getMsg());
						}
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					cfmQuoSet.put(invrg, "Quotation Not Found");
				}
			} else {
				return(new ReturnMsg(false,"Quotation Not Found"));
			}
			quoStatus = cfmQuoSet.get(invrg);
		}
		if(quoStatus.equals("Confirmed")) 
			return(ReturnMsg.defaultOk);
		else if(quoStatus.equals("Void")) 
			return(new ReturnMsg(false,"Quotation is Void"));
		else	
			return(new ReturnMsg(false,"Failded to confirm Quotation : " + quoStatus));
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		if(quoBr == null) quoBr = p_result.getSessionHelper().getBiSchema().getViewByName("erpv4.QuotationG2").newBiResult(p_result.getSessionHelper().getLoginId(), null, null, p_result.getSessionHelper());
		int invrg = p_result.getCellInt("inv_rg");
		ReturnMsg rtn = confirmOneQuotation(p_result, quoBr,invrg);
		if(rtn != null && !rtn.getStatus())  return(rtn);
		
		
		try {
			boolean ok = p_result.fetchOneRecV(p_recIdx);
			if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
			Value val = rpc.callSegment("erpv4_print_wo",
			new VectorUtil()
			.addElement(p_result.getCell("jm_rg").getInt())
			.addElement("CHNPRINT")
			.addElement("VARIABLE")
			.addElement("A3P")
			.addElement("NORMAL")
			.addElement("LPTRAW")
			.toVector()
			);
			if(val == null || !val.toString().startsWith("OK")) {
				return(new ReturnMsg(false,"Print Work Order " + p_result.getCellString("inv_invno") + " Failed reason " + (val == null ? "null" : val.toString())));
			}
			String fname = val.toString().substring(4);
			InputStream is = biBase.getSessionHelper().newErpFileInputStream(fname);
			ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ps.print(bos);
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			mainparser.loadTemplateStream(bis);
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			rpc.close();
			return(new ReturnMsg(false,"Print Work Order " + p_result.getCellString("inv_invno") + " Failed " ));
		}	
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		/*
		if(!cfmSet.isEmpty()) {
			BiResult quoBr = p_br.getSessionHelper().getBiSchema().getViewByName("erpv4.QuotationG2").newBiResult(p_br.getSessionHelper().getLoginId(), null, null, p_br.getSessionHelper());
			for(int invrg : cfmSet) {
				ReturnMsg rtn = confirmOneQuotation(p_br, quoBr,invrg);
				if(rtn != null && !rtn.getStatus()) {
					return(rtn);
				}
			}
		}
		*/
		//return (ReturnMsg.defaultOk);
					rpc.close();
//					ByteArrayInputStream bis = null;
//					ZkUtil.printFromStream(bis, "application/pdf", getSessionHelper());
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						mainparser.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						ZkUtil.printFromStream(bis, "application/pdf", biBase.getSessionHelper());
						if(needRefresh) {
							biBase.biBaseRefresh(p_br);
						}
						return(ReturnMsg.defaultOk);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"End Print Job Failed"));
					}
	}
	@Override
	public void actionPerformed(JxField field) {
		
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult br = jxf.getBr();
		int invrg = br.getCellInt("inv_rg");
		needRefresh = false;
		cfmQuoSet = new Hashtable<Integer,String>();
		if(quoBr == null) quoBr = br.getSessionHelper().getBiSchema().getViewByName("erpv4.QuotationG2").newBiResult(br.getSessionHelper().getLoginId(), null, null, br.getSessionHelper());
		ReturnMsg rtn = confirmOneQuotation(br, quoBr,invrg);
		if(rtn != null && !rtn.getStatus()) {
			field.getJxForm().messageBox("Error while confirm quotation " + rtn.getMsg());
		} else {
			
			RpcClient rpc = jxf.getRpcClient();
			ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
			rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
			Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
					);
			//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
			val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(br.getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			val = rpc.callSegment("erpv4_print_wo",
						new VectorUtil()
						.addElement(jxf.getBr().getCell("jm_rg").getInt())
						.addElement("CHNPRINT")
						.addElement("VARIABLE")
						.addElement("A3P")
						.addElement("NORMAL")
						.addElement("LPTRAW")
						.toVector()
					);
			rpc.close();
			if(val != null && val.toString().startsWith("OK")) {
				String fname = val.toString().substring(4);
				UniLog.log("Print wo got " + fname);
				try {
					InputStream is = jxf.erpFileInputStream(fname);
					ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
//					ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ps.print(bos);
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
					ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}			
			if(needRefresh) {
				jxf.getBr().refetchCurrent();
				jxf.bindCellCollection(jxf.getBr(), jxf.MODE_UPDATE);
//				((JxZkBiBase) jxf).refreshAllListitem();
			}
	 	}
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
}
