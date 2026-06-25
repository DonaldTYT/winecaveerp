package com.uniinformation.jx;

import java.util.*;
import java.text.*;

import org.apache.commons.lang3.StringUtils;
//import org.zkforge.timeline.Bandinfo; 250821 removed
//import org.zkoss.zul.impl.InputElement;

import com.uniinformation.utils.*;
import com.kyoko.common.*;
import com.uniinformation.webcore.SessionHelper;
//import com.uniinformation.zkcomp.S2Listbox;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.*;
//import com.uniinformation.jx.zk.JxZkSelector;
public class JxField implements CellValueMapper, JxFieldFlowInterface, JxFieldImageListInterface
{  

	public static final int ACTIONTYPE_CLICK = 0;
	public static final int ACTIONTYPE_DOUBLECLICK = 1;
	public static final int ACTIONTYPE_DELETE = 2;
	public static final int ACTIONTYPE_UPDATE = 3;
	public static final int ACTIONTYPE_INSERT = 4;
	public static final int ACTIONTYPE_SELECT = 5;
	public static final int ACTIONTYPE_EDITCELLREALIZED = 6;
	public static final int ACTIONTYPE_EDITCELLPICKED = 7;
	public static final int ACTIONTYPE_EDITCELLCHANGED = 8;
	public static final int ACTIONTYPE_PICKINPUTOPENED = 9;
	public static final int ACTIONTYPE_PICKINPUTCLOSED = 10;
	//public static final int ACTIONTYPE_ROWBTNCLICK = 11;

	public static final int FTYPE_INT		= 0;
	public static final int FTYPE_STRING	= 1;
	public static final int FTYPE_FLOAT		= 2;
	public static final int FTYPE_DATE		= 3;
	public static final int FTYPE_DATETIME  = 8;
	public static final int FTYPE_BOOLEAN	= 4;
	public static final int FMODE_NORMAL	= 1;
	public static final int FMODE_DISPONLY = 2;
//	public static final int FMODE_PROTECTED= 3;
//	public static final int FMODE_OVERRIDED= 4;
	public static final int FMODE_HIDDEN = 5;

	protected int	jxfieldmode = 0; 
	protected String jxfieldname;
	protected Object jxobject;
	protected JxForm jxform;
	protected JxSkinElement jxskinelement;
	protected boolean isEnabled = true;
	protected boolean isVisible = true;

	protected JxActionListener actionlistener;
	protected JxDblClickListener dblclicklistener;
	protected JxDragListener draglistener;
	protected JxDropTargetListener droptargetlistener;
	protected JxSelectListener selectlistener;
	protected JxChangeListener changelistener;
	protected JxGridChangeListener gridchangelistener;
	protected JxColumnClickListener columnclicklistener;
	protected JxGetDataIdxListener getdatalistener;

	protected Hashtable flowListeners;

	protected Vector additemlist = new Vector();
	protected String textvalue;
	private int color = 0x40000000;
	private int fontcolor = 0x40000000;
	private int fontsize = 0;
	private int width = 0;
	private int height = 0;
	private int jxFieldLen;
	/*
	private String jxFieldFmt;
	*/
	private int jxFieldType = -1;
	private int jxFieldLength = 0;
	private int jxFieldSublength = 0;
	private String jxFieldMask;
	private String jxFieldDefault;
	private DecimalFormat jxFieldFormat;
	private FieldPosition jxFieldPosition;
	/*
	private Vector jxFieldOption;
	*/
//	private boolean formatEnabled = true;
	private String HintContent = null;
	private Cell jxValue = null;
	private boolean noBackwordLookup = false;
	public boolean isbinded()	{
		return(jxskinelement != null ? true : false);
	}
	public String name()
	{
		return(jxfieldname);
	}
	public String getName()
	{
		return(jxfieldname);
	}
	public JxField(String p_fieldname)
	{
  		jxfieldname= p_fieldname;
	}
	public void setEnable(boolean p_isEnabled)
  	{
  		if(isEnabled == p_isEnabled)
  			return;
  		isEnabled = p_isEnabled;
		if(jxskinelement != null) 
			jxskinelement.setEnable(isEnabled);
	}
	public boolean getEnable()
	{
  		return(isEnabled);
	}

	public void setVisible(boolean p_isVisible)
	{
		if(isVisible == p_isVisible)
			return;
  		isVisible = p_isVisible;
		if(jxskinelement != null) {
			jxskinelement.setVisible(isVisible);
	   }
	}
	public boolean getVisible()
	{
  		return(isVisible);
	}

	public JxField addActionListener(JxActionListener listener)
	{
		if(jxskinelement == null) {
			actionlistener = listener;
		} else {
			jxskinelement.addActionListener(listener);
		}
		return(this);
	}
	public void addDblClickListener(JxDblClickListener listener)
	{
		if(jxskinelement == null) {
			dblclicklistener = listener;
		} else {
			jxskinelement.addDblClickListener(listener);
		}
	}
	public void addDragListener(JxDragListener listener)
	{
		if(jxskinelement == null) {
			draglistener = listener;
		} else {
			jxskinelement.addDragListener(listener);
		}
	}
	public void addDropTargetListener(JxDropTargetListener listener)
	{
		if(jxskinelement == null) {
			droptargetlistener = listener;
		} else {
			jxskinelement.addDropTargetListener(listener);
		}
	}
	public void addSelectListener(JxSelectListener listener) {
		if(jxskinelement == null) {
			selectlistener = listener;
		} else {
			jxskinelement.addSelectListener(listener);
		}  
	}
	
	/*
	 Wrong Code, Assumed never used, therefore remarked
	public Vector getItemList()
	{
		if(additemlist == null) return(null);
		return((Vector)additemlist.clone());
	}
	*/

	public void bind(JxSkinElement se)
	{
		bind(null,se);
	}

