package com.uniinformation.jxapp.axa;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.axa.BiResultAxaClaim;
import com.uniinformation.bicore.axa.BiResultAxaClaim.ClaimInfo;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

//import javafx.scene.control.PopupControl;

public class AxaClaim extends JxZkBiBase {
	static String EMAIL_VIEWNAME = "axa.PolicyEmail";
	static String BENEFIT_VIEWNAME = "axa.ClaimBenefit";
	GipiNamedItemList benifitList;

	class AxaClaimGetItemProperty extends ZkBiGetItemProperty {

		public AxaClaimGetItemProperty(BiResult p_br, JxZkBiBase p_bibase) {
			super(p_br, p_bibase);
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			UniLog.log("myGipi onValueChanged");
		}
		
	}
	AxaClaimGetItemProperty myGipi = null;

	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
//		return(br.doBeforeAdd(cl));
		if(sr.getView().getName().equals("axa.AxaClaimDet")) {
			cl.getCell("axacld_treatment").setItemPropertyInterface(((BiResultAxaClaim) getBr()).getBenefitList());
		}
		return(null);
	}	

	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		/*
		if(myGipi == null) {
			myGipi = new AxaClaimGetItemProperty(p_br.getSubLink("axa.PolicyEmail"),this);	
			setGipi("axa.PolicyEmail",myGipi);	
		}
		*/
		super.bindCellCollection(p_br, mode);
		if(mode == JxZkBiBase.MODE_ADD) {
			jxSetVisible("list_axa_AxaClaimDet",false);
			jxSetVisible("list_axa_ClaimBenefit",false);
		} else {
			if(p_br.getCellString("axaclm_status").equals("Submitted")) {
				jxSetVisible("btClaim",false);
				if(!getSessionHelper().hasAccessRight("#axaadm")) {
					try {
						p_br.getCell("axaclm_status").setMode(Cell.VMODE_DISPONLY);
					} catch (CellException cex) {
						UniLog.log(cex);
					}
				}
				/*
				try {
					p_br.getCurrentCollection().lock();
				} catch(CellException cex) {
					UniLog.log(cex);
				}
				*/
			} else {
				jxSetVisible("btClaim",true);
				jxSetEnable("axaclm_status",true);
				if(StringUtils.isBlank(p_br.getCellString("axaclm_totalstate"))) {
					jxSetEnable("btClaim",true);
//					jxSetEnable("axaclm_status",true);
				} else {
					jxSetEnable("btClaim",false);
//					jxSetEnable("axaclm_status",false);
				}
			}
			jxSetVisible("list_axa_AxaClaimDet",true);
			jxSetVisible("list_axa_ClaimBenefit",true);
			/*
			if(mode == JxZkBiBase.MODE_UPDATE) {
				benifitList = new GipiNamedItemList();
				Vector<BiCellCollection> vv = p_br.getSubLink("axa.ClaimBenefit").getRowCollectionList();
				for(int i=0;i<vv.size();i++) {
					benifitList.appendItem( vv.get(i).getCellString("axapol_benefitcode"), 
							vv.get(i).getCellString("axapol_benefitcode") + "XXX"
							);
				}
			}
			*/
					if(getSessionHelper().isAdminUser()) {
						p_br.getCell("axaclm_status").setItemList(
								new VectorUtil()
									.addElement("Pending")
									.addElement("Confirmed")
									.addElement("Void")
									.addElement("Submitted")
									.toVector()
								);
					}
			{
				BiResult sr = p_br.getSubLink(EMAIL_VIEWNAME);
				if(sr != null) {
					JxField sv = jxAdd("list_"+replaceViewName(sr.getView().getName()));
					sv.setAttribute("mode", "singleClickAction");
					String phno = p_br.getCellString("axaclm_phno");
//					Wherecl wcl = new Wherecl().andUniop("emm_hashtag", "like", "%"+phno+"%");
					String regStr = phno+"..GH|"+phno.substring(0,1)+"XXXXXPH";
					BiResult bfr = p_br.getSubLink(BENEFIT_VIEWNAME);
					for(BiCellCollection bc : bfr.getRowCollectionList()) {
						regStr += "|" + "BF_"+ bc.getCellString("axapol_benefitcode");
					}
					Wherecl wcl = new Wherecl().andUniop("emm_hashtag", "regexp", phno+"..GH|"+regStr);
//					Wherecl wcl = new Wherecl().andUniop("emm_hashtag", "regexp", phno+"..GH|"+phno.substring(0,1)+"XXXXXPH");
					p_br.fetchOneSubLink(p_br.getCurrentCollection(),sr,wcl) ;
					bindSublinkList(sv , sr);
				}
			}
		}
