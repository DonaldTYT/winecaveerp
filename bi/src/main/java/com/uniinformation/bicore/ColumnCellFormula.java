package com.uniinformation.bicore;

import java.util.Hashtable;

import com.uniinformation.bicore.bischema.BiResultExcelSheet;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellFormula;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.exprpar.Parser;

public class ColumnCellFormula extends CellFormula {
	final private static boolean fEnable = true;
	ColumnCell cc;
	public ColumnCellFormula(String p_formula, ColumnCell p_cc) {
		super(p_formula, p_cc.cl);
		cc = p_cc;
	}

	@Override
	protected void initParser()
	{
		if (fEnable) {
			Hashtable<String,com.uniinformation.utils.exprpar.Parser> formulaParserHash = ((BiCellCollection) collection).getBr().formulaParserHash;
			if(formulaParserHash == null) {
				formulaParserHash = new Hashtable<String,com.uniinformation.utils.exprpar.Parser> ();
				((BiCellCollection) collection).getBr().formulaParserHash = formulaParserHash;
			}
			parser = formulaParserHash.get(formula);
			if(parser == null) {
				parser = new Parser(collection.getCompareMode(),formula);
				formulaParserHash.put(formula,parser);
			}
		}
		else {
			super.initParser();
		}
	}

	@Override
	public Object eval() throws CellException
	{
		if (fEnable) {
			if(cc.br instanceof BiResultExcelSheet) {
				if(cc.br.inLoadingRec) {
					if(cc.col.getField() != null) {
						return(cc.getObject());
					}
				}
			}
			Object o = null;
			if(parser == null) initParser();
			synchronized(parser) {
				try {
					parser.setFunctInterface(collection);
					parser.setVarInterface(collection);
					o = parser.evaluate();
				} catch (Exception e) {
					//andrew230217: BiCellCollection.initFormula p_formula.eval() trigger false alarm when formula has multiple expression. it's due to boolean is represented by double 0.0. 
					if(e instanceof CellException) {
						if(((CellException) e).getExceptionType() == CellException.CELLEXCEPTION_EVAL_ERROR) {
							throw((CellException) e);
						}
					}
					UniLog.log1("error:" + e.getMessage() +" formula:" + formula);
					//UniLog.log(e);
				}
			}
			return(o);
		}
		else {
			return super.eval();
		}
	}
}
