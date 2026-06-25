package com.uniinformation.webcore;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.XStreamUtil;

/**
 * TODO experiment
 * input json with credential, viewid and condition
 * output return result in json format
 * 
 * 
input
=======
json = 
{ 
   "credential": {
      "login": "xxx",
      "passsword": "xxx"
   },
   "data": {
      "action": "listview",
      "viewId": "xxx",
      "conditions": [ 
         {
             "colName": "xxx", 
             "op": "=", 
             "val1": "xxx",
             "val2": "xxx"
         },
         {
             "colName": "xxx", 
             "op": "=", 
             "val1": "xxx",
             "val2": "xxx"
         }
      ]
   }
}

output:
{
     "data": {
     	"cols": [ 
     	   {
     	      "colName": "xxx",
     	      "colType": "xxx"
     	   },
     	   {
     	      "colName": "xxx",
     	      "colType": "xxx"
     	   },
     	   {
     	      "colName": "xxx",
     	      "colType": "xxx"
     	   }
     	],
     	"vals": [
          [ "111", "222", "333" ],
          [ "111", "222", "333" ],
          [ "111", "222", "333" ]
        ]
     },
     "errorMessage": "",
     "statusCode": 0
}
  
  test url JsonLoader?json=xxx
 *
 */
public class JsonLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean requireSecuredConnection = false; //require ssl!
    public JsonLoader() {
        super();
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
		
		request.isSecure();
		UniLog.log("JsonLoader:isSecure:" + request.isSecure());
		if (requireSecuredConnection && !request.isSecure()){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		//very slow !!
		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper(request, response);
		UniLog.log("sessionHelper: isLogin:1:" + sessionHelper.isLogin());
		if (!sessionHelper.isLogin()){
			UniLog.log("perform login");
			//sessionHelper.login("login", "password");
			//TODO obtain login / password from parameter
		}
		UniLog.log("sessionHelper: isLogin:2:" + sessionHelper.isLogin());
		if (!sessionHelper.isLogin()){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		BiResult biResult = getQueryResult(sessionHelper,"BdStep", null, null);
		if (biResult == null){
			UniLog.log("biResult is null");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
//		TableRec tr = biResult.getResultTr();
		Vector resultSet = new Vector();
		
//        for (int i=0; i < tr.getRecordCount(); i++) {
         Vector listColumn = biResult.getListColumns();
        for (int i=0; i < biResult.getRowCount(); i++) {
           biResult.loadOneRecV(i);
//           tr.setRecPointer(i);
           Vector curRec = new Vector();
//          for (int j=0; j<tr.getFieldCount(); j++){
          for (int j=0; j<listColumn.size(); j++){
//        	   curRec.add(tr.getField(j));
        	   curRec.add(biResult.getCell(
        			   	((BiColumn) listColumn.get(i)).getLabel()));
           }
           resultSet.add(curRec);
        }
        JSONArray jCols = new JSONArray();
        
			Vector v = biResult.getColumns();
		for(int i=0;i<v.size();i++) {
			BiColumn biColumn = (BiColumn) v.get(i);
			UniLog.log("Column:"+biColumn.getLabel() + " " + biColumn.getEngName() + " " + biColumn.getChnName() + " " + biColumn.getColumnType() + " " + biColumn.getColumnLength() );
			JSONObject j = new JSONObject();
			j.put("colName",biColumn.getLabel());
			j.put("colType",biColumn.getColumnType());
			jCols.put("HAHA");
			
			
			Vector tt = biColumn.getTableDepends();
        
        }
		
		JSONObject j = new JSONObject();
		JSONArray ja = new JSONArray(resultSet);
		
		j.put("statusCode",0);
		j.put("errorMessage","");
		//j.put("cols",v2);
		
		JSONObject jData = new JSONObject();
		j.put("data",jData);
		JSONArray jVals = new JSONArray(resultSet);
		jData.put("vals", jVals);
		/*
	    response.setCharacterEncoding("UTF-8");
	    response.setContentType("application/json; charset=UTF-8");
		response.getOutputStream().print(j.toString(5));
		response.getOutputStream().flush();
		*/
	    response.setCharacterEncoding("UTF-8");
	    response.setContentType("application/json; charset=UTF-8");
		PrintWriter pw = response.getWriter();
		pw.print(j.toString(3));
		
		}
		catch(Exception ex){
			ex.printStackTrace();
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
    BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, Vector p_sortIdxs, String p_wherecl)
    {
    	try {
    		BiSchema schema;
    		schema = (BiSchema) sessionHelper.getSessionData("biSchema");
    		if(schema == null) {
    			schema = BiSchema.loadSchema(sessionHelper);
    		}
    		UniLog.log("queryResult schema:"+schema);
    		if(schema == null) return(null); 
    		BiView view = schema.getViewByName(p_viewid);
    		UniLog.log("queryResult view:"+view);
    		if(view == null) return(null); 

			BiResult result = view.newBiResult(null,null,null,sessionHelper);
			UniLog.log("call view.newBiResult() return:" + result);
    		if(result == null) 
    			return(null); 
			
			result.clearOrderBy();
			if(p_sortIdxs != null) {
				for (int i=0;i<p_sortIdxs.size();i+=2){
					//result.addOrderByColumnList(Integer.parseInt(p_sortIdxs.elementAt(i).toString()), ((Boolean) p_sortIdxs.elementAt(i+1)).booleanValue()); 
					result.addOrderByColumnList(p_sortIdxs.elementAt(i).toString(), ((Boolean) p_sortIdxs.elementAt(i+1)).booleanValue()); 
				}
			}
			else{
				result.clearOrderBy();
			}
			
			result.clearCondition();
			if (p_wherecl != null){
				result.addCondition(new Vector(),p_wherecl);
				UniLog.log("addCondition:"+p_wherecl);
			}
			boolean queryStatus = result.query(true).getStatus();
    		UniLog.log("queryResult queryStatus:"+queryStatus);
			if (!queryStatus){
				return(null);
			}
			result.sort();
			result.close(); //close the db connection
    		return(result);
    	} catch (Exception ex) {
    		UniLog.log(ex);
    		return(null);
    	}
    }

}
