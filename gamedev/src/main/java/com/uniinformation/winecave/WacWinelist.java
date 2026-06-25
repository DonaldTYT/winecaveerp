package com.uniinformation.winecave;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.NumberUtil;
import com.kyoko.common.StringUtil.*;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.poi.ExcelPoi;

public class WacWinelist {
	private static Double cashDiscountPercent = 0.03;
	public static final int LANGUAGE_ENGLISH = 0;
	public static final int LANGUAGE_TRADITIONAL_CHINESE = 1;

	public final static int WINE_TABLE_TYPE_REGION = 0;
	public final static int WINE_TABLE_TYPE_SEARCH = 1;
	public final static int WINE_TABLE_TYPE_LATEST_OFFERS = 2;
	public final static int WINE_TABLE_TYPE_REGION_YEAR = 3;
	public final static int WINE_TABLE_TYPE_FULLLIST  = 4;
	public static boolean getFullWineList(String p_vcode,SelectUtil su,OutputStream p_out) {
		try {
			WacWinelist wl = new WacWinelist();
			int version=2;
			String regionCol = "storg_name alias_storg_name";
			String colorCol = "mt_tpname alias_mt_tpname";
			String remarkCol = "st_remark alias_st_remark";
			TableRec wTr =	queryWineList(su,WINE_TABLE_TYPE_FULLLIST,regionCol,colorCol,remarkCol,null,null,null,null,null,null,null,null,0,p_vcode);
			List<WineCaveStockRecord> stockList = toStockRecordList(su, wTr, "Region", false ,-1, 0,"HKD",true);
			return(wl.createWineList(stockList,version,p_out));
		} catch(Exception ex) {
			UniLog.log(ex);
			return(false);
		} 
		finally {
			if (su != null)
				su.close();
		}
		
	}
	static TableRec queryWineList(SelectUtil p_su,int p_type,String p_regionCol,String p_colorCol,String p_remarkCol,
			String p_sttpCode,String p_mttpCode,String p_countryName,String p_regionCode,String p_mszRange,String p_searchName,String p_myYear,Boolean p_fle,int p_language,String p_vcode) throws Exception {
		UniLog.log1("queryWineList sttpcode=%s,mttpcode=%s,countryname=%s,regioncode=%s,mszRange=%s,searchname=%s,myyear=%s", p_sttpCode, p_mttpCode, p_countryName, p_regionCode, p_mszRange, p_searchName, p_myYear);
		TableRec newTr = null;
		switch(p_type) {
		case WINE_TABLE_TYPE_REGION :
			return(p_su.getQueryResult("select sttp_name,st_irg, or_cocode, consgp_org,consgp_price, consgp_salebybtl, "+p_colorCol
												+", storg_code, st_msize3, st_iname, st_msize1, st_msize2, st_onsellqty"
												+", st_standardprice, st_score0, st_score1, "+p_remarkCol+","
												+" st_issalable, storg_name alias_storg_name,stbd_cname,st_modelno, storg_ecountry, "
												+" sum(pdls_stockqty) sum_stockqty"
												+" from podetlocstatus,stock,orders,st_brand,st_origin,mctype, st_type, outer(consgprice) "
				   							+ "where pdls_stockqty > 0 "
											+ "and pdls_loc = 'WH01' "
											+ "and st_irg = pdls_irg "
											+ "and or_org = pdls_org "
				   							+ "and stbd_code = st_mbrand "
				   							+ "and storg_code = stbd_origin "
				   							+ "and mt_tpcode = st_msubtype " 
				   							+ "and sttp_code = st_mtype "
											+ "and consgp_irg = pdls_irg "
											+ "and consgp_org = pdls_org "
											+ "and consgp_price > 0 "
											+ (StringUtils.isNotBlank(p_sttpCode) ? String.format(" and st_mtype = '%s' ", p_sttpCode) : "")
											+ (StringUtils.isNotBlank(p_mttpCode) ? String.format(" and st_msubtype = %s ", p_mttpCode) : "")
											+ (StringUtils.isNotBlank(p_countryName) ? String.format(" and %s = '%s' ", p_language == LANGUAGE_ENGLISH ? "storg_ecountry" : "storg_ccountry", p_countryName) : "")
											+ (StringUtils.isNotBlank(p_regionCode) ? String.format(" and storg_code = '%s' ", p_regionCode) : "")
											+ (StringUtils.isNotBlank(p_mszRange) ? String.format(" and st_mszrange = '%s' ", p_mszRange) : "")
											+ " group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22"
											, null
			));	
		case WINE_TABLE_TYPE_REGION_YEAR:
			return(p_su.getQueryResult("select sttp_name,st_irg, or_cocode, consgp_org,consgp_price, consgp_salebybtl, "+p_colorCol
												+", storg_code, st_msize3, st_iname, st_msize1, st_msize2, st_onsellqty"
												+", st_standardprice, st_score0, st_score1, "+p_remarkCol+","
												+" st_issalable, storg_name alias_storg_name,stbd_cname,st_modelno, storg_ecountry, "
												+" sum(pdls_stockqty) sum_stockqty"
												+" from podetlocstatus,stock,orders,st_brand,st_origin,mctype, st_type, outer(consgprice) "
				   							+ "where pdls_stockqty > 0 "
											+ "and pdls_loc = 'WH01' "
											+ "and st_irg = pdls_irg "
											+ "and or_org = pdls_org "
				   							+ "and stbd_code = st_mbrand "
				   							+ "and storg_code = stbd_origin "
				   							+ "and mt_tpcode = st_msubtype " 
				   							+ "and sttp_code = st_mtype "
											+ "and consgp_irg = pdls_irg "
											+ "and consgp_org = pdls_org "
											+ "and consgp_price > 0 "
											+ (StringUtils.isNotBlank(p_sttpCode) ? String.format(" and st_mtype = '%s' ", p_sttpCode) : "")
											+ (StringUtils.isNotBlank(p_mttpCode) ? String.format(" and st_msubtype = %s ", p_mttpCode) : "")
											+ (StringUtils.isNotBlank(p_countryName) ? String.format(" and %s = '%s' ", p_language == LANGUAGE_ENGLISH ? "storg_ecountry" : "storg_ccountry", p_countryName) : "")
											+ (StringUtils.isNotBlank(p_regionCode) ? String.format(" and storg_code = '%s' ", p_regionCode) : "")
											+ (StringUtils.isNotBlank(p_mszRange) ? String.format(" and st_mszrange = '%s' ", p_mszRange) : "")
											+(p_myYear.trim().equals("ALL") ? "" : (p_fle ? (" and st_msize3 <= "+p_myYear) : (" and st_msize3 = "+p_myYear)))
											+ " group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22"
											, null
			));	
		case WINE_TABLE_TYPE_SEARCH:
			return(p_su.getQueryResult("select sttp_name,st_irg, or_cocode, consgp_org, consgp_price, consgp_salebybtl, "+p_regionCol+", "+p_colorCol+", "
					+ "storg_code, st_msize3, st_iname, st_msize1, st_msize2, st_onsellqty, st_standardprice, st_score0, st_score1, "+p_remarkCol+","
					+" st_issalable, stbd_cname, st_modelno, storg_ecountry, "
					+ "sum(pdls_stockqty) sum_stockqty"
					+" from podetlocstatus,stock,orders,st_brand,st_origin,mctype, st_type, outer(consgprice) "
					+"where pdls_stockqty > 0 "
					+"and pdls_loc = 'WH01' "
					+ "and st_irg = pdls_irg "
					+ "and or_org = pdls_org "
					+ "and stbd_code = st_mbrand "
					+ "and storg_code = stbd_origin "
					+ "and mt_tpcode = st_msubtype " 
					+ "and sttp_code = st_mtype "
					+ "and consgp_irg = pdls_irg "
					+ "and consgp_org = pdls_org "
					+ "and consgp_price > 0 "
					+ "and (stbd_name like ? or st_iname like ?) "
					+ " group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22"
						, new Wherecl()
							.appendArgument("%"+p_searchName+"%")
							.appendArgument("%"+p_searchName+"%")

					));	
			
		case WINE_TABLE_TYPE_FULLLIST:

			return(p_su.getQueryResult("select sttp_name,st_irg, or_cocode, consgp_org, consgp_price, consgp_salebybtl, "+p_regionCol+", "+p_colorCol+", "
					+ "storg_code, st_msize3, st_iname, st_msize1, st_msize2, st_onsellqty, st_standardprice, st_score0, st_score1, "+p_remarkCol+","
					+" st_issalable, stbd_cname,st_modelno, storg_ecountry, "
					+ "sum(pdls_stockqty) sum_stockqty"
					+" from podetlocstatus,stock,orders,st_brand,st_origin,mctype, st_type, outer(consgprice) "
					+"where pdls_stockqty > 0 "
					+"and pdls_loc = 'WH01' "
					+ "and st_irg = pdls_irg "
					+ "and or_org = pdls_org "
					+ "and stbd_code = st_mbrand "
					+ "and storg_code = stbd_origin "
					+ "and mt_tpcode = st_msubtype " 
					+ "and sttp_code = st_mtype "
					+ "and consgp_irg = pdls_irg "
					+ "and consgp_org = pdls_org "
					+ "and consgp_price > 0 "
					+ (p_vcode == null ? "" : " and or_cocode = '"+p_vcode+"' ") 
					+ " group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22"
						, null

					));		
		}
		return(null);
	}

