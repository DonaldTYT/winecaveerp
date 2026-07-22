package com.uniinformation.bicore.erpv4;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;


public class BiResultMoDet extends BiResultStmovd{
	
	Boolean autoUpdatePriceAndCost = null;
	CellValueAction actionTypeChanged = null;
	public BiResultMoDet (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		actionTypeChanged = 
				new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value)
					throws CellException {
				CellCollection col = ((ColumnCell) p_value).getCollection();
				if(col.getCell("stmd_tdtype").getString().equals("MO")) {
					if(col.testCell("or_ocode") != null) col.getCell("or_ocode").setMode(Cell.VMODE_NORMAL);
					if(col.testCell("inv_invno") != null) col.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("stmd_uprice") != null) {
						if(getColumnByLabel("stmd_uprice").isProtected()) 
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_PROTECTED);
								col.getCell("stmd_uprice").protect(true);
							else
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_NORMAL);
								col.getCell("stmd_uprice").protect(false);
					}
				} else if(col.getCell("stmd_tdtype").getString().equals("JO")) {
					if(col.testCell("or_ocode") != null) col.getCell("or_ocode").setMode(Cell.VMODE_NORMAL);
					if(col.testCell("inv_invno") != null) col.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("stmd_uprice") != null) {
						if(getColumnByLabel("stmd_uprice").isProtected()) 
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_PROTECTED);
								col.getCell("stmd_uprice").protect(true);
							else
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_NORMAL);
								col.getCell("stmd_uprice").protect(false);
					}
				} else if(col.getCell("stmd_tdtype").getString().equals("RO")) {
					if(col.testCell("or_ocode") != null) col.getCell("or_ocode").setMode(Cell.VMODE_NORMAL);
					if(col.testCell("inv_invno") != null) col.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("stmd_uprice") != null) {
						if(getColumnByLabel("stmd_uprice").isProtected()) 
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_PROTECTED);
								col.getCell("stmd_uprice").protect(true);
							else
								col.getCell("stmd_uprice").protect(false);
					}
				} else if(col.getCell("stmd_tdtype").getString().equals("JI")) {
					if(col.testCell("or_ocode") != null) col.getCell("or_ocode").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("inv_invno") != null) col.getCell("inv_invno").setMode(Cell.VMODE_NORMAL);
					if(col.testCell("stmd_uprice") != null) {
						if(getColumnByLabel("stmd_uprice").isProtected()) 
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_PROTECTED);
								col.getCell("stmd_uprice").protect(true);
							else
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_NORMAL);
								col.getCell("stmd_uprice").protect(false);
					}
				} else if(col.getCell("stmd_tdtype").getString().equals("RI")) {
					if(col.testCell("or_ocode") != null) col.getCell("or_ocode").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("inv_invno") != null) col.getCell("inv_invno").setMode(Cell.VMODE_NORMAL);
					if(col.testCell("stmd_uprice") != null) {
						if(getColumnByLabel("stmd_uprice").isProtected()) 
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_PROTECTED);
								col.getCell("stmd_uprice").protect(true);
							else
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_NORMAL);
								col.getCell("stmd_uprice").protect(false);
					}
				} else if(col.getCell("stmd_tdtype").getString().equals("MI")) {
					if(col.testCell("or_ocode") != null) col.getCell("or_ocode").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("inv_invno") != null) col.getCell("inv_invno").setMode(Cell.VMODE_NORMAL);
					if(col.testCell("stmd_uprice") != null) {
						if(getColumnByLabel("stmd_uprice").isProtected()) 
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_PROTECTED);
								col.getCell("stmd_uprice").protect(true);
							else
