package com.uniinformation.bicore.erpv4;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCustomer extends BiResultErpv4 {
	static public final String dft_Price0_label = "List Price";
	static public final String dft_Price1_label = "Discount 1";
	static public final String dft_Price2_label = "Discount 2";
	static public final String dft_Price3_label = "Discount 3";
	/*
	static final String key_Price0 = ".LB_ST_STANDARDPRICE";
	static final String key_Price1 = ".LB_ST_PRICE1";
	static final String key_Price2 = ".LB_ST_PRICE2";
	static final String key_Price3 = ".LB_ST_PRICE3";
	*/
	
	protected String stockViewId = null;
	
	TranslateListGetItemProperty priceTypeList = null;
	public BiResultCustomer(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		try {
			
			if(col.getCell("vd_addr0")==null) return(ReturnMsg.defaultOk);
			if((!col.getCell("vd_addr0").getString().equals("")) &&
		       ( col.getCell("vd_addr1").getString().equals("")) ) {
				UniLog.log(" Addr put in first line only, try splitting to 2 lines");
				List<String>addrLines  = ChnftrParser.splitText(col.getCell("vd_addr0").getString(), "helv_nr", "chinese", 10.0f, 350);
				if(addrLines.size() <= 2)  {
					
				}
			}
			Vector<BiResult> v = getSubLinks();
			for(BiResult sr : v) {
				if(sr.getView().getTable().getName().equals("sv_loc")) {
					if(sr.getRowCount() <= 0) {
						BiCellCollection scol;
						scol = sr.newRowCollection();
						ReturnMsg rtn = sr.addSubRecord(scol, 0,"");
//						Object tr = rtn.getData();
						if(rtn != null && !rtn.getStatus()) {
							return new ReturnMsg(false,"Fail to add default deliver address");
						}
						if(scol.testCell("svloc_sameasmain") != null ) {
							scol.testCell("svloc_sameasmain").set(true);
						}
					}
					Vector<BiCellCollection> cv = sr.getRowCollectionList();
					for(BiCellCollection scol : cv ) {
						if(scol.testCell("svloc_sameasmain") == null ||  scol.getCell("svloc_sameasmain").getBoolean()) {
							scol.getCell("svloc_desp").set(col.getCell("vd_vname"));
							scol.getCell("svloc_chndesp").set(col.getCell("vd_chnname"));
							scol.getCell("svloc_contact").set(col.getCell("vd_contact"));
							scol.getCell("svloc_tel").set(col.getCell("vd_tel"));
							if(col.testCell("vd_caddr0") != null && !col.getCellString("vd_caddr0").trim().equals("")) {
								scol.getCell("svloc_addr1").set(col.getCell("vd_caddr0"));
							} else {
								scol.getCell("svloc_addr1").set(col.getCell("vd_addr0"));
							}
							scol.getCell("svloc_addr2").set(col.getCell("vd_addr1"));
							scol.getCell("svloc_city").set(col.getCell("vd_addr2"));
							scol.getCell("svloc_state").set(col.getCell("vd_addr3"));
							if(col.testCell("vd_country") != null && scol.testCell("svloc_country") != null) {
								scol.getCell("svloc_country").set(col.getCell("vd_country"));
							}
							if(col.testCell("vd_zipcode") != null && scol.testCell("svloc_zipcode") != null) {
								scol.getCell("svloc_zipcode").set(col.getCell("vd_zipcode"));
							}
							if(col.testCell("vd_permobile") != null &&  scol.testCell("svloc_tel2") != null) {
								scol.getCell("svloc_tel2").set(getCell("vd_permobile"));
							}
							if(col.testCell("vd_title") != null &&  scol.testCell("svloc_title") != null) {
								scol.getCell("svloc_title").set(getCell("vd_title"));
							}
							if(col.testCell("vd_district") != null &&  scol.testCell("svloc_district") != null) {
								scol.getCell("svloc_district").set(getCell("vd_district"));
							}
						}
						if(scol.testCell("svloc_sameasmain") == null) break;
					}
				}
			}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		return(null);
	}
	
	@Override
	protected ReturnMsg validateOneRow(CellCollection col,boolean p_update)
	{
		ReturnMsg rtnMsg = super.validateOneRow(col,p_update);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			if(col.getCell("vd_vcode").getString().equals("")) {
//				int rgno = getView().getSchema().getRg("", 1026);
//				col.getCell("vd_vcode").set( String.format("C%04d", rgno));
				Value v = getView().getSchema().getUniqueRg(this,"",1026,"vendor","vd_vcode","C&&&&");
				col.getCell("vd_vcode").set( v.toString());
			}
			col.getCell("vd_cuser").set(su.getLoginId());
			col.getCell("vd_uuser").set(su.getLoginId());
			col.getCell("vd_cdate").set(new java.util.Date());
			col.getCell("vd_udate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtnMsg);
	}
	
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		if(stockViewId == null) stockViewId = "erpv4.stock";
		super.createColumnCells(p_col);
		if(p_col.testCell("vd_priceclass") != null) {
			if(priceTypeList == null) {
				final Hashtable<String,String> ht = new Hashtable();
				ht.put(dft_Price0_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price0);
				ht.put(dft_Price1_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price1);
				ht.put(dft_Price2_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price2);
				ht.put(dft_Price3_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price3);
				priceTypeList = new TranslateListGetItemProperty(
					new VectorUtil()
					.addElement(dft_Price0_label)
					.addElement(dft_Price1_label)
					.addElement(dft_Price2_label)
					.addElement(dft_Price3_label)
					.toVector()
						) {

					@Override
					public String translate(Object p_item) {
						return(TranslateUtil.getText(getSessionHelper(), ht.get((String) p_item), "LABEL", (String) p_item));
						// TODO Auto-generated method stub
					}

					@Override
					public int getRowWidth() {
						// TODO Auto-generated method stub
						return 0;
					}};
					
			}
			p_col.getCell("vd_priceclass").setItemPropertyInterface(priceTypeList);
		}
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(Erpv4Config.isMultiCompany(sh)) {
		BiColumn locCol = getColumnByLabel("vdx_cocode");
		if(locCol != null && columnInSelectList(locCol)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and " + "vdx_cocode = '"+cocode+"' ").stripAnd();
			p_where.andWherecl(wcl1);
		}
		}
		return(ht);
	}			
}
