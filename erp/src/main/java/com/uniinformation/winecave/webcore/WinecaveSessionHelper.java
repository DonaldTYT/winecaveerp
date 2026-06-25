package com.uniinformation.winecave.webcore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kikyosoft.rpccall.RpcClient;
import com.kikyosoft.rpccall.Value;
import com.kikyosoft.utils.LogUtil;
import com.kikyosoft.utils.StringUtil;
import com.kyoko.common.CoreLog;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.FunctionInterfaceEx;
import com.uniinformation.bicore.JsonToBiCellCollectionInterface;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.webcore.erpv4.Erpv4SessionHelper;
import com.uniinformation.winecave.WineCaveOrderPayPayDollar;
import com.uniinformation.winecave.WineCaveUtil;

public class WinecaveSessionHelper extends Erpv4SessionHelper {
	TableRec vendorTr;
	BiResult customerBr = null;
	Date lastAccess = null;
	Date lastProfile = null;
	
//	static final String erpapiHost = "192.168.46.13";
	static final String erpapiHost = "192.168.33.3";
	static final int erpapiPort = 5101;

	static class StockItem {
		String icode;
		int org;
	}
	
	synchronized public static SessionHelper getSessionHelper(HttpServletRequest p_request, HttpServletResponse p_response, boolean p_requireNew) {
		return getSessionHelper(p_request, p_response, p_requireNew, () -> new WinecaveSessionHelper());
	}
	@Override
	public boolean loginProceed(String p_loginid, String p_password, boolean test) throws Exception{
			if(!p_loginid.startsWith("weborder:")) {
				return(super.loginProceed(p_loginid, p_password, test));
			}
			try {
				String webloginid = p_loginid.substring(9);
				SelectUtil su = new SelectUtil(); 
				su.init(getJdbcPool());
				vendorTr = su.getQueryResult("select * from vendor where vd_loginid = ? ",new Wherecl().appendArgument(webloginid));
				su.close();
				if(vendorTr.getRecordCount() <= 0) return(false);
				vendorTr.setRecPointer(0);
				if(vendorTr.getFieldString("vd_passwd").equals(p_password)) {
					boolean ok = super.loginProceed("weborder", "Qwe123456",test);
					if(ok) {
						setLastAccess(new Date());
						//setVcode(p_loginid.substring(9));
						setVcode(vendorTr.getFieldString("vd_vcode"));
						/*
						fullName = tr.getFieldString("otlm_name");
						mrg = tr.getFieldIndex("otlm_rg");
						*/
						return(ok);
					}
				}
			} catch (Exception ex) {
				LogUtil.log(ex);
			}
			return(false);
	}
	
	public String getWebLoginId() {
		try {
			return(vendorTr.getFieldString("vd_loginid"));
		} catch (Exception ex) { 
			CoreLog.log(ex);
			return("N/A");
		}
	}
	
