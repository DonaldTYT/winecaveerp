package com.uniinformation.zkbi;

import java.io.File;
import java.io.FileOutputStream;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerBiTable extends ZkBiComposerBase {
	   protected void setupAddButton(final BiResult result)
	    {
		    super.setupAddButton(result);
	       	final Button btnExport = new ZkBiButton();
	        btnExport.setLabel("Export Schema");
	        btnExport.setId("btSchema");
	        btnExport.addEventListener("onClick",
	        	new EventListener() {
	        		public void onEvent(Event event) throws Exception {
	        			UniLog.log("HAHA export Schema");
	        			/*
	        			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
	        			StringBuffer schxml = new StringBuffer();
	        			schema.toXML(schxml);
	        			FileOutputStream os = new FileOutputStream("c:\\tmp\\abc.xml");
	        			os.write(schxml.toString().getBytes());
	        			os.close();
	        			*/
	        			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
	        			try {
	        				schema.exportToXML(new File("/tmp/abc.xml"));
	        				ZkUtil.showMsg("schema exported");
	        			
	        			} catch (Exception ex) {
	        				UniLog.log(ex);
	        			}
	        		}
	        	}
	        );
	        //masterWin.appendChild(btnExport); 
	        abHelper.addButton(btnExport);
	        final Button btnAddUser = new ZkBiButton();
	        btnAddUser.setLabel("Add User");
	        btnAddUser.setId("btAddUser");
	        btnAddUser.addEventListener("onClick",
	        	new EventListener() {
	        		public void onEvent(Event event) throws Exception {
	        			UniLog.log("HAHA Webmenu Add User");
	        			final ZkForm zkf1 = new ZkForm(null,"zkf/webmenu001.zul");
	        			final CellCollection col = new CellCollection();
	        			col.addCell("userid", new Cell("donald"));
	        			zkf1.doModal(col,new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
									UniLog.log("HAHA clicked");
									if(arg0.getTarget().getId().equals("btOK")) {
										UniLog.log("Value = " + col.getCell("userid").getString());
										if(col.getCell("userid").getString().trim().equals("")) {
											Messagebox.show("User ID Shoule not be blank");
										} else {
										SelectUtil su = result.getSelectUtil();
										TableRec tr = su.getQueryResult("select * from webmenu where webm_rg not in (select webmu_mrg from webmenuuser where webmu_user = '"+ col.getCell("userid").getString()+ "')",null);
										for(int i = 0;i<tr.getRecordCount();i++) {
											tr.setRecPointer(i);
											su.executeUpdate("insert into webmenuuser (webmu_mrg,webmu_user,webmu_active) values (?,?,?)", 
														new Wherecl()
															.appendArgument(tr.getFieldInt("webm_rg"))
															.appendArgument(col.getCell("userid").getString())
															.appendArgument("")
													);
										}
										zkf1.exitModal();
										}
									}
									if(arg0.getTarget().getId().equals("btCancel")) {
										zkf1.exitModal();
									}
								}
	        				}
	        			);
	        		}
	        	}
	        );
	        //masterWin.appendChild(btnAddUser); 
	        abHelper.addButton(btnAddUser);
	    }
}
