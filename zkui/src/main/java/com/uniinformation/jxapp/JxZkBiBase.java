package com.uniinformation.jxapp;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.Disable;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Box;
import org.zkoss.zul.Button;
import org.zkoss.zul.Calendar;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Frozen;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Html;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listfoot;
import org.zkoss.zul.Listfooter;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.jx.*;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.JxZkSkin;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.jx.zk.ZkJxTimePicker;
import com.uniinformation.jx.zk.ZkJxTimePickerList;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.cell.*;
import com.uniinformation.erpv4.BiConfig;
import com.google.api.client.util.Lists;
import com.google.gson.JsonObject;
import com.kyoko.common.NumberUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.*;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ActionButtonHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiGetItemProperty;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiRecordCopy;
import com.uniinformation.zkbi.ZkBiRuntimeException;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
import com.uniinformation.zkcomp.S2Listbox;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkcomp.ZkBiDiv;
import com.uniinformation.zkcomp.ZkBiFdiv;
import com.uniinformation.zkcomp.ZkBiFdiv2;
import com.uniinformation.zkf.ZkfAction;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.AttachmentUploadInterface;
import com.uniinformation.utils.BiMedia;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.GsonUtil;

/***
 * Andrew remark:
 * Use JxZkBiBaseCallback for loose coupling. Do not call ZkBiComposerBase directly
 *
 */

public class JxZkBiBase extends JxZkBase
{
	/*
   	public final static int pxPerChar = 10;
   	public final static int pxForInt  = 140;
   	public final static int pxForFloat= 140;
   	public final static int pxForDate = 140;
   	public final static int pxForDateTime = 280;
   	public final static int pxMin = 80;
   	public final static int pxMax = 600;
   	public final static int pxComboButton = 40;
   	public final static int numberDefaultChar = 10;
   	*/
	public enum CloseAction {Close,Reload,Prompt};
	public static final int AFTERADDUPDATE_ACTION_NONE  = 0;
	public static final int AFTERADDUPDATE_ACTION_CLOSE = 1;
	public static final int AFTERADDUPDATE_ACTION_RELOAD= 2;
	public static final int AFTERADDUPDATE_ACTION_NEXT  = 3;
   	
	public static final String EV_ON_SELOPT_OK = "EV_ON_SELOPT_OK";
   	protected boolean LOCK_RECORD_FOR_UPDATE = false;
   	
	protected final static int INS_IDX_APPEND = -1;
	protected final static int INS_IDX_ACTIONIDX = -2;
	
	//public static boolean frozenActionColumn = false; //disable frozen due to Chrome scolling bug
	
	
	private BiResult br = null;  //andrew200717: br change to private to avoid br null exception, please use getBr() to obtain biresult object
	protected JxZkBiBaseCallback zkcb = null;
	public boolean isMobile = false;
	boolean hasAUDColumn = true;
	public static final int MODE_ADD = 1;
	public static final int MODE_UPDATE = 2;
	public static final int MODE_DISPLAY = 3;
	protected int curMode = -1;     // called by bindCellCollection
	//SessionHelper sessionHelper = null; //181121 andrew: change to protected to allow it used by child class
	
//	protected SessionHelper sessionHelper = null;
	protected Component parentComp = null;
	protected Component curComp = null;
	
	private Hashtable <String,AbstractGetItemProperty> gipiHash;
	private DetailDecoration curDetailDecoration = null;
	protected boolean needRefreshFlag = false;
	boolean abortFlag = false;
//	boolean defaultUpdateAndCloseFlag = false;
//	boolean defaultAddAndCloseFlag = false;
	CloseAction defaultAddCloseAction = CloseAction.Prompt;
	CloseAction defaultUpdateCloseAction = CloseAction.Prompt;
	protected ActionButtonHelper abHelper = null;
	boolean isAddModeWhenOpen = false;
	
	public boolean displayOnlyWhenUpdate = false;
	public boolean useS2Listbox = false;
	private final static boolean allowReloadDetail = true;  //temporary switch, can remove it later.
	
	private Object userData;

	protected Hashtable<String,BiActionHandler> bahHash;

	public JxZkBiBase(){
		super();
//		sessionHelper = getSessionHelper();
	}
	
	static class DetailDecoration {
    	Rows dRows;
    	Columns dCols;
    	Component dToolBar;
    	Grid grid = null;
    	DetailDecoration(Component dWin , boolean isMobile, SessionHelper p_sessionHelper,boolean p_useCompDiv) {
			if(dWin.hasFellow("detail_grid",true)) {
				grid = (Grid) dWin.getFellow("detail_grid");
			} else {
				grid = new Grid();
				grid.setId("detail_grid");
				if (p_sessionHelper.useJxFormG2()) {
					grid.setSclass("zkbi-detail-grid");
				}
				dWin.appendChild(grid);
			}
			if (isMobile){
				grid.setWidth("100%");
			}
			else{
				//grid.setWidth("800px");
				grid.setWidth("100%");
				
				//add group box for separate parent/child record (TODO)
				/*
				Groupbox groupBox = new Groupbox();
				groupBox.appendChild(new Caption("Record Details"));
				grid.setParent(groupBox);
				dWin.appendChild(groupBox);
				*/
			}
			
	    	dCols = grid.getColumns();
	    	if(dCols ==null){
	    		dCols  = new Columns();
//		    	if(/* isMobile && Not Work will have issue in detail screen master column when useCompDiv is on */ p_useCompDiv) {
		    	if(p_useCompDiv) {
		    		if(isMobile) {
		    			Column divColumn = new Column();
	    				dCols .appendChild(divColumn);
	//    			divColumn.setWidth("600px"); 
		    		}
		    	} else {
		    		Column labelColumn = new Column();
		    		labelColumn.setAlign("right");
		    		//labelColumn.setWidth("20%");
		    		if(!isMobile) {
	    				labelColumn.setWidth("250px"); 
	    				//labelColumn.setHflex("min");  //why this line does not work???
	    			}
	    			else {
	    				labelColumn.setWidth("120px");
	    				//labelColumn.setHflex("min");  //andrew200325: hflex=min does not work for colspan. https://tracker.zkoss.org/browse/ZK-1162
	    			}
	    			Column textColumn = new Column();
	    			//textColumn.setWidth("80%");
	    			textColumn.setAlign("left");
	    			textColumn.setHflex("1");
	    			dCols .appendChild(labelColumn);
	    			dCols .appendChild(textColumn);
		    	}
	    		grid.appendChild(dCols );    	
	    	}
	    	Rows rows = grid.getRows();
	    	if(rows == null) {
	    		rows = new Rows();
	    		grid.appendChild(rows);
	    	}
	    	dRows = rows;
			if(dWin.hasFellow("detail_toolbar",true)) {
				dToolBar = (Component) dWin.getFellow("detail_toolbar");
			} else {
				Toolbar tb = new Toolbar();
				tb.setAlign("end");
				tb.setId("detail_toolbar");
				if(!isMobile && p_sessionHelper.getNewButtonPanelLayout()){
					dWin.insertBefore(tb, grid);
				} else {
					dWin.appendChild(tb);
				}
				
				dToolBar = (Component) tb;
			}
			if(isMobile) {
				if(dToolBar instanceof Toolbar) {
					//((Toolbar) dToolBar).setOrient("vertical");
					((Toolbar) dToolBar).setAlign("center");
				}
			}
			if (dToolBar instanceof Toolbar) {
				ZkUtil.appendSclass((Toolbar)dToolBar, "zkbi-detail-actionbar");
			}
			/*
			if(dToolBar instanceof Toolbar){
				((Toolbar)dToolBar).setOverflowPopup(true); //andrew 181206: not reliable in mobile env
				if(isMobile){
					((Toolbar) dToolBar).setAlign("start");
				}
			}
			*/
    	}
    }
	
	@Override
	public AbstractGetItemProperty getGipi(String p_sublink)
	{
		if(gipiHash == null) return(null);
		return(gipiHash.get(p_sublink));
	}
	public void setGipi(String p_sublink,AbstractGetItemProperty p_gipi)
	{
		if(gipiHash == null) {
			gipiHash = new Hashtable <String,AbstractGetItemProperty>();
		}
		if(p_gipi == null) {
			if(gipiHash.get(p_sublink) != null) {
				gipiHash.remove(p_sublink);
			}
		} 
		else {
			gipiHash.put(p_sublink,p_gipi);
		}
	}
	public void setIsMobile(boolean p_isMobile){
		isMobile = p_isMobile;
	}
	protected void mapSubLink( Vector subCols, CellCollection fds , BiResult sl, int j)
	{
		for(int k=0;k<subCols.size();k++) {
			BiColumn cl = (BiColumn) subCols.get(k);
			Cell ce = fds.testCell(cl.getLabel());

			if(ce != null) {
				if(ce.getType() == Cell.VTYPE_INT) {
					if(cl.getFormat() != null && !cl.getFormat().trim().equals("")) {
						ce.map(jxAdd(sl.getView().getName()+"_"+cl.getLabel()+"_"+j,JxField.FTYPE_INT,cl.getFormat()));
					} else {
						ce.map(jxAdd(sl.getView().getName()+"_"+cl.getLabel()+"_"+j,JxField.FTYPE_INT,0));
					}
				} else if(ce.getType() == Cell.VTYPE_DOUBLE) {
					if(cl.getFormat() != null && !cl.getFormat().trim().equals("")) {
						ce.map(jxAdd(sl.getView().getName()+"_"+cl.getLabel()+"_"+j,JxField.FTYPE_FLOAT,cl.getFormat()));
					} else {
						ce.map(jxAdd(sl.getView().getName()+"_"+cl.getLabel()+"_"+j,JxField.FTYPE_FLOAT,0));
					}
				} else if(ce.getType() == Cell.VTYPE_DATETIME) {
					ce.map(jxAdd(sl.getView().getName()+"_"+cl.getLabel()+"_"+j,JxField.FTYPE_DATETIME,0));
				} else if(ce.getType() == Cell.VTYPE_DATE) {
					ce.map(jxAdd(sl.getView().getName()+"_"+cl.getLabel()+"_"+j,JxField.FTYPE_DATE,0));
				} else
					ce.map(jxAdd(sl.getView().getName()+"_"+cl.getLabel()+"_"+j,JxField.FTYPE_STRING,0));
			}
		}
	}
	
