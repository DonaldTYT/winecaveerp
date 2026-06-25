package com.uniinformation.jx.zk;

import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import org.apache.commons.lang3.tuple.Pair;
public class JxZkSelector extends JxZkInputElement {
//	Vector itemList;
	Listbox listbox;
	
	AbstractGetItemProperty gipi;
	public JxZkSelector(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		comp.addEventListener(Events.ON_SELECT, zkEventListener);

		listbox = (Listbox) comp;
		
		
		UniLog.log("HAHA 2016 JxZKSelector created " + getName());
	}
	@Override
	public void setItemList(List p_itemList)
	{
		setItemListGen(p_itemList,null);
	}
	@Override
	public void setItemListInterface(AbstractGetItemProperty p_interface)
	{
		setItemListGen(null,p_interface);
	}
	void setItemListGen(List p_itemList,AbstractGetItemProperty p_interface)
	{
		listbox.setSelectedIndex(-1);
		textValue = orgValue = null;
		int n = listbox.getItemCount();
		for(int i = n-1;i>=0;i--) {
			listbox.removeItemAt(i);
		}
		if(p_interface != null) {
			gipi = p_interface;
//			itemList = null;
		} 
		else {
			gipi = null;
			if(p_itemList != null) {
//				itemList = new Vector(p_itemList);
//				itemList = new Vector();
				for(Object o : p_itemList) {
					if(o instanceof Pair) {
//						itemList.add(((Pair) o).getRight());
//						itemList.add(((Pair) o).getLeft());
					} else {
//						itemList.add(o);
					}
							
				}
			} // else itemList = null;
		}
		if(p_itemList != null) {
			n = p_itemList.size();
			for(int i = 0;i<n;i++) {
				Object o = p_itemList.get(i);
				if(o instanceof Pair) {
//					listbox.appendItem(((Pair) o).getRight().toString(), ((Pair ) o).getLeft().toString());
					Listitem li = listbox.appendItem(((Pair) o).getRight().toString(), null);
					li.setValue(o);
				} else {
					listbox.appendItem(o.toString(), null);
				}
//				listbox.appendItem(p_itemList.get(i).toString(), null);
			}
		} else {
			if(p_interface != null) {
				n = gipi.getRowCount();
				for(int i = 0;i<n;i++) {
					//listbox.appendItem( gipi.getRow(i).toString(),null);
					if (gipi instanceof GipiNamedItemList) {
						listbox.appendItem(((GipiNamedItemList)gipi).getName(i),gipi.getRow(i).toString());
					} else if (gipi instanceof TranslateListGetItemProperty) {
						listbox.appendItem(
								((TranslateListGetItemProperty)gipi).getString( gipi.getRow(i).toString()),
								gipi.getRow(i).toString()
								);
					} else if (gipi instanceof BiPickGetItemProperty) {
						listbox.appendItem(((BiPickGetItemProperty)gipi).getRowString(i), ((BiPickGetItemProperty)gipi).getRowListItemValue(i));
					} else {
						listbox.appendItem( gipi.getRow(i).toString(),null);
					}
				}
			}
		}
		//ZkUtil.dumpData(listbox);
		if (ZkUtil.isSelect2(listbox)) {
			ZkUtil.setupSelect2(listbox);
		}
	}
	
	@Override
	public int getItemIndex()
	{
		return(((Listbox) comp).getSelectedIndex());
	}
	@Override
	public void setItemIndex(int p_idx)
	{
		((Listbox) comp).setSelectedIndex(p_idx);
	}
	/*
	@Override
	public Object getValue() {
		return(null);
	}
	*/
	@Override
	public Object getValue() {
		Listitem li = listbox.getSelectedItem();
		//ZkUtil.dumpData(li);
		if (li != null) {
			return li.getValue();
		}
		return null;
	}

	@Override
	public void setText(String p_text)
	{
		orgValue = p_text;
		if(textValue == null || !textValue.equals(p_text)) {
			/*
			int n = listbox.getItemCount();
			for(int i = 0;i<n;i++) {
				Listitem li = listbox.getItemAtIndex(i);
				if(li.getLabel().equals(p_text)) {
					listbox.setSelectedIndex(i);
					textValue = p_text;
					ZkUtil.setupSelect2(listbox);
					return;
				}
			}
			listbox.appendItem(p_text,null);
			listbox.setSelectedIndex(n);
			*/
			/*
		if(itemList != null) {
			int idx = itemList.indexOf(p_text);
			if(idx >= 0) {
				listbox.setSelectedIndex(idx);
			} else {
//				listbox.setSelectedIndex(-1);
				itemList.add(p_text);
				listbox.appendItem(p_text,null);
				listbox.setSelectedIndex(itemList.size()-1);
			}
		}	
			*/
		if(gipi != null) {
			int idx = gipi.getIndexOf(p_text);
			listbox.setSelectedIndex(idx);
		} else if(getJxField().getJxFieldType() == JxField.FTYPE_INT) {
			int idx = -1;
			if(p_text != null && !p_text.trim().equals("")) idx = Integer.parseInt(p_text.trim());
			if(idx >= 0) {
				listbox.setSelectedIndex(idx);
			} else {
				listbox.clearSelection();
			}
		} else {
			int n = listbox.getItemCount();
			boolean found = false;
			for(int i = 0;i<n;i++) {
				Listitem li = listbox.getItemAtIndex(i);
				if(li.getLabel().equals(p_text)) {
					listbox.setSelectedIndex(i);
					found = true;
					break;
				}
			}
			if(!found) {
				/*
				if(p_text instanceof Pair) {
					Listitem li = listbox.appendItem(((Pair) o).getRight().toString(), null);
					li.setValue(o);
				} else {
					listbox.appendItem(p_text,null);
				}
				*/
				listbox.appendItem(p_text,null);
				listbox.setSelectedIndex(n);
			}
		}
		textValue = p_text;
		//for init select2 and value update trigger select2 change
		ZkUtil.setupSelect2(listbox);
		}
	}
	
	public void setEnable(boolean b)
	{
		listbox.setDisabled(!b); 
		if(b) {
			ZkUtil.removeSclass((HtmlBasedComponent) listbox, "zkbi-selector-disabled");
		} else {
			ZkUtil.appendSclass((HtmlBasedComponent) listbox, "zkbi-selector-disabled");
		}
		
	}
	@Override
	public int getFieldClass() {
		return(1);
	}
}
