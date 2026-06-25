package com.uniinformation.zkbi.clinic;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellVector;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;

public class ZkBiComposerIcodeBarcodeConflict extends ZkComposerBase {

	@Wire
	private Listbox lbMain;
	@Wire
	private Button btRefresh;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");
		
		btRefresh.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			private ListModelList<R> listModelList = new ListModelList<R>();
			class R {
				String barcode;
				String icode;
			}
			{
				lbMain.setModel(listModelList);
				lbMain.setItemRenderer(new ListitemRenderer<R> () {
					@Override
					public void render(Listitem li, R r, int index) throws Exception {
						li.appendChild(new Listcell(r.barcode));
						li.appendChild(new Listcell(r.icode));
					}
				});
			}
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				try {
					listModelList.clear();
					BiResult br = sessionHelper.newBiResult("erpv4.Stock");
					CellVector cv = br.getSelectUtil().getQueryResultToCellVector("select st_barcode, count(*) c from stock where st_barcode <> '' group by st_barcode having count(*) > 1", null);
					for (Object o : cv) {
						CellCollection cc = (CellCollection)o;
						String barcode = cc.getString("st_barcode");
						int count = cc.getInt("c");
						UniLog.log1("barcode:%s, count:%d", barcode, count);
						TableRec tr = br.getSelectUtil().getQueryResult("select st_icode from stock where st_barcode = ?", new Wherecl().appendArgument(barcode));
						for (int i = 0; i < tr.getRecordCount(); i++) {
							tr.setRecPointer(i);
							R r = new R();
							r.barcode = barcode;
							r.icode = tr.getFieldString("st_icode");
							listModelList.add(r);
						}
					}
				} catch(Exception ex) {
					UniLog.log(ex);
				}
			}
		});
	}
}
