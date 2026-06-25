package com.uniinformation.jx.zk;

import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.LabelCell;
import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiRuntimeException;
import com.uniinformation.rpccall.*;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

//import org.zkforge.timeline.Bandinfo; 250821 remoevd
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.LabelImageElement;

abstract class JxZkElement extends JxSkinElement
{
	JxZkSkin skin;
	int fdtypeid;
	Component comp;
	JxActionListener actionListener = null;
	EventListener zkEventListener;
	int lastActionType=0;
	Event lastActionEvent = null;
	
	public JxZkElement(JxZkSkin p_skin,int p_fdtypeid, Component c)
	{
		super(p_skin,c.getId(),null);
		skin = p_skin;
		fdtypeid = p_fdtypeid;
		comp = c;
		zkEventListener = new ZkBiEventListener() {
			public void onZkBiEvent(Event event) throws Exception {
					UniLog.log("Event Received " + event);
					try {
						processAction(event);
					}
					catch(ZkBiRuntimeException ex) {
						//when ZkBiRuntmeException occur (e.g. br is null). just abort the processAction. 
						ex.printStackTrace();
						if (!ex.getIgnore()) {
							ZkUtil.showErrMsg("Runtime Error:" + ex.getMessage());
						}
					}
			}	
		};
		comp.addEventListener(Events.ON_CTRL_KEY, zkEventListener);
		useSkinElementDefault = true;
	}
	
	protected String processAction(Event ev) {
//		UniLog.log("Event Name: " + ev.getName()+ " comp " + ev.getTarget());
		//skin.setDirtyFlag(true); //wrong dirty for add new/update record
		//if (!ZkUtil.checkDupEvent(ev)) return null;  //andrew210318 validation moved to ZkBiEventListener
		lastActionEvent = ev;
		switch(JxZkGadgetProvider.getEventID(ev.getName())) {
			case JxZkGadgetProvider.EV_ONDELAYCLICK :
			case JxZkGadgetProvider.EV_ONCLICK :
				if(actionListener != null) {
					lastActionType = JxField.ACTIONTYPE_CLICK;
					actionListener.actionPerformed(getJxField());
					lastActionType = 0;
				} else {
					if(comp instanceof Button) {
						JxField fd = getJxField();
						if(fd != null) {
							if(getJxField().getJxFieldType() == JxField.FTYPE_INT) {
								int n = fd.getJxValue().getInt();
								fd.validateChange(""+n,""+(n+1));
							
							}						
						}
					}
				}
				break;
			case JxZkGadgetProvider.EV_ONDOUBLECLICK :
				if(actionListener != null) {
					lastActionType = JxField.ACTIONTYPE_DOUBLECLICK;
					actionListener.actionPerformed(getJxField());
					lastActionType = 0;
				}
				break;
			case JxZkGadgetProvider.EV_ONOK:
				if(actionListener != null) {
					lastActionType = JxField.ACTIONTYPE_SELECT;
					actionListener.actionPerformed(getJxField());
					lastActionType = 0;
				}
				break;
			default:
				UniLog.log1("event not supported:" + ev.getName());
				break;
		}
		lastActionEvent = null;
		//ZkUtil.checkDupEvent(ev,0); //for update timestamp only //andrew210318 validation moved to ZkBiEventListener
		return(null);
	}

	
	protected int getFieldTypeID()
	{
		return(fdtypeid);
	}
	public void addActionListener(JxActionListener p_listener)
	{
		//UniLog.log("addActionListener on Unknown Element Type field " + getName());
		UniLog.log("JxCbuilderButton.addActionListener " + getName());
		if(p_listener != null) {
			if(actionListener == null && ! (comp instanceof Button)) {
				comp.addEventListener("onClick", zkEventListener);
			}
		} else {
			if(actionListener != null && !(comp instanceof Button)) {
				comp.removeEventListener("onClick", zkEventListener);
			}
		}
		actionListener = p_listener;
	}
	
	protected JxDragListener dragListener;
	protected JxDblClickListener dblClickListener;
	protected String focusReturnStr;
	protected String encoding = null;
	
