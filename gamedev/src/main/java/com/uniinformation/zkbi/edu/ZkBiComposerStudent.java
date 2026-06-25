package com.uniinformation.zkbi.edu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.MessageboxDlg;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.zk.ZkJxTimePicker;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.edu.Student;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.utils.whereclpar.Expression;
import com.uniinformation.utils.whereclpar.Parser;
import com.uniinformation.utils.whereclpar.Variable;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiAdvSearch;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;
import static com.uniinformation.jxapp.edu.Student.showErrMsg;

public class ZkBiComposerStudent extends ZkBiComposerBase {
	Button btAsQuery;
	Button btAddToCourse, btAddAttendance, btAddToAsmt;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("doAfterCompose called");
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin")) {
			queryBar.setVisible(false);
			bottomPanelVbox.setVisible(false);
		}
	}

	@Override
    public void showListPanel() {
		super.showListPanel();
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin")) {
			queryBar.setVisible(false);
			bottomPanelVbox.setVisible(false);
		}
    }

	@Override
	protected void setupExtraButton(final BiResult result) {
		btAsQuery = new ZkBiButton("Student Query", "images/icons/zkweb/062-search-25x25.png", "btAsQuery", "Asmt Query", sessionHelper);
		btAsQuery.setTooltiptext("Search student record");
		abHelper.addButton(btAsQuery,"fa-search");
		btAsQuery.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				showAsQueryForm(result);
			}
		});
		
		btAddToAsmt = new ZkBiButton("Add to Asmt", "images/icons/zkweb/063-more-25x25.png", "btAddToAsmt", "Add to Asmt", sessionHelper);
		btAddToAsmt.setTooltiptext("Add student(s) to assessment session");
		abHelper.addButton(btAddToAsmt,"fa-plus");
		btAddToAsmt.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				final Set<Integer> studentList = getBatchStudentMap(result).keySet();
       			if (studentList.isEmpty()) {
       				ZkUtil.showErrMsg("Please choose student");
       				return;
       			}
       			doAddToAsmtSch(sessionHelper, btAddToAsmt, true, studentList);
			}
		});
		
		
		
		btAddToCourse = new ZkBiButton("Add to Course", "images/icons/zkweb/063-more-25x25.png", "btAddToCourse", "Add to Course", sessionHelper);
		btAddToCourse.setTooltiptext("Add student(s) to course");
		abHelper.addButton(btAddToCourse,"fa-plus");
		btAddToCourse.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				final Set<Integer> studentList = getBatchStudentMap(result).keySet();
       			if (studentList.isEmpty()) {
       				ZkUtil.showErrMsg("Please choose student");
       				return;
       			}
       			doAddToCourse(sessionHelper, btAddToCourse, null, -1, studentList);
			}
		});
		ZkUtil.setupBatchModeButton(btAddToCourse, batchModeToggleButton);
		ZkUtil.setupBatchModeButton(btAddToAsmt, batchModeToggleButton);
		
		btAddAttendance = new ZkBiButton("Update Attendance", "images/icons/zkweb/063-more-25x25.png", "btAddAttendance", "Add Attendance", sessionHelper);
		btAddAttendance.setTooltiptext("Update student(s) attendance status");
		abHelper.addButton(btAddAttendance,"fa-plus");
		btAddAttendance.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				final Map<Integer, String> studentMap = getBatchStudentMap(result);
       			if (studentMap.isEmpty()) {
       				ZkUtil.showErrMsg("Please choose student");
       				return;
       			}
       			Student.doUpdateAttendance(sessionHelper, "Batch Update Attendance", btAddAttendance, null, -1, studentMap);
			}
		});
		ZkUtil.setupBatchModeButton(btAddAttendance, batchModeToggleButton);
	}
	
	private void showAsQueryForm(final BiResult result) {
		final CellCollection cc = new CellCollection();
		final ZkForm zkf = new ZkForm(null,"zkf/edu/AsQuery.zul");
		try {
			Window win = (Window) zkf.getRootComponent();
			win.setTitle("Student Query");
			win.setClosable(true);
			win.setWidth("1030px");
			win.setHeight("2130px");
			win.setStyle(String.format("max-width:100%%;max-height:%s;", sessionHelper.isMobile() ? "100%" : "95%"));

			BiResult brCourses = result.getSubLink("edu.StudentCourse");
			BiResult brAttendance = result.getSubLink("edu.StudentAttendance");
			BiSchema schema = BiSchema.loadSchema(sessionHelper);
			BiView bvCourse = schema.getViewByName("edu.Course");	
			BiView bvCourseStudent = schema.getViewByName("edu.CourseStudent");	
			BiView bvSession = schema.getViewByName("edu.Session");	
			BiView bvAttendance = schema.getViewByName("edu.Attendance");	
			Map<String, String[]> courseOptMap = new HashMap<String, String[]>();
			Map<String, String[]> courseStudentOptMap = new HashMap<String, String[]>();
			Map<String, String[]> sessionOptMap = new HashMap<String, String[]>();
			Map<String, String[]> attendanceOptMap = new HashMap<String, String[]>();
			for (BiColumn bc : bvCourse.getColumns()) {
				if (bc.getColumnType().equals("radio") && bc.getField() != null && bc.getOptionList(getSessionHelper()) != null)
					courseOptMap.put(bc.getField().getName(), bc.getOptionList(getSessionHelper()));
			}
			for (BiColumn bc : bvCourseStudent.getColumns()) {
				if (bc.getColumnType().equals("radio") && bc.getField() != null && bc.getOptionList(getSessionHelper()) != null)
					courseStudentOptMap.put(bc.getField().getName(), bc.getOptionList(getSessionHelper()));
			}
			for (BiColumn bc : bvSession.getColumns()) {
				if (bc.getColumnType().equals("radio") && bc.getField() != null&& bc.getOptionList(getSessionHelper()) != null)
					sessionOptMap.put(bc.getField().getName(), bc.getOptionList(getSessionHelper()));
			}
			for (BiColumn bc : bvAttendance.getColumns()) {
				if (bc.getColumnType().equals("radio") && bc.getField() != null && bc.getOptionList(getSessionHelper()) != null)
					attendanceOptMap.put(bc.getField().getName(), bc.getOptionList(getSessionHelper()));
			}

			final Tabbox tabbox = (Tabbox) win.query("tabbox");
			for (Component rowComp : tabbox.queryAll("row")) {
				Row row = (Row) rowComp;
				Div asDiv = (Div) row.query("row #essd_asdiv");
				if (asDiv != null) {
					for (Component cCbAllOpt : asDiv.queryAll(".cballopt")) {
						final Checkbox cbAllOpt = (Checkbox) cCbAllOpt;
						final Div parentDiv = (Div) cbAllOpt.getParent();
						cbAllOpt.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
							@Override
							public void onZkBiEvent(CheckEvent event) throws Exception {
								UniLog.log1("event:%s", event);
								for (Component cCheckbox : parentDiv.queryAll("checkbox")) {
									Checkbox cb = (Checkbox) cCheckbox;
									if (StringUtils.isNotBlank(cb.getId()))
										cb.setChecked(event.isChecked());
								}
							}
						});
					}
					for (Component cBtClear : asDiv.queryAll(".btnclear"))
						cBtClear.getParent().setVisible(false);
				}
				else {
					for (Component comp : row.queryAll("textbox,datebox,combobox,radiogroup")) {
						if (StringUtils.isNotBlank(comp.getId())) {
							//UniLog.log1("id:%s", comp.getId());
							String tid = comp.getId().replaceAll("_from$|_to$", "");
							if (tid != null) {
								BiColumn bc = result.getColumnByLabel(tid);
								if (bc == null)
									bc = brCourses.getColumnByLabel(tid);
								if (bc == null)
									bc = brAttendance.getColumnByLabel(tid);
								if (bc != null && !bc.isInvisible(sessionHelper)) {
									JxZkBiBase.setComponentFormat(comp, bc, false, sessionHelper);
									comp.setAttribute("biColumn", bc);
									if (comp instanceof Textbox) {
										Textbox tb = (Textbox) comp;
										if (tb.getMaxlength() > 0 && tb.getMaxlength() <= Integer.MAX_VALUE - 2)
											tb.setMaxlength(tb.getMaxlength() + 2);
									}
									else if (comp instanceof Radiogroup) {
										//fill radiogroup options
										Radiogroup rg = (Radiogroup) comp;
										String[] opts = null;
										if (bc.getColumnType().equals("radio"))
											opts = bc.getOptionList(getSessionHelper());
										if (bc.getTable() != null && bc.getField() != null) {
											if (opts == null && bc.getTable().getName().equals(bvCourse.getTable().getName()))
												opts = courseOptMap.get(bc.getField().getName());
											if (opts == null && bc.getTable().getName().equals(bvCourseStudent.getTable().getName()))
												opts = courseStudentOptMap.get(bc.getField().getName());
											if (opts == null && bc.getTable().getName().equals(bvSession.getTable().getName()))
												opts = sessionOptMap.get(bc.getField().getName());
											if (opts == null && bc.getTable().getName().equals(bvAttendance.getTable().getName()))
												opts = attendanceOptMap.get(bc.getField().getName());
										}
										UniLog.log1("bcLabel:%s, opts:%s, table:%s", bc.getLabel(), opts, bc.getTable());
										if (opts != null) {
											List<String> optList = new ArrayList<String>(Arrays.asList(opts));
											for (Radio r : rg.getItems()) {
												int i;
												if ((i = optList.indexOf(r.getLabel())) >= 0)
													optList.remove(i);
											}
											for (String opt : optList)
												rg.appendItem(opt, opt);
										}
									}
								}
							}
						}
					}
				}
			}

			zkf.doModal(cc, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("doModal onEvent called:%s", event);
					if (StringUtils.equalsAny(event.getTarget().getId(), "btClose")) {
						UniLog.log("clicked close button");
						zkf.exitModal();
						return;
					}
					if (StringUtils.equalsAny(event.getTarget().getId(), "btSearch")) {
						UniLog.log("clicked search button");
						/*ZkUtil.showMsg("search dev in progress");
						JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(cc);
						UniLog.log1("json:\n%s",jo.toString(3));*/
						if (makeCustomCondition(tabbox, result))
							zkf.exitModal();
						return;
					}
					if (StringUtils.equalsAny(event.getTarget().getId(), "btClear")) {
						UniLog.log("clicked clear button");
						for (Component comp : tabbox.queryAll("textbox,radiogroup,datebox,checkbox,combobox")) {
							if (comp instanceof Textbox)
								((Textbox) comp).setValue(null);
							else if (comp instanceof Datebox)
								((Datebox) comp).setValue(null);
							else if (comp instanceof Combobox)
								((Combobox) comp).setValue(null);
							else if (comp instanceof Checkbox)
								((Checkbox) comp).setChecked(false);
							else if (comp instanceof Radiogroup)
								((Radiogroup) comp).setSelectedIndex(0);
						}
						return;
					}
				}
				
			});
			restoreFormValues(tabbox);
			bindDayOfWeekComponents(tabbox, "eaav0_sessionday", "eaav0_sessionday_cbc");
			bindDayOfWeekComponents(tabbox, "avts_day", "avts_day_cbc");
			
			final Radiogroup rgMothertongue = (Radiogroup) tabbox.query("row #essd_motongue");
			final Textbox tbOtherMothertongue = (Textbox) tabbox.query("row #essd_omotongue");
			rgMothertongue.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					UniLog.log1("event:%s, target:%s", event, event.getTarget(), event.getTarget());
					Radio rd = (Radio) event.getTarget();
					if (StringUtils.equals(rd.getLabel(), "Others"))
						tbOtherMothertongue.setDisabled(false);
					else {
						tbOtherMothertongue.setDisabled(true);
						tbOtherMothertongue.setText("");
					}
				}
			});
			tbOtherMothertongue.setDisabled(true);
		} 
		catch (Exception cex) {
			UniLog.log(cex);
		}
		
	}
	
	private Condition andCondition(Condition newCondition, Condition oldCondition) throws Exception {
		if (newCondition != null)
			return new Condition(newCondition, Condition.LOGIC_OP_AND, oldCondition);
		else
			return oldCondition;
	}

	private Expression getInputExpression(Component valueComp, BiColumn bc, boolean[] byLikeOperators) throws Exception {
		Object obj = null;
		byLikeOperators[0] = false;
		boolean eqFlag = false;
		if (valueComp instanceof Radiogroup) {
			int idx = ((Radiogroup) valueComp).getSelectedIndex();
			if (idx > 0) {
				obj = ((Radiogroup) valueComp).getItemAtIndex(idx).getLabel();
				String[] optionList = ZkBiAdvSearch.getTypeIntOptionList(bc, sessionHelper);
				obj = ZkBiAdvSearch.indexOfOptionList(optionList, obj);
			}
			eqFlag = true;
		}
		else if (valueComp instanceof ZkJxTimePicker) {
			String v = ((ZkJxTimePicker) valueComp).getValue();
			if (StringUtils.isNotBlank(v)) 
				obj = "1970/01/01 " + v;
			eqFlag = true;
		}
		else if (valueComp instanceof InputElement)
			obj = ((InputElement) valueComp).getRawValue();
		//UniLog.log1("getInputExpression obj:%s, valueComp:%s", obj, valueComp);
		if (obj != null) {
			if (obj instanceof Integer)
				return new Expression((Integer)obj);
			else if (obj instanceof Date)
				return new Expression((Date)obj);
			else if (obj instanceof Float)
				return new Expression((Float)obj);
			else if (obj instanceof Double)
				return new Expression((Double)obj);
			else {
				String str = obj.toString();
				boolean hasEqualPrefix = str.startsWith("=");
				if (hasEqualPrefix)
					str = str.substring(1);
				if (StringUtils.isNotBlank(str)) {
					if (bc.getColumnType().trim().matches("integer|serial"))
						return new Expression(NumberUtils.toInt(str));
					else if (bc.getColumnType().trim().matches("float|double|money"))
						return new Expression(NumberUtils.toDouble(str));
					else {
						if (!eqFlag && !hasEqualPrefix) {
							byLikeOperators[0] = true;
							if (!str.startsWith("%") && !str.endsWith("%"))
								str = "%" + str + "%";
						}
						return new Expression(str);
					}
				}
				else
					return null;
			}
		} else
			return null;
	}

	private static class ConditionWrapper {
		Condition cond;
		List<ConditionWrapper> childs = new ArrayList<ConditionWrapper>();
		ConditionWrapper(Condition cond) {
			this.cond = cond;
		}
	}

	private List<ConditionWrapper> parseCondition(String str, Boolean[] byAnds) throws Exception {
		List<ConditionWrapper> list = new ArrayList<ConditionWrapper>();
		Parser yyparser = new Parser(null, null, null);
		UniLog.log1("str:" + str);
		Condition result = (Condition) yyparser.parse(str);
		UniLog.log1(result.toString());
		if (result.get_isPredicate())
			list.add(new ConditionWrapper(result));
		else {
			boolean byAnd = result.get_operator() == Condition.LOGIC_OP_OR;
			byAnds[0] = byAnd;
			List<Condition> l1 = Condition.serializeCondition(byAnd, result);
			l1 = Condition.optimizeConditionList(l1, yyparser, byAnd);
			for (Condition cond : l1) {
				UniLog.log1("cond:" + cond + "," + cond.get_isPredicate());
				ConditionWrapper condw = new ConditionWrapper(cond);
				list.add(condw);
				if (!cond.get_isPredicate())
					condw.childs.addAll(parseCondition(cond.toString(), new Boolean[1]));
			}
		}
		return list;
	}

	private Condition parseCondition(String str) throws Exception {
		if (StringUtils.isBlank(str))
			return null;
		Condition result = null;
		try {
			Parser yyparser = new Parser(null, null, null);
			UniLog.log1("parseCondition str:" + str);
			result = (Condition) yyparser.parse(str);
			UniLog.log1("result:%s,get_isPredicate:%b,get_leftExpression:%s,get_rightExpression:%s", result.toString(), result.get_isPredicate(), result.get_leftExpression(), result.get_rightExpression());
		}
		catch (Exception e) {
		}
		catch (Error e) {
		}
		return result;
	}
	
	private void restoreFormSingleValue(Tabbox tabbox, Expression leftExp, Expression rightExp, int operator) throws Exception {
		String compId = null;
		String compValue = expression2CompValue(rightExp.toString());
		String leftExpStr = leftExp.toString().trim();
		if (leftExpStr.startsWith("subCell")) {
			String str = leftExpStr.replaceAll("\\s", "");
			Pattern p = Pattern.compile("subCell\\(subcell,'essdx_jsoncc','(\\w+)'\\)");
			Matcher m = p.matcher(str);
			if (m.matches()) {
				compId = m.group(1);
				UniLog.log1("compId:%s", compId);
			}
			else
				throw new Exception("Invalid Expression");
			if (compValue.equals("1"))
				compValue = "Y";
			else if (compValue.equals("2"))
				compValue = "N";
			else
				return;
		}
		else
			compId = leftExpStr;

		Component comp = tabbox.query("row #" + compId);
		if (comp != null) {
			if (comp instanceof Textbox) {
				if (operator == Condition.COMPARE_OP_LK) {
					if (StringUtils.isNotBlank(compValue)) {
						if (compValue.startsWith("%") && compValue.endsWith("%")) {
							if (compValue.length() > 2)
								compValue = compValue.substring(1, compValue.length() - 1);
						}
						else if (!compValue.startsWith("%") && !compValue.endsWith("%"))
							compValue += "%";
					}
				}
				else if (operator == Condition.COMPARE_OP_EQ)
					compValue = "=" + compValue;
				((Textbox) comp).setValue(compValue);
			}
			else if (comp instanceof Radiogroup) {
				BiColumn bc = (BiColumn)comp.getAttribute("biColumn");
				if (bc != null && bc.getField() != null && StringUtils.equals(bc.getField().getFieldType(), "integer"))
					compValue = ZkBiAdvSearch.getOptionLabel(bc.getOptionList(sessionHelper), compValue);
				Radiogroup rg = (Radiogroup) comp;
				for (Radio item : rg.getItems()) {
					if (StringUtils.equals(item.getLabel(), compValue)) {
						rg.setSelectedItem(item);
						break;
					}
				}
			}
			else if (comp instanceof Checkbox) {
				if (compValue.equals("Y"))
					((Checkbox) comp).setChecked(true);
			}
		}
	}

	private void restoreFormDoubleValue(Tabbox tabbox, Expression leftExp, Expression rightExp1, Expression rightExp2) {
		String compValue1 = rightExp1 != null ? expression2CompValue(rightExp1.toString()) : null;
		String compValue2 = rightExp2 != null ? expression2CompValue(rightExp2.toString()) : null;
		Component compFrom = tabbox.query("row #" + leftExp.toString() + "_from");
		Component compTo = tabbox.query("row #" + leftExp.toString() + "_to");
		if (compFrom != null && compTo != null) {
			if (compFrom instanceof Datebox) {
				((Datebox) compFrom).setValue(DateUtil.dateTimeStrToDate(compValue1));
				((Datebox) compTo).setValue(DateUtil.dateTimeStrToDate(compValue2));
			}
			else if (compFrom instanceof ZkJxTimePicker) {
				Date d1 = DateUtil.dateTimeStrToDate(compValue1);
				Date d2 = DateUtil.dateTimeStrToDate(compValue2);
				SimpleDateFormat sdtf = new SimpleDateFormat("HH:mm:dd");
				((ZkJxTimePicker) compFrom).setValue(sdtf.format(d1));
				((ZkJxTimePicker) compTo).setValue(sdtf.format(d2));
			}
			else if (compFrom instanceof Textbox) {
				((Textbox) compFrom).setValue(compValue1);
				((Textbox) compTo).setValue(compValue2);
			}
		}
	}
	
	private void restoreFormValues(Tabbox tabbox) {
		if (StringUtils.isBlank(inputFieldsList.getCustomCondition()))
			return;
		try {
			Boolean[] byAnds = new Boolean[1];
			List<ConditionWrapper> list = parseCondition(inputFieldsList.getCustomCondition(), byAnds);
			boolean onlyOneBlock = true;
			for (ConditionWrapper condw : list) {
				if (!condw.cond.get_isPredicate() || !condw.childs.isEmpty()) {
					onlyOneBlock = false;
					break;
				}
			}
			UniLog.log("onlyOneBlock:" + onlyOneBlock + ", byAnds[0]:" + byAnds[0] + ", listSize:" + list.size());
			if (onlyOneBlock && (byAnds[0] == null || !byAnds[0])) {
				UniLog.log1("valid condition");
				for (ConditionWrapper condw : list) {
					Condition predicateCond = condw.cond;
					Expression leftExp = predicateCond.get_leftExpression();
					int operator = predicateCond.get_operator();
					switch (operator) {
					case Condition.COMPARE_OP_LK:
						Expression rightExp = predicateCond.get_rightExpression();
						UniLog.log1("leftExp:%s, operator:%d, rightExp:%s", leftExp, operator, rightExp);
						restoreFormSingleValue(tabbox, leftExp, rightExp, operator);
						break;
					case Condition.COMPARE_OP_EQ:
						rightExp = predicateCond.get_rightExpression();
						UniLog.log1("leftExp:%s, operator:%d, rightExp:%s", leftExp, operator, rightExp);
						restoreFormSingleValue(tabbox, leftExp, rightExp, operator);
						break;
					case Condition.COMPARE_OP_GE:
						rightExp = predicateCond.get_rightExpression();
						UniLog.log1("leftExp:%s, operator:%d, rightExp:%s", leftExp, operator, rightExp);
						restoreFormDoubleValue(tabbox, leftExp, rightExp, null);
						break;
					case Condition.COMPARE_OP_LE:
						rightExp = predicateCond.get_rightExpression();
						UniLog.log1("leftExp:%s, operator:%d, rightExp:%s", leftExp, operator, rightExp);
						restoreFormDoubleValue(tabbox, leftExp, null, rightExp);
						break;
					case Condition.COMPARE_OP_BETWEEN:
						Expression rightExp1 = predicateCond.get_rightExpression1();
						Expression rightExp2 = predicateCond.get_rightExpression2();
						UniLog.log1("leftExp:%s, operator:%d, rightExp1:%s, rightExp2:%s", leftExp, operator, rightExp1, rightExp2);
						restoreFormDoubleValue(tabbox, leftExp, rightExp1, rightExp2);
						break;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			ZkUtil.showErrMsg("Error:%s", e.getMessage());
		}
	}

	private boolean makeCustomCondition(Tabbox tabbox, BiResult result) {
		try {
			BiResult brCourses = result.getSubLink("edu.StudentCourse");
			BiResult brAttendance = result.getSubLink("edu.StudentAttendance");
			Vector<BiColumn> bcList = new Vector<BiColumn>();
			bcList.addAll(result.getColumns());
			bcList.addAll(brCourses.getColumns());
			bcList.addAll(brAttendance.getColumns());

			Condition condition = null;
			for (BiColumn bc : bcList) {
				Component comp = tabbox.query("row #" + bc.getLabel());
				//UniLog.log1("bcLabel:%s,comp:%s", bc.getLabel(), comp);
				if (comp != null) {
					Expression fExp = new Expression(new Variable(bc.getLabel(), null, null));
					boolean[] byLikeOpts = new boolean[1];
					Expression vExp = getInputExpression(comp, bc, byLikeOpts);
					if (vExp != null)
						condition = andCondition(condition, new Condition(fExp, byLikeOpts[0] ? Condition.COMPARE_OP_LK : Condition.COMPARE_OP_EQ, vExp));
				}
				else {
					Component compFrom = tabbox.query("row #" + bc.getLabel() + "_from");
					Component compTo = tabbox.query("row #" + bc.getLabel() + "_to");
					if (compFrom != null && compTo != null) {
						Expression fExp = new Expression(new Variable(bc.getLabel(), null, null));
						Expression vExp0 = getInputExpression(compFrom, bc, new boolean[1]);
						Expression vExp1 = getInputExpression(compTo, bc, new boolean[1]);
						if (vExp0 != null && vExp1 != null)
							condition = andCondition(condition, new Condition(fExp, Condition.COMPARE_OP_BETWEEN, vExp0, vExp1));
						else if (vExp0 != null)
							condition = andCondition(condition, new Condition(fExp, Condition.COMPARE_OP_GE, vExp0));
						else if (vExp1 != null)
							condition = andCondition(condition, new Condition(fExp, Condition.COMPARE_OP_LE, vExp1));
					}
				}
			}
			Div asDiv = (Div) tabbox.query("row #essd_asdiv");
			for (Component comp : asDiv.queryAll("radiogroup,checkbox")) {
				if (StringUtils.isNotBlank(comp.getId())) {
					//Expression fExp = new Expression(new Variable(comp.getId(), null, null));
					//Expression vExp = null;
					Integer v = null;
					if (comp instanceof Radiogroup) {
						Radiogroup rg = (Radiogroup) comp;
						switch (rg.getSelectedIndex()) {
						case 1:
							//vExp = new Expression("Y");
							v = 1;
							break;
						case 2:
							//vExp = new Expression("N");
							v = 2;
							break;
						}
					}
					else if (comp instanceof Checkbox) {
						Checkbox cb = (Checkbox) comp;
						if (cb.isChecked()) {
							//vExp = new Expression("Y");
							v = 1;
						}
					}
					/*if (vExp != null)
						condition = andCondition(condition, new Condition(fExp, Condition.COMPARE_OP_EQ, vExp));*/
					if (v != null)
						condition = andCondition(condition, parseCondition(String.format("subCell(subcell,'essdx_jsoncc','%s') = %d", comp.getId(), v)));
				}
			}
			if (condition != null) {
				String curPreset = (String)conditionPresetListbox.getSelectedItem().getValue();
				String conditionStr = condition.toString();
				String oldConditionStr = "";
				if (curPreset != null) {
					ConditionFieldMap cfm = mConditionPresets.getFieldMap(curPreset);
					oldConditionStr = cfm.getCustomCondition();
				}
				UniLog.log1("condition:%s", conditionStr);

				inputFieldsList.setCustomCondition(conditionStr);
				MultiSortMap sortMap = curPreset != null ? new MultiSortMap(curPreset,result) : null;
				refresh(result,masterWin,sortMap,false);
				Events.sendEvent("onReset", conditionPresetListbox, curPreset);
				if (!StringUtils.equals(conditionStr, oldConditionStr))
					divAdvSearchG2Indicator.setVisible(true);
			}
			else {
				showErrMsg(tabbox, "Please input condition first");
				return false;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			ZkUtil.showErrMsg("Error:%s", e.getMessage());
		}
		return true;
	}

	private String expression2CompValue(String src) {
		src = StringUtils.defaultString(src).trim();
		if (StringUtils.startsWithAny(src, new String[]{"\"", "'"}))
			src = src.substring(1);
		if (StringUtils.endsWithAny(src, new String[]{"\"", "'"}))
			src = src.substring(0, src.length() - 1);
		return src;
	}

	private void bindDayOfWeekComponents(Tabbox tabbox, String tbName, String cbcName) {
		final Textbox tb = (Textbox) tabbox.query("row #" + tbName);
		final Hlayout cbc = (Hlayout) tabbox.query("row #" + cbcName);
		String tbValue = tb.getValue().replaceAll("^=", "");
		final List<String> allDayList = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
		final List<String> curDayList = Arrays.asList(StringUtils.split(tbValue, ",;"));
		final Checkbox[] cbs = new Checkbox[allDayList.size()];

		for (Component cbComp: cbc.queryAll("Checkbox")) {
			final Checkbox cb = (Checkbox) cbComp;
			final int idx = allDayList.indexOf(cb.getLabel());
			cbs[idx] = cb;
			cb.setChecked(curDayList.contains("" + idx));
			ZkBiEventListener<CheckEvent> el = (ZkBiEventListener<CheckEvent>) cb.getAttribute("checkEvent");
			if (el != null)
				cb.removeEventListener(Events.ON_CHECK, el);
			cb.addEventListener(Events.ON_CHECK, el = new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					UniLog.log1("cbComp %d onZkBiEvent:%s", idx, event);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < cbs.length; i++) {
						if (cbs[i].isChecked()) {
							if (sb.length() > 0)
								sb.append(",");
							sb.append(i);
						}
					}
					if (sb.length() > 0)
						sb.insert(0, "=");
					tb.setValue(sb.toString());
				}
			});
			cb.setAttribute("checkEvent", el);
		}
	}
	
	private static String getCourseCustomCondition() {
		return "eaav0_status = 'Normal'";
	}
	/*
	//obsoleted, replaced by getCourseJson
	private int getCourseRg(String courseCode) throws Exception {
		BiResult br = null;
		try {
			br = sessionHelper.newBiResult("edu.Course");
			br.addCustomCondition(getCourseCustomCondition());
			br.addCustomCondition(String.format("eaav0_code = '%s'", courseCode));
			ReturnMsg rtn;
			if ((rtn = br.query(true, false)).getStatus()) {
				if (br.next()) {
					JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection());					
					UniLog.log1("HAHAJSON:\n" + jo.toString(3));
					return br.getCellInt("eaav0_rg");
				}
			}
			else
				throw new Exception(rtn.getMsg());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (br != null)
				br.close();
		}
		return 0;
	}
	*/
	private static JSONObject getCourseJson(SessionHelper sessionHelper, Integer courseRg, String courseCode) {
		BiResult br = null;
		try {
			br = sessionHelper.newBiResult("edu.Course");
			br.addCustomCondition(getCourseCustomCondition());
			if (courseRg != null) {
				br.addCustomCondition(String.format("eaav0_rg = %d", courseRg));
			}
			else {
				br.addCustomCondition(String.format("eaav0_code = '%s'", courseCode));
			}
			ReturnMsg rtn;
			if ((rtn = br.query(true, false)).getStatus()) {
				if (br.next()) {
					JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection());					
					return jo;
				}
				else {
					return null;
				}
			}
			else {
				throw new Exception(rtn.getMsg());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			if (br != null)
				br.close();
		}
	}
	
	private static Map<Integer, String> findAvilableCourseDatas(SessionHelper sessionHelper, Set<Integer> studentList) throws Exception {
		BiResult br = null;
		BiResult brStudent = null;
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		try {
			br = sessionHelper.newBiResult("edu.Course");
			brStudent = sessionHelper.newBiResult("edu.CourseStudent");
			
			br.addCustomCondition(getCourseCustomCondition());
			//andrew210826 before is check session date, now change to check enddate
			//br.addCustomCondition(String.format("essncs_date >= '%s'", DateUtil.dateToDateTimeStr(new Date(), "yyyy/MM/dd")));
			br.addCustomCondition(String.format("(eaav0_enddate < '%s' or  eaav0_enddate >= '%s')", DateUtil.getMinDateY4MD(), DateUtil.dateToDateTimeStr(new Date(), "yyyy/MM/dd")));
			ReturnMsg rtn;
			if ((rtn = br.query(true, false)).getStatus()) {
				while (br.next()) {
					int courseRg = br.getCellInt("eaav0_rg");
					String courseCode = br.getCellString("eaav0_code");
					String courseName = br.getCellString("eaav0_name");
					Date startDate = br.getCellDate("eaav0_startdate");
					
					Set<Integer> tStudentList = new HashSet<Integer>();
					brStudent.clearCondition();
					brStudent.addCustomCondition(String.format("essbsd_avrg = %d", courseRg));
					if ((rtn = brStudent.query(true, false)).getStatus()) {
						while (brStudent.next()) {
							int studentRg = brStudent.getCellInt("essbsd_sdrg");
							String studentName = brStudent.getCellString("essd_name");
							UniLog.log1("studentRg:%d, studentName:%s", studentRg, studentName);
							if (studentList.contains(studentRg)) {
								tStudentList.add(studentRg);
								if (tStudentList.size() == studentList.size())
									break;
							}
						}
						if (tStudentList.size() < studentList.size())
							map.put(courseRg, String.format("%s (%s, %s)", courseCode, courseName, DateUtil.dateToDateTimeStr(startDate, "yyyy/MM/dd")));
						UniLog.log1("courseRg:%d, coursecode:%s, coursename:%s, startdate:%s, list size:%d,%d", courseRg, courseCode, courseName, startDate, tStudentList.size(), studentList.size());
					}
					else
						throw new Exception(rtn.getMsg());
				}
			}
			else
				throw new Exception(rtn.getMsg());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (br != null)
				br.close();
			if (brStudent != null)
				br.close();
		}
		return map;
	}
	
	private static Map<Integer, String> findAvailableAsmtSchDatas(SessionHelper sessionHelper, Set<Integer> studentList) throws Exception {
		BiResult brAsmt = null;
		Map<Integer, String> map = new LinkedHashMap<Integer, String>();
		try {
			brAsmt = sessionHelper.newBiResult("edu.Assessment");
			brAsmt.clearCondition();
			//brAsmt.addCustomCondition(String.format("essnas_date >= '%s' and essnas_status <> 'Cancelled'", DateUtil.dateToDateTimeStr(new Date(), "yyyy/MM/dd")));
			brAsmt.addCustomCondition(String.format("essnas_date >= '%s' and essnas_status = 'Normal'", DateUtil.dateToDateTimeStr(new Date(), "yyyy/MM/dd")));
			ReturnMsg rtn;
			if ((rtn = brAsmt.query(true, false)).getStatus()) {
				while (brAsmt.next()) {
					int rg = brAsmt.getCellInt("essnas_rg");
					String name = brAsmt.getCellString("essnas_name");
					Date date = brAsmt.getCellDate("essnas_date");
					Date sttime = brAsmt.getCellDate("essnas_sttime");
					Date endtime = brAsmt.getCellDate("essnas_endtime");
					map.put(rg, String.format("%s %s-%s %s", 
							DateUtil.dateToDateTimeStr(date, "yyyy/MM/dd"),
							DateUtil.dateToTimeStr(sttime,false),
							DateUtil.dateToTimeStr(endtime,false),
							name));
				}
			}
			else
				throw new Exception(rtn.getMsg());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (brAsmt != null) brAsmt.close();
		}
		return map;
	}
	
	private static Pair<Integer, Integer> addToAsmtSch(SessionHelper sessionHelper, List<Integer> asmtSchList, Set<Integer> studentList) throws Exception {
		BiResult br = null;
		BiResult brQuery = null;
		int addCount = 0;
		int skipCount = 0;
		try {
			br = sessionHelper.newBiResult("edu.Attendance");
			brQuery = sessionHelper.newBiResult("edu.Attendance");
			br.beginWork();
			for (int asmtSchRg : asmtSchList) {
				for (int studentRg : studentList) {
					brQuery.clearCondition();
					brQuery.addCustomCondition(String.format("esatsd_snrg = %d and esatsd_atrg = %d", asmtSchRg, studentRg));
					ReturnMsg rtn;
					if ((rtn = brQuery.query(true, false)).getStatus()) {
						if (!brQuery.next()) {
							br.clearCurrentRec();
							br.getCell("esatsd_snrg").set(asmtSchRg);
							br.getCell("esatsd_attype").set("SD");
							br.getCell("esatsd_atrg").set(studentRg);
							//br.getCell("esatsd_status").set("Normal"); //andrew210923: status should be Present/Absent/Leave
							br.getCell("esatsd_status").set(""); //andrew210923: status Present/Absent/Leave, new status blank means new record
							rtn = br.addCurrent();
							if (!rtn.getStatus())
								throw new Exception(rtn.getMsg());
							addCount++;
						}
						else {
							skipCount++;
							UniLog.log1("skip asmtSchRg:%d, asmtSchRg:%d", asmtSchRg, asmtSchRg);
						}
					}
					else
						throw new Exception(rtn.getMsg());
				}
			}
			br.commitWork();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (br != null)
				br.rollbackWork();
			throw e;
		}
		finally {
			if (br != null)
				br.close();
			if (brQuery != null)
				brQuery.close();
		}
		return Pair.of(addCount, skipCount);
	}
	private static Pair<Integer, Integer> addToCourse(SessionHelper sessionHelper, List<JSONObject> courseList, Set<Integer> studentList) throws Exception {
		BiResult br = null;
		BiResult brQuery = null;
		int addCount = 0;
		int skipCount = 0;
		try {
			br = sessionHelper.newBiResult("edu.CourseStudent");
			brQuery = sessionHelper.newBiResult("edu.CourseStudent");
			br.beginWork();
//			Vector arg = new Vector();
//			arg.add(-1);  //negative will not clear balance
//			arg.add(-1);  //negative will not clear balance
			
			for (JSONObject courseJson : courseList) {
				int courseRg = courseJson.getInt("eaav0_rg");
				String tokenccy = courseJson.getString("eaav0_tokenccy");
				Double courseFee = courseJson.getDouble("eaav0_coursefee");
				String allowReminder = courseJson.getString("eaav0_allowrem");
				
				for (int studentRg : studentList) {
					brQuery.clearCondition();
					brQuery.addCustomCondition(String.format("essbsd_avrg = %d and essbsd_sdrg = %d", courseRg, studentRg));
					ReturnMsg rtn;
					if ((rtn = brQuery.query(true, false)).getStatus()) {
						if (!brQuery.next()) {
							br.clearCurrentRec();
							br.getCell("essbsd_avrg").set(courseRg);
							br.getCell("essbsd_type").set(0);
							br.getCell("essbsd_sdrg").set(studentRg);
							br.getCell("essbsd_status").set("New");
							br.getCell("essbsd_allowrem").set(allowReminder);
							rtn = br.addCurrent();
							if (!rtn.getStatus())
								throw new Exception(rtn.getMsg());
							addCount++;
//							arg.add(studentRg);
//							arg.add(courseRg);
//							arg.add(tokenccy);
//							arg.add(courseFee);
						}
						else {
							skipCount++;
							UniLog.log1("skip essbsd_avrg:%d, essbsd_sdrg:%d", courseRg, studentRg);
						}
					}
					else
						throw new Exception(rtn.getMsg());
				}
			}
			
//			if (addCount > 0) {
//				UniLog.log1("DEBUG:" + arg);
//				Value v = br.getSelectUtil().getRpcClient().callSegment("token_addUpdateCourseSubscribeMulti",arg);
//				UniLog.log1("update token result:" + v);
//				
//			}
			
			
			
			
			
			br.commitWork();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (br != null)
				br.rollbackWork();
			throw e;
		}
		finally {
			if (br != null)
				br.close();
			if (brQuery != null) {
				brQuery = null;
			}
		}
		return Pair.of(addCount, skipCount);
	}
	
	public static void doAddToAsmtSch(final SessionHelper sessionHelper, final Button btAddToAsmt, final boolean batchMode, final Set<Integer> studentList) throws Exception {
		final GridHelper gh = new GridHelper(1);
		gh.setWidth("100%");
		gh.getColumn(0).setHflex("1");

 		final Listbox s2AsmtSch = new Listbox();
		s2AsmtSch.setMold("select");
		s2AsmtSch.setMultiple(false);
		//s2AsmtSch.setAttribute("placeholder", "Assessment Schedule (exclude completed/cancelled)");
		s2AsmtSch.setAttribute("placeholder", "Assessment Schedule(status normal)");
		s2AsmtSch.setAttribute("select2-enable", "Y");
		s2AsmtSch.setAttribute("select2-multiple", "N");
		s2AsmtSch.setHflex("1");
		gh.addRow(s2AsmtSch);

		for (Map.Entry<Integer, String> entry : findAvailableAsmtSchDatas(sessionHelper, studentList).entrySet()) {
			s2AsmtSch.appendItem(entry.getValue(), "" + entry.getKey());
		}
		
		MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Choose Assessment Schedule", 
			gh, 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
				btAddToAsmt.getRoot(),
				new EventListener<Messagebox.ClickEvent>(){
					@Override
					public void onEvent(ClickEvent event) throws Exception {
						if (event.getButton() == null)
							return;
						switch (event.getButton()) {
						case OK:
							List<Integer> asmtSchList = new ArrayList<Integer>();
							Set<Listitem> lis = s2AsmtSch.getSelectedItems();
							for (Listitem li : lis) {
								int rg = Integer.parseInt((String)li.getValue());
								UniLog.log1("rg:%d", rg);
								asmtSchList.add(rg);
							}
							if (asmtSchList.isEmpty()) {
								showErrMsg(gh, "Please choose assessment schedule");
								event.stopPropagation();
								return;
							}
							try {
								Pair<Integer, Integer> p = addToAsmtSch(sessionHelper, asmtSchList, studentList);
								if (p.getRight() > 0)
									ZkUtil.msg("%d record inserted, %d record ignored", p.getLeft(), p.getRight());
								else
									ZkUtil.msg("%d record inserted", p.getLeft());
								if (batchMode)
									ZkUtil.clickBatchCancel(btAddToAsmt);
							}
							catch(Exception e) {
								ZkUtil.showErrMsg(e.getMessage());
							}
							break;
						default:
							break;
						}
					}
		});
		dlg.setWidth("500px");
		dlg.setStyle("max-width:100%");
		dlg.addEventListener("onDelayInit", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				ZkUtil.setupSelect2(s2AsmtSch, true, false);
			}
		});
		dlg.doHighlighted();
		ZkUtil.delayPostEvent("onDelayInit", dlg, null, 100);
	}

	public static void doAddToCourse(final SessionHelper sessionHelper, final Button btAddToCourse, final JxZkBiBase biBase, final int curMode, final Set<Integer> studentList) throws Exception {
		final GridHelper gh = new GridHelper(2);
		gh.setWidth("100%");
		gh.getColumn(0).setWidth("30px");
		gh.getColumn(1).setHflex("1");

		final Radiogroup radiogroup = new Radiogroup();
		final Radio rbChoose = new Radio();
		final Listbox s2Course = new Listbox();
		final Radio rbInput = new Radio();
		final Textbox tbCourse = new Textbox();
		rbChoose.setRadiogroup(radiogroup);
		rbChoose.setChecked(true);
		rbInput.setRadiogroup(radiogroup);
		s2Course.setMold("select");
		s2Course.setMultiple(false);
		//s2Course.setAttribute("placeholder", "Course List (exclude completed course)");
		s2Course.setAttribute("placeholder", "Course List(status normal, valid enddate)");
		s2Course.setAttribute("select2-enable", "Y");
		s2Course.setAttribute("select2-multiple", "N");
		s2Course.setHflex("1");
		tbCourse.setWidth("240px");
		tbCourse.setPlaceholder("Enter course code");
		tbCourse.setDisabled(true);
		tbCourse.setStyle("max-width:100%");

		gh.addRow(new Div() {{
			appendChild(radiogroup);
			appendChild(rbChoose);
		}}, s2Course);
		gh.addRow(rbInput, tbCourse);

		for (Map.Entry<Integer, String> entry : findAvilableCourseDatas(sessionHelper, studentList).entrySet())
			s2Course.appendItem(entry.getValue(), "" + entry.getKey());
		radiogroup.addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>() {
			@Override
			public void onEvent(CheckEvent event) throws Exception {
				UniLog.log1("event:%s", event);
				if (event.getTarget() == rbInput) {
					s2Course.setDisabled(true);
					tbCourse.setDisabled(false);
				}
				else {
					s2Course.setDisabled(false);
					tbCourse.setDisabled(true);
				}
			}
		});

		final MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Choose course", 
			gh, 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
				btAddToCourse.getRoot(),
				new EventListener<Messagebox.ClickEvent>(){
					@Override
					public void onEvent(ClickEvent event) throws Exception {
						if (event.getButton() == null)
							return;
						switch (event.getButton()) {
						case OK:
							//List<Integer> courseList = new ArrayList<Integer>();
							List<JSONObject> courseList = new ArrayList<JSONObject>();
							if (rbChoose.isChecked()) {
								Set<Listitem> lis = s2Course.getSelectedItems();
								for (Listitem li : lis) {
									int courseRg = Integer.parseInt((String)li.getValue());
									UniLog.log1("courseRg:%d", courseRg);
									JSONObject courseJson = getCourseJson(sessionHelper, courseRg,null);
									//courseList.add(courseRg);
									if (courseJson != null){
										courseList.add(courseJson);
									}
								}
								if (courseList.isEmpty()) {
									showErrMsg(gh, "Please choose course");
									event.stopPropagation();
									return;
								}
							}
							else {
								String courseCode = tbCourse.getText();
								if (StringUtils.isBlank(courseCode)) {
									showErrMsg(gh, "Please input course code");
									event.stopPropagation();
									return;
								}
								try {
									//int courseRg = getCourseRg(courseCode);
									//if (courseRg > 0) courseList.add(courseRg);
									JSONObject courseJson = getCourseJson(sessionHelper,null,courseCode);
									if (courseJson == null) {
										showErrMsg(gh, "Invalid course");
										event.stopPropagation();
										return;
									}
									else {
										courseList.add(courseJson);
									}
								}
								catch (Exception e) {
									ZkUtil.showErrMsg(e.getMessage());
									return;
								}
							}

							try {
								Pair<Integer, Integer> p = addToCourse(sessionHelper, courseList, studentList);
								if (p.getRight() > 0)
									ZkUtil.msg("%d record inserted, %d record ignored", p.getLeft(), p.getRight());
								else
									ZkUtil.msg("%d record inserted", p.getLeft());
								if (biBase != null) {
									biBase.getBr().refetchCurrent();
									biBase.bindCellCollection(biBase.getBr(), curMode);
								}
								else {
									Button btCancel = (Button) btAddToCourse.getParent().query("#" + btAddToCourse.getId() + "BatchCancel");
									if (btCancel != null)
										Events.echoEvent(Events.ON_CLICK, btCancel, null);
								}
							}
							catch (Exception e) {
								ZkUtil.showErrMsg(e.getMessage());
							}
							break;
						default:
							break;
						}
						event.stopPropagation();
						Events.postEvent(Events.ON_CLOSE, event.getTarget(), null);
					}
		});
		dlg.setWidth("500px");
		dlg.setStyle("max-width:100%");
		dlg.addEventListener("onDelayInit", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				ZkUtil.setupSelect2(s2Course, true, false);
			}
		});
		dlg.addEventListener(Events.ON_CLOSE, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("Close");
				event.stopPropagation();
				//destory select2 listbox
				Clients.evalJavaScript(String.format("$('#%s').select2('destroy')", s2Course.getUuid()));
				new ZkBiAbstractLongOp(dlg, "", 100){
					@Override
					public ReturnMsg longOp() {
						dlg.onClose();
						return null;
					}
				};
			}
		});
		dlg.doHighlighted();
		ZkUtil.delayPostEvent("onDelayInit", dlg, null, 100);
	}

	private Map<Integer, String> getBatchStudentMap(BiResult result) {
		final Map<Integer, String> studentMap = new LinkedHashMap<Integer, String>();
		Set selection = listModelList.getSelection();
       	for (Iterator it = selection.iterator(); it.hasNext();) {
       		Object o = it.next();
         	Object ts = o;
          	if (ts instanceof TrStatFilter)
            	ts = ((TrStatFilter)ts).getTrStatIdx();
       		CellCollection cc = result.getRowCollectionO(ts);
       		int rg = cc.getCellInt("essd_rg");
       		String cardNo = cc.getCellString("essd_cardno");
       		String name = cc.getCellString("essd_name");
       		UniLog.log1("rg:%d, cardNo:%s, name:%s", rg, cardNo, name);
       		studentMap.put(rg, cardNo);
       	}
       	return studentMap;
	}
}
