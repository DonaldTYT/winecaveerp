package com.uniinformation.jxapp.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Messagebox;

import com.google.common.collect.Lists;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.utils.AttachmentUploadInterface;
import com.uniinformation.utils.BiMedia;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class QuotationG2 extends JxZkBiBase{

	@Override 
	protected void afterAddLink(BiResult sr,int idx) {
		BiResultQuotation qbr = (BiResultQuotation) sr.getParent();
		if(sr.getView().getName().equals(qbr.get_subLinkId())) {
			try {
				qbr.real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx) {
		BiResultQuotation qbr = (BiResultQuotation) sr.getParent();
		if(sr.getView().getName().equals(qbr.get_subLinkId())) {
			try {
				qbr.real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			
		}
	}
	@Override 
	protected void afterUnDeleteLink(BiResult sr,int idx) {
		BiResultQuotation qbr = (BiResultQuotation) sr.getParent();
		if(sr.getView().getName().equals(qbr.get_subLinkId())) {
			try {
				qbr.real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			
		}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btAttach") {
			@Override
			public void actionPerformed(JxField jxfield) {
				Component bt = (Component) jxfield.getNativeObject();
				final BiResult attResult = getBr().getSubLink((String) bt.getAttribute("biResult"));
				if(attResult != null) {
					try {
					    Fileupload.get(new HashMap<String, Object>(), null, null, ".pdf|.jpg", 1, 100*1024, false, new EventListener <UploadEvent>(){
				    		public void onEvent(UploadEvent event) {
				        		UniLog.log("upload event catched");
				                org.zkoss.util.media.Media media = event.getMedia();
				                if(media != null) {
				                	UniLog.log1("contenttype:%s, name:%s", media.getContentType(), media.getName());
				                	if (!StringUtils.equalsAny(media.getContentType(), "image/jpeg", "application/pdf")) {
				                		ZkUtil.errMsg(sessionHelper.getLabel("Only pdf/jpg File are accepted"));
				                		return;
				                	}
				                	try  {
				                		((AttachmentUploadInterface) attResult).saveImageFile(new BiMedia(media));
				                		getBr().refetchCurrent();
				                		bindCellCollection(getBr(),curMode);
				                	} catch (Exception ex) {
				                		UniLog.log(ex);
				                		ZkUtil.errMsg(ex.toString());
				                	}
				                }
			                }
					    });
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
			}
			
		};
		if(jxAdd("btPrintQuo") != null) {
			new JxFieldAction("btPrintQuo") {
			public void actionPerformed(JxField fd){
				try {
					String prtdocClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintQuotationClassG2");
					if(prtdocClass == null) {
						prtdocClass = "com.uniinformation.dynamic.chungkee.ChungkeePrintQuotation";
					}
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					PrtdocClass jpi = null;
					Class[]	paramTypes = new Class[]{BiResultQuotation.class};
					jpi = (PrtdocClass) DynamicClassLoader.newInstance(prtdocClass, paramTypes,getBr());
					jpi.print();
					ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
					if(rtn != null && !rtn.getStatus()) {
						Messagebox.show(rtn.getMsg());
					} else {
						String ss = getBr().getCellString("inv_quonum");
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), os.toByteArray(), "Quotation-"+ss);
					}
				} catch (Exception ex) {
					UniLog.log(ex); 
					Messagebox.show(ex.toString());
				}
			}
			};
			abHelper.addButton((Button) jxAdd("btPrintQuo").getNativeObject(),false,true,"fa-print",-1);
			
		}
		if(jxAdd("btPrintInv") != null) {
			new JxFieldAction("btPrintInv") {
			public void actionPerformed(JxField fd){
				try {
					String prtdocClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClassG2");
					if(prtdocClass == null) {
						prtdocClass = "com.uniinformation.dynamic.chungkee.ChungkeePrintInvoice";
					}
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					PrtdocClass jpi = null;
					Class[]	paramTypes = new Class[]{BiResultQuotation.class};
					jpi = (PrtdocClass) DynamicClassLoader.newInstance(prtdocClass, paramTypes,getBr());
					jpi.print();
					ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
					if(rtn != null && !rtn.getStatus()) {
						Messagebox.show(rtn.getMsg());
					} else {
						String ss = getBr().getCellString("inv_invno");
						ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
						ZkUtil.printFromStream(is, "application/pdf", getBr().getSessionHelper());
					}
				} catch (Exception ex) {
					UniLog.log(ex); 
					Messagebox.show(ex.toString());
				}
			}
			};
			abHelper.addButton((Button) jxAdd("btPrintInv").getNativeObject(),false,true,"fa-print",-1);
		}
		if(jxAdd("btPrintCon") != null) {
			new JxFieldAction("btPrintCon") {
			public void actionPerformed(JxField fd){
				try {
					String prtdocClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintContractClassG2");
					if(prtdocClass == null) {
						messageBox("Contract Document Not Available");
						return;
					}
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					PrtdocClass jpi = null;
					Class[]	paramTypes = new Class[]{BiResultQuotation.class};
					jpi = (PrtdocClass) DynamicClassLoader.newInstance(prtdocClass, paramTypes,getBr());
					jpi.print();
					ReturnMsg rtn = jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
					if(rtn != null && !rtn.getStatus()) {
						Messagebox.show(rtn.getMsg());
					} else {
						String ss = getBr().getCellString("inv_contract");
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), os.toByteArray(), "Contract-"+ss);
					}
				} catch (Exception ex) {
					UniLog.log(ex); 
					Messagebox.show(ex.toString());
				}
			}
			};
			abHelper.addButton((Button) jxAdd("btPrintCon").getNativeObject(),false,true,"fa-print",-1);
			
		}
		
		new JxFieldChange("vd_vcode") {
			public boolean valueChanged(JxField fd,String orgValue){  
				UniLog.log("vd_vcode changed , reset all dependent cell to preset");
				Cell cc = getBr().getCell("vd_vcode");
				try {
					cc.clearAllOverrides();
				} catch (CellException cex) {
					UniLog.log(cex);
					return(false);
				}
				/*
				if(jxAdd("svloc_desp") != null ) {
				try {
					TableRec tr = getBr().getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + getBr().getCell("inv_vcode").getString() + "'", null);
					Vector v = new Vector();
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						v.add(tr.getField("svloc_desp"));
					}
					getBr().getCell("svloc_desp").setItemList(v);
				} catch (Exception ex){ 
					UniLog.log(ex);
				}	
				}
				*/
				return(true);
			}
		};
		
		LOCK_RECORD_FOR_UPDATE = true;
	}
	
	void setDisplayOnly(Cell c) {
		try {
				switch(c.getMode()) {
				case Cell.VMODE_PROTECT:
				case Cell.VMODE_NORMAL:
					c.setMode(Cell.VMODE_DISPONLY);
				}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		
	}
	
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		ColumnCell cc = br.getCell("inv_quostatus");
		/*
		Vector statusOptions = null;
		if(cc != null)  {
			statusOptions = new Vector();
			if(br.getSessionHelper().hasAccessRight("!!QuoNew")) {
				statusOptions.add("New");
			}
			if(br.getSessionHelper().hasAccessRight("!!QuoConfirm")) {
				statusOptions.add("Confirmed");
			}
			cc.setItemList(statusOptions);
		}
		*/
		if(cc != null) {
			Vector sList = ((BiResultQuotationG2) br).getOptionList(cc.getBiColumn(), (Comparable) cc.getObject(), mode);
			if(sList != null) {
				cc.setItemList(sList);
			}
		}
		if(mode == JxZkBiBase.MODE_ADD) {
			jxSetEnable("btPrintQuo",false);
			jxSetEnable("btPrintInv",false);
		}
		if(mode == JxZkBiBase.MODE_UPDATE) {
			jxSetEnable("btPrintQuo",true);
			if(br.getCellString("inv_quostatus").equals("Confirmed")) {
				jxSetEnable("btPrintInv",true);
			} else {
				jxSetEnable("btPrintInv",false);
			}
		}
		Cell c;
		switch(((BiResultQuotationG2) br).getQuomode()) {
		case QUOTATION:
				setDisplayOnly(br.getCell("inv_invno"));
				setDisplayOnly(br.getCell("inv_date"));
				break;
		case ORDER:
				setDisplayOnly(br.getCell("inv_quonum"));
				setDisplayOnly(br.getCell("inv_quodate"));
				break;
		}
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new QuoDetG2GetItemProperty(p_br.getSubLink("erpv4.QuoDetG2"), this)
		);
	}

	/* Customized GIPI for QuoDetG2 */
	private class QuoDetG2GetItemProperty extends ZkBiGetItemProperty {
		PickByBiForm pickIcodeForm;

		public QuoDetG2GetItemProperty(BiResult p_br, JxZkBiBase p_bibase) {
			super(p_br, p_bibase);
			UniLog.log1("QuoDetG2GetItemProperty");
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_PULLDOWN_OPENED) {
				if (StringUtils.equals(bcc.getCellLabel(), "st_icode")) {
					try {
						ZkJxPickInput pickComp = (ZkJxPickInput)((ZkBiCellValueMapper)bcc.getMapper()).getComponent();
						if (pickIcodeForm == null) {
							pickIcodeForm = new PickByBiForm(sessionHelper, new PickByBiForm.PickByBiFormCallback() {
								@Override
								public void callback(CellCollection col, Object userData) {
									try {
										BiCellCollection cl = (BiCellCollection) userData;
										cl.getCell("st_icode").set(col.getString("st_icode"));
									} catch (Exception e) {
										UniLog.log(e);
									}
								}
							}); 
						} else pickIcodeForm.beforeBick();
						pickIcodeForm.bindComponent(pickComp, cl, "erpv4.StockPick", StringUtils.equals(((BiResultQuotation)getBr()).getQuotationType(), "AQP") ? "st_mtype = 'P'" : null);
					}
					catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
			else if (p_ctype == GIPI_VALUE_CHANGED) {
			}
			if (p_ctype != GIPI_CELL_MAPPED)
				setDirtyFlag(true);
			else {
				if (StringUtils.equals(bcc.getCellLabel(), "st_icode")) {
					ZkJxPickInput zjpi = (ZkJxPickInput)((ZkBiCellValueMapper)bcc.getMapper()).getComponent();
					zjpi.setPopupWidth("920px");
				}
			}
		}
	}

	public static class PickByBiForm {
		private JxSelOpt gipiForm;
		private BiGetItemProperty gipi;
		private SessionHelper sh;

		public interface PickByBiFormCallback {
			void callback(CellCollection col, Object userData);
		}

		public PickByBiForm(SessionHelper sh, final PickByBiFormCallback cb) {
			this.sh = sh;
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
			gipiForm = JxSelOpt.createJxSelOpt(pvdr);
			gipiForm.setOnSelectAction(new JxActionListener() {
				public void actionPerformed(JxField fd) {
					try {
						CellCollection col = gipi.getCellCollectionByValue(fd.getValue());
						cb.callback(col, gipiForm.getUserData());
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					gipiForm.closeForm();
				}
			});
		}

		public void bindComponent(ZkJxPickInput pickComp, Object userData, String brViewName, String condition) throws Exception {
			BiResult br;
			if (gipi == null || !StringUtils.equals(gipi.getBiResult().getView().getName(), brViewName)) {
				UniLog.log1("newBiResult %s", brViewName);
				br = sh.newBiResult(brViewName);
				gipi = new BiGetItemProperty(br);
				gipi.setItemMode(BiGetItemProperty.GETITEM_MODE_PICK);
			} else
				br = gipi.getBiResult();
			br.clear();
			br.clearCondition();
			br.clearOrderBy();
			if (StringUtils.isNotBlank(condition)) 
				br.addCustomCondition(condition);
			br.query();
			pickComp.setJxZkForm(gipiForm);
			gipiForm.setUserData(userData);
			gipiForm.jxAdd("pickListBox").setItemListInterface(gipi);
		}
		public void beforeBick() {
			gipiForm.beginPick(false);
		}
	}
	
	public static class PickByTrForm {
		private JxSelOpt gipiForm;
		private TrGetItemProperty gipi;
		
		public interface PickByTrFormCallback {
			void callback(Object[] rec, TableRec tr, Object userData);
		}

		public PickByTrForm(SessionHelper sh, String[] fieldList, final PickByTrFormCallback cb) {
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
			gipi = new TrGetItemProperty(Lists.newArrayList(fieldList));
			gipiForm = JxSelOpt.createJxSelOpt(pvdr);
			gipiForm.setOnSelectAction(new JxActionListener() {
				public void actionPerformed(JxField fd) {
					try {
						Object[] rec = (Object[]) fd.getValue();
						TableRec tr = gipi.getTableRec();
						cb.callback(rec, tr, gipiForm.getUserData());
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					gipiForm.closeForm();
				}
			});
		}
		
		public void bindComponent(ZkJxPickInput pickComp, Object userData, BiResult br, String selectStr, Wherecl wherecl) throws Exception {
			pickComp.setJxZkForm(gipiForm);
			TableRec tr = br.getSelectUtil().getQueryResult(selectStr, wherecl);
			gipi.setTableRec(tr);
			gipiForm.setUserData(userData);
			gipiForm.jxAdd("pickListBox").setItemListInterface(gipi);
		}
	}
	
	public void afterPaste() throws Exception {
		jxSetText("inv_quostatus","");
		jxSetText("inv_quostatus","New");
	}
}
