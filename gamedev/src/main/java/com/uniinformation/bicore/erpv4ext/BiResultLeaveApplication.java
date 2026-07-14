package com.uniinformation.bicore.erpv4ext;

import static com.uniinformation.bicore.erpv4ext.BiResultLeaveApplication.LEAVEUNIT_PER_DAY;
import static com.uniinformation.bicore.erpv4ext.BiResultLeaveApplication.LEAVEUNIT_PER_HALFDAY;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLeaveApplication extends BiResultErpv4 {
	
	HashMap<String,LeaveCal> leaveCalHash;

	public BiResultLeaveApplication(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}
	public static final int MINUTES_PER_LEAVEUNIT = 45;
	public static final int DAYS_PER_YEAR = 365;
	public static final int YEAR_TO_IGNORE_MAX_CARRYFORWARD = 9999;
	public static final int LEAVE_EXPIRE_YEAR = 1;
	public static final int LEAVEUNIT_PER_DAY = 10;
	public static final int LEAVEUNIT_PER_HALFDAY = 5;
	public static final int LEAVEUNIT_MAX_CARRYFORWARD = 0;
	public static final int LEAVEUNIT_MIN_INCREMENT = 1;
	public static final double LEAVEUNIT_INCREMENT_ROUNDING = 0.5;

	public static final Date START_TIME_IN_DAY = new Date(-DateUtil.getGmtOffset());
	public static final Date END_TIME_IN_DAY = new Date(48 * 3600000 - DateUtil.getGmtOffset());
	public static final Date MAX_TIME_IN_DAY = new Date(36 * 3600000 - DateUtil.getGmtOffset());
	public static final Date MAX_DATE = DateUtil.dateTimeStrToDate("2037/12/31");
	public static final int MAX_MINUTE_IN_DAY = 24 * 60;
	public static class RlvItem {
		public int unit;
		public Date eftd;
		public Date expd;
		public int total;
	}
	
	public static class LvUtilItem {
		String code;
		Date date;
		Date expire;
		int unit;
		int used;
	}
	
	public static class LeaveCal {

		private List<LvUtilItem> lvUtilList = new ArrayList<LvUtilItem>();
		private BiResult br;
		private Date em_stdate;
		private Date em_enddate;
		private int em_stalcnt;
		private int em_maxalcnt;
		private int em_ofsalcnt;
		private String em_alstday;
		private String em_alendday;
		private String  emx_ralstr;
		private String  emx_rclstr;
		
		private Map<String, Object> userMap = new HashMap<String, Object>();
	
		public LeaveCal(BiResult br, CellCollection emCc) {
			this.br = br;
			emx_ralstr = null;
			emx_rclstr = null;
			em_stdate = emCc.getDate("em_stdate");
			em_enddate = emCc.getDate("em_enddate");
			em_stalcnt = emCc.getInt("em_stalcnt");
			em_maxalcnt = emCc.getInt("em_maxalcnt");
			em_ofsalcnt = emCc.getInt("em_ofsalcnt");
			em_alstday = emCc.getString("em_alstday");
			em_alendday = emCc.getString("em_alendday");
		}

		public LeaveCal(BiResult br, Date em_stdate, Date em_enddate, int em_stalcnt, int em_maxalcnt, int em_ofsalcnt, String em_alstday, String em_alendday) {
			this.emx_ralstr = null;
			this.emx_rclstr = null;
			this.br = br;
			this.em_stdate = em_stdate;
			this.em_enddate = em_enddate;
			this.em_stalcnt = em_stalcnt;
			this.em_maxalcnt = em_maxalcnt;
			this.em_ofsalcnt = em_ofsalcnt;
			this.em_alstday = em_alstday;
			this.em_alendday = em_alendday;
		}
		
		public Map<String, Object> getUserMap() {
			return userMap;
		}

		public void genGetLeaveExpired(String p_code, Date p_asAtDate, List<RlvItem> rlvList) {
			int idx0 = 0;
			int r0 = 0;
			Date date1 = null;
			for (LvUtilItem item : lvUtilList) {
				if (StringUtils.equals(item.code, p_code)) {
					if(
							item.expire != null &&
							item.expire.after( DateUtil.zeroDate) &&
							(!item.expire.after(p_asAtDate))) {
							if(item.unit > item.used)	 {
								if(!item.expire.equals(date1)) {
									if(date1 != null) {
										RlvItem newItem = new RlvItem();
										rlvList.add(idx0, newItem);
										newItem.unit = r0;
										newItem.expd = date1;
										idx0++;
									}
									r0 = item.unit - item.used;
									date1 = item.expire;
								} else {
									r0 += item.unit - item.used;
								}
							}
					}
				}
			}
			if (date1 != null) {
				RlvItem newItem = new RlvItem();
				rlvList.add(idx0, newItem);
				newItem.unit = r0;
				newItem.expd = date1;
				idx0++;
			}
		}
		public void genGetLeaveRemain(String p_code, Date p_stdate, Date p_enddate, List<RlvItem> rlvList) {
			int idx0 = 0;
			int r0 = 0;
			int r1 = 0;
			Date date0 = null;
			Date date1 = null;
			Date tdate = null;
			for (LvUtilItem item : lvUtilList) {
				if (StringUtils.equals(item.code, p_code)) {
					if (item.date.compareTo(p_enddate) > 0) 
						break;
					tdate = item.date;
					if (item.expire.compareTo(p_stdate) > 0 && item.unit - item.used != 0) {
						if (date0 != tdate) {
							if (r0 != 0 || r1 != 0) {
								RlvItem newItem = new RlvItem();
								rlvList.add(idx0, newItem);
								newItem.unit = r0;
								newItem.total = r1;
								newItem.eftd = date0;
								newItem.expd = date1;
								idx0++;
							}
							r0 = item.unit - item.used;
							r1 = item.unit;
							date0 = tdate;
							date1 = item.expire;
						} else {
							r0 += item.unit - item.used;
							r1 += item.unit;
						}
					}
				}
			}
			if (r0 != 0 || r1 != 0) {
				RlvItem newItem = new RlvItem();
				rlvList.add(idx0, newItem);
				newItem.unit = r0;
				newItem.total= r1;
				newItem.eftd = date0;
				newItem.expd = date1;
				idx0++;
			}
		}
		
		public void clearLvUtilList(String p_code) {
			for (int i = lvUtilList.size() - 1; i >= 0; i--) {
				if (StringUtils.isBlank(p_code) || StringUtils.equals(p_code, lvUtilList.get(i).code))
					lvUtilList.remove(i);
			}
		}
	
		public void resetLvUtilList(String p_code) {
			for (int i = lvUtilList.size() - 1; i >= 0; i--) {
				if (StringUtils.equals(p_code, lvUtilList.get(i).code))
					lvUtilList.get(i).used = 0;
			}
		}
		
		public void genCalLeave(String p_code, String p_eid, Date p_todate) throws Exception {
			UniLog.log1("genCalLeave %s,%s,%s", p_code, p_eid, p_todate);
			Date todate = p_todate;
			if (StringUtils.equals(p_code, "AL")) {
				//sal,mal,ofs,tstday
				Date stdate = em_stdate;
				Date enddate = em_enddate;
				if (DateUtil.isDateNull(enddate))
					enddate = MAX_DATE;
				int sal = em_stalcnt;
				int mal = em_maxalcnt;
				int ofs = em_ofsalcnt;
				String tstday = em_alstday;
				String tendday = em_alendday;
				//ifelse(CUSTOMIZATION_PREFIX
				//\)
				int ty = NumberUtils.toInt(DateUtil.dateToDateTimeStr(todate, "yyyy"));
				int fy = NumberUtils.toInt(DateUtil.dateToDateTimeStr(stdate, "yyyy"));
				//ifelse(CAL_ANNUALLEAVE_FROM_STDATE,1,\	
				tstday = "/" + tstday.trim();
				Date tstdate = getValidYymmdd(fy, tstday);
				Date tenddate = getValidYymmdd(ty, tstday);
				if (tstdate.compareTo(stdate) > 0)
					fy--;
				genCalAnnualLeave(stdate, fy, ty, enddate, sal, mal, ofs, tstday, tendday);
				//\,\
				//\)
				UniLog.log1("genCalAnnualLeave startDate:%s, fy:%d, ty:%d, endDate:%s, sal:%d, mal:%d, ofs:%d, tstday:%s, lvUtilList:%s", stdate, fy, ty, enddate, sal, mal, ofs, tstday, GsonUtil.objToStr(lvUtilList));
			}
			if (StringUtils.equals(p_code, "CL"))
				genCalPresetLeave(p_eid, p_code);
			//ifelse(REGULAR_LEAVE_CODE,,\
			//\,\
			//\)
		}
		
	
		private int genCalLeaveRemained(String p_code, Date p_date, int p_leaveunit) {
			Date date0 = p_date;
			int idx0 = -1;
			for (int i = 0; i < lvUtilList.size(); i++) {
				if (StringUtils.equals(lvUtilList.get(i).code, p_code)) {
					idx0 = i;
					break;
				}
			}
			if (idx0 < 0)
				return -p_leaveunit;
			else {
				int r0 = p_leaveunit;
				int r1 = 0;
				int r2 = 0;
				int idx1;
				for (idx1 = idx0; idx1 < lvUtilList.size();idx1++) {
					LvUtilItem item = lvUtilList.get(idx1);
					if (!StringUtils.equals(item.code, p_code)) 
						break;
					if (item.date.compareTo(date0) > 0)
						break;
					if (item.expire.compareTo(p_date) > 0) {
						if (item.unit - item.used > r0) {
							item.used += r0;
							r0 = 0;
							r1 += item.unit - item.used;
						} else {
							if (item.unit > item.used) {
								r0 -= item.unit - item.used;
								item.used = item.unit;
							} else
								r2 += item.used - item.unit;
						}
					}
					/*
					 *  the following line of code is remarked because
					 *  it is added to let annual leave application for more that one days can use lvUtilItem accross year
					 *  e.g. on Dec 30 2025 , if user has 2 days annual leave left, he can apply annual leave for 10 days because after the first 2 day
					 *  he can use the annual leave for 2026
					 *  but the currenly implementatin has 2 missing things
					 *  1) it cannot handble public holiday and sunday for the leave period 
					 *  2) if the applied date only have very few days to accross year, and the user have large number of AL remain, whether he can use all
					 *  the remaining al in 2025 is unclear
					 */				
					// date0 = DateUtil.nextday(date0, (int)Math.floor((r1 + p_leaveunit - r0) / LEAVEUNIT_PER_DAY));
				}
				if (r0 > 0) {
					for (idx1 = idx1 - 1; idx1 >= idx0; idx1--) {
						LvUtilItem item = lvUtilList.get(idx1);
						if (item.date.compareTo(date0) <= 0 && item.expire.compareTo(p_date) > 0) {
							item.used += r0;
							return -r0 - r2;
						}
					}
					UniLog.log("Leave calculateion error (1)");
					return -r0 - r2;
				} else
					return r1 - r2;
			}
		}
		
		private void genCalAnnualLeave(Date p_stdate, int p_fy, int p_ty, Date p_enddate, int sal, int mal, int ofs, String p_stday, String p_endday) throws Exception {
			String tstday = p_stday;
			String tendday = p_endday;
			Date stdate = p_stdate;
			int fy = p_fy;
			int ty = p_ty;
	
			int idx0 = -1;
			for (int i = 0; i < lvUtilList.size(); i++) {
				if (StringUtils.equals(lvUtilList.get(i).code, "AL")) {
					idx0 = i;
					break;
				}
			}
			int idx1;
			if (idx0 < 0) {
				idx0 = lvUtilList.size();
				idx1 = idx0;
			} else {
				for (idx1 = idx0; idx1 < lvUtilList.size(); idx1++) {
					if (!StringUtils.equals(lvUtilList.get(idx1).code, "AL"))
						break;
				}
			}
	
			for (int yy = fy; yy <= ty; yy++) {
				int tday = customGetAnnualLeave(p_stdate, yy, p_enddate, sal, mal, ofs, tstday);
				Date date0 = getValidYymmdd(yy, tstday);
				if (date0.compareTo(stdate) < 0) 
					date0 = stdate;
				for (;idx0 < idx1; idx0++) {
					LvUtilItem item = lvUtilList.get(idx0);
					if (item.date.compareTo(date0) == 0)
						break;
					
					if (item.date.compareTo(date0) > 0) {
						//ifelse(IS_MACAU_3,1,\
						//\,\
						if (yy < YEAR_TO_IGNORE_MAX_CARRYFORWARD) {
							if (LEAVE_EXPIRE_YEAR != 1 && LEAVEUNIT_MAX_CARRYFORWARD != 0) {
								if (tday > LEAVEUNIT_MAX_CARRYFORWARD) {
									LvUtilItem newItem = new LvUtilItem();
									lvUtilList.add(idx0, newItem);
									newItem.code = "AL";
									newItem.date = date0;
									newItem.expire = getValidYymmdd(yy + 1, tstday);
									newItem.unit = tday - LEAVEUNIT_MAX_CARRYFORWARD;
									idx0++;
									idx1++;
									tday = LEAVEUNIT_MAX_CARRYFORWARD;
								}
							}
						}
						//\)
						LvUtilItem newItem = new LvUtilItem();
						lvUtilList.add(idx0, newItem);
						newItem.code = "AL";
						newItem.date = date0;
						if (LEAVE_EXPIRE_YEAR > 0) {
							if(!StringUtils.isBlank(tendday)) {
								newItem.expire = getValidYymmdd(yy + LEAVE_EXPIRE_YEAR, tendday);
							} else {
								newItem.expire = getValidYymmdd(yy + LEAVE_EXPIRE_YEAR, tstday);
							}
						} else 
							newItem.expire = MAX_DATE;
						if (yy + LEAVE_EXPIRE_YEAR > YEAR_TO_IGNORE_MAX_CARRYFORWARD)
							newItem.expire = MAX_DATE;
						newItem.unit = tday;
						idx0++;
						idx1++;
						//ifelse(IS_MACAU_3,1,\
						//\)
						break;
					}
				}
				if (idx0 >= idx1) {
					//ifelse(IS_MACAU_3,1,\
					//\,\
					if (yy < YEAR_TO_IGNORE_MAX_CARRYFORWARD) {
						if (LEAVE_EXPIRE_YEAR != 1 && LEAVEUNIT_MAX_CARRYFORWARD != 0) {
							if (tday > LEAVEUNIT_MAX_CARRYFORWARD) {
								LvUtilItem newItem = new LvUtilItem();
								lvUtilList.add(idx0, newItem);
								newItem.code = "AL";
								newItem.date = date0;
								newItem.expire = getValidYymmdd(yy + 1, tstday);
								newItem.unit = tday - LEAVEUNIT_MAX_CARRYFORWARD;
								idx0++;
								idx1++;
								tday = LEAVEUNIT_MAX_CARRYFORWARD;
							}
						}
					}
					//\)
					LvUtilItem newItem = new LvUtilItem();
					lvUtilList.add(idx0, newItem);
					newItem.code = "AL";
					newItem.date = date0;
					if (LEAVE_EXPIRE_YEAR > 0) {
							if(!StringUtils.isBlank(tendday)) {
								newItem.expire = getValidYymmdd(yy + LEAVE_EXPIRE_YEAR, tendday);
							} else {
								newItem.expire = getValidYymmdd(yy + LEAVE_EXPIRE_YEAR, tstday);
							}
					}
					else 
						newItem.expire = MAX_DATE;
					if (yy + LEAVE_EXPIRE_YEAR > YEAR_TO_IGNORE_MAX_CARRYFORWARD)
						newItem.expire = MAX_DATE;
					newItem.unit = tday;
					idx0++;
					//ifelse(IS_MACAU_3,1,\
					//\)
				}
			}
		}
		
		public void genCalPresetLeave(String p_eid, String p_code) throws Exception {
			int idx0 = lvUtilList.size();
			TableRec tr = br.getSelectUtil().getQueryResult("select emlvr_stdate, emlvr_enddate, emlvr_lvdaterange, emlvr_cancelled from emleaverange, leavereason"
					+ " where emlvr_emid = ? and lvrs_name = ? and lvrs_rg = emlvr_lvreasonrg"
					+ " order by emlvr_stdate", 
					new Wherecl().appendArgument(p_eid)
								.appendArgument(p_code));
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				if (!StringUtils.equals(tr.getFieldString("emlvr_cancelled"), "Y")) {
					LvUtilItem item = new LvUtilItem();
					lvUtilList.add(idx0, item);
					item.code = p_code;
					item.date = tr.getFieldDate("emlvr_stdate");
					item.expire = DateUtil.nextday(tr.getFieldDate("emlvr_enddate"));
					item.unit = (int)Math.ceil(tr.getFieldDouble("emlvr_lvdaterange") * LEAVEUNIT_PER_DAY);
					idx0++;
				}
			}
		}
		
		public void dumplvUtilList() {
			UniLog.log1("lvUtilList:%s", GsonUtil.objToStr(lvUtilList));
		}
	}
	

	private static Date getValidYymmdd(int p_yy, String pp_mdstr) {
		String mdstr = pp_mdstr;
		if(!mdstr.startsWith("/")) mdstr = "/"+mdstr;
		if (StringUtils.equals(mdstr, "/02/29")) {
			Date tmpDate = DateUtil.dateTimeStrToDate(String.format("%04d/03/01", p_yy));
			tmpDate = DateUtil.prevday(tmpDate);
			if (!DateUtil.dateToDateTimeStr(tmpDate, "/MM/dd").equals(mdstr))
				return DateUtil.nextday(tmpDate);
		}
		return DateUtil.dateTimeStrToDate(String.format("%04d%s", p_yy, mdstr));
	}
	public static int getLvStr2LeaveUnit(String p_lvstr) {
	int tday = 0;
	int cc = p_lvstr.indexOf('/');
	if (cc >= 0) {
		double tmpf = NumberUtils.toDouble(p_lvstr.substring(0, cc));
		tday = (int)Math.floor(tmpf * LEAVEUNIT_PER_DAY);
		String lvStr = p_lvstr.substring(cc + 1);
		cc = lvStr.indexOf(':');
		if (cc >= 0) {
			tmpf = NumberUtils.toDouble(lvStr.substring(0, cc));
			tday += (int)Math.floor(tmpf * 60 / MINUTES_PER_LEAVEUNIT);
			tmpf = NumberUtils.toDouble(lvStr.substring(cc + 1));
			tday += (int)Math.floor(tmpf / MINUTES_PER_LEAVEUNIT);
		} else {
			tmpf = NumberUtils.toDouble(lvStr);
			tday += (int)Math.floor(tmpf * 60 / MINUTES_PER_LEAVEUNIT);
		}
	} else
		tday = (int)Math.floor(NumberUtils.toDouble(p_lvstr) * LEAVEUNIT_PER_DAY);
	return tday;
	}

