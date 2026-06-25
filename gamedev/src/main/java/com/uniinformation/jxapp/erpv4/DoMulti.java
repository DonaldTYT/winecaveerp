package com.uniinformation.jxapp.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultDO;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.wc.PrintBarcode;

public class DoMulti extends DO {
	@Override
	public void afterBind() {
		super.afterBind();
		detViewName = "erpv4.DoDetMulti";
		prtDoSegName= "erpv4_print_domulti";
		new JxFieldAction("btPrintAllInv") {
			public void actionPerformed(JxField fd){
				try {
					BiResult sobr = getBr().getView().getSchema().getViewByName("erpv4.QuotationG2").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
					String cond = null;
					for(BiCellCollection bc : getBr().getSubLink("erpv4.StmPostInv").getRowCollectionList()) {
						if(cond == null) 
								cond =  "inv_invno in ('"+bc.getCellString("inv_invno")+"'";
						else
								cond +=  ",'"+bc.getCellString("inv_invno")+"'";
						
					}
					cond += ")";
					sobr.addCustomCondition(cond);
					sobr.query();
					
					PrtdocClass jpi = null;
					String prtdocClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClassG2");
					Class[]	paramTypes = new Class[]{BiResultQuotation.class};
					jpi = (PrtdocClass) DynamicClassLoader.newInstance(prtdocClass, paramTypes,sobr);
					for(int i=0;i<sobr.getRowCount();i++) {
						sobr.fetchOneRecV(i);
						jpi.print();
					}
					
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
					if(rtn != null && !rtn.getStatus()) {
						Messagebox.show(rtn.getMsg());
					} else {
						ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
						ZkUtil.printFromStream(is, "application/pdf", getSessionHelper());
					}
						/*
					for(int i = 0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						UniLog.log("Print one invoice " + tr.getFieldString("inv_invno"));
					}
						*/

				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		new JxFieldAction("btPrintPacklist") {
			public void actionPerformed(JxField fd){
						UniLog.log("print Pressed");
				RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement("c:\\images\\").toVector());
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(getSessionHelper().getWebContentRealPath("images", true)).toVector());
				String cocode = null;
				if(getBr().getCell("stm_cocode") != null)  {
					cocode = getBr().getCellString("stm_cocode");
				} else {
					cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
				}
				val = rpc.callSegment("setCocodeBaseccy",
									new VectorUtil()
									.addElement(cocode)
									.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),cocode))
									.toVector()
									);
				
				val = rpc.callSegment("erpv4_print_pklist",
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
//						ChnftrParser ps = new ChnftrParser(is,"'");
//						ChnftrParser ps = new ChnftrParser(is,"-p14");
						ChnftrParser ps = new ChnftrParser(is,"");
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						/*
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						*/

						/*
						{
							Collection<Component> comps = Executions.getCurrent().getDesktop().getComponents();
							for (Component comp : comps) {
								if (comp instanceof Window) {
									ZkUtil.showPdfDialog(comp, getSessionHelper(), bos.toByteArray(), "PackingList");
									break;
								}
							}
						}
						*/
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "PackingList");
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					if(val != null) 
						messageBox(sessionHelper.getLabel("Print Packing List Error") + ": " + val.toString());
					else
						messageBox(sessionHelper.getLabel("Print Packing List Error") + ": Unknown");
				}
			}
		};
	}
	class DnInvoice {
		String so;
		String svloc_desp;
		String svloc_addr2;
		String svloc_addr3;
		double codAmount;
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		if("Y".equals(Erpv4Config.getString(getSessionHelper(), "DnMultiUseSoInvoice"))) {
		BiResult srd = p_br.getSubLink("erpv4.DoDetMulti");
		BiResult srm = p_br.getSubLink("erpv4.StmPostInv");
		Hashtable<Integer,DnInvoice> dih = new Hashtable<Integer,DnInvoice>();
		if(srm != null && srd != null) {
			JxField sv=jxAdd("list_erpv4_StmPostInv");
			for(BiCellCollection bc: srd.getRowCollectionList()) {
				int invrg = bc.getCellInt("ind_rg");
				DnInvoice dni = dih.get(invrg);
				if(dni == null) {
					dni = new DnInvoice();
					dni.so = bc.getCellString("inv_invno");
					dni.svloc_desp = bc.getCellString("svloc_desp");
					dni.svloc_addr2= bc.getCellString("svloc_addr2");
					dni.svloc_addr3= bc.getCellString("svloc_addr3");
					dih.put(invrg, dni);
				}
				int term = bc.getCellInt("inv_termtype");
				if(term == 0) {
					dni.codAmount += bc.getCellDouble("stmd_exprice");
				}
			}
			try {
			for(int invrg : dih.keySet()) {
				BiCellCollection col = srm.newRowCollection();
				DnInvoice dni = dih.get(invrg);
				ReturnMsg rtn = srm.addSubRecord(col, -1,"");
				col.getCell("inv_rg").set(invrg);
				col.getCell("inv_invno").set(dni.so);
				col.getCell("svloc_desp").set(dni.svloc_desp);
				col.getCell("svloc_addr2").set(dni.svloc_addr2);
				col.getCell("svloc_addr3").set(dni.svloc_addr3);
				col.getCell("stmpi_codamt").set(dni.codAmount);
				Object tr = rtn.getData();
				int rowIdx = getGipi(srm.getView().getName()).getIndexOf(tr);
				sv.addItemToList(tr, rowIdx);
			}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		}
		jxAdd("list_"+JxZkBiBase.replaceViewName("erpv4.DoDetMulti")).setAttribute("paging", "withfilter");
	}
	@Override
	void setupActionButton(boolean is_dirty)
	{
		super.setupActionButton(is_dirty);
		jxSetEnable("btPrint",false);
		jxSetEnable("btPrintPacklist",false);
		if(curMode == JxZkBiBase.MODE_UPDATE) { 
			if(!is_dirty) {
				if(getBr().getCell("stm_status").getString().equals("Confirmed")) {
					jxSetEnable("btPrint",true);
					jxSetEnable("btPrintPacklist",true);
				}
			}
		}
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		//handle Download button
		return ListUtil.of(
			new BiGetItemProperty(p_br.getSubLink("erpv4.StmPostInv")) {
				@Override
				public void onValueChanged(Object p_value,int p_ctype) {
					ColumnCell bcc = (ColumnCell) p_value;
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("stmpi_print")){
						UniLog.log1("%s clicked", bcc.getCellLabel());
						try {
							if("Y".equals(Erpv4Config.getString(getSessionHelper(), "DnMultiUseSoInvoice"))) {
								CellCollection col = bcc.getCollection();
								String invno = col.getCellString("inv_invno");
								try {
									BiResult sobr = p_br.getView().getSchema().getViewByName("erpv4.QuotationG2").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
									sobr.addCustomCondition("inv_invno = '"+invno+"'");
									sobr.query();
									if(sobr.getRowCount() == 1) {
										sobr.fetchOneRecV(0);
										String prtdocClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClassG2");
										ByteArrayOutputStream os = new ByteArrayOutputStream();
										PrtdocClass jpi = null;
										Class[]	paramTypes = new Class[]{BiResultQuotation.class};
										jpi = (PrtdocClass) DynamicClassLoader.newInstance(prtdocClass, paramTypes,sobr);
										jpi.print();
										ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
										if(rtn != null && !rtn.getStatus()) {
											Messagebox.show(rtn.getMsg());
										} else {
											String ss = getBr().getCellString("inv_invno");
											ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
											ZkUtil.printFromStream(is, "application/pdf", getBr().getSessionHelper());
										}
									}
								} catch (Exception ex) {
									UniLog.log(ex); 
									Messagebox.show(ex.toString());
								}							
							} else {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							BiResult sr = getBr().getSubLink("erpv4.StmPostInv");
							CellCollection col = bcc.getCollection();
							int idx  = sr.getIndexByCollection(col);
							sr.fetchOneRecV(idx);
							JSONObject jo = new JSONObject();
							JSONArray ja = new JSONArray();
							ja.put(col.getCellString("stmpi_sno"));
							jo.put("snolist", ja);
							ReturnMsg rtn = ((BiResultDO) getBr()).printInvoice(bos,jo);
							if(rtn.getStatus()) {
								ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Invoice-"+col.getCell("stmpi_sno").getString());
							} else {
								Messagebox.show(rtn.getMsg());
							}
							}
						} catch (Exception ex) {
							UniLog.log(ex);
							messageBox(ex.toString());
						}
					}
				}
			}
		);	
		
	}
	
}
