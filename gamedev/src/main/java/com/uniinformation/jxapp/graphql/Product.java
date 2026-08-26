package com.uniinformation.jxapp.graphql;

import com.uniinformation.bicore.BiActionListener;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.wc.BiResultStock;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.JxZkBiFastCreate;

public class Product extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult br, int mode) {
		super.bindCellCollection(br, mode);
		if(!(br instanceof BiResultStock)) return;
		BiResultStock sr = (BiResultStock) br;
		if(sr.getActionOnCreate() == null) {
			sr.setActionOnCreate(
					new BiActionListener<ColumnCell>() {
						@Override
						public void actionPerformed(final ColumnCell p_cc) {
							final String pickView = p_cc.getBiColumn().getPickViewName();
							JxZkBiFastCreate.open(
									getSessionHelper(),
									sr.getSelectUtil(),
									"wc.CreateBrand",
									new BiActionListener<BiResult>() {
										@Override
						public void actionPerformed(BiResult p_createdResult) {
							removePickBySelectCache(pickView);
							setPickBySelectSelectedItem(p_createdResult.getCurrentCollection());
						}
									});
						}
					}
			);
		}
	}
}
