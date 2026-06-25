package com.uniinformation.jxapp.erpv4ext;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.exprpar.FunctionInterface;
import com.uniinformation.utils.exprpar.Parser;
import com.uniinformation.utils.exprpar.VariableInterface;
import com.uniinformation.zkbi.erpv4ext.ZkBiComposerPaymentGen;

public class PaymentMaster extends JxZkBiBase {

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
	}
	
	static public String makePmiXdesc( String type,String code,SelectUtil su) throws Exception {
		String str = null;
		TableRec tr = null;
		if (StringUtils.equals(type, ZkBiComposerPaymentGen.PAYMENT_TYPE_INCOME))
					tr = su.getQueryResult("select inci_compdesc from incomeitem where inci_code = ?", new Wherecl().appendArgument(code));
				else if (StringUtils.equals(type, ZkBiComposerPaymentGen.PAYMENT_TYPE_DEDUCTION))
					tr = su.getQueryResult("select deci_compdesc from deductionitem where deci_code = ?", new Wherecl().appendArgument(code));
				else if (StringUtils.equals(type, ZkBiComposerPaymentGen.PAYMENT_TYPE_PENSION))
					tr = su.getQueryResult("select peni_compdesc from pensionitem where peni_code = ?", new Wherecl().appendArgument(code));
				if (tr != null && tr.getRecordCount() > 0) return( tr.getFieldString(0, 0));
		return(str);
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		try {
			SelectUtil su = p_br.getSelectUtil();
			Vector<BiCellCollection> recs = p_br.getSubLinkResult("erpv4ext.PaymentItem");
			for (BiCellCollection cc : recs) {
				String code = cc.getString("pmi_code");
				String type = cc.getString("pmi_type");
				String flag = cc.getString("pmi_flag");
				UniLog.log1("code:%s, type%s, flag:%s", code, type, flag);
				TableRec tr = null;
				if (StringUtils.equals(type, ZkBiComposerPaymentGen.PAYMENT_TYPE_INCOME))
					tr = su.getQueryResult("select inci_compdesc from incomeitem where inci_code = ?", new Wherecl().appendArgument(code));
				else if (StringUtils.equals(type, ZkBiComposerPaymentGen.PAYMENT_TYPE_DEDUCTION))
					tr = su.getQueryResult("select deci_compdesc from deductionitem where deci_code = ?", new Wherecl().appendArgument(code));
				else if (StringUtils.equals(type, ZkBiComposerPaymentGen.PAYMENT_TYPE_PENSION))
					tr = su.getQueryResult("select peni_compdesc from pensionitem where peni_code = ?", new Wherecl().appendArgument(code));
				if (tr != null && tr.getRecordCount() > 0)
					cc.getCell("pmi_xdesc").set(tr.getFieldString(0, 0));

				char[] flagc = ZkBiComposerPaymentGen.flagStrToCharArray(flag);
				char c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_TAXIBLE];
				cc.getCell("pmi_xtax").set(isAddDelFlagC(c));
				c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_RELAVENT];
				cc.getCell("pmi_xeoa").set(isAddDelFlagC(c));

				String xcon = "";
				c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_EPENSION];
				if (isAddDelFlagC(c))
					xcon = "E";
				c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_RPENSION];
				if (isAddDelFlagC(c))
					xcon = "R";
				if (StringUtils.isNotBlank(xcon)) {
					cc.getCell("pmi_xcon").set(xcon);
					cc.getCell("pmi_pension").setMode(Cell.VMODE_NORMAL);
				} else
					cc.getCell("pmi_pension").setMode(Cell.VMODE_DISPONLY);
				
				c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_RINCOME];
				cc.getCell("pmi_rincome").setMode(isAddDelFlagC(c) ? Cell.VMODE_NORMAL : Cell.VMODE_DISPONLY);
				c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_OINCOME];
				cc.getCell("pmi_oincome").setMode(isAddDelFlagC(c) ? Cell.VMODE_NORMAL : Cell.VMODE_DISPONLY);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new PaymentItemGetItemProperty(p_br.getSubLink("erpv4ext.PaymentItem"))
		);	
	}

	private class PaymentItemGetItemProperty extends BiGetItemProperty {

		public PaymentItemGetItemProperty(BiResult p_br) {
			super(p_br);
		}

		@Override
		public String getColumnWidth(Object p_v, int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			if (o instanceof BiColumn && StringUtils.equalsAny(((BiColumn)o).getLabel(), "pmi_code", "pmi_xdesc", "pmi_xcon"))
				return "hflex=min";
			return super.getColumnWidth(p_v, p_col);
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_VALUE_CHANGED) {
				try {
					if (StringUtils.equalsAny(bcc.getCellLabel(), "pmi_rincome", "pmi_oincome", "pmi_pension")) {
						cl.getCell("pmi_override").set(true);
						reloadOrRecalPaymentItem(cl);
					} else if (StringUtils.equals(bcc.getCellLabel(), "pmi_override"))
						reloadOrRecalPaymentItem(cl);
				}
				catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED)
				setDirtyFlag(true);
		}
	}
	
	private boolean isAddDelFlagC(char c) {
		return c == '+' || c == '-';
	}
	
	private void reloadOrRecalPaymentItem(BiCellCollection cl) throws Exception {
		UniLog.log1("reloadOrRecalPaymentItem code:%s", cl.getCellString("pmi_code"));
		if (StringUtils.equals(cl.getString("pmi_type"), ZkBiComposerPaymentGen.PAYMENT_TYPE_PENSION)) {
			if (!cl.getBoolean("pmi_override"))
				recalOnePension(cl);
		} else {
			if (!cl.getBoolean("pmi_override"))
				reloadOneIncomeDeduction(cl);
			recalAllPension();
		}
		recalPayment();
	}
	
	private void recalOnePension(BiCellCollection cl) throws Exception {
		char[] flagc = ZkBiComposerPaymentGen.flagStrToCharArray(cl.getString("pmi_flag"));
		TableRec tr = getBr().getSelectUtil().getQueryResult("select peni_formula from pensionitem where peni_code = ?", new Wherecl().appendArgument(cl.getString("pmi_code")));
		double pension = 0.0;
		if (tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			double tmpf = evalFormula(cl.getCellString("pmi_code"), tr.getFieldString("peni_formula"));
			char c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_EPENSION];
			switch (c) {
				case '+':
					pension = tmpf;
					break;
				case '-':
					pension = -tmpf;
					break;
			}
			c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_RPENSION];
			switch (c) {
				case '+':
					pension = tmpf;
					break;
				case '-':
					pension = -tmpf;
					break;
			}
		}
		cl.getCell("pmi_pension").set(pension);
	}
	
	private void reloadOneIncomeDeduction(BiCellCollection cl) throws Exception {
		char[] flagc = ZkBiComposerPaymentGen.flagStrToCharArray(cl.getString("pmi_flag"));
		TableRec tr = getBr().getSelectUtil().getQueryResult("select * from paymentdet, paymentitemdet where " + 
				"pmd_eid = ? and " + 
				"pmd_date = ? and " + 
				"pmd_stdate = ? and " + 
				"pmd_enddate = ? and " + 
				"pmdi_eid = pmd_eid and " + 
				"pmdi_date = pmd_date and " + 
				"pmdi_stdate = pmd_stdate and " + 
				"pmdi_enddate = pmd_enddate and " + 
				"pmdi_emgstdate = pmd_emgstdate and " + 
				"pmdi_type = ? and " + 
				"pmdi_code = ?", 
				new Wherecl().appendArgument(getBr().getCellString("pm_eid")).appendArgument(getBr().getCellDate("pm_date"))
							.appendArgument(cl.getDate("pmi_stdate")).appendArgument(cl.getDate("pmi_enddate"))
							.appendArgument(cl.getString("pmi_type")).appendArgument(cl.getString("pmi_code")));
		double tmpf = 0.0;
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			tmpf += tr.getFieldDouble("pmdi_amount");
		}
		char c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_RINCOME];
		switch (c) {
			case '+':
				cl.getCell("pmi_rincome").set(tmpf);
				break;
			case '-':
				cl.getCell("pmi_rincome").set(-tmpf);
				break;
		}
		c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_OINCOME];
		switch (c) {
			case '+':
				cl.getCell("pmi_oincome").set(tmpf);
				break;
			case '-':
				cl.getCell("pmi_oincome").set(-tmpf);
				break;
		}
	}
	
	private void recalAllPension() throws Exception {
		double totRIncome = 0.0, totOIncome = 0.0;
		Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.PaymentItem");
		for (BiCellCollection cc : recs) {
			totRIncome += cc.getDouble("pmi_rincome");
			totOIncome += cc.getDouble("pmi_oincome");
		}
		getBr().getCell("pm_rincome").set(totRIncome);
		getBr().getCell("pm_oincome").set(totOIncome);
		for (BiCellCollection cc : recs) {
			if (StringUtils.equals(cc.getString("pmi_type"), ZkBiComposerPaymentGen.PAYMENT_TYPE_PENSION))
				recalOnePension(cc);
		}
	}
	
	private void recalPayment() throws Exception {
		double totEPension = 0.0;
		double totRPension = 0.0;
		double totTax = 0.0;
		double totEoa = 0.0;
		Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.PaymentItem");
		for (BiCellCollection cc : recs) {
			char[] flagc = ZkBiComposerPaymentGen.flagStrToCharArray(cc.getString("pmi_flag"));
			char c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_EPENSION];
			if (isAddDelFlagC(c))
				totEPension += cc.getCellDouble("pmi_pension");
			c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_RPENSION];
			if (isAddDelFlagC(c))
				totRPension += cc.getCellDouble("pmi_pension");
			c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_TAXIBLE];
			if (isAddDelFlagC(c))
				totTax += cc.getCellDouble("pmi_pension") + cc.getCellDouble("pmi_rincome") + cc.getCellDouble("pmi_oincome");
			c = flagc[ZkBiComposerPaymentGen.PAYAMOUNT_IDX_RELAVENT];
			if (isAddDelFlagC(c))
				totEoa += cc.getCellDouble("pmi_pension") + cc.getCellDouble("pmi_rincome") + cc.getCellDouble("pmi_oincome");
		}
		getBr().getCell("pm_epension").set(totEPension);
		getBr().getCell("pm_rpension").set(totRPension);
		getBr().getCell("pm_taxible").set(totTax);
		getBr().getCell("pm_eoawages").set(totEoa);
	}

	private double evalFormula(String code, String formula) throws Exception {
		Parser parser = new Parser(0,formula);
		MyParserCallback cb = new MyParserCallback();
		parser.setFunctInterface(cb);
		parser.setVarInterface(cb);
		double r = (Double)parser.evaluate();
		UniLog.log1("code:%s, formula:%s, r:%f", code, formula, r);
		return r;
	}

	private class MyParserCallback implements FunctionInterface, VariableInterface {

		public Object evalVariable1(String p_varname) throws Exception {
			if (StringUtils.equals(p_varname, "rincome"))
				return getBr().getCellDouble("pm_rincome");
			else if (StringUtils.equals(p_varname, "oincome"))
				return getBr().getCellDouble("pm_oincome");
			else if (StringUtils.equals(p_varname, "joindate"))
				return getBr().getCellDate("em_stdate").getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "penddate"))
				return getBr().getCellDate("pm_edate").getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "pstdate"))
				return getBr().getCellDate("pm_date").getTime() / 86400000;
			else if (StringUtils.equals(p_varname, "over18date")) {
				Date birth = getBr().getCellDate("em_birth");
				if (!DateUtil.isValid(birth)) {
					UniLog.log("Error getting Employee Brithday");
					return 0;
				}
				int mm = DateUtil.getMonth(birth);
				int yy = DateUtil.getYear(birth);
				int dd = DateUtil.getDay(birth);
				if (mm == 2 && dd == 29)
					dd = 28;
				yy += 18;
				Date tmpdate1 = DateUtil.dateTimeStrToDate(String.format("%04d/%02d/%02d", yy, mm, dd));
				return tmpdate1.getTime() / 86400000;
			}
			UniLog.log1("Variable %s not found", p_varname);
			return null;
		}

		@Override
		public Object evalVariable(String p_varname) throws Exception {
			Object r = evalVariable1(p_varname);
			UniLog.log1("p_varname:%s, r:%s", p_varname, r);
			return r;
		}

		@Override
		public Object evalVariable(String p_varname, int p_idx) throws Exception {
			UniLog.log1("Variable %s(%d) not found", p_varname, p_idx);
			return null;
		}

		@Override
		public Object evalFunction(String p_functName, Vector p_args) throws Exception {
			if (StringUtils.equals(p_functName, "xyz"))
				return 333.0;
			else if (StringUtils.equals(p_functName, "if")) {
				if (p_args.size() != 3) 
					return null;
				if (((Boolean) p_args.get(0)).booleanValue())
					return p_args.get(1);
				else
					return p_args.get(2);
			} else if (StringUtils.equals(p_functName, "f_penddate")) {
				Date edate = getBr().getCellDate("pm_edate");
				int cc = ((Double)p_args.get(0)).intValue();
				Date tmpdate1;
				if (cc < 0)
					tmpdate1 = DateUtil.monthEnd(DateUtil.prevmonth(edate, -cc));
				else if (cc > 0)
					tmpdate1 = DateUtil.monthEnd(DateUtil.nextmonth(edate, cc));
				else
					tmpdate1 = edate;
				return tmpdate1.getTime() / 86400000;
			} else if (StringUtils.equals(p_functName, "f_rincome")) {
				String eid = getBr().getCellString("pm_eid");
				Date date = getBr().getCellDate("pm_date");
				int cc = ((Double)p_args.get(0)).intValue();
				Date tmpdate1;
				if (cc < 0) 
					tmpdate1 = DateUtil.monthStart(DateUtil.prevmonth(date, -cc));
				else if(cc > 0) 
					tmpdate1 = DateUtil.monthStart(DateUtil.nextmonth(date, cc));
				else 
					tmpdate1 = date;
				double tmpf = 0.0;
				TableRec tr = getBr().getSelectUtil().getQueryResult("select pm_rincome from paymentmaster where pm_eid = ? and pm_date = ?", 
						new Wherecl().appendArgument(eid).appendArgument(tmpdate1));
				if (tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					tmpf = tr.getFieldDouble("pm_rincome");
				}
				return tmpf;
			}
			UniLog.log1("Function %s not found", p_functName);
			return null;
		}

		@Override
		public Object evalVariableRelative(String p_varname, int p_idx) throws Exception {
			throw new Exception("evalVariableRelative not supported");
		}
	}
}
