package com.uniinformation.scorpion.driver;
import com.kyoko.common.CoreLog;

public class ScpField{
	static final public int FDTYPE_CHAR    = 0;
	static final public int FDTYPE_INTEGER = 2;
	static final public int FDTYPE_FLOAT   = 3;
	static final public int FDTYPE_SERIAL  = 6;
	static final public int FDTYPE_DATE    = 7;
	int fdtype;
	int fdlen;
	String fdname;
	String tabname;
	String colname;
	public ScpField(String p_fdname,int p_type,int p_length) throws Exception {
		switch(p_type) {
		case FDTYPE_CHAR    : 
		case FDTYPE_INTEGER :
		case FDTYPE_FLOAT   :
		case FDTYPE_SERIAL  :
		case FDTYPE_DATE    : 
//								 if(p_length <= 0 || p_length > 4096) {
								 if(p_length > 4096) {
									throw new Exception("SCP String Field Too Long " + p_length);
								 }
								 fdname = p_fdname;
								 fdtype = p_type;
								 fdlen  = p_length == 0 ? 10 : p_length;
								 int i = fdname.indexOf('.');
								 if (i < 0) {
								    tabname = null;
									 colname = fdname;
								 }
								 else {
								    tabname = fdname.substring(0, i);
									 colname = fdname.substring(i+1);
								 }
								 break;
		default : throw new Exception("SCP Invalid Field Type" + p_type);
		}
	}
	public String getTabname()
	{
		return(tabname);
	}
	public String getColname()
	{
		return(colname);
	}
	public String getFdname()
	{
		return(fdname);
	}
	public int getFdlen()
	{
		return(fdlen);
	}
	public int getFdtype()
	{
		return(fdtype);
	}
	static public void main(String[] arg)
	{
		try {
			new ScpField("hello",ScpField.FDTYPE_CHAR,10);
		} catch (Exception e) {
				CoreLog.log(e);
			}
	}
}
