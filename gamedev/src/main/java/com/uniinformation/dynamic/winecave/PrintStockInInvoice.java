package com.uniinformation.dynamic.winecave;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.Executions;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultInvoiceBase;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintStockInInvoice  extends BiActionHandler implements JxActionListener {

	public PrintStockInInvoice() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public PrintStockInInvoice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	
	
	HashMap<String,HashSet<Integer>> invHash;
	
	void addOneStockIn(BiResult br) throws Exception {
		invHash = new HashMap<String,HashSet<Integer>>();
		Vector<BiCellCollection> invList = br.getSubLink("wc.StmpostExt").getRowCollectionList();
		for(BiCellCollection bc : invList) {
			HashSet<Integer> mrgHash = invHash.get(bc.getCellString("stmp_cocode"));
			if(mrgHash == null) {
				mrgHash = new HashSet<Integer>();
				invHash.put(bc.getCellString("stmp_cocode"),mrgHash);
			}
			mrgHash.add(bc.getInt("stmp_mrg"));
		}
	}
	String printStockInInvoices(RpcClient rpc,String p_cocode,HashSet<Integer> mrgHash) throws Exception {
		List<Integer> mrgList = new ArrayList<>(mrgHash);
		mrgList.sort(null);
		Vector args = new Vector();
		args.add(mrgList.size());
		for(int mrg : mrgList) {
			args.add(mrg);
			args.add(p_cocode);
		}
		args.add("CHNPRINT");
		args.add("VARIABLE");
		args.add("A4P");
		args.add("NORMAL");
		args.add("LPTRAW");
		Value val = rpc.callSegment("winecave_printStockInInvoice",args);
		if(val != null && val.toString().startsWith("OK")) {
			return(val.toString().substring(4));
		}
		return(null);
	}
	
	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub 
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult br = (BiResult) jxf.getBr();
		RpcClient rpc = jxf.getRpcClient();
		try {
		addOneStockIn(br);
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
		
		List<String> invList = new ArrayList<>(invHash.keySet());
		invList.sort(null);
		if(invHash.size() == 1) {
			String docUrl = printStockInInvoices(rpc,invList.get(0),invHash.get(invList.get(0)));
			if(docUrl != null) {
				InputStream is = jxf.erpFileInputStream(docUrl);
				ChnftrParser ps = new ChnftrParser(is,""); // print as A3 , always two pages
//				ChnftrParser ps = new ChnftrParser(is,""); // print as A4 , ok
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ps.print(bos);
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
			}
		}
		rpc.close();
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
		} catch (Exception ex){
			UniLog.log(ex);
			rpc.close();
		}
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		// TODO Auto-generated method stub
		return null;
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
