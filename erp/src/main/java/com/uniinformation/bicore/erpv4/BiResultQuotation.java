package com.uniinformation.bicore.erpv4;


import java.io.OutputStream;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.hw.BiResultHwInvoice;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;




import com.uniinformation.webcore.SessionHelper;

//import org.zkoss.json.parser.JSONParser;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BiResultQuotation extends BiResultErpv4 {
	static public enum QUOMODE {ANY,QUOTATION,ORDER};
	BiCellAction_calTotalAmount caCalTotalAmount=null; 
	protected String quotationType;
	protected String subLinkId = null;
	protected String invdLinkId = null;
//	protected boolean useGenBucket = true;
	protected boolean isInvoice = false;
	protected boolean hideComboDetailAmount = false;
	protected String invoiceLinkid = null;
	protected boolean modeG2 = false;
	protected QUOMODE quomode = QUOMODE.ANY;
	
	public QUOMODE getQuomode() {
		return quomode;
	}

	public void setQuomode(QUOMODE quomode) {
		this.quomode = quomode;
	}
	
	public String getQuotationType() {
		return quotationType;
	}

	public BiResultQuotation(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr,SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		quotationType = "QUO";
		if(getSubLinks() != null) {
		for(BiResult sr : getSubLinks()) {
			if(sr.getView().getTable().getName().equals("quodet")) {
				subLinkId = sr.getView().getName();
				break;
			}
		}
		for(BiResult sr : getSubLinks()) {
			if(sr.getView().getTable().getName().equals("invoice")) {
				invoiceLinkid = sr.getView().getName();
			}
		}
		}
	}
	
	public String get_subLinkId() {
		return(subLinkId);
	}
	public String get_invdLinkId() {
		return(invdLinkId);
	}
	public String get_invoiceLinkId() {
		return(invoiceLinkid);
	}
	public void real_calTotalAmount() throws CellException {
		Vector <BiCellCollection> recs = getSubLinkResult(subLinkId);
		boolean hasTradeInItem = false;
		double totGr = 0, totTradeIn = 0;
		CellCollection comboCol = null;
		int itemno = 0;
		double area = 0.0;
		boolean allowUpdateTotal = false;
		BiColumn setAmtColumn = getSubLink(subLinkId).getColumnByLabel("ind_setamount");
		if(setAmtColumn != null  && setAmtColumn.isProtected()) {
			allowUpdateTotal = true;
		}
		for(CellCollection col:recs) {
			Cell unitPriceDiv = col.testCell("ind_unitPriceDiv");
			Cell subTotalDiv = col.testCell("ind_subTotalDiv");
			Cell qtyDiv = col.testCell("ind_qtyUnitDiv");
			if(qtyDiv == null) qtyDiv = col.testCell("ind_qtydiv");
			Cell productDiv = col.testCell("ind_productDiv");
			if(unitPriceDiv == null) {
				unitPriceDiv = col.testCell("ind_pricediv");
			}
			if(modeG2) {
				col.getCell("ind_subitem").setMode(Cell.VMODE_DISPONLY);
/*	use pricediv instead of ind_uprice in modeG2
				col.getCell("ind_uprice").setMode(Cell.VMODE_HIDDEN);
*/
//				col.getCell("ind_pricediv").setMode(Cell.VMODE_HIDDEN);
				if(unitPriceDiv != null) unitPriceDiv.setMode(Cell.VMODE_HIDDEN);
				if(subTotalDiv != null) subTotalDiv.setMode(Cell.VMODE_HIDDEN);
				if(qtyDiv != null) qtyDiv.setMode(Cell.VMODE_HIDDEN);
				if(productDiv != null) productDiv.setMode(Cell.VMODE_HIDDEN);
				col.getCell("ind_setamount").setMode(Cell.VMODE_HIDDEN);
				col.getCell("ind_itemno").setMode(Cell.VMODE_DISPONLY);
				col.getCell("ind_itemno").set(0);
			}
			BiResultQuoDet.DELTATYPE deltaType = BiResultQuoDet.getDeltaType(sh,col.getCell("ind_pdsrg").getInt());
			if(deltaType == BiResultQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM) {
				comboCol = col;
				comboCol.getCell("ind_setamount").set(0.0);
				if(modeG2) {

//					col.getCell("ind_uprice").resetValue();
//					col.getCell("ind_amount").resetValue();
					if(subTotalDiv != null) subTotalDiv.setMode(Cell.VMODE_NORMAL);
					if(!allowUpdateTotal) {
						col.getCell("ind_setamount").setMode(Cell.VMODE_DISPONLY);
					} else {
						col.getCell("ind_setamount").setMode(Cell.VMODE_PROTECT);
						totGr += col.getCell("ind_amount").getDouble();
						col.getCell("ind_setamount").set(col.getCell("ind_amount").getDouble());
					}
				}
				itemno++;
				col.getCell("ind_itemno").set(itemno);
			} else if(deltaType == BiResultQuoDet.DELTATYPE.DELTALTYPE_TRADEIN) {
				comboCol = null;
				if(!modeG2) {
					col.getCell("ind_itemno").set("Trade-In");
				} else {
					itemno++;
					col.getCell("ind_itemno").set(itemno);
				}
				totTradeIn += col.getCell("ind_amount").getDouble();
				hasTradeInItem = true;
			} else if(deltaType == BiResultQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK) {
				comboCol = null;
			} else {
				totGr += col.getCell("ind_amount").getDouble();
				if(modeG2) {
					if(comboCol == null) {
						col.getCell("ind_subitem").set("N");
					} else {
						col.getCell("ind_subitem").setMode(Cell.VMODE_NORMAL);
					}
				}
				if(comboCol != null && (!modeG2 || col.getCell("ind_subitem").getBoolean())) {
					double sd = comboCol.getCell("ind_setamount").getDouble();
					sd += col.getCell("ind_amount").getDouble();
					comboCol.getCell("ind_setamount").set(sd);
					if(modeG2) {
						if(deltaType == BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM && productDiv != null) productDiv.setMode(Cell.VMODE_NORMAL);
/*	use pricediv instead of ind_uprice in modeG2
						if(col.getCell("ind_uprice").getFormula() != null) {
//							col.getCell("ind_uprice").setMode(Cell.VMODE_PROTECTED);
							col.getCell("ind_uprice").protect(true);
							col.getCell("ind_uprice").setMode(Cell.VMODE_NORMAL);
						} else {
							col.getCell("ind_uprice").setMode(Cell.VMODE_NORMAL);
							col.getCell("ind_uprice").protect(false);
						}
*/						
//						col.getCell("ind_pricediv").setMode(Cell.VMODE_NORMAL);
						switch(deltaType) {
						case DELTALTYPE_STOCK_ITEM:
						case DELTALTYPE_SERVICE_ITEM:
						case DELTALTYPE_TRADEIN:
						case DELTALTYPE_PRINTING_ITEM:
							if(unitPriceDiv != null) unitPriceDiv.setMode(Cell.VMODE_NORMAL);
						}
						switch(deltaType) {
						case DELTALTYPE_STOCK_ITEM:
						case DELTALTYPE_SERVICE_ITEM:
						case DELTALTYPE_TRADEIN:
						case DELTALTYPE_PRINTING_ITEM:
						case DELTALTYPE_DESCRIPTION:
							if(qtyDiv != null) qtyDiv.setMode(Cell.VMODE_NORMAL);
						}
						col.getCell("ind_itemno").set(0);
						col.getCell("ind_itemno").setMode(Cell.VMODE_HIDDEN);
					} else {
						col.getCell("ind_subitem").set("Y");
						col.getCell("ind_itemno").setMode(Cell.VMODE_HIDDEN);
					}
					if(hideComboDetailAmount) col.getCell("ind_amount").setMode(Cell.VMODE_HIDDEN);
					if(!modeG2) {
						col.getCell("ind_srg").set(comboCol.getCell("ind_srg").getInt());
					}
				} else {
					itemno++;
					if(modeG2) {
						if(deltaType == BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM && productDiv != null) productDiv.setMode(Cell.VMODE_NORMAL);
						col.getCell("ind_setamount").set(col.getCell("ind_amount").getDouble());
/*	use pricediv instead of ind_uprice in modeG2
						if(col.getCell("ind_uprice").getFormula() != null) {
//							col.getCell("ind_uprice").setMode(Cell.VMODE_PROTECTED);
							col.getCell("ind_uprice").setMode(Cell.VMODE_NORMAL);
							col.getCell("ind_uprice").protect(true);
						} else {
							col.getCell("ind_uprice").setMode(Cell.VMODE_NORMAL);
							col.getCell("ind_uprice").protect(false);
						}
*/						
//						col.getCell("ind_pricediv").setMode(Cell.VMODE_NORMAL);
						if(unitPriceDiv != null) unitPriceDiv.setMode(Cell.VMODE_NORMAL);
						if(qtyDiv != null) qtyDiv.setMode(Cell.VMODE_NORMAL);
						if(!col.getCell("ind_subitem").getBoolean()) {
							col.getCell("ind_itemno").set(itemno);
							col.getCell("ind_setamount").setMode(Cell.VMODE_DISPONLY);
						} else {
							col.getCell("ind_itemno").set(0);
							col.getCell("ind_itemno").setMode(Cell.VMODE_HIDDEN);
						}
					} else {
						col.getCell("ind_itemno").set(itemno);
						col.getCell("ind_subitem").set("N");
						col.getCell("ind_itemno").setMode(Cell.VMODE_DISPONLY);
						if(hideComboDetailAmount) col.getCell("ind_amount").setMode(Cell.VMODE_DISPONLY);
					}
				}
				if(deltaType == BiResultQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
					double aa = 
							col.getCell("ind_usize1").getDouble() *
							col.getCell("ind_usize2").getDouble() *
							col.getCell("ind_qty").getDouble();
					area += aa * 0.000001;
				}
			}
			if(modeG2) {
				if(deltaType != BiResultQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM
						&& comboCol != null && !col.getCell("ind_subitem").getBoolean()
						) {
					comboCol = null;
				}
			}
				if(deltaType == BiResultQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
				} else {
					if(col.testCell("ind_puprice") != null) col.getCell("ind_puprice").setMode(Cell.VMODE_HIDDEN);
					if(col.testCell("mur_name") != null) col.getCell("mur_name").setMode(Cell.VMODE_HIDDEN);
				}
		}
		UniLog.log("real_calTotalAmount hasTradeInItem:" + hasTradeInItem + ",totGr:" + totGr + ",totTradeIn:" + totTradeIn);
		getCell("inv_grtotal").sync(totGr);
		if (getCell("inv_tradein") != null){ //andrew 181115: hotfix SO(Parts) null exception
			getCell("inv_tradein").sync(-totTradeIn);
		}
		//getCell("inv_tradein").setMode(hasTradeInItem ? Cell.VMODE_DISPONLY : Cell.VMODE_HIDDEN);
		//getCell("inv_tilabel").setMode(hasTradeInItem ? Cell.VMODE_DISPONLY : Cell.VMODE_HIDDEN);
		if(getCell("inv_areatotal") != null) getCell("inv_areatotal").sync(area);
		
	}
	
	class BiCellAction_calTotalAmount extends CellValueAction 
	{
		CellCollection col=null;
//		private boolean enabled = false;
		BiCellAction_calTotalAmount(CellCollection p_col) {
			col = p_col;
		}

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(!isActionEnabled()) return;
			real_calTotalAmount();
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
		}
		
	}	
	protected void setCellActionCalTotalAmount(ColumnCell p_cell) {
	BiCellAction_calTotalAmount caCalTotalAmount = null;
		if(caCalTotalAmount == null) caCalTotalAmount = new BiCellAction_calTotalAmount (getCurrentCollection());
		p_cell.addAction(caCalTotalAmount);
		
	}
	
	ReturnMsg updateQuoDetGenBucket(CellCollection col)
	{
			if(isInvoice) return(ReturnMsg.defaultOk);
			RpcClient rpc = getSelectUtil().getRpcClient();
	   		Vector args = new Vector();
	   		boolean useNewRpc = "Y".equals(Erpv4Config.getString(sh, "UseNewGenbuckRpc"));
	   				
			args.add(col.getCell("inv_rg").getInt());
			args.add(col.getCell("inv_quostatus").getString());
			Vector <BiCellCollection> v = getSubLinkResult(subLinkId);
			for(int i=0;i<v.size();i++) {
				CellCollection scol = v.get(i);
				if(useNewRpc) {
					try {
						JSONObject jo =  new JSONObject();
						jo.put("odrg", scol.getCell("ind_odrg").getInt());
						jo.put("irg", scol.getCell("ind_irg").getInt());
						if(scol.testCell("ind_cocode") != null) {
							jo.put("cocode", scol.getCell("ind_cocode").getString());
						}
						if(scol.testCell("ind_resrg") != null) {
							jo.put("resrg", scol.getCell("ind_resrg").getInt());
						}
						jo.put("stqty", scol.getCell("ind_stqty").getDouble());
						jo.put("tiqty", scol.getCell("ind_tiqty").getDouble());
						jo.put("amount", scol.getCell("ind_amount").getDouble());
						args.add(jo.toString());
					} catch (JSONException jex) {
						UniLog.log(jex);
					}
				} else {
					args.add(scol.getCell("ind_odrg").getInt());
					args.add(scol.getCell("ind_irg").getInt());
					args.add(scol.getCell("ind_stqty").getDouble());
					args.add(scol.getCell("ind_tiqty").getDouble());
					args.add(scol.getCell("ind_amount").getDouble());
				}
				UniLog.logm(this, "updateQuoDetGenBucket odrg:" + scol.getCell("ind_odrg").getInt() 
						+ ",irg:" + scol.getCell("ind_irg").getInt()
						+ ",qty:" + scol.getCell("ind_stqty").getDouble()
						+ ",tiqty:" + scol.getCell("ind_tiqty").getDouble());
			}
	  		Value val;
			if(useNewRpc) {
				val = rpc.callSegment(
						"erpv4JsonUpdateQuodetGenBucket",
						args
					);
			} else {
				val = rpc.callSegment(
						"erpv4UpdateQuodetGenBucket",
						args
					);
			}
			
	  		

			if(val != null && val.toString().startsWith("OK")) {
//				val = rpc.callSegment(
//						"erpv4GenbucketCommit", new Vector()
//					);
//				if(val != null && val.toString().startsWith("OK")) {
//					String s = StringUtil.strpart(val.toString(), 4 , -1);
//					if(!s.trim().equals("")) {
//						return(GenbucketUtil.qoGenBucketCheckResult(s));
//					} else return(null);
//				} 
				return(null);
			}
			if(val == null) {
				return(new ReturnMsg(false, "Confirm Failed : Unknown Reason"));
			} else {
				return(new ReturnMsg(false, "Confirm Failed : " + val.toString().substring(4)));
			}
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			if(!modeG2) {
			String s = col.getCell("inv_invno").getString();
			if(s == null || s.trim().equals("")) {
				BiResult sr = getSubLink(subLinkId);
				if(sr.getRowCollectionList().size() > 0) {
					col.getCell("inv_invno").set(getNewOrderNumber(col.getCell("inv_date").getDate()));
				}
			}
			Vector <BiCellCollection> v = getSubLinkResult(subLinkId);
			for(int i=0;i<v.size();i++) {
				CellCollection scol = v.get(i);
				if((BiResultQuoDet.getDeltaType(sh,scol.getCellInt("ind_pdsrg")) == BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM) && scol.getCell("ind_irg").getInt() <= 0 ) {
					return(new ReturnMsg(false,"Item Record Incomplete"));
				}
				scol.getCell("ind_seq").set(i);
			}
			col.getCell("inv_uuser").set(su.getLoginId());
			col.getCell("inv_udate").set(new java.util.Date());
			}
			return(updateQuoDetGenBucket(col));
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
	}
	public String getNewQuotationNumber(java.util.Date p_date) throws Exception {
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
//				.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//				.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
				.addElement(getCellString("inv_cocode"))
				.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),getCellString("inv_cocode")))
				.toVector()
				);
		Value val = rpc.callSegment("getrg_byrgcontrol_bycategory",
				new VectorUtil()
				.addElement("quonumber")
				.addElement(p_date)
				.toVector()
				);
		if(val == null || !(val instanceof Strval)) {
			throw new Exception("Get Quotation Number Failed");
		}
		//return(val.toString());
		return(val.toString().trim()); //andrew200528: fix value too long bug
	}
	public String getNewOrderNumber(java.util.Date p_date) throws Exception {
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
//				.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//				.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
				.addElement(getCellString("inv_cocode"))
				.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),getCellString("inv_cocode")))
				.toVector()
				);
		Value val = rpc.callSegment("getrg_byrgcontrol_bycategory",
				new VectorUtil()
				.addElement("quotation")
				.addElement(p_date)
				.toVector()
				);
		if(val == null || !(val instanceof Strval)) {
			throw new Exception("Get Quotation Number Failed");
		}
		//return(val.toString());
		return(val.toString().trim()); //andrew200528: fix value too long bug
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		try {
			real_calTotalAmount();
		} catch (CellException cex) {
			UniLog.log(cex);
			return new ReturnMsg(false,cex.toString());
		}
		RpcClient rpc = getSelectUtil().getRpcClient();
		if(col.testCell("inv_type") != null && col.getCellString("inv_type").equals("") ){
			try {
				col.getCell("inv_type").set(quotationType);
			} catch(CellException cex) {
				UniLog.log(cex);
			}
		}
		Value val = rpc.callSegment(
				"erpv4GenbucketBegin", new Vector()
				);	
		if(val == null || !val.toString().startsWith("OK")) {
			return(new ReturnMsg(false,"genbucket begin failed"));
		}
		if(isInvoice) {
			val = rpc.callSegment(
				"erpv4AddInvquodetGenBucket", 
					new VectorUtil()
					.addElement(col.getCell("inv_rg").getInt())
					.addElement(-1.0)
					.toVector()
				);	
			if(val == null || !val.toString().startsWith("OK")) {
				return(new ReturnMsg(false,"genbucket add invquodet 0 failed"));
			}
		}
		return(null);
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			if(!modeG2) {
			String s = col.getCell("inv_invno").getString();
			if(s == null || s.trim().equals("")) {
				BiResult sr = getSubLink(subLinkId);
				if(this instanceof BiResultHwInvoice || sr.getRowCollectionList().size() > 0 || modeG2) {
					col.getCell("inv_invno").set(getNewOrderNumber(col.getCell("inv_date").getDate()));
				}
			}
			Vector <BiCellCollection> v = getSubLinkResult(subLinkId);
			for(int i=0;i<v.size();i++) {
				CellCollection scol = v.get(i);
				scol.getCell("ind_seq").set(i);
			}
			col.getCell("inv_cuser").set(su.getLoginId());
			col.getCell("inv_cdate").set(new java.util.Date());
			}
			
			return(updateQuoDetGenBucket(col));
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
	}
	
	@Override 
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
			RpcClient rpc = getSelectUtil().getRpcClient();
	  		Value val = null;
			if(isInvoice) {
				val = rpc.callSegment(
					"erpv4AddInvquodetGenBucket", 
					new VectorUtil()
					.addElement(col.getCell("inv_rg").getInt())
					.addElement(1.0)
					.toVector()
				);	
				if(val == null || !val.toString().startsWith("OK")) {
				return(new ReturnMsg(false,"genbucket add invquodet 0 failed"));
				}
			}
	  		val = 
				rpc.callSegment(
						"erpv4GenbucketCommit", new Vector()
					);
				if(val != null && val.toString().startsWith("OK")) {
					String s = StringUtil.strpart(val.toString(), 4 , -1);
					if(!s.trim().equals("")) {
						HashSet<Integer> ignoreIrgSet = new HashSet<Integer>();
						for(BiCellCollection scol : getSubLinkResult(subLinkId)) {
							if(scol.getCellBoolean("st_isconsumable")) {
								ignoreIrgSet.add(scol.getCellInt("ind_irg"));
							}
						}
						return(GenbucketUtil.qoGenBucketCheckResult(s,sh,ignoreIrgSet));
					} else return(null);
				} 
			if(val == null) {
				return(new ReturnMsg(false, "Confirm Failed : Unknown Reason",true));
			} else {
				return(new ReturnMsg(false, "Confirm Failed : " + val.toString().substring(4),true));
			}
	}
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
//   		RpcClient rpc = getView().getSchema().getRpcClient();
//		if(isInvoice) {
//			return(ReturnMsg.defaultOk);
////			try {
////				String tn = getSubLink(subLinkId).getView().getTable().getDbtName();
////				su.executeUpdate("delete from " + tn + " ",
////						new Wherecl().andUniop("ind_rg", "=",col.getCell("inv_rg").getObject()).stripAnd()
////					);
////				return(new ReturnMsg(true));
////			} catch (Exception ex) {
////				UniLog.log(ex);
////				return(new ReturnMsg(false, ex.toString()));
////			}
//		}
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value v = rpc.callSegment(
				"erpv4GenbucketBegin", new Vector()
				);	
		if(v == null || !v.toString().startsWith("OK")) {
			return(new ReturnMsg(false,"genbucket begin failed"));
		}
		if(isInvoice) {
			v = rpc.callSegment(
				"erpv4AddInvquodetGenBucket", 
					new VectorUtil()
					.addElement(col.getCell("inv_rg").getInt())
					.addElement(-1.0)
					.toVector()
				);	
			if(v == null || !v.toString().startsWith("OK")) {
				return(new ReturnMsg(false,"genbucket delete invquodet failed"));
			}
			v = rpc.callSegment(
					"erpv4GenbucketCommit", new Vector()
				);
			if(v == null || !v.toString().startsWith("OK")) {
				return(new ReturnMsg(false,"genbucket commit failed"));
			}
			return(ReturnMsg.defaultOk);
		} else {
			Vector args = new Vector();
			args.add(col.getCell("inv_rg").getInt());
			args.add(col.getCell("inv_quostatus").getString());
			v = rpc.callSegment(
					"erpv4UpdateQuodetGenBucket",
					args
				);
//    		rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				// redir to P.O. Update Page
				try {
							TableRec tr = su.getQueryResult("select ind_odrg,ind_irg from quodet where ind_rg = " + col.getCell("inv_rg").getInt() , null);
							for(int i=0;i< tr.getRecordCount();i++) {
								tr.setRecPointer(i);
								args = new Vector();
								args.add(col.getCell("inv_rg").getInt());
								args.add(tr.getField("ind_odrg"));
								args.add(tr.getField("ind_irg"));
								Value val = rpc.callSegment( "erpv4UpdateQuodetStmdGenBucket", args);
								if(val == null || !val.toString().startsWith("OK")) {
									return ( new ReturnMsg(false,"Failed 001"));
								}
							}
							su.executeUpdate("delete from stmovd",
									new Wherecl().andUniop("stmd_mrg", "=",col.getCell("inv_rg").getObject()).stripAnd()
									);
				} catch (Exception ex) {
					UniLog.log(ex);
					return(new ReturnMsg(false, ex.toString()));
				}
				v = rpc.callSegment(
						"erpv4GenbucketCommit", new Vector()
					);
				if(v != null && v.toString().startsWith("OK")) {
					ReturnMsg rtnMsg = null;
					String s = StringUtil.strpart(v.toString(), 4 , -1);
					if(!s.trim().equals("")) {
						rtnMsg = GenbucketUtil.qoGenBucketCheckResult(s,sh);
					} 
					if(rtnMsg == null || rtnMsg.getStatus()) {
						try {
							su.executeUpdate("delete from quodet",
									new Wherecl().andUniop("ind_rg", "=",col.getCell("inv_rg").getObject()).stripAnd()
									);
							return(null);
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false, ex.toString()));
						}
					}
				} 
			} 
			if(v == null) {
				return(new ReturnMsg(false, "Delete Failed : Unknown Reason"));
			} else {
				return(new ReturnMsg(false, "Delete Failed : " + v.toString().substring(4)));
			}
		}	
	}
	@Override
	protected void afterFetch() {
			try {
				real_calTotalAmount();
			} catch (CellException cex){
				UniLog.log(cex);
			}
	}
	
	/*
	@Override
	public boolean fetchOneRecV(int p_tridx)
	{
//		currentTrIdx = p_tridx;
		boolean b = super.fetchOneRecV(p_tridx);
		if(b) {
			try {
				real_calTotalAmount();
			} catch (CellException cex){
				UniLog.log(cex);
			}
			
		}
		return(b);
	}
	*/
	@Override
	protected ReturnMsg validateOneRow(CellCollection col,boolean p_update) {
		try {
			if(modeG2) {
			if(p_update) {
				switch(quomode) {
					case ANY:
					case ORDER:  {
						String s = col.getCell("inv_invno").getString();
						if(s == null || s.trim().equals("")) {
							BiResult sr = getSubLink(subLinkId);
							if(sr.getRowCollectionList().size() > 0) {
								col.getCell("inv_invno").set(getNewOrderNumber(col.getCell("inv_date").getDate()));
							}
						}
					}
					break;
					case QUOTATION: {
						String s = col.getCell("inv_quonum").getString();
						if(s == null || s.trim().equals("")) {
							BiResult sr = getSubLink(subLinkId);
							if(sr.getRowCollectionList().size() > 0) {
								col.getCell("inv_quonum").set(getNewQuotationNumber(col.getCell("inv_quodate").getDate()));
							}
						}
					}
					break;
				}
				Vector <BiCellCollection> v = getSubLinkResult(subLinkId);
				for(int i=0;i<v.size();i++) {
					CellCollection scol = v.get(i);
			        if((BiResultQuoDet.getDeltaType(sh,scol.getCellInt("ind_pdsrg")) == BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM) && scol.getCell("ind_irg").getInt() <= 0 ) {
//					if(scol.getCell("ind_pdsrg").equals(BiResultQuoDet.DELTALTYPE_STOCK_ITEM) && scol.getCell("ind_irg").getInt() <= 0 ) {
						return(new ReturnMsg(false,"Item Record Incomplete"));
					}
					scol.getCell("ind_seq").set(i);
				}
				col.getCell("inv_uuser").set(su.getLoginId());
				col.getCell("inv_udate").set(new java.util.Date());
			} else {
				switch(quomode) {
					case ANY:
					case ORDER:  {
						String s = col.getCell("inv_invno").getString();
						if(s == null || s.trim().equals("")) {
							BiResult sr = getSubLink(subLinkId);
							if(this instanceof BiResultHwInvoice || sr.getRowCollectionList().size() > 0 || modeG2) {
								col.getCell("inv_invno").set(getNewOrderNumber(col.getCell("inv_date").getDate()));
							}
						}
					}
					break;
					case QUOTATION: {
						String s = col.getCell("inv_quonum").getString();
						if(s == null || s.trim().equals("")) {
							BiResult sr = getSubLink(subLinkId);
							if(this instanceof BiResultHwInvoice || sr.getRowCollectionList().size() > 0 || modeG2) {
								col.getCell("inv_quonum").set(getNewQuotationNumber(col.getCell("inv_quodate").getDate()));
							}
						}
					}
					break;
				}
				Vector <BiCellCollection> v = getSubLinkResult(subLinkId);
				for(int i=0;i<v.size();i++) {
					CellCollection scol = v.get(i);
					scol.getCell("ind_seq").set(i);
				}
				col.getCell("inv_cuser").set(su.getLoginId());
				col.getCell("inv_cdate").set(new java.util.Date());
			}
			}
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		ReturnMsg rtn = super.validateOneRow(col,p_update);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(isInvoice) return(rtn);
		if(invoiceLinkid != null) {
			RpcClient rpc = getSelectUtil().getRpcClient();
			rpc.callSegment("setCocodeBaseccy",
					new VectorUtil()
//					.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//					.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
					.addElement(getCellString("inv_cocode"))
					.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),getCellString("inv_cocode")))
					.toVector()
					);
			BiResult sr = getSubLink(invoiceLinkid);
			boolean hasInvoice = false;
			Vector<BiCellCollection> v = sr.getRowCollectionList();
			for(BiCellCollection c : v) {
				if(c.getCell("invh_invno").equals("")) {
					Value val = rpc.callSegment("getrg_byrgcontrol_bycategory",
							new VectorUtil()
							.addElement("invoicing")
							.addElement(c.getCell("invh_date").getDate())
							.toVector()
							);
					if(val == null || !(val instanceof Strval)) {
						return(new ReturnMsg(false,"Fail to get Invoice Number"));
					}
					try {
						c.getCell("invh_invno").set(val.toString().trim());
					} catch (CellException cex) {
						UniLog.log(cex);
						return(new ReturnMsg(false,"Fail to set Invoice Number"));
					}
				}
				if((!c.getCellString("invh_voidflag").equals("Y")) && (!c.getCell("invh_post").equals("P"))) {
					hasInvoice = true;
//					break;
				}
			}
			if(hasInvoice ) {
				if(!getCellString("inv_quostatus").equals("Confirmed")) return(new ReturnMsg(false,"Invoice Exist, Cannot UnConfirm"));
			}
		}
		return(rtn);
		
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
		BiColumn locCol = getColumnByLabel("inv_cocode");
		if(locCol != null && columnInSelectList(locCol)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and " + getView().getTable().getName() + "." + "inv_cocode = '"+cocode+"' ").stripAnd();
			p_where.andWherecl(wcl1);
		}
		}
		if(quomode != null) {
		switch(quomode) {
		case QUOTATION:
//				p_where.andUniop("inv_quonum", ">", ""); because  '>' operation not works for string field
				p_where.andUniop("inv_quonum", ">=", "!");
				break;
		case ORDER:
//				p_where.andUniop("inv_invno", ">", "");
				p_where.andUniop("inv_invno", ">=", "!");
				break;
		}
		}
		return(ht);
	}		
	
	
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(Erpv4Config.isMultiCompany(sh)) {
		if(p_lookupTable.getName().equals("locationcode")) {
			if(wcl == null ) wcl = new Wherecl();
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			wcl.appendString(" and " + p_lookupTable.getDbtName() + ".loc_cocode = '"+cocode+"' ").stripAnd();
		}
		if(p_lookupTable.getName().equals("fromloc")) {
			if(wcl == null ) wcl = new Wherecl();
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' ").stripAnd();
		}
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}

	public boolean isModeG2() {
		return(modeG2);
	}

	public ReturnMsg printQuotation(OutputStream os,JSONObject option) { 
		BiResultQuotation ppr = null;
		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		String dnDoc = Erpv4Config.getString(getSessionHelper(),"dnDocCode");
    		if(dnDoc == null) dnDoc = "GENINV03";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("stm_cocode"),
    				paperType,
    			    dnDoc,
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		/*ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","Quotation");
    		if(Erpv4Config.isMultiCompany(getSessionHelper())) {
    			String logo = Erpv4Config.getCoLogo(getSessionHelper(), getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,0,0,0,440);
    			}
    		} else {
    			if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,0,0,0,440);
    			}
    		}*/
  			ppj.addHeaderImage("logo", Erpv4Config.getString(getSessionHelper(), "QuoBgImage"),0,0,0,800);
    		String ds = DateUtil.toDateString( getCell("inv_date").getDate(),"yyyy/mm/dd");
    		ppj.addHeaderField("dfvalue", ds,-120,290);
    		ppj.addHeaderField("cicontent",getCellString("vd_vname"), -45, 150);
   			String addr = getCellString("vd_addr0").trim()+getCellString("vd_addr1").trim() + " " +  getCellString("vd_addr2").trim() + " " + getCellString("vd_addr3").trim();
    		ppj.addHeaderField("cicontent",addr,-45, 170);
    		ppj.addHeaderField("cicontent",getCellString("inv_contact"),-45,190);
    		ppj.addHeaderField("cicontent",getCellString("inv_tel"),-45,210);

    		/*ppj.addHeaderField("ciname","Company Name:");
    		ppj.addHeaderField("ciname","Address:",0,20);
    		ppj.addHeaderField("ciname","Contact Person:",0,40);
    		ppj.addHeaderField("ciname","Email:",0,60);
    		ppj.addHeaderField("ciname","Tel:",0,80);
    		ppj.addHeaderField("ciname","Mob:",0,100);
    		

    		ppj.addHeaderField("cicontent",getCellString("vd_vname"));
   			String addr = getCellString("vd_addr0").trim()+getCellString("vd_addr1").trim() + " " +  getCellString("vd_addr2").trim() + " " + getCellString("vd_addr3").trim();
    		ppj.addHeaderField("cicontent",addr,0,20);
    		ppj.addHeaderField("cicontent",getCellString("inv_contact"),0,40);
    		ppj.addHeaderField("cicontent",getCellString("inv_tel"),0,80);
    		ppj.addHeaderField("cicontent",getCellString("inv_fax"),0,100);

    		
    		ppj.addHeaderField("dflabel","Refs:",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("inv_quonum"),0,0);
    		ppj.addHeaderField("dflabel","Date:",0,30);
    		String ds = DateUtil.toDateString( getCell("inv_date").getDate(),"yyyy/mm/dd");
    		ppj.addHeaderField("dfvalue", " "+ds ,-5,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_seq","Item");
    		ppj.addDetailHeaderField("hdr_orderno","Brand");
    		ppj.addDetailHeaderField("hdr_itemcode","Model");
    		ppj.addDetailHeaderField("hdr_description","Description");
    		ppj.addDetailHeaderField("hdr_qty","Qty");
    		ppj.addDetailHeaderField("hdr_serialno","Amount("+getCellString("inv_cid")+")");*/


    		Vector<BiCellCollection> v = null;
    		v = getSubLink(subLinkId).getRowCollectionList();
    		String indPrefix = "ind_";

    		int n = 0;
    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			String s = "";
    			boolean isSubitem = false;
    			if(c.testCell(indPrefix+"subitem") != null) {
    				if(!c.testCell(indPrefix+"subitem").getBoolean()) {
    					if(c.testCell("stmcm_name") != null)  {
    						ppj.setBold(true);
    						s += c.getCellString("stmcm_name");
    						//ppj.addDetailRecordField("amount", c.getCell(indPrefix+"setamount").getString());
    					}
    				} else {
    					isSubitem = true;
    				}
    			}
    			if(!isSubitem) {
    					n++;
    					//ppj.addDetailRecordField("seq", ""+n);
    			}
    			if(c.testCell(indPrefix+"irg") != null) {
    				if(c.testCell(indPrefix+"irg").getInt() > 0) {
    					if(c.testCell("st_iname") != null)  {
    						s += c.getCellString("st_iname");
    					}
    					if(Erpv4Config.getString(getSessionHelper(), "CustomSmartac") != null) {
							s += " ";
							s += "["+c.getCellString("st_icode")+"]";
    					}
    				}
    			}
    			if(c.testCell(indPrefix+"desp") != null) {
   					s += c.getCellString(indPrefix+"desc");
    			}
    			ppj.addDetailRecordField("description", s, -200, 0);
    			ppj.addDetailRecordField("description", c.getCellString("mt_tpname"), 90, 0);
//    			ppj.addDetailRecordField("serialno",c.getCellString(indPrefix+"ref4"));
//    			ppj.addDetailRecordField("orderno",c.getCellString("inv_invno"));
    			if(!isSubitem) {
    				//ppj.addDetailRecordField("amount",c.getCellString(indPrefix+"amount"));
    			}
    			//ppj.addDetailRecordField("brand", c.getCellString("stbd_name"));
    			String oicode = c.getCellString("st_oicode");
    			String modelno = c.getCellString("st_modelno");
    			if(oicode != null && !oicode.equals("")) {
    				//ppj.addDetailRecordField("itemcode", oicode);
    			} else {
    				if(modelno != null && !modelno.equals("")) {
    					//ppj.addDetailRecordField("itemcode", modelno);
    				}
    			}
    			ppj.setBold(false);
//    			ppj.setUnderLine(false);
    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
    				//ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
    			}
   				ppj.addDetailRecordField("amount", c.getCell(indPrefix+"uprice").getString(), -100, 0);
    		}
    		int ofs = 0;
   			String remark = null; 
   			remark = getCellString("inv_term");
			if(remark == null) remark = getCell("inv_quodeli").getString(); else remark += "\r" + getCell("inv_quodeli").getString();
			if(!getCellString("inv_remark").equals("")) {
				remark += "\r"+ getCellString("inv_remark");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("inv_remark");
			}
   			ppj.addBottomField("val_remark",remark,160,255,20,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
	}
}

