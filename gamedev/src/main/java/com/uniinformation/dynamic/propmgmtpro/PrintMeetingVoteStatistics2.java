package com.uniinformation.dynamic.propmgmtpro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zul.Filedownload;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZipUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.BiUtil.CheckedSupplier;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintMeetingVoteStatistics2 extends BiActionHandler {
	private static final SimpleDateFormat msdf = new SimpleDateFormat("yyyy-M-d");
	private int maxDocCount = 10000;
	private SessionHelper sh;
	private BiResult br, brMettingTopic, brMettingSignin;
	
	private Map<String, MyWorkbook> workbookMap = new HashMap<>();
	
	public PrintMeetingVoteStatistics2(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		useAsync = p_bibase != null ? p_bibase.getSessionHelper().getAllowBatchPrtdocAsync() : false;
	}

	public PrintMeetingVoteStatistics2() {
		this(null);
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		sh = p_result.getSessionHelper();
		if (cnt > maxDocCount)
			return new ReturnMsg(false,sh.getLabel("Cannot Print more than 10000 documents"));
		workbookMap.clear();
		return ReturnMsg.defaultOk;		
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			if (!p_result.fetchOneRecV(p_recIdx)) 
				return new ReturnMsg(false,sh.getLabel("Fetch Record failed"));
			br = p_result;
			MyWorkbook wb = workbookMap.get(br.getCellString("lc_desc"));
			if (wb == null)
				workbookMap.put(br.getCellString("lc_desc"), (wb = new MyWorkbook()));
			wb.writeWorkSheet();
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,String.format(sh.getLabel("Export Excel %d Failed"), p_recIdx)));
		}
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		return downloadExcel();
	}

	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		biBase.hideProgressPanel();
		cb.callback(downloadExcel());
	}

	private ReturnMsg downloadExcel() {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			if (workbookMap.size() > 1) {
				List<Object> list = new ArrayList<>();
				workbookMap.entrySet().stream().forEach(e -> {
					try (ByteArrayOutputStream os1 = new ByteArrayOutputStream()) {
						e.getValue().write(os1);
						list.add(os1.toByteArray());
						list.add(getFileName(e.getKey(), true) + ".xlsx");
					} catch (IOException ex) {
						UniLog.log(ex);
						list.add(null);
						list.add("");
					}
				});
				ZipUtil.createZip(null, false, os, list.toArray());
				Filedownload.save(os.toByteArray(), "application/zip", "簽到和選票完整記錄.zip");
			} else {
				Map.Entry<String, MyWorkbook> entry = workbookMap.entrySet().iterator().next();
				entry.getValue().write(os);
				Filedownload.save(os.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", getFileName(entry.getKey(), true) + ".xlsx");
			}
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(e);
		}
		return ReturnMsg.defaultOk;
	}
	
	private static String getFileName(String lcdesc, boolean replaceNoMatch) {
		String s = String.format("%s 簽到和選票完整記錄", lcdesc);
		if (replaceNoMatch)
			s = s.replace("[<>:\"/\\\\|?*\\x00-\\x1F]", "");
		return s;
	}
	
	
	private class MyWorkbook extends XSSFWorkbook {
		private XSSFCellStyle commonCellStyle, boldCellStyle, doubleCellStyle, doubleBoldCellStyle;

		MyWorkbook() {
			XSSFDataFormat format = createDataFormat();

			commonCellStyle = createCellStyle();
	        commonCellStyle.setAlignment(HorizontalAlignment.CENTER);
	        commonCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	        XSSFFont font = createFont();
	        font.setFontHeightInPoints((short)12);
	        commonCellStyle.setFont(font);

	        boldCellStyle = createCellStyle();
	        boldCellStyle.cloneStyleFrom(commonCellStyle);
	        font = cloneFont(this, font);
	        font.setBold(true);
	        boldCellStyle.setFont(font);

	        doubleCellStyle = createCellStyle();
	        doubleCellStyle.cloneStyleFrom(commonCellStyle);
	        doubleCellStyle.setDataFormat(format.getFormat("0.0000"));

	        doubleBoldCellStyle = createCellStyle();
	        doubleBoldCellStyle.cloneStyleFrom(doubleCellStyle);
	        font = cloneFont(this, font);
	        font.setBold(true);
	        doubleBoldCellStyle.setFont(font);
		}

		public void writeWorkSheet() throws Exception {
			brMettingTopic = BiResultHelper.create(sh, "propmgmtpro.MettingTopic", brMettingTopic, String.format("col_a = %d and col_b = '%s'", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
			brMettingSignin = BiResultHelper.create(sh, "propmgmtpro.MettingSignin", brMettingSignin, String.format("col_a = %d and col_b = '%s'", br.getCellInt("col_a"), br.getCellString("col_b")), null, -1, null);
			List<Map<String, Object>> signinList = ZkUtil.getBiResultRecordMap(brMettingSignin, false);
			List<Map<String, Object>> signinAttList = signinList.stream().filter(m -> StringUtils.equalsAny((String)m.get("col_d"), "Y", "A")).collect(Collectors.toList());
			List<Map<String, Object>> signinNoAttList = signinList.stream().filter(m -> !StringUtils.equalsAny((String)m.get("col_d"), "Y", "A")).collect(Collectors.toList());
			AtomicDouble totalQuotientRef = new AtomicDouble();
			Map<String, Object> voteMap = PrintMeetingVoteStatistics.getVoteMap(ZkUtil.getBiResultRecordMap(brMettingTopic, false), totalQuotientRef);
			Map<String, Object> voteMap2 = PrintMeetingVoteStatistics.getVoteMap(ZkUtil.getBiResultRecordMap(brMettingTopic, false), totalQuotientRef);
			
			List<String> ks = Lists.newArrayList("贊成", "反對", "棄權", "空白票");
			List<String> voteKeyList = voteMap.entrySet().stream().filter(e -> e.getValue() instanceof CheckedSupplier && StringUtils.endsWithAny(e.getKey(), ks.toArray(new String[0]))).map(e -> e.getKey()).collect(Collectors.toList());
			List<String> dvoteKeyList = voteMap.entrySet().stream().filter(e -> e.getValue() instanceof Double).map(e -> e.getKey()).collect(Collectors.toList());
			voteKeyList.sort((a, b) -> {
				String sa = a.substring(0, 4) + ks.indexOf(a.substring(4));
				String sb = b.substring(0, 4) + ks.indexOf(b.substring(4));
				return sa.compareTo(sb);
			});
			Map<String, Integer> oMap = new LinkedHashMap<>();
			List<String> vList = new ArrayList<>();
			voteKeyList.stream().forEach(s -> {
				int topicNum = Integer.parseInt(s.substring(0, 2));
				int optionNum = Integer.parseInt(s.substring(2, 4));
				String optionStr = optionNum > 0 ? topicNum + "." + optionNum : topicNum + "";
				String voteStr = s.substring(4);
				Integer count = oMap.get(optionStr);
				oMap.put(optionStr, count != null ? count + 1 : 1);
				vList.add(voteStr);
			});
	
			XSSFSheet workSheet = createSheet(msdf.format(br.getCellDate("col_b")));
			XSSFRow sheetRow = workSheet.createRow(0);
			final int voteColOffset = 5;
			for (int i = 0; !voteKeyList.isEmpty() && i < voteColOffset; i++)
				workSheet.addMergedRegion(new CellRangeAddress(0, 1, i, i));
			setCellValue(sheetRow, true, 0, "座號", "樓層", "單位", "份額", "簽到狀態");
			int col = voteColOffset;
			for (Map.Entry<String, Integer> entry : oMap.entrySet()) {
				workSheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + entry.getValue() - 1));
				setCellValue(sheetRow, true, col, entry.getKey());
				col += entry.getValue();
			}
			sheetRow = workSheet.createRow(1);
			setCellValue(sheetRow, true, voteColOffset, vList.toArray(new Object[0]));
	
			int row = voteKeyList.isEmpty() ? 1 : 2;
			for (Map<String, Object> m : signinAttList) {
				sheetRow = workSheet.createRow(row);
				totalQuotientRef.set((double)m.get("pcol_m"));
				for (String key : dvoteKeyList)
					voteMap.put(key, 0.0);
				Arrays.stream(((String)m.get("col_f")).split(",")).filter(s -> StringUtils.isNotBlank(s)).forEach(voteKey -> {
					Object o = voteMap.get(voteKey);
					Object o2 = voteMap2.get(voteKey);
					if (o != null && o instanceof Double) {
						voteMap.put(voteKey, (double)o + (double)m.get("pcol_m"));
						voteMap2.put(voteKey, (double)o2 + (double)m.get("pcol_m"));
					}
				});
				setCellValue(sheetRow, false, 0, m.get("pcol_c"), m.get("pcol_d"), m.get("pcol_e"), m.get("pcol_m"), m.get("ss_desc"));
				setCellValue(sheetRow, false, voteColOffset, voteKeyList.stream().map(k -> voteMap.get(k)).toArray());
				row++;
			}
			for (Map<String, Object> m : signinNoAttList) {
				sheetRow = workSheet.createRow(row);
				setCellValue(sheetRow, false, 0, m.get("pcol_c"), m.get("pcol_d"), m.get("pcol_e"), m.get("pcol_m"), m.get("ss_desc"));
				row++;
			}
			
			workSheet.shiftRows(0, workSheet.getLastRowNum(), 1);
			workSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 15));
			sheetRow = workSheet.createRow(0);
			setCellValue(sheetRow, false, 0, getFileName(br.getCellString("lc_desc"), false));

			XSSFCellStyle style = cloneCellStyle(this, sheetRow.getCell(0));
			XSSFFont font = cloneCellFont(this, sheetRow.getCell(0));
			font.setFontHeightInPoints((short)13);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setFont(font);

			if (!voteKeyList.isEmpty()) {
				workSheet.shiftRows(1, workSheet.getLastRowNum(), 1);
				sheetRow = workSheet.createRow(1);
				setCellValue(sheetRow, false, 3, signinAttList.stream().mapToDouble(m -> (double)m.get("pcol_m")).sum());
				totalQuotientRef.set(signinAttList.stream().mapToDouble(m -> (double)m.get("pcol_m")).sum());
				setCellValue(sheetRow, false, voteColOffset, voteKeyList.stream().map(k -> voteMap2.get(k)).toArray());
				workSheet.createFreezePane(0, 4);
			} else
				workSheet.createFreezePane(0, 2);
			workSheet.setColumnWidth(0, 1500);
			workSheet.setColumnWidth(1, 1500);
			workSheet.setColumnWidth(2, 1500);
		}

		private void setCellValue(XSSFRow sheetRow, boolean isBold, int startCol, Object...values) throws Exception {
			int i = 0;
			for (Object v : values) {
				XSSFCell cell = sheetRow.createCell(startCol + i);
				if (v instanceof CheckedSupplier) {
					double d = ((CheckedSupplier<Double>)v).get();
					if (d != 0) {
						cell.setCellStyle(isBold ? doubleBoldCellStyle : doubleCellStyle);
						cell.setCellValue(d);
					} else if (sheetRow.getRowNum() == 1) {
						cell.setCellStyle(isBold ? boldCellStyle : commonCellStyle);
						cell.setCellValue("-");
					}
				} else if (v instanceof Double) {
					cell.setCellStyle(isBold ? doubleBoldCellStyle : doubleCellStyle);
					cell.setCellValue((double)v);
				} else {
					cell.setCellStyle(isBold ? boldCellStyle : commonCellStyle);
					cell.setCellValue((String)v);
				}
				i++;
			}
		}
	}
	
	public static XSSFFont cloneFont(XSSFWorkbook workBook, XSSFFont source) {
        XSSFFont newFont = workBook.createFont();
        newFont.setFontName(source.getFontName());
        newFont.setFontHeightInPoints(source.getFontHeightInPoints());
        newFont.setColor(source.getColor());
        newFont.setBold(source.getBold());
        newFont.setItalic(source.getItalic());
        newFont.setStrikeout(source.getStrikeout());
        newFont.setUnderline(source.getUnderline());
        return newFont;
    }
	
	public static XSSFCellStyle cloneCellStyle(XSSFWorkbook workBook, XSSFCell cell) {
		XSSFCellStyle newStyle = workBook.createCellStyle();
		newStyle.cloneStyleFrom(cell.getCellStyle());
		cell.setCellStyle(newStyle);
		return newStyle;
	}

	public static XSSFFont cloneCellFont(XSSFWorkbook workBook, XSSFCell cell) {
		return cloneFont(workBook, cell.getCellStyle().getFont());
	}
}
