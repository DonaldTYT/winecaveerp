package com.uniinformation.winecave;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public abstract class WineCaveOrderPay {
	private static Object gLogOrderPayDatafeedLocker = new Object();
	public void logDatafeed(HttpServletRequest request, String path, String dataType) {
		synchronized(gLogOrderPayDatafeedLocker) {
			BufferedWriter fw = null;
			try {
				UniLog.log("logOrderPayDatafeed haha ");
				fw = new BufferedWriter(new FileWriter(path, true));
				fw.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ");
				Map<String, String> map = new LinkedHashMap<String, String>();
				map.put("dataType", dataType);
				map.put("fromIp", request.getRemoteAddr());
				if (isValidPayIP(request.getRemoteAddr())) {
					Enumeration<String> paramNames = request.getParameterNames();
					while (paramNames.hasMoreElements()) {
						String name = paramNames.nextElement();
						map.put(name, request.getParameter(name));
					}
				} else
					map.put("error", "Warning! calling from invalid IP address");
				fw.write(new Gson().toJson(map));
				fw.write("\n");
				fw.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public abstract String getPayUrl();
	public abstract void logDatafeed(HttpServletRequest request);
	public static boolean isValidPayIP(String ipRange, String ip) {
		String[] rs = ipRange.split(",");
		for (String s : rs) {
			String[] rs2 = ipRange.split("-");
			if (rs2.length < 1) return(false);
			if (rs2.length > 2) return(false);
			if (rs2.length > 1) {
				int[] ips = WineCaveUtil.splitIPAddr(ip);
				int[] ips0 = WineCaveUtil.splitIPAddr(rs2[0]);
				int[] ips1 = WineCaveUtil.splitIPAddr(rs2[1]);
				if (ips != null && ips0 != null && ips1 != null) {
					for (int i = 0; i < ips.length; i++) {
						if (!(ips[i] >= ips0[i] && ips[i] <= ips1[i]))
							return false;
					}
					return true;
				}
			} else {
				Long l = WineCaveUtil.iPAddrToLong(ip);
				if(l == null) return(false);
				Long l0 = WineCaveUtil.iPAddrToLong(s);
				if ((l0 != null) && (l0.longValue() == l.longValue()))
				return true;
			}
		}
		return false;
	}
//	public static boolean isValidPayIPx(String ipRange, String ip) {
//		String[] rs = ipRange.split("-");
//		if (rs.length > 1) {
//			int[] ips = WineCaveUtil.splitIPAddr(ip);
//			int[] ips0 = WineCaveUtil.splitIPAddr(rs[0]);
//			int[] ips1 = WineCaveUtil.splitIPAddr(rs[1]);
//			if (ips != null && ips0 != null && ips1 != null) {
//				for (int i = 0; i < ips.length; i++) {
//					if (!(ips[i] >= ips0[i] && ips[i] <= ips1[i]))
//						return false;
//				}
//				return true;
//			}
//		} else {
//			Long l = WineCaveUtil.iPAddrToLong(ip);
//			if (l != null) {
//				rs = ipRange.split(",");
//				for (String s : rs) {
//					Long l0 = WineCaveUtil.iPAddrToLong(s);
//					if ((l0 != null) && (l0.longValue() == l.longValue()))
//						return true;
//				}
//			}
//		}
//		return false;
//	}
	public abstract boolean isValidPayIP(String ip);
	public abstract String getRequestParams(int p_invrg, int p_language, SessionHelper p_sp);
	public abstract String payWebOrder(HttpServletRequest request, String status);
	public abstract int getInvoiceRg(String orderRef);

//	public static void main(String args[]){
//		UniLog.log( ""+isValidPayIP("103.149.149.1-192.149.149.1","103.149.149.1"));
//		UniLog.log( ""+isValidPayIP("18.140.106.57,18.142.92.184","18.140.106.57"));
//		UniLog.log( ""+isValidPayIP("18.140.106.57,18.142.92.184","18.142.92.184"));
//	}
}


