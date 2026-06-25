package com.uniinformation.jxapp.wc;

import java.util.Vector;

import org.zkoss.zul.Textbox;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class Stock extends JxZkBiBase {
	@Override
	public void afterBind() {
		super.afterBind();
		Textbox tb = (Textbox) jxAdd("st_memo").getNativeObject();
		tb.setHeight("200px");
		if(!sessionHelper.isMobileDevice()){
			tb.setWidth("700px");
		}
		tb.setHeight(null);
		tb.setRows(6);
		tb.setMaxlength(4096);
		jxAdd("list_"+JxZkBiBase.replaceViewName("wc.StockMemo")).setVisible(false);
	}
	
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		String notes = "";
		BiResult sr = br.getSubLink("wc.StockMemo");
		Vector<BiCellCollection> v = sr.getRowCollectionList();
		for(BiCellCollection col : v){
			notes += col.getCell("mm_desc").toString();
			notes += "\n";
		}
		try {
			br.getCell("st_memo").set(notes);
		} catch (CellException cex){
			UniLog.log(cex);
		}
	}
}
