package com.uniinformation.webcore;

import java.awt.Desktop;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zul.*;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.*;
import com.uniinformation.zkbi.*;
 
	public class ZkWebOrderInputForm extends SelectorComposer<Component> {
	    private static final long serialVersionUID = 1L;
	 
	    @Wire
	    private Window window1;
	    
	    @Wire
	    private Datebox db_deliverydate;
	    @Wire
	    private Textbox tb_po;
	    @Wire
	    private Textbox tb_title;
	    @Wire
	    private Intbox ib_pages;
	    @Wire
	    private Checkbox cb_affixingcd;
	    @Wire
	    private Checkbox cb_affixinglabel;
	    @Wire
	    private Selectbox sb_textmaterial;
	    @Wire
	    private Label l_textmaterial_name;
	    @Wire
	    private Intbox ib_textmaterial_weight;
	    @Wire
	    private Label l_textmaterial_group;
	    @Wire
	    private Intbox ib_textmaterial_color1;
	    @Wire
	    private Intbox ib_textmaterial_color2;
	    @Wire
	    private Selectbox sb_covermaterial;
	    @Wire
	    private Label l_covermaterial_name;
	    @Wire
	    private Intbox ib_covermaterial_weight;
	    @Wire
	    private Label l_covermaterial_group;
	    @Wire
	    private Selectbox sb_covermaterial_finishing;
	    @Wire
	    private Intbox ib_covermaterial_color1;
	    @Wire
	    private Intbox ib_covermaterial_color2;
	    @Wire
	    private Selectbox sb_casematerial;
	    @Wire
	    private Label l_casematerial_name;
	    @Wire
	    private Intbox ib_casematerial_weight;
	    @Wire
	    private Label l_casematerial_group;
	    @Wire
	    private Selectbox sb_casematerial_finishing;
	    @Wire
	    private Intbox ib_casematerial_color1;
	    @Wire
	    private Intbox ib_casematerial_color2;
	    @Wire
	    private Selectbox sb_boardmaterial;
	    @Wire
	    private Label l_boardmaterial_name;
	    @Wire
	    private Intbox ib_boardmaterial_weight;
	    @Wire
	    private Label l_boardmaterial_group;
	    @Wire
	    private Selectbox sb_boardmaterial_finishing;
	    @Wire
	    private Intbox ib_boardmaterial_color1;
	    @Wire
	    private Intbox ib_boardmaterial_color2;
	    @Wire
	    private Selectbox sb_spinematerial;
	    @Wire
	    private Label l_spinematerial_name;
	    @Wire
	    private Intbox ib_spinematerial_weight;
	    @Wire
	    private Label l_spinematerial_group;
	    @Wire
	    private Selectbox sb_spinematerial_finishing;
	    @Wire
	    private Intbox ib_spinematerial_color1;
	    @Wire
	    private Intbox ib_spinematerial_color2;
	    @Wire
	    private Selectbox sb_endpapermaterial;
	    @Wire
	    private Label l_endpapermaterial_name;
	    @Wire
	    private Intbox ib_endpapermaterial_weight;
	    @Wire
	    private Label l_endpapermaterial_group;
	    @Wire
	    private Selectbox sb_endpapermaterial_finishing;
	    @Wire
	    private Intbox ib_endpapermaterial_color1;
	    @Wire
	    private Intbox ib_endpapermaterial_color2;
	    @Wire
	    private Selectbox sb_qty1_edition;
	    @Wire
	    private Intbox ib_qty1_quantity;
	    @Wire
	    private Textbox tb_qty1_isbn;
	    @Wire
	    private Decimalbox db_qty1_unitprice;
	    @Wire
	    private Selectbox sb_qty2_edition;
	    @Wire
	    private Intbox ib_qty2_quantity;
	    @Wire
	    private Textbox tb_qty2_isbn;
	    @Wire
	    private Decimalbox db_qty2_unitprice;
  
	    
	    
	 
	    @Listen("onClick = #b_submit")
	    public void submit() {
	    	UniLog.logClass(this, "submit clicked");
	    	UniLog.logClass(this, "" + db_deliverydate == null || db_deliverydate.getValue() == null ? "" : db_deliverydate.getValue().toString());
	    	UniLog.logClass(this, "" + tb_po == null || tb_po.getValue() == null ? "" : tb_po.getValue().toString());
	    	UniLog.logClass(this, "" + ib_pages == null || ib_pages.getValue() == null ? "" : ib_pages.getValue().toString());
	    	UniLog.logClass(this, "" + cb_affixingcd == null || !cb_affixingcd.isChecked() ? "N" : "Y");
	    	UniLog.logClass(this, "" + sb_textmaterial == null ? "" : "" + sb_textmaterial.getSelectedIndex());
	    	UniLog.logClass(this, "" + l_textmaterial_name == null || l_textmaterial_name.getValue() == null ? "" : l_textmaterial_name.getValue().toString());
	    	UniLog.logClass(this, "" + db_qty1_unitprice == null || db_qty1_unitprice.getText() == null ? "0" : db_qty1_unitprice.getText());
	    	
			//show a popup here
			Window detailWin = buildResultWindow();
			detailWin.setParent(window1);
			//detailWin.doPopup();
			detailWin.doHighlighted();
			detailWin.setPosition("middle_center");
		
	    	
	        showNotify("Submitted", window1);
	    }
	    
	    @Listen("onClick = #b_cancel")
	    public void cancel() {
	    	UniLog.logClass(this, "cancel clicked");
	        showNotify("TODO", window1);
	    }
	 
	    private void showNotify(String msg, Component ref) {
	        Clients.showNotification(msg, "info", ref, "end_center", 2000);
	    }
	    
	    public Window buildResultWindow(){
	    	try{
				final Window detailWin = new Window();
				detailWin.setTitle("Result");
				
				detailWin.setBorder("normal");
				detailWin.setClosable(true);
				detailWin.setMaximizable(true);
				detailWin.setShadow(true);
				detailWin.setSizable(true);  
				detailWin.setTopmost();
				Grid grid = new Grid();
				grid.setId("detail_grid");
				grid.setWidth("600px");
				detailWin.appendChild(grid);
				
		    	Columns columns = new Columns();
		    	Column col1 = new Column();
		    	col1.setAlign("right");
		    	col1.setWidth("20%");
		    	
		    	Column col2 = new Column();
		    	col2.setWidth("40%");
		    	col2.setAlign("left");
		    	
		    	Column col3 = new Column();
		    	col3.setWidth("40%");
		    	col3.setAlign("left");
		    	
		    	
		    	
		    	columns.appendChild(col1);
		    	columns.appendChild(col2);
		    	columns.appendChild(col3);
		    	grid.appendChild(columns);    	
		    	Rows rows = new Rows();
		    	grid.appendChild(rows);
				
				
				
				
			    Row titleRow = new Row();
			    titleRow.appendChild(new Label("Title"));
			    titleRow.appendChild(new Label("The Photo Book 2016"));
			    titleRow.appendChild(new Label(""));
			    rows.appendChild(titleRow);
			    
			    Row sizeRow = new Row();
			    sizeRow.appendChild(new Label("Size"));
			    sizeRow.appendChild(new Label("12 x 15"));
			    sizeRow.appendChild(new Label(""));
			    rows.appendChild(sizeRow);
			    
			    Row pagesRow = new Row();
			    pagesRow.appendChild(new Label("Pages"));
			    pagesRow.appendChild(new Label("128"));
			    pagesRow.appendChild(new Label(""));
			    rows.appendChild(pagesRow);
			    
			    Row coverRow = new Row();
			    coverRow.appendChild(new Label("Pages"));
			    coverRow.appendChild(new Label("32"));
			    coverRow.appendChild(new Label(""));
			    rows.appendChild(coverRow);
			    
			    Row textRow = new Row();
			    textRow.appendChild(new Label("Size"));
			    textRow.appendChild(new Label("12 x 15"));
			    textRow.appendChild(new Label(""));
			    rows.appendChild(textRow);
			    
			    Row finishingRow = new Row();
			    finishingRow.appendChild(new Label("Finishing"));
			    finishingRow.appendChild(new Label("Lamination"));
			    finishingRow.appendChild(new Label(""));
			    rows.appendChild(finishingRow);
			    
			    Row shippingDateRow = new Row();
			    shippingDateRow.appendChild(new Label("Shipping Date"));
			    shippingDateRow.appendChild(new Label("11/15/2015"));
			    shippingDateRow.appendChild(new Label(""));
			    rows.appendChild(shippingDateRow);
			    
			    
			    Row headingRow = new Row();
			    headingRow.appendChild(new Label(""));
			    headingRow.appendChild(new Label("Quantity"));
			    headingRow.appendChild(new Label("Uni Price"));
			    rows.appendChild(headingRow);
			    
			    Row detail1Row = new Row();
			    detail1Row.appendChild(new Label("A)"));
			    detail1Row.appendChild(new Label("1000"));
			    detail1Row.appendChild(new Label("5.8"));
			    rows.appendChild(detail1Row);
			    
			    Row detail2Row = new Row();
			    detail2Row.appendChild(new Label("B)"));
			    detail2Row.appendChild(new Label("2000"));
			    detail2Row.appendChild(new Label("4"));
			    rows.appendChild(detail2Row);
			    
			    
			    
				return(detailWin);
	    	}
	    	catch(Exception ex){
	    		UniLog.log(ex);
	    		return(new Window());
	    	}
	    } 
	    
	}
