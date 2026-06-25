package com.uniinformation.jxapp.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;

public class StockCreator extends JxForm {
	RpcClient rpc;
	SelectUtil su;
	Cell target;
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btAdd") {
			public void actionPerformed(JxField fd){
				if(
					jxAdd("ipc_icode").getText().trim().equals("") ||
					jxAdd("ipc_iname").getText().trim().equals("")
					) {
					messageBox("Please fill in Item Code and Name");
				} else {
					Vector args = 
							new VectorUtil()
							.addElement(jxAdd("ipc_icode").getText())
							.addElement(jxAdd("ipc_iname").getText())
							.addElement(jxAdd("ipc_brand").getText())
							.addElement(jxAdd("ipc_partno").getText())
							.toVector();
					if(jxAdd("ipc_category") != null) {
						try {
						TableRec tr = su.getQueryResult("select * from mctype where mt_tpname = ? ", 
									new Wherecl().appendArgument(jxGetText("ipc_category"))
								);
						if(tr.getRecordCount() > 0) {
							tr.setRecPointer(0)	;
							args.add(tr.getFieldInt("mt_tpcode"));
						}
						if(jxAdd("ipc_modelno") != null) {
							args.add(jxGetText("ipc_modelno"));
						}
						}
						catch (Exception ex) {
							UniLog.log(ex);
						}
					}
					if(jxAdd("ipc_modelno") != null) {
						
					}
					Value v = rpc.callSegment(
							"erpV4CreateStock",args
							);
					rpc.close();
					if(v != null && v.toString().startsWith("OK")) {
						try {
							target.update(jxAdd("ipc_icode").getText());
						} catch (CellException cex) {
							UniLog.log(cex);
						}
						closeForm();
					} else {
						String err;
						if(v != null) err = v.toString(); else err = "Failed Unknown Error";
						messageBox(err,1,
							new MessageBoxActionInterface()  {
								public void onButtonClicked(Object p_data) {
									closeForm();
								}
							}	
						);
					}
				}
			}
		};	
		new JxFieldAction("btCancel") {
			public void actionPerformed(JxField fd){
				rpc.close();
				closeForm();
			}
		};	
		jxAdd("ipc_icode").setEnable(false);
		
		new JxFieldChange("ipc_brand ipc_partno") {
			public boolean valueChanged(JxField fd,String p_text){
				if(
					jxAdd("ipc_brand").getText().trim().equals("") ||
					jxAdd("ipc_partno").getText().trim().equals("")
					) {
					jxAdd("ipc_icode").setText("");
				} else {
					jxAdd("ipc_icode").setText("PH10-"+ 
							jxAdd("ipc_brand").getText().trim() + "-" +
							jxAdd("ipc_partno").getText().trim());
				}
				return(true);
			}
		};
	}
	
	public void modalExecute(SelectUtil p_su,RpcClient p_rpc,Cell p_target) {
		
		su = p_su;
		rpc = p_rpc;
		target = p_target;
		try {
			TableRec tr = su.getQueryResult("select stbd_code from st_brand order by 1",null);
			Vector v = new Vector();
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				v.add(tr.getField("stbd_code"));
			}
			jxAdd("ipc_brand").setItemList(v);
			if(jxAdd("ipc_category") != null) {
			tr = su.getQueryResult("select * from mctype order by mt_tpname",null);
			v = new Vector();
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				v.add(tr.getField("mt_tpname"));
			}
			jxAdd("ipc_category").setItemList(v);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		jxAdd("ipc_icode").setText("");
		jxAdd("ipc_iname").setText("");
		jxAdd("ipc_brand").setText("");
		jxAdd("ipc_partno").setText("");
		modalForm();
	}
}