	static List toStockRecordList(SelectUtil p_su, TableRec p_stockTr, final String orderbyType, final boolean orderbyDesc, int up_minPrice, int p_maxPrice, String p_ccy, boolean useNewSelect) throws Exception {
		
			//obtain exchange rate for ccy
			double price;
			double xRate = getXRate(p_su, p_ccy);
			
			//TODO:1 filter the record based on price range
			//TODO:2 calculate the selected currency price based on exchange rate
		
			Hashtable stirgsHash = new Hashtable(); 
			TableRec stockTr = p_stockTr;
			stirgsHash = new Hashtable(); 
			int rowCount = 0;
			List<WineCaveStockRecord> stockRecords = new ArrayList<WineCaveStockRecord>();
			WineCaveStockRecord stockRecord = null;
			for (int i=0; stockTr != null && i<stockTr.size(); i++) {
				stockTr.setRecPointer(i);
				String color = stockTr.getField("alias_mt_tpname").toString().trim();
				double size1 = (int) NumberUtil.parseDouble(stockTr.getField("st_msize1").toString().trim());
				double size2 = (int) NumberUtil.parseDouble(stockTr.getField("st_msize2").toString().trim());
				int vint = (int) NumberUtil.parseDouble(stockTr.getField("st_msize3").toString().trim());
				String wine = stockTr.getField("st_iname").toString().trim();
				String packing  = ((int) size1)+"x"+ ((int) size2);
				int onSellQty = (int) NumberUtil.parseDouble(stockTr.getField("st_onsellqty").toString().trim());
				
				int pdlsstockqty = 0;
				if(useNewSelect)
					pdlsstockqty = (int) NumberUtil.parseDouble(stockTr.getField("sum_stockqty").toString().trim());	 //
				else
					pdlsstockqty = (int) NumberUtil.parseDouble(stockTr.getField("pdls_stockqty").toString().trim());	 //



//				double hkdbot = getWebStandardprice(NumberUtil.parseDouble(stockTr.getField("st_standardprice").toString().trim()));

//				double consgpprice = getWebConsigpprice(NumberUtil.parseDouble(stockTr.getField("consgp_price").toString().trim()));	 //
				double hkdbot = NumberUtil.parseDouble(stockTr.getField("st_standardprice").toString().trim());

				double consgpprice = NumberUtil.parseDouble(stockTr.getField("consgp_price").toString().trim());	 //


				int score0 = (int) NumberUtil.parseDouble(stockTr.getField("st_score0").toString().trim());
				int score1 = (int) NumberUtil.parseDouble(stockTr.getField("st_score1").toString().trim());
				String score = "";
				if (score1 > 0)
					score = score0 + "-" + score1;
				else
					score = "" + (score0 == 0 ? "-" : score0);
				String remark = stockTr.getField("alias_st_remark").toString().trim();
				String cname = stockTr.getField("stbd_cname").toString().trim(); 
				String modelno = stockTr.getField("st_modelno").toString().trim(); 
				if(cname == null) {
					UniLog.log("HAHA cname got null");
				}
				int st_irg = NumberUtil.parseInt(stockTr.getField("st_irg").toString().trim());
				boolean bPrinted;
				int pdlsorg = 0;
				String cocode = null;
				if(useNewSelect) {
					pdlsorg = NumberUtil.parseInt(stockTr.getField("consgp_org").toString().trim());
					bPrinted = true;
					cocode = stockTr.getField("or_cocode").toString().trim();
				} else {
					pdlsorg = NumberUtil.parseInt(stockTr.getField("pdls_org").toString().trim());
					if (onSellQty <= 0 && pdlsorg <= 0) {
						continue;
					}
					bPrinted = (stirgsHash.get(""+st_irg) != null);
					/*
						1. onSellQty > 0 && pdlsorg == 0     1
						2. onSellQty > 0 && pdlsorg > 0      1 + n
						3. onSellQty = 0 && pdlsorg > 0          n
					 */
				}

				String tempRegionName = stockTr.getField("alias_storg_name").toString().trim();
				price = hkdbot / xRate;
				UniLog.log("add stockRecords 0 irg:" + st_irg + ",org:" + pdlsorg + ",onSellQty:" + onSellQty + ",hkdbot:" + hkdbot + ",consgpprice:" + consgpprice);
				if(useNewSelect) {
					if (pdlsorg == 0 && onSellQty > 0 && (up_minPrice < 0 || (price >= up_minPrice && price <= p_maxPrice))) {
						if(!cocode.equals("WINECAVE")) continue;
						int myOnSellQty = onSellQty > pdlsstockqty ? pdlsstockqty : onSellQty;
						int cs = size1 != 0 ? (int) (myOnSellQty / size1) : 0;
						int btls = size1 != 0 ? (int) (myOnSellQty % size1) : 0;
						/*
						int cs = size1 != 0 ? (int) (onSellQty / size1) : 0;
						int btls = size1 != 0 ? (int) (onSellQty % size1) : 0;
						*/

						stockRecord = new WineCaveStockRecord();
						stockRecord.setIrg(st_irg);
						stockRecord.setOrg(0);
						stockRecord.setRegion(tempRegionName);
						stockRecord.setColor(color);
						stockRecord.setVint(vint);
						stockRecord.setName(wine);
						stockRecord.setPacking(packing);
						stockRecord.setCsQty(cs);
						stockRecord.setBtlQty(btls);
						stockRecord.setPrice(price);
						stockRecord.setScore(score);
						stockRecord.setScore0(score0);
						stockRecord.setScore1(score1);
						stockRecord.setRemark(" ");
						stockRecord.setCname(cname);
						stockRecord.setWineClass(modelno);
						stockRecord.setMtype( stockTr.getField("sttp_name").toString().trim());
						stockRecord.setCountry( stockTr.getField("storg_ecountry").toString().trim());
						stirgsHash.put(""+st_irg, "");
						stockRecords.add(stockRecord);
						UniLog.log("new add stockRecords irg:" + st_irg + ",org:0" + ",cs:" + cs + ",btls:" + btls + ",price:" + price);
					}
				} else {
				if (!bPrinted && onSellQty > 0
						&& (up_minPrice < 0 || (price >= up_minPrice && price <= p_maxPrice))
						) {
					int realOnSellQty = 0;
					TableRec pdlsstockqtyTr = p_su.getQueryResult(
																"select sum(pdls_stockqty) sumPdlsStockqty from"
																+" podetlocstatus,orders"
																+" where pdls_irg = "+st_irg+" and pdls_loc = 'WH01' and pdls_stockqty > 0 and or_org = pdls_org and or_cocode = 'WINECAVE'"
																, null
															);
					if (pdlsstockqtyTr.size() > 0) {
						pdlsstockqtyTr.setRecPointer(0);
						realOnSellQty = (int) pdlsstockqtyTr.getFieldDouble("sumPdlsStockqty");
					}
					UniLog.log("HAHA after select sum: "  + realOnSellQty);

					int myOnSellQty = onSellQty;
					if (realOnSellQty < onSellQty) {
						myOnSellQty = realOnSellQty;
					} 

					if (realOnSellQty > 0 && onSellQty > 0) {
						int cs = size1 != 0 ? (int) (myOnSellQty / size1) : 0;
						int btls = size1 != 0 ? (int) (myOnSellQty % size1) : 0;
						/*
						int cs = size1 != 0 ? (int) (onSellQty / size1) : 0;
						int btls = size1 != 0 ? (int) (onSellQty % size1) : 0;
						*/

						stockRecord = new WineCaveStockRecord();
						stockRecord.setIrg(st_irg);
						stockRecord.setOrg(0);
						stockRecord.setRegion(tempRegionName);
						stockRecord.setColor(color);
						stockRecord.setVint(vint);
						stockRecord.setName(wine);
						stockRecord.setPacking(packing);
						stockRecord.setCsQty(cs);
						stockRecord.setBtlQty(btls);
						stockRecord.setPrice(price);
						stockRecord.setScore(score);
						stockRecord.setScore0(score0);
						stockRecord.setScore1(score1);
						stockRecord.setRemark(" ");
						stockRecord.setMtype( stockTr.getField("sttp_name").toString().trim());
						stockRecord.setCountry( stockTr.getField("storg_ecountry").toString().trim());
						stirgsHash.put(""+st_irg, "");
						stockRecords.add(stockRecord);
						UniLog.log("add stockRecords irg:" + st_irg + ",org:0" + ",cs:" + cs + ",btls:" + btls + ",price:" + price);
					}
				}
				}
				price = consgpprice / xRate;
				if (pdlsorg > 0
						&& (up_minPrice < 0 || (price >= up_minPrice && price <= p_maxPrice))) {
					int consgpcs = size1 != 0 ? (int) (pdlsstockqty / size1) : 0; //
					int consgpbtls = size1 != 0 ? (int) (pdlsstockqty % size1) : 0; //
					stockRecord = new WineCaveStockRecord();
					stockRecord.setIrg(st_irg);
					stockRecord.setOrg(pdlsorg );
					stockRecord.setRegion(tempRegionName);
					stockRecord.setColor(color);
					stockRecord.setVint(vint);
					stockRecord.setName(wine);
					stockRecord.setPacking(packing);
					stockRecord.setCsQty(consgpcs);
					stockRecord.setBtlQty(consgpbtls);
					stockRecord.setPrice(price);
					stockRecord.setScore(score);
					stockRecord.setScore0(score0);
					stockRecord.setScore1(score1);
					stockRecord.setRemark(" ");
					stockRecord.setCname(cname);
					stockRecord.setWineClass(modelno);
					stockRecord.setMtype( stockTr.getField("sttp_name").toString().trim());
					stockRecord.setCountry( stockTr.getField("storg_ecountry").toString().trim());
					stockRecords.add(stockRecord);
					UniLog.log("add stockRecords irg:" + st_irg + ",org:" + pdlsorg + ",cs:" + consgpcs + ",btls:" + consgpbtls + ",price:" + price);
				}
			}
			Collections.sort(
				stockRecords, 
				new Comparator<WineCaveStockRecord>() {
					public int compare(WineCaveStockRecord stockRecord1, WineCaveStockRecord stockRecord2) {
						int result = stockRecord1.getVint() - stockRecord2.getVint();
						if (orderbyType != null) {
							if (orderbyType.equals("Region")) {
								result = compare(stockRecord1.getRegion(), stockRecord2.getRegion(), orderbyDesc);
								if (result == 0) {
									result = compare(stockRecord1.getColor(), stockRecord2.getColor(), false);
								}
								if (result == 0) {
									result = compare(stockRecord1.getVint(), stockRecord2.getVint(), false);
								}
							} else if (orderbyType.equals("Color")) {
								result = compare(stockRecord1.getColor(), stockRecord2.getColor(), orderbyDesc);
								if (result == 0) {
									result = compare(stockRecord1.getVint(), stockRecord2.getVint(), false);
								}
							} else if (orderbyType.equals("Vint."))
								result = compare(stockRecord1.getVint(), stockRecord2.getVint(), orderbyDesc);
							else if (orderbyType.equals("Wine"))
								result = compare(stockRecord1.getName(), stockRecord2.getName(), orderbyDesc);
							else if (orderbyType.equals("BTL"))
								result = compare(stockRecord1.getPrice(), stockRecord2.getPrice(), orderbyDesc);
							else if (orderbyType.equals("Score")) {
								double ls0 = stockRecord1.getScore0();
								double ls1 = stockRecord1.getScore1() == 0 ? ls0 : stockRecord1.getScore1();
								double rs0 = stockRecord2.getScore0();
								double rs1 = stockRecord2.getScore1() == 0 ? rs0 : stockRecord2.getScore1();
								result = compare(ls0, rs0, orderbyDesc);
								if (result == 0)
									result = compare(ls1, rs1, orderbyDesc);
							}
						}
						if (result == 0) {
							result = compare(stockRecord1.getName(), stockRecord2.getName(), false);
						}
						if (result == 0) {
							result = stockRecord1.getIrg()- stockRecord2.getIrg();
						}
						if (result == 0) {
							result = stockRecord1.getOrg()- stockRecord2.getOrg();
						}
						if (result == 0)
							result = compare(stockRecord1.getPrice(), stockRecord2.getPrice(), false);
						return(result);
					}
					private int compare(String s1, String s2, boolean orderbyDesc) {
						String ss1 = orderbyDesc ? s2 : s1;
						String ss2 = orderbyDesc ? s1 : s2;
						return ss1.compareTo(ss2);
					}
					private int compare(double d1, double d2, boolean orderbyDesc) {
						double dd1 = orderbyDesc ? d2 : d1;
						double dd2 = orderbyDesc ? d1 : d2;
						if (dd1 > dd2)
							return 1;
						else if (dd1 < dd2)
							return -1;
						else
							return 0;
					}
					public boolean equals(Object p_obj) {
						return(this == p_obj);
					}
			});
			return(stockRecords);
	}
	public static double getXRate(SelectUtil p_su, String p_ccy) {
		double xRate = 1.0;
		if(p_ccy != null ){
			RpcClient rpc = p_su.getRpcClient();
			Value v = rpc.callSegment("getxrate", new VectorUtil()
				.addElement(p_ccy)
				.addElement(DateUtil.today())
				.toVector()
			);
			if(v != null) xRate = v.toDouble();
		}
		return xRate;
	}
	public static double getCashDiscountPercent() {
		return cashDiscountPercent;
	}
	public static double getWebConsigpprice(double p_price) {
		return Math.ceil(p_price / (1 - getCashDiscountPercent()));
	}
	public static double getWebStandardprice(double p_price) {
		return Math.ceil(p_price / (1 - getCashDiscountPercent()));
	}
	public boolean createWineList(List<WineCaveStockRecord> p_l,int p_version , OutputStream p_out) throws Exception {
//		String ofname = "c:\\tmp\\winelist_outout.xls";
		ExcelPoi xpoi = ExcelPoi.newExcelPoi("c:\\tmp\\winelist_template.xls",false);
		ExcelPoi.CellRC rc = xpoi.getNameRegion("Date");
		xpoi.excel_setDateValue(rc.row, rc.col, DateUtil.today());
		{

				double disc = cashDiscountPercent;
				if(disc > 0.00) {
					int idisc = (int) (disc * 100.0);
					UniLog.log("discount = " + idisc + "%");
					rc = xpoi.getNameRegion("Notice");
					if(rc != null) {
						xpoi.excel_setStringValue(rc.row, rc.col, ""+idisc+"% off - payment via cash/bank transfer");
					}
				}
		}
		
		String lastRegion = null;
		String lastColor = null;
		rc = xpoi.getNameRegion("FirstHeader");
		int regionStyle = xpoi.excel_getCellStyleIdx(rc.row,rc.col);
		rc = xpoi.getNameRegion("SecondHeader");
		int colorStyle = xpoi.excel_getCellStyleIdx(rc.row,rc.col);
		int currentRow = rc.row;
		for(WineCaveStockRecord wr : p_l) {
//			if(!wr.getRegion().equals(lastRegion)|| (p_version >=2 && !wr.getColor().equals(lastColor))) {
//			if(lastRegion != null) currentRow++;
//			xpoi.excel_setCellStyle(currentRow, 0, regionStyle);
//			if(p_version >= 2 ) {
//			xpoi.excel_setStringValue(currentRow, 0, wr.getMtype() + "-" + wr.getColor()+", "+ wr.getRegion() + "," + wr.getCountry());
//			} else {
//			xpoi.excel_setStringValue(currentRow, 0, wr.getRegion());
//			}
//			xpoi.excel_MergeCells(currentRow, 0, currentRow, 8);
//			lastRegion = wr.getRegion();
//			if(p_version >= 2 ) lastColor = wr.getColor();
//			currentRow++;
//			currentRow++;
//			}
			if(p_version >= 2) {
			if(!v2Region(wr).equals(lastRegion)) {
				if(lastRegion != null) currentRow++;
				xpoi.excel_setCellStyle(currentRow, 0, regionStyle);
				xpoi.excel_setStringValue(currentRow, 0, v2Region(wr));
				xpoi.excel_MergeCells(currentRow, 0, currentRow, 8);
				lastRegion = v2Region(wr);
				currentRow++;
				currentRow++;
				lastColor = null;
			}
			if(!wr.getColor().equals(lastColor)) {
				xpoi.excel_setCellStyle(currentRow, 0, colorStyle);
				xpoi.excel_setStringValue(currentRow, 0, wr.getColor());
				xpoi.excel_MergeCells(currentRow, 0, currentRow, 8);
				lastColor = wr.getColor();
				currentRow++;
			}
			} else {
			if(!wr.getRegion().equals(lastRegion)) {
			if(lastRegion != null) currentRow++;
			xpoi.excel_setCellStyle(currentRow, 0, regionStyle);
			xpoi.excel_setStringValue(currentRow, 0, wr.getRegion());
			xpoi.excel_MergeCells(currentRow, 0, currentRow, 8);
			lastRegion = wr.getRegion();
			currentRow++;
			currentRow++;
			}
			}
			xpoi.excel_setNumericValue(currentRow, 0, wr.getVint());;
			xpoi.excel_setNumericValue(currentRow, 1, wr.getCsQty());
			xpoi.excel_setNumericValue(currentRow, 2, wr.getBtlQty());
			xpoi.excel_setStringValue(currentRow, 3, wr.getPacking());
			xpoi.excel_setStringValue(currentRow, 4, wr.getName());
			xpoi.excel_setStringValue(currentRow, 5, wr.getScore());
			xpoi.excel_setStringValue(currentRow, 6, wr.getWineClass());
			xpoi.excel_setNumericValue(currentRow, 7, wr.getPrice());
			xpoi.excel_setStringValue(currentRow, 8, wr.getCname());
			currentRow++;
		}
		xpoi.writeWorkBook(p_out);
		return(true);
	}
	static String v2Region (WineCaveStockRecord p_wrec) {
		return(p_wrec.getCountry()+" - "+p_wrec.getRegion());
	}
}