public static int getMinute2LeaveUnit(long p_minute) {
	return (int)Math.floor(p_minute / MINUTES_PER_LEAVEUNIT);
}	
	
	
	//Customer Specific routine to calculate the annual leave days of whole year base on date joined
	private static int customGetAnnualLeave(Date p_stdate, int yy, Date p_enddate, int p_sal, int p_mal, int p_ofs, String p_stday) throws Exception {
		//this calculation is only valid if p_stday > mm/dd(p_stdate)
		//ifelse(PRORA_LEAVE_EVERYYEAR,1,\
		if(true) return customGetAnnualLeaveReal(p_stdate, yy, p_enddate, p_sal, p_mal, p_ofs, p_stday);
		String tstday = p_stday;
		Date tstdate = getValidYymmdd(yy, tstday);
		if (p_enddate.compareTo(tstdate) <= 0) 
			return 0;
		if (tstdate.compareTo(p_stdate) <= 0)
			return customGetAnnualLeaveReal(p_stdate, yy, p_enddate, p_sal, p_mal, p_ofs, p_stday);
		else {
			String tstday2 = DateUtil.dateToDateTimeStr(p_stdate, "/MM/dd");
			int yy2 = yy;
			Date tmpdate = getValidYymmdd(yy2, tstday2);
			if (tstdate == tmpdate)
				return customGetAnnualLeaveReal(p_stdate, yy, p_enddate, p_sal, p_mal, p_ofs, p_stday);
			if (tmpdate.compareTo(tstdate) > 0) {
				yy2--;
				tmpdate = getValidYymmdd(yy2, tstday2);
			}
			Date tenddate = getValidYymmdd(yy2 + 1, tstday2);
			int tday = (int)((tstdate.getTime() - tmpdate.getTime()) / 86400000);
			int a0 = customGetAnnualLeaveReal(p_stdate, yy2, tenddate, p_sal, p_mal, p_ofs, tstday2);
			yy2++;
			tmpdate = getValidYymmdd(yy2,tstday2);
			tenddate = getValidYymmdd(yy2 + 1,tstday2);
			int a1 = customGetAnnualLeaveReal(p_stdate, yy2, tenddate, p_sal, p_mal, p_ofs, tstday2);
			if (a0 == a1)
				return customGetAnnualLeaveReal(p_stdate, yy, p_enddate, p_sal, p_mal, p_ofs, p_stday);

			a0 /= LEAVEUNIT_PER_DAY;
			a1 /= LEAVEUNIT_PER_DAY;
			//ifelse(LEAVEUNIT_CALCULATION_ROUNDUP,1,\
			//\,\
			int a2 = (int)Math.floor( (a0 * tday / DAYS_PER_YEAR * LEAVEUNIT_PER_DAY + LEAVEUNIT_INCREMENT_ROUNDING) / LEAVEUNIT_MIN_INCREMENT) * LEAVEUNIT_MIN_INCREMENT;
			//\)

			a0 = a0 * LEAVEUNIT_PER_DAY - a2;
			if (p_enddate.compareTo(tmpdate) <= 0) {
				tday = (int)((p_enddate.getTime() - tstdate.getTime()) / 86400000);
				int tday2 = (int)((tmpdate.getTime() - tstdate.getTime()) / 86400000);
				a2 = a0 * tday / tday2;
				return a2;
			} 
			else {
				tenddate = getValidYymmdd(yy + 1, tstday);
				tday = (int)((tenddate.getTime() - tmpdate.getTime()) / 86400000);
				//ifelse(LEAVEUNIT_CALCULATION_ROUNDUP,1,\
				//\,\
				a2 = (int)Math.floor((a1 * tday / DAYS_PER_YEAR * LEAVEUNIT_PER_DAY + LEAVEUNIT_INCREMENT_ROUNDING) / LEAVEUNIT_MIN_INCREMENT) * LEAVEUNIT_MIN_INCREMENT;
				//\)

				a1 = a2;
				if (p_enddate.compareTo(tenddate) < 0) {
					int tday2 = (int)((p_enddate.getTime() - tmpdate.getTime()) / 86400000);
					a2 = a1 * tday / tday2;
					return a2 + a0;
				} else 
					return a1 + a0;
			}
		}
		//\,\
		///)
	}

	private static int customGetAnnualLeaveReal(Date p_stdate, int yy, Date p_enddate, int p_sal, int p_mal, int p_ofs, String p_stday) throws Exception {
		Date stdate = p_stdate;
		Date enddate = p_enddate;
		int sy = NumberUtils.toInt(DateUtil.dateToDateTimeStr(stdate, "yyyy"));
		int cc = -1;
		String tstday = p_stday;
		Date tstdate = getValidYymmdd(yy, tstday);
		Date tenddate = getValidYymmdd(yy + 1, tstday);

		//ifelse(IS_STD_ANNUALLEAVE,1,\
		Date tmpstdate = getValidYymmdd(sy - 1, tstday);
		Date tmpenddate = getValidYymmdd(sy, tstday);
		if (stdate.compareTo(tmpstdate) >= 0 && stdate.compareTo(tmpenddate) < 0)
			sy--;
		//\)
		
		//ifelse(IS_MASTV,1,\
		//\)

		//ifelse(IS_STD_ANNUALLEAVE,1,\
		int sal = p_sal;
		int mal = p_mal;
		if (yy - sy - p_ofs >= 0)
			cc = sal + yy - sy - p_ofs;
		else
			cc = sal;
		if (cc > mal) 
			cc = mal;
		//\)
		//ifelse(IS_MACAU_3,1,\
		//\)

		if (cc < 0)
			throw new Exception("Annual Leave Calculation Mathod not Set");
		//cc = num of annual leave days for whole year
		if (stdate.compareTo(tstdate) > 0 || enddate.compareTo(tenddate) < 0) {
			Date date0, date1;
			if (enddate.compareTo(tenddate) < 0) 
				date0 = DateUtil.nextday(enddate);
			else 
				date0 = tenddate;
			if (stdate.compareTo(tstdate) > 0) 
				date1 = stdate;
			else 
				date1 = tstdate;
			int tday = (int)((date0.getTime() - date1.getTime()) / 86400000);
			if (tday >= DAYS_PER_YEAR) 
				return cc * LEAVEUNIT_PER_DAY;
			if (tday <= 0) 
				return 0;
			//ifelse(LEAVEUNIT_CALCULATION_ROUNDUP,1,\
			//\,\
			return (int)Math.floor((cc * tday / DAYS_PER_YEAR * LEAVEUNIT_PER_DAY + LEAVEUNIT_INCREMENT_ROUNDING) / LEAVEUNIT_MIN_INCREMENT) * LEAVEUNIT_MIN_INCREMENT;
			//\)
		} 
		else
			return cc * LEAVEUNIT_PER_DAY;
	}

	public void calLeaveRemain() throws Exception {
		LeaveCal leaveCal = leaveCalHash.get(getCellString("em_eid"));
		leaveCal.emx_ralstr = calLeaveRemain("AL",leaveCal);
		leaveCal.emx_rclstr = calLeaveRemain("CL",leaveCal);
		if(getCell("emx_ralstr") != null) getCell("emx_ralstr").set(leaveCal.emx_ralstr);
		if(getCell("emx_rclstr") != null) getCell("emx_rclstr").set(leaveCal.emx_rclstr);
//		getCell("emx_rclstr").set(calLeaveRemain("CL"));
//		getCell("emx_ralstr").set(calLeaveRemain("AL"));
//		getCell("emx_rclstr").set(calLeaveRemain("CL"));
		clearLeaveRemain("AL", "CL");
	}
	private String calLeaveRemain(String code,LeaveCal leaveCal) throws Exception {
		leaveCal.resetLvUtilList(code);
		Vector<BiCellCollection> recs = getSubLinkResult( getLeaveAppliationDetViewName());
		for (BiCellCollection cc : recs) {
			if (StringUtils.equals(cc.getString("lv_reason"), code)) {
				int nrUnit = leaveCal.genCalLeaveRemained(code, cc.getDate("lv_sdate"), cc.getInt("lv_leaveunit"));
				cc.getCell("lvx_nrunit").set(nrUnit);
				cc.getCell("lvx_nrdays").set(NumberUtils.toDouble(getLeaveUnit2LvStr(nrUnit)));
			}
		}

		List<RlvItem> rlvList = new ArrayList<RlvItem>();
		leaveCal.genGetLeaveRemain(code, DateUtil.today(), DateUtil.yearEnd(DateUtil.today()), rlvList);
		StringBuilder sbRlvStr = new StringBuilder();
		int cc = 0;
		for (RlvItem item : rlvList) { 
			if (sbRlvStr.length() > 0)
				sbRlvStr.append(", ");
			if (item.eftd == null || item.eftd.compareTo(DateUtil.today()) <= 0) 
				cc += item.unit;
			else {
				if (cc != 0) {
					sbRlvStr.append(getLeaveUnit2LvStr(cc));
					sbRlvStr.append(" + ");
					cc = 0;
				}
				sbRlvStr.append(getLeaveUnit2LvStr(item.unit));
				sbRlvStr.append(" ( ");
				sbRlvStr.append(DateUtil.dateToDateTimeStr(item.eftd, "yyyy/MM/dd"));
				sbRlvStr.append(" )");
			}
		}
		if (cc != 0)
			sbRlvStr.append(getLeaveUnit2LvStr(cc));
		UniLog.log1("genGetLeaveRemain %s,%s,%s,%s,%s", code, DateUtil.today(), DateUtil.yearEnd(DateUtil.today()), GsonUtil.objToStr(rlvList), sbRlvStr);
		return sbRlvStr.toString();
	}
	
	private void clearLeaveRemain(String... excludeCode) throws Exception {
		Vector<BiCellCollection> recs = getSubLinkResult( getLeaveAppliationDetViewName());
		for (BiCellCollection cc : recs) {
			if (!StringUtils.equalsAny(cc.getString("lv_reason"), excludeCode)) {
				cc.getCell("lvx_nrunit").set(0);
				cc.getCell("lvx_nrdays").set(0.0);
			}
		}
	}
	public static String getLeaveUnit2LvStr(int p_leaveunit) {
		if (p_leaveunit < 0)  
			return "-" + getLeaveUnit2LvStr(-p_leaveunit);
		StringBuilder sbLvStr = new StringBuilder();
		int cc = p_leaveunit / LEAVEUNIT_PER_DAY;
		int r0 = p_leaveunit - cc * LEAVEUNIT_PER_DAY;
		sbLvStr.append(cc);
		if (r0 > 0) {
			if (r0 == LEAVEUNIT_PER_HALFDAY) {
				sbLvStr.append(".5");
				return sbLvStr.toString();
			} else {
				//ifelse(SHOW_LEAVE_IN_DECIMAL,1,\
				cc = (int)Math.round((double)r0 / LEAVEUNIT_PER_DAY * 10);
				sbLvStr.append(".");
				sbLvStr.append(cc);
				//\,\
				///)
			}
		}
		return sbLvStr.toString();
	}	
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		leaveCalHash = new HashMap<String,LeaveCal>();
		return(super.afterLoadSerialMap());
	}
	
	@Override
	public boolean fetchOneRecV(int p_tridx) {
		boolean ok = super.fetchOneRecV(p_tridx);
		if(ok) {
			String eid = getCellString("em_eid");
			LeaveCal leaveCal = leaveCalHash.get(eid);
			try {
				if(getCell("emx_ralstr") != null && leaveCal != null) getCell("emx_ralstr").set(leaveCal.emx_ralstr);
				if(getCell("emx_rclstr") != null && leaveCal != null) getCell("emx_rclstr").set(leaveCal.emx_rclstr);
			} catch (Exception ex) {
				UniLog.log(ex);
			};
		}
		return(ok);
	}
	
	protected void loadLeaveCal() throws Exception {
		String eid = getCellString("em_eid");
		LeaveCal leaveCal = new LeaveCal(this,getCurrentCollection());
		Date almaxd1 = DateUtil.yearEnd(DateUtil.today());
		leaveCal.clearLvUtilList("");
		leaveCal.genCalLeave("AL", eid, almaxd1);
		leaveCal.genCalLeave("CL", eid, almaxd1);
		leaveCalHash.put(eid, leaveCal);
		calLeaveRemain();
	}
	
	@Override
	public boolean loadOneRecV(int p_tridx)
	{
		boolean ok = super.loadOneRecV(p_tridx);
		if(ok) {
		String eid = getCellString("em_eid");
		LeaveCal leaveCal = leaveCalHash.get(eid);
		if(leaveCal == null) {
			//lvUtilList.clear();
			try {
				fetchOneRecV(p_tridx);
				loadLeaveCal();
				/*
				leaveCal = new LeaveCal(this,getCurrentCollection());
				Date almaxd1 = DateUtil.yearEnd(DateUtil.today());
				leaveCal.clearLvUtilList("");
				leaveCal.genCalLeave("AL", eid, almaxd1);
				leaveCal.genCalLeave("CL", eid, almaxd1);
				leaveCalHash.put(eid, leaveCal);
				calLeaveRemain();
				*/
			} catch (Exception ex) {
				UniLog.log(ex);
			}	
		}
		try {
			if(getCell("emx_ralstr") != null && leaveCal != null) getCell("emx_ralstr").set(leaveCal.emx_ralstr);
			if(getCell("emx_rclstr") != null && leaveCal != null) getCell("emx_rclstr").set(leaveCal.emx_rclstr);
		} catch (Exception ex) {
			UniLog.log(ex);
		};
		}
		return(ok);
	}
//	@Override
//	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
//		super.afterLoadCollection(p_isFetch,p_cc);
//		String eid = p_cc.getCellString("em_eid");
//		LeaveCal leaveCal = leaveCalHash.get(eid);
//		if(leaveCal == null) {
//			leaveCal = new LeaveCal(this,p_cc);
//			//lvUtilList.clear();
//			try {
//				Date almaxd1 = DateUtil.yearEnd(DateUtil.today());
//				leaveCal.clearLvUtilList("");
//				leaveCal.genCalLeave("AL", eid, almaxd1);
//				leaveCal.genCalLeave("CL", eid, almaxd1);
//				leaveCalHash.put(eid, leaveCal);
//			} catch (Exception ex) {
//				UniLog.log(ex);
//			}	
//		}
//	}
	
	public LeaveCal getLeaveCal(String eid) {
		return(leaveCalHash.get(eid));
	}
	
	private String leaveDetViewName = null;
	public String getLeaveAppliationDetViewName() {
		if(leaveDetViewName == null) {
			for(BiResult sr : getSubLinks()) {
				if(sr.getView().getTable().getName().equals("leave")) {
					leaveDetViewName = sr.getView().getName();
					break;
				}
			}
		}
		return(leaveDetViewName);
	}
}
