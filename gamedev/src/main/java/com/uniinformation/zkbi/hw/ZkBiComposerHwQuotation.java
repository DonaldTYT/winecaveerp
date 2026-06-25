package com.uniinformation.zkbi.hw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerHwQuotation extends ZkBiComposerBase {
	@Override
    protected void setupExtraButton(final BiResult result) {
    	super.setupExtraButton(result);
    	
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbBatchPrintWo",sessionHelper.getBtLabel("Batch Print"),"fa-print",
			new BiActionHandler(this) {
				RpcClient rpc = null;
				ChnftrParser mainparser = null;

				@Override
				public ReturnMsg beforeAction(BiResult p_result,int cnt) { 
					if(cnt > 50) {
						return(new ReturnMsg(false,"Cannot Print more than 50 orders"));
					}
					
					try {
//					mainparser = new ChnftrParser((InputStream)null, "-p14");
					mainparser = new ChnftrParser((InputStream)null, "");
					rpc = getSessionHelper().getRpcClient();
					ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
					rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
					Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
					//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
					val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"Initialized Print Job Failed"));
					}
					return(ReturnMsg.defaultOk);
				}

				@Override
				public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							boolean ok = result.fetchOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
							
							RpcClient rpc = getSessionHelper().getRpcClient();
							ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
							rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
							Value val = rpc.callSegment("printer_autoselect",
									new VectorUtil()
									.addElement(1)
									.toVector()
							);
							
							val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			
							val = rpc.callSegment("erpv4_print_wo",
									new VectorUtil()
									.addElement(result.getCell("inv_rg").getInt())
									.addElement("USEFILEING")
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
								UniLog.log("Print wo got " + fname);
								InputStream is = getSessionHelper().newErpFileInputStream(fname);
								ChnftrParser ps = new ChnftrParser(is,"'");
								ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
									@Override
									public byte[] getImage(String p_key) {
										if (!StringUtils.startsWith(p_key, "jxHwQuoDetFiling_")){
											UniLog.logm(this, "invalid getImage key %s", p_key);
											return(null);
										}
										try {
											ByteArrayOutputStream bos = new ByteArrayOutputStream();
											FilingUtil.getFile(sessionHelper.getAgent(), null, p_key, bos);
											byte[] bytes = bos.toByteArray();
											bos.close();
											return(bytes);
										}
										catch(Exception ex){
											ex.printStackTrace();
											return(null);
										}
									}}
								);
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								ps.print(bos);
								ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
								mainparser.loadTemplateStream(bis);
							};
//							ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//							mainparser.loadTemplateStream(bis);
							return(ReturnMsg.defaultOk);
						} catch (Exception ex) {
							UniLog.log(ex);
							rpc.close();
							return(new ReturnMsg(false,"Print Work Order " + result.getCellString("inv_invno") + " Failed " ));
						}
				}

				@Override
				public ReturnMsg afterAction(BiResult p_br) {
					rpc.close();
//					ByteArrayInputStream bis = null;
//					ZkUtil.printFromStream(bis, "application/pdf", getSessionHelper());
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						mainparser.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						ZkUtil.printFromStream(bis, "application/pdf", getSessionHelper());
						return(ReturnMsg.defaultOk);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"End Print Job Failed"));
					}
				}
			}
		);
	}
}
