package com.uniinformation.jxapp.clinic;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4.MoPos;
import com.uniinformation.utils.UniLog;

public class MoPosClinic extends MoPos {
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		/*
		if(mode == JxZkBiBase.MODE_ADD) {
			try {
				br.getCell("vd_vname").set("TBC");
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
		*/
	}
}
