package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;

public class Erpv4PriceDetCellCollection extends Erpv4BaseCellCollection {
	CellValueAction actionIrgChanged = null;
	Erpv4StockAttribute stattr = null;
	private enum FuncName { FUNC_getStPrice,FUNC_getStPrice0,FUNC_getStPrice1,FUNC_getStPrice2,FUNC_getStPrice3, NOT_DEFINED }
	public Erpv4PriceDetCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
		super(p_parent, p_br);
		// TODO Auto-generated constructor stub
		
		actionIrgChanged = new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
				int irg = getInt("prpd_irg");
				if(irg > 0) {
					if(stattr == null || stattr.irg != irg) {
						stattr = Erpv4StockAttribute.getStockAttribute(DateUtil.zeroDate,br.getSessionHelper(),br.getSelectUtil(), irg);
					}
				} else {
					stattr = null;
				}
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
	
	@Override
	protected void afterCreateColumnCells() {
		if(testCell("prpd_irg") != null) {
			getCell("prpd_irg").addAction(actionIrgChanged);
		}
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
		case FUNC_getStPrice: {
				if(stattr != null) {
					String pl = (String) p_args.get(1);
					Double dd = stattr.prices.get(pl);
					if(dd != null) return(dd);
				}
				return(0.0);
		}
		case FUNC_getStPrice0: {
				if(stattr != null) return(stattr.prices.get(BiResultCustomer.dft_Price0_label));
				return(0.0);
			}
		case FUNC_getStPrice1: {
				if(stattr != null) return(stattr.prices.get(BiResultCustomer.dft_Price1_label));
				return(0.0);
			}
		case FUNC_getStPrice2: {
				if(stattr != null) return(stattr.prices.get(BiResultCustomer.dft_Price2_label));
				return(0.0);
			}
		case FUNC_getStPrice3: {
				if(stattr != null) return(stattr.prices.get(BiResultCustomer.dft_Price3_label));
				return(0.0);
			}
		}
		return(super.evalFunction(p_fname, p_args));
	}
}
