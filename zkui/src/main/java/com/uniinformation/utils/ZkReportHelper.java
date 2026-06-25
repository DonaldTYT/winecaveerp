package com.uniinformation.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.event.SizeEvent;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Foot;
import org.zkoss.zul.Footer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListitemComparator;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.MessageboxDlg;

import com.kyoko.common.StringUtil;
import com.uniinformation.utils.poi.ExcelPoi;


public class ZkReportHelper {
	JSONObject data;
   	MultiSortMap mMultiSortMap = new MultiSortMap();
   	Sorter mSorter = new Sorter();
   	Map<Component, String> mCompMinWidthMap = new HashMap<Component, String>();
	public ZkReportHelper(JSONObject p_data) {
		data = p_data;
	}
	/***
	 * 
	 * @param p_grid  - grid for output
	 * @param sumTotal - sumTotal flag
	 * @param p_title - auxheader label
	 * @throws JSONException
	 */
	void gridAddSubTotal(double xx[][],JSONArray jsonRowsArr,int nCellColumn,JSONArray jcols,DecimalFormat df[],Rows gr,boolean showTotal,int rowHdrIdx) {
		if(rowHdrIdx >= jsonRowsArr.length()-1) return;
		Cell ce;
		Row subTotalrow = new Row();

		ce = new org.zkoss.zul.Cell();
		ce.setColspan(rowHdrIdx+1);
		subTotalrow.appendChild(ce);
		
		ce = new org.zkoss.zul.Cell();
		ce.setColspan(jsonRowsArr.length()-rowHdrIdx-1);
		ce.appendChild(new Label("Subtotal"));
		
		subTotalrow.appendChild(ce);
		int nn = jcols.length() + (showTotal ? 1 : 0);
		for(int n = 0; n < nn;n++) {
			for(int k=0;k<nCellColumn;k++) {
				ce = new org.zkoss.zul.Cell();
				ce.setAlign("right");
				if(xx[k][n] != 0.0) {
					if(df[k] != null) {
						ce.appendChild(new Label(df[k].format(xx[k][n])));
					} else {
						ce.appendChild(new Label(""+xx[k][n]));
					}
				}
				subTotalrow.appendChild(ce);
			}
		}
		gr.appendChild(subTotalrow);
	}
	public void analysedDataToGrid(final Grid p_grid,boolean sumTotal,String p_title) throws JSONException{
		JSONArray jsonAggregatesArr = data.getJSONArray("Aggregates");
		JSONArray jsonRowsArr = data.getJSONArray("RowHeaders");
		JSONArray jsonColsArr = data.getJSONArray("ColHeaders");
		Components.removeAllChildren(p_grid);
		JSONArray jcols = data.optJSONArray("Columns");
		JSONArray jrows = data.optJSONArray("Rows");
		boolean mergeCell = false;
		boolean showSubTotal = false;
		int nCellColumn = jsonAggregatesArr.length();
		DecimalFormat df[] = new DecimalFormat[nCellColumn];
		for(int i=0;i<nCellColumn;i++) {
			String fmt = jsonAggregatesArr.getJSONObject(i).optString("format");
			if(fmt != null) df[i] = new DecimalFormat(fmt);
		}
		double gTotal[] = new double[nCellColumn];
		double[] colTotal[] = new double[nCellColumn][jcols.length()];
		Columns gc = p_grid.getColumns();
		if(jcols != null) {
//			String lastColName = null;
			if(jsonColsArr.length() > 0 ) { 
//				lastColName = jsonColsArr.getString(jsonColsArr.length() - 1);
				Auxhead ac = new Auxhead();
				p_grid.appendChild(ac);
				Auxheader achdr = new Auxheader();
				/*
				Vlayout hdrdv = new Vlayout();
				hdrdv.setHeight("50px");
				hdrdv.appendChild(new Label(p_title));
				achdr.appendChild(hdrdv);
				*/
				achdr.setLabel(p_title);
				achdr.setAlign("center");
				achdr.setColspan(jsonRowsArr.length()+ (jcols.length() + (sumTotal ? 1 : 0)) * nCellColumn);
				ac.appendChild(achdr);
				for(int i = 0;i<jsonColsArr.length();i++) {
					ac = new Auxhead();
					p_grid.appendChild(ac);
					if(jsonRowsArr.length() > 0) {
						achdr = new Auxheader(jsonColsArr.getString(i));
						achdr.setColspan(jsonRowsArr.length());
						achdr.setAlign("right");
						ac.appendChild(achdr);
					}
					String thishdr = null;
					String lasthdr = null;
					int thisspan = 0;
					for(int j = 0;j<jcols.length();j++) {
						String s = jcols.getJSONObject(j).getString(jsonColsArr.getString(i));
						if(!s.equals(thishdr)) {
							if(thishdr != null) {
								achdr = new Auxheader(thishdr);
								achdr.setColspan(thisspan);
								achdr.setAlign("center");
								ac.appendChild(achdr);
							}
							thishdr = s;
							thisspan = nCellColumn;
						} else {
							thisspan += nCellColumn;
						}
					}
					if(thisspan > 0) {
						achdr = new Auxheader(thishdr);
						achdr.setColspan(thisspan);
						achdr.setAlign("center");
						ac.appendChild(achdr);
					}
					if(jsonColsArr.length() > 0 && sumTotal) {
						achdr = new Auxheader("Total");
						achdr.setColspan(nCellColumn);
						achdr.setAlign("center");
						ac.appendChild(achdr);
					}
				}
			}
			setupMenupopup(findMessageboxDlg(p_grid));
			gc = new Columns();
			gc.setMenupopup("menu_zkreport");
			gc.setSizable(true);
			p_grid.appendChild(gc);
			if(jrows != null) {
				for(int i = 0;i<jsonRowsArr.length();i++) {
					String s = jsonRowsArr.optString(i);
					Column cl = new Column(s);
					cl.setWidth("200px");
					cl.setId("zkreport_column_" + gc.getChildren().size());
					setupColumnSort(gc, cl);
					gc.appendChild(cl);
				}
			}
			Column cl ;
			for(int i = 0;i<jcols.length();i++) {
				for(int k=0;k<nCellColumn;k++) {
				cl = new Column(jsonAggregatesArr.getJSONObject(k).optString("name"));
				String wstr = jsonAggregatesArr.getJSONObject(k).optString("width");
				//if(wstr != null) cl.setWidth(wstr);else cl.setWidth("100px");
				if (wstr != null) wstr = "100px";
				cl.setWidth(wstr);
				mCompMinWidthMap.put(cl, wstr);
				cl.setAlign("right");
				cl.setId("zkreport_column_" + gc.getChildren().size());
				setupColumnSort(gc, cl);
//				cl.setSort("auto");
//				cl.setHflex("min");
				gc.appendChild(cl);
				}
			}
			if(jsonColsArr.length() > 0 && sumTotal) {
//				cl = new Column("Total");
//				cl = new Column(jsonAggregatesArr.getString(0));
				for(int k=0;k<nCellColumn;k++) {
				cl = new Column(jsonAggregatesArr.getJSONObject(k).optString("name"));
				String wstr = jsonAggregatesArr.getJSONObject(k).optString("width");
				//if(wstr != null) cl.setWidth(wstr);else cl.setWidth("100px");
				if (wstr != null) wstr = "100px";
				cl.setWidth(wstr);
				mCompMinWidthMap.put(cl, wstr);
				cl.setAlign("right");
				cl.setId("zkreport_column_" + gc.getChildren().size());
				setupColumnSort(gc, cl);
				gc.appendChild(cl);
				}
			}
		}
		Rows gr = p_grid.getRows();
		if(gr == null) {
			gr = new Rows();
			p_grid.appendChild(gr);
		}
		Row row ;
		org.zkoss.zul.Cell ce ;
		String lastRowName[] = new String[jsonRowsArr.length()];
		org.zkoss.zul.Cell lastRowCell[] = new org.zkoss.zul.Cell[jsonRowsArr.length()];
		int lastRowNum[] = new int[jsonRowsArr.length()];
		ArrayList<double[][]> colSubTotal = null;
		if(showSubTotal) {
			colSubTotal = new ArrayList<double[][]>();
			for(int i=0;i<jsonRowsArr.length();i++) {
				colSubTotal.add(null);
			}
		}
		{
		int i = 0;
		for(;i<jrows.length();i++) {
			row = new Row();
			row.setValue(i);
			JSONObject jo = jrows.getJSONObject(i);
			for(int j = 0;j<jsonRowsArr.length();j++) {
				String s = jo.getString( jsonRowsArr.getString(j));
				boolean spanned = false;
				if(lastRowName[j] != null && lastRowName[j].equals(s)) {
					spanned = true;
				}
				if(spanned && mergeCell) {
					lastRowCell[j].setRowspan(lastRowCell[j].getRowspan()+1);
				} else {
					ce = new org.zkoss.zul.Cell();
					ce.setWidth("200px");
					ce.appendChild(new Label(s));
					ce.setAttribute("cell_value", s);
					row.appendChild(ce);
					if(!spanned) {
						for(int k = jsonRowsArr.length()-1; k > j ;k--) {
							if(showSubTotal) {
								double xx[][]=	colSubTotal.get(k);
								if(i-lastRowNum[k]>1) {
									if(xx != null) gridAddSubTotal(xx,jsonRowsArr,nCellColumn,jcols,df,gr,sumTotal,k);
								}
								colSubTotal.set(k, null);
							}
							lastRowName[k] = null;
							lastRowNum[k] = 0;
							lastRowCell[k] = null;
						}
						if(showSubTotal) {
							double xx[][]=	colSubTotal.get(j);
							if(i-lastRowNum[j]>1) {
								if(xx != null) gridAddSubTotal(xx,jsonRowsArr,nCellColumn,jcols,df,gr,sumTotal,j);
							}
							xx = new double[nCellColumn][jcols.length()+1];
							colSubTotal.set(j,xx);
						}
						lastRowName[j] = s;
						lastRowNum[j] = i;
						lastRowCell[j] = ce;
					}
				}
			}
			JSONArray da = jrows.getJSONObject(i).getJSONArray("datas");
			double rowTotal[] = new double[nCellColumn];
			for(int j = 0;j<da.length();j++) {
				JSONArray cellArr = da.getJSONArray(j);
				for(int k=0;k<nCellColumn;k++) {
					double d = cellArr.getDouble(k);
					ce = new org.zkoss.zul.Cell();
//					ce.setWidth("100px");
					ce.setAlign("right");
//					ce.setHflex("min");
					if(d != 0.0) {
						if(df[k] != null)
							ce.appendChild(new Label(df[k].format(d)));
						else
							ce.appendChild(new Label(""+d));
					} // else ce.appendChild(new Label("N/A"));
					ce.setAttribute("cell_value", d);
					if(sumTotal) {
						rowTotal[k] += d;
						colTotal[k][j] += d;
						gTotal[k] += d;
					}
					if(showSubTotal) {
						for(int n = 0;n<jsonRowsArr.length();n++) {
							double xx[][] = colSubTotal.get(n);
							if(xx != null) {
								xx[k][j] += d;
								xx[k][da.length()] += d;
							}
						}
					}
					row.appendChild(ce);
				}
			}
			if(jsonColsArr.length() > 0 && sumTotal) {
				for(int k=0;k<nCellColumn;k++) {
				ce = new org.zkoss.zul.Cell();
//				ce.setWidth("100px");
				ce.setAlign("right");
				if(rowTotal[k] != 0.0) {
					if(df[k] != null)
						ce.appendChild(new Label(df[k].format(rowTotal[k])));
					else
						ce.appendChild(new Label(""+rowTotal[k]));
				}
				ce.setAttribute("cell_value", rowTotal[k]);
				row.appendChild(ce);
				}
			}
			gr.appendChild(row);
		}
		if(showSubTotal) {
			for(int k = jsonRowsArr.length()-1; k >= 0 ;k--) {
				double xx[][]=	colSubTotal.get(k);
				if(showSubTotal && i-lastRowNum[k]>1) {
					if(xx != null) gridAddSubTotal(xx,jsonRowsArr,nCellColumn,jcols,df,gr,sumTotal,k);
				}
				lastRowName[k] = null;
				lastRowNum[k] = 0;
				lastRowCell[k] = null;
				colSubTotal.set(k, null);
			}
		}
		}
		
		if(sumTotal && jsonRowsArr.length() > 0) {
			Foot gridFoot = new Foot();
			Footer gridFooter;
			gridFooter = new Footer();
			gridFooter.setSpan(jsonRowsArr.length());
//			gridFooter.appendChild(new Label("Total"));
			gridFooter.setLabel("Total");
			gridFoot.appendChild(gridFooter);
			for(int i = 0; i < jcols.length();i++) {
				for(int k=0;k<nCellColumn;k++) {
				gridFooter = new Footer();
				gridFooter.setAlign("right");
				if(colTotal[k][i] != 0.0) {
					if(df[k] != null)
//						gridFooter.appendChild(new Label(df[k].format(colTotal[k][i])));
						gridFooter.setLabel(df[k].format(colTotal[k][i]));
					else
//						gridFooter.appendChild(new Label(""+colTotal[k][i]));
						gridFooter.setLabel(""+colTotal[k][i]);
				}
				gridFoot.appendChild(gridFooter);
				}
			}
			if(jsonColsArr.length() > 0) { 
				for(int k=0;k<nCellColumn;k++) {
					gridFooter = new Footer();
					gridFooter.setAlign("right");
					if(gTotal[k] != 0.0) {
						if(df[k] != null)
							gridFooter.setLabel(df[k].format(gTotal[k]));
						else
							gridFooter.setLabel(""+gTotal[k]);
					}
					gridFoot.appendChild(gridFooter);
				}
			}
			p_grid.appendChild(gridFoot);
		}
		p_grid.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>(){
			@Override
			public void onEvent(AfterSizeEvent event) throws Exception {
				UniLog.log("grid onaftersize width:" + event.getWidth() + ",height:" + event.getHeight());
				p_grid.setAttribute("aftersize_width", event.getWidth());
				p_grid.setAttribute("aftersize_height", event.getHeight());
			}
		});
		p_grid.addEventListener("onInitSize", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("grid onInitSize " + event.getData());
				JSONObject json = new JSONObject(event.getData().toString());
				int width = json.getInt("width");
				Integer gridWidth = (Integer) p_grid.getAttribute("aftersize_width");
				if (gridWidth != null && gridWidth < width) {
					for (Map.Entry<Component, String> entry : mCompMinWidthMap.entrySet()) {
						Component cl = entry.getKey();
						if (cl instanceof Column) {
							((Column)cl).setStyle("min-width:" + entry.getValue());
							((Column)cl).setWidth("");
						}
					}
				}
			}
		});
		Clients.evalJavaScript(String.format("setTimeout(function(){"
				+ "var grid = $('#' + jq('$%s').attr('id'));"
				+ "var zkComp = zk.Widget.$('$%s');"
				+ "zAu.send(new zk.Event(zkComp, 'onInitSize', {'width':grid.width(),'height':grid.height()}, {toServer:true}));"
				+ "},100);", p_grid.getId(), p_grid.getId()));
		/*Clients.evalJavaScript(String.format("setTimeout(function(){"
				+ "var grid = $('#' + jq('$%s').attr('id'));"
				+ "grid.find('table').css('width', '');"
				+ "},100);", p_grid.getId()));*/
	}

	static public ByteArrayOutputStream exportGridToExcel(Grid p_grid,int p_nHeaderRow,InputStream p_template,boolean p_isXlsx) throws Exception {
//    		ExcelPoi jxf = ExcelPoi.newExcelPoi(p_template,true); 
			ExcelPoi jxf = null;
			if(p_template != null) {
				jxf = ExcelPoi.newExcelPoi(p_isXlsx); 
			} else {
				jxf = ExcelPoi.newExcelPoi(p_isXlsx); 
			}
			int rowIdx = 0;
			/*
			Columns cols = p_grid.getColumns();
			if(cols != null) {
				List<Column> colList = cols.getChildren();
				for(int i=0;i<colList.size();i++) {
					String ss = colList.get(i).getLabel();
					if(ss != null) jxf.excel_setStringValue(rowIdx, i, ss);
				}
			}
			*/
			Collection<Component> heads = p_grid.getHeads();
			for(Component comp:heads) {
				if(comp instanceof Auxhead) {
					Auxhead aheads = (Auxhead) comp;
					List<Auxheader> colList = aheads.getChildren();
					for(int i=0,j=0;i<colList.size();i++) {
						String ss = colList.get(i).getLabel();
						if(ss != null) jxf.excel_setStringValue(rowIdx, j, ss);
						if(colList.get(i).getColspan() > 1) {
							jxf.excel_MergeCells(rowIdx,j,rowIdx, j + colList.get(i).getColspan() -1);
						}
						j += colList.get(i).getColspan();
					}
					rowIdx++;
				}
				if(comp instanceof Columns) {
					Columns cols = (Columns) comp;
					List<Column> colList = cols.getChildren();
					for(int i=0;i<colList.size();i++) {
						String ss = colList.get(i).getLabel();
						List<Component> compList  = colList.get(i).getChildren();
						if(compList.size() > 0) {
							if(ss == null) ss = "";
							for(Component comp2:compList) {
								if(comp2 instanceof Label) {
									ss += ((Label) comp2).getValue();
								}
							}
						}
						if(ss != null) jxf.excel_setStringValue(rowIdx, i, ss);
					}
					rowIdx++;
				}
			}
			Rows rows = p_grid.getRows(); 
			List<Row> rowList = rows.getChildren();
			for(int i=0;i<rowList.size();i++) {
				List<Cell> cellList = rowList.get(i).getChildren();
				for(int j=0,k=0;j<cellList.size();j++) {
					String ss = getCellAsString(cellList.get(j));
					List<Component> compList  = cellList.get(j).getChildren();
					if(ss != null) {
						if(k < p_nHeaderRow ) {
//								jxf.excel_MergeCells(lastRowNum[j], k, rowIdx, k);
							jxf.excel_setStringValue(rowIdx, k, ss);
						} else {
							double val=0.0;
							try {
								val = Double.parseDouble(StringUtil.toNumberOnly(ss));
							} catch (NumberFormatException nex) {
								UniLog.log("value is not number, set to 0");
							}
							jxf.excel_setNumericValue(rowIdx, k, val);
						}
					}
					if(cellList.get(j).getColspan() > 1) {
							jxf.excel_MergeCells(rowIdx,j,rowIdx, j + cellList.get(i).getColspan() -1);
					}
					k += cellList.get(j).getColspan();
				}
				rowIdx++;
			}
			Foot foots = p_grid.getFoot();
			if(foots != null) {
				List<Footer> footList = foots.getChildren();
				for(int i=0,j=0;i<footList.size();i++) {
					String ss = footList.get(i).getLabel();
					if(ss != null) {
						if(j < p_nHeaderRow ) {
							jxf.excel_setStringValue(rowIdx, j, ss);
						} else {
							double val=0.0;
							try {
								val = Double.parseDouble(StringUtil.toNumberOnly(ss));
							} catch (NumberFormatException nex) {
								UniLog.log("value is not number, set to 0");
							}
							jxf.excel_setNumericValue(rowIdx, j, val);
						}
					}
					if(footList.get(i).getSpan() > 1) {
						jxf.excel_MergeCells(rowIdx,j,rowIdx, j + footList.get(i).getSpan() -1);
					}
					j += footList.get(i).getSpan();
				}
			}
			
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		jxf.writeWorkBook(bos);
    		return(bos);
	}
	static String getCellAsString(org.zkoss.zul.Cell p_cell) {
		List<Component> compList  = p_cell.getChildren();
		if(compList.size() > 0) {
			String ss = "";
			for(Component comp:compList) {
				if(comp instanceof Label) {
					ss += ((Label) comp).getValue();
				}
			}
			return(ss);
		}
		return(null);
	}
	private void setupMenupopup(final Component parent) {
		UniLog.log("setupMenupopup " + parent);
    	final Menupopup menupopup;
		if(parent.hasFellow("menu_zkreport",true)) {
			menupopup = (Menupopup) parent.getFellow("menu_zkreport");
			Components.removeAllChildren(menupopup);
		}  else menupopup = new Menupopup();
    	menupopup.setParent(parent);
    	menupopup.setId("menu_zkreport");
    	Menuitem menuitem = new Menuitem();
    	menuitem.setParent(menupopup);
    	menuitem.setLabel("Sort Ascending");
    	menuitem.setImage("~./zul/img/grid/menu-arrowup.png");
    	menuitem.addEventListener("onClick", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				Column refColumn = (Column) menupopup.getAttribute("ref_column");
				mMultiSortMap.clear();
				mMultiSortMap.sortMulti(parent, refColumn, false);
			}
    	});
    	menuitem = new Menuitem();
    	menuitem.setParent(menupopup);
    	menuitem.setLabel("Sort Descending");
    	menuitem.setImage("~./zul/img/grid/menu-arrowdown.png");
    	menuitem.addEventListener("onClick", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				Column refColumn = (Column) menupopup.getAttribute("ref_column");
				mMultiSortMap.clear();
				mMultiSortMap.sortMulti(parent, refColumn, true);
			}
    	});
    	Menu menu = new Menu();
    	menu.setParent(menupopup);
    	menu.setLabel("Sort Multicolumn");
    	menu.setIconSclass("z-icon-sort");
    	{
    		Menupopup submpopup = new Menupopup();
    		submpopup.setParent(menu);
    		Menuitem submitem = new Menuitem();
    		submitem.setParent(submpopup);
	    	submitem.setLabel("Ascending");
	    	submitem.setImage("~./zul/img/grid/menu-arrowup.png");
	    	submitem.addEventListener("onClick", new EventListener<Event>(){
				@Override
				public void onEvent(Event event) throws Exception {
					Column refColumn = (Column) menupopup.getAttribute("ref_column");
					mMultiSortMap.sortMulti(parent, refColumn, false);
				}
	    	});
    		submitem = new Menuitem();
    		submitem.setParent(submpopup);
    		submitem.setLabel("Descending");
	    	submitem.setImage("~./zul/img/grid/menu-arrowdown.png");
	    	submitem.addEventListener("onClick", new EventListener<Event>(){
				@Override
				public void onEvent(Event event) throws Exception {
					Column refColumn = (Column) menupopup.getAttribute("ref_column");
					mMultiSortMap.sortMulti(parent, refColumn, true);
				}
	    	});
    		submitem = new Menuitem();
    		submitem.setParent(submpopup);
    		submitem.setLabel("Clear");
    		submitem.setIconSclass("z-icon-eraser");
	    	submitem.addEventListener("onClick", new EventListener<Event>(){
				@Override
				public void onEvent(Event event) throws Exception {
					mMultiSortMap.clear();
					mMultiSortMap.clearSortDirection(parent);
					mMultiSortMap.clearColumnSortDescAttr(parent);
				}
	    	});
    	}

    	menupopup.addEventListener("onOpen", new EventListener<OpenEvent>(){
			@Override
			public void onEvent(OpenEvent event) throws Exception {
				if (!event.isOpen())
					return;
				Column refColumn = (Column) event.getReference();
				menupopup.setAttribute("ref_column", refColumn);
			}
    	});
	}
	private Component findMessageboxDlg(Component comp) {
		Component c = comp;
		while ((c = c.getParent()) != null) {
			if (c instanceof MessageboxDlg)
				return c;
		}
		return comp.getParent();
	}
	private void setupColumnSort(final Component parent, final Column cl) {
		try {
			cl.setSort("auto");
			cl.setSortAscending(mSorter);
			cl.addEventListener(Events.ON_SORT, new EventListener<SortEvent>(){
				@Override
				public void onEvent(SortEvent event) throws Exception {
					UniLog.log("sort column:" + event.getTarget() + "," + event.isAscending());
					mMultiSortMap.clear();
					mMultiSortMap.sortMulti(parent, cl);
					event.stopPropagation();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class Sorter implements Comparator<Row> {
		private Cell findCell(Row r, int colIndex) {
			Cell cell = null;
			Iterable<Component> it = r.queryAll("Cell");
			int i = 0;
			for (Component comp : it) {
				if (i++ == colIndex) {
					cell = (Cell)comp;
					break;
				}
			}
			return cell;
		}
		@Override
		public int compare(Row r1, Row r2) {
			for (Map.Entry<Integer, MultiSortInfo> entry : mMultiSortMap.entrySet()) {
				int colIdx = entry.getKey();
				MultiSortInfo sortInfo = entry.getValue();
				Cell cell1 = findCell(r1, colIdx);
				Cell cell2 = findCell(r2, colIdx);
				Object o1 = cell1.getAttribute("cell_value");
				Object o2 = cell2.getAttribute("cell_value");
				if (o1 != null && o2 != null) {
					if (o1 instanceof Double && o2 instanceof Double) {
						double d1 = (Double)o1;
						double d2 = (Double)o2;
						if (d1 < d2)
							return sortInfo.sortDesc ? 1 : -1;
						else if (d1 > d2)
							return sortInfo.sortDesc ? -1 : 1;
					}
					else if (o1 instanceof Integer && o2 instanceof Integer) {
						int r = (Integer)o1 - (Integer)o2;
						if (r != 0)
							return r;
					}
					else if (o1 instanceof String && o2 instanceof String) {
						int r = sortInfo.sortDesc ? ((String)o2).compareTo((String)o1) : ((String)o1).compareTo((String)o2);
						if (r != 0)
							return r;
					}
				}
			}
			return 0;
		}
	}
    private static class MultiSortInfo {
    	boolean sortDesc;
    	int num;
    	public MultiSortInfo(boolean sortDesc) {
    		this.sortDesc = sortDesc;
    	}
    	public MultiSortInfo(boolean sortDesc, int num) {
    		this(sortDesc);
    		this.num = num;
    	}
    	@Override
    	public Object clone() {
    		return new MultiSortInfo(sortDesc, num);
    	}
    }
    private class MultiSortMap extends LinkedHashMap<Integer, MultiSortInfo> {
    	private Column lastClickColumn;
    	public void sortMulti(Component comp) {
    		((Column)comp.query("#zkreport_column_0")).sort(true, true);
			clearSortDirection(comp);
       		int i = 0;
    		for (Map.Entry<Integer, MultiSortInfo> entry : mMultiSortMap.entrySet()) {
    			entry.getValue().num = ++i;
    			String id = "#zkreport_column_" + entry.getKey();
    			Column column = (Column) comp.query(id);
    			if (column != null) {
    				if (entry.getValue().sortDesc)
    					column.setSortDirection("descending");
    				else
    					column.setSortDirection("ascending");
    			}
    		}
    		refreshSortIcon(comp);
    	}
    	public void sortMulti(Component comp, Column column, boolean sortDesc) {
    		column.setAttribute("sort_desc", sortDesc);
    		lastClickColumn = column;
    		int columnIdx = Integer.parseInt(column.getId().replace("zkreport_column_", ""));
    		if (mMultiSortMap.containsKey(columnIdx))
    			mMultiSortMap.remove(columnIdx);
    		mMultiSortMap.put(columnIdx, new MultiSortInfo(sortDesc));
    		sortMulti(comp);
    	}
    	public void sortMulti(Component comp, Column column) {
    		Boolean sortDesc = (Boolean) column.getAttribute("sort_desc");
    		sortDesc = column == lastClickColumn && sortDesc != null && !sortDesc;
    		sortMulti(comp, column, sortDesc);
    	}
    	private void clearSortDirection(Component comp) {
			for (Component tmpComp : comp.queryAll("Column")){
				Column column = (Column) tmpComp;
				if (column.getId().startsWith("zkreport_column_")){
					Clients.evalJavaScript(String.format(""
							+ "var $sorticon = jq('$%s').find('.z-column-sorticon');"
							+ "if ($sorticon.hasClass('column-sorticon')) {"
							+ "		$sorticon.find('.column-sortnum').remove();"
							+ "		$sorticon.removeClass('column-sorticon');"
							+ "}", column.getId()));
					column.setSortDirection("natural");
				}
			}
    	}
    	private void clearColumnSortDescAttr(Component comp) {
			for (Component tmpComp : comp.queryAll("Column")){
				Column column = (Column) tmpComp;
				if (column.getId().startsWith("zkreport_column_"))
					column.setAttribute("sort_desc", null);
			}
    	}
    	private void refreshSortIcon(Component comp) {
			UniLog.log("refreshSortIcon");
			for (Component c : comp.queryAll("Column")) {
				Column lh = (Column) c;
				String id = lh.getId();
				String prefix = "zkreport_column_";
				if (id.startsWith(prefix)) {
					int sortIdx = Integer.parseInt(id.substring(prefix.length()));
					if (mMultiSortMap.containsKey(sortIdx)) {
						Clients.evalJavaScript(String.format("setTimeout(function(){"
								+ "var $sorticon = jq('$%s').find('.z-column-sorticon');"
								+ "if (!$sorticon.hasClass('column-sorticon')) {"
								+ "		$sorticon.append('<span class=\\'column-sortnum\\'>%d</span>').addClass('column-sorticon');"
								+ "}}, 10)", id, mMultiSortMap.get(sortIdx).num));
					}
				}
			}
    	}
    }
}
