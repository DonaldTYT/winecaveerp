package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.accumulator.BaseAccumulator;
import com.uniinformation.accumulator.CalculationErrorException;
//import com.uniinformation.accumulator.CostCalculator;
import com.uniinformation.accumulator.DateAccumulator;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.StockOpening;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.utils.poi.ExcelPoiFormula;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStock extends BiResultErpv4 {
	/*
	static public final String key_Price0 = ".LB_ST_STANDARDPRICE";
	static public final String key_Price1 = ".LB_ST_PRICE1";
	static public final String key_Price2 = ".LB_ST_PRICE2";
	static public final String key_Price3 = ".LB_ST_PRICE3";
	*/
	Boolean useCocodeForStockRg = null;
	BiResult cbr = null;
	HashSet<String> excelFormulaColumns = new HashSet<String>();
	String stmdLinkName = null;
	String stlocLinkName= null;

//	class BalanceAccumulator extends BaseAccumulator {
//
//		public BalanceAccumulator() {
//			super(/*DateUtil.maxDate */Integer.MAX_VALUE);
//		}
//
//		@Override
//		public void saveToCache(Comparable p_date, double p_pAmount, double p_nAmount)
//				throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void deleteFromCache(Comparable p_date) throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public DatedValue getCurrentBalance() throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public List<DatedValue> getDatedValues(Comparable p_datefrom, Comparable p_dateto)
//				throws CalculationErrorException {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	}
//	
//	Hashtable <String,BalanceAccumulator> accuhash = new Hashtable<String,BalanceAccumulator>();

//	Hashtable <String,CostCalculator> costhash = new Hashtable<String,CostCalculator>();
	public BiResultStock(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		for(BiResult sr : getSubLinks()) {
			if(sr.getView().getTable().getName().equals("stmovd_any")) {
				stmdLinkName = sr.getView().getName();
			}
			if(sr.getView().getTable().getName().equals("stloc")) {
				stlocLinkName = sr.getView().getName();
			}
		}
		UniLog.log("BiResultStock Used");
	}
	
	protected void updateIcode(CellCollection col)  throws CellException {
			if(col.getCell("st_icode").getMode() != Cell.VMODE_DISPONLY) return;
			String prefix = "",ss;
			ss = col.getCell("st_mtype").getString();
			if(!ss.trim().equals("")) {
				prefix += ss.trim();
			}
			ss = col.getCell("mt_tpscode").getString();
			if(!ss.trim().equals("")) {
				prefix += ss.trim();
				ss += "-";
			}
			ss = col.getCell("st_mbrand").getString();
			if(!ss.trim().equals("")) {
				prefix += ss.trim();
				ss += "-";
			}
			ss = col.getCell("st_modelno").getString();
			if(!ss.trim().equals("")) {
				prefix += ss.trim();
				ss += "-";
			}
			if(!col.getCell("st_icode").getString().startsWith(prefix)) {
				col.getCell("st_icode").set(
						makeUniqueIcode(
						prefix,
						true,
						1,
						999999,
						30,
						col.getCell("st_irg").getInt(),
						""
						)
				);
			}
	}
	
	protected String makeUniqueIcode(String p_icodex,boolean p_autoidx,int p_stidx,int p_maxidx,int p_maxlen,int p_skipirg,String p_seperator) throws CellException {
		try {
			TableRec tr;
			String sprefix,smin,smax,sfmt;
			String p_icode;
			if(p_icodex == null ) throw new CellException("Generate Icode Error : code is null");
			p_icode = p_icodex.replaceAll("\\s+","");
//			if(p_icode.contains(" "))throw new CellException("Generate Icode Error : '" + p_icode + "' has space");
			if(p_icode.contains("("))throw new CellException("Generate Icode Error : '" + p_icode + "' has bracket");
			if(p_icode.contains(")"))throw new CellException("Generate Icode Error : '" + p_icode + "' has bracket");
			sprefix = p_icode+p_seperator; 
			if(! p_autoidx) {
				tr = su.getQueryResult("select st_irg from stock where st_icode = '" + sprefix + "' and st_irg <> " + p_skipirg,null);
				if(tr.getRecordCount() > 0) {
					throw new CellException("Generate Icode Error : '" + p_icode + "' already exist");
				}
				return(sprefix);
			}
			int digits = (""+p_maxidx).length();
			int imax;
			smin = sprefix+StringUtil.strpart("00000000", 0, digits);
			smax = sprefix+StringUtil.strpart("99999999", 0, digits);
			sfmt =	"%s%0"+digits+"d";
			if(smin.length() > p_maxlen) {
				throw new CellException("Generate Icode Error : '" + p_icode + "' too long");
			}
			tr = su.getQueryResult("select st_icode,st_irg from stock where st_icode between '" + smin + "' and '" + smax + "' and st_irg <> " + p_skipirg + " order by st_icode desc",null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				smax = StringUtil.strpart(tr.getFieldString("st_icode"),sprefix.length(),-1);
				imax = Integer.parseInt(smax) + 1;
			} else {
				imax = p_stidx;
			}
			if(imax > p_maxidx) {
				throw new CellException("Generate Icode Error : '" + p_icode + "' index out of range");
			}
			return(String.format(sfmt, sprefix,imax));
			
				/*
				if(i == 0) s = p_icode; else   {
					if(! p_autoidx) throw new CellException("Generate Icode Error : '" + p_icode + "' already exist");
						else {
							s = p_icode+p_seperator; 
							smin = p_icode+p_seperator; 
						}
				}
				if(s.length() > p_maxlen) throw new CellException("Generate Icode Error : '" + p_icode + "' too long");
				tr = su.getQueryResult("select st_irg from stock where st_icode = '" + s + "' and st_irg <> " + p_skipirg,null);
				if(tr.getRecordCount() == 0) {
					UniLog.log("HAHA 2018 make icode " + s);
					return(s);
				}
				*/
		} catch (Exception ex){
			UniLog.log(ex);
			throw new CellException(ex.toString());
		}
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			updateIcode(col);
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtnMsg);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			updateIcode(col);
			col.getCell("st_cuser").set(su.getLoginId());
			col.getCell("st_uuser").set(su.getLoginId());
			col.getCell("st_cdate").set(new java.util.Date());
			col.getCell("st_udate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtnMsg);
	}

	
	void reloadStockMove() {
		try {
			SelectUtil su = getSelectUtil();
			TableRec tr = getSelectUtil().getQueryResult("select stmd_mrg,stmd_uprice,stmd_cur from stmovd where stmd_tdtype in("+Erpv4Config.STOCKIN_TDtypes+") and stmd_irg = "+ getCell("st_irg").getInt() + " and stmd_uprice > 0 order by stmd_mrg desc", null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				getCell("st_lastpcost").set(
							String.format("%s %.2f", tr.getField("stmd_cur"),tr.getField("stmd_uprice"))
						);
			}
			tr = getSelectUtil().getQueryResult("select inv_cid,ind_odrg,ind_uprice,ind_cid from stmovd,quodet,quotation where stmd_tdtype = 'SO' and stmd_irg = "+ getCell("st_irg").getInt() + " and ind_odrg = stmd_qorg and inv_rg = ind_rg and ind_uprice > 0 order by ind_odrg desc", null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				getCell("st_lastsprice").set(
							String.format("%s %.2f", tr.getField("inv_cid"),tr.getField("ind_uprice"))
						);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		
	}
	@Override
	public boolean fetchOneRecV(int p_recidx) {
		boolean b = super.fetchOneRecV(p_recidx);
		reloadStockMove();
		return(b);
	}
	static public String newStockTakeCode(SelectUtil su,java.util.Date p_date) {
		try {
			String s = null;
			java.util.Date d = p_date;
			String ds = DateUtil.toDateString(d, "yymmdd");
			int nextidx = 1;
			TableRec tr = su.getQueryResult("select stm_ref1 from stmov where stm_ref1 matches '" + "STTK" + ds + "*' order by stm_ref1 desc",null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				s = tr.getField("stm_ref1").toString();
				String ss = StringUtil.strpart(s, 11, -1);
				nextidx = Integer.parseInt(ss) + 1;
			}
			s = String.format("STTK%s-%03d",ds, nextidx);
			return(s);
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(null);
		}
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new Erpv4BaseCellCollection(p_parent,this));
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);	
//		try {
//			BiResultMcType.triggerMunitDUnit(col,"mt_tpsize1","st_munit1","st_dunit1");
//			BiResultMcType.triggerMunitDUnit(col,"mt_tpsize2","st_munit2","st_dunit2");
//			BiResultMcType.triggerMunitDUnit(col,"mt_tpsize3","st_munit3","st_dunit3");
//		} catch (CellException cex) {
//			UniLog.log(cex);
//		}
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection p_col) {
		try {
			TableRec tr = getSelectUtil().getQueryResult("select serial_id from stmovd where stmd_irg = " + p_col.getCell("st_irg").getInt() + " limit 1 ");
			if(tr.getRecordCount() > 0) {
				return (new ReturnMsg(false,"Stock " + p_col.getCell("st_icode").getString() + " already has transaction, cannot delete",true));
			}
		} catch (Exception ex ) {
			UniLog.log(ex);
			return (new ReturnMsg(false,"Fatal Sql Error",true));
		}
		return(null);
	}
	
//	static public Vector getEntryUnits(SelectUtil p_su,int p_irg) {
//		try {
//			TableRec tr = p_su.getQueryResult("select * from stock,mctype "
//					+ "	where st_irg = " + p_irg + " and mt_tpcode = st_msubtype ");
//			if(tr.getRecordCount() > 0) {
//				tr.setRecPointer(0);
//				Vector v = new Vector();
//				v.add(tr.getFieldString("st_unit"));
//				String s = BiResultMcType.getMunit(tr.getFieldString("mt_tpsize1"));
//				if(s != null && !s.equals("")) v.add(s);
//				s = BiResultMcType.getMunit(tr.getFieldString("mt_tpsize2"));
//				if(s != null && !s.equals("")) v.add(s);
//				s = BiResultMcType.getMunit(tr.getFieldString("mt_tpsize3"));
//				if(s != null && !s.equals("")) v.add(s);
//				return(v);
//			}
//			return(null);
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return(null);
//		}
//	}
	
//	static void addOneUnit(Erpv4StockAttribute stattr,String sizeStr,double ratio) {
//		String mu = BiResultMcType.getMunit(sizeStr);
//		if(mu != null && !mu.equals("") && ratio > 0) {
//			String bu = BiResultMcType.getDunit(sizeStr);
//			if(stattr.ratios.get(bu) != null) {
//				double rt = stattr.ratios.get(bu);
//				rt *= ratio;
//				stattr.allUnits.add(mu);
//				stattr.ratios.put(mu, rt);
//			}
//		}
//	}
	
//	static public Erpv4StockAttribute getStockAttribute(Date p_date,SessionHelper p_sh,SelectUtil p_su,int p_irg) {
//		try {
//			TableRec tr;
//			//if(Erpv4Config.isMultiStock(p_sh)) {
//			if(Erpv4Config.isMultiCompany(p_sh)) {
//				String cocode = Erpv4Config.getDefaultCoCode(p_sh);
//				if(Erpv4Config.isMultiStockPrice(p_sh)) {
//					int lcrg = Erpv4Config.getDefaultLcrg(p_sh);
//					tr = p_su.getQueryResult("select * from stock,mctype,outer costock "
//						+ "	where st_irg = " + p_irg + " and mt_tpcode = st_msubtype and st_stirg = st_irg and st_cocode = '"+cocode+"' and st_lcrg = " + lcrg);
//				} else {
//					tr = p_su.getQueryResult("select * from stock,mctype,outer costock "
//						+ "	where st_irg = " + p_irg + " and mt_tpcode = st_msubtype and st_stirg = st_irg and st_cocode = '"+cocode+"'");
//				}
//			} else {
//			tr = p_su.getQueryResult("select * from stock,mctype "
//					+ "	where st_irg = " + p_irg + " and mt_tpcode = st_msubtype ");
//			}
//			if(tr.getRecordCount() == 1) {
//				Erpv4StockAttribute stattr = new Erpv4StockAttribute();
//				tr.setRecPointer(0);
//				
//				stattr.irg = p_irg;
//				stattr.baseUnit = tr.getFieldString("st_unit");
//				stattr.defaultSellUnit = tr.getFieldString("st_unit");
//				stattr.defaultBuyUnit = tr.getFieldString("st_unit");
//				
//				stattr.allUnits = new Vector<String>();
//				stattr.ratios = new Hashtable<String,Double>();
//				stattr.allUnits.add(stattr.baseUnit);
//				stattr.ratios.put(stattr.baseUnit , 1.0);
//
//				addOneUnit(stattr,tr.getFieldString("mt_tpsize1"),tr.getFieldDouble("st_msize1"));
//				addOneUnit(stattr,tr.getFieldString("mt_tpsize2"),tr.getFieldDouble("st_msize2"));
//				addOneUnit(stattr,tr.getFieldString("mt_tpsize3"),tr.getFieldDouble("st_msize3"));
//				
//				stattr.prices = new Hashtable<String,Double>();
//				stattr.priceCid = tr.getFieldString("st_standardcur");
//				stattr.cost = tr.getFieldDouble("st_standardcost");
//				stattr.costCid = tr.getFieldString("st_standardcostcur");
//				
//				String pm = Erpv4Config.getString(p_sh,"HasPromotionPrice");
//				if(pm != null && pm.equals("Y")) {
//					double stprice = tr.getFieldDouble("st_standardprice");
//					double stprice1 = tr.getFieldDouble("st_price1"); 
//					double stprice2 = tr.getFieldDouble("st_price2"); 
//					double stprice3 = tr.getFieldDouble("st_price3"); 
//					if(p_date != null && p_date.after(DateUtil.zeroDate)) {
//						tr = p_su.getQueryResult("select * from priceplan,pricepland where prpd_irg = ? and prp_rg = prpd_mrg and prp_start <= ? and prp_end >= ? and prp_active = 'Y' order by prp_rg desc",
//									new Wherecl().appendArgument(p_irg)
//											.appendArgument(p_date)
//											.appendArgument(p_date)
//						);
//						if(tr.getRecordCount() > 0) {
//							tr.setRecPointer(0);
//							stattr.promoStr = tr.getFieldString("prp_remark");
//							int disc = tr.getFieldInt ("prpd_discount");
//							if(disc > 0) {
//								stprice = Math.floor(stprice * ((double) (-disc + 100.0)) + 5.0) / 100.0;
//								stprice1= Math.floor(stprice1 * ((double) (-disc + 100.0)) + 5.0) / 100.0;
//								stprice2= Math.floor(stprice2 * ((double) (-disc + 100.0)) + 5.0) / 100.0;
//								stprice3= Math.floor(stprice3 * ((double) (-disc + 100.0)) + 5.0) / 100.0;
//							} else {
//								double setPrice = tr.getFieldDouble("prpd_price");
//								if(setPrice > 0) {
//								stprice = setPrice;
//								stprice1= setPrice;
//								stprice2= setPrice;
//								stprice3= setPrice;
//								}
//							}
//						}
//					}
//					stattr.prices.put(BiResultCustomer.dft_Price0_label, stprice);
//					stattr.prices.put(BiResultCustomer.dft_Price1_label,  stprice1);
//					stattr.prices.put(BiResultCustomer.dft_Price2_label,  stprice2);
//					stattr.prices.put(BiResultCustomer.dft_Price3_label,  stprice3);
//				} else {
//					stattr.prices.put(BiResultCustomer.dft_Price0_label, tr.getFieldDouble("st_standardprice"));
//					stattr.prices.put(BiResultCustomer.dft_Price1_label, tr.getFieldDouble("st_price1"));
//					stattr.prices.put(BiResultCustomer.dft_Price2_label, tr.getFieldDouble("st_price2"));
//					stattr.prices.put(BiResultCustomer.dft_Price3_label, tr.getFieldDouble("st_price3"));
//				}
//				
//				return(stattr);
//			}
//			return(null);
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return(null);
//		}
//	}
//	
	
	/* customized import worksheet for stock record, not in use currently */
	
	@Override
	protected ExcelPoiFormula getExcelFormula(ExcelPoi jxf,BiColumn bc ,int p_row,Vector<BiColumn> p_cols) {
		/*
		String ss = jxf.cellRangeToString("Category", 0 , 8 , 3, 3, false);
		ss = jxf.cellRangeToString("Category", 0 , 8 , 3, 3, true);
		ss = jxf.cellRangeToString(null, 0 , 8 , 3, 3, true);
		return("Category!$C$2:$C$7");
		*/
		if(cbr == null) return(null);
		String bcl = bc.getLabel();
		
		if(bcl.equals("st_unit"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_baseunit")
				);
		}
		if(bcl.equals("st_size1str"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_sizestr1")
				);
		}
		if(bcl.equals("st_size2str"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_sizestr2")
				);
		}
		if(bcl.equals("st_size3str"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_sizestr3")
				);
		}
		if(bcl.equals("st_munit1"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_munit1")
				);
		}
		if(bcl.equals("st_munit2"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_munit2")
				);
		}
		if(bcl.equals("st_munit3"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_munit3")
				);
		}
		if(bcl.equals("st_dunit1"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_dunit1")
				);
		}
		if(bcl.equals("st_dunit2"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_dunit2")
				);
		}
		if(bcl.equals("st_dunit3"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("mt_tpname")),"Category",cbr.getListColumns(),"mt_tpname","mt_dunit3")
				);
		}
		
		return(null);
	}
	@Override
	protected String getExcelValidation(ExcelPoi jxf,BiColumn bc ,Vector<BiColumn> p_cols,BiCellCollection p_cl,int p_idx) {
		/*
		String ss = jxf.cellRangeToString("Category", 0 , 8 , 3, 3, false);
		ss = jxf.cellRangeToString("Category", 0 , 8 , 3, 3, true);
		ss = jxf.cellRangeToString(null, 0 , 8 , 3, 3, true);
		return("Category!$C$2:$C$7");
		*/
		if(bc.getLabel().equals("mt_tpname")) {
			if(cbr != null) {
				int keyPos = -1;
				Vector<BiColumn>vvv = cbr.getListColumns();
				for(int i =0;i<vvv.size();i++) {
					BiColumn cbc = vvv.get(i);
					if(cbc.getLabel().equals("mt_tpname")) {
						keyPos = i;
					}
				}
				if(keyPos >= 0) {
					return(ExcelPoi.cellRangeToString("Category", 1 , cbr.getRowCount()+1 , keyPos, keyPos, true));
				}
			}
			
		}
		return(super.getExcelValidation(jxf, bc, p_cols,p_cl,p_idx));
	}
	@Override
	protected void beforeFormatExportExcel(ExcelPoi jxf,boolean p_forImport,Vector<BiColumn> cols) {
		if(!p_forImport) return;
		int orgidx = jxf.getCurrentSheetIndex();
		/* int idx = jxf.excel_newSheet("Category");*/
		int idx = jxf.excel_cloneSheet(0,"Category");
		if(idx < 0) return;
		jxf.excel_useSheet(idx);
		
		if(cbr == null) {
			cbr = (BiResult) getView().getSchema().getViewByName("erpv4.McType").newBiResult(getSessionHelper().getLoginId(),null, null, getSessionHelper());
		}
		cbr.clearCondition();
		cbr.clearOrderBy();
		cbr.addOrderByColumnList("mt_tpname",false);
		cbr.query();
		Vector<BiColumn>vvv = cbr.getListColumns();
		int[] colt = cbr.formatExportExcel (
   					jxf,
   					null
   					, null,
   					false
   					, vvv
   					, null
			);
		int xr = 0;
		for(int i=0;i<cbr.getRowCount();i++) {
   			xr = cbr.putOneRowToExceli(
					i,
					null,
					null,
					vvv,
					colt,
					jxf,
					xr,
					false,
					p_forImport,
					null
			);
		}
    	cbr.postProcessExportExcel(jxf,false,vvv,false);
		jxf.excel_useSheet(orgidx);
		
	}
	@Override
	public void clearCondition() { 
		super.clearCondition();
		if(Erpv4Config.isMultiCompany(sh)) {
			Wherecl p_where = new Wherecl();
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			if(getCell("st_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and st_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			Integer lcrg = null;
			if(Erpv4Config.isMultiStockPrice(sh)) {
				lcrg = Erpv4Config.getDefaultLcrg(sh);
			}
			if(lcrg != null) {
//				int lcrg = Erpv4Config.getDefaultLcrg(sh);
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and st_lcrg = " + lcrg).stripAnd();
				p_where	.andWherecl(wcl1);
			} 
			if(getCell("stg_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stg_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stl1_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				if(lcrg != null) {
					wcl1.andUniop("stl1_lcrg", "=", lcrg);
//					wcl1.appendString(" and stl1_lcrg = st_lcrg ").stripAnd();
				}
				wcl1.appendString(" and stl1_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stl2_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				if(lcrg != null) {
					wcl1.andUniop("stl2_lcrg", "=", lcrg);
				}
				wcl1.appendString(" and stl2_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stl3_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stl3_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stl4_lcrg") != null) {
				Wherecl wcl1 = new Wherecl();
				if(lcrg != null) {
					wcl1.andUniop("stl4_lcrg", "=", lcrg);
				}
				p_where.andWherecl(wcl1);
			}
			appendWherecl(p_where);
		}
	}

	/*
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash) {
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
			if(!sh.hasAccessRight("#multicomp")) {
				if(ht == null) ht = new HashSet<BiTable>();
				ht.add(sh.getBiSchema().getTable("costock"));
			}
		}
		return(ht);
	}
	*/
	/*
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			if(getCell("st_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and st_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stg_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stg_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stl1_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stl1_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stl2_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stl2_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
			if(getCell("stl3_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and stl3_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
			}
		}
		return(ht);
	}
	*/
	public Object makeCacheKey(Object[] sids) {
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			return(cocode+sids[0]);
		}
		return(sids[0]);
	}
	public String getStmovdLink( ) {
		return(stmdLinkName);
	}
	public String getStlocLink( ) {
		return(stlocLinkName);
	}
	
	@Override
	protected void afterFetch() {
		BiResult sr = getSubLink(getStmovdLink());
		if(sr instanceof BiResultStockMove) {
//				((BiResultStockMove) sr).reloadStockMove();
		}
	}
//	@Override
//	public void setColumnRg(CellCollection col,BiColumn cl,int rg) throws CellException {
//		if(!cl.getLabel().equals("st_irg")) {
//			super.setColumnRg(col, cl,rg);
//			return;
//		}
//		if(useCocodeForStockRg == null) {
//			String ss = Erpv4Config.getString(getSessionHelper(), "useCocodeForStockRg");
//			if(ss == null || !ss.equals("Y")) {
//				useCocodeForStockRg = false;
//			} else {
//				useCocodeForStockRg = true;
//			}
//		}
//		String cocode;
//		if(useCocodeForStockRg) {
//			cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
//		} else {
//			cocode = "";
//		}
//		int newrg = getView().getSchema().getUniqueRg(this,cocode, rg,
//			cl.getField().getTable().getDbtName(),
//				cl.getField().getName(),
//				null
//			).toInt();
//		col.getCell(cl.getLabel()).set(newrg);
//	}
	

}
