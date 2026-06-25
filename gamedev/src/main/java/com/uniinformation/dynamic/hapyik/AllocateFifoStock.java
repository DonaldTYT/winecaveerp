package com.uniinformation.dynamic.hapyik;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.erpv4.DoMulti;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;



public class AllocateFifoStock implements JxActionListener {
	Hashtable <Integer, List<Pair<Comparable,Double>>> fifoHash;
	void fifoHashInit(DoMulti jxf) throws Exception {
		if(jxf.isDirty()) {
			throw new Exception("Order has been changed, can not apply fifo, please reload record.");
		}
		jxf.setDirtyFlag(true);
		fifoHash = new Hashtable <Integer, List<Pair<Comparable,Double>>> ();
		if(jxf.getBr().getCellString("stm_status").equals("Confirmed")) {
			if(true) throw new Exception("Fifo Allocation for conformed order not yet supported");
			List<BiCellCollection> rows = jxf.getBr().getSubLink("erpv4.DoDetMulti").getRowCollectionList();
			for(int i=0;i<rows.size();i++) {
				BiCellCollection col = rows.get(i);
				int irg = col.getCellInt("stmd_irg");
				String ref4 = col.getCellString("stmd_ref4");
				double qty = col.getCellDouble("stmd_qty");
				List<Pair<Comparable,Double>> fifoList = getOrLoadOneFifoRec(irg,jxf.getBr().getSelectUtil());
				int j=fifoList.size()-1;
				for(;j>=0;j--) {
					Pair<Comparable,Double> fifoAllocate = fifoList.get(j);
					int cc = fifoAllocate.getLeft().compareTo(ref4);
					if(cc == 0) {
						qty += fifoAllocate.getRight();
						fifoList.remove(j);
						fifoList.add(j,Pair.of(ref4, qty));
						break;
					} else if(cc < 0) {
						fifoList.add(j+1,Pair.of(ref4, qty));
						break;
					}
				}
				if(j < 0) {
					fifoList.add(0,Pair.of(ref4, qty));
				}
			}
		}
	}
	List<Pair<Comparable,Double>> getOrLoadOneFifoRec(int p_irg,SelectUtil p_su) throws Exception {
		List<Pair<Comparable,Double>> fifoList = fifoHash.get(p_irg);
		if(fifoList == null) {
			fifoList = new ArrayList<Pair<Comparable,Double>> ();
			TableRec tr = p_su.getQueryResult("select * from stockserial where stsn_irg = ? and stsn_nqty > 0 order by stsn_ref4",
						new Wherecl().appendArgument(p_irg)
					);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				fifoList.add(Pair.of(tr.getFieldString("stsn_ref4"), tr.getFieldDouble("stsn_nqty")));
			}
			fifoHash.put(p_irg, fifoList);
		}
		return(fifoList);
	}
	List<Pair<Comparable,Double>> getFifoStock(int p_irg,double p_qty,SelectUtil p_su) throws Exception {
		List<Pair<Comparable,Double>> fifoList = fifoHash.get(p_irg);
		if(fifoList == null) {
			fifoList = new ArrayList<Pair<Comparable,Double>> ();
			TableRec tr = p_su.getQueryResult("select * from stockserial where stsn_irg = ? and stsn_nqty > 0 order by stsn_ref4",
						new Wherecl().appendArgument(p_irg)
					);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				fifoList.add(Pair.of(tr.getFieldString("stsn_ref4"), tr.getFieldDouble("stsn_nqty")));
			}
			fifoHash.put(p_irg, fifoList);
		}
		List<Pair<Comparable,Double>> fifoAllocate = new ArrayList<Pair<Comparable,Double>> ();
		double remain = p_qty;
		while (remain > 0 && fifoList.size() > 0) {
			Pair<Comparable,Double> strec = fifoList.get(0);
			double qtyAllocate;
			if(remain >= strec.getRight() ) {
				qtyAllocate = strec.getRight();
				fifoList.remove(0);
			} else {
				qtyAllocate = remain;
				fifoList.remove(0);
				fifoList.add(0,Pair.of(strec.getLeft(), strec.getRight()-qtyAllocate));
//				strec.setValue(strec.getRight()-qtyAllocate);
			}
			fifoAllocate.add(Pair.of(strec.getLeft(), qtyAllocate));
			remain -= qtyAllocate;
		}
		return(fifoAllocate);
	}
	@Override
	public void actionPerformed(JxField field) {
			DoMulti jxf = (DoMulti) field.getJxForm();
			BiResult sr = jxf.getBr().getSubLink("erpv4.DoDetMulti");
			try {
			fifoHashInit(jxf);
			for(int i= sr.getRowCount()-1;i>=0;i--) {
				BiCellCollection col = sr.getRowCollectionV(i);
				int irg = col.getCellInt("stmd_irg");
				List<Pair<Comparable,Double>> fifoStock = getFifoStock(irg,col.getDouble("stmd_qty"),jxf.getBr().getSelectUtil());
				double remain = 0.0;
				for(int j=0;j<fifoStock.size();j++) {
					Pair<Comparable,Double> strec = fifoStock.get(j);
					if(j == 0) {
						col.getCell("stmd_ref4").set(strec.getLeft().toString());
						if(strec.getRight() < col.getDouble("stmd_qty")) {
							double r = col.getCellDouble("stmd_eratio");
							remain  = col.getDouble("stmd_qty") - strec.getRight();
							if(r != 1.0) {
								if(strec.getRight() % r != 0.0) {
									col.getCell("stmd_entryunit").set(col.getCellString("st_unit"));
									col.getCell("stmd_eratio").set(1.0);
									col.getCell("stmd_qty").set(strec.getRight());
								} else {
									col.getCell("stmd_entryqty").set(strec.getRight()/r);
									col.getCell("stmd_qty").set(strec.getRight());
								}
							} else {
								col.getCell("stmd_qty").set(strec.getRight());
								col.getCell("stmd_entryqty").set(strec.getRight());
							}
						}
					} else {
						JxField sv = jxf.jxAdd("list_"+sr.getView().getName().replace(".", "_"));
						BiCellCollection ncol = sr.newRowCollection();
//						ncol.getCell("inv_invno").set(col.getCellString("inv_invno"));
						ncol.getCell("stmd_irg").set(col.getCellInt("stmd_irg"));
						ncol.getCell("stmd_org").set(col.getCellInt("stmd_org"));
						ncol.getCell("stmd_qorg").set(col.getCellInt("stmd_qorg"));
						ncol.getCell("stmd_qirg").set(col.getCellInt("stmd_qirg"));
						ncol.getCell("stmd_ref4").set(strec.getLeft().toString());
//						ncol.getCell("st_icode").set(col.getCellString("st_icode"));
						ncol.getCell("stmd_entryunit").set(col.getCellString("st_unit"));
						ncol.getCell("stmd_eratio").set(1.0);
						ncol.getCell("stmd_entryqty").set(strec.getRight());
						ncol.getCell("stmd_qty").set(strec.getRight());
						ReturnMsg rtn = sr.addSubRecord(ncol, i,"");
						Object tr = rtn.getData();
						int rowIdx = jxf.getGipi(sr.getView().getName()).getIndexOf(tr);
						sv.addItemToList(tr, rowIdx);
						remain -= strec.getRight();
					}
				}
				if(remain > 0) {
						JxField sv = jxf.jxAdd("list_"+sr.getView().getName().replace(".", "_"));
						BiCellCollection ncol = sr.newRowCollection();
						ncol.getCell("stmd_irg").set(col.getCellInt("stmd_irg"));
						ncol.getCell("stmd_org").set(col.getCellInt("stmd_org"));
						ncol.getCell("stmd_qorg").set(col.getCellInt("stmd_qorg"));
						ncol.getCell("stmd_qirg").set(col.getCellInt("stmd_qirg"));
						ncol.getCell("stmd_entryunit").set(col.getCellString("st_unit"));
						ncol.getCell("stmd_eratio").set(1.0);
						ncol.getCell("stmd_entryqty").set(remain);
						ncol.getCell("stmd_qty").set(remain);
						ReturnMsg rtn = sr.addSubRecord(ncol, i,"");
						Object tr = rtn.getData();
						int rowIdx = jxf.getGipi(sr.getView().getName()).getIndexOf(tr);
						sv.addItemToList(tr, rowIdx);
					
				}
			}
			} catch (Exception ex) {
				UniLog.log(ex);
				jxf.messageBox(ex.toString());
				return;	
			}
	}

}
