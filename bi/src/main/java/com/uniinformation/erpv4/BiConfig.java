package com.uniinformation.erpv4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiView;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.kyoko.common.*;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.erpv4.Erpv4SessionHelper;

public class BiConfig {
//	static public String STOCK_IN_TDtypes="'PD','MI','JI'";

//	static public String STOCK_IN_TDtypes="'RI','MI','JI'";
	public static String defIniFileName = "erpv4config.ini";
	
	
	static public String getBaseCcy(SessionHelper sp,String p_cocode) {
//		String bccy = (String) sp.getSessionData("BASECCY");
		
		String bccy = null;
		if(isMultiCompany(sp)) {
			Hashtable<String,String> bccyHash = (Hashtable<String,String>) sp.getSessionData("BASECCYLIST");
			if(bccyHash == null)  {
				bccyHash = new Hashtable<String,String>();
				SelectUtil su = null;
				try {
					su = new SelectUtil();
					su.init(sp.getBiSchema().getConn());
					TableRec tr = su.getQueryResult("select * from cocode");
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						bccyHash.put(tr.getFieldString("co_cocode"), tr.getFieldString("co_baseccy") );
					}
					sp.putSessionData("BASECCYLIST", bccyHash);
				} catch (Exception ex) {
					UniLog.log(ex);
					return(null);
				}
				finally {
					if (su != null) su.close();
				}
			}
			return(bccyHash.get(p_cocode)); 
		} else {
			bccy = (String) sp.getSessionData("BASECCY");
			if(bccy == null ) {
				SelectUtil su = null;
				try {
					su = new SelectUtil();
					su.init(sp.getBiSchema().getConn());
					TableRec tr;
					tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
					if(tr.getRecordCount() == 1) {
						if(tr.existField("co_baseccy")) {
							bccy = tr.getFieldString("co_baseccy");
						}
					}
					if(bccy == null) {
						tr = su.getQueryResult("select * from baseccy");
						if(tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							bccy = tr.getFieldString("bbc_cid");
						}
					}
					if(bccy == null)  bccy = "HKD";
					sp.putSessionData("BASECCY", bccy);
				} 
				catch (Exception ex) {
					UniLog.log(ex);
				}
				finally {
					if (su != null) su.close();
					
				}
			}
			return(bccy);
		}
	}
	
	static public void setDefaultCocode(SessionHelper p_sp,String p_cocode) throws Exception {
		SelectUtil su = null;
		try {
			su = new SelectUtil();
			su.init(p_sp.getBiSchema().getConn());
			TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
			if(tr.getRecordCount() == 1) {
				tr.setRecPointer(0);
				String cocode = tr.getFieldString("co_cocode");
				if(cocode != null && !cocode.trim().equals("")) {
					p_sp.putSessionData("COCODE", cocode); 
					if(tr.getFieldIndex("co_shortcode") >= 0) {
						p_sp.putSessionData("COSHORTCODE", tr.getFieldString("co_shortcode")); 
					} else {
						p_sp.removeSessionData("COSHORTCODE");
					}
					if(p_sp instanceof Erpv4SessionHelper) {
						((Erpv4SessionHelper) p_sp).clearCoName();
						p_sp.clearSideMenuCache();

					}
				}
			} else {
				throw new Exception ("Switch Company to " + p_cocode + "Failed");
			}
		}
		catch(Exception ex) {
			throw ex;
		}
		finally {
			if (su != null) su.close();
		}
	}

	static public String getDefaultCoCode(SessionHelper sp) {

		String cocode = (String) sp.getSessionData("COCODE");
		if(cocode == null) {
			try {
				cocode = getString(sp,"DefaultCoCode");
				UniLog.log("HAHA 210121 DefaultCocode is " + cocode);
				TableRec tr = null;
				if(cocode==null) {
					SelectUtil su = null;
					try {
						su = new SelectUtil();
						su.init(sp.getBiSchema().getConn());
						tr = su.getQueryResult("select * from cocode");
						UniLog.log("HAHA 210121 select cocode");
						if(tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							cocode = tr.getFieldString("co_cocode");
							UniLog.log("HAHA 210121 cocode.co_cocode is " + cocode);
						}
					}
					catch(Exception ex) {
						//ex.printStackTrace();
						UniLog.log1("error:" + ex.getMessage());
					}
					finally {
						if (su != null) su.close();
					}
				}
				if(cocode != null)  {
					sp.putSessionData("COCODE", cocode);
					if(tr != null && tr.existField("co_shortcode")) {
						sp.putSessionData("COSHORTCODE", tr.getFieldString("co_shortcode")); 
					} else {
						sp.removeSessionData("COSHORTCODE");
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
//		if(cocode != null)  return(cocode); else return("XXX");
		return(cocode);
	}
	
	static public String getCoLogo(SessionHelper p_sp,String p_cocode) {
			SelectUtil su = null;
			try {
				su = new SelectUtil();
				su.init(p_sp.getBiSchema().getConn());
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
				
				String logo = null;
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(tr.existField("co_logo")) {
						logo = tr.getFieldString("co_logo");
					}
				}
				if(logo != null && !logo.trim().equals("")) {
					return(logo);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			finally {
				if (su != null) su.close();
			}
			return(getDefaultLogo(p_sp));
	}
	static public String getCoShortname(SessionHelper p_sp,String p_cocode) {
			SelectUtil su = null;
			try {
				su = new SelectUtil();
				su.init(p_sp.getBiSchema().getConn());
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
				String shortname = null;
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(tr.existField("co_shortname")) {
						shortname = tr.getFieldString("co_shortname");
					}
				}
				if(shortname != null && !shortname.trim().equals("")) {
					return(shortname);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			finally {
				if (su != null) su.close();
			}
			return(null);
	}
	
	static public String getCoShortcode(SessionHelper p_sp,String p_cocode) {
		boolean isDefaultCo = (p_cocode != null && p_cocode.equals(getDefaultCoCode(p_sp)));
		if(isDefaultCo) {
			String sc = (String) p_sp.getSessionData("COSHORTCODE");
			if(sc != null) return(sc);
		}
		SelectUtil su = null;
		try {
			su = new SelectUtil();
			su.init(p_sp.getBiSchema().getConn());
			TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
			String shortcode = null;
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				if(tr.existField("co_shortcode")) {
					shortcode = tr.getFieldString("co_shortcode");
				}
			}
			if(shortcode != null && !shortcode.trim().equals("")) {
				if(isDefaultCo) {
					p_sp.putSessionData("COSHORTCODE", shortcode);
				}
				return(shortcode);
			} else {
				return("");
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			if (su != null) su.close();
		}
		return(null);
}
	
	static public Map<String, Object> getLcFieldMap(SessionHelper p_sp,int p_lcrg) {
		SelectUtil su = null;
		try {
			su = new SelectUtil();
			su.init(p_sp.getBiSchema().getConn());
			TableRec tr = su.getQueryResult("select * from location where lc_rg = " + p_lcrg);
			if (tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				return MapUtil.of("lc_desc", tr.getFieldString("lc_desc"), "lc_payment", tr.getFieldString("lc_payment"), "lc_epayment", tr.getFieldInt("lc_epayment"));
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			if (su != null) su.close();
		}
		return new HashMap<String, Object>();
	}
	static public Map<String, Object> getLcFieldMap(SessionHelper p_sp,String p_lcdesc) {
		SelectUtil su = null;
		try {
			su = new SelectUtil();
			su.init(p_sp.getBiSchema().getConn());
			TableRec tr = su.getQueryResult("select * from location where lc_desc = ?", new Wherecl().appendArgument(p_lcdesc));
			if (tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				return MapUtil.of("lc_rg", tr.getFieldInt("lc_rg"), "lc_payment", tr.getFieldString("lc_payment"), "lc_epayment", tr.getFieldInt("lc_epayment"));
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			if (su != null) su.close();
		}
		return new HashMap<String, Object>();
	}
	static public String getCoAddr(SessionHelper sp,String p_cocode) {
		String coaddr = null;
			SelectUtil su = null;
			try {
				su = new SelectUtil();
				su.init(sp.getBiSchema().getConn());
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					coaddr = tr.getFieldString("co_coaddr1");
					coaddr = coaddr.trim() + " " + tr.getFieldString("co_coaddr2");
					coaddr = coaddr.trim() + " " + tr.getFieldString("co_coaddr3");
//					if(p_cocode.equals(cocode)) sp.putSessionData("CONAME", coname);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			finally {
				if (su != null) su.close();
			}
		return(coaddr);
	}	
	static public String getCoName(SessionHelper sp,String p_cocode) {
		String coname = null;
		/*
		String cocode = null;
		if(p_cocode.equals(cocode)) {
			coname = (String) sp.getSessionData("CONAME");
		}
		*/
		if(coname == null) {
			SelectUtil su = null;
			try {
				su = new SelectUtil();
				su.init(sp.getBiSchema().getConn());
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					coname = tr.getFieldString("co_coname");
//					if(p_cocode.equals(cocode)) sp.putSessionData("CONAME", coname);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			finally {
				if (su != null) su.close();
			}
		}
		return(coname);
	}	
	static public Map<String, Object> getCoFieldMap(SessionHelper sp,String p_cocode) {
		SelectUtil su = null;
		try {
			su = new SelectUtil();
			su.init(sp.getBiSchema().getConn());
			TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+p_cocode+"'");
			if (tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				return MapUtil.of("co_coname", tr.getFieldString("co_coname"), "co_chnname", tr.getFieldString("co_chnname"), 
							"co_telnum", tr.getFieldString("co_telnum"),
							"co_faxnum", tr.getFieldString("co_faxnum"), "co_email", tr.getFieldString("co_email"), 
							"co_license", tr.getFieldString("co_license"), "co_payment", tr.getFieldString("co_payment"));
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			if (su != null) su.close();
		}
		return new HashMap<String, Object>();
	}
	static public void resetErpv4Config(SessionHelper sp) {
		sp.removeSessionData("erpv4Config");
	}
	static IniHelper loadErpv4Config(SessionHelper sp) {
		//andrew210225 remark: session data is not available before login
		IniHelper erpv4Config = (IniHelper) sp.getSessionData("erpv4Config");
		if(erpv4Config == null) {
			try {
				IniHelper ini = null;
				ini = SessionHelper.getIniHelper(sp.getAgent());
				String erpv4ConfigFile = ini.getString("erpv4ConfigFile",null); 
				erpv4Config = new IniHelper(erpv4ConfigFile, defIniFileName, sp.getAgent());
				sp.putSessionData("erpv4Config", erpv4Config); //andrew210204: hotfix. store erpv4Config. Remark: it has multiple thread issue.
			} 
			catch (Exception ex) {
				UniLog.log(ex);
				return(null);
			}
		}
		return(erpv4Config);
	}
	static public String getString(SessionHelper p_sp,String p_key) {
		IniHelper erpv4Config = loadErpv4Config(p_sp);
		if(erpv4Config != null) return(erpv4Config.getString(p_key));
		return(null);
	}
	static public String getString(SessionHelper p_sp,String p_key,String p_def) {
		IniHelper erpv4Config = loadErpv4Config(p_sp);
		if(erpv4Config != null) return(erpv4Config.getString(p_key,p_def));
		return(p_def);
	}
	static public Integer getInteger(SessionHelper p_sp,String p_key,Integer p_def) {
		IniHelper erpv4Config = loadErpv4Config(p_sp);
		if(erpv4Config != null) return(erpv4Config.getInteger(p_key,p_def));
		return(p_def);
	}
	static public String getDefaultLogo(SessionHelper p_sp) {
		return(getCompanyLogo(p_sp,BiConfig.getDefaultCoCode(p_sp)));
//		try {
//		FilingUtilObject fobj = FilingUtil.getFile(p_sp.getAgent(), null, "LOGO_IMAGE_"+Erpv4Config.getDefaultCoCode(p_sp), null);
//		if(fobj != null) {
//			File f = new File(
//					ZkUtil.getWebContentRealPath("", true) + "images/logo/custom_"+p_sp.getAgent()+"_"+Erpv4Config.getDefaultCoCode(p_sp)+"_"+fobj.cts.toString().replace(" ", "_").replace(":", "_")+".png");
//			if(!f.exists()) {
//				FileOutputStream os = new FileOutputStream(f);
//				FilingUtil.getFile(p_sp.getAgent(), null, "LOGO_IMAGE_"+Erpv4Config.getDefaultCoCode(p_sp), os);
//				os.close();
//			}
//			
//			String s = "logo/custom_"+p_sp.getAgent()+"_"+Erpv4Config.getDefaultCoCode(p_sp)+"_"+fobj.cts.toString().replace(" ", "_").replace(":", "_")+".png";
//			return(s);
//		}
//		} catch (Exception ex) {
//			//UniLog.log(ex);
//			UniLog.log("error:" + ex);
//		}
//		
//		
//			String logo = Erpv4Config.getString(p_sp, "LogoImage");
//			return(logo);
	}	
	static public String getCompanyLogo(SessionHelper p_sp,String p_cocode) {
		try {
		FilingUtilObject fobj = FilingUtil.getFile(p_sp.getAgent(), null, "LOGO_IMAGE_"+p_cocode, null);
		if(fobj != null) {
			File f = new File(
					p_sp.getWebContentRealPath("", true) + "images/logo/custom_"+p_sp.getAgent()+"_"+p_cocode+"_"+fobj.cts.toString().replace(" ", "_").replace(":", "_")+".png");
			if(!f.exists()) {
				FileOutputStream os = new FileOutputStream(f);
				FilingUtil.getFile(p_sp.getAgent(), null, "LOGO_IMAGE_"+p_cocode, os);
				os.close();
			}
			
			String s = "logo/custom_"+p_sp.getAgent()+"_"+p_cocode+"_"+fobj.cts.toString().replace(" ", "_").replace(":", "_")+".png";
			return(s);
		}
		} catch (Exception ex) {
			//UniLog.log(ex);
			UniLog.log("error:" + ex);
		}
		/* company logo not found, use agent default logo */
		String logo = BiConfig.getString(p_sp, "LogoImage");
		return(logo);
	}		

	static public boolean useS2Listbox(SessionHelper p_sp) {
		Boolean b = (Boolean) p_sp.getSessionData("USES2LISTBOX");
		if(b == null) {
			String s = getString(p_sp,"UseS2Listbox");
			if(s != null && s.equals("Y")) b = true; else b = false;
			p_sp.putSessionData("USES2LISTBOX", b);
		}
		return(b);
		
	}
	
	public static String getDefaultDateFormat(SessionHelper sp) {
		String ss = (String) sp.getSessionData("DEFAULTDATEFORMAT");
		if(ss == null) {
			ss = getString(sp,"defaultDateFormat");
			if(ss == null) ss = "";
			sp.putSessionData("DEFAULTDATEFORMAT", ss);
		}
		return(ss.isEmpty() ? null : ss);
	}
	static public boolean isMultiCompany(SessionHelper p_sp) {
		Boolean b = (Boolean) p_sp.getSessionData("MULTICOMPANY");
		if(b == null) {
			String s = getString(p_sp, "MultiCompany");
			if(s != null && s.equals("Y")) {
				b = true;
			} else {
				b = false;
			}
			p_sp.putSessionData("MULTICOMPANY", b);
		}
		return(b);
	}
	static public void setDefaultLcrg(SessionHelper sp,int p_lcrg) throws Exception{
		sp.putSessionData("LCRG", p_lcrg);
		if(p_lcrg > 0) {
			SelectUtil su = new SelectUtil();
			su.init(sp.getBiSchema().getConn());
			TableRec tr = su.getQueryResult("select lc_group from location where lc_rg = "+p_lcrg);
			if(tr.getRecordCount() <= 0) {
				setDefaultLcrg(sp,0);
				return;
			}
			tr.setRecPointer(0);
			sp.putSessionData("LCGROUP", tr.getFieldString("lc_group"));
		} else {
			sp.putSessionData("LCGROUP", null);
		}
		if(sp instanceof Erpv4SessionHelper) {
			((Erpv4SessionHelper) sp).clearCoName();
			sp.clearSideMenuCache();
		}
		setLCDefaultPrinter(sp, p_lcrg);
	}
	static public String getDefaultLcGroup(SessionHelper sp) {
		String defaultLcGroup = (String) sp.getSessionData("LCGROUP");
		return(defaultLcGroup);
	}
	static public int getDefaultLcrg(SessionHelper sp) {
		Integer defaultLcrg = (Integer) sp.getSessionData("LCRG");
		if(defaultLcrg == null) return(0);
		return(defaultLcrg);
	}
	static public String getLcDesc(SessionHelper p_sp,int p_lcrg) {
			SelectUtil su = null;
			try {
				su = new SelectUtil();
				su.init(p_sp.getBiSchema().getConn());
				TableRec tr = su.getQueryResult("select * from location where lc_rg = " + p_lcrg);
				String shortname = null;
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					shortname = tr.getFieldString("lc_desc");
				}
				if(shortname != null && !shortname.trim().equals("")) {
					return(shortname);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			finally {
				if (su != null) su.close();
			}
			return(null);
	}
	
	/***
	 * set a default printer name based on devprefix
	 * e.g. TSP01
	 * it target for clerpmulti with site, it assume each site cannot has more than one printer
	 * @param sp
	 * @param p_lcrg
	 */
	static private void setLCDefaultPrinter(SessionHelper sp, int p_lcrg) {
		try {
			if (sp == null || p_lcrg <= 0) {
				return;
			}
			if (!isMultiStockLoc(sp)) {
				return;
			}
			if (!getAllowLCDefaultPrinter(sp)) {
				UniLog.log1("not allow lcdefaultprinter");
				return;
			}
			
			BiResult coLocBr = BiResultHelper.create(sp, "erpv4.CoLocation", null, String.format("lc_rg = %d", p_lcrg), null, -1, null ,false);
			UniLog.log1("DEBUG:coLocBr:%s", coLocBr);
			if (coLocBr != null && coLocBr.next()) {
				String devprefix = coLocBr.getCellString("lc_devprefix");
				if (StringUtils.isNotBlank(devprefix)) {
					String defaultPrinterName = String.format("%sP01", devprefix);
					UniLog.log1("defaultPrinterName: " + defaultPrinterName);
					sp.putSessionData("LC_DEFAULT_PRINTER", defaultPrinterName);
				}
			}
		}
		catch(Exception ex) {
			UniLog.log1("error:"+ex.getMessage());
		}
		
	}
	static public String getLCDefaultPrinter(SessionHelper sp) {
		if (!getAllowLCDefaultPrinter(sp)) {
			UniLog.log1("not allow lcdefaultprinter");
			return "";
		}
		if (isMultiStockLoc(sp)) {
			String defaultPrinterName = (String) sp.getSessionData("LC_DEFAULT_PRINTER");
			if (StringUtils.isNotBlank(defaultPrinterName)) {
				return defaultPrinterName;
			}
		}
		return "";
	}
	
	/***
	 * obtain from ini only
	 * if you want to check whether it has a valid lcdefaultprinter, you better use getLCDefaultPrinter()
	 * @param p_sh
	 * @return
	 */
	static private boolean getAllowLCDefaultPrinter(SessionHelper p_sh) {
		String an = getString(p_sh,"allowLCDefaultPrinter","Y");  //default Y. enable for all site should be fine
		return("Y".equals(an));
	}
	static public boolean isMultiStockLoc(SessionHelper p_sp) {
//		String s = Erpv4Config.getString(p_sp, "MultiStockPrice");
		String s = getString(p_sp, "MultiStockLoc");
		return("Y".equals(s));
	}
}
