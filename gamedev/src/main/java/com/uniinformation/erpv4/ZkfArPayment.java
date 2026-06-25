package com.uniinformation.erpv4;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;


public class ZkfArPayment extends com.uniinformation.zkf.ZkForm {

	EventListener hdl = null;
	SelectUtil su;
	
	public ZkfArPayment(Component p_root,SelectUtil p_su) {
		super(p_root, "zkf/erpv4/arpayment.zul");
		su = p_su;
		hdl = new EventListener() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				if(arg0.getTarget().getId().equals("btCancel")) {
					exitModal();
				}
				
			}
			
		};
		// TODO Auto-generated constructor stub
	}
	public void doModalPayment(CellCollectionArPayment  col) throws CellException {
		super.doModal(col, hdl);
	}
}
