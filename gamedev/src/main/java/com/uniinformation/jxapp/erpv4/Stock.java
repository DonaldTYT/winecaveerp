package com.uniinformation.jxapp.erpv4;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Image;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.erpv4.BiResultStock;
import com.uniinformation.bicore.erpv4.BiResultStockMove;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkf.ZkForm;


public class Stock extends JxZkBiBase {
	String stockMoveViewId = null;
	
	@Override
	public void onBarcode(String p_barcode) {
		try {
          getBr().getCell("st_barcode").set(p_barcode);
          setDirtyFlag(true);
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		JxField fd;
		super.bindCellCollection(br, mode);
		if(stockMoveViewId == null) {
			for(BiResult sr:br.getSubLinks()) {
				if(sr.getView().getTable().getName().equals("stmovd_any")) {
					stockMoveViewId = sr.getView().getName();
				
				}
			}
		}
		if(stockMoveViewId != null) {
			fd = jxAdd("list_"+JxZkBiBase.replaceViewName(stockMoveViewId));
			if(fd != null) {
//				fd.setAttribute("paging", "withfilter");
				fd.setAttribute("showfilter", "");
				HtmlBasedComponent c = (HtmlBasedComponent) fd.getNativeObject();
				if(!getSessionHelper().useJxFormG2()) {
					c.setHeight("400px");
				}
			}
		}
		if(!BiSchema.hasAccessRight(getSessionHelper(),"invshowp")) {
			jxSetVisible("stockPricePanel",false);
		} else {
			jxSetVisible("stockPricePanel",true);
		}
		Cell dimUnit = br.getCell("st_dimensionunit");
		if(dimUnit != null) {
		dimUnit.setItemPropertyInterface(
				new TranslateListGetItemProperty(
						new VectorUtil()
							.addElement("")
							.addElement("M")
							.addElement("c")
							.addElement("m")
							.addElement("Y")
							.addElement("f")
							.addElement("i")
							.addElement("MM")
							.addElement("fM")
							.addElement("iM")
							.addElement("MY")
							.addElement("fY")
							.addElement("iY")
							.addElement("ff")
							.addElement("if")
							.addElement("ii")
							.toVector()
						) {
					public String translate(Object p_o) {
						if(p_o.toString().equals(""))return("No Dimemsion");
						if(p_o.toString().equals("M"))return("m");
						if(p_o.toString().equals("c"))return("cm");
						if(p_o.toString().equals("m"))return("mm");
						if(p_o.toString().equals("Y"))return("yard");
						if(p_o.toString().equals("f"))return("feet");
						if(p_o.toString().equals("i"))return("inch");
						if(p_o.toString().equals("MM"))return("m x m");
						if(p_o.toString().equals("fM"))return("ft x m");
						if(p_o.toString().equals("iM"))return("in x m");
						if(p_o.toString().equals("MY"))return("m x yd");
						if(p_o.toString().equals("fY"))return("ft x yd");
						if(p_o.toString().equals("iY"))return("in x yd");
						if(p_o.toString().equals("ff"))return("ft x ft");
						if(p_o.toString().equals("if"))return("in x ft");
						if(p_o.toString().equals("ii"))return("in x in");
						return("");
					}

					@Override
					public int getRowWidth() {
						// TODO Auto-generated method stub
						return 0;
					}
				}
		);
		}
		
		if(getBr() instanceof BiResultStock) {
			if(((BiResultStock) getBr()).getStmovdLink() != null) {
				BiResult sr = getBr().getSubLink(((BiResultStock) getBr()).getStmovdLink());
				JxField sv = jxAdd("list_"+replaceViewName(sr.getView().getName()));
				if(sr instanceof BiResultStockMove) {
					if(getBr().getView().linkOnDemand(sr.getView())) {
						getBr().fetchOneSubLink(getBr().getCurrentCollection(),sr,null) ;
						bindSublinkList(sv , sr);
					}
					((BiResultStockMove) sr).reloadStockMove();
				}
				Listbox lb = (Listbox) sv.getNativeObject();
				ListModelList lm = (ListModelList) lb.getListModel();
				int n = lm.getSize();
				Listitem li = lb.getItemAtIndex(n-1);
				Clients.scrollIntoView(li);
			}
			if(((BiResultStock) getBr()).getStlocLink() != null) {
				BiResult sr = getBr().getSubLink(((BiResultStock) getBr()).getStlocLink());
				JxField sv = jxAdd("list_"+replaceViewName(sr.getView().getName()));
				if(getBr().getView().linkOnDemand(sr.getView())) {
					getBr().fetchOneSubLink(getBr().getCurrentCollection(),sr,null) ;
					bindSublinkList(sv , sr);
				}
			}
		}
		if(!getSessionHelper().getLoginId().equals("hlv"))  {
			jxSetVisible("btRecalCost",false);
		}
		setOnOfUnitStrDivs(br);
	}

	void saveImageFile( org.zkoss.util.media.Media media ) {
			RpcClient rpc = getRpcClient();
			Value v = rpc.callSegment("getFilingMessageId",new Vector());
			rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				int cc = Integer.parseInt(v.toString().substring(4));
				try  {
				    byte[] photoData = media.getByteData();

//					Map<String, String> map = new HashMap<String, String>();
//					photoData = rotatePhoto(photoData, map);
//					String photoSize = map.get("data_size");

					//String filekey = new Sprintf("jxStockImageFiling_%06d").add(cc).toString();
					String filekey = String.format("jxStockImageFiling_%010d_%010d",getBr().getCellInt("st_irg"),cc);  //add stirg to key
					ByteArrayInputStream is = new ByteArrayInputStream(photoData);
				    FilingUtil.storeFile(
				      sessionHelper.getAgent(),
				      null,
				      filekey,
				      "",//mConditionPresetMapMap.customStoreName, 
				      "",//mConditionPresetMapMap.customStoreDesc, 
				    is);
				    is.close();

//				    String sfilekey = storeThumbnal(getBr().getCellInt("st_irg"), cc, photoData, map);
//					String thumbSize = map.get("data_size");
				    
				    TableRec tr = getBr().getSelectUtil().getQueryResult(
				         "select * from multidoc where mdoc_type = 'STIM' and mdoc_mrg = '" + getBr().getCell("st_irg").getInt() + "' order by mdoc_seq desc", null);
				    int seq = 0;
				    if(tr.getRecordCount() > 0) {
				        tr.setRecPointer(0);
				        seq = (Integer) tr.getField("mdoc_seq");
				        seq++;
				    }
				    getBr().getSelectUtil().executeUpdate("insert into multidoc (mdoc_type,mdoc_mrg,mdoc_seq,mdoc_drg,mdoc_ctime,mdoc_cuser,mdoc_doctype,mdoc_filekey,mdoc_sfilekey,mdoc_photosize,mdoc_thumbsize) values (?,?,?,?,?,?,?,?,?,?,?)", 
				        new Wherecl()
				            .appendArgument("STIM")
				            .appendArgument(getBr().getCell("st_irg").getInt())
				            .appendArgument(seq)
				            .appendArgument(cc)
				            .appendArgument(DateUtil.dateToUnixtime(new java.util.Date()))
				            .appendArgument(getLoginId())
				            .appendArgument(media.getContentType())
				            .appendArgument(filekey)
				            /*
				            .appendArgument(sfilekey)
				            .appendArgument(photoSize)
				            .appendArgument(thumbSize)
				            */
				            .appendArgument("")
				            .appendArgument("")
				            .appendArgument(0)
				            );
				    
				    //patch extraimg field
				    /*
				    RpcClient rpc2 = getRpcClient();
					rpc2.callSegment("updateStExtraImg", 
								new VectorUtil()
									.addElement(getBr().getCell("st_irg").getInt())
									.toVector()
							);
					rpc2.close();
					*/
					
				    getBr().refetchCurrent();
				    bindCellCollection(getBr(),curMode);
				    
				} catch (Exception ex) {
				    UniLog.log(ex);
				}
			}
	}
	
