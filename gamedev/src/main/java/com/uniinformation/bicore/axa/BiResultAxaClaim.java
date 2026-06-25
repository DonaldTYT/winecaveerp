package com.uniinformation.bicore.axa;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.Filedownload;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
//import com.uniinformation.utils.AbstractGetItemProperty;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZipUtil;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.SessionHelper;


public class BiResultAxaClaim extends BiResultErpv4 {
	GipiNamedItemList benifitList;
	GipiNamedItemList icdList;
	
	CellValueAction setIdcDesc;
//	private int classno = 0;
	public BiResultAxaClaim(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		
	}
	public void addItemToIcdList(String p_code,String p_name) throws Exception {
			int idx = icdList.getIndexOf(p_code);
			if(idx < 0) {
				icdList.appendItem( p_code,p_code + " " + p_name);
				getCell("axaclm_diagnosis").setItemPropertyInterface(icdList);
			}
			getCell("axaclm_diagnosis").set(p_code);
			
	}

	@Override
	protected void createColumnCells(BiCellCollection p_col) {
		super.createColumnCells(p_col);
		if(icdList == null) {
				try {
					TableRec tr = null;
					
					tr = getSelectUtil().getQueryResult("select * from icddetail where icdd_code in (select distinct axaclm_diagnosis from axaclaim)  order by icdd_desc");
					icdList = new GipiNamedItemList();
						icdList.appendItem( "","");
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						icdList.appendItem( 
								tr.getFieldString("icdd_code"),
								tr.getFieldString("icdd_code") + " " + tr.getFieldString("icdd_desc")
								);
					}
					//col.getCell("axaclm_diagnosis").set("");
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			
		}
		p_col.getCell("axaclm_diagnosis").setItemPropertyInterface(icdList);
//		if(setIdcDesc == null) setIdcDesc = new CellValueAction () {
//
//			
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			ColumnCell cc = (ColumnCell) p_value;
//			BiCellCollection col = cc.getCollection();
//			if(cc.getCellLabel().equals("icdm_rg")) {
//				try {
//					TableRec tr = null;
//					/*
//					if( col.getCellInt("icdm_rg") > 0) {
//						tr = getSelectUtil().getQueryResult("select * from icddetail where icdd_rg = " + col.getCellInt("icdm_rg") + " order by icdd_desc");
//					} else {
//						tr = getSelectUtil().getQueryResult("select * from icddetail order by icdd_desc");
//					}
//					*/
//					tr = getSelectUtil().getQueryResult("select * from icddetail where icdd_rg = " + col.getCellInt("icdm_rg") + " order by icdd_desc");
//					GipiNamedItemList icdList = new GipiNamedItemList();
//						icdList.appendItem( "","");
//					for(int i=0;i<tr.getRecordCount();i++) {
//						tr.setRecPointer(i);
//						icdList.appendItem( 
//								tr.getFieldString("icdd_code"),
//								tr.getFieldString("icdd_code") + " " + tr.getFieldString("icdd_desc")
//								);
//					}
//					//col.getCell("axaclm_diagnosis").set("");
//					col.getCell("axaclm_diagnosis").setItemPropertyInterface(icdList);
//				} catch (Exception ex) {
//					UniLog.log(ex);
//					throw new CellException(ex.toString());
//				}
//			}
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//			// TODO Auto-generated method stub
//			
//		}
//		
//		};
//		p_col.getCell("icdm_rg").addAction(setIdcDesc);
	}
	
	private int benefitClaimOrder(int p_btype) {
		switch(p_btype) {
		case 1 : return (0);
		case 0 : return (1);
		case 3 : return (2);
		}
		return(9999);
	}

