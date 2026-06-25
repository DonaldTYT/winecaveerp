package com.uniinformation.bicore;

import java.util.ArrayList;
import java.util.HashMap;

import com.uniinformation.utils.TableRec;

public class BiRef {
	HashMap<String,TableRec> refTabTrHM = new HashMap<String,TableRec>(); //key: from refTabName, data: refTab
	HashMap<String,String> affectedColHM = new HashMap<String,String>();  //key: refTabCol, value: affectedCol
	

	public BiRef(String p_view){
		//construct the HM
	}
	public String getAffectedCol(String p_refCol){ //should return a value pair 
		return(null);
	}
	public boolean isAffected(String p_col){
		return(false);
	}
	public ArrayList getRefColData(String p_refCol){
		return(null);
		
	}
}
