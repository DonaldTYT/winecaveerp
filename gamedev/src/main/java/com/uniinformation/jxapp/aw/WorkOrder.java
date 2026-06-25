package com.uniinformation.jxapp.aw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.aw.BiResultWoFin;
import com.uniinformation.bicore.aw.PrintOwCallback;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class WorkOrder extends JxZkBiBase {
	JxSelOpt jxfSelPresetStr = null;
	JxSelOpt jxfSelQuotation= null;
	JxSelOpt jxfSelCustomer= null;
	TrGetItemProperty tgipiSelQuotation;
	TrGetItemProperty tgipiSelPresetStr;
	TrGetItemProperty tgipiSelCustomer;
	String nextNewJobNo = null;
	JxForm formCreateQuo = null;
	void createSelectopt() {
		if(jxfSelPresetStr == null) {
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
//		jxfSelPresetStr = JxSelOpt.createJxSelOpt(pvdr,"SelOptPresetStr");
		jxfSelPresetStr = JxSelOpt.createJxSelOpt(pvdr);
//		jxfSelPresetStr.setWidth("300px");
		jxfSelPresetStr.setOnSelectAction (
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object[] rec = (Object[]) jxfSelPresetStr.getPickListBoxValue();
					if (rec != null) {
						try {
							ColumnCell ccol = (ColumnCell) jxfSelPresetStr.getUserData();
							TableRec tr = tgipiSelPresetStr.getTableRec();
							ccol.update(
									rec[tr.getFieldIndex("pstd_str")].toString().trim()
									);
							
//							if(jxfServiceLoc.getUserData().equals("svloc_desp")) {
//								TableRec tr = tgipiServiceLoc.getTableRec();
//								getBr().getCell("svjob_mcrg").update(0);
//								getBr().getCell("svloc_desp").update(rec[tr.getFieldIndex("svloc_desp")]);
//							}
//							if(jxfServiceLoc.getUserData().equals("st_iname")) {
//								TableRec tr = tgipiServiceItem.getTableRec();
//								getBr().getCell("svjob_mcrg").sync(rec[tr.getFieldIndex("svmc_rg")]);
//							}
						} catch (CellException cex ) {  
							UniLog.log(cex);
						} 
						jxfSelPresetStr.closeForm();
					}
				}
			}
		);
		}
		if(tgipiSelPresetStr == null) {
			tgipiSelPresetStr = new TrGetItemProperty(
				new VectorUtil()
					.addElement("pstd_str")
					.toVector(),
				new VectorUtil()
					.addElement("Preset Value")
					.toVector(),
				new VectorUtil()
					.addElement("100%")
					.toVector()
			);
		}
	}
	
	
	boolean pickPresetString(String p_key,ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
		createSelectopt();
		p_zkpi.setJxZkForm(jxfSelPresetStr);
		try {
			TableRec tr = getBr().getSelectUtil().getQueryResult("select * from presetmaster,presetdetail where "
					+ "pstm_key = '" + p_key + "' and pstd_mrg = pstm_rg order by pstd_seq",null);
			if(tr.getRecordCount() > 0) {
				tgipiSelPresetStr.setTableRec(tr);
				jxfSelPresetStr.setUserData(p_bcc);
				jxfSelPresetStr.jxAdd("pickListBox").setItemListInterface(tgipiSelPresetStr);	
				jxfSelPresetStr.beginPick();
			} else p_zkpi.close();
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
	void setQuoAndJobNumber(String p_invjobno,String p_invjobtitle,int p_invrg,SelectUtil su) throws Exception {
		if(nextNewJobNo == null || nextNewJobNo.trim().equals("")) {
			nextNewJobNo = p_invjobno;
		} else {
			getBr().getCell("jm_jobno").set("");
		}
		getBr().getCell("jm_title").set(StringUtil.strpart(p_invjobtitle,0,120));
		if(nextNewJobNo == null || nextNewJobNo.trim().equals("")) {
			Value v = su.getRpcClient().callSegment(
				"createJobNumber",
				new VectorUtil()
					.addElement(DateUtil.today())
					.toVector()
				);
			nextNewJobNo = StringUtil.strpart(v.toString(),4,-1);
		}
		su.executeUpdate("update quotation set inv_jobno = ? where inv_rg = ?", 
			new Wherecl()
			.appendArgument(nextNewJobNo)
			.appendArgument(p_invrg)
			);
		getBr().getCell("jm_jobno").set(nextNewJobNo);
		TableRec tr=su.getQueryResult("select max(jm_rev) maxrev from jobmaster_real where jm_jobno = '"+nextNewJobNo+"'",null);
		getBr().getCell("jm_rev").set(tr.getFieldInt("maxrev")+1);
		getBr().getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
		Component comp = (Component) jxAdd("jm_finsize").getNativeObject();
		((HtmlBasedComponent) comp).focus();	
		getBr().getCell("jm_jobno").set(nextNewJobNo);
	}
	void createSelectoptQuotation() {
		if(jxfSelQuotation == null) {
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
//		jxfSelQuotation = JxSelOpt.createJxSelOpt(pvdr,"SelOptQuotation");
		jxfSelQuotation = JxSelOpt.createJxSelOpt(pvdr);
//		jxfSelQuotation.setWidth("1000px");
		jxfSelQuotation.setOnSelectAction (
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object[] rec = (Object[]) jxfSelQuotation.getPickListBoxValue();
					if (rec != null) {
						try {
							ColumnCell ccol = (ColumnCell) jxfSelQuotation.getUserData();
							SelectUtil su = getBr().getSelectUtil();
							TableRec tr = tgipiSelQuotation.getTableRec();
							if(nextNewJobNo == null || nextNewJobNo.trim().equals("")) {
								nextNewJobNo = rec[tr.getFieldIndex("inv_jobno")].toString().trim();
							} else {
								getBr().getCell("jm_jobno").set("");
							}
							getBr().getCell("jm_title").set(StringUtil.strpart(rec[tr.getFieldIndex("inv_projecttitle")].toString().trim(),0,120));
							if(nextNewJobNo == null || nextNewJobNo.trim().equals("")) {
								Value v = su.getRpcClient().callSegment(
									"createJobNumber",
										new VectorUtil()
											.addElement(DateUtil.today())
											.toVector()
									);
								nextNewJobNo = StringUtil.strpart(v.toString(),4,-1);
							}
							su.executeUpdate("update quotation set inv_jobno = ? where inv_rg = ?", 
								new Wherecl()
										.appendArgument(nextNewJobNo)
										.appendArgument((Integer) rec[tr.getFieldIndex("inv_rg")])
								);
							getBr().getCell("jm_jobno").set(nextNewJobNo);
							tr=su.getQueryResult("select max(jm_rev) maxrev from jobmaster_real where jm_jobno = '"+nextNewJobNo+"'",null);
							getBr().getCell("jm_rev").set(tr.getFieldInt("maxrev")+1);
							ccol.setMode(Cell.VMODE_DISPONLY);
							Component comp = (Component) jxAdd("jm_finsize").getNativeObject();
							((HtmlBasedComponent) comp).focus();	
							getBr().getCell("jm_jobno").set(nextNewJobNo);
							
						} catch (Exception cex ) {  
							UniLog.log(cex);
						} 
						jxfSelQuotation.closeForm();
					}
				}
			}
		);
		}
		if(tgipiSelQuotation == null) {
			tgipiSelQuotation = new TrGetItemProperty(
				new VectorUtil()
					.addElement("inv_invno")
					.addElement("inv_jobno")
					.addElement("inv_projecttitle")
					.addElement("vd_vname")
					.toVector(),
				new VectorUtil()
					.addElement("Quotation")
					.addElement("Job No.")
					.addElement("Title")
					.addElement("Customer")
					.toVector(),
				new VectorUtil()
					.addElement("150px")
					.addElement("150px")
					.addElement("100%")
					.addElement("400px")
					.toVector()
			);
		}
	}
	boolean pickQuotation(ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
		createSelectoptQuotation();
		p_zkpi.setJxZkForm(jxfSelQuotation);
		try {
			TableRec tr;
			if(nextNewJobNo == null) {
				tr = getBr().getSelectUtil().getQueryResult("select * from quotation,vendor where inv_quostatus in('','New','Revised','Quoted') and inv_invno <> '' and vd_vcode = inv_vcode"
					,null);
			} else {
				tr = getBr().getSelectUtil().getQueryResult("select * from quotation,vendor where inv_quostatus in('','New','Revised','Quoted') and inv_invno <> '' and inv_jobno = '' and vd_vcode = inv_vcode"
					,null);
			}
			if(tr.getRecordCount() > 0) {
				tgipiSelQuotation.setTableRec(tr);
				jxfSelQuotation.setUserData(p_bcc);
				jxfSelQuotation.jxAdd("pickListBox").setItemListInterface(tgipiSelQuotation);	
				jxfSelQuotation.beginPick();
			} else p_zkpi.close();
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
	boolean pickPresetStringByBiColumn(ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
		if(p_bcc.getBiColumn().getLabel().equals("wm_matname")) return(pickPresetString("MU",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wm_matwt")) return(pickPresetString("MW",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wm_mattype")) return(pickPresetString("MT",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wm_matsize")) return(pickPresetString("MS",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wm_brand")) return(pickPresetString("MB",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wm_vendor")) return(pickPresetString("MV",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wm_fsc")) return(pickPresetString("FSCT",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wt_name")) return(pickPresetString("MU",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wt_matsize")) return(pickPresetString("MS",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wt_matcut")) return(pickPresetString("MK",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wt_cutsize")) return(pickPresetString("MC",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wp_name")) return(pickPresetString("MU",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wp_imposition")) return(pickPresetString("BT",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wp_fold")) return(pickPresetString("FOLD",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wp_machine")) return(pickPresetString("PTR",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wf_vendor")) return(pickPresetString("FV",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wf_name")) return(pickPresetString("FT",p_bcc,p_zkpi));
		if(p_bcc.getBiColumn().getLabel().equals("wr_name")) return(pickPresetString("MU",p_bcc,p_zkpi));
		return(false);
	}
	class WoMatGetItemProperty extends BiGetItemProperty {
		WoMatGetItemProperty(BiResult p_br) {
			super(p_br);
		}
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			BiColumn o = (BiColumn) getListColumns(p_v).get(p_col);
			if(o.getView().getName().equals("aw.WoMat")) {
				switch(p_col) {
				case 1: return("100px");
				default : return(super.getColumnWidth(p_v, p_col));
				}
			}
			return(super.getColumnWidth(p_v, p_col));
		}		
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) {;
				if(!bcc.getCellLabel().equals("wf_prtow")) setDirtyFlag(true);
			} else {
				if(bcc.getCellLabel().equals("inv_invno")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("300px");
					}
				}
			}
			if(p_ctype == GIPI_PULLDOWN_CLOSED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
			}
			if(p_ctype == GIPI_PULLDOWN_OPENED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
				ZkJxPickInput zkpi = (ZkJxPickInput) zcvm.getComponent();
				if(zkpi.isOpen()) {
//					zkpi.setPopupWidth("300px");
//					zkpi.setPopupHeight("500px");
					pickPresetStringByBiColumn(bcc,zkpi);
				}
			} 
		}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		SessionHelper sh = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		if(sh.isMobileDevice()){
//			zkpi.setPopupWidth("100%");
		} else {
			ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("inv_invno").getNativeObject();
			zkpi.setPopupWidth("1000px");
		}
		new JxFieldAction("btCopy") {
			public void actionPerformed(JxField fd){
				UniLog.log("copy workorder");
        			Messagebox.show(
        					"Copy Work Order",
        					"Confirm Copy Order ?", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
        			    public void onEvent(Event evt) throws InterruptedException {
        			        if (evt.getName().equals("onOK")) {
        			        	RpcClient rpc = getRpcClient();
        			        	Vector args = new Vector();
        			        	args.add(getBr().getCell("jm_rg").getInt());
        			        	args.add(getBr().getCell("jm_jobno").getString());
        			        	args.add(getLoginId());
        			        	args.add(DateUtil.today());
        		           		Value v = rpc.callSegment(
        								"erpv4CloneWorkOrder",
        								args
        							);
        			        	rpc.close();
        	    				if(v != null && v.toString().startsWith("OK")) {
        	       					// redir to DO Update Page
        	    					try {
        	    					int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
        	    					JSONObject jo = new JSONObject();
        	    					JSONArray ja = new JSONArray();
        	    					BiView pov = getBr().getView().getSchema().getViewByName("aw.WorkOrder");
        	    					ja.put(pov.getTable().getName());
        	    					jo.put("tablist", ja);
        	    					jo.put("wherestr", "jm_rg = " + mrg);
        	    					String key = sessionHelper.putOneTimeData( jo);
        	    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=update&viewid=aw.WorkOrder&page_id=WorkOrder_01&zul=zkbiloader.zul&composer=aw.ZkBiComposerWorkOrder&querycondition="+key);
        	    					} catch (Exception ex) {
        	    						UniLog.log(ex);
        	    					}
//        	    					doPopupStmd(doViewName,mrg);
        	    				} else {
        	    					if(v == null) {
        	    						Messagebox.show(
        	    								"Copy Work Order Failed : Unknown Reason",
            								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            						
        	    					} else {
        	    						Messagebox.show(
            								"Copy Work Order Failed : " + v.toString().substring(4),
            								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        	    					}
        	    				}
        			        } else {
        			        	UniLog.log("Copy Order Canceled");
        			        }
        			    }
       					}
       			    );
			}
		};
		new JxFieldAction("btPrint") {
			public void actionPerformed(JxField fd){
				UniLog.log("print workorder");
				RpcClient rpc = getRpcClient();
				ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
				rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
				val = rpc.callSegment("erpv4_print_wo",
							new VectorUtil()
							.addElement(getBr().getCell("jm_rg").getInt())
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
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
//						ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
		};	
		new JxFieldAction("jm_packing") {
			public void actionPerformed(JxField fd){
				UniLog.log("jm_packing clicked");
				ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("jm_packing").getNativeObject();
				pickPresetString("PACK",(ColumnCell) getBr().getCell("jm_packing"),zkpi);
			}
		};			
		new JxFieldAction("jm_delivery") {
			public void actionPerformed(JxField fd){
				UniLog.log("jm_packing clicked");
				ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("jm_delivery").getNativeObject();
				pickPresetString("DELI",(ColumnCell) getBr().getCell("jm_delivery"),zkpi);
			}
		};			
		new JxFieldChange("inv_invno") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				// TODO Auto-generated method stub
				UniLog.log("inv_invno changed ["+jxfield.getText()+"]["+orgvalue+"]["+getBr().getCell("inv_invno").toString());
				String ss = jxfield.getText();
				if(!getBr().getCell("jm_jobno").getString().trim().equals("")) {
					return false;
				}
				if(ss.trim().equals("")) return(true);
				try {
					SelectUtil su = getBr().getSelectUtil();
					TableRec tr;
					if(nextNewJobNo == null) {
						tr = getBr().getSelectUtil().getQueryResult("select * from quotation,vendor where inv_quostatus in('','Revised','Quoted') and inv_invno = '"+ ss + "' and vd_vcode = inv_vcode"
								,null);
					} else {
						tr = getBr().getSelectUtil().getQueryResult("select * from quotation,vendor where inv_quostatus in('','Revised','Quoted') and inv_invno = '"+ ss + "' and inv_jobno = '' and vd_vcode = inv_vcode"
								,null);
					}
					if(tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
						setQuoAndJobNumber(
								tr.getFieldString("inv_jobno"),
								tr.getFieldString("inv_projecttitle"),
								tr.getFieldInt("inv_rg"),
								su);
						return(true);
					} else {
						return(false);
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					return false;
				}
			}
		};			
		new JxFieldAction("inv_invno") {
			public void actionPerformed(JxField fd){
				UniLog.log("inv_invno clicked");
				ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("inv_invno").getNativeObject();
//				zkpi.setPopupWidth("1000px");
//				zkpi.setPopupHeight("500px");
				pickQuotation((ColumnCell) getBr().getCell("inv_invno"),zkpi);
			}
		};			
//		new JxFieldChange("inv_invno") {
//
//			@Override
//			public boolean valueChanged(JxField jxfield, String orgvalue) {
//				/*
//				setDirtyFlag(true);
//				// TODO Auto-generated method stub
//				String s = jxfield.getText();
//				UniLog.log("inv_invno changed");
//				if(!s.equals("")) {
//					try {
//					SelectUtil su = getBr().getSelectUtil();
//					TableRec tr;
//						if(nextNewJobNo == null) {
//							tr = getBr().getSelectUtil().getQueryResult("select * from quotation,vendor where inv_quostatus in('','Revised','Quoted') and inv_invno = '" + s + "' and vd_vcode = inv_vcode"
//								,null);
//						} else {
//							tr = getBr().getSelectUtil().getQueryResult("select * from quotation,vendor where inv_quostatus in('','Revised','Quoted') and inv_invno = '" + s + "' and inv_jobno = '' and vd_vcode = inv_vcode"
//								,null);
//						}
//						if(tr.getRecordCount() > 0) {
//							if(nextNewJobNo == null || nextNewJobNo.trim().equals("")) {
//								nextNewJobNo = tr.getFieldString("inv_jobno");
//							}
//							if(nextNewJobNo == null || nextNewJobNo.trim().equals("")) {
//								Value v = su.getRpcClient().callSegment(
//									"createJobNumber",
//										new VectorUtil()
//											.addElement(DateUtil.today())
//											.toVector()
//									);
//								nextNewJobNo = StringUtil.strpart(v.toString(),4,-1);
//							}
//							su.executeUpdate("update quotation set inv_jobno = ? where inv_rg = ?", 
//								new Wherecl()
//										.appendArgument(nextNewJobNo)
//										.appendArgument(tr.getFieldInt("inv_rg"))
//								);
//							return(true);
//						} 
//						return(false);
//					} catch (Exception ex){
//						UniLog.log(ex);
//						return(false);
//					}
//				}
//				return true;
//				*/
//				return false;
//			}
//		};
		new JxFieldAction("btNewQuo") {
			public void actionPerformed(JxField fd){
				UniLog.log("new quo clicked");
				if(formCreateQuo == null) {
					Window cwin = new Window();
					cwin.setParent(((Component) getNativeComponent()).getParent());
					formCreateQuo = JxZkBiBase.getOrCreateJxZkForm(cwin,
							(JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider")
							,"aw.CreateNewQuotation");	
					formCreateQuo.addFormCloseListener(
							new JxFormCloseListener() {
								public int formClose(JxForm p_form) {
									return(JxFormCloseListener.caHide);
								}
							}
					); 
					{
						ZkJxPickInput zkpi = (ZkJxPickInput) formCreateQuo.jxAdd("cQuoCuCode").getNativeObject();
						zkpi.setPopupWidth("620px");
					}
					JxField ffd = formCreateQuo.jxAdd("btOK");
					if(ffd != null) {
						ffd.addActionListener(
								new JxActionListener() {
									public void actionPerformed(JxField p_fd) {
										UniLog.log("formCreateQuo OK Pressed");
										formCreateQuo.closeForm();
										JxField f0,f1,f2,f3;
										f0 = formCreateQuo.jxAdd("cQuoCoCode");
										f1 = formCreateQuo.jxAdd("cQuoCuCode");
										f2 = formCreateQuo.jxAdd("cQuoTitle");
										f3 = formCreateQuo.jxAdd("cQuoCustPo");
										setDirtyFlag(true);
										RpcClient rpc = getBr().getSelectUtil().getRpcClient();
										Value v = rpc.callSegment("erpv4CreateQuotation", 
												new VectorUtil()
													.addElement(f0.getText())
													.addElement(f1.getText())
													.addElement(f2.getText())
													.addElement(f3.getText())
													.toVector()
												);
//										rpc.close();
										if(v != null && v.toString().startsWith("OK")) {
											try {
												setQuoAndJobNumber(
													"",
													f2.getText(),
													Integer.parseInt(StringUtil.strpart(v.toString(),4,10).trim()),
													getBr().getSelectUtil());
											} catch (Exception ex) {
												Messagebox.show(
														"Create Failed : Exception"+ex,
														sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
												UniLog.log(ex);
											}
										} else {
    					if(v == null) {
    						Messagebox.show(
    						"Create Failed : Unknown Reason",
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
    						
    					} else {
    						Messagebox.show(
    								"Create Failed : " + v.toString().substring(4),
    								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
    					}
											
										}
									}
								}
						);
					}
					ffd = formCreateQuo.jxAdd("btCancel");
					if(ffd != null) {
						ffd.addActionListener(
								new JxActionListener() {
									public void actionPerformed(JxField p_fd) {
										UniLog.log("formCreateQuo Cancel Pressed");
										formCreateQuo.closeForm();
									}
								}
						);
					}
					ffd = formCreateQuo.jxAdd("cQuoCoCode");
					if(ffd != null) {
						ffd.setItemList(
								new VectorUtil()
									.addElement("Power")
									.addElement("Artway")
									.toVector()
								);
					}
					ffd = formCreateQuo.jxAdd("cQuoCuCode");
					if(ffd != null) {
						ffd.addActionListener(
								new JxActionListener() {
									public void actionPerformed(JxField p_fd) {
										UniLog.log("formCreateQuo CuCode Pressed");
										createSelectoptCustomer();
										ZkJxPickInput zkpi = (ZkJxPickInput) p_fd.getNativeObject();
//										zkpi.setPopupWidth("620px");
//										zkpi.setPopupHeight("480px");
										zkpi.setJxZkForm(jxfSelCustomer);
										try {
											TableRec tr;
											tr = getBr().getSelectUtil().getQueryResult("select * from vendor where vd_type = 'C' order by vd_vname" ,null);
											if(tr.getRecordCount() > 0) {
												tgipiSelCustomer.setTableRec(tr);
												jxfSelCustomer.jxAdd("pickListBox").setItemListInterface(tgipiSelCustomer);	
												jxfSelCustomer.beginPick();
											} else zkpi.close();
										} catch (Exception ex) {
											UniLog.log (ex);
										}
									}
								}
						);
					}
				}
				formCreateQuo.modalForm();
			}
		};			
		LOCK_RECORD_FOR_UPDATE = true;
		{
			JxField jxf = jxAdd("jm_otherrem");
			if(jxf != null) {
				jxf.setInstant(false);
			}
		}
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		if(getGipi("aw.WoMat") == null) {
			setGipi("aw.WoMat",new WoMatGetItemProperty(p_br.getSubLink("aw.WoMat")));	
		}
		if(getGipi("aw.WoExt") == null) {
			setGipi("aw.WoExt",new WoMatGetItemProperty(p_br.getSubLink("aw.WoExt")));	
		}
		if(getGipi("aw.WoPrt") == null) {
			setGipi("aw.WoPrt",new WoMatGetItemProperty(p_br.getSubLink("aw.WoPrt")));	
		}
		if(getGipi("aw.WoFin") == null) {
			setGipi("aw.WoFin",new WoMatGetItemProperty(p_br.getSubLink("aw.WoFin")));	
		}
		if(getGipi("aw.WoProd") == null) {
			setGipi("aw.WoProd",new WoMatGetItemProperty(p_br.getSubLink("aw.WoProd")));	
		}
//		ZkBiGetItemProperty.useGetItemPropertyForSubLink(p_br,this);
		super.bindCellCollection(p_br,mode);
		{
			BiResultWoFin bwof = (BiResultWoFin) p_br.getSubLink("aw.WoFin");
			bwof.setPrtWoCallback(
						new PrintOwCallback() {

							@Override
							public void invoke(ColumnCell col) {
								// TODO Auto-generated method stub
								UniLog.log("HAHA prtwo in jxapp");
								if(getBr().inBeginWork()) {
        	    						Messagebox.show(
        	    								"Workorder Modified, please save before print",
            								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        	    						return;
								}

								BiCellCollection owcol = col.getCollection();
								String owno = owcol.getCellString("wf_owno");
								int sid = owcol.getCellInt("serial_id");
								String owvname = owcol.getCellString("wf_vendor");
								if(owno.equals("")) {
									RpcClient rpc = getSessionHelper().getRpcClient();
									/*
									int rgno = getBr().getView().getSchema().getRg("", 16006);
									owno = String.format("OW%05d", rgno);
									*/
									rpc.callSegment("setCocodeBaseccy",
											new VectorUtil()
											.addElement("AAW1")
											.addElement("HKD")
											.toVector()
									);
									Value val = rpc.callSegment("getrg_byrgcontrol_bycategory",
											new VectorUtil()
											.addElement("oworder")
											.addElement(DateUtil.today())
											.toVector()
									);
									rpc.close();
									if(val == null || !(val instanceof Strval)) {
        	    						Messagebox.show(
        	    								"get oworder number failed ",
            								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        	    						return;
									}
									owno = val.toString();
									/*
									int mrg = owcol.getCellInt("wf_mrg");
									int seq = owcol.getCellInt("wf_seq");
									*/
									try {
										getBr().getSelectUtil().executeUpdate("update wofin set wf_owno = '" + owno + "' where serial_id = " + sid,null);
										getBr().refetchCurrent();
										bindCellCollection(getBr(),curMode);
									} catch (Exception ex) {
										UniLog.log(ex);
									}
								} else {
									printOwOrder(sid,owno,owvname);
								}
							}
							
						}
					);
					
		}
//		if(mode == JxZkBiBase.MODE_ADD) {
		{
			try {
				if(!p_br.getCell("jm_cdate").getDate().after(DateUtil.minDate)) {
					p_br.getCell("jm_cdate").set(DateUtil.today());
				}
				if(p_br.getCell("jm_cuser").getString().trim().equals("")) {
					p_br.getCell("jm_cuser").set(getLoginId());
				}
				if(p_br.getCell("inv_invno").getString().trim().equals("")) {
					if(p_br.getCell("jm_jobno").getString().trim().equals("")) {
						nextNewJobNo = null;
					} else {
						nextNewJobNo = p_br.getCell("jm_jobno").getString();
					}
				} else {
					p_br.getCell("jm_jobno").setMode(Cell.VMODE_DISPONLY);
					p_br.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
					nextNewJobNo = null;
				}
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		} 
//		if(mode == JxZkBiBase.MODE_UPDATE) { 
//		}
//		jxAdd("pdfview").setVisible(false);
		setupActionButton(false);
	}	
	
	void setupActionButton(boolean is_dirty)
	{
//		if(curMode == JxZkBiBase.MODE_ADD) {
//			jxAdd("btUpload").setEnable(false);
//			jxAdd("btUpload").setVisible(false);
//			jxAdd("btView").setEnable(false);
//			jxAdd("btView").setVisible(false);
//		} 
//		if(curMode == JxZkBiBase.MODE_UPDATE) { 
//			jxAdd("btUpload").setVisible(true);
//			jxAdd("btView").setVisible(true);
//			if(is_dirty) {
//			jxAdd("btUpload").setEnable(false);
//			jxAdd("btView").setEnable(false);
//			} else {
//			jxAdd("btUpload").setEnable(true);
//			jxAdd("btView").setEnable(true);
//			}
//		}
		if(curMode == JxZkBiBase.MODE_ADD) {
//			jxAdd("btPrint").setEnable(false);
			jxSetEnable("btPrint",false);
//			jxAdd("btCopy").setEnable(false);
			jxSetEnable("btCopy",false);
		} 
		if(curMode == JxZkBiBase.MODE_UPDATE) { 
			if(is_dirty) {
//				jxAdd("btPrint").setEnable(false);
				jxSetEnable("btPrint",false);
//				jxAdd("btCopy").setEnable(false);
				jxSetEnable("btCopy",false);
			} else {
//				jxAdd("btPrint").setEnable(true);
				jxSetEnable("btPrint",true);
				String invno = getBr().getCell("inv_invno").getString().trim();
				if(!invno.equals("")) {
//					jxAdd("btCopy").setEnable(true);
					jxSetEnable("btCopy",true);
				} else {
//					jxAdd("btCopy").setEnable(false);
					jxSetEnable("btCopy",false);
				}
			}
		}
		if(getBr().getCell("inv_invno").getString().equals("")) {
			jxSetVisible("btNewQuo",true);
		} else {
			jxSetVisible("btNewQuo",false);
		}
	}	
	
	@Override
	protected ReturnMsg doAfterLockRecord(ReturnMsg p_rtnMsg) {
		try {
			String invno = getBr().getCell("inv_invno").getString().trim();
			if(!invno.equals("")) {
				SelectUtil su = getBr().getSelectUtil();
				su.executeUpdate("update quotation set inv_jobno = inv_jobno where inv_invno = ?", 
						new Wherecl()
								.appendArgument(invno)
							);
			}
			return(p_rtnMsg);
		} catch (Exception ex){
			UniLog.log(ex);
			return(new ReturnMsg(false,"Lock Quotation Failed"));
			
		}
	}
	@Override 
	protected void formDirtyChanged() {
		super.formDirtyChanged();
		setupActionButton(isDirty());
	}
	
	void syncSublinkInsert(BiResult sr,int p_idx) {
		int idx = sr.getRowCount();
		if(p_idx < idx) idx = p_idx;
		JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
		CellCollection col = sr.newRowCollection();
		ReturnMsg rtn = sr.addSubRecord(col, idx,"");
		Object tr = rtn.getData();
		int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
		sv.addItemToList(tr, rowIdx);
	}
	void syncSublinkDelete(BiResult sr,int p_idx,boolean p_delete) {
		Object o = null;
		if(sr.getRowCount() <= p_idx) return;
		JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
		o = sr.getTrStatObj(new Integer(p_idx));
		if(o != null) {
			sr.markDelete(o, p_delete);
			if(p_delete ) 
				sv.gridSetDataFormat(-1,p_idx,"add_deleted");
			else
				sv.gridSetDataFormat(-1,p_idx,"remove_deleted");
		}
	}
	@Override 
	protected void afterAddLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("aw.WoMat")) {
		syncSublinkInsert(getBr().getSubLink("aw.WoExt"),idx);
		syncSublinkInsert(getBr().getSubLink("aw.WoPrt"),idx);
		syncSublinkInsert(getBr().getSubLink("aw.WoProd"),idx);
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("aw.WoMat")) {
		syncSublinkDelete(getBr().getSubLink("aw.WoExt"),idx,true);
		syncSublinkDelete(getBr().getSubLink("aw.WoPrt"),idx,true);
		syncSublinkDelete(getBr().getSubLink("aw.WoProd"),idx,true);
		}
	}
	@Override 
	protected void afterUnDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("aw.WoMat")) {
		syncSublinkDelete(getBr().getSubLink("aw.WoExt"),idx,false);
		syncSublinkDelete(getBr().getSubLink("aw.WoPrt"),idx,false);
		syncSublinkDelete(getBr().getSubLink("aw.WoProd"),idx,false);
		}
	}
	void createSelectoptCustomer() {
		if(jxfSelCustomer == null) {
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
		jxfSelCustomer = JxSelOpt.createJxSelOpt(pvdr);
//		jxfSelCustomer.setWidth("600px");
		jxfSelCustomer.setOnSelectAction (
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object[] rec = (Object[]) jxfSelCustomer.getPickListBoxValue();
					if (rec != null) {
						try {
							ColumnCell ccol = (ColumnCell) jxfSelCustomer.getUserData();
							SelectUtil su = getBr().getSelectUtil();
							TableRec tr = tgipiSelCustomer.getTableRec();
							formCreateQuo.jxSetText("cQuoCuCode", 
									StringUtil.strpart(rec[tr.getFieldIndex("vd_vcode")].toString().trim(),0,120));
						} catch (Exception cex ) {  
							UniLog.log(cex);
						} 
						jxfSelCustomer.closeForm();
					}
				}
			}
		);
		}
		if(tgipiSelCustomer == null) {
			tgipiSelCustomer = new TrGetItemProperty(
				new VectorUtil()
					.addElement("vd_vcode")
					.addElement("vd_vname")
					.addElement("vd_chnname")
					.toVector(),
				new VectorUtil()
					.addElement("Customer Code")
					.addElement("Customer Name")
					.addElement("Chinese Name")
					.toVector(),
				new VectorUtil()
					.addElement("120px")
					.addElement("300px")
					.addElement("100%")
					.toVector()
			);
		}
	}

	void printOwOrder(int p_sid,String p_owno,String p_owvname) {
				RpcClient rpc = getRpcClient();
				ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
				rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
				val = rpc.callSegment("erpv4_print_oworder",
							new VectorUtil()
							.addElement(getBr().getCell("jm_rg").getInt())
							.addElement(p_sid)
							.addElement(p_owno)
							.addElement(p_owvname)
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
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
//						ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
	}
}