	/***
	 * called by bindCellCollection for define custom item property
	 * 
	 * @param p_br
	 * @param mode
	 * @return
	 */
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return null;
	}		
	
	/***
	 * bind data to form
	 * - call after afterBind()
	 * - call multiple times
	 * @param c
	 * @param mode
	 */
	
	@Override
	public void bindSublinkList(JxField sv , BiResult sl) {
		Vector subCols = sl.getListColumns();
		sv.clear();
		if(getGipi(sl.getView().getName()) != null) {
			sv.setItemListInterface(getGipi(sl.getView().getName()));
		} else {
		sv.gridSetRow(sl.getRowCount());
		for(int j = 0;j<sl.getRowCount();j++) {
			CellCollection fds = (CellCollection) sl.getRowCollectionV(j);
			if (isMobile){ //TODO: rendering related code move to JxZkListbox render is better
				Listcell lc = new Listcell();
				Div div = new Div();
				for(int k=0;k<subCols.size();k++) {
					BiColumn cl = (BiColumn) subCols.get(k);
					Cell ce = fds.testCell(cl.getLabel());
					Div divRow = new Div();
					Div divCellLabel = new Div();
					Div divCellValue = new Div();
					divRow.setStyle("display:table-row;");
					divCellLabel.setStyle("display:table-cell;height:25px;text-align:left;padding-right:10px;padding-bottom:5px;white-space:nowrap;color:#888");
					divCellValue.setStyle("display:table-cell;text-align:left;");
					divRow.appendChild(divCellLabel);
					divRow.appendChild(divCellValue);
					div.appendChild(divRow);
					divCellLabel.appendChild(new Label(cl.getEngName()));
					if (ce != null)
						divCellValue.appendChild(new Label(ce.getString()));
				}
				lc.appendChild(div);
				sv.gridSetValue(0, j, lc);
			}
			else{
				for(int k=0;k<subCols.size();k++) {
					BiColumn cl = (BiColumn) subCols.get(k);
					Cell ce = fds.testCell(cl.getLabel());
					if(ce != null) {
						sv.gridSetValue(k, j, ce);
					}
				}
			}
		}
		}	
	}
	public void bindSublinkList2(JxField sv , BiResult sl) {
		Vector subCols = sl.getListColumns();
		sv.clear();
		sv.gridSetRow(sl.getRowCount());
		BiCellCollection fds = sl.getCurrentCollection();
		if(fds == null) {
			fds = sl.newRowCollection();
			sl.setCurrentCollection(fds);
		}
		for(int j = 0;j<sl.getRowCount();j++) {
			sl.loadOneRecV(j);
			for(int k=0;k<subCols.size();k++) {
				BiColumn cl = (BiColumn) subCols.get(k);
				Cell ce = fds.testCell(cl.getLabel());
				if(ce != null) {
					sv.gridSetValue(k, j, ce.getObject());
				}
			}
		}
	}
	public void bindCellCollection(BiResult c,int mode) {
		
		//set custom gipi
		c.beforeBind();
		List<BiGetItemProperty> customGipiList = getCustomItemPropertyList(c, mode);
		if (customGipiList != null){
			for (BiGetItemProperty ip : customGipiList){
				if(getGipi(ip.getBiResult().getView().getName()) == null) {
					if(c.getView().linkNoAddUpDate(ip.getBiResult().getView())) {
						ip.setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
					}					
					setGipi(ip.getBiResult().getView().getName(), ip);
				}
			}
		}
		
		//currentMode = mode;
		ZkBiGetItemProperty.useGetItemPropertyForSubLinks(c,this);
		changeMode(c, mode, curComp);
//		recordAddedFlag = false; //TODO: should moved to init block
		abortFlag = false; //TODO: should moved to init block
		
		if(c.getParent() == null) {
			br = c;
			setDirtyFlag(false);
		}
		Vector cols = c.getColumns();
		for(int i = 0;i<cols.size();i++) {
			BiColumn cl = (BiColumn) cols.get(i);
			if(c.isVirtualMasterColumn(cl)) continue;
			Cell ce = c.getCell(cl.getLabel());
			if(ce != null) {
				if(ce.getType() == Cell.VTYPE_INT) {
					if(cl.getFormat() != null && !cl.getFormat().trim().equals("")) {
						ce.map(jxAdd(cl.getLabel(),JxField.FTYPE_INT,cl.getFormat()));
					} 
					else {
						ce.map(jxAdd(cl.getLabel(),JxField.FTYPE_INT,0));
					}
				} 
				else if(ce.getType() == Cell.VTYPE_DOUBLE) {
					if(cl.getFormat() != null && !cl.getFormat().trim().equals("")) {
						ce.map(jxAdd(cl.getLabel(),JxField.FTYPE_FLOAT,cl.getFormat()));
					} else {
						ce.map(jxAdd(cl.getLabel(),JxField.FTYPE_FLOAT,0));
					}
				} 
				else if(ce.getType() == Cell.VTYPE_DATE){
					ce.map(jxAdd(cl.getLabel(),JxField.FTYPE_DATE,0));
				}
				else if(ce.getType() == Cell.VTYPE_BOOLEAN){
					ce.map(jxAdd(cl.getLabel(),JxField.FTYPE_BOOLEAN,0));
				}
				else{
					ce.map(jxAdd(cl.getLabel(),JxField.FTYPE_STRING,0));
				}
				try {
					if(mode == MODE_ADD) {
						//						if(cl.isNoEntry()) ce.setMode(Cell.VMODE_DISPONLY);  else ce.setMode(Cell.VMODE_PROTECTED);
						if(cl.isNoEntry(getSessionHelper())) 
							ce.setMode(Cell.VMODE_DISPONLY);  
						/*
						else if(cl.isProtected())  {
//							if(ce.getMode() != Cell.VMODE_OVERRIDED) ce.setMode(Cell.VMODE_PROTECTED);  
							// cell.isOverride
							ce.setMode(Cell.VMODE_PROTECTED);  
						} else 
							ce.setMode(Cell.VMODE_NORMAL);
						*/
					} else if(mode == MODE_UPDATE) {
						//						if(cl.isNoUpdate()) ce.setMode(Cell.VMODE_DISPONLY);  else ce.setMode(Cell.VMODE_PROTECTED);
						if(cl.isNoUpdate(getSessionHelper())) {
							ce.setMode(Cell.VMODE_DISPONLY);  
							
						}; /* else if(cl.isProtected()) {
//							if(ce.getMode() != Cell.VMODE_OVERRIDED) ce.setMode(Cell.VMODE_PROTECTED);  
							// cell.isOverride
							ce.setMode(Cell.VMODE_PROTECTED);  
						} else  {
							ce.setMode(Cell.VMODE_NORMAL);
						}
							*/
					}
					if(cl.getColumnType().equals("combobox") && ce.getMode() != Cell.VMODE_DISPONLY) {
						if(cl.getField() != null && cl.getField().getTable() == c.getView().getTable() && cl.getField().getFieldType().equals("char") && cl.isSelfPick()) {
							UniLog.log("Sellpick");
							HashSet<Comparable> pl = cl.getSelPickList(c);
							Vector il = new Vector();
							for(Comparable cc : pl) {
								il.add(cc.toString());
							}
							JxField jxf = jxAdd(cl.getLabel());
							if(jxf != null) {
								jxf.setItemList(il);
							}
						}
					}
					if (cl.getAllowPickOldInput() && cl.getColumnType().equals("char") && !cl.isInvisible(sessionHelper)) {
						JxField fd = jxAdd(cl.getLabel());
						Component comp = (Component) fd.getNativeObject();
						if (comp instanceof ZkJxQueryInput && StringUtils.equals((String)comp.getAttribute("isQueryInputComp"), "Y")) {
							((ZkJxQueryInput)comp).clearStringListboxSelection();
						}
					}
				} 
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		if(zkcb != null && zkcb.getVisibleColumns(c) != null) {
			HashSet<BiColumn> vc = zkcb.getVisibleColumns(c);
			Component rootWin = (Component) getNativeComponent();
			for(BiColumn bc : c.getColumns()) {
				JxField jf = jxAdd(bc.getLabel());
				if(jf != null) {
					boolean vis = vc.contains(bc);
						Div div = (Div) rootWin.getFellowIfAny("compdiv_"+bc.getLabel(),true);
						if(div != null) {
							if (ZkUtil.matchesSelector(div.getParent(), ".zkbifdiv2-compdiv"))
								div.getParent().getParent().setVisible(false);
							else
								div.getParent().setVisible(false);
						} else jf.setVisible(vis);
				}
			}
		}
		Vector links = c.getSubLinks();
		if(links != null) {
			for(int i = 0;i<links.size();i++) {
				final BiResult sl = (BiResult) links.get(i);
				if(c.getView().linkAutoExpand(sl.getView())) {
					UniLog.log("Sublink " + sl.getView().getName() + " set to autoexpand , bindCellCollection to subLink instead of map to listbox");
					bindCellCollection(sl,mode);
					continue;
				}
				Vector subCols = sl.getListColumns();
				
				//UniLog.log("subCols("+i+") = " + subCols);
				JxField sv = jxAdd("link_"+sl.getView().getName());
				if(sv != null) {
					UniLog.log("set column: link_" + sl.getView().getName());
					sv.realizeTemplate("template_"+sl.getView().getName(), sl.getRowCount());
					for(int j = 0;j<sl.getRowCount();j++) {
						CellCollection fds = sl.getRowCollectionV(j);
						for(int k=0;k<subCols.size();k++) {
							BiColumn cl = (BiColumn) subCols.get(k);
							Cell ce = fds.testCell(cl.getLabel());
							mapSubLink( subCols, fds , sl, j);
						}
					}
				} 
				else {
//					sv = jxAdd("list_"+sl.getView().getName().replace(".", "_"));
					sv = jxAdd("list_"+replaceViewName(sl.getView().getName()));
					if(sv != null){
						UniLog.log("set column: list_" + replaceViewName(sl.getView().getName()) +" isMobile:"+isMobile);
						if (isMobile){
							if(getGipi(sl.getView().getName()) != null) {
								if(useMobileLink(isMobile,"Y".equals(jxGetParameter("useCompDiv")))) {
									sv.setAttribute("mode", "nocheckmark");
									sv.setAttribute("mobileView", "on");
								} else {
									if(c.getView().linkAllowAdd(sl.getView(),c.getSessionHelper()) ||
										c.getView().linkAllowUpdate(sl.getView(),c.getSessionHelper()) ||
										c.getView().linkAllowRemove(sl.getView(),c.getSessionHelper())
											){
										sv.setAttribute("mode", "checkmarkcol");
									}
								}
								if(c.getView().linkAllowRemove(sl.getView(),c.getSessionHelper())) {
									sv.setAttribute("mode", "canDelete");
								}
								if(c.getView().linkAllowUpdate(sl.getView(),c.getSessionHelper())) {
									if(getGipi(sl.getView().getName()) == null) {
										sv.setAttribute("mode", "canUpdate");
									}
								}	
								if(c.getView().linkAllowAdd(sl.getView(),c.getSessionHelper())) {
									if(getGipi(sl.getView().getName()) != null) {
										sv.setAttribute("mode", "canInsert");
									}
								}
							} else {
								sv.gridSetCol(1);
								sv.gridSetColHeader(0, "");
								sv.gridSetColWidth(0, "100%");
							}
						}
						else{
							if(displayOnlyWhenUpdate) {
							if(c.getView().linkNoAddUpDateDelete(sl.getView()) || mode ==  MODE_DISPLAY
									) {
								AbstractGetItemProperty gipi = getGipi(sl.getView().getName());
								if(gipi instanceof BiGetItemProperty) {
									((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
								}
							} else {
								AbstractGetItemProperty gipi = getGipi(sl.getView().getName());
								if(gipi instanceof BiGetItemProperty) {
									((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_INPUT);
								}
							}
							}
							/*
							if(getSessionHelper().useCompDiv()) {
								UniLog.log("useCompDiv is true : skip doubleClickAction mode in sublink");
							} else {
							}
							*/
							sv.setAttribute("mode", "doubleClickAction");
							if(c.getView().linkAllowAdd(sl.getView(),c.getSessionHelper()) ||
									c.getView().linkAllowUpdate(sl.getView(),c.getSessionHelper()) ||
									c.getView().linkAllowRemove(sl.getView(),c.getSessionHelper())
											){
								sv.setAttribute("mode", "checkmarkcol");
							}
								
//							if((!displayOnlyWhenUpdate || mode == MODE_ADD)) {
//								jxSetVisible("btupd_list_"+replaceViewName(sl.getView().getName()),false);
//							} else {
//								jxSetVisible("btupd_list_"+replaceViewName(sl.getView().getName()),true);
//							}
							if((mode != MODE_DISPLAY) && c.getView().linkAllowRemove(sl.getView(),c.getSessionHelper())) {
								sv.setAttribute("mode", "canDelete");
							} else {
								sv.setAttribute("mode", "noDelete");
							}
							if((mode != MODE_DISPLAY) && c.getView().linkAllowUpdate(sl.getView(),c.getSessionHelper())) {
								if(getGipi(sl.getView().getName()) == null) {
									sv.setAttribute("mode", "canUpdate");
								}
							}

							if(getGipi(sl.getView().getName()) != null) {
								if((mode != MODE_DISPLAY) && c.getView().linkAllowAdd(sl.getView(),c.getSessionHelper())) {
									sv.setAttribute("mode", "canInsert");
									jxSetVisible("btadd_list_"+replaceViewName(sl.getView().getName()),true);
								} else {
									sv.setAttribute("mode", "noInsert");
									jxSetVisible("btadd_list_"+replaceViewName(sl.getView().getName()),false);
								}
							}
							if(getGipi(sl.getView().getName()) != null) {
//								int n = getGipi(sl.getView().getName()).getColumnCount(null);
//								sv.gridSetCol(n);
//								for(int k=0;k<n;k++) {
//									AbstractGetItemProperty itemProperty = getGipi(sl.getView().getName());
//									sv.gridSetColHeader(k, itemProperty.getHeader(null,k));
//									
//									if (itemProperty.getColumnWidth(null, k) != null){
//										sv.gridSetColWidth(k, itemProperty.getColumnWidth(null, k));
//									}
//								}
							}
							else {
								sv.gridSetCol(subCols.size());
								for(int k = 0;k < subCols.size();k++) {
									BiColumn cl = (BiColumn) subCols.get(k);
									//sv.gridSetColHeader(k, sessionHelper.getLabel(cl.getEngName()));
									sv.gridSetColHeader(k, sessionHelper.getLabel(cl));
									sv.gridSetColWidth(k, BiUtil.calColumnWidth(cl,0,0,0,null));
								}
							}
						}
						
						sv.clear();
						if(getGipi(sl.getView().getName()) != null) {
							sv.setItemListInterface(getGipi(sl.getView().getName()));
						} else {
						sv.gridSetRow(sl.getRowCount());
						for(int j = 0;j<sl.getRowCount();j++) {
							CellCollection fds = (CellCollection) sl.getRowCollectionV(j);
							if (isMobile){ //TODO: rendering related code move to JxZkListbox render is better
								Listcell lc = new Listcell();
								Div div = new Div();
								for(int k=0;k<subCols.size();k++) {
									BiColumn cl = (BiColumn) subCols.get(k);
									Cell ce = fds.testCell(cl.getLabel());
									Div divRow = new Div();
									Div divCellLabel = new Div();
									Div divCellValue = new Div();
									divRow.setStyle("display:table-row;");
									divCellLabel.setStyle("display:table-cell;height:25px;text-align:left;padding-right:10px;padding-bottom:5px;white-space:nowrap;color:#888");
									divCellValue.setStyle("display:table-cell;text-align:left;");
									divRow.appendChild(divCellLabel);
									divRow.appendChild(divCellValue);
									div.appendChild(divRow);
									divCellLabel.appendChild(new Label(cl.getEngName()));
									if (ce != null)
										divCellValue.appendChild(new Label(ce.getString()));
								}
								lc.appendChild(div);
								sv.gridSetValue(0, j, lc);
							}
							else{
								for(int k=0;k<subCols.size();k++) {
									BiColumn cl = (BiColumn) subCols.get(k);
									Cell ce = fds.testCell(cl.getLabel());
									if(ce != null) {
										sv.gridSetValue(k, j, ce);
									}
								}
							}
						}
						}
						final JxField svx = sv;
						final boolean isEditable;
						if(c.getView().linkAllowUpdate(sl.getView(),c.getSessionHelper())){
							isEditable = true;
						} 
						else {
							isEditable = false;
						}
						final JxZkBiBase biBase = this;  //seems not a good idea
						sv.addActionListener(
								new JxActionListener(){
									public void actionPerformed(JxField fd) {
										UniLog.log("HAHA listbox cell selected = " + fd.getCurrentRow() + "," + fd.getCurrentCol() + " actionType" + fd.getActionType());
										switch(fd.getActionType()) {
										/*
										//obsoleted
										case JxField.ACTIONTYPE_ROWBTNCLICK:
											onRowBtnClick(fd);
											break;
										*/
										case JxField.ACTIONTYPE_CLICK:
											linkClickedAction(sl,fd.getCurrentRow(),fd.getActionType());
											break;
										case JxField.ACTIONTYPE_EDITCELLPICKED:
											int r = fd.getCurrentRow();
											if(r >= 0) onListEditRowAction(fd,fd.getCurrentCol(),r);
											break;
										case JxField.ACTIONTYPE_EDITCELLCHANGED:
											onListEditRowAction(fd,fd.getCurrentCol(),fd.getCurrentRow());
											break;
										case JxField.ACTIONTYPE_EDITCELLREALIZED:
											onListEditRowAction(fd,fd.getCurrentCol(),-1);
											break;
										case JxField.ACTIONTYPE_UPDATE:
										case JxField.ACTIONTYPE_DOUBLECLICK:
											if(isEditable) {
												UniLog.logm(this,"change to edit mode");
												ReturnMsg rtnMsg = beforeUpdateLink(sl,0);
												if(rtnMsg != null && !rtnMsg.getStatus()) {
													messageBox(rtnMsg.getMsg());
													return;
												}
												
												fd.gridSetDataFormat(-1,fd.getCurrentRow(),"editmode");
												setDirtyFlag(true);
											} else {
												linkClickedAction(sl,fd.getCurrentRow(),fd.getActionType());
											}
											break;
										case JxField.ACTIONTYPE_DELETE:
											int cc = svx.getCurrentRow();
											if(cc >= 0) {
//												Object o = sl.getTrStatObj(new Integer(cc));
												Object o = svx.getValue();
												svx.gridSetCurrentRow(-1);
												if (o == null) {
													UniLog.log("object not found");
													return;
												}
												int trIdx = sl.getResultStat().indexOf(o);
												if(sl.isMarkedDelete(o)) {
													ReturnMsg rtnMsg = beforeUnDeleteLink(sl,trIdx);
													if(rtnMsg != null && !rtnMsg.getStatus()) {
														messageBox(rtnMsg.getMsg());
														return;
													}
													if(!sl.markDelete(o, false)) {
														UniLog.log("markDelete(false) fail");
														return;
													}
													svx.gridSetDataFormat(-1,cc,"remove_deleted");
													afterUnDeleteLink(sl,cc);
													setDirtyFlag(true);
												} 
												else {
													ReturnMsg rtnMsg = beforeDeleteLink(sl,trIdx);
													if(rtnMsg != null && !rtnMsg.getStatus()) {
														messageBox(rtnMsg.getMsg());
														return;
													}
													if(!sl.markDelete(o, true)) {
														UniLog.log("markDelete(true) fail");
														return;
													}
													svx.gridSetDataFormat(-1,cc,"add_deleted");
													afterDeleteLink(sl,cc);
													setDirtyFlag(true);
												}
											}
											
											break;
										case JxField.ACTIONTYPE_PICKINPUTOPENED:
											 linkDetailPickInputOpened(sl, fd);
											break;
										case JxField.ACTIONTYPE_PICKINPUTCLOSED:
											 linkDetailPickInputClosed(sl, fd);
											break;
										case JxField.ACTIONTYPE_INSERT:
											UniLog.logm(this,"action insert %d",svx.getCurrentRow());
											//listboxAddRow(biBase, sl, svx, fd, -1);
											//listboxAddRow(biBase, sl, svx, fd, svx.getCurrentRow()); //has init problem, investigating
											int insIdx = svx.getCurrentRow() + 1; //insert after last record
											insIdx = sl.getResultStat().indexOf(svx.getValue())+1;
											ReturnMsg rtn = listboxAddRow(biBase, sl, svx, fd, insIdx);
											if(!rtn.getStatus()) {
												messageBox(rtn.getMsg());
											}
											break;
										default:
											UniLog.logm(this,"unknown actiontype %d", fd.getActionType());
											break;
										}
									}
								}
							);
						sv.gridSetDataFormat(-1,-1,"sizableHeader");
						if (!isMobile){
//							new JxFieldAction("btdel_list_"+sl.getView().getName().replace(".", "_")) {  //}
							new JxFieldAction("btdel_list_"+ replaceViewName(sl.getView().getName())) {
								public void actionPerformed(JxField fd) 
								{
									UniLog.log("link delete pressed");
									int cc = svx.getCurrentRow();
									svx.gridSetCurrentRow(-1);
									if(cc >= 0) {
										Object o = sl.getTrStatObj(new Integer(cc));
										if(o != null && sl.isMarkedDelete(o)) {
											ReturnMsg rtnMsg = beforeUnDeleteLink(sl,cc);
											if(rtnMsg != null && !rtnMsg.getStatus()) {
												messageBox(rtnMsg.getMsg());
												return;
											}
											if(!sl.markDelete(o, false)) return;
											svx.gridSetDataFormat(-1,cc,"remove_deleted");
										} 
										else {
											ReturnMsg rtnMsg = beforeDeleteLink(sl,cc);
											if(rtnMsg != null && !rtnMsg.getStatus()) {
												messageBox(rtnMsg.getMsg());
												return;
											}
											if(!sl.markDelete(o, true)) return;
											svx.gridSetDataFormat(-1,cc,"add_deleted");
										}
									}
								}
							};
						}
//						JxField addListBtn = jxAdd("btadd_list_"+sl.getView().getName().replace(".", "_"));
						JxField addListBtn = jxAdd("btadd_list_"+replaceViewName(sl.getView().getName()));
						if(addListBtn != null) {
							//addListBtn.addActionListener(genListboxAddActionListener(this, sl, svx, INS_IDX_APPEND));
							addListBtn.addActionListener(genListboxAddActionListener(this, sl, svx, 0)); //insert at first
						}
					}
				}
				JxField slv = jxAdd("list_"+JxZkBiBase.replaceViewName(sl.getView().getName()));
				if(slv != null) {
				if(sessionHelper.getMaxDetailRow() > 0 && sl.getRowCount() > sessionHelper.getMaxDetailRow()) {
					slv.setAttribute("paging", "withfilter");
				} else {
					slv.setAttribute("nopaging",null);
				}
				}
			}
		}
		/*
		if(!br.allowUpdate()) {
			jxSetVisible("btUpdate",false);
		}
		*/
		if(c.getParent() == null) {
		if(LOCK_RECORD_FOR_UPDATE){
			JxField fd = jxAdd("btUpdate");
			if(fd != null) {
				((Button) fd.getNativeObject()).setImage("images/icons/zkweb/040-file-5-20x20.png");
			}
			fd = jxAdd("btAdd");
			if(fd != null) {
				((Button) fd.getNativeObject()).setImage("images/icons/zkweb/040-file-5-20x20.png");
			}
		}
		}
//		modalForm();
//		if(isAddModeWhenOpen && mode == MODE_UPDATE) setDirtyFlag(true);
		jxSetEnable("btDelCurrent", false);
		if(mode == MODE_DISPLAY) {
			try {
				c.getCurrentCollection().lock();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		} 
		if(mode == MODE_UPDATE) {
			jxSetEnable("btDelCurrent", c.allowDelete()); 
		}
		
		if(zkcb != null) {
			jxSetEnable("btNext",zkcb.hasNextRec() == null || zkcb.hasNextRec() == true);
			jxSetEnable("btPrevious",zkcb.hasPrevRec() == null || zkcb.hasPrevRec() == true);
		}
		
		if(bahHash != null) {
			for(String bt : bahHash.keySet()) {
				BiActionHandler bah = bahHash.get(bt);
				jxSetVisible(bt, bah.isVisible(c, false));
				jxSetEnable(bt, !bah.isDisabled(c, false));
			}
		}
	}
	

	private ComponentsFocusManage mComponentsFocusManage;
	/**
	 * Change detail form based of mode
	 * @param p_result
	 * @param p_mode
	 * @param p_dWin
	 */
	private void changeMode(BiResult p_result, int p_mode, Component p_dWin){
		UniLog.log("changeMode curMode:" + curMode + ",p_mode:" + p_mode);
		if (p_dWin == null) {
			UniLog.logm(this, "dWin is null, ignore change mode");
			return;
		}
		if (mComponentsFocusManage == null)
			mComponentsFocusManage = new ComponentsFocusManage(sessionHelper);
		if (curMode == p_mode){
    		if (p_mode == MODE_ADD)
    			mComponentsFocusManage.setFocusFirstComp(p_dWin);
			return;
		}
		curMode = p_mode;
		
		mComponentsFocusManage.clear();
		mComponentsFocusManage.collectAllTabComps(p_dWin);
		//add mode - disable inplace
		//update mode - enable inplace
		Vector resultColumns = p_result.getColumns();
		for (int i=0; i<resultColumns.size(); i++){
		    BiColumn biColumn = (BiColumn) p_result.getColumns().elementAt(i);
		    Component childComp = p_dWin.getFellowIfAny(biColumn.getLabel());
		    if (childComp != null) { 
		    	if(
		    			(p_mode == MODE_ADD && !biColumn.isNoEntry(getSessionHelper())) ||
		    			(p_mode == MODE_UPDATE && !biColumn.isNoUpdate(getSessionHelper()))
		    			)  {
//		    		setPlaceholder(biColumn, childComp);
		    		setPlaceholder(p_result.isRequired(biColumn), childComp);
		    	}
		    	mComponentsFocusManage.addComponent(childComp);
		    }
		}
		if (mComponentsFocusManage.process()) {
			//When add record, set focus to first input element which is editable. This changes apply for add new record only.
    		if (p_mode == MODE_ADD)
    			mComponentsFocusManage.setFocusFirstComp(p_dWin);
    		//Add focus to next input element logic when user choose a option or enter deleted. This apply to both add record and update record.
    		mComponentsFocusManage.setFocusNextComp();
		}
	}
	
//	private void setPlaceholder(BiColumn p_col, Component p_comp) {
	private void setPlaceholder(boolean p_required, Component p_comp) {
		/*
		if (!p_col.isRequired()) {
			return;
		}
		*/
		if(!p_required) return;
		if (p_comp instanceof InputElement) {
			InputElement ie = (InputElement) p_comp;
			if (!ie.isDisabled()) {
				ie.setPlaceholder(sessionHelper.getLabel("*required"));
			}
		}
    	if (p_comp instanceof HtmlBasedComponent) {  //for non-standard input. e.g. select2 listbox
    		p_comp.setAttribute("placeholder", sessionHelper.getLabel("*required"));
    	}
	}
	
//	protected ReturnMsg processAddXX(){
//		return(processAddXX(addAndCloseFlag));  //default add and close
//	}
	protected ReturnMsg processAdd(int p_afterAddAction){
		ReturnMsg rtnMsg;
//		if (recordAddedFlag){
//			return(new ReturnMsg(false,"Record Added Already"));
//		}
		if (!checkBr()) return(new ReturnMsg(false,ZkUtil.joinStringLabel(sessionHelper, "\n", "Add Record Failed", null, "No Current Record", null)));
			
		if(!(rtnMsg = beforeAdd(br)).getStatus()) {
			return(rtnMsg);
		}
		rtnMsg = br.addCurrent();
		int addedSid = 0;
		if((rtnMsg == null || rtnMsg.getStatus())) {
			addedSid = (Integer) rtnMsg.getData();
			rtnMsg = afterAdd(br);
		}
		if(rtnMsg == null || rtnMsg.getStatus()) {
			if(br.inBeginWork()) {
				try {
					br.commitWork();
					needRefreshFlag = true;
				} catch (Exception ex) {
					UniLog.log(ex);
					rtnMsg = new ReturnMsg(false,ex.toString(),true);
				}
			} else {
				needRefreshFlag = true;
			}
			//zkcb.biBaseRefresh(br);  ///better not refresh the br
		}
		if(rtnMsg != null && !rtnMsg.getStatus()) {
			if(rtnMsg.isFatal()) {
				if(br.inBeginWork()) br.rollbackWork();
				br.clearCurrentRec();
				bindCellCollection(br,curMode);
			}
			//return(new ReturnMsg(false,"Add Record Failed\n" + rtnMsg.getMsg()));
			UniLog.log("addCurrent error " + " fatal ? " + rtnMsg.isFatal() + " " + rtnMsg.getMsg() + " data " + rtnMsg.getData());
			return(new ReturnMsg(false,sessionHelper.getLabel("Error !!! Add Record Failed") + ":-\n" + rtnMsg.getMsg() + ((sessionHelper.isAdminUser() && rtnMsg.getData() != null )? rtnMsg.getData().toString() : "")));
		}
		
		//afterUpdate(br);   //required to call afterUpdate??
		// afterAdd(br);  // 2020/11/30 moved to do before commitwork
		setDirtyFlag(false);
		ReturnMsg rtn;
		switch(p_afterAddAction) {
		case	AFTERADDUPDATE_ACTION_CLOSE:
			doClose(false);
            return(new ReturnMsg(true,sessionHelper.getLabel("Record Added")));
		case	AFTERADDUPDATE_ACTION_RELOAD:
		{
			if(addedSid > 0) {
				BiView view = br.getView();
                br.clearCondition();
                br.addCustomCondition("serial_id = " + addedSid);
//                br.appendWherecl(new Wherecl().andUniop(view.getTable().getSidField(), "=", addedSid));
                br.query(true);
                if(br.getRowCount() >0) {
					br.fetchOneRecV(0);
					br.clearLastUpdate();
					//initForm(MODE_UPDATE);
					bindCellCollection(br,MODE_UPDATE);
					initForm(MODE_UPDATE); //andrew190821: initForm() call after bindCellCollection()
					needRefreshFlag = true;
                }
                rtn = new ReturnMsg(true,sessionHelper.getLabel("Record Added"));
                rtn.setData(addedSid);
                return(rtn);
			}  else {
				doClose(false);
				return(new ReturnMsg(true,sessionHelper.getLabel("Record Added")));
			}
		}
		default :
		{
			rtn = new ReturnMsg(true);
			if(addedSid > 0) {
				rtn.setData(addedSid);
			}
			return(rtn);
		}
		}
	}
//	protected ReturnMsg processAddXX(boolean p_addAndCloseFlag){
//		ReturnMsg rtnMsg;
////		if (recordAddedFlag){
////			return(new ReturnMsg(false,"Record Added Already"));
////		}
//		if (!checkBr()) return(new ReturnMsg(false,"Add Record Failed\nNo Current Record"));
//			
//		if(!(rtnMsg = beforeAdd(br)).getStatus()) {
//			return(rtnMsg);
//		}
//		rtnMsg = br.addCurrent();
//		if(rtnMsg == null || rtnMsg.getStatus()) {
//			if(br.inBeginWork()) {
//				br.commitWork();
//			}
//			needRefreshFlag = true;
//			//zkcb.biBaseRefresh(br);  ///better not refresh the br
//		}
//		else {
//			if(rtnMsg.isFatal()) {
//				if(br.inBeginWork()) br.rollbackWork();
//				br.clearCurrentRec();
//				bindCellCollection(br,curMode);
//			}
//			//return(new ReturnMsg(false,"Add Record Failed\n" + rtnMsg.getMsg()));
//			UniLog.log("addCurrent error " + " fatal ? " + rtnMsg.isFatal() + " " + rtnMsg.getMsg() + " data " + rtnMsg.getData());
//			return(new ReturnMsg(false,"Error !!! Add Record Failed:-\n" + rtnMsg.getMsg() + ((sessionHelper.isAdminUser() && rtnMsg.getData() != null )? rtnMsg.getData().toString() : "")));
//		}
//		
//		//afterUpdate(br);   //required to call afterUpdate??
//		afterAdd(br);
//		setDirtyFlag(false);
//		if (p_addAndCloseFlag){
//			doClose(false);
//			return(null);
//		} else {
//			int addedSid = ((Integer) rtnMsg.getData());
//			if(addedSid > 0) {
//				BiView view = br.getView();
//                br.clearCondition();
//                br.addCustomCondition("serial_id = " + addedSid);
////                br.appendWherecl(new Wherecl().andUniop(view.getTable().getSidField(), "=", addedSid));
//                br.query(true);
//                if(br.getRowCount() >0) {
//					br.fetchOneRecV(0);
//					br.clearLastUpdate();
//					//initForm(MODE_UPDATE);
//					bindCellCollection(br,MODE_UPDATE);
//					initForm(MODE_UPDATE); //andrew190821: initForm() call after bindCellCollection()
//					needRefreshFlag = true;
//                }
//			} else {
//				UniLog.log("Warning !!! addCurrent OK but serial_id not returned, close JxZkBiBase");
//				doClose(false);
//				return(null);
//				
//			}
//		}
//
//
//		return(new ReturnMsg(true,"Record Added"));
//	}
	
	/*
	protected ReturnMsg processUpdate(){
		if(defaultUpdateAndCloseFlag) {
			return(processUpdate( AFTERADDUPDATE_ACTION_CLOSE));
		}
		return(processUpdate( AFTERADDUPDATE_ACTION_CLOSE));
	}
	*/
	protected ReturnMsg processUpdate(int p_afterUpdateAction)
	{
		if (!checkBr()) return(new ReturnMsg(false,sessionHelper.getLabel("Error !!! No Current Record")));
//		boolean updateAndClose;
//		if(p_updateAndCloseFlag == null) updateAndClose = defaultUpdateAndCloseFlag; else updateAndClose = p_updateAndCloseFlag;
		ReturnMsg rtnMsg;
		if(!(rtnMsg = beforeUpdate(br)).getStatus()) {
			if (!rtnMsg.getMsg().equals("")){
				messageBox(rtnMsg.getMsg());
			}
			return(null);
		}
		rtnMsg = br.updateCurrent();
		setKeepAliveInterval(0);
		if((rtnMsg == null || rtnMsg.getStatus())) rtnMsg = afterUpdate(br);
		if((rtnMsg == null || rtnMsg.getStatus())) {
			if(zkcb != null)  {
				/*
					 Don't work , have to re-write after filter feature added
					Object o = br.getCurrentResoutStatObject();
					if(o != null) zkcb.refreshListItems(o);
				 */
				//zkcb.biBaseRefreshListitems(null);
				zkcb.biBaseRefreshListitems(getBr().getCurrentRecord());
			}

			if(br.inBeginWork()) {
				try {
					br.commitWork();
				} catch (Exception ex) {
					UniLog.log(ex);
					rtnMsg = new ReturnMsg(false,ex.toString(),true);
				}
			}
		} 
		if(rtnMsg != null && !rtnMsg.getStatus()){
			if(rtnMsg.isFatal()) {
				try {
				if(br.inBeginWork()) br.rollbackWork();
				br.refetchCurrent();
				bindCellCollection(br,curMode);
				} catch (Exception xex) {
					UniLog.log(xex);
//					throw(xex);
				}
			}
			UniLog.log("updateCurrent error " + " fatal ? " + rtnMsg.isFatal() + " " + rtnMsg.getMsg() + " data " + rtnMsg.getData());
			return(new ReturnMsg(false,sessionHelper.getLabel("Error !!! Update Record Failed") + ":-\n" + rtnMsg.getMsg() + ((sessionHelper.isAdminUser() && rtnMsg.getData() != null )? rtnMsg.getData().toString() : "")));
		}
		setDirtyFlag(false);
		
		/*
		if (updateAndClose){
			doClose(false);
			return(null);
		} else {
			br.refetchCurrent();
			bindCellCollection(br,curMode);
		}
		return(new ReturnMsg(true,"Record Updated"));
		*/
		switch(p_afterUpdateAction) {
		case	AFTERADDUPDATE_ACTION_CLOSE:
			doClose(false);
            return(null);
		case	AFTERADDUPDATE_ACTION_RELOAD:
		{
			br.refetchCurrent();
			bindCellCollection(br,curMode);
		}
		default :
		{
			ReturnMsg rtn = new ReturnMsg(true);
			return(rtn);
		}
		}
	}
	@Override
	public void beforeBind(){
		super.beforeBind();
		try{
			abHelper = (ActionButtonHelper) getSkin().getParameterObject("abHelper");
			UniLog.log1("abHelper="+abHelper);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	protected ReturnMsg reloadCurrentBySid(int p_sid, int p_mode) {
		BiView view = getBr().getView();
        getBr().clearCondition();
        getBr().addCustomCondition("serial_id = " + p_sid);
        getBr().query(true);
        if(getBr().getRowCount() >0) {
        	getBr().fetchOneRecV(0);
        	getBr().clearLastUpdate();
        	//initForm(MODE_UPDATE);
        	bindCellCollection(br,p_mode);
			translateAllComp(br); //andrew231205 fix radio button not translate bug
        	initForm(p_mode); //andrew190821: initForm() call after bindCellCollection()
        	needRefreshFlag = true;
        }
        return(ReturnMsg.defaultOk);
	}
	
	/***
	 * call after jxform bind.
	 * call one time only
	 */
	public void afterBind() {
		new JxFormClose() {
			public int formClose(JxForm p_form) {
				UniLog.log1("formClosed called");
				if(br != null) {
					BiResult r = br;
					br = null;
					r.unmapColumns();
				}
				return(JxFormCloseListener.caHide);
			}
		};
		new JxFieldAction("btNext") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				if(zkcb != null) {
					ReturnMsg rtn = zkcb.fetchNext(getBr());
					if(rtn != null && rtn.getStatus()) {
						bindCellCollection(br,curMode);
						ZkUtil.showMsg(sessionHelper.getLabel("Next Record loaded"));
					} else {
						String errMsg = sessionHelper.getLabel("No Next Record");
						if (rtn != null && StringUtils.isNotBlank(rtn.getMsg())){
							errMsg+=": "+rtn.getMsg();
						}
						ZkUtil.showErrMsg(errMsg);
					}
				}
				
			}
			
		};
		new JxFieldAction("btPrevious") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				if(zkcb != null) {
					ReturnMsg rtn = zkcb.fetchPrevious(getBr());
					if(rtn != null && rtn.getStatus()) {
						bindCellCollection(br,curMode);
						ZkUtil.showMsg(sessionHelper.getLabel("Previous Record loaded"));
					} else {
						String errMsg = sessionHelper.getLabel("No Previous Record");
						if (rtn != null && StringUtils.isNotBlank(rtn.getMsg())){
							errMsg+=": "+rtn.getMsg();
						}
						ZkUtil.showErrMsg(errMsg);
					}
				}
				
			}
			
		};
		new JxFieldAction("btEdit") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
//				getBr().refetchCurrent();
				ReturnMsg rtn = allowDisplayToUpdate();
				if(rtn != null && !rtn.getStatus()) {
					messageBox(rtn.getMsg());
					return;
				}
				try {
					getBr().getCurrentCollection().unlock();
				} catch (CellException cex) {
					UniLog.log(cex);
				}
				bindCellCollection(getBr(),JxZkBiBase.MODE_UPDATE);
				initForm(MODE_UPDATE);
				setDirtyFlag(false);
			}
			
		};
		new JxFieldAction("btUpdate") {
			public void actionPerformed(JxField fd) 
			{
				UniLog.log("Update Pressed");
				
				if (validateAddUpdate(fd, MODE_UPDATE).isBad()) { //andrew220105 perform validation before processAdd/processUpdate	
					resetAutoDisableButton(fd);
					return;
				}
				
				ReturnMsg rtnMsg = null;
				int afterUpdateAction = AFTERADDUPDATE_ACTION_NONE;
				if(defaultUpdateCloseAction == CloseAction.Reload) {
						afterUpdateAction = AFTERADDUPDATE_ACTION_RELOAD;
				}
				if (fd.getNativeObject() != null && fd.getNativeObject() instanceof Button){
//					updateAndCloseFlag = StringUtils.equals(((Button)fd.getNativeObject()).getAttribute("UPDATE_AND_CLOSE") +"","Y");
					String ss = (String) ((Button)fd.getNativeObject()).getAttribute("UPDATE_AND_CLOSE");
					if(ss != null) {
						afterUpdateAction = AFTERADDUPDATE_ACTION_CLOSE;
					}
				} 
				rtnMsg = processUpdate(afterUpdateAction);
				/*
				if(rtnMsg != null && !rtnMsg.getStatus()) {
					messageBox(rtnMsg.getMsg());
				}
				*/

				if(afterUpdateAction != AFTERADDUPDATE_ACTION_NONE) {
				if(rtnMsg != null && StringUtils.isNotBlank(rtnMsg.getMsg())){
					if(isDirty()) JxZkBiBase.this.setDirtyFlag(false);
					messageBox(rtnMsg.getMsg());
				}
				} else {
				ArrayList<ZkBiMsgboxButton> btns = new ArrayList<ZkBiMsgboxButton>();
				if(isAddModeWhenOpen) {
					if (!StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("ADD_NEXT"),"N")) {
						btns.add(new ZkBiMsgboxButton("Add Next...").setName("addNext").appendStyle("margin-right:50px"));
					}
				} 
				else {
					if (!StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("UPDATE_NEXT"),"N")) {
						btns.add(new ZkBiMsgboxButton("Next Record").setName("updateNext").appendStyle("margin-right:50px"));
					}
				}
				if (!StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("UPDATE_CONTINUE"),"N") && getBr().getView().allowUpdate(sessionHelper)) {
					btns.add(new ZkBiMsgboxButton("Continue").setName("continue"));
				}
				if (!StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("UPDATE_CLOSE"),"N")) {
					btns.add(new ZkBiMsgboxButton("Close").setName("close").setDefault());
				}
				if(rtnMsg != null && rtnMsg.getStatus()) {
					if (StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("NO_PROMPT_WITH_CONTINUE"),"Y")) {
						br.refetchCurrent();
						bindCellCollection(br,curMode);
						translateAllComp(br); //andrew231205 fix radio button not translate bug
						ZkUtil.showMsg(sessionHelper.getLabel("Record Saved"));
					} else {
						ZkBiMsgbox.show(sessionHelper.getLabel("Record Saved"), btns.toArray(new ZkBiMsgboxButton[btns.size()]), new ZkBiEventListener(){
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								if (btn.getName().equals("addNext")) {
									getBr().clearCurrentRec();
									getBr().clearLastUpdate();
									bindCellCollection(getBr(),JxZkBiBase.MODE_ADD);
									translateAllComp(br); //andrew231205 fix radio button not translate bug
									initForm(MODE_ADD);
									
								} else
								if (btn.getName().equals("updateNext")) {
									/*
									getBr().clearCurrentRec();
									getBr().clearLastUpdate();
									bindCellCollection(getBr(),JxZkBiBase.MODE_ADD);
									initForm(MODE_ADD);
									*/
									ReturnMsg rtn = zkcb.fetchNext(getBr());
									if(rtn != null && rtn.getStatus()) {
									} else {
										ZkUtil.errMsg(sessionHelper.getLabel("No Next Record") + ": ", rtn == null ? "" : rtn.getMsg());
										br.refetchCurrent();
						
									}
									bindCellCollection(br,curMode);
									translateAllComp(br); //andrew231205 fix radio button not translate bug
								}
								else if (btn.getName().equals("continue")) {
									br.refetchCurrent();
									bindCellCollection(br,curMode);
									translateAllComp(br); //andrew231205 fix radio button not translate bug
								}
								else {
									doClose(false);
								}
							}
						});
					}
						return;
				} else {
					resetAutoDisableButton(fd);
					if(rtnMsg != null && StringUtils.isNotBlank(rtnMsg.getMsg())){
						messageBox(rtnMsg.getMsg());
					}
				}
				}
			}
		};
		
		new JxFieldAction("btAdd") {
			public void actionPerformed(JxField fd) 
			{
				UniLog.log("Add Pressed");
				
				if (validateAddUpdate(fd, MODE_ADD).isBad()) { //andrew220105 perform validation before processAdd/processUpdate	
					resetAutoDisableButton(fd);
					return;
				}				
				
//				//TODO: need to verify add and not close
//				boolean addAndCloseFlag = true;  //default add and close
//				if (fd.getNativeObject() != null && fd.getNativeObject() instanceof Button){
//					addAndCloseFlag = !StringUtils.equals(((Button)fd.getNativeObject()).getAttribute("ADD_AND_CLOSE") +"","N");
//				}
//				addAndCloseFlag = false;
//				ReturnMsg rtnMsg = processAdd(addAndCloseFlag ? JxZkBiBase.AFTERADDUPDATE_ACTION_CLOSE:JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
//				if(rtnMsg != null && StringUtils.isNotBlank(rtnMsg.getMsg())){
//					messageBox(rtnMsg.getMsg());
//				}
				if(defaultAddCloseAction == CloseAction.Close) {
					ReturnMsg rtnMsg = processAdd(JxZkBiBase.AFTERADDUPDATE_ACTION_CLOSE);
					if(rtnMsg != null && StringUtils.isNotBlank(rtnMsg.getMsg())){
						messageBox(rtnMsg.getMsg());
					}
					return;
				}
				if(defaultAddCloseAction == CloseAction.Reload) {
					ReturnMsg rtnMsg = processAdd(AFTERADDUPDATE_ACTION_RELOAD);
					if(rtnMsg != null && rtnMsg.getStatus()) {
						final Integer Sid = (Integer) rtnMsg.getData();
						if(Sid != null && Sid > 0) {
							reloadCurrentBySid(Sid, MODE_UPDATE);
							return;
						} else {
							UniLog.log("Warning !!! added record with sid == null or 0");
						}
					} else {
						resetAutoDisableButton(fd);
						if(rtnMsg != null && StringUtils.isNotBlank(rtnMsg.getMsg())){
							messageBox(rtnMsg.getMsg());
						}
					}
					return;
				}
				
				ArrayList<ZkBiMsgboxButton> btns = new ArrayList<ZkBiMsgboxButton>();
				if (!StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("ADD_NEXT"),"N")) {
					btns.add(new ZkBiMsgboxButton("Add Next...").setName("addNext").appendStyle("margin-right:50px"));
				}
				if (!StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("ADD_CONTINUE"),"N") && getBr().getView().allowUpdate(sessionHelper)) {
					btns.add(new ZkBiMsgboxButton("Update Current").setName("continue"));
				}
				if (!StringUtils.equals((String)((Button)fd.getNativeObject()).getAttribute("ADD_CLOSE"),"N")) {
					btns.add(new ZkBiMsgboxButton("Close").setName("close").setDefault());
				}
				ReturnMsg rtnMsg = processAdd(AFTERADDUPDATE_ACTION_NONE);
				if(rtnMsg != null && rtnMsg.getStatus()) {
					final Integer Sid = (Integer) rtnMsg.getData();
					if(Sid != null && Sid > 0) {
						ZkBiMsgbox.show(sessionHelper.getLabel("Record Added"), btns.toArray(new ZkBiMsgboxButton[btns.size()]), new ZkBiEventListener(){
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								if (btn.getName().equals("addNext")) {
									getBr().clearCurrentRec();
									getBr().clearLastUpdate();
									bindCellCollection(getBr(),JxZkBiBase.MODE_ADD);
									translateAllComp(br); //andrew231205 fix radio button not translate bug
									initForm(MODE_ADD);
									
									
								}
								else if (btn.getName().equals("continue")) {
									reloadCurrentBySid(Sid, MODE_UPDATE);
								}
								else {
									doClose(false);
								}
							}
						});
						return;
					} else {
						UniLog.log("Warning !!! added record with sid == null or 0");
					}
					doClose(true);
				} else {
					resetAutoDisableButton(fd);
					if(rtnMsg != null && StringUtils.isNotBlank(rtnMsg.getMsg())){
						messageBox(rtnMsg.getMsg());
					}
				}
			}
		};
		new JxFieldAction("btDelCurrent") {
			public void actionPerformed(JxField fd) 
			{
				UniLog.log("Delete Current");
			    Messagebox.show("Confirm Delete ? ", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			    	     new EventListener() {
			    	       public void onEvent(Event evt) throws Exception {
			    	    	   if (((Integer)evt.getData()) == Messagebox.YES){

				ReturnMsg rtnMsg = getBr().deleteCurrent();
				if(rtnMsg != null && rtnMsg.getStatus()) {
					needRefreshFlag = true;
					doClose(true);
				} else {
					resetAutoDisableButton(fd);
					if(rtnMsg != null && StringUtils.isNotBlank(rtnMsg.getMsg())){
						messageBox(rtnMsg.getMsg());
					}
				}
			    	    	   }
			    	      }
			    	    }
			    );
				
			}
		};
		
		new JxFieldAction("btClose") {
			public void actionPerformed(JxField fd) 
			{
				
				UniLog.log("Close Pressed");
				doClose(true);
				/*if (sessionHelper.getURLParamBool("closetab")) {
					UniLog.log1("closetab");
					ZkUtil.js("closeTab()");
				}*/
			}
		};
		

		new JxFieldAction("btAttach") {

			@Override
			public void actionPerformed(JxField jxfield) {
				Component bt = (Component) jxfield.getNativeObject();
				final BiResult attResult = getBr().getSubLink((String) bt.getAttribute("biResult"));
				if(attResult != null) {
					try {
					    Fileupload.get(new EventListener <UploadEvent>(){
				    		public void onEvent(UploadEvent event) {
				        		UniLog.log("upload event catched");
				        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
				                org.zkoss.util.media.Media media = event.getMedia();
				                if(media != null) {
				                	/*
				                	if(!media.getContentType().equals("application/pdf") )
				                	{
				                		messageBox("Only Pdf File Are Accepted");
				                		return;
				                	}
				                	*/
				                	try  {
				                		((AttachmentUploadInterface) attResult).saveImageFile(new BiMedia(media) );
				                		getBr().refetchCurrent();
				                		bindCellCollection(getBr(),curMode);
				                	} catch (Exception ex) {
				                		UniLog.log(ex);
				                		messageBox(ex.toString());
				                	}
				                	
				                }
				    		}
					    });
					} catch (Exception ex) {
							UniLog.log(ex);
					}	
				}
			}
			
		};
		new JxFieldAction("btReloadDetail") {
			public void actionPerformed(JxField fd) 
			{
				UniLog.log("Reload Detail pressed");
				if (isDirty()) {
					ZkUtil.showWarnMsg(sessionHelper.getLabel("Record is updating. Reload action abort"));
					return;
				}
				getBr().refetchCurrent();
				bindCellCollection(getBr(), curMode);
				ZkUtil.showMsg(sessionHelper.getLabel("Record reloaded"));
			}
		};
		
		if (sessionHelper.getAllowRecordCopy()) {
			JxField fd = jxAdd("bgRecordCopy");
			if (fd != null) {
				final Button btn = ((Button)fd.getNativeObject());
				new JxFieldAction("bgRecordCopy") {
					@Override
					public void actionPerformed(JxField jxfield) {
						UniLog.log("Button group RecordCopy pressed");
						Events.echoEvent("onCustomClick", btn, null);
					}
				};
			}
		}
		
		//add delay click event for add/update button, avoid instant field data loss
		/*
		ActionButtonHelper.setDelayClickEventOne(((Button) jxAdd("btUpdate").getNativeObject()), sessionHelper);
		ActionButtonHelper.setDelayClickEventOne(((Button) jxAdd("btAdd").getNativeObject()), sessionHelper);
		*/
		

		addFormDirtyListener(new JxFormDirtyListener () {
			public void formDirtyFlagChanged(JxForm jxf) {
				if (!checkBr()) return;
				if(!br.allowUpdate() && !br.allowAdd()) return;
				if(isDirty()) {
					//lock and display rolling button
					if(LOCK_RECORD_FOR_UPDATE) {
						try  {
							ReturnMsg rtnMsg;
							br.beginWork();
							rtnMsg = br.lockRecordForUpdate();
							if(rtnMsg == null || rtnMsg.getStatus()) {
								rtnMsg = doAfterLockRecord(rtnMsg);
							}
							if(rtnMsg != null && !rtnMsg.getStatus()) {
								messageBox(rtnMsg.getMsg(),1, new MessageBoxActionInterface() {
									public void onButtonClicked(Object p_obj) {
										if (!checkBr()) return;
										br.rollbackWork();
										br.refetchCurrent();
										setDirtyFlag(false);
									}
								});
								return;
							}
							setKeepAliveInterval(10000);
							doKeepAlive();
							if(curMode == MODE_UPDATE){
								//((Button) jxAdd("btUpdate").getNativeObject()).setImage("images/source.gif");
								((Button) jxAdd("btUpdate").getNativeObject()).setImage("images/icons/zkweb/085-lock-20x20.png");
								((Button) jxAdd("btUpdate").getNativeObject()).setTooltiptext(
										sessionHelper.getLabel("Save Record") + "\n" + sessionHelper.getLabel("Record is currently locked and ready for save"));
							}
							else if(curMode == MODE_ADD){
								//((Button) jxAdd("btAdd").getNativeObject()).setImage("images/source.gif");
								((Button) jxAdd("btAdd").getNativeObject()).setImage("images/icons/zkweb/085-lock-20x20.png");
								((Button) jxAdd("btAdd").getNativeObject()).setTooltiptext(sessionHelper.getTtLabel("Save New Record"));
							}
						} 
						catch (Exception ex) {
							UniLog.log(ex);
							messageBox(ex.toString());
						}
					}
					
					//display updating tag
					/*
					if (curDetailDecoration != null && curDetailDecoration.grid != null){
						curDetailDecoration.grid.setSclass("rec_editing");
					}
					*/
					//display editing tag
					if (curMode == MODE_UPDATE){
						Clients.evalJavaScript("if (typeof showEditing !== 'undefined'){showEditing(true);}");
					}
					
					//set confirm close (trigger by close browser/tab)
					Clients.confirmClose("Are you sure to leave?");
					
					//enable button 
				    jxSetEnable("btUpdate",true);
				    jxSetEnable("btAdd",true);
				    jxSetEnable("btNext",false);
				    jxSetEnable("btPrevious",false);
				    jxSetEnable("btReloadDetail",false);
				}
				else{  //not dirty block
					if(curMode == MODE_UPDATE){
						((Button) jxAdd("btUpdate").getNativeObject()).setImage("images/icons/zkweb/040-file-5-20x20.png");
						((Button) jxAdd("btUpdate").getNativeObject()).setTooltiptext(sessionHelper.getTtLabel("Save Record"));
					}
					else if(curMode == MODE_ADD){
						((Button) jxAdd("btAdd").getNativeObject()).setImage("images/icons/zkweb/040-file-5-20x20.png");
						((Button) jxAdd("btAdd").getNativeObject()).setTooltiptext(sessionHelper.getTtLabel("Save New Record"));
					}
					//clear updating tag
					/*
					if (curDetailDecoration != null && curDetailDecoration.grid != null){
						curDetailDecoration.grid.setSclass("");
					}
					*/
					//clear editing tag
               		Clients.evalJavaScript("if (typeof showEditing !== 'undefined'){showEditing(false);}");
					
					//clear confirm close
					Clients.confirmClose(null);
					
					//disable button
					if(curMode == MODE_UPDATE && br.allowUpdate()) {
						jxSetEnable("btUpdate",false);
					} 
					if(curMode == MODE_ADD && br.allowAdd()) {
						jxSetEnable("btUpdate",false);
						jxSetEnable("btAdd",false);
					}
				    jxSetEnable("btNext",true);
				    jxSetEnable("btPrevious",true);
				    jxSetEnable("btReloadDetail",true);
				}
				formDirtyChanged();
			}
		});
		{
			SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
			if(jxAdd("co_logo") != null) {
				String logo = BiConfig.getCoLogo(sessionHelper,BiConfig.getDefaultCoCode(sessionHelper));
				if(logo != null) {
					jxAdd("co_logo").setText("images/" + logo);
				}
			}
			if(sessionHelper.isMobileDevice()) {
				jxSetHeight("mobile_bottom", sessionHelper.getScreenHeight()-100);
			}
		}
	}
	
	void setupExpandedViewButtons(final BiResult p_br) {
		Vector links = p_br.getSubLinks();
		if(links == null) return;
		for(int i = 0;i<links.size();i++) {
			BiResult sl = (BiResult) links.get(i);
			if(p_br.getView().linkAutoExpand(sl.getView())) {
				UniLog.log("Sublink " + sl.getView().getName() + " set to autoexpand , bindCellCollection to subLink instead of map to listbox");
				final String sn = sl.getView().getName();
				Button bt = new Button("Next "+sn);
				curComp.appendChild(bt);
				bt.addEventListener(Events.ON_CLICK, 
				new org.zkoss.zk.ui.event.EventListener(){
					@Override
					public void onEvent(Event arg0) throws Exception {
						// TODO Auto-generated method stub
						UniLog.log("Expaned SubLink " + sn + " nextrecord clicked");
						BiResult sl = p_br.getSubLink(sn);
						int cc = sl.getIndexByCollection_real(sl.getCurrentCollection());
						for(cc++; cc < sl.getRowCount();cc++) {
							Object o = sl.getTrStatObj(cc);
							if(!sl.isMarkedDelete(o)) {
								sl.fetchOneRecV(cc);
								bindCellCollection(sl,curMode);
								break;
							}
						}
					}
				}
				);
				bt = new Button("Prev "+sn);
				curComp.appendChild(bt);
				bt.addEventListener(Events.ON_CLICK, 
				new org.zkoss.zk.ui.event.EventListener(){
					@Override
					public void onEvent(Event arg0) throws Exception {
						// TODO Auto-generated method stub
						UniLog.log("Expaned SubLink " + sn + " prevrecord clicked");
						BiResult sl = p_br.getSubLink(sn);
						int cc = sl.getIndexByCollection_real(sl.getCurrentCollection());
						for(cc--; cc >= 0;cc--) {
							Object o = sl.getTrStatObj(cc);
							if(!sl.isMarkedDelete(o)) {
								sl.fetchOneRecV(cc);
								bindCellCollection(sl,curMode);
								break;
							}
						}
					}
				}
				);
			}
			setupExpandedViewButtons(sl);
		}
	}
	void addPickInputActionListner(BiResult p_br) {
		for(BiColumn bc : p_br.getColumns()) {
		    if(bc.isInvisible(sessionHelper)) continue;
		    if(bc.getPickViewName() != null) {
		    	JxField fd = jxAdd(bc.getLabel());
		    	Component comp = (Component) fd.getNativeObject();
		    	if(comp instanceof ZkJxPickInput) {
		    		if(fd.getActionListner() == null) fd.addActionListener(
		    					new JxActionListener() {

									@Override
									public void actionPerformed(JxField field) {
										// TODO Auto-generated method stub
										if(field.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
											try {
												doPickInputByJxField(field);
											} catch (Exception ex ) { 
												UniLog.log(ex);
											}
										}
									}
		    						
		    					}
		    				);
		    	}
		    }
		}
		setupExpandedViewButtons(p_br);
	}
