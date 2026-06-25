package com.uniinformation.jxapp.bischema;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Space;
import org.zkoss.zul.Splitter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;

public class BiView extends JxZkBiBase {
	private Listbox lbBiColumns, lbColumns;
	private Div divColumnProps;
	private Map<String, ColumnVar> columnVarMap = new HashMap<String, ColumnVar>() {
		void put(String varName, String displayName, String cellName, int cellOffset) {
			ColumnVar cv = new ColumnVar();
			cv.varName = varName;
			cv.displayName = displayName;
			cv.cellName = cellName;
			cv.cellOffset = cellOffset;
			put(cv.varName, cv);
		} 
		void put(String varName, String displayName, String cellName) {
			put(varName, displayName, cellName, -1);
		}
		{
			put("masterTable", "Parent Table", "grptc_mastertab");
			put("subTable", "Table", "grptc_subtable");
			put("fd", "Col Name", "grptc_fd");
			put("label", "Cell Name", "grptc_label");
			put("header", "Header", "grptc_header");
			put("fdtype", "Type", "grptc_fdtype");
			put("formula", "Formula", "grptc_formula");
			put("fdformat", "Format", "grptc_format");
			put("rgno", "RG", "grptc_rgno");
			put("fdlen", "Length", "grptc_fdlen");
			put("fdOptionList", "Option List", "grptc_optionlist");
			put("validation", "Validation", "grptc_validation");
			put("accesskey", "Access Key", "grptc_accesskey");
			put("defaultValue", "Default Value", "grptc_default");
			put("aggregate", "Aggregate", "grptc_aggregate");
			put("pickView", "Pick View", "grptc_pickview");
			put("fgColor", "Foreground", "grptc_fgcolor");
			put("bgColor", "Background", "grptc_bgcolor");
			put("inList", "In List", "grptc_inlist");
			put("inSelect", "In Select", "grptc_inselect");
			put("noQuery", "No Query", "grptc_noquery");
			put("invisible", "Invisible", "grptc_invisible");
			put("noUpdate", "No Update", "grptc_attribute", 0);
			put("noEntry", "No Entry", "grptc_attribute", 1);
			put("upShift", "Up Shift", "grptc_attribute", 2);
			put("noSpace", "No Space", "grptc_attribute", 3);
			put("required", "Required", "grptc_attribute", 4);
			put("storedFunction", "Stored Func", "grptc_attribute", 5);
			put("flexWidth", "Flex Width", "grptc_attribute", 6);
			put("excludeForMobile", "No Mobile", "grptc_attribute", 7);
			put("lookup", "Lookup", "grptc_attribute", 8);
			put("protect", "Protect", "grptc_attribute", 9);
			put("sequence", "Sequence", "grptc_attribute", 10);
			put("allowBatchUpdate", "Batch Update", "grptc_attribute", 11);
			put("readOnly", "Readonly", "grptc_attribute", 12);
			put("inPickList", "In Pick List", "grptc_attribute", 13);
			put("formulaOnLoad", "On Load", "grptc_attribute", 14);
			put("formulaOnSave", "On Save", "grptc_attribute", 15);
			put("skipImport", "Skip Import", "grptc_attribute", 16);
			put("skipExport", "Skip Export", "grptc_attribute", 17);
			put("isChinese", "Chinese", "grptc_attribute", 18);
			put("isPivot", "Pivot", "grptc_attribute", 19);
			put("isAggregate", "Aggregate", "grptc_attribute", 20);
			put("noPaste", "No Paste", "grptc_attribute", 21);
			put("isUTF8", "UTF8", "grptc_attribute", 22);
		}
	};
	private Map<String, List<String>> biTableMap;
	
	private static class BiColumnEx {
		String masterTable, subTable, fd;
		boolean sequence;
	}
	
