package com.uniinformation.jxapp.clinic;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4.Stock;
import com.uniinformation.utils.UniLog;


public class ClerpMultiStock extends Stock {
	boolean isMyCreatedStock(BiResult br) {
		String coShortCode = Erpv4Config.getCoShortcode(getSessionHelper(), 
					Erpv4Config.getDefaultCoCode(getSessionHelper())
				);
		if(coShortCode.startsWith("PD")) {
			if(br.getCellString("st_icode").startsWith("TST")) return(true);
			if(br.getCellString("st_icode").startsWith("SKU")) return(true);
			if(br.getCellString("st_icode").startsWith("CTR")) return(true);
		}
		if(coShortCode.startsWith("GA")) {
			if(br.getCellString("st_icode").startsWith("MK")) return(true);
		}
		if(br.getCellString("st_icode").startsWith(coShortCode)) return(true);
		return(false);
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		if(mode == JxZkBiBase.MODE_UPDATE) {
		if(BiSchema.hasAccessRight(getSessionHelper(), "#updstockname") || getSessionHelper().isAdminUser()) {
			
		} else {
//			String coShortCode = Erpv4Config.getCoShortcode(getSessionHelper(), 
//						Erpv4Config.getDefaultCoCode(getSessionHelper())
//					);
//			if(!br.getCellString("st_icode").startsWith(coShortCode)) {
//				try {
//					br.getCell("st_iname").setMode(Cell.VMODE_DISPONLY);
//					br.getCell("st_einame").setMode(Cell.VMODE_DISPONLY);
//					br.getCell("st_mszrange").setMode(Cell.VMODE_DISPONLY);
//					br.getCell("st_oicode").setMode(Cell.VMODE_DISPONLY);
//					br.getCell("st_barcode").setMode(Cell.VMODE_DISPONLY);
//					br.getCell("mt_tpname").setMode(Cell.VMODE_DISPONLY);
//					br.getCell("stu_desc").setMode(Cell.VMODE_DISPONLY);
//					br.getCell("st_remark").setMode(Cell.VMODE_DISPONLY);
//				} catch(CellException cex) {
//					UniLog.log(cex);
//				}
//				
//			}
			if( !isMyCreatedStock(br)) {
			try {
				br.getCell("st_iname").setMode(Cell.VMODE_DISPONLY);
				br.getCell("st_einame").setMode(Cell.VMODE_DISPONLY);
				br.getCell("st_mszrange").setMode(Cell.VMODE_DISPONLY);
				br.getCell("st_oicode").setMode(Cell.VMODE_DISPONLY);
				br.getCell("st_barcode").setMode(Cell.VMODE_DISPONLY);
				br.getCell("mt_tpname").setMode(Cell.VMODE_DISPONLY);
				br.getCell("stu_desc").setMode(Cell.VMODE_DISPONLY);
				br.getCell("st_remark").setMode(Cell.VMODE_DISPONLY);
			} catch(CellException cex) {
				UniLog.log(cex);
			}
			
			}
		}
		}
	}

}
