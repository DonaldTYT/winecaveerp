package com.uniinformation.zkbi;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Window;

import com.kyoko.common.*;
import com.uniinformation.utils.*;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiField;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class ZkBiTranslateHelper extends TranslateUtil {
	
	public static void main(String args[]){
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("erpv4","hlv",null);
		//listField(sh, "clinic.HealthQnr");
		sh.setLHLang("SCHN", 2, false);
		
		
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha1 123 haha1"));
		
		updateText(sh, "(haha1) ([0-9]*) (haha1)", "PATTERN", "TCHN", "xxx1 $2 with tchn content \u7E41\u9AD4\u4E2D\u6587");
		updateText(sh, "(haha2) ([0-9]*) (haha2)", "PATTERN", "SCHN", "xxx2 $2 with schn content \u7B80\u4F53\u4E2D\u6587");
		
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha1 123 haha1"));
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha2 123 haha2"));
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha3 123 haha3"));
		System.exit(0);
		
		
	}
	/***
	 * TODO
	 * list all field with label 
	 * will include label/button/popup/childform/etc.
	 * @param p_sh
	 * @param p_viewName
	 */
	
/* Remarked By DT 2022/07/10, view.getLinkViewws no longer visiable by public class, since the actual visible linked Views now depends on BiResult */	
	
//	public static void listField(SessionHelper p_sh, String p_viewName){
//		BiSchema schema;
//		BiView view;
//		try {
//			schema = BiSchema.loadSchema(p_sh);
//			view = schema.getViewByName(p_viewName);
//			//UniLog.log1("view:%s table:%s", view.getName(), view.getTable().getName());
//			listField(p_sh,view);
//			for (BiView linkView : view.getLinkViews()){
//				//UniLog.log1("lview:%s table:%s", linkView.getName(), linkView.getTable().getName());
//				listField(p_sh, linkView);
//			}
//			
//		} 
//		catch (Exception ex) {
//			UniLog.log(ex);
//		}
//		UniLog.logm(null,"bye");
//	}
	public static void listField(SessionHelper p_sh, BiView view){
		try{
			if (view == null) return;
			Vector<BiColumn> cols = view.getColumns();
			for(int i=0;i<cols.size();i++) {
				BiColumn col = cols.get(i);
				BiField field = col.getField();
				if (field == null){
					//UniLog.log1("%s(%s) field is null, ignore", col.getCellLabel(), col.getEngName());
					continue;
				}
				if (!col.isInList(p_sh) && col.isInvisible(p_sh)){
					//UniLog.log1("%s(%s) field is invisible, ignore", col.getCellLabel(), col.getEngName());
					continue;
				}
				UniLog.log1("view:%s cell:%s label:%s", view.getName(), col.getCellLabel(), col.getEngName());
				/*
				BiTable table = field.getTable();
				BiChain chain = view.findChain(table);
				if (chain.getParent() != null){
					CellCollection subChainCC =  chain.getParent().getCollection("subChains");
					BiTable subChainTable = subChainCC == null ? null : (BiTable)subChainCC.getCollection("table");
					
					if (subChainTable != null){
						if (!subChainTable.equals(view.getTable())){
							UniLog.log1("view:%s subChainTable:%s  table:%s label:%s(%s) field:%s", view.getName(),
									subChainTable == null ? "null" : subChainTable.getName(), table.getName(), col.getLabel(), col.getEngName(), field.getName());
						}
					}
					
				}
				
				BiTable tableDep = col.getTableDepend();
				boolean tableDepFlag = false;
				if (tableDep != null && !tableDep.getName().equals(view.getTable().getName())){
					tableDepFlag = true;
				}
				UniLog.log1("view:%s checkdep: col:%d: label:%s parent:%s tableDep:%s joinFlag:%s size:%d", view.getName(), i, col.getLabel(), ((BiView)col.getParent()).getName(), tableDep == null ? "null" : tableDep.getName(), tableDepFlag, col.getTableDepends().size());
				if (tableDepFlag){
					BiJoin join = tableDep.getJoin(view.getTable());
					if (join != null){
						for (int j=0; j<join.getJoinCount(); j++){
							UniLog.log1("view:%s join from:%s to:%s", view.getName(), join.getFromField(j).getName(), join.getToField(j).getName());
						}
					}
				}
				*/
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
		
	/***
	 * create a translation popup window, when close, will detach
	 * do not cache popupwindow, jx and biresult.
	 * @param p_sh
	 * @param p_parentComp - if null, using mainComp as parent
	 * @param p_key
	 * @param p_type
	 * @param p_defaultValue
	 */
	public static void newPopupWin(SessionHelper p_sh, Component p_parentComp, String p_key, String p_type, String p_defaultValue){
		if (!p_sh.getAllowUpdateTranslate()){
			UniLog.log1("not allow update translate");
			return;
		}
		if (StringUtils.isBlank(p_key)){
			UniLog.log1("key is blank");
			return;
		}
		if (StringUtils.isBlank(p_type)){
			UniLog.log1("type is blank");
			return;
		}
		//obtain translate view
		BiView view = p_sh.getBiView("BiTranslate");
		if (view == null){
			UniLog.log1("BiTranslate view does not exist");
			return;
		}
		
		//create bi result
		BiResult biResult = view.newBiResult(p_sh.getLoginId(),null,null,p_sh);
		if (biResult == null){
			UniLog.log1("biresult is null, abort");
			return;
		}
		
		//create popup
		Window popupWin = ZkUtil.newPopupWindow("Translation",null,true);
		//popupWin.setParent(Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot()); //andrew190806: getfirstroot only return first comp under page
		if (p_parentComp != null){
			popupWin.setParent(p_parentComp);
		}
		else{
			popupWin.setParent(ZkUtil.getMainComp());
		}
		popupWin.setWidth("50%");
		//popupWin.setHeight("50%");
		popupWin.setHeight("min");
		
		
		JxZkBiBase popupJx = JxZkBiBase.buildDetailWindow(biResult, popupWin, false, true, (JxZkBiBaseCallback) null);
		
		//does form close listener is required? seems no different?
		popupJx.addFormCloseListener( new JxFormCloseListener() {
			public int formClose(JxForm p_form) {
				return(JxFormCloseListener.caFree);
			}
		});
		
		
		biResult.clearCondition();
		biResult.addCustomCondition(String.format("bitl_key = '%s' and bitl_type = '%s' and bitl_lang = '%s'", p_key, p_type, p_sh.getLHLang()));
		biResult.query(true);
		if (biResult.getRowCount() > 0){
			biResult.loadOneRecV(0);
			biResult.fetchOneRecV(0);
			/*
			//set default value
			try{
				if (StringUtil.isBlank(biResult.getCellString("bitl_labelstr"))){
					biResult.getCell("bitl_labelstr").set(p_col.getEngName());
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			*/
			popupJx.bindCellCollection(biResult,JxZkBiBase.MODE_UPDATE);
			try{
				if (StringUtils.isBlank(biResult.getCellString("bitl_labelstr"))){
					biResult.getCell("bitl_labelstr").set(p_defaultValue == null ? "" : p_defaultValue);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			popupJx.showForm();	
			popupJx.doModalUpdate();   					
		}
		else{
			try{
				//set default value
				biResult.getCell("bitl_key").set(p_key); 
				biResult.getCell("bitl_type").set(p_type); 
				biResult.getCell("bitl_lang").set(p_sh.getLHLang()); 
				biResult.getCell("bitl_labelstr").set(p_defaultValue == null ? "" : p_defaultValue);
			}
			catch(Exception ex) { 
				ex.printStackTrace(); 
			}
			try{ biResult.getCell("bitl_key").set(p_key); }catch(Exception ex) { ex.printStackTrace(); }
			popupJx.bindCellCollection(biResult,JxZkBiBase.MODE_ADD);
			
			popupJx.showForm();	
			popupJx.doModalAdd();
		}
	}
	
	public static void addOnUpdateTranslateEventListener(Component comp, final SessionHelper sh){
		if (!sh.getAllowUpdateTranslate()) return;
    	comp.addEventListener("onUpdateTranslate", new EventListener(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("data:" + event.getData());
				JSONObject jd = new JSONObject((String)event.getData());
				ZkBiTranslateHelper.newPopupWin(sh, null, 
												StringUtil.trimSqlSafe(jd.getString("key")), 
												StringUtil.trimSqlSafe(jd.getString("type")), 
												StringUtil.trimSqlSafe(jd.getString("defaultValue")));
			}});
	}
	
	
}
