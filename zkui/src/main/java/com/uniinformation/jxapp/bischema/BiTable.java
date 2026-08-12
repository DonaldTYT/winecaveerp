package com.uniinformation.jxapp.bischema;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.bischema.BiResultBiTable;
import com.uniinformation.bicore.bischema.BiResultBiTable.FieldRec;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil.PickByTableTrForm;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkf.ZkForm;

public class BiTable extends JxZkBiBase{
	private PickByTableTrForm pickTabNameForm;
	@Override
	public void afterBind() {
		super.afterBind();
		try {
			JxField fd = jxAdd("ddt_dbtname");
			ZkJxPickInput comp = (ZkJxPickInput)fd.getNativeObject();
			if (pickTabNameForm == null) {
				pickTabNameForm = new PickByTableTrForm(sessionHelper, new String[] { "table_name" }, () -> {
					String dbName = getBr().getCellString("dddb_database");
					return Triple.of(getBr().getSelectUtil(), "select table_name from information_schema.tables where table_schema = ? order by 1", new Wherecl().appendArgument(dbName));
				}, (rec, tr, userData) -> {
					comp.setText((String)rec[0]);
					Events.echoEvent(Events.ON_CHANGE, comp, null);
				});
			}
			pickTabNameForm.bindComponent(comp, null, true);
			comp.setPopupWidth("360px");
			fd.addChangeListener((field, orgvalue) -> {
				if (!StringUtils.equals(getBr().getCellString("ddt_dbtname"), orgvalue)) {
					BiResultBiTable br = (BiResultBiTable)getBr();
					Collection<FieldRec> fdList = br.dbCatalogGetFieldList();
					if (fdList != null) {
						try {
							BiResult sr = br.getSubLink("bischema.BiField");
							for (FieldRec fr : fdList) {
								ReturnMsg rtn = listboxAddRow(this, sr, jxAdd("list_bischema_BiField"), null, -1);
								if (rtn.getStatus()) {
									CellCollection col = sr.getRowCollectionV(sr.getRowCount() - 1);
									col.getCell("ddf_fdname").set(fr.fieldName);
									col.getCell("ddf_type").set(fr.fieldType);
									col.getCell("ddf_len").set(fr.fieldLen);
								}
							}
						} catch (CellException ex) {
							UniLog.log(ex);
						}
					}
				}
				return true;
			});
		} catch (Exception e) {
			UniLog.log(e);
		}

		new JxFieldAction("btCreateView") {
			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
    			try {
    				final ZkForm zkf1 = new ZkForm(null,"zkf/bischema/CreateView.zul");
    				final CellCollection col = new CellCollection();
    				zkf1.doModal(col,new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							if(arg0.getTarget().getId().equals("btOK")) {
								String viewName = col.getCellString("cvViewName");
								if(StringUtils.isBlank(viewName)) return;
								BiResult br = getBr().getView().getSchema().getViewByName("bischema.BiView").newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
								br.clearCurrentRec();
								br.getCell("grpth_database").set(getBr().getCellString("ddt_database"));
								br.getCell("grpth_table").set(getBr().getCellString("ddt_tabname"));
								br.getCell("grpth_id").set(viewName);
								br.getCell("grpth_header").set(col.getCellString("cvViewHeader"));
								br.getCell("grpth_remark").set(col.getCellString("cvViewRemark"));
								br.getCell("grpth_attribute").set(BiView.DEFAULT_ATTRIBUTE); 
										BiResult sr = br.getSubLink("bischema.BiColumn");
										int seq=1;
										for(BiCellCollection bc : getBr().getSubLink("bischema.BiField").getRowCollectionList()) {
												BiCellCollection col = sr.newRowCollection();								
												sr.addSubRecord(col, -1 ,"");
												col.getCell("grptc_subtable").set(getBr().getCellString("ddt_tabname"));
												col.getCell("grptc_fd").set(bc.getCellString("ddf_fdname"));
												col.getCell("grptc_label").set(bc.getCellString("ddf_fdname")); /* should replace to use prefix if prefix is not blank */
												col.getCell("grptc_header").set(bc.getCellString("ddf_fdname")); /* should replace to use prefix if prefix is not blank */
												col.getCell("grptc_attribute").set(BiColumn.DEFAULT_ATTRIBUTE);
												col.getCell("grptc_fdtype").set(bc.getCellString("ddf_type"));
												col.getCell("grptc_fdlen").set(bc.getCellInt("ddf_len"));
												col.getCell("grptc_inlist").set(true);
												col.getCell("grptc_seq").set(seq);
												seq++;
										}
								ReturnMsg rtn = br.addCurrent();
//								if(rtn.getStatus()) {
//									int sid = (Integer) rtn.getData();
//									br.addCustomCondition("grpth_database = '"+ getBr().getCellString("ddt_database")+"' and grpth_id='"+viewName+"'");
//									br.query();
//									if(br.getRowCount() == 1) {
//										br.fetchOneRecV(0);
//										BiResult sr = br.getSubLink("bischema.BiColumn");
//										for(BiCellCollection bc : getBr().getSubLink("bischema.BiField").getRowCollectionList()) {
//												BiCellCollection col = sr.newRowCollection();								
//												sr.addSubRecord(col, -1 ,"");
//												col.getCell("grptc_subtable").set(getBr().getCellString("ddt_tabname"));
//												col.getCell("grptc_fd").set(bc.getCellString("ddf_fdname"));
//												col.getCell("grptc_label").set(bc.getCellString("ddf_fdname")); /* should replace to use prefix if prefix is not blank */
//												col.getCell("grptc_header").set(bc.getCellString("ddf_fdname")); /* should replace to use prefix if prefix is not blank */
//												col.getCell("grptc_attribute").set(BiColumn.DEFAULT_ATTRIBUTE);
//												col.getCell("grptc_inlist").set(true);
//										}
//										rtn = br.updateCurrent();
//									}
//								}
								if(rtn != null && !rtn.getStatus()) {
									messageBox("Create View Error : " + rtn == null ? "Null":rtn.getMsg());
								} else {
									messageBox("Create View OK");
								}
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
				
			}	
			
		};
	}
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);	
		if(mode == JxZkBiBase.MODE_ADD) {
			unlockSubLink("bischema.BiField");
			jxSetEnable("btCreateView",false);
		} else {
		if(((BiResultBiTable) br).isFetchJdbcCatalogOk()) {
			jxSetEnable("btCreateView",true);
			if(((BiResultBiTable) br).isAllowAlterSqlTable()) {
				unlockSubLink("bischema.BiField");
			} else {
				lockSubLink("bischema.BiField");
			}
		} else {
			lockSubLink("bischema.BiField");
			jxSetEnable("btCreateView",false);
		}
		}
	}
}
