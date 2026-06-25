package com.uniinformation.jxapp.afs;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.AfsQuotation;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkf.ZkForm;

public class AfsQuoMc extends AfsQuotation {
	@Override
	protected Wherecl createPullDownWherecl(CellCollection col) {
		Wherecl wcl;
		wcl = super.createPullDownWherecl(col);
		if(wcl == null) wcl = new Wherecl();
		wcl.genInList("and", "st_mtype","in","M","O");
//		wcl.appendString(" and st_mtype in ('M','O') ");
		return(wcl.orWherecl( new Wherecl().andUniop("st_mtype", "=", "B")
				)
			);
	}
	void doCopyChinaPoToSo(String p_ponum) {
				String rHost="erpv4afscn";
				RpcClient rpc = RecSync.openRpc(getSessionHelper().getAgent(), rHost, 500);
				if(rpc == null) {
					Messagebox.show("Fail to connect to remote server");
					return;
				}
				
				try {
					Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncErpv4RpcServlet.getBiCollection",
						new VectorUtil()
						.addElement(rHost)
						.addElement("erpv4.PoMulti")
						.addElement("stm_ref1 = '" + p_ponum + "'")
						.toVector()
					);
					if(v != null && v.toString().startsWith("OK")) {
						JSONObject jo = new JSONObject(v.toString().substring(4));
						CellCollection jcol = new CellCollection();
						CellCollectionToJsonInterface.JSONObjectToCellCollection(jcol,jo);
						Vector<CellCollection> itemList = jcol.getCollectionList("erpv4.PoDet");
						if(itemList == null) {
							Messagebox.show("Read Remote Order Detail Error 1");
							return;
						}
						BiResult sr = getBr().getSubLink("AfsQuoDet");
						JxField sv = null;
						sv = jxAdd("list_"+"AfsQuoDet".replace(".", "_"));
						for(int i=0;i<itemList.size();i++) {
							CellCollection iCol = (CellCollection) itemList.get(i);
							BiCellCollection col = null;
							if(i >= sr.getRowCount()) {
								col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, -1 ,"");
								Object trobj = rtn.getData();
								sv.addItemToList(trobj, i);
							} else {
								Object o = sr.getTrStatObj(new Integer(i));
								sr.markDelete( o, false);
								col = sr.getRowCollectionV(i);
								sv.gridSetDataFormat(-1,i,"remove_deleted");
							}
							
							col.getCell("ind_pdsrg").set(1);
							col.getCell("ind_irg").set(iCol.getCellInt("stmd_irg"));
							col.getCell("ind_uprice").set(iCol.getCellInt("stmd_uprice"));
							col.getCell("ind_qty").set(iCol.getCellInt("stmd_qty"));
							/*
							col.getCell("stmd_nref4").set(1);
							col.getCell("stmd_irg").set(tr.getFieldInt("stsn_irg"));
							col.getCell("stmd_org").set(tr.getFieldInt("stsn_org"));
							col.getCell("stmd_ref4").set(StringUtil.strpart(tr.getFieldString("stsn_ref4"),0,30));
							col.getCell("stmdki_ref4").set(tr.getFieldString("stsn_ref4"));
							col.getCell("stmd_entryqty").set(tr.getFieldDouble("stsn_nqty"));
							*/
						}
					} else {
						Messagebox.show("Read Remote Order Detail Error 0");
					}
				} catch (Exception ex) {
					rpc.close();
					UniLog.log(ex);
				} finally {
					rpc.close();
				}
	}
	
	@Override
	public void afterBind() {
		super.afterBind();
		detTypeList.clear();
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_TRADEIN));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK));
		
		
//		new JxFieldChange("vd_vname") {
//		public boolean valueChanged(JxField fd,String orgValue){  
//			try {
//				TableRec tr = br.getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + br.getCell("inv_vcode").getString() + "'", null);
//					Vector v = new Vector();
//					for(int i=0;i<tr.getRecordCount();i++) {
//						tr.setRecPointer(i);
//						v.add(tr.getField("svloc_desp"));
//					}
//					br.getCell("svloc_desp").setItemList(v);
//				} catch (Exception ex){ 
//					UniLog.log(ex);
//				}	
//			return(true);
//		}
//		};
		
		new JxFieldAction("btFromChina") {
			@Override
			public void actionPerformed(JxField jxfield) {
	        		final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/input_ponum.zul");
	        		final CellCollection col = new CellCollection();
	        		try {
	        			zkf1.doModal(col,new EventListener() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								// TODO Auto-generated method stub
								if(arg0.getTarget().getId().equals("btOK")) {
									zkf1.exitModal();
									String ss = col.getCellString("input_ponum");
									if(!StringUtils.isBlank(ss)) {
										doCopyChinaPoToSo(ss);
									}
								}
								if(arg0.getTarget().getId().equals("btCancel")) {
									zkf1.exitModal();
								}
							}
	        				}
	        			);
	        		} catch (Exception ex) {
	        		}
	        		
	        		
//				UniLog.log("Call bt from china");
			}
			
		};
	}
	@Override
	public void bindCellCollection(BiResult p_br,int p_mode)
	{
		super.bindCellCollection(p_br,p_mode);
//		if(p_mode == JxZkBiBase.MODE_ADD) {
//				br.getCell("svloc_desp").setItemList(new Vector());
//		} 
//		if(p_mode == JxZkBiBase.MODE_UPDATE) { 
//			try {
//			TableRec tr = br.getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + br.getCell("inv_vcode").getString() + "'", null);
//				Vector v = new Vector();
//				for(int i=0;i<tr.getRecordCount();i++) {
//					tr.setRecPointer(i);
//					v.add(tr.getField("svloc_desp"));
//				}
//				br.getCell("svloc_desp").setItemList(v);
//			} catch (Exception ex){ 
//				UniLog.log(ex);
//			}
//		}
		if(p_mode == MODE_UPDATE) {
//			JxField sv = jxAdd("list_"+replaceViewName(detailViewId));
			if(p_br.getCellString("inv_quostatus").equals("Confirmed")) {
				if(reviseConfirmedQuo) {
					reviseConfirmedQuo = false;
					unlockQuo(p_br);
					try {
						reviceQuotation() ;
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					lockQuo(p_br);
				}
				/*
				AbstractGetItemProperty gipi = getGipi(detailViewId );
				((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
				sv.setAttribute("mode", "noDelete");
				sv.setAttribute("mode", "noInsert");
				try {
					p_br.getCurrentCollection().lock();
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				*/
			} else {
				unlockQuo(p_br);
				/*
				AbstractGetItemProperty gipi = getGipi(detailViewId );
				((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_INPUT);
				sv.setAttribute("mode", "canDelete");
				sv.setAttribute("mode", "canInsert");
				try {
					p_br.getCurrentCollection().unlock();
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				*/
			}
		}
		if(p_mode == MODE_ADD) {
			unlockQuo(p_br);  //andrew231113 hotfix after confirm so, add new so cannot pick stock bug 
		}
		if("Y".equals(Erpv4Config.getString(getSessionHelper(),"QUORequireApproval"))) {
			if(!BiSchema.hasAccessRight(getSessionHelper(), "#cfmso")) {
				jxAdd("btConfirmOdr").setEnable(false);
			} 
		}
	}
}
