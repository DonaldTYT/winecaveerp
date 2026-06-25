package com.kikyosoft.utils;

import com.kikyosoft.parser.Condition;

public class ConditionUtil_NotUse {
	Condition cond;
	public ConditionUtil_NotUse(Condition p_initCond) {
		cond = p_initCond;
	}
	public ConditionUtil_NotUse and(Condition p_cond) throws Exception {
		if(cond == null) cond = p_cond; else {
			cond = new Condition(cond,Condition.LOGIC_OP_AND,p_cond);
		}
		return(this);
	}
	public ConditionUtil_NotUse or(Condition p_cond) throws Exception {
		if(cond == null) cond = p_cond; else {
			cond = new Condition(cond,Condition.LOGIC_OP_OR,p_cond);
		}
		return(this);
	}

	public ConditionUtil_NotUse not(Condition p_cond) throws Exception {
		if(cond == null) cond = p_cond; else {
			cond = new Condition(Condition.LOGIC_OP_NOT,cond);
		}
		return(this);
	}
	
	public Condition toCondition() {
		return(cond);
	}
}