//		jxSetFontColor("axaclm_totalstate",0xff0000);
//		((HtmlBasedComponent) comp).setStyle(String.format("color:#%06x;! important",(p_color & 0xffffff)));
	}
	
	void claimBenifit() {
		{
			Date d1 = getBr().getCellDate("axaclm_poledate");
			if(! d1.before(BiResultAxaClaim.d240401)) {
				try {
					JxField sv=jxAdd("list_axa_AxaClaimDet");
					BiResult slcl = getBr().getSubLink("axa.AxaClaimDet");
					List<ClaimInfo> claimList = ((BiResultAxaClaim) getBr()).claimBenefit2014();
					BiCellCollection col;
					for(int i=0;i<claimList.size();i++) {
						ClaimInfo ci = claimList.get(i);
						if(slcl.getRowCount() <= i) {
							col = slcl.newRowCollection();
							ReturnMsg rtn = slcl.addSubRecord(col, i,"");
							Object tr = rtn.getData();
							int rowIdx = getGipi(slcl.getView().getName()).getIndexOf(tr);
							sv.addItemToList(tr, rowIdx);
						} else {
							Object o = slcl.getTrStatObj(new Integer(i));
							slcl.markDelete( o, false);
							col = slcl.getRowCollectionV(i);
							sv.gridSetDataFormat(-1,i,"remove_deleted");
						}
						try {
							col.getCell("axacld_treatment").set(ci.bfCode);
							col.getCell("axacld_totalamt").set(ci.claimAmt);
							col.getCell("axacld_axaamt").set(ci.axaPaid);
							col.getCell("axacld_copaidamt").set(ci.patientPaid);
						} catch(CellException cex) {
							UniLog.log(cex);
						}
					}
					for(int i=claimList.size();i<slcl.getRowCount();i++) {
						Object o = slcl.getTrStatObj(new Integer(i));
						slcl.markDelete( o, true);
						sv.gridSetDataFormat(-1,i,"add_deleted");
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					messageBox(ex.toString());
				}
				return;
			}
		}
				double totalAmount = getBr().getCellDouble("axaclm_amount");
				double agreedFee = getBr().getCellDouble("axabfg_agreefee");
				String consultClaim = null;
				String medicClaim = null;
				double consultAmt = 0.0;
				double medicAmt = 0.0;
				// TODO Auto-generated method stub
				BiResult slbf = getBr().getSubLink("axa.ClaimBenefit");
				BiResult slcl = getBr().getSubLink("axa.AxaClaimDet");
				try {
					for( BiCellCollection bc : slcl.getRowCollectionList()) {
						bc.getCell("axacld_totalamt").set(0);
					}
					
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				Vector<BiCellCollection> bflist = slbf.getRowCollectionList();
				JxField sv=jxAdd("list_axa_AxaClaimDet");


				/* end with 1 + single or extramed case 1 */
				boolean use8 = false;
				if(consultClaim == null) {
				for(BiCellCollection bc : bflist) {
					if(!bc.getCell("axapol_canuse").getBoolean()) continue;
					int bftype = bc.getCellInt("axabfc_type");
					if(bftype == 1 || bftype == 3 ) {
						if(bftype == 3) {
							use8 = true;
						}
						consultClaim = bc.getCellString("axapol_benefitcode");
						double claimLimit = bc.getCellDouble("axapol_benefit");
						if(claimLimit <= 0) continue;
//				if(bc.getBoolean("axapol_olimitcode")) {
//					if(bc.getCellDouble("axapol_oblimit") > 0 ) {
//						double maxclaim = bc.getCellDouble("axapol_oblimit") - bc.getDouble("axaclm_totalclaim");
//						if(maxclaim < claimLimit) {
//							claimLimit = maxclaim;
//						}
//					}
//				}
//						consultAmt = totalAmount;
						consultAmt = getBr().getCellDouble("axaclm_consfee");
						/*
						if(consultAmt > claimLimit) {
							if(agreedFee > 0) {
								if(consultAmt > agreedFee) consultAmt = agreedFee;
							}
						}
						if(consultAmt < agreedFee) {
							consultAmt = agreedFee;
						}
						*/
						totalAmount = totalAmount - consultAmt;
						if(totalAmount > 0) {
						for(BiCellCollection bcm : bflist) {
							if(!bcm.getCell("axapol_canuse").getBoolean()) continue;
							int bftypem = bcm.getCellInt("axabfc_type");
							if((!use8 && bftypem == 0) || bftypem == 2) {
								double claimLimitm = bcm.getCellDouble("axapol_benefit");

//				if(bc.getBoolean("axapol_olimitcode")) {
//					if(bc.getCellDouble("axapol_oblimit") > 0 ) {
//						double maxclaim = bc.getCellDouble("axapol_oblimit") - bc.getDouble("axaclm_totalclaim");
//						if(maxclaim < claimLimitm) {
//							claimLimitm = maxclaim;
//						}
//					}
//				}
								if(bftypem==0) {
									double claimdeduct = claimLimit < agreedFee ?  claimLimit : agreedFee;
									if(agreedFee > 0) {
									}
									if(claimLimitm <= claimdeduct) continue;
									claimLimitm = claimLimitm-claimdeduct;
								}
								
								medicClaim = bcm.getCellString("axapol_benefitcode");
								medicAmt = totalAmount;
								if(medicAmt > claimLimitm) medicAmt = claimLimitm;
								totalAmount = totalAmount - medicAmt;
								break;
							}
						}
						
						}
						break;
					}
				}
				}
				
				/* single + extramed  case 2 */
				if(consultClaim == null) {
				for(BiCellCollection bc : bflist) {
					if(!bc.getCell("axapol_canuse").getBoolean()) continue;
					int bftype = bc.getCellInt("axabfc_type");
					if(bftype == 0) {
						boolean hasExtraMed = false;
						for(BiCellCollection bcm : bflist) {
							int bftypem = bcm.getCellInt("axabfc_type");
							if(bftypem == 2) {
								hasExtraMed = true;
								break;
							}
						}
						if(!hasExtraMed) continue;
						consultClaim = bc.getCellString("axapol_benefitcode");
						double claimLimit = bc.getCellDouble("axapol_benefit");
//				if(bc.getBoolean("axapol_olimitcode")) {
//					if(bc.getCellDouble("axapol_oblimit") > 0 ) {
//						double maxclaim = bc.getCellDouble("axapol_oblimit") - bc.getDouble("axaclm_totalclaim");
//						if(maxclaim < claimLimit) {
//							claimLimit = maxclaim;
//						}
//					}
//				}
						consultAmt = totalAmount;
						/*
						if(consultAmt > claimLimit) consultAmt = claimLimit;
						if(consultAmt < agreedFee) consultAmt = agreedFee;
						*/
						if(consultAmt > claimLimit) {
							if(agreedFee > 0) {
								if(consultAmt > agreedFee) consultAmt = agreedFee;
							}
						}
						totalAmount = totalAmount - consultAmt;
						if(totalAmount > 0) {
						for(BiCellCollection bcm : bflist) {
							if(!bcm.getCell("axapol_canuse").getBoolean()) continue;
							int bftypem = bcm.getCellInt("axabfc_type");
							if(bftypem == 2) {
								medicClaim = bcm.getCellString("axapol_benefitcode");
								double claimLimitm = bcm.getCellDouble("axapol_benefit");

//				if(bc.getBoolean("axapol_olimitcode")) {
//					if(bc.getCellDouble("axapol_oblimit") > 0 ) {
//						double maxclaim = bc.getCellDouble("axapol_oblimit") - bc.getDouble("axaclm_totalclaim");
//						if(maxclaim < claimLimitm) {
//							claimLimitm = maxclaim;
//						}
//					}
//				}
								
								
								medicAmt = totalAmount;
								if(medicAmt > claimLimitm) medicAmt = claimLimitm;
								totalAmount = totalAmount - medicAmt;
								break;
							}
						}
						}
						break;
					}
				}
				}
				/* single only */
				if(consultClaim == null) {
				for(BiCellCollection bc : bflist) {
					if(!bc.getCell("axapol_canuse").getBoolean()) continue;
					int bftype = bc.getCellInt("axabfc_type");
					if(bftype == 0) {
						consultClaim = bc.getCellString("axapol_benefitcode");
						double claimLimit = bc.getCellDouble("axapol_benefit");
//						if(bc.getBoolean("axapol_olimitcode")) {
//							if(bc.getCellDouble("axapol_oblimit") > 0 ) {
//								double maxclaim = bc.getCellDouble("axapol_oblimit") - bc.getDouble("axaclm_totalclaim");
//								if(maxclaim < claimLimit) {
//									claimLimit = maxclaim;
//								}
//							}
//						}
//						consultAmt = totalAmount;
						consultAmt = getBr().getCellDouble("axaclm_consfee");
						if(consultAmt > claimLimit) consultAmt = claimLimit;
						if(consultAmt < agreedFee) consultAmt = agreedFee;
						totalAmount = totalAmount - consultAmt;
						break;
					}
				}
				}
				int i=0;
				if(consultClaim != null) {
					BiCellCollection col = null;
					if(slcl.getRowCount() <= i) {
						col = slcl.newRowCollection();
						ReturnMsg rtn = slcl.addSubRecord(col, i,"");
						Object tr = rtn.getData();
						int rowIdx = getGipi(slcl.getView().getName()).getIndexOf(tr);
						sv.addItemToList(tr, rowIdx);
					} else {
						Object o = slcl.getTrStatObj(new Integer(i));
						slcl.markDelete( o, false);
						col = slcl.getRowCollectionV(i);
						sv.gridSetDataFormat(-1,i,"remove_deleted");
					}
					try {
						col.getCell("axacld_treatment").set(consultClaim);
						col.getCell("axacld_totalamt").set(consultAmt);
					} catch(CellException cex) {
						UniLog.log(cex);
					}
					i++;
				}
				if(medicClaim != null) {
					BiCellCollection col = null;
					if(slcl.getRowCount() <= i) {
						col = slcl.newRowCollection();
						ReturnMsg rtn = slcl.addSubRecord(col, i,"");
						Object tr = rtn.getData();
						int rowIdx = getGipi(slcl.getView().getName()).getIndexOf(tr);
						sv.addItemToList(tr, rowIdx);
					} else {
						Object o = slcl.getTrStatObj(new Integer(i));
						slcl.markDelete( o, false);
						col = slcl.getRowCollectionV(i);
						sv.gridSetDataFormat(-1,i,"remove_deleted");
					}
					try {
						col.getCell("axacld_treatment").set(medicClaim);
						col.getCell("axacld_totalamt").set(medicAmt);
					} catch(CellException cex) {
						UniLog.log(cex);
					}
					i++;
				}
				{
					double surg = getBr().getCellDouble("axaclm_surg");
					if(surg > 0) {
						for(BiCellCollection bc : bflist) {
							if(!bc.getCell("axapol_canuse").getBoolean()) continue;
							int bftype = bc.getCellInt("axabfc_type");
							if(bftype == 6) {
								BiCellCollection col = null;
								double claimLimit = bc.getCellDouble("axapol_benefit");
								if(claimLimit <= 0) continue;
								/*
								double claimAmt = surg;
								if(claimAmt > claimLimit) claimAmt = claimLimit;
								*/
								if(slcl.getRowCount() <= i) {
									col = slcl.newRowCollection();
									ReturnMsg rtn = slcl.addSubRecord(col, i,"");
									Object tr = rtn.getData();
									int rowIdx = getGipi(slcl.getView().getName()).getIndexOf(tr);
									sv.addItemToList(tr, rowIdx);
								} else {
									Object o = slcl.getTrStatObj(new Integer(i));
									slcl.markDelete( o, false);
									col = slcl.getRowCollectionV(i);
									sv.gridSetDataFormat(-1,i,"remove_deleted");
								}
								try {
									col.getCell("axacld_treatment").set(bc.getCellString("axapol_benefitcode"));
									col.getCell("axacld_totalamt").set(surg);
								} catch(CellException cex) {
									UniLog.log(cex);
								}
								i++;
								break;
							}
						}
					}
				}
				{
					double xray = getBr().getCellDouble("axaclm_xray");
					if(xray > 0) {
						boolean isAdvance = false;
						if(getBr().getCell("axaclm_advxray").getBoolean()) {
							isAdvance = true;
						}
						for(BiCellCollection bc : bflist) {
							if(!bc.getCell("axapol_canuse").getBoolean()) continue;
							int bftype = bc.getCellInt("axabfc_type");
							if(bftype == 8 || (bftype == 7 && !isAdvance)) {
								BiCellCollection col = null;
								double claimLimit = bc.getCellDouble("axapol_benefit");
								if(claimLimit <= 0) continue;
								/*
								double claimAmt = surg;
								if(claimAmt > claimLimit) claimAmt = claimLimit;
								*/
								if(slcl.getRowCount() <= i) {
									col = slcl.newRowCollection();
									ReturnMsg rtn = slcl.addSubRecord(col, i,"");
									Object tr = rtn.getData();
									int rowIdx = getGipi(slcl.getView().getName()).getIndexOf(tr);
									sv.addItemToList(tr, rowIdx);
								} else {
									Object o = slcl.getTrStatObj(new Integer(i));
									slcl.markDelete( o, false);
									col = slcl.getRowCollectionV(i);
									sv.gridSetDataFormat(-1,i,"remove_deleted");
								}
								try {
									col.getCell("axacld_treatment").set(bc.getCellString("axapol_benefitcode"));
									col.getCell("axacld_totalamt").set(xray);
								} catch(CellException cex) {
									UniLog.log(cex);
								}
								i++;
								break;
							}
						}
					}
				}				
				
				
				for(;i<slcl.getRowCount();i++) {
					Object o = slcl.getTrStatObj(new Integer(i));
					slcl.markDelete( o, true);
					sv.gridSetDataFormat(-1,i,"add_deleted");
				}
		
	}
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btClaim") {
			@Override
			public void actionPerformed(JxField jxfield) {
				
//				double totalAmount = getBr().getCellDouble("axaclm_amount");
//				double agreedFee = getBr().getCellDouble("axabfg_agreefee");
//				String consultClaim = null;
//				String medicClaim = null;
//				double consultAmt = 0.0;
//				double medicAmt = 0.0;
//				// TODO Auto-generated method stub
//				BiResult slbf = getBr().getSubLink("axa.ClaimBenefit");
//				BiResult slcl = getBr().getSubLink("axa.AxaClaimDet");
//				Vector<BiCellCollection> bflist = slbf.getRowCollectionList();
//				JxField sv=jxAdd("list_axa_AxaClaimDet");
//
//
//				/* end with 1 + single or extramed case 1 */
//				if(consultClaim == null) {
//				for(BiCellCollection bc : bflist) {
//					if(bc.getCell("axapol_canuse").getBoolean()) continue;
//					int bftype = bc.getCellInt("axabfc_type");
//					if(bftype == 1) {
//						consultClaim = bc.getCellString("axapol_benefitcode");
//						double claimLimit = bc.getCellDouble("axapol_benefit");
//						consultAmt = totalAmount;
//						if(consultAmt > claimLimit) consultAmt = claimLimit;
//							else if(consultAmt < agreedFee) consultAmt = agreedFee;
//						totalAmount = totalAmount - consultAmt;
//						if(totalAmount > 0) {
//						for(BiCellCollection bcm : bflist) {
//							if(bcm.getCell("axapol_canuse").getBoolean()) continue;
//							int bftypem = bcm.getCellInt("axabfc_type");
//							if(bftypem == 0 || bftypem == 2) {
//								double claimLimitm = bcm.getCellDouble("axapol_benefit");
//								if(bftypem==0) {
//									if(claimLimitm <= claimLimit) continue;
//									claimLimitm = claimLimitm-claimLimit;
//								}
//								medicClaim = bcm.getCellString("axapol_benefitcode");
//								medicAmt = totalAmount;
//								if(medicAmt > claimLimitm) medicAmt = claimLimitm;
//								totalAmount = totalAmount - medicAmt;
//								break;
//							}
//						}
//						
//						}
//						break;
//					}
//				}
//				}
//				
//				/* single + extramed  case 2 */
//				if(consultClaim == null) {
//				for(BiCellCollection bc : bflist) {
//					if(bc.getCell("axapol_canuse").getBoolean()) continue;
//					int bftype = bc.getCellInt("axabfc_type");
//					if(bftype == 0) {
//						boolean hasExtraMed = false;
//						for(BiCellCollection bcm : bflist) {
//							int bftypem = bcm.getCellInt("axabfc_type");
//							if(bftypem == 2) {
//								hasExtraMed = true;
//								break;
//							}
//						}
//						if(!hasExtraMed) continue;
//						consultClaim = bc.getCellString("axapol_benefitcode");
//						double claimLimit = bc.getCellDouble("axapol_benefit");
//						consultAmt = totalAmount;
//						if(consultAmt > claimLimit) consultAmt = claimLimit;
//						if(consultAmt < agreedFee) consultAmt = agreedFee;
//						totalAmount = totalAmount - consultAmt;
//						if(totalAmount > 0) {
//						for(BiCellCollection bcm : bflist) {
//							if(bcm.getCell("axapol_canuse").getBoolean()) continue;
//							int bftypem = bcm.getCellInt("axabfc_type");
//							if(bftypem == 2) {
//								medicClaim = bcm.getCellString("axapol_benefitcode");
//								double claimLimitm = bcm.getCellDouble("axapol_benefit");
//								medicAmt = totalAmount;
//								if(medicAmt > claimLimitm) medicAmt = claimLimitm;
//								totalAmount = totalAmount - medicAmt;
//								break;
//							}
//						}
//						}
//						break;
//					}
//				}
//				}
//
//				/* single only */
//				if(consultClaim == null) {
//				for(BiCellCollection bc : bflist) {
//					if(bc.getCell("axapol_canuse").getBoolean()) continue;
//					int bftype = bc.getCellInt("axabfc_type");
//					if(bftype == 0) {
//						consultClaim = bc.getCellString("axapol_benefitcode");
//						double claimLimit = bc.getCellDouble("axapol_benefit");
//						consultAmt = totalAmount;
//						if(consultAmt > claimLimit) consultAmt = claimLimit;
//						if(consultAmt < agreedFee) consultAmt = agreedFee;
//						totalAmount = totalAmount - consultAmt;
//						break;
//					}
//				}
//				}
//				int i=0;
//				if(consultClaim != null) {
//					BiCellCollection col = null;
//					if(slcl.getRowCount() <= 0) {
//						col = slcl.newRowCollection();
//						ReturnMsg rtn = slcl.addSubRecord(col, i,"");
//						Object tr = rtn.getData();
//						int rowIdx = getGipi(slcl.getView().getName()).getIndexOf(tr);
//						sv.addItemToList(tr, rowIdx);
//					} else {
//						Object o = slcl.getTrStatObj(new Integer(0));
//						slcl.markDelete( o, false);
//						col = slcl.getRowCollectionV(0);
//						sv.gridSetDataFormat(-1,0,"remove_deleted");
//					}
//					try {
//						col.getCell("axacld_treatment").set(consultClaim);
//						col.getCell("axacld_totalamt").set(consultAmt);
//					} catch(CellException cex) {
//						UniLog.log(cex);
//					}
//					i=1;
//				}
//				if(medicClaim != null) {
//					BiCellCollection col = null;
//					if(slcl.getRowCount() <= 1) {
//						col = slcl.newRowCollection();
//						ReturnMsg rtn = slcl.addSubRecord(col, 1,"");
//						Object tr = rtn.getData();
//						int rowIdx = getGipi(slcl.getView().getName()).getIndexOf(tr);
//						sv.addItemToList(tr, rowIdx);
//					} else {
//						Object o = slcl.getTrStatObj(new Integer(1));
//						slcl.markDelete( o, false);
//						col = slcl.getRowCollectionV(1);
//						sv.gridSetDataFormat(-1,1,"remove_deleted");
//					}
//					try {
//						col.getCell("axacld_treatment").set(medicClaim);
//						col.getCell("axacld_totalamt").set(medicAmt);
//					} catch(CellException cex) {
//						UniLog.log(cex);
//					}
//					
//					i=2;
//				}
//				for(;i<slcl.getRowCount();i++) {
//					Object o = slcl.getTrStatObj(new Integer(i));
//					slcl.markDelete( o, true);
//					sv.gridSetDataFormat(-1,i,"add_deleted");
//				}
				
				if(
					(!getBr().getCellDate("axaclm_date").before(getBr().getCellDate("axaphr_nextandate"))) ||
					( getBr().getCellDate("axacvp_termination").after(DateUtil.minDate) &&
							(!getBr().getCellDate("axaclm_date").before(getBr().getCellDate("axacvp_termination"))) 
							)
					) {
					Messagebox.show(sessionHelper.getLabel("Policy Not Effective or Expired. Continue to Claim ?"), 
							sessionHelper.getLabel("Confirmation"), Messagebox.OK | Messagebox.CANCEL,Messagebox.QUESTION,
							new org.zkoss.zk.ui.event.EventListener(){
			            		public void onEvent(Event e){
			            			if(Messagebox.ON_OK.equals(e.getName())){
			            				claimBenifit();
			            				setDirtyFlag(true);
			            			}
			            		}
			            	}
					);	
				} else {
					claimBenifit();
					setDirtyFlag(true);
				}
			}
		
		};

		new JxFieldAction("btMoreIcd") {
			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				try { 
				pickBySelect(getBr().getSessionHelper(),"axa.IcdDetail",null,
							new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
									CellCollection col = (CellCollection) arg0.getData();
									Cell cc = col.getCell("icdd_code");
									UniLog.log("add icd " + cc.getString());
									((BiResultAxaClaim) getBr()).addItemToIcdList(
												col.getCellString("icdd_code"),
												col.getCellString("icdd_desc")
											);
								}
					
							}
						);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			
		};
		new JxFieldAction("list_"+replaceViewName(EMAIL_VIEWNAME)) {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				UniLog.log("Email onclick");
			}
			
		};
		
		JxField jxf = jxAdd("axaclm_totalstate");
		if(jxf != null) {
			HtmlBasedComponent hc = (HtmlBasedComponent) jxf.getNativeObject();
			hc.setStyle(String.format("color:#%06x;! important",0xff0000));
			
		}
		
