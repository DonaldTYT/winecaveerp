package com.uniinformation.jxapp.erpv4;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultControlAccount;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class ChartOfAccount extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		BiResultControlAccount sr = (BiResultControlAccount) p_br.getSubLink("erpv4.ControlAccount");
		Vector<BiCellCollection> cl = sr.getRowCollectionList();
		/*
		for(BiCellCollection col:cl) {
			int level = sr.getLevel(col.getCellString("ca_ano"));
			level = 0;
		}
		*/
	}
	
	@Override
	public void onCellMapp(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("ca_anspacer")) {
			BiResultControlAccount sr = (BiResultControlAccount) p_cc.getBiResult();
			BiCellCollection col = p_cc.getCollection();
			ZkBiCellValueMapper cm = (ZkBiCellValueMapper) p_cc.getMapper();
			if(cm != null) {
				HtmlBasedComponent c = (HtmlBasedComponent) cm.getComponent();
				if(c != null) {
//					int level = sr.getLevel(col.getCellString("ca_ano"));
					int level = col.getCellInt("ca_level");
					c.setWidth(String.format("%dpx", level*30));
				}
			}
		}
	}
}
