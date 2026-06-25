package com.uniinformation.utils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.zkoss.zk.ui.select.impl.Reflections;
import org.zkoss.zk.ui.select.impl.Reflections.FieldRunner;
import org.zkoss.zk.ui.select.impl.Reflections.MethodRunner;

import com.uniinformation.utils.UniLog;

public class AnnoUtil {
	public static void showAnno(Object p_obj, Class p_annoClass){
		UniLog.logm(null,"showAnno begin");
		final String classShortName = p_obj.getClass().toString().replaceAll("^.*\\.", "");
		final String annoShortName = p_annoClass.toString().replaceAll("^.*\\.", "");
		Reflections.forFields(p_obj.getClass(), p_annoClass, new FieldRunner(){
			@Override
			public void onField(Class p_class, Field p_field, Annotation p_anno) {
				UniLog.logm(null,"showAnno(%s,%s):onField: %s", classShortName, annoShortName, p_field.getName());
		}});
		Reflections.forMethods(p_obj.getClass(), p_annoClass, new MethodRunner(){
			@Override
			public void onMethod(Class p_class, Method p_method, Annotation p_anno) {
				UniLog.logm(null,"showAnno(%s,%s):onMethod: %s", classShortName, annoShortName, p_method.getName());
		}});
		UniLog.logm(null,"showAnno end");
	}
}

