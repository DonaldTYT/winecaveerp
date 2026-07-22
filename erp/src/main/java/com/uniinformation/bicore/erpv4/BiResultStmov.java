package com.uniinformation.bicore.erpv4;


import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Vector;

//import org.zkoss.zk.ui.Component;
//import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.erpv4.AccApi;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
//import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
//import com.uniinformation.zkbi.BiActionHandler;
//import com.uniinformation.zkbi.ZkBiComposerBase;

public class BiResultStmov extends BiResultErpv4 {
	static public final String GEN_MO_STMOVD_LINK = "erpv4.MoGenericDet";
	protected String stmdLinkName;
	protected String tolAmtCell;
	protected String tolGwtCell;
	public String detAmtCell;
	protected String detGwtCell;
	protected HashSet<String> extraStmds;
	HashSet<String> affectedIrg = null;
	boolean updateCostTable = true;
	protected String docCode = null;
	protected String paperType = null;
	protected boolean queryAllCompany = false;
	
	public CellValueAction stmCalGrossWeight = new CellValueAction() {

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(isActionEnabled()) calGrossWeight();
			
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
	public void calGrossWeight()
	{
			if(tolGwtCell == null || detGwtCell == null) return;
    		BiResult sr = getSubLink(stmdLinkName);
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		double fval = 0;
    		for(BiCellCollection c : v) {
    			fval += c.getDouble(detGwtCell);
    		}
    		try {
    			getCell(tolGwtCell).set(fval);
    		} catch (CellException cex) {
    			UniLog.log(cex);
    		}
	}

	public CellValueAction stmCalAmount = new CellValueAction() {

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(isActionEnabled()) calAmount();
			
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
		}
		
	};
	
	boolean inClearRec = false;
	public void clearCurrentRec() {
		inClearRec = true;
		super.clearCurrentRec();
		inClearRec = false;
	}
	
	public void calAmount()
	{
			if(tolAmtCell == null || detAmtCell == null) return;
			if(inClearRec) return;
    		BiResult sr = getSubLink(stmdLinkName);
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		double fval = 0;
    		for(BiCellCollection c : v) {
    			fval += c.getDouble(detAmtCell);
    		}
    		try {
    			getCell(tolAmtCell).set(fval);
    		} catch (CellException cex) {
    			UniLog.log(cex);
    		}
	}
	
	public BiResultStmov (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		if(getSubLinks() != null) {
		for(BiResult sr : getSubLinks()) {
			if(sr.getView().getTable().getName().equals("stmovd_any")) {
				stmdLinkName = sr.getView().getName();
				addSublinkAction(stmdLinkName,stmCalAmount);
				break;
			}
		}
		}
		extraStmds = new HashSet<String>();
	}
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
//   		RpcClient rpc = getView().getSchema().getRpcClient();
		if(col.getCell("stm_status").equals("Confirmed")) {
			if(!getSessionHelper().isAdminUser()) {
				return(new ReturnMsg(false,"Cannot Delete Confirmed Transaction"));
			}
		}
		if(updateCostTable)  {
		try {
			affectedIrg = new HashSet<String>(); 
			setAffectedIrgFromDatabase();
		} catch (Exception ex) {
			UniLog.log(ex);
			ReturnMsg rtnMsg = new ReturnMsg(false,"select stmovd error");
			rtnMsg.setFatal(true);
			return(rtnMsg);
		}
		}
		RpcClient rpc = getSelectUtil().getRpcClient();

