package com.uniinformation.jx.zk;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Layout;

import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.cell.LabelCell;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.jx.JxChangeListener;
import com.uniinformation.utils.UniLog;

public class JxZkCheckListBox extends JxZkElement {
	int vCols=0;
	int itemCount=0;
//	int vRows=0;
	ZkJxCheckListBox cb;
	Hashtable <Integer,Object> itemHash;
	Hashtable <Component,Cell> cellHash;
	EventListener itemClickListener;
	EventListener subOptionListener;
	JxChangeListener changeListener = null;	
	public JxZkCheckListBox(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		cb = (ZkJxCheckListBox) c;
//		comp.addEventListener("onCheck", zkEventListener);
		//UniLog.log("HAHA 2016 JxZKCheckbox created " + getName());
		Columns cl = cb.getColumns();
		if(cl == null) {
			cl = new Columns();
			cb.appendChild(cl);
			vCols = 0;
		} else vCols = cl.getChildren().size();
		Rows r = cb.getRows();
		if(r == null) {
			r = new Rows();
			cb.appendChild(r);
		}
		if(vCols > 0) itemCount = vCols * r.getChildren().size();
		itemHash = new Hashtable <Integer,Object>();
		cellHash = new Hashtable <Component,Cell>();
		itemClickListener = new EventListener() {
			public void onEvent(Event event) throws Exception {
					UniLog.log("item clicked ");
					Checkbox checkbox = (Checkbox) event.getTarget();
					Layout ly = (Layout) checkbox.getParent();
					if(ly.getChildren().size() > 1) ly.getChildren().get(1).setVisible(checkbox.isChecked());
					processAction(event);
			}	
		};
		subOptionListener = new EventListener() {
			public void onEvent(Event event) throws Exception {
					UniLog.log("Suboption Changed");
					Component comp = event.getTarget();
					LabelCell lc = (LabelCell) comp.getAttribute("labelcell");
					if(lc != null) {
						if(comp instanceof Combobox) {
							lc.getCell().set(((Combobox) comp).getText());
						}
						if(comp instanceof Radiogroup) {
							Radio rd = ((Radiogroup) comp).getSelectedItem();
							if(rd != null ) {
								lc.getCell().set(rd.getLabel());
							}
//							lc.getCell().set(((Radiogroup) comp).getSelectedItem().getLabel());
						}
					}
			}	
		};
	}
	public void grid_setCol(int n)
	{	
		Columns cl = cb.getColumns();
		for(int i=vCols;i<n;i++) {
			Column c = new Column();
			cl.appendChild(c);
		}
		if(n > vCols) vCols = n;
	}
	public void grid_setRow(int p_row)
	{
		Rows rs = cb.getRows();
		int n = rs.getChildren().size();
		for(int i = n;i<p_row;i++) {
			Row r = new Row();
			rs.appendChild(r);
		}
	}
	void setAddOneCheckbox(int p_col,int p_row,String p_label)
	{
		Rows rs = cb.getRows();
		Row r;
		Checkbox chkb;
		int n;
		n = rs.getChildren().size();
		for(int i=n;i<=p_row;i++) {
			r = new Row();
			rs.appendChild(r);
		}
		r = (Row) rs.getChildren().get(p_row);
		r.setVisible(true);
		n = r.getChildren().size();
		for(int i=n;i<=p_col;i++) {
			Layout vl;
			if(vCols == 1)  {
				vl = new Hlayout();
				((Hlayout) vl).setValign("middle");
			} else
				vl = new Vlayout();
			chkb = new Checkbox();
			chkb.addEventListener("onClick", itemClickListener);
			UniLog.log("HAHA in CheckListBox grid_setDataFormat add one checkbox " + i);
			vl.appendChild(chkb);
			r.appendChild(vl);
		}
		Component c = r.getChildren().get(p_col);
//		if(c instanceof Checkbox) {
//			chkb = (Checkbox) c;
//			chkb.setVisible(true);
//			chkb.setLabel(p_label.trim());
//		} else {
			Layout vl = (Layout) c;
			vl.setVisible(true);
			chkb = (Checkbox) vl.getChildren().get(0);
			chkb.setChecked(false);
			chkb.setVisible(true);
			chkb.setLabel(p_label.trim());
			if(p_label.startsWith(".")) chkb.setVisible(false);
			if(vl.getChildren().size() > 1) vl.removeChild( vl.getChildren().get(1));
//		}
	}
	/*
	public void grid_setDataFormat(int p_col,int p_row,String p_format)
	{
		Rows rs = cb.getRows();
		Row r;
		Checkbox chkb;
		int n;
		n = rs.getChildren().size();
		for(int i=n;i<=p_row;i++) {
			r = new Row();
			rs.appendChild(r);
		}
		r = (Row) rs.getChildren().get(p_row);
		
		n = r.getChildren().size();
		for(int i=n;i<=p_col;i++) {
			chkb = new Checkbox();
			chkb.addEventListener("onClick", itemClickListener);
			UniLog.log("HAHA in CheckListBox grid_setDataFormat add one checkbox " + i);
			r.appendChild(chkb);
		}
		chkb = (Checkbox) r.getChildren().get(p_col);
		chkb.setVisible(true);
		chkb.setLabel(p_format.trim());
	}
	*/

