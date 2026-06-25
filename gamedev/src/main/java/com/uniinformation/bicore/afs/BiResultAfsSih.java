package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultArAp;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsSih extends BiResultArAp{

	public BiResultAfsSih(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getLinkedView(String p_colName,CellCollection p_col) {
		if(p_colName.equals("inv_srcref")) {
			String fxref = p_col.getCellString("stm_ref1");
			if(fxref.isEmpty()) {
				fxref = p_col.getCellString("inv_invno");
				if(fxref.startsWith("AQP")) return("afs.AfsQuoParts");
				if(fxref.startsWith("AQM")) return("afs.AfsQuoMc");
			} else {
				if(fxref.startsWith("MDN")) return("afs.AfsDoMc");
				if(fxref.startsWith("ADN")) return("afs.AfsDoParts");
				if(fxref.startsWith("GM")) return("AfsGR");
			}
		}
		if(p_colName.equals("inv_invno")) {
			String fxref = p_col.getCellString("inv_invno");
			if(fxref.startsWith("AQP")) return("afs.AfsQuoParts");
			if(fxref.startsWith("AQM")) return("afs.AfsQuoMc");
		}
		if(p_colName.equals("stm_ref1")) {
			String fxref = p_col.getCellString("stm_ref1");
			if(fxref.startsWith("MDN")) return("afs.AfsDoMc");
			if(fxref.startsWith("ADN")) return("afs.AfsDoParts");
			if(fxref.startsWith("GM")) return("AfsGR");
		}
		return(super.getLinkedView(p_colName,p_col));
	}
}