	public void addSelectListener(JxSelectListener x)
	{
	}
	public void addChangeListener(JxChangeListener x)
	{
	}
	public void addGridChangeListener(JxGridChangeListener x)
	{
	}
	public void addColumnClickListener(JxColumnClickListener x)
	{
	}
	public void addGetDataIdxListener(JxGetDataIdxListener x)
	{
	}
	public void addDblClickListener(JxDblClickListener x)
	{
		dblClickListener = x;
	}
	public void addDragListener(JxDragListener p_listener)
	{
		dragListener = p_listener;
	}
	public void addDropTargetListener(JxDropTargetListener x)
	{
	}
	public void setEnable(boolean b)
	{
	}
	public void setVisible(boolean b)
	{
		comp.setVisible(b);
	}
	public void setText(String p_text)
	{
	}
	public String getText()
	{
		return(null);
	}
	public void setCaption(String p_text)
	{
		UniLog.log("ZkElement set Caption " + p_text);
	}
	public void clear()
	{
	}
	public void grid_setFixedCol(int n)
	{
		// TODO: grid_setFixedCol()
	}
	public void grid_setFixedRow(int n)
	{
	}
	public boolean grid_setValue(int col, int row, Object value)
	{
		return(false);
	}
	public boolean grid_setValue(Vector p_v)
	{
		return(false);
	}
	public Object grid_getValue(int col, int row)
	{
		return(null);
	}
	public void grid_setCol(int n)
	{
	}
	public void grid_setRow(int n)
	{
	}

	public boolean grid_deleterow(int idx)
	{
		return(false);
	}
	public boolean grid_appendrow()
	{
		return(false);
	}
	public boolean grid_insertrow(int idx)
	{
		return(false);
	}
	public boolean grid_setrowheader(int idx,String s)
	{
		return(false);
	}
	@Override
	public boolean grid_setcolheader(int idx,Object s)
	{
		return(false);
	}
	@Override
	public boolean grid_setcolheader(int idx,Object header,
		String alignment, boolean autosize, int imgidx,int minwidth,int maxwidth,int width)
	{
		return(false);
	}
	public void addMenuItem(JxSkinElement p_menuitem)
	{
	}
	public void reload()
	{
	}
	public void addNode(JxSkinElement p_node)
	{
		return;
	}
	public void removeNode(JxSkinElement p_node)
	{
		return;
	}
	public boolean addImage(String p_fileprefix)
	{
		return(false);
	}
	public boolean removeImage(int p_index)
	{
		return(false);
	}
	public boolean setImageList(JxSkinElement p_imagelist)
	{
		return(false);
	}
	public void setImageIndex(int p_target,int p_index)
	{
	}
	public void setImageIndex(int p_index)
	{
	}
	public String processAction(int actionType)
	{
		return(null);
	}
	public String processAction(int actionType, Vector p_args)
	{
		return(null);
	}
	public String processAction(int actionType, int arg1)
	{
	   return(null);
	}
	public String processAction(int actionType, int arg1,int arg2)
	{
	   return(null);
	}
	public void syncText(String p_text)
	{
	}
	public void syncGrid(int col,int row,String p_text)
	{
	}
	public void setFocus()
	{
	}
	public void setLength(int p_length)
	{
	}
	public void setFormat(boolean sw)
	{
	}
	public String makeFormat(int p_type,int p_length,int p_sublength,String p_format)
	{
		return(null);
	}
	public String makeDefault(int p_type,int p_length,int p_sublength,String p_format)
	{
		if(p_format != null) return(p_format);
		switch(p_type) {
		case 0 /* integer */ : 		return("0");
		case 1 /* string */  :		return("");
		case 2 /* float */   : 		return("0.0");
		case 3 /* date */		:		return("0000-00-00");
		}
		return(null);
	}

	public void moveItemByIdx(int fromidx,int toidx)
	{
	}
	public int getItemIndex()
	{
		return(-1);
	}
	public void setItemIndex(int p_idx)
	{
	}
	/*
	@Override
	public boolean setItemToList(Object p_item,int p_idx)
	{
		return(false);
	}
	*/
	@Override
	public int addItemToList(Object p_item,int p_idx)
	{
		return(-1);
	}
	@Override
	public void setItemList(List itemlist)
	{
	}
	public int addNode(String p_text)
	{
		return(-1);
	}
	public int addNode(String p_text,int p_imgidx)
	{
		return(-1);
	}
	public int addChild(int parent,String p_text)
	{
		return(-1);
	}
	public int addChild(int parent,String p_text,int p_imgidx)
	{
		return(-1);
	}
	public int deleteNode(int nodeid)
	{
		return(-1);
	}

