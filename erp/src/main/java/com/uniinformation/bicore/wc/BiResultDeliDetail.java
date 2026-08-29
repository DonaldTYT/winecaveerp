package com.uniinformation.bicore.wc;

import java.util.HashMap;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4GetItemProperty;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultDeliDetail extends BiResultErpv4 {
	public class DnPalcGetItemProperty extends Erpv4GetItemProperty {
		public DnPalcGetItemProperty(BiResult p_pickBr, ColumnCell p_pickCell) {
			super(BiResultDeliDetail.this, p_pickBr, p_pickCell,
					new BiColumn[] {
							p_pickBr.getView().getColumnByLabel("palc_org"),
							p_pickBr.getView().getColumnByLabel("palc_irg"),
							p_pickBr.getView().getColumnByLabel("palc_qorg")
					},
					p_pickBr.getView().getColumnByLabel("palc_netqty"),
					new BiColumn[] {
							getView().getColumnByLabel("stmd_org"),
							getView().getColumnByLabel("stmd_irg"),
							getView().getColumnByLabel("stmd_qorg")
					},
					getView().getColumnByLabel("stmd_dqty"));
			setItemMode(BiGetItemProperty.GETITEM_MODE_PICK);
		}

		@Override
		protected HashMap<Integer,Double> getCommitedValues() {
			BiResult parent = BiResultDeliDetail.this.getParent();
			if(!(parent instanceof BiResultDelivery)) return(null);
			return(((BiResultDelivery) parent).getPalcCommitedHash());
		}
	}

	public class IcodeGetItemProperty extends Erpv4GetItemProperty {
		public IcodeGetItemProperty(BiResult p_pickBr, ColumnCell p_pickCell) {
			super(BiResultDeliDetail.this, p_pickBr, p_pickCell,
					new BiColumn[] {
							p_pickBr.getView().getColumnByLabel("pdls_org"),
							p_pickBr.getView().getColumnByLabel("pdls_irg"),
							p_pickBr.getView().getColumnByLabel("pdls_loc")
					},
					p_pickBr.getView().getColumnByLabel("pdls_stockqty"),
					new BiColumn[] {
							getView().getColumnByLabel("stmd_org"),
							getView().getColumnByLabel("stmd_irg"),
							getView().getColumnByLabel("stmd_loc")
					},
					getView().getColumnByLabel("stmd_dqty"));
			setItemMode(BiGetItemProperty.GETITEM_MODE_PICK);
		}

		@Override
		protected HashMap<Integer,Double> getCommitedValues() {
			BiResult parent = BiResultDeliDetail.this.getParent();
			if(!(parent instanceof BiResultDelivery)) return(null);
			return(((BiResultDelivery) parent).getIcodeCommitedHash());
		}
	}

	public BiResultDeliDetail(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	public AbstractGetItemProperty pickColumnGetItemProperty(BiResult p_br,
			ColumnCell p_pickColumn) {
		if(p_pickColumn != null && "stmom_ref1".equals(p_pickColumn.getCellLabel())) {
			return(new DnPalcGetItemProperty(p_br, p_pickColumn));
		}
		if(p_pickColumn != null && "st_icode".equals(p_pickColumn.getCellLabel())) {
			return(new IcodeGetItemProperty(p_br, p_pickColumn));
		}
		return(super.pickColumnGetItemProperty(p_br, p_pickColumn));
	}

	@Override
	public String getPickColumnCondition(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("st_icode")) {
			BiCellCollection bc = p_cc.getCollection();
			String icodeCondition = " pdls_loc in ('STOR','WH01') and pdls_stockqty > 0 and or_cocode = '"
					+bc.getCellString("stm_ref2")+"' ";
			BiResult parent = getParent();
			if(!(parent instanceof BiResultDelivery)) return(icodeCondition);

			HashMap<Integer,Double> icodeCommitedHash = ((BiResultDelivery) parent).getIcodeCommitedHash();
			if(icodeCommitedHash == null || icodeCommitedHash.isEmpty()) return(icodeCondition);

			StringBuilder icodeSidList = new StringBuilder();
			for(Integer icodeSid : icodeCommitedHash.keySet()) {
				if(icodeSid == null) continue;
				if(icodeSidList.length() > 0) icodeSidList.append(',');
				icodeSidList.append(icodeSid.intValue());
			}
			if(icodeSidList.length() == 0) return(icodeCondition);
			return("(("+icodeCondition+") or serial_id in("+icodeSidList+") )");
		}
		if(p_cc.getCellLabel().equals("stmom_ref1")) {
			BiCellCollection bc = p_cc.getCollection();
			String condition = " palc_delqty+palc_actdelqty > 0 and stm_ref2 = '"+bc.getCellString("stm_ref2")+"' ";
			BiResult parent = getParent();
			if(!(parent instanceof BiResultDelivery)) return(condition);

			HashMap<Integer,Double> palcCommitedHash = ((BiResultDelivery) parent).getPalcCommitedHash();
			if(palcCommitedHash == null || palcCommitedHash.isEmpty()) return(condition);

			StringBuilder sidList = new StringBuilder();
			for(Integer sid : palcCommitedHash.keySet()) {
				if(sid == null) continue;
				if(sidList.length() > 0) sidList.append(',');
				sidList.append(sid.intValue());
			}
			if(sidList.length() == 0) return(condition);
			return("(("+condition+") or serial_id in("+sidList+") )");
		}
		if(p_cc.getCellLabel().equals("stmd_ref3")) {
			BiCellCollection bc = p_cc.getCollection();
			int irg = bc.getCellInt("stmd_irg");
			int org = bc.getCellInt("stmd_org");
			String loc = bc.getCellString("stmd_loc");
			return("pdlbs_loc = '"+loc+"' and pdlbs_irg = " + irg + " and pdlbs_org = "+org);
		}
		return(null);
	}

	@Override
	public void afterPickColumn(ColumnCell p_pickColumn,
			BiCellCollection p_pickedCollection, String p_pickedColumnName,
			boolean p_update) throws CellException {
		if(p_pickedCollection != null) {
			
		if("st_icode".equals(p_pickColumn.getCellLabel())) {
			Cell pdlsIrg = p_pickedCollection.testCell("pdls_irg");
			if(pdlsIrg == null) {
				throw new CellException(
						"pdls_irg is missing from the selected stock-location row");
			}
			Cell pdlsOrg = p_pickedCollection.testCell("pdls_org");
			if(pdlsOrg == null) {
				throw new CellException(
						"pdls_org is missing from the selected stock-location row");
			}
			Cell pdlsLoc = p_pickedCollection.testCell("pdls_loc");
			if(pdlsLoc == null) {
				throw new CellException(
						"pdls_loc is missing from the selected stock-location row");
			}

			Cell stmdIrg = p_pickColumn.getCollection().testCell("stmd_irg");
			if(stmdIrg == null) {
				throw new CellException(
						"stmd_irg is missing from the DeliveryDetail row");
			}
			Cell stmdOrg = p_pickColumn.getCollection().testCell("stmd_org");
			if(stmdOrg == null) {
				throw new CellException(
						"stmd_org is missing from the DeliveryDetail row");
			}
			Cell stmdLoc = p_pickColumn.getCollection().testCell("stmd_loc");
			if(stmdLoc == null) {
				throw new CellException(
						"stmd_loc is missing from the DeliveryDetail row");
			}
			Cell stmdQorg = p_pickColumn.getCollection().testCell("stmd_qorg");
			if(stmdQorg == null) {
				throw new CellException(
						"stmd_loc is missing from the DeliveryDetail row");
			}
			stmdOrg.set(pdlsOrg.getObject());
			stmdIrg.set(pdlsIrg.getObject());
			stmdLoc.set(pdlsLoc.getObject());
			stmdQorg.set(0);
			return;
		}

		if("stmom_ref1".equals(p_pickColumn.getCellLabel())) {
			Cell palcOrg = p_pickedCollection.testCell("palc_org");
			if(palcOrg == null) {
				throw new CellException(
						"palc_org is missing from the selected PickOsOrder row");
			}
			Cell palcIrg = p_pickedCollection.testCell("palc_irg");
			if(palcIrg == null) {
				throw new CellException(
						"palc_irg is missing from the selected PickOsOrder row");
			}
			Cell palcQorg = p_pickedCollection.testCell("palc_qorg");
			if(palcQorg == null) {
				throw new CellException(
						"palc_qorg is missing from the selected PickOsOrder row");
			}

			Cell stmdOrg = p_pickColumn.getCollection().testCell("stmd_org");
			if(stmdOrg == null) {
				throw new CellException(
						"stmd_org is missing from the DeliveryDetail row");
			}
			Cell stmdIrg = p_pickColumn.getCollection().testCell("stmd_irg");
			if(stmdIrg == null) {
				throw new CellException(
						"stmd_irg is missing from the DeliveryDetail row");
			}
			Cell stmdQorg = p_pickColumn.getCollection().testCell("stmd_qorg");
			if(stmdQorg == null) {
				throw new CellException(
						"stmd_qorg is missing from the DeliveryDetail row");
			}
			Cell stmdLoc = p_pickColumn.getCollection().testCell("stmd_loc");
			if(stmdLoc == null) {
				throw new CellException(
						"stmd_qorg is missing from the Warehouse row");
			}

			// The source-detail lookup uses the composite key (stmd_qorg,
			// stmd_irg). Set stmd_irg first so the stmd_qorg action evaluates
			// the lookup with both key values available.
			stmdOrg.set(palcOrg.getObject());
			stmdIrg.set(palcIrg.getObject());
			stmdQorg.set(palcQorg.getObject());
			stmdLoc.set("SOLD");
			return;
		}
		} else {
			Exception ex = new Exception("pickCollection is null 2");
			UniLog.log(ex);
		}

		super.afterPickColumn(p_pickColumn, p_pickedCollection,
				p_pickedColumnName, p_update);
	}
}