	@Override
	protected void afterFetch() {
		benifitList = new GipiNamedItemList();
		Vector<BiCellCollection> vv ;
		vv = getSubLink("axa.AxaClaimDet").getRowCollectionList();
		for(int i=0;i<vv.size();i++) {
			vv.get(i).getCell("axacld_treatment").setItemPropertyInterface(benifitList);
		}

		vv = getSubLink("axa.AxaSTA2").getRowCollectionList();
		double dd = 0;
		int dv = 0;
		for(int i=0;i<vv.size();i++) {
			dd += vv.get(i).getCellDouble("axasta_payamount");
			dv += vv.get(i).getCellInt("axasta_totaldays");
		}
		

		try {
			getCell("axaclm_totalclaim").set(dd);
			getCell("axaclm_totalvisit").set(dv);
			getCell("axaclm_remaining").set("");
		} catch(CellException cex) {
			UniLog.log(cex);
		}
		vv = getSubLink("axa.ClaimBenefit").getRowCollectionList();
		Collections.sort(vv,new Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				CellCollection cc;
			int t0 = benefitClaimOrder(((CellCollection) arg0).getCellInt("axabfc_type"));
			int t1 = benefitClaimOrder(((CellCollection) arg1).getCellInt("axabfc_type"));
				// TODO Auto-generated method stub
				return(Integer.compare(t0,t1));
			}
			
		});
		/*
		for(int i=0;i<vv.size();i++) {
			benifitList.appendItem( vv.get(i).getCellString("axapol_benefitcode"), 
				vv.get(i).getCellString("axapol_benefitcode") + ""
			);
		}
		*/

		for(BiCellCollection bc : vv) {
			try {
			TableRec tr = getSelectUtil().getQueryResult("select count(*) pdclaimcnt,sum (axacld_axaamt) pdclaimamt from axaclaimdet where axacld_phno = ? and axacld_certdep = ? and axacld_treatment = ? and axacld_date between ? and ? ",
						new Wherecl()
							.appendArgument(getCellString("axaclm_phno"))
							.appendArgument(getCellString("axaclm_certdep"))
							.appendArgument(bc.getCellString("axapol_benefitcode"))
							.appendArgument(bc.getCell("axapol_effectdate").getDate())
							.appendArgument(getCell("axaclm_date").getDate())
//							.appendArgument(DateUtil.prevday(getCell("axaclm_date").getDate()))
					);
					tr.setRecPointer(0);
					bc.getCell("axasta_totaldays").set( bc.getCellInt("axasta_totaldays") + tr.getFieldInt("pdclaimcnt"));
					bc.getCell("axasta_payamount").set( bc.getCellDouble("axasta_payamount") + tr.getFieldDouble("pdclaimamt"));
					if(bc.getBoolean("axapol_olimitcode")) {
						getCell("axaclm_totalvisit").set( getCellInt("axaclm_totalvisit") +tr.getFieldInt("pdclaimcnt"));
						getCell("axaclm_totalclaim").set( getCellDouble("axaclm_totalclaim") +tr.getFieldDouble("pdclaimamt"));
					}
			} catch (Exception ex)	 {
				UniLog.log(ex);
			}
			
		}
			try {
				getCell("axaclm_totalstate").set("");
		for(BiCellCollection bc : vv) {
			bc.getCell("axapol_canuse").set(true);
			benifitList.appendItem( bc.getCellString("axapol_benefitcode"), 
				bc.getCellString("axapol_benefitcode") + ""
			);
			
			/*
			if(bc.getCellInt("axapol_noofday") > 0 && bc.getCellInt("axasta_totaldays") >= bc.getCellInt("axapol_noofday")) {
				try {
					bc.getCell("axapol_canuse").set(true);
				} catch (CellException cex) {
					UniLog.log(cex);
				}
			}
			*/
				if(bc.getCellInt("axapol_noofday") > 0 ) {
					if(bc.getCellInt("axasta_totaldays") >= bc.getCellInt("axapol_noofday")) {
						bc.getCell("axapol_canuse").set(false);
					}
				} else {
					if(bc.getCellDouble("axasta_paymount") >= bc.getCellDouble("axapol_benefit")) {
						bc.getCell("axapol_canuse").set(false);
					}
				}
				if(bc.getBoolean("axapol_olimitcode")) {
					if(bc.getCellInt("axapol_ovlimit") > 0 ) {
						if(bc.getCellInt("axaclm_totalvisit") >= bc.getCellInt("axapol_ovlimit")  ) {
							bc.getCell("axapol_canuse").set(false);
							getCell("axaclm_totalstate").set("Benefit Used Up, Cannot Claim");
							getCell("axaclm_remaining").set("No. of visit used up");
						} else {
							
						}
					}
					if(bc.getCellDouble("axapol_oblimit") > 0 ) {
						if(bc.getCellDouble("axaclm_totalclaim") >= bc.getCellDouble("axapol_oblimit")  ) {
							bc.getCell("axapol_canuse").set(false);
							getCell("axaclm_totalstate").set("Benefit Used Up, Cannot Claim");
							getCell("axaclm_remaining").set("Claim amount used up");
						}
					}
				}
		}
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			
		try {
//			vv = getSubLink("axa.ClaimBenefit").getRowCollectionList();
			boolean canclaim = false;
			int visitPerBenefit = 0;
			for(BiCellCollection bc : vv) {
				int btorder = benefitClaimOrder(bc.getCellInt("axabfc_type"));
				if(btorder < 9999) {
				if(bc.getCell("axapol_canuse").getBoolean()) {
					if(bc.getCellInt("axapol_noofday") <= 0) {
						visitPerBenefit += 1000000;
					} else {
						visitPerBenefit += 
								(bc.getCellInt("axapol_noofday") - bc.getCellInt("axasta_totaldays"));
					}
					break;
				}
				}
			}
			for(BiCellCollection bc : vv) {
				if(bc.getCell("axapol_canuse").getBoolean()) {
					int btorder = benefitClaimOrder(bc.getCellInt("axabfc_type"));
					if(btorder < 9999) {
							canclaim = true;
							if(bc.getBoolean("axapol_olimitcode")) {
								if(bc.getCellInt("axapol_ovlimit") > 0 ) {
									int vm = bc.getCellInt("axapol_ovlimit") - bc.getCellInt("axaclm_totalvisit");
									if(vm < visitPerBenefit) visitPerBenefit = vm;
									/*
									getCell("axaclm_remaining").set(
												""+(bc.getCellInt("axapol_ovlimit") - bc.getCellInt("axaclm_totalvisit")) + " visit"
											); 
									*/
									if(visitPerBenefit >= 1000000) {
										getCell("axaclm_remaining").set("No Limit"); 
									} else {
										getCell("axaclm_remaining").set(
													""+ visitPerBenefit + " visit"
												); 
									}
								} else {
									String sss = null;
									if(visitPerBenefit < 1000000) {
										sss = "" + visitPerBenefit + " visit and ";
									} else {
										sss = "";
									}
									getCell("axaclm_remaining").set(
											String.format("$%.2f", (bc.getCellDouble("axapol_oblimit")  - bc.getCellDouble("axaclm_totalclaim") ))
											); 
								}
							} else {
								if(visitPerBenefit >= 1000000) {
									getCell("axaclm_remaining").set("No Limit"); 
								} else {
									getCell("axaclm_remaining").set(
												""+ visitPerBenefit + " visit"
											); 
								}
							}
							break;
					}
				}
			}

			if(!canclaim) {
				if(StringUtils.isBlank(getCellString("axaclm_totalstate"))) {
					getCell("axaclm_totalstate").set("No Valid Benifit");
				}
			}
			if(
				(!getCellDate("axaclm_date").before(getCellDate("axaphr_nextandate"))) ||
				( getCellDate("axacvp_termination").after(DateUtil.minDate) &&
						(!getCellDate("axaclm_date").before(getCellDate("axacvp_termination"))) 
						)
				) {
				getCell("axaclm_totalstate").set("Terminated, Cannot Claim");
			}
		} catch (Exception cex) {
			UniLog.log(cex);
		}
		
	}
	
	public AbstractGetItemProperty getBenefitList() {
		return(benifitList);
	}
	
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(p_lookupTable.getName().equals("axabfgrp")) {
			if(wcl == null ) wcl = new Wherecl();
			wcl.appendString(" and axabfgrp.axabfg_enabled='Y'").stripAnd();
		}
		if(Erpv4Config.isMultiCompany(sh)) {
		if(p_lookupTable.getName().equals("cldoctor")) {
			if(wcl == null ) wcl = new Wherecl();
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			wcl.appendString(" and cldoctor.cldoc_cocode = '"+cocode+"' ").stripAnd();
		}
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(!sh.hasAccessRight("#allclaim")) {
			BiColumn locCol = getColumnByLabel("cldoc_cocode");
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			Wherecl wcl1 = new Wherecl();
			wcl1.appendString(" and " + "cldoc_cocode = '"+cocode+"' ").stripAnd();
			p_where.andWherecl(wcl1);
			if(ht == null) ht = new HashSet<BiTable>();
			ht.add(locCol.getTable());
		}
		return(ht);
	}	
	
	/*
	@Override
	public boolean fetchOneRecV(int p_tridx)
	{
		boolean isFlexi = false;
		classno = 0;
		if(getCell("axaphr_plantype") != null) {
			if(getCellString("axaphr_plantype").equals("F")) isFlexi = true;
		}
		try {
			TableRec tr = getSelectUtil().getQueryResult("select * from axacvpd where axacvd_phno = ? and axacvd_certdep = ? and axacvd_effectdate <= ? and axacvd_termination >= ? order by axacvd_effectdate desc",
					new Wherecl()
						.appendArgument(getCellString("axaclm_phno"))
						.appendArgument(getCellString("axaclm_certdep"))
						.appendArgument(getCellString("axaclm_date"))
						.appendArgument(getCellString("axaclm_date"))
				);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				if(isFlexi) {
					classno = tr.getFieldInt("axacvd_occlass");
				} else {
					classno = tr.getFieldInt("axacvd_ohclass");
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		
		return(super.fetchOneRecV(p_tridx));
	}
	*/

	public void writeOneClaimRecord(HashMap<String,Object> p_dataHM, BiCellCollection bc) throws Exception {
		String id = "";
		if (StringUtils.startsWithAny(bc.getCellString("axaclm_phno"), "1")) {
			id = "1";
		}
		if (StringUtils.startsWithAny(bc.getCellString("axaclm_phno"), "0","4")) {
			id = "04";
		}
		if (StringUtils.startsWithAny(bc.getCellString("axaclm_phno"), "7")) {
			id = "7";
		}
		
		
		BufferedWriter bw = (BufferedWriter)p_dataHM.get("bw" + id);
		if (bw == null) {
			UniLog.log1("ignore bw not available");
			return;
		}
		
		Date minDate = (Date) p_dataHM.get("minDate" + id);
		Date maxDate = (Date) p_dataHM.get("maxDate" + id);
		Date curDate = bc.getCell("axaclm_date").getDate();
		if (curDate != null && (minDate == null || curDate.compareTo(minDate) < 0)) {
			p_dataHM.put("minDate" + id, curDate);
		}
		if (curDate != null && (maxDate == null || curDate.compareTo(maxDate) > 0)) {
			p_dataHM.put("maxDate" + id, curDate);
		}
		
		String tm = bc.getCellString("axacld_treatment");
		if(StringUtils.isBlank(tm)) return; /* skip if benifit code is blank added 2025/09/10 */
		if("SURG".equals(tm)) return; /* SURG skiped as requested by AXA */
		String output = 
		String.format("%-6s-%7s      %4d                              %-30s  %8s%-7s%-20s%11s%11s%11s%-8s        %-8s%-5s%-41s%-8s",
						bc.getCellString("axaclm_phno"),
						bc.getCellString("axaclm_certno"),
						bc.getCellInt("axaclm_dependno"),
						bc.getCellString("axacvp_covername"),
						DateUtil.toDateString(bc.getCell("axaclm_date").getDate(), "yyyymmdd"),
						bc.getCellString("axaclm_voucher"),
						bc.getCellString("axacld_treatment"),
                        StringUtil.ftostr(bc.getCellDouble("axacld_totalamt"),"#######0.00"),
                        StringUtil.ftostr(bc.getCellDouble("axacld_axaamt"),"#######0.00"),
                        StringUtil.ftostr(bc.getCellDouble("axacld_copaidamt"),"#######0.00"),
						bc.getCellString("axaclm_diagnosis"),
						"",
						"",
						bc.getCellString("cldoc_name"),
						""
                        );
		bw.write(output);
		bw.newLine();
	}	
	
	/*
	int getClassno() {
		return(classno);
	}
	*/
	
	
