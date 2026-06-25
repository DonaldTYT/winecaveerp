package com.uniinformation.bicore.axa;

import java.util.Date;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.IgnoreValue;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultClaimDet extends BiResultErpv4 {

	public BiResultClaimDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	private enum FuncName { FUNC_calAxaAmount,NOT_DEFINED }
	class AxaClaimCellCollection extends Erpv4BaseCellCollection {
		public AxaClaimCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
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
				case FUNC_calAxaAmount: {
					String bc = (String) p_args.get(0);
					double amt = (Double) p_args.get(1);
					Date d = (Date) p_args.get(2);
					if(!BiResultAxaClaim.d240401.after(d)) {
						return(new IgnoreValue());
					}
					if(amt == 0 || bc == null || bc.equals("")) return(0.0);
					double axaamt = 0;
					Vector<BiCellCollection> vv = BiResultClaimDet.this.getParent().getSubLink("axa.ClaimBenefit").getRowCollectionList();
					for(BiCellCollection c : vv) {
						if(bc.equals(c.getCellString("axapol_benefitcode"))) {
							return(BiResultClaimBenefit.calAxaAmount(c,amt));
						}
					}
					return(axaamt);
				}
			}
			return(super.evalFunction(p_fname,p_args) );
		}
		
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new AxaClaimCellCollection(p_parent, this));
	}
}
