package com.uniinformation.jxapp.erpv4;

import java.util.Hashtable;
import java.util.Vector;

import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;

public class JxZkBiErpv4 extends JxZkBiBase {
	Hashtable<String,BiResult> pickBrHash=null;
	TrGetItemProperty tgipiSelPresetStr;
	JxActionListener presetStrActionListener;
	protected boolean jxZkBiPickPresetString(ColumnCell p_bcc,ZkJxPickInput p_zkpi,String p_key) {
		return(jxZkBiPickPresetString(p_bcc,p_zkpi,p_key,null));
	}
	protected boolean jxZkBiPickPresetString(ColumnCell p_bcc,ZkJxPickInput p_zkpi,String p_key,String p_subCond) {
		if(tgipiSelPresetStr == null) {
			tgipiSelPresetStr = new TrGetItemProperty(
				new VectorUtil()
					.addElement("pstd_str")
					.toVector(),
				new VectorUtil()
					.addElement("Presets")
					.toVector(),
				new VectorUtil()
					.addElement("100%")
					.toVector()
			);
			
		}
		if(presetStrActionListener == null) {
			presetStrActionListener = 
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object[] rec = (Object[]) ((JxSelOpt) fd.getJxForm()).getPickListBoxValue();
					if (rec != null) {
						try {
							ColumnCell ccol = (ColumnCell) ((JxSelOpt) fd.getJxForm()).getUserData();
							TableRec tr = tgipiSelPresetStr.getTableRec();
							ccol.update(
									rec[tr.getFieldIndex("pstd_str")].toString().trim()
									);
						} catch (Exception cex ) {  
							UniLog.log(cex);
						} 
						fd.getJxForm().closeForm();
					}
				}
			};
		}
		JxSelOpt selopt = getPulldownSelOpt();
		p_zkpi.setJxZkForm(selopt);
		try {
			TableRec tr;
			String keystr = p_key;
			if(keystr == null) keystr = (p_bcc.getBiColumn().getView().getName() + "." + p_bcc.getBiColumn().getLabel());
			keystr = keystr.toUpperCase();
			/*
			tr = getBr().getSelectUtil().getQueryResult("select * from presetmaster,outer presetdetail where "
					+ "pstm_key = '" + keystr
					+ "' and pstd_mrg = pstm_rg order by pstd_seq",null);
					*/
			Wherecl wl = new Wherecl();
			wl.andUniop("pstm_key", "=", keystr);
			if(p_subCond != null) {
				wl.appendString(p_subCond);
			}
			//wl.appendString(" and pstd_mrg = pstm_rg order by pstd_seq");
			//andrew211021 fix seq0 item wrong order
			wl.appendString(" and pstd_mrg = pstm_rg order by pstd_mrg, pstd_seq");
			tr = getBr().getSelectUtil().getQueryResult("select * from presetmaster,outer presetdetail",wl);
			if(tr.getRecordCount() > 0) {
				tgipiSelPresetStr.setTableRec(tr);
				selopt.jxAdd("pickListBox").setItemListInterface(tgipiSelPresetStr);	
				selopt.setOnSelectAction(presetStrActionListener);
				selopt.setUserData(p_bcc);
				selopt.beginPick();
			} else {
				getBr().getSelectUtil().executeUpdate("insert into presetmaster (pstm_rg,pstm_key,pstm_name,pstm_len) values (?,?,?,?)", 
							new Wherecl()
								.appendArgument(getBr().getView().getSchema().getRg(getBr(),"", 16005))
								.appendArgument(keystr)
								.appendArgument(p_bcc.getBiColumn().getEngName())
								.appendArgument(0)
				);
				p_zkpi.close();
			}
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
	

	/*
	static public void jxZkBiGetPresetList(BiResult p_br,ColumnCell p_bcc,String p_subOption) {
			String keystr = null;
			if(keystr == null) keystr = (p_bcc.getBiColumn().getView().getName() + "." + p_bcc.getBiColumn().getLabel());
			keystr = keystr.toUpperCase();
			Wherecl wl = new Wherecl();
			wl.andUniop("pstm_key", "=", keystr);
			if(p_subOption != null) {
				wl.appendString( " and (pstd_subcond = '' or pstd_subcond = '" + p_subOption + "') ");
			}
			TableRec tr;
			//wl.appendString(" and pstd_mrg = pstm_rg order by pstd_seq");
			//andrew211021 fix seq0 item wrong order
			wl.appendString(" and pstd_mrg = pstm_rg order by pstd_mrg, pstd_seq");
			try {
			tr = p_br.getSelectUtil().getQueryResult("select * from presetmaster,outer presetdetail",wl);
			Vector v = new Vector();
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				v.add(tr.getFieldString("pstd_str"));
			}
			p_bcc.setItemList(v);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
	}
	*/
	static public Vector jxZkBiGetPresetItemList(BiResult p_br,ColumnCell p_bcc,String p_subOption) {
		String keystr = null;
		if(keystr == null) keystr = (p_bcc.getBiColumn().getView().getName() + "." + p_bcc.getBiColumn().getLabel());
		keystr = keystr.toUpperCase();
		Wherecl wl = new Wherecl();
		wl.andUniop("pstm_key", "=", keystr);
		if(p_subOption != null) {
			wl.appendString( " and (pstd_subcond = '' or pstd_subcond = '" + p_subOption + "') ");
		}
		TableRec tr;
		//wl.appendString(" and pstd_mrg = pstm_rg order by pstd_seq");
		//andrew211021 fix seq0 item wrong order
		wl.appendString(" and pstd_mrg = pstm_rg order by pstd_mrg, pstd_seq");
		try {
		tr = p_br.getSelectUtil().getQueryResult("select * from presetmaster,outer presetdetail",wl);
		Vector v = new Vector();
		for(int i=0;i<tr.getRecordCount();i++) {
			tr.setRecPointer(i);
			v.add(tr.getFieldString("pstd_str"));
		}
		return(v);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
}
}
