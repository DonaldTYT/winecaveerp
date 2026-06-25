package com.uniinformation.prtdoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.PrtdocJsonChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class PrtdocPerfJson extends PrtdocJson {
	private PrtdocJsonChnftrRpcServlet.Callback prtdocJsonRpcservletCallback;
//	private final String imageDir = ZkUtil.getWebContentRealPath("images", true);

	String cocode;
	protected PrtdocPerfJson(String p_cocode,String p_paperType, String p_docname, String p_callsegment,PrtdocJson.Encoding p_encoding) throws JSONException {
		super(p_paperType, p_docname, p_callsegment,p_encoding);
		cocode = p_cocode;
		JSONArray ja = new JSONArray();
    	ja.put("CHNPRINT");
    	ja.put("VARIABLE");
    	ja.put(p_paperType == null ? "A4P" : p_paperType);
    	ja.put("NORMAL");
    	ja.put("LPTRAW");
    	if(p_encoding == PrtdocJson.Encoding.UTF8) {
    		jRoot.put("Encoding", "UTF-8");
    	}
    	jRoot.put("plptopts", ja);
	}

	public ReturnMsg prtDoc(SessionHelper p_sh)  throws Exception {
			RpcClient rpc = p_sh.getRpcClient();
			if(encode == Encoding.UTF8) {
				rpc.setWriteEncoding("UTF-8");
				rpc.setReadEncoding("UTF-8");
			}
			ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
			PrtdocJsonChnftrRpcServlet prtdocJsonRpcservlet = new PrtdocJsonChnftrRpcServlet(rpc.getConnection(), prtdocJsonRpcservletCallback);
			rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
			rpc.setRpcServlet(prtdocJsonRpcservlet.getClass().getName(), prtdocJsonRpcservlet);
 			rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
//				.addElement(Erpv4Config.getCoCode(p_sh))
//				.addElement(Erpv4Config.getBaseCcy(p_sh))
				.addElement(cocode)
				.addElement(BiConfig.getBaseCcy(p_sh,cocode))
				.toVector()
				);
    		rpc.callSegment("printer_autoselect",
    				new VectorUtil()
    			.addElement(1)
    			.toVector()
    			);
    		/*
			if(encode == Encoding.ENCODE_UTF8) {
				rpc.callSegment("setBig5ToUtf8",
    				new VectorUtil()
    				.addElement(1)
    				.toVector()
				);
			}
			*/
    		//rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(ZkUtil.getWebContentRealPath("images", true)).toVector());
    		final String imageDir = p_sh.getWebContentRealPath("images", true);
    		UniLog.log1("imageDir:%s", imageDir);
    		rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(imageDir).toVector());
    		
    		Value val = rpc.callSegment("unique_filename",
    				new VectorUtil()
    				.addElement("/tmp")
    				.addElement("jrpt")
    				.toVector()
    						);
    		OutputStream pos = p_sh.newErpFileOutputStream(val.toString());	
    		toJsonStream(pos,true);
    		rpc.setTimeout(900000); /* set call timeout to 15 mins */
    		val = rpc.callSegment("prtdocutil_print_jsonfile",
    				new VectorUtil()
    				.addElement(val.toString())
    				.toVector()
    						);
    		rpc.close();
    		if(val != null && val.toString().startsWith("OK")) {
				String fname = val.toString().substring(4);
				return(new ReturnMsg(true,fname));
			} else {
				if(val != null) 
					return(new ReturnMsg(false,"Print Quotation Error : " + val.toString()));
				else
					return(new ReturnMsg(false,"Print Quotation Error : Unknown"));
			}
	}

	@Override
	public ReturnMsg toPdfStream(OutputStream os, final SessionHelper sh) throws Exception {
		// TODO Auto-generated method stub
		ReturnMsg rtn = prtDoc(sh);
		if(! rtn.getStatus()) return(rtn);
		String fname = rtn.getMsg();
		InputStream is = sh.newErpFileInputStream(fname);
		int cc = ChnftrParser.getPaperTypeIndex(paperType);
		ChnftrParser ps;
		if(encode == Encoding.MS950_HKSCS) {
		if( cc >= 0) {
			ps = new ChnftrParser(is,"-p"+cc);
		} else {
			ps = new ChnftrParser(is,"'");
		}
		} else {
		if( cc >= 0) {
			if(topLeftMargin >= 0) {
				ps = new ChnftrParser(is,"-p"+cc+" -m"+topLeftMargin,"UTF-8");
			} else {
				ps = new ChnftrParser(is,"-p"+cc,"UTF-8");
			}
		} else {
			ps = new ChnftrParser(is,"'","UTF-8");
		}
		}
					ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
						@Override
						public byte[] getImage(String p_key) {
							//TODO obtain image file from filing
							/*
							if (!StringUtils.startsWith(p_key, "jxHwQuoDetFiling_")){
								UniLog.logm(this, "invalid getImage key %s", p_key);
								return(null);
							}
							*/
							try{
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								String key;
								String table = null;
								int idx = p_key.indexOf("@");
								if(idx > 0) {
									key = p_key.substring(0,idx);
									table = p_key.substring(idx+1);
								} else {
									key = p_key;
								}
								FilingUtil.getFile(sh.getAgent(), table, key, bos);
								byte[] bytes = bos.toByteArray();
								bos.close();
								return(bytes);
							}
							catch(Exception ex){
								ex.printStackTrace();
								return(null);
							}
						}});
		ps.print(os);
		return(ReturnMsg.defaultOk);
	}

	@Override
	public ReturnMsg toExcelStream(OutputStream os, SessionHelper sh) throws Exception {
		// TODO Auto-generated method stub
		return new ReturnMsg(false,"Error Format Not Suppoerted");
	}
	

	@Override
	public void toJsonStream(OutputStream os,boolean p_close) throws JSONException,IOException {
		if(encode == Encoding.MS950_HKSCS) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		super.toJsonStream(bos, true);
		byte[] bs = bos.toByteArray();
		bos.close();
		boolean isBig5 = false;
		for(byte b :bs) {
			if(isBig5) {
				if(b == 92) {
					os.write(b);
				}
				isBig5 = false;
			} else {
				if(b < 0) isBig5 = true;
			}
			os.write(b);
		}
			
		} else {
		super.toJsonStream(os, true);
		}
		os.flush();
		if(p_close) os.close();
	}	
	
	public void setNotifyCallback(PrtdocJsonChnftrRpcServlet.Callback cb) {
		prtdocJsonRpcservletCallback = cb;
	}
}
