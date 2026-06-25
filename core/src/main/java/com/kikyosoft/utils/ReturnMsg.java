package com.kikyosoft.utils;

/***
 * General result class return return boolean and string
 *
 */
public class ReturnMsg {
	final private Boolean statusFlag;
	final private String msg;
	final private Integer statusCode;
	final private Exception ex;
	private Object rtnData = null;
	private Boolean fatal = false;
	public final static ReturnMsg defaultOk = new ReturnMsg(true); //It is a shared value object. Do not modify the content of this object.
	public final static ReturnMsg defaultFail = new ReturnMsg(false);  //It is a shared value object. Do not modify the content of this object.
	
	public ReturnMsg(boolean p_statusFlag){
		statusFlag = p_statusFlag;
		msg = "";
		statusCode = null;
		ex = null;
	}
	public ReturnMsg(boolean p_statusFlag, String p_msg){
		statusFlag = p_statusFlag;
		msg = p_msg;
		statusCode = null;
		ex = null;
	}
	public ReturnMsg(boolean p_statusFlag, String p_msg,boolean p_fatal){
		statusFlag = p_statusFlag;
		msg = p_msg;
		statusCode = null;
		ex = null;
		fatal = p_fatal;
	}
	public ReturnMsg(boolean p_statusFlag, Exception p_ex){
		statusFlag = p_statusFlag;
		String tmpMsg = p_ex.getMessage();
		if (tmpMsg != null && tmpMsg.contains(": ")){
			//tmpMsg = tmpMsg.replaceFirst("^.*: ", "");
			tmpMsg = tmpMsg.replaceFirst("^[a-zA-Z.-_]*: ", "");  //fix when error msg contain multiple colon
		}
		msg = tmpMsg;
		ex = p_ex;
		statusCode = null;
	}
	public ReturnMsg(Exception p_ex){
		statusFlag = false;
		String tmpMsg = p_ex.getMessage();
		if (tmpMsg != null && tmpMsg.contains(": ")){
			//tmpMsg = tmpMsg.replaceFirst("^.*: ", "");
			tmpMsg = tmpMsg.replaceFirst("^[a-zA-Z.-_]*: ", "");  //fix when error msg contain multiple colon
		}
		msg = tmpMsg;
		ex = p_ex;
		statusCode = null;
	}
	public ReturnMsg(int p_statusCode, String p_msg){
		statusFlag = null;
		statusCode = p_statusCode;
		msg = p_msg;
		ex = null;
	}
	public ReturnMsg(boolean p_statusFlag, int p_statusCode, String p_msg){
		statusFlag = p_statusFlag;
		statusCode = p_statusCode;
		msg = p_msg;
		ex = null;
	}
	public ReturnMsg(boolean p_statusFlag, int p_statusCode, String p_msg,boolean p_fatal){
		statusFlag = p_statusFlag;
		statusCode = p_statusCode;
		msg = p_msg;
		ex = null;
		fatal = p_fatal;
	}
	
	public boolean getStatus(){
		if (statusFlag == null){
			LogUtil.log("statusFlag is null, assume false");
			return(false);
		}
		return(statusFlag.booleanValue());
	}
	public boolean isGood() {
		return(getStatus());
	}
	public boolean isBad() {
		return(!getStatus());
	}
	public boolean booleanValue(){
		return(getStatus());
	}
	
	public int getStatusCode(){
		if (statusCode == null){
			LogUtil.log("statusCode is null, assume -1");
			return(-1);
		}
		return(statusCode.intValue());
	}
	public String getMsg(){
		if (msg == null){
			LogUtil.log("msg is null, assume empty");
			return("");
		}
		else{
			return(msg);
		}
	}
	public Exception getEx(){
		return(ex);
	}
	
	@Override
	public String toString(){
		return(String.format("statusFlag:%s statusCode:%s msg:%s", statusFlag, statusCode, msg));
	}
	
	public static void main(String args[]){
		ReturnMsg rtnMsg;
		if ((rtnMsg = new ReturnMsg(false,123,"ok")).getStatus()){
			LogUtil.log(String.format("true %s", rtnMsg.getMsg()));
		}
		else{
			LogUtil.log(String.format("false %s", rtnMsg.getMsg()));
		}
		LogUtil.log("" + rtnMsg);
	}
	public ReturnMsg setData(Object p_data) {
		rtnData = p_data;
		return this;
	}
	public Object getData() {
		return(rtnData);
	}
	public void setFatal(boolean p_fatal) {
		fatal = p_fatal;
	}
	public boolean isFatal() {
		return(fatal);
	}
}
