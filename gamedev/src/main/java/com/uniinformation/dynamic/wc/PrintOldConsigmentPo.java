package com.uniinformation.dynamic.wc;

import java.util.List;

import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintOldDocMulti;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Prints the consignment purchase orders belonging to a wc.StockOut row. */
public class PrintOldConsigmentPo extends PrintOldDocMulti {
	private static final String CONSIGNMENT_PO_SUBLINK = "wc.StmpostExt";
	private static final String PRINT_SEGMENT = "winecave_print_cnpo";

	public PrintOldConsigmentPo() {
		super(null);
		docName = "ConsigmentPO";
	}

	public PrintOldConsigmentPo(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		docName = "ConsigmentPO";
	}

	private ReturnMsg printConsignmentPos(BiResult stockOut) {
		BiResult poResult = stockOut.getSubLink(CONSIGNMENT_PO_SUBLINK);
		List<BiCellCollection> poRows = poResult.getRowCollectionList();
		boolean printed = false;

		for(int i = 0;i < poRows.size();i++) {
			if(poResult.isMarkedDelete(poResult.getTrStatObj(i))) continue;

			BiCellCollection po = poRows.get(i);
			int mrg = po.getCellInt("stmp_mrg");
			String cocode = po.getCellString("stmp_cocode");
			if(mrg <= 0 || cocode == null || cocode.trim().isEmpty()) continue;

			Value val = rpc.callSegment(PRINT_SEGMENT,new VectorUtil()
					.addElement(mrg)
					.addElement(0) // Print with logo, matching the old PERF action.
					.addElement(cocode)
					.addElement("CHNPRINT")
					.addElement("VARIABLE")
					.addElement("A4P")
					.addElement("NORMAL")
					.addElement("LPTRAW")
					.toVector());
			if(val == null || !val.toString().startsWith("OK  ")) {
				return new ReturnMsg(false,"Unable to print consignment PO for "
						+ cocode + ": " + String.valueOf(val));
			}

			ReturnMsg result = printOneDocToPdf(val.toString().substring(4));
			if(result != null && !result.getStatus()) return result;
			printed = true;
		}

		if(!printed)
			return new ReturnMsg(false,"No consignment purchase order is available to print.");
		return ReturnMsg.defaultOk;
	}

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);
		return printConsignmentPos(br);
	}

	@Override
	public void actionPerformed(JxField field) {
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult stockOut = jxf.getBr();
		ReturnMsg result = beforeAction(stockOut,1);
		if(result == null || !result.getStatus()) {
			String message = result == null ? "Unable to initialize consignment PO printing."
					: result.getMsg();
			UniLog.log(message);
			Messagebox.show(message);
			return;
		}
		result = printConsignmentPos(stockOut);

		// afterAction owns the RPC/PDF lifecycle initialized by beforeAction.
		ReturnMsg closeResult = afterAction(stockOut);
		if(result == null) result = closeResult;
		if(result != null && !result.getStatus()) {
			UniLog.log(result.getMsg());
			Messagebox.show(result.getMsg());
		}
	}

	@Override
	protected void doInitRpcClient() {
		String cocode = Erpv4Config.getDefaultCoCode(sh);
		rpc.callSegment("setCocodeBaseccy",new VectorUtil()
				.addElement(cocode)
				.addElement(Erpv4Config.getBaseCcy(sh,cocode))
				.toVector());
		rpc.callSegment("erpv4SetImageDir",new VectorUtil()
				.addElement(sh.getWebContentRealPath("images",true))
				.toVector());
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return true;
		if(!p_br.getSessionHelper().hasAccessRight("#prtpo")) return true;
		return !p_isBatch && p_br.inBeginWork();
	}
}
