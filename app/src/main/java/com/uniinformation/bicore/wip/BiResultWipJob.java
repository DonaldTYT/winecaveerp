package com.uniinformation.bicore.wip;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellVector;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.wip.WipJob;

public class BiResultWipJob extends BiResult {

		public BiResultWipJob(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
		@Override
		protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
			SelectUtil su = getSelectUtil();
			try {
				//CellVector cv = su.getQueryResultToCellVector("select wfmact_rg, wfmact_content from wfmactivity", new Wherecl().andUniop("wfmact_frg", "=", getCellInt("wfmj_rg")));
				CellVector cv = su.getQueryResultToCellVector("select wfmact_rg, wfmact_content from wfmactivity where wfmact_frg = ? and wfmact_content like 'FILING://%'", new Wherecl().appendArgument(getCellInt("wfmj_rg")));
				for (Object o : cv) {
					CellCollection cc = (CellCollection)o;
					int rg = cc.getInt("wfmact_rg");
					String content = cc.getString("wfmact_content");
					UniLog.log1("rg:%d, content:%s", rg, content);
					if (StringUtils.startsWith(content, "FILING://")) {
						String flKey = String.format("zkbi_wfmact_%010d", rg);;
						UniLog.log1("deleteFile key:%s", flKey);
						FilingUtil.deleteFile(sh.getAgent(), null, flKey);
					}
				}
				su.executeUpdate("delete from wfmtasktask", new Wherecl().andUniop("wfmtt_frg", "=", getCellInt("wfmj_rg")));
				su.executeUpdate("delete from wfmactivity", new Wherecl().andUniop("wfmact_frg", "=", getCellInt("wfmj_rg")));
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"Delete Task Task Error",true));
			}
			return(super.biBeforeDeleteCurrent(col));
		}
//		@Override
//		protected void addExtraWhereStr(Wherecl p_where)
//		{
//			String uid = getSelectUtil().getLoginId();
//			UniLog.log("user = " + getSelectUtil().getLoginId());
//			if(!BiSchema.hasAccessRight(sh, "allwip")) {
//				Wherecl wcl1 = new Wherecl();
//				wcl1.genInList("and", "wfmj_id", "in", sh.getAccessUsers());
//					Wherecl wcl2 = new Wherecl();
//					String ss = null;
//					for(String as : sh.getAccessUsers()) {
//						if(ss == null) ss = (
//								"wfmj_rg in (select wfmjtxx.wfmjt_frg from wfmjobtask wfmjtxx where wfmjtxx.wfmjt_access in('"
//								+as+"')"); else ss += ",'"+as+"'";
//					}
//					ss += ")";
//					wcl2.appendString(ss);
//					wcl1.orWherecl(wcl2);
//				p_where.andWherecl(wcl1);
//			}
//		}
		
		@Override
		public String getColumnDisplayClass(ColumnCell p_cell) {
			if((!p_cell.getCellLabel().equals("wfmj_state")) &&
			   (!p_cell.getCellLabel().equals("wfmj_updtime")) ) return(null);
			Date d = getCell("wfmj_updtime").getDate();
			long dd = new Date().getTime();
			dd -= d.getTime();
			if(dd < 60000) {
				return("myLabelNews");
			} else {
				return(null);
			}
		}
		@Override
		public String getColumnDisplayString(ColumnCell p_cell) {
			String s = super.getColumnDisplayString(p_cell);
			if(p_cell.getCellLabel().equals("wfmj_state")) {
				if(getCellInt("wfmj_state") == WipJob.JOB_STATE_STARTED) {
					if(!getCellString("wfmj_updstr").equals("")) {
						return(getCellString("wfmj_updstr"));
					}
				}
			}
			if(p_cell.getCellLabel().equals("wfmj_timeout")) {
				if(s.endsWith("00:00:00")) {
					return("On "+DateUtil.toDateString(p_cell.getDate(), "yyyy/mm/dd"));
				}
				if(s.endsWith("23:59:59")) {
					return("By "+DateUtil.toDateString(p_cell.getDate(), "yyyy/mm/dd"));
				}
				if(s.trim().equals("")) {
					return("ASAP");
				}
			}
			return(s);
		}	
		
		
		/*
		static public int credateJobFromFlow(BiResult flowBr,Object key) throws CellException {
			BiSchema schema = flowBr.getView().getSchema();
			BiResult jobBr = schema.getViewByName("wip.WfmWipJob").newBiResult(null,flowBr.getSessionHelper().getLoginId(), null, null, flowBr.getSessionHelper());
			BiResult linkBr = schema.getViewByName(flowBr.getCellString("wfmf_viewid")).newBiResult(null,flowBr.getSessionHelper().getLoginId(), null, null, flowBr.getSessionHelper());
			BiResult flowStepBr = schema.getViewByName("wip.WfmFlowStep").newBiResult(null,flowBr.getSessionHelper().getLoginId(), null, null, flowBr.getSessionHelper());
			jobBr.clearCurrentRec();
			jobBr.getCell("wfmj_viewid").set(flowBr.getCellString("wfmf_viewid"));
			jobBr.getCell("wfmj_keyfd").set(flowBr.getCellString("wfmf_keyfd"));
			jobBr.getCell("wfmj_key").set(key);
			jobBr.getCell("wfmj_frg").set(flowBr.getCellString("wfmf_rg"));
			jobBr.getCell("wfmj_autostart").set(flowBr.getCell("wfmf_autostart").getBoolean());
			jobBr.getCell("wfmj_createcond").set(flowBr.getCellString("wfmf_createcond"));
			jobBr.getCell("wfmj_endcond").set(flowBr.getCellString("wfmf_endcond"));
			linkBr.addCustomCondition(flowBr.getCellString("wfmf_keyfd")+"="+key);
			linkBr.query();
			if(linkBr.getRowCount() != 1) throw new CellException("Link View Not Found");
			linkBr.loadOneRecV(0);
			if(!flowBr.getCellString("wfmf_idfd").equals("")) 
				jobBr.getCell("wfmj_id").set(linkBr.getCellString(flowBr.getCellString("wfmf_idfd")));
			if(!flowBr.getCellString("wfmf_titlefd").equals("")) 
				jobBr.getCell("wfmj_title").set(linkBr.getCellString(flowBr.getCellString("wfmf_titlefd")));
			jobBr.addCurrent();
			flowStepBr.addCustomCondition("wfmfs_frg="+flowBr.getCellInt("wfmf_rg"));
			flowStepBr.query();
			
			
			return(0);
		}
		*/
}
