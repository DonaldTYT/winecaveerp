package com.uniinformation.jxapp.wc;

import com.uniinformation.bicore.BiActionListener;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.wc.BiResultStmdMI;
import com.uniinformation.jxapp.JxZkBiFastCreate;
import com.uniinformation.jxapp.JxZkBiBase;

public class StockIn extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult br, int mode) {
		super.bindCellCollection(br, mode);
		BiResult sublink = br.getSubLink("wc.StmdMi");
		if(!(sublink instanceof BiResultStmdMI)) return;
		final BiResultStmdMI sr = (BiResultStmdMI) sublink;
		if(sr.getActionOnCreate() == null) {
			sr.setActionOnCreate(
					new BiActionListener<ColumnCell>() {
						@Override
						public void actionPerformed(final ColumnCell p_cc) {
							final String pickView = p_cc.getBiColumn().getPickViewName();
							JxZkBiFastCreate.open(
									getSessionHelper(),
									sr.getSelectUtil(),
									"wc.CreateProduct",
									new BiActionListener<BiResult>() {
										@Override
										public void actionPerformed(BiResult p_createdResult) {
											removePickBySelectCache(pickView);
										}
									});
						}
					}
			);
		}
	}
}
