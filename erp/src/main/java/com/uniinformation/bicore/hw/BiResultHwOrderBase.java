package com.uniinformation.bicore.hw;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValidation;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultHwOrderBase extends BiResultQuotation{
	void realCalUntrimSize(int p_which,Cell p_usize,Cell p_size,Cell p_bleed,Cell p_bleedr) throws CellException
	{
		switch(p_which)  {
		case 0: 
			p_bleed.set(0.0);
			p_bleedr.set(0.0);
			p_size.set(p_usize.getDouble());
			break;
		case 1: 
			if( p_bleed.getDouble() <= 0.0 && p_bleedr.getDouble() <= 0.0) {
				double m = (p_usize.getDouble() - p_size.getDouble())/2.0;
				if(m >= 0.0) {
					p_bleed.set(m);
					p_bleedr.set(m);
				} else {
					p_usize.set(p_size.getDouble());
				}
				break;
			}
		default:
			p_usize.set( p_size.getDouble()+ p_bleed.getDouble() + p_bleedr.getDouble());
			break;
		}
	}
	class BiCellAction_calUntrimSize extends CellValueAction 
	{
		BiCellAction_calUntrimSize() {
		}
		boolean inuse = false;

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(!isActionEnabled()) return;
			if(inuse) return;
			try {
			inuse = true;	
			CellCollection col = ((ColumnCell) p_value).getCollection();
			ColumnCell cc = (ColumnCell) p_value;
			if(cc.getBiColumn().getLabel().equals("ind_usize1")) realCalUntrimSize(0, col.getCell("ind_usize1"), col.getCell("ind_size1"), col.getCell("ind_bleed1"), col.getCell("ind_bleed1r"));
			if(cc.getBiColumn().getLabel().equals("ind_size1")) realCalUntrimSize(1, col.getCell("ind_usize1"), col.getCell("ind_size1"), col.getCell("ind_bleed1"), col.getCell("ind_bleed1r"));
			if(cc.getBiColumn().getLabel().equals("ind_bleed1")) realCalUntrimSize(2, col.getCell("ind_usize1"), col.getCell("ind_size1"), col.getCell("ind_bleed1"), col.getCell("ind_bleed1r"));
			if(cc.getBiColumn().getLabel().equals("ind_bleed1r")) realCalUntrimSize(3, col.getCell("ind_usize1"), col.getCell("ind_size1"), col.getCell("ind_bleed1"), col.getCell("ind_bleed1r"));
			if(cc.getBiColumn().getLabel().equals("ind_usize2")) realCalUntrimSize(0, col.getCell("ind_usize2"), col.getCell("ind_size2"), col.getCell("ind_bleed2"), col.getCell("ind_bleed2r"));
			if(cc.getBiColumn().getLabel().equals("ind_size2")) realCalUntrimSize(1, col.getCell("ind_usize2"), col.getCell("ind_size2"), col.getCell("ind_bleed2"), col.getCell("ind_bleed2r"));
			if(cc.getBiColumn().getLabel().equals("ind_bleed2")) realCalUntrimSize(2, col.getCell("ind_usize2"), col.getCell("ind_size2"), col.getCell("ind_bleed2"), col.getCell("ind_bleed2r"));
			if(cc.getBiColumn().getLabel().equals("ind_bleed2r")) realCalUntrimSize(3, col.getCell("ind_usize2"), col.getCell("ind_size2"), col.getCell("ind_bleed2"), col.getCell("ind_bleed2r"));
			inuse = false;
			} catch (Exception ex) {
				inuse = false;
				if(ex instanceof CellException ) throw ((CellException) ex); else {
					UniLog.log(ex);
					throw new CellException(ex.toString());
				}
			}
//			col.getCell("ind_usize1").set( col.getCell("ind_size1").getDouble() + col.getCell("ind_bleed1").getDouble() );
//			col.getCell("ind_usize2").set( col.getCell("ind_size2").getDouble() + col.getCell("ind_bleed2").getDouble() );
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
		}
		
	}	
	BiCellAction_calUntrimSize caCalUntrimSize =null; 
	protected void setCellActionCalUntrimSize(ColumnCell p_cell) {
		if(caCalUntrimSize == null) caCalUntrimSize = new BiCellAction_calUntrimSize();
		p_cell.addAction(caCalUntrimSize);
		
	}
	protected void triggerCellActionCalUntrimSize(ColumnCell p_cell) throws CellException {
		if(caCalUntrimSize == null) caCalUntrimSize = new BiCellAction_calUntrimSize();
		caCalUntrimSize.cellAction_onchange(p_cell);
	}
	public BiResultHwOrderBase(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("erp.BiResultHwOrderBase used");
		subLinkId = "hw.QuoDet";
	}
	
	void updatePresetStr() {
		try {
			SelectUtil su = getSelectUtil();
			TableRec tr = su.getQueryResult( "select * from presetMaster,presetDetail where pstm_key = 'DTO' and pstd_mrg = pstm_rg and pstd_str = '" + getCell("inv_vname").getString().trim() + "'" , null);
			if(tr.getRecordCount() <= 0)  {
				tr = su.getQueryResult("select pstm_rg,max(pstd_seq) maxseq from presetMaster,outer presetDetail where pstm_key = 'DTO' and pstd_mrg = pstm_rg group by 1",null);
				if(tr.getRecordCount() <= 0) return;
				int psrg = tr.getFieldInt("pstm_rg");
				int maxseq = tr.getFieldInt("maxseq");
				su.executeUpdate("insert into presetDetail (pstd_mrg,pstd_seq,pstd_str) values (?,?,?)", 
							new Wherecl()
								.appendArgument(psrg)
								.appendArgument(maxseq+1)
								.appendArgument(getCell("inv_vname").getString().trim())
						);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		updatePresetStr();
		return(rtnMsg);
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		updatePresetStr();
		return(rtnMsg);
	}
	@Override
	protected void setCellActionCalTotalAmount(ColumnCell p_cell) {
		super.setCellActionCalTotalAmount(p_cell);
	}

}
