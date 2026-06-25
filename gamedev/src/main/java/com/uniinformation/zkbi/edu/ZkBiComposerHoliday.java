package com.uniinformation.zkbi.edu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerHoliday extends ZkBiComposerBase {
	private final static SimpleDateFormat jsonDateFormat = new SimpleDateFormat("yyyyMMdd");

	@Override
	protected void setupDeleteButton(final BiResult result) {
		super.setupDeleteButton(result);
		UniLog.log1("setupDeleteButton");

        final Button btnAutoUpdate;
    	if(masterWin.hasFellow("btnAutoUpdate")) {
    		btnAutoUpdate = (Button) masterWin.getFellow("btnAutoUpdate");
    	} else {
        	btnAutoUpdate = new ZkBiButton();
	        btnAutoUpdate.setLabel(sessionHelper.getBtLabel("Auto Update"));
	        btnAutoUpdate.setId("btnAutoUpdate");
	        btnAutoUpdate.setImage("images/icons/zkweb/038-file-4-25x25.png");
	        abHelper.addButton(btnAutoUpdate,"fa-plus");
    	}
    	btnAutoUpdate.setTooltiptext(sessionHelper.getLabel("Auto Update"));
        btnAutoUpdate.addEventListener(Events.ON_CLICK,
        	new ZkBiEventListener<Event>() {
        		public void onZkBiEvent(Event event) throws Exception {
        			UniLog.log1("event:%s", event);
        			try {
        				final TreeMap<Date, String> oldMap = loadHolidayDataByOldData(result);
        				final TreeMap<Date, String> newMap = loadHolidayDataByWeb();
        				
        				//remove exists records
        				Iterator<Map.Entry<Date, String>> it = newMap.entrySet().iterator();
        				while (it.hasNext()) {
        					Map.Entry<Date, String> entry = it.next();
        					if (oldMap.containsKey(entry.getKey()))
        						it.remove();
        				}
        				
        				Date minDate = null, maxDate = null;
        				for (Map.Entry<Date, String> entry : newMap.entrySet()) {
        					if (minDate == null)
        						minDate = entry.getKey();
        					maxDate = entry.getKey();
        				}

        				if (minDate != null && maxDate != null) {
        					SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd");
        					Messagebox.show(String.format("Are you sure to auto update holiday record (%s-%s)?", f.format(minDate), f.format(maxDate)), 
        						"Question", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener<Event>() {
			   					@Override
			   					public void onZkBiEvent(Event event) throws Exception {
				   					if (event.getName().equals(Events.ON_OK)) {
				   						autoUpdateHoliday(result, newMap);
				   						refresh(result,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
				   					}
			   					}
		   					});
        				}
        				else
        					ZkUtil.showMsg("No new records found");
        			}
        			catch (Exception e) {
        				e.printStackTrace();
        				ZkUtil.showErrMsg("error: %s", e.getMessage());
        			}
        		}
        	}
        );
	}
	
	private TreeMap<Date, String> loadHolidayDataByOldData(BiResult br) {
		TreeMap<Date, String> map = new TreeMap<Date, String>();
		for (int i = 0; i < br.getRowCount(); i++) {
			br.loadOneRecV(i);
			Date date = br.getCell("eshd_date").getDate();
			String desc = br.getCellString("eshd_desc");
			map.put(date, desc);
		}
		return map;
	}
	
	private TreeMap<Date, String> loadHolidayDataByWeb() throws Exception {
		TreeMap<Date, String> map = new TreeMap<Date, String>();
		HttpURLConnection conn = null;
		try {
			final String urlStr = "https://www.1823.gov.hk/common/ical/en.json";
			URL url = new URL(urlStr);
			conn = (HttpsURLConnection) url.openConnection();
			if (urlStr.startsWith("https")) {
				conn = (HttpsURLConnection) url.openConnection();
				setupSslConnection((HttpsURLConnection)conn);
			} else
				conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(false);  
        	conn.setDoInput(true);  
        	conn.setUseCaches(false);  
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();
			UniLog.log1("responseCode:%s",responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				StringBuilder sb = new StringBuilder();
				String str;
				while ((str = br.readLine()) != null)
					sb.append(str);
				br.close();
				String jsonSrc = sb.toString().trim();

				JsonParser parser = new JsonParser();
				JsonObject rootObj = parser.parse(jsonSrc).getAsJsonObject();
				JsonObject vcalendarObj = rootObj.getAsJsonArray("vcalendar").get(0).getAsJsonObject();
				JsonArray veventArr = vcalendarObj.getAsJsonArray("vevent");
				Calendar cdStart = Calendar.getInstance();
				Calendar cdEnd = Calendar.getInstance();
				for (int i = 0; i < veventArr.size(); i++) {
					JsonObject veventObj = veventArr.get(i).getAsJsonObject();
					JsonArray dtstartArr = veventObj.getAsJsonArray("dtstart");
					JsonArray dtendArr = veventObj.getAsJsonArray("dtend");
					String summary = veventObj.get("summary").getAsString();
					String dtStartStr = dtstartArr.get(0).getAsString();
					String dtEndStr = dtendArr.get(0).getAsString();
					Date dtStart = jsonDateFormat.parse(dtStartStr);
					Date dtEnd = jsonDateFormat.parse(dtEndStr);
					UniLog.log1("summary:%s, dtStart:%s, dtEnd:%s, summary len:%d", summary, jsonDateFormat.format(dtStart), jsonDateFormat.format(dtEnd), summary.length());
					cdStart.setTime(dtStart);
					cdEnd.setTime(dtEnd);
					for (Calendar cd = cdStart; cd.compareTo(cdEnd) < 0; cd.add(Calendar.DATE, 1)) {
						UniLog.log1("date:%s", jsonDateFormat.format(cd.getTime()));
						map.put(cd.getTime(), summary);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (conn != null) {
				try {
					conn.disconnect();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	private static void setupSslConnection(HttpsURLConnection conn) throws Exception {
		//setup https ssl connection
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
	    		public X509Certificate[] getAcceptedIssuers() {
	     			return null;
	    		}
	    		public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
	     			return;
	    		}
	    		public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
	     			return;
	    		}
			}
		};
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
	    		//if (!urlHostName.equalsIgnoreCase(session.getPeerHost()))
				//	System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
	    		UniLog.log("verify:" + urlHostName);
	    		return true;
			}
		};
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new SecureRandom());
		conn.setSSLSocketFactory(sslContext.getSocketFactory());
		conn.setHostnameVerifier(hv);
	}
	
	private void autoUpdateHoliday(BiResult result, Map<Date, String> newMap) {
		try {
			int addCount = 0;
        	for (Map.Entry<Date, String> entry : newMap.entrySet()) {
        		Date date = entry.getKey();
        		String desc = entry.getValue();
    			result.clearCurrentRec();
    			result.getCell("eshd_date").set(date);
    			result.getCell("eshd_desc").set(desc);
    			result.getCell("eshd_holtype").set("Public Holiday");
    			UniLog.log1("add record date:%s, desc:%s", jsonDateFormat.format(date), desc);
    			ReturnMsg rtnMsg = result.addCurrent();
    			if (rtnMsg != null && !rtnMsg.getStatus()) {
    				ZkUtil.showErrMsg("insert record fail: %s", rtnMsg.getMsg());
    				break;
    			}
    			addCount++;
        	}
        	ZkUtil.showMsg("Auto update finish, added %d records", addCount);
		}
		catch (Exception e) {
			e.printStackTrace();
    		ZkUtil.showErrMsg("error: %s", e.getMessage());
		}
	}
}
