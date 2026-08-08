package com.uniinformation.bicore.wc;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.IgnoreValue;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class WcCellCollection extends Erpv4BaseCellCollection {
	
	String getNonConsignOwner() {
		return("WINECAVE");
	}

	public WcCellCollection(BiCellCollection p_col, BiResultErpv4 p_br) {
		super(p_col, p_br);
		// TODO Auto-generated constructor stub
	}
	private enum FuncName { FUNC_newBrandCode, FUNC_getConsignCost, FUNC_getConsignPrice, FUNC_getSmCode, NOT_DEFINED }
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
		case FUNC_getSmCode: {
			SelectUtil su = br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from salesman where sm_logname = ?",
						new Wherecl().appendArgument(br.getSessionHelper().getVcode()));
			if(tr.getRecordCount() <= 0) return("");
			tr.setRecPointer(0);
			String ss = tr.getFieldString("sm_code");
			if(StringUtils.isBlank(ss)) {
				return(new IgnoreValue());
			} else return(ss);
		}
		case FUNC_getConsignCost: {
			int irg = Cell.objectToInt(p_args.get(0));
			int org = Cell.objectToInt(p_args.get(1));
			if(irg == 0 || org  == 0) return(0.0);
			String cocode = (String) p_args.get(2);
			if(StringUtils.isBlank(cocode) || cocode.equals(getNonConsignOwner())) {
				return(0.0);
			}
			SelectUtil su = br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from consgprice where consgp_irg = ? and consgp_org = ?",
						new Wherecl().appendArgument(irg).appendArgument(org));
			if(tr.getRecordCount() <= 0) return(0.0);
			tr.setRecPointer(0);
			return(tr.getFieldDouble("consgp_cost"));
		}
		case FUNC_getConsignPrice : {
			int irg = Cell.objectToInt(p_args.get(0));
			int org = Cell.objectToInt(p_args.get(1));
			if(irg == 0 || org  == 0) return(0.0);
			double wprice = (Double) p_args.get(2);
			String cocode = (String) p_args.get(3);
			if(StringUtils.isBlank(cocode) || cocode.equals(getNonConsignOwner())) {
				return(wprice);
			}
			SelectUtil su = br.getSelectUtil();
			TableRec tr = su.getQueryResult("select * from consgprice where consgp_irg = ? and consgp_org = ?",
						new Wherecl().appendArgument(irg).appendArgument(org));
			if(tr.getRecordCount() <= 0) return(0.0);
			tr.setRecPointer(0);
			return(tr.getFieldDouble("consgp_price"));
		}
		case FUNC_newBrandCode: {
				String wc = Erpv4Config.getString(br.getSessionHelper(), "WINEAC");
				if(wc != null && wc.equals("Y")) {
					String ss = br.getCellString("stbd_code");
					if(ss.trim().equals("")) {
						RpcClient rpc = br.getSelectUtil().getRpcClient();
						Value v = rpc.callSegment("new_brand_code");
						if(v != null ) {
							return(v.toString());
						}
					}
				}
				return(br.getCellString("stbd_code"));
			}
		}

		return(super.evalFunction(p_fname,p_args) );
	}

}