//		jxSetFontColor("axaclm_totalstate",0xff0000);
//		((HtmlBasedComponent) comp).setStyle(String.format("color:#%06x;! important",(p_color & 0xffffff)));
	}
	
	@Override
	protected ReturnMsg beforeUpdate(BiResult br)
	{
		if(!getSessionHelper().isAdminUser() && br.getCellString("axaclm_status").equals("Submitted")) {
			return(new ReturnMsg(false,"Cannot Update Submitted Records"));
		}
		if(br.getCellString("axaclm_status").equals("Confirmed")) {
			if(StringUtils.isBlank(br.getCellString("axaclm_voucher"))) {
				return(new ReturnMsg(false,"Voucher is blank, Cannot Confirm "));
			}
			if(StringUtils.isBlank(br.getCellString("axaclm_diagnosis"))) {
				return(new ReturnMsg(false,"Diagnosis is blank, Cannot Confirm "));
			}
			if(!StringUtils.isBlank(br.getCellString("axaclm_totalstate"))) {
				return(new ReturnMsg(false,"Cannot Confirm " + br.getCellString("axaclm_totalstate")));
			}
			if(!StringUtils.isBlank(br.getCellString("axaclm_totalstate"))) {
				return(new ReturnMsg(false,"Cannot Confirm " + br.getCellString("axaclm_totalstate")));
			}
		}
		return(ReturnMsg.defaultOk);
	}
	@Override
	protected ReturnMsg afterAdd(BiResult br)
	{
		/*
		ReturnMsg rtn = new ReturnMsg(false,"Policy Holder Cannot Claim");
		return(rtn);
		*/
		return(ReturnMsg.defaultOk);
	}

	@Override
	protected void linkClickedAction(BiResult p_sr,int p_rowIdx,int p_actionType) {
		if(p_sr.getView().getName().equals(EMAIL_VIEWNAME)) {
			int row = 0;
			JxField sv = jxAdd("list_"+replaceViewName(EMAIL_VIEWNAME));
			if(p_rowIdx >= 0) {
				Object o = sv.gridGetValue(-1, p_rowIdx);
				row = getGipi(EMAIL_VIEWNAME).getIndexOf(o);
				BiCellCollection bc = p_sr.getRowCollectionV(row);
				int sid = bc.getSid();
				String policy = getBr().getCellString("axaclm_phno");
//					Wherecl wcl = new Wherecl().andUniop("emm_hashtag", "regexp", phno+"..GH|"+phno.substring(0,1)+"XXXXXPH");
				
				String regStr = policy+"..GH|"+policy.substring(0, 1)+"XXXXXPH";
					BiResult bfr = getBr().getSubLink(BENEFIT_VIEWNAME);
					for(BiCellCollection embc : bfr.getRowCollectionList()) {
						regStr += "|" + "BF_"+ embc.getCellString("axapol_benefitcode");
					}
				
//				doPopupPolicyEmail(getSessionHelper(),"axa.EmailMessage","emm_hashtag regexp '"+policy+"..GH|"+policy.substring(0, 1)+"XXXXXPH'",sid,"View");
				doPopupPolicyEmail(getSessionHelper(),"axa.EmailMessage","emm_hashtag regexp '"+ regStr + "'" ,sid,"View");
				/*
					String regStr = phno+"..GH|"+phno.substring(0,1)+"XXXXXPH";
					BiResult bfr = p_br.getSubLink(BENEFIT_VIEWNAME);
					for(BiCellCollection bc : bfr.getRowCollectionList()) {
						regStr += "|" + "BF_"+ bc.getCellString("axapol_benefitcode");
					}
					*/
				
			}
		}
	}

	JxZkBiBase popupJx = null;
	Window emailPopup = null;
