package com.uniinformation.dynamic.winecave;

import java.util.List;

import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintOrMailOldDocMulti;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Prints one or more legacy wc.DeliveryNote documents as a combined PDF. */
public class PrintOrMailOldDeliveryNote extends PrintOrMailOldDocMulti {
	private static final String PRINT_SEGMENT = "winecave_print_delivery";

	public PrintOrMailOldDeliveryNote() {
		super();
		docName = "DeliveryNote";
	}

	public PrintOrMailOldDeliveryNote(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		docName = "DeliveryNote";
	}

	private ReturnMsg printDeliveryNote(BiResult delivery) {
		int mrg = delivery.getCellInt("stm_mrg");
		if(mrg <= 0) return new ReturnMsg(false,"Please save the Delivery Note before printing.");

		Value value = rpc.callSegment(PRINT_SEGMENT,
				new VectorUtil()
				.addElement(mrg)
				.addElement("CHNPRINT")
				.addElement("VARIABLE")
				.addElement("A4P")
				.addElement("NORMAL")
				.addElement("LPTRAW")
				.toVector());
		if(value == null || !value.toString().startsWith("OK  ")) {
			return new ReturnMsg(false,"Unable to print Delivery Note: "
					+ String.valueOf(value));
		}
		return printOneDocToPdf(value.toString().substring(4));
	}

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);
		return printDeliveryNote(br);
	}

	@Override
	public void actionPerformed(JxField field) {
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		printFromForm(jxf);
	}

	private void printFromForm(JxZkBiBase jxf) {
		BiResult delivery = jxf.getBr();
		ReturnMsg result = beforeAction(delivery,1);
		if(result == null || !result.getStatus()) {
			showError(result,"Unable to initialize Delivery Note printing.");
			return;
		}
		result = printDeliveryNote(delivery);

		// afterAction owns the RPC/PDF lifecycle initialized by beforeAction.
		ReturnMsg closeResult = afterAction(delivery);
		if(result == null || result.getStatus()) result = closeResult;
		if(result == null || !result.getStatus()) showError(result,"Unable to print Delivery Note.");
	}

	private void showError(ReturnMsg result,String defaultMessage) {
		String message = result == null ? defaultMessage : result.getMsg();
		UniLog.log(message);
		Messagebox.show(message);
	}

	@Override
	protected void doInitRpcClient() {
		String cocode = Erpv4Config.getDefaultCoCode(sh);
		rpc.callSegment("setCocodeBaseccy",new VectorUtil()
				.addElement(cocode)
				.addElement(Erpv4Config.getBaseCcy(sh,cocode)).toVector());
		rpc.callSegment("erpv4SetImageDir",new VectorUtil()
				.addElement(sh.getWebContentRealPath("images",true)).toVector());
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return true;
		if(!p_br.getSessionHelper().hasAccessRight("#Prtvoucher")) return true;
		return !p_isBatch && p_br.inBeginWork();
	}

	@Override protected String getPrintSegmentName() { return PRINT_SEGMENT; }

	/* Delivery Note is the master document, so the sublink selection dialog is unused. */
	@Override protected String getInvoiceSublinkName() { return ""; }
	@Override protected String getSettingsFormPath() { return ""; }
	@Override protected String getSelectionFormPath() { return ""; }
	@Override protected void outputDocuments(JxZkBiBase jxf,
			List<BiCellCollection> documents,boolean sendByEmail) {
		printFromForm(jxf);
	}
}
