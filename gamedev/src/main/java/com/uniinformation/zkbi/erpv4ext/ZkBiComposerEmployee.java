package com.uniinformation.zkbi.erpv4ext;

import org.json.JSONException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkcomp.ZkBiButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ZkBiComposerEmployee extends ZkBiComposerBase {

	@Override
	protected void setupExtraButton(final BiResult result) {
        final Button btPrintEmployee = new ZkBiButton();
        btPrintEmployee.setLabel(sessionHelper.getBtLabel("Print"));
        btPrintEmployee.setId("btPrintEmployee");
        btPrintEmployee.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("btPrintEmployee event:%s", event);
				printBatchEmployee(result, btPrintEmployee);
			}
        });
        abHelper.addButton(btPrintEmployee, "fa-user");
		ZkUtil.setupBatchModeButton(btPrintEmployee, batchModeToggleButton);
	}
	
	private void printBatchEmployee(BiResult br, Button btPrintEmployee) {
		Set<String> emList = getBatchEmList(br);
		if (emList.isEmpty()) {
			ZkUtil.showErrMsg("Please choose Employee");
			return;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String errMsg = printBatchEmployee(bos, emList);
		if (errMsg != null)
			ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Error:" + errMsg);
		else {
			ZkUtil.showPdfDialog((Component) btPrintEmployee.getRoot(), getSessionHelper(), bos.toByteArray(), "Print Employee");
			Button btCancel = (Button) btPrintEmployee.getParent().query("#" + btPrintEmployee.getId() + "BatchCancel");
			Events.echoEvent(Events.ON_CLICK, btCancel, null);
		}
	}

	private String printBatchEmployee(ByteArrayOutputStream os, Set<String> emList) {
		try {
    		PrtdocEmFullJson ppj = new PrtdocEmFullJson(emList);
    		ReturnMsg rtn = ppj.toPdfStream(os, getSessionHelper());
    		return rtn.getStatus() ? null : rtn.getMsg();
		}
		catch (Exception ex) {
			UniLog.log(ex);
			return ex.toString();
		}
	}

	private Set<String> getBatchEmList(BiResult br) {
		final Set<String> emList = new LinkedHashSet<String>();
		Set selection = listModelList.getSelection();
       	for (Iterator it = selection.iterator(); it.hasNext();) {
       		Object o = it.next();
         	Object ts = o;
          	if (ts instanceof TrStatFilter)
            	ts = ((TrStatFilter)ts).getTrStatIdx();
       		CellCollection cc = br.getRowCollectionO(ts);
       		String eid = cc.getString("em_eid");
       		emList.add(eid);
       	}
       	return emList;
	}

	private static class PrtdocEmFullJson extends PrtdocPerfJson {

		public PrtdocEmFullJson(Set<String> emList) throws JSONException {
			super("001", "A4P", "EMREC_02", "erpv4_hremfull_printDocument", Encoding.MS950_HKSCS);
			boolean flag = false;
			for (String eid : emList) {
				if (flag)
					newContent();
				jContent.put("eid", eid);
				flag = true;
			}
		}

		@Override
		public ReturnMsg toPdfStream(OutputStream os, final SessionHelper sh) throws Exception {
			ReturnMsg rtn = prtDoc(sh);
			if (!rtn.getStatus()) return(rtn);
			String fname = rtn.getMsg();
			InputStream is = sh.newErpFileInputStream(fname);
			int cc = ChnftrParser.getPaperTypeIndex(paperType);
			final ChnftrParser ps = new ChnftrParser(is, cc >= 0 ? "-p" + cc : "'");

			ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
				@Override
				public byte[] getImage(String p_key) {
					try{
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						FilingUtil.getFile(sh.getAgent(), null, p_key, bos);
						byte[] bytes = bos.toByteArray();
						bos.close();
						return(bytes);
					}
					catch(Exception ex){
						ex.printStackTrace();
						return(null);
					}
				}
			});
			ps.print(os);
			return(ReturnMsg.defaultOk);
		}
	}
}