	public void bind(JxForm p_jxform,JxSkinElement se)
	{
		jxobject = se.getControl();
		jxskinelement = se;
		jxform = p_jxform;
		se.setJxField(this);
		if(actionlistener != null) {
			this.addActionListener(actionlistener);
			actionlistener = null;
		}
		if(selectlistener != null) {
			this.addSelectListener(selectlistener);
			selectlistener = null;
	  	}
		if(changelistener != null) {
			this.addChangeListener(changelistener);
			changelistener = null;
	  	}
		if(gridchangelistener != null) {
			this.addGridChangeListener(gridchangelistener);
			gridchangelistener = null;
	  	}
		if(columnclicklistener != null) {
			this.addColumnClickListener(columnclicklistener);
			columnclicklistener = null;
	  	}
		if(getdatalistener != null) {
			this.addGetDataIdxListener(getdatalistener);
			getdatalistener = null;
	  	}
		if(dblclicklistener != null) {
			this.addActionListener(dblclicklistener);
			dblclicklistener = null;
		}
	  	for(int i=0;i<additemlist.size();i++) {
//			this.addItem((String)additemlist.elementAt(i));
			this.addItemToList(additemlist.elementAt(i),i);
		}
		// additemlist.removeAllElements();
		if(jxFieldType >= 0) {
			setText(null);
		} else {
			if(textvalue != null)
				jxskinelement.setText(textvalue);
		}
		if(!jxskinelement.useSkinElementDefault) {
			if(p_jxform != null) {
				isEnabled = p_jxform.defaultEnabled;
				isVisible = p_jxform.defaultVisible;
			}
			UniLog.log("bind: " + jxfieldname );
			jxskinelement.setEnable(isEnabled);
			jxskinelement.setVisible(isVisible);
		}
	}
//	public int addItem(Object p_item)
//	{
//		if(jxskinelement == null) {
//			additemlist.addElement(p_item);
//			return(0);
//		} else {
//			return(jxskinelement.addItem(p_item));
//		}
//	}
	public void setText(String p_text)
	{
		String s;
		if(p_text == null) {
			switch(jxFieldType) {
			case FTYPE_INT       : 		if(jxFieldDefault != null) {
													s = jxFieldDefault;
												} else {
													s = "0";
												}
												break;
			case FTYPE_STRING    :		if(jxFieldDefault != null) {
													s = jxFieldDefault;
												} else {
													s = "";
												}
												break;
			case FTYPE_FLOAT     : 		if(jxFieldDefault != null) {
													s = jxFieldDefault;
												} else {
													s = "0.00";
												}
												break;
			case FTYPE_DATETIME     :
			case FTYPE_DATE  		:		
												if(jxFieldDefault != null &&
													jxFieldDefault.equals("today")) {
													s = DateUtil.toDateString(
															DateUtil.today(),
															"yyyy/mm/dd"
															);
												} else {
													s = "2000/00/00";
												}
												break;
			case FTYPE_BOOLEAN	:		if(jxFieldDefault != null) {
													s = jxFieldDefault;
												} else {
													s = "N";
												}
												break;
			default : s = "";
			} 
		} else {
			s = p_text;
		}
		if(jxskinelement == null) {
			textvalue = new String(s);
		} else {
			jxskinelement.setText(s);
		}
	}
	public String getText()
	{
		if(jxskinelement == null) {
			/* Sat Nov  1 13:55:10 HKG 2003 changed by DT */
			// return(null);
			return(textvalue);
		}
		String s = jxskinelement.getText();
		if(s != null) return(s) ; else return("");
//		return(jxskinelement.getText());
	}
	public void setCaption(String p_caption)
	{
		if(jxskinelement != null) {
			jxskinelement.setCaption(p_caption);
		}
	}
	protected Object getControl()
	{
		return(jxobject);
	}
	
	public void clear()
	{
		textvalue = null;		
		if(jxskinelement != null) {
				jxskinelement.clear();
		}
	}
	
	public boolean setImageList(JxImageList p_list)
	{
		if(jxskinelement != null)
			return(jxskinelement.setImageList(p_list.jxskinelement));
		return(false);
	}
	
	public void setImageIndex(int p_nodeid,int p_index)
	{
		if(jxskinelement != null)
			jxskinelement.setImageIndex(p_nodeid,p_index);
	}

	public void setImageIndex(int p_index)
	{
		if(jxskinelement != null)
			jxskinelement.setImageIndex(p_index);
	}
	
	public boolean addImage(String p_imagefile)
	{
		if(jxskinelement != null) {
			return(jxskinelement.addImage(p_imagefile));
		}
		return false;
	}
	public boolean removeImage()
	{
		return(removeImage(0));
	}
	public boolean removeImage(int p_index)
	{
		if(jxskinelement != null) {
			return(jxskinelement.removeImage(p_index));
		}
		return false;
	}
	public void addChangeListener(JxChangeListener listener) {
		if(jxskinelement == null) {
			changelistener = listener;
		} else {
			jxskinelement.addChangeListener(listener);
		}
	}
	public void addGridChangeListener(JxGridChangeListener listener) {
		if(jxskinelement == null) {
			gridchangelistener = listener;
		} else {
			jxskinelement.addGridChangeListener(listener);
		}
	}
	public void addColumnClickListener(JxColumnClickListener listener) {
		if(jxskinelement == null) {
			columnclicklistener = listener;
		} else {
			jxskinelement.addColumnClickListener(listener);
		}
	}
	public void addGetDataIdxListener(JxGetDataIdxListener listener) {
		if(jxskinelement == null) {
			getdatalistener = listener;
		} else {
			jxskinelement.addGetDataIdxListener(listener);
		}
	}

