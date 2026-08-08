package com.uniinformation.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Filedownload;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultInvoiceBase;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public abstract class PrintOldDocMulti  extends BiActionHandler implements JxActionListener {

	public PrintOldDocMulti(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		if(p_bibase != null) useAsync = p_bibase.getSessionHelper().getAllowBatchPrtdocAsync(); else useAsync = false;
		// TODO Auto-generated constructor stub
	}

	protected String docName = "downloadpdf";
	protected Boolean downloadPdf = null;
	protected BiResult br = null;
	protected SessionHelper sh = null;
	protected RpcClient rpc = null;
	PdfByteArrayOutputStream combinedPdfOutput = null;
	Document combinedPdfDocument = null;
	PdfCopy combinedPdfCopy = null;
	int numPage = 0;
	static final int MAX_PAGE_TO_VIEW = 100;

	private static class PdfByteArrayOutputStream extends ByteArrayOutputStream {
		InputStream toInputStream() {
			return new ByteArrayInputStream(buf, 0, count);
		}
	}
//	public PrintOldDocMulti() {
//		super(null);
//		// TODO Auto-generated constructor stub
//	}
//	public PrintOldDocMulti(ZkBiComposerBase p_bibase) {
//		super(p_bibase);
//		// TODO Auto-generated constructor stub
//	}
	
//	abstract protected List<String >printOneOldDoc(int p_idx);
//	protected Value printOneOldDoc(BiResult br) {
//		int invrg = br.getCellInt("invh_rg");
//		Value val = rpc.callSegment("winecave_print_invinvoice",
//					new VectorUtil()
//					.addElement(invrg)
//					.addElement("CHNPRINT")
//					.addElement("VARIABLE")
//					.addElement("A4P")
//					.addElement("NORMAL")
//					.addElement("LPTRAW")
//					.toVector()
//				);
//		return(val);
//	}
//	
	abstract protected void doInitRpcClient();
//	protected void doInitRpcClient(RpcClient rpc) {
//		rpc.callSegment("setCocodeBaseccy",
//				new VectorUtil()
//				.addElement( br.getCellString("invh_cocode"))
//				.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),br.getCellString("invh_cocode")))
//				.toVector()
//				);
//		rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(br.getSessionHelper().getWebContentRealPath("images", true)) .toVector());
//	}
	
	@Override
	public void actionPerformed(JxField field) {
		// TODO Aute-generated method stub 
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		br = (BiResultInvoiceBase) jxf.getBr();
		sh = br.getSessionHelper();
		rpc = jxf.getRpcClient();
		ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
		rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
		rpc.callSegment("printer_autoselect",
					new VectorUtil()
					.addElement(1)
					.toVector()
				);
		doInitRpcClient();
		List<String> files = null;
		rpc.close();
		if(files != null) {
			String fname ;
			if(files.size() != 1) {
				UniLog.log("Multidoc in action perform not supported");
				return;
			} else {
				fname = files.get(0);
			}
			UniLog.log("Print invoice got " + fname);
			if(StringUtils.isBlank(fname)) return;
			try {
				InputStream is = jxf.erpFileInputStream(fname);
				ChnftrParser ps = new ChnftrParser(is,""); // print as A3 , always two pages
//				ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
				ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
					@Override
					public byte[] getImage(String p_key) {
						String url=null;
						if(p_key.startsWith(ChnftrParser.GETIMAGE_TAG)) {
							url = SessionHelper.URLHEADER_FILING+p_key.substring(ChnftrParser.GETIMAGE_TAG.length());
						} else {
							url = p_key;
						}
						try {
							return(sh.newErpFileToByteArray(url));
						} catch (Exception ex) {
							UniLog.log(ex);
							return(null);
						}
						
					}});	
				ps.setUseGetImageInterfaceByDefault(true);	
				
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ps.print(bos);
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				ZkUtil.printFromStream(bis, "application/pdf", sh);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
//		Messagebox.show("Print Old Invoice ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
//				new EventListener() {
//				   public void onEvent(Event evt) throws Exception {
//				    	if (((Integer)evt.getData()) == Messagebox.YES){
//				    	} else{
//				    		return;
//				    	}
//				   }
//				}
//			)k";
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		// An interrupted batch may not have reached afterAction(). Do not carry its
		// accumulated PDF or RPC connection into the next batch.
		combinedPdfCopy = null;
		combinedPdfDocument = null;
		combinedPdfOutput = null;
		numPage = 0;
		downloadPdf = null;
		if(rpc != null) {
			rpc.close();
			rpc = null;
		}

		br = p_result;
		sh = br.getSessionHelper();
		try {
			combinedPdfOutput = new PdfByteArrayOutputStream();
			combinedPdfDocument = new Document();
			combinedPdfCopy = new PdfCopy(combinedPdfDocument, combinedPdfOutput);
			combinedPdfDocument.open();
			rpc = biBase.getSessionHelper().getRpcClient();
   		    ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
   		    rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
   		    rpc.callSegment("printer_autoselect",
					new VectorUtil()
					.addElement(1)
					.toVector()
				);
   		    doInitRpcClient();
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Initialized Print Job Failed"));
		}
		
		
		return ReturnMsg.defaultOk;
	}
	
	protected ReturnMsg printOneDocToPdf(String fname) {
			PdfReader invoiceReader = null;
			try {
				InputStream is = biBase.getSessionHelper().newErpFileInputStream(fname);
				ChnftrParser ps = new ChnftrParser(is,""); // print as A3 , always two pages
//				ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
				ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
					@Override
					public byte[] getImage(String p_key) {
						String url=null;
						if(p_key.startsWith(ChnftrParser.GETIMAGE_TAG)) {
							url = SessionHelper.URLHEADER_FILING+p_key.substring(ChnftrParser.GETIMAGE_TAG.length());
						} else {
							url = p_key;
						}
						try {
							return(sh.newErpFileToByteArray(url));
						} catch (Exception ex) {
							UniLog.log(ex);
							return(null);
						}
						
					}});	
				ps.setUseGetImageInterfaceByDefault(true);	
				PdfByteArrayOutputStream invoiceOutput = new PdfByteArrayOutputStream();
				ps.print(invoiceOutput);

				invoiceReader = new PdfReader(invoiceOutput.toInputStream());
				for(int page = 1; page <= invoiceReader.getNumberOfPages(); page++) {
					combinedPdfCopy.addPage(combinedPdfCopy.getImportedPage(invoiceReader, page));
					numPage++;
				}
				return(ReturnMsg.defaultOk);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			} finally {
				if(invoiceReader != null) {
					try {
						combinedPdfCopy.freeReader(invoiceReader);
					} catch (Exception ex) {
						UniLog.log(ex);
					} finally {
						invoiceReader.close();
					}
				}
			}
	}

