package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.HashSet;

import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;

public interface ZkBiSearchInterface {
	public void searchResult(ArrayList <TrStatFilter> searchResultStatList,HashSet<TrStatFilter> il);
	public HashSet<Integer> getInludeList();

}