	JSONObject listSublinkToJsonxx(JSONObject p_jo,BiResult p_br,String p_joName,String p_subLink,int p_start,int p_cnt, com.uniinformation.utils.whereclpar.Condition p_cond) throws Exception {
		JSONObject jo = p_jo == null ? new JSONObject() : p_jo;
		synchronized(p_br) {
		BiResult sr = p_br.getSubLink(p_subLink);
		if(sr != null) {
			JSONArray ja = new JSONArray();
			Vector<BiCellCollection> rList = sr.getRowCollectionList();
			if(p_cond == null) {
				if(rList != null && rList.size() > p_start)  {
					for(int i=p_start;i < rList.size();i++) {
						BiCellCollection bc = rList.get(i);
						ja.put(sr.listColumnsToJson(bc, null));
						if(ja.length() >= p_cnt) break;
					}
				}
			} else {
				int i=0;
				int stidx = 0;
				for(;i < rList.size() && stidx < p_start;i++) {
					BiCellCollection bc = rList.get(i);
					if(p_cond.eval(bc)) {
						stidx++;
					}
				}
				for(;i < rList.size();i++) {
					BiCellCollection bc = rList.get(i);
					if(p_cond.eval(bc)) {
						ja.put(sr.listColumnsToJson(bc, null));
						if(ja.length() >= p_cnt) break;
					}
				}
			}
			jo.put(p_joName,ja);
		}
		}
		return(jo);
	}
	JSONObject listSublinkToJson(JSONObject p_jo,BiResult p_br,String p_joName,String p_subLink,int p_start,int p_cnt, com.uniinformation.utils.whereclpar.Condition p_cond,String order) throws Exception {
		JSONObject jo = p_jo == null ? new JSONObject() : p_jo;
		synchronized(p_br) {
		BiResult sr = p_br.getSubLink(p_subLink);
		if(sr != null) {
			JSONArray ja = new JSONArray();
			Vector<BiCellCollection> rList = sr.getRowCollectionList();
			if(p_cond == null) {
				if(rList != null && rList.size() > p_start)  {
					for(int i=p_start;i < rList.size();i++) {
						BiCellCollection bc = rList.get(i);
						ja.put(sr.listColumnsToJson(bc, null));
						if(ja.length() >= p_cnt) break;
					}
				}
			} else {
				int i=0;
				int stidx = 0;
				for(;i < rList.size() && stidx < p_start;i++) {
					BiCellCollection bc = rList.get(i);
					if(p_cond.eval(bc)) {
						stidx++;
					}
				}
				for(;i < rList.size();i++) {
					BiCellCollection bc = rList.get(i);
					if(p_cond.eval(bc)) {
						ja.put(sr.listColumnsToJson(bc, null));
						if(ja.length() >= p_cnt) break;
					}
				}
			}
			if(!StringUtils.isBlank(order)) {
				List<JSONObject> jl = new ArrayList();
				for(int i=0;i<ja.length();i++) {
					jl.add(ja.getJSONObject(i));
				}
				jl.sort(new Comparator() {

					@Override
					public int compare(Object o1, Object o2) {
						JSONObject jo1 = (JSONObject) o1;
						JSONObject jo2 = (JSONObject) o2;
						Comparable jv1 = (Comparable) jo1.get(order);
						Comparable jv2 = (Comparable) jo2.get(order);
						// TODO Auto-generated method stub
						return(jv1.compareTo(jv2));
//						return 0;
					}
				});
				ja = new JSONArray(jl);
			}
			jo.put(p_joName,ja);
		}
		}
		return(jo);
	}

			
	synchronized BiResult getCustomerBr() throws Exception {
		if(customerBr == null) {
			customerBr = getBiSchema().getViewByName("graphql.Customer").newBiResult(getLoginId(), null, null, this);
		}
		if(lastProfile == null) {
			customerBr.clear();
			customerBr.clearCondition();
			customerBr.addCustomCondition("vd_customerCode = '"+vendorTr.getFieldString("vd_vcode").trim()+"'");
			customerBr.query();
			if(customerBr.getRecordCount() != 1) return(null);
			customerBr.loadOneRecV(0);
			customerBr.fetchOneRecV(0);
			setLastProfile(new Date());
		}
		return(customerBr);
	}
	public JSONObject getNetPrice(JSONObject p_jo,double p_price) {
		String rtn = WineCaveUtil.getNetUnitPrice(getVcode(), p_price);
		if(rtn != null && rtn.startsWith("OK  ")) {
			int p = Integer.parseInt(rtn.substring(4).trim());
			JSONObject jo = p_jo == null ? new JSONObject() : p_jo;
			jo.put("netprice",p);
			return(jo);
		} else {
			return(errorJson(null,rtn == null ? "null" : rtn.trim()));
		}
	}
	public JSONObject getCustomerProfile(JSONObject p_jo) {
		if(vendorTr == null) return(null);
		try {
			BiResult tCustomerBr = getCustomerBr();
			JSONObject jo = tCustomerBr.listColumnsToJson(tCustomerBr.getCurrentCollection(),p_jo);
			String soCond = p_jo.optString("soCond", null);
			String poCond = p_jo.optString("poCond", null);
			String stCond = p_jo.optString("stCond", null);
			String stOrder = p_jo.optString("stOrder", null);
			if(!StringUtils.isBlank(stOrder)) {
				if(stOrder.equals("inDate")) {
					stOrder = "cst_inDate";
				} else if(stOrder.equals("itemName")) {
					stOrder = "st_iname";
				} else if(stOrder.equals("vintage")) {
					stOrder = "st_vintage";
				} else stOrder = null;
			}
			com.uniinformation.utils.whereclpar.Condition pSO = null;
			com.uniinformation.utils.whereclpar.Condition pST = null;
			com.uniinformation.utils.whereclpar.Condition pPO = null;
			if(!StringUtils.isBlank(stCond)) {
				ReturnMsg rtn = customerBr.getSubLink("graphql.CustStorage").addCustomCondition(stCond, true);
				if(!rtn.getStatus()) {
					rtn = customerBr.getSubLink("graphql.CustStorage").addCustomCondition("st_iname like '"+stCond+"'" , true);
				}
				if(rtn.getStatus()) pST = (com.uniinformation.utils.whereclpar.Condition) rtn.getData();
			}
			int soCnt = 10;
			int stCnt = 10;
			int poCnt = 10;
			if("all".equals(p_jo.optString("soList", null))) {
				soCnt=10000;
			};
			if("all".equals(p_jo.optString("stList", null))) {
				stCnt=10000;
			};
			if("all".equals(p_jo.optString("poList", null))) {
				poCnt=10000;
			};
			
			jo = listSublinkToJson(jo,tCustomerBr,"salesorders","graphql.CustSalesOrder",0,soCnt,pSO,null);
			jo = listSublinkToJson(jo,tCustomerBr,"storage","graphql.CustStorage",0,stCnt,pST,stOrder);
			jo = listSublinkToJson(jo,tCustomerBr,"purchaseorders","graphql.CustConsignPO",0,poCnt,pPO,null);
			jo = listSublinkToJson(jo,tCustomerBr,"transfers","graphql.TfrDetail",0,1000,null,null);
		return(jo);
		} catch (Exception ex) { 
			CoreLog.log(ex);
			return(null);
		}
	}
	
	
	public String createOrder(String loginid,String orderDetail) {
		JSONObject od = new JSONObject(orderDetail);
		JSONObject cd = od.getJSONObject("checkoutDetails");
		JSONArray ia = cd.getJSONArray("lines");
		String addr;
		String delimethod = od.optString("shippingMethod", "");
		if(delimethod.startsWith("Self Pickup")) {
			delimethod = "Self Pickup";
		}
		if(delimethod.startsWith("Pickup At")) {
			delimethod = "Self Pickup";
		}
		if(delimethod.startsWith("Local Delivery")) {
			delimethod = "Local Delivery";
		}
		if(delimethod.startsWith("Standard Delivery")) {
			delimethod = "Standard Delivery";
		}
		if(delimethod.startsWith("Scheduled Delivery")) {
			delimethod = "Scheduled Delivery";
		}
		if(delimethod.startsWith("Self Pickup")) {
			addr = "Self Pickup";
		} else {
			addr = new StringUtil()
				.cat(od.optString("s_street", "")," ")
				.cat(od.optString("s_district", "")," ")
				.cat(od.optString("s_countryArea", "")," ")
				.cat(od.optString("s_country", "")," ")
				.toString()
				;
			
		}
		String name = new StringUtil()
				.cat(od.optString("s_lastName", "")," ")
				.cat(od.optString("s_firstName", "")," ")
				.toString()
				;
		double discount = 0.0;
		String paymentMethod = od.optString("paymentMethod", "");
		double delicharge = od.optDouble("shippingCharge", 0.0);
		String phone = od.optString("s_phone", "");
		/*
		String deliaddr = new StringUtil()
					.cat(name,"\r")
					.cat(addr,"\r")
					.cat(phone,"\r")
					.toString();
					*/
		String deliaddr = addr;
		Vector id = new Vector();
		for(int i=0;i<ia.length();i++) {
			JSONObject it = ia.getJSONObject(i);
			String sku = it.getString("sku");
			StockItem si = getStockItemFromSku(sku);
			id.add(si.icode);
			id.add(si.org);
			id.add(it.getInt("quantity"));
			int qtyPerUnit = it.getInt("qtyPerUnit");
			if(qtyPerUnit > 1) id.add(0); else id.add(1);
			id.add(it.getInt("unitPrice"));
		}
		String result = WineCaveUtil.placeWebOrderNew(
				loginid, 
				paymentMethod,
				delimethod,
				deliaddr,
				discount,
				delicharge,
				ia.length(), id);
		JSONObject jo = new JSONObject();
		if(result != null && result.startsWith("OK  ")) {
			int mrg = Integer.parseInt(StringUtil.strpart(result, 4,10).trim());
			String so=null;
			try {
				SelectUtil su = getBiSchema().getSelectUtil();
				TableRec tr =su.getQueryResult("select stm_ref1 from stmov where stm_mrg = "+mrg);
				tr.setRecPointer(0);
				so = tr.getFieldString("stm_ref1");
			} catch (Exception ex) {
				UniLog.log(ex);
				return(errorJson(null,ex.toString()).toString());
			}
			setLastProfile(null);
			jo.put("ok", true);
			jo.put("SO", so);
		}
		return(jo.toString());
	}

