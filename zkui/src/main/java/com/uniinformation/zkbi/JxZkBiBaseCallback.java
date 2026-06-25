package com.uniinformation.zkbi;


import java.util.HashSet;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.kyoko.common.*;

//TODO make method more generic!
public interface JxZkBiBaseCallback {
	/***
	 * fresh one row
	 * @param p_dataObj
	 */
	public void biBaseRefreshListitems(Object p_dataObj);
	
	/***
	 * refresh all rows
	 * @param p_result
	 */
	
	public void biBaseRefresh(BiResult p_result);
	/***
	 *  call when bibase open
	 */
   	public void biBaseOpen();
   	
   	/***
   	 * call when bibase close
   	 */
   	public void biBaseClose(BiResult p_br);
   	
   	public ReturnMsg fetchNext(BiResult p_br);
   	public ReturnMsg fetchPrevious(BiResult p_br);
   	
   	/***
   	 * check has next record is available. used for control pre/next button.
   	 * @return true/null - available; false - not available;
   	 */
   	public Boolean hasNextRec();
   	/***
   	 * check has previous record is available. used for control prev/next button.
   	 * @return true/null - available; false - not available;
   	 */
   	public Boolean hasPrevRec();
   	public String getExtraInfo();
	public HashSet<BiColumn> getVisibleColumns(BiResult p_br);
   	
    
}
