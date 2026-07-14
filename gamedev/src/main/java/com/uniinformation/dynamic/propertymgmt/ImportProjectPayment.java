package com.uniinformation.dynamic.propertymgmt;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.propertymgmt.BiResultProjectPayment;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.NumberUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiFileuploadDlg;
import com.uniinformation.zkbi.ZkBiMsgbox;

import static com.uniinformation.dynamic.propertymgmt.ImportProjectFeeUnit.quickReadExcelByRow;
import static com.uniinformation.dynamic.propertymgmt.ImportProjectFeeUnit.getCellString;
import static com.uniinformation.dynamic.propertymgmt.ImportProjectFeeUnit.getCellDouble;
import static com.uniinformation.dynamic.propertymgmt.ImportProjectFeeUnit.getCellDate;
import static com.uniinformation.utils.ZkUtil.throwConsumer;
import static com.uniinformation.utils.ZkUtil.throwFunction;
import static com.uniinformation.utils.ZkUtil.throwPredicate;

public class ImportProjectPayment implements EventListener<Event> {
	private Map<String, Integer> colMap = new LinkedHashMap<>();
	
	private SessionHelper sh;
	private String locDesc;
	private Runnable refreshAction;
	private BiResultProjectPayment paymentBr;
	private Set<String> payMethodList;
	private Map<String, Integer> projectPeriodMap = new HashMap<>();
	
