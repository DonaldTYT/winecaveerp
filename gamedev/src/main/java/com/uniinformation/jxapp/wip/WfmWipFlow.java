package com.uniinformation.jxapp.wip;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.wip.BiResultWipJob;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.wip.WfmStep;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.wip.WipException;
import com.uniinformation.wip.WipFlow;
import com.uniinformation.wip.WipStep;
//import com.uniinformation.wip.WipPresetTask;
import com.uniinformation.wip.WipVisCallbackInterface;
import com.uniinformation.wip.WipVisNetwork;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkf.ZkForm;

public class WfmWipFlow extends JxZkBiBase implements WipVisCallbackInterface {
	WipFlow wj;
	Hashtable <Integer,String> extNameHash;
	protected WipVisNetwork nW;
//	JxField ppl;
	ZkForm zkf1;
	CellCollection propertyCollection;
	protected String flowPrefix="wfmf_";
	protected String flowStepPrefix="wfmfs_";
	protected String stepStepPrefix="wfmss_";
	protected String stepStepTable="wfmstepstep";
	protected String flowStepView="wip.WfmFlowStep";
	protected CellValueAction wfmfs_desc_Changed = 
					new CellValueAction() {

						@Override
						public void cellAction_onchange(Cell p_value)
								throws CellException {
							// TODO Auto-generated method stub
							ColumnCell cl = (ColumnCell) p_value;
							int rg = cl.getCollection().getCell(flowStepPrefix+"rg").getInt();
							String id = WfmStep.makeNodeId(rg);
							WfmStep step = (WfmStep) wj.getStep(id);
							if(step != null) step.setDescription(p_value.getString()); {
								nW.drawNetwork(null);
							}
						}

						@Override
						public void cellAction_onfree() throws CellException {
							// TODO Auto-generated method stub
							
						}
						
					};
					/*
	protected CellValueAction node_state_changed =
					new CellValueAction() {
						@Override
						public void cellAction_onchange(Cell p_value)
								throws CellException {
							// TODO Auto-generated method stub
							UniLog.log("HAHA state changed");
						}

						@Override
						public void cellAction_onfree() throws CellException {
							// TODO Auto-generated method stub
						}
						
					};
					*/
			
					/*
	static String makeNodeId(int p_rg) {
		return(String.format("%08d", p_rg));
	}
					*/
	/*
	protected class WfmStep implements WipStep {
		String id;
		String description;
		int rg;
		int level = 0;
		WfmStep(int p_rg,String p_description) {
			id = makeNodeId(p_rg);
			rg = p_rg;
			description = p_description;
		}
		int getStepRg() {
			return(rg);
		}
		@Override
		public int getType() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return id;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return description;
		}

		public void setDescription(String p_desc) {
			// TODO Auto-generated method stub
			description = p_desc;
		}

		@Override
		public int getLevel() {
			// TODO Auto-generated method stub
			return level;
		}

		@Override
		public void setLevel(int p_level) {
			// TODO Auto-generated method stub
			level = p_level;
		}
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return 0;
		}
		public String getColor() {
			return(null);
		}
	}
	*/
	
	protected WfmStep createWfmStep(CellCollection col,int p_rg,String p_description) {
		return(new WfmStep(p_rg,p_description,col.getCellInt(flowStepPrefix+"order")));
	}
	
	/*
	protected CellCollection getStepCollectionByNode(WfmStep p_step) {
		int rg = p_step.getStepRg();
		BiResult sr = getBr().getSubLink(flowStepView);
		Vector<CellCollection> v = sr.getRowCollectionList();
		for(CellCollection cl : v) {
			if(cl.getCell(flowStepPrefix+"rg").getInt() == rg) return(cl);
		}
		return(null);
	}
	*/
	
	int convertToInt(Object o) {
		if(o instanceof String) {
			return((int) Double.parseDouble((String) o));
		} else if(o instanceof Double) {
			return((int) ((Double) o).doubleValue());
		} else if(o instanceof Integer) {
			return((int) ((Integer) o).intValue());
		} 
		return(0);
	}	
	
	public void afterBind() {
		super.afterBind();
		init();
	}
	