	public String commitOrder(String commitDetail) {
		JSONObject cd = new JSONObject(commitDetail);
		String erpSO = cd.getString("erpSO");
		JSONObject jo = new JSONObject();
		
		try {
		SelectUtil su = getBiSchema().getSelectUtil();
		TableRec tr = su.getQueryResult("select * from stmov where stm_ref1 = '"+erpSO+"'");
		tr.setRecPointer(0);
		int stmrg = tr.getFieldInt("stm_mrg");
		TableRec wtr =su.getQueryResult("select * from weborder where wodr_mrg = " + stmrg);
		if(wtr.getRecordCount() <= 0) {	    	
			JSONObject js = cd.optJSONObject("shippingInfo");
			su.executeUpdate("insert into weborder (wodr_number,wodr_mrg,wodr_id,wodr_firstname,wodr_lastname,wodr_phone,wodr_ctime,wodr_delimethod) values (?,?,?,?,?,?,?,?) ", 
					new Wherecl()
					.appendArgument(cd.optInt("weborder", 0))
					.appendArgument(stmrg)
					.appendArgument(cd.optString("saleorOrderId", ""))
					.appendArgument(js == null ? "" : js.optString("firstName", ""))
					.appendArgument(js == null ? "" : js.optString("lastName", ""))
					.appendArgument(js == null ? "" : js.optString("phone", ""))
					.appendArgument(DateUtil.dateToUnixtime(new Date()))
					.appendArgument(js == null ? "" : js.optString("shippingMethod", ""))
						);
		}
		WineCaveOrderPayPayDollar orderpay = new WineCaveOrderPayPayDollar();
		String pm = tr.getFieldString("stm_pmmethod");
		if(pm.equals("Online")) {
			String url = orderpay.getPayUrl();
			int lang=0;
			String locale = cd.optString("locale");
			if("zh-Hant".equals(locale)) lang=1;
			String param = orderpay.getRequestParams(stmrg, lang, this);
			jo.put("params", new JSONObject(param));
			jo.put("url", url);
			jo.put("requirePayment" , true);
		}
		
		jo.put("ok", true);
		setLastProfile(null);
		return(jo.toString());
		} catch (Exception ex) {
			UniLog.log(ex);
			return(errorJson(null,ex.toString()).toString());
		}
	}

