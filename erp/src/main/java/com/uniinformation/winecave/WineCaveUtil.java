package com.uniinformation.winecave;

import java.util.Vector;

import javax.mail.internet.*;

import org.apache.commons.lang3.StringUtils;

import com.kikyosoft.rpccall.RpcClient;
import com.kikyosoft.rpccall.Value;
import com.kikyosoft.utils.StringUtil;
import com.kikyosoft.utils.VectorUtil;
import com.kyoko.common.CoreLog;
import com.uniinformation.utils.SelectUtil;

public class WineCaveUtil {
	private static Double cashDiscountPercent;
	public static double getCashDiscountPercent() {
		if (cashDiscountPercent == null) {
			String ss = WineCaveConfig.getProperty("cashPaymentDiscount");
			if(!StringUtils.isBlank(ss)) {
				cashDiscountPercent = Double.parseDouble(ss);
			} else {
				cashDiscountPercent = 0.0;
			}
		}
		return cashDiscountPercent;
	}
	public static double getWebConsigpprice(double p_price) {
		return Math.ceil(p_price / (1 - getCashDiscountPercent()));
	}
	public static double getWebStandardprice(double p_price) {
		return Math.ceil(p_price / (1 - getCashDiscountPercent()));
	}
	
   public static InternetAddress getInternetAddress(String p_address) throws Exception {
      if (p_address.indexOf('<') >= 0) {
         String namePart = StringUtil.strpart(p_address, 0, p_address.indexOf('<')).trim();
         String addressPart = StringUtil.strpart(
                              p_address, 
                              p_address.indexOf('<')+1, 
                              p_address.lastIndexOf('>')-p_address.indexOf('<')-1
                           ).trim();
         return(new InternetAddress(addressPart, namePart, "utf-8"));
      }
      else
         return(new InternetAddress(p_address, p_address, "utf-8"));
   }
	public static Long iPAddrToLong(String ipAddr) {
		if (ipAddr != null) {
			String[] ss = ipAddr.trim().split("\\.");
			if (ss.length == 4) {
				long result = 0;
				boolean flag = true;
				for (int i = 0; i < ss.length; i++) {
					try {
						int p = Integer.parseInt(ss[i]);
						if (p >= 0 && p <= 255)
							result |= p << ((3 - i) * 8);
						else {
							flag = false;
							break;
						}
					} catch (Exception e) {
						flag = false;
						break;
					}
				}
				if (flag)
					return result;
			}
		}
		return null;
	}
	public static int[] splitIPAddr(String ipAddr) {
		Long ip = iPAddrToLong(ipAddr);
		if (ip != null)
			return new int[] {
				(int) ((ip >> 24) & 0xff),
				(int) ((ip >> 16) & 0xff),
				(int) ((ip >> 8) & 0xff),
				(int) ((ip) & 0xff)
			};
		else
			return null;
	}
	