	public void createWipFlow() {
		wj = new WipFlow();	
	}
	protected void init() {
		
		try {
			JxField fd = jxAdd("visNwEventDivDet");
			Component visNwEventDiv=null;
			if(fd != null) {
				visNwEventDiv = (Component) fd.getNativeObject();
			}
			nW = new WipVisNetwork(visNwEventDiv, "visNwContentDivDet",jxAdd("btAddLink"), jxAdd("btDelLink"),jxAdd("btRedraw"),jxAdd("btUpdLink"),this);
			if(visNwEventDiv != null) {
				visNwEventDiv.addEventListener(Events.ON_SIZE, 
						new EventListener() {

							@Override
							public void onEvent(Event arg0) throws Exception {
								// TODO Auto-generated method stub
								UniLog.log("HAHA network window resized");
								nW.drawNetwork(null);
							}
					
						}
						);
			}
		} catch (WipException ex) {
			UniLog.log(ex);
		}
		new JxFieldAction("btCreateJob") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				if(getBr().getCellString("wfmf_viewid").equals("")) {
					
				} else {
					BiResult thisBr = getBr().getView().getSchema().getViewByName(getBr().getCellString("wfmf_viewid")).newBiResult(
						getLoginId(), null, null, getSessionHelper());
						thisBr.addCustomCondition(getBr().getCellString("wfmf_createcond"));
						thisBr.query();
						final JxSelOpt selopt = getPopupSelOpt();
						selopt.setOnSelectAction (
						new JxActionListener() {
							public void actionPerformed(JxField fd) {
								Object o  = fd.getValue();
								BiGetItemProperty gipi = (BiGetItemProperty) selopt.getUserData();
								CellCollection col = gipi.getCellCollectionByValue(o);
								int invrg = col.getCell(getBr().getCellString("wfmf_keyfd")).getInt();
								String jobid = col.getCell(getBr().getCellString("wfmf_idfd")).getString();
								String title = col.getCell(getBr().getCellString("wfmf_titlefd")).getString();
								UniLog.log("Haha start job " + invrg);
								selopt.closeForm();
								RpcClient rpc = getRpcClient();
								rpc.callSegment("wiputil_createjobfromflow", 
											new VectorUtil()
												.addElement(flowPrefix)
												.addElement(getBr().getCell(flowPrefix+"rg"))
												.addElement(invrg)
												.addElement(jobid)
												.addElement(title)
												.toVector()
										);
								/*
								try {
									BiResultWipJob.credateJobFromFlow(getBr(), col.getCell(getBr().getCellString("wfmf_keyfd")));
								} catch (CellException ex) {
									UniLog.log(ex);
								}
								*/
							}
						}
								);
				
					BiGetItemProperty gipi = new BiGetItemProperty(thisBr);
					gipi.setItemMode(BiGetItemProperty.GETITEM_MODE_PICK);
					selopt.jxAdd("pickListBox").setItemListInterface( gipi);
					selopt.setUserData(gipi);
					selopt.setPopupWidth(""+gipi.getRowWidth()+"px");
					selopt.modalForm();
				}
			}
			
		};
	}
	public void doUpdate() {
		super.doUpdate();
	}
	
	public void doAdd() {
		super.doAdd();
//		wj.clear();
//		nW.drawNetwork();
	}
	protected void loadNetwork()
	{
		wj.clear(); 
		nW.setWorkFlow(wj);
//		WipPresetTask.loadExtNetworkFromDb(wj, getBr().getSelectUtil(),"", getBr().getCell("extSubtype").getInt(),true);
		try {
			BiResult sr = getBr().getSubLink(flowStepView);
			Vector v = sr.getRowCollectionList();
			for(int i=0;i<v.size();i++) {
				CellCollection col = (CellCollection) v.get(i);
				WfmStep step  = createWfmStep(col,col.getCell(flowStepPrefix+"rg").getInt(),col.getCell(flowStepPrefix+"desc").getString());
				wj.addStep(step);
			}
			TableRec tr = getBr().getSelectUtil().getQueryResult("select * from "+stepStepTable+" where " + stepStepPrefix+"frg = " + getBr().getCellInt(flowPrefix+"rg"), null);
			for(int i=0;i<tr.getRecordCount();i++){
				tr.setRecPointer(i);
				String n0 = WfmStep.makeNodeId(tr.getFieldInt(stepStepPrefix+"fromrg"));
				String n1 = WfmStep.makeNodeId(tr.getFieldInt(stepStepPrefix+"torg"));
				WipStep t0 = wj.getStep(n0);
				WipStep t1 = wj.getStep(n1);
				if(t0 != null && t1 != null) {
					wj.addPreq(t0, t1);
					String ss = tr.getFieldString(stepStepPrefix+"condition");
					if(!StringUtils.isBlank(ss)) {
						wj.setLinkData(t0, t1, ss);
					}
				}
			}
					/*
//			BiREsult sr = public Vector<CellCollection> getRowCollectionList()
			
			
			TableRec tr;
			
			tr = su.getQueryResult("select bdext_seq,bdext_bindnum from bindextent where bdext_extent = " + p_extId, null);
			for(int i=0;i<tr.getRecordCount();i++){
				tr.setRecPointer(i);
				WipPresetTask t = new WipPresetTask(TASKTYPE_BINDING,p_extCode,p_extId,((Integer) tr.getField("bdext_bindnum")).intValue());
				if(p_loadBindName) t.setDescription( tr.getField("bd_name").toString());
				wj.addTask(t);
			}
			tr = su.getQueryResult("select bprq_rg0,bprq_rg1 from bindpreq where bprq_ext = " + p_extId, null);
			for(int i=0;i<tr.getRecordCount();i++){
				tr.setRecPointer(i);
				String k0 = (makeTaskId(p_extCode,p_extId,
							((Integer) tr.getField("bprq_rg0")).intValue()
							));
				String k1 = (makeTaskId(p_extCode,p_extId,
							((Integer) tr.getField("bprq_rg1")).intValue()
							));
				if(k0 != null && k1 != null ) {
					WipTask t0 = wj.getTask(k0);
					WipTask t1 = wj.getTask(k1);
					if(t0 != null && t1 != null) {
						wj.addPreq(t0, t1);
					}
				}
			}
			*/
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	private void saveNetwork()
	{
		SelectUtil su = getBr().getSelectUtil();
		try {
			su.executeUpdate("delete from "+stepStepTable,
						new Wherecl().andUniop(stepStepPrefix+"frg", "=",getBr().getCellInt(flowPrefix+"rg")).stripAnd());
			List <WipStep> stepList = wj.getStepList(null);
			{
				BiTable t = getBr().getView().getSchema().getTable(stepStepTable);
				TableRec tr = t.newTableRec();
				for(int i=0;i<stepList.size();i++) {
					List <WipStep> cl = wj.getChildList(stepList.get(i),true);
					for(int j=0;j<cl.size();j++) {
						tr.addRecord();
						tr.setField(stepStepPrefix+"frg", getBr().getCellInt(flowPrefix+"rg"));
						tr.setField(stepStepPrefix+"seq", 0);
						tr.setField(stepStepPrefix+"fromrg", ((WfmStep) stepList.get(i)).getStepRg());
						tr.setField(stepStepPrefix+"torg", ((WfmStep) cl.get(j)).getStepRg());
						String ldata = (String) wj.getLinkData(
								((WfmStep) stepList.get(i)),
								((WfmStep) cl.get(j)));
						tr.setField(stepStepPrefix+"condition", ldata);
					}
				}
				su.insertByTableRec(t.getDbtName(), tr,true,t.getSerialId());
			}	
			su.close();
		} catch (Exception ex) {
			su.close();
			UniLog.log(ex);
		}
	}
	
	protected void doAfterAddNewStep(WipStep wt) {
		
	}
	
	@Override
	protected void afterAddLink(BiResult sr,int p_idx) {
		CellCollection cl = sr.getRowCollectionV(p_idx);
		try {
			/*
			if(wfmfs_desc_Changed == null) {
				wfmfs_desc_Changed = 
					new CellValueAction() {

						@Override
						public void cellAction_onchange(Cell p_value)
								throws CellException {
							// TODO Auto-generated method stub
							ColumnCell cl = (ColumnCell) p_value;
							int rg = cl.getCollection().getCell(flowStepPrefix+"rg").getInt();
							String id = makeNodeId(rg);
							MyStep step = (MyStep) wj.getTask(id);
							step.setDescription(p_value.getString());
							nW.drawNetwork();
						}

						@Override
						public void cellAction_onfree() throws CellException {
							// TODO Auto-generated method stub
							
						}
						
					};
			}
			*/
			sr.setColumnRg(cl,flowStepPrefix+"rg");
			cl.getCell(flowStepPrefix+"desc").set("New Step");
//			cl.getCell(flowStepPrefix+"srg").set(1);
			cl.getCell(flowStepPrefix+"desc").addAction(wfmfs_desc_Changed);
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		WfmStep t = createWfmStep(cl,cl.getCell(flowStepPrefix+"rg").getInt(),cl.getCell(flowStepPrefix+"desc").getString());
		wj.addStep(t);
		doAfterAddNewStep(t);
		nW.drawNetwork();
	}
	
	@Override
	protected void afterUnDeleteLink(BiResult sr,int p_idx) {
		CellCollection cl = sr.getRowCollectionV(p_idx);
		WfmStep t = createWfmStep(cl,cl.getCell(flowStepPrefix+"rg").getInt(),cl.getCell(flowStepPrefix+"desc").getString());
		wj.addStep(t);
		nW.drawNetwork();
	}
	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) {
		UniLog.log("beforeAddLink " + getBr().getView().getName());
		if(sr.getView().getName().equals("BdExtBind")) {
			Vector v = jxGetSelectList("extPick");
			if(v == null || v.size() != 1) {
				return(new ReturnMsg(false,"Please Select Item to Add"));
						/*
				messageBox("Please Select Item to Add");
				return(false);
				*/
			}
			Integer I = (Integer) v.get(0);
			String s = extNameHash.get(I);
			try {
				cl.getCell("bdext").set(getBr().getCell("extSubtype").getInt());
				cl.getCell("bdrg").set(I.intValue());
				cl.getCell("bdname").set(s);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			setDirtyFlag(true);
			return(null);
		}
		return(null);
	}
	@Override
	public void bindCellCollection(BiResult c,int mode) {
		super.bindCellCollection(c,mode);
		
		
		try {
//			final Vector v = new Vector();
//			SelectUtil su = getBr().getSelectUtil();
//			
//			TableRec tr = su.getQueryResult("select wfms_name,wfms_rg,wfms_seq from wfmstep where wfms_rg not in (select wfmfs_srg from wfmflowstep where wfmfs_frg = " + 
//					getBr().getCell(flowPrefix+"rg").getInt() + " ) order by wfms_seq", null);
//			
//			extNameHash = new Hashtable<Integer,String>();
//			for(int i = 0;i<tr.getRecordCount();i++) {
//				tr.setRecPointer(i);
//				String  s = tr.getField("wfms_name").toString();
//				Integer x = (Integer) tr.getField("wfms_rg");
////				v.add(tr.getField("bd_name").toString());
//				extNameHash.put(x,s);
//				v.add(x);
//			}
//			jxSetItemListInterface("extPick",
//					new TranslateListGetItemProperty(v) {
//						public String translate(Object p_item) {
//							return(extNameHash.get(p_item));
//						};
//					}
//				);
			createWipFlow();
			wj.clear();
			nW.setWorkFlow(wj);
			if(mode == JxZkBiBase.MODE_UPDATE) {
				BiResult sr = getBr().getSubLink(flowStepView);
				Vector<BiCellCollection> v = sr.getRowCollectionList();
				for(BiCellCollection col : v) {
					col.getCell(flowStepPrefix+"desc").addAction(wfmfs_desc_Changed);
				}
				loadNetwork();
			}
			nW.drawNetwork();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	
	}
@Override
	protected ReturnMsg afterUpdate(BiResult br) {
		saveNetwork();
		return(super.afterUpdate(br));
	}
@Override
	protected ReturnMsg afterAdd(BiResult br) {
		saveNetwork();
		return(super.afterAdd(br));
	}
void setStepLevel() {
		BiResult sr = getBr().getSubLink(flowStepView);
		Vector<BiCellCollection> v = sr.getRowCollectionList();
		try {
		for(BiCellCollection cl : v) {
			cl.getCell(flowStepPrefix+"level").set(
					wj.getStep(WfmStep.makeNodeId(cl.getCellInt(flowStepPrefix+"rg"))).getLevel() 
					);
		}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
}
@Override
	protected ReturnMsg beforeAdd(BiResult br) {
//		WipPresetTask.saveExtNetworkToDb(br.getView().getSchema(),wj, br.getCell("extSubtype").getInt());
	
		setStepLevel();
		return(new ReturnMsg(true));
	}
@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
//		WipPresetTask.saveExtNetworkToDb(br.getView().getSchema(),wj, br.getCell("extSubtype").getInt());
	
		setStepLevel();
		return(new ReturnMsg(true));
	}
@Override
	protected ReturnMsg beforeDeleteLink(BiResult sr,int p_idx) {
		CellCollection cl = sr.getRowCollectionV(p_idx);
		String id = WfmStep.makeNodeId(cl.getCell(flowStepPrefix+"rg").getInt());
		WipStep t = wj.getStep(id);
		if(t != null) wj.delStep(t, true);
		nW.drawNetwork();
		return(null);
	}
	@Override
	public void visActionCallBack(int mode,final WipStep p_s0,final WipStep p_s1) {
		// TODO Auto-generated method stub
		switch(mode) {
		case WipVisNetwork.WIPVIS_CBMODE_START_CLICKED:
		case WipVisNetwork.WIPVIS_CBMODE_END_CLICKED:
			//nW.drawNetwork();
			break;
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_ADDED:
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_DELETED:
		case WipVisNetwork.WIPVIS_CBMODE_NONE_CLICKED:
			nW.drawNetwork(null);
			break;
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_UPDATED:
			try {
				final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/wipupdlink.zul"); 
				final CellCollection col = new CellCollection();
				Object data = wj.getLinkData(p_s0, p_s1);
				if(data != null) {
					JSONObject jo = new JSONObject((String) data);
					if(jo != null) CellCollectionToJsonInterface.JSONObjectToCellCollection(col, jo);
				}
				
    			zkf1.doModal(col,new ZkBiEventListener() {
							@Override
							public void onZkBiEvent(Event arg0) throws Exception {
								if(arg0.getTarget().getId().equals("btOK")) {
									JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(col);
									wj.setLinkData(p_s0, p_s1, jo.toString());
									zkf1.exitModal();
								}
								if(arg0.getTarget().getId().equals("btCancel")) {
									zkf1.exitModal();
								}
							}
    					}
        		);
				
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			break;
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_CLICKED:
		case WipVisNetwork.WIPVIS_CBMODE_NODE_CLICKED:
			break;
		default:
			setDirtyFlag(true);
			break;
		}
	}	

	static public void loadExtNetworkFromDb(WipFlow wj,SelectUtil su,String p_extCode,int p_extId,boolean p_loadBindName) {
		try {
					/*
//			BiREsult sr = public Vector<CellCollection> getRowCollectionList()
			
			
			TableRec tr;
			
			tr = su.getQueryResult("select bdext_seq,bdext_bindnum from bindextent where bdext_extent = " + p_extId, null);
			for(int i=0;i<tr.getRecordCount();i++){
				tr.setRecPointer(i);
				WipPresetTask t = new WipPresetTask(TASKTYPE_BINDING,p_extCode,p_extId,((Integer) tr.getField("bdext_bindnum")).intValue());
				if(p_loadBindName) t.setDescription( tr.getField("bd_name").toString());
				wj.addTask(t);
			}
			tr = su.getQueryResult("select bprq_rg0,bprq_rg1 from bindpreq where bprq_ext = " + p_extId, null);
			for(int i=0;i<tr.getRecordCount();i++){
				tr.setRecPointer(i);
				String k0 = (makeTaskId(p_extCode,p_extId,
							((Integer) tr.getField("bprq_rg0")).intValue()
							));
				String k1 = (makeTaskId(p_extCode,p_extId,
							((Integer) tr.getField("bprq_rg1")).intValue()
							));
				if(k0 != null && k1 != null ) {
					WipTask t0 = wj.getTask(k0);
					WipTask t1 = wj.getTask(k1);
					if(t0 != null && t1 != null) {
						wj.addPreq(t0, t1);
					}
				}
			}
			*/
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}	
}
