package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiField;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.jx.zk.ZkJxCellValueMapper;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.jx.zk.ZkJxQueryInput.EventListenerCallback;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.utils.whereclpar.Expression;
import com.uniinformation.utils.whereclpar.Variable;
import com.uniinformation.webcore.SessionHelper;
import com.kyoko.common.*;

public class QueryConditionRow extends Hlayout {
//		BiResult result=null;
		static public enum ConditionOp { 
			EQ(Condition.COMPARE_OP_EQ), NE(Condition.COMPARE_OP_NE), GT(Condition.COMPARE_OP_GT), LT(Condition.COMPARE_OP_LT), 
			GE(Condition.COMPARE_OP_GE), LE(Condition.COMPARE_OP_LE), IN_ITEMLIST(Condition.COMPARE_OP_IN_ITEMLIST), 
			NOTIN_ITEMLIST(Condition.COMPARE_OP_NOTIN_ITEMLIST), BETWEEN(Condition.COMPARE_OP_BETWEEN), NOT_BETWEEN(Condition.COMPARE_OP_NOT_BETWEEN),
			LK(Condition.COMPARE_OP_LK), NLK(Condition.COMPARE_OP_NLK), MA(Condition.COMPARE_OP_MA), NM(Condition.COMPARE_OP_NM),
			IS_NULL(Condition.COMPARE_OP_IS_NULL), IS_NOT_NULL(Condition.COMPARE_OP_IS_NOT_NULL),
			IS_BLANK(0), IS_NOT_BLANK(0), REGEXP(Condition.COMPARE_OP_REGEXP), NOT_REGEXP(Condition.COMPARE_OP_NOT_REGEXP);
			int id;
			ConditionOp(int id) {
				this.id = id;
			}
			static ConditionOp findConditionOp(int id) {
				for (ConditionOp op : ConditionOp.values()) {
					if (op.id == id)
						return op;
				}
				return null;
			}
		}
		static Map<ConditionOp, Pair<String, String>> operatorMap = new LinkedHashMap<ConditionOp, Pair<String, String>>(){{
				put(ConditionOp.EQ, Pair.of("=", "field value = keyword"));
				put(ConditionOp.NE, Pair.of("<>", "field value <> keyword"));
				put(ConditionOp.GT, Pair.of(">", "field value > keyword"));
				put(ConditionOp.LT, Pair.of("<", "field value < keyword"));
				put(ConditionOp.GE, Pair.of(">=", "field value >= keyword"));
				put(ConditionOp.LE, Pair.of("<=", "field value <= keyword"));
				put(ConditionOp.IN_ITEMLIST, Pair.of("in list of", "field value in keyword1,keyword2,..."));
				put(ConditionOp.NOTIN_ITEMLIST, Pair.of("not in list of", "field value not in keyword1,keyword2,..."));
				put(ConditionOp.BETWEEN, Pair.of("between", "field value in between keyword1 and keyword2"));
				put(ConditionOp.NOT_BETWEEN, Pair.of("not between", "field value not between keyword1 and keyword2"));
				//put(Condition.COMPARE_OP_MA, Pair.of("matches", "field value matches keyword"));
				//put(Condition.COMPARE_OP_NM, Pair.of("not matches", "field value not matches keyword"));
				put(ConditionOp.LK, Pair.of("like", "field value like keyword"));
				put(ConditionOp.NLK, Pair.of("not like", "field value not like keyword"));
				//put(Condition.COMPARE_OP_IS_NULL, Pair.of("is null", "field value is null"));
				//put(Condition.COMPARE_OP_IS_NOT_NULL, Pair.of("is not null", "field value is not null"));
				put(ConditionOp.IS_BLANK, Pair.of("is blank", "field value is blank"));
				put(ConditionOp.IS_NOT_BLANK, Pair.of("is not blank", "field value is not blank"));
				put(ConditionOp.REGEXP, Pair.of("contains", "field value contains keyword"));
				put(ConditionOp.NOT_REGEXP, Pair.of("not contains", "field value not contains keyword"));
			}};
		
			class QueryGetItemProperty extends AbstractGetItemProperty
			{
				BiField bifd;
				SelectUtil su;
				List<Object> qList = null;
				QueryGetItemProperty(BiField p_fd,SelectUtil p_su) {
					bifd = p_fd;
					su = p_su;
				}
		
				@Override
				public int getColumnCount(Object item) {
					// TODO Auto-generated method stub
					return 1;
				}