	private static class ColumnVar {
		String varName;
		String displayName;
		String cellName;
		int cellOffset;
		Component component;
	}

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
	}

	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		lbBiColumns = (Listbox)jxAdd("list_bischema_BiColumn").getNativeObject();
		Button btAddBiColumn = (Button)jxAdd("btadd_list_bischema_BiColumn").getNativeObject();
		Hbox hbColumnsContainer = (Hbox)jxAdd("hbColumnsContainer").getNativeObject();
		lbColumns = (Listbox)hbColumnsContainer.query(".lbColumns");
		divColumnProps = (Div)hbColumnsContainer.query(".divColumnProps");
		Splitter sl = (Splitter)hbColumnsContainer.query(".sl");
		
		lbBiColumns.setMold("default");

		lbColumns.getChildren().clear();
		lbColumns.appendChild(new Listhead() {{
			setSizable(true);
			appendChild(new Listheader() {{
				setWidth("60px");
				setAlign("center");
				appendChild(new Toolbarbutton() {{
					setSclass(btAddBiColumn.getSclass());
					setImage(btAddBiColumn.getImage());
					setIconSclass(btAddBiColumn.getIconSclass());
					setTooltiptext(btAddBiColumn.getTooltiptext());
					setStyle(btAddBiColumn.getStyle());
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							Events.echoEvent(Events.ON_CLICK, btAddBiColumn, null);
							ZkUtil.echoEvent(lbColumns, "onRefreshColumnItems", null, (Event) -> refreshColumnItems(true));
						}
					});
				}});
			}});
			appendChild(new Listheader() {{
				setWidth("45px");
				setLabel("Seq");
			}});
			appendChild(new Listheader() {{
				setLabel("Column");
			}});
			appendChild(new Listheader() {{
				setLabel("Header");
			}});
		}});
		ZkUtil.removeAllEventListener(lbColumns, Events.ON_SELECT);
		lbColumns.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
			@Override
			public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
				UniLog.log1("lbColumns ON_SELECT li:%s", event.getReference());
				showColumnProps(event.getReference());
			}
		});

		ZkUtil.removeAllEventListener(sl, Events.ON_OPEN);
		sl.addEventListener(Events.ON_OPEN, (OpenEvent event) -> {
			UniLog.log1("event:%s", event);
			if (event.isOpen()) {
				Integer hbWidth = (Integer)hbColumnsContainer.getAttribute("activeWidth");
				Integer lbWidth = (Integer)lbColumns.getAttribute("activeWidth");
				Integer ssWidth = (Integer)lbColumns.getAttribute("ssWidth");
				if (hbWidth != null && lbWidth != null && ssWidth != null) {
					int tmpWidth = hbWidth - lbWidth - ssWidth;
					UniLog.log1("hbWidth:%d, lbWidth:%d, ssWidth:%d, tmpWidth:%d", hbWidth, lbWidth, ssWidth, tmpWidth);
					lbColumns.setHflex("" + lbWidth);
					divColumnProps.setHflex("" + (tmpWidth >= 0 ? tmpWidth : 0));
					hbColumnsContainer.invalidate();
				}
			}
		});
		final Component[] comps = new Component[] {hbColumnsContainer, lbColumns, divColumnProps};
		for (final Component comp : comps) {
			ZkUtil.removeAllEventListener(comp, Events.ON_AFTER_SIZE);
			comp.addEventListener(Events.ON_AFTER_SIZE, (AfterSizeEvent event) -> {
				UniLog.log1("%s ON_AFTER_SIZE width:%s", comp, event.getWidth());
				comp.setAttribute("activeWidth", event.getWidth());
				Integer hbWidth = (Integer)hbColumnsContainer.getAttribute("activeWidth");
				Integer lbWidth = (Integer)lbColumns.getAttribute("activeWidth");
				Integer diWidth = (Integer)divColumnProps.getAttribute("activeWidth");
				if (hbWidth != null && lbWidth != null && diWidth != null && sl.isOpen()) {
					int o = hbWidth - lbWidth - diWidth;
					UniLog.log1("o:%d, hbWidth:%d, lbWidth:%d, diWidth:%d", o, hbWidth, lbWidth, diWidth);
					if (o > 0)
						lbColumns.setAttribute("ssWidth", o);
				}
			});
		}
		
		ZkBiAbstractLongOp.newInstance(curComp, "Loading", 100, () -> {
			Integer cWidth = (Integer)hbColumnsContainer.getAttribute("activeWidth");
			UniLog.log1("hbColumnsContainer activeWidth:%d", cWidth);
			if (cWidth != null && cWidth > 340) {
				lbColumns.setHflex("330");
				divColumnProps.setHflex(String.valueOf(cWidth - 340));
			}
			lbBiColumns.renderAll();
			refreshColumnItems(false);
			return null;
		});
	}
	
	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			Vector<BiCellCollection> recs = br.getSubLinkResult("bischema.BiColumn");
			List<String> colLabelList = new ArrayList<String>();
			for (BiCellCollection cc : recs) {
				String s = cc.getCellString("grptc_label").trim();
				if (StringUtils.isBlank(s))
					return new ReturnMsg(false, "Column label cannot be empty", true);
				if (colLabelList.contains(s))
					return new ReturnMsg(false, "Column label duplicated", true);
				colLabelList.add(s);
			}
			recs = br.getSubLinkResult("bischema.BiOrderby");
			List<String> obFdList = new ArrayList<String>();
			for (BiCellCollection cc : recs) {
				String s = cc.getCellString("grpto_fd").trim();
				if (StringUtils.isBlank(s))
					return new ReturnMsg(false, "Orderby column cannot be empty", true);
				if (!colLabelList.contains(s))
					return new ReturnMsg(false, "Orderby column not found", true);
				if (obFdList.contains(s))
					return new ReturnMsg(false, "Orderby column duplicated", true);
				cc.getCell("grpto_colidx").set(colLabelList.indexOf(s) + 1);
				obFdList.add(s);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd, BiResult sr, CellCollection cl, int p_insIdx) {
		ReturnMsg rtn = super.beforeAddLink(fd, sr, cl, p_insIdx);
		UniLog.log1("beforeAddLink p_insIdx:%d, fdName:%s", p_insIdx, fd != null ? fd.getName() : "");
		if (rtn != null && !rtn.getStatus()) return rtn;
		try {
			if (fd != null) {
				if (StringUtils.equalsAny(fd.getName(), "list_bischema_BiColumn", "btadd_list_bischema_BiColumn"))
					setCellSeq(sr, cl, "grptc_seq", p_insIdx);
				else if (StringUtils.equalsAny(fd.getName(), "list_bischema_BiOrderby", "btadd_list_bischema_BiOrderby"))
					setCellSeq(sr, cl, "grpto_seq", p_insIdx);
			}
		} catch (Exception ex) {
			rtn = new ReturnMsg(false, -1, ex.getMessage());
		}
		return rtn;
	}

	@Override
	protected void afterUnDeleteLink(BiResult sr, int idx) {
		super.afterUnDeleteLink(sr, idx);
		UniLog.log1("afterUnDeleteLink idx:%d", idx);
		try {
			BiCellCollection cl = sr.getRowCollectionV(idx);
			if (StringUtils.equals(sr.getView().getName(), "bischema.BiColumn"))
				setCellSeq(sr, cl, "grptc_seq", idx);
			else if (StringUtils.equals(sr.getView().getName(), "bischema.BiOrderby"))
				setCellSeq(sr, cl, "grpto_seq", idx);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	
	private void setCellSeq(BiResult sr, CellCollection cl, String cellName, int idx) throws CellException {
		if (cl.getCellInt(cellName) == 0) {
			int i;
			for (i = idx - 1; i >= 0; i--) {
				Object ts = sr.getTrStatObj(i);
				if (ts == null || !sr.isMarkedDelete(ts)) {
					BiCellCollection c = sr.getRowCollectionV(i);
					cl.getCell(cellName).set(c.getInt(cellName) + 1);
					break;
				}
			}
			if (i < 0)
				cl.getCell(cellName).set(1);
		}
	}

	private Set<String> getBiTableList() {
		if (biTableMap == null) {
			biTableMap = new LinkedHashMap<String, List<String>>();
			BiResult br = BiResultHelper.create(sessionHelper, "bischema.BiTable", null, -1, null);
			while (br.next())
				biTableMap.put(br.getCellString("ddt_tabname"), null);
		}
		return biTableMap.keySet();
	}
	
	private List<String> getBiFieldList(String tabName) {
		if (biTableMap == null)
			getBiTableList();
		if (tabName == null || !biTableMap.containsKey(tabName))
			return new ArrayList<String>();
		List<String> biFieldList = biTableMap.get(tabName);
		if (biFieldList != null)
			return biFieldList;
		biFieldList = new ArrayList<String>();
		biTableMap.put(tabName, biFieldList);
		BiResult br = BiResultHelper.create(sessionHelper, "bischema.BiTable", String.format("ddt_tabname = '%s'", tabName), -1, null);
		if (br.next(false)) {
			BiResult br1 = br.getSubLink("bischema.BiField");
			while (br1.next(false))
				biFieldList.add(br1.getCellString("ddf_fdname"));
		}
		return biFieldList;
	}
	
	//Show Column Item
	private void showColumnItem(Listitem li, Listitem liSrc, CellCollection cc, boolean isMarkDelete) {
		Button srcAddButton = null, srcDelButton = null;
		if (liSrc.getFirstChild() instanceof Listcell) {
			for (Component c : liSrc.getFirstChild().getChildren()) {
				if (c instanceof Button) {
					if (c.hasAttribute("isAddButton"))
						srcAddButton = (Button)c;
					else if (c.hasAttribute("JxZkListbox.deleteItemButton"))
						srcDelButton = (Button)c;
				}
			}
		}
		Button srcAddButton1 = srcAddButton, srcDelButton1 = srcDelButton;
		li.setAttribute("cellCollection", cc);
		li.setAttribute("listitem", liSrc);
		li.getChildren().clear();
		li.appendChild(new Listcell() {{
			if (srcAddButton1 != null) {
				appendChild(new Toolbarbutton() {{
					setSclass(srcAddButton1.getSclass());
					setIconSclass(srcAddButton1.getIconSclass());
					setTooltiptext(srcAddButton1.getTooltiptext());
					setStyle(srcAddButton1.getStyle());
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							Events.echoEvent(Events.ON_CLICK, srcAddButton1, null);
							ZkUtil.echoEvent(li, "onRefreshColumnItems", null, (Event) -> refreshColumnItems(true));
						}
					});
				}});
			}
			if (srcDelButton1 != null) {
				appendChild(new Toolbarbutton() {{
					setSclass(srcDelButton1.getSclass());
					setIconSclass(srcDelButton1.getIconSclass());
					setTooltiptext(srcDelButton1.getTooltiptext());
					setStyle(srcDelButton1.getStyle());
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							Events.echoEvent(Events.ON_CLICK, srcDelButton1, null);
							ZkUtil.echoEvent(li, "onRefreshColumnItems", null, (Event) -> refreshColumnItems(false));
						}
					});
				}});
			}
			if (isMarkDelete)
				ZkUtil.appendStyle(this, "background:pink !important;");
		}});
		li.appendChild(new Listcell() {{
			appendChild(new Intbox() {{
				setWidth("40px");
				setValue(cc.getInt("grptc_seq"));
				li.setAttribute("itemSeqComp", this);
				addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
					@Override
					public void onZkBiEvent(InputEvent event) throws Exception {
						try {
							cc.getCell("grptc_seq").set(NumberUtils.toInt(event.getValue()));
						} catch (Exception ex) {
	          				showExceptionMessage(ex);
						}
						setDirtyFlag(true);
						ZkUtil.echoEvent(li, "onRefreshColumnItems", null, (Event) -> refreshColumnItems(false));
					}
				});
			}});
			if (isMarkDelete)
				ZkUtil.appendStyle(this, "background:pink !important;");
		}});
		li.appendChild(new Listcell() {{
			setLabel(cc.getCellString("grptc_label"));
			li.setAttribute("itemNameComp", this);
			if (isMarkDelete)
				ZkUtil.appendStyle(this, "background:pink !important;");
		}});
		li.appendChild(new Listcell() {{
			setLabel(cc.getCellString("grptc_header"));
			li.setAttribute("itemHeaderComp", this);
			if (isMarkDelete)
				ZkUtil.appendStyle(this, "background:pink !important;");
		}});
		li.setAttribute("isMarkDelete", isMarkDelete);
	}
	
	private void showColumnProps(Listitem li) {
		if (li == null) {
			divColumnProps.addSclass("hidden");
			return;
		}
		divColumnProps.removeSclass("hidden");
		if (divColumnProps.getChildren().size() == 0) {
			Field[] fieldArray = BiColumn.class.getDeclaredFields();
			fieldArray = (Field[]) ArrayUtils.addAll(fieldArray, BiColumnEx.class.getDeclaredFields());
			List<Component> compList = new ArrayList<Component>();
			//sortTypeNumA: 0: Boolean, 1: Select, 1: Integer, 2: String, 3: String[]
			//sortTypeNumB: 0: Boolean, 1: Select, 2: Integer, 3: String, 4: String[]
			for (final Field f : fieldArray) {
				try {
					f.setAccessible(true);
					final String fieldName = f.getName();
					final String fieldType = f.getType().getName();
					ColumnVar colVar = columnVarMap.get(fieldName);
					if (colVar != null) {
						UniLog.log1("field name:%s, type:%s", fieldName, fieldType);
						if (fieldType.equals("java.lang.String")) {
							if (fieldName.equals("fdtype")) {
								final Combobox cb = new Combobox();
								cb.appendItem("");
								cb.appendItem("float");
								cb.appendItem("integer");
								cb.appendItem("char");
								cb.appendItem("date");
								cb.appendItem("list");
								cb.appendItem("checkbox");
								cb.appendItem("button");
								cb.appendItem("pickinput");
								cb.appendItem("datetime");
								cb.appendItem("time");
								cb.appendItem("label");
								cb.appendItem("radio");
								cb.appendItem("colorbox");
								cb.appendItem("memo");
								cb.appendItem("div");
								cb.appendItem("combobox");
								cb.setSelectedIndex(0);
								cb.setReadonly(true);
								cb.setWidth("100px");
								compList.add(new Hlayout() {{
									colVar.component = cb;
									setAttribute("columnVar", colVar);
									setAttribute("sortTypeNumA", 1);
									setAttribute("sortTypeNumB", 1);
									appendChild(new Label(colVar.displayName) {{
										setStyle("display:inline-block;min-width:50px");
									}});
									appendChild(cb);
									ZkUtil.appendStyle(this, "margin:5px 0");
								}});
							} else if (fieldName.equals("aggregate")) {
								final Combobox cb = new Combobox();
								cb.appendItem("");
								cb.appendItem("SUM");
								cb.appendItem("FIRST");
								cb.appendItem("LAST");
								cb.appendItem("STRCAT");
								cb.appendItem("UNIQUECAT");
								cb.appendItem("EXPRESSION");
								cb.appendItem("EXPRESSION2");
								cb.setSelectedIndex(0);
								cb.setReadonly(true);
								cb.setWidth("135px");
								compList.add(new Hlayout() {{
									colVar.component = cb;
									setAttribute("columnVar", colVar);
									setAttribute("sortTypeNumA", 1);
									setAttribute("sortTypeNumB", 1);
									appendChild(new Label(colVar.displayName) {{
										setStyle("display:inline-block;min-width:50px");
									}});
									appendChild(cb);
									ZkUtil.appendStyle(this, "margin:5px 0");
								}});
							} else if (StringUtils.equalsAny(fieldName, "masterTable", "subTable"/*, "pickView"*/)) {
								final Listbox lb = new Listbox();
								lb.setMold("select");
								lb.setWidth("150px");
								lb.appendItem("", "");
								for (String s : getBiTableList())
									lb.appendItem(s, s);
								lb.setSelectedIndex(0);
								compList.add(new Hlayout() {{
									colVar.component = lb;
									setAttribute("columnVar", colVar);
									setAttribute("sortTypeNumA", 1);
									setAttribute("sortTypeNumB", 1);
									appendChild(new Label(colVar.displayName) {{
										setStyle("display:inline-block;min-width:50px");
									}});
									appendChild(lb);
									ZkUtil.appendStyle(this, "margin:5px 0");
								}});
								lb.setAttribute("select2-enable", "Y");
								ZkUtil.setupSelect2(lb, true, false);
							} else if (fieldName.equals("fd")) {
								final Listbox lb = new Listbox();
								lb.setMold("select");
								lb.setWidth("150px");
								lb.appendItem("", "");
								lb.setSelectedIndex(0);
								compList.add(new Hlayout() {{
									colVar.component = lb;
									setAttribute("columnVar", colVar);
									setAttribute("sortTypeNumA", 1);
									setAttribute("sortTypeNumB", 1);
									appendChild(new Label(colVar.displayName) {{
										setStyle("display:inline-block;min-width:50px");
									}});
									appendChild(lb);
									ZkUtil.appendStyle(this, "margin:5px 0");
								}});
								lb.setAttribute("select2-enable", "Y");
								ZkUtil.setupSelect2(lb, true, false);
							} else {
								final Textbox tb = new Textbox();
								tb.setHflex("1");
								compList.add(new Hlayout() {{
									colVar.component = tb;
									setAttribute("columnVar", colVar);
									setAttribute("sortTypeNumA", 2);
									setAttribute("sortTypeNumB", fieldName.equals("formula") ? 4 : 3);
									appendChild(new Label(colVar.displayName) {{
										setStyle("display:inline-block;min-width:90px");
									}});
									appendChild(tb);
									setWidth(sessionHelper.isMobile() || fieldName.equals("formula") ? "calc(100% - 10px)" : "calc(50% - 10px)");
									ZkUtil.appendStyle(this, "margin:5px 0");
								}});
							}
						} else if (fieldType.equals("int")) {
							final Intbox ib = new Intbox();
							ib.setWidth("100px");
							compList.add(new Hlayout() {{
								colVar.component = ib;
								setAttribute("columnVar", colVar);
								setAttribute("sortTypeNumA", 1);
								setAttribute("sortTypeNumB", 2);
								appendChild(new Label(colVar.displayName) {{
									setStyle("display:inline-block;min-width:50px");
								}});
								appendChild(ib);
								ZkUtil.appendStyle(this, "margin:5px 0");
							}});
						} else if (fieldType.equals("boolean")) {
							final Checkbox cb = new Checkbox(colVar.displayName);
							ZkUtil.appendStyle(cb, "margin-top:8px");
							colVar.component = cb;
							cb.setAttribute("columnVar", colVar);
							cb.setAttribute("sortTypeNumA", 0);
							cb.setAttribute("sortTypeNumB", 0);
							compList.add(cb);
						} else if (fieldType.equals("[Ljava.lang.String;")) {
							final Textbox tb = new Textbox();
							tb.setHflex("1");
							compList.add(new Hlayout() {{
								colVar.component = tb;
								setAttribute("columnVar", colVar);
								setAttribute("sortTypeNumA", 3);
								setAttribute("sortTypeNumB", 4);
								appendChild(new Label(colVar.displayName) {{
									setStyle("display:inline-block;min-width:90px");
								}});
								appendChild(tb);
								setWidth("calc(100% - 10px)");
								ZkUtil.appendStyle(this, "margin:5px 0");
							}});
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Collections.sort(compList, new Comparator<Component>() {
				@Override
				public int compare(Component c1, Component c2) {
					int sortTypeNumA1 = (Integer) c1.getAttribute("sortTypeNumA");
					int sortTypeNumA2 = (Integer) c2.getAttribute("sortTypeNumA");
					int sortTypeNumB1 = (Integer) c1.getAttribute("sortTypeNumB");
					int sortTypeNumB2 = (Integer) c2.getAttribute("sortTypeNumB");
					String displayName1 = ((ColumnVar) c1.getAttribute("columnVar")).displayName;
					String displayName2 = ((ColumnVar) c2.getAttribute("columnVar")).displayName;
					if (sortTypeNumA1 != sortTypeNumA2)
						return sortTypeNumA1 - sortTypeNumA2;
					else if (sortTypeNumB1 != sortTypeNumB2)
						return sortTypeNumB1 - sortTypeNumB2;
					else
						return displayName1.compareTo(displayName2);
				}
			});

			for (int i = 0; i < compList.size(); i++) {
				Component comp = compList.get(i);
				final int sortTypeNumA = (Integer) comp.getAttribute("sortTypeNumA");
				if (i > 0) {
					final int lastSortTypeNumA = (Integer) compList.get(i - 1).getAttribute("sortTypeNumA");
					divColumnProps.appendChild(new Space() {{
						if (sortTypeNumA != lastSortTypeNumA) {
							setWidth("100%");
							setHeight("0px");
							setOrient("horizontal");
						}
					}});
				}
				divColumnProps.appendChild(comp);
			}
		}
		
		CellCollection cc = (CellCollection)li.getAttribute("cellCollection");
		boolean isMarkDelete = (boolean) li.getAttribute("isMarkDelete");
		ZkBiAbstractLongOp.Callback opcb = () -> {
			for (Component comp : divColumnProps.getChildren()) {
				ColumnVar colVar = (ColumnVar) comp.getAttribute("columnVar");
				if (colVar == null)
					continue;
				String varName = colVar.varName;
				Component c = colVar.component;
				if (c instanceof Combobox) {
					Combobox cb = (Combobox)c;
					cb.setValue(cc.getCellString(colVar.cellName));
					cb.setDisabled(isMarkDelete);
					ZkUtil.removeAllEventListener(cb, Events.ON_SELECT);
					cb.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Comboitem, String>>() {
						@Override
						public void onZkBiEvent(SelectEvent<Comboitem, String> event) throws Exception {
							Comboitem ci = event.getReference();
							if (ci == null)
								return;
							UniLog.log1("cb ON_SELECT ci:%s", ci);
							cc.getCell(colVar.cellName).set(ci.getLabel());
							setDirtyFlag(true);
						}
					});
				} else if (c instanceof Listbox) {
					Listbox lb = (Listbox)c;
					if (varName.equals("fd")) {
						lb.setSelectedIndex(-1);
						lb.getItems().clear();
						lb.appendItem("", "");
						lb.setSelectedIndex(0);
						ColumnVar cvTable = columnVarMap.get("subTable");
						List<String> biFieldList = getBiFieldList(cc.getCellString(cvTable.cellName));
						if (biFieldList != null) {
							for (String s : biFieldList)
								lb.appendItem(s, s);
						}
					}
					String s = cc.getCellString(colVar.cellName);
					lb.setSelectedIndex(0);
					for (Listitem lii : lb.getItems()) {
						if (lii.getLabel().equals(s)) {
							lb.setSelectedItem(lii);
							break;
						}
					}
					if (StringUtils.isNotBlank(s) && lb.getSelectedIndex() == 0)
						lb.setSelectedItem(lb.appendItem(s, s));
					
					lb.setDisabled(isMarkDelete);
					ZkUtil.removeAllEventListener(lb, Events.ON_SELECT);
					lb.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
						@Override
						public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
							Listitem lii = event.getReference();
							String label = lii.getLabel();
							UniLog.log1("lb ON_SELECT lii:%s, label:%s", lii, label);
							cc.getCell(colVar.cellName).set(label);
							setDirtyFlag(true);
							if (varName.equals("subTable")) {
								ColumnVar cvFd = columnVarMap.get("fd");
								cc.getCell(cvFd.cellName).set("");
								Listbox lbFd = (Listbox)cvFd.component;
								lbFd.setSelectedIndex(-1);
								lbFd.getItems().clear();
								lbFd.appendItem("", "");
								lbFd.setSelectedIndex(0);
								ZkBiAbstractLongOp.Callback cb = () -> {
									List<String> biFieldList = getBiFieldList(label);
									if (biFieldList != null) {
										for (String s : biFieldList)
											lbFd.appendItem(s, s);
									}
									resetupS2Comp(lbFd);
									return null;
								};
								if (biTableMap.get(label) != null)
									cb.action();
								else
									ZkBiAbstractLongOp.newInstance(curComp, "Loading", 100, cb);
							} else if (varName.equals("fd")) {
								ColumnVar cvLabel = columnVarMap.get("label");
								ColumnVar cvHeader = columnVarMap.get("header");
								if (StringUtils.isBlank(cc.getCellString(cvLabel.cellName))) {
									cc.getCell(cvLabel.cellName).set(label);
									((Textbox)cvLabel.component).setValue(label);
									((Listcell)li.getAttribute("itemNameComp")).setLabel(label);
								}
								if (StringUtils.isBlank(cc.getCellString(cvHeader.cellName))) {
									cc.getCell(cvHeader.cellName).set(label);
									((Textbox)cvHeader.component).setValue(label);
									((Listcell)li.getAttribute("itemHeaderComp")).setLabel(label);
								}
							}
						}
					});
					resetupS2Comp(lb);
				} else if (c instanceof Textbox) {
					Textbox tb = (Textbox)c;
					tb.setValue(cc.getCellString(colVar.cellName));
					tb.setDisabled(isMarkDelete);
					ZkUtil.removeAllEventListener(tb, Events.ON_CHANGE);
					tb.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
						@Override
						public void onZkBiEvent(InputEvent event) throws Exception {
							UniLog.log1("tb ON_CHANGE value:%s", event.getValue());
							try {
								cc.getCell(colVar.cellName).set(event.getValue());
							} catch (Exception ex) {
	          					showExceptionMessage(ex);
	          					tb.setValue(cc.getCellString(colVar.cellName));
							}
							setDirtyFlag(true);
							if (varName.equals("label"))
								((Listcell)li.getAttribute("itemNameComp")).setLabel(tb.getValue());
							else if (varName.equals("header"))
								((Listcell)li.getAttribute("itemHeaderComp")).setLabel(tb.getValue());
						}
					});
				} else if (c instanceof Intbox) {
					Intbox ib = (Intbox)c;
					ib.setValue(cc.getCellInt(colVar.cellName));
					ib.setDisabled(isMarkDelete);
					ZkUtil.removeAllEventListener(ib, Events.ON_CHANGE);
					ib.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
						@Override
						public void onZkBiEvent(InputEvent event) throws Exception {
							UniLog.log1("ib ON_CHANGE value:%s", event.getValue());
							try {
								cc.getCell(colVar.cellName).set(NumberUtils.toInt(event.getValue()));
							} catch (Exception ex) {
	          					showExceptionMessage(ex);
	          					ib.setValue(cc.getCellInt(colVar.cellName));
							}
							setDirtyFlag(true);
						}
					});
				} else if (c instanceof Checkbox) {
					Checkbox cb = (Checkbox)c;
					if (colVar.cellOffset >= 0) {
						String ss = cc.getCellString(colVar.cellName);
						boolean b = StringUtils.substring(ss, colVar.cellOffset, colVar.cellOffset + 1).equals("Y");
						cb.setChecked(b);
					} else
						cb.setChecked(cc.getCellBoolean(colVar.cellName));
					cb.setDisabled(isMarkDelete);
					ZkUtil.removeAllEventListener(cb, Events.ON_CHECK);
					cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
						@Override
						public void onZkBiEvent(CheckEvent event) throws Exception {
							UniLog.log1("cb ON_CHECK value:%s", event.isChecked());
							if (colVar.cellOffset >= 0) {
								String ss = cc.getCellString(colVar.cellName);
								ss = StringUtils.rightPad(ss, colVar.cellOffset + 1);
								StringBuilder sb = new StringBuilder(ss);
								sb.setCharAt(colVar.cellOffset, event.isChecked() ? 'Y' : 'N');
								cc.getCell(colVar.cellName).set(sb.toString().replaceAll("\\s+$", ""));
							} else
								cc.getCell(colVar.cellName).set(event.isChecked());
							setDirtyFlag(true);
						}
					});
				}
			}
			return null;
		};
		String masterTableValue = cc.getCellString(columnVarMap.get("masterTable").cellName);
		String subTableValue = cc.getCellString(columnVarMap.get("subTable").cellName);
		//String pickViewValue = cc.getCellString(columnVarMap.get("pickView").cellName);
		if ((StringUtils.isNotBlank(masterTableValue) && biTableMap.get(masterTableValue) == null)
				|| (StringUtils.isNotBlank(subTableValue) && biTableMap.get(subTableValue) == null)
				/*|| (StringUtils.isNotBlank(pickViewValue) && biTableMap.get(pickViewValue) == null)*/) {
			ZkBiAbstractLongOp.newInstance(curComp, "Loading", 100, opcb);
		} else
			opcb.action();
	}
	
	private void refreshColumnItems(boolean scrollToNewItem) {
		try {
			BiResult br = getBr().getSubLink("bischema.BiColumn");
			List<Listitem> items = lbColumns.getItems();
			ListModel<Object> modelList = lbBiColumns.getListModel();
			Listitem newItem = null;
			for (Listitem liSrc : lbBiColumns.getItems()) {
				int i = liSrc.getIndex();
				Object o = br.getTrStatObj(modelList.getElementAt(i));
				CellCollection cc = null;
				if (o != null)
					cc = br.getRowCollectionO(o);
				if (cc == null)
					continue;
				boolean isMarkDelete = br.isMarkedDelete(o);
				Listitem li;
				if (i > items.size() - 1) {
					newItem = li = new Listitem();
					showColumnItem(li, liSrc, cc, isMarkDelete);
					items.add(li);
				} else if (items.get(i).getAttribute("cellCollection") != cc) {
					newItem = li = new Listitem();
					showColumnItem(li, liSrc, cc, isMarkDelete);
					items.add(i, li);
				} else if (items.get(i).getAttribute("listitem") != liSrc) {
					li = items.get(i);
					showColumnItem(li, liSrc, cc, isMarkDelete);
				} else
					li = items.get(i);
				((Intbox)li.getAttribute("itemSeqComp")).setValue(cc.getInt("grptc_seq"));
				((Listcell)li.getAttribute("itemNameComp")).setLabel(cc.getCellString("grptc_label"));
				((Listcell)li.getAttribute("itemHeaderComp")).setLabel(cc.getCellString("grptc_header"));
			}
			showColumnProps(lbColumns.getSelectedItem());
			if (scrollToNewItem && newItem != null) {
				UniLog.log1("scrollIntoView %d", newItem.getIndex());
				Clients.scrollIntoView(newItem);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	private void showExceptionMessage(Exception ex) {
   		Messagebox.show(StringUtils.defaultIfBlank(ex.getMessage(), ex.toString()), sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
	}
	
	private static void resetupS2Comp(Listbox lb) {
		ZkUtil.delayJs(lb,null,50,"zkbis2.setup('%s',%b,%b,'%s',%b,%b);$('#%s').focus()",lb.getUuid(), lb.isMultiple(), false, StringUtils.defaultString((String)lb.getAttribute("placeholder")), false, false, lb.getUuid());
	}
}
