package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
//import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class BiResultQuoDet extends BiResultErpv4 {
	
	static public enum DELTATYPE {
		DELTALTYPE_STOCK_ITEM,
		DELTALTYPE_SERVICE_ITEM,
		DELTALTYPE_DESCRIPTION,
		DELTALTYPE_HEADER,
		DELTALTYPE_COMBO_ITEM,
		DELTALTYPE_LINEBREAK,
		DELTALTYPE_PAGEBREAK,
		DELTALTYPE_DELIVERY,
		DELTALTYPE_TRADEIN,
		DELTALTYPE_PRINTING_ITEM,
		DELTALTYPE_UNKNOWN
	};		
	
	static private boolean useOldPdsrgMapping(SessionHelper p_sh) {
		RpcClient rpc = p_sh.getRpcClient();
		if(rpc == null) return(false);
		try {
			Value v = rpc.callSegment("erpv4_getPdsrgVersion");
			if(v != null && v.toString().equals("OK  0")) {
				return(true);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		} finally {
			rpc.close();
		}
		return(false);
	}

	static private void createDeltaTypeHash(SessionHelper p_sh) {
		Hashtable<Integer,DELTATYPE> ht0 = new Hashtable<Integer,DELTATYPE> ();
		Hashtable<DELTATYPE,Integer> ht1 = new Hashtable<DELTATYPE,Integer> ();
		if(!useOldPdsrgMapping(p_sh)) {
			ht0.put(1,DELTATYPE.DELTALTYPE_STOCK_ITEM);
			ht0.put(2,DELTATYPE.DELTALTYPE_SERVICE_ITEM);
			ht0.put(3,DELTATYPE.DELTALTYPE_DESCRIPTION);
			ht0.put(5,DELTATYPE.DELTALTYPE_COMBO_ITEM);
			ht0.put(6,DELTATYPE.DELTALTYPE_LINEBREAK);
			ht0.put(9,DELTATYPE.DELTALTYPE_TRADEIN);
			ht0.put(102,DELTATYPE.DELTALTYPE_PRINTING_ITEM);

			ht1.put(DELTATYPE.DELTALTYPE_STOCK_ITEM,1);
			ht1.put(DELTATYPE.DELTALTYPE_SERVICE_ITEM,2);
			ht1.put(DELTATYPE.DELTALTYPE_DESCRIPTION,3);
			ht1.put(DELTATYPE.DELTALTYPE_COMBO_ITEM,5);
			ht1.put(DELTATYPE.DELTALTYPE_LINEBREAK,6);
			ht1.put(DELTATYPE.DELTALTYPE_TRADEIN,9);
			ht1.put(DELTATYPE.DELTALTYPE_PRINTING_ITEM,102);
		} else {
			ht0.put(5,DELTATYPE.DELTALTYPE_STOCK_ITEM);
			ht0.put(8,DELTATYPE.DELTALTYPE_SERVICE_ITEM);
			ht0.put(0,DELTATYPE.DELTALTYPE_DESCRIPTION);
			ht0.put(1,DELTATYPE.DELTALTYPE_COMBO_ITEM);

			ht1.put(DELTATYPE.DELTALTYPE_STOCK_ITEM,5);
			ht1.put(DELTATYPE.DELTALTYPE_SERVICE_ITEM,8);
			ht1.put(DELTATYPE.DELTALTYPE_DESCRIPTION,0);
			ht1.put(DELTATYPE.DELTALTYPE_COMBO_ITEM,1);
		}
		p_sh.putSessionData("pdsToDeltaTypeHash", ht0);
		p_sh.putSessionData("deltaTypeToPdsHash", ht1);
	}
	
//	static private void getDeltaTypeMapping(SessionHelper p_sh) {
//		RpcClient rpc = p_sh.getRpcClient();
//		if(rpc == null) return;
//		try {
//		Value v = rpc.callSegment("erpv4_getPdsrgVersion");
//		} catch (Exception ex) {
//			
//		} finally {
//			rpc.close();
//		}
//	}
	
	static public DELTATYPE getDeltaType(SessionHelper p_sh,int p_pdsrg) {
		return(getDeltaType_new(p_sh,p_pdsrg));
	}
	static private DELTATYPE getDeltaType_new(SessionHelper p_sh,int p_pdsrg) {
		Hashtable<Integer,DELTATYPE> ht0 = (Hashtable<Integer,DELTATYPE>) p_sh.getSessionData("pdsToDeltaTypeHash");
		if(ht0 == null) {
			createDeltaTypeHash(p_sh);
			ht0 = (Hashtable<Integer,DELTATYPE>) p_sh.getSessionData("pdsToDeltaTypeHash");
		}
		if(ht0 != null) {
			return(ht0.get(p_pdsrg));
		}
		return(DELTATYPE.DELTALTYPE_UNKNOWN);
		/*
		switch (p_pdsrg) {
		case 1 : return(DELTATYPE.DELTALTYPE_STOCK_ITEM);
		case 2 : return(DELTATYPE.DELTALTYPE_SERVICE_ITEM);
		case 3 : return(DELTATYPE.DELTALTYPE_DESCRIPTION);
		case 5 : return(DELTATYPE.DELTALTYPE_COMBO_ITEM);
		case 6 : return(DELTATYPE.DELTALTYPE_LINEBREAK);
		case 9 : return(DELTATYPE.DELTALTYPE_TRADEIN);
		case 102 : return(DELTATYPE.DELTALTYPE_PRINTING_ITEM);
		default: return(DELTATYPE.DELTALTYPE_UNKNOWN);
		}
		*/
	};
	static public int getPdsrg(SessionHelper p_sh,DELTATYPE p_deltaType) {
		return(getPdsrg_new(p_sh,p_deltaType));
	}
	static private int getPdsrg_new(SessionHelper p_sh,DELTATYPE p_deltaType) {
		/*
		switch (p_deltaType) {
		case DELTALTYPE_STOCK_ITEM : return(1);
		case DELTALTYPE_SERVICE_ITEM : return(2);
		case DELTALTYPE_DESCRIPTION : return(3);
		case DELTALTYPE_COMBO_ITEM : return(5);
		case DELTALTYPE_LINEBREAK : return(6);
		case DELTALTYPE_TRADEIN : return(9);
		case DELTALTYPE_PRINTING_ITEM : return(102);
		default: return(-1);
		}
		*/
		Hashtable<DELTATYPE,Integer> ht1 = (Hashtable<DELTATYPE,Integer>) p_sh.getSessionData("deltaTypeToPdsHash");
		if(ht1 == null) {
			createDeltaTypeHash(p_sh);
			ht1 = (Hashtable<DELTATYPE,Integer>) p_sh.getSessionData("deltaTypeToPdsHash");
		}
		if(ht1 != null) {
			return(ht1.get(p_deltaType));
		}
		return(-1);
	};
		/*
	static public final int DELTALTYPE_STOCK_ITEM = 1;
	static public final int DELTALTYPE_SERVICE_ITEM = 2;
	static public final int DELTALTYPE_DESCRIPTION = 3;
	static public final int DELTALTYPE_HEADER = 4;
	static public final int DELTALTYPE_COMBO_ITEM = 5;
	static public final int DELTALTYPE_LINEBREAK = 6;
	static public final int DELTALTYPE_PAGEBREAK = 7;
	static public final int DELTALTYPE_DELIVERY = 8;
	static public final int DELTALTYPE_TRADEIN = 9;
	static public final int DELTALTYPE_PRINTING_ITEM = 102;
		*/
		
	
/*	
	static public final int DELTALTYPE_STOCK_ITEM = 1;
	static public final int DELTALTYPE_SERVICE_ITEM = 2;
	static public final int DELTALTYPE_DESCRIPTION = 3;
	static public final int DELTALTYPE_HEADER = 4;
	static public final int DELTALTYPE_COMBO_ITEM = 5;
	static public final int DELTALTYPE_LINEBREAK = 6;
	static public final int DELTALTYPE_PAGEBREAK = 7;
	static public final int DELTALTYPE_DELIVERY = 8;
	static public final int DELTALTYPE_TRADEIN = 9;
	static public final int DELTALTYPE_PRINTING_ITEM = 102;
	*/
	public BiResultQuoDet(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
	}
	
	@Override
	protected ReturnMsg validateOneRow(CellCollection col,boolean isUpdate) {
		if(getDeltaType(sh,col.getCell("ind_pdsrg").getInt()) == DELTATYPE.DELTALTYPE_UNKNOWN) {
			return(new ReturnMsg(false,"Mandatory  Field [" + "Item Type"+ "] is Empty"));	
		}
		ReturnMsg rtnMsg = super.validateOneRow(col,isUpdate);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(getDeltaType(sh,col.getCell("ind_pdsrg").getInt()) == DELTATYPE.DELTALTYPE_STOCK_ITEM) {
			if(col.getCell("st_icode").isBlank()) {
				return(new ReturnMsg(false,"Mandatory  Field [" + "Item Code"+ "] is Empty"));	
			}
		}
		return(rtnMsg);
	}
	
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		BiResultQuotation brQuotation = (BiResultQuotation) getParent();
//		//biStmovOm.setCellActionCalTotalAmount( (ColumnCell) col.getCell("stmd_exprice") );
		brQuotation.setCellActionCalTotalAmount((ColumnCell) (col.getCell("ind_amount") ));
		if(brQuotation.isModeG2()) {
			brQuotation.setCellActionCalTotalAmount((ColumnCell) (col.getCell("ind_pdsrg") ));
			brQuotation.setCellActionCalTotalAmount((ColumnCell) (col.getCell("ind_subitem") ));
		} else {
			brQuotation.setCellActionCalTotalAmount((ColumnCell) (col.getCell("ind_srg") ));
			UniLog.log("setCalTotalAmount for ind_srg");
		}
	}
	
	@Override
	protected void afterLoadCollection(boolean p_isfetch,BiCellCollection col)
	{
			try {
				if(!col.getCell("inv_quostatus").equals("Confirmed")) {
					/*
					col.getCell("qdst_status").set("");
					if(col.getCell("stg_freestock").getDouble() + col.getCell("inv_resqty").getDouble() > col.getCell("ind_stqty").getDouble()) {
						col.getCell("qdst_status").set("In Stock");
					} else {
						col.getCell("qdst_status").set("Back Order");
					}
					*/
				} else {
					if(col.testCell("qdst_status")!= null) {
					TableRec tr = su.getQueryResult("select * from qodetstatus where qdst_qorg = " 
							+ col.getCell("ind_odrg").getInt() + " and qdst_qirg = " + col.getCell("ind_irg").getInt()
							,null);
					if(tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
					if(tr.getFieldDouble("qdst_actdelqty") >= col.getCell("ind_stqty").getDouble()) {
						col.getCell("qdst_status").set("Delivered");
					} else if(tr.getFieldDouble("qdst_ostqty") >= 0.001) {
						col.getCell("qdst_status").set("Pending for Sale");
					} else if(tr.getFieldDouble("qdst_alcqty") >= 0.001) {
						col.getCell("qdst_status").set("Pending for Receive");
					} else if(tr.getFieldDouble("qdst_delqty") >= 0.001) {
						col.getCell("qdst_status").set("Pending for Deliver");
					}
					}
					/*
					if(col.getCell("qdst_actdelqty").getDouble() >= col.getCell("ind_stqty").getDouble()) {
						col.getCell("qdst_status").set("Delivered");
					} else if(col.getCell("qdst_ostqty").getDouble() >= 0) {
						col.getCell("qdst_status").set("Outstanding");
					} else if(col.getCell("qdst_alcqty").getDouble() >= 0) {
						col.getCell("qdst_status").set("On Order");
					} else if(col.getCell("qdst_delqty").getDouble() >= 0) {
						col.getCell("qdst_status").set("Ready to Deliver");
					}
					*/
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
	}
	
	ReturnMsg updateBucket(CellCollection pcol,String subLinkId) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Value val = null;

		Vector args = new Vector();
		args.add(pcol.getCell("ind_rg").getInt());
		args.add(pcol.getCell("ind_odrg").getInt());
		args.add(pcol.getCell("ind_irg").getInt());
		Vector <BiCellCollection> recs = getSubLinkResult(subLinkId);
		double d = 0;
		int n=0;
		try {
			for(CellCollection col:recs) {
				col.getCell("stmd_tdindex").set(n);
				args.add("SI");
				args.add(col.getCell("stmd_org").getInt());
				args.add(col.getCell("stmd_irg").getInt());
				if(col.testCell("stmd_loc") != null) {
					args.add(col.getCell("stmd_loc").getString());
				} else {
					args.add("");
				}
				args.add(col.getCell("stmd_qty").getDouble());
				n++;
			}
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,"Update Failed"));
		}
		val = rpc.callSegment(
				"erpv4UpdateQuodetStmdGenBucket",
				args
				);

		if(val != null && val.toString().startsWith("OK")) {
//			val = rpc.callSegment(
//					"erpv4GenbucketCommit", new Vector()
//					);
//			if(val != null && val.toString().startsWith("OK")) {
//				String s = StringUtil.strpart(val.toString(), 4 , -1);
//				if(!s.trim().equals("")) {
//					return(GenbucketUtil.qoGenBucketCheckResult(s));
//				} else return(null);
//			} 
			return(null);
		}
		if(val == null) {
			return(new ReturnMsg(false, "Confirm Failed : Unknown Reason"));
		} else {
			return(new ReturnMsg(false, "Confirm Failed : " + val.toString().substring(4)));
		}
	}
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection p_col) {
		Vector<BiResult> v = getSubLinks();
		if(v != null) {
			for(BiResult sr: v) {
				if(sr.getView().getTable().getName().equals("stmovd_si")) {
					Vector recs = sr.getRowCollectionList();
					for(int i=0;i<sr.getRowCount();i++) {
						sr.markDelete(sr.getTrStatObj(i),true);
					}
					ReturnMsg rtnmsg = updateBucket(p_col,sr.getView().getName());
					return(rtnmsg);
				}
			}
		}
		return(new ReturnMsg(true));  //andrew190218:better not return null
	}
	

	ReturnMsg checkAndAddAllocation(CellCollection p_col) {
		Vector<BiResult> v = getSubLinks();
		if(v != null) {
			for(BiResult sr: v) {
				if(sr.getView().getTable().getName().equals("stmovd_si")) {
					try {
					Vector recs = sr.getRowCollectionList();
					if(p_col.getCell("ind_odrg").getInt() <= 0 || p_col.getCell("ind_irg").getInt() <= 0 || (!p_col.getCell("inv_quostatus").getString().equals("Confirmed"))) {
						for(int i=0;i<sr.getRowCount();i++) {
							sr.markDelete(sr.getTrStatObj(i),true);
						}
					} else {
//						if(!getCell("st_policy").getBoolean()) {
						if(!p_col.getCellBoolean("st_fserial") && !p_col.getCellBoolean("ind_instock")) {
//						if(false) {
						CellCollection scol=null;
						if(sr.getRowCount() <= 0) {
							scol = sr.newRowCollection();
							ReturnMsg rtn = sr.addSubRecord(scol, 0,"");
							Object tr = rtn.getData();
						} else {
							if(sr.isMarkedDelete(sr.getTrStatObj(0))) {
								sr.markDelete(sr.getTrStatObj(0),false);
							}
							for(int i = 1;i<sr.getRowCount();i++) {
								/*
								 * 2022/03/28, fixed  suspesious bug.
								 * not verified as that is only test to work on erpv4 system that stock will only have one org (200000001) 
								sr.markDelete(sr.getTrStatObj(0),true);
								 */
								sr.markDelete(sr.getTrStatObj(i),true);
							}
							scol = sr.getRowCollectionV(0);
						}
						scol.getCell("stmd_mrg").set(p_col.getCell("ind_rg").getInt());
						scol.getCell("stmd_qorg").set(p_col.getCell("ind_odrg").getInt());
						scol.getCell("stmd_irg").set(p_col.getCell("ind_irg").getInt());
						scol.getCell("stmd_qirg").set(p_col.getCell("ind_irg").getInt());
						scol.getCell("stmd_qty").set(p_col.getCell("ind_stqty").getDouble());
						scol.getCell("stmd_tdtype").set("SI");
						scol.getCell("stmd_tdindex").set(0);
						scol.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(sh, p_col.getCellString("inv_cocode")));
						if(p_col.testCell("inv_loc") != null && scol.testCell("stmd_loc") != null)  {
							scol.getCell("stmd_loc").set(p_col.getCellString("inv_loc"));
						}
						}
					}
					ReturnMsg rtnmsg = updateBucket(p_col,sr.getView().getName());
					return(rtnmsg);
						
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"Error 10101"));
					}
				}
			}
		}
		return(null);
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection p_col) {
		return(checkAndAddAllocation(p_col));
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection p_col)
	{
		return(checkAndAddAllocation(p_col));
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new Erpv4QuoDetCellCollection(p_parent,this));
	}	

	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(Erpv4Config.isMultiCompany(sh)) {
			if(p_col != null && p_lookupTable.getName().equals("stock_gen")) {
				if(wcl == null ) wcl = new Wherecl();
				String cocode = p_col.getCellString("inv_cocode");
				if(!cocode.equals("")) {
					wcl.appendString(" and stg_cocode = '"+cocode+"' ").stripAnd();
				}
			}
		}
		if(p_col != null && p_lookupTable.getName().equals("customerprice")) {
			if(wcl == null ) wcl = new Wherecl();
			wcl.appendString(" and vdpr_vcode = '"+p_col.getCellString("inv_vcode")+"' ").stripAnd();
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(getView().getColumnByLabel("vdpr_irg") != null) {
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and vdpr_vcode = '"+getParent().getCellString("inv_vcode")+"' ").stripAnd();
			p_where.andWherecl(wcl1);
		}
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			if(getView().getColumnByLabel("stg_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stg_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
			}
			return(ht);
		} else return(ht);
	}
}
