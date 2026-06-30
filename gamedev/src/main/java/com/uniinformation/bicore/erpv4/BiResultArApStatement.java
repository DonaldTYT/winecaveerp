package com.uniinformation.bicore.erpv4;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultStockLedger.StockCostCalculator;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;

public class BiResultArApStatement extends BiResultErpv4 {
	
	boolean showOsBalanceOnly=true;

	public BiResultArApStatement(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	public CellValueAction setStmtEdate;
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		BiCellCollection col = super.createColumnCollection(p_parent);
		Cell cc = col.addCell("stmt_sdate", new ColumnCell(DateUtil.zeroDate,Cell.VMODE_NORMAL));
		if(setStmtEdate == null) {
			setStmtEdate = new CellValueAction() {
				@Override
				public void cellAction_onchange(Cell p_value) throws CellException {
					Date sd = p_value.getDate();
					if(sd != null && sd.after(DateUtil.minDate)) {
						Date ed = DateUtil.monthEnd(sd);
						Cell ced = getCell("stmt_edate");
						if(ced != null) ced.set(ed);
					}
				}
				@Override
				public void cellAction_onfree() throws CellException {
				}
			};
		};
		cc.addAction(setStmtEdate);
		col.addCell("stmt_edate", new ColumnCell(DateUtil.zeroDate,Cell.VMODE_NORMAL));
		return(col);
	}
	
	Condition obl = null;
	String queryCond = null;
	String filterStr = null;
	BiResult sihArAp = null;
	BiResult crhArAp = null;
	List<SihRecord> agingList = null;
	HashMap<String,vIndexes> indexHash = null;
	