	public void setLength(int p_length)
	{
		if(jxskinelement == null) return;
		jxskinelement.setLength(p_length);
	}
	/*
	public void setJxFieldLen(int p_length)
	{
		jxFieldLen = p_length;
	}*/
	public void setFormat(int p_type,int p_length,  String p_mask)
	{
		StringBuffer sb;
		FieldPosition fp;
		jxFieldType   		= p_type;
		jxFieldLength 		= p_length;
		jxFieldMask			= p_mask;
		switch(jxFieldType) {
		case FTYPE_STRING:
/*
			if(p_mask != null) {
				String s;
				JxField fd;
				jxFieldOption = new Vector();
				for( StringTokenizer token = new StringTokenizer(p_mask);
						token.hasMoreTokens();) {
					s = token.nextToken();
					jxFieldOption.add(s);
				}
				setItemList(jxFieldOption);
			}
*/
			break;
		case FTYPE_INT:
			if(p_mask != null) {
				if(p_mask.equals("#")) jxFieldFormat = null;
					else jxFieldFormat = new DecimalFormat(p_mask);
			} else {
				jxFieldFormat 	 = new DecimalFormat("#########0");
			}
			jxFieldPosition = new FieldPosition(NumberFormat.INTEGER_FIELD);
			break;
		case FTYPE_FLOAT :
			if(p_mask != null) {
				if(p_mask.equals("#")) jxFieldFormat = null;
					else jxFieldFormat 	 = new DecimalFormat(p_mask);
			} else {
				jxFieldFormat 	 = new DecimalFormat("##########0.00");
			}
			jxFieldPosition = new FieldPosition(NumberFormat.INTEGER_FIELD);
			break;
		case FTYPE_BOOLEAN:
			break;
		}
		if(jxskinelement != null) {
			jxskinelement.setFormat(p_mask);
		}
		
	}
	public void setDefault(String p_default)
	{
		jxFieldDefault = p_default;
	}
	/*
	public int getJxFieldLen()
	{
		return(jxFieldLen);
	}
	public String getJxFieldFmt()
	{
		return(jxFieldFmt);
	}
	*/
	public String getSkinName()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getName());
		}
		return null;
	}
	public void setItemList(List p_itemlist)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemList(p_itemlist);
		}
	}
	public void setItemListInterface(AbstractGetItemProperty p_interface)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemListInterface(p_interface);
		}
	}
	/*
	public void setItemList(Vector p_itemlist,Vector itemLabel)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemList(p_itemlist,itemLabel);
		}
	}
	*/
	public void addItemToList(Object p_item,int p_idx)
	{
		if(jxskinelement != null) {
			jxskinelement.addItemToList(p_item,p_idx);
			
		}
	}
	public void moveItemByIdx(int fromidx, int toidx)
	{
		if(jxskinelement != null) {
			jxskinelement.moveItemByIdx(fromidx,toidx);
		}
	}
	public int getItemIndex()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getItemIndex());
		} else return(-1);
	}

	public void setItemIndex(int p_idx)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemIndex(p_idx);
		} 
	}

	public int addNode(String p_text)
	{
UniLog.log("addNode:haha:0");
		if(jxskinelement != null) {
UniLog.log("addNode:haha:1");
			return(jxskinelement.addNode(p_text));
		} 
		else {
UniLog.log("addNode:haha:2");
		   return(-1);
	   }
	}

	public int addNode(String p_text,int p_imgidx)
	{
		if(jxskinelement != null) {
			return(jxskinelement.addNode(p_text,p_imgidx));
		} else return(-1);
	}

	public int addChild(int parent,String p_text)
	{
		if(jxskinelement != null) {
			return(jxskinelement.addChild(parent,p_text));
		} else return(-1);
	}

	public int addChild(int parent,String p_text,int p_imgidx)
	{
		if(jxskinelement != null) {
			return(jxskinelement.addChild(parent,p_text,p_imgidx));
		} else return(-1);
	}
	public void deleteNode(int nodeid)
	{
		if(jxskinelement != null) {
			jxskinelement.deleteNode(nodeid);
		}
	}
	public void changeNode(int nodeid,String desc)
	{
		if(jxskinelement != null) {
			jxskinelement.changeNode(nodeid,desc);
		}
	}

	public int getCurNodeIdx()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getCurNodeIdx());
		} else return(-1);
	}

	public void setCurNodeIdx(int p_idx)
	{
		if(jxskinelement != null) {
			jxskinelement.setCurNodeIdx(p_idx);
		}
	}


	public void gridSetCol(int p_col)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setCol(p_col);
		}
	}
	public void gridSetColHeader(int p_col,Object p_header)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setcolheader(p_col,p_header);
		}
	}
	public void gridSetColHeader(int p_col,Object p_header,
		String alignment, boolean autosize, int imgidx,int minwidth,int maxwidth,int width)
	{
		/*
			alignment = "L","R","C"
			for TlistView 
				width = -2 HeaderWidth 
				width = -1 TextWidth 
		*/
		if(jxskinelement != null) {
			jxskinelement.grid_setcolheader(p_col,p_header,
				alignment, autosize, imgidx,minwidth,maxwidth,width);
		}
	}
	public void gridSetRow(int p_row)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setRow(p_row);
		}
	}
	public void gridSetRowHeader(int p_row,String p_header)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setrowheader(p_row,p_header);
		}
	}
	public boolean gridSetValue(int p_col,int p_row,Object s)
	{
		if(jxskinelement != null) {
			return(jxskinelement.grid_setValue(p_col,p_row,s));
		}
		return(false);
	}
	public boolean gridSetValue(Vector p_v)
	{
		if(jxskinelement != null) {
			return(jxskinelement.grid_setValue(p_v));
		}
		return(false);
	}
	public Object gridGetValue(int p_col,int p_row)
	{
		if(jxskinelement != null) {
			return(jxskinelement.grid_getValue(p_col,p_row));
		}
		return(null);
	}
	public boolean deleterow(int p_idx)
	{
		if(jxskinelement != null) {
			return(jxskinelement.grid_deleterow(p_idx));
		}
		return(false);
	}
	public boolean appendrow()
	{
		if(jxskinelement != null) {
			return(jxskinelement.grid_appendrow());
		}
		return(false);
	}
	public int getCurrentRow()
	{
		if(jxskinelement != null) {
			return(jxskinelement.grid_getcurrentrow());
		}
		return(-1);
	}
	public int getCurrentCol()
	{
		if(jxskinelement != null) {
			return(jxskinelement.grid_getcurrentcol());
		}
		return(-1);
	}
	public void cleararray()
	{
		if(jxskinelement != null) {
			jxskinelement.grid_cleararray();
		}
	}
	public boolean isfocused()
	{
		if(jxskinelement != null) {
			return(jxskinelement.isfocused());
		} return(false);
	}
	public void setColor(int p_color)
	{
		if(jxskinelement != null) {
			if(p_color == color) return;
			color = p_color;
			jxskinelement.setColor(p_color);

		} 
	}
	public void setFontColor(int p_color)
	{
		if(jxskinelement != null) {
			if(p_color == fontcolor) return;
			fontcolor = p_color;
			jxskinelement.setFontColor(p_color);
		} 
	}
