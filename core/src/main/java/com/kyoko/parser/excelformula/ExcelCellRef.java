package com.kyoko.parser.excelformula;

import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.parser.*;
import com.kyoko.common.CoreLog;

public class ExcelCellRef extends Variable {
	String sheetName = null;
	boolean rowAbsolute = false;
	int rowIdx;
	int colIdx;
	CellPositionInterface cellPositionInterface;
	public ExcelCellRef (String p_cellRef, VariableInterface p_interface,CellPositionInterface p_cellPositionInterface) throws Exception{
		super(p_cellRef,p_interface);
		cellPositionInterface = p_cellPositionInterface;
		Pair<String,String> p0 = splitStringTo2(p_cellRef,'!');
		sheetName = p0.getLeft();
		if(sheetName != null && sheetName.startsWith("'")) {
			sheetName = sheetName.substring(1,sheetName.length()-1);
		}
		Pair<Boolean,Integer>[] p1 = decodeExcelRC(p0.getRight());
		colIdx = p1[0].getRight(); 
		rowAbsolute = p1[1].getLeft(); 
		rowIdx = p1[1].getRight(); 
		if(!rowAbsolute) rowIdx -= cellPositionInterface.getRowIdx();
	}
	
	public boolean getRowAbsolute() {
		return(rowAbsolute);
	}
	public String getWorkSheet() {
		return(sheetName);
	}
	public int getColIdx() {
		return(colIdx);
	}
	public int getRowIdx() {
		return(rowIdx);
	}
	public String toString() {
		int r;
		String sheetRef = "";
		if(sheetName != null) {
			if(sheetName.indexOf(" ") >= 0) {
				sheetRef = "'"+sheetName+"'!";
			} else {
				sheetRef = sheetName+"!";
			}
		}
		r = rowIdx;
		if(!rowAbsolute) {
			r += cellPositionInterface.getRowIdx();
		}
		return(sheetRef+encodeExcelRC(colIdx,r,true,rowAbsolute));
	}
	static public ExcelCellRef newExcelCell(String p_cellRef,VariableInterface p_interface,CellPositionInterface p_cellPositionInterface) throws Exception {
		ExcelCellRef exc = new ExcelCellRef(p_cellRef,p_interface,p_cellPositionInterface);
		return(exc);
	}

	static public Pair<String,String> splitStringTo2(String ref,char delimiter) {
		String sheetName = null;
		String cellOrRange = null;
		int xidx = ref.indexOf(delimiter);
		if(xidx > 0) {
			sheetName = ref.substring(0,xidx);
			cellOrRange = ref.substring(xidx+1);
		} else if (xidx == 0) {
			cellOrRange = ref.substring(1);
		} else {
			cellOrRange = ref;
		}
		return(Pair.of(sheetName, cellOrRange));
	}
	static public Pair<Boolean,Integer>[] decodeExcelRC(String rcStr) {
		Pair<Boolean,Integer>[] rcPair = new Pair[2];
		char[] rcChars = rcStr.toCharArray();
		int idx = 0;
		boolean isAbsolute;
		int rc;
		if(rcChars[idx] == '$') {
			isAbsolute = true;
			idx++;
		} else {
			isAbsolute = false;
		}
		if(rcChars[idx] < 'A' || rcChars[idx] > 'Z') {
			return(null);
		} else {
			rc = (rcChars[idx] - 'A' + 1);
			idx++;
		}
		for(;idx <rcChars.length;idx++) {
			if(rcChars[idx] < 'A' || rcChars[idx] > 'Z') {
				break;
			} 
			rc *= 26;
			rc += (rcChars[idx] - 'A' + 1);
			if(rc > 16384) return(null);
		}
		rc--;
		rcPair[0] = Pair.of(isAbsolute, rc);
		if(idx >= rcChars.length) {
			isAbsolute = true;
			rcPair[1] = Pair.of(isAbsolute, Integer.MAX_VALUE);
			return(rcPair);
		}
		if(rcChars[idx] == '$') {
			isAbsolute = true;
			idx++;
		} else {
			isAbsolute = false;
		}
		if(rcChars[idx] < '0' || rcChars[idx] > '9') {
			return(null);
		} else {
			rc = (rcChars[idx] - '0');
			idx++;
		}
		for(;idx <rcChars.length;idx++) {
			if(rcChars[idx] < '0' || rcChars[idx] > '9') {
				return(null);
			} 
			rc *= 10;
			rc += (rcChars[idx] - '0');
			if(rc > 1048576) return(null);
		}
		rc--; // Excel rows start from 1, not 0 
		rcPair[1] = Pair.of(isAbsolute, rc);
		return(rcPair);
	}
//	static public String encodeExcelRC(int p_col,int p_row) {
//		String ss = "";
//		int cc = p_col+1;
//		while(cc > 0) {
//			if(cc > 26) {
//			  int n = (cc - 1) % 26;
//			  ss = ""+((char)(n + 'A'))+ss; 
//			  cc -= (n + 1);
//			  cc /= 26;
//			} else {
//			  ss = ""+((char)(cc + 'A'-1))+ss; 
//			  break;
//			}
//		}
//		return(ss+p_row);
//	}
	static public String encodeExcelRC(int p_col,int p_row,boolean p_colAbsolute,boolean p_rowAbsolute) {
		String ss = "";
		int cc = p_col+1;
		while(cc > 0) {
		    int n = (cc - 1) % 26;
		    ss = ""+((char)(n + 'A'))+ss; 
		    cc -= (n + 1);
			cc /= 26;
		}
		if(p_colAbsolute) ss = "$"+ss;
		if(p_row == Integer.MAX_VALUE) {
			return(ss);
		} else {
			if(p_rowAbsolute) return(ss+"$"+(p_row+1)); else return(ss+(p_row+1));
		}
	}	
//	public static void main(String args[]){
//		/*
//		CoreLog.log("Test " + 676 + " " + encodeExcelRC(676,25));
//		*/
//		for(int i=0;i<1024;i++) {
//			CoreLog.log("Test " + i + " " + encodeExcelRC(i,25,false,true));
//		}
//		
//		/*
//		Pair<Boolean,Integer>[] zeropos = ExcelCellRef.decodeExcelRC( "AMJ1"
//				) ;
//		CoreLog.log("Test " + zeropos[0].getRight());
//		*/
//	}
}