	@Override
	public void setItemList(List itemlist)
	{
		setItemListGen(itemlist,null);
	}
	@Override
	public void setItemListInterface(AbstractGetItemProperty p_interface)
	{
		setItemListGen(null,p_interface);
	}
	private void setItemListGen(List itemlist,AbstractGetItemProperty p_interface)
	{
		if(vCols <= 0) grid_setCol(1); 
		itemHash.clear();
		cellHash.clear();
		if(itemlist == null && p_interface == null) {
			itemCount = 0;
		} else {
			if(p_interface != null) itemCount = p_interface.getRowCount(); else itemCount = itemlist.size();
//			for(int i=0;i<itemlist.size();i++) {
			for(int i=0;i<itemCount;i++) {
				String s=null;
				if(p_interface !=  null) {
//					s = p_interface.getString(itemlist.get(i));
					s = p_interface.getString(p_interface.getRow(i));
				} else {
					s = itemlist.get(i).toString();
				}
				/*
				if(labelList != null) {
					s = (String) labelList.get(i);
					UniLog.log("HAHA 2017 checkListBOx setItemList "+ itemlist.get(i) + " hash " + s);
				}
				if(s == null) s = (String) itemlist.get(i);
				*/
				setAddOneCheckbox(i % vCols,i / vCols,s);
				if(p_interface != null) {
					itemHash.put(new Integer(i),p_interface.getRow(i));
				} else {
					itemHash.put(new Integer(i),itemlist.get(i));
				}
			}
		}
		Rows rs = cb.getRows();
		for(int i=itemCount/vCols;i<rs.getChildren().size();i++) {
			Row r = (Row) rs.getChildren().get(i);
			for(int j = 0;j<r.getChildren().size();j++) {
				if(i * vCols + j >= itemCount) {
					Component b = r.getChildren().get(j);
					b.setVisible(false);
				}
			}
		}
		for(int i = itemCount/vCols+1;i<rs.getChildren().size();i++) {
			Row r = (Row) rs.getChildren().get(i);
			r.setVisible(false);
		}
	}
	/*
	public void setItemList(Vector itemlist,Vector labelList)
	{
		if(vCols <= 0) grid_setCol(1); 
		itemHash.clear();
		cellHash.clear();
		if(itemlist == null) {
			itemCount = 0;
		} else {
			itemCount = itemlist.size();
			for(int i=0;i<itemlist.size();i++) {
				String s=null;
				if(labelList != null) {
					s = (String) labelList.get(i);
					UniLog.log("HAHA 2017 checkListBOx setItemList "+ itemlist.get(i) + " hash " + s);
				}
				if(s == null) s = (String) itemlist.get(i);
				setAddOneCheckbox(i % vCols,i / vCols,s);
				itemHash.put(new Integer(i),(String) itemlist.get(i));
			}
		}
		Rows rs = cb.getRows();
		for(int i=itemCount/vCols;i<rs.getChildren().size();i++) {
			Row r = (Row) rs.getChildren().get(i);
			for(int j = 0;j<r.getChildren().size();j++) {
				if(i * vCols + j >= itemCount) {
					Component b = r.getChildren().get(j);
					b.setVisible(false);
				}
			}
		}
		for(int i = itemCount/vCols+1;i<rs.getChildren().size();i++) {
			Row r = (Row) rs.getChildren().get(i);
			r.setVisible(false);
		}
	}
	*/
	public void setSelectList(Vector itemlist) {
		for(int i = 0;i < itemCount;i++) {
			Checkbox b;
			Layout vl = null;
			Component c = cb.getRows().getChildren().get(i/vCols).getChildren().get(i % vCols);
			if(c instanceof Checkbox) b = (Checkbox) c; else {
				vl = (Layout) c;
				b = (Checkbox) vl.getChildren().get(0);
			}
			if(itemlist != null && itemlist.indexOf(itemHash.get(i)) >= 0) {
				UniLog.log("set to true");
				b.setChecked(true);
				if(vl.getChildren().size() > 1) vl.getChildren().get(1).setVisible(true);
				/*
				if(c instanceof Vlayout) {
					((Vlayout) c).getChildren().get(1).setVisible(true);
				}
				*/
			} else {
				b.setChecked(false);
				if(vl.getChildren().size() > 1) vl.getChildren().get(1).setVisible(false);
				/*
				if(c instanceof Vlayout) {
					((Vlayout) c).getChildren().get(1).setVisible(false);
				}
				*/
			}
				/*
			if(itemlist != null) {
				for(int j = 0;j<itemlist.size();j++) {
//					UniLog.log("comparing " + i + ":" + j + " ("+b.getLabel()+")("+itemlist.get(j));
//					if(((String) itemlist.get(j)).equals(b.getLabel())) {
					String s = (String) itemHash.get(new Integer(i));
					if(((String) itemlist.get(j)).equals(s)) {
						UniLog.log("set to true");
						b.setChecked(true);
						break;
					}
				}
			}
				*/
		}
	}
	public Vector getSelectList() {
		Vector v = new Vector();
		for(int i = 0;i < itemCount;i++) {
			Checkbox b;
			Component c = cb.getRows().getChildren().get(i/vCols).getChildren().get(i % vCols);
			if(c instanceof Checkbox) b = (Checkbox) c; else {
				Layout vl = (Layout) c;
				b = (Checkbox) vl.getChildren().get(0);
			}
			if(b.isChecked()) {
//				v.add(b.getLabel());
				v.add(itemHash.get(new Integer(i)));
			}
		}
		return(v);
	}
	public void addChangeListener(JxChangeListener x)
	{
		changeListener = x;
	}
	@Override
	public void setItemSubOptions(Map<String,Vector<LabelCell>> p_options)
	{
//		Object keys[] = p_options.keySet().toArray();
		cellHash.clear();
		for(Enumeration e = itemHash.keys();e.hasMoreElements();) {
			Integer cIdx = (Integer) e.nextElement();
			Object key = itemHash.get(cIdx);
			Row rs = (Row) cb.getRows().getChildren().get(cIdx.intValue()/vCols);
			Component c =  rs.getChildren().get(cIdx.intValue() % vCols);
			Layout vl = (Layout) c;
			if(vl.getChildren().size() > 1) vl.removeChild( vl.getChildren().get(1));
			Vector <LabelCell> cv = null;
			if(p_options != null) cv = p_options.get(key);
			if(cv != null) {
//				UniLog.log("HAHA 2017 setItemSubOptions " + cIdx.intValue() + " count " + cv.size());
				Hlayout hl;
				if(vl.getChildren().size() <= 1) {
//					UniLog.log("HAHA 2017 Adding Hlayout ");
					hl = new Hlayout();
					hl.setValign("middle");
					vl.appendChild(hl);
				} else {
					hl = (Hlayout) vl.getChildren().get(1);
				}
				Checkbox checkbox = (Checkbox) vl.getChildren().get(0);
				hl.setVisible(checkbox.isChecked());
				for(int i = 0;i<cv.size();i++) {
				if(c != null) {
					if(cv.get(i).getCell().getType() == Cell.VTYPE_BOOLEAN) {
						Label lb = new Label();
						lb.setWidth("10px");
						hl.appendChild(lb);
						Checkbox cb2 = new Checkbox();
						cb2.setLabel(cv.get(i).getLabel());
						hl.appendChild(cb2);
					} else {
						if(cv.get(i).getType() == LabelCell.CTYPE_RADIOGROUP) {
							Radiogroup rg = new Radiogroup();
							rg.setAttribute("labelcell", cv.get(i));
							rg.addEventListener("onClick", subOptionListener);
							Vector il = cv.get(i).getCell().getItemList();
							int curIdx = -1;
							if(il != null) {
								for(int k=0;k<il.size();k++) {
									rg.appendItem(il.get(k).toString(),null);
									if(il.get(k).toString().equals(cv.get(i).getCell().getString())) {
										curIdx = k;
									}
								}
							}
							if(curIdx >= 0) rg.setSelectedIndex(curIdx); else {
								if(cv.get(i).getCell().getString().trim().equals("") && il.size() > 0) {
									try {
										cv.get(i).getCell().set(il.get(0).toString());
										rg.setSelectedIndex(0);
									} catch (CellException cex) {
										UniLog.log(cex);
									}
								}
							}
							hl.appendChild(rg);
							continue;
						}
						if(cv.get(i).getType() == LabelCell.CTYPE_COMBOBOX) {
							Combobox cb = new Combobox();
							cb.setWidth("200px");
							cb.setAttribute("labelcell", cv.get(i));
							cb.addEventListener("onChange", subOptionListener);
							Vector il = cv.get(i).getCell().getItemList();
							if(il != null) {
								for(int k=0;k<il.size();k++) {
									cb.appendItem(il.get(k).toString());
								}
							}
//							cb.setText(cv.get(i).getCell().getString());
							String ss = cv.get(i).getCell().getString();
							if(ss.trim().equals("") && il.size() > 0) {
								try {
									cv.get(i).getCell().set(il.get(0).toString());
									cb.setText(il.get(0).toString());
								} catch (CellException cex) {
									UniLog.log(cex);
								}
							} else {
								cb.setText(ss);
							}
							hl.appendChild(cb);
							continue;
						}
						Label lb = new Label();
						lb.setPre(true);
						lb.setValue("  "+cv.get(i).getLabel());
//						lb.setLeft("10px");
						hl.appendChild(lb);
						Textbox tb = new Textbox();
						tb.setWidth("40px");
						hl.appendChild(tb);
					}
				}
				}
			}
		}
		/*
		for(int i=0;i<keys.length;i++) {
			Vector<LabelCell> cv = p_options.get(keys[i]);
			int idx = itemHash.
			UniLog.log("HAHA 2017 setItemSubOptions " + keys[i] + " count " + cv.size());
			
		}
		*/
	}
	
	@Override
	public Object getValue() {
		return(null);
	}
}