//	void addPickQueryActionListner(BiResult p_br) {
//		if (!sessionHelper.getAllowPickOldInput())
//			return;
//		for(BiColumn bc : p_br.getColumns()) {
//		    if(bc.isInvisible(sessionHelper)) continue;
//	    	JxField fd = jxAdd(bc.getLabel());
//	    	Component comp = (Component) fd.getNativeObject();
//	    	if (comp instanceof ZkJxQueryInput && StringUtils.equals((String)comp.getAttribute("isQueryInputComp"), "Y")) {
//	    		((ZkJxQueryInput)comp).setEventListenerCallback(new ZkJxQueryInput.EventListenerCallback() {
//					@Override
//					public void callback(Event event) throws Exception {
//						UniLog.log1("event:%s", event);
//						setDirtyFlag(true);
//					}
//	    		});
//	    	}
//		}
//	}
	/***
	 * validate br object
	 * 
	 * @return
	 */
	protected void doPickInputByJxField(JxField p_field) throws Exception {
		final ColumnCell ccell = getBr().getCell(p_field.getName());
		if(ccell != null) {
			BiColumn bc = ccell.getBiColumn();
			String pv = bc.getPickViewName();
			if(pv != null) {
//				JxZkBiBase.pickBySelect(sessionHelper,pv,null, new ZkBiEventListener() {
				JxZkBiBase.pickBySelect(sessionHelper,pv,getBr().getPickColumnCondition(ccell), new ZkBiEventListener() {

					@Override
					public void onZkBiEvent(Event arg0) throws Exception {
						CellCollection col = (CellCollection) arg0.getData();
						String pcl = ccell.getBiColumn().getPickColName();
						if(pcl == null) pcl = ccell.getCellLabel();
						Object o = col.getCell(pcl).getObject();
						ccell.set(o);
						afterPickField(pcl);
					}
					
					}
				);
				
			}
		}
	}
	protected boolean checkBr() {
		if (br == null) {
			UniLog.log1(1,"sth wrong. br is null");
			return false;
		}
		return true;
	}
	
	private boolean isShowDialog = false;
	public void doClose(boolean p_withDialog){
		doClose(p_withDialog, false);
	}
	public void doClose(boolean p_withDialog, final boolean p_needCloseParentWindow){
		if (!checkBr()) return;
		if (p_withDialog && isDirty()){
			if (isShowDialog)
				return;
			isShowDialog = true;
			Messagebox.show(sessionHelper.getLabel("Are you sure to leave this page?"), 
			    sessionHelper.getLabel("Confirmation"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			        //new org.zkoss.zk.ui.event.EventListener(){  ...}
			        new ZkBiEventListener(){
			            public void onZkBiEvent(Event e){
			            	isShowDialog = false;
			            	if (!checkBr()) return;
			                if(Messagebox.ON_OK.equals(e.getName())){
			                	setKeepAliveInterval(0);
			                	if(br.inBeginWork()) br.rollbackWork();
			                	BiResult lastBr = br;
			                	if (needRefreshFlag && zkcb != null){
			                		zkcb.biBaseRefresh(br);
			                		needRefreshFlag = false; 
			                	}
			                	isAddModeWhenOpen = false;
			                	//Clients.evalJavaScript("zkbis2.destroyAll()");
			                	closeForm();
			                	/*
			                	if(zkcb.getComposerAction().equals("add")|| zkcb.getComposerAction().equals("update")) {
			                		Clients.evalJavaScript("goBack()");
			                		// currently hardcoded to the main menu url, should find someway to generalize this 
			                		//Executions.getCurrent().sendRedirect("menu.html?menuid=menu_main.html&menuitem=SITEMAP");
			                	} else {
			                		zkcb.showListPanel();
			                	}
			                	*/
			                	//clear editing tag
		                		Clients.evalJavaScript("if (typeof showEditing !== 'undefined'){showEditing(false);}");
		                		Clients.confirmClose(null);
		                		abHelper.removeActionButtonMenu();
			                	if (zkcb != null) {
			                		//zkcb.biBaseRefreshListitems(null);
//			                		zkcb.biBaseRefreshListitems(lastBr.getCurrentRecord());
			                		zkcb.biBaseClose(lastBr);
			                	}
			                	if (p_needCloseParentWindow)
			                		Clients.evalJavaScript("closeParentWindow()");
			                	if (sessionHelper.getURLParamBool("closetab")) {
				                	UniLog.log1("closetab");
				                	ZkUtil.js("closeTab()");
			                	}
			                }
			            }
			        }
			    );	
		}
		else{
			BiResult lastBr = br;
			if (needRefreshFlag && zkcb != null){
				zkcb.biBaseRefresh(br);
				needRefreshFlag = false; 
			}
			isAddModeWhenOpen = false;
            //Clients.evalJavaScript("zkbis2.destroyAll()");
			closeForm();
			/*
			if(zkcb.getComposerAction().equals("add") || zkcb.getComposerAction().equals("update")) {
				//Clients.evalJavaScript("goBack()");
		        //currently hardcoded to the main menu url, should find someway to generalize this
				//Executions.getCurrent().sendRedirect("menu.html?menuid=menu_main.html&menuitem=SITEMAP");
				//Clients.evalJavaScript("history.back(-1)"); 
				zkcb.nextComposerAction();
			    zkcb.showListPanel();
			} else {
			    zkcb.showListPanel();
			}
			*/
			//clear editing tag
       		Clients.evalJavaScript("if (typeof showEditing !== 'undefined'){showEditing(false);}");
       		Clients.confirmClose(null);
       		abHelper.removeActionButtonMenu();
           	if (zkcb != null && lastBr != null){
           		//zkcb.biBaseRefreshListitems(null);
//           		zkcb.biBaseRefreshListitems(lastBr.getCurrentRecord());
           		zkcb.biBaseClose(lastBr);
           	}
       		if (p_needCloseParentWindow)
       			Clients.evalJavaScript("closeParentWindow()");
			if (sessionHelper.getURLParamBool("closetab")) {
				UniLog.log1("closetab");
				ZkUtil.js("closeTab()");
			}
		}
	}
	
	/*
	//TODO: width calculation logic should move to a new utility class
	public static int calPxByString(String p_str){
		return(calPxByLen(p_str.trim().length(), pxPerChar, pxMin, pxMax));
	}
	//TODO: width calculation logic should move to a new utility class
	public static int calPxByLen(int p_len, int p_charWidth, int p_minWidth, int p_maxWidth){
		int minWidth = p_minWidth;
		if (minWidth <= 0){
			minWidth = pxMin;
		}
		int maxWidth = p_maxWidth;
		if (maxWidth <= 0){
			maxWidth = pxMax;
		}
		int charWidth = p_charWidth;
		if (charWidth <= 0){
			charWidth = pxPerChar;
		}
		
		int px = minWidth;
		try{
			px = p_len * charWidth;
			if (px < minWidth){
				px = minWidth;
			}
			else if (px > maxWidth){
				px = maxWidth;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return(px);
	}
	*/
	static public Component createComponentByBiColumn(BiColumn biColumn) {
		Component fieldTextbox;
		if (biColumn.getColumnType().trim().equals("date") ) {
			fieldTextbox = new Datebox();
			if(biColumn.getFormat() == null) ((Datebox) fieldTextbox).setFormat("yyyy/MM/dd");
			//andrew220915 replaced by customDatebox
			((Datebox) fieldTextbox).setShowTodayLink(true);
			((Datebox) fieldTextbox).setTodayLinkLabel("Today");
		} else if (biColumn.getColumnType().trim().equals("datetime") ) {
			if(biColumn.displayOnly()) {
				fieldTextbox = new Label();
			} else {
				fieldTextbox = new Datebox();
				((Datebox) fieldTextbox).setFormat("yyyy/MM/dd HH:mm:ss");
			}
		} else if (biColumn.getColumnType().trim().equals("time") ) {
			if(ZkSessionHelper.getSessionHelper().isMobile()) {
			ZkJxTimePickerList tp = new ZkJxTimePickerList();
			/*
			String endTimeStr = biColumn.getTimeCompEndTime();
			if (StringUtils.isNotBlank(endTimeStr))
				tp.setEndTime(endTimeStr);
			tp.setIsShortFormat(biColumn.getTimeCompIsShortFmt());
			tp.setStepMin(biColumn.getTimeCompStepMin());
			*/
			fieldTextbox = tp;
			} else {
			ZkJxTimePicker tp = new ZkJxTimePicker();
//			ZkJxTimePickerList tp = new ZkJxTimePickerList();
			String endTimeStr = biColumn.getTimeCompEndTime();
			if (StringUtils.isNotBlank(endTimeStr))
				tp.setEndTime(endTimeStr);
			tp.setIsShortFormat(biColumn.getTimeCompIsShortFmt());
			tp.setStepMin(biColumn.getTimeCompStepMin());
			fieldTextbox = tp.init(); 
			}
		} else if (biColumn.getColumnType().trim().equals("checkbox") ) {
			fieldTextbox = new Checkbox();
		} else if (biColumn.getColumnType().trim().equals("radio") ) {
			fieldTextbox = new Radiogroup();
			if(biColumn.getFormat() != null && !biColumn.getFormat().equals("")) {
				((Radiogroup) fieldTextbox).setOrient(biColumn.getFormat());
			}
		} else if (biColumn.getColumnType().trim().equals("combobox") ) {
			fieldTextbox = new Combobox();
		} else if (biColumn.getColumnType().trim().equals("list") ) {
			if(ZkSessionHelper.getSessionHelper().useS2ListboxForReadOnly()) {
//				if(SessionHelper.getSessionHelper().isMobile() && biColumn.isReadOnly()) {
//			    }
				if(biColumn.isReadOnly() || ZkSessionHelper.getSessionHelper().isMobile()) {
					SessionHelper sh = ZkSessionHelper.getSessionHelper();
					if(biColumn.getOptionList(sh) == null && !ZkSessionHelper.getSessionHelper().isMobile() &&
							(ZkSessionHelper.getSessionHelper().getURLParamBool("s2") || BiConfig.useS2Listbox(ZkSessionHelper.getSessionHelper())|| biColumn.useS2Listbox())) {
						fieldTextbox = new S2Listbox();
					} else {
						fieldTextbox = new Listbox();
						((Listbox) fieldTextbox).setMold("select");
					}
				} else {
					fieldTextbox = new Combobox();
				}
			} else {
			if(ZkSessionHelper.getSessionHelper().isMobile() /* && biColumn.isReadOnly() */) {
				fieldTextbox = new Listbox();
				((Listbox) fieldTextbox).setMold("select");
			} else {
				//if(useS2Listbox || biColumn.useS2Listbox()) { ... }
					//UniLog.log1("new S2Listbox");
					/*
				if(biColumn.isReadOnly()) {
					fieldTextbox = new S2Listbox();
				} else {
					fieldTextbox = new Combobox();
				}
					*/
				if( (ZkSessionHelper.getSessionHelper().getURLParamBool("s2") || BiConfig.useS2Listbox(ZkSessionHelper.getSessionHelper())|| biColumn.useS2Listbox())) {
					fieldTextbox = new S2Listbox();
				} else {
					fieldTextbox = new Combobox();
				}
			}
			/*
			fieldTextbox = new Combobox();
			*/
			}
		} else if (biColumn.getColumnType().trim().equals("pickinput") ) {
			fieldTextbox = new ZkJxPickInput();
		} else if (biColumn.getColumnType().trim().equals("integer") ) {
			if(biColumn.displayOnly()) {
				fieldTextbox = new Label();
			} else {
				fieldTextbox = new Intbox();
			}
		} else if (biColumn.getColumnType().trim().equals("float") ) {
			if(biColumn.displayOnly()) {
				fieldTextbox = new Label();
			} else {
				fieldTextbox = new Doublebox();
			}
		} else if (biColumn.getColumnType().trim().equals("money") ) {
			fieldTextbox = new Doublebox();
		} else if (biColumn.getColumnType().trim().equals("decimal") ) {
			fieldTextbox = new Doublebox();
		} else if (biColumn.getColumnType().trim().equals("label") ) {
			fieldTextbox = new Label();
		} else if (biColumn.getColumnType().trim().equals("colorbox") ) {
			fieldTextbox = new Combobox();
		} else if (biColumn.getColumnType().trim().equals("div") ) {
			fieldTextbox = new Div();
		} else if (biColumn.getColumnType().trim().equals("html") ) {
			fieldTextbox = new Html();			
		} else if (biColumn.getColumnType().trim().equals("button") ) {
			String btLabel = ZkSessionHelper.getSessionHelper().getBtLabel(biColumn);
			fieldTextbox = new ZkBiButton(btLabel);
		} else if (biColumn.getColumnType().trim().equals("memo") ) {
			fieldTextbox = new Textbox();
			((Textbox) fieldTextbox).setMultiline(true);
			int fdheight = biColumn.getColumnHeight();
			if(fdheight > 0) {
				((Textbox) fieldTextbox).setHeight(""+fdheight+"px");
			} else {
				((Textbox) fieldTextbox).setHeight("50px");
			}
		} else {
			fieldTextbox = new Textbox();
//			((Textbox) fieldTextbox).setConstraint("/.+@.+\\.[a-z]+/: Please enter an e-mail address");
		}
		if (StringUtils.isNotBlank(biColumn.getColumnType()) && fieldTextbox instanceof HtmlBasedComponent){
			((HtmlBasedComponent) fieldTextbox).setSclass("zkbi-coltype-"+biColumn.getColumnType().trim());
		}

		/*
		//TODO obtain curMode and set inplace based on mode
		if (fieldTextbox instanceof InputElement && curMode == MODE_UPDATE)
			((InputElement)fieldTextbox).setInplace(true);
		*/
   		return(fieldTextbox);
	}
	public static void setComponentFormat(Component comp, BiColumn biColumn, boolean p_widthByContent, SessionHelper p_sh) {
		//setComponentFormat(comp, biColumn, 0, 0, 0, p_widthByContent, p_sh);
		int adjust = 0;
		if (p_sh != null && p_sh.getLargeFlag()) {
			adjust = 20;
			if (comp instanceof Datebox)
				adjust += 10;
		}
		setComponentFormat(comp, biColumn, adjust, 0, 0, p_widthByContent, p_sh,null,false); //andrew211007 test large mode. should be scale by radio instead of fixed value
	}
	
	public static void setInputFormat(Component comp,BiColumn biColumn) {
		if (comp instanceof InputElement) {
			if(comp instanceof Textbox || comp instanceof Combobox) {
				if (biColumn.getColumnType().trim().equals("char")) {
					if(((InputElement) comp).getMaxlength() <= 0){
						((InputElement) comp).setMaxlength(biColumn.getColumnLength());
					}
				}
			}
			
			//set number formatting 
			if (biColumn.getFormat() != null && !biColumn.getFormat().trim().equals("")){
				if (comp instanceof Intbox){
					((Intbox) comp).setFormat(biColumn.getFormat());
				}
				else if (comp instanceof Doublebox){
					((Doublebox) comp).setFormat(biColumn.getFormat());
				} else if (comp instanceof Datebox){
					if(((Datebox) comp).getFormat() == null ||
							((Datebox) comp).getFormat().isEmpty()
							) {
						((Datebox) comp).setFormat(biColumn.getFormat());
					}
				} else{
					UniLog.logm(null, "column not support formatting");
				}
			} else {
				if(comp instanceof Datebox ) {
					if(((Datebox) comp).getFormat() == null || 
							((Datebox) comp).getFormat().isEmpty()
							) {
						if (biColumn.getColumnType().trim().equals("datetime") ) {
							((Datebox) comp).setFormat("yyyy/MM/dd HH:mm:ss");
						} else {
							((Datebox) comp).setFormat("yyyy/MM/dd");
						}
					}
				}
			}
			if(biColumn.isReadOnly()) {
				((InputElement) comp).setReadonly(true);
			}
		}
	
	}
//	public static void setComponentFormat(Component comp, BiColumn biColumn, int p_adjust, int p_minWidth, int p_maxWidth, boolean p_widthByContent, SessionHelper p_sh) {
//		setComponentFormat(comp, biColumn, p_adjust, p_minWidth, p_maxWidth, p_widthByContent, p_sh, null, false);
//	}

	static public void setMobileDateInput(Datebox p_comp) {
			p_comp.setButtonVisible(false);
			p_comp.setReadonly(true);
    		p_comp.addEventListener(Events.ON_CLICK,
    		    	new ZkBiEventListener<Event>() {
    		       		public void onZkBiEvent(Event event) throws Exception {
    		        			UniLog.log("Databox clicked");
    		        	        Window window = (Window)Executions.createComponents(
    		        	                "/zkf/MobileDatePicket.zul", null, null);

    		        	        {
    		        	        	if(window.hasFellow("fdtitle")) {
    		        	        		
    		        	        	}
    		        	        	if(window.hasFellow("mcalendar")) {
    		        	        		final Calendar cal = (Calendar) window.getFellow("mcalendar");
    		        	        		cal.setValue(p_comp.getValue());
    		        	        		UniLog.log(cal.getClass().toString());
    		        	        		if(cal != null) {
    		        	        			List<Component> childrens = cal.getChildren();
    		        	        			for(Component cp : childrens) {
    		        	        				UniLog.log(cp.getId());
    		        	        			}
    		        	        		}
    		        	        		if(window.hasFellow("btOK")) {
    		        	        		Button btOK = (Button) window.getFellow("btOK");
    		        	        		btOK.addEventListener(Events.ON_CLICK, new EventListener() {
											@Override
											public void onEvent(Event arg0) throws Exception {
												if(cal != null) {
													p_comp.setValue(cal.getValue());
													Events.echoEvent(Events.ON_CHANGE, p_comp, null);	
												}
												window.setVisible(false);
											}
    		        	        			
    		        	        		}
    		        	        		);
    		        	        		}
    		        	        		if(window.hasFellow("btCancel")) {
    		        	        		Button btCancel = (Button) window.getFellow("btCancel");
    		        	        		btCancel.addEventListener(Events.ON_CLICK, new EventListener() {
											@Override
											public void onEvent(Event arg0) throws Exception {
												window.setVisible(false);
											}
    		        	        			
    		        	        		}
    		        	        		);
    		        	        		}
    		        	        	}
    		        	        }
    		        	        window.doModal();
//    		        			Calendar cal = new Calendar();
    		            	}	
    		       	});		
	}
	
	public static void setComponentFormat(Component comp, BiColumn biColumn, int p_adjust, int p_minWidth, int p_maxWidth, boolean p_widthByContent, SessionHelper p_sh, String p_flexValue, boolean p_needSetListHeaderMinWidth) {
		//UniLog.logm(null,"comp:%s:%s min:%d max:%d wbc:%s", comp.getClass().getName().replaceAll("^.*\\.", ""), comp.getId(), p_minWidth, p_maxWidth, p_widthByContent);
		if (comp instanceof Listheader || comp instanceof Listbox || comp instanceof InputElement){
			if(((XulElement)comp).getWidth() == null && ((XulElement) comp).getHflex() == null) {
				if (p_widthByContent){
					((XulElement) comp).setHflex("min");
				}
				else{
					/*
					String calWidthStr = ZkUtil.calColumnWidth(biColumn, p_adjust, p_minWidth, p_maxWidth);
					String widthValue = ZkUtil.extractColWidthValue(calWidthStr, "width");
					if (!StringUtils.isBlank(widthValue)){
						((XulElement) comp).setWidth(widthValue);
					}
					String hflexValue = ZkUtil.extractColWidthValue(calWidthStr, "hflex");
					if (!StringUtils.isBlank(hflexValue)){
						//((XulElement) comp).setWidth(hflexValue); //andrew200325: it does not work as expected, change to setHflex
						((XulElement) comp).setHflex(hflexValue);
						if (!StringUtils.equalsIgnoreCase(hflexValue,"min")) { //remark: cannot apply both hflex and width (beside hflex=min)
							((XulElement) comp).setWidth(null); 
						}
					}
					*/
					
					//andrew221205 add isNoDOM check for fix m.style.position error. as nodom element is not allowed to sethflex
					if (p_sh.isMobile() && p_sh.getAllowExpandMobileInputField() && !ZkUtil.isNoDOM(comp)) {
						((XulElement) comp).setWidth(null);
						((XulElement) comp).setHflex("1");
						if(p_sh.isMobile()) {
							if(comp instanceof Textbox && !(comp instanceof Combobox) && !(comp instanceof Bandbox)) {
								if(biColumn.getColumnLength() > 25) {
									((Textbox) comp).setMultiline(true);
									if(StringUtils.isBlank(((Textbox) comp).getHeight()) ) {
									if(biColumn.getColumnLength() > 50) {
										((Textbox) comp).setRows(3);
									} else {
										((Textbox) comp).setRows(2);
									}
									}
								}
							}
						}
					} else {
						int adjust=p_adjust;
						if(comp instanceof Listbox) adjust -= 5;
						
					String calWidthStr = BiUtil.calColumnWidth(biColumn, adjust, p_minWidth, p_maxWidth, p_flexValue);
					String widthValue=BiUtil.extractColDecorationValue(calWidthStr, "width");
					if (!StringUtils.isBlank(widthValue)){
						((XulElement) comp).setWidth(widthValue);
					}
					String alignValue=BiUtil.extractColDecorationValue(biColumn.getDecoration(), "calign");
					if (StringUtils.isNotBlank(alignValue)) {
						ZkUtil.appendStyle((XulElement)comp, "text-align:" + alignValue);
					}
					String hflexValue = BiUtil.extractColDecorationValue(calWidthStr, "hflex");
					//UniLog.log1("p_adjust:%d, calWidthStr:%s, widthValue:%s, hflexValue:%s", p_adjust, calWidthStr, widthValue, hflexValue);
					if (!StringUtils.isBlank(hflexValue)){
						//((XulElement) comp).setWidth(hflexValue); //andrew200325: it does not work as expected, change to setHflex
						((XulElement) comp).setHflex(hflexValue);
						if (!StringUtils.equalsIgnoreCase(hflexValue,"min")) { //remark: cannot apply both hflex and width (beside hflex=min)
							((XulElement) comp).setWidth(null); 
							if (comp instanceof Listheader && p_needSetListHeaderMinWidth)
								ZkUtil.js("jq('#%s-hdfaker,#%s-bdfaker,#%s-ftfaker').css('min-width', '50px')", comp.getUuid(), comp.getUuid(), comp.getUuid());
						}
					}
					}
				}
			}
		}
		
		//set input format
		if (comp instanceof InputElement) {
			setInputFormat(comp,biColumn);
			/*
			if(comp instanceof Textbox || comp instanceof Combobox) {
				if (biColumn.getColumnType().trim().equals("char")) {
					if(((InputElement) comp).getMaxlength() <= 0){
						((InputElement) comp).setMaxlength(biColumn.getColumnLength());
					}
				}
			}

			//set number formatting 
			if (biColumn.getFormat() != null && !biColumn.getFormat().trim().equals("")){
				if (comp instanceof Intbox){
					((Intbox) comp).setFormat(biColumn.getFormat());
				}
				else if (comp instanceof Doublebox){
					((Doublebox) comp).setFormat(biColumn.getFormat());
				}
				else{
					UniLog.logm(null, "column not support formatting");
				}
			}
			if(biColumn.isReadOnly()) {
				((InputElement) comp).setReadonly(true);
			}
			*/
		}
		//implement radiogroup read only
		if (comp instanceof Radiogroup) {
			if (biColumn.isReadOnly()) {
				ZkUtil.appendSclass((Radiogroup)comp, "zkbi-disable-radiogroup");
			}
//			((XulElement) comp).setHflex("min");
//			((Radiogroup) comp).setWidth("100px");
		}
		if (comp instanceof Button) {
			String calWidthStr = BiUtil.calColumnWidth(biColumn, p_adjust, 20, p_maxWidth,null);
			String widthValue = BiUtil.extractColDecorationValue(calWidthStr, "width");
			if (!StringUtils.isBlank(widthValue)){
				int px = NumberUtil.atoi(widthValue);
				if (px <= 0) {
					//nth to do
				}
				else if (px < 40) {
					((Button) comp).setSclass("zkbi-xsmall-button");
				}
				else if (px < 80) {
					((Button) comp).setSclass("zkbi-small-button");
				} ((Button) comp).setWidth(widthValue);
			}
		}
		if(comp instanceof S2Listbox) {
			//((S2Listbox) comp).setWidth("200px");
			((S2Listbox) comp).setWidth("");
			String calWidthStr = BiUtil.calColumnWidth(biColumn, -5, p_minWidth, p_maxWidth,null);
			String widthValue = BiUtil.extractColDecorationValue(calWidthStr, "width");
			if (!StringUtils.isBlank(widthValue)){
				((S2Listbox) comp).setWidth(widthValue);
			}
			else {
				((S2Listbox) comp).setWidth("200px");
			}
		}
		if(comp instanceof Datebox) {
			if(p_sh.isMobileDevice()) {
				UniLog.log("Datebox is mobile device, disable popup button");
				setMobileDateInput((Datebox) comp);
//				Popup pp = new Popup();
			}
		}
//		if(comp instanceof Label) {
//			int align = biColumn.getAlignment();
//			if(align != 0) {
//				for(Component pc = comp.getParent();pc != null;pc = pc.getParent() ) {
//					if(pc instanceof HtmlBasedComponent) {
//						if(align > 0) {
//							ZkUtil.appendStyle((HtmlBasedComponent) pc, "text-align:left;");
//						} else {
//							ZkUtil.appendStyle((HtmlBasedComponent) pc, "text-align:right;");
//						}
//					}
//				}
//			}
//		}
		
		ZkUtil.addCompMark(comp, biColumn);
	}
	
    static public JxZkBiBase buildDetailWindow(BiResult result,final Component dWin,boolean p_isMobile,boolean p_hasAUDColumn,JxZkBiBaseCallback zkcb) {
    	return buildDetailWindow(result,dWin,p_isMobile,p_hasAUDColumn,zkcb,null);
    }
    static public JxZkBiBase buildDetailWindow(final BiResult result,final Component dWin, boolean p_isMobile,boolean p_hasAUDColumn,JxZkBiBaseCallback zkcb,Map<String,Object> p_urlParams) 
    {
    	BiResult brAttach=null;
    	boolean useCompDiv = false;
    	try{
			ZkBiRecordCopy recordCopy = null;
    		if(!(dWin instanceof IdSpace)) {
    			UniLog.log( new Exception("buildDetailWindow dWin not IdSpace "));
    			return(null);
    		}
			DetailDecoration dDerac = null;
			SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper();
			try {
				UniLog.log("jxapp."+result.getView().getName() + " 000 ");
				Class jxFormClass = null;
				if(result.getView().getClassPath() != null) {
					jxFormClass = Class.forName( result.getView().getClassPath());
					if(jxFormClass != null) {
						dWin.setAttribute("formClass", result.getView().getClassPath());
					}
				} else {
					jxFormClass = Class.forName("com.uniinformation.jxapp." + result.getView().getName());
				}
				if(jxFormClass == null) {
					UniLog.log("jxapp."+result.getView().getName() + " not found , use JxZkBase 1");
					dWin.setId("JxZkBiBase");
				} 
				else {
					dWin.setId(result.getView().getName());
				}
			}
			catch (Exception ex) {
				UniLog.log("jxapp."+result.getView().getName() + " not found , use JxZkBase 2");
				dWin.setId("JxZkBiBase");
			}
			
			boolean templateFound = false;
			String custViewStr = "viewforms/"+ result.getView().getName().replaceAll("\\.", "/");
			//andrew190522: this block of code is obsoleted
			String formSuffix = BiConfig.getString(sessionHelper,"FormSuffix");
			if(p_isMobile) {
				if(formSuffix != null && !StringUtils.isBlank(formSuffix)) {
					try {
						Executions.getCurrent().createComponents(custViewStr+"_mobile"+formSuffix+".zul", dWin, null);
						//ZkUtil.translateAllComp(sessionHelper,dWin);  //test translate child view
						UniLog.log("Customized with suffix mobile view for " + result.getView().getName() + " loaded " + custViewStr);
						templateFound = true;
					} 
					catch (Exception ex) {
						UniLog.log1("customized view %s %s not found. msg:%s", result.getView().getName(), custViewStr, ex.getMessage());
					}
				}
				if(!templateFound) {
					try {
						Executions.getCurrent().createComponents(custViewStr+"_mobile.zul", dWin, null);
						//ZkUtil.translateAllComp(sessionHelper,dWin);  //test translate child view
						UniLog.log("Customized mobile view for " + result.getView().getName() + " loaded " + custViewStr);
						templateFound = true;
					} 
					catch (Exception ex) {
						UniLog.log1("customized view %s %s not found. msg:%s", result.getView().getName(), custViewStr, ex.getMessage());
					}
				}
			}
			if(!templateFound) {
				if(formSuffix != null && !StringUtils.isBlank(formSuffix)) {
					int idx = formSuffix.indexOf(",");
					if(idx > 0) {
						StringTokenizer tk = new StringTokenizer(formSuffix, ",");
						for (int i=0; tk.hasMoreTokens(); ) {
							String arg = tk.nextToken().trim();
							try {
								Executions.getCurrent().createComponents(custViewStr+arg+".zul", dWin, null);
								UniLog.log("Customized suffix view for " + result.getView().getName() + " loaded " + custViewStr+arg+".zul");
								templateFound=true;
								break;
							} 
							catch (Exception ex) {
								UniLog.log1("tokenized customized view %s %s not found. msg:%s", result.getView().getName(), arg, ex.getMessage());
							}
							i++;
						}
						
					} else {
					try {
						Executions.getCurrent().createComponents(custViewStr+formSuffix+".zul", dWin, null);
						//ZkUtil.translateAllComp(sessionHelper,dWin);  //test translate child view
						//ZkUtil.translateAllComp(result, dWin);
						UniLog.log("Customized suffix view for " + result.getView().getName() + " loaded " + custViewStr);
						templateFound=true;
					} 
					catch (Exception ex) {
						UniLog.log1("customized view %s %s not found. msg:%s", result.getView().getName(), custViewStr, ex.getMessage());
					}
					}
				}
				if(!templateFound) {
					try {
						Executions.getCurrent().createComponents(custViewStr+".zul", dWin, null);
						//ZkUtil.translateAllComp(sessionHelper,dWin);  //test translate child view
						//ZkUtil.translateAllComp(result, dWin);
						UniLog.log("Customized view for " + result.getView().getName() + " loaded " + custViewStr);
						templateFound=true;
					} 
					catch (Exception ex) {
						UniLog.log1("customized view %s %s not found. msg:%s", result.getView().getName(), custViewStr, ex.getMessage());
					}
				}
				if(!templateFound && result.getSessionHelper().useJxFormG2()) {
					try {
						Executions.getCurrent().createComponents("viewforms/JxFormDefault.zul", dWin, null);
						//ZkUtil.translateAllComp(sessionHelper,dWin);  //test translate child view
						//ZkUtil.translateAllComp(result, dWin);
						UniLog.log("Use JxFormDefault for " + result.getView().getName() + " loaded " + custViewStr);
						templateFound=true;
					} 
					catch (Exception ex) {
						UniLog.log1("customized view %s %s not found. msg:%s", result.getView().getName(), custViewStr, ex.getMessage());
					}
				}
			}
			
			Vector<BiColumn> resultColumns;
			if(result.getView().getPreferOrders() == null) {
				resultColumns = result.getColumns();
			} else {
				resultColumns = new Vector<BiColumn>();
				Vector<BiColumn> vCols = result.getColumns();
				for(String col : result.getView().getPreferOrders()) {
					BiColumn bc = result.getColumnByLabel(col);
					
					if(vCols.contains(bc)) {
						resultColumns.add(bc);
					}
				}
				for(BiColumn bc : vCols) {
					if(!resultColumns.contains(bc)) {
						resultColumns.add(bc);
					}
				}
			}
			ActionButtonHelper abHelper = new ActionButtonHelper(sessionHelper);
			Map paramMap = MapUtil.of("abHelper",abHelper);
			if (p_urlParams != null) {
					paramMap.putAll(p_urlParams);
			}
			if(paramMap.get("useCompDiv") == null) {
				if(sessionHelper.useCompDiv()) {
					paramMap.put("useCompDiv","Y");
				}
			}
			useCompDiv = "Y".equals(SessionHelper.getURLParamAsString("useCompDiv",paramMap));
			
			ArrayList<Integer> rowCntAtCol=null;
			ArrayList<Integer> colCntAtRow=null;
			if(useCompDiv) {
				rowCntAtCol = new ArrayList<Integer>();
				colCntAtRow = new ArrayList<Integer>();
			}
			for (int i=0; i<resultColumns.size(); i++){
//			    BiColumn biColumn = (BiColumn) result.getColumns().elementAt(i);
			    BiColumn biColumn = resultColumns.get(i);
			    //UniLog.log("HAHA3:"+ biColumn.getFormatedValue(bitablerec));
			    if(biColumn.isInvisible(sessionHelper)) {
			    	Div div = (Div) dWin.getFellowIfAny("compdiv_"+biColumn.getLabel(),true);
			    	if(div != null) {
						if (ZkUtil.matchesSelector(div.getParent(), ".zkbifdiv2-compdiv"))
			    			div.getParent().getParent().setVisible(false);
			    		else
			    			div.getParent().setVisible(false);
			    	}
			    	continue;
			    }
			    try {
			    	Component ccc;
			    	if(dWin.hasFellow(biColumn.getLabel(),true))  {
			    		ccc = dWin.getFellow(biColumn.getLabel(),true);
			    		if (biColumn.getAllowPickOldInput() && biColumn.getColumnType().trim().equals("char") 
			    				&& ccc instanceof Textbox && !StringUtils.equals((String)ccc.getAttribute("isQueryInputComp"), "Y")) {
			    			Textbox tb = (Textbox)ccc;
			    			XulElement qi = ZkBiAdvSearch.buildQueryInput(sessionHelper, dWin, false, false, false, result, biColumn);
			    			Component parent = tb.getParent();
			    			qi.setId(tb.getId());
			    			qi.setStyle(tb.getStyle());
			    			qi.setHflex(tb.getHflex());
			    			qi.setAttribute("isQueryInputComp", "Y");
			    			tb.setId(null);
			    			parent.insertBefore(qi, tb);
			    			parent.removeChild(tb);
			    			ccc = qi;
			    		}
	    				setComponentFormat(ccc,biColumn,false,sessionHelper);
	    				/*
	    				//translate the corresponding label, suppose the label id is lb_ + label
	    				String lbIdString = "lb_" + biColumn.getLabel();
	    				Component labelComp = dWin.getFellowIfAny(lbIdString);
	    				if (labelComp != null && labelComp instanceof Label){
	    					//((Label)labelComp).setValue(sessionHelper.getLabel(biColumn));
	    					((Label)labelComp).setValue(ZkBiTranslateHelper.getText(sessionHelper, biColumn.getCellFullName(), "LABEL", sessionHelper.getLabel(biColumn)));
	    					JxZkBiBase.addContextMenu(sessionHelper,(Label)labelComp, MapUtil.of("changeLabel", biColumn));
	    				}
	    				*/
	    				
	    				//andrew190704:hot fix for artway work order page
	    				String lbIdString = "lb_" + biColumn.getLabel();
	    				Component labelComp = dWin.getFellowIfAny(lbIdString);
	    				if (labelComp != null && labelComp instanceof Label && StringUtils.isBlank(((Label)labelComp).getValue())){
	    					((Label)labelComp).setValue(sessionHelper.getLabel(biColumn));
	    				}
	    				
	    				//andrew230602 hotfix afscn cannot translate zkbiradiogroup, radiogroup
	    				if (sessionHelper.getAllowOptionTranslate() && ccc instanceof Radiogroup && StringUtils.isBlank((String)ccc.getAttribute("tlkey"))){
	    					ccc.setAttribute("tlkey", biColumn.getLabel());
	    				}
	    				continue;
			    	}
			    } 
			    catch (Exception ex) {
			    	UniLog.log("Trapped Exception 240320" + ex.toString());
			    }

		    	Div div = (Div) dWin.getFellowIfAny("compdiv_"+biColumn.getLabel(),true);
		    	if(div == null & useCompDiv) {
		    		if(p_isMobile) {
		    			Row row = new Row();
		    			ZkBiFdiv biDiv = new ZkBiFdiv();
		    			biDiv.setJxId(biColumn.getLabel());
   			    		row.appendChild(biDiv);
   			    		if(dDerac == null) {
   			    			dDerac = new DetailDecoration(dWin,p_isMobile,sessionHelper,useCompDiv);
   			    		}
		    			dDerac.dRows.appendChild(row);
		    			div = (Div) dWin.getFellowIfAny("compdiv_"+biColumn.getLabel(),true);
		    		} else {
   			    		if(dDerac == null) {
   			    			dDerac = new DetailDecoration(dWin,p_isMobile,sessionHelper,useCompDiv);
   			    			if(useCompDiv) {
   			    				int nr = dDerac.dRows.getChildren().size();
   			    				for(int ri = 0;ri<nr;ri++) {
   			    					Row rr = (Row) dDerac.dRows.getChildren().get(ri);
//   			    					rowCntAtCol = new ArrayList<Integer>();
   			    					int rcColIdx = 0;
   			    					for(int ci=0;ci < rowCntAtCol.size();ci++) {
   			    						rowCntAtCol.set(ci,ri+1);
   			    					}
   			    					for(Component rc : rr.getChildren()) {
   			    						int rcSpan = 1;
   			    						if(rc instanceof org.zkoss.zul.Cell) {
   			    							rcSpan = ((org.zkoss.zul.Cell) rc).getColspan();
   			    						}
   			    						for(int j =rowCntAtCol.size();j < rcColIdx+rcSpan;j++) {
   			    							rowCntAtCol.add(ri+1);
   			    						}
   			    						rcColIdx += rcSpan;
   			    					}
   			    					colCntAtRow.add(rcColIdx);
   			    				}
   			    			}
   			    		}
		    			int curColIdx = 0;  /* default to first column */
		    			int curRowIdx = -1; /* default to next row */
		    			int colSpan = 1;
		    			int rowSpan = 1;
//						lcol.setColspan(dDerac.grid.getColumns().getChildren().size());
		    			String ss = BiUtil.extractColDecorationValue(biColumn.getDecoration(),"colidx");
		    			if(!StringUtils.isBlank(ss)) curColIdx = Integer.parseInt(ss);
		    			ss = BiUtil.extractColDecorationValue(biColumn.getDecoration(),"colspan");
		    			if(!StringUtils.isBlank(ss)) colSpan = Integer.parseInt(ss);
		    			ss = BiUtil.extractColDecorationValue(biColumn.getDecoration(),"rowspan");
		    			if(!StringUtils.isBlank(ss)) rowSpan = Integer.parseInt(ss);
		    			for(int j =rowCntAtCol.size();j < curColIdx+colSpan;j++) {
		    				rowCntAtCol.add(0);
		    				Column cn = new Column();
//		    				cn.setAlign("top"); notwork
		    				dDerac.dCols .appendChild(cn);
		    				if(j > 1) {
		    					cn = (Column) dDerac.dCols.getChildren().get(0);
		    					cn.setWidth("50%");
		    					cn = (Column) dDerac.dCols.getChildren().get(1);
		    					cn.setWidth("30%");
		    				}
		    			}
		    			curRowIdx = rowCntAtCol.get(curColIdx);
		    			for(int j =colCntAtRow.size();j < curRowIdx+rowSpan;j++) {
		    				colCntAtRow.add(0);
		    				dDerac.dRows.appendChild(new Row());
		    			}
		    			/*
		    			for(int j = 0;j<curColIdx+1;j++) {
		    				rowCntAtCol.set(j,curRowIdx);
		    			}
		    			List<Component> cells = row.getChildren();
		    			if(cells.size() < curColIdx) {
							org.zkoss.zul.Cell ce = new org.zkoss.zul.Cell();
							ce.setColspan(curColIdx-cells.size());
							row.appendChild(ce);
		    			}
		    			*/
						org.zkoss.zul.Cell ce;
		    			Row row = (Row) dDerac.dRows.getChildren().get(curRowIdx);
		    			if(colCntAtRow.get(curRowIdx) < curColIdx) {
							ce = new org.zkoss.zul.Cell();
							ZkUtil.appendSclass(ce, "gridcell_no_border");
							ce.setColspan(curColIdx-colCntAtRow.get(curRowIdx));
							row.appendChild(ce);
		    			}
						ce = new org.zkoss.zul.Cell();
						ZkUtil.appendSclass(ce, "gridcell_no_border");
						ce.setColspan(colSpan);
						ce.setRowspan(rowSpan);
//						ce.setAlign("top");  notwork
						row.appendChild(ce);
		    			curRowIdx += rowSpan;
		    			curColIdx += colSpan;
		    			for(int j=0;j<curColIdx;j++) {
		    				if(curRowIdx > rowCntAtCol.get(j)) rowCntAtCol.set(j,curRowIdx);
		    			}
		    			for(int j=0;j<curRowIdx;j++) {
		    				if(curColIdx > colCntAtRow.get(j)) colCntAtRow.set(j,curColIdx);
		    			}
		    			
		    			ZkBiDiv biDiv = new ZkBiDiv();
//		    			ZkBiFdiv biDiv = new ZkBiFdiv();
		    			//biDiv.setWidth("800px");
//		    			biDiv.setCompWidth("600px");
		    			biDiv.setLabelWidth("200px");
		    			biDiv.setJxId(biColumn.getLabel());
		    			biDiv.setContainerStyle("display:flex");
   			    		ce.appendChild(biDiv);
		    			div = (Div) dWin.getFellowIfAny("compdiv_"+biColumn.getLabel(),true);

		    		}
		    	}
		    	if(div == null) {
			    Row row = new Row();
			    Label fieldLabel = null;
			    fieldLabel = (Label) dWin.getFellowIfAny("lb_"+biColumn.getLabel(),true);
			    if(fieldLabel == null) {
			    	fieldLabel = new Label(ZkBiTranslateHelper.getText(sessionHelper, biColumn.getCellFullName(), "LABEL", sessionHelper.getLabel(biColumn)));
				   	fieldLabel.setSclass("zkbi-col-label");
				   	fieldLabel.setId("lb_" + biColumn.getLabel());
   			    	fieldLabel.setWidth("95%");
   			    	row.appendChild(fieldLabel);
			    } else {
			    	fieldLabel.setValue(ZkBiTranslateHelper.getText(sessionHelper, biColumn.getCellFullName(), "LABEL", sessionHelper.getLabel(biColumn)));
			    }
			    
		    	//fieldLabel.setStyle("font-style:italic !important;font-family:\"Times New Roman\", Times, serif, DFKai-sb, BiauKai !important");
		    	//fieldLabel.setStyle("font-style:italic !important;font-family:\"Times New Roman\", Times, DFKai-sb, BiauKai !important");  //andrew220704 move to style-large.css
			    Component fieldTextbox;
			    if (biColumn.getAllowPickOldInput() && biColumn.getColumnType().trim().equals("char")) {
 		    		fieldTextbox = ZkBiAdvSearch.buildQueryInput(sessionHelper, dWin, false, false, false, result, biColumn);
    		    	fieldTextbox.setAttribute("isQueryInputComp", "Y");
			    } else
			    	fieldTextbox = createComponentByBiColumn(biColumn);
		    	fieldTextbox.setId(biColumn.getLabel());
		    	setComponentFormat(fieldTextbox,biColumn,false,sessionHelper);
		    	if(fieldLabel != null) {
		    		fieldTextbox.setAttribute("fdLabelComp", fieldLabel);
		    	}
		    	row.appendChild(fieldTextbox);
		    	if(dDerac == null) {
   			    	dDerac = new DetailDecoration(dWin,p_isMobile,sessionHelper,useCompDiv);
			    }
			    dDerac.dRows.appendChild(row);
				JxZkBiBase.addContextMenu(sessionHelper,fieldLabel, MapUtil.of("changeLabel", MapUtil.of("key",biColumn.getCellFullName(),"defaultValue",biColumn.getEngName())));
			    UniLog.log("fieldLabel round1:"+fieldLabel.getValue());
	    		if (sessionHelper.getAllowOptionTranslate() && fieldTextbox instanceof Radiogroup)
	    			fieldTextbox.setAttribute("tlkey", biColumn.getLabel());
		    	} else {
		    		Label fieldLabel = null;
		    		fieldLabel = (Label) dWin.getFellowIfAny("lb_"+biColumn.getLabel(),true);
		    		if(fieldLabel != null) {
			    		fieldLabel.setValue(ZkBiTranslateHelper.getText(sessionHelper, biColumn.getCellFullName(), "LABEL", sessionHelper.getLabel(biColumn)));
			    		JxZkBiBase.addContextMenu(sessionHelper,fieldLabel, MapUtil.of("changeLabel", MapUtil.of("key",biColumn.getCellFullName(),"defaultValue",biColumn.getEngName())));
			    		UniLog.log("fieldLabel round1:"+fieldLabel.getValue());
			    	}
		    		Component fieldTextbox;
		    		if (sessionHelper.getAllowPickOldInput() && biColumn.getColumnType().trim().equals("char")) {
 		    			fieldTextbox = ZkBiAdvSearch.buildQueryInput(sessionHelper, dWin, false, false, false, result, biColumn);
    		    		fieldTextbox.setAttribute("isQueryInputComp", "Y");
			    	} else
			    		fieldTextbox = createComponentByBiColumn(biColumn);
		    		fieldTextbox.setId(biColumn.getLabel());
		    		setComponentFormat(fieldTextbox,biColumn,false,sessionHelper);
		    		boolean isFlexDiv = ZkUtil.hasSclass(div, "zkbifdiv-compdiv");
		    		String compWidth = div.getWidth();
		    		if(!StringUtils.isBlank(compWidth) || isFlexDiv) {
		    			String w = null;
		    			ZkBiFdiv2 div2 = isFlexDiv && ZkUtil.matchesSelector(div.getParent(), ".zkbifdiv2-compdiv") ? (ZkBiFdiv2)ZkUtil.closestComponent(div.getParent(), "zkbifdiv2") : null;
		    			if(fieldTextbox instanceof XulElement) {
		    				w = ((XulElement)fieldTextbox).getWidth();
		    				((XulElement) fieldTextbox).setHflex(null);
		    				((XulElement) fieldTextbox).setWidth("100%");
		    			}
		    			if(fieldTextbox instanceof S2Listbox) {
		    				w = ((S2Listbox)fieldTextbox).getWidth();
		    				((S2Listbox) fieldTextbox).setHflex(null);
		    				((S2Listbox) fieldTextbox).setWidth("100%");
		    			}
		    			if (div2 != null && StringUtils.isNotBlank(w) && StringUtils.isBlank(div2.getCompMaxWidth()))
		    				div2.setCompMaxWidth(String.format("calc(%s + 10px)", w));
		    		}
		    		div.appendChild(fieldTextbox);
		    		if (sessionHelper.getAllowOptionTranslate() && fieldTextbox instanceof Radiogroup) {
		    			fieldTextbox.setAttribute("tlkey", biColumn.getLabel());
		    		}
		    	}
		    	
		    	
				//JxZkBiBase.addContextMenu(sessionHelper,fieldLabel, MapUtil.of("changeLabel", biColumn));
			}
			Vector links = result.getSubLinks();
			if(links != null) {
				for(int i = 0;i<links.size();i++) {
					Component linkComp = null;
					BiResult sl = (BiResult) links.get(i);
					String ak = result.getView().linkAccessKey(sl.getView());
					if(ak != null && !result.getSessionHelper().hasAccessRight(ak)) {
						Component slComp;
						String slStr = "link_"+replaceViewName(sl.getView().getName());
						slComp = dWin.getFellowIfAny(slStr,true);
						if(slComp != null) slComp.setVisible(false);
						slStr = "list_"+replaceViewName(sl.getView().getName());
						slComp = dWin.getFellowIfAny(slStr,true);
						if(slComp != null) slComp.setVisible(false);
						continue;
					}
					if(result.getView().linkAutoExpand(sl.getView())) {
						UniLog.log("Sublink " + sl.getView().getName() + " set to autoexpand , build detail window instead of create listbox");
						buildDetailWindow(sl,dWin, p_isMobile,p_hasAUDColumn,zkcb,p_urlParams);
						continue;
					}
					String linkStr = "";
					try {
						linkStr = "link_"+replaceViewName(sl.getView().getName());
						linkComp = dWin.getFellow(linkStr,true);
						UniLog.log("linkComp is valid " + linkStr);
				    } 
					catch (Exception ex) {
						UniLog.log("linkComp invalid " + linkStr);
				    }
					if(linkComp == null ) {
						try {
//							linkStr = "list_"+sl.getView().getName().replace(".", "_");
							linkStr = "list_"+replaceViewName(sl.getView().getName());
							linkComp = dWin.getFellow(linkStr,true);
							UniLog.log("listComp is valid " + linkStr);
				    	} 
						catch (Exception ex) {
							UniLog.log("listComp invalid " + linkStr);
						}
					}
					if(linkComp == null ) {
						UniLog.log("linkComp is null");
						linkComp = new Listbox();
//						linkComp.setId("list_"+sl.getView().getName().replace(".", "_"));
						linkComp.setId("list_"+replaceViewName(sl.getView().getName()));
						//((Listbox) linkComp).setVflex(true);
						/*
						((Listbox) linkComp).setMold("paging");
						((Listbox) linkComp).setPageSize(20);
						((Listbox) linkComp).setAutopaging(false);
						*/
						//((Listbox) linkComp).setMold("select");
						((Listbox) linkComp).setMold("default");
						
						Label fieldLabel = new Label(sl.getView().getHeader());
						if(!p_isMobile) {
							/*
							Auxhead ah = new Auxhead();
							linkComp.appendChild(ah);
							Row nameRow = new Row();
							org.zkoss.zul.Cell lcol = new org.zkoss.zul.Cell();
							lcol.setColspan(2);
							lcol.setAlign("left");
							lcol.appendChild(fieldLabel);
							nameRow.appendChild(lcol);
							dd.dRows.appendChild(nameRow);
							*/

//							{
//								Listfoot lf = new Listfoot();
//								linkComp.appendChild(lf);
//								Listfooter lfdr = new Listfooter();
//								lf.appendChild(lfdr);
//								lfdr.appendChild(new Label("HAHA"));
//							}
						}
						
						Row contentRow = new Row();
						org.zkoss.zul.Cell lcol = new org.zkoss.zul.Cell();
						if(dDerac == null) {
							dDerac = new DetailDecoration(dWin,p_isMobile,sessionHelper,useCompDiv);
						}
						if(dDerac.grid.getColumns().getChildren().size() > 0) {
							lcol.setColspan(dDerac.grid.getColumns().getChildren().size());
						}
						lcol.appendChild(linkComp);
						contentRow.appendChild(lcol);
						if(dDerac == null) {
							dDerac = new DetailDecoration(dWin,p_isMobile,sessionHelper,useCompDiv);
						}
						dDerac.dRows.appendChild(contentRow);
					}
					if (linkComp instanceof Listbox && StringUtils.isBlank(((Listbox) linkComp).getEmptyMessage())){
						if(result.getView().linkNoAddUpDateDelete(sl.getView())) {
							((Listbox) linkComp).setEmptyMessage(sessionHelper.getLabel("No Record Details"));
						} else {
							((Listbox) linkComp).setEmptyMessage(sessionHelper.getLabel("Please enter record detail"));
						}
					}
					/*
					if (linkComp instanceof Listbox && StringUtils.isBlank(((Listbox)linkComp).getSclass())){
						//((Listbox) linkComp).setSclass("zkbi-linkcomp-listbox");
						((Listbox) linkComp).setSclass("zkbi-linkcomp-listbox " + linkComp.getId());
					}
					*/
					//andrew220816 should append sclass
					if (linkComp instanceof Listbox) {
						ZkUtil.appendSclass((Listbox)linkComp, "zkbi-linkcomp-listbox " + linkComp.getId());
					}
					
					if(p_hasAUDColumn) {
						Component addBt;
						if(/* (!p_isMobile || !useCompDiv) && */ result.getView().linkAllowAdd(sl.getView(),result.getSessionHelper())) {
//							addBt = dWin.getFellowIfAny("btadd_list_"+sl.getView().getName().replace(".", "_"),true);
							addBt = dWin.getFellowIfAny("btadd_list_"+replaceViewName(sl.getView().getName()) ,true);
							if(addBt == null && linkComp instanceof Listbox) {
								Listhead lh ;
								lh = ((Listbox) linkComp).getListhead();
								if(lh == null) {
									lh = new Listhead();
									lh.setParent(linkComp);
								}
								Listheader lhdr;
								List <Component> lhc =lh.getChildren();
								if(lhc.size() > 0) {
									lhdr = (Listheader) lhc.get(0);
								} 
								else {
									lhdr = new Listheader();
									lhdr.setParent(lh);
//									if(!useCompDiv || !p_isMobile) {
									if(!useMobileLink(p_isMobile,useCompDiv)) {
										lhdr.setWidth("70px");
										lhdr.setAlign("center");
									} else {
										lhdr.setAlign("left");
									}
								}
    							addBt = (Component) new Toolbarbutton();
    							//((Toolbarbutton) addBt).setIconSclass("z-icon-plus z-icon-2x");
    							//((Toolbarbutton) addBt).setIconSclass("z-icon-plus-square-o z-icon-2x");
    							//((Toolbarbutton) addBt).setImage("images/file_add_20.png");
								((Toolbarbutton) addBt).setImage("images/row_add20.png");
								((Toolbarbutton) addBt).setTooltiptext(sessionHelper.getTtLabel("Add Item"));
								((Toolbarbutton) addBt).setSclass("narrowtoolbarbutton");
//								addBt.setId("btadd_list_"+sl.getView().getName().replace(".", "_"));
								addBt.setId("btadd_list_"+replaceViewName(sl.getView().getName()));
								lhdr.appendChild(addBt);
								/*
								if (frozenActionColumn){
									//experiment: frozen first column
									((Listbox) linkComp).appendChild(new Frozen(){{
										this.setColumns(1);
										this.setStart(0);
									}});
								}
								*/
								
//								{
//									Toolbarbutton tbb = new Toolbarbutton();
//									tbb.setImage("images/icons/zkweb/090-edit-20x20.png");
//									tbb.setTooltiptext("Update List");
//									tbb.setId("btupd_list_"+replaceViewName(sl.getView().getName()));
//									tbb.setVisible(false);
//									lhdr.appendChild(tbb);
//								}
							}
						}
					}
					
		    		//set frozen column
		    		final int frozenCount = getFrozenCount(p_hasAUDColumn, sessionHelper);
		   			if (frozenCount > 0){
						((Listbox) linkComp).appendChild(new Frozen(){{
		  					this.setColumns(frozenCount);
		  					this.setStart(0);
		   				}});
		   			}
				}
			}
			
			
			if (p_hasAUDColumn){
				
				//record copy button
				if (sessionHelper.getAllowRecordCopy()) {
					String bgRecordCopyId = "bgRecordCopy";
					Button bgRecordCopy = (Button) dWin.getFellowIfAny(bgRecordCopyId, true);
					if (bgRecordCopy == null) {
						recordCopy = new ZkBiRecordCopy();
						bgRecordCopy = recordCopy.buildButtonGroup(sessionHelper, dWin, result.getView().getName(), bgRecordCopyId);
					}
					if (bgRecordCopy != null) {
						List<EventListener<? extends Event>> clickEventList = Lists.newArrayList(bgRecordCopy.getEventListeners(Events.ON_CLICK));
						for (EventListener<?> evt : clickEventList) {
							bgRecordCopy.removeEventListener(Events.ON_CLICK, evt);
							bgRecordCopy.addEventListener("onCustomClick", evt);
						}
						abHelper.addButton(bgRecordCopy, false, true, null, 998);
					}
				}
				
				
				//previous button
				Button btPrevious = null;
				btPrevious = (Button) dWin.getFellowIfAny("btPrevious" ,true);
				//if no next button, create it automatically
				if (btPrevious == null){
					UniLog.log("add btPrevious Button");
					btPrevious = new ZkBiButton();
					//btPrevious.setLabel(sessionHelper.getBtLabel("Prev"));
					btPrevious.setTooltiptext(sessionHelper.getTtLabel("Previous Record"));
					btPrevious.setStyle("min-width:50px !important");
					//btPrevious.setImage("images/icons/zkweb/040-file-5-20x20.png");
					btPrevious.setIconSclass("z-icon-arrow-left");
					btPrevious.setAttribute("tlkey", "bt_detail_next");
					btPrevious.setId("btPrevious");
				}
				abHelper.addButton(btPrevious, true, true, "fa-arrow-left",999);
				
				//next button
				Button btNext = null;
				btNext = (Button) dWin.getFellowIfAny("btNext" ,true);
				//if no next button, create it automatically
				if (btNext == null){
					UniLog.log("add btNext Button");
					btNext = new ZkBiButton();
					//btNext.setLabel(sessionHelper.getBtLabel("Next"));
					btNext.setTooltiptext(sessionHelper.getTtLabel("Next Record"));
					btNext.setStyle("min-width:50px !important");
					//btNext.setImage("images/icons/zkweb/040-file-5-20x20.png");
					btNext.setIconSclass("z-icon-arrow-right");
					btNext.setAttribute("tlkey", "bt_detail_next");
					btNext.setId("btNext");
				}
				abHelper.addButton(btNext, true, true, "fa-arrow-right",999);
				
				
				Button btReloadDetail = (Button) dWin.getFellowIfAny("btReloadDetail" ,true);
				if (btReloadDetail == null){
					btReloadDetail = new ZkBiButton();
					//btReloadDetail.setLabel(sessionHelper.getBtLabel("Reload"));
					btReloadDetail.setTooltiptext(sessionHelper.getTtLabel("Reload Current Record From DB"));
					btReloadDetail.setStyle("min-width:50px !important");
					btReloadDetail.setAttribute("tlkey", "bt_detail_reload");
					btReloadDetail.setId("btReloadDetail");
					btReloadDetail.setIconSclass("z-icon-refresh");
			    }
				abHelper.addButton(btReloadDetail, true, true, "fa-reload",1000);

				Button btEdit = null;
				btEdit = (Button) dWin.getFellowIfAny("btEdit" ,true);
				if (btEdit == null){
					UniLog.log("add btEdit Button");
					btEdit = new ZkBiButton();
					//btUpdate.setLabel(sessionHelper.getBtLabel("Save Changes"));
					btEdit.setLabel(sessionHelper.getBtLabel("Edit"));
					btEdit.setImage("images/icons/zkweb/039-file-3-20x20.png");
					btEdit.setAttribute("tlkey", "bt_detail_edit");
					btEdit.setId("btEdit");
					btEdit.setVisible(false);
				}
				abHelper.addButton(btEdit, true, true, "fa-edit",1000);
				
				Button btUpdate = null;
				btUpdate = (Button) dWin.getFellowIfAny("btUpdate" ,true);
				
				//if no update button, create it automatically
				if (btUpdate == null){
					UniLog.log("add btUpdate Button");
					btUpdate = new ZkBiButton();
					//btUpdate.setLabel(sessionHelper.getBtLabel("Save Changes"));
					btUpdate.setSclass("save_record");
					btUpdate.setLabel(sessionHelper.getBtLabel("Save"));
					btUpdate.setImage("images/icons/zkweb/040-file-5-20x20.png");
					btUpdate.setAttribute("tlkey", "bt_detail_update");
					btUpdate.setId("btUpdate");
				}
				btUpdate.setTooltiptext(sessionHelper.getTtLabel("Save Record"));
				btUpdate.setAutodisable("+self");
				abHelper.addButton(btUpdate, true, true, "fa-pencil",1000);
				
				btUpdate.setDisabled(true);
				//zkcb.addHotkey('U', btUpdate); //alt+u seems a bit misleading
				Button btAdd = (Button) dWin.getFellowIfAny("btAdd",false);
				if(btAdd == null) {
					btAdd= new ZkBiButton();
					btAdd.setLabel(sessionHelper.getBtLabel("Save New Record"));
					btAdd.setImage("images/icons/zkweb/040-file-5-20x20.png");
					btAdd.setAttribute("tlkey", "bt_detail_add");
					btAdd.setId("btAdd");
				}
				btAdd.setTooltiptext(sessionHelper.getTtLabel("Save New Record"));
				btAdd.setAutodisable("+self");
				abHelper.addButton(btAdd, true, true, "fa-pencil",1000);
				btAdd.setDisabled(true);
				//zkcb.addHotkey('A', btAdd);
				
				

				Button btClose = (Button) dWin.getFellowIfAny("btClose" ,true);
				if (btClose == null){
					btClose = new ZkBiButton();
					btClose.setLabel(sessionHelper.getBtLabel("Close"));
					btClose.setAttribute("tlkey", "bt_detail_close");
					btClose.setImage("images/icons/zkweb/092-file-cancel-25x25.png");
					btClose.setId("btClose");
			    }
				btClose.setTooltiptext(sessionHelper.getTtLabel("Close Record"));
				abHelper.addButton(btClose, true, true, "fa-window-close-o",1000);
				
				{
					if(result.getSubLinks() != null) {
					for(BiResult sl : result.getSubLinks()) {
						if(sl instanceof AttachmentUploadInterface) {
							Button btAttach = (Button) dWin.getFellowIfAny("btAttach" ,true);
							if (btAttach == null){
								btAttach = new ZkBiButton();
								btAttach.setLabel(sessionHelper.getBtLabel("Attach"));
								btAttach.setAttribute("tlkey", "bt_detail_attach");
								btAttach.setImage("images/icons/zkweb/092-file-cancel-25x25.png");
								btAttach.setId("btAttach");
							}
							btAttach.setTooltiptext(sessionHelper.getTtLabel("Upload Attachement"));
							btAttach.setAttribute("biResult", sl.getView().getName());
							abHelper.addButton(btAttach, true, true, "fa-window-close-o",1000);

							brAttach = sl;
							break;
						}
					}
					}
					
				}
				{
					for(int i=0;i<9;i++) {
						String ss = ((ZkSessionHelper) sessionHelper).getViewExtraJxFormAction(result.getView().getName(), i);
						if(ss == null) break;
			    		String ss2[] = ss.split(","); // 0 : buttonLabel, 1 : buttonIcon, 2 : accessKey, 3 : classPath
						try {
							Button btExtraAction = new ZkBiButton();
							btExtraAction.setLabel(sessionHelper.getBtLabel(ss2[0]));
//							btExtraAction.setAttribute("tlkey", "bt_detail_attach");
//							btExtraAction.setImage("images/icons/zkweb/092-file-cancel-25x25.png");
							btExtraAction.setId("btExtraJxFormAction_"+i);
							abHelper.addButton(btExtraAction, true, true, ss2[1],1000);
//							((JxZkSkin) getSkin()).addOneElementToSkin(bt);
						} catch (Exception ex){
							UniLog.log(ex);
						}
					}
				}
				
				//zkcb.addHotkey('C', btClose);
				if(dDerac == null) {
					dDerac = new DetailDecoration(dWin,p_isMobile,sessionHelper,useCompDiv);
				}
				if(dDerac.dToolBar == null) {
					Hlayout hl = new Hlayout();
					abHelper.setContainerParent(hl);
					Row brow = new Row();
					brow.appendChild(hl);
					dDerac.dRows.appendChild(brow);
				} 
				else {
					abHelper.setContainerParent(dDerac.dToolBar);
				}
				abHelper.attachButtonsToContainer();
			}
	        
			if(result.getParent() == null) {
			JxZkBiBase bf = null;
			
			if(sessionHelper == null || !sessionHelper.isLogin()) {
				UniLog.log("sessionhelper is null or not yet login");
			} 
			else {
				JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
				if(pvdr == null) {
					pvdr = new JxZkGadgetProvider(sessionHelper);
					if(pvdr != null) sessionHelper.putSessionData("jxzkgadgetprovider", pvdr);
				}
				if(pvdr == null) {
					UniLog.log("pvdr is null.");
				} 
				else {
					ZkUtil.translateAllComp(result, dWin);
					//bf = (JxZkBiBase) pvdr.jxzk_forminit(dWin,MapUtil.of("abHelper",abHelper),null);
					
					//andrew200909: pass url parameter and abHelper to construct jxform
					bf = (JxZkBiBase) pvdr.jxzk_forminit(dWin,paramMap,null);

				{
					// 2
					for(int i=0;i<9;i++) {
						JxField jxf = bf.jxAdd("btExtraJxFormAction_"+i);
						if(jxf != null) {
							String ss = ((ZkSessionHelper) sessionHelper).getViewExtraJxFormAction(result.getView().getName(), i);
							if(ss == null) break;
							String ss2[] = ss.split(","); // 0 : buttonLabel, 1 : buttonIcon, 2 : accessKey, 3 : classPath
							try {
								JxActionListener lnr = null;
								Class[]	paramTypes = new Class[]{};
								lnr = (JxActionListener) DynamicClassLoader.newInstance(ss2[3],paramTypes);
								if(lnr != null) {
									jxf.addActionListener(lnr);
								}
								if(lnr instanceof BiActionHandler) {
									if(bf.bahHash == null) {
										bf.bahHash = new Hashtable<String,BiActionHandler>();
									}
									bf.bahHash.put("btExtraJxFormAction_"+i, ((BiActionHandler) lnr));
								}
							} catch (Exception ex){
								UniLog.log(ex);
							}
						}
					}
				}
				
					//abHelper.setDelayClickEvent(); //need to run after formint->new eventlistener
					bf.zkcb = zkcb;
					bf.parentComp = dWin.getParent();
					bf.curComp = dWin;
					bf.curDetailDecoration = dDerac;
				}
			}
			if(bf != null) {
				bf.addPickInputActionListner(result);
//				if (sessionHelper.getAllowPickOldInput())
//					bf.addPickQueryActionListner(result);
			}
			
			//andrew221003 hotfix recordcopy null exception
			if (recordCopy != null) {
				recordCopy.setBiBase(bf);
			}

			if(brAttach != null) {
				final JxZkBiBase fbf = bf;
			bf.setGipi(brAttach.getView().getName(),new BiGetItemProperty(brAttach) {
				@Override
				public void onValueChanged(Object p_value,int p_ctype) {
					ColumnCell cc = (ColumnCell) p_value;
					BiCellCollection bcc = cc.getCollection();
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED) {
						if(cc.getCellLabel().equals("mdoc_download")){
							String fileName;
							if (bcc.testCell("mdoc_remark") != null)
								fileName = bcc.getCellString("mdoc_remark");
							else
								fileName = "download";
							UniLog.log1("%s clicked filekey:%s, filename:%s", cc.getCellLabel(), bcc.getString("mdoc_doctype"), bcc.getString("mdoc_filekey"), fileName);
							ZkUtil.downloadFileFromFiling(result.getSessionHelper(), bcc.getString("mdoc_doctype"), bcc.getString("mdoc_filekey"), fileName);
						} else {
							fbf.setDirtyFlag(true);	
						}
					}
				}
				}
			);
			}
			
			return(bf);
			} else {
				return(null);
			}
    	}
    	catch(Exception ex){
    		UniLog.log(ex);
    		return(null);
    	}
    }
	
   	/***
   	 * perform action when after the form displayed
   	 * action code filled in child class
   	 * e.g. simulate user click a button
   	 * @param p_comp
   	 */
   	public void afterDisplayAction(Component p_comp, int p_mode){
   		//action code will be filled by child class
   	}
   	
   	/***
   	 * init the form elements, call when doUpdate/doAdd/doModalUpdate/doModalAdd
   	 * @param p_mode
   	 */
   	public void initForm(int p_mode){
		if (!checkBr()) return;
		jxSetVisible("btUpdate",p_mode == MODE_UPDATE);
		jxSetVisible("btAdd", p_mode == MODE_ADD);
		jxSetVisible("btEdit", p_mode == MODE_DISPLAY);
		
		if(br != null && !br.allowUpdate()) {
			jxSetVisible("btUpdate", false);
		}
		if(p_mode == MODE_ADD && defaultAddCloseAction == CloseAction.Close /* CdefaultAddAndCloseFlag */) {
			jxSetVisible("btNext",false);
			jxSetVisible("btPrevious",false);
		} else {
			jxSetVisible("btNext",true);
			jxSetVisible("btPrevious",true);
		}
		if((p_mode == MODE_UPDATE  && defaultUpdateCloseAction == CloseAction.Reload /* defaultUpdateAndCloseFlag */) || isAddModeWhenOpen) {
			jxSetVisible("btNext",false);
			jxSetVisible("btPrevious",false);
		} else {
			jxSetVisible("btNext",true);
			jxSetVisible("btPrevious",true);
		}
		jxSetVisible("btReloadDetail", allowReloadDetail && p_mode == MODE_UPDATE);

		if (sessionHelper.getAllowRecordCopy()) {
			jxSetEnable("btRecordCopyWithCopy", p_mode == MODE_UPDATE);
			jxSetEnable("btRecordCopyWithCopyAndAdd", p_mode == MODE_UPDATE);
			jxSetEnable("btRecordCopyWithExportToFile", p_mode == MODE_UPDATE);
		}
		
		//abHelper.attachButtonsToContainer();
		abHelper.showActionButtonMenu();
		abHelper.setDelayClickEvent();
		
		//init "editing" tag
		/*
		if (curDetailDecoration != null && curDetailDecoration.grid != null){
			curDetailDecoration.grid.setSclass("");
		}
		*/
   	}
	
	public void doDisplay() {
		isAddModeWhenOpen = false;
		if (abortFlag) return;
		initForm(MODE_DISPLAY);
		showForm();	
		zkcb.biBaseOpen();
		afterDisplayAction(curComp, MODE_DISPLAY);
	}
	public void doUpdate() {
		isAddModeWhenOpen = false;
		if (abortFlag) return;
		initForm(MODE_UPDATE);
		showForm();	
		zkcb.biBaseOpen();
		afterDisplayAction(curComp, MODE_UPDATE);
	}
	public void doAdd(){
		isAddModeWhenOpen = true;
		if (abortFlag) return;
		initForm(MODE_ADD);
		showForm();	
		zkcb.biBaseOpen();
		afterDisplayAction(curComp, MODE_ADD);
	}
	public void doModalUpdate() {
		isAddModeWhenOpen = false;
		if (abortFlag) return;
//		defaultUpdateAndCloseFlag = true;
		defaultUpdateCloseAction = CloseAction.Reload;
		initForm(MODE_UPDATE);
		modalForm();	
		if(zkcb != null) zkcb.biBaseOpen();
		afterDisplayAction(curComp, MODE_UPDATE);
	}
	public void doModalAdd() {
		isAddModeWhenOpen = false;
		if (abortFlag) return;
//		defaultAddAndCloseFlag = true;
		defaultAddCloseAction = CloseAction.Close;
		initForm(MODE_ADD);
		modalForm();	
		if(zkcb!=null)zkcb.biBaseOpen();
		afterDisplayAction(curComp, MODE_ADD);
	}
	
	protected ReturnMsg beforeUpdateLink(BiResult sr,int idx)
	{
		return(null);
	}
	protected ReturnMsg beforeDeleteLink(BiResult sr,int idx)
	{
		return(null);
	}
	protected ReturnMsg beforeUnDeleteLink(BiResult sr,int idx)
	{
		return(null);
	}
					
	protected void afterDeleteLink(BiResult sr,int idx)
	{
	}
	protected void afterUnDeleteLink(BiResult sr,int idx)
	{
	}
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
//		return(br.doBeforeAdd(cl));
		return(null);
	}
	protected void afterAddLink(BiResult sr,int idx)
	{
	}
	protected void linkDetailPickInputOpened(BiResult sr, JxField fd)
	{
	}
	protected void linkDetailPickInputClosed(BiResult sr, JxField fd)
	{
	}
	
	
	protected ReturnMsg beforeAdd(BiResult br)
	{
		return(ReturnMsg.defaultOk);
	}
	protected ReturnMsg afterAdd(BiResult br)
	{
		return(ReturnMsg.defaultOk);
	}
	protected ReturnMsg beforeUpdate(BiResult br)
	{
		return(ReturnMsg.defaultOk);
	}
	protected ReturnMsg afterUpdate(BiResult br)
	{
		return(ReturnMsg.defaultOk);
	}
	
	protected void onListEditRowAction(JxField fd,int col,int row) {
		
	}

	/*
	//obsoleted
	protected void onRowBtnClick(JxField fd) {
		Component comp = (Component) fd.getNativeObject();
		if (comp == null) {
			UniLog.log1("comp is null");
			return;
		}
		Event event = (Event) comp.getAttribute("rowBtnEvent");
		if (event == null || !(event.getTarget() instanceof Button)){
			UniLog.log1("invalid event or event target");
			return;
		}
		Button targetBtn = (Button) event.getTarget();
		UniLog.log1("DEBUG %s %s col:%s row:%s label:%s", comp.getId(), targetBtn.getId(), targetBtn.getAttribute("rowBtnCol"), targetBtn.getAttribute("rowBtnRow"), targetBtn.getAttribute("rowBtnLabel"));
	}
	*/
	
    public static Component getDetailWindow(Component p_masterWin, BiResult p_result){
		Component dWin = p_masterWin.getFellowIfAny("JxZkBiBase");
		if (dWin == null){
			dWin = p_masterWin.getFellowIfAny(p_result.getView().getName());
		}
		return(dWin);
    }
	public static class ComponentsFocusManage {
		private SessionHelper sessionHelper;
		private final Map<HtmlBasedComponent, Integer> collectedTabCompMap = new LinkedHashMap<HtmlBasedComponent, Integer>();
		private final Map<Integer, Map.Entry<HtmlBasedComponent, Integer>> tabCompMap = new TreeMap<Integer, Map.Entry<HtmlBasedComponent, Integer>>();
		private final List<Map.Entry<HtmlBasedComponent, Integer>> tabCompList = new ArrayList<Map.Entry<HtmlBasedComponent, Integer>>();
		public ComponentsFocusManage() {
			sessionHelper = ZkSessionHelper.getSessionHelper();
		}
		public ComponentsFocusManage(SessionHelper sessionHelper) {
			this.sessionHelper = sessionHelper;
		}
		public void clear() {
			collectedTabCompMap.clear();
			tabCompMap.clear();
			tabCompList.clear();
		}
		public void collectAllTabComps(Component comp) {
			collectAllTabComps(comp, collectedTabCompMap, 1);
		}
		private void collectAllTabComps(Component comp, Map<HtmlBasedComponent, Integer> map, int index) {
			if (!sessionHelper.getAllowFocusIndex() || comp.getChildren() == null)
				return;
			for (Component child : comp.getChildren()) {
				//UniLog.log1("child:%s:%s:%s", child.getClass().getName(), child.getId(), (child instanceof HtmlBasedComponent));
				if (child instanceof HtmlBasedComponent){
					map.put((HtmlBasedComponent)child, index++);
				}
				collectAllTabComps(child, map, index);
			}
		}
		public void setFocusFirstComp(Component p_dWin) {
			if (!sessionHelper.getAllowFocusIndex() || tabCompList.isEmpty())
				return;
			HtmlBasedComponent firstComp = tabCompList.get(0).getKey();
			if (!checkTabComp(firstComp, false))
				firstComp = findNextTabComp(firstComp);
			if (firstComp == null)
				return;
			final HtmlBasedComponent firstComp1 = firstComp;
	 		final Timer initTimer = new Timer();
	    	initTimer.setPage(p_dWin.getPage());
	    	initTimer.setDelay(1);
	    	initTimer.setRepeats(false);
	    	initTimer.addEventListener(Events.ON_TIMER, new ZkBiEventListener<Event>() {
	    		@Override
	    		public void onZkBiEvent(Event event) throws Exception {
	     			UniLog.log("setfocus first component " + event.getTarget());
	    			initTimer.setRunning(false);
	    			initTimer.detach();
	    			firstComp1.setFocus(true);
	    		}
	    	});
	    	initTimer.setRunning(true);
		}
		/*
		public boolean checkTabComp(HtmlBasedComponent comp) {
	   		if (comp instanceof InputElement) {
	   			if (!((InputElement)comp).isDisabled())
	   				return true;
	   		} 
	   		return false;
		}
		public boolean checkTabComp1(HtmlBasedComponent comp) {
	   		if (comp instanceof InputElement) {
	   			if (!((InputElement)comp).isDisabled())
	   				return true;
	   		} else if (comp.isVisible())
	   			return true;
	   		return false;
		}
		*/
		
		//andrew190826: combine checkTabComp. revise validation logic
		public boolean checkTabComp(HtmlBasedComponent comp, boolean p_requireVisible) {
			if (p_requireVisible && !comp.isVisible()) {
				return false;
			}
	   		if (comp instanceof Disable && comp instanceof ComponentCtrl) {
	   			if (((Disable) comp).isDisabled()){
	   				return false;
	   			}
	   			return true;
	   		} 
	   		return false;
		}
		private HtmlBasedComponent findNextTabComp(HtmlBasedComponent comp) {
			int foundIndex = -1;
	   		for (int i = 0; i < tabCompList.size(); i++) {
	   			if (tabCompList.get(i).getKey() == comp) {
	   				foundIndex = i;
	   				break;
	   			}
	   		}
			return foundIndex >= 0 ? findNextTabComp(foundIndex) : null;
		}
		private HtmlBasedComponent findNextTabComp(int i) {
	   		HtmlBasedComponent nextComp = (i < tabCompList.size() - 1) ? tabCompList.get(i + 1).getKey() : null;
	   		if (nextComp == null)
	   			return null;
   			return checkTabComp(nextComp, false) ? nextComp : findNextTabComp(i + 1);
		}
		public void setFocusNextComp() {
			if (!sessionHelper.getAllowFocusIndex())
				return;
	   		for (int i = 0; i < tabCompList.size(); i++) {
	   			final HtmlBasedComponent curComp = tabCompList.get(i).getKey();
	   			final HtmlBasedComponent nextComp = (i < tabCompList.size() - 1) ? tabCompList.get(i + 1).getKey() : null;
	   			EventListener<Event> el = new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
	   					UniLog.logm(this, "enter pressed 1 " + event.getTarget());
	   					setFocusNextComp(curComp, nextComp);
					}
	   			};
	   			curComp.setAttribute("eventlistener_ok1", new AbstractMap.SimpleEntry<EventListener<Event>, Boolean>(el, true));
	   			curComp.addEventListener(Events.ON_OK, el);
	   			curComp.addEventListener(Events.ON_SELECT, new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
	   					UniLog.logm(this, "selected " + event.getTarget());
	   					setFocusNextComp(curComp, nextComp);
					}
	   			});
	   			/*
	   			//enable cursor jump to next field feature
	   			//side effect: user enter date manually, cursor will jump during middle of input
	   			if (curComp instanceof Datebox) {
	   				curComp.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
	   					@Override
						public void onEvent(Event event) throws Exception {
							UniLog.log("change " + event.getTarget());
							setFocusNextComp(curComp, nextComp);
						}
					});
	   			}
	   			*/
	   		}
		}
		private void setFocusNextComp(HtmlBasedComponent curComp, HtmlBasedComponent nextComp) {
			if (nextComp != null && !checkTabComp(nextComp, false))
				nextComp = findNextTabComp(nextComp);
			if (nextComp != null) {
				nextComp.setFocus(true);
				UniLog.logm(this, "setFocusNextComp " + nextComp);
			} else {
				boolean found = false;
				for (HtmlBasedComponent comp : collectedTabCompMap.keySet()) {
					if (!found) {
						if (comp == curComp)
							found = true;
					} else if (checkTabComp(comp,true)) {
						comp.setFocus(true);
						UniLog.logm(this, "setFocusNextComp " + comp);
						break;
					}
				}
			}
		}
		public void addComponent(Component comp) {
	    	if (comp instanceof HtmlBasedComponent && sessionHelper.getAllowFocusIndex()) {
	    		Integer collectedTabCompMapIndex = collectedTabCompMap.get(comp);
	    		if (collectedTabCompMapIndex != null) {
	    			Map.Entry<HtmlBasedComponent, Integer> entry = new AbstractMap.SimpleEntry<HtmlBasedComponent, Integer>((HtmlBasedComponent)comp, null);
	    			entry.setValue(((HtmlBasedComponent)comp).getTabindexInteger());
	    			tabCompMap.put(collectedTabCompMapIndex, entry);
	    			UniLog.logm(this, "addComponent: " + comp);
	    		}
	    	}
		}
		public void addComponent(List<Component> compList) {
			for (Component comp : compList)
				addComponent(comp);
		}
		public boolean process() {
			int j = 1;
			for (Map.Entry<HtmlBasedComponent, Integer> entry : tabCompMap.values()) {
				HtmlBasedComponent comp = entry.getKey();
				Integer tabIndex = entry.getValue();
				if (tabIndex == null)
					tabIndex = j;
				tabCompList.add(new AbstractMap.SimpleEntry<HtmlBasedComponent, Integer>(comp, tabIndex));
				j++;
			}
			Collections.sort(tabCompList, new Comparator<Map.Entry<HtmlBasedComponent, Integer>>(){
				@Override
				public int compare(Entry<HtmlBasedComponent, Integer> o1, Entry<HtmlBasedComponent, Integer> o2) {
					return o1.getValue() - o2.getValue();
				}
			});
			return !tabCompList.isEmpty();
		}
		public List<HtmlBasedComponent> getComponentList() {
			List<HtmlBasedComponent> list = new ArrayList<HtmlBasedComponent>();
			for (Map.Entry<HtmlBasedComponent, Integer> entry : tabCompList)
				list.add(entry.getKey());
			return list;
		}
		public HtmlBasedComponent getLastCanFocusComponent() {
			HtmlBasedComponent c = null;
			for (Map.Entry<HtmlBasedComponent, Integer> entry : tabCompList) {
				if (checkTabComp(entry.getKey(), false))
					c = entry.getKey();
			}
			return c;
		}
		public boolean isLastCanFocusComponent(HtmlBasedComponent comp) {
			return getLastCanFocusComponent() == comp;
		}
		
	}
	static public JxForm getOrCreateJxZkForm(Component dWin,JxZkGadgetProvider pvdr , String formName)
	{
			JxForm jxf = null;
//			jxf = pvdr.jxGetForm(instanceName);
//			if(jxf != null) return(jxf);
			try {
				Class jxFormClass = Class.forName("com.uniinformation.jxapp." + formName);
				if(jxFormClass == null) return(null);
				dWin.setId(formName);
				
			} 
			catch (Exception ex) {
				UniLog.logm("com.uniinformation.jxapp.%s not found, use JxZkBase" , formName);
				dWin.setId("JxZkBase");
			}
			String formTemplate = "/viewforms/"+formName.replaceAll("\\.", "/")+".zul";
			try {
				Executions.getCurrent().createComponents(formTemplate, dWin, null);
			} 
			catch (Exception ex) {
				UniLog.logm("zul %s not found" , formTemplate);
			}
			jxf = pvdr.jxzk_forminit(dWin);
//			jxf = pvdr.jxzk_forminit(dWin,null,instanceName);
			return(jxf);
	}
	
	@Override
	public void doKeepAlive() {
		if (!checkBr()) return;
		UniLog.log("doKeepAlive fbegin " + br.inBeginWork());
		br.getSelectUtil().setAliveUntil(10000);
	}	
	
	protected static ReturnMsg listboxAddRow(JxZkBiBase p_biBase, BiResult p_sl, JxField p_svx, JxField p_fd, int p_insIdx){
		UniLog.logm(null,"listbox insert idx:%d", p_insIdx);
		CellCollection col = p_sl.newRowCollection();
	
		//beforeAddLink block
		ReturnMsg rtnMsg = p_biBase.beforeAddLink(p_fd,p_sl,col,p_insIdx) ;
		if(rtnMsg != null && !rtnMsg.getStatus()) {
			p_biBase.messageBox(rtnMsg.getMsg());
			return (rtnMsg);
		}
	
		if(p_biBase.getGipi(p_sl.getView().getName()) != null) {
			//addSubRecord
			ReturnMsg rtn = p_sl.addSubRecord(col, p_insIdx,"");
			if(rtn == null || !rtn.getStatus()) {
				UniLog.logm(null, "addSubRecord Failed");
				return(rtn);
			}
			Object tr = rtn.getData();
			int rowIdx = p_biBase.getGipi(p_sl.getView().getName()).getIndexOf(tr);
			//p_svx.addItemToList(tr, p_insIdx);
			p_svx.addItemToList(tr, rowIdx);
			p_svx.gridSetCurrentRow(-1);
			//p_svx.gridSetDataFormat(-1, rowIdx,"add_inserted");  //gipi not required to set attribute, can obtain the status from gipi interface directly
			Vector sv = p_svx.getSelectList();
			p_biBase.afterAddLink(p_sl, rowIdx);
			p_biBase.setDirtyFlag(true);
			
			//listbox updatebtn has duplicate id problem, so post click event by id no longer work
			//ZkBiComposerBase.postEventDelay(Events.ON_CLICK,(Component)p_fd.getNativeObject(),String.format("gipi btupadd_list_row%d_list_%s",rowIdx,p_sl.getView().getName()),null,10);
		} 
		else {
			//addSubRecord
			ReturnMsg rtn = p_sl.addSubRecord(col, -1,"");
			if(rtn == null || !rtn.getStatus()) {
				UniLog.logm(null, "addSubRecord Failed");
				return(rtn);
			}
			Object tr = rtn.getData();
			int rowIdx = p_sl.getRowCount() - 1;
			p_svx.gridSetRow(p_sl.getRowCount());
			Vector<BiColumn> subCols = p_sl.getListColumns();
			for(int k=0;k<subCols.size();k++) {
				BiColumn ccl = (BiColumn) subCols.get(k);
				Cell ce = col.testCell(ccl.getLabel());
				if(ce != null) {
					p_svx.gridSetValue(k, rowIdx, ce);
				}
			}
			p_svx.gridSetDataFormat(-1,rowIdx,"add_inserted");
			p_biBase.afterAddLink(p_sl,rowIdx);
			p_biBase.setDirtyFlag(true);
			
			//listbox updatebtn has duplicate id problem, so post click event by id no longer work
			//ZkBiComposerBase.postEventDelay(Events.ON_CLICK,(Component)p_fd.getNativeObject(),String.format("btupdate_list_row%d_list_%s",rowIdx,p_sl.getView().getName()),null,10);
		}
		return(new ReturnMsg(true));
		
	}
	/***
	 * 
	 * @param p_biBase
	 * @param p_sl
	 * @param p_svx
	 * @param p_insIdx 
	 *          INS_IDX_APPEND - append to tail, 
	 *	 		INS_IDX_ACTIONIDX - based on action index
	 *          >= 0 - user defined position
	 * 
	 * @return JxActionListener
	 */
	public static JxActionListener genListboxAddActionListener(final JxZkBiBase p_biBase, final BiResult p_sl, final JxField p_svx, final int p_insIdx){
		
		JxActionListener newActionListener = new JxActionListener(){
			public void actionPerformed(JxField fd) {
				//obtain insert position
				int insIdx = p_insIdx;
				if (p_insIdx == INS_IDX_ACTIONIDX){
					//try to obtain action index from zk component
					Integer actionIdx = (Integer)ZkUtil.getAttribute((Component) fd.getNativeObject(), "actionIdx", 3);
					if (actionIdx == null){
						actionIdx = -1;
					}
					//insIdx = actionIdx;
					if (actionIdx >= 0){
						insIdx = actionIdx + 1; //insert after actionIdx
					}
					else{
						insIdx = 0;  //insert at first
					}
					UniLog.logm(this, "insIdx:%d actionIdx:%d", p_insIdx, actionIdx);
				}
				
				//obtain insert cnt
				int multipleCnt = 1;
				try{
					if (fd.getActionEvent() != null && ((Event)fd.getActionEvent()).getData() != null){
						org.json.JSONObject json = new org.json.JSONObject(((Event)fd.getActionEvent()).getData().toString());
						multipleCnt = json.getInt("multipleCnt");
						UniLog.logm(this, "multipleCnt = %d", multipleCnt);
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				for (int i=0; i<multipleCnt; i++){
					ReturnMsg addRowResult = listboxAddRow(p_biBase, p_sl, p_svx, fd, insIdx);
					if (!addRowResult.getStatus()){
						return;
					}
				}
			}
		};
		return(newActionListener);
	}
	
	/***
	 * get current session helper from attribute
	 * @return
	 */
//	public static SessionHelper getSessionHelper(){
//		return((SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath())));	
//	}
	
    public Window newPopupWindow(String p_title){
   		if (parentComp == null){
   			UniLog.logm(this, "parentComp is null, force ignore");
   		}
   		return(ZkUtil.newPopupWindow(p_title, parentComp));
//    	try{
//			Window pWin = new Window();
//			pWin.setBorder("normal");
//        	pWin.setTitle(p_title);
//			pWin.setWidth("300px");  //should not hard code window size
//			pWin.setHeight("200px");
//			pWin.setClosable(true);
//			
//    		if (parentComp == null){
//    			UniLog.logm(this, "parentComp is null, force ignore");
//    		}
//    		else{
//    			pWin.setParent(parentComp);
//    		}
//			pWin.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
//			public void onEvent(Event event) throws Exception {
//				event.getTarget().setVisible(false);
//				event.stopPropagation();
//			}});
//			return(pWin);
//    	}
//    	catch(Exception ex){
//    		UniLog.log(ex);
//    		return(null);
//    	}
    }
	public static int getFrozenCount(boolean p_hasActionCol, SessionHelper p_sessionHelper){
//		return(3);
		
		int count = 0;
		if (p_hasActionCol && p_sessionHelper.getFrozenActionCol()){
			count++;
		}
		if (p_sessionHelper.getFrozenFirstCol()){
			count++;
		}
		return(count);
	}
	
	public static void setFocusComponent(ColumnCell p_bcc,Component p_parentComp) {
		ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) p_bcc.getMapper();
		final Component comp = zcvm.getComponent();
		new ZkBiAbstractLongOp(p_parentComp, null){
			@Override
			public ReturnMsg longOp() {
				((HtmlBasedComponent) comp).focus();
				return null;
			}
		};
	} 
	protected void formDirtyChanged() {
		if(bahHash != null) {
			for(String bt : bahHash.keySet()) {
				BiActionHandler bah = bahHash.get(bt);
				jxSetEnable(bt, !bah.isDisabled(getBr(), false));
			}
		}
	}
	public void abort(){
		abort(false);
	}
	public synchronized void abort(boolean p_closeFlag){
		abortFlag = true;
		if (p_closeFlag){
			doClose(false);
		}
	}
	protected ReturnMsg doAfterLockRecord(ReturnMsg p_rtnmsg) {
		return(p_rtnmsg);
	}
	/***
	 * add right click menu.
	 * @param p_parentComp - parent component for store contextMenu.
	 *                       parent component must be attached to root component
	 * @param p_optionMap - define menu item
	 *                      "changeLabel" - change label option. valueMap:key,defaultValue
	 */
	public static void addContextMenu(final SessionHelper p_sh, XulElement p_parentComp, Map p_optionMap){
		try{
			//validation
			if (p_parentComp == null){
				UniLog.log1("parentComp is null");
				return;
			}
			if (p_parentComp.getRoot() == null){
				UniLog.log1("parentComp.getRoot() is null");
				return;
			}
			if (p_sh == null){
				UniLog.log1("sessionHelper is null");
				return;
			}

			//handle options
			ArrayList<Menuitem> items = new ArrayList<Menuitem>();
			for (String optionKey :  (Set<String>) p_optionMap.keySet()){
				//UniLog.log1("%s:%s:hasChangeLabel:%s", p_parentComp, p_parentComp.getId(), p_parentComp.getAttribute("hasChangeLabel"));
				if (StringUtils.equals(optionKey, "changeLabel") && p_sh.getAllowUpdateTranslate() && !"Y".equals(p_parentComp.getAttribute("hasChangeLabel"))){
					//get option value
					if (p_optionMap.get(optionKey) == null || !(p_optionMap.get(optionKey) instanceof Map)){
						UniLog.log1("%s value is null, ignore", optionKey);
						continue;
					}
					Map valueMap = (Map) p_optionMap.get(optionKey);
					final String translateKey = MapUtil.getString(valueMap, "key","").toUpperCase();
					final String defaultValue = MapUtil.getString(valueMap, "defaultValue");
					if (StringUtils.isBlank(translateKey)){
						UniLog.log1("translateKey is blank");
						continue;
					}
					final String translateType;
					/*
					if (p_parentComp instanceof Button || p_parentComp instanceof Checkbox || p_parentComp instanceof Tab){
						translateType = "BUTTON";
					}
					else if (p_parentComp instanceof Radio){
						translateType = "OPTION";
					}
					else{
						translateType = "LABEL";
					}
					*/
					//andrew230531 fix cannot detect radio
					if (p_parentComp instanceof Radio){
						translateType = "OPTION";
					}
					else if (p_parentComp instanceof Button || p_parentComp instanceof Checkbox || p_parentComp instanceof Tab){
						translateType = "BUTTON";
					}
					else{
						translateType = "LABEL";
					}
					
					Menuitem menuitem = new Menuitem("Change " + (translateType.equals("OPTION") ? "Option" : "Label") + " #"+ translateKey);
					menuitem.setIconSclass("z-icon-cog");
					menuitem.addEventListener("onClick", new ZkBiEventListener<Event>(){
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							ZkBiTranslateHelper.newPopupWin(p_sh, null, translateKey, translateType, defaultValue);
						}
					});
					items.add(menuitem);
					p_parentComp.setAttribute("hasChangeLabel", "Y");
				}
			}
			if (items.size() > 0){
				//try to get old context menu
				Menupopup contextMenu = (Menupopup) p_parentComp.getAttribute("contextMenu");
				//create new context menu
				if (contextMenu == null){
					contextMenu = new Menupopup();
					p_parentComp.setContext(contextMenu);
					contextMenu.setParent(p_parentComp.getRoot());
					p_parentComp.setAttribute("contextMenu", contextMenu);
				}
				else{
					UniLog.log1("contextMenu already exist, append item to it");
				}
				for (Menuitem item : items){
					contextMenu.appendChild(item);
				}
			}
			else{
				//UniLog.log1("items is empty");
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/*
	public void setAddAndClose(boolean p_sw) {
		defaultAddAndCloseFlag = p_sw;
	}
	public void setUpdateAndClose(boolean p_sw) {
		defaultUpdateAndCloseFlag = p_sw;
	}
	public static String replaceViewName(String p_vname) {
		return(p_vname.replace(".", "_"));
	}
	*/
	public void setAddAndClose(CloseAction p_action) {
		defaultAddCloseAction = p_action;
	}
	public void setUpdateAndClose(CloseAction p_action) {
		defaultUpdateCloseAction = p_action;
	}
	protected boolean addToolBarButton(String p_id,String p_label,String p_icon) {
		IdSpace isp = (IdSpace) getNativeComponent();
		Button btn = null;
		if(!isp.hasFellow(p_id,true)) {
			if(isp.hasFellow("btClose",true)) {
				Component ca = isp.getFellow("btClose",true);
				btn = new ZkBiButton(p_label);
				btn.setId(p_id);
				btn.setParent(ca.getParent());
				((JxZkSkin) getSkin()).addOneElementToSkin(btn);
			};
		
		} else {
			btn = (Button) isp.getFellow(p_id);
		}
		if(btn != null) {
				abHelper.addButton(btn,p_icon);	
				return(true);
		} else {
				return(false);
		}
	}
	

	protected String makeKeyFromField(JxField p_fd) {
		String s = p_fd.getName();
		return(getName()+"."+p_fd.getName());
	}
	
	
	public static void pickBySelect(SessionHelper p_sh,String p_view,String p_condition,final EventListener p_listener) throws Exception {
		BiSchema schema = BiSchema.loadSchema(p_sh);
		BiResult br = schema.getViewByName(p_view).newBiResult(p_sh.getLoginId(), null, null, p_sh);
		JxSelOpt selopt = JxSelOpt.createPopupJxSelOpt(p_sh);
		pickBySelect(selopt,br,p_condition,p_listener);
	}
	public static void pickBySelect(BiResult p_br,String p_condition,final EventListener p_listener) throws Exception {
		JxSelOpt selopt = JxSelOpt.createPopupJxSelOpt(p_br.getSessionHelper());
		pickBySelect(selopt,p_br,p_condition,p_listener);
	}
	public static void pickBySelect(final JxSelOpt p_selopt,BiResult p_br,String p_condition,final EventListener p_listener) throws Exception {
		BiResult pbr = p_br;
		pbr.clear();
		pbr.clearCondition();
		pbr.clearOrderBy();
		if(p_condition != null) pbr.addCustomCondition(p_condition);
		pbr.query();
		p_selopt.setOnSelectAction (
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object o  = fd.getValue();
					BiGetItemProperty gipi = (BiGetItemProperty) p_selopt.getUserData();
					CellCollection col = gipi.getCellCollectionByValue(o);
					p_selopt.closeForm();
					if(p_listener != null) {
						try {
							p_listener.onEvent(new Event(EV_ON_SELOPT_OK,null,col));
						} catch (Exception ex) {
							UniLog.log(ex);
						}
					}
				}
			}
		);
		BiGetItemProperty gipi = new BiGetItemProperty(pbr);
		gipi.setItemMode(BiGetItemProperty.GETITEM_MODE_PICK);
		p_selopt.jxAdd("pickListBox").setItemListInterface( gipi);
		p_selopt.setUserData(gipi);
		p_selopt.setPopupWidth(""+gipi.getRowWidth()+"px");
		p_selopt.modalForm();
	}		
	
	/***
	 * use method to wrap the br object. workaround for br is null exception.
	 * child class please use this method to obtain br
	 * @return BiResult
	 */
	public BiResult getBr() {
		if (br == null) {
			throw new ZkBiRuntimeException("br is null");
			//throw new ZkBiRuntimeException("br is null").setIgnore();
		}
		return br;
	}
	
	public BiResult optBr() {
		return br;
	}
	
	public void onCellMapp(ColumnCell p_cc) {
	}
	
	public void disableDeleteLink(BiResult p_br,String p_subLinkName) {
		JxField fd = jxAdd("list_"+replaceViewName(p_subLinkName));
		if(fd != null) {
			fd.setAttribute("mode", "nocheckmark");
		}
	}
	public void onBarcode(String p_barcode) {
		
	}

	protected void afterPickField(String p_FieldName) {
		
	}
	
	protected void linkClickedAction(BiResult p_sr,int p_rowIdx,int p_actionType) {
		
	}
	
	protected ReturnMsg allowDisplayToUpdate() {
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	public JxField jxAdd(String p_fieldname) {
		JxField jxf = super.jxAdd(p_fieldname);
		if (jxf == null) return null;
		Object natObj = jxf.getNativeObject();
		if (natObj instanceof Button && !(natObj instanceof Toolbarbutton) && abHelper != null && !abHelper.checkButtonExist(natObj)) {
			abHelper.addButton((Button)jxf.getNativeObject());
		}
		return jxf;
		
	}
	
	/***
	 * handle call for obtain jxf of view
	 * @param p_view
	 * @return
	 */
	public JxField jxAdd(BiView p_view) {
		if (p_view == null) return null;
		return jxAdd("list_" + replaceViewName(p_view.getName()));
	}
	public JxField jxAdd(BiResult p_br) {
		if (p_br == null) return null;
		return jxAdd("list_" + replaceViewName(p_br.getView().getName()));
	}
	
	
	/***
	 * enable record locking
	 * you can call in inside afterBind
	 * @param flag
	 * @return
	 */
	public boolean setRecLock(boolean flag) {
		boolean orgFlag = LOCK_RECORD_FOR_UPDATE;
		LOCK_RECORD_FOR_UPDATE = flag;
		return orgFlag;
	}
	
	//for reset the auto disabled button state
	public void resetAutoDisableButton(Object p_obj) {
		Button button = null;
		if (p_obj instanceof JxField) {
			JxField fd = (JxField) p_obj;
			if (fd.getNativeObject() instanceof Button) {
				button = (Button) fd.getNativeObject();
				button.setDisabled(!fd.getEnable());
			}
		}
		UniLog.log1("object not supported: " + p_obj);
	}
	
	/***
	 * add custom interactive validation by override this method
	 * @param fd
	 * @param mode
	 * @return
	 */
	protected ReturnMsg validateAddUpdate(JxField fd, int mode) {
		return ReturnMsg.defaultOk;
	}
	
	public void translateAllComp(BiResult p_result){
		ZkUtil.translateAllComp(p_result, curComp);
	}

	public void setUserData(Object p_userdata) {
		userData = p_userdata;
	}

	public Object getUserData() {
		return userData;
	}
	class ActionFormListener implements EventListener
	{
		void processAction(Component p_target,boolean p_needResponse,InputStream p_upload) throws Exception {
			  String actionHandler = (String) p_target.getAttribute("actionHandler");
			  if(actionHandler != null) {
			    	Class cl = Class.forName(actionHandler);
			    	Constructor constructor = cl.getConstructor((Class[]) null);
			    	ZkfAction zkfa = (ZkfAction) constructor.newInstance();
			    	String actionId = (String) p_target.getAttribute("actionId");
			    	if(actionId == null) actionId = p_target.getId();
			    	String actionData = (String) p_target.getAttribute("actionData");
			    	JsonObject actionDataJson = GsonUtil.createJsonObject(actionData);
			    	ReturnMsg msg = zkfa.processAction(actionId, sessionHelper,getBr().getCurrentCollection(),actionDataJson,p_upload,p_target);
			    	if(p_needResponse) {
			    		if(msg.getStatus()) {
			    			ZkUtil.msg("Request OK " + msg.getMsg());
			    			//Messagebox.show("Request OK " + msg.getMsg());
			    		} else {
			    			ZkUtil.errMsg("Failed " + msg.getMsg());
			    			//Messagebox.show("Failed " + msg.getMsg());
			    		}
			    	}
			  }
		}
		
		@Override
		public void onEvent(final Event p_event) throws Exception {
			String confirmMsg = (String) p_event.getTarget().getAttribute("confirmMsg");
			String uploadFileType = (String) p_event.getTarget().getAttribute("uploadType");
			String strNeedResponse = (String) p_event.getTarget().getAttribute("needResponse");
			if(uploadFileType != null) {
//    		    Fileupload.get(new ZkBiEventListener <UploadEvent>(){
				final boolean needResponse;
				if(strNeedResponse == null) needResponse = true; else {
					needResponse = strNeedResponse.equals("Y");
				}
				HashMap<String,Object> params = new HashMap<String,Object>();
    		    Fileupload.get(
    		    	params,
    		    	"message",
    		    	"title",
    		    	-1,-1,true,
    		    	new ZkBiEventListener <UploadEvent>(){
    		    		@Override
						public void onZkBiEvent(UploadEvent event) throws Exception {
    		    			// TODO Auto-generated method stub
						    org.zkoss.util.media.Media media = event.getMedia();
						    if(media != null) {
						    	try  {
						    		InputStream is = media.getStreamData();
						    		processAction(p_event.getTarget(),needResponse,is);
						    		is.close();
						    	} catch (Exception ex) {
						    		UniLog.log(ex);
						    		throw(ex);
						    	}
						    }
						
    		    		}
    		    	}
    		    );
				
			} else if(confirmMsg != null) {
				final boolean needResponse;
				if(strNeedResponse == null) needResponse = true; else {
					needResponse = strNeedResponse.equals("Y");
				}
			    Messagebox.show(confirmMsg, "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			    	     new EventListener() {
			    	       public void onEvent(Event evt) throws Exception {
			    	    	   if (((Integer)evt.getData()) == Messagebox.YES){
			    	    		   processAction(p_event.getTarget(),needResponse,null);
//			    	    		   onOkPressed(sessionHelper,ZkCellActionForm.this);
			    	    	   } else{
			    	    	   }
			    	      }
			    	    }
			    );
			} else {
				final boolean needResponse;
				if(strNeedResponse == null) needResponse = false; else {
					needResponse = strNeedResponse.equals("Y");
				}
			    processAction(p_event.getTarget(),needResponse,null);
			}
		}
	};
	
	@Override
	protected void afterBindOneField(JxSkinElement jxse,boolean bindOK) {
		if(jxse.getNativeComponent() instanceof Button) {
			Button bt = (Button) jxse.getNativeComponent();
			String actionHandler = (String) bt.getAttribute("actionHandler");
			if(actionHandler != null) {
				bt.addEventListener(Events.ON_CLICK, new ActionFormListener());
			}
		}
	}
	
	public void refreshAllListitem() {
		if (zkcb != null)
			zkcb.biBaseRefreshListitems(null);
	}
	public void refreshCurrentBiBaseListitem() {
		if (zkcb != null)
			zkcb.biBaseRefreshListitems(getBr().getCurrentRecord());
	}
	
	public void afterPaste() throws Exception {
		
	}

	protected void lockSubLink(String p_subLinkView)
	{
		JxField sv = jxAdd("list_"+replaceViewName(p_subLinkView));
		if(sv == null) return;
		AbstractGetItemProperty gipi = getGipi(p_subLinkView);
				((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
		sv.setAttribute("mode", "noDelete");
		sv.setAttribute("mode", "noInsert");
	}
	protected void unlockSubLink(String p_subLinkView)
	{
		JxField sv = jxAdd("list_"+replaceViewName(p_subLinkView));
		AbstractGetItemProperty gipi = getGipi(p_subLinkView);
		((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_INPUT);
		sv.setAttribute("mode", "canDelete");
		sv.setAttribute("mode", "canInsert");
	}
	
	static boolean useMobileLink(boolean p_isMobile , boolean p_useCompDiv) {
		return(p_isMobile && p_useCompDiv);
//		return(false);
	}
	
	public int getCurMode() {
		return curMode;
	}
}