//								col.getCell("stmd_uprice").setMode(Cell.VMODE_NORMAL);
								col.getCell("stmd_uprice").protect(false);
					}
				} else {
					if(col.testCell("or_ocode") != null) col.getCell("or_ocode").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("inv_invno") != null)col.getCell("inv_invno").setMode(Cell.VMODE_DISPONLY);
					if(col.testCell("stmd_uprice") != null) {
						col.getCell("stmd_uprice").setMode(Cell.VMODE_DISPONLY);
						col.getCell("stmd_uprice").set(0.0);
					}
				}
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
	
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col,isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(autoUpdatePriceAndCost == null) {
			autoUpdatePriceAndCost = "Y".equals(Erpv4Config.getString(sh, "MoAutoUpdatePriceAndCost"));
		}
		if(autoUpdatePriceAndCost) {
			double uprice=0.0f;
			double sprice=0.0f;
			String updField=null;
			sprice = col.getDouble("stmd_uprice");
			if(col.getCellString("stmd_tdtype").equals("MO")) {
				if(col.testCell("vd_priceclass") != null && col.testCell("stmd_rprice") != null) {
					uprice =	col.getCell("stmd_rprice").getDouble();
					Erpv4StockAttribute stAttr = ((Erpv4StmdCellCollection) col).getStockAttribute();
					if(stAttr != null && stAttr.irg == col.getCellInt("stmd_irg") && stAttr.getPromoStr() == null) {
						String pc = col.getCellString("vd_priceclass");
						if(pc.equals(BiResultCustomer.dft_Price0_label)) updField = "st_standardprice";
						if(pc.equals(BiResultCustomer.dft_Price1_label)) updField = "st_price1";
						if(pc.equals(BiResultCustomer.dft_Price2_label)) updField = "st_price2";
						if(pc.equals(BiResultCustomer.dft_Price3_label)) updField = "st_price3";
					}
				}
			} else {
				if(col.testCell("standardcost") != null) {
					uprice =	col.getCell("st_standardcost").getDouble();
					updField = "st_standardcost";
				}
			}
			
			if(updField != null && sprice > 0 && uprice != sprice) {
				try {
					String execStr = null;
					Wherecl wcl = null;
					if(Erpv4Config.isMultiCompany(sh) && col.testCell("st_stirg") != null) {
						String cocode = Erpv4Config.getDefaultCoCode(sh);
						execStr = "update costock set " + updField + " = " + sprice + " where st_cocode = '" + cocode + "' and st_stirg = " + col.getCellInt("stmd_irg");
					} else {
						execStr = "update stock set " + updField + " = " + sprice + " where st_irg = " + col.getCellInt("stmd_irg");
					}
					su.executeUpdate(execStr,wcl);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
		return(rtn);
	}

	@Override
	
	protected void createColumnCells(final BiCellCollection col)
	{
		super.createColumnCells(col);	
		if(col.getCell("stm_module").getString().equals("vstmo")) {
			col.getCell("stmd_tdtype").setItemPropertyInterface(
					new GipiNamedItemList()
						.appendItem("MI",Erpv4Config.getStmdName(sh, "MI"))
						.appendItem("RO", Erpv4Config.getStmdName(sh, "RO"))
					);
		}
		if(col.getCell("stm_module").getString().equals("cstmo")) {
			/*
			col.getCell("stmd_tdtype").setItemList(
						new VectorUtil()
						.addElement("MO")
						.addElement("RI")
						.toVector()
					);
					*/
			col.getCell("stmd_tdtype").setItemPropertyInterface(
					new GipiNamedItemList()
						.appendItem("MO",Erpv4Config.getStmdName(sh, "MO"))
						.appendItem("RI", Erpv4Config.getStmdName(sh, "RI"))
					);
		}
		if(col.getCell("stm_module").getString().equals("stadj")) {
			col.getCell("stmd_tdtype").setItemList(
						new VectorUtil()
						.addElement("JI")
						.addElement("JO")
						.toVector()
					);
		}
		/*
		 This code should never be reached
		if(col.getCell("stm_module").getString().equals("sttfr")) {
			col.getCell("stmd_tdtype").setItemList(
						new VectorUtil()
						.addElement("KO")
						.addElement("JI")
						.toVector()
					);
		}
		*/
		if(!getView().getName().equals("erpv4.MoGenericDet")) {
			col.getCell("stmd_tdtype").addAction(actionTypeChanged);
			/*
			if(((BiResultStmov) getParent()).detAmtCell != null) {
				col.getCell(
						((BiResultStmov) getParent()).detAmtCell
						).addAction(((BiResultStmov) getParent()).stmCalAmount);
			}
			*/
		}
	}
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(p_col != null && p_lookupTable.getName().equals("mo_stmdtype")) {
			if(wcl == null ) wcl = new Wherecl();
			wcl.appendString(" and mo_stmdtype.stmdo_module = '"+p_col.getCellString("stm_module")+"' ").stripAnd();
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	/*
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		BiColumn locCol = getColumnByLabel("stmdo_name");
		if(locCol != null && locCol.getTable() != null && locCol.getTable().getName().equals("mo_stmdtype")) {
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and mo_stmdtype.stmdo_module = '"+getParent().getCurrentCollection().getCellString("stm_module")+"' ").stripAnd();
			p_where.andWherecl(wcl1);
		}
		return(ht);
	}		
	*/
	
	@Override
    protected String brEvalFunction(String p_functName,List p_args) {
    	if(p_functName.equals("brGetStmdModule")) {
    		return("'"+getParent().getCurrentCollection().getCellString("stm_module")+"'");
    	}
		return(super.brEvalFunction(p_functName, p_args));
    }	
}
