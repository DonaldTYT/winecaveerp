package com.uniinformation.zkbi.wc;



import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.Filedownload;

import com.kyoko.common.DateUtil;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;


public class PrintBarcode {
	static public enum LABEL_TYPE{
			STOCK_ITEM,
	};	
	boolean isFileDownload = false;
	DataOutputStream dos = null;
	ByteArrayOutputStream bos=null;
	Socket socket=null;
	public PrintBarcode(String Devid) throws Exception {
		if(Devid.equals("FILE")) {
			bos = new ByteArrayOutputStream();
			dos = new DataOutputStream(bos);
			isFileDownload = true;
		} else {
			DeviceControl.DevHandler dhdr = DeviceControl.getDevHandler(Devid);
			if(dhdr != null) {
				socket = dhdr.getOrConnectSocket();
				if(socket != null) {
					dos = new DataOutputStream(socket.getOutputStream());
				} else throw new Exception ("Printer Connect Fail");
			} else throw new Exception ("Printer Not Ready");
		}
	}
	
	public void printOne(LABEL_TYPE p_type,CellCollection p_col,int p_count,boolean p_iscase) throws Exception {
		switch(p_type) {
		case STOCK_ITEM:
			UniLog.log1("Print one Label: %s", p_col.getCellString("st_icode"));
			dos.writeBytes("SIZE 76.9 mm, 50.8 mm\n");
			dos.writeBytes("GAP 2 mm, 0 mm\n");
			dos.writeBytes("DIRECTION 0,0\n");
			dos.writeBytes("REFERENCE 0,0\n");
			dos.writeBytes("OFFSET 0 mm\n");
			dos.writeBytes("SET PEEL OFF\n");
			dos.writeBytes("SET CUTTER OFF\n");
			dos.writeBytes("SET PARTIAL_CUTTER OFF\n");
			dos.writeBytes("SET TEAR ON\n");
			dos.writeBytes("CLS\n");
			dos.writeBytes("CODEPAGE UTF-8\n");
//			dos.write(String.format("QRCODE 50,50,L,10,A,0,M2,S7,\"%s\"\n",escStr(p_col.getCellString("st_icode"))).getBytes("UTF-8"));
			
			{
				String bc = "";
				if(p_iscase) bc += "1"; else bc += "2";
				bc += String.format("%06d%06d000", p_col.getCellInt("st_irg"),p_col.getCellInt(("pdlbs_org")));
				dos.write(String.format("QRCODE 50,50,L,10,A,0,M2,S7,\"%s\"\n",bc).getBytes("UTF-8"));
			}
			dos.write(String.format("TEXT 350,50,\"ROMAN.TTF\",0,12,12,\"%s\"\n",escStr(p_col.getCellString("st_icode"))).getBytes("UTF-8"));

			List<String> listStr = ChnftrParser.splitText(p_col.getCellString("or_cocode")+" "+ p_col.getCellString("vd_vname"),"helv_nr","chinese",(float) 12.0,400);
			UniLog.log(listStr.toString());
			for(int i=0;i<3;i++) {
				if(listStr.size() <= i) break;
				String ss = listStr.get(i);
				dos.write(String.format("TEXT 50,%d,\"ROMAN.TTF\",0,10,10,\"%s\"\n",280+i*60,escStr(ss)).getBytes("UTF-8"));
			}
			
			listStr = ChnftrParser.splitText(p_col.getCellString("st_iname"),"helv_nr","chinese",(float) 12.0,400);
			UniLog.log(listStr.toString());
			for(int i=0;i<3;i++) {
				if(listStr.size() <= i) break;
				String ss = listStr.get(i);
				dos.write(String.format("TEXT 50,%d,\"ROMAN.TTF\",0,10,10,\"%s\"\n",400+i*60,escStr(ss)).getBytes("UTF-8"));
			}
			
			dos.write(String.format("TEXT 350,120,\"ROMAN.TTF\",0,10,10,\"%s\"\n",escStr(
					String.format("%dx%d %s", 
					(int) p_col.getCellDouble("st_msize1"),
					(int) p_col.getCellDouble("st_msize2"),
					p_col.getCellString("stpk_packing")
					)
					)).getBytes("UTF-8"));
			dos.write(String.format("TEXT 350,190,\"ROMAN.TTF\",0,10,10,\"%s\"\n",escStr(
						String.format("%s %s", 
						p_col.getCellString("or_ocode"),
						DateUtil.toDateString(p_col.getCell("or_date").getDate(),"dd/mm/yyyy")
						)
						)).getBytes("UTF-8"));
			dos.write(String.format("PRINT 1,%d\n",p_count).getBytes("UTF-8"));
			dos.flush();
			break;
		}
	}
	
	public void close() throws Exception {
		if(isFileDownload) {
			Filedownload.save(bos.toByteArray(), "application/text", "barcode.pbn");
		} else {
			dos.flush();
//			Thread.sleep(5000);
		}
		cleanup();
	}
	
	void cleanup() {
		try {
			if(socket != null) {
				socket.close();
				socket = null;
			}
			if(dos != null) {
				dos.close();
				dos = null;
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	protected void finalize()
	{
		cleanup();
	}
	private static String escStr(String p_str) {
		if (StringUtils.isBlank(p_str)){
			return "";
		}
		return p_str.replace("\"", "\\[\"]");
	}
}
