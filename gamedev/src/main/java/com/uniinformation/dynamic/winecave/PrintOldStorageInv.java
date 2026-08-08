package com.uniinformation.dynamic.winecave;

import java.util.List;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintOldDocMulti;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiAiAgentContext;

public class PrintOldStorageInv extends PrintOldStockOutInv {

	public PrintOldStorageInv() {
		super();
		docName = "StorageInvoice";
	}

	public PrintOldStorageInv(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
		docName = "StorageInvoice";
	}

	@Override protected String getInvoiceSublinkName() { return "graphql.StmpostExtSM"; }
	@Override protected String getPrintSegmentName() { return "winecave_print_storageinv"; }
	@Override protected String getSettingsFormPath() { return "zkf/winecave/PrintStorageInv.zul"; }
	@Override protected String getSelectionFormPath() { return "zkf/winecave/SelectStorageInv.zul"; }
	@Override protected String getProgressFormPath() { return "zkf/winecave/EmailStorageInvProgress.zul"; }
	@Override protected String getInvoiceDescription() { return "storage invoice"; }
	@Override protected String getChargeDescription() { return "Storage Charge Invoice"; }
	@Override protected String getChargeDescriptionLowerCase() { return "storage charge invoice"; }
	@Override protected String getAttachmentPrefix() { return "StorageInvoice"; }

	@Override
	public ZkBiAiAgentContext getAiAgentContext() {
		return null;
	}
	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);
		// TODO Auto-generated method stub
		List<BiCellCollection> outInvoices = br.getSubLink("graphql.StmpostExtSM").getRowCollectionList();
		for(BiCellCollection bcol : outInvoices) {
				int mrg = bcol.getCellInt("stmp_mrg");
				String cocode = bcol.getCellString("stmp_cocode");
				Value val = rpc.callSegment("winecave_print_storageinv",
							new VectorUtil()
							.addElement(mrg)
							.addElement(cocode)
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement("A4P")
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
						);
				if(val != null && val.toString().startsWith("OK  ")) {
					ReturnMsg rtn = printOneDocToPdf(val.toString().substring(4));
					if(rtn != null && !rtn.getStatus()) return(rtn);
				} else {
					return(null);
				}
			
		}
		return ReturnMsg.defaultOk;
	} 

//	@Override
//	protected List<String> printOneOldDoc(int p_idx) {
//		br.fetchOneRecV(p_idx);
//		ArrayList<String> files = new ArrayList<String>();
//		// TODO Auto-generated method stub
//		List<BiCellCollection> outInvoices = br.getSubLink("graphql.StmpostExtSM").getRowCollectionList();
//		for(BiCellCollection bcol : outInvoices) {
//				int mrg = bcol.getCellInt("stmp_mrg");
//				String cocode = bcol.getCellString("stmp_cocode");
//				Value val = rpc.callSegment("winecave_print_storageinv",
//							new VectorUtil()
//							.addElement(mrg)
//							.addElement(cocode)
//							.addElement("CHNPRINT")
//							.addElement("VARIABLE")
//							.addElement("A4P")
//							.addElement("NORMAL")
//							.addElement("LPTRAW")
//							.toVector()
//						);
//				if(val != null && val.toString().startsWith("OK  ")) {
//					files.add(val.toString().substring(4));
//				} else {
//					return(null);
//				}
//			
//		}
//		return files;
//	}

	@Override
	protected void doInitRpcClient() {
		// TODO Auto-generated method stub
		rpc.callSegment("setCocodeBaseccy",
		new VectorUtil()
		.addElement( Erpv4Config.getDefaultCoCode(sh))
		.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),
				Erpv4Config.getDefaultCoCode(sh)
				))
		.toVector()
		);	
	}

}
