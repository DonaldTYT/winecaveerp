package com.uniinformation.erpv4;

import java.util.Date;

import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.VectorUtil;

public class AccApi {
	static public ReturnMsg crh_add_adddesc(RpcClient p_rpc,
			String p_desc) {
		Value v = p_rpc.callSegment("crh_add_adddesc",
			new VectorUtil()
			.addElement(p_desc)
			.toVector()
				);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error crh_add_adddesc"));
		}
		return(ReturnMsg.defaultOk);
	}
	static public ReturnMsg crh_add_start(RpcClient p_rpc,
			String p_module,
			String p_crno,
			Date p_date,
			String p_vcode,
			String p_cheque,
			String p_bank,
			double p_amount,
			String p_cid,
			double p_xrate,
			String p_ano,
			String p_voucher, //Receipt No.
			double p_lamount
			) {
		Value v = p_rpc.callSegment("crh_add_start_real",
			new VectorUtil()
			.addElement(p_module)
			.addElement(p_crno)
			.addElement(p_date)
			.addElement(p_vcode)
			.addElement(p_cheque)
			.addElement(p_bank)
			.addElement(p_amount)
			.addElement(p_cid)
			.addElement(p_xrate)
			.addElement(p_ano)
			.addElement(p_voucher)
			.addElement("")
			.addElement("")
			.addElement("")
			.addElement(p_lamount)
			.addElement("DP")
			.addElement("Y")
			.addElement("")
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error crh_add_start"));
		}
		return(ReturnMsg.defaultOk);
		
	}
	static public ReturnMsg crh_add_crd(RpcClient p_rpc,
			String p_invno,
			String p_ano,
			String p_cid,
			double p_xrate,
			double p_amount,
			double p_lamount,
			String p_vcode,
			String p_desc
			) {
	Value v = p_rpc.callSegment("crh_add_crd",
		new VectorUtil()
		.addElement(p_invno)
		.addElement(p_ano)
		.addElement(p_cid)
		.addElement(p_xrate)
		.addElement(p_amount)
		.addElement(p_lamount)
		.addElement(p_vcode)
		.addElement(p_desc)
		.toVector()
	);
	if(v != null && v.toInt() != 0) {
		return (new ReturnMsg(false,"Error crh_add_crd"));
	}
	return(ReturnMsg.defaultOk);
	
	}	
	static public ReturnMsg crh_add_end(RpcClient p_rpc) {
		Value v = p_rpc.callSegment("crh_add_end",
			new VectorUtil()
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error crh_add_end"));
		}
		return(ReturnMsg.defaultOk);
	}
	static public ReturnMsg sih_add_start(RpcClient p_rpc,
			String p_module,
			String p_sno,
			String p_vcode,
			Date p_date,
			int p_dueday,
			String p_cid,
			double p_amount,
			double p_xrate,
			double p_lamount,
			String p_type, /* I,C,D */
			String p_dpcode,
			String p_frxno,
			String p_vendorref
			) {
		Value v = p_rpc.callSegment("sih_real_add_start",
			new VectorUtil()
			.addElement(p_module)
			.addElement(p_sno)
			.addElement(p_vcode)
			.addElement(p_date)
			.addElement(p_dueday)
			.addElement(p_cid)
			.addElement(p_amount)
			.addElement(p_xrate)
			.addElement(p_lamount)
			.addElement(p_type) /* I,C,D */
			.addElement(p_dpcode)
			.addElement(p_frxno)
			.addElement(p_vendorref)
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error sih_add_start"));
		}
		return(ReturnMsg.defaultOk);
	}
	
	/* no analysis code version */
	static public ReturnMsg sih_add_addano(RpcClient p_rpc,
			String p_ano,
			double p_amount,
			double p_xrate,
			double p_lamount,
			String p_cid,
			String p_desc0
			) {
		Value v = p_rpc.callSegment("sih_add_addano",
			new VectorUtil()
			.addElement(p_ano)
			.addElement(p_amount)
			.addElement(p_xrate)
			.addElement(p_lamount)
			.addElement(p_cid)
			.addElement(p_desc0)
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error sih_add_addan0"));
		}
		return(ReturnMsg.defaultOk);
	}
	static public ReturnMsg sih_add_addanoex(RpcClient p_rpc,
			String p_ano,
			double p_amount,
			double p_xrate,
			double p_lamount,
			String p_cid,
			String p_desc0,
			String p_dpcode
			) {
		Value v = p_rpc.callSegment("sih_add_addano",
			new VectorUtil()
			.addElement(p_ano)
			.addElement(p_amount)
			.addElement(p_xrate)
			.addElement(p_lamount)
			.addElement(p_cid)
			.addElement(p_desc0)
			.addElement(p_dpcode)
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error sih_add_addan0"));
		}
		return(ReturnMsg.defaultOk);
	}
	static public ReturnMsg sih_add_end(RpcClient p_rpc) {
		Value v = p_rpc.callSegment("sih_add_end",
			new VectorUtil()
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error sih_add_end"));
		}
		return(ReturnMsg.defaultOk);
	}
	static public ReturnMsg arpost_one_sih(RpcClient p_rpc,String p_module,String p_voucher) {
		Value v = p_rpc.callSegment("arpost_one_sih_silent",
					new VectorUtil()
						.addElement(p_module)
						.addElement(p_voucher)
						.toVector()
				);
		if(v != null && v.toInt() == 0) {
			return(ReturnMsg.defaultOk);
		} else {
			return(new ReturnMsg(false,"Fail posting invoice record"));
		}
	}
	static public String getRgControl(RpcClient p_rpc,String p_key,java.util.Date p_date) {
		Value v = p_rpc.callSegment(
					"getrg_byrgcontrol_bycategory",
					new VectorUtil()
						.addElement(p_key)
						.addElement(p_date)
						.toVector()
				);
		return(v.toString().trim());
	}
	static public ReturnMsg sih_remove(RpcClient p_rpc,String p_cocode,String p_baseccy,String p_voucher) {
		p_rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(p_cocode)
							.addElement(p_baseccy)
							.toVector()
							);
		Value v = p_rpc.callSegment("stmpostinv_removesih",
					new VectorUtil()
						.addElement(p_voucher)
						.toVector()
				);
		if(v != null && v.toInt() == 0) {
			return(ReturnMsg.defaultOk);
		} else {
			return(new ReturnMsg(false,"Fail deleting invoice record"));
		}
	}
	static public ReturnMsg arpost_one_crh(RpcClient p_rpc,String p_module,String p_voucher) {
		Value v = p_rpc.callSegment("arpost_one_crh_silent",
					new VectorUtil()
						.addElement(p_module)
						.addElement(p_voucher)
						.toVector()
				);
		if(v != null && v.toInt() == 0) {
			return(ReturnMsg.defaultOk);
		} else {
			return(new ReturnMsg(false,"Fail posting payment record"));
		}
	}
}