		/* No need to do this in java, erpv4RegenStmdInv will do the remove sih stuff , 2020/12/04, by DT
		UniLog.logm(this, "biBeforeDeleteCurrent haha0");
		BiResult sr = getSubLinkByTable("stmpostinv");
		if(sr != null) {
			Vector<BiCellCollection> v = sr.getRowCollectionList();
			for(BiCellCollection c : v) {
				ReturnMsg rtnMsg = AccApi.sih_remove(rpc, Erpv4Config.getBaseCcy(getSessionHelper()),
						c.getCell("stmpi_cocode").getString(),c.getCell("stmpi_sno").getString());
				if(!rtnMsg.getStatus()) {
					rtnMsg.setFatal(true);
					return(rtnMsg);
				}
			}
		}
		*/
		
		
   		Vector args = new Vector();
		args.add(col.getCell("stm_mrg").getInt());
		args.add(col.getCell("stm_status").getString());
  		Value v = rpc.callSegment(
					"erpv4UpdateStmdGenBucket",
					args
				);
//    		rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				
				v = rpc.callSegment(
						"erpv4GenbucketCommit", new Vector()
					);
		  		if(v != null && v.toString().startsWith("OK")) {
		  			ReturnMsg rtnMsg = null;
		  			String s = StringUtil.strpart(v.toString(), 4 , -1);
		  			if(!s.trim().equals("")) {
		  				String allowNegativeStock = Erpv4Config.getString(getSessionHelper(), "AllowNegativeStock");

		  				if(allowNegativeStock != null && allowNegativeStock.equals("Y")) {
		  						return(ReturnMsg.defaultOk);
		  				} 
		  				rtnMsg = GenbucketUtil.qoGenBucketCheckResult(s,sh);
		  			} 
					if(rtnMsg == null || rtnMsg.getStatus()) {
						try {
							su.executeUpdate("delete from stmovd",
									new Wherecl().andUniop("stmd_mrg", "=",col.getCell("stm_mrg").getObject()).stripAnd()
								);
							args = new Vector();
							args.add(col.getCell("stm_type")) ;
							args.add(col.getCell("stm_mrg")) ;
							Value val = rpc.callSegment( "erpv4RegenStmdInv", args);
							if(val == null || !val.toString().startsWith("OK")) { 
								rtnMsg = new ReturnMsg(false, "RegenInv Failed " + (val == null ? "": val.toString()),true); 
//								rtnMsg.setFatal(true);
								return( rtnMsg);
							}
							return(null);
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false, ex.toString()));
						}
						
					}
		  		} 
				// redir to P.O. Update Page
			} 
			if(v == null) {
				return(new ReturnMsg(false, "Delete Failed : Unknown Reason"));
			} else {
				return(new ReturnMsg(false, "Delete Failed : " + v.toString()));
			}
	}
	
	ReturnMsg updateBucket(CellCollection pcol) {
//  		RpcClient rpc = getView().getSchema().getRpcClient();
		UniLog.logm(this, "updateBucket haha0");
		RpcClient rpc = getSelectUtil().getRpcClient();
   		Vector args = new Vector();
		args.add(pcol.getCell("stm_mrg").getInt());
		args.add(pcol.getCell("stm_status").getString());
//		Vector <CellCollection> recs = getSubLink("AfsPoDet").getRecs();
		Vector <BiCellCollection> recs = getSubLinkResult(stmdLinkName);
		double d = 0;
		int n=0;
		boolean needReIndex = !GEN_MO_STMOVD_LINK.equals(stmdLinkName);
  		ReturnMsg msg;
		try {
		for(CellCollection col:recs) {
			if(needReIndex) col.getCell("stmd_tdindex").set(n);
			int org = col.getCell("stmd_org").getInt();
			if(org == 0 && Erpv4Config.isMultiCompany(sh)) {
				msg = new ReturnMsg(false, "stmd_org cannot be zero for multicomplay setup");
				msg.setFatal(true);
				return(msg);
			}
			/*
			if(org <= 0) {
				org = getView().getSchema().getRg("", 1006);
				col.getCell("stmd_org").set(org);
			}
			*/
			args.add(col.getCell("stmd_tdtype").getString());
			args.add(col.getCellInt("stmd_qorg"));
			args.add(col.getCell("stmd_org").getInt());
			args.add(col.getCell("stmd_irg").getInt());
			args.add(col.getCellInt("stmd_qirg"));
			Cell c0 = null;
			c0 = col.testCell("stmd_loc");
			if(c0 != null) args.add(c0.getString()); else args.add("");
			c0 = col.testCell("stmd_bin");
			if(c0 != null) args.add(c0.getString()); else args.add("");
			c0 = col.testCell("stmd_ref4");
			if(c0 != null) args.add(c0.getString()); else args.add("");
			args.add(col.getCell("stmd_qty").getDouble());
			c0 = col.testCell("stmd_xqty");
			if(c0 != null) args.add(c0.getDouble()); else args.add(0.0d);
			// hardcode to check whether stmdki exists, should generalized to able defined multiple set of stmovd per row in initialization stage , 2020/09/09
			/*
			if(col.testCell("stmdki_mrg") != null) {
				col.getCell("stmdki_tdindex").set(n);
				org = col.getCell("stmdki_org").getInt();
				args.add(col.getCell("stmdki_tdtype").getString());
				args.add(col.getCell("stmdki_qorg").getInt());
				args.add(col.getCell("stmdki_org").getInt());
				args.add(col.getCell("stmdki_irg").getInt());
				args.add(col.getCell("stmdki_qirg").getInt());
				c0 = null;
				c0 = col.testCell("stmdki_loc");
				if(c0 != null) args.add(c0.getString()); else args.add("");
				c0 = col.testCell("stmdki_bin");
				if(c0 != null) args.add(c0.getString()); else args.add("");
				c0 = col.testCell("stmdki_ref4");
				if(c0 != null) args.add(c0.getString()); else args.add("");
				args.add(col.getCell("stmdki_qty").getDouble());
			}
			*/
			for(String stmdPrefix : extraStmds) {
				if(col.testCell(stmdPrefix+"_mrg") != null) {
					col.getCell(stmdPrefix+"_tdindex").set(n);
					org = col.getCell(stmdPrefix+"_org").getInt();
					args.add(col.getCell(stmdPrefix+"_tdtype").getString());
//					args.add(col.getCell(stmdPrefix+"_qorg").getInt());
					args.add(col.getCellInt(stmdPrefix+"_qorg"));
					args.add(col.getCell(stmdPrefix+"_org").getInt());
					args.add(col.getCell(stmdPrefix+"_irg").getInt());
					args.add(col.getCellInt(stmdPrefix+"_qirg"));
					c0 = null;
					c0 = col.testCell(stmdPrefix+"_loc");
					if(c0 != null) args.add(c0.getString()); else args.add("");
					c0 = col.testCell(stmdPrefix+"_bin");
					if(c0 != null) args.add(c0.getString()); else args.add("");
					c0 = col.testCell(stmdPrefix+"_ref4");
					if(c0 != null) args.add(c0.getString()); else args.add("");
					args.add(col.getCell(stmdPrefix+"_qty").getDouble());
					c0 = col.testCell(stmdPrefix+"_xqty");
					if(c0 != null) args.add(c0.getDouble()); else args.add(0.0d);
				}
			}
			n++;
		}
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,"Update Failed"));
		}
  		Value val = rpc.callSegment(
				"erpv4UpdateStmdGenBucket",
				args
			);
	
  		if(val != null && val.toString().startsWith("OK")) {
  			val = rpc.callSegment(
				"erpv4GenbucketCommit", new Vector()
			);
  			if(val != null && val.toString().startsWith("OK")) {
  				String s = StringUtil.strpart(val.toString(), 4 , -1);
  				if(!s.trim().equals("")) {
		  				String allowNegativeStock = Erpv4Config.getString(getSessionHelper(), "AllowNegativeStock");

		  				if(allowNegativeStock != null && allowNegativeStock.equals("Y")) {
		  						return(ReturnMsg.defaultOk);
		  				} 
  					return(GenbucketUtil.qoGenBucketCheckResult(s,sh));
  				} else return(null);
  			} 
  		}
  		if(val == null) {
  			UniLog.log("stmd commit fatal error : got null");
  			
  			msg = new ReturnMsg(false, "Confirm Failed : Unknown Reason");
  		} else {
  			msg = new ReturnMsg(false, "Confirm Failed : " + val.toString());
  			UniLog.log("stmd commit fatal error : got "+val.toString());
  		}
  		msg.setFatal(true);
  		return(msg);
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			pcol.getCell("stm_cuser").set(su.getLoginId());
			pcol.getCell("stm_cdate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		calAmount();
		calGrossWeight();
		if(updateCostTable) {
			affectedIrg = new HashSet<String>(); 
		}
		return(updateBucket(pcol));
	}
	

	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		calAmount();
		calGrossWeight();
		if(updateCostTable)  {
		try {
			affectedIrg = new HashSet<String>(); 
			setAffectedIrgFromDatabase();
		} catch (Exception ex) {
			UniLog.log(ex);
			rtnMsg = new ReturnMsg(false,"select stmovd error");
			rtnMsg.setFatal(true);
			return(rtnMsg);
		}
		}
		return(updateBucket(pcol));
	}
	
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtnMsg = super.biAfterAddUpdateCurrent(col,isUpdate);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		RpcClient rpc = getSelectUtil().getRpcClient();
   		Vector args = new Vector();
   		args.add(col.getCell("stm_type")) ;
   		args.add(col.getCell("stm_mrg")) ;
  		Value val = rpc.callSegment(
				"erpv4RegenStmdInv",
				args
			);
  		if(val == null || !val.toString().startsWith("OK")) {
  			return( new ReturnMsg(false, "RegenInv Failed" + (val == null ? "" : val.toString()),true));
  		}
		if(updateCostTable) {
			setAffectedIrgFromCurrentRec();
			clearAffectedCostTable();
		}
		return(ReturnMsg.defaultOk);
	}

	public String getStmdLinkName() {
		return(stmdLinkName);
	}

	
	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		if("Y".equals(Erpv4Config.getString(getSessionHelper(),p_cc.getCellString("stm_type")+"RequireApproval"))) {
			if(p_cc.getCell("stm_status").equals("Confirmed")) {
				try {
					if(!BiSchema.hasAccessRight(getSessionHelper(), "#cfm"+p_cc.getCellString("stm_type"))) {
						((BiCellCollection) p_cc).lock();
					}
				} catch(CellException cex) {
					UniLog.log(cex);
				}
			}
		}
	}	
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(queryAllCompany == true) return(ht);
		if(Erpv4Config.isMultiCompany(sh)) {
		BiColumn locCol = getColumnByLabel("stm_cocode");
		if(locCol != null && columnInSelectList(locCol)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and " + getView().getTable().getName() + "." + "stm_cocode = '"+cocode+"' ").stripAnd();
			if(Erpv4Config.isMultiStockLoc(sh)) {
				wcl1.appendString(" and fromloc.loc_mrg = " + Erpv4Config.getDefaultLcrg(sh)).stripAnd();
				if(ht == null) ht=new HashSet<BiTable>();
				ht.add(getView().getSchema().getTable("fromloc"));
			}
			p_where.andWherecl(wcl1);
		}
		}
		return(ht);
	}	
	protected TableRec getLookupTabTrXX(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(Erpv4Config.isMultiCompany(sh)) {
			if(!allowCrossCompanyLoc()) {
				int mrg = Erpv4Config.getDefaultLcrg(sh);
				if(Erpv4Config.isMultiStockLoc(sh) && !allowCrossLocation()) {
					if(p_lookupTable.getName().equals("toloc")) {
						/*XX1*/
						if(wcl == null ) wcl = new Wherecl();
						String cocode = Erpv4Config.getDefaultCoCode(sh);
//						wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " or toloc.loc_transit = 'Y') ").stripAnd();
						wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and ( (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_transit = 'Y') ) ").stripAnd();
					}
					if(p_lookupTable.getName().equals("fromloc")) {
						/*XX2*/
						if(wcl == null ) wcl = new Wherecl();
						String cocode = Erpv4Config.getDefaultCoCode(sh);
						wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and fromloc.loc_tfronly <> 'Y'").stripAnd();
					}
				} else {
					if(mrg != 0) {
						if(p_lookupTable.getName().equals("toloc")) {
							/*XX3*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' and toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh)).stripAnd();
						}
						if(p_lookupTable.getName().equals("fromloc")) {
							/*XX4*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg ="+Erpv4Config.getDefaultLcrg(sh)).stripAnd();
						}
					} else {
						if(p_lookupTable.getName().equals("toloc")) {
							/*XX5*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' ").stripAnd();
						}
						if(p_lookupTable.getName().equals("fromloc")) {
							/*XX6*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' ").stripAnd();
						}
					}
				}
			}
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(Erpv4Config.isMultiCompany(sh)) {
			if(p_lookupTable.getName().equals("toloc")) {
				if(Erpv4Config.isMultiStockLoc(sh) && !allowCrossLocation()) {
					/*XX1*/
//					if(wcl == null ) wcl = new Wherecl();
//					String cocode = Erpv4Config.getDefaultCoCode(sh);
//					//wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " or toloc.loc_transit = 'Y') ").stripAnd();
//					if(allowCrossCompanyLoc()) {
//						wcl.appendString(" and toloc.loc_cocode <> '"+cocode+"' " + " and ((toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_transit = 'Y') or ( toloc.loc_mrg = " + Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_transit <> 'Y' ) ) ").stripAnd();
//					} else {
//						//wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and ( (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_transit = 'Y') ) ").stripAnd();
//						if(sh.isAdminUser()) {
//							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and ( (toloc.loc_transit = 'Y') or (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y' and toloc.loc_transit <> 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly <> 'Y') ) ").stripAnd();
//						} else {
//							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and ( (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y' and toloc.loc_transit <> 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly <> 'Y') ) ").stripAnd();
//						}
//					}
				} else {
//					int mrg = Erpv4Config.getDefaultLcrg(sh);
//					if(mrg != 0) {
//						/*XX3*/
//						if(wcl == null ) wcl = new Wherecl();
//						String cocode = Erpv4Config.getDefaultCoCode(sh);
//						wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' and toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh)).stripAnd();
//					} else {
//						/*XX5*/
//						if(wcl == null ) wcl = new Wherecl();
//						String cocode = Erpv4Config.getDefaultCoCode(sh);
//						wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' ").stripAnd();
//					}
				}
			}
			if(p_lookupTable.getName().equals("fromloc")) {
				if(Erpv4Config.isMultiStockLoc(sh) && !allowCrossLocation()) {
					/*XX2*/
//					if(wcl == null ) wcl = new Wherecl();
//					String cocode = Erpv4Config.getDefaultCoCode(sh);
//					wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and fromloc.loc_tfronly <> 'Y'").stripAnd();
				} else {
//					int mrg = Erpv4Config.getDefaultLcrg(sh);
//					if(mrg != 0) {
//						/*XX4*/
//						if(wcl == null ) wcl = new Wherecl();
//						String cocode = Erpv4Config.getDefaultCoCode(sh);
//						wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg ="+Erpv4Config.getDefaultLcrg(sh)).stripAnd();
//					} else {
//						/*XX6*/
//						if(wcl == null ) wcl = new Wherecl();
//						String cocode = Erpv4Config.getDefaultCoCode(sh);
//						wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' ").stripAnd();
//					}
				}
			}
			if(!allowCrossCompanyLoc()) {
				int mrg = Erpv4Config.getDefaultLcrg(sh);
				if(Erpv4Config.isMultiStockLoc(sh) && !allowCrossLocation()) {
					if(p_lookupTable.getName().equals("toloc")) {
						/*XX1*/
						if(wcl == null ) wcl = new Wherecl();
						String cocode = Erpv4Config.getDefaultCoCode(sh);
//						wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " or toloc.loc_transit = 'Y') ").stripAnd();
//						wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and ( (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_transit = 'Y') ) ").stripAnd();
						if(sh.isAdminUser()) {
//							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' " + "and ( (toloc.loc_transit = 'Y') or (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y' and toloc.loc_transit <> 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly <> 'Y') ) ").stripAnd();
//							wcl.appendString(" and (toloc.loc_cocode = '"+cocode+"' or toloc.loc_code = 'HQ01') " + "and ( (toloc.loc_transit = 'Y') or (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'N' ) ) ").stripAnd();
							wcl.appendString(" and toloc.loc_cocode in ('"+cocode+"','001') " + "and ( (toloc.loc_transit = 'Y') or (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'N' ) ) ").stripAnd();
						} else {
							wcl.appendString(" and toloc.loc_cocode in ('"+cocode+"','001') " + "and ( (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y' and toloc.loc_transit = 'N') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'N' and toloc.loc_transit = 'N') ) ").stripAnd();
//							wcl.appendString(" and (toloc.loc_cocode = '"+cocode+"' or toloc.loc_code = 'HQ01') " + "and ( (toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'Y' and toloc.loc_transit = 'N') or (toloc.loc_mrg <> "+Erpv4Config.getDefaultLcrg(sh) + " and toloc.loc_tfronly = 'N' and toloc.loc_transit = 'N') ) ").stripAnd();
						}
					}
					if(p_lookupTable.getName().equals("fromloc")) {
						/*XX2*/
						if(wcl == null ) wcl = new Wherecl();
						String cocode = Erpv4Config.getDefaultCoCode(sh);
						if(sh.isAdminUser()) {
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and (fromloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + "or fromloc.loc_transit = 'Y' )  and fromloc.loc_tfronly = 'N'").stripAnd();
						} else {
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and fromloc.loc_tfronly = 'N'").stripAnd();
						}
					}
				} else {
					if(mrg != 0) {
						if(p_lookupTable.getName().equals("toloc")) {
							/*XX3*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' and toloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh)).stripAnd();
							/*
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' and toloc.loc_mrg <> " + mrg).stripAnd();
							*/
						}
						if(p_lookupTable.getName().equals("fromloc")) {
							/*XX4*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg ="+Erpv4Config.getDefaultLcrg(sh)).stripAnd();
						}
					} else {
						if(p_lookupTable.getName().equals("toloc")) {
							/*XX5*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and toloc.loc_cocode = '"+cocode+"' ").stripAnd();
						}
						if(p_lookupTable.getName().equals("fromloc")) {
							/*XX6*/
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' ").stripAnd();
						}
					}
				}
			} else {
				int mrg = Erpv4Config.getDefaultLcrg(sh);
				if(Erpv4Config.isMultiStockLoc(sh) && !allowCrossLocation()) {
					if(p_lookupTable.getName().equals("toloc")) {
						if(mrg == 64) {
							if(wcl == null ) wcl = new Wherecl();
							/* HAHA Modified */
							wcl.appendString(" and toloc.loc_code in ('HZ01','CTL03','TST06','DVR02','PDHQ','PPDVRT','DSCT1','DSCT2','PO005','NC005','PANA05') ").stripAnd();
						} 
					}
					if(p_lookupTable.getName().equals("fromloc")) {
						if(wcl == null ) wcl = new Wherecl();
						String cocode = Erpv4Config.getDefaultCoCode(sh);
						if(sh.isAdminUser()) {
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and (fromloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + "or fromloc.loc_transit = 'Y' )  and fromloc.loc_tfronly = 'N'").stripAnd();
						} else {
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg = "+Erpv4Config.getDefaultLcrg(sh) + " and fromloc.loc_tfronly = 'N'").stripAnd();
						}
					}
				} else {
					if(mrg != 0) {
						if(p_lookupTable.getName().equals("toloc")) {
						if(mrg == 64) {
							if(wcl == null ) wcl = new Wherecl();
							wcl.appendString(" and toloc.loc_code in ('HZ01','CTL03','TST06','DVR02') ").stripAnd();
						} 
						}
						if(p_lookupTable.getName().equals("fromloc")) {
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' and fromloc.loc_mrg ="+Erpv4Config.getDefaultLcrg(sh)).stripAnd();
						}
					} else {
						if(p_lookupTable.getName().equals("toloc")) {
						if(mrg == 64) {
							if(wcl == null ) wcl = new Wherecl();
							wcl.appendString(" and toloc.loc_code in ('HZ01','CTL03','TST06','DVR02') ").stripAnd();
						} 
						}
						if(p_lookupTable.getName().equals("fromloc")) {
							if(wcl == null ) wcl = new Wherecl();
							String cocode = Erpv4Config.getDefaultCoCode(sh);
							wcl.appendString(" and fromloc.loc_cocode = '"+cocode+"' ").stripAnd();
						}
					}
				}
			}
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	@Override
	protected ReturnMsg biAfterDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biAfterDeleteCurrent(col);
		if(rtn == null || rtn.getStatus()) {
			if(updateCostTable) {
				clearAffectedCostTable();
			}
		}
		return(rtn);
	}
	
	void setAffectedIrgFromDatabase() throws Exception {
			TableRec tr = getSelectUtil().getQueryResult("select stmd_irg,stmd_org from stmovd where stmd_mrg = " + getCellInt("stm_mrg"));
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				affectedIrg.add(CostCalculation.getCostKey(tr.getFieldInt("stmd_irg"),tr.getFieldInt("stmd_org")));
			}
	}
	void setAffectedIrgFromCurrentRec() {
   		BiResult sr = getSubLink(stmdLinkName);
    	Vector<BiCellCollection> v = sr.getRowCollectionList();
    	for(BiCellCollection c : v) {
			affectedIrg.add(CostCalculation.getCostKey(c.getCellInt("stmd_irg"),c.getCellInt("stmd_org")));
			for(String stmdPrefix : extraStmds) {
				if(c.testCell(stmdPrefix+"_mrg") != null) {
					affectedIrg.add(CostCalculation.getCostKey(c.getCellInt(stmdPrefix+"_irg"),c.getCellInt(extraStmds+"_org")));
				}
			}
    	}
	}
	
	
	void clearAffectedCostTable() {

    	try {
    	for(String s : affectedIrg) {
    		CostCalculation.clearCostTable(sh,s);
    	}
    	} catch (Exception ex) {
    		UniLog.log(ex);
    	}
	}

	   public ReturnMsg printVoucher(ByteArrayOutputStream bos) {
//			String paperType;
//			String docCode;
			PrtdocJson ppj;

			if(paperType == null) {
				paperType = Erpv4Config.getString(getSessionHelper(),"MoPaperType");
				if(paperType == null || paperType.trim().equals("")) paperType = "A4P";
			}
			if(docCode == null) {
				docCode = Erpv4Config.getString(getSessionHelper(),"MoDocCode");
				if(docCode == null || docCode .trim().equals("")) docCode = "GENINV01";
			}
			try {
				ppj = PrtdocJson.newPrtdocJson(	
					Erpv4Config.getDefaultCoCode(getSessionHelper()),
				    paperType,
				    docCode,
				    "erpv4_printDocument"
				    ) ;
//					ppj.setTrailerAtLastPageOnly(true);
				ppj.setTrailerAtLastPageOnly(true);
				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
				String module = getCellString("stm_module");
					if(module.equals("cstmo")) {
						ppj.addHeaderField("doctitle","Stock Out");	
						ppj.addHeaderField("cvname","From: " + getCellString("floc_desc"),-2,-40);
						ppj.addHeaderField("cvname",getCellString("vd_vname"),0,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);

						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
						ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
						ppj.addDetailHeaderField("hdr_uprice","("+ getCellString("stm_cur")+")",0,12);
						ppj.addDetailHeaderField("hdr_amount","AMOUNT");
						
					} else if(module.equals("vstmo")) {
						ppj.addHeaderField("cvname","From: " + getCellString("floc_desc"),0,-30);
						ppj.addHeaderField("doctitle","Stock In");	
					} else if(module.equals("stadj")) {
						ppj.addHeaderField("doctitle","Adjustment");	
						ppj.addHeaderField("cvname","" + getCellString("floc_desc"),-2,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);
						
						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
					} else if(module.equals("sttfr")) {
						ppj.addHeaderField("doctitle","Transfer");	
						ppj.addHeaderField("cvname","From: " + getCellString("floc_desc"),-2,-40);
						ppj.addHeaderField("cvname",getCellString("tloc_desc"),0,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);

						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
					}
					BiResult ssr = getSubLink(stmdLinkName);
					Vector<BiCellCollection> v = ssr.getRowCollectionList();
					for(BiCellCollection c : v) {
						ppj.addDetailRecord();
						String s = "";
						s += c.getCellString("st_iname");
						s += "("+c.getCellString("st_icode")+")";
						ppj.addDetailRecordField("description", s);
//    					ppj.setBold(false);
//    					ppj.setUnderLine(false);
						ppj.addDetailRecordField("quantity", c.getCell("stmd_qty").getString());
					}
					
				return(ppj.toPdfStream(bos, getSessionHelper()));
			} catch(Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
	    	
	    }	


    @Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
    	java.util.Date pd = Erpv4Config.stmMinDate(getSessionHelper());
    	java.util.Date stmd = col.getCell("stm_date").getDate();
    	if(!stmd.after(pd) && !getSessionHelper().hasAccessRight("#stmpostall")) {
    		return(new ReturnMsg(false,"Post Date Control Denied"));
    	}
    	{
    		Cell updCnt = getCell("stm_updcnt");
    		if(updCnt != null) { 
    			try {
    				updCnt.set(updCnt.getInt()+1);
    			} catch (CellException cex) {
    				UniLog.log(cex);
    			}
    		}
    		
    	}
    	String ss = Erpv4Config.getString(sh, "RequireLoc") ;
    	if(ss != null && ss.equals("Y")) {
    	if(col.getCellString("stm_status").equals("Confirmed")
    			&& col.getCellString("stm_fromloc").trim().equals("") 
    				&& !col.getCellString("stm_module").equals("stake")
    					&& !col.getCellString("stm_module").equals("stkg2")
    				) {
    		return(new ReturnMsg(false,"From Location is Empty, cannot confirm request"));
    	}
    	}
		return(super.biBeforeAddUpdateCurrent(col, isUpdate));
	}

	protected boolean allowCrossLocation() {
		return(false);
	}
	protected boolean allowCrossCompanyLoc() {
		return(false);
	}
}