	@Override
	public void afterBind() {

		super.afterBind();
		new JxFieldAction("btRecalCost") {
			public void actionPerformed(JxField fd){
                	try {
                		String cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
                		int org = Erpv4Config.getCoWtAvOrg(getSessionHelper(), cocode);
                		CostCalculation.debug(true);
                		CostCalculation.clearCostTable(getSessionHelper(), getBr().getCellInt("st_irg"), org);
                		double avCost = CostCalculation.getWaCost(getSessionHelper(),getBr().getCellInt("st_irg"), org, DateUtil.today());
                		UniLog.log("avCost = " + avCost);
                		CostCalculation.debug(false);
                	} catch (Exception ex) {
                		UniLog.log(ex);
                	}
			}
		};
		new JxFieldChange("locFilter") {
			public boolean valueChanged(JxField fd,String orgValue){  
				BiResult sr = getBr().getSubLink("erpv4.StockMove");
				if(sr != null && sr instanceof BiResultStockMove) {
					int idx = fd.getItemIndex();
					if(idx > 0) {
						((BiResultStockMove) sr).setLocGroup(idx-1);
					} else {
						((BiResultStockMove) sr).setLocGroup(-1);
					}
					getBr().refetchCurrent();
					bindCellCollection(getBr(),curMode);
				}
				/*
				String s = fd.getText();
				if(s != null && !s.trim().equals("")) {
						((BiResultStockMove) sr).setLocFilter(" and stmd_loc = '"+s+"'");
						getBr().refetchCurrent();
						bindCellCollection(getBr(),curMode);
					}
				} else {
					BiResult sr = getBr().getSubLink("erpv4.StockMove");
					if(sr != null && sr instanceof BiResultStockMove) {
						((BiResultStockMove) sr).setLocFilter(null);
						getBr().refetchCurrent();
						bindCellCollection(getBr(),curMode);
					}
				}
				*/
				return(true);
			}
		};
		new JxFieldAction("btUpload") {
			public void actionPerformed(JxField fd){
					UniLog.log("upload Pressed");
					try {
					    Fileupload.get(new EventListener <UploadEvent>(){
				    		public void onEvent(UploadEvent event) {
				        		UniLog.log("upload event catched");
				                org.zkoss.util.media.Media media = event.getMedia();
				                if(media != null) {
				                	if(!media.getContentType().equals("image/jpeg") &&
				                	   !media.getContentType().equals("image/png")) {
				                		messageBox(sessionHelper.getLabel("Only Jpeg/Png Image File Are Accepted"));
				                		return;
				                	}
				                	saveImageFile(media );
				                }
				    		}
					    });
					} catch (Exception ex) {
							UniLog.log(ex);
					}
			}
		};

		new JxFieldChange("mt_tpname") {
			public boolean valueChanged(JxField fd,String orgValue){  
				setOnOfUnitStrDivs(getBr());
				return(true);
			}
		};

	}
	