//	public void enableFormat()
//	{
//		formatEnabled = true;
//		UniLog.log("before enableFormat " + jxfieldname + " " + formatEnabled + " " + jxskinelement);
//		if(jxskinelement != null) {
//			jxskinelement.setFormat(true);
//		} 
//		UniLog.log("after enableFormat " + jxfieldname + " " + formatEnabled);
//	}
//	public void disableFormat()
//	{
//		formatEnabled = false;
//		if(jxskinelement != null) {
//			jxskinelement.setFormat(false);
//		} 
//		UniLog.log("after disableFormat " + jxfieldname + " " + formatEnabled);
//	}
	public void setInt(int p_value)
	{
		if(jxFieldType != FTYPE_INT) {
			setFormat(FTYPE_INT,0,"#########0");
		}
		if(jxFieldFormat == null) {
			if(p_value == 0) setText("");else setText(""+p_value);
		} else {
			StringBuffer sb = new StringBuffer();
			sb = jxFieldFormat.format(p_value,sb,jxFieldPosition);
			setText(sb.toString());
		}
	}

	public void setFloat(double p_value)
	{

		if(jxFieldType != FTYPE_FLOAT) {
			DecimalFormat df;
			if(jxValue != null && (df = jxValue.getDecFormat()) != null)  {
				jxFieldType = FTYPE_FLOAT;
				jxFieldFormat = df;
				jxFieldPosition = new FieldPosition(NumberFormat.INTEGER_FIELD);
			} else {
				UniLog.log("JxField SetFloat Format to Default " + getName());
				setFormat(FTYPE_FLOAT,0,"##########0.00");
			}
		}
		if(Double.isNaN(p_value)) {
			setText("NaN");
		} else {
			if(jxFieldFormat != null) {
				StringBuffer sb = new StringBuffer();
				sb = jxFieldFormat.format(p_value,sb,jxFieldPosition);
				setText(sb.toString());
			} else {
				setText(""+p_value);
			}
		}
	}
	public double getFloat()
	{
		String s;
		if(jxskinelement != null) s = jxskinelement.getText();
		else s = textvalue;
		if(s != null && !s.trim().equals("")) {
			return(Double.parseDouble(StringUtil.toNumberOnly(s)));
		} else {
			return(0.0);
		}
	}
	public int getInt()
	{
		String s;
		if(jxskinelement != null) s = jxskinelement.getText();
		else s = textvalue;
		if(s != null && !s.trim().equals("")) {
			return(Integer.parseInt(StringUtil.toNumberOnly(s)));
		} else {
			return(0);
		}
	}
	public boolean getBoolean()
	{
		String s;
		if(jxskinelement != null) s = jxskinelement.getText();
		else s = textvalue;
		if(s != null) {
			if(s.equals("Y")) return(true); else return(false);
		} else {
			return(false);
		}
	}
	public boolean validateChange(String p_orgvalue,String p_newvalue) 
	{
//		UniLog.logm(this,"jxValue:%s jxFieldType:%d orgValue:%s newValue:%s", jxValue, jxFieldType, p_orgvalue, p_newvalue);
//		if(!formatEnabled) return(true);
		Object fieldValue = null;
		if(jxValue != null && (p_newvalue != null && !p_newvalue.trim().equals(""))) {
			if(jxValue.getItemPropertyInterface() != null || jxValue.getItemList() != null) { 
				//obtain the real value(tbc)
				int i = jxskinelement.getItemIndex();
				if(i >= 0 && jxValue.getItemPropertyInterface() != null) {
					fieldValue = getValue();
					if(jxValue.getItemPropertyInterface().getStatus(jxskinelement.getValue(), AbstractGetItemProperty.GIPI_DELETED)) {
						i = -1;
					}
				}
				if(i < 0) {
					if(!(jxValue instanceof ColumnCell) ||
							(jxValue.getItemPropertyInterface() != null || !((ColumnCell) jxValue).getBiResult().getSessionHelper().useS2ListboxForReadOnly())) {
					setColor(0xff0000);
					jxform.messageBox(BiUtil.getSessionHelperLabel(jxValue, "Invalid Input Value") + ": " + p_newvalue);
					setColor(0x10000001);
					jxform.setFocus(jxfieldname);
					setText(p_orgvalue); // the org value may be incorrect if displayed text not equals item value 
					return(false);
					}
				}
			}
			// should also convert the orgvalue and newvalue to the contain of getValue from skinElement, otherwise may have wrong value if translation is used
		}
		boolean ret = true;
		switch(jxFieldType) {
		case FTYPE_BOOLEAN:
								try {
									if(jxValue != null) {
										if(p_newvalue.equals("Y")) {
											jxValue.sync(true);
										} else {
											jxValue.sync(false);
										}
										if(jxform != null && jxform.cellCheck != null) {
											jxform.cellCheck.cellUpdate(new java.util.Date(),jxValue);
										}
									}
								} catch (Exception nfe) {
									if(! (nfe instanceof NumberFormatException) &&
										! (nfe instanceof CellException)) {
										UniLog.log(nfe);
									}
									setColor(0xff0000);
									jxform.messageBox(BiUtil.getSessionHelperLabel(jxValue, "Invalid Input Value") + ": " +
													nfe.getMessage());
									setColor(0x10000001);
									jxform.setFocus(jxfieldname);
									setText(p_orgvalue);
									return(false);
								}
								return(true);
		case FTYPE_DATETIME: 
		case FTYPE_DATE: 
		case FTYPE_STRING:
								try {
									if(jxValue != null) {
										if(jxValue.getType() == Cell.VTYPE_INT) {
											ret = jxValue.update(new Integer(getItemIndex())).getStatus();
										} else {
											if(fieldValue != null) {
												ret = jxValue.update(fieldValue).getStatus();
											} else {
												ret = jxValue.update(p_newvalue).getStatus();
											}
										}
									}
									if(ret) {
										if(jxform != null && jxform.cellCheck != null) {
											jxform.cellCheck.cellUpdate(new java.util.Date(),jxValue);
										}
									} else {
										/*
										switch(jxFieldType) {
										case FTYPE_DATETIME: 
										case FTYPE_DATE: 
											setColor(0xff0000);
										}
										*/
											jxform.messageBox(BiUtil.getSessionHelperLabel(jxValue, "Invalid Input Value") + ": " + p_newvalue);
											setColor(0x10000001);
											jxform.setFocus(jxfieldname);
											setText(p_orgvalue);
											return(false);
									}
								} catch (Exception nfe) {
									if(! (nfe instanceof NumberFormatException) &&
										! (nfe instanceof CellException)) {
										UniLog.log(nfe);
									}
									setColor(0xff0000);
									jxform.messageBox(BiUtil.getSessionHelperLabel(jxValue, "Invalid Input Value") + ": " +
													nfe.getMessage());
									setColor(0x10000001);
									jxform.setFocus(jxfieldname);
									setText(p_orgvalue);
									return(false);
								}
								break;
		case FTYPE_INT   : 
								Integer i;
								try {
									if(p_newvalue.equals("")) i = new Integer(0);
									else i = new Integer(p_newvalue);
									if(jxFieldFormat == null) {
										if(i.intValue() == 0) setText("");else setText(i.toString());
									} else {
										StringBuffer sb = new StringBuffer();
										sb = jxFieldFormat.format(i.longValue(),sb,jxFieldPosition);
										setText(sb.toString());
										if(jxValue != null) {
											/*
											jxValue.sync(i.intValue());
											*/
											ret = jxValue.update(i).getStatus();
										}
									}
									if(ret) {
										if(jxform != null && jxform.cellCheck != null) {
											jxform.cellCheck.cellUpdate(new java.util.Date(),jxValue);
										}
									} else {
										jxform.setFocus(jxfieldname);
									}
								} catch (/* NumberFormatException */ Exception nfe) {
									if(! (nfe instanceof NumberFormatException) &&
										! (nfe instanceof CellException)) {
										UniLog.log(nfe);
									}
									setColor(0xff0000);
									jxform.messageBox(BiUtil.getSessionHelperLabel(jxValue, "Invalid Input Value") + ": " +
													nfe.getMessage());
									setColor(0x10000001);
									jxform.setFocus(jxfieldname);
									setText(p_orgvalue);
									return(false);
								} 
								break;
		case FTYPE_FLOAT : 
								Double f;
								try {
									if(p_newvalue.equals("")) f = new Double(0.0);
									else f = new Double(StringUtil.stripDecimal(p_newvalue));
									if(jxFieldFormat == null) {
										setText(f.toString());
									} else {
										StringBuffer sb = new StringBuffer();
										sb = jxFieldFormat.format(f.doubleValue(),sb,jxFieldPosition);
										setText(sb.toString());
									}
									if(jxValue != null) {
										/*
										jxValue.sync(f.doubleValue());
										*/
										ret = jxValue.update(f).getStatus();
									}
									if(ret) {
										if(jxform != null && jxform.cellCheck != null) {
											jxform.cellCheck.cellUpdate(new java.util.Date(),jxValue);
										}
									} else {
										jxform.setFocus(jxfieldname);
									}
								} 
								catch (Exception nfe) {
									UniLog.log(nfe);
									if(! (nfe instanceof NumberFormatException) &&
										! (nfe instanceof CellException)) {
										UniLog.log(nfe);
									}
									setColor(0xff0000);
									jxform.messageBox(BiUtil.getSessionHelperLabel(jxValue, "Invalid Input Value") + ": " +
													nfe.getMessage());
									setColor(0x10000001);
									jxform.setFocus(jxfieldname);
									jxskinelement.setText(p_orgvalue);
									return(false);
								} 
								break;
		default : textvalue = p_newvalue;
		}
		return(ret);
	}

	public void afterChange(String p_newvalue)
	{
		//UniLog.log("afterChange " + jxfieldname + " " + formatEnabled);
		/*
		if(formatEnabled && jxform != null && jxform.tmThread != null 
				&& noBackwordLookup == false) {
			Vector v = jxform.setFieldWithLookup(
											jxfieldname,p_newvalue);
			UniLog.log("setFieldWithLookup " + jxfieldname + "[" + p_newvalue + "] " + v);
			if(v != null) {
				for(int i = 0;i<v.size();i++) {
					String s = (String) v.get(i);
					jxform.jxSetText(s,jxform.getField(s));
				}
			}
		}
		*/
	}

	public void setFieldMode(int p_mode) throws CellException
	{
//		if(jxValue != null) jxValue.syncMode(jxfieldmode);
		if(p_mode == jxfieldmode) return;
		jxfieldmode = p_mode;
		if(jxValue != null) {
			boolean lastOverrided = jxValue.isOverrided();
			jxValue.setMode(jxfieldmode);
			realSetMode(jxValue.getMode(),jxValue.isOverrided(),jxValue.isProtected(),jxValue.isDirty());
		} else {
			realSetMode(p_mode,false,false,false);
		}
	}

	void realSetMode(int p_mode,boolean p_override,boolean p_protected,boolean p_isDirty)
	{
		//UniLog.log("SetMode " + jxfieldname + " " + p_mode + " " + jxfieldmode);
		if(jxskinelement == null) return;
		/*
		if(jxValue != null) {
			if(jxValue.getMode() == p_mode && jxValue.isOverrided() == p_override) return;
		} else {
			if(jxfieldmode == p_mode ) return;
		}
		jxfieldmode = p_mode;
		*/
		setVisible(true);
		switch(jxfieldmode) {
		case FMODE_NORMAL : 	setEnable(true);
									jxskinelement.setFontColor(0x1000000);
									/* jxskinelement.setReadOnly(false); */
									break;
		case FMODE_HIDDEN :	setVisible(false);
									break;
		case FMODE_DISPONLY:	setEnable(false);
//									jxskinelement.setFontColor(0x1000000);
									jxskinelement.setFontColor(0x00ff00);
									/* jxskinelement.setReadOnly(false); */
									break;
//		case FMODE_PROTECTED:
//									if(jxform.getProvider().checkJxCellOverridable(this)) {
//										setEnable(true);
//										jxskinelement.setFontColor(0x800000);
//									} else {
//										setEnable(false);
//										jxskinelement.setFontColor(0x00FF00);
//									}
//									/* jxskinelement.setReadOnly(true); */
//									break;
									/*
		case FMODE_OVERRIDED:
									if(jxform.getProvider().checkJxCellOverridable(this)) {
										setEnable(true);
										jxskinelement.setFontColor(0x0000FF);
									} else {
										setEnable(false);
										jxskinelement.setFontColor(0x00FF00);
									}
									break;
									*/
		}
		if(p_override) {
									if(jxform.getProvider().checkJxCellOverridable(this)) {
										if(p_mode != FMODE_DISPONLY) setEnable(true);
										jxskinelement.setFontColor(0x0000FF);
									} else {
										setEnable(false);
										jxskinelement.setFontColor(0x00FF00);
									}
		} else if(p_protected) {
			
		}
		if(p_isDirty) {
			jxskinelement.setLabelColor("red");
		} else {
			jxskinelement.setLabelColor("black");
		}
	}
	public int getFieldMode()
	{
		return(jxfieldmode);
	}

	public void editHint()
	{
			if(jxform == null) return;
			/*
			Edithint npdform = (Edithint) jxform.getForm("Edithint");
			if(npdform != null) {
				npdform.promptEditHint(this);
				if(jxform != null) {
					jxform.setFocus();
					jxform.setFocus(jxskinelement.getName());
				}
			}
			*/
			jxform.EditHint(this);
	}

	public void chgHint(String p_str)
	{
		if(jxskinelement == null) return;
		jxskinelement.setHint(true,p_str);
	}

	public void setHint(String p_str)
	{
		realSetHint(p_str);
		if(jxValue != null) {
			jxValue.syncHint(HintContent);
		}
	}

	void realSetHint(String p_str)
	{
		HintContent = p_str;
		if(jxskinelement == null) return;
		if(HintContent != null) {
			jxskinelement.setHint(true,HintContent);
			jxskinelement.setColor(0xffffc0);
		} else {
			jxskinelement.setHint(false,"");
			jxskinelement.setColor(0x1000000);
		}
	}

	public String getHint()
	{
		return(HintContent);
	}

	public void cellMap_bind(Cell p_value) 
	{
		//UniLog.log("ES2000 HAHA jxfield " + this + " map to " + p_value);
		if(p_value != null && jxValue != null && p_value != jxValue) {
			Cell c = jxValue;
			c.map(null);
		}
		/*
		if(jxValue == p_value) return;
		if(jxValue != null) {
			Cell c = jxValue;
			c.map(null);
		}
		*/
		jxValue = p_value;
		Vector v;
		if(jxValue != null) {
			if((v = p_value.getItemList()) != null) {
				setItemList(v);
				setText("");
			}
			cellMap_hintchange(jxValue);
			cellMap_modechange(jxValue);
			cellMap_formatchange(jxValue);
			cellMap_formulachange(jxValue);
			cellMap_listchange(jxValue);
			cellMap_valchange(jxValue);
		}
	}

	public void cellMap_valchange(Cell c) 
	{
		if(jxValue != null) {
			//UniLog.log1("native:%s value:%s", getNativeObject(),jxValue.getString());
			switch(jxValue.getType()) {
			case Cell.VTYPE_INT    :
													/* jxFIeldType will always be FTYPE_+INT */
													/* the setItemIndex method will never been called */
													/* 2022/07/31 by DT */
													if(jxFieldType == FTYPE_STRING) {
														setItemIndex(jxValue.getInt());
													} else {
														setInt(jxValue.getInt());
													}
													break;
			case Cell.VTYPE_DOUBLE :
													setFloat(jxValue.getDouble());
													break;
			case Cell.VTYPE_STRING :
													if(jxValue.getItemPropertyInterface() != null) {
														//setText(jxValue.getItemPropertyInterface().getString(jxValue.getString()));
														
														//andrew210510 for handle s2listbox, need actual value instead of label to maintain selector index
														//if (jxValue.getItemPropertyInterface() instanceof GipiNamedItemList && ZkUtil.isSelect2(jxskinelement.getZkComponent())) { ... }
														/*
														if ((jxValue.getItemPropertyInterface() instanceof GipiNamedItemList) && (jxskinelement instanceof JxZkSelector)) {
															setText(jxValue.getString());
														} else if (jxValue.getItemPropertyInterface() instanceof TranslateListGetItemProperty && jxskinelement instanceof JxZkSelector) { 
															setText(jxValue.getString());
														} else {
															//preserve old logic (e.g. handle combobox). 
															setText(jxValue.getItemPropertyInterface().getString(jxValue.getString()));  
														}
														*/
														if ((jxValue.getItemPropertyInterface() instanceof GipiNamedItemList) && (jxskinelement.getFieldClass() == 1)) {
															setText(jxValue.getString());
														} else if (jxValue.getItemPropertyInterface() instanceof TranslateListGetItemProperty && jxskinelement.getFieldClass() == 1 ) { 
															setText(jxValue.getString());
														} else {
															//preserve old logic (e.g. handle combobox). 
															setText(jxValue.getItemPropertyInterface().getString(jxValue.getString()));  
														}
													} else {
														setText(jxValue.getString());
													}
													break;
			case Cell.VTYPE_DATETIME:
//													setText(DateUtil.dateToDateTimeStr(jxValue.getDate()));
													if(jxskinelement.getElementType().equals("TimePicker")) {
														if (jxValue instanceof ColumnCell) {
															BiColumn bc = ((ColumnCell)jxValue).getBiColumn();
															if (StringUtils.isNotBlank(bc.getTimeCompEndTime()))
																setText(DateUtil.dateDigtalToTimeStr(jxValue.getDate(), !bc.getTimeCompIsShortFmt()));
															else
																setText(DateUtil.dateToTimeStr(jxValue.getDate(), !bc.getTimeCompIsShortFmt()));
														} else
															setText(DateUtil.dateToTimeStr(jxValue.getDate()));
													} else {
														if(jxValue.isTimeOnly()) {
															setText(DateUtil.dateToTimeStr(jxValue.getDate()));
														} else {
															setText(DateUtil.dateToDateTimeStr(jxValue.getDate()));
														}
													}
													break;
			case Cell.VTYPE_DATE:
													setText(DateUtil.toDateString(jxValue.getDate(),"yyyy/mm/dd"));
													break;
			case Cell.VTYPE_BOOLEAN:	
													if(jxValue.getBoolean()) {
														setText("Y");
													} else {
														setText("N");
													}
													break;
			}
		}
	}
	public void cellMap_formulachange(Cell c)
	{
		if(jxskinelement == null) return;
		
		if(/* c.getType() != Cell.VTYPE_STRING && */ c.getMode() != Cell.VMODE_DISPONLY) {
			if(c.getFormula() != null && c.getMode() == Cell.VMODE_NORMAL && !c.isProtected()) {
				if(c.getIgnoreFormula()) {
					jxskinelement.setEnable(true);
				} else {
					jxskinelement.setEnable(false);
				}
			} else {
				jxskinelement.setEnable(true);
			}
		}	
	}
	public void cellMap_formatchange(Cell c)
	{
		if(jxValue != null) {
			if(c.getDecFormat() != null) setJxValueFormat(c.getDecFormat().toPattern());
		}
	}
	public void cellMap_modechange(Cell c)
	{
		if(jxValue != null) {
			jxfieldmode = jxValue.getMode();
			realSetMode(jxValue.getMode(),jxValue.isOverrided(),jxValue.isProtected(),jxValue.isDirty());
		}
		cellMap_formulachange(c);
	}
	public void cellMap_hintchange(Cell c)
	{
		if(jxValue != null) {
			realSetHint(jxValue.getHint());
		}
	}
	public void cellMap_listchange(Cell c)
	{
		Vector v;
		if(jxValue != null) {
				Object o = jxskinelement.getValue();
				if(o == null) o = jxskinelement.getText();
				if(jxValue.getItemPropertyInterface() != null) {
					setItemListInterface( jxValue.getItemPropertyInterface() );
					int idx = jxValue.getItemPropertyInterface().getIndexOf(o);
//					if(idx >= 0) setItemIndex(idx);
					setItemIndex(idx);
				} else if(jxValue.getItemList() != null) {
					setItemList( jxValue.getItemList() );
					int idx = jxValue.getItemList().indexOf(o);
//					if(idx >= 0) setItemIndex(idx);
					setItemIndex(idx);
				}
		}
	}
	public Vector getSelectedIndexes()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getSelectedIndexes());
		}  else return(null);
	}
	public void setSelectedIndexes(Vector v)
	{
		if(jxskinelement != null) {
			jxskinelement.setSelectedIndexes(v);
		}  
		return;
	}
	public Vector getSelectList()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getSelectList());
		}  else return(null);
	}
	public void setSelectList(Vector v)
	{
		if(jxskinelement != null) {
			jxskinelement.setSelectList(v);
		}  
		return;
	}
	public JxForm getJxForm() {
	   return(jxform);
	}
	public boolean addSubMenu(String p_name,String p_label)
	{
		if(jxskinelement != null) {
			if(jxskinelement.addSubMenu(p_name,p_label) != null) {
				return(true);
			}
		} 
		return(false);
	}
	public void gridSetItemList(int col,int row,Vector itemlist,boolean pickonly)
	{
		if(jxskinelement != null) {
			jxskinelement.gridsetitemlist(col,row,itemlist,pickonly);
		} 
	}
	public Vector getTreeExpanded()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getTreeExpanded());
		}  else return(null);
	}
	public void setTreeExpanded(Vector expanded)
	{
		if(jxskinelement != null) {
			jxskinelement.setTreeExpanded(expanded);
		}
	}
	public int getNodeByXY(int x,int y)
	{
		if(jxskinelement != null) {
			return(jxskinelement.getNodeByXY(x,y));
		}  else return(-1);
	}
	public int startDropSource()
	{
		if(jxskinelement != null) {
			return(jxskinelement.startDropSource());
		}  else return(-1);
	}
	/*
	public int twainOpenDsm()
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainOpenDsm());
		}  else return(-1);
	}
	public int twainCloseDsm()
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainCloseDsm());
		}  else return(-1);
	}
	public int twainOpenDs()
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainOpenDs());
		}  else return(-1);
	}
	public int twainCloseDs()
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainCloseDs());
		}  else return(-1);
	}
	public int twainEnableDs(boolean showUI)
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainEnableDs(showUI));
		}  else return(-1);
	}
	public int twainDisableDs()
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainDisableDs());
		}  else return(-1);
	}
	public int twainSetSource(String source)
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainSetSource(source));
		}  else return(-1);
	}
	public Vector twainGetSources()
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainGetSources());
		}  else return(null);
	}
	public int twainSetCapability(TwainCapability cap)
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainSetCapability(cap));
		}  else return(-1);
	}
	public TwainCapability twainGetCapability(int capability)
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainGetCapability(capability));
		}  else return(null);
	}
	public int twainScan(boolean showUI,int dpi,int p_pixelType, int bitdepth,
		int p_left, int p_top, int p_right, int p_bottom,
		int p_fileType,String p_filename)
		throws Exception
	{
		if(jxskinelement != null) {
			return(jxskinelement.twainScan(showUI,dpi,p_pixelType, bitdepth,
					p_left, p_top, p_right, p_bottom, p_fileType,p_filename));
		}  else return(-1);
	}
	*/
	public int scaleImage(int x1,int x2, int y1,int y2)
	{
		if(jxskinelement != null) {
			return(jxskinelement.scaleImage(x1,x2,y1,y2));
		}  else return(-1);
	}
	public void setStyle(String p_style)
	{
		if(jxskinelement != null) {
			jxskinelement.setStyle(p_style);
		}
	}
	public String getStyle(){
		if(jxskinelement != null) {
			return(jxskinelement.getStyle());
		}
		return("");
	}
	public void setIconSclass(String p_style)
	{
		if(jxskinelement != null) {
			jxskinelement.setIconSclass(p_style);
		}
	}
	// implementation of JxFieldImageListInterface
	public int getIconByFileType(String p_filetype,String p_icontype /* S or L */) {
		if (jxskinelement != null) {
			return(((JxFieldImageListInterface)jxskinelement).getIconByFileType(p_filetype,p_icontype));
		}
		return(-1);
	}
	public int imageListLoadImage(String p_filetype,String p_filename) {
		if (jxskinelement != null) {
			return(((JxFieldImageListInterface)jxskinelement).imageListLoadImage(p_filetype,p_filename));
		}
		return(-1);
	}
	public void imageListSetSize(int width,int height) {
		if (jxskinelement != null) {
			((JxFieldImageListInterface)jxskinelement).imageListSetSize(width,height);
		}
	}
	// implementation of JxFieldFlowInterface
	public void addFlowListener(JxFlowListener listener) {
		if (jxskinelement == null) {
		   if (flowListeners == null) {
			   flowListeners = new Hashtable();
		      flowListeners.put(listener, listener);
		   }
		} 
		else
			((JxFieldFlowInterface) jxskinelement).addFlowListener(listener);
	}
	public void flowSave(String p_filename) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).flowSave(p_filename);
	}
	public void flowLoad(String p_filename) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).flowLoad(p_filename);
	}
	public int nodeGetShape(String p_nodekey) {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).nodeGetShape(p_nodekey));
	   return(-1);
	}
	public void nodeSetShape(String p_nodekey, int p_shape) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).nodeSetShape(p_nodekey, p_shape);
	}
	public String nodeGetFillColor(String p_nodekey) {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).nodeGetFillColor(p_nodekey));
	   return(null);
	}
	public void nodeSetFillColor(String p_nodekey, int p_r, int p_g, int p_b) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).nodeSetFillColor(p_nodekey, p_r, p_g, p_b);
	}
	public String nodeGetText(String p_nodekey) {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).nodeGetText(p_nodekey));
	   return(null);
	}
	public void nodeSetText(String p_nodekey, String p_text) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).nodeSetText(p_nodekey, p_text);
	}
	public String linkGetText(String p_nodekey, String p_linkkey) {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).linkGetText(p_nodekey, p_linkkey));
	   return(null);
	}
	public void linkSetText(String p_nodekey, String p_linkkey, String p_text) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).linkSetText(p_nodekey, p_linkkey, p_text);
	}
	public void nodeCreate(String p_nodekey, double p_left, double p_top, double p_width, double p_height) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).nodeCreate(p_nodekey, p_left, p_top, p_width, p_height);
	}
	public Vector getSelectedItems() {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).getSelectedItems());
	   return(null);
	}
	public int nodeDelete(String p_nodekey) {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).nodeDelete(p_nodekey));
	   return(-1);
	}
	public int linkDelete(String p_nodekey, String p_linkkey) {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).linkDelete(p_nodekey, p_linkkey));
	   return(-1);
	}
	public Vector flowTraverse() {
		if (jxskinelement != null)
			return(((JxFieldFlowInterface) jxskinelement).flowTraverse());
	   return(null);
	}
	public void linkCreate(String p_orgNodeKey, String p_dstNodeKey, String p_linkKey) {
		if (jxskinelement != null)
			((JxFieldFlowInterface) jxskinelement).linkCreate(p_orgNodeKey, p_dstNodeKey, p_linkKey);
	   return;
	}
	public void setNoBackwordLookup(boolean p_sw)
	{
		noBackwordLookup = p_sw;
	}
	public int setMenuShortCut(int p_vkey, int p_shiftstate)
	{
		if (jxskinelement != null)
			return(jxskinelement.setMenuShortCut(p_vkey,p_shiftstate));
		else
			return(-1);
	}
	public void setFocusReturn(String p_str)
	{
		if (jxskinelement != null)
			jxskinelement.setFocusReturn(p_str);
	}
	public void updateDisplay()
	{
		if (jxskinelement != null)
			jxskinelement.updateDisplay();
	}
	public JxField addElement(String p_skintype,Vector p_properties)
	{
		if(jxform == null) return(null);
		if (jxskinelement != null) {
			JxSkinElement jxse
				= jxskinelement.addElement(p_skintype,p_properties);
			if(jxse != null) {
				return(jxform.jxAdd(jxse.getName()));
			}
			return(null);
		}
		return(null);
	}
	public void gridSetColWidth(int p_col,int p_width)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setcolwidth(p_col,p_width);
		}
	}
	public void gridSetColWidth(int p_col,String p_width)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setcolwidth(p_col,p_width);
		}
	}
	public void gridSetRowHeight(int p_row,int p_height)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setrowheight(p_row,p_height);
		}
	}
	public void gridSetCurrentRow(int p_row)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setcurrentrow(p_row);
		}
	}

	public void unMapCell()
	{
		if(jxValue != null) {
			Cell c = jxValue;
			c.map(null);
			jxfieldmode = 0;
			setText("");
		}
	}

	public void setJxValue(Object o)
	{
		try {
			if(jxValue != null) jxValue.set(o);
		} catch (CellException  ex) {
			UniLog.log(ex);
		}
	}

	public void setJxValueFormat(String p_mask)
	{
			if(jxValue == null) return;
			if(p_mask != null) {
				if(p_mask.equals("#")) jxFieldFormat = null;
					else jxFieldFormat 	 = new DecimalFormat(p_mask);
			} else {
				jxFieldFormat 	 = new DecimalFormat("##########0.00");
			}
			jxFieldPosition = new FieldPosition(NumberFormat.INTEGER_FIELD);
	}
	public Vector grid_getDirtyCells()
	{
		if(jxskinelement == null) return(null);
		return(jxskinelement.grid_getDirtyCells());
	}
	public void setHeight(int p_height)
	{
		if(jxskinelement != null) {
			if(p_height == height) return;
			height = p_height;
			jxskinelement.setFieldHeight(p_height);

		} 
	}
	public void setWidth(int p_width)
	{
		if(jxskinelement != null) {
			if(p_width == width) return;
			width = p_width;
			jxskinelement.setFieldHeight(p_width);

		} 
	}
	public void setFontSize(int p_size)
	{
		UniLog.log("HAHA_00003");
		if(jxskinelement != null) {
			if(p_size == fontsize) return;
			fontsize = p_size;
			if(p_size > 0) {
				UniLog.log("HAHA_00004");
				jxskinelement.setFontSize(p_size);
			}
		} 
	}

	public void setEncoding(String p_encoding)
	{
		String enc;
		if(p_encoding == null || p_encoding.trim().equals("")) 
			enc = null;
		else
			enc = p_encoding;
		if(jxskinelement != null) {
			jxskinelement.setEncoding(enc);
		}
	}
	public void gridSetColor(int p_col,int p_row,int p_color)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setcolor(p_col,p_row,p_color);
		}
	}
	public void gridSetFontColor(int p_col,int p_row,int p_color)
	{
		if(jxskinelement != null) {
			jxskinelement.grid_setfontcolor(p_col,p_row,p_color);
		}
	}
	public void setItemPosition(int p_idx, int p_x, int p_y)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemPosition(p_idx, p_x, p_y);
		}
	}
	public void setItemWidth(int p_idx, int p_w)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemWidth(p_idx, p_w);
		}
	}
	public void setItemHeight(int p_idx, int p_h)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemHeight(p_idx, p_h);
		}
	}
	public void linkItem(int p_idx1, int p_idx2)
	{
		if(jxskinelement != null) {
			jxskinelement.linkItem(p_idx1, p_idx2);
		}
	}
	public void unlinkItem(int p_idx1, int p_idx2)
	{
		if(jxskinelement != null) {
			jxskinelement.unlinkItem(p_idx1, p_idx2);
		}
	}
	public void setAttribute(String p_attr,String p_value)
	{
		if(jxskinelement != null) {
			jxskinelement.setAttribute(p_attr, p_value);
		}
	}
	
	public int realizeTemplate(String p_template,int p_count)	
	{
		if(jxskinelement != null) {
			return(jxskinelement.realizeTemplate(p_template,p_count));
		} else return(-1);
	}
	public int realizeTemplate(String p_template,String p_suffix,int p_count)	
	{
		if(jxskinelement != null) {
			return(jxskinelement.realizeTemplate(p_template,p_suffix,p_count));
		} else return(-1);
	}
	public void gridSetDataFormat(int p_col,int p_row,String p_format) {
		if(jxskinelement != null) {
			jxskinelement.grid_setDataFormat(p_col,p_row,p_format);
		}
	}
	
