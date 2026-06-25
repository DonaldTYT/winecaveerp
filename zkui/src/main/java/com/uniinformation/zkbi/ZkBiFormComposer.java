package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkf.ZkCellActionForm;
import com.uniinformation.zkf.ZkCellComposer;

public class ZkBiFormComposer extends ZkCellComposer {
	protected BiResult result = null;
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		super.doAfterCompose(arg0);	
		if(!sessionHelper.isLogin()) return;
		if(arg0 instanceof HtmlBasedComponent) {
			((HtmlBasedComponent) arg0).setWidth("");
			((HtmlBasedComponent) arg0).setHflex("min");
		}
		String viewId = Executions.getCurrent().getParameter("viewid");
		if(viewId != null) {
    		BiSchema schema = BiSchema.loadSchema(sessionHelper);
    		BiView view = schema.getViewByName(viewId);
			result = view.newBiResult(sessionHelper.getLoginId(),null,null,sessionHelper);
		}
		List qiList = ZkUtil.getChildrenbyClass(arg0,"com.uniinformation.zkbi.QueryConditionRow") ;
		for(QueryConditionRow  qr : (List<QueryConditionRow>) qiList) {
			/*
			int queryType = 0;
			String bc = (String) qr.getAttribute("bicolumn");
			if(bc != null) {
				queryType = result.getCell(ss).getType();
			}
			bc = (String) qr.getAttribute("querytype");
			if(bc != null) {
				if(bc.equals("date")) queryType = Cell.VTYPE_DATE;
			}
			*/
//			String id = qr.getId();
				
			String fdname = (String) qr.getAttribute("fdname");
			BiColumn bc = null;
			if(fdname != null) {
				if(result != null) bc = result.getColumnByLabel(fdname);
				List<QueryConditionRow.ConditionOp> ss = new ArrayList<QueryConditionRow.ConditionOp>();
				ss.add(QueryConditionRow.ConditionOp.EQ);
				ss.add(QueryConditionRow.ConditionOp.BETWEEN);
				int fdtype = -1;
				if(qr.getAttribute("fdtype") != null) {
					fdtype = Cell.getTypeByName((String) qr.getAttribute("fdtype"));
				} else {
					if(bc != null) fdtype = result.getCell(bc.getLabel()).getType();
				}
				switch(fdtype) {
				case Cell.VTYPE_STRING:
//					ss.add(QueryConditionRow.ConditionOp.IN_ITEMLIST);
//					ss.add(QueryConditionRow.ConditionOp.NOTIN_ITEMLIST);
					ss.add(QueryConditionRow.ConditionOp.REGEXP);
					ss.add(QueryConditionRow.ConditionOp.NOT_REGEXP);
					break;
				case Cell.VTYPE_DATE:
//					ss.add(QueryConditionRow.ConditionOp.GT);
					ss.add(QueryConditionRow.ConditionOp.GE);
//					ss.add(QueryConditionRow.ConditionOp.LT);
					ss.add(QueryConditionRow.ConditionOp.LE);
					break;
				}
				/*
				if(bc.getColumnType().equals("char")) {
					ss.add(QueryConditionRow.ConditionOp.IN_ITEMLIST);
					ss.add(QueryConditionRow.ConditionOp.NOTIN_ITEMLIST);
					ss.add(QueryConditionRow.ConditionOp.REGEXP);
					ss.add(QueryConditionRow.ConditionOp.NOT_REGEXP);
				}
				if(bc.getColumnType().equals("date")) {
					ss.add(QueryConditionRow.ConditionOp.GT);
					ss.add(QueryConditionRow.ConditionOp.GE);
					ss.add(QueryConditionRow.ConditionOp.LT);
					ss.add(QueryConditionRow.ConditionOp.LE);
				}
				*/
				qr.createConditionBlockRow(result, bc, null,ss);
//					qr.createConditionBlockRow(result, bc, null,null);
			}
		}
	}
}
