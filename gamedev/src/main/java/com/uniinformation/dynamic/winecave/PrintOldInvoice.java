package com.uniinformation.dynamic.winecave;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

public class PrintOldInvoice  extends BiActionHandler implements JxActionListener {

	boolean downloadPdf = false;
	boolean useDiskForFinalPdf = true;
	BiResultInvoiceBase  br = null;
	SessionHelper sh = null;
	RpcClient rpc = null;
	PdfByteArrayOutputStream combinedPdfOutput = null;
	OutputStream combinedPdfOutputStream = null;
	File combinedPdfTempFile = null;
	Document combinedPdfDocument = null;
	PdfCopy combinedPdfCopy = null;

	private static class PdfByteArrayOutputStream extends ByteArrayOutputStream {
		InputStream toInputStream() {
			return new ByteArrayInputStream(buf, 0, count);
		}
	}

	private static class DeleteOnCloseFileInputStream extends FileInputStream {
		private final File file;

		DeleteOnCloseFileInputStream(File file) throws IOException {
			super(file);
			this.file = file;
		}

		@Override
		public void close() throws IOException {
			try {
				super.close();
			} finally {
				if(file != null && file.exists() && !file.delete()) {
					UniLog.log("Unable to delete temp pdf file " + file.getAbsolutePath());
				}
			}
		}
	}

	private void cleanupCombinedPdfState(boolean deleteTempFile) {
		combinedPdfCopy = null;
		combinedPdfDocument = null;
		combinedPdfOutput = null;
		if(combinedPdfOutputStream != null) {
			try {
				combinedPdfOutputStream.close();
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			combinedPdfOutputStream = null;
		}
		if(deleteTempFile && combinedPdfTempFile != null && combinedPdfTempFile.exists() && !combinedPdfTempFile.delete()) {
			UniLog.log("Unable to delete temp pdf file " + combinedPdfTempFile.getAbsolutePath());
		}
		combinedPdfTempFile = null;
	}

	public PrintOldInvoice() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public PrintOldInvoice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub 
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		br = (BiResultInvoiceBase) jxf.getBr();
		sh = br.getSessionHelper();
		int invrg = br.getCellInt("invh_rg");
		rpc = jxf.getRpcClient();
		ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
		rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
		Value val = rpc.callSegment("printer_autoselect",
					new VectorUtil()
					.addElement(1)
					.toVector()
				);
		//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement( br.getCellString("invh_cocode"))
				.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),br.getCellString("invh_cocode")))
				.toVector()
				);
		val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(br.getSessionHelper().getWebContentRealPath("images", true)) .toVector());
		val = rpc.callSegment("winecave_print_invinvoice",
					new VectorUtil()
					.addElement(invrg)
					.addElement("CHNPRINT")
					.addElement("VARIABLE")
					.addElement("A4P")
					.addElement("NORMAL")
					.addElement("LPTRAW")
					.toVector()
				);
		rpc.close();
		if(val != null && val.toString().startsWith("OK")) {
			String fname = val.toString().substring(4);
			UniLog.log("Print invoice got " + fname);
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
		cleanupCombinedPdfState(true);
		if(rpc != null) {
			rpc.close();
			rpc = null;
		}

		br = (BiResultInvoiceBase) p_result;
		sh = br.getSessionHelper();
		if(cnt > 100) {
			downloadPdf = true;
		} else {
			downloadPdf = false;
		}
		try {
			if(useDiskForFinalPdf) {
				combinedPdfTempFile = File.createTempFile("PrintOldInvoice-", ".pdf");
				combinedPdfTempFile.deleteOnExit();
				combinedPdfOutputStream = new FileOutputStream(combinedPdfTempFile);
			} else {
				combinedPdfOutput = new PdfByteArrayOutputStream();
				combinedPdfOutputStream = combinedPdfOutput;
			}
			combinedPdfDocument = new Document();
			combinedPdfCopy = new PdfCopy(combinedPdfDocument, combinedPdfOutputStream);
			combinedPdfDocument.open();
			rpc = biBase.getSessionHelper().getRpcClient();
   		    ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
   		    rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
   		    rpc.callSegment("printer_autoselect",
					new VectorUtil()
					.addElement(1)
					.toVector()
				);
   		    rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement( br.getCellString("invh_cocode"))
				.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),br.getCellString("invh_cocode")))
				.toVector()
				);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Initialized Print Job Failed"));
		}
		
		
		return ReturnMsg.defaultOk;
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		int invrg = br.getCellInt("invh_rg");
		Value val = rpc.callSegment("winecave_print_invinvoice",
				new VectorUtil()
				.addElement(invrg)
				.addElement("CHNPRINT")
				.addElement("VARIABLE")
				.addElement("A4P")
				.addElement("NORMAL")
				.addElement("LPTRAW")
				.toVector()
			);
		if(val != null && val.toString().startsWith("OK")) {
			PdfReader invoiceReader = null;
			try {
				String fname = val.toString().substring(4);
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
				}
			} catch (Exception ex) {
				UniLog.log(ex);
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
		return ReturnMsg.defaultOk;
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		try {
			combinedPdfDocument.close();
			combinedPdfOutputStream = null;
			InputStream combinedPdfInput = null;
			if(useDiskForFinalPdf) {
				combinedPdfInput = new DeleteOnCloseFileInputStream(combinedPdfTempFile);
				combinedPdfTempFile = null;
			} else {
				combinedPdfInput = combinedPdfOutput.toInputStream();
			}
			
			if(downloadPdf) {
				Filedownload.save(combinedPdfInput, "application/pdf", "PaymentNotice"+ ".pdf");
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

			// The download/print InputStream owns the final byte buffer/file from here.
			// Release the writer graph so the completed batch is not retained by this
			// action handler after response delivery.
			cleanupCombinedPdfState(false);
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
}
