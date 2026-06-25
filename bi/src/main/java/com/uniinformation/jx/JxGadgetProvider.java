package com.uniinformation.jx;
import java.util.*;
import java.net.InetAddress;

import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;

import java.io.*;

public abstract class JxGadgetProvider
{
//	static JxGadgetProvider provider;
	protected Hashtable formlist = new Hashtable();
	Hashtable userdata = new Hashtable();
	Hashtable jxCellLockList = null;
	/*
	 * 250821 to be add later
	private WxUser wxuser;
	*/
	
	

	protected void finalize()
	{
		UniLog.log("finalize JxGadgetProvider");
		providerCleanUp();
	}
	public void providerCleanUp()
	{
		UniLog.log("JxGadgetProvider CleanUp");
		if (formlist != null) { //andrew220811 fix formlist null exception
			Enumeration jxformlist = formlist.elements();
			while(jxformlist.hasMoreElements()) {
				((JxForm) jxformlist.nextElement()).cleanup();
			}
		}
		formlist = null;
		userdata = null;
	}
//	public static synchronized JxGadgetProvider getProvider()
//	{
//		return(provider);
//	}
//	public static synchronized void setProvider(JxGadgetProvider p)
//	{
//		provider = p;
//	}
	public void setUserData(String p_name,Object p_object)
	{
		if(userdata == null) return;
		if(p_object != null) {
			if(p_name == null || p_object == null || userdata == null) return;
			userdata.put(p_name,p_object);
		} else {
			userdata.remove(p_name);
		}
	}
	public Object getUserData(String p_name)
	{
		if(userdata == null) {
			UniLog.log("ERROR 2018 userdata in JxGadgetProvider is null");
			return(null);
		} else return(userdata.get(p_name));
	}
	public void jxRegisterForm(String p_instanceName,JxForm p_form,JxSkin p_skin)
	{
		/* 2003/04/14 wing change order
		p_form.bind(p_skin);
		formlist.put(p_instanceName,p_form);				
		*/
		formlist.put(p_instanceName,p_form);				
		p_form.bind(p_skin);
	}
	public void jxUnRegisterForm(String p_instanceName)
	{
		JxForm jxform;
		UniLog.log("UnRegisterForm " + p_instanceName);
		if((jxform = (JxForm) formlist.get(p_instanceName)) != null) {
			jxform.cleanup();
			formlist.remove(p_instanceName);
		}
	}
	public JxForm jxGetForm(String p_name)
	{
		if(formlist == null) return(null);
		return((JxForm)formlist.get(p_name));
	}

	/*
	 * 250821 to be add later
	public void setWxUser(WxUser p_wxuser)
	{
		wxuser = p_wxuser; 
	}

	public WxUser getWxUser()
	{
		return(wxuser);
	}
	*/
	public static JxGadgetProvider getProvider()
	{
		return(null);
	}

	public boolean checkJxCellOverridable(JxField p_field) {
		return(true);
	}
//	public boolean checkJxCellOverridable(JxField p_field) 250821 removed
//	{
//		Hashtable ht;
//		if(jxCellLockList == null) {
//			jxCellLockList = new Hashtable();
//			FileInputStream fs = null;
//			String fname;
//			if(wxuser == null) return(true);
//			fname = "/yic/v/unidev/wxcache/jxCellLockList."+wxuser.getLoginName();
//			try {
//				UniLog.log("reading jxCellLockList " + fname);
//				fs = new FileInputStream(fname);
//			} catch (Exception ex) {
//				fname = "/yic/v/unidev/wxcache/jxCellLockList.default";
//				try {
//					UniLog.log("reading jxCellLockList " + fname);
//					fs = new FileInputStream(fname);
//				} catch (Exception ex2) {
//					UniLog.log("reading jxCellLockList failed");
//				}
//			}
//			if(fs != null) {
//				BufferedReader brdr = 
//					new BufferedReader(new InputStreamReader(fs));
//				try {
//					String ln;
//					for(;;) {
//						ln = brdr.readLine();
//						if(ln == null) break;
//						UniLog.log("addd jxCellLockList " + ln);
//						jxCellLockList.put(ln,ln);
//					}
//				} catch (Exception ex) {
//					UniLog.log(ex);
//				}
//			}
//		}
//		String s = p_field.getJxForm().getSkinClass()+"."+p_field.getName();
//		//UniLog.log("checkJxCellOverridable " + s);  //andrew220411 too much log for kanghong
//		if(jxCellLockList.get(s) != null) return(false);
//		return(true);
//	}




