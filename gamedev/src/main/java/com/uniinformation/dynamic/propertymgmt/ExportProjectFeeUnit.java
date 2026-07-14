package com.uniinformation.dynamic.propertymgmt;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.api.client.util.Objects;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultProjectFee;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;

public class ExportProjectFeeUnit extends BiActionHandler implements JxActionListener {
	private static int maxDocCount = 10000;
	private SessionHelper sh;
	private BiResultProjectFee br;
	private MyWorkbook workbook;

	public ExportProjectFeeUnit(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		useAsync = p_bibase != null ? p_bibase.getSessionHelper().getAllowBatchPrtdocAsync() : false;
	}

	public ExportProjectFeeUnit() {
		this(null);
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		sh = p_result.getSessionHelper();
		if (cnt > maxDocCount)
			return new ReturnMsg(false, sh.getLabel("Cannot Print more than 10000 documents"));
		workbook = new MyWorkbook();
		return ReturnMsg.defaultOk;		
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			if (!p_result.fetchOneRecV(p_recIdx)) 
				return new ReturnMsg(false,sh.getLabel("Fetch Record failed"));
			br = (BiResultProjectFee)p_result;
			workbook.writeWorkSheet();
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

	@Override
	public void actionPerformed(JxField field) {
		try {
			JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
			br = (BiResultProjectFee) jxf.getBr();
			sh = br.getSessionHelper();
			if (StringUtils.isBlank(br.getCellString("col_a"))) {
				ZkUtil.errMsg("必須先建立分攤項目內容才可以下載單位分攤表 !");
				return;
			}
			workbook = new MyWorkbook();
			workbook.writeWorkSheet();
			ReturnMsg rtn = downloadExcel();
			if (!rtn.getStatus())
				throw new Exception(rtn.getMsg());
		} catch (Exception ex) {
			UniLog.log(ex); 
			ZkBiMsgbox.show(ex.toString());
		}
	}

	private ReturnMsg downloadExcel() {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			workbook.write(os);
			ZkUtil.downloadFileByRenameDlg(sh, "維修分攤費", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", os.toByteArray(), null);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(e);
		}
		return ReturnMsg.defaultOk;
	}

	private class MyWorkbook extends XSSFWorkbook {
		private short DEFAULT_FONT_SIZE = 12;
		private XSSFDataFormat cellDataFormat = createDataFormat();

		private XSSFCellStyle commonCellStyle;
		private List<XSSFCellStyle> cellStyleList = new ArrayList<>();

		public MyWorkbook() {
			commonCellStyle = createCellStyle();
	        commonCellStyle.setAlignment(HorizontalAlignment.LEFT);
	        commonCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
	        XSSFFont font = createFont();
	        font.setFontHeightInPoints(DEFAULT_FONT_SIZE);
	        font.setBold(false);
	        commonCellStyle.setDataFormat(cellDataFormat.getFormat("General"));
	        commonCellStyle.setFont(font);
	        cellStyleList.add(commonCellStyle);
		}
		
		private XSSFCellStyle getCellStyle(HorizontalAlignment halign, short fontSize, boolean isBold, String format) {
			return cellStyleList.stream().filter(s -> isStyleMatch(s, halign, fontSize, isBold, format)).findFirst().orElseGet(() -> {
				XSSFCellStyle style = createCellStyle();
				style.cloneStyleFrom(commonCellStyle);
				style.setAlignment(halign);
				XSSFFont font = cloneFont(this, commonCellStyle.getFont());
				font.setFontHeightInPoints(fontSize);
				font.setBold(isBold);
				style.setFont(font);
				style.setDataFormat(cellDataFormat.getFormat(format));
				UniLog.log1("create style:%s,%d,%b,%s", halign, fontSize, isBold, format);
				cellStyleList.add(style);
				return style;
			});
		}

		private XSSFCellStyle getCellStyle(HorizontalAlignment halign, boolean isBold, String format) {
			return getCellStyle(halign, DEFAULT_FONT_SIZE, isBold, format);
		}

		private XSSFCellStyle getCellStyle(HorizontalAlignment halign, boolean isBold) {
			return getCellStyle(halign, DEFAULT_FONT_SIZE, isBold, "General");
		}
		
		private boolean isStyleMatch(XSSFCellStyle style, HorizontalAlignment halign, short fontSize, boolean isBold, String format) {
			if (style.getAlignmentEnum() != halign)
				return false;
			XSSFFont font = style.getFont();
			if (font.getFontHeightInPoints() != fontSize)
				return false;
			if (font.getBold() != isBold)
				return false;
			return Objects.equal(style.getDataFormatString(), format);
		}

		private void setCellValue(XSSFRow sheetRow, boolean isBold, int startCol, Object...values) throws Exception {
			int i = 0;
			for (Object v : values) {
				int col = startCol + i;
				HorizontalAlignment halign = col > 0 ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT;
				XSSFCell cell = sheetRow.createCell(col);
				if (v instanceof Double) {
					cell.setCellStyle(getCellStyle(halign, isBold, "0.00"));
					cell.setCellValue((double)v);
				} else {
					cell.setCellStyle(getCellStyle(halign, isBold));
					cell.setCellValue((String)v);
				}
				i++;
			}
		}

		public void writeWorkSheet() throws Exception {
			BiResult brUnit = br.getSubLink("propertymgmt.ProjectFeeUnit");
			XSSFSheet sheet = createSheet(br.getCellString("col_a"));
			List<String> labelList = Stream.concat(Stream.of("col_b", "col_c"), br.getPeriodColumnLabelStream()).collect(Collectors.toList());
			setCellValue(sheet.createRow(0), true, 0, labelList.stream()
					.map(k -> brUnit.getColumnByLabel(k))
					.map(bl -> TranslateUtil.getText(sh, bl.getCellFullName(), "LABEL", sh.getLabel(bl)))
					.toArray());
			int row = 0;
			for (BiCellCollection bcc : brUnit.getRowCollectionList())
				setCellValue(sheet.createRow(++row), false, 0, labelList.stream().map(k -> bcc.getCell(k).getObject()).toArray());
			sheet.autoSizeColumn(0);
			sheet.createFreezePane(0, 1);
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

}
