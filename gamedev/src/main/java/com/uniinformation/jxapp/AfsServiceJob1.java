package com.uniinformation.jxapp;

import java.util.Vector;

import org.zkoss.zk.ui.util.Template;
import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;

public class AfsServiceJob1 extends AfsServiceJob {
	Vector jobListColumn;
	class AfsServiceEngineerGetItemProperty extends BiGetItemProperty {
		Template template_AfsJobEngineer;
		AfsServiceEngineerGetItemProperty(BiResult p_br) {
			super(p_br);
			template_AfsJobEngineer = ((Component) getNativeComponent()).getTemplate("template_AfsJobEngineer");
			if(template_AfsJobEngineer != null) {
				Vector<BiColumn> v = p_br.getListColumns();
				jobListColumn = new Vector();
				jobListColumn.add(template_AfsJobEngineer);
			} 
		}
		@Override
		protected Vector getListColumns(Object p_v) {
			if(jobListColumn == null) return(super.getListColumns(p_v));
			return(jobListColumn);
		}	
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			if(jobListColumn == null) return(super.getColumnWidth(p_v, p_col));
			return("width=100%");
		}	
		@Override
		public Object getHeader(Object p_v,int p_col) {
			if(jobListColumn == null) return(super.getHeader(p_v, p_col));
			return("Engineer Log Time");
		}
		@Override
		public Object getColumnValue(Object p_v,int p_col) {
			if(jobListColumn == null) return(super.getColumnValue(p_v, p_col));
			Object o = getListColumns(p_v).get(p_col);
			if(o instanceof BiColumn) return(super.getColumnValue(p_v, p_col));
			else return(o);
		}	
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			switch(p_ctype ) {
			case GIPI_VALUE_ONOK:
			case GIPI_VALUE_CHANGED:
					setDirtyFlag(true);
			}
		}	
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		if(getGipi("AfsJobEngineer") == null) {
			setGipi("AfsJobEngineer",new AfsServiceEngineerGetItemProperty(p_br.getSubLink("AfsJobEngineer")));	
		}
		super.bindCellCollection(p_br,mode);
	}
}
