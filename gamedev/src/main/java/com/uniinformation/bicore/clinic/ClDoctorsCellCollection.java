package com.uniinformation.bicore.clinic;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.ZkUtil;

public class ClDoctorsCellCollection extends BiCellCollection {

	public ClDoctorsCellCollection(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
		// TODO Auto-generated constructor stub
	}
	private enum FuncName { FUNC_makeImgFileName, NOT_DEFINED }
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
		case FUNC_makeImgFileName : {
			
			String s="";
			s+=getCellString("cldoc_name");
			s+="-";
			s+=getCellString("cldoc2_name");
			s+="-";
			Date d = getCell("mdoc_ctime").getDate();
			s += DateUtil.toDateString(d, "yyyymmdd");
			String ff = getCellString("mdoc_doctype");
			s += ZkUtil.mineTypeToExtention(ff);
			//return(s.replaceAll("\\s", "_"));
			return(s);
			}
		}
		return(super.evalFunction(p_fname,p_args) );
	}

}