	public abstract String jxOpenDialog(String p_dir,String p_filter);
	public abstract String jxSaveDialog(String p_name);
	public abstract void jxNotifyMsg(String p_msg,int p_type,int p_dur);
	public abstract void jxMessageBox(String p_msg,int p_type,MessageBoxActionInterface p_action);
	public abstract int jxMessageBoxWithReturn(Vector v);
	public abstract boolean jxConfirm(String p_msg,MessageBoxActionInterface p_action);
	public abstract JxSkinElement jxMenuItem(String p_fieldname);
	public abstract JxSkinElement jxNewTreeNode();
	public abstract void runApp(Object p_form);
	public abstract JxSkinElement jxImageList();
	public abstract String jxCreateForm(String p_formName,String p_instanceName,String p_dfm);
	public abstract void jxDestroyForm(String p_instanceName);
	public abstract void jxModalForm(String p_instanceName);
	public abstract InetAddress jxRemoteAddress();
	public abstract void delayClick(String p_instanceName,String p_buttonname);
//	public abstract String getCurFocusForm();
//	public abstract String getCurFocusField();
	public abstract int jxGetFile(DataOutput output,String p_filename);
	public abstract int jxGetFile(OutputStream output,String p_filename);
	public abstract int jxGetFile(OutputStream output,String p_filename,int p_kbyte,JxUpdateProgress p_interface);
	public abstract int jxPutFile(DataInput output,String p_filename);
	public abstract int jxPutFile(InputStream output,String p_filename);
	public abstract int jxPutFile(InputStream output,String p_filename,int p_kbyte,JxUpdateProgress p_interface);
	public abstract String jxGetTempFile(String p_prefix);
	public abstract int jxUnlink(String p_path);
	public abstract int jxShellExecute(String p_operation,String p_path,String p_parameter,String p_dir);
	public abstract String jxNewTempDir(String p_prefix);
	public abstract int jxMakeDir(String p_path);
	public abstract int jxRemoveDir(String p_path);
	public abstract int jxChnftr(Vector v);
	public abstract int jxSaveFileToTif(String p_imgfile,String p_imgtype,String p_tiffname,String p_append_or_write,String p_colormode,int resolution);
	public abstract void jxLoadDfm(String p_formName, String p_dfmFile) throws Exception;
	public abstract void jxLoadDfm(String p_formName, String p_fieldname,String p_dfmFile) throws Exception;
	public abstract void setDebug(boolean p_sw);
	public abstract long jxGetFileSize(String p_path);
	public abstract void jxTranslateMessage();
	public abstract RpcServerConnection getConn();
	public abstract String getSessionLabel();
	
	public abstract JxForm getOrCreateForm(String p_formName);
	public abstract void editHint(JxForm p_form,JxField p_field);
	public abstract void putChnCache(String p_fname,int p_mode);
	public abstract void putWxCache(String p_fname,int p_mode);
	public abstract String getClientWxCacheDir();
	public abstract String getClientWxTmpDir();
	public abstract String getClientChnCacheDir();
	public abstract void removeWxCache(String p_cacheFile);
	
	public abstract String getLoginId();
	public abstract RpcClient getRpcClient();
	public abstract InputStream erpFileInputStream(String p_filename) throws Exception;
	public abstract OutputStream erpFileOutputStream(String p_filename) throws Exception;
	public abstract Object getSessionObject(String p_key) ;
	public abstract Properties loadProperty(String p_propfile);
	public abstract InputStream getResourceAsStream(String p_str);
}