	public int changeNode(int nodeid,String desc)
	{
		return(-1);
	}

	public int getCurNodeIdx()
	{
		return(-1);
	}

	public void setCurNodeIdx(int p_idx)
	{
	}

	public int grid_getcurrentcol()
	{
		return(-1);
	}
	public int grid_getcurrentrow()
	{
		return(-1);
	}
	public void grid_cleararray()
	{
	}
	public boolean isfocused()
	{
		return(false);
	}
	public void setReadOnly(boolean p_sw)
	{
	}
	public void setColor(int p_color)
	{
	}
	public void setFontColor(int p_color)
	{
	}
	public String getStyle(){
		String styleString = null;
		if (comp instanceof HtmlBasedComponent){
			styleString = ((HtmlBasedComponent)comp).getStyle();
		}
		return((styleString == null ? "" : styleString));
	}
	public void setStyle(String p_style){
		if (comp instanceof HtmlBasedComponent){
			((HtmlBasedComponent)comp).setStyle(p_style);
		}
	}
	public void setIconSclass(String p_style){
		if (comp instanceof LabelImageElement){
			((LabelImageElement)comp).setIconSclass(p_style);
		}
	}
	public void setHint(boolean p_sw, String p_str)
	{
	}
	public boolean forceValidateChange()
	{
		return(true);
	}
	public Vector getSelectedIndexes()
	{
		return(null);
	}
	public void setSelectedIndexes(Vector itemlist)
	{
	}
	public Vector getSelectList()
	{
		return(null);
	}
	public void setSelectList(Vector itemlist)
	{
	}
	public JxSkinElement addSubMenu(String p_name,String p_label)
	{
		return(null);
	}
	public void gridsetitemlist(int col,int row,Vector itemlist,boolean pickonly)
	{
	}
	public void setTreeExpanded(Vector v)
	{
	}
	public Vector getTreeExpanded()
	{
		return(null);
	}
	public int getNodeByXY(int x, int y)
	{
		return(-1);
	}
	public int startDropSource()
	{
		return(-1);
	}
	public int twainOpenDsm()
	{
		return(-1);
	}
	public int twainCloseDsm()
	{
		return(-1);
	}
	public int twainOpenDs()
	{
		return(-1);
	}
	public int twainCloseDs()
	{
		return(-1);
	}
	public int twainEnableDs(boolean showUI)
	{
		return(-1);
	}
	public int twainDisableDs()
	{
		return(-1);
	}
	public int twainSetSource(String source)
	{
		return(-1);
	}
	public Vector twainGetSources()
	{
		return(null);
	}
	public int twainScan(boolean showUI,int dpi,int p_pixelType, int bitdepth,
		int p_left, int p_top, int p_right, int p_bottom,
		int p_fileType,String p_filename)
		throws Exception
	{
		return(-1);
	}
	public int scaleImage(int x1,int x2,int y1,int y2)
	{
		return(0);
	}
	public int getIconByFileType(String p_filetype,String p_icontype)
	{
		return(-1);
	}
	public int setMenuShortCut(int p_vkey, int p_shiftstate)
	{
		return(-1);
	}
	public void setFocusReturn(String p_str)
	{
		focusReturnStr = p_str;
	}
	public void updateDisplay()
	{
	}
	public JxSkinElement addElement(String p_skintype,Vector p_properties)
	{
		return(null);
	}
	public boolean grid_setcolwidth(int idx,int width)
	{
		return(false);
	}
	public boolean grid_setcolwidth(int idx,String width)
	{
		return(false);
	}
	public boolean grid_setrowheight(int idx,int height)
	{
		return(false);
	}
	public void grid_setcurrentrow(int row)
	{
	}
	public Vector grid_getDirtyCells()
	{
		return(null);
	}
	public void setFieldHeight(int p_height)
	{
		if(comp instanceof HtmlBasedComponent) {
			((HtmlBasedComponent) comp).setHeight(""+p_height+"px");
		}
	}
	public void setFieldWidth(int p_width)
	{
	}
	public void setFontSize(int p_size)
	{
	}
	public void setEncoding(String p_encoding)
	{
	}
	public void grid_setcolor(int col,int row,int color)
	{
		// TODO: grid_setFixedCol()
	}
	public void grid_setfontcolor(int col,int row,int color)
	{
		// TODO: grid_setFixedCol()
	}

