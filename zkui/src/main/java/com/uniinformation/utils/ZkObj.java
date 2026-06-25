package com.uniinformation.utils;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.impl.InputElement;

import com.uniinformation.jx.JxField;
/***
 * Utility class for cast object to zk object
 * It do not handle runtime class cast exception
 *
 */
public class ZkObj {
    public static Button toButton(Object p_obj){
    	if (p_obj instanceof JxField){
    		return (Button)((JxField)p_obj).getNativeObject();
    	}
    	return (Button)p_obj;
    }
    public static Textbox toTextbox(Object p_obj){
    	if (p_obj instanceof JxField){
    		return (Textbox)((JxField)p_obj).getNativeObject();
    	}
    	return (Textbox)p_obj;
    }
    public static Bandbox toBandbox(Object p_obj){
    	if (p_obj instanceof JxField){
    		return (Bandbox)((JxField)p_obj).getNativeObject();
    	}
    	return (Bandbox)p_obj;
    }
    public static Combobox toCombobox(Object p_obj){
    	if (p_obj instanceof JxField){
    		return (Combobox)((JxField)p_obj).getNativeObject();
    	}
    	return (Combobox)p_obj;
    }
    public static Div toDiv(Object p_obj){
    	if (p_obj instanceof JxField){
    		return (Div)((JxField)p_obj).getNativeObject();
    	}
    	return (Div)p_obj;
    }
    public static InputElement toInputElement(Object p_obj){
    	if (p_obj instanceof JxField){
    		return (InputElement)((JxField)p_obj).getNativeObject();
    	}
    	return (InputElement)p_obj;
    }
    public static Component toComp(Object p_obj){
    	if (p_obj instanceof JxField){
    		return (Component)((JxField)p_obj).getNativeObject();
    	}
    	return (Component)p_obj;
    }

}