	public class vIndexes{
		public int agingIdx;
		public int sihIdx;
		public int crdIdx;
		vIndexes() {
			agingIdx = -1;
			sihIdx = -1;
			crdIdx = -1;
		}
	}
	public class SihRecord {
		public String sno;
		public String module;
		public String type;
		public String frxno;
		public String vcode;
		public Date date;
		public Date duedate;
		public double ltotal;
		public double losbal;
	}

//	@Override
//	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
//	{
//		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
//		Date sdate = getCellDate("stmt_sdate");
//		Date edate = getCellDate("stmt_edate");
//		if(sdate != null && edate != null && sdate.after(DateUtil.minDate) && edate.after(sdate)) {
//		} else {
//			Wherecl wcl1 = new Wherecl();
//			wcl1.appendString(" and vd_vcode = ''").stripAnd();
//			p_where.andWherecl(wcl1);
//		}
//		return ht;
//	}
	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag)
	{
		Condition cond = getCustomCondition();
		obl = null;
		if(cond != null) {
			try {
				List<Condition> l1 = Condition.serializeCondition(false, cond);
				for(Condition cd : l1) {
					if(cd.get_isPredicate()) {
						String s= cd.get_leftExpression().toString();
						{
							if(obl != null) {
								obl = new Condition(obl,Condition.LOGIC_OP_AND,cd);
							} else {
								obl = cd;
							}
						}
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		if(obl != null) {
			queryCond = BiCellCollection.translateCond(this.getView(), obl.toString(),this);
		} else queryCond = null;
		return(super.query(p_rollback, p_sortFlag));
	}
	
	void addExtraSnoToAgingHash( Date sdate,HashMap<String,SihRecord> agingHash,HashSet<String> xsno ) {
			sihArAp.clear();
			sihArAp.clearCondition();
			sihArAp.clearOrderBy();

			if(obl != null) sihArAp.addCustomCondition(obl.toString());
			sihArAp.addCustomCondition(
					String.format("sih_date < '%s'", DateUtil.toDateString(sdate, "yyyy/mm/dd")));
			String condStr = null;
			for(String ss : xsno) {
				if(condStr == null) condStr = "sih_sno in('"+ss.trim()+"'";
				else condStr += ",'"+ ss.trim()+"'";
			}
			condStr += ")";
			sihArAp.addCustomCondition(condStr);
			sihArAp.addOrderByColumnList("sih_vcode",false);
			sihArAp.addOrderByColumnList("sih_date",false);
			sihArAp.query();
			UniLog.log("query extra sih got " + sihArAp.getRowCount() + " records");
			condStr = null;
			for(int i=0;i<sihArAp.getRowCount();i++) {
				sihArAp.loadOneRecV(i);
				if(!agingHash.containsKey(sihArAp.getCellString("sih_sno"))) {
					SihRecord sih = new SihRecord();
					sih.sno = sihArAp.getCellString("sih_sno");
					sih.module = sihArAp.getCellString("sih_module");
					sih.type = sihArAp.getCellString("sih_type");
					sih.duedate = sihArAp.getCellDate("sih_duedate");
					sih.frxno = sihArAp.getCellString("sih_frxno");
					sih.date = sihArAp.getCellDate("sih_date");
					sih.vcode = sihArAp.getCellString("sih_vcode");
					sih.ltotal = sihArAp.getCellDouble("sih_ltotal");
					sih.losbal = sihArAp.getCellDouble("sih_losbal");
					agingHash.put(sih.sno, sih);
				}
			}
	}
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		agingList = new ArrayList<SihRecord>();
		Date sdate = getCellDate("stmt_sdate");
		Date edate = getCellDate("stmt_edate");
		indexHash = new HashMap<String,vIndexes>();
		if(sdate != null && edate != null && sdate.after(DateUtil.minDate) && edate.after(sdate)) {
			if(sihArAp == null) {
				sihArAp = getView().getSchema().getViewByName("erpv4.SihArAp").newBiResult(sh.getLoginId(), null, null, sh);
			}
			sihArAp.clear();
			sihArAp.clearCondition();
			sihArAp.clearOrderBy();

			if(obl != null) sihArAp.addCustomCondition(obl.toString());
			sihArAp.addCustomCondition(
					String.format("sih_date < '%s' and sih_losbal <> 0.0", DateUtil.toDateString(sdate, "yyyy/mm/dd")));
			sihArAp.addOrderByColumnList("sih_vcode",false);
			sihArAp.addOrderByColumnList("sih_date",false);
			sihArAp.query();
			UniLog.log("query b/f sih got " + sihArAp.getRowCount() + " records");
			HashMap<String,SihRecord> agingHash = new HashMap<String,SihRecord>();
			for(int i=0;i<sihArAp.getRowCount();i++) {
				sihArAp.loadOneRecV(i);
				SihRecord sih = new SihRecord();
				sih.sno = sihArAp.getCellString("sih_sno");
				sih.module = sihArAp.getCellString("sih_module");
				sih.type = sihArAp.getCellString("sih_type");
				sih.duedate = sihArAp.getCellDate("sih_duedate");
				sih.frxno = sihArAp.getCellString("sih_frxno");
				sih.date = sihArAp.getCellDate("sih_date");
				sih.vcode = sihArAp.getCellString("sih_vcode");
				sih.ltotal = sihArAp.getCellDouble("sih_ltotal");
				sih.losbal = sihArAp.getCellDouble("sih_losbal");
				agingHash.put(sih.sno, sih);
			}
			if(crhArAp == null) {
				crhArAp = getView().getSchema().getViewByName("erpv4.CrdSno").newBiResult(sh.getLoginId(), null, null, sh);
			}
			crhArAp.clear();
			crhArAp.clearCondition();
			crhArAp.clearOrderBy();
			if(obl != null) crhArAp.addCustomCondition(obl.toString());
			if(showOsBalanceOnly) {
				crhArAp.addCustomCondition(
					String.format("crh_date > '%s'",
							DateUtil.toDateString(edate, "yyyy/mm/dd"))
					);
			} else {
			crhArAp.addCustomCondition(
					String.format("crh_date >= '%s'",
							DateUtil.toDateString(sdate, "yyyy/mm/dd"))
					);
			crhArAp.addCustomCondition(
					String.format("(sih_date <= '%s' or crh_date <= '%s')",
							DateUtil.toDateString(edate, "yyyy/mm/dd"),
							DateUtil.toDateString(edate, "yyyy/mm/dd")
							)
					);
			}
			crhArAp.addOrderByColumnList("vd_vcode",false);
			crhArAp.addOrderByColumnList("crh_date",false);
			crhArAp.query();
			UniLog.log("query crh got " + crhArAp.getRowCount() + " records");

			HashSet<String> xsno = new HashSet();
			for(int i=0;i<crhArAp.getRowCount();i++) {
				crhArAp.loadOneRecV(i);
				vIndexes vidx = indexHash.get(crhArAp.getCellString("vd_vcode"));
				if(vidx == null) {
					vidx = new vIndexes();
					indexHash.put(crhArAp.getCellString("vd_vcode"),vidx);
				}
				if(vidx.crdIdx < 0) {
					vidx.crdIdx = i;
				}
				if(!xsno.contains(crhArAp.getCellString("crd_sno"))) {
					Date sihDate = crhArAp.getCellDate("sih_date");
					if(sdate.after(sihDate)) {
						xsno.add(crhArAp.getCellString("crd_sno"));
					} else {
						continue;
					}
				}
				if(xsno.size() > 1500) {
					addExtraSnoToAgingHash(sdate,agingHash,xsno);
					xsno.clear();
				}
			}
			if(xsno.size() > 0) { 
				addExtraSnoToAgingHash(sdate,agingHash,xsno);
			}
			for(int i=0;i<crhArAp.getRowCount();i++) {
				crhArAp.loadOneRecV(i);
				SihRecord sihRec = agingHash.get(crhArAp.getCellString("crd_sno"));
				if(sihRec != null) {
					sihRec.losbal -= crhArAp.getCellDouble("crd_lamount");
				}
			}
			for(SihRecord srec : agingHash.values()) {
				agingList.add(srec);
			}
			agingList.sort(new Comparator() {

				@Override
				public int compare(Object arg0, Object arg1) {
					// TODO Auto-generated method stub
					SihRecord sih0 = (SihRecord) arg0;
					SihRecord sih1 = (SihRecord) arg1;
					int cc = sih0.vcode.compareTo(sih1.vcode);
					if(cc != 0) return(cc);
					cc = sih0.date.compareTo(sih1.date);
					if(cc != 0) return(cc);
					return 0;
				}
				
			});
			for(int i=0;i<agingList.size();i++) {
				SihRecord sihRec = agingList.get(i);
				vIndexes vidx = indexHash.get(sihRec.vcode);
				if(vidx == null) {
					vidx = new vIndexes();
					indexHash.put(sihRec.vcode,vidx);
				}
				if(vidx.agingIdx < 0) {
					vidx.agingIdx = i;
				}
				
			}
			
			sihArAp.clear();
			sihArAp.clearCondition();
			sihArAp.clearOrderBy();

//			Wherecl unionwcl = new Wherecl();
//			unionwcl.appendString(String.format(" sih_sno in (select crd_sno from crh,crd,vendor where crh_date >= '%s' and crh_cocode = '001' and crd_crno = crh_crno and vd_vcode = crh_vcode)",
//							DateUtil.toDateString(sdate, "yyyy/mm/dd")));
//			sihArAp.unionWherecl(unionwcl);
			
			if(obl != null) sihArAp.addCustomCondition(obl.toString());
			sihArAp.addCustomCondition(
					String.format("sih_date between '%s' and '%s'",
							DateUtil.toDateString(sdate, "yyyy/mm/dd"),
							DateUtil.toDateString(edate, "yyyy/mm/dd"))
					);
			sihArAp.addOrderByColumnList("sih_vcode",false);
			sihArAp.addOrderByColumnList("sih_date",false);
			sihArAp.query();
			UniLog.log("query sih got " + sihArAp.getRowCount() + " records");
			HashMap<String,Integer> sihHash = new HashMap<String,Integer>();
			for(int i=0;i<sihArAp.getRowCount();i++) {
				sihArAp.loadOneRecV(i);
				sihHash.put(sihArAp.getCellString("sih_sno"), i);
				vIndexes vidx = indexHash.get(sihArAp.getCellString("sih_vcode"));
				if(vidx == null) {
					vidx = new vIndexes();
					indexHash.put(sihArAp.getCellString("sih_vcode"),vidx);
				}
				if(vidx.sihIdx < 0) {
					vidx.sihIdx = i;
				}
			}
//			int losPos = sihArAp.getSelectFieldPosition( sihArAp.getView().getColumnByLabel("sih_losbal"));
			for(int i=0;i<crhArAp.getRowCount();i++) {
				crhArAp.loadOneRecV(i);
				Integer sihIdx = sihHash.get(crhArAp.getCellString("crd_sno"));
				if(sihIdx != null) {
					sihArAp.loadOneRecV(sihIdx);
					Cell cc = sihArAp.getCell("sih_losbal");
					double losbal = cc.getDouble();
					losbal -= crhArAp.getCellDouble("crd_lamount");
					try {
						cc.set(losbal);;
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					sihArAp.saveOneRecV(sihIdx);
				}
			}
			int bfPos = getSelectFieldPosition( getView().getColumnByLabel("stmt_bf"));
			int billPos = getSelectFieldPosition( getView().getColumnByLabel("stmt_bill"));
			int paidPos = getSelectFieldPosition( getView().getColumnByLabel("stmt_paid"));
			for(int i=0;i<getTableRecCount();i++) {
				loadOneRec(i,getDefaultRowCollection(),false);
				String vcode = getCellString("vd_vcode");
				vIndexes vIdx = indexHash.get(vcode);
				if(vIdx != null && vIdx.agingIdx >= 0) {
					SihRecord srec;
					try {
						double bf = (Double) getResultTrObject(false,bfPos,i);
						for(int j=vIdx.agingIdx;j < agingList.size();j++) {
							srec = agingList.get(j);
							if(!srec.vcode.equals(vcode)) break;
							bf += srec.losbal;
						}
						saveOneObjectToResultTr(i,bfPos,bf);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				if(vIdx != null && vIdx.sihIdx >= 0) {
					try {
						double bill = (Double) getResultTrObject(false,billPos,i);
						for(int j=vIdx.sihIdx;j < sihArAp.getRowCount();j++) {
							sihArAp.loadOneRecV(j);
							if(!sihArAp.getCellString("sih_vcode").equals(vcode)) break;
							bill += sihArAp.getCellDouble("sih_losbal");
						}
						saveOneObjectToResultTr(i,billPos,bill);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				if(vIdx != null && vIdx.crdIdx >= 0) {
					try {
						double paid = (Double) getResultTrObject(false,paidPos,i);
						for(int j=vIdx.crdIdx;j < crhArAp.getRowCount();j++) {
							crhArAp.loadOneRecV(j);
							if(!crhArAp.getCellString("vd_vcode").equals(vcode)) break;
							Date d = crhArAp.getCellDate("crh_date");
							if(d.after(edate)) break;
							paid -= crhArAp.getCellDouble("crd_lamount");
						}
						saveOneObjectToResultTr(i,paidPos,paid);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
			
		}
		invalidateLoadCache();
		return(ReturnMsg.defaultOk);
	}
	
	public vIndexes getVindexes(String vcode) {
		if(indexHash == null) return(null);
		return(indexHash.get(vcode));
	}
	/*
	public int getAgingIdx(String vCode) {
		if(indexHash == null) return(-1);
		vIndexes vidx = indexHash.get(vCode);
		if(vidx == null) return(-1);
		return(vidx.agingIdx);
	}
	public int getCrdIdx(String vCode) {
		if(indexHash == null) return(-1);
		vIndexes vidx = indexHash.get(vCode);
		if(vidx == null) return(-1);
		return(vidx.crdIdx);
	}
	public int getSihIdx(String vCode) {
		if(indexHash == null) return(-1);
		vIndexes vidx = indexHash.get(vCode);
		if(vidx == null) return(-1);
		return(vidx.sihIdx);
	}
	*/
	@Override
	protected Condition addExtraNullCond(Condition p_nullCond) {
		Condition nullCond = super.addExtraNullCond(p_nullCond);
		ReturnMsg rtn = addCustomCondition("stmt_bf <> 0.0 or stmt_cf <> 0.0", true);
		if(rtn.getStatus()) {
			if (nullCond == null) {
				nullCond = ((Condition) rtn.getData());
			} else {
				try {
					nullCond = new Condition(nullCond,Condition.LOGIC_OP_AND,((Condition) rtn.getData()));
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
		return(nullCond);
	}
	public BiCellCollection getSihRec(int p_idx) {
		if(p_idx < 0 || p_idx >= sihArAp.getRowCount()) return(null);
		sihArAp.loadOneRecV(p_idx);
		return(sihArAp.getCurrentCollection());
	}
	public BiCellCollection getCrdRec(int p_idx) {
		if(p_idx < 0 || p_idx >= crhArAp.getRowCount()) return(null);
		crhArAp.loadOneRecV(p_idx);
		return(crhArAp.getCurrentCollection());
	}
	
	public SihRecord getAgingSih(int p_idx) {
		if(agingList == null) return(null);
		if(p_idx >= agingList.size()) return(null);
		return(agingList.get(p_idx));
	}
	
}
