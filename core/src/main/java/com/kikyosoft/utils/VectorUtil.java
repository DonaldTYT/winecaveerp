package com.kikyosoft.utils;

import java.util.*;

import com.kikyosoft.utils.*;

public class VectorUtil implements ToXMLInterface {
	Vector v;
   public VectorUtil() {
	   v = new Vector();
	}
   public VectorUtil(Vector p_v) {
	   v = p_v;
	}
	public VectorUtil(Enumeration p_en) {
		this();
		for (; p_en.hasMoreElements(); ) {
		   v.addElement(p_en.nextElement());
		}
	}
	public VectorUtil addElement(Object p_obj) {
	   v.addElement(p_obj);
		return(this);
	}
	public VectorUtil addElements(Enumeration p_en) {
		for (; p_en.hasMoreElements(); ) {
		   v.addElement(p_en.nextElement());
		}
		return(this);
	}
	public VectorUtil addElements(Object[] p_objarr) {
	   if (p_objarr == null)
		   return(this);
		for (int i=0; i<p_objarr.length; i++) 
	      v.addElement(p_objarr[i]);
		return(this);
	}
	public VectorUtil addElements(int[] p_objarr) {
	   if (p_objarr == null)
		   return(this);
		for (int i=0; i<p_objarr.length; i++) 
	      addElement(p_objarr[i]);
		return(this);
	}
	public VectorUtil addElements(double[] p_objarr) {
	   if (p_objarr == null)
		   return(this);
		for (int i=0; i<p_objarr.length; i++) 
	      addElement(p_objarr[i]);
		return(this);
	}
	public VectorUtil addElements(String[] p_objarr) {
	   if (p_objarr == null)
		   return(this);
		for (int i=0; i<p_objarr.length; i++) 
	      addElement(p_objarr[i]);
		return(this);
	}
	public VectorUtil addElements(Vector p_v) {
	   if (p_v == null)
		   return(this);
		for (int i=0; i<p_v.size(); i++) {
	      v.addElement(p_v.elementAt(i));
		}
		return(this);
	}
	public VectorUtil addElements(Vector p_v, int p_offset, int p_cnt) {
	   if (p_v == null)
		   return(this);
		if (p_offset < 0 || p_offset >= p_v.size())
		   return(this);
		int cnt = p_cnt;
		if (cnt < 0) 
		   cnt = p_v.size() - p_offset;
		else if (cnt > p_v.size() - p_offset)
		   cnt = p_v.size() - p_offset;
		for (int i=0; i<cnt; i++) 
	      v.addElement(p_v.elementAt(p_offset+i));
		return(this);
	}
	public VectorUtil addElement(int p_int) {
	   v.addElement(new Integer(p_int));
		return(this);
	}
	public VectorUtil addElement(double p_double) {
	   v.addElement(new Double(p_double));
		return(this);
	}
	public Vector toVector() {
	   return(v);
	}
	public StringBuffer toXML(StringBuffer p_sb) {
	   StringBuffer sb;
		if (p_sb == null)
		   sb = new StringBuffer();
		else
		   sb = p_sb;
		sb.append("<VectorUtil>")
		  .append("<size>").append(v.size()).append("</size>");
	   for (int i=0; i<v.size(); i++) {
		   sb.append("<entry idx=\"").append(i).append("\">");
			if (v.elementAt(i) instanceof ToXMLInterface)
			   ((ToXMLInterface) v.elementAt(i)).toXML(sb);
			else {
				if (v.elementAt(i) != null)
			      sb.append(v.elementAt(i).toString());
			}
		   sb.append("</entry>");
		}
		sb.append("</VectorUtil>");
		return(sb);
	}
	public String[] toStringArray() {
	   String[] arr = new String[v.size()];
		for (int i=0; i<v.size(); i++) {
		   arr[i] = v.elementAt(i).toString();
		}
		return(arr);
	}
	public double[] toDoubleArray() {
	   double[] arr = new double[v.size()];
		for (int i=0; i<v.size(); i++) {
			Object o = v.get(i);
			if(o instanceof Boolean) {
				if(((Boolean) o).booleanValue()) arr[i] = 1.0;
				else arr[i] = 0.0;
			} else {
				if(o instanceof Number) {
		  			arr[i] = ((Number) o).doubleValue();
				} else {
					arr[i] = 0.0;
				}
			}
		}
		return(arr);
	}
	public Hashtable toHashtable() {
		if (v == null)
		   return(null);
		Hashtable h = new Hashtable();
		for (int i=0; i<v.size(); i++) {
		   Object obj = v.elementAt(i);
		   h.put(obj, obj);
		}
		return(h);
	}
	public static int addUnique(Vector p_v,Object p_o)
	{
		if(p_v == null) return(-1);
		for(int i = 0;i < p_v.size();i++) {
			if(p_o != null) {
				if(p_o.equals(p_v.get(i))) return(i);
			} else {
				if(p_v.get(i) == null) return(i);
			}
		} 
		p_v.add(p_o);
		return(p_v.size()-1);
	}
   public static void moveElement(Vector p_vector, int p_index,int p_offset) throws Exception{
	   if (p_index < 0 || p_index >= p_vector.size()){
		   throw new Exception("Element does not exist");
		}
	   Object tmpObj = p_vector.elementAt(p_index);
		p_vector.remove(p_index);

		int tmpNewIndex = p_index +p_offset;
		if (tmpNewIndex < 0){
		   LogUtil.log("out of boundary, move to top");
			tmpNewIndex = 0;
		}
		if (tmpNewIndex > p_vector.size()){
		   LogUtil.log("out of boundary, move to bottom");
		   tmpNewIndex = p_vector.size();
		}
		p_vector.insertElementAt(tmpObj, tmpNewIndex);
	}
	public static void main(String[] args) {
	   LogUtil.log(""+new VectorUtil()
								.addElement("haha1")
								.addElement("haha2")
								.addElement("haha3")
								.addElement("haha4")
		                  .toVector());

      Vector v = new VectorUtil()
								.addElement("haha1")
								.addElement("haha2")
								.addElement("haha3")
								.addElement("haha4")
		                  .toVector();
		try{
	      VectorUtil.moveElement(v,3,-999);
		}
		catch(Exception e){
		   LogUtil.log(e);
		}
  		for (int i=0; i<v.size();i++){
  		   System.out.println(""+v.elementAt(i));
  		}
		Vector<Object> vec = VectorUtil.of("a",1);
		for (int i=0;i<vec.size();i++){
			LogUtil.log(i+":"+vec.elementAt(i));
		}
	}
	public String toString() {
	   return(toXML(new StringBuffer()).toString());
	}
	public static Vector nullToEmpty(Vector p_v){
		return p_v == null ? new Vector(): p_v;
	}
	public static Vector safe(Vector p_v){
		return nullToEmpty(p_v);
	}
	public static <V>Vector <V>of(Object... p_values){
		Vector<V> vector = new Vector<V>();
		if (p_values == null || p_values.length == 0){
			return(vector);
		}
	    for (int i=0; i<p_values.length; i++) {
	    	vector.add((V)p_values[i]);
	    }
	    return vector;	
	}	
}
