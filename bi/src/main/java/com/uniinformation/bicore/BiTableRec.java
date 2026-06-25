package com.uniinformation.bicore;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TableRecException;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiTableRec implements java.io.Serializable {
	TableRec tr;
	BiTable bt;
	String versionAgent = null;
	ArrayList<Integer> trVersion;
	public BiTableRec(BiTable p_table,String p_versionAgent) throws Exception {
		bt = p_table;
		versionAgent = p_versionAgent;
	}
	/*
	public BiTableRec(SelectUtil su,BiTable p_table,int p_serialid,String p_versionAgent) throws Exception {
		bt = p_table;
		versionAgent = p_versionAgent;
		if(p_serialid <= 0) {
			Object fds[] = (bt.getCollection("fields").getCollections());
			String fdname[] = new String[fds.length];
			int fdtype[] = new int[fds.length];
			for(int i=0;i<fds.length;i++) {
				BiField fd = (BiField) fds[i];
				fd.getName();
				fd.getFieldType();
				fdname[i] = fd.getName();
				fdtype[i] = BiSchema.getSqlTypeFromFieldType(fd.getFieldType());
			}
			tr = new TableRec(bt.getName(),fdname,fdtype);
		} else {
			tr = su.getQueryResult( "select * from "+bt.getSelectFromName()+ " where " + bt.getSerialId() + " = "+p_serialid,null);
			tr.setRecPointer(0);
		}
	}
	*/
	public int setRecPointer(int p_recPointer)  
       throws TableRecException {
		return(tr.setRecPointer(p_recPointer));
	}
	public Object getField(int index) throws TableRecException {
		return(tr.getField(index));
	}
	public Object getField(int index,int row) throws TableRecException {
		return(tr.getField(index,row));
	}
	public Object getField(String p_key) throws TableRecException {
		return(tr.getField(p_key));
	}
	public int getFieldInt(String p_key) throws TableRecException {
		return(tr.getFieldInt(p_key));
	}
	public Object[] getRecord(int p_idx) {
		return(tr.getRecord(p_idx));
	}
	public void setField(int index, Object value) 
               throws TableRecException {
		tr.setField(index, value);
	}
	public void deleteRecord(int row) throws TableRecException {
		tr.deleteRecord(row);
	}
	public int getRecordCount() {
      return tr.getRecordCount();
	}
	public int size() {
	   return(tr.size());
	}
   	public TableRec getTableRec() {
		return(tr);
	}
   	public int addRecord() throws TableRecException {
   		if(tr == null) {
			Object fds[] = (bt.getCollection("fields").getCollections());
			String fdname[] = new String[fds.length];
			int fdtype[] = new int[fds.length];
			for(int i=0;i<fds.length;i++) {
				BiField fd = (BiField) fds[i];
				fd.getName();
				fd.getFieldType();
				fdname[i] = fd.getName();
				fdtype[i] = BiSchema.getSqlTypeFromFieldType(fd.getFieldType());
			}
			tr = new TableRec(bt.getName(),fdname,fdtype);	
   		}
   		return(tr.addRecord());
   	}
   	public void setField(String key, Object value) throws TableRecException{
   		tr.setField(key, value);
   	}

	public void deleteBiTableRecBySerialId(SelectUtil su,int p_sid) throws Exception {
		su.executeUpdate("delete from " + bt.getDbtName(),
			new Wherecl().andUniop(bt.getSerialId(), "=",p_sid).stripAnd());
		
	}
	
	public void queryBiTableRecBySerialId(SelectUtil su,int p_sid) throws Exception {
		int sidx;
		if(tr == null) {
			sidx = 0; 
		} else sidx = tr.getRecordCount();
		if(versionAgent != null) {
			if(trVersion == null)	trVersion = new ArrayList<Integer>();
			Hashtable<Integer,Integer> vh = getVersionHash(versionAgent,bt.getDbtName());
			Integer ver = vh.get(p_sid);
			if(ver == null) ver = 0;
			trVersion.add(ver);
		}
		tr = su.getQueryResultWithMaxCount(
				"select * from "+bt.getSelectFromName()+ " where " + bt.getSerialId() + " = "+p_sid,null,false,-1,tr
			);
		if(tr.getRecordCount() != sidx + 1) throw new Exception ("BiTableRec Query Error");
		tr.setRecPointer(sidx);
	}
   	
   	public void doUpdate(SelectUtil su,Vector colList) throws Exception{
   		for(int i=0;i<tr.getRecordCount();i++) {
   			tr.setRecPointer(i);
   			if(versionAgent != null) {
   				Hashtable<Integer,Integer> versionHash = getVersionHash(versionAgent,bt.getDbtName());
   				int sid = tr.getFieldInt(bt.getSerialId());
   				Integer ver = versionHash.get(sid);
   				if(ver == null) ver = 0;
   				if(ver > trVersion.get(i)) {
   					throw new Exception ("Update Denied By Version Control");
   				}
   				synchronized(versionHash) {
   					ver++;
   					versionHash.put(sid, ver);
   				}
   			}
   			su.updateByTableRec(bt.getDbtName(), tr, colList, colList, 
				new Wherecl().andUniop(bt.getSerialId(), "=",tr.getField(bt.getSerialId()) ).stripAnd());
   		}
   	}
   	public void doAdd(SelectUtil su) throws Exception{
   		for(int i=0;i<tr.getRecordCount();i++) {
   			tr.setRecPointer(i);
			su.insertByTableRec(bt.getDbtName(), tr, true, bt.getSerialId());
   		}
   	}
   	/*
	void addRecord(Object[] rec,int p_row) throws TableRecException {
		// not yet implemented
		tr.addRecord(p_row);
		Object[] newrec = tr.getRecord(p_row);
		for(int i=0;i<rec.length;i++) {
			newrec[i] = rec[i];
		}
	}
	void setRecord(int p_recidx,Object[] p_rec) {
		// not yet implemented
	}
	*/
   	
	static Hashtable<String,Hashtable<String,Hashtable<Integer,Integer>>> versionHash = new Hashtable<String,Hashtable<String,Hashtable<Integer,Integer>>> ();
	
   	Hashtable<Integer,Integer> getVersionHash(String p_agent,String p_tabName) {
		Hashtable<String,Hashtable<Integer,Integer>> agentVersionHash = versionHash.get(p_agent);
		if(agentVersionHash == null) {
			synchronized(versionHash) {
				if((agentVersionHash = versionHash.get(p_agent)) == null) {
					agentVersionHash = new Hashtable<String,Hashtable<Integer,Integer>> ();
					versionHash.put(p_agent, agentVersionHash);
				}
			}
		}
		Hashtable<Integer,Integer> tableVersionHash = agentVersionHash.get(p_tabName);
		if(tableVersionHash == null) { 
			synchronized(agentVersionHash) {
				if((tableVersionHash = agentVersionHash.get(p_tabName)) == null) {
					tableVersionHash = new Hashtable<Integer,Integer> ();
					agentVersionHash.put(p_tabName, tableVersionHash);
				}
			}
		}
		return(tableVersionHash);
   	}
}
