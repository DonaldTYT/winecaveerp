package com.uniinformation.bicore.aw;

import java.io.FileInputStream;

import com.uniinformation.utils.poi.ExcelPoi;
import com.kyoko.common.ChineseConvert;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.WebCoreUtil;

public class ArtwayLoadData {
	static String stripNonPrintable(String p_s)
	{
		StringBuffer sb = new StringBuffer();
		if(p_s == null) return(null);
		char[] ca = p_s.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if(ca[i] < 32) continue;
			if(Character.isWhitespace(ca[i])) sb.append(' '); else sb.append(ca[i]);
		}
		return(sb.toString());
	}
	
	public static void loadStrFromExcel(String p_fname,SelectUtil su) throws Exception {
		FileInputStream is;
		is = new FileInputStream(p_fname);
   		ExcelPoi exlpoi = null;	
		exlpoi = ExcelPoi.newExcelPoi(is,true);	
		is.close();
		for (int i=1;i<exlpoi.getRowCount();i++) {
			String xo = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(i, 1)));
			if(xo != null && !(xo.trim().equals(""))) {
				UniLog.log("row " + i + " [" + xo + "]");
				TableRec tr = su.getQueryResult(
									"select * from presetmaster where pstm_key = ?"
									, new Wherecl().appendArgument(xo));
				if(tr != null && tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					int pstm_rg = tr.getFieldInt("pstm_rg");
					for(int j=2;j<1024;j++) {
						String xd = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(i, j))).trim();
						if(xd != null && !(xd.trim().equals(""))) {
							UniLog.log(String.format("Check String %6s %4d %s",xo,j-1,xd));
							tr = su.getQueryResult("select * from presetdetail where pstd_mrg = ? and pstd_str = ? ", 
										new Wherecl()
											.appendArgument(pstm_rg)
											.appendArgument(xd)
									);
							if(tr.getRecordCount() <= 0) {
								UniLog.log(String.format("Add String %6s %4d %s",xo,j-1,xd));
								su.executeUpdate("insert into presetdetail (pstd_mrg,pstd_seq,pstd_str) values (?,?,?) ", 
										new Wherecl()
											.appendArgument(pstm_rg)
											.appendArgument(0)
											.appendArgument(xd)
										);
							}
						} else break;
					}
				}
			}
		}
		
	}
	public static void main(String args[]) throws Exception {
		UniLog.log("ArtwayLoadData");
		ChineseConvert.setFontPath("c:\\eclipse_dev\\app_basedir\\config");
		SelectUtil su = new SelectUtil();
		su.init(WebCoreUtil.getJdbcPoolByConnectionString("awLoadData",2,
				"jdbc:scorpion:perfrpc:dtqemu2.uniconn.com:19002:gl:dbpath:/yic/v/acc.new/data:chaindir:-p /yic/v/acc.new/src/chn -p /yic/v/acc.new/src/gl -p /yic/v/acc.new/src/glchn"
				,null,null).getConnection());
		loadStrFromExcel("c:\\tmp\\awstrlist.xlsx",su);
	}
}