//	public ReturnMsg processActionXX(BiResult p_result, int p_recIdx) {
//		List<String> files = printOneOldDoc(p_recIdx);
//		if(files != null) {
//			PdfReader invoiceReader = null;
//			try {
//			for(String fname :files) {
//				InputStream is = biBase.getSessionHelper().newErpFileInputStream(fname);
//				ChnftrParser ps = new ChnftrParser(is,""); // print as A3 , always two pages
//				//ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
//				ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
//					@Override
//					public byte[] getImage(String p_key) {
//						String url=null;
//						if(p_key.startsWith(ChnftrParser.GETIMAGE_TAG)) {
//							url = SessionHelper.URLHEADER_FILING+p_key.substring(ChnftrParser.GETIMAGE_TAG.length());
//						} else {
//							url = p_key;
//						}
//						try {
//							return(sh.newErpFileToByteArray(url));
//						} catch (Exception ex) {
//							UniLog.log(ex);
//							return(null);
//						}
//						
//					}});	
//				ps.setUseGetImageInterfaceByDefault(true);	
//				PdfByteArrayOutputStream invoiceOutput = new PdfByteArrayOutputStream();
//				ps.print(invoiceOutput);
//
//				invoiceReader = new PdfReader(invoiceOutput.toInputStream());
//				for(int page = 1; page <= invoiceReader.getNumberOfPages(); page++) {
//					combinedPdfCopy.addPage(combinedPdfCopy.getImportedPage(invoiceReader, page));
//					numPage++;
//				}
//			}
//			} catch (Exception ex) {
//				UniLog.log(ex);
//			} finally {
//				if(invoiceReader != null) {
//					try {
//						combinedPdfCopy.freeReader(invoiceReader);
//					} catch (Exception ex) {
//						UniLog.log(ex);
//					} finally {
//						invoiceReader.close();
//					}
//				}
//			}
//		}
//		return ReturnMsg.defaultOk;
//	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		try {
			combinedPdfDocument.close();
			InputStream combinedPdfInput = combinedPdfOutput.toInputStream();
			
			if(downloadPdf == null) {
				downloadPdf = (numPage > MAX_PAGE_TO_VIEW);
			}
			
			if(downloadPdf) {
				Filedownload.save(combinedPdfInput, "application/pdf", docName + ".pdf");
			} else {
				ZkUtil.printFromStream(combinedPdfInput, "application/pdf", biBase.getSessionHelper());
			}
			
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"End Print Job Failed"));
		} finally {
			if(rpc != null) {
				rpc.close();
				rpc = null;
			}

			// The download/print InputStream owns the final byte buffer from here.
			// Release the writer graph so the completed batch is not retained by this
			// action handler after response delivery.
			combinedPdfCopy = null;
			combinedPdfDocument = null;
			combinedPdfOutput = null;
			br = null;
			sh = null;
		}
	}

	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(true);
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			if(!p_br.getSessionHelper().hasAccessRight("#prtinv")) {
				return(true);
			}
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
	
	public ReturnMsg isRunnable(BiResult br,boolean isBatch) {
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		UniLog.log1("afterActionAsync start");
		ReturnMsg rtn = afterAction(null);
		biBase.hideProgressPanel();
		cb.callback(rtn);
	}
	
	@Override
	public boolean preserveListOrder () {
		return(true);
	}
}
