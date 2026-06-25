package com.uniinformation.bicore.propmgmtpro;

import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection;
import com.uniinformation.dynamic.propmgmtpro.PrintMeetingTopic;

public class PropmgmtProCellCollection extends PropertyMgmtCellCollection {
	private enum FuncName { 
		FUNC_decimalToLetter, FUNC_voteListDesc,
		NOT_DEFINED }

	public PropmgmtProCellCollection(BiCellCollection p_col, BiResult p_br) {
		super(p_col, p_br);
	}

	@Override
	public Object evalFunction(String p_fname, Vector p_args) throws Exception {
		//UniLog.log1("p_fname:%s", p_fname);
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		switch (funcName){
		case FUNC_decimalToLetter: {
			Double d = (Double) p_args.get(0);
			if (d == null || d <= 0 || !Double.isFinite(d))
				return "";
			return decimalToLetter((int)(double)d);
			}
		case FUNC_voteListDesc:
			return getVoteListDesc((String) p_args.get(0));
		}
		return super.evalFunction(p_fname, p_args);
	}

	public static String decimalToLetter(int number) {
        if (number <= 0)
            return "";
        StringBuilder result = new StringBuilder();
        while (number > 0) {
            number--;  
            char digit = (char) ('A' + (number % 26));
            result.append(digit);
            number /= 26;
        }
        return result.reverse().toString();
    }

	public static int letterToDecimal(String s) throws Exception {
	    if (StringUtils.isBlank(s))
	    	return 0;
	    int result = 0;
	    for (int i = 0; i < s.length(); i++) {
	        char c = s.charAt(i);
	        if (c < 'A' || c > 'Z')
	        	throw new Exception("Invalid Char");
	        int value = c - 'A' + 1;
	        result = result * 26 + value;
	    }
	    return result;
	}

	public static String getVoteDesc(String s) {
		if (StringUtils.length(s) != 5 || !s.matches("^\\d+$"))
			return "";
		int topicNum = Integer.parseInt(s.substring(0, 2));
		int optionNum = Integer.parseInt(s.substring(2, 4));
		int voteNum = Integer.parseInt(s.substring(4, 5));
		return (optionNum == 0 ? String.valueOf(topicNum) : String.format("%d.%d", topicNum, optionNum))
				+ PrintMeetingTopic.VoteResultMap.entrySet().stream().filter(e -> e.getValue() == voteNum).map(e -> e.getKey()).findFirst().orElse("").replaceAll("單選|多選|統一", "");
	}
	
	public static String getVoteListDesc(String str) {
		if (StringUtils.isBlank(str))
			return "";
		return Arrays.stream(str.split(",")).map(s -> getVoteDesc(s)).collect(Collectors.joining(", "));
	}
}