	public String clientTransfer(String commitDetail) {
		JSONObject jo = new JSONObject(commitDetail);
		int language = 0;
		String vcode = getVcode();
		int irg = jo.getInt("irg");
		int org = jo.getInt("org");
		int botpercase = jo.getInt("botpercase");
		int storQty = jo.getInt("storqty");
		int consignQty = jo.getInt("conqty");
		String loc = "STOR";
		int qty = storQty;
		if(jo.getString("transferSource").equals("consignment")) {
			 loc = "WH01";
			 qty = consignQty;
		}
		Vector clist = new Vector();
		JSONArray ja = jo.getJSONArray("rows");
		for(int i=0;i<ja.length();i++) {
			JSONObject ji = ja.getJSONObject(i);
			if(!StringUtils.isBlank(ji.getString("loginId"))) {
				clist.add(ji.getString("loginId"));
				clist.add(ji.getInt("caseQty") * botpercase + ji.getInt("btlQty"));
			}
		}
		String xx = WineCaveUtil.clientStockTransfer(language,vcode, irg,org, loc,qty,clist);
//		String xx = "OK  ";
		
		if(xx.startsWith("OK")) {
			jo = new JSONObject();
			jo.put("ok", true);
			setLastProfile(null);
		} else {
			jo = new JSONObject();
			jo.put("ok",false);
			jo.put("message",xx != null ? xx : "(null)");
		}
		return(jo.toString());
	}
	
	
	public void syncProductToSaleor(JSONObject p_argument) {
		RpcClient erpRpc = new RpcClient (erpapiHost ,erpapiPort, 10000);
		erpRpc.open();
		Value v = erpRpc.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.login",
					new VectorUtil()
						.addElement("")
						.addElement("hlv")
						.addElement("k2khlv")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("APIOLD Login failed");
			return;
		}
		v = erpRpc.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.view",
					new VectorUtil()
						.addElement("wc.stocklist")
						.addElement("ProductRecord")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API open productrecord view failed");
			return;
		}
		v = erpRpc.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.call",
					new VectorUtil()
						.addElement("ProductRecord")
						.addElement("syncProductRecords")
						.addElement(p_argument.toString())
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("call to syncProductRecord failed");
			return;
		}
		return;
	}
	public String updateConsignment(String commitDetail) {
		JSONObject jo = new JSONObject(commitDetail);
		int language = 0;
		String vcode = getVcode();
		int irg = jo.getInt("irg");
		int org = jo.getInt("org");
		int botpercase = jo.getInt("botpercase");
		int storQty = jo.getInt("storageCase") * botpercase + jo.getInt("storageBtl");
		int consignQty = jo.getInt("consignmentCase") * botpercase + jo.getInt("consignmentBtl");
		int purchasedQty = jo.getInt("purchasedCase") * botpercase + jo.getInt("purchasedBtl");
		boolean salebybot = jo.getBoolean("saleByBottle");
		int price = jo.getInt("sellingPrice");
		
		String xx = WineCaveUtil.updateConsignment(language,vcode, irg,org, storQty,consignQty,salebybot, price, purchasedQty);
		
		if(xx.startsWith("OK")) {
			jo = new JSONObject();
		    JSONArray ja = new JSONArray();
		    ja.put(irg);
		    jo.put("irgList", ja);
		    syncProductToSaleor(jo);
		    
		    
			jo = new JSONObject();
			jo.put("ok", true);
			setLastProfile(null);
		} else {
			jo = new JSONObject();
			jo.put("ok",false);
			jo.put("message",xx != null ? xx : "(null)");
		}
		return(jo.toString());
	}
	
	static JSONObject errorJson(JSONObject p_jo, String message) {
		JSONObject jo = (p_jo == null ? new JSONObject() : p_jo);
		jo.put("ok",false); 
		jo.put("message",message);
		return(jo);
	}
	static JSONObject okJson(JSONObject p_jo, String message) {
		JSONObject jo = (p_jo == null ? new JSONObject() : p_jo);
		jo.put("ok",true); 
		jo.put("message",message);
		return(jo);
	}
	
	
	static StockItem getStockItemFromSku(String sku) {
		int idx = -1;
		int ofs = 0;
		for(;;) {
			int i = sku.substring(idx+1).indexOf("-");
			if( i < 0 ) break;
			idx += i+1;
		}
		if(idx < 0) return(null);
		StockItem si = new StockItem();
		si.icode = sku.substring(0, idx).toUpperCase();
		si.org = Integer.parseInt(sku.substring(idx+1));
		return(si);
	}
	
	static public JSONObject getStockAvailability(SessionHelper sp,List<String> slugs) throws Exception {
		HashSet<Integer> irgs = new HashSet<Integer>();
		Hashtable<Integer,String> slugHash = new Hashtable<Integer,String>();
		for(String slug : slugs ) {
			int idx = -1;
			int ofs = 0;
			for(;;) {
				int i = slug.substring(idx+1).indexOf("-");
				if( i < 0 ) break;
				idx += i+1;
			}
			if(idx < 0) {
				CoreLog.log("got invald slug " + slug);
				continue;
			}
			Integer irg = Integer.parseInt(slug.substring(idx+1).trim());
			irgs.add(irg);
			slugHash.put(irg,slug);
		}
		SelectUtil su =  sp.getBiSchema().getSelectUtil();	
		/*
		TableRec tr = su.getQueryResult(
				"select pdls_irg,pdls_org,st_icode,pdls_stockqty,consgp_price,consgp_salebybtl,st_msize1,or_cocode,st_standardprice,st_issalable from stock , orders, podetlocstatus, outer consgpreal ",
								new Wherecl().genInList("and", "pdls_irg", "in", icodes).appendString( "and pdls_irg = st_irg and consgp_irg = pdls_irg and or_org = pdls_org and consgp_ctime = 0 and consgp_org = pdls_org and pdls_loc = 'WH01'")
						);
						*/
		TableRec tr = su.getQueryResult(
				"select pdls_irg,pdls_org,st_icode,pdls_stockqty,consgp_price,consgp_salebybtl,st_msize1,or_cocode,st_standardprice,st_issalable from stock , podetlocstatus, orders, outer consgpreal ",
								new Wherecl()
									.genInList("and", "st_irg", "in", irgs)
									.appendString( "and pdls_irg = st_irg and consgp_irg = pdls_irg and or_org = pdls_org and consgp_ctime = 0 and consgp_org = pdls_org and pdls_loc = 'WH01'")
									.setOrderby("pdls_irg")
						);
		su.close();
		int lastIrg = -1;
		double lastQty = 0;
		double lastQtyPerCase = 0;
		JSONArray ja = new JSONArray();
		for(int i=0;i<tr.getRecordCount();i++) {
			tr.setRecPointer(i);
			int irg = tr.getFieldInt("pdls_irg");
			if(irg != lastIrg) {
				if(lastIrg > 0) {
				JSONObject jd = new JSONObject();
				jd.put("slug", slugHash.get(lastIrg));
				jd.put("qty", lastQty);
				jd.put("qtypercase", lastQtyPerCase);
				ja.put(jd);
				}
				lastIrg = irg;
				lastQty = 0;
				lastQtyPerCase = tr.getFieldDouble("st_msize1");
			}
			String owner = tr.getFieldString("or_cocode");
			double qty = 0;
			double price = 0.0;
			if("WINECAVE".equals(owner)) {
				price = tr.getFieldDouble("st_standardprice");
			} else{
				price = tr.getFieldDouble("consgp_price");
			}
			if(price > 0.0) lastQty += tr.getFieldDouble("pdls_stockqty");
		}
		if(lastIrg > 0) {
			JSONObject jd = new JSONObject();
			jd.put("slug", slugHash.get(lastIrg));
			jd.put("qty", lastQty);
			jd.put("qtypercase", lastQtyPerCase);
			ja.put(jd);
		}
		JSONObject jo = new JSONObject();
		jo.put("ok", true);
		jo.put("availability", ja);
		return(jo);
	}
	