	public void setItemPosition(int p_idx,int p_x,int p_y)
	{
	}
	public void setItemWidth(int p_idx,int p_w)
	{
	}
	public void setItemHeight(int p_idx,int p_h)
	{
	}
	public void linkItem(int p_idx1,int p_idx2)
	{
	}
	public void unlinkItem(int p_idx1,int p_idx2)
	{
	}
	public void setAttribute(String p_attr,String p_value)
	{
	}
	public int realizeTemplate(String p_template,int p_count)
	{
		return(-1);
	}
	public int realizeTemplate(String p_template,String p_suffix,int p_count)
	{
		return(-1);
	}
	
	
	public void grid_setDataFormat(int p_col,int p_row,String p_format)
	{
	}

//	@Override
//	public boolean addBandInfo(String p_bandinfoId) {
//		return false;
//	}
//
//	@Override
//	public Bandinfo getBandInfo(String p_bandinfoId) {
//		return null;
//	}

	@Override
	public Component getNativeComponent() { //dirty way to obtain zk component
		return (comp);
	}
	
	@Override
	public Event getActionEvent() { //dirty way to obtain zk component
		return (lastActionEvent);
	}

	@Override
	public void grid_settooglemode(boolean p_toogleModeFlag) {
	}

//	@Override
//	public boolean addOccurEvent(String p_bandinfoId, String p_occurEventId,
//			Date p_startDate, Date p_endDate, String p_color, String p_desc,
//			String p_text) {
//		return false;
//	}
//	
//	@Override
//	public boolean delOccurEvent(String p_bandinfoId, String p_occurEventId) {
//		return false;
//	}
//
//	@Override
//	public boolean refreshBandInfo(String p_bandinfoId) {
//		return false;
//	}
	public void setJxField(JxField f)
	{
		super.setJxField(f);
		jxfield_setVisible(comp.isVisible());
	}
	/*
	@Override
	public void setItemList(Vector itemList,Vector itemLabel)
	{
		setItemList(itemList);
	}
	*/
	@Override
	public void setItemListInterface(AbstractGetItemProperty p_interface)
	{
		
	}
	@Override
	public void setItemSubOptions(Map<String,Vector<LabelCell>> p_options)
	{
	}
	@Override
	public int getActionType()
	{
		return(lastActionType);
	}
	
	@Override
	public void setItemStyle(int p_idx,String p_style)
	{
	}

	@Override
	public void invlidate() {
		comp.invalidate();
	}
	
	@Override 
	public void setInstant(boolean p_flag) {
		
	}
	
//	@Override
//	public Object getValue() {
//		return(null);
//	}
	
	@Override
	public void setFormat(String p_format) {
		
	}

	boolean needAutoConvert(JxField fd) { 
		/*
		if(fd != null && fd.getJxValue() != null && fd.getJxValue() instanceof ColumnCell) {
			ColumnCell cc = (ColumnCell)fd.getJxValue();
			return(!cc.getBiColumn().isUTF8());
//			return(!cc.getBiColumn().isUTF8() && cc.getBiColumn().isAutoTranslate() );
		}
		return(true);
		*/
		return(false);
	}
	
	public String getElementType() {
		return("");
	}

	public void setLabelColor(String p_color) {
		UniLog.log("setLabelColor " + p_color);
		HtmlBasedComponent hComp = (HtmlBasedComponent) comp.getAttribute("fdLabelComp");
		if(hComp != null) {
			ZkUtil.setFontColor(hComp, p_color);
		}
	}
	
	@Override
	public int getFieldClass() {
		return(0);
	}
}
