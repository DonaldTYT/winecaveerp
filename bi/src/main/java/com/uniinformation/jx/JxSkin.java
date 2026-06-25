package com.uniinformation.jx;

import java.util.*;

import com.uniinformation.utils.*;

public abstract class JxSkin
{
	//	protected JxGadgetProvider gadgetProvider;
	Hashtable fields = new Hashtable();

	protected String jxskinname;
	protected String jxskinclass;
	protected String instanceName;

	public JxSkin()
	{
	}
	public JxGadgetProvider getGadgetProvider()
	{
		return(JxGadgetProvider.getProvider());
	}
	protected void finalize()
	{
		fields.clear();
		fields = null;
		jxskinname = null;
	}
	public JxSkinElement getField(String p_fieldname)
	{
		return((JxSkinElement)fields.get(p_fieldname));
	}
	public void addField(String p_fieldname,JxSkinElement p_field)
	{
		fields.put(p_fieldname,p_field);
	}
	public Enumeration getFields()
	{
		return(fields.elements());
	}
	public String getSkinClass()
	{
		return(jxskinclass);
	}
	public abstract void enableForm();
	public abstract void disableForm();
	public abstract void showForm();
	public abstract void hideForm();
	public abstract boolean isFormVisible();
	/*
	public abstract void modalForm(Object p_owner);
	*/
	public abstract void modalForm();
	public abstract void modalFormWithCallback(String p_focusfield);
	public abstract Object getControl();
	public abstract void closeForm();
//	public abstract String getName();
    public String getName() {
		return(jxskinname);
    }
    public String getInstanceName() {
    	return(instanceName);
    }
	public abstract void tileChildForm();
	public abstract void cascadeChildForm();
	public abstract void arrangeChildForm();
	public abstract String getActiveChildForm();
	public abstract void setTitle(String p_string);
	public abstract void setFormStyle(String p_string);
	public abstract void setFocus(String p_fieldname);
	public abstract void setFocus();
	public abstract void addFormCloseListener(JxFormCloseListener p_listener);
	public abstract void beep();
	public abstract int processFormClose(JxForm p_form);
	public abstract void unFocus();
	public abstract void maximize();

	public abstract String getParameter(String p_paramName);
	public abstract Object getParameterObject(String p_paramName);
	public abstract void setKeepAliveInterval(int p_msec);
	public abstract Object getNativeComponent();
	
	public abstract void setDirtyFlag(boolean p_flag);
	public abstract boolean isDirty();
	public abstract void addFormDirtyListener(JxFormDirtyListener p_listener);
	
}
