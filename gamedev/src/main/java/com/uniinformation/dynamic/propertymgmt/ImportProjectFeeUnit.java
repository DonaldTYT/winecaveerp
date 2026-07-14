package com.uniinformation.dynamic.propertymgmt;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.util.media.AMedia;
import org.zkoss.zul.Fileupload;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultProjectFee;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.BiUtil.CheckedPredicate2;
import com.uniinformation.webcore.SessionHelper;

import static com.uniinformation.utils.ZkUtil.throwConsumer;

public class ImportProjectFeeUnit implements JxActionListener {
	private SessionHelper sh;
	private BiResultProjectFee br;

	@Override
	public void actionPerformed(JxField field) {
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		sh = jxf.getSessionHelper();
		br = (BiResultProjectFee) jxf.getBr();
		Fileupload.get(new HashMap<>(), null, "導入分攤數據", 1, -1, true, event -> {
			AMedia amedia = (AMedia) event.getMedia();
       		if (amedia != null && Objects.equals(amedia.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
       			try {
       				if (readExcel(amedia.getByteData())) {
       					br.setupStatisticsField();
       					jxf.setDirtyFlag(true);
       				}
       				ZkUtil.showMsg("導入完成");
       			} catch (Exception e) {
       				UniLog.log(e);
       				ZkUtil.errMsg(e.getMessage());
       			}
       		} else
       			ZkUtil.showErrMsg(sh.getLabel("Invalid import file"));
		});
	}
	
	private boolean readExcel(byte[] data) throws Exception {
		AtomicBoolean isDirty = new AtomicBoolean();
		Set<BiCellCollection> dirtyList = new HashSet<>();
		BiResult brUnit = br.getSubLink("propertymgmt.ProjectFeeUnit");
		Vector<BiCellCollection> unitList = brUnit.getRowCollectionList();
		List<String> labelList = br.getPeriodColumnLabelStream().collect(Collectors.toList());
		labelList.addAll(0, Arrays.asList("col_b", "col_c"));
		Map<String, Integer> colMap = new LinkedHashMap<>();
		Map<String, Object> valueMap = new LinkedHashMap<>();
		quickReadExcelByRow(sh, data, br.getCellString("col_a"), 0, Integer.MAX_VALUE, (row, sheet) -> {
			if (row.getRowNum() == 0) {
				IntStream.range(0, labelList.size()).forEach(i -> {
					String s = getCellString(row, i);
					labelList.stream().filter(k -> {
							BiColumn bl = brUnit.getColumnByLabel(k);
							String header = TranslateUtil.getText(sh, bl.getCellFullName(), "LABEL", sh.getLabel(bl));
							return Objects.equals(header, s);
						}).findFirst().ifPresent(label -> colMap.put(label, i));
				});
				if (colMap.size() != labelList.size())
					throw new Exception(sh.getLabel("Invalid import file"));
				return true;
			}
			colMap.entrySet().forEach(entry -> {
				String label = entry.getKey();
				int colNum = entry.getValue();
				valueMap.put(label, !label.equals("col_b") ? getCellDouble(row, colNum) : getCellString(row, colNum));
			});
			if (StringUtils.isBlank((String)valueMap.get("col_b")))
				return false;
			if (valueMap.entrySet().stream().anyMatch(e -> e.getValue() == null))
				throw new Exception(sh.getLabel("Invalid import file"));
			//UniLog.log1("readRow:%s", valueMap.values().stream().map(v -> String.valueOf(v)).collect(Collectors.joining(",")));
			unitList.stream().filter(c -> Objects.equals(c.getCellString("col_b"), valueMap.get("col_b"))).findFirst().ifPresent(c -> {
				valueMap.entrySet().stream().filter(e -> !e.getKey().equals("col_b")).forEach(throwConsumer(e -> {
					String label = e.getKey();
					double newD = (double)e.getValue();
					double oldD = c.getCellDouble(label);
					if (newD != oldD) {
						c.getCell(label).set(newD);
						isDirty.set(true);
					}
					dirtyList.add(c);
				}));
			});
			return true;
		});
		unitList.stream().filter(c -> !dirtyList.contains(c)).forEach(bcc -> {
			labelList.stream().filter(k -> !k.equals("col_b")).forEach(throwConsumer(label -> {
				bcc.getCell(label).set(0.0);
			}));
		});
		return isDirty.get();
	}

	public static XSSFWorkbook quickReadExcelByRow(SessionHelper sh, byte[] data, Object sheetNameOrIndex, int startRowNum, int endRowNum, CheckedPredicate2<XSSFRow, XSSFSheet> cb) throws Exception {
		try (ByteArrayInputStream fis = new ByteArrayInputStream(data)) {
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			String[] sheetNames;
			if (sheetNameOrIndex == null)
				sheetNames = IntStream.range(0, workbook.getNumberOfSheets()).mapToObj(workbook::getSheetName).toArray(String[]::new);
			else if (sheetNameOrIndex instanceof Integer)
				sheetNames = new String[] { workbook.getSheetName((int)sheetNameOrIndex) };
			else
				sheetNames = new String[] { (String)sheetNameOrIndex};
			for (String name : sheetNames) {
				XSSFSheet sheet = workbook.getSheet(name);
				if (sheet == null)
					throw new Exception(sh.getLabel("Invalid import file"));
				for (int i = startRowNum; i <= Math.min(endRowNum, sheet.getLastRowNum()); i++) {
					if (!cb.test(sheet.getRow(i), sheet))
						break;
				}
			}
			return workbook;
		}
	}

	public static String getCellString(XSSFRow row, int cellNum) {
		return row != null ? getCellString(row.getCell(cellNum)) : null;
	}
	
	public static String getCellString(XSSFCell cell) {
		return getCellString(cell, cell != null ? cell.getCellType() : -1);
	}

	public static String getCellString(XSSFCell cell, int type) {
		if (cell == null)
			return null;
		switch (type) {
			case XSSFCell.CELL_TYPE_BOOLEAN: 
				return cell.getBooleanCellValue() ? "Y" : "N";
			case XSSFCell.CELL_TYPE_NUMERIC:
				return new DataFormatter().formatCellValue(cell).trim();
			case XSSFCell.CELL_TYPE_STRING: 
				return cell.getStringCellValue().trim();
			case XSSFCell.CELL_TYPE_BLANK:
				return "";
			case XSSFCell.CELL_TYPE_ERROR:
				return FormulaError.forInt(cell.getErrorCellValue()).getString();
			case XSSFCell.CELL_TYPE_FORMULA: 
				return cell.getCachedFormulaResultType() != XSSFCell.CELL_TYPE_FORMULA ? getCellString(cell, cell.getCachedFormulaResultType()) : null;
			default:
				return null;
		}
	}

	public static Double getCellDouble(XSSFRow row, int cellNum) {
		return row != null ? getCellDouble(row.getCell(cellNum)) : null;
	}

	public static Double getCellDouble(XSSFCell cell) {
		return getCellDouble(cell, cell != null ? cell.getCellType() : -1);
	}

	public static Double getCellDouble(XSSFCell cell, int type) {
		if (cell == null)
			return null;
		switch (type) {
			case XSSFCell.CELL_TYPE_BOOLEAN: 
				return cell.getBooleanCellValue() ? 1.0 : 0.0;
			case XSSFCell.CELL_TYPE_NUMERIC:
				return cell.getNumericCellValue();
			case XSSFCell.CELL_TYPE_STRING: 
				return Double.parseDouble(cell.getStringCellValue());
			case XSSFCell.CELL_TYPE_FORMULA: 
				return cell.getCachedFormulaResultType() != XSSFCell.CELL_TYPE_FORMULA ? getCellDouble(cell, cell.getCachedFormulaResultType()) : null;
			default:
				return null;
		}
	}

	public static Date getCellDate(XSSFRow row, int cellNum, String p_datefmt) {
		return row != null ? getCellDate(row.getCell(cellNum), p_datefmt) : null;
	}

	public static Date getCellDate(XSSFCell cell, String p_datefmt) {
		return getCellDate(cell, cell != null ? cell.getCellType() : -1, p_datefmt);
	}

	public static Date getCellDate(XSSFCell cell, int type, String p_datefmt) {
		if (cell == null)
			return null;
		switch (type) {
			case XSSFCell.CELL_TYPE_BOOLEAN: 
				return null;
			case XSSFCell.CELL_TYPE_STRING: 
				if (p_datefmt == null) 
					return null;
				String ds = cell.getStringCellValue();
				return DateUtil.getDate(ds, p_datefmt);
			case XSSFCell.CELL_TYPE_NUMERIC:
				return cell.getDateCellValue();
			case XSSFCell.CELL_TYPE_FORMULA: 
				return cell.getCachedFormulaResultType() != XSSFCell.CELL_TYPE_FORMULA ? getCellDate(cell, cell.getCachedFormulaResultType(), p_datefmt) : null;
			default:
				return null;
		}
	}
}
