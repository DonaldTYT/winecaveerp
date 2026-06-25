package com.uniinformation.jxapp.afs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.zkoss.zk.ui.Executions;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.AfsDO;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class AfsDoMc extends AfsDO {
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btPrint") {
			public void actionPerformed(JxField fd){
				UniLog.log("print Pressed afsdomc");
				JxZkBiBase jxf = (JxZkBiBase) fd.getJxForm();
				BiResult br = jxf.getBr();
				SessionHelper sh = br.getSessionHelper();
				RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement("c:\\images\\").toVector());
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(sh.getWebContentRealPath("images", true)).toVector());
				
				val = rpc.callSegment("erpv4_print_domc",
							new VectorUtil()
							.addElement(getBr().getCell("stm_mrg").getInt())
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
					UniLog.log("Print Do got " + fname);
					try {
//					ZkUtil.print((Component) (jxAdd("detail_grid").getNativeObject()));	
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"'");
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						bos.close();
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					if(val != null) 
						messageBox("Print D/N Error : " + val.toString());
					else
						messageBox("Print D/N Error : Unknown");
				}
			}
		};
	}
}
