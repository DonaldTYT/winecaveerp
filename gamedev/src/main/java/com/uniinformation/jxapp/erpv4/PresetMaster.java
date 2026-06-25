package com.uniinformation.jxapp.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGipiPickViewInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.jxapp.JxZkBiBase;
//import com.uniinformation.utils.AbstractGetItemProperty;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class PresetMaster extends JxZkBiBase {
	
	class PresetDetailGipi extends ZkBiGetItemProperty {
		Vector myListColumns;
		public PresetDetailGipi(BiResult p_br, JxZkBiBase p_bibase) {
			super(p_br, p_bibase);
			// TODO Auto-generated constructor stub
			myListColumns = new Vector();
			for(Object o : super.getListColumns(null)) {
				if(o instanceof BiColumn && ((BiColumn) o).getLabel().equals("pstd_subcond")) {
//					myListColumns.add(null);
					continue;
				}
				myListColumns.add(o);
			}
		}

		@Override
		protected Vector getListColumns(Object p_v) {
			if(getBr().getCellString("pstm_scview").equals("")) {
				return(myListColumns);
			}
//			if(customizedTemplate == null) return(super.getListColumns(p_v));
			return(super.getListColumns(p_v));
		}	
		
	}
	
	public void bindCellCollection(BiResult c,int mode) {
		if( getGipi("erpv4.PresetDetail") == null) {
			setGipi("erpv4.PresetDetail",new PresetDetailGipi(c.getSubLink("erpv4.PresetDetail"),this));
		}
		super.bindCellCollection(c, mode);
		AbstractGetItemProperty gipi = getGipi("erpv4.PresetDetail");
		((ZkBiGetItemProperty) gipi).setUseDefaultPickup(true);
		((ZkBiGetItemProperty) gipi).setPickView("pstd_subcond", 
				new BiGipiPickViewInterface( ) {

					@Override
					public String getPickViewName() {
						// TODO Auto-generated method stub
						String s = getBr().getCellString("pstm_scview");
						if(s != null && !s.trim().equals("")) return(s); 
						return null;
					}

					@Override
					public String getPickColName() {
						// TODO Auto-generated method stub
						String s = getBr().getCellString("pstm_sccol");
						if(s != null && !s.trim().equals("")) return(s); 
						return null;
					}

					@Override
					public String getPickCondition(ColumnCell p_cc) {
						// TODO Auto-generated method stub
						String s = getBr().getCellString("pstm_sccond");
						if(s != null && !s.trim().equals("")) return(s); 
						return null;
					}
			
				}
			);
		if(c.getCellString("pstm_scview").equals("")) {
			
		}
	}
}