//	public boolean addBandInfo(String p_bandinfoId){
//		if(jxskinelement != null) {
//			return(jxskinelement.addBandInfo(p_bandinfoId));
//		}
//		return(false);
//	}
	/*
	public boolean setTooltip(String p_bandinfoId, String p_tooltip){
		if(jxskinelement != null) {
			return(jxskinelement.setTooltip(p_bandinfoId, p_tooltip));
		}
		return(false);
	}
	public boolean setMold(String p_bandinfoId, String p_mold){
		if(jxskinelement != null) {
			return(jxskinelement.setMold(p_bandinfoId, p_mold));
		}
		return(false);
	}
	*/
//	public boolean addOccurEvent(String p_bandinfoId, String p_occurEventId, Date p_startDate, Date p_endDate, String p_color, String p_desc, String p_text){
//		if(jxskinelement != null) {
//			return(jxskinelement.addOccurEvent(p_bandinfoId, p_occurEventId, p_startDate, p_endDate, p_color, p_desc, p_text));
//		}
//		return(false);
//	}
//	public Bandinfo getBandInfo(String p_bandinfoId) {
//		if(jxskinelement != null) {
//			return(jxskinelement.getBandInfo((p_bandinfoId)));
//		}
//		return(null);
//	}
	public void gridSetToogleMode(boolean p_toogleModeFlag){
		if(jxskinelement != null) {
			jxskinelement.grid_settooglemode(p_toogleModeFlag);
		}
	}
	
	public Object getNativeObject()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getNativeComponent());
		} else return(null);
	}
	public Object getActionEvent()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getActionEvent());
		} else return(null);
	}
	
	public Cell getJxValue()
	{
		return(jxValue);
	}
	
	public void setItemSubOptions(Map<String,Vector<LabelCell>> p_options)
	{
		if(jxskinelement != null) {
			jxskinelement.setItemSubOptions(p_options);
		} 
	}

	public int getActionType()
	{
		if(jxskinelement != null) {
			return(jxskinelement.getActionType());
		} else return(0);
		
	}
	
	public void setItemStyle(int p_item,String p_style) {
		if(jxskinelement != null) {
			jxskinelement.setItemStyle(p_item,p_style);
		} 
	}
	
	public void invlidate(){
		if (jxskinelement != null){
			jxskinelement.invlidate();
		}
	}
	
	public int getJxFieldType()
	{
		return(jxFieldType);
	}
	public void setInstant(boolean p_flag){
		if (jxskinelement != null){
			jxskinelement.setInstant(p_flag);
		}
	}
	public Object getValue(){
		if (jxskinelement != null){
			return(jxskinelement.getValue());
		} else return(null);
	}
	
	public JxActionListener getActionListner() {
		return(actionlistener);
	}
	
	public boolean isOverrided() {
		if(jxValue != null) return(jxValue.isOverrided()); else return(false);
	}
	public void clearOverride() throws CellException {
		if(jxValue != null) jxValue.clearOverride();
	}
	public void setProtect(boolean p_sw) throws CellException {
		if(jxValue != null) jxValue.protect(p_sw);
	}
}
