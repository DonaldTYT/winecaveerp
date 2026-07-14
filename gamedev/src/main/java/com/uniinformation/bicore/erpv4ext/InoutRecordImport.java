package com.uniinformation.bicore.erpv4ext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kyoko.common.DateUtil;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.JdbcPool;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.BiUtil.CheckedConsumer2;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.WebCoreUtil;
import static com.uniinformation.utils.ZkUtil.throwConsumer;

public class InoutRecordImport {
	private static final SimpleDateFormat tdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private static final SimpleDateFormat ddf1 = new SimpleDateFormat("yyyy-MM-dd");

	protected String filePath;
	protected XSSFWorkbook workbook;
	protected Map<String, User> userMap = new LinkedHashMap<>();
	protected Map<String, Set<Date>> inoutMap = new LinkedHashMap<>();
	protected Map<Integer, Date> dateMap = new TreeMap<>();
	protected String atype;
	
	public InoutRecordImport(String filePath, String atype) {
		this.filePath = filePath;
		this.atype = atype;
	}

	public Map<String, List<String>> findDuplicateNamesWithIds() {
        return userMap.values().stream()
            .collect(Collectors.groupingBy(u -> u.name))
            .entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(u -> u.userId).collect(Collectors.toList())
            ));
    }

	public Date getMinInoutDate() {
        return inoutMap.values().stream()
            .flatMap(Set::stream)
            .min(Date::compareTo)
            .orElse(null);
    }

	public Date getMaxInoutDate() {
        return inoutMap.values().stream()
            .flatMap(Set::stream)
            .max(Date::compareTo)
            .orElse(null);
    }
	
	public void writeNeedFixSheet() throws Exception {
		UniLog.log1("writeNeedFixSheet %s", filePath);
		XSSFSheet sheet = getOrCreateSheet(workbook, "Need Fix");
		AtomicInteger rowNum = new AtomicInteger();
		if (userMap.values().stream().anyMatch(u -> u.employeeId == null)) {
			XSSFRow row = sheet.createRow(rowNum.getAndIncrement());
			row.createCell(0).setCellValue("不存在於Employee Record:");
			userMap.values().stream().filter(u -> u.employeeId == null).forEach(u -> {
				XSSFRow row1 = sheet.createRow(rowNum.getAndIncrement());
				row1.createCell(0).setCellValue(u.userId);
				row1.createCell(1).setCellValue(u.name);
				row1.createCell(2).setCellValue(u.department);
				row1.createCell(3).setCellValue(inoutMap.containsKey(u.userId) ? "有記錄" : "");
			});
		}
		Map<String, List<String>> map = findDuplicateNamesWithIds();
		if (!map.isEmpty()) {
			rowNum.incrementAndGet();
			XSSFRow row = sheet.createRow(rowNum.getAndIncrement());
			row.createCell(0).setCellValue("Name重复的User ID:");
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				for (String id : entry.getValue()) {
					row = sheet.createRow(rowNum.getAndIncrement());
					row.createCell(0).setCellValue(id);
					row.createCell(1).setCellValue(entry.getKey());
					row.createCell(2).setCellValue(userMap.get(id).department);
					row.createCell(3).setCellValue(inoutMap.containsKey(id) ? "有記錄" : "");
				}
			}
		}
		try (FileOutputStream fos = new FileOutputStream(filePath)) {
			workbook.write(fos);
		}
	}
	
	private static void addInoutRecords(JdbcPool jdbcPool, User[] users) throws Exception {
		ZkUtil.importActionByJdbcPool.accept(jdbcPool, su -> {
			for (User u : users) {
				Optional.ofNullable(u.parent.inoutMap.get(u.userId)).ifPresent(v -> v.forEach(throwConsumer(date -> {
					ZkUtil.executeInsertIntoSql(su, "attenddet", Arrays.asList("atd_eid", "atd_time", "atd_atime", "atd_atype", "atd_adate"), 
							new Wherecl().appendArgument(u.employeeId)
										.appendArgument(-28800)
										.appendArgument((int)(date.getTime() / 1000))
										.appendArgument(u.parent.atype)
										.appendArgument(DateUtil.dayBeginning(date)));
				})));
			}
		});
	}

	private static class User {
		String userId, name, department, employeeId;
		InoutRecordImport parent;

		@Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        User user = (User) o;
	        return Objects.equals(userId, user.userId) &&
	               Objects.equals(name, user.name) &&
	               Objects.equals(department, user.department) &&
	               Objects.equals(employeeId, user.employeeId);
	    }
	}
	
	private static class InoutRecordImport1 extends InoutRecordImport {
		public InoutRecordImport1(String filePath, int sheetNum, int startRowNum, Date startDate, Date endDate, String atype) {
			super(filePath, atype);
			try {
				AtomicReference<User> lastUser = new AtomicReference<>();
				workbook = quickReadExcelByRow(filePath, sheetNum, startRowNum, Integer.MAX_VALUE, (rowNum, row) -> {
					if (rowNum == startRowNum) {
						for (int j = 3; j <= row.getLastCellNum(); j++) {
							if (row.getCell(j) == null)
								break;
							String s = getCellString(row, j);
							Date date = DateUtil.nextday(startDate, NumberUtils.toInt(s) - 1);
							if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0)
								throw new Exception(String.format("Invalid date:%s", s));
							dateMap.put(j, date);
						}
					} else if (rowNum >= startRowNum + 2) {
						String uid = getCellString(row, 0);
						User user = null;
						if (StringUtils.isNotBlank(uid)) {
							user = new User();
							user.parent = this;
							user.userId = uid;
							user.name = getCellString(row, 1);
							user.department = getCellString(row, 2);
							userMap.put(user.userId, user);
							lastUser.set(user);
						} else
							user = lastUser.get();
						if (user == null)
							throw new Exception(String.format("User is null at row %d", rowNum));
						for (Map.Entry<Integer, Date> entry : dateMap.entrySet()) {
							int colNum = entry.getKey();
							Date date = entry.getValue();
							String s = getCellString(row, colNum);
							if (StringUtils.isNotBlank(s)) {
								Date date1 = parseTimeWithBaseDate(s, date, "h:mm a");
								Set<Date> list = inoutMap.get(user.userId);
								if (list == null)
									inoutMap.put(user.userId, list = new TreeSet<>());
								list.add(date1);
							}
						}
					}
				});
			} catch (Exception e) {
				UniLog.log(e);
			}
		}
	}

	private static class InoutRecordImport2 extends InoutRecordImport {
		public InoutRecordImport2(String filePath, int sheetNum, int startRowNum, Date startDate, Date endDate, String atype) {
			super(filePath, atype);
			try {
				AtomicReference<User> lastUser = new AtomicReference<>();
				workbook = quickReadExcelByColumn(filePath, sheetNum, startRowNum, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, (colNum, cells) -> {
					if (cells.length == 0 || cells[0] == null)
						return;
					if (colNum == 0) {
						for (int rowNum = 3; rowNum < cells.length; rowNum++) {
							String s = getCellString(cells[rowNum]);
							if (StringUtils.isBlank(s))
								break;
							Date date = DateUtil.nextday(startDate, NumberUtils.toInt(s) - 1);
							if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0)
								throw new Exception(String.format("Invalid date:%s", s));
							dateMap.put(rowNum, date);
						}
					} else if (colNum >= 2) {
						String uid = getCellString(cells[0]);
						User user = null;
						if (StringUtils.isNotBlank(uid)) {
							user = new User();
							user.parent = this;
							user.userId = uid;
							user.name = getCellString(cells[1]);
							user.department = getCellString(cells[2]);
							userMap.put(user.userId, user);
							lastUser.set(user);
						} else
							user = lastUser.get();
						if (user == null)
							throw new Exception(String.format("User is null at column %d", colNum));
						for (Map.Entry<Integer, Date> entry : dateMap.entrySet()) {
							int rowNum = entry.getKey();
							Date date = entry.getValue();
							String s = getCellString(cells[rowNum]);
							if (StringUtils.isNotBlank(s)) {
								Date date1 = parseTimeWithBaseDate(s, date, "h:mm a");
								Set<Date> list = inoutMap.get(user.userId);
								if (list == null)
									inoutMap.put(user.userId, list = new TreeSet<>());
								list.add(date1);
							}
						}
					}
				});
			} catch (Exception e) {
				UniLog.log(e);
			}
		}
	}

	private static class InoutRecordImport3 extends InoutRecordImport {
		public InoutRecordImport3(String filePath, int sheetNum, int startRowNum, String atype) {
			super(filePath, atype);
			try {
				AtomicReference<User> lastUser = new AtomicReference<>();
				workbook = quickReadExcelByRow(filePath, sheetNum, startRowNum, Integer.MAX_VALUE, (rowNum, row) -> {
					String uid = getCellString(row, 1);
					User user = null;
					if (StringUtils.isNotBlank(uid)) {
						user = new User();
						user.parent = this;
						user.userId = uid;
						user.name = getCellString(row, 2);
						user.department = getCellString(row, 0);
						userMap.put(user.userId, user);
						lastUser.set(user);
					} else
						user = lastUser.get();
					if (user == null)
						throw new Exception(String.format("User is null at row %d", rowNum));
					Date date = ddf1.parse(getCellString(row, 3).substring(0, 10));
					addInoutData(user, date, getCellString(row, 4));
					addInoutData(user, date, getCellString(row, 6));
				});
			} catch (Exception e) {
				UniLog.log(e);
			}
		}

		private void addInoutData(User user, Date date, String s) throws Exception {
			if (StringUtils.isNotBlank(s)) {
				Date date1 = parseTimeWithBaseDate(s, date, "HH:mm:ss");
				Set<Date> list = inoutMap.get(user.userId);
				if (list == null)
					inoutMap.put(user.userId, list = new TreeSet<>());
				list.add(date1);
			}
		}
	}

	private static XSSFWorkbook quickReadExcelByRow(String filePath, int sheetNum, int startRowNum, int endRowNum, CheckedConsumer2<Integer, XSSFRow> cb) throws Exception {
		try (FileInputStream fis = new FileInputStream(filePath)) {
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(sheetNum);
			for (int i = startRowNum; i <= Math.min(endRowNum, sheet.getLastRowNum()); i++)
				cb.accept(i, sheet.getRow(i));
			return workbook;
		}
	}

	private static XSSFWorkbook quickReadExcelByColumn(String filePath, int sheetNum, int startRowNum, int endRowNum, int startColNum, int endColNum, CheckedConsumer2<Integer, XSSFCell[]> cb) throws Exception {
		try (FileInputStream fis = new FileInputStream(filePath)) {
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(sheetNum);
			endRowNum = Math.min(endRowNum, sheet.getLastRowNum());
			endColNum = Math.min(endColNum, (int)IntStream.rangeClosed(startRowNum, endRowNum).mapToLong(i -> sheet.getRow(i).getLastCellNum()).max().orElse(-1));
			for (int colNum = startColNum; colNum <= endColNum; colNum++) {
				int cn = colNum;
				cb.accept(colNum, IntStream.rangeClosed(startRowNum, endRowNum).mapToObj(rowNum -> {
					XSSFRow row = sheet.getRow(rowNum);
					return cn <= row.getLastCellNum() ? row.getCell(cn) : null;
				}).toArray(XSSFCell[]::new));
			}
			return workbook;
		}
	}
	
	private static String getCellString(XSSFRow row, int cellNum) {
		return row != null ? getCellString(row.getCell(cellNum)) : null;
	}
	
	private static String getCellString(XSSFCell cell) {
		return getCellString(cell, cell != null ? cell.getCellType() : -1);
	}

	private static String getCellString(XSSFCell cell, int type) {
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

	private static Double getCellDouble(XSSFRow row, int cellNum) {
		return row != null ? getCellDouble(row.getCell(cellNum)) : null;
	}

	private static Double getCellDouble(XSSFCell cell) {
		return getCellDouble(cell, cell != null ? cell.getCellType() : -1);
	}

	private static Double getCellDouble(XSSFCell cell, int type) {
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

	private static Date getCellDate(XSSFRow row, int cellNum, SimpleDateFormat fmt) throws Exception {
		return row != null ? getCellDate(row.getCell(cellNum), fmt) : null;
	}

	private static Date getCellDate(XSSFCell cell, SimpleDateFormat fmt) throws Exception {
		return getCellDate(cell, cell != null ? cell.getCellType() : -1, fmt);
	}

	private static Date getCellDate(XSSFCell cell, int type, SimpleDateFormat fmt) throws Exception {
		if (cell == null)
			return null;
		switch (type) {
			case XSSFCell.CELL_TYPE_NUMERIC:
				return cell.getDateCellValue();
			case XSSFCell.CELL_TYPE_STRING:
				return fmt.parse(cell.getStringCellValue());
			case XSSFCell.CELL_TYPE_FORMULA: 
				return cell.getCachedFormulaResultType() != XSSFCell.CELL_TYPE_FORMULA ? getCellDate(cell, cell.getCachedFormulaResultType(), fmt) : null;
			default:
				return null;
		}
	}
	
	private static XSSFSheet getOrCreateSheet(XSSFWorkbook workbook, String sheetName) {
		XSSFSheet sheet = workbook.getSheet(sheetName);
		if (sheet == null)
			sheet = workbook.createSheet(sheetName);
		else
			IntStream.rangeClosed(0, sheet.getLastRowNum())
        			.mapToObj(sheet::getRow)
        			.filter(Objects::nonNull)
        			.forEach(sheet::removeRow);
		return sheet;
	}
	
	private static Date parseTimeWithBaseDate(String timeStr, Date baseDate, String format) throws Exception {
		LocalDate baseLocalDate = baseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.US);
        LocalTime localTime = LocalTime.parse(timeStr.trim().replace("\\s+", " "), formatter);
        LocalDateTime dateTime = LocalDateTime.of(baseLocalDate, localTime);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
	
	
	public static void main(String args[]) throws Exception{
      	//IniHelper ini = SessionHelper.getIniHelper("erpv4artwaylive");
      	IniHelper ini = SessionHelper.getIniHelper("erpv4artway");
		JdbcPool jdbcPool = WebCoreUtil.getJdbcPoolByConnectionString("inoutRecordImport", 2, ini.getString("databaseString"), null, null);

		InoutRecordImport1 import1 = new InoutRecordImport1("/tmp/OT Sep 2025.xlsx", 0, 2, DateUtil.dateTimeStrToDate("2025/09/01"), DateUtil.dateTimeStrToDate("2025/09/30"), "01");
		UniLog.log1("import1 userMap size:%d, has duplicate:%s", import1.userMap.size(), import1.findDuplicateNamesWithIds());
		/*import1.userMap.entrySet().forEach(entry -> {
			User user = entry.getValue();
			UniLog.log1("%s,%s,%s", user.userId, user.name, user.department);
		});
		import1.dateMap.entrySet().forEach(entry -> {
			UniLog.log1("%s:%s", entry.getKey(), ddf.format(entry.getValue()));
		});
		import1.inoutMap.entrySet().forEach(entry -> {
			UniLog.log1("%s:%s", entry.getKey(), entry.getValue().stream().map(tdf::format).collect(Collectors.toList()));
		});*/

		/*InoutRecordImport2 import2 = new InoutRecordImport2("/tmp/OT Oct 2025 (1).xlsx", 1, 1, DateUtil.dateTimeStrToDate("2025/10/01"), DateUtil.dateTimeStrToDate("2025/10/31"));
		UniLog.log1("import2 userMap size:%d, has duplicate:%s", import2.userMap.size(), import2.findDuplicateNamesWithIds());
		import2.userMap.entrySet().forEach(entry -> {
			User user = entry.getValue();
			UniLog.log1("%s,%s,%s", user.userId, user.name, user.department);
		});
		import2.dateMap.entrySet().forEach(entry -> {
			UniLog.log1("%s:%s", entry.getKey(), ddf.format(entry.getValue()));
		});
		import2.inoutMap.entrySet().forEach(entry -> {
			UniLog.log1("%s:%s", entry.getKey(), entry.getValue().stream().map(tdf::format).collect(Collectors.toList()));
		});
		UniLog.log1("userMap same:%b", Objects.equals(import1.userMap, import2.userMap));
		UniLog.log1("inoutMap same:%b", Objects.equals(import1.inoutMap, import2.inoutMap));*/

		/*InoutRecordImport3 import3 = new InoutRecordImport3("/tmp/Attendance Report-2025-09-29 to 2025-10-30.xlsx", 0, 1, "02");
		UniLog.log1("import3 userMap size:%d, has duplicate:%s", import3.userMap.size(), import3.findDuplicateNamesWithIds());
		UniLog.log1("minTime:%s, maxTime:%s", 
				Optional.ofNullable(import3.getMinInoutDate()).map(tdf::format).orElse(null),
				Optional.ofNullable(import3.getMaxInoutDate()).map(tdf::format).orElse(null));*/
		/*import3.userMap.entrySet().forEach(entry -> {
			User user = entry.getValue();
			UniLog.log1("%s,%s,%s", user.userId, user.name, user.department);
		});
		import3.inoutMap.entrySet().forEach(entry -> {
			UniLog.log1("%s:%s", entry.getKey(), entry.getValue().stream().map(tdf::format).collect(Collectors.toList()));
		});*/
		
		
		TableRec tr = new SelectUtil().init(jdbcPool).getQueryResult("select * from employee");
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			String emid = tr.getFieldString("em_eid");
			String ename = tr.getFieldString("em_ename");
			String chinname = tr.getFieldString("em_chinname");
			import1.userMap.values().stream().filter(u -> StringUtils.equalsAny(u.name, chinname, ename)).forEach(u -> u.employeeId = emid);
			//import3.userMap.values().stream().filter(u -> StringUtils.equalsAny(u.name, chinname, ename)).forEach(u -> u.employeeId = emid);
		}
		//import1.writeNeedFixSheet();
		//import3.writeNeedFixSheet();
		User[] users = //Stream.concat(import1.userMap.values().stream(), import3.userMap.values().stream())
						import1.userMap.values().stream()
							.filter(u -> u.employeeId != null)
							.sorted(Comparator.comparing(u -> u.employeeId))
							.toArray(User[]::new);
		for (User u : users) {
			Set<Date> list = u.parent.inoutMap.get(u.userId);
			UniLog.log1("%s,%s,%s,%s,%b", u.userId, u.name, u.department, u.employeeId, list != null);
			if (list != null)
				UniLog.log1("%s,%s,%s", u.employeeId, u.parent.atype, list.stream().map(tdf::format).collect(Collectors.toList()));
		}

		for (InoutRecordImport ip : new InoutRecordImport[] {import1/*, import3*/}) {
			User[] users1 = ip.userMap.values().stream()
								.filter(u -> u.employeeId != null)
								.sorted(Comparator.comparing(u -> u.employeeId)).toArray(User[]::new);
			addInoutRecords(jdbcPool, users1);
		}
	}
}