	public static String getOrderStatusByCode(String p_so)
	{
		//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		perfsvr.open();
		if(perfsvr.isConnected() != true) {
			CoreLog.log("reprintOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
			perfsvr.setDebug(true);
			Vector v = new Vector();
			v.add("winecave_getorderstatus_bycode");
			v.add(StringUtil.stripNonPrintable(p_so));
			val = perfsvr.callSegment("WineCaveConnection",v);
			CoreLog.log("call to rpcserver got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null && val.toString() != null && val.toString().startsWith("OK  ")) return(val.toString().substring(4)); else return("FAIL Unknown Error");
		}
	}
	
	public static String registorNewCustomer(int p_language,String p_loginId, String p_password, String p_engName, 
            String p_chiName, String p_address,String p_contact, 
							String p_phone, String p_fax, String p_email)
{
CoreLog.log("HAHA registorNewCustomer Called");
//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
perfsvr.open();
if(perfsvr.isConnected() != true) {
CoreLog.log("reprintOrder Connect to jdbc server error");
perfsvr.close();
perfsvr = null;
return("FAIL Connection Error");
} else {
Value val;
perfsvr.setDebug(true);
Vector v = new Vector();
v.add("winecave_registerCustomer");



v.add(p_language);
v.add(StringUtil.stripNonPrintable(p_loginId));
v.add(StringUtil.stripNonPrintable(p_password));
v.add(StringUtil.stripNonPrintable(p_engName));
v.add(StringUtil.stripNonPrintable(p_chiName));
v.add(StringUtil.stripNonPrintable(p_address));
v.add(StringUtil.stripNonPrintable(p_contact));
v.add(StringUtil.stripNonPrintable(p_phone));
v.add(StringUtil.stripNonPrintable(p_fax));
v.add(StringUtil.stripNonPrintable(p_email));
val = perfsvr.callSegment("WineCaveConnection",v);
CoreLog.log("call to rpcserver got " + val);
perfsvr.close();
perfsvr = null;
if(val != null) return(val.toString()); else return("FAIL Unknown Error");
}
}
	public static String updateConsignment(int p_language,String p_vcode, int p_irg,int p_org, int p_storageqty,int p_consignmentqty,boolean p_saleasbot, double p_sellingprice, int p_purchasedqty)  {
		CoreLog.log("updateConsignment " + p_vcode + " " + p_irg + " " + p_org + " " + p_storageqty + " " + p_consignmentqty + " " + p_saleasbot + " " +  p_sellingprice + " " + p_purchasedqty);
//		if(true) return("FAILOut of service");
		//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		perfsvr.open();
		if(perfsvr.isConnected() != true) {
			CoreLog.log("reprintOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
			perfsvr.setDebug(true);
			Vector v = new Vector();
			v.add("updateConsignmentDetail");
			v.add(p_language);
			v.add(p_vcode);
			v.add(p_irg);
			v.add(p_org);
			v.add(p_storageqty);
			v.add(p_consignmentqty);
			if(p_saleasbot) v.add("Y"); else v.add("N");
			v.add(p_sellingprice);
			v.add(p_purchasedqty);
			val = perfsvr.callSegment("WineCaveConnection",v);
			CoreLog.log("call to rpcserver got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
		/*
		return("OK  Record Updated");
		return("FAILService Not Available");
		*/
	}
	public static String getNetUnitPrice(String p_vcode, double p_sellingprice)  {
		CoreLog.log("updateConsignment HAHAHAH" + p_vcode + " " +  p_sellingprice);
		//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		perfsvr.open();
		if(perfsvr.isConnected() != true) {
			CoreLog.log("reprintOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
			perfsvr.setDebug(true);
			Vector v = new Vector();
			v.add("winecave_getnetunitprice");
			v.add(p_vcode);
			v.add(p_sellingprice);
			val = perfsvr.callSegment("WineCaveConnection",v);
			CoreLog.log("call to rpcserver got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
	}
	
	/**
	 * 
	 * @param p_language 0,1
	 * @param p_vcode current custcode
	 * @param p_irg st_irg
	 * @param p_org or_org
	 * @param p_loc: "WH01" = consignment,"STOR" = storage
	 * @param p_tfrqty : total quantity to transfer 
	 * @param tranferToList vector of number of client to receive the transfer, each client contains 2 argument (vd_vcode,tfrqty)
	 *                       total number of argument = number client * 2
	 * @return "OK  " or "FAIL reason"
	 */
	public static String clientStockTransfer(int p_language,String p_vcode, int p_irg,int p_org, String p_loc,int p_tfrqty,Vector tranferToList)  {
		CoreLog.log("clientStockTransfer " + p_vcode + " " + p_irg + " " + p_org + " " + p_loc + " " + p_tfrqty + " " + tranferToList);
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		perfsvr.open();
		if(perfsvr.isConnected() != true) {
			CoreLog.log("reprintOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val=null;
			perfsvr.setDebug(true);
			Vector v = new Vector();
			v.add("winecave_clienttransfer");
			v.add(p_language);
			v.add(p_vcode);
			v.add(p_irg);
			v.add(p_org);
			v.add(p_loc);
			v.add(p_tfrqty);
			for (Object obj : tranferToList)
				v.add(obj);
			val = perfsvr.callSegment("WineCaveConnection",v);
			CoreLog.log("call to rpcserver got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
	}
	
	public static String placeWebOrder(String p_loginid,String p_paymentMethod, String p_shipping, String p_deliveryAddress, double p_discountTotal, double p_deliveryCharge, int p_itemCount,Vector p_item_Detail) {
		CoreLog.log("placeWebOrder: "+p_loginid + ", " + p_paymentMethod +", "+p_shipping+", "+p_deliveryAddress+", "+p_itemCount + ", " + p_discountTotal + ", " + p_deliveryCharge);
		for(int i = 0;i< p_itemCount;i++) {
			int irg = ((Integer) p_item_Detail.get(i * 5 + 0)).intValue();
			int org = ((Integer) p_item_Detail.get(i * 5 + 1)).intValue();
			int qty = ((Integer) p_item_Detail.get(i * 5 + 2)).intValue();
			int isbot = ((Integer) p_item_Detail.get(i * 5 + 3)).intValue();
			int iprice = ((Integer) p_item_Detail.get(i * 5 + 4)).intValue();
			double price = iprice;
			price = price / 100;
			CoreLog.log("Item " + i + " " + irg + " " + org + " " + qty + " " + isbot + " " + iprice );
		}
		//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		perfsvr.open();
		if(perfsvr.isConnected() != true) {
			CoreLog.log("reprintOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
		CoreLog.log("Place Order " + p_loginid + "," + p_shipping + "," + p_itemCount);
			perfsvr.setDebug(true);
			Vector v = new Vector();
//			v.add("placeOrderNew");
			v.add("placeOrderWithArgcnt");
			v.add(8);
			v.add(p_loginid);
			v.add(p_paymentMethod);
			v.add(p_shipping);
			v.add(StringUtil.stripNonPrintable(p_deliveryAddress));
			v.add(p_discountTotal); // this is the discount field
			v.add(p_deliveryCharge); // this is the delivery charge field
			v.add(p_itemCount);
			for(int i = 0;i< p_itemCount;i++) {
				int irg = ((Integer) p_item_Detail.get(i * 5 + 0)).intValue();
				int org = ((Integer) p_item_Detail.get(i * 5 + 1)).intValue();
				int qty = ((Integer) p_item_Detail.get(i * 5 + 2)).intValue();
				int isbot = ((Integer) p_item_Detail.get(i * 5 + 3)).intValue();
				int iprice = ((Integer) p_item_Detail.get(i * 5 + 4)).intValue();
				double price = iprice;
				price = price / 100;
				v.add(irg);
				v.add(org);
				v.add(qty);
				v.add(isbot);
				v.add(price);
			}

//			val = perfsvr.callSegment("WineCaveConnection",v);
			val = null;
			CoreLog.log("call to rpcserver got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
//		return("OK  /tmp/testinv.jpg");
	}
	public static String placeWebOrderNew(
			String p_loginid,
			String p_paymentMethod,
			String p_delimethod,
			String p_deliaddr,
			double p_discount,
			double p_delichg,
			int p_itemCount,Vector p_item_Detail) {
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
		perfsvr.open();
		if(perfsvr.isConnected() != true) {
			CoreLog.log("reprintOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
		CoreLog.log("Place Order " + p_loginid + "," + p_itemCount);
			perfsvr.setDebug(true);
			Vector v = new Vector();
//			v.add("placeOrderNew");
			v.add("placeOrderWithArgcnt");
			v.add(8);
			v.add(p_loginid);
			v.add(p_paymentMethod);
			v.add(p_delimethod);
			v.add(p_deliaddr);
			v.add(p_discount);
			v.add(p_delichg);
			v.add(p_itemCount);
			for(int i = 0;i< p_itemCount;i++) {
				String ss = (String) p_item_Detail.get(i * 5 + 0);
				val = perfsvr.callSegment("WineCaveConnection",
							new VectorUtil()
								.addElement("winecave_getirg")
								.addElement(ss)
								.toVector()
						);
				if(val == null || ! val.toString().startsWith("OK  ")) {
					perfsvr.close();
					return("FAILItem Invalid");
				}
				int irg = Integer.parseInt(val.toString().substring(4).trim());
				int org = ((Integer) p_item_Detail.get(i * 5 + 1)).intValue();
				int qty = ((Integer) p_item_Detail.get(i * 5 + 2)).intValue();
				int isbot = ((Integer) p_item_Detail.get(i * 5 + 3)).intValue();
				int iprice = ((Integer) p_item_Detail.get(i * 5 + 4)).intValue();
				double price = iprice;
//				price = price / 100;
				v.add(irg);
				v.add(org);
				v.add(qty);
				v.add(isbot);
				v.add(price);
			}

			val = perfsvr.callSegment("WineCaveConnection",v);
//			val = null;
			CoreLog.log("call to rpcserver got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
//		return("OK  /tmp/testinv.jpg");
	}
	

	public static String resendPassword(String p_vcode,String p_password)
	{
		CoreLog.log("HAHA resendPassword Called");
		//RpcClient perfsvr = new RpcClient("127.0.0.1",6002);
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		perfsvr.open();
		if(perfsvr.isConnected() != true) {
			CoreLog.log("reprintOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
			perfsvr.setDebug(true);
			Vector v = new Vector();
			v.add("winecave_resendpassword");
			v.add(p_vcode);
			v.add(p_password);
			val = perfsvr.callSegment("WineCaveConnection",v);
			CoreLog.log("call to rpcserver got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
	}
}