				@Override
				public String getString(Object item) {
					// TODO Auto-generated method stub
					return(item.toString());
				}

				@Override
				public Object getRow(int p_row) {
					// TODO Auto-generated method stub
					return(qList.get(p_row));
				}

				@Override
				public int getRowCount() {
					// TODO Auto-generated method stub
					if(qList == null) {
						qList = bifd.getUniqueList(su,null);
					}
					return (qList == null ? 0:qList.size());
				}

				@Override
				public int getIndexOf(Object item) {
					// TODO Auto-generated method stub
					return(qList.indexOf(item));
				}

				@Override
				public int getRowWidth() {
					// TODO Auto-generated method stub
					return 0;
				}
		
			}
	
		
		
		
			InputElement buildInputComp(BiResult p_result,BiColumn bc, int p_type,int p_maxlen) {
				InputElement ie;
				SessionHelper sh = p_result != null ? p_result.getSessionHelper() : null;
				if (p_type < 0) {
					if (bc.getColumnType().trim().equals("date")){
						ie = new ZkJxQueryInput();
						((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_DATE, sh);
					} else if (bc.getColumnType().trim().equals("datetime")){
						ie = new ZkJxQueryInput();
						((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_DATETIME, sh);
					} else if (bc.getColumnType().trim().equals("time")){
						ie = new Timebox();
					} else if (bc.getColumnType().trim().matches("integer|serial")){
						ie = new ZkJxQueryInput();
						((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_INTEGER, sh);
					} else if (bc.getColumnType().trim().matches("float|double|money")){
						ie = new ZkJxQueryInput();
						((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_FLOAT, sh);
					} else {
						ie = new ZkJxQueryInput();
						((ZkJxQueryInput)ie).setType(ZkJxQueryInput.TYPE_STRING, sh);
						if(bc != null && p_result != null) {
							if(bc.getField() != null) {
								((ZkJxQueryInput)ie).setGiPi(new QueryGetItemProperty(bc.getField(),p_result.getSelectUtil()));
							}
						}
						if(p_maxlen > 0) {
							ie.setMaxlength(p_maxlen);
						} else {
							if(bc != null) ie.setMaxlength(bc.getColumnLength());
						}
					}
				} else {
					ie = new ZkJxQueryInput();
					((ZkJxQueryInput)ie).setType(p_type, sh);
					if(p_maxlen > 0) {
							ie.setMaxlength(p_maxlen);
					} 
				}
	   			return ie;
			}
			private boolean isCharColumnType(String type) {
				return (type.equals("char")
						|| type.equals("pickinput")
						|| type.equals("combobox")
						|| type.equals("list")
						|| type.equals("checkbox")
						|| type.equals("radio"));
			}
			void visibleInputComps(Combobox cbOperator, InputElement valComp0, InputElement valComp1, Label valAnd, InputElement valComp2) {
				if (cbOperator.getSelectedIndex() >= 0) {
					cbOperator.setVisible(true);
					switch ((ConditionOp)cbOperator.getSelectedItem().getValue()) {
					case BETWEEN:
					case NOT_BETWEEN:
						valComp0.setVisible(true);
						valComp1.setVisible(true);
						valComp2.setVisible(false);
						valAnd.setVisible(true);
						break;
					case IS_NULL:
					case IS_NOT_NULL:
					case IS_BLANK:
					case IS_NOT_BLANK:
						valComp0.setVisible(false);
						valComp1.setVisible(false);
						valComp2.setVisible(false);
						valAnd.setVisible(false);
						break;
					case IN_ITEMLIST:
					case NOTIN_ITEMLIST:
					case MA:
					case NM:
					case LK:
					case NLK:
					case REGEXP:
					case NOT_REGEXP:
						valComp0.setVisible(false);
						valComp1.setVisible(false);
						valComp2.setVisible(true);
						valAnd.setVisible(false);
						break;
					default:
						valComp0.setVisible(true);
						valComp1.setVisible(false);
						valComp2.setVisible(false);
						valAnd.setVisible(false);
						break;
					}
				} else {
					cbOperator.setVisible(false);
					valComp0.setVisible(false);
					valComp1.setVisible(false);
					valComp2.setVisible(true);
					valAnd.setVisible(false);
					valComp2.setDisabled(true);
					((Hlayout)valComp2.getParent()).setHflex("3");
				}
				final Timer timer = new Timer();
				timer.setPage(cbOperator.getPage());
				timer.setDelay(50);
				timer.setRepeats(false);
				timer.addEventListener(Events.ON_TIMER, new EventListener<Event>(){
					@Override
					public void onEvent(Event event) throws Exception {
//						resizeConditionBlock();
						timer.setRunning(false);
						timer.detach();
					}
				});
				timer.setRunning(true);
			}
	Hlayout createConditionBlockRow(/* final Component win, */ final BiResult br, final BiColumn bc, final Condition condition,List<ConditionOp> opList) {
//		final Vlayout parent = (Vlayout) win.getAttribute("vlRows");
//		final Set<Hlayout> blockRowCompList = blockWinMap.get(win);
//		Div divConnector = null;
//		if (!blockRowCompList.isEmpty())
//			divConnector = createConnector(parent);
//		final Hlayout hlRow = new Hlayout();
//		final Textbox tbLabel = new Textbox();
		final Label tbLabel = new Label();
		final Combobox cbOperator = new Combobox();
		final Hlayout hlValue = new Hlayout();
		int fdtype = -1;
		if(getAttribute("fdtype") != null) {
			String ss = (String) getAttribute("fdtype");
			if(ss.equals("char")) fdtype = Cell.VTYPE_STRING;
			if(ss.equals("date")) fdtype = Cell.VTYPE_DATE;
			if(ss.equals("int")) fdtype = Cell.VTYPE_INT;
			if(ss.equals("boolean")) fdtype = Cell.VTYPE_BOOLEAN;
			if(ss.equals("datetime")) fdtype = Cell.VTYPE_DATETIME;
			if(ss.equals("double")) fdtype = Cell.VTYPE_DOUBLE;
		}
		int maxlen = -1;
		if(getAttribute("maxlen") != null) {
			maxlen = Integer.parseInt((String) getAttribute("maxlen"));
			
		}
		final InputElement valComp0 = buildInputComp(br,bc, fdtype,maxlen);
		final InputElement valComp1 = buildInputComp(br,bc, fdtype,maxlen);
		final InputElement valComp2 = buildInputComp(br,bc, Cell.VTYPE_STRING,-1);
		final Label valAnd = new Label("and");
//		final Toolbarbutton btnDelete = new Toolbarbutton("Delete");
//		this.setParent(parent);
		tbLabel.setParent(this);
		cbOperator.setParent(this);
		hlValue.setParent(this);
		valComp0.setParent(hlValue);
		valAnd.setParent(hlValue);
		valComp1.setParent(hlValue);
		valComp2.setParent(hlValue);
//		btnDelete.setParent(this);
		this.setHflex("1");
		tbLabel.setHflex("1");
		cbOperator.setHflex("1");
		hlValue.setHflex("2");
		valComp0.setHflex("1");
		valComp1.setHflex("1");
		valComp2.setHflex("1");
//		tbLabel.setReadonly(true);
			valAnd.setStyle("font-size:8pt");
		if(opList != null)	 {
			for(final ConditionOp op : opList) {
				final Pair<String,String> entry = operatorMap.get(op);
				cbOperator.appendChild(new Comboitem(){{
					setValue(op);
					setLabel(entry.getLeft());
					setDescription(entry.getRight());
				}});
				
			}
		} else {
		for (final Map.Entry<ConditionOp, Pair<String, String>> entry : operatorMap.entrySet()) {
			boolean flag = false;
			switch (entry.getKey()) {
			case LK:
			case NLK:
			case IS_BLANK:
			case IS_NOT_BLANK:
			case REGEXP:
			case NOT_REGEXP:
				if (isCharColumnType(bc.getColumnType().trim()))
					flag = true;
				break;
			case GT:
			case LT:
			case GE:
			case LE:
			case BETWEEN:
			case NOT_BETWEEN:
				if (!isCharColumnType(bc.getColumnType().trim()))
					flag = true;
				break;
			default:
				flag = true;
				break;
			}
			if (flag) {
				cbOperator.appendChild(new Comboitem(){{
					setValue(entry.getKey());
					setLabel(entry.getValue().getLeft());
					setDescription(entry.getValue().getRight());
				}});
			}
		}
		}
		cbOperator.setSelectedIndex(0);
		cbOperator.setReadonly(true);
			visibleInputComps(cbOperator, valComp0, valComp1, valAnd, valComp2);
		if(getAttribute("label") == null) {
			tbLabel.setValue(bc.getEngName());
		} else {
			tbLabel.setValue((String) getAttribute("label"));
		}
		this.setAttribute("oldCondition", condition);
//		hlRow.setAttribute("connectorComp", divConnector);
		this.setAttribute("operatorComp", cbOperator);
		this.setAttribute("labelComp", tbLabel);
		this.setAttribute("valueComp0", valComp0);
		this.setAttribute("valueComp1", valComp1);
		this.setAttribute("valueComp2", valComp2);
		this.setAttribute("valueAnd", valAnd);
		this.setAttribute("biResult", br);
		this.setAttribute("biColumn", bc);
		if(getAttribute("fdname") == null) {
			this.setAttribute("fdname", bc.getLabel());
		}
//		blockRowCompList.add(hlRow);
//		btnDelete.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
//			@Override
//			public void onEvent(Event event) throws Exception {
//				Component connectorComp = (Component) getAttribute("connectorComp");
//				if (connectorComp != null)
//					parent.removeChild(connectorComp);
//				parent.removeChild(QueryConditionRow.this);
//				blockRowCompList.remove(hlRow);
//				if (!blockRowCompList.isEmpty()) {
//					connectorComp = (Component) blockRowCompList.iterator().next().getAttribute("connectorComp");
//					if (connectorComp != null)
//						parent.removeChild(connectorComp);
//				}
//				makeCustomCondition();
//			}
//		});
		cbOperator.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("cbOperator " + event);
				visibleInputComps(cbOperator, valComp0, valComp1, valAnd, valComp2);
				makeCustomCondition();
			}
		});
		InputElement[] vcs = new InputElement[]{valComp0, valComp1, valComp2};
		for (InputElement vc : vcs) {
			if (vc instanceof ZkJxQueryInput) {
				((ZkJxQueryInput)vc).setEventListenerCallback(new EventListenerCallback(){
					@Override
					public void callback(Event event) throws Exception {
						UniLog.log("vc ZkJxQueryInput " + event);
						makeCustomCondition();
					}
				});
			} else {
				vc.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log("vc " + event);
						makeCustomCondition();
					}
				});
			}
		}
		return this;
	}
	
	List<Expression> getInputExpression(InputElement valueComp, InputElement valueComp0) {
		Object obj;
		if (valueComp instanceof ZkJxQueryInput)
			obj = ((ZkJxQueryInput)valueComp).getQueryObject();
			else
				obj = valueComp.getText();
//		UniLog.log("getInputExpression obj:" + obj);
		List<Expression> list = new ArrayList<Expression>();
		if (obj != null && obj instanceof String) {
			String[] ss = ((String)obj).split(",");
			for (String s : ss) {
				Object o = null;
				if (valueComp0 instanceof ZkJxQueryInput)
					o = ((ZkJxQueryInput)valueComp0).getQueryObjectFromText(s);
				else
					o = s;
				if (o != null) {
					if (o instanceof Integer)
						list.add(new Expression((Integer)o));
					else if (o instanceof Date)
						list.add(new Expression((Date)o));
					else if (o instanceof Float)
						list.add(new Expression((Float)o));
					else if (o instanceof Double)
						list.add(new Expression((Double)o));
					else 
						list.add(new Expression(o.toString()));
				}
			}
		}
		return list;
	}
	
	
			Expression getInputExpression(InputElement valueComp) throws Exception {
				Object obj;
				obj = ((ZkJxQueryInput)valueComp).getQueryObject();
				UniLog.log("getInputExpression obj:" + obj);
				if (obj != null) {
					if (obj instanceof Integer)
						return new Expression((Integer)obj);
					else if (obj instanceof Date)
						return new Expression((Date)obj);
					else if (obj instanceof Float)
						return new Expression((Float)obj);
					else if (obj instanceof Double)
						return new Expression((Double)obj);
					else 
						return new Expression(obj.toString());
				} else
					return new Expression("");
			}
			Condition getInputCondition(/* BiColumn bc, */ String p_fdname,InputElement valueComp0, Combobox cbOperator, InputElement valueComp1, InputElement valueComp2, Condition oldCondition) throws Exception {
				Expression fExp = new Expression(new Variable(p_fdname,null,null));
				if (cbOperator.getSelectedIndex() >= 0) {
					ConditionOp operator = (ConditionOp)cbOperator.getSelectedItem().getValue();
					switch (operator) {
					case BETWEEN:
					case NOT_BETWEEN:
						Expression vExp0 = getInputExpression(valueComp0);
						Expression vExp1 = getInputExpression(valueComp1);
						return new Condition(fExp, operator.id, vExp0, vExp1);
					case IN_ITEMLIST:
					case NOTIN_ITEMLIST:
						return new Condition(fExp, operator.id, getInputExpression(valueComp2, valueComp0));
					case MA:
					case NM:
					case LK:
					case NLK:
					case REGEXP:
					case NOT_REGEXP:
						return new Condition(fExp, operator.id, getInputExpression(valueComp2));
					case IS_NOT_NULL:
					case IS_NULL:
						return new Condition(fExp, operator.id);
					case IS_BLANK:
						return new Condition(fExp, Condition.COMPARE_OP_EQ, new Expression(""));
					case IS_NOT_BLANK:
						return new Condition(fExp, Condition.COMPARE_OP_NE, new Expression(""));
					default:
						return new Condition(fExp, operator.id, getInputExpression(valueComp0));
					}
				} else 
					return oldCondition;
			}
	
		Condition getCondition() throws Exception {
//					BiResult br = (BiResult) getAttribute("biResult");
//					BiColumn bc = (BiColumn) getAttribute("biColumn");
					String fdname = (String) getAttribute("fdname");
					Combobox opComp = (Combobox) getAttribute("operatorComp");
					InputElement valueComp0 = (InputElement) getAttribute("valueComp0");
					InputElement valueComp1 = (InputElement) getAttribute("valueComp1");
					InputElement valueComp2 = (InputElement) getAttribute("valueComp2");
					Condition oldCondition = (Condition) getAttribute("oldCondition");
					Condition condition = getInputCondition(fdname, valueComp0, opComp, valueComp1, valueComp2, oldCondition);
					return(condition);
			
		}
			void makeCustomCondition() throws Exception {
					
					UniLog.log("HAHA convert input to custom condition here");
					ZkJxCellValueMapper cm = ((ZkJxCellValueMapper) getAttribute("CellValueMapper"));
					if(cm != null) {
						UniLog.log("Condition = " + getCondition().toString());
						cm.validateChange(getCondition().toString());
					}
					/*
				try {
					Condition winCondition = null;
					for (Map.Entry<Window, Set<Hlayout>> entry : blockWinMap.entrySet()) {
						Div divConnector = (Div) entry.getKey().getAttribute("connectorComp");
						Integer winConnector = divConnector != null ? (Integer) divConnector.getAttribute("connector") : null;
						UniLog.log("win connector: " + winConnector);
						Condition rowCondition = null;
						for (Hlayout hlRow : entry.getValue()) {
							BiResult br = (BiResult) hlRow.getAttribute("biResult");
							BiColumn bc = (BiColumn) hlRow.getAttribute("biColumn");
							Combobox opComp = (Combobox) hlRow.getAttribute("operatorComp");
							InputElement valueComp0 = (InputElement) hlRow.getAttribute("valueComp0");
							InputElement valueComp1 = (InputElement) hlRow.getAttribute("valueComp1");
							InputElement valueComp2 = (InputElement) hlRow.getAttribute("valueComp2");
							Condition oldCondition = (Condition) hlRow.getAttribute("oldCondition");
							divConnector = (Div) hlRow.getAttribute("connectorComp");
							Expression fExp = new Expression(new Variable(bc.getLabel(),null,null));
							Condition condition = getInputCondition(bc, valueComp0, opComp, valueComp1, valueComp2, oldCondition);
							Integer connector = divConnector != null ? (Integer) divConnector.getAttribute("connector") : null;
							UniLog.log("hlrow condition: " + condition + ",connector:" + connector);
							if (rowCondition != null) {
								if (connector > 0 && condition != null)
									rowCondition = new Condition(rowCondition, connector, condition);
							} else
								rowCondition = condition;
						}
						UniLog.log("hlrows condition: " + rowCondition);
						if (rowCondition != null) {
							if (winCondition != null) {
								if (winConnector > 0)
									winCondition = new Condition(winCondition, winConnector, rowCondition);
							} else
								winCondition = rowCondition;
						}
					}
					UniLog.log("win condition: " + winCondition);
					tbCustomParam.setText(winCondition != null ? winCondition.toString() : ""); 
				} catch (Exception e) {
					e.printStackTrace();
					tbCustomParam.setText(""); 
				}
					*/
			}
}
