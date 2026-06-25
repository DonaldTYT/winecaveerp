package com.uniinformation.erpv4;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kikyosoft.utils.ChineseConvert;
import com.kikyosoft.utils.JsonUtil;
import com.kikyosoft.utils.OptionFinder;
import com.kikyosoft.utils.TableRecOptionFinder;
import com.kikyosoft.utils.OptionFinder.Condition;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.webcore.SessionHelper;
import com.ibm.icu.text.Transliterator;


public class ProductOptionFinder extends TableRecOptionFinder {
	HashMap<String,String> tlspair ;
	public ProductOptionFinder(SessionHelper p_sp) {
		super(p_sp,"ProductOption");
	    addOption("category","sttp_name");
	    addOption("type","mt_tpname");
	    addOption("brand","stbd_name");
	    addOption("region","storg_name");
	    addOption("maturity","st_maturity");
	    addOption("country","storg_ecountry");
	    addOption("appellation","stbd_appellation");
	    addOption("score","st_score0");
	    addOption("packing","st_msize1");
//	    addOption("volume","st_msize2");
	    addOption("vintage","st_msize3");
	    tlspair = new HashMap<String,String>();
	    tlspair.put("mt_tpname","mt_tpcname");
	    tlspair.put("stbd_name","stbd_cname");
	    tlspair.put("storg_name","storg_cname");
	    tlspair.put("storg_ecountry", "storg_ccountry");
	    tlspair.put("stbd_appellation","stbd_cappellation");
	}
	
	Transliterator  b2gTranslater;
	
	@Override
	protected Object getValueFromOption(TableRec tr,String p_id) throws Exception {
		if(b2gTranslater == null) {
			b2gTranslater = Transliterator.getInstance("Traditional-Simplified");
		}
		if(p_id.equals("st_msize3")) {
			double dd = tr.getFieldDouble(p_id);
			int nn = (int) dd;
			if(nn < 1500) return(""); else return(""+nn);
		}
		String cField = tlspair.get(p_id);
		if(!StringUtils.isBlank(cField)) {
			return(
					new JsonUtil(new JSONObject())
						.add("name",tr.getFieldString(p_id))
						.addI18NObject("zh_Hant",tr.getFieldString(cField))
//						.addI18NObject("zh_Hans",ChineseConvert.convertAuto2G(tr.getFieldString(cField)))
						.addI18NObject("zh_Hans",b2gTranslater.transliterate(tr.getFieldString(cField)))
						.toI18NObject()
			);
		}
		/*
		if(p_id.equals("storg_ecountry")) {
			return(
					new JsonUtil(new JSONObject())
						.add("name",tr.getFieldString("storg_ecountry"))
						.addI18NObject("zh_Hant",tr.getFieldString("storg_ccountry"))
						.toI18NObject()
			);
		}
		*/
		return(super.getValueFromOption(tr, p_id));
	}
		
	@Override
	protected TableRec doQueryResult(SelectUtil su) throws Exception {
			return(su.getQueryResult(
					" select "+
							" unique "+
							" sttp_name,"+
							" mt_tpname,"+
							" mt_tpcname,"+
							" stbd_name,"+
							" stbd_cname,"+
							" storg_name,"+
							" storg_cname,"+
							" st_msize1,"+
							" st_msize2,"+
							" st_msize3,"+
							" st_maturity,"+
							" st_score0,"+
							" storg_ecountry,"+
							" storg_ccountry,"+
							" stbd_appellation"+
							" stbd_cappellation"+
							" from podetlocstatus,consgpreal,"+
							" stock,st_type,mctype,st_brand,st_origin where consgp_irg = pdls_irg "+
							" and consgp_org = pdls_org"+
							" and consgp_ctime = 0"+
							" and consgp_price > 0"+
							" and pdls_stockqty > 0"+
							" and pdls_loc = 'WH01'"+
							" and st_irg = pdls_irg"+
							" and st_mtype in ('W','S','K') " +
							" and sttp_code = st_mtype"+
							" and mt_tpcode = st_msubtype"+
							" and stbd_code = st_mbrand"+
							" and storg_code = stbd_origin"
							, null
					)
			);
		
	}

	@Override
	public boolean compareOption(Object p_record, Condition p_cond) throws Exception{
		// TODO Auto-generated method stub
		return super.compareOption(p_record, p_cond);
	}

}
