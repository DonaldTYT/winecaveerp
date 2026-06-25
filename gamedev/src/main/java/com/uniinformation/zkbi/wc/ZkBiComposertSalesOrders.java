package com.uniinformation.zkbi.wc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.winecave.WineCaveApiUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposertSalesOrders extends ZkBiComposerReport {

	@Override
    protected void setupExtraButton(final BiResult result)
    {
    	final Button btnPrintInvoice = new ZkBiButton();
       	btnPrintInvoice.setLabel(sessionHelper.getBtLabel("Print Invoice"));
    	btnPrintInvoice.setImage("images/icons/zkweb/020-import-25x25.png");
        btnPrintInvoice.setId("btnPrintInv");
        btnPrintInvoice.setTooltiptext(sessionHelper.getLabel("Print Invoice"));
    	abHelper.addButton(btnPrintInvoice, "fa-cloud-upload");
		btnPrintInvoice.addEventListener("onClick",
				new EventListener() {
					@Override
					public void onEvent(Event arg0) throws Exception {
						final java.util.Set selection = listModelList.getSelection();
						if(selection.size() != 1) {
							Messagebox.show(
									"Please Select Order",
									sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
							return;
						}
						String pdfFile = WineCaveApiUtil.wcPrintInvoice(getSessionHelper(),result.getCellInt("stm_mrg"));
						if(pdfFile != null) {
							String fname = pdfFile;
							InputStream is = sessionHelper.newErpFileInputStream(fname);
							ChnftrParser ps = new ChnftrParser(is,"");
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							ps.print(bos);
							ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
							ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
							/*
							String fileName = GsonUtil.getString(p_actionData, "fileName","");
							if (isShowPdfDialog) {
								UniLog.log1("show pdf dialog, download file name:%s", fileName);
								Collection<Component> comps = Executions.getCurrent().getDesktop().getComponents();
								for (Component comp : comps) {
									if (comp instanceof Window) {
										ZkUtil.showPdfDialog(comp, p_sh, bos.toByteArray(), fileName);
										break;
									}
								}
							} else {
								ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
								ZkUtil.printFromStream(bis, "application/pdf", p_sh);
							}
							*/
							bos.close();
						}
					}
			}
		);
    }
}
