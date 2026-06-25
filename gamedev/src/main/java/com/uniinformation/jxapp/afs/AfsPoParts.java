package com.uniinformation.jxapp.afs;

import com.uniinformation.cell.CellCollection;
import com.uniinformation.jxapp.AfsPO;
import com.uniinformation.utils.Wherecl;

public class AfsPoParts extends AfsPO {
	@Override
	protected Wherecl createPullDownWherecl(CellCollection col) {
		Wherecl wcl;
//		return(new Wherecl().appendString(" st_mtype in ('P')"));
		wcl = super.createPullDownWherecl(col);
		if(wcl == null) wcl = new Wherecl();
		wcl.genInList("and", "st_mtype","in","P");
		return(wcl);
//		new Wherecl().appendString(" st_mtype in ('P')"));
	}
}
