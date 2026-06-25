package com.uniinformation.jxapp.erpv4ext;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class ClLeaveArrange extends JxZkBiBase {

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		try {
			Vector<BiCellCollection> recs = p_br.getSubLinkResult("erpv4ext.ClLeaveArrangeDet");
			for (BiCellCollection cc : recs) {
				if (cc.getBoolean("emlvr_mode")) {
					cc.getCell("emlvr_stdate").setMode(Cell.VMODE_DISPONLY);
					cc.getCell("emlvr_lvdaterange").setMode(Cell.VMODE_DISPONLY);
					cc.getCell("emlvr_remark").setMode(Cell.VMODE_DISPONLY);
					cc.getCell("emlvr_cancelled").setMode(Cell.VMODE_DISPONLY);
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new ClLeaveArrangeDetGetItemProperty(p_br.getSubLink("erpv4ext.ClLeaveArrangeDet"))
		);	
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			Vector<BiCellCollection> recs = br.getSubLinkResult("erpv4ext.ClLeaveArrangeDet");
			for (BiCellCollection cc : recs) {
				Date startDate = cc.getDate("emlvr_stdate");
				Date endDate = cc.getDate("emlvr_enddate");
				double totaldays = cc.getDouble("emlvr_lvdaterange");
				if (DateUtil.isDateNull(startDate))
					return new ReturnMsg(false, "Start Date cannot be empty", true);
				if (DateUtil.isDateNull(endDate))
					return new ReturnMsg(false, "End Date cannot be empty", true);
				if (startDate.compareTo(endDate) >= 0)
					return new ReturnMsg(false, "Start Date must be less than End Date", true);
				double d = totaldays * 10;
				int l = (int)d;
				if (d != l || l % 5 != 0)
					return new ReturnMsg(false, "Invalid total days", true);
				if ((endDate.getTime() - startDate.getTime()) / 86400000 < totaldays)
					return new ReturnMsg(false, "Invalid total days", true);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}

	/* Customized GIPI for ClLeaveArrangeDet */
	private class ClLeaveArrangeDetGetItemProperty extends BiGetItemProperty {

		public ClLeaveArrangeDetGetItemProperty(BiResult p_br) {
			super(p_br);
			UniLog.log1("LeaveApplicationDetGetItemProperty");
		}

		@Override
		public boolean getAllowDelete(Object item) {
			BiCellCollection bcc = getBiResult().getRowCollectionO(item) ;
			UniLog.log1("getAllowDelete:%s, bcc:%s, reason:%s, autogen:%b", item, bcc, bcc.getDate("emlvr_stdate"), bcc.getBoolean("emlvr_mode"));
			return !bcc.getBoolean("emlvr_mode");
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_VALUE_CHANGED) {
				Component comp = ((ZkBiCellValueMapper)bcc.getMapper()).getComponent();
				Date startDate = cl.getDate("emlvr_stdate");
				Date endDate = cl.getDate("emlvr_enddate");
				double totaldays = cl.getDouble("emlvr_lvdaterange");
				UniLog.log1("totaldays:%f", totaldays);

				boolean flag = false;
				if (StringUtils.equals(bcc.getCellLabel(), "emlvr_stdate")) {
					if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && startDate.compareTo(endDate) >= 0) {
						Clients.showNotification("Start Date must be less than End Date", "error", comp, "end_center", 5000, true); 
						flag = true;
					}
				} 
				if (StringUtils.equals(bcc.getCellLabel(), "emlvr_lvdaterange")) {
					try {
						double d = (double)Math.round(totaldays * 10) / 10;
						if (d != totaldays)
							bcc.set(d);
						if ((d * 10) % 5 != 0) {
							Clients.showNotification("Invalid total days", "error", comp, "end_center", 5000, true); 
							flag = true;
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				if (!flag && StringUtils.equalsAny(bcc.getCellLabel(), "emlvr_stdate", "emlvr_lvdaterange")) {
					if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && startDate.compareTo(endDate) < 0 && (endDate.getTime() - startDate.getTime()) / 86400000 < totaldays)
						Clients.showNotification("Invalid total days", "error", comp, "end_center", 5000, true); 
				} 
			}
		}
	}
}
