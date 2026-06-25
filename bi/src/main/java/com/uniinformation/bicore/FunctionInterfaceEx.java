package com.uniinformation.bicore;

import java.util.Vector;

import com.uniinformation.cell.Cell;
import com.uniinformation.utils.exprpar.FunctionInterface;

public class FunctionInterfaceEx implements FunctionInterface {
	
	Vector orgArgs;
	FunctionInterface orgInterface;
	
	public FunctionInterfaceEx (FunctionInterface p_orgFunctionInterface, Vector p_args) {
		orgArgs = p_args;
		orgInterface = p_orgFunctionInterface;
	}

	@Override
	public Object evalFunction(String p_functName, Vector p_args) throws Exception {
		if(p_functName.equals("getParam")) {
			int idx = Cell.objectToInt(p_args.get(0));
			return(orgArgs.get(idx));
		} else if(p_functName.equals("paramCount")) {
			if(orgArgs == null) return(0);
			return(orgArgs.size());
		} else return orgInterface.evalFunction(p_functName, p_args);
	}

}
