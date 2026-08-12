package com.uniinformation.bicore.aw;

import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.UniLog;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.cell.IgnoreValue;

public class ArtwayCellCollection extends Erpv4BaseCellCollection {
	private enum FuncName { FUNC_getImpString,NOT_DEFINED }
	public ArtwayCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
		super(p_parent, p_br);
		// TODO Auto-generated constructor stub
	}
	@Override
	public Object evalFunction(String p_fname,Vector p_args) throws Exception {
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		
		switch (funcName){
			case FUNC_getImpString: {
				String imps[] = ((String)p_args.get(0)).split(" ");
				String res = "";
				for(int i=0;i<imps.length;i++) {
					try {
						if(!StringUtils.isBlank(imps[i])) {
						int idx = Integer.parseInt(imps[i]);
						switch(idx) {
						case 1 : res += "左右反版 "; break;
						case 2 : res += "牙口反版 "; break;
						case 3 : res += "低面版 "; break;
						case 4 : res += "單面 "; break;
						}
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				return(res);
			}
		}
		return(super.evalFunction(p_fname,p_args) );
	}
}
