package com.uniinformation.clerpmulti;

import java.util.Date;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.webcore.SessionHelper;

public interface RefillHzInterface {
	public void runRefill(SessionHelper p_sh,Date p_date) throws Exception;
}
