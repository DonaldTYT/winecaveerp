package com.uniinformation.zkbi;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Idspace;

import com.uniinformation.bicore.BiCellCollection;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiGipiPickViewInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiGetItemProperty extends BiGetItemProperty {
	class TemplateProperty {
		BiColumn bicol;
		String title;
		List<String> columns;
	}
	JxZkBiBase biBase;
	Hashtable<String,BiGipiPickViewInterface> pickViewHash=null;
	Hashtable<Template,TemplateProperty> templateHash;
	boolean useDefaultPickup = true;
	Template customizedTemplate=null;
	Vector listColumns=null;
	public ZkBiGetItemProperty (BiResult p_br,JxZkBiBase p_bibase) {
		super(p_br);
		biBase = p_bibase;
		customizedTemplate = ((Component) p_bibase.getNativeComponent()).getTemplate("template_"+JxZkBiBase.replaceViewName(p_br.getView().getName()));
			listColumns = new Vector();
			if(customizedTemplate != null) {
				listColumns.add(customizedTemplate);
			} else {

				for(BiColumn bc : super.getListColumns(null)) {
//					String prefix = (getItemMode == GETITEM_MODE_INPUT ? "template_" : "display_");
					String prefix = "template_";
					Template colTemplate = ((Component) p_bibase.getNativeComponent()).getTemplate(prefix+JxZkBiBase.replaceViewName(p_br.getView().getName())+"_"+bc.getLabel());
					if(colTemplate != null) {
						if(templateHash == null) templateHash = new Hashtable<Template,TemplateProperty>();
						listColumns.add(colTemplate);
						TemplateProperty tlp = new TemplateProperty();
						tlp.bicol = bc;
						templateHash.put(colTemplate, tlp);
					} else {
						listColumns.add(bc);
					}
				}
			}
	}
	
		@Override
		public Object getHeader(Object p_v,int p_col) {
			if(p_col < 0) {
				if(bigibr != null) return(bigibr.getView().getHeader());
				return("");
			}
			if(customizedTemplate == null) {
				Object o = getListColumns(p_v).get(p_col);
				if(o instanceof BiColumn) return(super.getHeader(p_v, p_col));
				if(o instanceof Template) {
					BiColumn bcol = (templateHash.get((Template) o)).bicol;
					//if(bcol != null) return (bcol.getEngName());
					//if(bcol != null) return (bigibr.getSessionHelper().getLabel(bcol.getEngName())); //andrew211027 try to translate template header
					
					//andrew220715 allow user translate template header
					if (bcol != null) {
						return MapUtil.of("label",bigibr.getSessionHelper().getLabel(bcol), 
										  "cellFullName", bcol.getCellFullName(), 
										  "biColumn", bcol, 
										  "biResult", bigibr, 
										  "allowUpdateTranslate", bigibr.getSessionHelper().getAllowUpdateTranslate());
					}
					
				}
				return(null);
			}
			//return(bigibr.getView().getHeader());
			return(bigibr.getSessionHelper().getLabel(bigibr.getView().getHeader()));  //andrew211022 try to translate list header of customizedTemplate 
		}	
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			if(customizedTemplate == null) {
				if(listColumns.get(p_col) instanceof Template) {
					String w = (String) ((Template) listColumns.get(p_col)).getParameters().get("twidth");
					if(w != null) {
						if(!w.contains("=")) {
							return("width="+w);
						} else {
							return(w);
						}
					}
				}
				return(super.getColumnWidth(p_v, p_col));
			}
			return("width=100%");
		}
		
		@Override
		public Object getColumnValue(Object p_v,int p_col) {
			if(customizedTemplate == null) return(super.getColumnValue(p_v, p_col));
			return(customizedTemplate);
		}	
		@Override
		public int getColumnSpan(Object p_v,int p_col) {
			if(customizedTemplate == null) return(super.getColumnSpan(p_v, p_col));
			return(1);
		}
		@Override
		protected Vector getListColumns(Object p_v) {
//			if(customizedTemplate == null) return(super.getListColumns(p_v));
			return(listColumns);
		}	
	
	@Override
	public void onValueChanged(Object p_value,int p_ctype) {
		final ColumnCell bcc = (ColumnCell) p_value;
		if(p_ctype != GIPI_CELL_MAPPED) {
			if(getItemMode == GETITEM_MODE_INPUT) biBase.setDirtyFlag(true);
			if(useDefaultPickup) {
				switch(p_ctype) {
				case GIPI_PULLDOWN_OPENED:
					ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
					if(zcvm.getComponent() instanceof ZkJxPickInput) {
						try {

							ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
							final BiGipiPickViewInterface pvi = 
										(pickViewHash == null || pickViewHash.get(bcc.getCellLabel()) == null) ? bcc.getBiColumn() : pickViewHash.get(bcc.getCellLabel());
							if(pvi.getPickViewName() != null) {
							biBase.pickBySelect(biBase.getSessionHelper(),pvi.getPickViewName(),pvi.getPickCondition(bcc), new EventListener() {
									@Override
									public void onEvent(Event arg0) throws Exception {
										// TODO Auto-generated method stub
										CellCollection col = (CellCollection) arg0.getData();
										
										String pcl = pvi.getPickColName();
										if(pcl == null) pcl = bcc.getCellLabel();
										Cell cc = col.getCell(pcl);
										if(cc != null) {
											bcc.update(cc.getObject());
										}
									}
								}
							);
							}
														
							
						} catch (Exception ex) {
							UniLog.log(ex);
						}
					}
					break;
				}
			}
		} else {
			biBase.onCellMapp(bcc);
		}
	}

	public static void useGetItemPropertyForSubLinks(BiResult p_br,JxZkBiBase p_bibase) 
	{
		Vector v = p_br.getSubLinks();
		if(v == null) return;
		for(int i =0;i< v.size();i++ ) {
			BiResult sr = (BiResult) v.get(i);
			String s = sr.getView().getName();
			if(p_bibase.getGipi(s) == null) {
				ZkBiGetItemProperty gipi = new ZkBiGetItemProperty(sr,p_bibase);	
//				if(p_br.getView().linkNoAddUpDateDelete(sr.getView())) {
				if(p_br.getView().linkNoAddUpDate(sr.getView())) {
					gipi.setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
				}
				p_bibase.setGipi(s,gipi);
			}
		}	
	}
	public void setUseDefaultPickup(boolean p_sw) {
		useDefaultPickup = p_sw;
	}
	public void setPickView(String p_colName,BiGipiPickViewInterface p_interface) {
		if(pickViewHash == null) {
			pickViewHash = new Hashtable<String,BiGipiPickViewInterface>();
		}
		if(p_interface == null) {
			pickViewHash.remove(p_colName);
		} else {
			pickViewHash.put(p_colName, p_interface);
		}
	}

	@Override
	public String getString(Object p_v) {
		String str = "";
		for(int i=0;i<getColumnCount(p_v);i++) {
			if(getItemMode != GETITEM_MODE_INPUT) {
				str += getColumnValue(p_v,i);
			} else {
				Object o = getColumnValue(p_v,i);
				if(o != null) {
					if(o instanceof ColumnCell ) {
						str += ((ColumnCell)o).getString() + " ";
					}
					if(o instanceof Template) {
						TemplateProperty tlp = templateHash.get(o);
						if(tlp != null) {
							Object oo = bigibr.getTrStatObj(p_v);
							BiCellCollection col = bigibr.getRowCollectionO(oo);
							for(String colid : tlp.columns) {
								str += col.getCellString(colid) + " ";
							}
						}
					}
				}
			}
			//str += getColumnLabel(p_v,i);
		}
		return(str);
	}		
	
	@Override
	public void setColumnCellList(int p_idx,List<String> p_ccList)  { 
		Object o = getColumnValue(null,p_idx);
		if(o instanceof Template) {
			TemplateProperty tlp = null;
			if(templateHash != null) tlp = templateHash.get(o);
			if(tlp != null) {
				tlp.columns = p_ccList;
			}
		}
	}

	@Override
	public Object getColumnNativeObject(Object p_data,Object p_obj) { 
		if(templateHash != null && p_obj instanceof Template) {
			TemplateProperty  tp = templateHash.get((Template) p_obj);
			return(getColumnValueByName(p_data,tp.bicol.getLabel()));
		}
		return (null);
	};
	
}