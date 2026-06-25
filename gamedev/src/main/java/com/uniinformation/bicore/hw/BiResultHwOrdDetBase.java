package com.uniinformation.bicore.hw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.Sprintf;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.ColorPickerGetItemProperty;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.ImageUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BiResultHwOrdDetBase extends BiResultQuoDet {
	ColorPickerGetItemProperty cpi = null;
	public BiResultHwOrdDetBase(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("erp.BiResultHwQuoDet used");
		cpi = new ColorPickerGetItemProperty();
	}
	
	class HwQuoDetCellCollection extends BiCellCollection {
		public HwQuoDetCellCollection (BiCellCollection p_parent,BiResult p_br) {
			super(p_parent,p_br);
		}
		@Override
		public Object evalFunction(String p_fname,Vector args) throws Exception
		{
			if(p_fname.equals("optionToDesc")) {
				if(getDeltaType(sh,getCell("ind_pdsrg").getInt()) == DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
					int messrg = 0;
					if(args.size() > 0) {
						if(args.get(1) instanceof Integer) messrg = (Integer) args.get(1);
						if(args.get(1) instanceof Double) messrg = (int) ((Double) args.get(1)).doubleValue();
					}
					String s = optionToDesc((String) args.get(0),messrg,getView().getSchema().getAgent());
					return(s);
				} else return("");
			}
			return(super.evalFunction(p_fname,args));
		}
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return (new HwQuoDetCellCollection (p_parent,this));
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		BiResultHwOrderBase brQuotation = (BiResultHwOrderBase) getParent();

		brQuotation.setCellActionCalTotalAmount((ColumnCell) (col.getCell("ind_qty") ));
		brQuotation.setCellActionCalTotalAmount((ColumnCell) (col.getCell("ind_usize1") ));
		brQuotation.setCellActionCalTotalAmount((ColumnCell) (col.getCell("ind_usize2") ));
		
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_size1") ));
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_usize1") ));
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_bleed1") ));
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_bleed1r") ));
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_size2") ));
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_usize2") ));
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_bleed2") ));
		brQuotation.setCellActionCalUntrimSize((ColumnCell) (col.getCell("ind_bleed2r") ));
		col.getCell("ind_color").setItemPropertyInterface(cpi);
	}

	@Override
	protected void afterLoadCollection(boolean p_isfetch,BiCellCollection col)
	{
		int pdsrg = col.getCell("ind_pdsrg").getInt();
		if(getDeltaType(sh,pdsrg) == DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
//			try {
//				col.getCell("ind_optdesc").set(optionToDesc(col.getCell("ind_options").getString()));
//			} catch (CellException cex ) {
//				UniLog.log(cex);
//			}
			boolean ae = isActionEnabled();
			try {
				/* disable trigger before set ind_usize manually */
				setActionEnabled(false);
				col.getCell("ind_usize1").sync(
				col.getCell("ind_size1").getDouble() +
				col.getCell("ind_bleed1").getDouble() +
				col.getCell("ind_bleed1r").getDouble()
						);
				col.getCell("ind_usize2").sync(
				col.getCell("ind_size2").getDouble() +
				col.getCell("ind_bleed2").getDouble() +
				col.getCell("ind_bleed2r").getDouble()
						);
				setActionEnabled(ae);
			} catch (CellException cex) {
				setActionEnabled(ae);
				UniLog.log(cex);
			}
		}
	}	
	public int optionGetPrinterId(String p_option) {
		try {
			String s = null,desc="";
			if(p_option == null || p_option.trim().equals("")) {
				return(0);
			}
			JSONObject jo = new JSONObject(p_option);
			if((s = jo.optString("machine")) != null) {
				TableRec tr = getSelectUtil().getQueryResult("select * from stock where st_mtype = 'D' and st_iname = '"+s+"'");
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					return(tr.getFieldInt("st_irg"));
				}
			}
			return(0);
		} catch (Exception jex) {
			UniLog.log(jex);
			return(0);
		}
	}
	static String optionToDesc(String p_option,int p_messrg,String p_agent) {
		try {
			String s = null,desc="";
			if(p_option == null || p_option.trim().equals("")) {
//				return("-- Plese select print option --");
				s = "-- Plese select print option --" + "<br>";
			} else {
			JSONObject jo = new JSONObject(p_option);
			if((s = jo.optString("machine")) != null) {
				desc += "Machine:&nbsp;" + s ;
				desc += "<br>";
				JSONArray ja = (JSONArray) jo.opt("mhcoptions");
				if(ja != null) {
				for(int i=0;i<ja.length();i++) {
					JSONObject jopt = (JSONObject) ja.get(i);
					boolean needComma = false;
					if(jopt != null) {
						if((s = jopt.optString("optname")) != null && !s.trim().equals("")) {
							if(!s.startsWith(".")) {
								desc +=  s ;
								needComma = true;
							}
							if((s = jopt.optString("selopt1")) != null && !s.trim().equals("")) {
								if(needComma) desc += ",";
								desc +=  s;
								needComma = true;
							}
							if((s = jopt.optString("selopt2")) != null && !s.trim().equals("")) {
								if(needComma) desc += ",";
								desc +=  s;
								needComma = true;
							}
							desc += "<br>";
						}
					}
				}
				}
			}
			if((s = jo.optString("material")) != null) {
				desc += "Material:&nbsp;" + s ;
				desc += "<br>";
				JSONArray ja = (JSONArray) jo.opt("matoptions");
				if(ja != null) {
				for(int i=0;i<ja.length();i++) {
					JSONObject jopt = (JSONObject) ja.get(i);
					boolean needComma = false;
					if(jopt != null) {
						if((s = jopt.optString("optname")) != null && !s.trim().equals("")) {
							if(!s.startsWith(".")) {
//								desc += "Option:&nbsp;" + s ;
								desc += s ;
								needComma = true;
							}
							if((s = jopt.optString("selopt1")) != null && !s.trim().equals("")) {
								if(needComma) desc += ",";
								desc += s;
								needComma = true;
							}
							if((s = jopt.optString("selopt2")) != null && !s.trim().equals("")) {
								needComma = true;
								if(needComma) desc += ",";
								desc += s;
							}
							desc += "<br>";
						}
					}
				}
				}
			}
			JSONArray ja = (JSONArray) jo.opt("catoptions");
			if(ja != null) {
				for(int i=0;i<ja.length();i++) {
					JSONObject jopt = (JSONObject) ja.get(i);
					boolean needComma = false;
					if(jopt != null) {
						if((s = jopt.optString("optname")) != null && !s.trim().equals("")) {
							if(!s.startsWith(".")) {
//								desc += "Option:&nbsp;" + s ;
								desc += s ;
								needComma = true;
							}
							if((s = jopt.optString("selopt1")) != null && !s.trim().equals("")) {
								if(needComma) desc += ",";
								desc += s;
								needComma = true;
							}
							if((s = jopt.optString("selopt2")) != null && !s.trim().equals("")) {
								needComma = true;
								if(needComma) desc += ",";
								desc += s;
							}
							desc += "<br>";
						}
					}
				}
			}
			if((s = jo.optString("remark")) != null) {
				desc += "Remark:&nbsp;" + s ;
				desc += "<br>";
			}
			}
			if(p_messrg > 0) {
			String base64Img = null;
			try{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				FilingUtil.getFile(p_agent, null, new Sprintf("jxHwQuoDetFiling_%06d").add(p_messrg).toString() , bos);
				bos.close();
				//TODO: obtain image
//				base64Img = ImageUtil.getBase64ImageString(new FileInputStream(ZkUtil.getWebContentRealPath("/images/logo/banner_hw.jpg",false)),"image/jpeg");
				ByteArrayInputStream ios = new ByteArrayInputStream(bos.toByteArray());
				base64Img = ImageUtil.getBase64ImageString(ios ,"image/jpeg");
//				base64Img = ImageUtil.getBase64ImageString(ios ,"image/png");
				ios.close();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			if (base64Img != null){
				desc +="<img src=\""+ base64Img + "\" style=\"max-width:300px; max-height:300px;\" alt=\"Image\" />";
			}
			}
			return(desc);
		} catch (JSONException jex) {
			UniLog.log(jex);
			return("Error");
		}
	}
	
}
