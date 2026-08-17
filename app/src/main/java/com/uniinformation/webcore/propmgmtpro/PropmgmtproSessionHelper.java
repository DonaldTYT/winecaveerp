package com.uniinformation.webcore.propmgmtpro;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.google.api.client.util.Objects;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.erpv4.Erpv4SessionHelper;

public class PropmgmtproSessionHelper extends Erpv4SessionHelper {

	@Override
	public String getWebPageCoHeaderHtml() {
		String shortname = Erpv4Config.getLcDesc(this, Erpv4Config.getDefaultLcrg(this));
		return String.format("<div class=\"hbg dropdownlink\" style=\"display:table;vertical-align:middle;text-align:left;padding-right:2px;padding-top:5px;padding-bottom:5px;margin-bottom:10px;border-bottom:1px solid\">"
							   + "<div style='font-size:12px;line-height:20px;padding:5px 0'>"
							   + "<div style='display:flex;justify-content:center;height:60px'><img src='images/%s' style='width:100%%;height:100%%;object-fit:contain'></div>"
							   + ((StringUtils.isNotBlank(getDbLocation()) && Erpv4Config.getString(this, "show_dbloc", "").equals("Y")) ? 
									   String.format("<div style='margin-top:5px'>%s</div>", getDbLocation()) : "")
							   + "<div style='margin-top:5px'>大廈名稱：</div>"
							   + "<div>%s</div>"
							   + "</div>"
							   + "<div style='position:absolute;left:20px;bottom:0;font-size:12px;color:gray'>%s</div>"
				+ (getSideMenuAutoHideDef() ? "<div style='display:table-cell;vertical-align:top;width:15px'><i class=\"pin fa fa-thumb-tack\" title=\"Pin/Unpin Menu (Alt+M)\" style=\"display:inline-block;width:10px;height:15px;line-height:15px;\" aria-hidden=\"true\"></i></div>" : "")
				+ "</div>"
				, getCompanyLogo(), StringUtils.defaultIfBlank(shortname, "未選擇"), 
				getVersion());
	}

	public String getCompanyLogo() {
		try {
			String cocode = Erpv4Config.getDefaultCoCode(this);
			FilingUtilObject fobj = FilingUtil.getFile(getAgent(), null, "LOGO_IMAGE_"+cocode, null);
			if (fobj != null)
				return "logo/custom_"+getAgent()+"_"+cocode+"_"+fobj.cts.toString().replace(" ", "_").replace(":", "_")+".png";
		} catch (Exception ex) {
			UniLog.log("error:" + ex);
		}
		/* company logo not found, use agent default logo */
		return Erpv4Config.getString(this, "LogoImage");
	}		
	
	public String getBrandLogo() {
		return (getAgent().startsWith("propertymgmt") || Objects.equal(getAgent(), "propmgmtlive")) ? "images/logo/pdss_logo.svg" : null;
	}
	
	public String getVersion() {
		return (getAgent().startsWith("propertymgmt") || Objects.equal(getAgent(), "propmgmtlive")) ? "v.2.0 - 2026060501" : "v2026052701";
	}

	@Override
	protected void setDefaultCocode() throws Exception {
		super.setDefaultCocode();
		if(loginTr != null && loginTr.existField("lgu_mettingdate")) {
			if(loginTr.getRecordCount() > 0 && loginTr.getFieldInt("lgu_lcrg") > 0) {
				Date mettingDate = loginTr.getFieldDate("lgu_mettingdate");
				putSessionData("METTING_DATE", mettingDate);
			}
		}
	}
}
