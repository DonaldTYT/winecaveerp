package com.uniinformation.winecave;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.text.*;

import javax.mail.*; 
import javax.mail.internet.*;
import javax.servlet.http.*;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.uniinformation.utils.*;
import com.uniinformation.utils.poi.ExcelPoi;
//import com.uniinformation.unidata.*;
import com.uniinformation.webcore.SessionHelper;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.rpccall.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
public class WineCaveApiUtil {
	static String MAJORWEB = "MAJORWEB";
	static String STOCK_IMAGE_PATH = "/yic/v/erp_v3/message/STOCK_IMAGE";
	static String STORAGE_IMAGE_PATH = "/yic/v/erp_v3/message/ORDIMG_IMAGE";
	
	
	
	public static int apiPlaceWebOrder(SessionHelper p_sh, int p_itemCount, Vector p_itemDetails) throws Exception{
		return apiPlaceWebOrder(p_sh, p_itemCount, p_itemDetails, new HashMap());
	}
	public static int apiPlaceWebOrderByJson(SessionHelper p_sh, String p_jsonStr) throws Exception{
		SelectUtil su = null; //TODO get su from sh
		BiSchema schema = BiSchema.loadSchema(p_sh);
		su = new SelectUtil();
		su.setLoginId(p_sh.getLoginId());
		su.init(schema.getConn());
		RpcClient perfsvr = su.getRpcClient();
		if(perfsvr.isConnected() != true) {
			UniLog.log("reprintOrder Connect to jdbc server error");
			//perfsvr.close();
			//perfsvr = null;
			throw new Exception("FAIL Connection Error");
		} else {
			Value val = perfsvr.callSegment("WineCaveConnection",
						new VectorUtil()
						.addElement("placeOrderByJson")
						.addElement(p_jsonStr)
						.toVector()
					);
			UniLog.log("call to rpcserver got " + val);
			if(val != null) {

				String rtnStr = val.toString();
				if (StringUtils.startsWith(rtnStr, "OK")){
					int orderRef = Integer.parseInt(rtnStr.substring(4,14).trim());

					JSONObject jo = new JSONObject(p_jsonStr);
					String email = jo.optString("billing_email");
					int webOrderNo = jo.optInt("wc_order_id");
					if(email != null && !email.equals("")) {
						File tempFile = File.createTempFile("winv", ".pdf");
						
						String invFile = wcPrintInvoice(p_sh,orderRef);
						InputStream is = p_sh.newErpFileInputStream(invFile);
//						ChnftrParser ps = new ChnftrParser(is,"");
						FileOutputStream fos = new FileOutputStream(tempFile);
//						ps.print(fos);
						ChnftrRpcServlet.streamChnftrToPdf(is,fos,p_sh);
						fos.close();
						ArrayList<Pair<String,String>> toList = new ArrayList();
						toList.add( Pair.of( email, ""));
						EmailAttachment ema = new EmailAttachment();
						ema.setDescription("Invoice");
						ema.setPath(tempFile.getPath());
						ema.setName("Invoice_"+webOrderNo+".pdf");
						ArrayList<EmailAttachment>emaList = new ArrayList();
						emaList.add(ema);
						ZkUtil.sendEmail(
								Pair.of( "sales@wineac.com",""), toList, null, "Invoice for Web Order #"+webOrderNo, null, 
								"Thank you for your order,\n Attached please find the invoice for order #" + webOrderNo + " \n" , 
								emaList, p_sh);
					} 
//					{
//						ArrayList<Pair<String,String>> toList = new ArrayList();
//						toList.add( Pair.of( "sales@wineac.com", ""));
//						ZkUtil.sendEmail(
//								Pair.of( "sales@wineac.com",""), toList, null, "Web Order Received", null, "There is a new Web Order Reference " + orderRef + " \nReceived, Please followup" , null, p_sh);
//					}
					TableRec tr = su.getQueryResult("select distinct stm_ref1,vd_email,or_cocode from stmovd,orders,vendor,stmov where stmd_mrg = " + orderRef + " and stm_mrg = stmd_mrg and stmd_tdtype = 'MO' and or_org = stmd_org and vd_vcode = or_cocode");
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						email = tr.getFieldString("vd_email");
//						email = "tyt223china@outlook.com";
						if(email != null && !email.equals("")) {
							String poFile = wcPrintPO(p_sh,orderRef,tr.getFieldString("or_cocode"));
							if(poFile != null && !poFile.equals("")) {

						File tempFile = File.createTempFile("winv", ".pdf");
						
						InputStream is = p_sh.newErpFileInputStream(poFile);
//						ChnftrParser ps = new ChnftrParser(is,"");
						FileOutputStream fos = new FileOutputStream(tempFile);
//						ps.print(fos);
						ChnftrRpcServlet.streamChnftrToPdf(is,fos,p_sh);
						fos.close();
						String po="CPO"+tr.getFieldString("stm_ref1").substring(2)+"-"+tr.getFieldString("or_cocode");
						ArrayList<Pair<String,String>> toList = new ArrayList();
						toList.add( Pair.of( email, ""));
						EmailAttachment ema = new EmailAttachment();
						ema.setDescription("Purchase Order");
						ema.setPath(tempFile.getPath());
						ema.setName(po+".pdf");
						ArrayList<EmailAttachment>emaList = new ArrayList();
						emaList.add(ema);
						ZkUtil.sendEmail(
								Pair.of( "sales@wineac.com",""), toList, null, "Purchase Order for Order #"+po, null, 
								"\n Attached please find the purchase order for #" + po + " \n" , 
								emaList, p_sh);
								
							}
						}
					}
					return (orderRef);
				}
				throw new Exception("Error " + rtnStr);
			} throw new Exception("Error call to Perf return null");
		}
//		return("OK  /tmp/testinv.jpg");
		
	}
	public static int apiPlaceWebOrder(SessionHelper p_sh, int p_itemCount, Vector p_itemDetails, Map p_customParamMap) throws Exception{
		SelectUtil su = null; //TODO get su from sh
		BiSchema schema = BiSchema.loadSchema(p_sh);
		su = new SelectUtil();
		su.setLoginId(p_sh.getLoginId());
		su.init(schema.getConn());
		UniLog.log1("DEBUG CustomParamMap: %s", MapUtil.getString(p_customParamMap, "customOrderNo","")); //TODO need to handle customOrderNo
		try{
			String rtnStr = apiPlaceWebOrder(su,p_itemCount,p_itemDetails,MapUtil.getString(p_customParamMap,"customOrderNo",""));
			if (StringUtils.startsWith(rtnStr, "OK")){
				return (Integer.parseInt(rtnStr.substring(4,14).trim()));
			}
			throw new Exception(rtnStr);
		}
		catch(Exception ex){
			throw new Exception("apiPlaceWebOrder error: " + ex.getMessage());
		}
		finally{
			if (su != null) su.close();
		}
	}
	/*
	  apiCall to  placeOrder
	  p_su : the SelectUtil get from biSchema
	  p_itemCount : number of item to order
	  p_item_Detail vector of details of order item  
	   {
	   	 vector size = p_itemCount * 5
	   	 each order item has 5 arguments 
	   	 arg0 = Major's sku for this item (i.e. st_oicode in our stock table)
	   	 arg1 = org (lot number)
	   	 arg2 = quantity (always quantity in bottle, no matter it is sold by bottle or case
	   	 arg3 = 0 : sold by case, 1 : sold by bottle
	   	 arg4 = selling proce 
	   }
	   
	   return String "OK  ....." (the first 4 char is "OK  ") if successful, "FAIL..." if failed 
	 */
	public static String apiPlaceWebOrder(SelectUtil p_su,int p_itemCount,Vector p_item_Detail,String p_customPO) throws Exception{
		return(
			genPlaceWebOrder(p_su,MAJORWEB,"Cash", "Self Pickup", "", p_itemCount,p_item_Detail,p_customPO)
				);
	}
	public static String genPlaceWebOrder(SelectUtil p_su,String p_loginid,String p_paymentMethod, String p_shipping, String p_deliveryAddress, int p_itemCount,Vector p_item_Detail,String p_customPO) throws Exception {
		UniLog.log("HAHA Place Order = "+p_loginid + ", " + p_paymentMethod +", "+p_shipping+", "+p_deliveryAddress+", "+p_itemCount);
//		if(true){
//			return("OK  0000000001");
//		}
		for(int i = 0;i< p_itemCount;i++) {
//			int irg = ((Integer) p_item_Detail.get(i * 5 + 0)).intValue();
			String oicode = ((String) p_item_Detail.get(i * 6 + 0));
			int irg = ((Integer) p_item_Detail.get(i * 6 + 1)).intValue();
			int org = ((Integer) p_item_Detail.get(i * 6 + 2)).intValue();
			int qty = ((Integer) p_item_Detail.get(i * 6 + 3)).intValue();
			int isbot = ((Integer) p_item_Detail.get(i * 6 + 4)).intValue();
			double iprice = ((Double) p_item_Detail.get(i * 6 + 5)).doubleValue();
//			double price = iprice;
//			price = price / 100;
			UniLog.log("Item " + i + " " + /* irg */ oicode + " " + irg + " " + org + " " + qty + " " + isbot + " " + iprice );
		}
		//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
//		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
// 		perfsvr.open();
		RpcClient perfsvr = p_su.getRpcClient();
		if(perfsvr.isConnected() != true) {
			UniLog.log("reprintOrder Connect to jdbc server error");
			//perfsvr.close();
			//perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
		UniLog.log("Place Order " + p_loginid + "," + p_shipping + "," + p_itemCount);
			//perfsvr.setDebug(true);
			Vector v = new Vector();
			/*
			v.add("placeOrderNew");
			v.add(p_loginid);
			v.add(p_paymentMethod);
			v.add(p_shipping);
			v.add(StringUtil.stripNonPrintable(p_deliveryAddress));
			v.add(p_itemCount);
			*/
			v.add("placeOrderWithArgcnt");
			v.add(9);
			v.add(p_loginid);
			v.add(p_paymentMethod);
			v.add(p_shipping);
			v.add(StringUtil.stripNonPrintable(p_deliveryAddress));
			v.add(0.00); // this is the discount field
			v.add(0.0); // this is the delivery charge field
			v.add(p_itemCount);
			v.add(p_customPO);
			for(int i = 0;i< p_itemCount;i++) {
				int irg = ((Integer) p_item_Detail.get(i * 6 + 1)).intValue();
				TableRec tr = null;
				if( irg <= 0) {
					String oicode  = ((String) p_item_Detail.get(i * 6 + 0));
					if(oicode == null || oicode.trim().equals("")) {
						return("FAIL Both Item oicode and irg is empty");
					}
					tr = p_su.getQueryResult("select * from stock where st_oicode = '" + oicode + "'");
				} else {	
					tr = p_su.getQueryResult("select * from stock where st_irg = " + irg);
				}
				if(tr.getRecordCount() != 1) {
					return("FAIL Item oicode not exist or not unique");
				}
				tr.setRecPointer(0);
				if(irg <= 0) irg = tr.getFieldInt("st_irg");
				int org = ((Integer) p_item_Detail.get(i * 6 + 2)).intValue();
				int qty = ((Integer) p_item_Detail.get(i * 6 + 3)).intValue();
				int isbot = ((Integer) p_item_Detail.get(i * 6 + 4)).intValue();
				double price = ((Double) p_item_Detail.get(i * 6 + 5)).doubleValue();
				if(org > 0) {
				TableRec tr2 = p_su.getQueryResult("select * from orders where or_org = " + org);
				if(tr2.getRecordCount() != 1) {
					return("FAIL Item Order code not exist");
				}
				if(price <= 0.0) {
					tr2.setRecPointer(0);
					if("WINECAVE".equals(tr2.getField("or_cocode"))) {
						price = tr.getFieldDouble("st_retailprice");
					}
				}
				}
				v.add(irg);
				v.add(org);
				v.add(qty);
				v.add(isbot);
				v.add(price);
			}

			val = perfsvr.callSegment("WineCaveConnection",v);
			UniLog.log("call to rpcserver got " + val);
			//perfsvr.close();
			//perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
//		return("OK  /tmp/testinv.jpg");
	}
	
	public static String wcPrintPO(SessionHelper p_sh,int p_mrg,String p_cocode) {
		RpcClient rpc = p_sh.getRpcClient();
		Value val = rpc.callSegment("printer_autoselect",
				new VectorUtil()
				.addElement(1)
				.toVector()
			);
//		val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(ZkUtil.getWebContentRealPath("images", true)).toVector());

		val = rpc.callSegment("erpv3_print_po",
				new VectorUtil()
				.addElement(p_mrg)
				.addElement(p_cocode)
				.addElement("CHNPRINT")
				.addElement("VARIABLE")
				.addElement("A4P")
				.addElement("NORMAL")
				.addElement("LPTRAW")
				.toVector()
			);
		rpc.close();
		if(val != null && val.toString().startsWith("OK  ")) {
				return(val.toString().substring(4));
		} else{
			return(null);
		}
	}
	public static String wcPrintInvoice(SessionHelper p_sh,int p_mrg) {
		RpcClient rpc = p_sh.getRpcClient();
		Value val = rpc.callSegment("printer_autoselect",
				new VectorUtil()
				.addElement(1)
				.toVector()
			);
//		val = rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(ZkUtil.getWebContentRealPath("images", true)).toVector());
		val = rpc.callSegment("erpv3_print_invoice",
				new VectorUtil()
				.addElement(p_mrg)
				.addElement("CHNPRINT")
				.addElement("VARIABLE")
				.addElement("A4P")
				.addElement("NORMAL")
				.addElement("LPTRAW")
				.toVector()
			);
		rpc.close();
		if(val != null && val.toString().startsWith("OK  ")) {
				return(val.toString().substring(4));
		} else{
			return(null);
		}
	}

}
