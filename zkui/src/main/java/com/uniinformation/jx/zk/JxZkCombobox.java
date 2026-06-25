package com.uniinformation.jx.zk;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Radiogroup;

import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.UniLog;
import org.apache.commons.lang3.tuple.Pair;
public class JxZkCombobox extends JxZkInputElement {
	public JxZkCombobox(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		comp.addEventListener(Events.ON_SELECT, zkEventListener);
//		comp.addEventListener(Events.ON_OPEN, zkEventListener);
	}
	@Override
	public void setItemList(List itemlist)
	{
			Combobox cb = (Combobox) comp;
			int n = cb.getItemCount();
			for(int i = n-1;i>=0;i--) {
				cb.removeItemAt(i);
			}
			if(itemlist == null) return;
			for(int i = 0;i<itemlist.size();i++) {
				Object o = itemlist.get(i);
				if(o instanceof Pair) {
					Comboitem ci = cb.appendItem(((Pair) o).getRight().toString());
					ci.setValue(o);
				} else {
					Comboitem ci = cb.appendItem(o.toString());
					ci.setValue(o);
				}
//				Comboitem ci = cb.appendItem(itemlist.get(i).toString());
//				ci.setValue(itemlist.get(i));
			}
	}
	@Override
	public void setItemListInterface(AbstractGetItemProperty p_interface)
	{
			Combobox cb = (Combobox) comp;
			int n = cb.getItemCount();
			for(int i = n-1;i>=0;i--) {
				cb.removeItemAt(i);
			}
			n = p_interface.getRowCount();
			for(int i = 0;i<n;i++) {
				String lb = null;
//				if(p_interface.getStatus(p_interface.getRow(i),AbstractGetItemProperty.GIPI_DELETED)) continue;
				lb = p_interface.getString(p_interface.getRow(i));
				Comboitem ci = cb.appendItem(lb);
				ci.setValue(p_interface.getRow(i));
				if(p_interface.getStatus(p_interface.getRow(i),AbstractGetItemProperty.GIPI_DELETED)) ci.setVisible(false);
			}
	}
	
//	@Override
//	public void setItemList(Vector itemlist,Vector labelList)
//	{
//			Combobox cb = (Combobox) comp;
//			int n = cb.getItemCount();
//			for(int i = n-1;i>=0;i--) {
//				cb.removeItemAt(i);
//			}
//			if(itemlist == null) return;
//			for(int i = 0;i<itemlist.size();i++) {
//				String lb = null;
//				if(labelList != null) lb = (String) labelList.get(i);
//				if(lb == null) lb = itemlist.get(i).toString();
//				Comboitem ci = cb.appendItem(lb);
//				ci.setValue(itemlist.get(i));
//			}
//	}
	@Override
	public void setItemIndex(int p_idx)
	{
		UniLog.log("HAHA in combobox setItemIndex " + p_idx);
		((Combobox) comp).setSelectedIndex(p_idx);
	}
	
	@Override
	public Vector getSelectList()
	{
		Combobox cb = (Combobox) comp;
		Vector v = new Vector();
		Comboitem ci = cb.getSelectedItem();
		if(ci == null) return(v);
		Object o = ci.getValue();
		if(o != null) v.add(o); else v.add(ci.getLabel());
		return(v);
	}
	@Override
	public int getItemIndex() {
		Combobox cb = (Combobox) comp;
		return(cb.getSelectedIndex());
	}
	
	@Override
	public Object getValue() {
		Combobox cb = (Combobox) comp;
		Comboitem ci = cb.getSelectedItem();
		if(ci != null) return(ci.getValue()); else return(null);
	}
}
