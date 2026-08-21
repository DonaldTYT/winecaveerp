package com.uniinformation.dynamic.winecave;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Downloads the PERF-compatible simple Excel packing list for a Delivery Note. */
public class DownloadDeliveryPackingListSimpleExcel extends BiActionHandler
		implements JxActionListener {
	private static final String TEMPLATE = "/template/poi_template16.xls";
	private static final String MIME_TYPE = "application/vnd.ms-excel";
	private static final int FIRST_DETAIL_ROW = 5;
	private static final String DEFAULT_OWNER = "WINECAVE";

	private static class OrderInfo {
		String po = "";
		String hwb = "";
		String owner = "";
	}

	public DownloadDeliveryPackingListSimpleExcel() {
		super(null);
	}

	public DownloadDeliveryPackingListSimpleExcel(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	@Override
	public void actionPerformed(JxField field) {
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult delivery = jxf.getBr();
		if(jxf.isDirty()) {
			Messagebox.show("Please save or cancel the current changes before exporting.");
			return;
		}
		if(delivery.getCellInt("stm_mrg") <= 0) {
			Messagebox.show("Please save the Delivery Note before exporting.");
			return;
		}

		try {
			byte[] workbook = createWorkbook(delivery);
			String ref = safeFilePart(delivery.getCellString("stm_ref1"));
			Filedownload.save(workbook,MIME_TYPE,ref + "_PackingList.xls");
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show("Unable to create the packing-list Excel file: " + ex.getMessage());
		}
	}

	private byte[] createWorkbook(BiResult delivery) throws Exception {
		ExcelPoi excel;
		try(InputStream input = delivery.getSessionHelper().openResourceAsStream(TEMPLATE)) {
			if(input == null) throw new Exception("Template not found: " + TEMPLATE);
			excel = ExcelPoi.newExcelPoi(input,false);
		}

		excel.excel_setStringValue(0,1,delivery.getCellString("stm_ref1"));
		excel.excel_setStringValue(1,1,new SimpleDateFormat("yyyy-MM-dd")
				.format(delivery.getCellDate("stm_date")));
		excel.excel_setStringValue(2,1,delivery.getCellString("vd_vname"));

		SelectUtil select = delivery.getSelectUtil();
		TableRec details = select.getQueryResult(
				"select stmd_irg,sum(-(stmd_entryqty)) entryqty,stmd_uprice,"
				+ "st_icode,st_iname,stmd_entryunit,stmd_cur,stmd_org,stmd_ref3,stmd_qorg "
				+ "from stmovd,stock where stmd_mrg = ? and st_irg = stmd_irg "
				+ "and stmd_tdtype = 'SO' "
				+ "group by stmd_irg,stmd_entryunit,stmd_cur,stmd_uprice,st_icode,"
				+ "st_iname,stmd_org,stmd_ref3,stmd_qorg "
				+ "order by stmd_irg,stmd_entryunit,stmd_cur,stmd_uprice,stmd_org,stmd_ref3",
				new Wherecl().appendArgument(delivery.getCellInt("stm_mrg")));

		for(int index = 0;index < details.getRecordCount();index++) {
			details.setRecPointer(index);
			int row = FIRST_DETAIL_ROW + index;
			int org = details.getFieldInt("stmd_org");
			int irg = details.getFieldInt("stmd_irg");
			int qorg = details.getFieldInt("stmd_qorg");
			OrderInfo order = getOrderInfo(select,org);
			String owner = getOwnerDescription(select,order.owner,irg,org,qorg);

			excel.excel_setStringValue(row,0,details.getFieldString("st_icode"));
			excel.excel_setStringValue(row,1,details.getFieldString("st_iname"));
			excel.excel_setNumericValue(row,2,details.getFieldDouble("entryqty"));
			excel.excel_setStringValue(row,3,details.getFieldString("stmd_entryunit"));
			excel.excel_setStringValue(row,4,order.po);
			excel.excel_setStringValue(row,5,order.hwb);
			excel.excel_setStringValue(row,6,details.getFieldString("stmd_ref3"));
			excel.excel_setStringValue(row,7,owner);
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if(!excel.writeWorkBook(output)) throw new Exception("Unable to write workbook");
		return output.toByteArray();
	}

	private OrderInfo getOrderInfo(SelectUtil select,int org) throws Exception {
		OrderInfo info = new OrderInfo();
		TableRec record = select.getQueryResult(
				"select or_ocode,or_cocode,stm_ref1 from orders,stmov "
				+ "where or_org = ? and stm_mrg = or_stmrg",
				new Wherecl().appendArgument(org));
		if(record.getRecordCount() > 0) {
			record.setRecPointer(0);
			info.po = record.getFieldString("or_ocode");
			info.owner = record.getFieldString("or_cocode");
			info.hwb = record.getFieldString("stm_ref1");
		}
		return info;
	}

	private String getOwnerDescription(SelectUtil select,String orderOwner,int irg,int org,
			int qorg) throws Exception {
		if(qorg > 0) {
			TableRec owner = select.getQueryResult(
					"select or_cocode from stmovd stmd1,stmovd stmd2,orders "
					+ "where stmd1.stmd_irg = ? and stmd1.stmd_org = ? "
					+ "and stmd1.stmd_tdtype = 'SI' "
					+ "and stmd2.stmd_mrg = stmd1.stmd_mrg "
					+ "and stmd2.stmd_tdindex = stmd1.stmd_tdindex "
					+ "and or_org = stmd2.stmd_org",
					new Wherecl().appendArgument(irg).appendArgument(org));
			String relatedOwner = firstString(owner,"or_cocode");
			return orderOwner + " (" + relatedOwner + ")";
		}

		Set<String> owners = new LinkedHashSet<String>();
		TableRec movements = select.getQueryResult(
				"select stmd_mrg,stmd_tdindex,stmd_tdtype,stm_type,stmd_bin "
				+ "from stmovd,stmov where stmd_irg = ? and stmd_org = ? "
				+ "and stmd_tdtype in ('MI','JA','BI') and stm_mrg = stmd_mrg",
				new Wherecl().appendArgument(irg).appendArgument(org));
		for(int i = 0;i < movements.getRecordCount();i++) {
			movements.setRecPointer(i);
			String type = movements.getFieldString("stmd_tdtype");
			String owner = "";
			String bin = movements.getFieldString("stmd_bin");
			if("MI".equals(type)) {
				owner = getMovementOwner(select,movements.getFieldInt("stmd_mrg"),
						movements.getFieldInt("stmd_tdindex"),"MI",false);
			} else if("JA".equals(type)) {
				if("A2".equals(movements.getFieldString("stm_type"))) {
					TableRec paired = getMovementOwnerRecord(select,
							movements.getFieldInt("stmd_mrg"),
							movements.getFieldInt("stmd_tdindex"),"JB",true);
					owner = firstString(paired,"or_cocode");
					if(paired.getRecordCount() > 0) bin = firstString(paired,"stmd_bin");
				} else {
					owner = "Adjustment";
				}
			} else if("BI".equals(type)) {
				owner = DEFAULT_OWNER;
			}
			if(!owner.isEmpty()) owners.add(owner + ":" + bin);
		}
		return orderOwner + " (" + String.join(",",owners) + ")";
	}

	private String getMovementOwner(SelectUtil select,int mrg,int index,String type,
			boolean includeBin) throws Exception {
		return firstString(getMovementOwnerRecord(select,mrg,index,type,includeBin),"or_cocode");
	}

	private TableRec getMovementOwnerRecord(SelectUtil select,int mrg,int index,String type,
			boolean includeBin) throws Exception {
		return select.getQueryResult("select or_cocode"
				+ (includeBin ? ",stmd_bin " : " ")
				+ "from stmovd,orders where stmd_mrg = ? and stmd_tdindex = ? "
				+ "and stmd_tdtype = ? and or_org = stmd_org",
				new Wherecl().appendArgument(mrg).appendArgument(index).appendArgument(type));
	}

	private String firstString(TableRec records,String field) throws Exception {
		if(records == null || records.getRecordCount() <= 0) return "";
		records.setRecPointer(0);
		String value = records.getFieldString(field);
		return value == null ? "" : value.trim();
	}

	private String safeFilePart(String value) {
		if(value == null || value.trim().isEmpty()) return "DeliveryNote";
		return value.trim().replaceAll("[^A-Za-z0-9_-]","_");
	}

	@Override public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		return ReturnMsg.defaultOk;
	}
	@Override public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
		return ReturnMsg.defaultOk;
	}
	@Override public ReturnMsg afterAction(BiResult p_result) {
		return ReturnMsg.defaultOk;
	}
	@Override public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		return p_br != null && !p_isBatch;
	}
	@Override public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null || p_isBatch || p_br.inBeginWork()) return true;
		return !p_br.getSessionHelper().hasAccessRight("#Prtvoucher");
	}
}
