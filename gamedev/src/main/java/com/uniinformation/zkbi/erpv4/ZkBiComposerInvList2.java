package com.uniinformation.zkbi.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellFormula;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerInvList2 extends ZkBiComposerBase {
	private final static boolean delayFlag = false;
	@Override
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		hasAUDColumn=false;
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	
	@Override
    protected void setupExportButton(final BiResult result)
	{
		Button btnPrintLabel;
		if(!result.allowUpdate()) return;
    	if(masterWin.hasFellow("btPrintLabel")) {
    		btnPrintLabel = (Button) masterWin.getFellow("btPrintLabel");
    	} 
    	else {	
	        btnPrintLabel = new ZkBiButton();
	        btnPrintLabel.setLabel("Print Label");
	        btnPrintLabel.setId("btPrintLabel");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btnPrintLabel, "fa-print");
    	} 
    	
        btnPrintLabel.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		UniLog.log("HAHA");
             		final java.util.Set selection = listModelList.getSelection();
            		RpcClient rpc = sessionHelper.getRpcClient();
        			Vector args = new Vector();
//        			args.add(DateUtil.now());
        			String custCode = null;
        			String ordType= null;
        			String doViewName = null;
        			if(selection.size() <= 0) {
      					Messagebox.show(
   							"Please Select Items To Print",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
        			
        			int itemCnt=0;
        			int labelCnt=0;
               		for(Iterator it=selection.iterator();it.hasNext();) {
            				Object o = it.next();
            				int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
            				result.loadOneRecV(idx);
            				itemCnt++;
            				labelCnt += result.getCellInt("pds_stockqty");
            				
               		}	
	        		final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/dialogPrintLabel.zul");
	        		
	        		final CellCollection col = new CellCollection();

	        		Cell c0 = null;
	        		c0 = new Cell(itemCnt);
	        		c0.setMode(Cell.VMODE_DISPONLY);
	        		col.addCell("dplb_itemcnt", c0);

	        		c0 = new Cell(0);
	        		c0.setItemList(
	        					new VectorUtil()
	        					.addElement("As Stock Qty")
	        					.addElement("Manual Input")
	        					.toVector()
	        				);
	        		c0.set(1);
	        		col.addCell("dplb_qtymode", c0);

	        		c0 = new Cell(0);
	        		col.addCell("dplb_qtyperitem", c0);
	        		c0.setFormula(new CellFormula("if(dplb_qtymode == 0,0,ignored())",col));
	        		
	        		c0 = new Cell(0);
	        		c0.setMode(Cell.VMODE_DISPONLY);
	        		col.addCell("dplb_labelcnt", c0);
	        		c0.setFormula(new CellFormula("if(dplb_qtymode == 0,"+labelCnt+",dplb_itemcnt * dplb_qtyperitem)",col));
	        		
	        		c0 = new Cell(0);
	        		c0.setItemList(
	        					new VectorUtil()
	        					.addElement("Save to File")
	        					.addElement("Barcode Printer")
	        					.toVector()
	        				);
	        		c0.set(1);
	        		col.addCell("dplb_output", c0);
	        		
	        			zkf1.doModal(col,new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									if(arg0.getTarget().getId().equals("btOK")) {
										
										zkf1.exitModal();
										int fixQty ;
										if(col.getCell("dplb_qtymode").getInt() == 1) {
											fixQty = col.getCell("dplb_qtyperitem").getInt();
										} else {
											fixQty = -1;
										}
										
										DataOutputStream dos;
										Socket socket=null;
										ByteArrayOutputStream bos=null;
										if( col.getCell("dplb_output").getInt() == 0) {
											bos = new ByteArrayOutputStream();
											dos = new DataOutputStream(bos);
										} else {
											String ptrDev = Erpv4Config.getString(getSessionHelper(), "LabelPrinter1");
											if(ptrDev == null) ptrDev = "Dev_01";
//											DeviceControl.DevHandler dhdr = DeviceControl.getDevHandler("Dev_01");
											DeviceControl.DevHandler dhdr = DeviceControl.getDevHandler(ptrDev);
											if(dhdr != null) {
//												UniLog.log1("Print Barcode to "+ dhdr.getDevip() + " : " + dhdr.getDevport());
//												socket = new Socket(dhdr.getDevip(), dhdr.getDevport());
												socket = dhdr.getOrConnectSocket();
												if(socket != null) {
													if (delayFlag) Thread.sleep(500);
													dos = new DataOutputStream(socket.getOutputStream());
												} else return;
											} else return;
										}
										
										
										
										
										
										for(Object obj : selection) {
											int idx = getTrIdxByObj(listModelList, obj);
											if (idx < 0) {
												UniLog.log1("invalid idx: %d", idx);
												continue;
											}
											result.loadOneRecV(idx);
											int qty;
											if(fixQty >= 0) {
												qty = fixQty; 
											}
											else {
												qty = result.getCellInt("pds_stockqty");
											}
											if(qty <= 0) {
												qty = 1;
											}
											
											UniLog.log1("Print one Label: %s", result.getCellString("st_icode"));
											printOneLabel_1( dos, result, qty);

//											dos.writeBytes("SIZE 48 mm, 30.0 mm\n");
//											dos.writeBytes("GAP 2 mm, 0 mm\n");
//											dos.writeBytes("DIRECTION 0,0\n");
//											dos.writeBytes("REFERENCE 0,0\n");
//											dos.writeBytes("OFFSET 0 mm\n");
//											dos.writeBytes("SET PEEL OFF\n");
//											dos.writeBytes("SET CUTTER OFF\n");
//											dos.writeBytes("SET PARTIAL_CUTTER OFF\n");
//											dos.writeBytes("SET TEAR ON\n");
//											
//											dos.writeBytes("CLS\n");
//											dos.writeBytes("CODEPAGE UTF-8\n");
//											/*
//											public static List<String> splitText(String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
//												return new TextSpliter(text, engFontFace, chnFontFace, fontSize, chnftrWidth).getResultList();
//											}
//											*/
//	
//											dos.write(String.format("QRCODE 20,50,L,6,A,0,M2,S7,\"%s\"\n",escStr(result.getCellString("st_icode"))).getBytes("UTF-8"));
//											dos.write(String.format("TEXT 20,220,\"ROMAN.TTF\",0,10,10,\"%s\"\n",escStr(result.getCellString("st_icode"))).getBytes("UTF-8"));
//											{
//												List<String> listStr = ChnftrParser.splitText(result.getCellString("st_iname"),"helv_nr","chinese",(float) 10.0,150);
//												UniLog.log(listStr.toString());
//												for(int i=0;i<3;i++) {
//													if(listStr.size() <= i) break;
//													String ss = listStr.get(i);
//													dos.write(String.format("TEXT 200,%d,\"ROMAN.TTF\",0,10,10,\"%s\"\n",50+i*60,escStr(ss)).getBytes("UTF-8"));
//												}
//											}
//											//dos.write(String.format("TEXT 350,50,\"DFFN.TTF\",0,12,12,\"%s\"\n",escStr(result.getCellString("st_iname"))).getBytes("UTF-8"));
//											//dos.write(String.format("TEXT 350,250,\"DFFN.TTF\",0,12,12,\"%s\"\n",escStr(result.getCellString("st_mszrange"))).getBytes("UTF-8"));
//											//dos.write(String.format("TEXT 350,350,\"ROMAN.TTF\",0,12,12,\"%s\"\n",escStr(DateUtil.toDateString(result.getCell("stm_date").getDate(),"dd/mm/yyyy"))).getBytes("UTF-8"));
//											dos.write(String.format("PRINT 1,%d\n",qty).getBytes("UTF-8"));
											
											dos.flush();
											if( col.getCell("dplb_output").getInt() != 0) {
												if (delayFlag) Thread.sleep(1000);
											}
										}	

										if( col.getCell("dplb_output").getInt() == 0) {
											dos.close();
											Filedownload.save(bos.toByteArray(), "application/text", "barcode.pbn");
										} 
										else {
											dos.flush();
											if (delayFlag) Thread.sleep(2000);
											socket.close();
											dos.close();
										}
					
               		
               		
									}
									if(arg0.getTarget().getId().equals("btCancel")) {
										zkf1.exitModal();
									}
								}
	        				}
	        			);
	        		
            	}
        	}
        );
        
        setupBatchModeButton(btnPrintLabel);
	}
	private static String escStr(String p_str) {
		if (StringUtils.isBlank(p_str)){
			return "";
		}
		return p_str.replace("\"", "\\[\"]");
	}
	
	private void printOneLabel_1( DataOutputStream dos, BiResult result, int qty) throws IOException {
	dos.writeBytes("SIZE 48 mm, 25.0 mm\n");
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
	/*
public static List<String> splitText(String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
return new TextSpliter(text, engFontFace, chnFontFace, fontSize, chnftrWidth).getResultList();
}
	*/

	dos.write(String.format("QRCODE 20,40,L,4,A,0,M2,S7,\"%s\"\n",escStr(result.getCellString("st_icode"))).getBytes("UTF-8"));
	dos.write(String.format("TEXT 20,150,\"ROMAN.TTF\",0,10,10,\"%s\"\n",escStr(result.getCellString("st_icode"))).getBytes("UTF-8"));
	{
		List<String> listStr = ChnftrParser.splitText(result.getCellString("st_iname"),"helv_nr","chinese",(float) 10.0,100);
		UniLog.log(listStr.toString());
		for(int i=0;i<3;i++) {
			if(listStr.size() <= i) break;
			String ss = listStr.get(i);
			dos.write(String.format("TEXT 200,%d,\"ROMAN.TTF\",0,10,10,\"%s\"\n",40+i*40,escStr(ss)).getBytes("UTF-8"));
		}
	}
	//dos.write(String.format("TEXT 350,50,\"DFFN.TTF\",0,12,12,\"%s\"\n",escStr(result.getCellString("st_iname"))).getBytes("UTF-8"));
//	dos.write(String.format("TEXT 350,250,\"DFFN.TTF\",0,12,12,\"%s\"\n",escStr(result.getCellString("st_mszrange"))).getBytes("UTF-8"));
//	dos.write(String.format("TEXT 350,350,\"ROMAN.TTF\",0,12,12,\"%s\"\n",escStr(DateUtil.toDateString(result.getCell("stm_date").getDate(),"dd/mm/yyyy"))).getBytes("UTF-8"));
	dos.write(String.format("PRINT 1,%d\n",qty).getBytes("UTF-8"));	
	}
}