//	@Override
//    protected String brEvalFunction(String p_functName,List p_args) {
//    	if(p_functName.equals("getPhrEffectDate")) {
//    		String phno = getCellString(p_args.get(0).toString());
//    		try {
//    			TableRec tr = su.getQueryResult("select * from axaphr where axaphr_phno = '"+phno+"' and axaphr_afco = '' order by axaphr_effectdate desc",null);
//    			Date dd = getCellDate("axaclm_date");
//    			if(DateUtil.minDate.after(dd)) {
//    			dd = DateUtil.today();
//    			}
//    			for(int i=0;i<tr.getRecordCount();i++) {
//    				tr.setRecPointer(i);
//    				Date d0 = tr.getFieldDate("axaphr_effectdate");
//    				if(!d0.after(dd)) {
//    					return(DateUtil.toDateString(d0, "'yyyy/mm/dd'"));
//    				}
//    			}
//    			if(tr.getRecordCount() > 0) {
//    				Date d3 = DateUtil.dateTimeStrToDate("2024/04/01");
//    				if(dd.before(d3)) {
//    					tr.setRecPointer(0);
//    					Date d0 = tr.getFieldDate("axaphr_effectdate");
//    					return(DateUtil.toDateString(d0, "'yyyy/mm/dd'"));
//    				}
//    			}
//    		} catch (Exception ex) {
//    			UniLog.log(ex);
//    			return(DateUtil.toDateString(DateUtil.maxDate, "'yyyy/mm/dd'"));
//    		}
//    		return(DateUtil.toDateString(DateUtil.maxDate, "'yyyy/mm/dd'"));
//    	}
//		return(super.brEvalFunction(p_functName, p_args));
//    }	

