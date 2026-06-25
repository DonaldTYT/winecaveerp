package com.uniinformation.bicore.erpv4;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.AttachmentUploadInterface;
import com.uniinformation.utils.BiMedia;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public abstract class BiResultMultiDoc extends BiResultErpv4 implements AttachmentUploadInterface {

	public BiResultMultiDoc(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	abstract String getMdocType() ;
	abstract int getMdocKey() ;
	/*
	protected String makeFilingKey( int p_messid) {
		return(null);
	}
	
	protected Wherecl makeQueryWherecl() {
		return(null);
	}
	*/
	public void saveImageFile( BiMedia media ) throws Exception {
//			RpcClient rpc = getSelectUtil().getRpcClient();

			RpcClient rpc = sh.getRpcClient();
			Value v = rpc.callSegment("getFilingMessageId",new Vector());
			rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				int cc = Integer.parseInt(v.toString().substring(4));
				    byte[] photoData = media.getByteData();
					Map<String, String> map = new HashMap<String, String>();
//					String filekey = String.format("jxOrdersFiling_%010d_%010d",getCellInt("inv_rg"),cc);  //add stirg to key
					String filekey = String.format("jxOrdersFiling_%010d_%010d",getMdocKey(),cc);  //add stirg to key
//					String filekey = makeFilingKey(cc);
					ByteArrayInputStream is = new ByteArrayInputStream(photoData);
				    FilingUtil.storeFile(
				      sh.getAgent(),
				      null,
				      filekey,
				      media.getName(),//mConditionPresetMapMap.customStoreName, 
				      "",//mConditionPresetMapMap.customStoreDesc, 
				    is);
				    is.close();

//				    String sfilekey = storeThumbnal(getBr().getCellInt("st_irg"), cc, photoData, map);
//					String thumbSize = map.get("data_size");
				    
				    TableRec tr = getSelectUtil().getQueryResult(
				         "select * from multidoc where mdoc_type = '"+getMdocType()+"' and mdoc_mrg = " + getMdocKey() + " order by mdoc_seq desc", null);
//				    TableRec tr = getSelectUtil().getQueryResult(
//				         "select * from multidoc ", makeQueryWherecl());
				    int seq = 0;
				    if(tr.getRecordCount() > 0) {
				        tr.setRecPointer(0);
				        seq = (Integer) tr.getField("mdoc_seq");
				        seq++;
				    }
				    if (tr.existField("mdoc_remark")) {
				    	getSelectUtil().executeUpdate("insert into multidoc (mdoc_type,mdoc_mrg,mdoc_seq,mdoc_drg,mdoc_ctime,mdoc_cuser,mdoc_doctype,mdoc_filekey,mdoc_sfilekey,mdoc_photosize,mdoc_thumbsize,mdoc_remark) values (?,?,?,?,?,?,?,?,?,?,?,?)", 
				    		new Wherecl()
				            .appendArgument(getMdocType())
				            .appendArgument(getMdocKey())
				            .appendArgument(seq)
				            .appendArgument(cc)
				            .appendArgument(DateUtil.dateToUnixtime(new java.util.Date()))
				            .appendArgument(sh.getLoginId())
				            .appendArgument(media.getContentType())
				            .appendArgument(filekey)
				            .appendArgument("")
				            .appendArgument("")
				            .appendArgument("")
				            .appendArgument(media.getName())
				            );
				    } else {
				    	getSelectUtil().executeUpdate("insert into multidoc (mdoc_type,mdoc_mrg,mdoc_seq,mdoc_drg,mdoc_ctime,mdoc_cuser,mdoc_doctype,mdoc_filekey,mdoc_sfilekey,mdoc_photosize,mdoc_thumbsize) values (?,?,?,?,?,?,?,?,?,?,?)", 
				    		new Wherecl()
				            .appendArgument(getMdocType())
				            .appendArgument(getMdocKey())
				            .appendArgument(seq)
				            .appendArgument(cc)
				            .appendArgument(DateUtil.dateToUnixtime(new java.util.Date()))
				            .appendArgument(sh.getLoginId())
				            .appendArgument(media.getContentType())
				            .appendArgument(filekey)
				            .appendArgument("")
				            .appendArgument("")
				            .appendArgument("")
				            );
				    }
				    
				    //patch extraimg field
			}
	}

}
