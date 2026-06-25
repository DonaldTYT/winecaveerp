package com.uniinformation.jxapp;

import java.util.Hashtable;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Window;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.erpv4.MO;
//import com.uniinformation.utils.AbstractGetItemProperty;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class AfsMO extends MO {
	@Override
	public void afterBind() {
		super.afterBind();
		icodePickerViewName = "AfsIcodePicker";
		detViewId = "AfsMoDet";
		canCreateStock = true;
//		LOCK_RECORD_FOR_UPDATE = true;
	}
}