//	static Hashtable<String,Vector<Date>> phrEffectDateHash = new Hashtable<String,Vector<Date>>();
	static Hashtable<String,ArrayList<Date>> polEffectDateHash = null;
	static Hashtable<String,ArrayList<CvpdRec>> cvpEffectDateHash = null;
	static Date minHashDate = DateUtil.dateTimeStrToDate("2021/01/01");
	static public Date d240401 = DateUtil.dateTimeStrToDate("2024/04/01");
	
	static public class CvpdRec {
		Date effectiveDate;
		int ohClass;
		int ocClass;
		Date termination;
	}
    synchronized static public CvpdRec getCvpdByEffectDate(String p_phno,String p_certDep,Date p_date,SelectUtil su) {
    	if(StringUtils.isBlank(p_phno)) return(null);
    	if(StringUtils.isBlank(p_certDep)) return(null);
    	if(DateUtil.minDate.after(p_date)) return(null);
    	if(cvpEffectDateHash == null) {
    		cvpEffectDateHash = new Hashtable<String,ArrayList<CvpdRec>>();
    	}
    	ArrayList<CvpdRec> edList = cvpEffectDateHash.get(p_phno+"_"+p_certDep);
    	CvpdRec crec;
    	if(edList == null) {
    		try {
    			edList = new ArrayList<CvpdRec>();
    			cvpEffectDateHash.put(p_phno+"_"+p_certDep, edList);
    			TableRec tr = su.getQueryResult("select * from axacvpd where axacvd_phno = ? and axacvd_certdep = ? order by axacvd_effectdate" ,
    						new Wherecl().appendArgument(p_phno).appendArgument(p_certDep));
    			for(int i=0;i<tr.getRecordCount();i++) {
    				tr.setRecPointer(i);
    				crec = new CvpdRec();
    				crec.effectiveDate = tr.getFieldDate("axacvd_effectdate");
    				crec.termination = tr.getFieldDate("axacvd_termination");
    				crec.ohClass = tr.getFieldInt("axacvd_ohclass");
    				crec.ocClass = tr.getFieldInt("axacvd_occlass");
    				edList.add(crec);
    			}
    		} catch (Exception ex) {
    			UniLog.log(ex);
   				return(null);
    		}
    	}
   		for(int i=edList.size()-1;i>=0;i--) {
   			crec = edList.get(i);
   			if(!crec.effectiveDate.after(p_date)) {
   					return(crec);
   			}
   		}
    	return(null);
    }
    synchronized static public void clearCache() {
    	polEffectDateHash = null;
    	cvpEffectDateHash = null;
    }
    synchronized static public Date getPolEffectDate(String p_phno,int p_class,Date p_date,SelectUtil su) {
    	ArrayList<Date> edates = null;
    	if(StringUtils.isBlank(p_phno) || DateUtil.minDate.after(p_date)) {
   			return(DateUtil.maxDate);
    	}
    	if(polEffectDateHash == null) {
    		try {
    			polEffectDateHash = new Hashtable<String,ArrayList<Date>>();
    			TableRec tr = su.getQueryResult("select distinct axapol_phno,axapol_class,axapol_effectdate from axapol order by 1,2,3" ,null);
    			for(int i=0;i<tr.getRecordCount();i++) {
    				tr.setRecPointer(i);
    				String phno = tr.getFieldString("axapol_phno");
    				int pclass = tr.getFieldInt("axapol_class");
    				Date edate = tr.getFieldDate("axapol_effectdate");
    				if(minHashDate.after(edate)) {
    					continue;
    				}
    				edates = polEffectDateHash.get(phno+"_"+pclass);
    				if(edates == null) {
    					edates = new  ArrayList<Date>();
    					polEffectDateHash.put(phno+"_"+pclass,edates);
    				}
    				edates.add(edate);
    			}
    		} catch (Exception ex) {
    			UniLog.log(ex);
   				return(null);
    		}
    	}
    	edates = polEffectDateHash.get(p_phno+"_"+p_class);
    	if(edates == null) return(DateUtil.maxDate);
    	/*
    	Date date0 = getPhrEffectDate(p_phno,p_date,su);
    	if(date0.before(p_date)) date0 = p_date;
    	*/
    	Date date0 = p_date;
   		for(int i=edates.size()-1;i>=0;i--) {
   			Date d0 = edates.get(i);
   			if(!d0.after(date0)) {
   					return(d0);
   			}
   		}
   		if(edates.size() > 0) {
   			if(date0.before(d240401)) {
   				Date d0 = edates.get(0);
   				if(d0.before(d240401)) {
   					return(d0);
   				} else {
   					return(DateUtil.maxDate);
   				}
 			}
   		}
   		return(DateUtil.maxDate);
    }
    /*
    static public Date getPhrEffectDate(String p_phno,Date p_date,SelectUtil su) {
    	String phno = p_phno;
    	if(StringUtils.isBlank(p_phno)|| DateUtil.minDate.after(p_date)) {
    		return(DateUtil.prevday(DateUtil.maxDate));
    	}
    	try {
    		Vector<Date> edates = phrEffectDateHash.get(phno);
    		if(edates == null) {
    			UniLog.log("load PhrEffectdates " + p_phno + " " + p_date);
    			TableRec tr = su.getQueryResult("select * from axaphr where axaphr_phno = '"+phno+"' and axaphr_afco = '' order by axaphr_effectdate",null);
    			edates = new Vector<Date>();
    			for(int i=0;i<tr.getRecordCount();i++) {
    				tr.setRecPointer(i);
    				edates.add(tr.getFieldDate("axaphr_effectdate"));
    			}
    			phrEffectDateHash.put(phno,edates);
    			
    		}
    		for(int i=edates.size()-1;i>=0;i--) {
    			Date d0 = edates.get(i);
    			if(!d0.after(p_date)) {
    					return(d0);
    			}
    		}
    		if(edates.size() > 0) {
    			Date d3 = DateUtil.dateTimeStrToDate("2024/04/01");
    			if(p_date.before(d3)) {
    				Date d0 = edates.get(0);
    				if(d0.before(d3)) {
    					return(d0);
    				} else {
    					return(DateUtil.prevday(DateUtil.maxDate));
    				}
   				}
   			}
   		} catch (Exception ex) {
   			UniLog.log(ex);
   			return(DateUtil.prevday(DateUtil.maxDate));
   		}
    	return(DateUtil.prevday(DateUtil.maxDate));
    }	
    */
	private enum FuncName { FUNC_getPhrEffectDate, FUNC_getPolEffectDate,FUNC_getPolClass,FUNC_getCvpEdate,FUNC_getPhrStDate,FUNC_getPhrEndDate,NOT_DEFINED }
	class AxaClaimCellCollection extends BiCellCollection {
		public AxaClaimCellCollection(BiCellCollection p_col, BiResult p_br) {
			super(p_col, p_br);
		}
		@Override
		public Object evalFunction(String p_fname,Vector p_args) throws Exception {
			FuncName funcName = FuncName.NOT_DEFINED;
			try {
				funcName = FuncName.valueOf("FUNC_"+p_fname);
			} catch(Exception ex) {
			}
			switch (funcName){
				case FUNC_getPhrEndDate: {
					Date lastAnDate = (Date) p_args.get(0);
					if(DateUtil.minDate.after(lastAnDate)) return(DateUtil.zeroDate);
					Date nextAnDate = (Date) p_args.get(1);
					if(DateUtil.minDate.after(nextAnDate)) return(DateUtil.zeroDate);
					int d = DateUtil.getDay(nextAnDate);
					int m = DateUtil.getMonth(nextAnDate);
					int y = DateUtil.getYear(lastAnDate);
					Date stDate = DateUtil.getDate(y, m, d);
					if(!stDate.after(lastAnDate)) {
						stDate = DateUtil.getDate(y+1, m, d);
					}
					return(stDate);
				}
				case FUNC_getPhrStDate: {
					Date clmDate = (Date) p_args.get(0);
					if(DateUtil.minDate.after(clmDate)) return(DateUtil.zeroDate);
					Date lastAnDate = (Date) p_args.get(1);
					if(DateUtil.minDate.after(lastAnDate)) return(DateUtil.zeroDate);
					Date cvpEdate = (Date) p_args.get(2);
					if(DateUtil.minDate.after(cvpEdate)) return(DateUtil.zeroDate);
					int d = DateUtil.getDay(lastAnDate);
					int m = DateUtil.getMonth(lastAnDate);
					int y = DateUtil.getYear(clmDate);
					Date stDate = DateUtil.getDate(y, m, d);
					if(stDate.after(clmDate)) {
						stDate = DateUtil.getDate(y-1, m, d);
					}
					return(stDate);
							
				}
					/*
				case FUNC_getPhrEffectDate: {
					String phno = (String) p_args.get(0);
					Date d0 = (Date) p_args.get(1);
					return(getPhrEffectDate(phno,d0,su));
				}
					*/
				case FUNC_getPolEffectDate: {
					String phno = (String) p_args.get(0);
					int pclass = Cell.objectToInt(p_args.get(1));
					Date d0 = (Date) p_args.get(2);
					return(getPolEffectDate(phno,pclass,d0,su));
				}

				case FUNC_getCvpEdate: {
					Date clmDate = (Date) p_args.get(0);
					if(DateUtil.minDate.after(clmDate)) return(DateUtil.zeroDate);
					Date cvpEdate = (Date) p_args.get(1);
					if(DateUtil.minDate.after(cvpEdate)) return(DateUtil.zeroDate);
					String phno = (String) p_args.get(2);
					if(StringUtils.isBlank(phno)) return(DateUtil.zeroDate);
					String certdep = (String) p_args.get(3);
					if(StringUtils.isBlank(certdep)) return(DateUtil.zeroDate);
					if(cvpEdate.after(clmDate)) {
						CvpdRec crec = getCvpdByEffectDate(phno,certdep,clmDate,getSelectUtil());
						if(crec == null) return(DateUtil.zeroDate);
						return(crec.effectiveDate);
					} else {
						return(cvpEdate);
					}
				}
				case FUNC_getPolClass: {
					Date clmDate = (Date) p_args.get(0);
					if(DateUtil.minDate.after(clmDate)) return(0);
					Date cvpEdate = (Date) p_args.get(1);
					if(DateUtil.minDate.after(cvpEdate)) return(0);
					String phno = (String) p_args.get(2);
					if(StringUtils.isBlank(phno)) return(0);
					String certdep = (String) p_args.get(3);
					if(StringUtils.isBlank(certdep)) return(0);
					boolean isFlexi = (Boolean) p_args.get(4);
					int ohClass  = Cell.objectToInt(p_args.get(5));
					int ocClass  = Cell.objectToInt(p_args.get(6));
					if(cvpEdate.after(clmDate)) {
						CvpdRec crec = getCvpdByEffectDate(phno,certdep,clmDate,getSelectUtil());
						if(crec == null) return(0);
						if(DateUtil.minDate.before(crec.termination)) {
							if(clmDate.after(crec.termination)) return(0);
						}
						if(isFlexi) {
							return(crec.ocClass);
						} else {
							return(crec.ohClass);
						}
					} else {
						if(isFlexi) return(ocClass) ; else return(ohClass);
					}
				}
			}
			return(super.evalFunction(p_fname,p_args) );
		}	
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new AxaClaimCellCollection(p_parent, this));
	}
	
	public class ClaimInfo {
		public String bfCode;
		public double claimAmt;
		public double axaPaid;
		public double patientPaid;
		public ClaimInfo (String p_bfCode,double p_claimAmt,double p_axaPaid,double p_patientPaid) {
			bfCode = p_bfCode;
			claimAmt = p_claimAmt;
			axaPaid = p_axaPaid;
			patientPaid = p_patientPaid;
		}
	}
	
	static public final int BFT_Single   = 0;
	static public final int BFT_EndWith1 = 1;
	static public final int BFT_ExtraMed = 2;
	static public final int BFT_EndWith8 = 3;
	static public final int BFT_Layer2   = 4;
	static public final int BFT_NoClaim  = 5;
	static public final int BFT_Surg     = 6;
	static public final int BFT_Xray     = 7;
	static public final int BFT_AdvLab   = 8;
	public void claimOneBenefit(BiCellCollection bc,double amtClaim,List<ClaimInfo> claimList) throws Exception {
		double reimbursement = bc.getCellInt("axapol_reimburse")/100.0;
		double benefitamt;
		double axaPaid;
		double patientPaid;
		benefitamt = bc.getCellDouble("axapol_benefit");
		int claim_per_day = bc.getCellInt("axapol_noofday");
		if(claim_per_day <= 0) {
			benefitamt -= bc.getCellDouble("axasta_payamount");
			if(benefitamt <= 0) return;
		}
		if(benefitamt > amtClaim * reimbursement) {
			axaPaid = amtClaim * reimbursement;
		} else {
			axaPaid = benefitamt;
		}
		patientPaid = amtClaim - axaPaid;
		claimList.add(new ClaimInfo (bc.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
			
	}
	public void claimXrayAndSurgery(BiCellCollection bcXray,BiCellCollection bcAdvLab,BiCellCollection bcSurg,List<ClaimInfo> claimList) throws Exception {
		double xray = getCellDouble("axaclm_xray");
		double surg = getCellDouble("axaclm_surg");
		boolean isAdvLab = getCellBoolean("axaclm_advxray");
		if(surg > 0 && bcSurg != null) {
			claimOneBenefit(bcSurg,surg,claimList);
		}
		if(xray > 0) {
			if(isAdvLab) {
				if(bcAdvLab != null) claimOneBenefit(bcAdvLab,xray,claimList);
			} else {
				if(bcXray != null) claimOneBenefit(bcXray,xray,claimList); 
					else
						if(bcAdvLab != null) claimOneBenefit(bcAdvLab,xray,claimList); 
			}
		}
	}
	public List<ClaimInfo>claimBenefit2014() throws Exception {
		int scenerio =0;
		double totalAmount = getCellDouble("axaclm_amount");
		double agreedFee = getCellDouble("axabfg_agreefee");
		double consgfee  = getCellDouble("axaclm_consfee");
		double medifee  = getCellDouble("axaclm_medifee");
		BiResult slbf = getSubLink("axa.ClaimBenefit");
		BiResult slcl = getSubLink("axa.AxaClaimDet");
		Vector<BiCellCollection> sl = slbf.getRowCollectionList();
		BiCellCollection bcSingle=null;
		BiCellCollection bcExtraMed =null;
		BiCellCollection bcEndWith1or8=null;
		BiCellCollection bcLayer2 =null;
		BiCellCollection bcXray =null;
		BiCellCollection bcSurg =null;
		BiCellCollection bcAdvLab =null;
		for(BiCellCollection bc : sl) {
			switch(bc.getCellInt("axabfc_type")) {
			case BFT_Single:scenerio |= (1 << BFT_Single);
							bcSingle = bc;
							break;
			case BFT_EndWith1:scenerio |= (1 << BFT_EndWith1);
							bcEndWith1or8 = bc;
							break;
			case BFT_ExtraMed:scenerio |= (1 << BFT_ExtraMed);
							bcExtraMed = bc;
							break;
			case BFT_EndWith8:scenerio |= (1 << BFT_EndWith8);
							bcEndWith1or8 = bc;
							break;
			case BFT_Layer2:scenerio |= (1 << BFT_Layer2);
							bcLayer2 = bc;
							break;
			case BFT_Xray: bcXray = bc;
							break;
			case BFT_Surg: bcSurg = bc;
							break;
			case BFT_AdvLab: bcAdvLab = bc;
							break;
			}
		}
		/*
		 * The following claim logic is messy and unresonable . 
		 * Its just stricy copied the logic defined in AXA's spec
		 */
		double totalClaimLimit = Double.POSITIVE_INFINITY;
		double totalClaim = 0;
		double amtClaim = 0;
		double axaPaid = 0;
		double patientPaid = 0;
		double deductable = 0;
		double mediClaim = 0;
		double benefitamt = 0;
		int claim_per_day = 0;
		ArrayList<ClaimInfo> claimList = new ArrayList<ClaimInfo>();
		switch(scenerio) {
		case (1<<BFT_EndWith1):
		case (1<<BFT_EndWith1)|(1<<BFT_ExtraMed):
		case (1<<BFT_EndWith1)|(1<<BFT_Layer2):
		case (1<<BFT_EndWith1)|(1<<BFT_Single):
		case (1<<BFT_EndWith1)|(1<<BFT_Single)|(1<<BFT_Layer2):
		case (1<<BFT_EndWith1)|(1<<BFT_Single)|(1<<BFT_ExtraMed):
		case (1<<BFT_EndWith8):
		case (1<<BFT_EndWith8)|(1<<BFT_ExtraMed):
		case (1<<BFT_EndWith8)|(1<<BFT_Single):
		case (1<<BFT_EndWith8)|(1<<BFT_Single)|(1<<BFT_ExtraMed):
			/*
			 * Calculate the simplest case with only Benefit Code Ends with 1
			 */
			amtClaim = consgfee > agreedFee ? agreedFee : consgfee;
			deductable = bcEndWith1or8.getCellDouble("axapol_deductamt");
			
			if(amtClaim > deductable) {
				axaPaid = amtClaim - deductable;
			} else {
				axaPaid = 0;
				deductable = amtClaim;
			}
			patientPaid = amtClaim - axaPaid;
			
			switch(scenerio) {
			case (1<<BFT_EndWith1):
			case (1<<BFT_EndWith1)|(1<<BFT_Layer2):
			case (1<<BFT_EndWith8):
			case (1<<BFT_EndWith8)|(1<<BFT_ExtraMed):
			case (1<<BFT_EndWith8)|(1<<BFT_Single):
			case (1<<BFT_EndWith8)|(1<<BFT_Single)|(1<<BFT_ExtraMed):
				/*
				 * bound with total claim limit 
				 */
				if(axaPaid + totalClaim > totalClaimLimit) {
					UniLog.log("Total Claim Limit Reached " + totalClaimLimit + " " + totalClaim + " " + axaPaid);
					axaPaid = totalClaimLimit - totalClaim;
					patientPaid = amtClaim - axaPaid;
					claimList.add(new ClaimInfo (bcEndWith1or8.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
					return(claimList);
				}
				/*
				 * No other useable benifit codes, return with single claim detail
				 */
				claimList.add(new ClaimInfo (bcEndWith1or8.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
				switch(scenerio) {
				case (1<<BFT_EndWith8)|(1<<BFT_ExtraMed):
				case (1<<BFT_EndWith8)|(1<<BFT_Single)|(1<<BFT_ExtraMed):
					if(medifee > 0) {
						benefitamt = bcExtraMed.getCellDouble("axapol_benefit");
						claim_per_day = bcExtraMed.getCellInt("axapol_noofday");
						if(claim_per_day <= 0) {
							benefitamt -= bcExtraMed.getCellDouble("axasta_payamount");
							if(benefitamt < 0) benefitamt = 0;
						}
						double reimbursement = bcExtraMed.getCellInt("axapol_reimburse")/100.0;
						amtClaim = medifee;
						if(benefitamt > amtClaim * reimbursement) {
							axaPaid = amtClaim * reimbursement;
						} else {
							axaPaid = benefitamt;
						}
						patientPaid = amtClaim - axaPaid;
						claimList.add(new ClaimInfo (bcExtraMed.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
					}
					break;
				}
				claimXrayAndSurgery(bcXray,bcAdvLab,bcSurg,claimList);
				return(claimList);
			}
			/*
			 * calculate the effective benifit amount of the Single benefit code
			 */
			if(bcSingle != null) {
			benefitamt = bcSingle.getCellDouble("axapol_benefit");
			claim_per_day = bcSingle.getCellInt("axapol_noofday");
			if(claim_per_day <= 0) {
				benefitamt -= bcSingle.getCellDouble("axasta_payamount");
				if(benefitamt < 0) benefitamt = 0;
			}
			} else {
				benefitamt = agreedFee;
			}
			/*
			 * Calculate the amount of extramed claimable by Single benefit code
			 */
			if(medifee + axaPaid > benefitamt) {
				/*
				 * very special case that the benefitamt < axaPaid, in this case, mediClaim will be < 0, and will supposed to reduce axaPaid correctly (not checked)
				 */
				mediClaim = benefitamt - axaPaid;
			} else {
				mediClaim = medifee;
			}
			if(mediClaim < 0) mediClaim = 0;
			axaPaid += mediClaim;
//			if(benefitamt - axaPaid > 0 && patientPaid > 0) {
//				/*
//				 * Still have room in benefitamt to furthre reduce the patientPaid
//				 */
//				if(benefitamt - axaPaid > patientPaid) {
//					axaPaid += patientPaid;
//					patientPaid = 0;
//				} else {
//					patientPaid -= benefitamt - axaPaid;
//					axaPaid = benefitamt;
//				}
//			}
			if(
				(	(scenerio & (1 << BFT_ExtraMed)) == 0 ) || (mediClaim >= medifee)
					) {
				/* all extramed claimed or no extramed */

				/*
				 * bound with total claim limit 
				 */
				amtClaim += medifee;
				patientPaid += medifee - mediClaim;
				if(axaPaid + totalClaim > totalClaimLimit) {
					UniLog.log("Total Claim Limit Reached " + totalClaimLimit + " " + totalClaim + " " + axaPaid);
					axaPaid = totalClaimLimit - totalClaim;
					patientPaid = amtClaim - axaPaid;
					claimList.add(new ClaimInfo (bcEndWith1or8.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
					return(claimList);
				}
				claimList.add(new ClaimInfo (bcEndWith1or8.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
				claimXrayAndSurgery(bcXray,bcAdvLab,bcSurg,claimList);
				return(claimList);
				
			}
			
			/* continue to claim extramed , set total treatment amount of benefit code Ends with 1 to (benefit amount + deductable) according to spec (just guess) */
			amtClaim = benefitamt + patientPaid;
			if(axaPaid + totalClaim > totalClaimLimit) {
				UniLog.log("Total Claim Limit Reached " + totalClaimLimit + " " + totalClaim + " " + axaPaid);
				axaPaid = totalClaimLimit - totalClaim;
				patientPaid = amtClaim - axaPaid;
				claimList.add(new ClaimInfo (bcEndWith1or8.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
				return(claimList);
			}
			claimList.add(new ClaimInfo (bcEndWith1or8.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
			totalClaim += axaPaid;

			benefitamt = bcExtraMed.getCellDouble("axapol_benefit");
			claim_per_day = bcExtraMed.getCellInt("axapol_noofday");
			if(claim_per_day <= 0) {
				benefitamt -= bcExtraMed.getCellDouble("axasta_payamount");
				if(benefitamt < 0) benefitamt = 0;
			}
			double reimbursement = bcExtraMed.getCellInt("axapol_reimburse")/100.0;
			amtClaim = medifee - mediClaim;
			if(benefitamt > amtClaim * reimbursement) {
				axaPaid = amtClaim * reimbursement;
			} else {
				axaPaid = benefitamt;
			}
			patientPaid = amtClaim - axaPaid;
			claimList.add(new ClaimInfo (bcExtraMed.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
			claimXrayAndSurgery(bcXray,bcAdvLab,bcSurg,claimList);
			return(claimList);
			/*
		case (1<<BFT_EndWith8):
		case (1<<BFT_EndWith8)|(1<<BFT_ExtraMed):
		case (1<<BFT_EndWith8)|(1<<BFT_Single):
		case (1<<BFT_EndWith8)|(1<<BFT_Single)|(1<<BFT_ExtraMed):
			amtClaim = consgfee > agreedFee ? agreedFee : consgfee;
			axaPaid = amtClaim - bcEndWith1or8.getCellDouble("axapol_deductamt");
			patientPaid = consgfee - axaPaid;
			claimList.add(new ClaimInfo (bcEndWith1or8.getCellString("axapol_benefitcode"),amtClaim,axaPaid,patientPaid));
			break;
			*/
		default : throw new Exception("Feature not yet implemented");
		}
	}
	
	Integer excelTreatmentTotalAmountColidx;
	Integer excelBillAxaAmountColidx;
	Integer excelTreatmentTypeColidx;
	@Override
	protected int postProcessOneMasterRecord(ExcelPoi jxf,int startExcelRow,int endExcelRow) throws Exception {
		
//		int xr = super.postProcessOneMasterRecord(startExcelRow,endExcelRow);
		if(excelTreatmentTotalAmountColidx == null) {
			String colhdr;
			for(int i=0;i<jxf.excel_getColumnCount(0);i++) {
				colhdr = jxf.getStringValue(0, i);
				if(colhdr.equals("Treatment total Amount")) {
					excelTreatmentTotalAmountColidx = i;
				}
				if(colhdr.equals("Bill Axa Amount")) {
					excelBillAxaAmountColidx = i;
				}
				if(colhdr.equals("Treatment Type")) {
					excelTreatmentTypeColidx = i;
				}
			}
		}
		if((excelTreatmentTotalAmountColidx == null) || (excelTreatmentTotalAmountColidx == null)) {
			return(endExcelRow);
		}
		double treatmentTotal = jxf.getDoubleValue(startExcelRow, excelTreatmentTotalAmountColidx);
		double billTotal = jxf.getDoubleValue(startExcelRow, excelBillAxaAmountColidx);
		if(treatmentTotal > getCellDouble("axaclm_consfee")) {
			double medicationFee = treatmentTotal - getCellDouble("axaclm_consfee");
			double medicationBill= billTotal - getCellDouble("axaclm_consfee");
			if(medicationBill < 0) medicationBill = 0;
			treatmentTotal -= medicationFee;
			billTotal -= medicationBill;
			jxf.excel_setNumericValue(startExcelRow, excelTreatmentTotalAmountColidx, treatmentTotal);
			jxf.excel_setNumericValue(startExcelRow, excelBillAxaAmountColidx, billTotal);
			if(endExcelRow > startExcelRow) {
				jxf.excel_shiftRow(startExcelRow+1, endExcelRow, 1);
			}
			for(int i=0;i<jxf.excel_getColumnCount(0);i++) {
				int styleIdx = jxf.excel_getCellStyleIdx(startExcelRow,i);
				jxf.excel_setCellStyle(styleIdx);
				if(i == excelTreatmentTotalAmountColidx) {
					jxf.excel_setNumericValue(startExcelRow+1, excelTreatmentTotalAmountColidx, medicationFee);
				} else if(i == excelBillAxaAmountColidx) {
					jxf.excel_setNumericValue(startExcelRow+1, excelBillAxaAmountColidx, medicationBill);
				} else if(i == excelTreatmentTypeColidx) {
					jxf.excel_setStringValue(startExcelRow+1, excelTreatmentTypeColidx, "Medication");
				} else {
					org.apache.poi.ss.usermodel.Cell xc = jxf.getCell(startExcelRow, i);
					if(xc.getCellType()== org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC) {
						   if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(xc)) {
						        // It's a date
						        Date dv = xc.getDateCellValue();
						        jxf.excel_setDateValue(startExcelRow+1, i, dv);
						    } else {
						        // It's a double (or other numeric)
						        double nv = xc.getNumericCellValue();
						        jxf.excel_setNumericValue(startExcelRow+1, i, nv);
						    }
					} else {
						String sv = xc.getStringCellValue();
						jxf.excel_setStringValue(startExcelRow+1, i, sv);
					}
				}
			}
			return(endExcelRow+1);
		}
		return(endExcelRow);
	}
	public byte[] exportStatementToExcel(java.util.Date p_date,String p_fileName) throws Exception {
				//"axaclm_submitdate = '"+ DateUtil.dateToDateTimeStr(p_date,"yyyy/mm/dd")+"'",
		excelTreatmentTotalAmountColidx = null;
		excelTreatmentTypeColidx = null;
		excelBillAxaAmountColidx = null;
		Vector<BiColumn> vvv = new Vector<BiColumn>();
		vvv.add(getColumnByLabel("cldoc_cocode"));
		vvv.add(getColumnByLabel("axaclm_phno"));
		vvv.add(getColumnByLabel("axaclm_certno"));
		vvv.add(getColumnByLabel("axaclm_dependno"));
		vvv.add(getColumnByLabel("axacvp_covername"));
		vvv.add(getColumnByLabel("axaclm_date"));
		vvv.add(getColumnByLabel("cldoc_name"));
		vvv.add(getColumnByLabel("axaclm_voucher"));
		vvv.add(getColumnByLabel("axabfg_group"));
		vvv.add(getColumnByLabel("axaclm_submitdate"));
		ExportToExcel ete = new ExportToExcel( p_fileName,false,true,false,vvv,null);
		while (ete.exportToExcelOnce()) {
			
		}
		return(ete.getResult());
	}
	
}
