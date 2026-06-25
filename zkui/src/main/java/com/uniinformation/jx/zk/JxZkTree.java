package com.uniinformation.jx.zk;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;

import com.uniinformation.utils.UniLog;

public class JxZkTree extends JxZkElement {
	int nextIdx = 0;
	int currIdx = 0;
	Tree tr;
	Treechildren tc;
//	EventListener treeItemClickedListener;
	public JxZkTree(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		tr = (Tree) c;
		tc = tr.getTreechildren();
		if(tc == null) {
			tc = new Treechildren();
			tr.appendChild(tc);
		}
		tr.setCheckmark(true);
		tr.setMultiple(true);
		
		/*
		treeItemClickedListener =
    	new EventListener() {
	       		public void onEvent(Event event) throws Exception {
	        			UniLog.log("TreeItem Clicked");
	       }	
	    };	
	    */
		
	}
	
	private Treeitem buildTreeitem(String p_desc,int p_idx) {
		String tiLabel;
		if(p_desc.contains("\t")) {
			StringTokenizer stk = new StringTokenizer(p_desc,"\t");
			String desc = stk.nextToken();
			Treeitem ti = new Treeitem(desc,new Integer(p_idx));
			while(stk.hasMoreTokens()) {
				ti.getTreerow().appendChild(new Treecell(stk.nextToken()));
			}
			return(ti);
		} else {
			return(new Treeitem(p_desc,new Integer(p_idx)));
		}
	}
	@Override
	public int addNode(String p_desc) {
		nextIdx++;
		/*
		Treeitem ti = new Treeitem(p_desc,new Integer(nextIdx));
		ti.getTreerow().appendChild(new Treecell("HAHA"));
		*/
		Treeitem ti = buildTreeitem(p_desc,new Integer(nextIdx));
		ti.addEventListener("onClick", zkEventListener); 
		ti.setId("ti_"+nextIdx);
		tc.appendChild(ti);
		return(nextIdx);
	}

	@Override
	public int addChild(int p_parent,String p_desc) {
		Treeitem ti = (Treeitem) tc.getFellow("ti_"+p_parent);
		if(ti != null) {
			Treechildren ttc = ti.getTreechildren();
			if(ttc == null) {
				ttc = new Treechildren();
				ti.appendChild(ttc);
			}
			nextIdx++;
			Treeitem tti = buildTreeitem(p_desc,new Integer(nextIdx));
			tti.addEventListener("onClick", zkEventListener); 
			tti.setId("ti_"+nextIdx);
			ttc.appendChild(tti);
			return(nextIdx);
		} else return(-1);
	}
	
	@Override
	public void setItemStyle(int p_idx,String p_style)
	{
		Treeitem ti = (Treeitem) tc.getFellow("ti_"+p_idx);
		if(ti != null) {
			if(p_style.equals("enabled")) ti.setDisabled(false);
			if(p_style.equals("disabled")) ti.setDisabled(true);
			if(p_style.equals("checked")) ti.setSelected(true);
			if(p_style.equals("unchecked")) ti.setSelected(false);
		}
	}
	
	@Override
	public int getCurNodeIdx()
	{
		return(currIdx);
	}	
	
	@Override
	protected String processAction(Event ev) {
		switch(JxZkGadgetProvider.getEventID(ev.getName())) {
		case JxZkGadgetProvider.EV_ONCLICK :
			Component c = ev.getTarget();
			if(c instanceof Treeitem) {
				if(((Treeitem) c).isDisabled()) return(null);
				currIdx = ((Integer) ((Treeitem) c).getValue()).intValue();
				if(actionListener != null) actionListener.actionPerformed(getJxField());
			}
			break;
		default :
			return(super.processAction(ev));
		}
		return(null);
	}

	@Override
	public Vector getSelectedIndexes()
	{
		Vector v = new Vector();
		Set<Treeitem> sl = tr.getSelectedItems();
		for(Treeitem ti:sl) {
			v.add(ti.getValue());
		}
		return(v);
	}
	
	private void doCollapseExpandAll(Component component, boolean aufklappen) {
		if (component instanceof Treeitem) {
			Treeitem treeitem = (Treeitem) component;
			treeitem.setOpen(aufklappen);
		}
		Collection<?> com = component.getChildren();
		if (com != null) {
			for (Iterator<?> iterator = com.iterator(); iterator.hasNext();) {
				doCollapseExpandAll((Component) iterator.next(), aufklappen);

			}
		}
	}
	@Override
	public void setStyle(String p_style) {
		if(p_style.equals("expandall")) {
			Collection<?> com = tr.getItems();
			if (com != null) {
				for (Iterator<?> iterator = com.iterator(); iterator.hasNext();) {
					doCollapseExpandAll((Component) iterator.next(), true);
	
				}
			}
		}
		if(p_style.equals("collapseall")) {
			Collection<?> com = tr.getItems();
			if (com != null) {
				for (Iterator<?> iterator = com.iterator(); iterator.hasNext();) {
					doCollapseExpandAll((Component) iterator.next(), false);
				}
			}
		}
	}
	
	@Override
	public Object getValue() {
		Treeitem ti = tr.getSelectedItem();
		if(ti != null) return(ti.getValue());
		return(null);
	}
	
}
