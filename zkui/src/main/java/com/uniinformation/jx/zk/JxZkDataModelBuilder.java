package com.uniinformation.jx.zk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Space;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;

import com.kyoko.parser.excelformula.CellPositionInterface;
import com.kyoko.parser.excelformula.ColumnTranslateInterface;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.DataModelHelper;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.MapUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class JxZkDataModelBuilder extends ZkComposerBase {
	public static AtomicBoolean fDebug = new AtomicBoolean(false);
	
	private final static boolean DISABLE_HIDE_ROW_COL_FLAG = true;
   	private final static int MAX_IMPORT_COLUMNS = ZkBiComposerBase.MAX_IMPORT_COLUMNS;
   	private final static int MAX_IMPORT_ROWS = 101;

	private boolean isViewPageDirty, isJoinsPageDirty;

	@Wire
	Tabbox tabbox;
	@Wire
	Tab tabJoins;
	@Wire
	Button btDataModelMt, btAddView, btUploadExcel, btAddLink, btDeleteLink, btApply, btClose;
	@Wire
	Hlayout hlViewContainer, divJoinsContainer;
	@Wire
	Hlayout hlView;
	@Wire
	Tree treeCols;
	@Wire
	Treechildren treechildrenCols;

	@Wire
	Vlayout vlNameProp, vlViewIdProp, vlDataTypeProp, vlIsInListProp, vlIsInvisibleProp, vlSeqProp, vlFormulaProp, vlNumFormatProp, vlLengthProp, vlOrderbyListProp, vlOrderbyList, vlAccessRightProp;
	@Wire
	Textbox tbNameProp, tbViewIdProp, tbFormulaProp, tbNumFormatProp, tbAccessRightProp;
	@Wire
	Combobox cbDataTypeProp;
	@Wire
	Checkbox cbIsInListProp, cbIsInvisibleProp;
	@Wire
	Intbox ibSeqProp, ibLenProp;
	@Wire
	Label lbDebugProp;
	@Wire
	Button btAddOrderby;

	@Wire
	Div divJoins, divS2ToTable;
	@Wire
	Vlayout vlJoinsProp, vlFieldsListProp, vlFieldsFromTo, vlJoinsOptionProp;
	@Wire
	Label lbFromTable, lbToTable;
	@Wire
	Textbox tbFromTable;
	@Wire
	Checkbox cbOneToOne, cbOptional;
	@Wire
	Listbox s2ToTable;
	@Wire
	Button btAddFieldFromTo;

	private int maxKey;
	private Map<Integer, ViewProp> viewMap = new LinkedHashMap<Integer, ViewProp>();
	private Map<Integer, ColumnProp> columnMap = new HashMap<Integer, ColumnProp>();
	private Map<Integer, LinkedHashSet<Integer>> viewColMap = new HashMap<Integer, LinkedHashSet<Integer>>();
	
	private Map<String, NodeProp> nodeMap = new TreeMap<String, NodeProp>();
	private Map<String, EdgeProp> edgeMap = new LinkedHashMap<String, EdgeProp>();
	
	private DataModelHelper dataModelHelper;

	private class ViewProp {
		int key;
		String name;
		String nName = null;  //240214 add normalized name
		Vlayout viewComp;
		Label titleLabel, titleStarLabel;
		Listbox listbox;
		Treeitem treeitem;
		boolean isNewView;
		boolean isDirty;
		boolean isReadyDel;
		boolean allowUpdate;

		List<OrderbyItem> orderbyList = new ArrayList<OrderbyItem>();
		String newAccessRight;

		ExcelPoi exlpoi;
		int sheetNum;
		String firstCellPos;
		List<Integer> readSheetRowList = new ArrayList<Integer>();
		
		int viewRg;
		String viewId;
		String tabName;
		
		String oldName;
		int maxSeq;

		ViewProp(String name, boolean isDirty, boolean allowUpdate) {
			key = ++maxKey;
			this.name = name;
			this.isDirty = isDirty;
			this.allowUpdate = allowUpdate;
			if (isDirty) {
				isNewView = true;
				JxZkDataModelBuilder.this.setViewPageDirty(true);
			}
			try {
				nName = DataModelHelper.normalizeTag(name);
			}
			catch(Exception ex) {
				UniLog.log1("error:" + ex.getMessage());
			}
		}
		String getLabel() {
			//return name + (isDirty ? "*" : "");
			return name;
		}
		String getTitleLabelSClass() {
			StringBuilder sb = new StringBuilder("view_title");
			if (isReadyDel)
				sb.append(" readydel");
			else if (isNewView)
				sb.append(" new");
			return sb.toString();
		}
		void setDirty(boolean b) {
			isDirty = b;
			titleLabel.setValue(getLabel());
			titleStarLabel.setVisible(b);
			treeitem.setLabel(getLabel());
			JxZkDataModelBuilder.this.setViewPageDirty(true);
		}
		void setReadyDel(boolean b) {
			isReadyDel = b;
			titleLabel.setSclass(getTitleLabelSClass());
			Component popupComp = viewComp.getFellowIfAny("menup" + key);
			Menuitem miRemove = (Menuitem)popupComp.getAttribute("removeViewMenu");
			Menuitem miReset = (Menuitem)popupComp.getAttribute("resetViewMenu");
			miRemove.setVisible(!b);
			miReset.setVisible(b);
			JxZkDataModelBuilder.this.setViewPageDirty(true);
		}
		String getApplyName() {
			return (isNewView || StringUtils.equals(name, oldName)) ? name : String.format("%s/%s", name, viewId);
		}
	}
	
	private class ColumnProp {
		int key;
		String name;
		Listitem listitem;
		Label listitemLabel, listitemStarLabel;
		Treeitem treeitem;
		ViewProp viewProp;
		boolean isNewCol;
		boolean isDirty;
		boolean isReadyDel;
		
		boolean needLoadData;
		int sheetCol = -1;
		String nameInSheet;

		String fdName;
		String viewLabel;
		DataType dataType;
		boolean isInList;
		boolean isInvisible;
		String formula;
		String numFormat;
		int seq;
		int len;
		
		String oldName;
		DataType oldDataType;
		String oldFormula;
		String oldNumFormat;
		
		ColumnProp(String name, boolean isDirty) {
			key = ++maxKey;
			this.name = name;
			this.isDirty = isDirty;
			if (isDirty) {
				isNewCol = true;
				JxZkDataModelBuilder.this.setViewPageDirty(true);
			}
		}
		String getLabel() {
			//return name + (isDirty ? "*" : "");
			return name;
		}
		String getListitemSClass() {
			StringBuilder sb = new StringBuilder("column_bg");
			if (isReadyDel)
				sb.append(" readydel");
			else if (isNewCol)
				sb.append(" new");
			return sb.toString();
		}
		void setDirty() {
			isDirty = true;
			listitemLabel.setValue(getLabel());
			listitemStarLabel.setVisible(true);
			treeitem.setLabel(getLabel());
			//viewProp.setDirty(true);
			JxZkDataModelBuilder.this.setViewPageDirty(true);
		}
		void setReadyDel(boolean b) {
			isReadyDel = b;
			listitem.setSclass(getListitemSClass());
			Component popupComp = viewProp.viewComp.getFellowIfAny("menup" + key);
			Menuitem miRemove = (Menuitem)popupComp.getAttribute("removeColumnMenu");
			Menuitem miReset = (Menuitem)popupComp.getAttribute("resetColumnMenu");
			miRemove.setVisible(!b);
			miReset.setVisible(b);
			JxZkDataModelBuilder.this.setViewPageDirty(true);
		}
		String getApplyName() {
			return (isNewCol || StringUtils.equals(name, oldName)) ? name : String.format("%s/%s", name, viewLabel);
		}
	}

	private class NodeProp {
		String tableName;
		Set<String> fieldNameList = new TreeSet<String>();
		Set<String> joinToNodeIdList = new TreeSet<String>();
		Set<String> joinFromNodeIdList = new TreeSet<String>();
		String nodeId;
		int nodeLevel;
		NodeProp(String tableName) {
			this.tableName = tableName;
			nodeId = tableName != null ? tableName : String.valueOf(++maxKey);
		}
		String getNodeLabel() {
			return StringUtils.defaultString(tableName);
		}
		boolean isDummy() {
			return tableName == null;
		}
	}

	private class EdgeProp {
		String nodeIdA, nodeIdB;
		boolean isOptional, isOneToOne;
		List<JoinItem> joinFieldList = new ArrayList<JoinItem>();

		boolean isNewJoin;
		boolean isDirty;
		boolean isReadyDel;
		
		String edgeId;
		
		int rg;
		String oldTableNameA, oldTableNameB;
		
		EdgeProp(String nodeIdA, String nodeIdB, boolean isDirty) {
			edgeId = String.valueOf(++maxKey);
			this.nodeIdA = nodeIdA;
			this.nodeIdB = nodeIdB;
			this.isDirty = isDirty;
			if (isDirty)
				isNewJoin = true;
		}
		String getEdgeLabel() {
	    	List<String> labelAList = new ArrayList<String>();
	    	List<String> labelBList = new ArrayList<String>();
	    	for (JoinItem p : joinFieldList) {
	    		if (StringUtils.isBlank(p.fieldNameA) || StringUtils.isBlank(p.fieldNameB))
	    			continue;
	    		labelAList.add(p.fieldNameA);
	    		labelBList.add(p.fieldNameB);
	    	}
	    	return String.format("%s / %s", String.join(",", labelAList), String.join(",", labelBList));
		}
		void setDirty() {
			isDirty = true;
			setJoinsPageDirty(true);
		}
		String getApplyName() {
			String a = (isNewJoin || StringUtils.equals(nodeIdA, oldTableNameA)) ? nodeIdA : String.format("%s/%s", nodeIdA, oldTableNameA);
			String b = (isNewJoin || StringUtils.equals(nodeIdB, oldTableNameB)) ? nodeIdB : String.format("%s/%s", nodeIdB, oldTableNameB);
			return String.format("[From Table '%s' To Table '%s']", a, b);
		}
	}
	
	private class JoinItem {
		EdgeProp edgeProp;
		String fieldNameA, fieldNameB;
		Listbox listboxA, listboxB;
		public JoinItem(EdgeProp edgeProp, String fieldNameA, String fieldNameB) {
			this.fieldNameA = fieldNameA;
			this.fieldNameB = fieldNameB;
			this.edgeProp = edgeProp;
		}
		void appendInputRow() {
			NodeProp npA = nodeMap.get(edgeProp.nodeIdA);
			NodeProp npB = nodeMap.get(edgeProp.nodeIdB);
			vlFieldsFromTo.appendChild(new Hlayout() {{
				Hlayout hlRow = this;
				appendChild(new Div() {{
					setHflex("1");
					setSclass("flex_block");
					appendChild(new Listbox() {{
						setMold("select");
						setAttribute("select2-enable", "Y");
						listboxA = this;
						for (String fieldNameA : npA.fieldNameList) {
							Listitem li = appendItem(fieldNameA, fieldNameA);
							if (StringUtils.equals(JoinItem.this.fieldNameA, fieldNameA))
								setSelectedItem(li);
						}
						addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
							@Override
							public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
								String s = event.getReference().getValue();
								fieldNameA = s;
								edgeProp.setDirty();
								renderErdWithSelectEdge(edgeProp);
							}
						});
					}});
				}});
				appendChild(new Div() {{
					setHflex("1");
					setSclass("flex_block");
					appendChild(new Listbox() {{
						setMold("select");
						setAttribute("select2-enable", "Y");
						listboxB = this;
						for (String fieldNameB : npB.fieldNameList) {
							Listitem li = appendItem(fieldNameB, fieldNameB);
							if (StringUtils.equals(JoinItem.this.fieldNameB, fieldNameB))
								setSelectedItem(li);
						}
						addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
							@Override
							public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
								String s = event.getReference().getValue();
								fieldNameB = s;
								edgeProp.setDirty();
								renderErdWithSelectEdge(edgeProp);
							}
						});
					}});
				}});
				appendChild(new Toolbarbutton() {{
					setIconSclass("z-icon-trash");
					setSclass("narrowtoolbarbutton");
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							vlFieldsFromTo.removeChild(hlRow);
							edgeProp.joinFieldList.remove(JoinItem.this);
							edgeProp.setDirty();
							renderErdWithSelectEdge(edgeProp);
						}
					});
				}});
			}});
			ZkUtil.setupSelect2(listboxA, true, false);
			ZkUtil.setupSelect2(listboxB, true, false);
		}
	}

	private class OrderbyItem {
		ViewProp viewProp;
		ColumnProp columnProp;
		boolean isDesc;
		int columnIdx;
		Listbox listbox;
		public OrderbyItem(ViewProp viewProp, ColumnProp columnProp, boolean isDesc, int columnIdx) {
			this.viewProp = viewProp;
			this.columnProp = columnProp;
			this.isDesc = isDesc;
			this.columnIdx = columnIdx;
		}
		void appendInputRow() {
			vlOrderbyList.appendChild(new Hlayout() {{
				Hlayout hlRow = this;
				appendChild(new Div() {{
					setHflex("1");
					setSclass("flex_block");
					appendChild(new Listbox() {{
						setMold("select");
						setAttribute("select2-enable", "Y");
						listbox = this;
						for (int colKey : viewColMap.get(viewProp.key)) {
							ColumnProp cp = columnMap.get(colKey);
							if (!cp.isReadyDel) {
								Listitem li = appendItem(cp.name, String.valueOf(cp.key));
								if (columnProp != null && columnProp.key == cp.key)
									setSelectedItem(li);
							}
						}
						addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
							@Override
							public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
								String s = event.getReference().getValue();
								columnProp = columnMap.get(Integer.parseInt(s));
								columnIdx = event.getReference().getIndex() + 1;
								viewProp.setDirty(true);
							}
						});
					}});
				}});
				appendChild(new Checkbox() {{
					setChecked(isDesc);
					setTooltiptext("Desc");
					addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
						@Override
						public void onZkBiEvent(CheckEvent event) throws Exception {
							isDesc = event.isChecked();
							viewProp.setDirty(true);
						}
					});
				}});
				appendChild(new Toolbarbutton() {{
					setIconSclass("z-icon-trash");
					setSclass("narrowtoolbarbutton");
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							vlOrderbyList.removeChild(hlRow);
							viewProp.orderbyList.remove(OrderbyItem.this);
							viewProp.setDirty(true);
						}
					});
				}});
			}});
			ZkUtil.setupSelect2(listbox, true, false);
		}
	}
	
	private enum DataType {
		/*TEXT("Text", "char"), INTEGER("Integer", "integer"), FLOAT("Float", "double"), DATE("Date", "date"), DATETIME("DateTime", "datetime"), TIME("Time", "time"), 
			MEMO("Memo", "memo"), CHECKBOX("Checkbox", "checkbox"), RADIO("Radio", "radio"), LIST("List", "list"), COMBOBOX("Combobox", "combobox"), BUTTON("Button", "button"),
			LABEL("Label", "label"), PICKINPUT("PickInput", "pickinput"), COLORBOX("Colorbox", "colorbox"), DIV("Div", "div");*/
		TEXT("Text", "char"), NUMBER("Number", "float"), DATE("Date", "date"), DATETIME("DateTime", "datetime");

		DataType(String name, String fdtype) {
			this.name = name;
			this.fdtype = fdtype;
		}
	
		private String name;
		private String fdtype;
		public String getName() {
			return name;
		}
		public String getFdtype() {
			return fdtype;
		}
		
		public static DataType fdTypeToDataType(String fdtype) throws Exception {
			/*if (fdtype.matches("integer|smallint|serial"))
				return DataType.INTEGER;
			if (fdtype.matches("float|double|money|decimal"))
				return DataType.FLOAT;
			if (fdtype.matches("char"))
				return DataType.TEXT;
			return DataType.valueOf(fdtype.toUpperCase());*/
			if (fdtype.matches("integer|smallint|serial|float|double|money|decimal"))
				return DataType.NUMBER;
			if (fdtype.matches("date"))
				return DataType.DATE;
			if (fdtype.matches("datetime|time"))
				return DataType.DATETIME;
			return DataType.TEXT;
		}
	}

	@Override
	protected boolean validateURL(String p_url) {
		//return sessionHelper.isAdminUser();
		return sessionHelper.hasAccessRight("#dmbuilder");  //240207 need special access right
	}

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			rootComp.setVisible(false);
			ZkUtil.registerClientInfoEvent((HtmlBasedComponent)rootComp, sessionHelper, true, -100); 
			return;
		}
		dataModelHelper = new DataModelHelper(sessionHelper);

		for (DataType t : DataType.values())
			cbDataTypeProp.appendItem(t.getName()).setValue(t);
		ZkUtil.setupSelect2(s2ToTable, true, false);

		treeCols.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Treeitem, Object>>() { //tree select
			@Override
			public void onZkBiEvent(SelectEvent<Treeitem, Object> event) throws Exception {
				Treeitem ti = event.getReference();
				UniLog.log1("label:%s, value:%d", ti.getLabel(), (Integer)ti.getValue());
				int key = (Integer)ti.getValue();
				for (ViewProp vp1 : viewMap.values()) {
					vp1.viewComp.setSclass("view normal");
					vp1.listbox.setSelectedIndex(-1);
				}
				if (columnMap.containsKey(key)) {
					ColumnProp cp = columnMap.get(key);
					cp.viewProp.listbox.setSelectedItem(cp.listitem);
					showViewPageProp(cp.key);
				} else {
					ViewProp vp = viewMap.get(key);
					vp.viewComp.setSclass("view active");
					showViewPageProp(vp.key);
				}
			}
		});

		tabbox.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Tab, Object>>() {
			@Override
			public void onZkBiEvent(SelectEvent<Tab, Object> event) throws Exception {
				Tab tab = event.getReference();
				if (StringUtils.equals(tab.getId(), "tabView")) {
					btUploadExcel.setVisible(true);
					btAddView.setVisible(true);
					btAddLink.setVisible(false);
					btDeleteLink.setVisible(false);
				} else if (StringUtils.equals(tab.getId(), "tabJoins")) {
					btUploadExcel.setVisible(false);
					btAddView.setVisible(false);
					btAddLink.setVisible(true);
					btDeleteLink.setVisible(true);
					if (!StringUtils.equals((String)tab.getAttribute("alreadyFirstSelected"), "Y")) {
						renderErd();
						tab.setAttribute("alreadyFirstSelected", "Y");
					}
				}
			}
		});
		
		btDataModelMt.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() { //goto view page
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				btDataModelMt.setVisible(false);
				btAddView.setVisible(true);
				btUploadExcel.setVisible(true);

				btAddLink.setVisible(false);
				btDeleteLink.setVisible(false);
				btAddLink.setDisabled(true);
				btDeleteLink.setDisabled(true);

				btApply.setVisible(true);
				btApply.setDisabled(true);
				btClose.setVisible(true);
				tabbox.setSelectedIndex(0);
				tabbox.setVisible(true);
				loadDataModel();
			}
		});
		btUploadExcel.setUpload(String.format("true,maxsize=%d,multiple=false,accept=.xls|.xlsx,native", 100*1024));
		btUploadExcel.addEventListener(Events.ON_UPLOAD, new ZkBiEventListener<UploadEvent>() { //upload excel
			@Override
			public void onZkBiEvent(UploadEvent event) throws Exception {
				UniLog.log1("event:%s", event);
				try {
					new ExcelReader(event.getMedia());
				} catch (Exception ex) {
					UniLog.log(ex);
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
				}
			}
		});
		btAddView.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() { //add new view
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				Map<Integer, ViewProp> viewMap1 = new LinkedHashMap<Integer, ViewProp>();
				Map<Integer, ColumnProp> columnMap1 = new LinkedHashMap<Integer, ColumnProp>();
				ViewProp vp = new ViewProp(newViewName("NewView"), true, true);
				viewMap1.put(vp.key, vp);
				try {
					addView(viewMap1, columnMap1, null, null, false);
				} catch (Exception ex) {
					UniLog.log(ex);
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
				}
				Clients.scrollIntoView(vp.viewComp);
			}
		});
		btApply.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() { //confirm
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				try {
					applyRecords();
				} catch (Exception ex) {
					UniLog.log(ex);
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
				}
			}
		});
		btAddLink.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() { //confirm
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				addLink((String)vlJoinsProp.getAttribute("id"));
			}
		});
		btDeleteLink.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() { //confirm
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				deleteLink((String)vlJoinsProp.getAttribute("id"));
			}
		});
		btClose.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() { //return to first page
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				if (isViewPageDirty || isJoinsPageDirty) {
					ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")), new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
					new ZkBiMsgbox(sessionHelper).setContent(sessionHelper.getLabel("Are you sure to leave this page?")).setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
							if (btn.getIdx() == 0)
								clearPage();
						}
					}).build().doModal();
				} else
					clearPage();
			}
		});
	}
	
	private void loadDataModel() {
		try {
			Map<Integer, ViewProp> viewMap1 = new LinkedHashMap<Integer, ViewProp>();
			Map<Integer, ColumnProp> columnMap1 = new LinkedHashMap<Integer, ColumnProp>();
			BiResult br = BiResultHelper.create(sessionHelper, "bischema.BiView", null, -1, null);
			while (br.next(false)) {
				ViewProp vp = new ViewProp(br.getCellString("grpth_header"), false, true);
				vp.oldName = vp.name;
				vp.viewRg = br.getCellInt("grpth_rg");
				vp.viewId = br.getCellString("grpth_id");
				vp.tabName = br.getCellString("grpth_table");
				viewMap1.put(vp.key, vp);
				Map<Integer, ColumnProp> columnMap2 = new LinkedHashMap<Integer, ColumnProp>();
				BiResult subbr = br.getSubLink("bischema.BiColumn");
				while (subbr.next()) {
					ColumnProp cp = new ColumnProp(subbr.getCellString("grptc_header"), false);
					cp.oldName = cp.name;
					cp.fdName = subbr.getCellString("grptc_fd");
					cp.viewLabel = subbr.getCellString("grptc_label");
					cp.dataType = cp.oldDataType = DataType.fdTypeToDataType(subbr.getCellString("grptc_fdtype"));
					cp.isInList = subbr.getCellBoolean("grptc_inlist");
					cp.isInvisible = subbr.getCellBoolean("grptc_invisible");
					cp.seq = subbr.getCellInt("grptc_seq");
					cp.len = subbr.getCellInt("grptc_fdlen");
					cp.formula = cp.oldFormula = subbr.getCellString("grptc_formula");
					cp.numFormat = cp.oldNumFormat = subbr.getCellString("grptc_format");
					cp.viewProp = vp;
					vp.maxSeq = Math.max(vp.maxSeq, cp.seq);
					columnMap2.put(cp.key, cp);
				}
				columnMap1.putAll(columnMap2);
				subbr = br.getSubLink("bischema.BiOrderby");
				while (subbr.next()) {
					String fd = subbr.getCellString("grpto_fd");
					ColumnProp cp = null;
					for (ColumnProp cp1 : columnMap2.values()) {
						if (StringUtils.equals(cp1.viewLabel, fd)) {
							cp = cp1;
							break;
						}
					}
					vp.orderbyList.add(new OrderbyItem(vp, cp, subbr.getCellBoolean("grpto_desc"), subbr.getCellInt("grpto_colidx")));
				}
			}
			addView(viewMap1, columnMap1, null, null, false);
			
			
			nodeMap.clear();
			edgeMap.clear();
			br = BiResultHelper.create(sessionHelper, "bischema.BiTable", null, -1, null);
			while (br.next(false)) {
				String tabName = br.getCellString("ddt_tabname");
				NodeProp tp = new NodeProp(tabName);
				nodeMap.put(tp.nodeId, tp);
				BiResult subbr = br.getSubLink("bischema.BiField");
				while (subbr.next(false)) {
					String fieldName = subbr.getCellString("ddf_fdname");
					tp.fieldNameList.add(fieldName);
				}
			}

			br = BiResultHelper.create(sessionHelper, "bischema.BiJoins", null, -1, null);
			while (br.next(false)) {
				EdgeProp ep = new EdgeProp(br.getCellString("ddjh_tabnamea"), br.getCellString("ddjh_tabnameb"), false);
				NodeProp npA = nodeMap.get(ep.nodeIdA);
				NodeProp npB = nodeMap.get(ep.nodeIdB);
				if (npA == null || npB == null) {
					UniLog.log1("Warning !!!! 20240711 npA or npB is null, nodeIdA:%s:%s, nodeIdB:%s:%s", ep.nodeIdA, npA, ep.nodeIdB, npB);
					continue;
				}
				ep.rg = br.getCellInt("ddjh_rg");
				ep.isOptional = br.getCellBoolean("ddjh_optional");
				ep.isOneToOne = br.getCellBoolean("ddjh_onetone");
				ep.oldTableNameA = ep.nodeIdA;
				ep.oldTableNameB = ep.nodeIdB;
				edgeMap.put(ep.edgeId, ep);
				BiResult subbr = br.getSubLink("bischema.BiJoinDet");
				while (subbr.next())
					ep.joinFieldList.add(new JoinItem(ep, subbr.getCellString("ddjd_joinfielda"), subbr.getCellString("ddjd_joinfieldb"))); 
				npA.joinToNodeIdList.add(ep.nodeIdB);
				npB.joinFromNodeIdList.add(ep.nodeIdA);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
		}
	}
	
	private class ExcelReader {
   		InputStream inStream;
		ExcelPoi exlpoi;
		Div previewDiv;
		JSONObject jsonProfile;
		Map<String, String> firstSheetPosMap = new HashMap<String, String>();

		ExcelReader(org.zkoss.util.media.Media media) throws Exception {
    		inStream = media.getStreamData();
   			try {
   				exlpoi = ExcelPoi.newExcelPoi(inStream, false);
      		} 
	    	catch (Exception ex) {
	    		inStream.close();
				inStream = media.getStreamData();
				exlpoi = ExcelPoi.newExcelPoi(inStream, true);
			}
			exlpoi.excel_translate_Chinese(0);
			inStream.close();
			
			loadProfile();

			final ZkBiMsgboxButton btnLoad = new ZkBiMsgboxButton(sessionHelper.getBtLabel("Next")) {{
				setDisabled(true);
				setTooltiptext(sessionHelper.getLabel("You can review the changes the changes in Data Model View page"));
			}};
			final Checkbox cbUpdateFormula = new Checkbox(sessionHelper.getLabel("Update Formula")) {{ 
				setMold("switch"); 
			}};
			final Checkbox cbUpdateDataModel = new Checkbox(sessionHelper.getLabel("Update Data Model")) {{ 
				setMold("switch"); 
				setTooltiptext(sessionHelper.getLabel("It will update data view model of the selected sheet"));
				addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
					@Override
					public void onZkBiEvent(CheckEvent event) throws Exception {
						if (event.isChecked())
							cbUpdateFormula.setChecked(true);
					}
				});
			}};
			Checkbox cbLoadData = new Checkbox(sessionHelper.getLabel("Load Data")) {{ 
				setMold("switch"); 
				setTooltiptext(sessionHelper.getLabel("It will import data from the selected sheet"));
				setChecked(false);
			}};
			final Listbox lb = new Listbox() {{
				setWidth("250px");
				setVflex("1");
				appendChild(new Listhead() {{
					appendChild(new Listheader() {{
						appendChild(new Hlayout() {{
							setHflex("1");
							appendChild(new Div() {{ setWidth("25px"); }});
							appendChild(new Label(sessionHelper.getLabel("Excel Sheet")) {{ setHflex("1"); }});
							appendChild(new Label(sessionHelper.getLabel("First Header Cell")) {{ setHflex("min"); setStyle("white-space:nowrap"); }});
						}});
					}});
				}});
				final int sheetCount = exlpoi.excel_getNumberOfSheets();
				for (int i = 0; i < sheetCount; i++) {
					final String name = exlpoi.excel_getSheetName(i);
					final Checkbox cb = new Checkbox();
					final Textbox tb = new Textbox() {{ //first header cell input
						setText("A1");
						String s = firstSheetPosMap.get(name);
						if (StringUtils.isNotBlank(s)) {
							try {
								CellReference cr = new CellReference(s);
								if (cr.getRow() >= 0 && cr.getCol() >= 0)
									setText(s);
							} catch (Exception ex) {
								UniLog.log(ex);
							}
						}
						setWidth("40px");
						setInstant(true);
					}}; 
					Listitem li = appendItem(null, name);
					li.removeChild(li.getFirstChild());
					li.appendChild(new Listcell() {{
						appendChild(new Hlayout() {{
							setHflex("1");
							appendChild(cb);
							appendChild(new Label(name) {{ setHflex("1"); }});
							appendChild(tb);
						}});
					}});
					li.setAttribute("checkbox", cb);
					li.setAttribute("textbox", tb);
					final Listbox lb1 = this;
					cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() { //check sheet
						@Override
						public void onZkBiEvent(CheckEvent event) throws Exception {
							List<Integer> selectedSheetList = new ArrayList<Integer>();
							for (Listitem li : lb1.getItems()) {
								Checkbox cb = (Checkbox)li.getAttribute("checkbox");
								if (cb.isChecked())
									selectedSheetList.add(li.getIndex());
							}
							btnLoad.setDisabled(selectedSheetList.isEmpty());
						}
					});
					tb.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() { //change first header cell
						@Override
						public void onZkBiEvent(InputEvent event) throws Exception {
							UniLog.log1("newValue:%s, oldValue:%s", event.getValue(), event.getPreviousValue());
							if (li.isSelected()) {
								try {
									CellReference cr = new CellReference(event.getValue());
									if (cr.getRow() < 0 || cr.getCol() < 0)
										throw new Exception("Invalid value");
									firstSheetPosMap.put(name, event.getValue());
									previewSheet(li, true);
								} catch (Exception ex) {
									tb.setText((String)event.getPreviousValue());
									UniLog.log(ex);
									ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
								}
							}
						}
					});
				}
			}};
			previewDiv = new Div() {{
				setStyle("margin-right:10px");
				setHflex("1");
				setVflex("1");
				appendChild(new Label(sessionHelper.getLabel("No items selected for preview")) {{
					setStyle("position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);font-size:20px !important");
				}});
			}};
			Hlayout hl = new Hlayout() {{
				setHeight("80vh");
				appendChild(lb);
				appendChild(previewDiv);
			}};
			ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {btnLoad,new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
			new ZkBiMsgbox(sessionHelper).setContent(hl).setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
					if (btn.getIdx() == 0) {
						List<Integer> selectedSheetList = new ArrayList<Integer>();
						List<CellReference> crList = new ArrayList<CellReference>();
						List<CellReference> crAllList = new ArrayList<CellReference>();
						for (Listitem li : lb.getItems()) {
							Checkbox cb = (Checkbox)li.getAttribute("checkbox");
							if (cb.isChecked()) {
								selectedSheetList.add(li.getIndex());
								crList.add(new CellReference(((Textbox)li.getAttribute("textbox")).getText()));
							}
							crAllList.add(new CellReference(((Textbox)li.getAttribute("textbox")).getText()));
						}
						if (!selectedSheetList.isEmpty()) {
							if (!cbUpdateDataModel.isChecked() && !cbUpdateFormula.isChecked() && !cbLoadData.isChecked()) {
								ZkBiMsgbox.show("Please check options");
								return;
							}
							try {
								saveProfile();
								loadSheets(selectedSheetList, crList, crAllList, cbUpdateDataModel.isChecked(), cbUpdateFormula.isChecked(), cbLoadData.isChecked());
							} catch (Exception ex) {
								UniLog.log(ex);
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
							}
						} else
							ZkBiMsgbox.show("Please choose sheets");
					}
				}
			}).build().setTitle("Execl sheet selection").appendStyle("width:90%").doModal();
			Component pc = btnLoad.getParent();
			pc.insertBefore(cbLoadData, btnLoad);
			pc.insertBefore(cbUpdateFormula, cbLoadData);
			pc.insertBefore(cbUpdateDataModel, cbUpdateFormula);
			pc.insertBefore(new Space() {{ setWidth("10px"); }}, btnLoad);
			pc.insertBefore(new Space() {{ setWidth("10px"); }}, cbUpdateFormula);
			
			lb.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
				@Override
				public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
					Listitem li = event.getReference();
					UniLog.log1("value:%s, index:%d", li.getValue(), li.getIndex());
					try {
						previewSheet(li, false);
					} catch (Exception ex) {
						UniLog.log(ex);
						ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
					}
				}
			});
		}
		
		void loadProfile() throws JSONException {
			firstSheetPosMap.clear();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				FilingUtilObject fuo = FilingUtil.getFile(sessionHelper.getAgent(), null, "zkbi_datamodelbuilder_profile_" + sessionHelper.getAgent(), os);
				if (fuo != null) {
	  				jsonProfile = new JSONObject(os.toString("UTF-8"));
	  				JSONObject sheets = jsonProfile.getJSONObject("sheets");
	  				Iterator<String> it = sheets.keys();
	  				while (it.hasNext()) {
	  					String sheetName = it.next();
	  					JSONObject sheet = sheets.getJSONObject(sheetName);
	  					String firstSheetPos = sheet.optString("firstSheetPos");
	  					if (StringUtils.isNotBlank(firstSheetPos))
	  						firstSheetPosMap.put(sheetName, firstSheetPos);
	  				}
					UniLog.log1("profile:%s", jsonProfile.toString(3));
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (IOException e) {
				}
			}
			if (jsonProfile == null)
				jsonProfile = new JSONObject();
			if (!jsonProfile.has("sheets"))
				jsonProfile.put("sheets", new JSONObject());
		}
		
		void saveProfile() {
			ByteArrayInputStream is = null;
			try {
				boolean flag = false;
				JSONObject sheets = jsonProfile.getJSONObject("sheets");
				for (Map.Entry<String, String> entry : firstSheetPosMap.entrySet()) {
					String sheetName = entry.getKey();
					String firstSheetPos = entry.getValue();
					if (!sheets.has(sheetName))
						sheets.put(sheetName, new JSONObject());
					JSONObject sheet = sheets.getJSONObject(sheetName);
					if (!StringUtils.equals(sheet.optString("firstSheetPos"), firstSheetPos)) {
						sheet.put("firstSheetPos", firstSheetPos);
						flag = true;
					}
				}
				if (!flag)
					return;

				is = new ByteArrayInputStream(jsonProfile.toString().getBytes("UTF-8"));
				String key = "zkbi_datamodelbuilder_profile_" + sessionHelper.getAgent();
				FilingUtil.storeFile(sessionHelper.getAgent(), null, key, key, key, is);
			} catch (Exception ex) {
				UniLog.log(ex);
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (IOException e) {
				}
			}
		}
		
		void previewSheet(Listitem li, boolean isReset) throws Exception {
			GridHelper gh = isReset ? null : (GridHelper)li.getAttribute("sheetPreviewGrid");
			Textbox tb = (Textbox)li.getAttribute("textbox");
			CellReference cr = new CellReference(tb.getText());
			int startRow = cr.getRow();
			int startCol = cr.getCol();
			
			//FormulaEvaluator evaluator = exlpoi.getWorkbook().getCreationHelper().createFormulaEvaluator();
			//FunctionEval.registerFunction(name, func);

			if (gh == null && startRow >= 0 && startCol >= 0) {
				exlpoi.excel_useSheet(li.getIndex());

				List<Integer> colNums = new ArrayList<Integer>();
				int count = MAX_IMPORT_COLUMNS;
				int rowCount = exlpoi.getRowCount();
				int colCount = getColumnCount(startRow, rowCount - 1);
				for (int i = startCol; i < colCount; i++) {
					if (isColumnHidden(i) || isMergedCellButNotFirstColumn(startRow, i))
						continue;
					colNums.add(i);
					if (--count == 0)
						break;
				}
				if (!colNums.isEmpty()) {
					gh = new GridHelper(colNums.size());
					gh.setVflex("1");
					gh.setHflex("1");
					int row = startRow;
					int col;
					for (int i = 0; i < colNums.size(); i++) {
						col = colNums.get(i);
						String name = StringUtils.defaultString(exlpoi.getStringValue(row, col)).trim();
						gh.getColumn(i).setHflex("min");
						gh.getColumn(i).setLabel(name);
					}
					count = MAX_IMPORT_ROWS;
					for (row = row + 1; row < rowCount; row++) {
						if (isRowHidden(row))
							continue;
						if (isBlankSheetRow(row, startCol, colCount - 1))
							break;
						HtmlBasedComponent[] comps = new HtmlBasedComponent[colNums.size()];
						for (int i = 0; i < colNums.size(); i++) {
							col = colNums.get(i);
							Cell cell = exlpoi.getCell(row, col);
							Map<String, Object> map = getDataType(cell);
							DataType dataType = (DataType)map.get("dataType");
							String v = getCellStringValue(exlpoi, cell, dataType);
							UniLog.log1("cellpos:%s, dataType:%s, cellType:%s, v:%s", getCellPos(row, col), dataType, (cell != null ? cell.getCellType() : ""), v);
							comps[i] = new Label(v);
						}
						gh.addRow(comps);
						if (--count == 0)
							break;
					}
				}
			}
			while (!previewDiv.getChildren().isEmpty())
				previewDiv.removeChild(previewDiv.getFirstChild());
			li.setAttribute("sheetPreviewGrid", gh);
			if (gh != null)
				previewDiv.appendChild(gh);
		}
		
		void loadSheets(List<Integer> sheetList, List<CellReference> crList, List<CellReference> crAllList, boolean needUpdateDataModel, boolean needUpdateFormula, boolean needLoadData) throws Exception {
			UniLog.log1("needUpdateDataModel:%b, needLoadData:%b", needUpdateDataModel, needLoadData);
			final Map<Integer, ViewProp> viewMap1 = new LinkedHashMap<Integer, ViewProp>();
			final Map<Integer, ColumnProp> columnMap1 = new LinkedHashMap<Integer, ColumnProp>();
			final List<ViewProp> loadDataViewList = new ArrayList<ViewProp>();
			final Set<Integer> loadDataColumnList = new LinkedHashSet<Integer>();
			for (int i = 0; i < sheetList.size(); i++) {
				int sheetNum = sheetList.get(i);
				CellReference cr = crList.get(i);
				int startRow = cr.getRow();
				int startCol = cr.getCol();

				String sheetName = exlpoi.excel_getSheetName(sheetNum);
				ViewProp vp = findView(sheetName);
				boolean dataModelFlag = needUpdateDataModel;
				boolean updateFormulaFlag = needUpdateFormula;
				if (vp == null) {
					if (needUpdateDataModel)
						vp = new ViewProp(sheetName, true, true);
				} else if (!vp.allowUpdate) {
					dataModelFlag = false;
					updateFormulaFlag = false;
				}
				if (vp == null)
					continue;
				vp.sheetNum = sheetNum;
				vp.firstCellPos = cr.formatAsString();
				vp.exlpoi = null;
				vp.readSheetRowList.clear();
				
				List<ColumnProp> clist = new ArrayList<ColumnProp>();
				Map<String, ColumnProp> cmap = new HashMap<String, ColumnProp>();
				Set<Integer> list = viewColMap.get(vp.key);
				if (list != null) {
					for (int colKey : list) {
						ColumnProp cp = columnMap.get(colKey);
						cp.sheetCol = -1;
						cp.nameInSheet = null;
						clist.add(cp);
					}
				}

				exlpoi.excel_useSheet(sheetNum);
				boolean recordFound = false;
				int count = MAX_IMPORT_COLUMNS;
				int rowCount = exlpoi.getRowCount();
				int colCount = getColumnCount(startRow, rowCount - 1);
				int row;
				int col;
				//save matched columnProp
				for (col = startCol; col < colCount; col++) {
					if (isColumnHidden(col) || isMergedCellButNotFirstColumn(startRow, col))
						continue;
					String name = StringUtils.defaultString(exlpoi.getStringValue(startRow, col)).trim();
					String newFdName = buildFdName(col);
					for (int j = 0; j < clist.size(); j++) {
						ColumnProp cp = clist.get(j);
						if (StringUtils.equals(cp.name, name) && StringUtils.equals(cp.fdName, newFdName)) {
							cmap.put(cp.fdName, cp);
							clist.remove(j);
							break;
						}
					}
				}
				//skip hide row
				for (row = startRow + 1; row < rowCount; row++) {
					if (!isRowHidden(row))
						break;
				}
				//int seq = 0;
				for (col = startCol; col < colCount; col++) {
					if (isColumnHidden(col) || isMergedCellButNotFirstColumn(startRow, col))
						continue;
					String name = StringUtils.defaultString(exlpoi.getStringValue(startRow, col)).trim();
					String newFdName = buildFdName(col);
					Cell cell = exlpoi.getCell(row, col);
					Map<String, Object> map = row < rowCount ? getDataType(cell) : MapUtil.of("dataType", DataType.TEXT);
					DataType dataType = (DataType)map.get("dataType");
					String formula = translateCellFormula(exlpoi, cell, cr, crAllList, vp);
					String numFormat = StringUtils.defaultString((String)map.get("numFormat"));
					numFormat = ""; //
					
					ColumnProp cp = cmap.get(newFdName);
					UniLog.log1("name:%s, newFdName:%s, row:%d, col:%d, cell:%s, dataType:%s, formula:%s, numFormat:%s, dataModelFlag:%b, updateFormulaFlag:%b, cp:%s", name, newFdName, row, col, cell, dataType, formula, numFormat, dataModelFlag, updateFormulaFlag, cp);
					/*if (cp == null) {
						for (int j = 0; j < clist.size(); j++) {
							ColumnProp cp1 = clist.get(j);
							if (StringUtils.equals(cp1.name, name)) {
								cp = cp1;
								clist.remove(j);
								break;
							}
						}
					}*/
					if (cp == null) {
						for (int j = 0; j < clist.size(); j++) {
							ColumnProp cp1 = clist.get(j);
							if (StringUtils.equals(cp1.fdName, newFdName)) {
								cp = cp1;
								cp.nameInSheet = name;
								clist.remove(j);
								break;
							}
						}
					}
					if (dataModelFlag) {
						if (cp == null) {
							cp = new ColumnProp(name, true);
							cp.fdName = cp.viewLabel = newFdName;
							cp.isInList = true;
							cp.dataType = dataType;
							cp.formula = formula;
							cp.numFormat = numFormat;
							cp.isDirty = true;
							//if (vp.isNewView)
							//	cp.seq = ++seq;
							cp.seq = ++vp.maxSeq;
						} else if (updateFormulaFlag) {
							if (cp.dataType != dataType || !StringUtils.equals(cp.formula, formula) || !StringUtils.equals(cp.numFormat, numFormat)
									|| !StringUtils.equals(cp.name, name))
								cp.isDirty = true;
							cp.name = name;
							cp.dataType = dataType;
							cp.formula = formula;
							cp.numFormat = numFormat;
						} else {
							if (cp.dataType != dataType || !StringUtils.equals(cp.numFormat, numFormat)
									|| !StringUtils.equals(cp.name, name))
								cp.isDirty = true;
							cp.name = name;
							cp.dataType = dataType;
							cp.numFormat = numFormat;
						}
						cp.sheetCol = col;
						cp.viewProp = vp;
						columnMap1.put(cp.key, cp);
					} else if (updateFormulaFlag) {
						if (cp != null) {
							if (!StringUtils.equals(cp.formula, formula)) {
								UniLog.log1("cpname:%s, newformula:%s, oldformula:%s", cp.name, formula, cp.formula);
								cp.isDirty = true;
							}
							cp.formula = formula;
							cp.sheetCol = col;
							cp.viewProp = vp;
							columnMap1.put(cp.key, cp);
						}
					}
					if (needLoadData && cp != null) {
						cp.sheetCol = col;
						cp.needLoadData = true;
						loadDataColumnList.add(cp.key);
					}
					recordFound = true;
					if (--count == 0)
						break;
				}
				if (recordFound) {
					if (dataModelFlag)
						viewMap1.put(vp.key, vp);
					else if (updateFormulaFlag) {
						if (vp != null)
							viewMap1.put(vp.key, vp);
					}
					if (needLoadData) {
						row = startRow;
						for (row = row + 1; row < rowCount; row++) {
							if (isRowHidden(row))
								continue;
							if (isBlankSheetRow(row, startCol, colCount - 1))
								break;
							//UniLog.log1("readSheetRowList add row:%d", row);
							vp.readSheetRowList.add(row);
						}
					}
				}
				if (!vp.readSheetRowList.isEmpty()) {
					vp.exlpoi = exlpoi;
					loadDataViewList.add(vp);
				}
			}
			addView(viewMap1, columnMap1, loadDataViewList, loadDataColumnList, needUpdateFormula && !needUpdateDataModel);
		}
		
		int getColumnCount(int startRow, int endRow) {
			int col = exlpoi.excel_getColumnCount(startRow) - 1;
			for (; col >= 0; col--) {
				if (!isBlankSheetCol(col, startRow, endRow))
					break;
			}
			return col + 1;
		}

		private boolean isBlankSheetRow(int row, int startCol, int endCol) {
			if (row >= exlpoi.getRowCount())
				return true;
			int colCount = exlpoi.excel_getColumnCount(row);
			for (int col = startCol; col < colCount && col <= endCol; col++) {
				Cell cell = exlpoi.getCell(row, col);
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK && StringUtils.isNotBlank(cell.toString()))
					return false;
			}
			return true;
		}

		private boolean isBlankSheetCol(int col, int startRow, int endRow) {
			if (col >= exlpoi.excel_getColumnCount(startRow))
				return true;
			int rowCount = exlpoi.getRowCount();
			for (int row = startRow; row < rowCount && row <= endRow; row++) {
				Cell cell = exlpoi.getCell(row, col);
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK && StringUtils.isNotBlank(cell.toString()))
					return false;
			}
			return true;
		}
		
		private boolean isColumnHidden(int col) {
			if (DISABLE_HIDE_ROW_COL_FLAG)
				return false;
			return exlpoi.excel_isColumnHidden(col);
		}
		
		private boolean isRowHidden(int row) {
			if (DISABLE_HIDE_ROW_COL_FLAG)
				return false;
			return exlpoi.excel_getZeroHeight(row);
		}
		
		private boolean isMergedCellButNotFirstColumn(int row, int col) {
			Sheet sheet = exlpoi.getSheet();
			int n = sheet.getNumMergedRegions();
			for (int i = 0; i < n; i++) {
				CellRangeAddress cra = sheet.getMergedRegion(i);
				if (cra.isInRange(row, col))
					return cra.getFirstColumn() != col;
			}
			return false;
		}
	}

	private ViewProp findView(String name) {
		Iterator<ViewProp> it = viewMap.values().iterator();
		while (it.hasNext()) {
			ViewProp vp = it.next();
			if (StringUtils.equals(vp.name, name))
				return vp;
		}
		return null;
	}

	private List<ColumnProp> findColumns(ViewProp vp, String name) {
		List<ColumnProp> cps = new ArrayList<ColumnProp>();
		Set<Integer> list = viewColMap.get(vp.key);
		if (list != null) {
			for (int key : list) {
				ColumnProp cp = columnMap.get(key);
				if (StringUtils.equals(cp.name, name))
					cps.add(cp);
			}
		}
		return cps;
	}

	private ColumnProp findColumnByFdName(ViewProp vp, String fdName) {
		Set<Integer> list = viewColMap.get(vp.key);
		if (list != null) {
			for (int key : list) {
				ColumnProp cp = columnMap.get(key);
				if (StringUtils.equals(cp.fdName, fdName))
					return cp;
			}
		}
		return null;
	}
	
	private void addView(Map<Integer, ViewProp> viewMap1, Map<Integer, ColumnProp> columnMap1, List<ViewProp> loadDataViewList, Set<Integer> loadDataColumnList, boolean updateFormulaOnly) throws Exception {
		//check col idx/field name match
		StringBuilder sb = new StringBuilder();
		StringBuilder sbw = new StringBuilder();
		for (ColumnProp cp1 : columnMap1.values()) {
			UniLog.log1("sheetcol:%d, oldname:%s, name:%s", cp1.sheetCol, cp1.oldName, cp1.name);
			if (cp1.sheetCol >= 0) {
				if (!StringUtils.equals(cp1.fdName, buildFdName(cp1.sheetCol)))
					sb.append(String.format("Column '%s' of View '%s' column idx(%s)/field name(%s) does not matched\n", cp1.name, cp1.viewProp.name, CellReference.convertNumToColString(cp1.sheetCol), cp1.fdName));
				if (StringUtils.isNotBlank(cp1.oldName) && StringUtils.isNotBlank(cp1.nameInSheet) && !StringUtils.equals(cp1.oldName, cp1.nameInSheet))
					sbw.append(String.format("Column '%s' of View '%s' header name (OldName:%s, NewName:%s) does not matched\n", CellReference.convertNumToColString(cp1.sheetCol), cp1.viewProp.name, cp1.oldName, cp1.nameInSheet));
			}
		}
		if (loadDataViewList != null) {
			for (ViewProp vp2 : loadDataViewList) {
				Set<Integer> keyList = viewColMap.get(vp2.key);
				if (keyList != null) {
					for (int colKey : keyList) {
						if (loadDataColumnList.contains(colKey) && !columnMap1.containsKey(colKey)) {
							ColumnProp cp1 = columnMap.get(colKey);
							if (!StringUtils.equals(cp1.fdName, buildFdName(cp1.sheetCol)))
								sb.append(String.format("Column '%s' column idx(%d)/field name(%s) does not matched)", cp1.name, cp1.sheetCol, cp1.fdName));
							if (StringUtils.isNotBlank(cp1.oldName) && StringUtils.isNotBlank(cp1.nameInSheet) && !StringUtils.equals(cp1.oldName, cp1.nameInSheet))
								sbw.append(String.format("Column '%s' of View '%s' header name (OldName:%s, NewName:%s) does not matched\n", CellReference.convertNumToColString(cp1.sheetCol), cp1.viewProp.name, cp1.oldName, cp1.nameInSheet));
						}
					}
				}
			}
		}
		if (sb.length() > 0)
			throw new Exception(sb.toString());
		if (sbw.length() > 0) {
			new ZkBiMsgbox(sessionHelper)
				.setContent(ZkBiMsgbox.buildMsgboxContentComp(sbw.toString()))
				.setButtons(new String[] {"Continue", "Cancel"})
				.setEventListener(new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
						if (btn.getIdx() == 0) {
							try {
								addView1(viewMap1, columnMap1, loadDataViewList, loadDataColumnList, updateFormulaOnly);
							} catch (Exception ex) {
								UniLog.log(ex);
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, getThrowMsg(ex));
							}
						}
					}
				})
				.build().doModal();
		} else 
			addView1(viewMap1, columnMap1, loadDataViewList, loadDataColumnList, updateFormulaOnly);
	}
	private void addView1(Map<Integer, ViewProp> viewMap1, Map<Integer, ColumnProp> columnMap1, List<ViewProp> loadDataViewList, Set<Integer> loadDataColumnList, boolean updateFormulaOnly) throws Exception {
		if (updateFormulaOnly) {
			columnMap1.clear();
			for (ViewProp vp1 : viewMap1.values()) {
				if (viewMap.containsKey(vp1.key)) {
					for (int colKey : viewColMap.get(vp1.key).toArray(new Integer[0])) {
						ColumnProp cp = columnMap.get(colKey);
						columnMap1.put(colKey, cp);
					}
				}
			}
		} else {
			for (ViewProp vp1 : viewMap1.values()) {
				if (viewMap.containsKey(vp1.key)) {
					for (int colKey : viewColMap.get(vp1.key).toArray(new Integer[0])) {
						if (!columnMap1.containsKey(colKey)) {
							ColumnProp cp = columnMap.get(colKey);
							cp.isReadyDel = true;
							columnMap1.put(colKey, cp);
						}
					}
				}
			}
		}

		//remove old column
		for (ViewProp vp1 : viewMap1.values()) {
			if (viewMap.containsKey(vp1.key)) {
				boolean isDirty = vp1.isDirty;
				for (int key : viewColMap.get(vp1.key).toArray(new Integer[0]))
					removeViewOrColumn(key);
				for (ColumnProp cp1 : columnMap1.values()) {
					if (cp1.isNewCol && cp1.viewProp.key == vp1.key) {
						isDirty = true;
						break;
					}
				}
				vp1.setDirty(isDirty);
			}
		}

		viewMap.putAll(viewMap1);
		columnMap.putAll(columnMap1);
		for (ViewProp vp : viewMap1.values())
			viewColMap.put(vp.key, new LinkedHashSet<Integer>());
		for (ColumnProp cp : columnMap1.values())
			viewColMap.get(cp.viewProp.key).add(cp.key);

		//reset unuse sheet col
		if (loadDataViewList != null) {
			for (ViewProp vp2 : loadDataViewList) {
				for (int colKey : viewColMap.get(vp2.key)) {
					if (!loadDataColumnList.contains(colKey)) {
						ColumnProp cp = columnMap.get(colKey);
						cp.needLoadData = false;
					}
				}
			}
		}

		for (final ViewProp vp : viewMap1.values()) {
			if (vp.viewComp == null) {
				Vlayout vl = new Vlayout() {{  //View list append child
					vp.viewComp = this;
					setWidth("200px");
					setSclass("view normal");
					appendChild(new Hlayout() {{
						setHflex("1");
						appendChild(new Div() {{
							setHflex("1");
							appendChild(new Label(vp.getLabel()) {{
								vp.titleLabel = this;
								setSclass(vp.getTitleLabelSClass());
							}});
							addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() { //active view
								@Override
								public void onZkBiEvent(Event event) throws Exception {
									UniLog.log1("event:%s", event);
									for (ViewProp vp1 : viewMap.values())
										vp1.viewComp.setSclass("view normal");
									vp.viewComp.setSclass("view active");
									if (vp.listbox != null)
										vp.listbox.setSelectedIndex(-1);
									treeCols.setSelectedItem(vp.treeitem);
									showViewPageProp(vp.key);
								}
							});
						}});
						appendChild(new Label("*") {{ 
							vp.titleStarLabel = this;
							setVisible(vp.isDirty);
						}});
						appendChild(new Toolbarbutton() {{
							setIconSclass("z-icon-ellipsis-v");
							setSclass("narrowtoolbarbutton");
							setPopup(String.format("%s, position=after_start", getMenuPopup(vp.key, "menup", vp.viewComp).getId()));
						}});
					}});
					appendChild(new Listbox() {{
						vp.listbox = this;
						setVflex("1");
						addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, Object>>() { //listbox select
							@Override
							public void onZkBiEvent(SelectEvent<Listitem, Object> event) throws Exception {
								Listitem li = event.getReference();
								UniLog.log1("label:%s, value:%s", li.getLabel(), li.getValue());
								for (ViewProp vp1 : viewMap.values()) {
									vp1.viewComp.setSclass("view normal");
									if (vp1 != vp)
										vp1.listbox.setSelectedIndex(-1);
								}
								int key = Integer.parseInt((String)li.getValue());
								if (key > 0) {
									ColumnProp cp = columnMap.get(key);
									if (!cp.viewProp.treeitem.isOpen())
										cp.viewProp.treeitem.setOpen(true);
									treeCols.setSelectedItem(cp.treeitem);
									Clients.scrollIntoView(cp.treeitem);
								} else
									treeCols.setSelectedItem(null);
								showViewPageProp(key);
							}
						});
					}});
				}};
				List<ViewProp> list = getSortedViewList();
				int pos = list.indexOf(vp);
				if (pos >= 0 && pos < list.size() - 1)
					hlView.insertBefore(vl, list.get(pos + 1).viewComp);
				else
					hlView.appendChild(vl);
			}

			if (vp.treeitem == null) {
				treechildrenCols.appendChild(new Treeitem() {{ //data tree append child
					vp.treeitem = this;
					setValue(vp.key);
					setOpen(false);
					appendChild(new Treerow() {{
						appendChild(new Treecell(vp.name) {{
							setupRightClickPopup(vp.key, this);
						}});
					}});
				}});
			}

			Set<Integer> colKeyList = viewColMap.get(vp.key);
			if (!colKeyList.isEmpty()) {
				for (Integer colKey : colKeyList) {
					ColumnProp cp = columnMap.get(colKey);
					addColumn(cp);
				}
			}
			
			setupViewCompHeight(vp);
		}

		if (loadDataViewList != null) {
			for (ViewProp vp : loadDataViewList) {
				UniLog.log1("name:%s, readSheetRowCount:%d", vp.name, vp.readSheetRowList.size());
				Listbox lb = vp.listbox;
				if (lb.getItemCount() > 0) {
					Listitem li = lb.getItemAtIndex(0);
					int v = Integer.parseInt(li.getValue());
					if (v != 0 && !vp.readSheetRowList.isEmpty()) {
						lb.getItems().add(0, new Listitem(null, "0") {{ 
							Listitem li1 = this;
							setSclass("column_bg new"); 
							removeChild(getFirstChild());
							appendChild(new Listcell() {{
								appendChild(new Hlayout() {{
									appendChild(new Label(String.format(sessionHelper.getLabel("Load Data:%d row"), vp.readSheetRowList.size())) {{ 
										setHflex("1"); 
										li1.setAttribute("label", this);
									}});
									appendChild(new Toolbarbutton() {{ 
										setIconSclass("z-icon-ellipsis-v"); 
										setSclass("narrowtoolbarbutton");
										//setPopup(String.format("%s, position=after_start", getMenuPopup(cp.key, "menup", vp.viewComp).getId()));
									}});
								}});
							}});
							addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
								@Override
								public void onEvent(AfterSizeEvent event) throws Exception {
									//UniLog.log1("listitem viewname:%s height:%d", vp.name, event.getHeight());
									Integer compHeight = (Integer)li.getAttribute("compHeight");
									if (compHeight != null)
										compHeight = Math.max(compHeight, event.getHeight());
									else
										compHeight = event.getHeight();
									li1.setAttribute("compHeight", compHeight);
									setupViewCompHeight(vp);
								}
							});
						}});
						setViewPageDirty(true);
					} else if (v == 0) {
						if (!vp.readSheetRowList.isEmpty()) {
							((Label)li.getAttribute("label")).setValue(String.format("Load Data: %d row", vp.readSheetRowList.size()));
							setViewPageDirty(true);
						} else
							lb.removeItemAt(0);
					}
				} else if (!vp.readSheetRowList.isEmpty()) {
					lb.appendItem(String.format("Load Data: %d row", vp.readSheetRowList.size()), "0");
					setViewPageDirty(true);
				}
				setupViewCompHeight(vp);
			}
		}

		//reset main dirty
		setViewPageDirty(false);
		setViewPageDirty(true);
	}
	
	private void addColumn(ColumnProp cp) {
		ViewProp vp = cp.viewProp;
		if (!columnMap.containsKey(cp.key)) {
			columnMap.put(cp.key, cp);
			viewColMap.get(vp.key).add(cp.key);
		}

		Listitem li = vp.listbox.appendItem(null, String.valueOf(cp.key));
		li.setSclass(cp.getListitemSClass());
		li.removeChild(li.getFirstChild());
		li.appendChild(new Listcell() {{
			appendChild(new Hlayout() {{
				appendChild(new Div() {{
					setHflex("1"); 
					appendChild(new Label(cp.getLabel()) {{ 
						cp.listitemLabel = this;
						setSclass("column_label");
					}});
				}});
				appendChild(new Label("*") {{ 
					cp.listitemStarLabel = this;
					setVisible(cp.isDirty);
				}});
				appendChild(new Toolbarbutton() {{ 
					setIconSclass("z-icon-ellipsis-v"); 
					setSclass("narrowtoolbarbutton");
					setPopup(String.format("%s, position=after_start", getMenuPopup(cp.key, "menup", vp.viewComp).getId()));
				}});
			}});
		}});
		li.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
			@Override
			public void onEvent(AfterSizeEvent event) throws Exception {
				//UniLog.log1("listitem viewname:%s height:%d, cplabel:%s", vp.name, event.getHeight(), cp.getLabel());
				Integer compHeight = (Integer)li.getAttribute("compHeight");
				if (compHeight != null)
					compHeight = Math.max(compHeight, event.getHeight());
				else
					compHeight = event.getHeight();
				li.setAttribute("compHeight", compHeight);
				setupViewCompHeight(vp);
			}
		});
		cp.listitem = li;
		setupViewCompHeight(vp);
		
		Treechildren t = (Treechildren)vp.treeitem.query("Treechildren");
		if (t == null) {
			t = new Treechildren();
			vp.treeitem.appendChild(t);
		}
		t.appendChild(new Treeitem() {{
			cp.treeitem = this;
			setValue(cp.key);
			appendChild(new Treerow() {{
				appendChild(new Treecell(cp.name + "*") {{
					setupRightClickPopup(cp.key, this);
				}});
			}});
		}});
	}
	
	private void renderErd() throws Exception {
		final int fontSize = 14;
	    JSONObject joOptions = new JSONObject("{"
    		+ "	edges : {"
    		+ "		smooth: {"
    		+ "			type : \"cubicBezier\","
    		+ "			forceDirection : true,"
    		+ "			roundness: 0.5"
    		+ "		},"
    		+ "		arrows : {"
    		+ "			to : {"
    		+ "				type : \"inv_curve\","
    		+ "				enabled : true"
    		+ "			}"
    		+ "		},"
    		+ "		font : {"
    		+ "			size: " + fontSize
    		+ "		}"
    		+ "	},"
    		+ "	layout : {"
    		+ "		hierarchical : {"
    		+ "			direction : \"LR\","
    		+ "			levelSeparation : 200,"
    		+ "			nodeSpacing : 100,"
    		+ "			treeSpacing : 100"
    		+ "		}"
    		+ "	},"
    		+ "	nodes: {"
    		+ "		shape : \"box\","
    		+ "		font : {"
    		+ "			size : " + fontSize
    		+ "		},"
    		+ "		widthConstraint : {"
    		+ "			minimum : 80"
    		+ "		},"
    		+ "		heightConstraint : {"
    		+ "			minimum : 20"
    		+ "		}"
    		+ "	},"
    		+ "	interaction : {"
    		+ "		dragNodes : false,"
    		+ "		dragView : true,"
    		+ "		zoomView : true,"
    		+ "		selectConnectedEdges : false"
    		+ "	}"
    		+ "}");
	    JSONArray jaNodes = new JSONArray();
	    JSONArray jaEdges = new JSONArray();
	    
	    Set<String> list = new TreeSet<String>();
	    for (NodeProp np : nodeMap.values()) {
	    	np.nodeLevel = 0;
	    	if (np.joinFromNodeIdList.isEmpty() && !np.joinToNodeIdList.isEmpty())
	    		list.add(np.nodeId);
	    }
	    int maxLevel = setupNodeLevel(list, 0);
	    UniLog.log1("list:%s, maxLevel:%d", String.join(",", list), maxLevel);
	    for (EdgeProp ep : getSortedEdgeList()) {
	    	NodeProp npa = nodeMap.get(ep.nodeIdA);
	    	NodeProp npb = nodeMap.get(ep.nodeIdB);
	    	if(npa == null || npb == null) {
				UniLog.log1("Warning !!!! 20240711 npA or npB is null, nodeIdA:%s:%s, nodeIdB:%s:%s", ep.nodeIdA, npa, ep.nodeIdB, npb);
				continue;
	    	}
	    	String label = ep.getEdgeLabel();
	    	//TextSpliter ts = ChnftrParser.getTextSpliter(label, "helv_nr", "chinese", fontSize, Integer.MAX_VALUE);
	    	/*if (npa.nodeLevel == 0)
	    		npa.nodeLevel = 1;
	    	if (npb.nodeLevel == 0)
	    		npb.nodeLevel = ++maxLevel;*/
	    	jaEdges.put(new JSONObject()
	    			.put("id", ep.edgeId)
	    			.put("from", npa.nodeId)
	    			.put("to", npb.nodeId)
	    			.put("label", label)
	    			//.put("length", Math.max(ts.getWidthPoint() + 50, 100))
	    			.put("color", ep.isReadyDel ? "#ff0000" : ep.isNewJoin ? "#00ff00" : ep.isDirty ? "#0000ff" : "#848484"));
	    }
	    if (maxLevel < 4)
	    	maxLevel = 4;
	    List<NodeProp> nodeList = getSortedNodeList();
	    for (int i = 0; i < nodeList.size();) {
	    	NodeProp np = nodeList.get(i);
	    	if (np.nodeLevel != 0) {
	    		jaNodes.put(new JSONObject() 
	    			.put("id", np.nodeId)
					.put("label", np.getNodeLabel())
					.put("level", np.nodeLevel));
	    		nodeList.remove(i);
	    	} else
	    		i++;
	    }
	    for (int i = 0; i < nodeList.size(); i++) {
	    	NodeProp np = nodeList.get(i);
    		jaNodes.put(new JSONObject() 
    			.put("id", np.nodeId)
				.put("label", np.getNodeLabel())
				.put("level", i % maxLevel + 1));
	    }
	    
		JSONObject jo = new JSONObject()
			.put("containerId", "divJoinsContent")
			.put("options", joOptions)
			.put("items", new JSONObject().put("nodes", jaNodes).put("edges", jaEdges));


		for (String es : new String[] {Events.ON_AFTER_SIZE, "onVisselectNode", "onVisselectEdge", "onVisdeselectNode", "onVisdeselectEdge"}) {
			Iterator<EventListener<?>> it = divJoins.getEventListeners(es).iterator();
			while (it.hasNext())
				divJoins.removeEventListener(es, it.next());
		}

		divJoins.addEventListener("onVisselectNode", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("%s data:%s", event.getName(), event.getData());
				selectNodeEdge(event.getData());
			}
		});
		divJoins.addEventListener("onVisselectEdge", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("%s data:%s", event.getName(), event.getData());
				selectNodeEdge(event.getData());
			}
		});
		divJoins.addEventListener("onVisdeselectNode", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("%s data:%s", event.getName(), event.getData());
				deselectNodeEdge(event.getData());
			}
		});
		divJoins.addEventListener("onVisdeselectEdge", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("%s data:%s", event.getName(), event.getData());
				deselectNodeEdge(event.getData());
			}
		});
		divJoins.addEventListener(Events.ON_AFTER_SIZE, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				//renderErd(jo);
				ZkUtil.js("$('#divJoinsContent').data('visnetwork').redraw()");
			}
		});

		UniLog.log1("jo:%s", jo.toString(3));
		ZkUtil.js("visUtilProcessNetwork(%s,'%s')", jo.toString(), divJoins.getId());
	}

	private void renderErdWithSelectEdge(EdgeProp ep) throws Exception {
		renderErd();
		ZkUtil.js("$('#divJoinsContent').data('visnetwork').selectEdges([%s])", ep.edgeId);
	}
	
	private int setupNodeLevel(Set<String> nodeList, int firstLevel) {
		int level = firstLevel + 1;
		int maxLevel = level;
		for (String nodea : nodeList) {
			NodeProp npa = nodeMap.get(nodea);
			if(npa != null) {
				if (npa.nodeLevel == 0) {
					npa.nodeLevel = level;
					maxLevel = Math.max(setupNodeLevel(npa.joinToNodeIdList, level), maxLevel);
				}
			} else {
				UniLog.log1("Warning !!!! 20240711 npA is null, nodea:%s", nodea);
			}
		}
		return nodeList.isEmpty() ? firstLevel : maxLevel;
	}

	private void selectNodeEdge(Object data) {
		Object[] os = (Object[])data;
		if (ArrayUtils.isNotEmpty(os))
			showJoinsPageProp((String)os[0]);
	}
	
	private void deselectNodeEdge(Object data) {
		Object[] os = (Object[])data;
		String propId = (String)vlJoinsProp.getAttribute("id");
		if (ArrayUtils.isNotEmpty(os) && StringUtils.equals(propId, (String)os[0]))
			showJoinsPageProp("");
	}
	
	private void setupViewCompHeight(ViewProp vp) {
		Iterator<EventListener<?>> it = vp.viewComp.getEventListeners("onCustomResize").iterator();
		if (!it.hasNext()) {
			vp.viewComp.addEventListener("onCustomResize", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					String oldHeight = vp.viewComp.getHeight();
					int listItemsHeight = 0;
					if (vp.listbox != null) {
						for (Listitem li : vp.listbox.getItems()) {
							Integer h = (Integer)li.getAttribute("compHeight");
							if (h == null)
								h = 0;
							//listItemsHeight += Math.max(h, 28);
							listItemsHeight += Math.max(h, 22);
						}
					}
					String newHeight = (43 + listItemsHeight + 5) + "px";
					if (!StringUtils.equals(newHeight, oldHeight)) {
						vp.viewComp.setHeight(newHeight);
						UniLog.log1("viewname:%s, listItemsHeight:%d, newHeight:%s, oldHeight:%s, realHeight:%s", vp.name, listItemsHeight, newHeight, oldHeight, vp.viewComp.getHeight());
						Clients.resize(vp.viewComp);
					}
				}
			});
		}
		Events.echoEvent("onCustomResize", vp.viewComp, null);
	}
	
	private void showViewPageProp(int key) {
		Component[] compList = new Component[] { tbNameProp,       cbDataTypeProp,   cbIsInListProp,  cbIsInvisibleProp, tbFormulaProp,    tbNumFormatProp,  ibLenProp,        tbAccessRightProp };
		String[] evKeyList   = new String[]    { Events.ON_CHANGE, Events.ON_SELECT, Events.ON_CHECK, Events.ON_CHECK,   Events.ON_CHANGE, Events.ON_CHANGE, Events.ON_CHANGE, Events.ON_CHANGE };
		removeCompEventListener(compList, evKeyList);

		lbDebugProp.setVisible(false);
		if (columnMap.containsKey(key)) {
			ColumnProp cp = columnMap.get(key);
			ViewProp vp = cp.viewProp;
			vlNameProp.setVisible(true);
			vlViewIdProp.setVisible(false);
			vlDataTypeProp.setVisible(true);
			vlIsInListProp.setVisible(true);
			vlIsInvisibleProp.setVisible(true);
			vlSeqProp.setVisible(true);
			vlFormulaProp.setVisible(true);
			vlNumFormatProp.setVisible(true);
			vlLengthProp.setVisible(true);
			vlOrderbyListProp.setVisible(false);
			vlAccessRightProp.setVisible(false);
			tbNameProp.setDisabled(!vp.allowUpdate);
			cbDataTypeProp.setDisabled(!vp.allowUpdate);
			cbIsInListProp.setDisabled(!vp.allowUpdate);
			cbIsInvisibleProp.setDisabled(!vp.allowUpdate);

			tbNameProp.setText(cp.name);
			for (Comboitem ci : cbDataTypeProp.getItems()) {
				if (ci.getValue() == cp.dataType) {
					cbDataTypeProp.setSelectedItem(ci);
					break;
				}
			}
			cbIsInListProp.setChecked(cp.isInList);
			cbIsInvisibleProp.setChecked(cp.isInvisible);
			ibSeqProp.setValue(cp.seq);
			ibLenProp.setValue(cp.len);
			tbFormulaProp.setValue(cp.formula);
			tbNumFormatProp.setValue(cp.numFormat);
			lbDebugProp.setValue(String.format("oldName:%s\noldDataType:%s\noldFormula:%s\noldNumFormat:%s", cp.oldName, cp.oldDataType, cp.oldFormula, cp.oldNumFormat));
			tbNameProp.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					UniLog.log1("value:%s, prevValue:%s", event.getValue(), event.getPreviousValue());
					String name = event.getValue().trim();
					/*if (findColumn(vp, name) != null) {
						ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Input name is duplicate");
						tbNameProp.setValue(((String)event.getPreviousValue()).trim());
						return;
					}*/
					if (!StringUtils.equals(event.getValue(), name))
						tbNameProp.setValue(name);
					cp.name = name;
					cp.setDirty();
				}
			});
			cbDataTypeProp.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Comboitem, Object>>() {
				@Override
				public void onZkBiEvent(SelectEvent<Comboitem, Object> event) throws Exception {
					cp.dataType = (DataType)event.getReference().getValue();
					cp.setDirty();
				}
			});
			cbIsInListProp.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					cp.isInList = event.isChecked();
					cp.setDirty();
				}
			});
			cbIsInvisibleProp.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					cp.isInvisible = event.isChecked();
					cp.setDirty();
				}
			});
			tbFormulaProp.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					cp.formula = event.getValue();
					cp.setDirty();
				}
			});
			tbNumFormatProp.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					cp.numFormat = event.getValue();
					cp.setDirty();
				}
			});
			ibLenProp.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					cp.len = NumberUtils.toInt(event.getValue());
					cp.setDirty();
				}
			});
		} else if (viewMap.containsKey(key)) {
			ViewProp vp = viewMap.get(key);
			vlNameProp.setVisible(true);
			vlViewIdProp.setVisible(true);
			vlDataTypeProp.setVisible(false);
			vlIsInListProp.setVisible(false);
			vlIsInvisibleProp.setVisible(false);
			vlSeqProp.setVisible(false);
			vlFormulaProp.setVisible(false);
			vlNumFormatProp.setVisible(false);
			vlLengthProp.setVisible(false);
			vlOrderbyListProp.setVisible(true);
			vlAccessRightProp.setVisible(vp.isNewView);
			tbNameProp.setDisabled(!vp.allowUpdate);
			tbNameProp.setText(vp.name);
			lbDebugProp.setValue(String.format("oldName:%s", vp.oldName));
			tbNameProp.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					UniLog.log1("value:%s, prevValue:%s", event.getValue(), event.getPreviousValue());
					String name = event.getValue().trim();
					if (findView(name) != null) {
						ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Input name is duplicate");
						tbNameProp.setValue(((String)event.getPreviousValue()).trim());
						return;
					}
					if (!StringUtils.equals(event.getValue(), name))
						tbNameProp.setValue(name);
					vp.name = name;
					vp.setDirty(true);
				}
			});
			tbAccessRightProp.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					UniLog.log1("value:%s, prevValue:%s", event.getValue(), event.getPreviousValue());
					if (vp.isNewView) {
						vp.newAccessRight = event.getValue().trim();
						vp.setDirty(true);
					}
				}
			});
			tbViewIdProp.setText(vp.viewId);

			Components.removeAllChildren(vlOrderbyList);
			for (OrderbyItem oi : vp.orderbyList)
				oi.appendInputRow();
			removeCompEventListener(btAddOrderby, Events.ON_CLICK);
			btAddOrderby.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					OrderbyItem oi = new OrderbyItem(vp, null, false, 0);
					oi.appendInputRow();
					vp.orderbyList.add(oi);
					vp.setDirty(true);
				}
			});
		} else {
			vlNameProp.setVisible(false);
			vlViewIdProp.setVisible(false);
			vlDataTypeProp.setVisible(false);
			vlIsInListProp.setVisible(false);
			vlIsInvisibleProp.setVisible(false);
			vlSeqProp.setVisible(false);
			vlFormulaProp.setVisible(false);
			vlNumFormatProp.setVisible(false);
			vlLengthProp.setVisible(false);
			vlOrderbyListProp.setVisible(false);
			vlAccessRightProp.setVisible(false);
		}
	}

	private void showJoinsPageProp(String id) {
		btDeleteLink.setLabel("Delete Link");
		if (edgeMap.containsKey(id)) {
			EdgeProp ep = edgeMap.get(id);
			NodeProp npA = nodeMap.get(ep.nodeIdA);
			vlJoinsProp.setVisible(true);
			tbFromTable.setText(nodeMap.get(ep.nodeIdA).tableName);
			lbFromTable.setValue("From Table");
			lbToTable.setVisible(true);
			divS2ToTable.setVisible(true);
			vlFieldsListProp.setVisible(true);
			vlJoinsOptionProp.setVisible(true);
			cbOneToOne.setChecked(ep.isOneToOne);
			cbOptional.setChecked(ep.isOptional);
			btAddLink.setDisabled(true);
			btDeleteLink.setDisabled(false);
			if (ep.isReadyDel)
				btDeleteLink.setLabel("UnDelete Link");

			clearListbox(s2ToTable);
			for (Map.Entry<String, NodeProp> entry : nodeMap.entrySet()) {
				String nodeId = entry.getKey();
				NodeProp np = entry.getValue();
				if (!np.isDummy() && !StringUtils.equals(nodeId, npA.nodeId)) {
					Listitem li = s2ToTable.appendItem(nodeId, nodeId);
					if (StringUtils.equals(ep.nodeIdB, nodeId))
						s2ToTable.setSelectedItem(li);
				}
			}
			resetupS2Comp(s2ToTable);
			removeCompEventListener(s2ToTable, Events.ON_SELECT);
			s2ToTable.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, Object>>() {
				@Override
				public void onZkBiEvent(SelectEvent<Listitem, Object> event) throws Exception {
					String nodeIdB = event.getReference().getValue();
					npA.joinToNodeIdList.remove(ep.nodeIdB);
					npA.joinToNodeIdList.add(nodeIdB);
					NodeProp onpB = nodeMap.get(ep.nodeIdB);
					NodeProp nnpB = nodeMap.get(nodeIdB);
					onpB.joinFromNodeIdList.remove(npA.nodeId);
					if (onpB.isDummy())
						nodeMap.remove(onpB.nodeId);
					nnpB.joinFromNodeIdList.add(npA.nodeId);
					ep.nodeIdB = nodeIdB;
					for (JoinItem p : ep.joinFieldList) {
						Listbox lb = p.listboxB;
						clearListbox(lb);
						NodeProp npB = nodeMap.get(nodeIdB);
						for (String fieldNameB : npB.fieldNameList)
							lb.appendItem(fieldNameB, fieldNameB);
						resetupS2Comp(lb);
						p.fieldNameB = null;
					}
					ep.setDirty();
					renderErdWithSelectEdge(ep);
				}
			});

			Components.removeAllChildren(vlFieldsFromTo);
			for (JoinItem ji : ep.joinFieldList)
				ji.appendInputRow();
			
			removeCompEventListener(btAddFieldFromTo, Events.ON_CLICK);
			btAddFieldFromTo.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					if (nodeMap.get(ep.nodeIdB).isDummy()) {
						ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Please choose 'To Table' first");
						return;
					}
					JoinItem ji = new JoinItem(ep, null, null);
					ji.appendInputRow();
					ep.joinFieldList.add(ji);
					ep.setDirty();
				}
			});

			removeCompEventListener(cbOneToOne, Events.ON_CHECK);
			cbOneToOne.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					ep.isOneToOne = event.isChecked();
					ep.setDirty();
					renderErdWithSelectEdge(ep);
				}
			});

			removeCompEventListener(cbOptional, Events.ON_CHECK);
			cbOptional.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					ep.isOptional = event.isChecked();
					ep.setDirty();
					renderErdWithSelectEdge(ep);
				}
			});
		} else if (nodeMap.containsKey(id)) {
			NodeProp npA = nodeMap.get(id);
			vlJoinsProp.setVisible(true);
			tbFromTable.setText(npA.tableName);
			lbFromTable.setValue("Table");
			lbToTable.setVisible(false);
			divS2ToTable.setVisible(false);
			vlFieldsListProp.setVisible(false);
			vlJoinsOptionProp.setVisible(false);
			btAddLink.setDisabled(false);
			btDeleteLink.setDisabled(true);
			for (String npNameB : npA.joinToNodeIdList) {
				if (nodeMap.get(npNameB).isDummy()) {
					btAddLink.setDisabled(true);
					break;
				}
			}
		} else {
			vlJoinsProp.setVisible(false);
			btAddLink.setDisabled(true);
			btDeleteLink.setDisabled(true);
		}
		vlJoinsProp.setAttribute("id", id);
	}
	
	private void addLink(String id) throws Exception {
		NodeProp npA = nodeMap.get(id);
		if (npA == null) {
			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, String.format("Table '%s' not found", id));
			return;
		}
		for (String npNameB : npA.joinToNodeIdList) {
			NodeProp npB = nodeMap.get(npNameB);
			if (npB.isDummy()) {
				ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "New link is already exist");
				return;
			}
		}
		NodeProp dummyNode = new NodeProp(null);
		nodeMap.put(dummyNode.nodeId, dummyNode);
		EdgeProp newEdge = new EdgeProp(npA.nodeId, dummyNode.nodeId, true);
		edgeMap.put(newEdge.edgeId, newEdge);
		npA.joinToNodeIdList.add(dummyNode.nodeId);
		dummyNode.joinFromNodeIdList.add(npA.nodeId);
		newEdge.setDirty();
		renderErdWithSelectEdge(newEdge);
		Events.echoEvent("onVisselectEdge", divJoins, new Object[] { newEdge.edgeId });
	}
	
	private void deleteLink(String id) throws Exception {
		EdgeProp ep = edgeMap.get(id);
		ep.isReadyDel = !ep.isReadyDel;
		setJoinsPageDirty(true);
		renderErdWithSelectEdge(ep);
		showJoinsPageProp(id);
	}

	private static void resetupS2Comp(Listbox lb) {
		ZkUtil.delayJs(lb,null,50,"zkbis2.setup('%s',%b,%b,'%s',%b,%b);$('#%s').focus()",lb.getUuid(), lb.isMultiple(), false, StringUtils.defaultString((String)lb.getAttribute("placeholder")), false, false, lb.getUuid());
	}

	private static void removeCompEventListener(Component[] compList, String[] evKeyList) {
		for (int i = 0; i < compList.length; i++) {
			Iterator<EventListener<?>> it = compList[i].getEventListeners(evKeyList[i]).iterator();
			while (it.hasNext())
				compList[i].removeEventListener(evKeyList[i], it.next());
		}
	}

	private static void removeCompEventListener(Component comp, String evKey) {
		Component[] compList = new Component[] { comp };
		String[] evKeyList = new String[] { evKey };
		removeCompEventListener(compList, evKeyList);
	}

	private static void clearListbox(Listbox lb) {
		lb.clearSelection();
		while (lb.getItemCount() > 0)
			lb.removeItemAt(0);
	}
	
	private void setupRightClickPopup(int key, Component comp) {
		comp.addEventListener(Events.ON_RIGHT_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				Menupopup p = (Menupopup)comp.getAttribute("popup");
				if (p == null) {
					p = getMenuPopup(key, "menur", comp);
					comp.setAttribute("popup", p);
				}
				p.open(comp);
			}
		});
	}
	
	private Menupopup getMenuPopup(int key, String idPrefix, Component parentComp) {
		if (columnMap.containsKey(key)) {
			ColumnProp cp = columnMap.get(key);
			ViewProp vp = cp.viewProp;
			return new Menupopup() {{
				final Menupopup popup = this;
				setId(idPrefix + key);
				setParent(parentComp);
				appendChild(new Menuitem("Remove Column") {{
					setVisible(!cp.isReadyDel);
					//setDisabled(!vp.allowUpdate);
					setDisabled(true);
					popup.setAttribute("removeColumnMenu", this);
					setIconSclass("z-icon-trash");
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("event:%s", event);
							//removeViewOrColumnWithMsgbox(cp.key);
							markRemoveViewOrColumn(cp.key, true);
						}
					});
				}});
				appendChild(new Menuitem("Reset Column") {{
					setVisible(cp.isReadyDel);
					//setDisabled(!vp.allowUpdate);
					setDisabled(true);
					popup.setAttribute("resetColumnMenu", this);
					setIconSclass("z-icon-trash");
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("event:%s", event);
							//removeViewOrColumnWithMsgbox(cp.key);
							markRemoveViewOrColumn(cp.key, false);
						}
					});
				}});
			}};
		} else {
			ViewProp vp = viewMap.get(key);
			return new Menupopup() {{
				Menupopup popup = this;
				setId(idPrefix + key);
				setParent(parentComp);
				appendChild(new Menuitem("Add Column") {{
					//setDisabled(!vp.allowUpdate);
					setDisabled(true);
					setIconSclass("z-icon-plus");
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("event:%s", event);
							ColumnProp cp = new ColumnProp(newColumnName(vp, "NewColumn"), true);
							cp.fdName = cp.viewLabel = newFdName(vp, 0);
							cp.dataType = DataType.TEXT;
							cp.isInList = true;
							cp.viewProp = vp;
							addColumn(cp);
							//vp.setDirty(true);
							Clients.scrollIntoView(cp.listitem);
						}
					});
				}});
				appendChild(new Menuitem("Remove View") {{
					setVisible(!vp.isReadyDel);
					setDisabled(!vp.allowUpdate);
					popup.setAttribute("removeViewMenu", this);
					setIconSclass("z-icon-trash");
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("event:%s", event);
							markRemoveViewOrColumn(vp.key, true);
						}
					});
				}});
				appendChild(new Menuitem("Reset View") {{
					setVisible(vp.isReadyDel);
					setDisabled(!vp.allowUpdate);
					popup.setAttribute("resetViewMenu", this);
					setIconSclass("z-icon-trash");
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("event:%s", event);
							markRemoveViewOrColumn(vp.key, false);
						}
					});
				}});
			}};
		}
	}
	
	private void markRemoveViewOrColumn(int key, boolean isMark) {
		if (columnMap.containsKey(key)) {
			ColumnProp cp = columnMap.get(key);
			cp.setReadyDel(isMark);
		} else {
			ViewProp vp = viewMap.get(key);
			vp.setReadyDel(isMark);
		}
	}
	private void removeViewOrColumn(int key) {
		if (columnMap.containsKey(key)) {
			ColumnProp cp = columnMap.get(key);
			ViewProp vp = cp.viewProp;
			vp.listbox.removeItemAt(cp.listitem.getIndex());
			cp.treeitem.getParent().removeChild(cp.treeitem);
			Component popupComp = vp.viewComp.getFellowIfAny("menup" + key);
			if (popupComp != null)
				vp.viewComp.removeChild(popupComp);
			UniLog.log1("removeColumn key:%d, popupComp:%s", key, popupComp);
			columnMap.remove(cp.key);
			viewColMap.get(vp.key).remove(cp.key);
			setupViewCompHeight(vp);
			showViewPageProp(0);
			vp.setDirty(true);
		} else {
			ViewProp vp = viewMap.get(key);
			hlView.removeChild(vp.viewComp);
			treechildrenCols.removeChild(vp.treeitem);
			for (int colKey : viewColMap.get(vp.key))
				columnMap.remove(colKey);
			Component popupComp = vp.viewComp.getFellowIfAny("menup" + key);
			if (popupComp != null)
				vp.viewComp.removeChild(popupComp);
			UniLog.log1("removeView key:%d, popupComp:%s", key, popupComp);
			viewColMap.remove(vp.key);
			viewMap.remove(vp.key);
			showViewPageProp(0);
			setViewPageDirty(true);
		}
	}

	private void clearPage() {
		setViewPageDirty(false);
		setJoinsPageDirty(false);
		while (!hlView.getChildren().isEmpty())
			hlView.removeChild(hlView.getFirstChild());
		while (!treechildrenCols.getChildren().isEmpty())
			treechildrenCols.removeChild(treechildrenCols.getFirstChild());
		showViewPageProp(0);
		showJoinsPageProp("");

		btDataModelMt.setVisible(true);

		btAddView.setVisible(false);
		btUploadExcel.setVisible(false);

		btAddLink.setVisible(false);
		btDeleteLink.setVisible(false);
		btAddLink.setDisabled(true);
		btDeleteLink.setDisabled(true);
		btDeleteLink.setLabel("Delete Link");

		tabbox.setSelectedIndex(0);
		tabbox.setVisible(false);
		tabJoins.removeAttribute("alreadyFirstSelected");

		btApply.setVisible(false);
		btApply.setDisabled(true);
		btClose.setVisible(false);

		viewMap.clear();
		columnMap.clear();
		viewColMap.clear();
		
		nodeMap.clear();
		edgeMap.clear();
	}

	private void setViewPageDirty(boolean isDirty) {
		if (isDirty) {
			Map<String, Object> map = getViewPageApplyMessage();
			String message = (String)map.get("message");
			isViewPageDirty = StringUtils.isNotBlank(message);
		} else
			isViewPageDirty = false;
		btApply.setDisabled(!isViewPageDirty && !isJoinsPageDirty);
		showDirty();
	}

	private void setJoinsPageDirty(boolean isDirty) {
		if (isDirty) {
			Map<String, Object> map = getJoinsPageApplyMessage();
			String message = (String)map.get("message");
			isJoinsPageDirty = StringUtils.isNotBlank(message);
		} else
			isJoinsPageDirty = false;
		btApply.setDisabled(!isViewPageDirty && !isJoinsPageDirty);
		showDirty();
	}
	
	private void showDirty() {
		Clients.evalJavaScript(String.format("if (typeof showEditing !== 'undefined'){showEditing(%s);}", (isViewPageDirty || isJoinsPageDirty) ? "true" : "false"));
		Clients.confirmClose((isViewPageDirty || isJoinsPageDirty) ? "Are you sure to leave?" : null);
	}
	
	private static Pair<String, Long> splitModelItemName(String name, long defaultNum) {
		String prefix = name;
		long num = defaultNum;
		Pattern p = Pattern.compile("[0-9]+$");
		Matcher m = p.matcher(name);
		if (m.find()) {
			String numStr = m.group();
			int pos = name.indexOf(numStr);
			prefix = name.substring(0, pos);
			num = Long.parseLong(numStr);
		}
		return Pair.of(prefix, num);
	}

	private static String buildModelItemName(String prefix, long num) {
		return prefix + num;
	}

	private static int getFdNameNum(String fdName) {
		if (!StringUtils.startsWith(fdName, "col_"))
			return 0;
		try {
			String a = fdName.substring(4).toUpperCase();
			return CellReference.convertColStringToIndex(a);
		} catch (Exception ex) {
			return 0;
		}
	}

	private static String buildFdName(int num) {
		return "col_" + CellReference.convertNumToColString(num).toLowerCase();
	}

	private String newViewName(String name) {
		if (findView(name) == null)
			return name;
		Pair<String, Long> p = splitModelItemName(name, 1);
		String prefix = p.getLeft();
		long num = p.getRight();
		do {
			name = buildModelItemName(prefix, ++num);
		} while (findView(name) != null);
		return name;
	}

	private String newColumnName(ViewProp vp, String name) {
		if (findColumns(vp, name).isEmpty())
			return name;
		Pair<String, Long> p = splitModelItemName(name, 1);
		String prefix = p.getLeft();
		long num = p.getRight();
		do {
			name = buildModelItemName(prefix, ++num);
		} while (!findColumns(vp, name).isEmpty());
		return name;
	}

	private String newFdName(ViewProp vp, int num) {
		String fdName = buildFdName(num);
		if (findColumnByFdName(vp, fdName) == null)
			return fdName;
		do {
			fdName = buildFdName(++num);
		} while (findColumnByFdName(vp, fdName) != null);
		return fdName;
	}

	private String translateCellFormula(ExcelPoi exlpoi, Cell cell, CellReference firstSheetPos, List<CellReference> firstSheetPosList, ViewProp p_formulaVp) throws Exception {
		if (cell == null || cell.getCellType() != Cell.CELL_TYPE_FORMULA || StringUtils.isBlank(cell.getCellFormula()))
			return "";
		//String formulaSheetName = p_formulaVp == null ? null : p_formulaVp.nName;
		String formulaSheetName = p_formulaVp == null ? null : p_formulaVp.name;
		UniLog.log1("cell:%s, colrow:%s, firstSheetPos:%s formulaSheetName:%s", cell, getCellPos(cell), firstSheetPos, formulaSheetName);
		try {
			com.kyoko.parser.excelformula.Parser yyparser = new com.kyoko.parser.excelformula.Parser(new CellPositionInterface() {
				@Override
				public int getColIdx() {
					return cell.getColumnIndex();
				}
				@Override
				public int getRowIdx() {
					return cell.getRowIndex();
				}
			});
			
			//parse formula
			//Object parseResult = yyparser.parse(cell.getCellFormula());
			Object parseResult = yyparser.parse(getCellFormulaStr(cell));
			UniLog.log1("parseResult:" + parseResult);
			if (!(parseResult instanceof com.kyoko.parser.Expression))
				throw new Exception("Invalid Formula:" + cell.getCellFormula());
	
			//translate the formula
			com.kyoko.parser.Expression translatedExpression = com.kyoko.parser.ExcelTranslate.translateFromXlsToBi((com.kyoko.parser.Expression)parseResult, null, null, new ColumnTranslateInterface() {
				CellReference getCellReference(String p_worksheet) {
					CellReference cr = null;
					if (p_worksheet != null) {
						Workbook wb = exlpoi.getWorkbook();
						int i = wb.getSheetIndex(p_worksheet);
						if (i >= 0 && i < firstSheetPosList.size())
							cr = firstSheetPosList.get(i);
						UniLog.log1("worksheet index:%d, cr:%s", i, cr);
					} else
						cr = firstSheetPos;
					return cr;
				}
				@Override
				public String cellColumnToBiColumn(String p_worksheet, int col) throws CellException {
					//if (p_worksheet == null)
					//	p_worksheet = exlpoi.excel_getSheetName(exlpoi.getCurrentSheetIndex());
					UniLog.log1("called: worksheet:%s, col:%d", p_worksheet, col);
					/*ViewProp vp = findView(workSheet);
					if (vp != null) {
						for (int colKey : viewColMap.get(vp.key)) {
							ColumnProp cp = columnMap.get(colKey);
							if (cp.sheetCol == col)
								return cp.fdName;
						}
						String fdName = buildFdName(col);
						for (int colKey : viewColMap.get(vp.key)) {
							ColumnProp cp = columnMap.get(colKey);
							if (StringUtils.equals(cp.fdName, fdName))
								return fdName;
						}
					}*/
					CellReference cr = getCellReference(p_worksheet);
					int offset = cr != null ? cr.getCol() : 0;
					UniLog.log1("offset:%d", offset);
					return "col_" + CellReference.convertNumToColString(col + offset).toLowerCase();
				}
				@Override
				public int biColumnToCellColumn( String p_workSheet, String p_label) throws CellException {
					throw new CellException("Method not implemented");
				}
				@Override
				public String getWorkSheetFirstValuePosition(String p_worksheet) {
					//get first data cell position
					UniLog.log1("called: worksheet:%s", p_worksheet);
					CellReference cr = getCellReference(p_worksheet);
					if (cr != null) {
						//String s = getCellPos(cr.getCol(), cr.getRow() + 1);
						String s = getCellPos(cr.getRow() + 1, cr.getCol());  //andrew240205 hotfix, seems row/col messed up
						UniLog.log1("cr:%s, s:%s", cr.formatAsString(), s);
						return s;
					}
					return "A2";
				}
				@Override
				public String getCurrentSheetName() {
					return formulaSheetName;
				}
			});	
			String translatedFormula = translatedExpression.toString();
			UniLog.log1("result: " + translatedFormula);
			return translatedFormula;
		} catch (Error ex) {
			//UniLog.log(getThrowMsg(ex));
			UniLog.log1("error:%s sheet:%s cell:%s", getThrowMsg(ex), formulaSheetName, cell);
			throw new Exception(String.format("%s (SheetName:%s, Cell:%s)", getThrowMsg(ex), formulaSheetName, getCellPos(cell)));
		} catch (Exception ex) {
			//UniLog.log(getThrowMsg(ex));
			UniLog.log1("error:%s sheet:%s cell:%s", getThrowMsg(ex), formulaSheetName, cell);
			throw new Exception(String.format("%s (SheetName:%s, Cell:%s)", getThrowMsg(ex), formulaSheetName, getCellPos(cell)));
		}
	}

	private String getViewPageInvalidMessage() {
		for (ViewProp vp : getSortedViewList()) {
			Set<String> list = new HashSet<String>();
			Set<Integer> colKeyList = viewColMap.get(vp.key);
			for (OrderbyItem oi : vp.orderbyList) {
				if (oi.columnProp == null)
					return String.format("Please input order by field (View: %s)", vp.name);
				if (!colKeyList.contains(oi.columnProp.key))
					return String.format("Please input valid order by field (View: %s)", vp.name);
				if (oi.columnProp.isReadyDel || StringUtils.isBlank(oi.columnProp.viewLabel))
					return String.format("Please input valid order by field (Column: %s, View: %s)", oi.columnProp.name, vp.name);
				int colIdx = 0;
				for (Integer k : colKeyList) {
					ColumnProp cp = columnMap.get(k);
					if (cp.isReadyDel)
						continue;
					colIdx++;
					if (oi.columnProp.key == k) {
						oi.columnIdx = colIdx;
						break;
					}
				}
				list.add(oi.columnProp.viewLabel);
			}
			if (list.size() != vp.orderbyList.size())
				return String.format("Duplicate order by fields (View: %s)", vp.name);
		}
		return null;
	}
	
	private String getJoinsPageInvalidMessage() {
		for (EdgeProp ep : getSortedEdgeList()) {
			NodeProp npa = nodeMap.get(ep.nodeIdA);
			NodeProp npb = nodeMap.get(ep.nodeIdB);
			if(npa == null || npb == null) {
				UniLog.log1("Warning !!!! 20240711 npA or npB is null, nodeIdA:%s:%s, nodeIdB:%s:%s", ep.nodeIdA, npa, ep.nodeIdB, npb);
				continue;
			}
			if (npa.isDummy())
				return String.format("found invalid link");
			if (npb.isDummy() && !ep.isReadyDel)
				return String.format("Please input 'To Table' (From Table: %s)", ep.nodeIdA);
			Set<String> alist = new HashSet<String>();
			Set<String> blist = new HashSet<String>();
			for (JoinItem ji : ep.joinFieldList) {
				if (StringUtils.isBlank(ji.fieldNameA) || StringUtils.isBlank(ji.fieldNameB))
					return String.format("Please input field (From Table: %s, To Table: %s)", ep.nodeIdA, ep.nodeIdB);
				alist.add(ji.fieldNameA);
				blist.add(ji.fieldNameB);
			}
			if (alist.size() != ep.joinFieldList.size() || blist.size() != ep.joinFieldList.size())
				return String.format("Duplicate fields (From Table: %s, To Table: %s)", ep.nodeIdA, ep.nodeIdB);
		}
		return null;
	}

	private Map<String, Object> getJoinsPageApplyMessage() {
		List<EdgeProp> addEdgeList = new ArrayList<EdgeProp>();
		List<EdgeProp> removeEdgeList = new ArrayList<EdgeProp>();
		List<EdgeProp> updateEdgeList = new ArrayList<EdgeProp>();
		StringBuilder sb = new StringBuilder();
		for (EdgeProp ep : getSortedEdgeList()) {
			if (ep.isNewJoin && !ep.isReadyDel) {
				sb.append(String.format("Add join %s?\n", ep.getApplyName()));
				addEdgeList.add(ep);
			} else if (ep.isReadyDel && !ep.isNewJoin) {
				sb.append(String.format("Remove join %s?\n", ep.getApplyName()));
				removeEdgeList.add(ep);
			} else if (ep.isDirty) {
				updateEdgeList.add(ep);
				sb.append(String.format("Update join %s?\n", ep.getApplyName()));
			}
		}
		return MapUtil.of("message", sb.toString(), "addEdgeList", addEdgeList, "removeEdgeList", removeEdgeList, "updateEdgeList", updateEdgeList);
	}
	
	private Map<String, Object> getViewPageApplyMessage() {
		List<ViewProp> addViewList = new ArrayList<ViewProp>();
		List<ViewProp> removeViewList = new ArrayList<ViewProp>();
		List<ViewProp> updateViewList = new ArrayList<ViewProp>();
		List<ColumnProp> addColumnList = new ArrayList<ColumnProp>();
		List<ColumnProp> removeColumnList = new ArrayList<ColumnProp>();
		List<ColumnProp> updateColumnList = new ArrayList<ColumnProp>();
		List<ViewProp> loadDataList = new ArrayList<ViewProp>();
		StringBuilder sb = new StringBuilder();
		for (ViewProp vp : getSortedViewList()) {
			if (!vp.allowUpdate)
				continue;
			if (vp.isNewView && !vp.isReadyDel) {
				sb.append(String.format("Add view '%s'?\n", vp.getApplyName()));
				addViewList.add(vp);
			} else if (vp.isReadyDel && !vp.isNewView) {
				sb.append(String.format("Remove view '%s'?\n", vp.getApplyName()));
				removeViewList.add(vp);
			} else {
				if (vp.isDirty) {
					sb.append(String.format("Update view '%s'?\n", vp.getApplyName()));
					updateViewList.add(vp);
				}
				for (int colKey : viewColMap.get(vp.key)) {
					ColumnProp cp = columnMap.get(colKey);
					if (cp.isNewCol && !cp.isReadyDel) {
						sb.append(String.format("Add column '%s' (View:%s)?\n", cp.getApplyName(), vp.getApplyName()));
						addColumnList.add(cp);
					} else if (cp.isReadyDel && !cp.isNewCol) {
						sb.append(String.format("Remove column '%s' (View:%s)?\n", cp.getApplyName(), vp.getApplyName()));
						removeColumnList.add(cp);
					} else if (cp.isDirty) {
						sb.append(String.format("Update column '%s' (View:%s)?\n", cp.getApplyName(), vp.getApplyName()));
						updateColumnList.add(cp);
					}
				}
			}
			if (!vp.isReadyDel && !vp.readSheetRowList.isEmpty()) {
				sb.append(String.format("Load data '%s' (%d rows)?\n", vp.getApplyName(), vp.readSheetRowList.size()));
				loadDataList.add(vp);
			}
		}
		return MapUtil.of("message", sb.toString(), "addViewList", addViewList, "removeViewList", removeViewList, "updateViewList", updateViewList, "addColumnList", addColumnList, "removeColumnList", removeColumnList, "updateColumnList", updateColumnList, "loadDataList", loadDataList);
	}
	
	private void applyRecords() throws Exception {
		String errMsg = this.getViewPageInvalidMessage();
		if (errMsg != null) {
			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, errMsg);
			return;
		}

		errMsg = this.getJoinsPageInvalidMessage();
		if (errMsg != null) {
			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, errMsg);
			return;
		}
		
		Map<String, Object> map = getViewPageApplyMessage();
		List<ViewProp> addViewList = (List<ViewProp>)map.get("addViewList");
		List<ViewProp> removeViewList = (List<ViewProp>)map.get("removeViewList");
		List<ViewProp> updateViewList = (List<ViewProp>)map.get("updateViewList");
		List<ColumnProp> addColumnList = (List<ColumnProp>)map.get("addColumnList");
		List<ColumnProp> removeColumnList = (List<ColumnProp>)map.get("removeColumnList");
		List<ColumnProp> updateColumnList = (List<ColumnProp>)map.get("updateColumnList");
		List<ViewProp> loadDataList = (List<ViewProp>)map.get("loadDataList");
		String viewPageMessage = (String)map.get("message");
		
		map = getJoinsPageApplyMessage();
		List<EdgeProp> addEdgeList = (List<EdgeProp>)map.get("addEdgeList");
		List<EdgeProp> removeEdgeList = (List<EdgeProp>)map.get("removeEdgeList");
		List<EdgeProp> updateEdgeList = (List<EdgeProp>)map.get("updateEdgeList");
		String joinsPageMessage = (String)map.get("message");

		String message = viewPageMessage + joinsPageMessage;
		if (StringUtils.isBlank(message)) {
			ZkBiMsgbox.show("No Operation");
			return;
		}

		ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Ok")), new ZkBiMsgboxButton(sessionHelper.getBtLabel("Cancel"))};
		new ZkBiMsgbox(sessionHelper).setType(ZkBiMsgbox.Type.question).setContent(ZkBiMsgbox.buildMsgboxContentComp(message)).setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
			void processToDb() throws Exception {
				String msg1 = processViewPageToDb(addViewList, removeViewList, updateViewList, addColumnList, removeColumnList, updateColumnList, loadDataList);
				String msg2 = processJoinsPageToDb(addEdgeList, removeEdgeList, updateEdgeList);
				StringBuilder sbMsg = new StringBuilder();
				if (msg1 != null)
					sbMsg.append(msg1);
				if (msg2 != null)
					sbMsg.append(msg2);
				new ZkBiMsgbox(sessionHelper)
					.setContent(ZkBiMsgbox.buildMsgboxContentComp(StringUtils.defaultIfBlank(sbMsg.toString(), "All operations completed")))
					.setButtons(new String[] {"Close"})
					.build().doModal();
				clearPage();
			}
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
				if (btn.getIdx() == 0) {
					String msg = processViewPageToDbPreCheck(loadDataList);
					if (StringUtils.isNotBlank(msg)) {
						new ZkBiMsgbox(sessionHelper)
							.setContent(ZkBiMsgbox.buildMsgboxContentComp(msg))
							.setButtons(new String[] {"Continue", "Cancel"})
							.setEventListener(new ZkBiEventListener<Event>() {
								@Override
								public void onZkBiEvent(Event event) throws Exception {
									ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
									if (btn.getIdx() == 0)
										processToDb();
								}
							})
							.build().doModal();
						return;
					}
					processToDb();
				}
			}
		}).build().doModal();
	}
	
	private String processViewPageToDbPreCheck(List<ViewProp> loadDataList) {
		List<String> ss = new ArrayList<String>();
		HashSet<String> errFieldHS = new HashSet<String>(); //avoid duplicate error msg for same column
		try {
			//load data precheck
			for (ViewProp vp : loadDataList) {
				UniLog.log1("Load data precheck viewId:%s, exlpoi:%s, sheetNum:%d, firstCellPos:%s, readSheetRowCount:%d", vp.viewId, vp.exlpoi, vp.sheetNum, vp.firstCellPos, vp.readSheetRowList.size());
				ExcelPoi exlpoi = vp.exlpoi;
				exlpoi.excel_useSheet(vp.sheetNum);
				String sheetName = exlpoi.excel_getSheetName(vp.sheetNum);
				for (int row : vp.readSheetRowList) {
					for (int cpKey : viewColMap.get(vp.key)) {
						ColumnProp cp = columnMap.get(cpKey);
						if (cp.isReadyDel || !cp.needLoadData)
							continue;
						int col = cp.sheetCol;
						Cell cell = exlpoi.getCell(row, col);

						Map<String, Object> map = getDataType(cell);
						DataType dataType = (DataType)map.get("dataType");
						//TODO should ignore for #N/A cell
						if (cp.dataType != DataType.TEXT && dataType != cp.dataType && !errFieldHS.contains(sheetName+":"+col)) {  //andrew240223 too much error msg. one error msg per column
							errFieldHS.add(sheetName+":"+col);
							String log = String.format("Cell type not match (sheet:%s col:%s coltype:%s cell:%s celltype:%s)", sheetName, cp.name, cp.dataType.getName(), getCellPos(row, col), dataType.getName());
							UniLog.log1(log);
							ss.add(log);
						}
						else {
							Object v = getCellValue(exlpoi, cell, cp.dataType);
							if (v == null) {
								String log = String.format("Invalid cell value (sheet:%s col:%s cell:%s)", sheetName, cp.name, getCellPos(row, col));
								UniLog.log1(log);
								ss.add(log);
							}
						}
						if (ss.size() > 9999) { //andrew240223 bigger threshold
							ss.add("...");
							return String.join("\n", ss);
						}
					}
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return String.join("\n", ss);
	}
	
	private String processJoinsPageToDb(List<EdgeProp> addEdgeList, List<EdgeProp> removeEdgeList, List<EdgeProp> updateEdgeList) {
		String dbName = sessionHelper.getDbName();
		BiResult br = null;
		List<String> resultMsgList = new ArrayList<String>();
		List<Object> pList = new ArrayList<Object>();
		try {
			//add Join
			if (!addEdgeList.isEmpty()) {
				br = sessionHelper.newBiResult("bischema.BiJoins");
				for (EdgeProp ep : addEdgeList) {
					pList.add(ep);
					br.beginWork();
					UniLog.log1("add join nodeIdA:%s, nodeIdB:%s, optional:%b, onotoone:%b", ep.nodeIdA, ep.nodeIdB, ep.isOptional, ep.isOneToOne);
					br.clearCurrentRec();
					try {
						//int rg = br.getView().getSchema().getUniqueRg(br, "", 20001, "dddjoinhdr", "ddjh_rg", "").toInt();
						//int rg = br.getView().getSchema().getRg(br, "", 20001);
						//br.getCell("ddjh_rg").set(rg);
						br.getCell("ddjh_database").set(dbName);
						br.getCell("ddjh_tabnamea").set(ep.nodeIdA);
						br.getCell("ddjh_tabnameb").set(ep.nodeIdB);
						br.getCell("ddjh_optional").set(ep.isOptional);
						br.getCell("ddjh_onetone").set(ep.isOneToOne);
					} catch (Exception ex) {
						throw new Exception(String.format("Add join %s - ERROR (%s)", ep.getApplyName(), getThrowMsg(ex)));
					}
					BiResult sr = br.getSubLink("bischema.BiJoinDet");
					int seq = -1;
					for (JoinItem ji : ep.joinFieldList) {
						UniLog.log1("add joindet fieldNameA:%s, fieldNameB:%s", ji.fieldNameA, ji.fieldNameB);
						BiCellCollection col = sr.newRowCollection();								
						sr.addSubRecord(col, -1 ,"");
						try {
							col.getCell("ddjd_idx").set(++seq);
							col.getCell("ddjd_joinfielda").set(ji.fieldNameA);
							col.getCell("ddjd_joinfieldb").set(ji.fieldNameB);
						} catch (Exception ex) {
							throw new Exception(String.format("Add join->joindet %s - ERROR (%s)", ep.getApplyName(), getThrowMsg(ex)));
						}
					}
					ReturnMsg rtn = br.addCurrent();
					if (!rtn.getStatus())
						throw new Exception(String.format("Add join %s - ERROR (%s)", ep.getApplyName(), rtn.getMsg()));
					br.commitWork();
					resultMsgList.add(String.format("Add join %s - OK", ep.getApplyName()));
				}
				br = null;
			}
			
			//remove join
			if (!removeEdgeList.isEmpty()) {
				br = sessionHelper.newBiResult("bischema.BiJoins");
				for (EdgeProp ep : removeEdgeList) {
					UniLog.log1("removejoin edgeId:%s, rg:%d, tabnamea:%s, tabnameb:%s", ep.edgeId, ep.rg, ep.oldTableNameA, ep.oldTableNameB);
					pList.add(ep);
					br.beginWork();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("ddjh_rg = %d", ep.rg));
					if (rtn.getStatus()) {
						if (br.query(true).getStatus() && br.getRowCount() > 0) {
							br.fetchOneRecV(0);
							Object o = br.getTrStatObj(0);
							br.markDelete(o, true);
							rtn = br.batchAddUpdateDelete();
							if (!rtn.getStatus())
								throw new Exception(String.format("Remove join %s - ERROR (%s)", ep.getApplyName(), rtn.getMsg()));
						}
					} else
						throw new Exception(String.format("Remove join %s - ERROR (%s)", ep.getApplyName(), rtn.getMsg()));
					br.commitWork();
					resultMsgList.add(String.format("Remove join %s - OK", ep.getApplyName()));
				}
				br = null;
			}
			
			//update join
			if (!updateEdgeList.isEmpty()) {
				br = sessionHelper.newBiResult("bischema.BiJoins");
				for (EdgeProp ep : updateEdgeList) {
					UniLog.log1("updatejoin edgeId:%s, rg:%d, tabnamea:%s, tabnameb:%s", ep.edgeId, ep.rg, ep.oldTableNameA, ep.oldTableNameB);
					pList.add(ep);
					br.beginWork();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("ddjh_rg = %d", ep.rg));
					if (rtn.getStatus()) {
						if (br.query(true).getStatus() && br.getRowCount() > 0) {
							br.fetchOneRecV(0);
							try {
								br.getCell("ddjh_tabnamea").set(ep.nodeIdA);
								br.getCell("ddjh_tabnameb").set(ep.nodeIdB);
								br.getCell("ddjh_optional").set(ep.isOptional);
								br.getCell("ddjh_onetone").set(ep.isOneToOne);
							} catch (Exception ex) {
								throw new Exception(String.format("Update join %s - ERROR (%s)", ep.getApplyName(), rtn.getMsg()));
							}
							BiResult sr = br.getSubLink("bischema.BiJoinDet");
							int rowCount = sr.getRowCount();
							int seq = -1;
							for (int i = 0; i < ep.joinFieldList.size(); i++) {
								JoinItem ji = ep.joinFieldList.get(i);
								try {
									BiCellCollection col;
									if (i < rowCount)
										col = sr.getRowCollectionV(i);
									else {
										col = sr.newRowCollection();								
										sr.addSubRecord(col, -1, "");
									}
									col.getCell("ddjd_idx").set(++seq);
									col.getCell("ddjd_joinfielda").set(ji.fieldNameA);
									col.getCell("ddjd_joinfieldb").set(ji.fieldNameB);
								} catch (Exception ex) {
									throw new Exception(String.format("update join->joindet %s - ERROR (%s)", ep.getApplyName(), getThrowMsg(ex)));
								}
							}
							for (int i = ep.joinFieldList.size(); i < rowCount; i++) {
								Object o = sr.getTrStatObj(i);
								sr.markDelete(o, true);
							}
							rtn = br.updateCurrent();
							if (rtn != null && !rtn.getStatus())
								throw new Exception(String.format("Update join %s - ERROR (%s)", ep.getApplyName(), rtn.getMsg()));
						}
					}
					br.commitWork();
					resultMsgList.add(String.format("Update join %s - OK", ep.getApplyName()));
				}
			}

			if (!addEdgeList.isEmpty() || !removeEdgeList.isEmpty() || !updateEdgeList.isEmpty())
				BiSchema.loadSchema(sessionHelper, true);
		} catch (Exception ex) {
			UniLog.log(ex);
			resultMsgList.add(getThrowMsg(ex));
			if (br != null) {
				UniLog.log1("rollbackWork br");
				br.rollbackWork();
			}
		}

		//handle abort
		for (EdgeProp ep : addEdgeList) {
			if (!pList.contains(ep))
				resultMsgList.add(String.format("Add join %s - ABORTED", ep.getApplyName()));
		}
		for (EdgeProp ep : removeEdgeList) {
			if (!pList.contains(ep))
				resultMsgList.add(String.format("Remove join %s - ABORTED", ep.getApplyName()));
		}
		for (EdgeProp ep : updateEdgeList) {
			if (!pList.contains(ep))
				resultMsgList.add(String.format("Update join %s - ABORTED", ep.getApplyName()));
		}
		return String.join("\n", resultMsgList);
	}
	
	private String processViewPageToDb(List<ViewProp> addViewList, List<ViewProp> removeViewList, List<ViewProp> updateViewList, List<ColumnProp> addColList, List<ColumnProp> removeColList, List<ColumnProp> updateColList, List<ViewProp> loadDataList) {
		String dbName = sessionHelper.getDbName();
		BiResult br = null;
		BiResult br1 = null;
		List<String> resultMsgList = new ArrayList<String>();
		List<Object> pList = new ArrayList<Object>();
		List<ViewProp> addMenuList = new ArrayList<ViewProp>();
		List<ViewProp> removeMenuList = new ArrayList<ViewProp>();
		List<ViewProp> updateMenuList = new ArrayList<ViewProp>();
		boolean loadedSchema = false;
		try {
			//add view
			if (!addViewList.isEmpty()) {
				br = sessionHelper.newBiResult("bischema.BiView");
				for (ViewProp vp : addViewList) {
					pList.add(vp);
					br.beginWork();
					String tabName = DataModelHelper.normalizeTag(vp.name);
					vp.viewId = dbName + "." + tabName;
					vp.tabName = tabName;
					UniLog.log1("add view dbName:%s, tabName:%s, viewId:%s, header:%s, firstCellPos:%s", dbName, tabName, vp.viewId, vp.name, vp.firstCellPos);
					br.clearCurrentRec();
					try {
						br.getCell("grpth_database").set(dbName);
						br.getCell("grpth_table").set(vp.tabName);
						br.getCell("grpth_id").set(vp.viewId);
						br.getCell("grpth_header").set(vp.name);
						br.getCell("grpth_remark").set(vp.name);
						//br.getCell("grpth_attribute").set("NNNNNNNYY");
						br.getCell("grpth_attribute").set("NNYNNNNYY");  //andrew240201 set default allow import
						br.getCell("grpth_delaccess").set("#delete");  //andrew240207 allow delete
						br.getCell("grpth_batupdaccess").set("#batchupd");  //andrew240207 allow batchupdate
						
						br.getCell("grpth_biclass").set("com.uniinformation.bicore.bischema.BiResultExcelSheet");
					} catch (Exception ex) {
						throw new Exception(String.format("Add view '%s' - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
					}
					BiResult sr = br.getSubLink("bischema.BiColumn");
					for (int cpKey : viewColMap.get(vp.key)) {
						ColumnProp cp = columnMap.get(cpKey);
						if (cp.isReadyDel)
							continue;
						UniLog.log1("add viewcolumn header:%s, fdname:%s, label:%s, dataType:%s, formula:%s, numFormat:%s, len:%d", cp.name, cp.fdName, cp.viewLabel, cp.dataType, cp.formula, cp.numFormat, cp.len);
						BiCellCollection col = sr.newRowCollection();								
						sr.addSubRecord(col, -1 ,"");
						try {
							col.getCell("grptc_subtable").set(tabName);
							col.getCell("grptc_seq").set(cp.seq);
							col.getCell("grptc_fd").set(cp.fdName);
							col.getCell("grptc_label").set(cp.viewLabel);
							col.getCell("grptc_header").set(cp.name);
							col.getCell("grptc_fdtype").set(cp.dataType.getFdtype());
							col.getCell("grptc_fdlen").set(cp.len);
							col.getCell("grptc_inlist").set(cp.isInList);
							col.getCell("grptc_invisible").set(cp.isInvisible);
							col.getCell("grptc_attribute").set("NNNNNNNNNNNY");
							col.getCell("grptc_formula").set(cp.formula);
							//col.getCell("grptc_format").set(cp.numFormat);
							col.getCell("grptc_format").set(""); //andrew240129 cannot set excel format to here. Unquoted special character ';' in pattern "_-* #,##0_-;\-* #,##0_-;_-* "-"??_-;_-@_-"
						} catch (Exception ex) {
							throw new Exception(String.format("Add view->column '%s' - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
						}
					}
					sr = br.getSubLink("bischema.BiOrderby");
					int seq = 0;
					for (OrderbyItem oi : vp.orderbyList) {
						ColumnProp cp = oi.columnProp;
						BiCellCollection col = sr.newRowCollection();								
						sr.addSubRecord(col, -1 ,"");
						try {
							col.getCell("grpto_seq").set(++seq);
							col.getCell("grpto_fd").set(cp.viewLabel);
							col.getCell("grpto_desc").set(oi.isDesc);
							col.getCell("grpto_colidx").set(oi.columnIdx);
							UniLog.log1("add orderby header:%s, label:%s, seq:%d, desc:%b, colidx:%d", cp.name, cp.viewLabel, seq, oi.isDesc, oi.columnIdx);
						} catch (Exception ex) {
							throw new Exception(String.format("Add view->orderby '%s' - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
						}
					}

					ReturnMsg rtn = br.addCurrent();
					if (!rtn.getStatus())
						throw new Exception(String.format("Add view '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
					br.commitWork();
					resultMsgList.add(String.format("Add view '%s' - OK", vp.getApplyName()));
					addMenuList.add(vp);
				}
				br = null;
			}

			/*//add menu
			for (ViewProp vp : addViewList) {
				ReturnMsg rtn = dataModelHelper.updateWebMenu(vp.viewId, vp.name, 0, false);
				if (!rtn.getStatus())
					throw new Exception(String.format("Add menu '%s/%s' - ERROR (%s)", vp.name, vp.viewId, rtn.getMsg()));
				resultMsgList.add(String.format("Add menu '%s/%s' - OK", vp.name, vp.viewId));
			}*/

			//remove view
			if (!removeViewList.isEmpty()) {
				br = sessionHelper.newBiResult("bischema.BiView");
				br1 = sessionHelper.newBiResult("bischema.BiTable");
				for (ViewProp vp : removeViewList) {
					UniLog.log1("removeview viewrg:%d, viewid:%s, tabname:%s", vp.viewRg, vp.viewId, vp.tabName);
					pList.add(vp);
					br.beginWork();
					br1.beginWork();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("grpth_rg = %d", vp.viewRg));
					if (rtn.getStatus()) {
						if (br.query(true).getStatus() && br.getRowCount() > 0) {
							br.fetchOneRecV(0);
							Object o = br.getTrStatObj(0);
							br.markDelete(o, true);
							rtn = br.batchAddUpdateDelete();
							if (!rtn.getStatus())
								throw new Exception(String.format("Remove view '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
						}
					} else
						throw new Exception(String.format("Remove view '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
					br1.clearCondition();
					rtn = br1.addCustomCondition(String.format("ddt_tabname = '%s'", vp.tabName));
					if (rtn.getStatus()) {
						if (br1.query(true).getStatus() && br1.getRowCount() > 0) {
							br1.fetchOneRecV(0);
							Object o = br1.getTrStatObj(0);
							br1.markDelete(o, true);
							rtn = br1.batchAddUpdateDelete();
							if (!rtn.getStatus())
								throw new Exception(String.format("Remove view '%s' - ERROR (%s, TableName:%s)", vp.getApplyName(), rtn.getMsg(), vp.tabName));
						}
					} else
						throw new Exception(String.format("Remove view '%s' - ERROR (%s, TableName:%s)", vp.getApplyName(), rtn.getMsg(), vp.tabName));
					br1.commitWork();
					br.commitWork();
					resultMsgList.add(String.format("Remove view '%s' - OK", vp.getApplyName()));
					removeMenuList.add(vp);
				}
				br = null;
				br1 = null;
			}

			/*//remove menu
			for (ViewProp vp : removeViewList) {
				ReturnMsg rtn = dataModelHelper.updateWebMenu(vp.viewId, vp.name, 0, true);
				if (!rtn.getStatus())
					throw new Exception(String.format("Remove menu '%s/%s' - ERROR (%s)", vp.name, vp.viewId, rtn.getMsg()));
				resultMsgList.add(String.format("Remove menu '%s/%s' - OK", vp.name, vp.viewId));
			}*/
			
			//add column
			if (!addColList.isEmpty()) {
				Map<ViewProp, List<ColumnProp>> map = new LinkedHashMap<ViewProp, List<ColumnProp>>();
				for (ColumnProp cp : addColList) {
					List<ColumnProp> cpList = map.get(cp.viewProp);
					if (cpList == null) {
						cpList = new ArrayList<ColumnProp>();
						map.put(cp.viewProp, cpList);
					}
					cpList.add(cp);
				}
				br = sessionHelper.newBiResult("bischema.BiView");
				for (Map.Entry<ViewProp, List<ColumnProp>> entry : map.entrySet()) {
					boolean updatedView = false;
					ViewProp vp = entry.getKey();
					List<ColumnProp> cpList = entry.getValue();
					pList.addAll(cpList);
					br.beginWork();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("grpth_rg = %d", vp.viewRg));
					if (rtn.getStatus()) {
						if (br.query(true).getStatus() && br.getRowCount() > 0) {
							br.fetchOneRecV(0);
							if (updateViewList.contains(vp)) {
								updateViewList.remove(vp);
								pList.add(vp);
								UniLog.log1("update view vpname:%s", vp.name);
								try {
									br.getCell("grpth_header").set(vp.name);
									br.getCell("grpth_remark").set(vp.name);
								} catch (Exception ex) {
									throw new Exception(String.format("Update view '%s' - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
								}
								BiResult sr = br.getSubLink("bischema.BiOrderby");
								int rowCount = sr.getRowCount();
								int seq = 0;
								for (int i = 0; i < vp.orderbyList.size(); i++) {
									OrderbyItem oi = vp.orderbyList.get(i);
									ColumnProp cp = oi.columnProp;
									try {
										BiCellCollection col;
										if (i < rowCount)
											col = sr.getRowCollectionV(i);
										else {
											col = sr.newRowCollection();								
											sr.addSubRecord(col, -1, "");
										}
										col.getCell("grpto_seq").set(++seq);
										col.getCell("grpto_fd").set(cp.viewLabel);
										col.getCell("grpto_desc").set(oi.isDesc);
										col.getCell("grpto_colidx").set(oi.columnIdx);
										UniLog.log1("update orderby header:%s, label:%s, seq:%d, desc:%b, colidx:%d", cp.name, cp.viewLabel, seq, oi.isDesc, oi.columnIdx);
									} catch (Exception ex) {
										throw new Exception(String.format("Update view->orderby %s - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
									}
								}
								for (int i = vp.orderbyList.size(); i < rowCount; i++) {
									Object o = sr.getTrStatObj(i);
									sr.markDelete(o, true);
									UniLog.log1("remove orderby %d", i);
								}
								updatedView = true;
							}

							BiResult brColumn = br.getSubLink("bischema.BiColumn");
							for (ColumnProp cp : cpList) {
								UniLog.log1("add viewcolumn header:%s, fdname:%s, label:%s, dataType:%s, formula:%s, numFormat:%s, len:%d", cp.name, cp.fdName, cp.viewLabel, cp.dataType, cp.formula, cp.numFormat, cp.len);
								BiCellCollection col = brColumn.newRowCollection();								
								brColumn.addSubRecord(col, -1 ,"");
								try {
									col.getCell("grptc_subtable").set(vp.tabName);
									col.getCell("grptc_seq").set(cp.seq);
									col.getCell("grptc_fd").set(cp.fdName);
									col.getCell("grptc_label").set(cp.viewLabel);
									col.getCell("grptc_header").set(cp.name);
									col.getCell("grptc_fdtype").set(cp.dataType.getFdtype());
									col.getCell("grptc_fdlen").set(cp.len);
									col.getCell("grptc_inlist").set(cp.isInList);
									col.getCell("grptc_invisible").set(cp.isInvisible);
									col.getCell("grptc_attribute").set("NNNNNNNNNNNY");
									col.getCell("grptc_formula").set(cp.formula);
									//col.getCell("grptc_format").set(cp.numFormat);
									col.getCell("grptc_format").set(""); //andrew240129 cannot set excel format to here. Unquoted special character ';' in pattern "_-* #,##0_-;\-* #,##0_-;_-* "-"??_-;_-@_-"
								} catch (Exception ex) {
									throw new Exception(String.format("Add view->column '%s' - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
								}
							}
							rtn = br.updateCurrent();
							if (rtn != null && !rtn.getStatus())
								throw new Exception(String.format("Add %scolumns '%s' - ERROR (%s, View:%s)", updatedView ? "view," : "", getColumnNameListString(cpList), rtn.getMsg(), vp.getApplyName()));
						}
					} else
						throw new Exception(String.format("Add %scolumns '%s' - ERROR (%s, View:%s)", updatedView ? "view," : "", getColumnNameListString(cpList), rtn.getMsg(), vp.getApplyName()));
					br.commitWork();
					resultMsgList.add(String.format("Add columns '%s' - OK (View:%s)", getColumnNameListString(cpList), vp.getApplyName()));
					if (updatedView) {
						resultMsgList.add(String.format("Update view '%s' - OK", vp.getApplyName()));
						updateMenuList.add(vp);
					}
				}
				br = null;
			}
			
			//remove column
			/*if (!removeColList.isEmpty()) {
				Map<ViewProp, List<ColumnProp>> map = new LinkedHashMap<ViewProp, List<ColumnProp>>();
				for (ColumnProp cp : removeColList) {
					List<ColumnProp> cpList = map.get(cp.viewProp);
					if (cpList == null) {
						cpList = new ArrayList<ColumnProp>();
						map.put(cp.viewProp, cpList);
					}
					cpList.add(cp);
				}
				br = sessionHelper.newBiResult("bischema.BiColumn");
				for (Map.Entry<ViewProp, List<ColumnProp>> entry : map.entrySet()) {
					br.beginWork();
					ViewProp vp = entry.getKey();
					List<ColumnProp> cpList = entry.getValue();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("grptc_mrg = %d", vp.viewRg));
					if (rtn.getStatus()) {
					    rtn = br.query(true);
					    UniLog.log1("query rtn:%s", rtn);
					}
				}
			}*/
			/*if (!removeColList.isEmpty()) {
				Map<ViewProp, List<ColumnProp>> map = new LinkedHashMap<ViewProp, List<ColumnProp>>();
				for (ColumnProp cp : removeColList) {
					List<ColumnProp> cpList = map.get(cp.viewProp);
					if (cpList == null) {
						cpList = new ArrayList<ColumnProp>();
						map.put(cp.viewProp, cpList);
					}
					cpList.add(cp);
				}
				br = sessionHelper.newBiResult("bischema.BiView");
				for (Map.Entry<ViewProp, List<ColumnProp>> entry : map.entrySet()) {
					br.beginWork();
					ViewProp vp = entry.getKey();
					List<ColumnProp> cpList = entry.getValue();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("grpth_rg = %d", vp.viewRg));
					if (rtn.getStatus()) {
						if (br.query(true).getStatus()) {
							BiResult brColumn = br.getSubLink("bischema.BiColumn");
							for (int i = 0; i < brColumn.getRowCount(); i++) {
								brColumn.fetchOneRecV(i);
								String viewLabel = br.getCellString("grptc_label");
								for (ColumnProp cp : cpList) {
									if (StringUtils.equals(cp.viewLabel, viewLabel)) {
										Object o = brColumn.getTrStatObj(i);
										brColumn.markDelete(o, true);
										UniLog.log1("remove column vpname:%s, cpname:%s, viewlabel:%s", vp.name, cp.name, viewLabel);
										break;
									}
								}
							}
							rtn = brColumn.batchAddUpdateDelete();
							if (rtn != null && !rtn.getStatus())
								throw new Exception(String.format("Remove Column:%s (ViewName:%s, ColumnNames:%s)", rtn.getMsg(), vp.name, getColumnNameListString(removeColList)));
						}
					} else
						throw new Exception(String.format("Remove Column:%s (ViewName:%s, ColumnNames:%s)", rtn.getMsg(), vp.name, getColumnNameListString(removeColList)));
					br.commitWork();
				}
				br = null;
			}*/
			
			//update column
			if (!updateColList.isEmpty()) {
				Map<ViewProp, List<ColumnProp>> map = new LinkedHashMap<ViewProp, List<ColumnProp>>();
				for (ColumnProp cp : updateColList) {
					List<ColumnProp> cpList = map.get(cp.viewProp);
					if (cpList == null) {
						cpList = new ArrayList<ColumnProp>();
						map.put(cp.viewProp, cpList);
					}
					cpList.add(cp);
				}
				br = sessionHelper.newBiResult("bischema.BiView");
				for (Map.Entry<ViewProp, List<ColumnProp>> entry : map.entrySet()) {
					boolean updatedView = false;
					ViewProp vp = entry.getKey();
					List<ColumnProp> cpList = entry.getValue();
					pList.addAll(cpList);
					br.beginWork();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("grpth_rg = %d", vp.viewRg));
					if (rtn.getStatus()) {
						if (br.query(true).getStatus() && br.getRowCount() > 0) {
							br.fetchOneRecV(0);
							if (updateViewList.contains(vp)) {
								updateViewList.remove(vp);
								pList.add(vp);
								UniLog.log1("update view vpname:%s", vp.name);
								try {
									br.getCell("grpth_header").set(vp.name);
									br.getCell("grpth_remark").set(vp.name);
								} catch (Exception ex) {
									throw new Exception(String.format("Update view '%s' - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
								}
								BiResult sr = br.getSubLink("bischema.BiOrderby");
								int rowCount = sr.getRowCount();
								int seq = 0;
								for (int i = 0; i < vp.orderbyList.size(); i++) {
									OrderbyItem oi = vp.orderbyList.get(i);
									ColumnProp cp = oi.columnProp;
									try {
										BiCellCollection col;
										if (i < rowCount)
											col = sr.getRowCollectionV(i);
										else {
											col = sr.newRowCollection();								
											sr.addSubRecord(col, -1, "");
										}
										col.getCell("grpto_seq").set(++seq);
										col.getCell("grpto_fd").set(cp.viewLabel);
										col.getCell("grpto_desc").set(oi.isDesc);
										col.getCell("grpto_colidx").set(oi.columnIdx);
										UniLog.log1("update orderby header:%s, label:%s, seq:%d, desc:%b, colidx:%d", cp.name, cp.viewLabel, seq, oi.isDesc, oi.columnIdx);
									} catch (Exception ex) {
										throw new Exception(String.format("Update view->orderby %s - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
									}
								}
								for (int i = vp.orderbyList.size(); i < rowCount; i++) {
									Object o = sr.getTrStatObj(i);
									sr.markDelete(o, true);
									UniLog.log1("remove orderby %d", i);
								}
								updatedView = true;
							}
							BiResult brColumn = br.getSubLink("bischema.BiColumn");
							for (BiCellCollection bc : brColumn.getRowCollectionList()) {
								String viewLabel = bc.getString("grptc_label");
								for (ColumnProp cp : cpList) {
									if (StringUtils.equals(cp.viewLabel, viewLabel)) {
										UniLog.log1("update column vpname:%s, cpname:%s, viewlabel:%s, formula:%s, len:%d", vp.name, cp.name, viewLabel, cp.formula, cp.len);
										try {
											bc.getCell("grptc_header").set(cp.name);
											bc.getCell("grptc_fdtype").set(cp.dataType.getFdtype());
											bc.getCell("grptc_inlist").set(cp.isInList);
											bc.getCell("grptc_invisible").set(cp.isInvisible);
											bc.getCell("grptc_formula").set(cp.formula);
											bc.getCell("grptc_format").set(cp.numFormat);
											bc.getCell("grptc_fdlen").set(cp.len);
										} catch (Exception ex) {
											throw new Exception(String.format("Update columns '%s' - ERROR (%s, View:%s)", getColumnNameListString(cpList), getThrowMsg(ex), vp.getApplyName()));
										}
										break;
									}
								}
							}
							rtn = br.updateCurrent();
							if (rtn != null && !rtn.getStatus())
								throw new Exception(String.format("Update %scolumns '%s' - ERROR (%s, View:%s)", updatedView ? "view," : "", getColumnNameListString(cpList), rtn.getMsg(), vp.getApplyName()));
						}
					} else
						throw new Exception(String.format("Update %scolumns '%s' - ERROR (%s, View:%s)", updatedView ? "view," : "", getColumnNameListString(cpList), rtn.getMsg(), vp.getApplyName()));
					br.commitWork();
					resultMsgList.add(String.format("Update columns '%s' - OK (View:%s)", getColumnNameListString(cpList), vp.getApplyName()));
					if (updatedView) {
						resultMsgList.add(String.format("Update view '%s' - OK", vp.getApplyName()));
						updateMenuList.add(vp);
					}
				}
				br = null;
			}

			//update view
			if (!updateViewList.isEmpty()) {
				br = sessionHelper.newBiResult("bischema.BiView");
				for (ViewProp vp : updateViewList) {
					pList.add(vp);
					br.beginWork();
					br.clearCondition();
					ReturnMsg rtn = br.addCustomCondition(String.format("grpth_rg = %d", vp.viewRg));
					if (rtn.getStatus()) {
						if (br.query(true).getStatus() && br.getRowCount() > 0) {
							br.fetchOneRecV(0);
							UniLog.log1("update view vpname:%s", vp.name);
							try {
								br.getCell("grpth_header").set(vp.name);
								br.getCell("grpth_remark").set(vp.name);
							} catch (Exception ex) {
								throw new Exception(String.format("Update view '%s' - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
							}
							BiResult sr = br.getSubLink("bischema.BiOrderby");
							int rowCount = sr.getRowCount();
							int seq = 0;
							for (int i = 0; i < vp.orderbyList.size(); i++) {
								OrderbyItem oi = vp.orderbyList.get(i);
								ColumnProp cp = oi.columnProp;
								try {
									BiCellCollection col;
									if (i < rowCount)
										col = sr.getRowCollectionV(i);
									else {
										col = sr.newRowCollection();								
										sr.addSubRecord(col, -1, "");
									}
									col.getCell("grpto_seq").set(++seq);
									col.getCell("grpto_fd").set(cp.viewLabel);
									col.getCell("grpto_desc").set(oi.isDesc);
									col.getCell("grpto_colidx").set(oi.columnIdx);
									UniLog.log1("update orderby header:%s, label:%s, seq:%d, desc:%b, colidx:%d", cp.name, cp.viewLabel, seq, oi.isDesc, oi.columnIdx);
								} catch (Exception ex) {
									throw new Exception(String.format("Update view->orderby %s - ERROR (%s)", vp.getApplyName(), getThrowMsg(ex)));
								}
							}
							for (int i = vp.orderbyList.size(); i < rowCount; i++) {
								Object o = sr.getTrStatObj(i);
								sr.markDelete(o, true);
								UniLog.log1("remove orderby %d", i);
							}
							rtn = br.updateCurrent();
							if (rtn != null && !rtn.getStatus())
								throw new Exception(String.format("Update view '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
						}
					} else
						throw new Exception(String.format("Update view '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
					br.commitWork();
					resultMsgList.add(String.format("Update view '%s' - OK", vp.getApplyName()));
					updateMenuList.add(vp);
				}
				br = null;
			}

			/*//update menu
			for (ViewProp vp : updateViewList) {
				ReturnMsg rtn = dataModelHelper.updateWebMenu(vp.viewId, vp.name, 0, false);
				if (!rtn.getStatus())
					throw new Exception(String.format("Update menu '%s/%s' - ERROR (%s)", vp.name, vp.viewId, rtn.getMsg()));
				resultMsgList.add(String.format("Update menu '%s/%s' - OK", vp.name, vp.viewId));
			}*/

			//load schema
			if (!addViewList.isEmpty() || !removeViewList.isEmpty() || !addColList.isEmpty() || !removeColList.isEmpty() || !updateViewList.isEmpty() || !updateColList.isEmpty()) {
				loadedSchema = true;
				BiSchema.loadSchema(sessionHelper, true);
			}

			//load data
			for (ViewProp vp : loadDataList) {
				UniLog.log1("Load data viewId:%s, exlpoi:%s, sheetNum:%d, firstCellPos:%s, readSheetRowCount:%d", vp.viewId, vp.exlpoi, vp.sheetNum, vp.firstCellPos, vp.readSheetRowList.size());
				pList.add(vp);
				br = sessionHelper.newBiResult(vp.viewId);
				br.beginWork();
				ExcelPoi exlpoi = vp.exlpoi;
				exlpoi.excel_useSheet(vp.sheetNum);
				for (int row : vp.readSheetRowList) {
					br.clearCurrentRec();
					for (int cpKey : viewColMap.get(vp.key)) {
						ColumnProp cp = columnMap.get(cpKey);
						if (cp.isReadyDel || !cp.needLoadData)
							continue;
						int col = cp.sheetCol;
						Cell cell = exlpoi.getCell(row, col);
						Object v = getCellValue(exlpoi, cell, cp.dataType);
						if (fDebug.get()) UniLog.log1("name:%s, label:%s, dataType:%s, cellType:%s, formula:%s, numFormat:%s, row:%d, col:%d, v:%s", cp.name, cp.viewLabel, cp.dataType, (cell != null ? cell.getCellType() : ""), cp.formula, cp.numFormat, row, col, v);
						if (v == null)
							throw new Exception(String.format("Load data '%s' - ERROR (Invalid cell value, Cell:%s)", vp.getApplyName(), getCellPos(row, col)));
						try {
							br.getCell(cp.viewLabel).set(v);
						} catch (Exception ex) {
							throw new Exception(String.format("Load data '%s' - ERROR (%s, Cell:%s)", vp.getApplyName(), getThrowMsg(ex), getCellPos(row, col)));
						}
					}
					ReturnMsg rtn = br.addCurrent();
					if (!rtn.getStatus())
						throw new Exception(String.format("Load data '%s' - ERROR (%s, SheetRow:%d)", vp.getApplyName(), rtn.getMsg(), row + 1));
				}
				br.commitWork();
				resultMsgList.add(String.format("Load data '%s' - OK", vp.getApplyName()));
				br = null;
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			resultMsgList.add(getThrowMsg(ex));
			if (br != null) {
				UniLog.log1("rollbackWork br");
				br.rollbackWork();
			}
			if (br1 != null) {
				UniLog.log1("rollbackWork br1");
				br1.rollbackWork();
			}
		}

		try {
			if (!loadedSchema && (!addViewList.isEmpty() || !removeViewList.isEmpty() || !addColList.isEmpty() || !removeColList.isEmpty() || !updateViewList.isEmpty() || !updateColList.isEmpty()))
				BiSchema.loadSchema(sessionHelper, true);
		} catch (Exception ex) {
			UniLog.log(ex);
			resultMsgList.add(getThrowMsg(ex));
		}

		//menu
		for (ViewProp vp : addMenuList) {
			ReturnMsg rtn = dataModelHelper.updateWebMenu(vp.viewId, vp.name, 0, false, vp.newAccessRight);
			if (!rtn.getStatus())
				resultMsgList.add(String.format("Add menu '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
			else
				resultMsgList.add(String.format("Add menu '%s' - OK", vp.getApplyName()));
		}
		for (ViewProp vp : removeMenuList) {
			ReturnMsg rtn = dataModelHelper.updateWebMenu(vp.viewId, vp.name, 0, true, null);
			if (!rtn.getStatus())
				resultMsgList.add(String.format("Remove menu '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
			else
				resultMsgList.add(String.format("Remove menu '%s' - OK", vp.getApplyName()));
		}
		for (ViewProp vp : updateMenuList) {
			ReturnMsg rtn = dataModelHelper.updateWebMenu(vp.viewId, vp.name, 0, false, null);
			if (!rtn.getStatus())
				resultMsgList.add(String.format("Update menu '%s' - ERROR (%s)", vp.getApplyName(), rtn.getMsg()));
			else
				resultMsgList.add(String.format("Update menu '%s' - OK", vp.getApplyName()));
		}
		
		//handle abort
		for (ViewProp vp : addViewList) {
			if (!pList.contains(vp))
				resultMsgList.add(String.format("Add view '%s' - ABORTED", vp.getApplyName()));
		}
		for (ViewProp vp : removeViewList) {
			if (!pList.contains(vp))
				resultMsgList.add(String.format("Remove view '%s' - ABORTED", vp.getApplyName()));
		}
		for (ColumnProp cp : addColList) {
			if (!pList.contains(cp))
				resultMsgList.add(String.format("Add column '%s' - ABORTED (View:%s)", cp.getApplyName(), cp.viewProp.getApplyName()));
		}
		for (ColumnProp cp : removeColList) {
			if (!pList.contains(cp))
				resultMsgList.add(String.format("Remove column '%s' - ABORTED (View:%s)", cp.getApplyName(), cp.viewProp.getApplyName()));
		}
		for (ColumnProp cp : updateColList) {
			if (!pList.contains(cp))
				resultMsgList.add(String.format("Update column '%s' - ABORTED (View:%s)", cp.getApplyName(), cp.viewProp.getApplyName()));
		}
		for (ViewProp vp : updateViewList) {
			if (!pList.contains(vp))
				resultMsgList.add(String.format("Update view '%s' - ABORTED", vp.getApplyName()));
		}
		for (ViewProp vp : loadDataList) {
			if (!pList.contains(vp))
				resultMsgList.add(String.format("Load data '%s' - ABORTED", vp.getApplyName()));
		}
		
		return String.join("\n", resultMsgList);
	}

	private static Map<String, Object> getDataType(Cell cell) {
		DataType dataType = DataType.TEXT;
		String formula = null;
		String numFormat = null;
		int numFormatIdx = 0;
		if (cell == null)
			return MapUtil.of("dataType", dataType);
		boolean isCellDateFormatted = false;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			numFormatIdx = cell.getCellStyle().getDataFormat();
			numFormat = cell.getCellStyle().getDataFormatString();
			if (DateUtil.isCellDateFormatted(cell))
				isCellDateFormatted = true;
			else
				dataType = DataType.NUMBER;
			break;
		case Cell.CELL_TYPE_FORMULA:
			formula = cell.getCellFormula();
			switch (cell.getCachedFormulaResultType()) {
			case Cell.CELL_TYPE_NUMERIC:
				numFormatIdx = cell.getCellStyle().getDataFormat();
				numFormat = cell.getCellStyle().getDataFormatString();
				if (DateUtil.isCellDateFormatted(cell))
					isCellDateFormatted = true;
				else
					dataType = DataType.NUMBER;
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				dataType = DataType.TEXT;
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			dataType = DataType.TEXT;
		}
		if (isCellDateFormatted) {
			Date date = cell.getDateCellValue();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(new Date(date.getTime() - date.getTime() % 1000));
			int hour = cal.get(Calendar.HOUR);
			int min = cal.get(Calendar.MINUTE);
			int sec = cal.get(Calendar.SECOND);
			if (hour > 0 || min > 0 || sec > 0 || numFormat.toLowerCase().contains("h"))
				dataType = DataType.DATETIME;
			else
				dataType = DataType.DATE;
		}
		return MapUtil.of("dataType", dataType, "formula", formula, "numFormat", numFormat, "numFormatIdx", numFormatIdx);
	}
	
	private static Object getCellValue(ExcelPoi exlpoi, Cell cell, DataType colDataType) {
		Map<String, Object> map = getDataType(cell);
		DataType cellDataType = (DataType)map.get("dataType");
		String cellFormula = (String)map.get("formula");
		String cellNumFormat = (String)map.get("numFormat");
		Integer cellNumFormatIdx = (Integer)map.get("numFormatIdx");
		final Date zeroDate = com.kyoko.common.DateUtil.zeroDate;
		switch (colDataType) {
		case DATE:
		case DATETIME:
			if (cell == null)
				return zeroDate;
			switch (cellDataType) {
			case DATE:
			case DATETIME:
				return ObjectUtils.defaultIfNull(cell.getDateCellValue(), zeroDate);
			case NUMBER:
			default:
				return zeroDate;
			}
		case NUMBER:
			if (cell == null)
				return 0.0;
			switch (cellDataType) {
			case DATE:
			case DATETIME:
				return 0.0;
			case NUMBER:
				return cell.getNumericCellValue();
			default:
				return 0.0;
			}
		default:
			if (cell == null)
				return "";
			switch (cellDataType) {
			case DATE:
				return com.kyoko.common.DateUtil.dateToDateTimeStr((Date)ObjectUtils.defaultIfNull(cell.getDateCellValue(), zeroDate), "yyyy/MM/dd");
			case DATETIME:
				return com.kyoko.common.DateUtil.dateToDateTimeStr((Date)ObjectUtils.defaultIfNull(cell.getDateCellValue(), zeroDate), "yyyy/MM/dd HH:mm:ss");
			case NUMBER:
				return StringUtils.defaultString(new DataFormatter().formatRawCellContents(cell.getNumericCellValue(), cellNumFormatIdx, cellNumFormat));
			default:
				String s = StringUtils.defaultString(exlpoi.getStringValue(cell.getRowIndex(), cell.getColumnIndex()));
				if (StringUtils.isNotBlank(s) && StringUtils.equals(s, cellFormula))
					return "";
				return s;
			}
		}
	}

	private static String getCellStringValue(ExcelPoi exlpoi, Cell cell, DataType colDataType) {
		Map<String, Object> map = getDataType(cell);
		DataType cellDataType = (DataType)map.get("dataType");
		String cellNumFormat = (String)map.get("numFormat");
		Integer cellNumFormatIdx = (Integer)map.get("numFormatIdx");
		Object v = getCellValue(exlpoi, cell, colDataType);
		switch (colDataType) {
		case DATE:
			return com.kyoko.common.DateUtil.dateToDateTimeStr((Date)v, "yyyy/MM/dd");
		case DATETIME:
			return com.kyoko.common.DateUtil.dateToDateTimeStr((Date)v, "yyyy/MM/dd HH:mm:ss");
		case NUMBER:
			if (cellDataType == colDataType) {
				String s = StringUtils.defaultString(new DataFormatter().formatRawCellContents((Double)v, cellNumFormatIdx, cellNumFormat));
				//UniLog.log1("cell:%s%d, v:%s, s:%s, formatIdx:%d, format:%s", CellReference.convertNumToColString(cell.getColumnIndex()), cell.getRowIndex(), v, s, cellNumFormatIdx, cellNumFormat);
				return s;
			}
		default:
			return v.toString();
		}
	}

	private static String getThrowMsg(Throwable ex) {
		return StringUtils.defaultIfBlank(ex.getMessage(), ex.toString());
	}
	
	private List<ViewProp> getSortedViewList() {
		List<ViewProp> sortList = new ArrayList<ViewProp>();
		for (ViewProp vp1 : viewMap.values()) {
			if (vp1.viewComp != null)
				sortList.add(vp1);
		}
		sortList.sort(new Comparator<ViewProp>() {
			@Override
			public int compare(ViewProp vp1, ViewProp vp2) {
				return StringUtils.compareIgnoreCase(vp1.name, vp2.name);
			}
		});
		return sortList;
	}

	private List<NodeProp> getSortedNodeList() {
		List<NodeProp> sortList = new ArrayList<NodeProp>(nodeMap.values());
		sortList.sort(new Comparator<NodeProp>() {
			@Override
			public int compare(NodeProp np1, NodeProp np2) {
				return StringUtils.compareIgnoreCase(np1.nodeId, np2.nodeId);
			}
		});
		return sortList;
	}
	
	private List<EdgeProp> getSortedEdgeList() {
		List<EdgeProp> sortList = new ArrayList<EdgeProp>(edgeMap.values());
		sortList.sort(new Comparator<EdgeProp>() {
			@Override
			public int compare(EdgeProp ep1, EdgeProp ep2) {
				return StringUtils.compareIgnoreCase(ep1.nodeIdA, ep2.nodeIdA);
			}
		});
		return sortList;
	}
	
	private static String getCellPos(int row, int col) {
		return String.format("%s%d", CellReference.convertNumToColString(col), row + 1);
	}

	private static String getCellPos(Cell cell) {
		return getCellPos(cell.getRowIndex(), cell.getColumnIndex());
	}
	
	private String getColumnNameListString(List<ColumnProp> list) {
		List<String> sl = new ArrayList<String>();
		for (ColumnProp cp : list)
			sl.add(String.format("%s/%s", cp.name, cp.viewLabel));
		return String.join(",", sl);
	}
	private String getCellFormulaStr(Cell cell) {
		String formulaStr = "";
		try {
			formulaStr = cell.getCellFormula();
			//handle _xlfn.IFNA
			if (StringUtils.startsWith(formulaStr,"_xlfn.")){
				formulaStr = StringUtils.remove(formulaStr, "_xlfn.");
				UniLog.log1("remove prefix. formulaStr:" + formulaStr);
			}
			return formulaStr;
		}
		catch(Exception ex) {
			UniLog.log1("error:"+ ex.getMessage());
			return "";
		}
		
	}
	
}