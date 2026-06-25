package com.uniinformation.jxapp.erpv4;

import java.util.List;

import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiJoin;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.Wherecl;

public class DetAccount extends JxZkBiBase {
	
//	void selectAggregateDetail(BiResult br,String viewName) {
//		BiResult sr = br.getSubLink(viewName);
//		if(sr == null) return;
//		sr.clear();
//		JxField sv = jxAdd("list_"+replaceViewName(sr.getView().getName()));
//		if(sv == null) return;
//		sv.setAttribute("paging", "withfilter");
//		List<Integer>sids = br.getCurrentSids();
//		if(sids == null) return;
//		if(sids.size() <= 0) return;
//		BiJoin bj = br.getView().getTable().getJoin(sr.getView().getTable());
//		if(bj == null) return;
//		StringBuffer wstr = new StringBuffer();
//		
//		if(bj.getJoinCount() == 1) {
//			wstr.append(bj.getToField(0).getFullName());
//			wstr.append(" in (select distinct ");
//			wstr.append(bj.getFromField(0).getFullName());
//			wstr.append(" from ");
//			wstr.append(br.getView().getTable().getSelectFromName());
//			wstr.append(" where ");
//			if(sids.size() == 1) {
//				wstr.append(br.getView().getTable().getSidField());
//				wstr.append(" = " + sids.get(0) + ")");
//			} else {
//				wstr.append(br.getView().getTable().getSidField());
//				wstr.append(" in (" + sids.get(0));
//				for(int j=1;j<sids.size();j++) {
//					wstr.append(" ,"+ sids.get(j));
//				}
//				wstr.append("))");
//			}
//		} else {
//			wstr.append(sr.getView().getTable().getSidField());
//			wstr.append(" in (select distinct ");
//			wstr.append(sr.getView().getTable().getSidField());
//			wstr.append(" from ");
//			wstr.append(br.getView().getTable().getSelectFromName());
//			wstr.append(" , ");
//			wstr.append(sr.getView().getTable().getSelectFromName());
//			wstr.append(" where ");
//			for(int i=0;i<bj.getJoinCount();i++) {
//				wstr.append(bj.getToField(i).getFullName());
//				wstr.append(" = ");
//				wstr.append(bj.getFromField(i).getFullName());
//				wstr.append(" and ");
//			}
//			if(sids.size() == 1) {
//				wstr.append(br.getView().getTable().getSidField());
//				wstr.append(" = " + sids.get(0) + ")");
//			} else {
//				wstr.append(br.getView().getTable().getSidField());
//				wstr.append(" in (" + sids.get(0));
//				for(int j=1;j<sids.size();j++) {
//					wstr.append(" ,"+ sids.get(j));
//				}
//				wstr.append("))");
//			}
//			
//		}
//		sr.appendWherecl(new Wherecl().appendString(wstr.toString()));
//		sr.query();
//		bindSublinkList2(sv , sr);
//		Listbox lb = (Listbox) sv.getNativeObject();
//		ListModelList lm = (ListModelList) lb.getListModel();
//		int n = lm.getSize();
//		Listitem li = lb.getItemAtIndex(n-1);
//		Clients.scrollIntoView(li);
//		
//	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		JxField fd;
		super.bindCellCollection(br, mode);
		/*
		selectAggregateDetail(br, "erpv4.GlJnListG2");
		*/
	}
}