//	PopupControl emailContent = null;
	BiResult psr = null;
	int recidx = -1;
	void doPopupPolicyEmail(SessionHelper p_sh,String p_viewName,String p_condition,int p_sid,String p_mode) {
		if(emailPopup == null) {
//			emailPopup = ZkUtil.newPopupWindow("Email Content",(Component) getNativeComponent());
			emailPopup = ZkUtil.newPopupWindow("Email Content",ZkUtil.getMainComp());
   			emailPopup.setClosable(false);
//			emailPopup.setWidth("1920px");
//			emailPopup.setHeight("1000px");
            emailPopup.setWidth("95%");
            emailPopup.setHeight("95%");
			emailPopup.setContentStyle("overflow:auto;");
			psr = p_sh.getBiSchema().getViewByName(p_viewName).newBiResult(p_sh.getLoginId(), null, null, p_sh);
			popupJx = JxZkBiBase.buildDetailWindow(psr, emailPopup, false, true, 
			new JxZkBiBaseCallback()  {
				public void biBaseRefresh(BiResult p_br) {
				}
				public void biBaseOpen() {
				}
				public void biBaseRefreshItem(Object p_obj) {
				}
				public void biBaseRefreshListitems(Object p_obj) {
				}
				public void biBaseClose(BiResult p_br) {
				}
				@Override
				public ReturnMsg fetchNext(BiResult p_br) {
					if(recidx >= psr.getRowCount() -1 ) return(new ReturnMsg (false,"No Next Record"));
					recidx++;
					psr.fetchOneRecV(recidx);
					return(ReturnMsg.defaultOk);
					// TODO Auto-generated method stub
				}
				@Override
				public ReturnMsg fetchPrevious(BiResult p_br) {
					// TODO Auto-generated method stub
					if(recidx <= 0) return(new ReturnMsg (false,"No Previous Record"));
					recidx--;
					psr.fetchOneRecV(recidx);
					return(ReturnMsg.defaultOk);
				}
				@Override
				public String getExtraInfo() {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public Boolean hasNextRec() {
					// TODO Auto-generated method stub
					return(psr.getRowCount() > recidx + 1);
				}
				@Override
				public Boolean hasPrevRec() {
					// TODO Auto-generated method stub
					return(recidx > 0);
				}
				@Override
				public HashSet<BiColumn> getVisibleColumns(BiResult p_br) {
					// TODO Auto-generated method stub
					return null;
				}
			}
					
			);
			
		}
   		psr.clearCondition();
		psr.addCustomCondition(p_condition);
		psr.query(true);
		if(psr.getRowCount() > 0 ) {
			for(recidx =0;recidx<psr.getRowCount();recidx++) {
				psr.loadOneRecV(recidx);
				if(psr.getCurrentCollection().getSid() == p_sid)  {
					psr.fetchOneRecV(recidx);
					psr.clearLastUpdate();
					popupJx.bindCellCollection(psr,JxZkBiBase.MODE_UPDATE);
					popupJx.jxSetVisible("btUpdate",false);
					popupJx.jxSetVisible("btAdd",false);
					popupJx.showForm();	
					popupJx.doModalUpdate();
					break;
				}
			}
			/*
			psr.fetchOneRecV(0);
			psr.clearLastUpdate();
		    popupJx.bindCellCollection(psr,JxZkBiBase.MODE_UPDATE);
		    popupJx.jxSetVisible("btUpdate",false);
		    popupJx.jxSetVisible("btAdd",false);
		    popupJx.showForm();	
		    popupJx.doModalUpdate();
		    */
		} else {
			Messagebox.show(
				"Fatal System Error : Reason Unknown. Code 3102",
				sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
		
		}		
	}	

}