	void setOnOfUnitStrDivs(BiResult br) {
		setOnOfUnitStrDiv(br.getCell("mt_tpsize1"),jxAdd("st_size1div"));
		setOnOfUnitStrDiv(br.getCell("mt_tpsize2"),jxAdd("st_size2div"));
		setOnOfUnitStrDiv(br.getCell("mt_tpsize3"),jxAdd("st_size3div"));
		
	}
	void setOnOfUnitStrDiv(Cell sizeC,JxField p_divfd) {
		if(sizeC == null || p_divfd == null) return;
		p_divfd.setVisible(!StringUtils.isBlank(sizeC.getString()));
	}
	
	
	@Override
	protected void linkClickedAction(BiResult p_sr,int p_rowIdx,int p_actionType) {
			if(p_actionType == JxField.ACTIONTYPE_DOUBLECLICK && p_sr.getView().getName().equals("erpv4.StockMove")) {
				try {
	        			final ZkForm zkf1 = new ZkForm(null,"zkf/webmenu001.zul");
	        			final CellCollection col = new CellCollection();
//	        			col.addCell("userid", new Cell("donald"));
	        			zkf1.doModal(col,new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									UniLog.log("HAHA clicked");
									if(arg0.getTarget().getId().equals("btOK")) {
//										UniLog.log("Value = " + col.getCell("userid").getString());
//										if(col.getCell("userid").getString().trim().equals("")) {
//											Messagebox.show("User ID Shoule not be blank");
//										} else {
//										SelectUtil su = getBr().getSelectUtil();
//										TableRec tr = su.getQueryResult("select * from webmenu where webm_rg not in (select webmu_mrg from webmenuuser where webmu_user = '"+ col.getCell("userid").getString()+ "')",null);
//										for(int i = 0;i<tr.getRecordCount();i++) {
//											tr.setRecPointer(i);
//											su.executeUpdate("insert into webmenuuser (webmu_mrg,webmu_user,webmu_active) values (?,?,?)", 
//														new Wherecl()
//															.appendArgument(tr.getFieldInt("webm_rg"))
//															.appendArgument(col.getCell("userid").getString())
//															.appendArgument("")
//													);
//										}
//										zkf1.exitModal();
//										}
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
	}	
	
}
