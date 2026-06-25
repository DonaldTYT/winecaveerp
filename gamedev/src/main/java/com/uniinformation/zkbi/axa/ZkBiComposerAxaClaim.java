package com.uniinformation.zkbi.axa;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailAttachment;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.axa.AxaUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.axa.BiResultAxaClaim;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellFormula;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.clinic.DispenseUpload;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.EmailUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZipUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.EmailUtil.SecMode;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;


public class ZkBiComposerAxaClaim extends ZkBiComposerBase {
	static public final String CLAIM_FFMT_1 = "PEDD_05 24_GI.txt";
	static public final String CLAIM_FFMT_04 = "PEDD_05 24_CR.txt";
	static public final String CLAIM_FFMT_7 = "PEDD_05 24_CP.txt";
	

	/*
	void writeOneClaimRecord(HashMap<String,Object> p_dataHM, BiCellCollection bc) throws Exception {
		String id = "";
		if (StringUtils.startsWithAny(bc.getCellString("axaclm_phno"), "1")) {
			id = "1";
		}
		if (StringUtils.startsWithAny(bc.getCellString("axaclm_phno"), "0","4")) {
			id = "04";
		}
		if (StringUtils.startsWithAny(bc.getCellString("axaclm_phno"), "7")) {
			id = "7";
			UniLog.log1("ignore id is 7");
			return;
		}
		
		
		BufferedWriter bw = (BufferedWriter)p_dataHM.get("bw" + id);
		if (bw == null) {
			UniLog.log1("ignore bw not available");
			return;
		}
		
		Date minDate = (Date) p_dataHM.get("minDate" + id);
		Date maxDate = (Date) p_dataHM.get("maxDate" + id);
		Date curDate = bc.getCell("axaclm_date").getDate();
		if (curDate != null && (minDate == null || curDate.compareTo(minDate) < 0)) {
			p_dataHM.put("minDate" + id, curDate);
		}
		if (curDate != null && (maxDate == null || curDate.compareTo(maxDate) > 0)) {
			p_dataHM.put("maxDate" + id, curDate);
		}
		String output = 
		String.format("%-6s-%7s      %4d                              %-30s  %8s%-7s%-20s%11s%11s%11s%-8s        %-8s%-5s%-41s%-8s",
						bc.getCellString("axaclm_phno"),
						bc.getCellString("axaclm_certno"),
						bc.getCellInt("axaclm_dependno"),
						bc.getCellString("axacvp_covername"),
						DateUtil.toDateString(bc.getCell("axaclm_date").getDate(), "yyyymmdd"),
						bc.getCellString("axaclm_voucher"),
						bc.getCellString("axacld_treatment"),
                        StringUtil.ftostr(bc.getCellDouble("axacld_totalamt"),"#######0.00"),
                        StringUtil.ftostr(bc.getCellDouble("axacld_axaamt"),"#######0.00"),
                        StringUtil.ftostr(bc.getCellDouble("axacld_copaidamt"),"#######0.00"),
						bc.getCellString("axaclm_diagnosis"),
						"",
						"",
						bc.getCellString("cldoc_name"),
						""
                        );
		bw.write(output);
		bw.newLine();
	}
	*/
	@Override
    protected void setupExtraButton(final BiResult result)
	{
		super.setupExtraButton(result);
		if(getSessionHelper().hasAccessRight("#clearcache")) {
	        Button btnClearCache = new ZkBiButton();
	        btnClearCache.setLabel("Clear EDI Cache");
	        btnClearCache.setId("btnClearCache");
	        abHelper.addButton(btnClearCache, "fa-user");
	        btnClearCache.addEventListener("onClick",
		        	new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							BiResultAxaClaim.clearCache();
						}
	      			}
		        );	
			
		}
		if(getSessionHelper().hasAccessRight("#claimstmt")) {
	        Button btnStmt = new ZkBiButton();
	        btnStmt.setLabel("Generate Statement");
	        btnStmt.setId("btnStmt");
	        abHelper.addButton(btnStmt, "fa-user");
	        btnStmt.addEventListener("onClick",
		        	new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {

							final ZkForm zkf1 = new ZkForm(null,"zkf/axa/CreateClaimStatement.zul");
									final CellCollection col = new CellCollection();
									col.addCell("fromdate", new Cell(DateUtil.today()));
									col.addCell("todate", new Cell(DateUtil.today()));
									try {
										zkf1.doModal(col,new EventListener() {
											@Override
											public void onEvent(Event arg0) throws Exception {
													if(arg0.getTarget().getId().equals("btCancel")) {
														zkf1.exitModal();					
														return;
													}
													if(arg0.getTarget() instanceof Button) {
														Date d0 = col.getCell("fromdate").getDate();
														Date d1 = col.getCell("todate").getDate();
														String emailAddrs=null;
														if(DateUtil.minDate.after(d0) ||
																DateUtil.minDate.after(d1) ||
																d0.after(d1)
																) {
															Messagebox.show("From Date and To Date Cannot be Empty and To Date should be >= From Date");
															return;
														}
														if(arg0.getTarget().getId().equals("btSendMail")) {
															emailAddrs = col.getCellString("emailAddrs");
															if(StringUtils.isBlank(emailAddrs)) {
																Messagebox.show("Email Address Cannot be Empty");
																return;
															}
														}
														BiResult stmtBr = result.getView().getSchema().getViewByName(result.getView().getName()).newBiResult(getSessionHelper().getLoginId(), null, null, getSessionHelper());
														stmtBr.addCustomCondition(
																String.format("axaclm_status='Submitted' and axaclm_submitdate between '%s' and '%s'",
																			DateUtil.toDateString(d0, "yyyy/mm/dd"),
																			DateUtil.toDateString(d1, "yyyy/mm/dd")
																		)
																);
														stmtBr.query();
														byte[] bos = ((BiResultAxaClaim) stmtBr).exportStatementToExcel(DateUtil.today(),null);
														if(arg0.getTarget().getId().equals("btDownload")) {
//							Filedownload.save(bos, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "AxaStatement");	
															Filedownload.save(bos, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
																		String.format("Clame_Statement_%s_%s",
																			DateUtil.toDateString(d0, "yyyymmdd"),
																			DateUtil.toDateString(d1, "yyyymmdd")
																			)
																	);	
														}
														if(arg0.getTarget().getId().equals("btSendMail")) {
															AxaUtil.sendEmailRemote(
																	String.format("Clame Statement from PEDD submitted on %s - %s",
																			DateUtil.toDateString(d0, "yyyy/mm/dd"),
																			DateUtil.toDateString(d1, "yyyy/mm/dd")
																			),
																	String.format("Clame_Statement_%s_%s.xlsx",
																			DateUtil.toDateString(d0, "yyyymmdd"),
																			DateUtil.toDateString(d1, "yyyymmdd")
																			)
																	, bos,emailAddrs);
														}
														stmtBr.getSelectUtil().close();
														zkf1.exitModal();					
													}
											}
										});
									} catch (Exception ex) {
										UniLog.log(ex);
									}
		
//							String outFileName = "c:\\tmp\\axastmt.xlsx";

							//String outFileName = "/tmp/axastmt.xlsx";
							//byte[] bos = ((BiResultAxaClaim) result).exportStatementToExcel(DateUtil.today(),outFileName);
							//Filedownload.save(bos, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "AxaStatement");	
							
							
						}
	      			}
		        );	
		}
		if(!getSessionHelper().hasAccessRight("#submitclm")) return;
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbSubmitClaim","Submit Claim","fa-user",
				new BiActionHandler(this) {
					HashMap<String,Object> dataHM = new HashMap();
					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						try {
							dataHM.clear();
							for (String id : Arrays.asList("1","04","7")) {
								OutputStream os = new ByteArrayOutputStream();
								dataHM.put("os" + id, os);
								dataHM.put("bw" + id, new BufferedWriter(new OutputStreamWriter(os,"ISO-8859-1")));
							}
							return(ReturnMsg.defaultOk);
						} catch (Exception ex) {
							UniLog.log(ex);
							CloseUtil.close(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
							return(new ReturnMsg(false,ex.toString()));
						}
					}
					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							boolean ok = result.fetchOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
							Vector<BiCellCollection> vv = result.getSubLink("axa.AxaClaimDet").getRowCollectionList();
							for(BiCellCollection bc : vv)		 {
								((BiResultAxaClaim) result).writeOneClaimRecord(dataHM,bc);
							}
							return(ReturnMsg.defaultOk);
						} catch (Exception ex) {
							UniLog.log(ex);
							CloseUtil.close(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
							return(new ReturnMsg(false,ex.toString()));
						}
					}
					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						try {
							CloseUtil.flush(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
							
							int okCnt = 0;
							int failCnt = 0;
							for (String id : Arrays.asList("1","04","7")) {
								String outFileName = null;
								try {
									outFileName = (String) ZkBiComposerAxaClaim.class.getDeclaredField("CLAIM_FFMT_" + id).get(null);
								}
								catch(Exception ex) { }
								
								if (outFileName == null) {
									UniLog.log1("outFileName not avialble. id:%s",id);
									continue;
								}
								
								ByteArrayOutputStream os = (ByteArrayOutputStream) dataHM.get("os" + id);
								byte[] ba = os.toByteArray();
								if (ba.length <= 0) {
									continue;
								}
								Date minDate = (Date) dataHM.get("minDate" + id);
								Date maxDate = (Date) dataHM.get("maxDate" + id);
								String minDateStr = minDate == null ? "" : DateUtil.toDateString(minDate, "yyyymmdd");
								String maxDateStr = maxDate == null ? "" : DateUtil.toDateString(maxDate, "yyyymmdd");
								
								//ReturnMsg sendRtn = AxaUtil.sendEmail(String.format("UAT Claims File from PEDD on %s-%s",minDateStr,maxDateStr), outFileName, ba);
								ReturnMsg sendRtn = AxaUtil.sendEmailRemote(String.format("Claims File from PEDD on %s-%s",minDateStr,maxDateStr), outFileName, ba);
								UniLog.log1("sendRtn:" + sendRtn);
								if (sendRtn.getStatus()) okCnt++; else failCnt++;
							}
							ZkUtil.normMsg("Email submit ok:%d fail:%d",okCnt, failCnt);
							return(ReturnMsg.defaultOk);
						} 
						catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false,ex.toString()));
						} 
						finally {
							CloseUtil.close(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
						}
					}
				}
			);
	}
}
