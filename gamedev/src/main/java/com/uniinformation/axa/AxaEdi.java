package com.uniinformation.axa;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;


public class AxaEdi {
	
	static public enum ERRTYPE{
		APHR,
		APOL,
		ACVP,
		SPCN,
		CHGC
	};


	static public ReturnMsg uploadER2(SessionHelper p_sh,InputStream is,HashSet<ERRTYPE> p_updSet) throws Exception {
		BufferedReader reader = null;
		SelectUtil su = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is,"ISO-8859-1"));
			su = p_sh.getBiSchema().getSelectUtil();
			while(reader.ready()) {
			     String line = reader.readLine();
			     if(line.length() < 10) {
			    	 UniLog.log("skip invalid line");
			    	 continue;
			     }
			     ERRTYPE etype=null;
			     try {
			    	 etype = ERRTYPE.valueOf(line.substring(0,4));
			     }
			     catch(Exception ex) {
			    	 UniLog.log("errtype not valid, skipped");
			    	 //remark: if enum not exist, will got exception here.
			     }
			     if(!p_updSet.contains(etype)) continue;
			     Date d;
			     int i;
			     switch(etype) {
			     case APHR :
			    	 		d = YYYYMMDDtoDate(line.substring(28, 36));
			    	 		su.executeUpdate(
			    	 				"update axaphr set axaphr_effectdate = ? where axaphr_phno = ? and axaphr_afcno = ? ",
			    	 				new Wherecl()
			    	 					.appendArgument(d)
			    	 					.appendArgument(line.substring(4,10).trim())
			    	 					.appendArgument(line.substring(10,12).trim())
			    	 				);
			    	 	break;
			     case APOL :
			    	 		d = YYYYMMDDtoDate(line.substring(28, 36));
			    	 		i = Integer.parseInt(line.substring(14,16).trim());
			    	 		su.executeUpdate(
			    	 				"update axaphr set axapol_effectdate = ? where axapol_phno = ? and axapol_class = ? ",
			    	 				new Wherecl()
			    	 					.appendArgument(d)
			    	 					.appendArgument(line.substring(4,10).trim())
			    	 					.appendArgument(i)
			    	 				);
			    	 	break;
			     case ACVP :
			    	 		d = YYYYMMDDtoDate(line.substring(28, 36));
			    	 		TableRec tr = su.getQueryResult("select * from axacvp where axacvp_phno = ? and axacvp_certdep = ?",
			    	 					new Wherecl()
			    	 						.appendArgument(line.substring(4,10).trim())
			    	 						.appendArgument(line.substring(16,26).trim())
			    	 				);
			    	 		if(tr.getRecordCount() > 0) {
			    	 			tr.setRecPointer(0);
			    	 			su.executeUpdate(
			    	 				"update axacvpd set axacvd_effectdate = ? where axacvd_phno = ? and axacvd_certdep = ? and axacvd_effectdate = ?",
			    	 				new Wherecl()
			    	 					.appendArgument(d)
			    	 					.appendArgument(line.substring(4,10).trim())
			    	 					.appendArgument(line.substring(16,26).trim())
			    	 					.appendArgument(tr.getFieldDate("axacvp_effectdate"))
			    	 				);
			    	 			su.executeUpdate(
			    	 				"update axacvp set axacvp_effectdate = ? where axacvp_phno = ? and axacvp_certdep = ? ",
			    	 				new Wherecl()
			    	 					.appendArgument(d)
			    	 					.appendArgument(line.substring(4,10).trim())
			    	 					.appendArgument(line.substring(16,26).trim())
			    	 				);
			    	 		}
			    	 	break;
			     case SPCN :
			    	 	break;
			     case CHGC :
			    	 	String oldCertDep = line.substring(16, 26).trim();
			    	 	String newCertDep = null;
			    	 	if(line.length() >= 46) {
			    	 		newCertDep = line.substring(36, 46).trim();
			    	 	} else {
			    	 		newCertDep = line.substring(36).trim();
			    	 	}
			    	 	int idx = newCertDep.indexOf('-');
			    	 	String newCert = newCertDep.substring(0,idx);
			    	 	int newDep = Integer.parseInt(newCertDep.substring(idx+1).trim());
	    	 			su.executeUpdate(
		    	 				"update axacvpd set axacvd_certdep = ? where axacvd_phno = ? and axacvd_certdep = ?",
		    	 				new Wherecl()
		    	 					.appendArgument(newCertDep)
		    	 					.appendArgument(line.substring(4,10).trim())
		    	 					.appendArgument(oldCertDep)
		    	 				);

	    	 			su.executeUpdate(
		    	 				"update axacvp set axacvp_certdep = ? , axacvp_certno = ? , axacvp_dependno = ? where axacvp_phno = ? and axacvp_certdep = ?",
		    	 				new Wherecl()
		    	 					.appendArgument(newCertDep)
		    	 					.appendArgument(newCert)
		    	 					.appendArgument(newDep)
		    	 					.appendArgument(line.substring(4,10).trim())
		    	 					.appendArgument(oldCertDep)
		    	 				);
			    	 	break;
			     }
			     UniLog.log(line);
			}
			return(ReturnMsg.defaultOk);
		}
		finally {
			CloseUtil.close(su,reader);
		}
	}
	/*
	static public ReturnMsg uploadPHRXX(SessionHelper p_sh,InputStream is) throws Exception {
		BiResult br = p_sh.getBiSchema().getViewByName("axa.AxaPHR").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is,"ISO-8859-1"));
			while(reader.ready()) {
				String line = reader.readLine();
				UniLog.log(line);
				String phno = line.substring(0,6);
				String afco = line.substring(6,8);
				br.clearCondition();
				br.addCustomCondition("axaphr_phno = '" + phno + "' and axaphr_afco = '" + afco + "'");
				br.query();
				if(br.getRowCount() > 0) {
					UniLog.log("update phr " + phno + " " + afco);
					br.loadOneRecV(0);
					br.fetchOneRecV(0);
					updatePHR(br,line);
					ReturnMsg rtn = br.updateCurrent();
					if(rtn != null && !rtn.getStatus()) {
						return(rtn);
					}
				} else {
					br.clearCurrentRec();
					updatePHR(br,line);
					ReturnMsg rtn = br.addCurrent();
					if(rtn != null && !rtn.getStatus()) {
						return(rtn);
					}
				}
			}
		}
		finally {
			CloseUtil.close(reader);
		}
		return(ReturnMsg.defaultOk);
	}
	*/
	static public ReturnMsg uploadPHR(SessionHelper p_sh,InputStream is) throws Exception {
		BiResult br = p_sh.getBiSchema().getViewByName("axa.AxaPHR").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		BufferedReader reader = null;
		SelectUtil su = null;
		int n = 0;
		try {
			reader = new BufferedReader(new InputStreamReader(is,"ISO-8859-1"));
			su = p_sh.getBiSchema().getSelectUtil();
			BiTable bt = br.getView().getTable();
			TableRec tr = bt.newTableRec();
			String sid = bt.getSerialId();
			tr.addRecord();
	        Date d;
	        int i;
	        
	        /*
	   	 br.getCell("axaphr_phno").set(s);
	   	 s = line.substring(6, 8);
	   	 br.getCell("axaphr_afco").set(s);
	   	 d = YYYYMMDDtoDate(line.substring(10, 18));
	   	 br.getCell("axaphr_effectdate").set(d);
	   	 d = YYYYMMDDtoDate(line.substring(20, 28));
	   	 br.getCell("axaphr_lastandate").set(d);
	   	 d = YYYYMMDDtoDate(line.substring(30, 38));
	   	 br.getCell("axaphr_nextandate").set(d);
	   	 s = line.substring(38, 88);
	   	 br.getCell("axaphr_phafname").set(s);
	   	 s = line.substring(88, 89);
	   	 br.getCell("axaphr_plantype").set(s);
	   	 d = YYYYMMDDtoDate(line.substring(91, 99));
	   	 br.getCell("axaphr_paytodate").set(s);      
	   	 */
	        
	        Vector<String> fv = new Vector<String>();
			     fv.add("axaphr_effectdate");
			     fv.add("axaphr_lastandate");
			     fv.add("axaphr_nextandate");
			     fv.add("axaphr_phafname");
			     fv.add("axaphr_plantype");
			     fv.add("axaphr_paytodate");
			while(reader.ready()) {
			     String line = reader.readLine();
			     if(line.length() < 10) {
			    	 UniLog.log("skip invalid line");
			    	 continue;
			     }
			     UniLog.log(line + " " + n);
			     String phno = line.substring(0,6).trim();
			     String afco = line.substring(6,8).trim();
	
			     tr.setField("axaphr_phno", phno);
			     tr.setField("axaphr_afco", afco);
	
			     d = YYYYMMDDtoDate(line.substring(10, 18));
			     Date effectdate = d;
			     tr.setField("axaphr_effectdate", d);
			     d = YYYYMMDDtoDate(line.substring(20, 28));
			     tr.setField("axaphr_lastandate", d);
			     d = YYYYMMDDtoDate(line.substring(30, 38));
			     tr.setField("axaphr_nextandate", d);
			     
			     tr.setField("axaphr_phafname", line.substring(38,88));
			     tr.setField("axaphr_plantype", line.substring(88,89));
	
			     d = YYYYMMDDtoDate(line.substring(91, 99));
			     tr.setField("axaphr_paytodate", d);
			     
			     int cc = su.updateByTableRec(bt.getDbtName(), tr, fv , fv, 
							new Wherecl().andUniop("axaphr_phno", "=",phno)
							.andUniop("axaphr_afco", "=", afco)
//							.andUniop("axaphr_effectdate", "=", effectdate)
							.stripAnd()
	//		    		 	new Wherecl().appendArgument(phno).appendArgument(classno)
			    		 );
			     if(cc == 0) {
			    	 su.insertByTableRec(bt.getDbtName(), tr,true,sid); 
			     } else {
			    	 
			    	 int ccc;
			    	 ccc = 0;
			     }
			     n++;
			}
		}
		finally {
			CloseUtil.close(reader,su);
		}
		UniLog.log("Total " + n + " records");
		return(ReturnMsg.defaultOk);
	}
	static public ReturnMsg uploadCVP(SessionHelper p_sh,InputStream is) throws Exception {
		BiResult br = p_sh.getBiSchema().getViewByName("axa.AxaCVP").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		BiResult brd = p_sh.getBiSchema().getViewByName("axa.AxaCVPD").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		int n = 0;
		BufferedReader reader = null;
		SelectUtil su = null;
		SelectUtil sud = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is,"ISO-8859-1"));
			su = p_sh.getBiSchema().getSelectUtil();
			sud = p_sh.getBiSchema().getSelectUtil();

			BiTable bt = br.getView().getTable();
			TableRec tr = bt.newTableRec();
			String sid = bt.getSerialId();
			tr.addRecord();

			BiTable btd = brd.getView().getTable();
			TableRec trd = btd.newTableRec();
			String sidd = btd.getSerialId();
			trd.addRecord();
			
	        Date d;
	        int i;
	        Vector<String> fv = new Vector<String>();
			     fv.add("axacvp_effectdate");
			     fv.add("axacvp_eecertno");
			     fv.add("axacvp_certno");
			     fv.add("axacvp_dependno");
			     fv.add("axacvp_covername");
	
			     fv.add("axacvp_afco");
			     fv.add("axacvp_birthdate");
			     fv.add("axacvp_relation");
			     fv.add("axacvp_ohclass");
			     fv.add("axacvp_occlass");
	
			     fv.add("axacvp_termination");
			     fv.add("axacvp_eeidno");
			     fv.add("axacvp_eedeptcode");


	        Vector<String> fvd = new Vector<String>();
			     fvd.add("axacvd_effectdate");
			     fvd.add("axacvd_ohclass");
			     fvd.add("axacvd_occlass");
			     fvd.add("axacvd_termination");
			     
			while(reader.ready()) {
			     String line = reader.readLine();
			     if(line.length() < 10) {
			    	 UniLog.log("skip invalid line");
			    	 continue;
			     }
			     UniLog.log(line + " " + n);
			     String phno = line.substring(0,6).trim();
			     String certdep = line.substring(6,16).trim();
			     tr.setField(sid, new Integer(0));
			     tr.setField("axacvp_phno", phno);
			     tr.setField("axacvp_certdep", certdep);
			     d = YYYYMMDDtoDate(line.substring(18, 26));
			     tr.setField("axacvp_effectdate", d);
			     tr.setField("axacvp_eecertno", line.substring(26,36).trim());
			     tr.setField("axacvp_certno", line.substring(36,43).trim());
			     i = Integer.parseInt(line.substring(45,47).trim());
			     tr.setField("axacvp_dependno", i);
			     tr.setField("axacvp_covername", line.substring(47,72));
			     tr.setField("axacvp_afco", line.substring(72,74));
			     d = YYYYMMDDtoDate(line.substring(76, 84));
			     tr.setField("axacvp_birthdate",d);
			     tr.setField("axacvp_relation", line.substring(84,85));
			     i = Integer.parseInt(line.substring(87,89).trim());
			     tr.setField("axacvp_ohclass", i);
			     i = Integer.parseInt(line.substring(91,93).trim());
			     tr.setField("axacvp_occlass", i);
			     String dd = line.substring(95, 103).trim();
			     if(dd.length() == 8) {
			    	 d = YYYYMMDDtoDate(dd);
			    	 tr.setField("axacvp_termination",d);
			     } else {
			    	 tr.setField("axacvp_termination",DateUtil.zeroDate);
			     }
			     if(line.length() >= 113) {
			    	 tr.setField("axacvp_eeidno", line.substring(103,113));
			     } else {
			    	 tr.setField("axacvp_eeidno", "");
			     }
			     if(line.length() >= 119) {
			    	 tr.setField("axacvp_eedeptcode", line.substring(113,119));
			     } else {
			    	 tr.setField("axacvp_eedeptcode", "");
			     }
			     int cc = su.updateByTableRec(bt.getDbtName(), tr, fv , fv, 
							new Wherecl().andUniop("axacvp_phno", "=",phno).andUniop("axacvp_certdep", "=", certdep).stripAnd()
	//		    		 	new Wherecl().appendArgument(phno).appendArgument(classno)
			    		 );
			     if(cc == 0) {
			    	 su.insertByTableRec(bt.getDbtName(), tr,true,sid); 
			     }
			     trd.setField(sidd, new Integer(0));
			     trd.setField("axacvd_phno", phno);
			     trd.setField("axacvd_certdep", certdep);
			     d = YYYYMMDDtoDate(line.substring(18, 26));
			     Date efdate = d;
			     trd.setField("axacvd_effectdate", d);
			     i = Integer.parseInt(line.substring(87,89).trim());
			     trd.setField("axacvd_ohclass", i);
			     i = Integer.parseInt(line.substring(91,93).trim());
			     trd.setField("axacvd_occlass", i);
			     dd = line.substring(95, 103).trim();
			     if(dd.length() == 8) {
			    	 d = YYYYMMDDtoDate(dd);
			    	 if(!d.after(DateUtil.minDate)) {
			    		 d = DateUtil.maxDate;
			    	 }
			    	 trd.setField("axacvd_termination",d);
			     } else {
			    	 trd.setField("axacvd_termination",DateUtil.maxDate);
			     }
			     cc = sud.updateByTableRec(btd.getDbtName(), trd, fvd , fvd, 
							new Wherecl().andUniop("axacvd_phno", "=",phno).andUniop("axacvd_certdep", "=", certdep).andUniop("axacvd_effectdate", "=", efdate)
							.stripAnd()
	//		    		 	new Wherecl().appendArgument(phno).appendArgument(classno)
			    		 );
			     if(cc == 0) {
			    	 sud.insertByTableRec(btd.getDbtName(), trd,true,sidd); 
			     }
			     n++;
			}
		}
		finally {
			CloseUtil.close(reader,su,sud);
		}
		UniLog.log("Total " + n + " records");
		return(ReturnMsg.defaultOk);
	}
	
	static public ReturnMsg uploadPOL(SessionHelper p_sh,InputStream is) throws Exception {
		BiResult br = p_sh.getBiSchema().getViewByName("axa.AxaPOL").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		int n = 0;
		BufferedReader reader = null;
		SelectUtil su = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is,"ISO-8859-1"));
			su = p_sh.getBiSchema().getSelectUtil();
			BiTable bt = br.getView().getTable();
			TableRec tr = bt.newTableRec();
			String sid = bt.getSerialId();
			tr.addRecord();
	        Date d;
	        int i;
	        Vector<String> fv = new Vector<String>();
			     fv.add("axapol_effectdate");
			     fv.add("axapol_ccy");
			     fv.add("axapol_numbenefit");
	//		     fv.add("axapol_benefitcode");
			     fv.add("axapol_benefit");
			     fv.add("axapol_noofday");
			     fv.add("axapol_yearcode");
			     fv.add("axapol_reimburse");
			     fv.add("axapol_dependcode");
			     fv.add("axapol_deductcode");
			     fv.add("axapol_deductamt");
			     fv.add("axapol_odeductcode");
			     fv.add("axapol_olimitcode");
			     fv.add("axapol_ovlimit");
			     fv.add("axapol_oblimit");
			     fv.add("axapol_odlimit");
			     
			while(reader.ready()) {
			     String line = reader.readLine();
			     if(line.length() < 10) {
			    	 UniLog.log("skip invalid line");
			    	 continue;
			     }
			     UniLog.log(line + " " + n);
			     String phno = line.substring(0,6).trim();
			     int classno = Integer.parseInt(line.substring(8,10).trim());
			     String benefitcode = line.substring(27,33).trim();
			     tr.setField(sid, new Integer(0));
			     tr.setField("axapol_phno", phno);
			     tr.setField("axapol_class", classno);
			     d = YYYYMMDDtoDate(line.substring(12, 20));
			     Date edate = d;
			     tr.setField("axapol_effectdate", d);
			     tr.setField("axapol_ccy", line.substring(20,23));
			     i = Integer.parseInt(line.substring(24,27).trim());
			     tr.setField("axapol_numbenefit", i);
			     tr.setField("axapol_benefitcode", benefitcode);
			     i = Integer.parseInt(line.substring(34,41).trim());
			     tr.setField("axapol_benefit", i);
			     i = Integer.parseInt(line.substring(42,45).trim());
			     tr.setField("axapol_noofday", i);
			     tr.setField("axapol_yearcode", line.substring(45,46));
			     i = Integer.parseInt(line.substring(47,50).trim());
			     tr.setField("axapol_reimburse", i);
			     tr.setField("axapol_dependcode", line.substring(50,51));
			     tr.setField("axapol_deductcode", line.substring(51,52));
			     i = Integer.parseInt(line.substring(53,58).trim());
			     tr.setField("axapol_deductamt", i);
			     tr.setField("axapol_odeductcode", line.substring(58,59));
			     tr.setField("axapol_olimitcode", line.substring(59,60));
			     i = Integer.parseInt(line.substring(61,64).trim());
			     tr.setField("axapol_ovlimit", i);
			     i = Integer.parseInt(line.substring(65,72).trim());
			     tr.setField("axapol_oblimit", i);
			     i = Integer.parseInt(line.substring(73,80).trim());
			     tr.setField("axapol_odlimit", i);
	
			     int cc = su.updateByTableRec(bt.getDbtName(), tr, fv , fv, 
							new Wherecl().andUniop("axapol_phno", "=",phno)
							.andUniop("axapol_class", "=", classno)
							.andUniop("axapol_benefitcode", "=", benefitcode)
							.andUniop("axapol_effectdate", "=", edate)
							.stripAnd()
	//		    		 	new Wherecl().appendArgument(phno).appendArgument(classno)
			    		 );
			     if(cc == 0) {
			    	 su.insertByTableRec(bt.getDbtName(), tr,true,sid); 
			     }
			     n++;
			}
		}
		finally {
			CloseUtil.close(reader,su);
		}
		UniLog.log("Total " + n + " records");
		return(ReturnMsg.defaultOk);
	}

	static public ReturnMsg uploadSTA(SessionHelper p_sh,InputStream is) throws Exception {
		BiResult br = p_sh.getBiSchema().getViewByName("axa.AxaSTA").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		BufferedReader reader = null;
		SelectUtil su = null;
		int n = 0;
		int m = 0;
		try {
			reader = new BufferedReader(new InputStreamReader(is,"ISO-8859-1"));
			su = p_sh.getBiSchema().getSelectUtil();
			String lastphno = null;
			String lastcertdep = null;
			Date lastandate = null;
			BiTable bt = br.getView().getTable();
			TableRec tr = bt.newTableRec();
			String sid = bt.getSerialId();
			tr.addRecord();
	        Date d;
	        double f;
	        int i;
			while(reader.ready()) {
			     String line = reader.readLine();
			     if(line.length() < 10) {
			    	 UniLog.log("skip invalid line");
			    	 continue;
			     }
			     UniLog.log(line + " " + n);
			     String phno = line.substring(0,6).trim();
			     String certdep = line.substring(6,16).trim();
			     Date andate = YYYYMMDDtoDate(line.substring(18, 26));
			     if(!(phno.equals(lastphno) && certdep.equals(lastcertdep) && andate.equals(lastandate))) {
			    	 lastphno = phno;
			    	 lastcertdep = certdep;
			    	 lastandate = andate;
			    	 UniLog.log("Covered Person " + m + " " + lastphno + " " + certdep);
			    	 su.executeUpdate("delete from axasta where axasta_phno = ? and axasta_certdep = ? and axasta_lastandate = ?", 
			    			 new Wherecl().appendArgument(phno).appendArgument(certdep).appendArgument(andate)
			    			 );
			    	 m++;
			     }
			     tr.setField(sid, new Integer(0));
			     tr.setField("axasta_phno", lastphno);
			     tr.setField("axasta_certdep", lastcertdep);
			     tr.setField("axasta_lastandate", lastandate);
			     /*
			     d = YYYYMMDDtoDate(line.substring(18, 26));
			     tr.setField("axasta_lastandate", d);
			     */
			     d = YYYYMMDDtoDate(line.substring(28, 36));
			     tr.setField("axasta_nextandate", d);
			     i = Integer.parseInt(line.substring(36,40).trim());
			     tr.setField("axasta_numbenefit", i);
			     tr.setField("axasta_benefitcode", line.substring(40,46));
			     i = Integer.parseInt(line.substring(46,50).trim());
			     tr.setField("axasta_totaldays", i);
			     f = Double.parseDouble(line.substring(50,61).trim());
			     tr.setField("axasta_payamount", f);
			     su.insertByTableRec(bt.getDbtName(), tr,true,sid); 
			     n++;
			}
		}
		finally {
			CloseUtil.close(reader,su);
		}
		UniLog.log("Total " + m + " covered persions " + n + " records");
		return(ReturnMsg.defaultOk);
	}


	/*
	static void updatePHR(BiResult br,String line) throws Exception {
		     Date d;
		     String s;
		    	 s = line.substring(0, 6);
		    	 br.getCell("axaphr_phno").set(s);
		    	 s = line.substring(6, 8);
		    	 br.getCell("axaphr_afco").set(s);
		    	 d = YYYYMMDDtoDate(line.substring(10, 18));
		    	 br.getCell("axaphr_effectdate").set(d);
		    	 d = YYYYMMDDtoDate(line.substring(20, 28));
		    	 br.getCell("axaphr_lastandate").set(d);
		    	 d = YYYYMMDDtoDate(line.substring(30, 38));
		    	 br.getCell("axaphr_nextandate").set(d);
		    	 s = line.substring(38, 88);
		    	 br.getCell("axaphr_phafname").set(s);
		    	 s = line.substring(88, 89);
		    	 br.getCell("axaphr_plantype").set(s);
		    	 d = YYYYMMDDtoDate(line.substring(91, 99));
		    	 br.getCell("axaphr_paytodate").set(s);
	}
	*/
	
	static Date YYYYMMDDtoDate(String p_str) throws Exception {
		String y = p_str.substring(0,4);
		String m = p_str.substring(4,6);
		String d = p_str.substring(6,8);
		return(DateUtil.dateTimeStrToDate(y+"/"+m+"/"+d));
	}
}
