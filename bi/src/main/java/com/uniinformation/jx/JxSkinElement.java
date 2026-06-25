package com.uniinformation.jx;
import java.util.*;

import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.LabelCell;
public abstract class JxSkinElement
{
	private String fieldname;
	private Object jxobject;
	protected JxField jxfield;
	protected JxActionListener jxactionlistener;
	protected JxSelectListener jxselectlistener;
	protected JxSkin skin;
	public boolean debug = false;
	protected boolean useSkinElementDefault = false;

	public JxSkinElement(JxSkin p_skin,String p_fieldname,Object p_obj)
	{
		skin = p_skin;
		fieldname= p_fieldname;
		jxobject = p_obj;
	}
	public JxSkin getSkin()
	{
		return(skin);
	}
	public void setControl(Object p_control)
	{
		jxobject = p_control;
	}
	public Object getControl()
	{
		return(jxobject); 
	}
	public String getName()
	{
		return(fieldname);
	}
	public void setJxField(JxField f)
	{
		jxfield = f;
	}
	public JxField getJxField()
	{
		return(jxfield);
	}
	
	protected void jxfield_setVisible(boolean p_sw)
	{
		jxfield.isVisible = p_sw;
	}
	protected void jxfield_setEnable(boolean p_sw)
	{
		jxfield.isEnabled = p_sw;
	}
	public abstract void setEnable(boolean b);
	public abstract void setVisible(boolean b);
	public abstract void addActionListener(JxActionListener x);
	public abstract void addSelectListener(JxSelectListener x);
	public abstract void addChangeListener(JxChangeListener x);
	public abstract void addGridChangeListener(JxGridChangeListener x);
	public abstract void addColumnClickListener(JxColumnClickListener x);
	public abstract void addGetDataIdxListener(JxGetDataIdxListener x);
	public abstract void addDblClickListener(JxDblClickListener x);
	public abstract void addDragListener(JxDragListener x);
	public abstract void addDropTargetListener(JxDropTargetListener x);
//	public abstract int addItem(Object p_item);
	// public abstract int clearItem(String p_item);
	public abstract void setText(String p_text);
	public abstract String getText();
	public abstract void setCaption(String p_caption);
	public abstract void clear();
	public abstract void grid_setFixedCol(int n);
	public abstract void grid_setFixedRow(int n);
	public abstract boolean grid_setValue(int col, int row, Object value);
	public abstract boolean grid_setValue(Vector p_v);
	public abstract Object grid_getValue(int col, int row);
	public abstract void grid_setCol(int n);
	public abstract void grid_setRow(int n);
	public abstract boolean grid_deleterow(int idx);
	public abstract boolean grid_appendrow();
	public abstract boolean grid_insertrow(int idx);
	/***
	 * @param idx - column index
	 * @param p_header - header can be String or Map
	 * @return
	 */
	public abstract boolean grid_setcolheader(int idx,Object p_header);
	/***
	 * @param p_col - column index
	 * @param p_header - header can be String or Map
	 * @param alignment
	 * @param autosize
	 * @param imgidx
	 * @param minwidth
	 * @param maxwidth
	 * @param width
	 * @return
	 */
	public abstract boolean grid_setcolheader(int p_col,Object p_header,
		String alignment, boolean autosize, int imgidx,int minwidth,int maxwidth,int width);
	public abstract boolean grid_setrowheader(int idx,String s);
	public abstract int grid_getcurrentrow();
	public abstract int grid_getcurrentcol();
	public abstract void grid_cleararray();
	public abstract void addMenuItem(JxSkinElement menuitem);
	public abstract void reload(); // for treenode
	public abstract void addNode(JxSkinElement node);
	public abstract void removeNode(JxSkinElement node);
	public abstract int	getCurNodeIdx();
	public abstract void setCurNodeIdx(int p_idx);
	public abstract boolean addImage(String p_imagefile);
	public abstract boolean removeImage(int p_index);
	public abstract boolean setImageList(JxSkinElement p_imagelist);
	public abstract void setImageIndex(int p_nodeid,int p_index);
	public abstract void setImageIndex(int p_index);
//	public abstract void setFocus();
	public abstract void setLength(int p_length);
	public abstract void setFormat(boolean p_sw);
/*
	public abstract String makeFormat(int p_type,int p_length,int p_sublength,String p_format);
	public abstract String makeDefault(int p_type,int p_length,int p_sublength,String p_format);
*/
	public abstract void moveItemByIdx(int fromidx,int toidx);
	public abstract int getItemIndex();
	public abstract void setItemIndex(int p_idx);
//	public abstract void setItemList(Vector itemlist);
	public abstract int addItemToList(Object p_item,int p_idx);
//	public abstract boolean setItemToList(Object p_item,int p_idx);
	public abstract int addNode(String p_text);
	public abstract int addNode(String p_text,int p_imgidx);
	public abstract int addChild(int parent,String p_text);
	public abstract int addChild(int parent,String p_text,int p_imgidx);
	public abstract int deleteNode(int nodeid);
	public abstract int changeNode(int nodeid,String p_desc);
	public abstract boolean isfocused();
	public abstract void setColor(int p_color);
	public abstract void setFontColor(int p_color);
	public abstract void setReadOnly(boolean p_sw);
	public abstract void setHint(boolean p_sw,String p_hint);
	public abstract Vector getSelectedIndexes();
	public abstract void setSelectedIndexes(Vector v);
	public abstract Vector getSelectList();
	public abstract void setSelectList(Vector v);
	public abstract JxSkinElement addSubMenu(String p_name,String p_label);
	public abstract void gridsetitemlist(int col,int row,Vector v,boolean pickonly);
	public abstract Vector getTreeExpanded();
	public abstract void setTreeExpanded(Vector p_expanded);
	public abstract int getNodeByXY(int x,int y);
	public abstract int startDropSource();
	public abstract int twainOpenDsm();
	public abstract int twainCloseDsm();
	public abstract int twainOpenDs();
	public abstract int twainCloseDs();
	public abstract int twainEnableDs(boolean showUI);
	public abstract int twainDisableDs();
	public abstract int twainSetSource(String source);
	public abstract Vector twainGetSources();
	public abstract int twainScan(boolean showUI,int dpi,int p_pixelType, int bitdepth,
		int p_left, int p_top, int p_right, int p_bottom,
		int p_fileType,String p_filename) throws Exception;
	public abstract int scaleImage(int x1,int x2, int y1, int y2);
	public abstract void setStyle(String p_style);
	public abstract void setIconSclass(String p_style);
	public abstract String getStyle();
	public abstract int getIconByFileType(String p_filetype,String p_icontype);
	public abstract int setMenuShortCut(int p_vkey, int p_shiftstate);
	public abstract void setFocusReturn(String p_str);
	public abstract void updateDisplay();
	public abstract JxSkinElement addElement(String p_skintype,Vector p_properties);
	public abstract boolean grid_setcolwidth(int p_col,int p_width);
	public abstract boolean grid_setcolwidth(int p_col,String p_width); //for % string
	public abstract boolean grid_setrowheight(int p_row,int p_height);
	public abstract void grid_setcurrentrow(int p_row);
	public abstract Vector grid_getDirtyCells();
	public abstract void setFieldHeight(int p_height);
	public abstract void setFieldWidth(int p_width);
	public abstract void setFontSize(int p_size);
	public abstract void setEncoding(String p_encoding);
	public abstract void grid_setcolor(int p_col,int p_row,int p_color);
	public abstract void grid_setfontcolor(int p_col,int p_row,int p_color);
	public abstract void setItemWidth(int p_idx,int p_w);
	public abstract void setItemHeight(int p_idx,int p_h);
	public abstract void setItemPosition(int p_idx,int p_x, int p_y);
	public abstract void linkItem(int p_idx1,int p_idx2);
	public abstract void unlinkItem(int p_idx1,int p_idx2);
	public abstract void setAttribute(String p_idx1,String p_idx2);
	public abstract int realizeTemplate(String p_template,int p_count);
	public abstract int realizeTemplate(String p_template,String p_suffix,int p_count);
	public abstract void grid_setDataFormat(int p_col,int p_row,String p_format);
	
//	public abstract boolean addBandInfo(String p_bandinfoId);
//	public abstract Bandinfo getBandInfo(String p_bandinfoId);
//	public abstract boolean addOccurEvent(String p_bandinfoId, String p_occurEventId, Date p_startDate, Date p_endDate, String p_color, String p_desc, String p_text);
//	public abstract boolean delOccurEvent(String p_bandinfoId, String p_occurEventId);
//	public abstract boolean refreshBandInfo(String p_bandinfoId);
	public abstract Object getNativeComponent();
	
	public abstract void grid_settooglemode(boolean p_toogleModeFlag);
//	public abstract void setItemList(Vector itemlist,Vector itemLabel);
	public abstract void setItemList(List itemlist);
	public abstract void setItemListInterface(AbstractGetItemProperty p_interface);
	public abstract void setItemSubOptions(Map<String,Vector<LabelCell>> p_options);
	public abstract int getActionType();
	public abstract Object getActionEvent();
	public abstract void setItemStyle(int p_idx,String p_style);
	public abstract void invlidate();
	public abstract void setInstant(boolean p_flag);
	public abstract Object getValue();
	public abstract void setFormat(String p_format);
	public abstract String getElementType();
	public abstract void setLabelColor(String p_color);
	public abstract int getFieldClass();
}

