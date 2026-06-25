package com.uniinformation.zkbi.erpv4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultCrhAr;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiPopupBase;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerCostAsAt extends com.uniinformation.zkbi.ZkBiComposerAnalysisReport {
		/*
		protected String module = "";
		protected String paymentViewId = "";
		
		class PopupCrh extends ZkBiPopupBase {
			String custCode = null;
			String cocode = null;
			String cid = null;
			double amount = 0.0;
			double lamount = 0.0;
			BiResult pbr;
			class CrdDetail {
				String vcode;
				String sno;
				String cid;
				double amount;
				double xrate;
				double lamount;
			}
			ArrayList<CrdDetail> crdarr = null;
			public PopupCrh(Window masterWin, SessionHelper sessionHelper, String p_viewName,BiResult p_pbr) throws Exception {
				super(masterWin, sessionHelper, p_viewName);
				pbr = p_pbr;
				init(
						new JxZkBiBaseCallback()  {
							@Override
							public void biBaseRefreshListitems(Object p_dataObj) {
								// TODO Auto-generated method stub
								
							}
							@Override
							public void biBaseRefresh(BiResult p_result) {
								// TODO Auto-generated method stub
								
							}
							@Override
							public void biBaseOpen() {
								// TODO Auto-generated method stub
								UniLog.log("before baseOpen");
							}
							@Override
							public void biBaseClose(BiResult p_br) {
								// TODO Auto-generated method stub
								refresh(pbr,null);
							}
							@Override
							public ReturnMsg fetchNext(BiResult p_br) {
								// TODO Auto-generated method stub
								return null;
							}
							@Override
							public ReturnMsg fetchPrevious(BiResult p_br) {
								// TODO Auto-generated method stub
								return null;
							}
							@Override
							public String getExtraInfo() {
								// TODO Auto-generated method stub
								return null;
							} }
						);
				// TODO Auto-generated constructor stub
				crdarr = new ArrayList<CrdDetail>();
			}
			@Override
			protected void beforePopup(int p_mode,BiResult p_br) {
				UniLog.log("before popup");
								try {

									popupBr.getCell("crh_cocode").set(cocode);
									String defaultBankAc = Erpv4Config.getString(getSessionHelper(), "DefaultBankAc");
									if(defaultBankAc == null) defaultBankAc = "";
									popupBr.getCell("crh_module").set(module);
//									popupBr.getCell("crh_dbinputano").set("1-2100");
									popupBr.getCell("crh_dbinputano").set(defaultBankAc);
									popupBr.getCell("crh_date").set(DateUtil.today());
									popupBr.getCell("crh_vcode").set(custCode);
									popupBr.getCell("crh_cid").set(cid);
									popupBr.getCell("crh_amount").set(module.equals("AR") ? amount : -amount);
//									popupBr.getCell("crh_lamount").set(module.equals("AR") ? lamount : -lamount);
									BiResult sr = popupBr.getSubLink("erpv4.CrdAr");
									for(CrdDetail cd : crdarr ) {
										CellCollection col = sr.newRowCollection();
//										col.getCell("crd_cocode").set("001");
//										col.getCell("crd_crno").set("");
//										col.getCell("crd_sno").set(cd.sno);
//										col.getCell("sih_vcode").set(cd.vcode);
										col.getCell("sih_sno").set(cd.sno);
										col.getCell("crd_cid").set(cd.cid);
										col.getCell("crd_xrate").set(cd.xrate);
//										col.getCell("crd_amount").set(-cd.amount);
//										col.getCell("crd_lamount").set(-cd.lamount);
										col.getCell("crd_settleamount").set(module.equals("AR") ? cd.amount : -cd.amount);
//										col.getCell("crd_drcr").set(cd.amount >= 0 ? "CR" : "DR");
										ReturnMsg rtn = sr.addSubRecord(col,"");
										Object tr = rtn.getData();
									}
									((BiResultCrhAr) popupBr).calPaymentAmount();
								} catch (CellException cex){
									UniLog.log(cex);
								}
			}

			void clearCrh() {
				custCode = null;
				cocode = null;
				cid = null;
				crdarr.clear();
				amount = 0.0;
				lamount = 0.0;
			}
//			public String getCustCode() {
//				return custCode;
//			}
//			public void setCustCode(String custCode) {
//				this.custCode = custCode;
//			}
			public boolean isMultiVcode() {
				return ("".equals(custCode));
			}
			public boolean isMultiCid() {
				return ("".equals(cid));
			}
			public int getCrdCount() {
				return(crdarr.size());
			}
			public void addOnePayment(String p_cocode,String p_custcode,String p_sno,String p_cid,double p_amount,double p_xrate, double p_lamount) throws Exception {
				if(cocode == null) {
					cocode = p_cocode;
				} else {
					if(!custCode.equals(p_custcode)) {
						throw new Exception("Cannot Create Payment for Multiple Company");
					}
				}
				if(cid == null) {
					cid = p_cid;
				} else {
					if(!cid.equals(p_cid)) {
						cid = "";
						amount = 0;
					}
				}
				if(custCode == null) {
					custCode = p_custcode;
				} else {
					if(!custCode.equals(p_custcode)) {
						custCode = "";
					}
				}
				CrdDetail cd = new CrdDetail();
				cd.vcode = p_custcode;
				cd.sno = p_sno;
				cd.cid = p_cid;
				cd.amount = p_amount;
				cd.xrate = p_xrate;
				cd.lamount = p_lamount;
				crdarr.add(cd);
				if(!isMultiCid()) amount += p_amount;
				lamount += p_lamount;
			}
		}
		PopupCrh zkcrhpo = null;
		*/

	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		aggregateOffset = 0;
		zkfName = "zkf/erpv4/CostAsAt.zul";
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}

	@Override
	protected void createZkfCollection(BiResult p_result) {
	    if(rptCol == null) {
	    	rptCol = p_result.getCurrentCollection();
	    	if(rptCol.testCell("pivotStType") == null) {
	    		Cell c = rptCol.addCell("pivotStType", new ColumnCell(false,Cell.VMODE_NORMAL));
	    		c.addAction(rptColChanged);
	    		
	    	}
	    	if(rptCol.testCell("pivotMcType") == null) {
	    		Cell c = rptCol.addCell("pivotMcType", new ColumnCell(false,Cell.VMODE_NORMAL));
	    		c.addAction(rptColChanged);
	    	}
	    	if(rptCol.testCell("showaggs") == null) {
	    		Cell c = rptCol.addCell("showaggs", new Cell(0,Cell.VMODE_NORMAL));
	    		c.addAction(rptColChanged);
	    	}
	    	rptCol.addCell("rpttitle",new Cell( "Stock As At",Cell.VMODE_NORMAL));
	    }
	}
	
	@Override 
	protected void setAggregates(BiResult p_result,AggregateOrPivot p_aop) {
		int showAggs = 0;
		if(p_result.getNativeCell("showaggs") != null) {
			showAggs = p_result.getCellInt("showaggs");
		}
		switch(showAggs) {
		case 1 :
			p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"stmd_sumqty");
			break;
		case 2 :
			p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"stmd_amount");
			break;
		default :
			p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"stmd_sumqty");
			p_aop.addAggregate(AggregateOrPivot.AGGREGATES.SUM,"stmd_amount");
			break;
		}
	} 
	@Override 
	protected void setPivots(BiResult p_result,AggregateOrPivot p_aop) {
		if(p_result.getNativeCell("pivotStType") != null) {
			if(p_result.getNativeCell("pivotStType").getBoolean()) {
				p_aop.addCol("sttp_name");
			}
		}
		if(p_result.getNativeCell("pivotMcType") != null) {
			if(p_result.getNativeCell("pivotMcType").getBoolean()) {
				p_aop.addCol("mt_tpname");
			}
		}
	}
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		super.doAfterCompose(comp);
		adjListboxHeight(60);
	}
}
