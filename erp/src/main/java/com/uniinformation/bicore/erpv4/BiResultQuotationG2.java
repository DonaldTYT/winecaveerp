package com.uniinformation.bicore.erpv4;


import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
//import org.eclipse.birt.report.model.api.util.StringUtil;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiCoreRpcServlet;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultQuotationG2 extends BiResultQuotation implements BiCoreRpcServlet.BiRpcInterface {

	public BiResultQuotationG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh,boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		modeG2=true;
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		if(p_col.testCell("vd_priceclass") != null) {
			p_col.getCell("vd_priceclass").setItemPropertyInterface(Erpv4StockAttribute.getPriceTypeList(sh));
			/*
			p_col.getCell("vd_priceclass").setItemList(
						new VectorUtil()
						.addElement("Discount 2")
						.addElement("Discount 3")
						.toVector()
					);
					*/
		}
	}
	Boolean OneToOneQuoInvoice = null;
	/*
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		rtn = doCreateAutoInvoice((BiCellCollection) col,true);
		return(rtn);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		rtn = doCreateAutoInvoice((BiCellCollection) col,false);
		return(rtn);
	}
	*/
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col,isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(modeG2 && (quomode == QUOMODE.QUOTATION)) {
			rtn = doCreateOrderFromQuotation(col,isUpdate);
			if(rtn != null && !rtn.getStatus()) return(rtn);
		}
		rtn = doCreateAutoInvoice(col,isUpdate);
		return(rtn);
		
	}
	ReturnMsg doCreateOrderFromQuotation(BiCellCollection col,boolean isUpdate) {
		if(getCellString("inv_quostatus").equals("Confirmed")) {
			try {
				if(DateUtil.minDate.after(getCellDate("inv_date"))) {
					getCell("inv_date").set(DateUtil.today());
				}
				if(StringUtils.isBlank(getCellString("inv_invno"))) {
					getCell("inv_invno").set(getNewOrderNumber(getCell("inv_date").getDate()));
				}
			} catch(Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
		}
		return(ReturnMsg.defaultOk);
	}
	
	ReturnMsg doCreateAutoInvoice(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(OneToOneQuoInvoice == null ){
			String ss = Erpv4Config.getString(sh, "OneToOneQuoInvoice");
			OneToOneQuoInvoice = "Y".equals(ss);
		}
		if(OneToOneQuoInvoice) {
			if(invoiceLinkid != null) {
				BiResult sr = getSubLink(invoiceLinkid);
				int rows = sr.getRowCount();
				if(getCellString("inv_quostatus").equals("Confirmed")) {
				BiCellCollection scol = null;
				if(rows <= 0) {
					scol = sr.newRowCollection();
					rtn = sr.addSubRecord(scol, 0,"");
					if(rtn != null && !rtn.getStatus()) return(rtn);
//					Object tr = rtn.getData();
				} else {
					scol =sr.getRowCollectionV(0);
					Object o = sr.getTrStatObj(new Integer(0));
					sr.markDelete( o, false);
					for(int i=rows-1;i>0;i--) {
						o = sr.getTrStatObj(new Integer(i));
						sr.markDelete( o, true);
					}
				}
				
				try {
					scol.getCell("invh_date").set(getCell("inv_delidate"));
					scol.getCell("invh_duedate").set(getCell("inv_duedate"));
					scol.getCell("invh_post").set("P");
					scol.getCell("invh_voidflag").set(false);
					scol.getCell("invh_payratio").set(100);
				} catch (CellException cex) {
					UniLog.log(cex);
					return(new ReturnMsg(false,cex.toString()));
				}
				} else {
					for(int i=rows-1;i>=0;i--) {
						Object o = sr.getTrStatObj(new Integer(i));
						sr.markDelete( o, true);
					}
				}
			}
		}
		return(rtn);
	}
	
	/*
	String[] quoStatusOptionList = {"New","Pending","Confirmed","Void","ReqApprove","Approved","Rejected"};
//			String[] errorSoon = {"Hello", "World"};
	public Vector getOptionList(BiColumn biCol, Comparable currentSelection, int mode) {
		if(!biCol.getLabel().equals("inv_quostatus")) return(super.getOptionList(biCol, currentSelection, mode));
		boolean useWfm = "Y".equals(Erpv4Config.getString(sh, "UseWfmOnQuoStatus"));
		if(!useWfm) return(super.getOptionList(biCol, currentSelection, mode));
		if (biCol.getOptionList(sh) != null) {
			Vector itemList = new Vector();
	    	for (String opt : quoStatusOptionList){
	    		if( currentSelection.equals(opt) ||
	    				sh.hasAccessRight("!!Quo"+opt)) {
	    			itemList.add(opt);
	    		}
	    	}	
	    	return(itemList);
		}
		
		return(null);
	}
	*/
	
	@Override
	public void resetViewList() {
		super.resetViewList();
		if(quomode == null) return;
		switch(quomode) {
		case ORDER :
		}
		moveViewColumn(getView().getColumnByLabel("inv_quonum"),getView().getColumnByLabel("inv_rg"));
		moveViewColumn(getView().getColumnByLabel("inv_quodate"),getView().getColumnByLabel("inv_quonum"));
		/*
		moveViewColumn(getView().getColumnByLabel("mt_tpname"),getView().getColumnByLabel("st_icode"));
		moveViewColumn(getView().getColumnByLabel("stbd_name"),getView().getColumnByLabel("mt_tpname"));
		moveViewColumn(getView().getColumnByLabel("st_modelno"),getView().getColumnByLabel("stbd_name"));
		*/
	}	

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new QuotationG2CellCollection(p_parent, this));
	}

	@Override
	public boolean isRequired(BiColumn bc) {
		switch(getQuomode()) {
		case QUOTATION:
			if(bc.getLabel().equals("inv_date")) return(false);
			break;
		case ORDER:
			if(bc.getLabel().equals("inv_quodate")) return(false);
			break;
		}
		return(super.isRequired(bc));
	}
	
	public Vector getOptionList(BiColumn biCol, Comparable currentSelection, int mode) {
		if (biCol.getOptionList(this,null) != null) {
			Vector itemList = new Vector();
	    	for (String opt : biCol.getOptionList(this,null)){
	    		itemList.add(opt);
	    	}	
	    	return(itemList);
		}
		return(null);
	}
	@Override
	public String biRpcCallSegment(String p_segName, String p_jsonstr) {
		// TODO Auto-generated method stub
		try {
		if(p_segName.equals("getQuoNum")) {
			boolean inBeginWork;
			JSONObject jo = new JSONObject(p_jsonstr);
			java.util.Date d = DateUtil.getDate(jo.getString("date"));
			if(d == null) return("FAILInvalid Argument");
			inBeginWork = inBeginWork();
			if(!inBeginWork) beginWork();
			String ss = getNewOrderNumber(d);
			if(!inBeginWork) commitWork();
			return("OK  "+ ss);
		}
		} catch (Exception ex) {
			UniLog .log(ex);
		}
		return null;
	}
}