//	static public JSONObject getStockAvailability(SessionHelper sp,List<String> skus) throws Exception {
//		HashSet<String> icodes = new HashSet<String>();
//		HashSet<String> skuset = new HashSet<String>();
//		for(String sku : skus) {
//			int idx = -1;
//			int ofs = 0;
//			for(;;) {
//				int i = sku.substring(idx+1).indexOf("-");
//				if( i < 0 ) break;
//				idx += i+1;
//			}
//			if(idx < 0) {
//		//		return(errorJson(null,"invalid sku"));
//				continue;
//			}
////			int org = Integer.parseInt(sku.substring(idx+1));
//			icodes.add(sku.substring(0, idx).toUpperCase());
//			skuset.add(sku);
//		}
//		SelectUtil su =  sp.getBiSchema().getSelectUtil();	
//		TableRec tr = su.getQueryResult(
//				"select pdls_irg,pdls_org,st_icode,pdls_stockqty,consgp_price,consgp_salebybtl,st_msize1,or_cocode,st_standardprice,st_issalable from stock , orders, podetlocstatus, outer consgpreal ",
//								new Wherecl().genInList("and", "st_icode", "in", icodes).appendString( "and pdls_irg = st_irg and consgp_irg = pdls_irg and or_org = pdls_org and consgp_ctime = 0 and consgp_org = pdls_org and pdls_loc = 'WH01'")
//						);
//		su.close();
//		JSONArray ja = new JSONArray();
//		for(int i=0;i<tr.getRecordCount();i++) {
//			tr.setRecPointer(i);
//			String sku = BiCellCollection.makeSlug(
//						tr.getFieldString("st_icode"),
//					    ""+tr.getFieldInt("pdls_org")
//					);
//			if(skuset.contains(sku)) {
//				String owner = tr.getFieldString("or_cocode");
//				double qty = 0;
//				double price = 0.0;
//				if("WINECAVE".equals(owner)) {
//					price = tr.getFieldDouble("st_standardprice");
//				} else{
//					price = tr.getFieldDouble("consgp_price");
//				}
//				if(price > 0.0) qty = tr.getFieldDouble("pdls_stockqty");
//				JSONObject jd = new JSONObject();
//				jd.put("sku", sku);
//				jd.put("qty", qty);
//				jd.put("qtypercase", tr.getFieldDouble("st_msize1"));
//				ja.put(jd);
//			}
//		}
//		JSONObject jo = new JSONObject();
//		jo.put("ok", true);
//		jo.put("availability", ja);
//		return(jo);
//	}
	
	static public JSONObject getStockDetail(SessionHelper sp,String sku) throws Exception {
		int idx = -1;
		int ofs = 0;
		for(;;) {
			int i = sku.substring(idx+1).indexOf("-");
			if( i < 0 ) break;
			idx += i+1;
		}
		if(idx < 0) {
			return(errorJson(null,"invalid sku"));
		}
		int org = Integer.parseInt(sku.substring(idx+1));
		String icode = sku.substring(0, idx).toUpperCase();
		SelectUtil su =  sp.getBiSchema().getSelectUtil();	
		TableRec tr = su.getQueryResult("select pdls_stockqty,consgp_price,consgp_salebybtl,st_msize1,or_cocode,st_standardprice,st_issalable from stock , podetlocstatus, orders, outer consgpreal where st_icode = ? and pdls_irg = st_irg and pdls_org = ? and consgp_irg = pdls_irg and or_org = pdls_org and consgp_ctime = 0 and consgp_org = pdls_org and pdls_loc = 'WH01'",
								new Wherecl().appendArgument(icode).appendArgument(org)
						);
		if(tr.getRecordCount() != 1) {
			return(errorJson(null,"item not available"));
		}
		tr.setRecPointer(0);
		JSONObject jo = new JSONObject();
		jo.put("ok", true);
		double price;
		String cocode = tr.getFieldString("or_cocode");
		if("WINECAVE".equals(cocode)) {
			price = tr.getFieldDouble("st_standardprice");
		} else {
			price = tr.getFieldDouble("consgp_price");
		}
		price = WineCaveUtil.getWebConsigpprice(price);
		jo.put("price", price);
		int qty = (int) tr.getFieldDouble("pdls_stockqty");
		String sb = tr.getFieldString("consgp_salebybtl");
		if("WINECAVE".equals(cocode)) {
			sb = tr.getFieldString("st_issalable");
		}
		if("Y".equals(sb)) {
//			jo.put("moq", 1);
			jo.put("qty", qty);
			if(qty <= 0) return(errorJson(null,"out of stock"));
			jo.put("baseUnit","btl");
			jo.put("sellUnit","btl");
			jo.put("qtyPerUnit",1);
		} else {
			int bpc = (int) tr.getFieldDouble("st_msize1");
			qty = (qty / bpc) * bpc;
			if(qty <= 0) return(errorJson(null,"out of stock"));
			jo.put("qty", qty);
//			jo.put("moq", bpc);
			jo.put("baseUnit","btl");
			jo.put("sellUnit","case");
			jo.put("qtyPerUnit",bpc);
		}
		return(jo);
	}
	
	synchronized public void setLastAccess(Date p_date) {
		lastAccess = p_date;
	}

	synchronized public void setLastProfile(Date p_date) {
		lastProfile = p_date;
	}

	public Date getLastAccess() {
		return(lastAccess);
	}

	public String sendMessage(String messageDetail) {
		JSONObject jo = new JSONObject(messageDetail);
		BiUtil.sendEmail(
				Pair.of("storage@winecavehk.com", (String) null),
				new VectorUtil()
				.addElement(Pair.of("tyt223@gmail.com",(String) null))
				.addElement(Pair.of("fai@winecavehk.com",(String) null))
				.toVector(),
				null, 
				null, 
				"Message from Web on " + new java.util.Date().toString() + " from " + new StringUtil().cat(jo.getString("firstName"), " ").cat(jo.getString("lastName"), " ").toString(),
				null, 
				new StringUtil()
					.cat(jo.getString("firstName"), " ")
					.cat(jo.getString("lastName"), " ")
					.cat(jo.getString("message"), "\n")
					.cat(jo.getString("contactNumber"), "\n")
					.cat(jo.getString("email"), "\n")
				.toString(),
				null, this);
	
		jo = new JSONObject();
		jo.put("ok", true);
		jo.put("message", "Your Message Has Been Sent");
		return(jo.toString());
	}

	public JSONObject getCustomerStockList(JSONObject p_jo) {
		if(vendorTr == null) return(null);
		try {
			BiResult tCustomerBr = getCustomerBr();
			BiResult custStoraged = getBiSchema().getViewByName("graphql.CustStoraged").newBiResult(getLoginId(), null, null, this);
			JSONObject jo = (p_jo == null) ? new JSONObject() : p_jo;
			jo.put("columns", custStoraged.getListColumnsAsJson());
			JSONArray ja = new JSONArray();
			custStoraged.addCustomCondition("cstd_cocode ='"+vendorTr.getFieldString("vd_vcode")+"'");
			custStoraged.query();
			for(int i=0;i < custStoraged.getRowCount();i++) {
				custStoraged.loadOneRecV(i);
				ja.put(custStoraged.listColumnsToJson(custStoraged.getCurrentCollection(),null));
			}
			jo.put("records", ja);
			jo.put("ok", true);
			return(jo);
		} catch (Exception ex) { 
			CoreLog.log(ex);
			return(null);
		}
	}

	public String resetPasswd(String loginid,String email) throws Exception {
		if(StringUtils.isBlank(loginid)) {
			return(errorJson(null,"Please enter loginid").toString());
		}
		if(StringUtils.isBlank(email)) {
			return(errorJson(null,"Please enter email").toString());
		}
		SelectUtil su = getBiSchema().getSelectUtil();
		TableRec tr = su.getQueryResult("select * from vendor where vd_loginid = '"+loginid+"'");
		if(tr.getRecordCount() != 1) {
			return(errorJson(null,"Login/Email Not Exist").toString());
		}
		tr.setRecPointer(0);
		if( !email.equals(tr.getFieldString("vd_email")))  {
			return(errorJson(null,"Login/Email Not Exist").toString());
		}
		
		BiUtil.sendEmail(
				Pair.of("storage@winecavehk.com", (String) null),
				new VectorUtil()
				.addElement(Pair.of(email,(String) null))
				.toVector(),
				null, 
				null, 
				"Winecave System on " + new java.util.Date().toString(),
				null, 
				new StringUtil()
					.cat("Dear Customer","\n")
					.addline()
					.cat("Your current password for the registered login account is", " ")
					.cat(tr.getFieldString("vd_passwd"), "\n")
				.toString(),
				null, this);
		
		
		JSONObject jo = new JSONObject();
		jo.put("ok", true);
		jo.put("message", "Your Password Has Been Sent To Your Email");
		return(jo.toString());
	}
	public byte[] getDocument(String docType, String docCode) throws Exception {
		com.uniinformation.rpccall.RpcClient rpc = getRpcClient();
		if(docType.equals("invoice")) {
				SelectUtil su = getBiSchema().getSelectUtil();
				TableRec tr =su.getQueryResult("select stm_mrg from stmov where stm_ref1 = '"+ docCode + "'");
				tr.setRecPointer(0);
				int mrg = tr.getFieldInt("stm_mrg");
				com.uniinformation.rpccall.Value v;
				
				
				ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
				rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
				v = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
//				v = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(ZkUtil.getWebContentRealPath("images", true)) .toVector());	
				
				v = rpc.callSegment("winecave_print_invoice",
							new VectorUtil()
							.addElement(mrg)
							.addElement(0)
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement("A4P")
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
						);
				if(v != null && v.toString().startsWith("OK  ")) {
					String docPath = v.toString().substring(4);
//					docPath="/tmp/abc";
					try {
						InputStream is = newErpFileInputStream(docPath);
//						ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
						ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
						
						ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
							@Override
							public byte[] getImage(String p_key) {
								String url=null;
								if(p_key.startsWith(ChnftrParser.GETIMAGE_TAG)) {
									url = SessionHelper.URLHEADER_FILING+p_key.substring(ChnftrParser.GETIMAGE_TAG.length());
								} else {
									url = p_key;
								}
								try {
									return(newErpFileToByteArray(url));
								} catch (Exception ex) {
									UniLog.log(ex);
									return(null);
								}
								
							}});	
						ps.setUseGetImageInterfaceByDefault(true);	
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						byte[] ba = bos.toByteArray();
						bos.close();
						return(ba);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
		}
		if(docType.equals("cnpo")) {
				SelectUtil su = getBiSchema().getSelectUtil();
				TableRec tr =su.getQueryResult("select stm_mrg from stmov where stm_ref1 = '"+ docCode + "'");
				tr.setRecPointer(0);
				int mrg = tr.getFieldInt("stm_mrg");
				String cocode = customerBr.getCellString("vd_customerCode");
				com.uniinformation.rpccall.Value v;
				
				
				ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
				rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
				v = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
//				v = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(ZkUtil.getWebContentRealPath("images", true)) .toVector());	
				
				v = rpc.callSegment("winecave_print_cnpo",
							new VectorUtil()
							.addElement(mrg)
							.addElement(0)
							.addElement(cocode)
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement("A4P")
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
						);
				if(v != null && v.toString().startsWith("OK  ")) {
					String docPath = v.toString().substring(4);
//					docPath="/tmp/abc";
					try {
						InputStream is = newErpFileInputStream(docPath);
//						ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
						ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
						
						ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
							@Override
							public byte[] getImage(String p_key) {
								String url=null;
								if(p_key.startsWith(ChnftrParser.GETIMAGE_TAG)) {
									url = SessionHelper.URLHEADER_FILING+p_key.substring(ChnftrParser.GETIMAGE_TAG.length());
								} else {
									url = p_key;
								}
								try {
									return(newErpFileToByteArray(url));
								} catch (Exception ex) {
									UniLog.log(ex);
									return(null);
								}
								
							}});	
						ps.setUseGetImageInterfaceByDefault(true);	
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						byte[] ba = bos.toByteArray();
						bos.close();
						return(ba);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
		}
		rpc.close();
		return(null);
	}

	public JSONObject saveCustomerProfile(String jsonString) {
		JSONObject jo = new JSONObject(jsonString);
		if(vendorTr == null) return(null);
		try {
			
			BiResult tCustomerBr = getCustomerBr();
			String newPassword = jo.optString("newPassword");
			if(!StringUtils.isBlank(newPassword)) {
				String oldPassword = jo.getString("oldPassword");
				if(!oldPassword.equals(tCustomerBr.getCellString("vd_password"))) {
					return(errorJson(null,"Invalid Login"));
				}
				synchronized(tCustomerBr) {
					SelectUtil su = tCustomerBr.getSelectUtil();
					su.executeUpdate("update vendor set vd_passwd = ? where vd_vcode = ? ", 
									new Wherecl().appendArgument(newPassword).appendArgument(tCustomerBr.getCellString("vd_customerCode"))
								);
				}
			} else {
				BiResult sCustomerBr = getBiSchema().getViewByName("graphql.Customer").newBiResult(getLoginId(), null, null, this);
				sCustomerBr.clear();
				sCustomerBr.clearCondition();
				sCustomerBr.addCustomCondition("vd_customerCode = '"+tCustomerBr.getCellString("vd_customerCode")+"'");
				sCustomerBr.query();
				if(sCustomerBr.getRowCount() != 1) {
					return(errorJson(null,"Customer Not Exist"));
				}
				sCustomerBr.loadOneRecV(0);
				sCustomerBr.fetchOneRecV(0);
				JsonToBiCellCollectionInterface.JsonToBiCellCollection(sCustomerBr.getCurrentCollection(), jo, null);
				ReturnMsg rtn = sCustomerBr.updateCurrent();
				if(rtn != null && !rtn.getStatus()) {
					return(errorJson(null,"Update Failed "+rtn == null ? "" : rtn.getMsg()));
				}
				setLastProfile(null);
			}
			return(okJson(null,"Saved"));
		} catch (Exception ex) { 
			CoreLog.log(ex);
			return(null);
		}
	}

	public JSONObject getShipInfo(JSONObject p_jo) {
		if(vendorTr == null) return(null);
		try {
			BiResult tCustomerBr = getCustomerBr();
			JSONObject jo = tCustomerBr.listColumnsToJson(tCustomerBr.getCurrentCollection(),p_jo);
			return(jo);
		} catch (Exception ex) { 
			CoreLog.log(ex);
			return(null);
		}
	}
	@Override
	public String loginGetId(String p_loginStr) {
		if (p_loginStr == null) {
			return "";
		}
		return(p_loginStr.trim());
	}

	public String getGoogleMerchant() {
		RpcClient rpcOld = new RpcClient("192.168.33.3",5101,10000);
//		RpcClient rpcOld = new RpcClient("192.168.46.13",5101,10000);
		rpcOld.open();
		Value v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.login",
					new VectorUtil()
//						.addElement("winecaveold")
						.addElement("")
						.addElement("hlv")
						.addElement("k2khlv")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("APIOLD Login failed");
			return(null);
		}
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.view",
				new VectorUtil()
					.addElement("wc.stocklist")
					.addElement("ProductRecord")
					.toVector()
			);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API open productrecord view failed");
			return(null);
		}
		rpcOld.setTimeout(3600000);
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.call",
					new VectorUtil()
						.addElement("ProductRecord")
						.addElement("downLoadGoogleMerchant")
						.addElement("{}")
						.toVector()
				);
		if(v == null) {
			LogUtil.log("call to downloadGoogelMerchat failed");
			return(null);
		}	
		String ss = v.toString();
		return(ss);
	}
}
