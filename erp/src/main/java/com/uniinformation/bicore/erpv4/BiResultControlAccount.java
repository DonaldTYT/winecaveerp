package com.uniinformation.bicore.erpv4;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.NetworkNodeUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultControlAccount extends BiResultErpv4 {
	NetworkNodeUtil accountTree;
	CellValueAction actionSetAno = new CellValueAction() {


		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(!isActionEnabled()) return;
			ColumnCell cc = (ColumnCell) p_value;
			BiCellCollection bcc = cc.getCollection();
			int idx = getIndexByCollection_real(bcc);
			int n = getRowCount();
			int level = bcc.getCellInt("ca_level");
			String ano = bcc.getCellString("ca_ano");
			if(ano.trim().equals("")) {
				throw new CellException("Account Not Cannot Be Blank");
			}
			
			for(int i=idx+1;i<n;i++) {
				BiCellCollection bcc2 = getRowCollectionV(i);
				if(bcc2.getCellInt("ca_level") == level) break;
				if(bcc2.getCellInt("ca_level") == level+1) {
					bcc2.getCell("ca_ctrlano").set(ano);
				} 
			}
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
			
		}
		
	};
	public BiResultControlAccount(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		accountTree = new NetworkNodeUtil();
		logUserPrefix = "ca";
	}


	void addSubAccountToList(List<String> cl,Vector tl) {
		cl.sort(null);
		for(String ac : cl) {
			tl.add(accountTree.getNode(ac));
			List<String> sl = accountTree.getChildList(ac, true);
			if(!sl.isEmpty()) addSubAccountToList(sl,tl);
		}
	}
	
	public void sort() {
		if(true /* getParent() != null */) {
			Vector ttr = new Vector();
			UniLog.log("Sort Chart of Account in Tree order");
			accountTree.clear();
			BiCellCollection col;
			if(getParent() == null) col = getCurrentCollection() ; else col = newRowCollection();
			for(int i=0;i<getResultStat().size();i++) {
				Object tr = getResultStat().get(i);
				loadOneRec(i,col,false);
				accountTree.addNode(col.getCellString("ca_ano"), tr);
			}

			for(int i=0;i<getResultStat().size();i++) {
				Object tr = getResultStat().get(i);
				loadOneRec(i,col,false);
				String preq = col.getCellString("ca_ctrlano");
				if(!StringUtils.isBlank(preq)) {
					try {
//						accountTree.addChild(preq, col.getCellString("ca_ano"), true);
						accountTree.addChild(preq, col.getCellString("ca_ano"), false);
					} catch(Exception ex) {
						UniLog.log(ex);
					}
				}
			}
			addSubAccountToList(accountTree.getRootList(),ttr);
			if(ttr.size() == getResultStat().size()) {
				for(int i=0;i<ttr.size();i++) {
					getResultStat().set(i, ttr.get(i));
				}
			} else {
				UniLog.log("Error Treed account list not match original resultStatList, sort skipped");
			}
			invalidateLoadCache();
		} else {
			super.sort();
		}
	}
	int getLevel(String p_ano) {
		String cano = p_ano;
		for(int i=0;;i++) {
			List<String> pl = accountTree.getParentList(cano, true);
			if(pl.size() != 1) return(i);
			cano = pl.get(0);
		}
	}
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement(col.getCellString("co_cocode")
						)
				.addElement(Erpv4Config.getBaseCcy(sh,col.getCellString("co_cocode")))
				.toVector()
				);
		rpc.callSegment("accv4_deletebl",
				new VectorUtil()
					.addElement(col.getCellString("ca_ano"))
					.toVector()
				);
		return(new ReturnMsg(true));  
	}
	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		if(!p_cc.getCellString("ca_organo").equals("")) {
			try {
				if(getParent() == null || p_cc.getCellInt("ca_numjn") > 0 || p_cc.getCellInt("ca_numdet") > 0) {
					p_cc.getCell("ca_ano").setMode(Cell.VMODE_DISPONLY);
				} else {
					p_cc.getCell("ca_ano").addAction( actionSetAno );
				}
				p_cc.getCell("ca_level").set(getLevel(p_cc.getCellString("ca_ano")));
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}	
	
	boolean canCaRestore(int p_idx) {
		BiCellCollection col = getRowCollectionV(p_idx);
		if(col.getCellString("ca_ctrlano").equals("")) return(true);
		for(int i=p_idx-1;i>=0;i--) {
			BiCellCollection pcol = getRowCollectionV(i);
			if(pcol.getCellString("ca_ano").equals(col.getCellString("ca_ctrlano"))) {
				if(isMarkedDelete(i)) return(false);
				return(canCaRestore(i));
			}
		}
		return(true);
	}
	
	@Override
	public boolean markDelete(Object o,boolean p_sw) {
		if(p_sw) {
			Object tr = (Object) o;
			int i = getResultStat().indexOf(tr);
			if( i >= 0) {
				HashSet<String> childAno = new HashSet<String>();
				BiCellCollection root = getRowCollectionV(i);
				if(root.getCellInt("ca_numjn") > 0) return(false);
				childAno.add(root.getCellString("ca_ano"));
				for(i++;i<getResultStat().size();i++) {
					BiCellCollection child = getRowCollectionV(i);
					if(childAno.contains(child.getCellString("ca_ctrlano"))) {
						if(!isMarkedDelete(getResultStat().get(i))) return(false);
						childAno.add(child.getCellString("ca_ano"));
					} else break;
				}
			}
		} else {
			Object tr = o;
			int i = getResultStat().indexOf(tr);
			if( i >= 0) {
				if(!canCaRestore(i)) return(false);
			}
		}
		return(super.markDelete(o, p_sw));
	}
	
	@Override
	public ReturnMsg addSubRecord(CellCollection cl, int p_insIdx,String p_dummy) {
		ReturnMsg rtn = super.addSubRecord(cl, p_insIdx,p_dummy);
		if(rtn == null || rtn.getStatus()) {
			if(p_insIdx > 0) {
				try {
					BiCellCollection col = getRowCollectionV(p_insIdx-1);
					if(col.getCellString("ca_utype").equals("UD")) {
						if(col.getCellInt("ca_numjn") > 0) {
							return(new ReturnMsg(false,"Acccount " + col.getCellString("ca_aname") + " already has transactions, cannot convert to control Account"));
						} else {
							col.getCell("ca_utype").set("UC");
						}
					}
					cl.getCell("ca_level").set(col.getCellInt("ca_level")+1);
					cl.getCell("ca_ctrlano").set(col.getCellString("ca_ano"));
				} catch (CellException cex) {
					UniLog.log(cex);
				}
			}
		}
		return(rtn);
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean p_isUpdate)
	{
		if(! p_isUpdate ) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement(col.getCellString("co_cocode")
						)
				.addElement(Erpv4Config.getBaseCcy(sh,col.getCellString("co_cocode")))
				.toVector()
				);
		rpc.callSegment("gl_gennewbl",
				new VectorUtil()
					.addElement(col.getCellString("ca_ano"))
					.toVector()
				);
		}
		return(ReturnMsg.defaultOk);
	}
	
	boolean hasSubAccount(String p_ano) {
		Vector<BiCellCollection> v = getRowCollectionList();
		for(BiCellCollection bc : v) {
			if(bc.getCellString("ca_ctrlano").equals(p_ano)) return(true);
		}
		return(false);
	}
	
	/* use this for pre process validataion before add/update */
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		try {
		if(hasSubAccount(col.getCellString("ca_ano"))) {
			if(col.getCellInt("ca_numjn") > 0 ) {
				return(new ReturnMsg(false,"Error : " + col.getCellString("ca_ano") + " Has Transactions,  cannot be set to control account"));
			}
			col.getCell("ca_utype").set("UC");
		} else {
			col.getCell("ca_utype").set("UD");
		}
		} catch(CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}
	
	public List<String> getParent(String ac) {
		List<String> pl = accountTree.getParentList(ac, false);
		return(pl);
	}
}