	@Override
	public void onEvent(Event event) throws Exception {
		sh = ZkSessionHelper.getSessionHelper();
		locDesc = Erpv4Config.getLcDesc(sh, Erpv4Config.getDefaultLcrg(sh));
		refreshAction = (Runnable)event.getTarget().getAttribute("refreshAction");
		ZkBiFileuploadDlg.get("zkf/propertymgmt/Fileuploaddlg.zul", new HashMap<>(), null, "導入", 1, -1, true, ev -> {
			AMedia amedia = (AMedia) ev.getMedia();
       		if (amedia != null && Objects.equals(amedia.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
               	if (Objects.equals(ev.getTarget().getId(), "upload2")) {
               		ZkBiMsgbox.show(ZkBiMsgbox.Type.question, sh.getLabel("Are you sure you want to delete all records?"), (ev1, btn) -> {
               			if (btn.getIdx() == 0) {
							BiResult sr = null;
							try {
								sr = BiResultHelper.create(sh, "propertymgmt.ProjectPayment", String.format("col_c = '%s'", locDesc), -1, null);
								for (int i = 0; i < sr.getRowCount(); i++) {
									sr.fetchOneRecV(i);
									sr.markDelete(sr.getTrStatObj(i), true);
								}
								ReturnMsg rtn = sr.batchAddUpdateDelete();
								if (rtn != null && !rtn.getStatus())
									throw new Exception(rtn.getMsg());
								refreshAction.run();
								importData(amedia);
							} catch (Exception e) {
								ZkBiMsgbox.show(ZkBiMsgbox.Type.error, e.getMessage());
							} finally {
								if (sr != null)
									sr.close();
							}
              			}
               		}, "Ok", "Cancel");
               	} else
               		importData(amedia);
       		} else
       			ZkUtil.showErrMsg(sh.getLabel("Invalid import file"));
		});
	}
	
	private void importData(AMedia amedia) throws Exception {
  		paymentBr = (BiResultProjectPayment) sh.newBiResult("propertymgmt.ProjectPayment");
       	payMethodList = ZkUtil.getTableRecStream(paymentBr.getSelectUtil(), "select ppm_name from paymentmethod").map(c -> c.getString("ppm_name")).collect(Collectors.toSet());
       	try {
       		paymentBr.beginWork();
       		readExcel(amedia.getByteData());
       		ZkUtil.msg("導入完成");
       		paymentBr.commitWork();
       	} catch (Exception e) {
       		UniLog.log(e);
       		paymentBr.rollbackWork();
       		ZkUtil.errMsg(e.getMessage());
       	} finally {
       		paymentBr.close();
       	}
      	refreshAction.run();
	}

	private void readExcel(byte[] data) throws Exception {
		quickReadExcelByRow(sh, data, 0, 0, Integer.MAX_VALUE, (row, sheet) -> {
			if (row.getRowNum() == 0) {
				Map<String, Integer> headerMap = Stream.of("物業單位", "項目編號", "項目名稱", "分攤金額", "支付方式", "繳費日期", "已繳").collect(Collectors.toMap(k -> k, k -> 0));
				Map<String, String> headerColMap = MapUtil.of("物業單位", "unit", "項目編號", "projectno", "項目名稱", "projectname", "分攤金額", "alcamt", "支付方式", "paymethod", "繳費日期", "paiddate", "已繳", "paidamt");
				List<String> list = IntStream.rangeClosed(1, 5).mapToObj(i -> String.format("第%s期數", NumberUtil.toTChinese(i))).collect(Collectors.toList());
				headerMap.putAll(list.stream().collect(Collectors.toMap(s -> s, s -> 0)));
				headerColMap.putAll(IntStream.range(0, list.size()).mapToObj(i -> i).collect(Collectors.toMap(i -> list.get(i), i -> "periodamt" + (i + 1))));
				colMap.clear();
				projectPeriodMap.clear();
	
				IntStream.rangeClosed(0, row.getLastCellNum()).forEach(i -> {
					String header = StringUtils.defaultString(getCellString(row, i)).trim();
					Integer count = headerMap.get(header);
					if (count != null) {
						count++;
						headerMap.put(header, count);
						colMap.put(headerColMap.get(header) + (StringUtils.equalsAny(header, "繳費日期", "支付方式", "已繳") ? "" + count : ""), i);
					}
				});
				UniLog.log1("colMap:%s", colMap);
			} else {
				Map<String, Object> m = new LinkedHashMap<>();
				for (Map.Entry<String, Integer> entry : colMap.entrySet()) {
					String key = entry.getKey();
					int colNum = entry.getValue();
					if (StringUtils.startsWithAny(key, "alcamt", "periodamt", "paidamt")) {
						Double d = getCellDouble(row, colNum);
						m.put(key, d != null ? d : 0.0);
					} else if (StringUtils.startsWith(key, "paiddate"))
						m.put(key, getCellDate(row, colNum, "y/m/d"));
					else
						m.put(key, StringUtils.defaultString(getCellString(row, colNum)));
				}
				//UniLog.log1("%d, %s", row.getRowNum(), m);
				String unit = (String)m.get("unit");
				if (StringUtils.isBlank(unit))
					return false;
				String projectNo = (String)m.get("projectno");
				if (StringUtils.isBlank(projectNo))
					return true;
				
				Integer periodCount = projectPeriodMap.get(projectNo);
				if (periodCount == null) {
					periodCount = ZkUtil.getFirstTableRec(paymentBr.getSelectUtil(), "select col_g from projectfee where col_a = ? and col_c = ?", new Wherecl().appendArgument(projectNo).appendArgument(locDesc))
											.map(throwFunction(tr -> tr.getFieldInt("col_g"))).orElse(0);
					projectPeriodMap.put(projectNo, periodCount);
				}
				if (periodCount < 1)
					throw new Exception(String.format("項目編號%s不正確[%s]", projectNo, getCellPos("projectno", row)));
				List<Integer> periodRange = IntStream.rangeClosed(1, periodCount).mapToObj(i -> i).collect(Collectors.toList());
				
				List<Long> l = Stream.of(colMap.keySet().stream().filter(k -> k.startsWith("periodamt")).count(),
										colMap.keySet().stream().filter(k -> k.startsWith("paiddate")).count(),
										colMap.keySet().stream().filter(k -> k.startsWith("paymethod")).count(),
										colMap.keySet().stream().filter(k -> k.startsWith("paidamt")).count()).distinct().collect(Collectors.toList());
				if (l.size() != 1 || l.get(0) < periodCount)
					throw new Exception(sh.getLabel("Invalid import file"));
				StringBuilder sb = new StringBuilder("select upf_unit unit, upf_allocamt alcamt, upf_projectname projectname, ");
				sb.append(periodRange.stream().map(i -> String.format("upf_period%damt periodamt%d, ", i, i)).collect(Collectors.joining()));
				sb.append(periodRange.stream().map(i -> String.format("upf_payamt%d paidamt%d, ", i, i)).collect(Collectors.joining()));
				sb.append(periodRange.stream().map(i -> String.format("upf_voucherno%d vno%d, ", i, i)).collect(Collectors.joining()));
				sb.append(periodRange.stream().map(i -> String.format("p%d.col_a paiddate%d, ", i, i)).collect(Collectors.joining()));
				sb.append(periodRange.stream().map(i -> String.format("p%d.col_g paymethod%d ", i, i)).collect(Collectors.joining(", ")));
				sb.append("from unitprojectfee ");
				sb.append(periodRange.stream().map(i -> String.format("left join projectpayment p%d on p%d.col_b = upf_voucherno%d ", i, i, i)).collect(Collectors.joining()));
				sb.append("where upf_projectno = ? and upf_location = ? and upf_unit = ?");
				UniLog.log1("sb:%s,%s,%s,%s", sb.toString(), projectNo, locDesc, unit);
				TableRec tr = ZkUtil.getFirstTableRec(paymentBr.getSelectUtil(), sb.toString(), new Wherecl().appendArgument(projectNo).appendArgument(locDesc).appendArgument(unit)).orElse(null);
				if (tr == null)
					throw new Exception(String.format("项目编号或物业单位不正确[%s]", getCellPos("unit", row)));
				if (!tr.getFieldString("projectname").equals(m.get("projectname")))
					throw new Exception(String.format("项目名稱不正确[%s]", getCellPos("projectname", row)));
				if (tr.getFieldDouble("alcamt") != (double)m.get("alcamt"))
					throw new Exception(String.format("分攤金額不正確[%s]", getCellPos("alcamt", row)));
				periodRange.forEach(throwConsumer(i -> {
					if (tr.getFieldDouble("periodamt" + i) != (double)m.get("periodamt" + i))
						throw new Exception(String.format("第%d期數不正確[%s]", i, getCellPos("paidamt" + i, row)));
				}));
				periodRange.forEach(throwConsumer(i -> {
					double periodAmt = (double)m.get("periodamt" + i);
					double paidAmt = (double)m.get("paidamt" + i);
					Date paidDate = (Date)m.get("paiddate" + i);
					String payMethod = (String)m.get("paymethod" + i);
					if (paidDate != null) {
						if (periodAmt != paidAmt || paidAmt == 0)
							throw new Exception(String.format("已付金額不正確[%s]", getCellPos("paidamt" + i, row)));
						if (!payMethod.isEmpty() && !payMethodList.contains(payMethod))
							throw new Exception(String.format("支付方式不正確[%s]", getCellPos("paymethod" + i, row)));
						if (i > 1) {
							Date date = (Date)m.get("paiddate" + (i - 1));
							if (date == null || date.after(paidDate))
								throw new Exception(String.format("繳費日期不正確[%s]", getCellPos("paiddate" + (i - 1), row)));
						}
					}
				}));

				Map<String, List<Integer>> m1 = periodRange.stream().filter(throwPredicate(i -> StringUtils.isNotBlank(tr.getFieldString("vno" + i))))
														.collect(Collectors.groupingBy(throwFunction(i -> tr.getFieldString("vno" + i)), Collectors.toList()));
				for (Map.Entry<String, List<Integer>> entry : m1.entrySet()) {
					String vno = entry.getKey();
					List<Integer> periodList = entry.getValue();
					Date paidDate = DateUtil.dayBeginning(tr.getFieldDate("paiddate" + periodList.get(0)));
					String paymethod = tr.getFieldString("paymethod" + periodList.get(0));
					//UniLog.log1("vno:%s, periodList:%s, paidDate:%s, paymethod:%s", vno, periodList, paidDate, paymethod);
					List<Date> dstPaidDateList = periodList.stream().map(i -> (Date)m.get("paiddate" + i)).collect(Collectors.toList());
					if (dstPaidDateList.stream().distinct().count() > 1)
						throw new Exception(String.format("缴费日期与单号%s冲突[%s]", vno, getCellPos("paiddate" + periodList.get(0), row)));
					List<String> dstPayMethodList = periodList.stream().map(i -> (String)m.get("paymethod" + i)).collect(Collectors.toList());
					if (dstPayMethodList.stream().distinct().count() > 1)
						throw new Exception(String.format("支付方式与单号%s冲突[%s]", vno, getCellPos("paymethod" + periodList.get(0), row)));
					Date paidDate1 = dstPaidDateList.get(0);
					if (DateUtil.isValid(paidDate1) && !paidDate1.equals(paidDate)) {
						UniLog.log1("update projectpayment %s, %s", paidDate1, vno);
						paymentBr.getSelectUtil().executeUpdate("update projectpayment set col_a = ? where col_b = ?", new Wherecl().appendArgument(paidDate1).appendArgument(vno));
					}
					String payMethod1 = dstPayMethodList.get(0);
					if (!payMethod1.equals(paymethod)) {
						UniLog.log1("update projectpayment %s, %s", payMethod1, vno);
						paymentBr.getSelectUtil().executeUpdate("update projectpayment set col_g = ? where col_b = ?", new Wherecl().appendArgument(payMethod1).appendArgument(vno));
					}
				}

				Map<GroupMember, List<Integer>> m2 = periodRange.stream().filter(throwPredicate(i -> StringUtils.isBlank(tr.getFieldString("vno" + i)) && m.get("paiddate" + i) != null))
													.collect(Collectors.groupingBy(i -> new GroupMember((Date)m.get("paiddate" + i), (String)m.get("paymethod" + i)), TreeMap::new, Collectors.toList()));
				BiResult sr = paymentBr.getSubLink("propertymgmt.PayProjectItem");
				for (Map.Entry<GroupMember, List<Integer>> entry : m2.entrySet()) {
					GroupMember gm = entry.getKey();
					paymentBr.clearCurrentRec();
					paymentBr.getCell("col_a").set(gm.paidDate);
					paymentBr.getCell("col_g").set(gm.payMethod);
					List<Integer> periodList = entry.getValue();
					BiCellCollection col = null;
					for (int i = 0; i < periodList.size(); i++) {
						int period = periodList.get(i);
						if (i == 0 || col.getCellInt("col_e") + col.getCellInt("col_f") < period) {
							sr.addSubRecord(col = sr.newRowCollection(), -1, "");
							col.getCell("col_c").set(projectNo);
							col.getCell("col_d").set(tr.getFieldString("unit"));
							col.getCell("col_e").set(period);
							col.getCell("col_f").set(1);
							col.getCell("col_g").set(tr.getFieldDouble("periodamt" + period));
						} else {
							col.getCell("col_f").set(period - col.getCellInt("col_e") + 1);
							col.getCell("col_g").set(col.getCellDouble("col_g") + tr.getFieldDouble("periodamt" + period));
						}
					}
					ReturnMsg rtn = paymentBr.addCurrent();
					//UniLog.log1("paymentBr:%s, sr:%s", ZkUtil.getBiResultRecordJson(paymentBr), ZkUtil.getBiResultRecordJson(sr));
					if (!rtn.getStatus())
						throw new Exception(rtn.getMsg());
				}
			}
			return true;
		});
	}
	
	private String getCellPos(String key, XSSFRow row) {
		return CellReference.convertNumToColString(colMap.get(key)) + (row.getRowNum() + 1);
	}
	
	private static class GroupMember implements Comparable<GroupMember> {
		Date paidDate;
		String payMethod;

		GroupMember(Date paidDate, String payMethod) {
			this.paidDate = paidDate;
			this.payMethod = payMethod;
		}
		@Override
        public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			GroupMember that = (GroupMember) o;
			return Objects.equals(paidDate, that.paidDate) && Objects.equals(payMethod, that.payMethod);
		}
		@Override
        public int hashCode() {
            return Objects.hash(paidDate, payMethod);
        }
		@Override
		public int compareTo(GroupMember o) {
			if (this == o) return 0;
			if (o == null) return 1;
			int c = Objects.compare(paidDate, o.paidDate, Comparator.nullsFirst(Date::compareTo));
			if (c != 0)
				return c;
			return Objects.compare(payMethod, o.payMethod, Comparator.nullsFirst(String::compareTo));
		}
	}
}
