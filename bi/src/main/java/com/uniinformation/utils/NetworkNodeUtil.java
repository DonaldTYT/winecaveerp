package com.uniinformation.utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkNodeUtil {
	public static class LinkAttribute {
		boolean singleLevel;
		Object data;
		public LinkAttribute(boolean p_isSingle,Object p_data) {
			singleLevel = p_isSingle;
			data = p_data;
		}
	}
	boolean allowLoop = true;
	private static class Node {
		Object value;
		Set<String> childKeys = new HashSet<String>();
		Hashtable<String,LinkAttribute> parentKeys = new Hashtable<String,LinkAttribute>();
	}
	private final Map<String, Node> mNodeMap = new HashMap<String, Node>();
	/***
	 * parse the p_json and set the data structure
	 * @param p_json
	 * @return
	 */
	public static NetworkNodeUtil readJson(JSONObject p_json) throws JSONException, Exception {
		NetworkNodeUtil nnu = new NetworkNodeUtil();
		JSONArray jsonArray = p_json.getJSONArray("root");
		if (jsonArray != null)
			readJson(nnu, null, jsonArray);
		return (nnu);
	}
	/***
	 * parse the p_json and set the data structure
	 * @param nnu
	 * @param p_json
	 * @return
	 * @throws JSONException 
	 */
	public static void readJson(NetworkNodeUtil nnu, String parentKey, JSONArray p_jsonArray) throws JSONException, Exception {
		for (int i = 0; i < p_jsonArray.length(); i++) {
			JSONObject node = p_jsonArray.getJSONObject(i);
			String key = node.getString("key");
			Object value = node.get("value");
			if (key != null) {
				nnu.addNode(key, value);
				if (parentKey != null)
					nnu.addParent(key, parentKey,new LinkAttribute(false,null));
				try {
					JSONArray child = node.getJSONArray("children");
					if (child != null)
						readJson(nnu, key, child);
				} catch (JSONException e) {
					//System.out.println("readJson " + key + " children error " + e.toString());
				}
			}
		}
	}
	
	/***
	 * convert data structure to json 
	 * @return
	 */
	public synchronized JSONObject toJson() throws JSONException {
		JSONObject jsonRoot = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (String key : mNodeMap.keySet()) {
			Node node = mNodeMap.get(key);
			if (node != null && node.parentKeys.isEmpty())
				toJson(key, jsonArray);
		}
		jsonRoot.put("root", jsonArray);
		return jsonRoot;
	}
	public synchronized void toJson(String nodeKey, JSONArray jsonArray) throws JSONException {
		Node node = mNodeMap.get(nodeKey);
		if (node == null)
			return;
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("key", nodeKey);
		jsonObj.put("value", node.value);
		if (!node.childKeys.isEmpty()) {
			JSONArray jsonChildArr = new JSONArray();
			for (String childNodeKey : node.childKeys)
				toJson(childNodeKey, jsonChildArr);
			jsonObj.put("children", jsonChildArr);
		}
		jsonArray.put(jsonObj);
	}
	
	/***
	 * add new node
	 * @param p_nodeKey
	 * @param p_valObj
	 * @return
	 */
	public synchronized NetworkNodeUtil addNode(String p_nodeKey, Object p_valObj){
		//suppose nodeKey is global unique
//		if (!mNodeMap.containsKey(p_nodeKey)) {
		Node node = mNodeMap.get(p_nodeKey);
		if (node == null) {
			node = new Node();
			node.value = p_valObj;
			mNodeMap.put(p_nodeKey, node);
		} else {
			node.value = p_valObj;
		}
		return(this);
	}
	
	/***
	 * remove node by key
	 * @param p_nodeKey
	 * @return
	 */
	public synchronized NetworkNodeUtil removeNode(String p_nodeKey) throws Exception {
		return removeNode(p_nodeKey, false);
	}
	/***
	 * remove node by key
	 * @param p_nodeKey
	 * @param p_joinFlag
	 * @return
	 */
	
	/* Modified By DT, when join flag is true, only join children of deleted node to it's parent if
	    the child is not already is child of the parent inherit from other path
	 */
	public synchronized NetworkNodeUtil removeNode(String p_nodeKey, boolean p_joinFlag) throws Exception {
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null) {
			for (String parentKey : node.parentKeys.keySet()) {
				Node parentNode = mNodeMap.get(parentKey);
				if (parentNode != null) {
					parentNode.childKeys.remove(p_nodeKey);
					/*
					if (p_joinFlag) {
						for (String childKey : node.childKeys)
							parentNode.childKeys.add(childKey);
					}
					*/
				}
			}
			for (String childKey : node.childKeys) {
				Node childNode = mNodeMap.get(childKey);
				if (childNode != null) {
					childNode.parentKeys.remove(p_nodeKey);
					/*
					if (p_joinFlag) {
						for (String parentKey : node.parentKeys)
							childNode.parentKeys.add(parentKey);
					}
					*/
					childNode = null;
				}
			}
			mNodeMap.remove(p_nodeKey);
			if(p_joinFlag) {
				for(String childKey : node.childKeys) {
					for (String parentKey : node.parentKeys.keySet()) {
						if(!isParent(childKey,parentKey)) {
							addParent(childKey, parentKey,node.parentKeys.get(parentKey));
							
						}
//	private synchronized boolean isParent(String p_nodeKey, String p_parentNodeKey){
					}
				}
			}
		}
		return(this);
	}
	/*
	public synchronized NetworkNodeUtil removeNode(String p_nodeKey, boolean p_joinFlag){
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null) {
			for (String parentKey : node.parentKeys) {
				Node parentNode = mNodeMap.get(parentKey);
				if (parentNode != null) {
					parentNode.childKeys.remove(p_nodeKey);
					if (p_joinFlag) {
						for (String childKey : node.childKeys)
							parentNode.childKeys.add(childKey);
					}
				}
			}
			for (String childKey : node.childKeys) {
				Node childNode = mNodeMap.get(childKey);
				if (childNode != null) {
					childNode.parentKeys.remove(p_nodeKey);
					if (p_joinFlag) {
						for (String parentKey : node.parentKeys)
							childNode.parentKeys.add(parentKey);
					}
				}
			}
			mNodeMap.remove(p_nodeKey);
		}
		return(this);
	}
	*/
	
	/***
	 * get node value by key
	 * @param p_nodeKey
	 * @return
	 */
	public synchronized Object getNode(String p_nodeKey){
		Node node = mNodeMap.get(p_nodeKey);
		return node != null ? node.value : null;
	}
	
	/***
	 * set parent node
	 * @param p_nodeKey
	 * @param p_parentNodeKey
	 * @return
	 */
	public NetworkNodeUtil addParent(String p_nodeKey, String p_parentNodeKey,boolean p_isSingle) throws Exception {
		return(addParent(p_nodeKey, p_parentNodeKey,new LinkAttribute (p_isSingle,null)));
	}
	public synchronized NetworkNodeUtil addParent(String p_nodeKey, String p_parentNodeKey,LinkAttribute p_attr) throws Exception{
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null && p_parentNodeKey != null) {
			Node parentNode = mNodeMap.get(p_parentNodeKey);
			if (parentNode != null) {
				//if (parentNode.parentKeys.contains(p_nodeKey))
				if (isParent(p_parentNodeKey, p_nodeKey))
					throw new Exception("addParent error: parentKeys of '" + p_parentNodeKey + "' already contains '" + p_nodeKey + "'");
				node.parentKeys.put(p_parentNodeKey,p_attr);
				parentNode.childKeys.add(p_nodeKey);
			}
		}
		return(this);
	}
	/***
	 * set child node
	 * @param p_nodeKey
	 * @param p_childNodeKey
	 * @return
	 */
	public NetworkNodeUtil addChild(String p_nodeKey, String p_childNodeKey,boolean p_isSingle) throws Exception {
		return(addChild(p_nodeKey, p_childNodeKey,new LinkAttribute(p_isSingle,null))) ;
		
	}
	public synchronized NetworkNodeUtil addChild(String p_nodeKey, String p_childNodeKey,LinkAttribute p_attr) throws Exception {
		boolean singleLevel = p_attr.singleLevel;
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null && p_childNodeKey != null) {
			Node childNode = mNodeMap.get(p_childNodeKey);
			if (childNode != null) {
				//if (childNode.childKeys.contains(p_nodeKey))
				/*
				if (isChild(p_childNodeKey, p_nodeKey)) {
//					throw new Exception("addChild error: childKeys of '" + p_childNodeKey + "' already contains '" + p_nodeKey + "'");
					UniLog.log("addChild error: childKeys of '" + p_childNodeKey + "' already contains '" + p_nodeKey + "' skipped");
				} else {
					node.childKeys.add(p_childNodeKey);
					childNode.parentKeys.put(p_nodeKey,p_singleLevel);
				}
				
				*/
				if (isChild(p_childNodeKey, p_nodeKey)) {
					if(!allowLoop) throw new Exception("addChild error: childKeys of '" + p_childNodeKey + "' already contains '" + p_nodeKey + "'");
					UniLog.log("addChild error: childKeys of '" + p_childNodeKey + "' already contains '" + p_nodeKey + "' force singleLevel ");
					singleLevel = true;
				}
				node.childKeys.add(p_childNodeKey);
				childNode.parentKeys.put(p_nodeKey,new LinkAttribute(singleLevel,p_attr.data));
			}
		}
		return(this);
	}
	
	/***
	 * check is child node
	 * @param p_nodeKey
	 * @param p_childNodeKey
	 * @return
	 */
	public synchronized boolean isChild(String p_nodeKey, String p_childNodeKey){
		//remark: all child/parent function need to handle dead loop situation
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null && p_childNodeKey != null) {
			for (String childKey : node.childKeys) {
				Node childNode = mNodeMap.get(childKey);
				if (childNode != null) {
//					if (childKey.equals(p_childNodeKey) || isChild(childKey, p_childNodeKey))
					if (childKey.equals(p_childNodeKey)) return true;
					boolean isSingleLevel = childNode.parentKeys.get(p_nodeKey).singleLevel;
					if(!isSingleLevel && isChild(childKey,p_childNodeKey)) return(true);
				}
			}
		}
		return(false);
	}
	
	/***
	 * check is parent node
	 * @param p_nodeKey
	 * @param parentNodeKey
	 * @return
	 */
	synchronized boolean isParent_real(String p_nodeKey, String p_parentNodeKey,boolean p_checkSingleLevel){
		//remark: all child/parent function need to handle dead loop situation
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null && p_parentNodeKey != null) {
			for (String parentKey : node.parentKeys.keySet()) {
				Node parentNode = mNodeMap.get(parentKey);
				if(p_checkSingleLevel && node.parentKeys.get(parentKey).singleLevel) continue;
				if (parentNode != null) {
					if (parentKey.equals(p_parentNodeKey) || isParent_real(parentKey, p_parentNodeKey,true))
						return true;
				}
			}
		}
		return(false);
	}
	public boolean isParent(String p_nodeKey, String p_parentNodeKey){
		return( isParent_real(p_nodeKey, p_parentNodeKey,false));
	}
	
	/***
	 * get List of parent
	 * @param p_nodeKey
	 * @param p_immediateOnly - true return 1 level, false return all level
	 * @return
	 */
	public synchronized List<String> getRootList(){
		List<String> list = new ArrayList<String>();
		for (String key : mNodeMap.keySet()) {
			List<String> pl = getParentList(key, true);
			if(pl == null || pl.size() <= 0) list.add(key);
		}
		return(list);
	}
	public synchronized List<String> getTerminalList(){
		List<String> list = new ArrayList<String>();
		for (String key : mNodeMap.keySet()) {
			List<String> pl = getChildList(key, true);
			if(pl == null || pl.size() <= 0) list.add(key);
		}
		return(list);
	}
	public synchronized List<String> getParentList(String p_nodeKey, boolean p_immediateOnly){
		//remark: all child/parent function need to handle dead loop situation
		List<String> list = new ArrayList<String>();
		getParentList(list, p_nodeKey, p_immediateOnly);
		return list;
	}
	/***
	 * get List of parent
	 * @param p_nodeList
	 * @param p_nodeKey
	 * @param p_immediateOnly - true return 1 level, false return all level
	 * @return
	 */
	private synchronized void getParentList(List<String> p_nodeList, String p_nodeKey, boolean p_immediateOnly){
		//remark: all child/parent function need to handle dead loop situation
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null) {
			for (String parentKey : node.parentKeys.keySet()) {
				Node parentNode = mNodeMap.get(parentKey);
				if (parentNode != null) {
					boolean immediateOnly = p_immediateOnly;
					if(node.parentKeys.get(parentKey).singleLevel) {
						immediateOnly = true;
					}
					p_nodeList.add(parentKey);
//					if (p_immediateOnly)
//						return;
					if(!immediateOnly) getParentList(p_nodeList, parentKey, p_immediateOnly); // is use p_immedateOnly or immediateOnly has same effect ? assumed yes
				}
			}
		}
	}
	
	/***
	 * get List of child
	 * @param p_nodeKey
	 * @param p_immediateOnly - true return 1 level, false return all level
	 * @return
	 */
	public synchronized List<String> getChildList(String p_nodeKey, boolean p_immediateOnly){
		//remark: all child/parent function need to handle dead loop situation
		List<String> list = new ArrayList<String>();
		getChildList(list, p_nodeKey, p_immediateOnly);
		return list;
	}
	/***
	 * get List of child
	 * @param p_nodeList
	 * @param p_nodeKey
	 * @param p_immediateOnly - true return 1 level, false return all level
	 * @return
	 */
	private synchronized void getChildList(List<String> p_nodeList, String p_nodeKey, boolean p_immediateOnly){
		Node node = mNodeMap.get(p_nodeKey);
		if (node != null) {
			for (String childKey : node.childKeys) {
				Node childNode = mNodeMap.get(childKey);
				if (childNode != null) {
					p_nodeList.add(childKey);
//					if (p_immediateOnly)
//						return;
					if(!p_immediateOnly) getChildList(p_nodeList, childKey, p_immediateOnly);
				}
			}
		}
	}
	public synchronized void removeLink(String p_parent, String p_child) {
		Node n0 = mNodeMap.get(p_parent);
		if(n0 != null) {
			n0.childKeys.remove(p_child);
		}
		n0 = mNodeMap.get(p_child);
		if(n0 != null) {
			n0.parentKeys.remove(p_parent);
		}
	}
	
	public List <Object> getNodeValueList() {
		List <Object> l = new ArrayList <Object>();
		for(Iterator e = mNodeMap.values().iterator();e.hasNext();) {
			Node n = (Node) e.next();
			l.add(n.value);
		}
		return(l);
	}
	
	public void clear()
	{
		mNodeMap.clear();
	}
	public void clearAllLinks()
	{
		for(Iterator e = mNodeMap.values().iterator();e.hasNext();) {
			Node n = (Node) e.next();
			n.childKeys.clear();
			n.parentKeys.clear();
		}
	}
	
	public static void main(String args[]) {
		//create a network data structure
		/*
		 * node1 - node1.1 - node1.1.1
		 *       - node1.2
		 *      
		 * {"root":[
		 * 		{"key":"1","value":"n1","children":[
		 * 			{"key":"1.1","value":"n1.1","children":[{"key":"1.1.1","value":"n1.1.1"}]},
		 * 			{"key":"1.2","value":"n1.2"}]}
		 * 		]
		 * }
		 * node1 - node1.1 
		 *       - node1.2
		 * node1.1.1 - node1.1
		 */
		NetworkNodeUtil nnu = new NetworkNodeUtil();
		nnu.addNode("1", "n1")
		   .addNode("1.1", "n1.1")
		   .addNode("1.2", "n1.2")
		   .addNode("1.1.1", "n1.1.1");
		try {
			nnu.addParent("1", null,false)
				.addParent("1.1", "1",false)
				.addParent("1.2", "1",false)
				.addParent("1.1.1", "1.1",false)
				.addParent("1.1", "1.1.1",false);
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
		System.out.println("nnu.isParent(\"1.1\", \"1\") return " + nnu.isParent("1.1", "1"));//return true
		System.out.println("nnu.isChild(\"1\", \"1.1\") return " + nnu.isChild("1", "1.1"));//return true
		System.out.println("nnu.isChild(\"1\", \"1.1.1\") return " + nnu.isChild("1", "1.1.1"));//return true
		System.out.println("nnu.isChild(\"1.1.1\", \"1\") return " + nnu.isChild("1.1.1", "1"));//return false
		System.out.println("nnu.getNode('1.2') return " + nnu.getNode("1.2"));//return 1.2
		
		JSONObject json = null;
		try {
			json = nnu.toJson();  //convert current data structure to JsonString
			System.out.println("nnu json return " + json.toString(5));//return true
		} catch (JSONException ex) {
			System.out.println("toJson error " + ex.toString());
		}
		
		
		//add node 2 and node 2.1, node 2.1 with 2 parents
		/*m
		 * node1 - node1.1 - node1.1.1
		 *       - node1.2 - node2.1
		 * node2 - node2.1
		 *      
		 * {"root":[
		 * 		{"key":"1","value":"n1","children":[
		 * 			{"key":"1.1","value":"n1.1","children":[{"value":"n1.1.1"}]},
		 * 			{"key":"1.2","value":"n1.2","children":[{"value":"n2.1"}]}]
		 * 			},
		 * 		{"key":"2","value":"n2","children":[
		 * 			{"key":"2.1","value":"n2.1"}]
		 * 			}
		 * 		]
		 * }
		 */
		NetworkNodeUtil nnu2 = null;
		try {
			nnu2 = NetworkNodeUtil.readJson(json);
			nnu2.addNode("2", "n2")
			 	.addNode("2.1", "n2.1");
			nnu2.addParent("2", null,false)
			   .addParent("2.1", "2",false)
			   .addParent("2.1", "1.2",false);
			printStringList("nnu2.getChildList(\"1\", false) return ", nnu2.getChildList("1", false));
			printStringList("nnu2.getChildList(\"2\", false) return ", nnu2.getChildList("2", false));
			printStringList("nnu2.getParentList(\"1.1.1\", true) return ", nnu2.getParentList("1.1.1", true));//return 1.1
			printStringList("nnu2.getParentList(\"1.1.1\", false) return ", nnu2.getParentList("1.1.1", false));//return 1, 1.1
			printStringList("nnu2.getParentList(\"2.1\", false) return ", nnu2.getParentList("2.1", false));//return 1, 1.2, 2
			System.out.println("nnu2.isParent(\"2.1\", \"2\") return " + nnu2.isParent("2.1", "2"));//return true
			System.out.println("nnu2.isParent(\"2.1\", \"1\") return " + nnu2.isParent("2.1", "1"));//return true
			System.out.println("nnu2.isParent(\"2.1\", \"1.1\") return " + nnu2.isParent("2.1", "1.1"));//return false
			System.out.println("nnu2.isChild(\"1\", \"2.1\") return " + nnu2.isChild("1", "2.1"));//return true
			System.out.println("nnu2.isChild(\"2\", \"2.1\") return " + nnu2.isChild("2", "2.1"));//return true
			System.out.println("nnu2.isChild(\"2.1\", \"1.1.1\") return " + nnu2.isChild("2.1", "1.1.1"));//return false
		} catch (JSONException ex) {
			System.out.println("readJson error: " + ex.toString());
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
		
		try {
			json = nnu2.toJson();  //convert current data structure to JsonString
			System.out.println("nnu2 json return " + json.toString(5));//return true
		} catch (JSONException ex) {
			System.out.println("toJson error: " + ex.toString());
		}
		
		//add node 3 and node 3.1, node 3.2, node 3.3, node 3.2.1
		/*m
		 * node1 - node1.1 - node1.1.1
		 *       - node1.2 - node2.1
		 * node2 - node2.1 - node3.2
		 * node3 - node3.1     
		 *       - node3.2 - node3.2.1
		 *       - node3.3
		 */
		NetworkNodeUtil nnu3 = null;
		try {
			nnu3 = NetworkNodeUtil.readJson(json);
			nnu3.addNode("3", "n3")
			   .addNode("3.1", "n3.1")
			   .addNode("3.2", true)
			   .addNode("3.3", -38)
			   .addNode("3.2.1", 321);
			nnu3.addChild("3", "3.1",false)
				.addChild("3", "3.2",false)
				.addChild("3", "3.3",false)
				.addChild("3.2", "3.2.1",false)
				.addParent("3.2", "2.1",false);
			printStringList("nnu3.getChildList(\"1\", false) return ", nnu3.getChildList("1", false));
			printStringList("nnu3.getChildList(\"2\", false) return ", nnu3.getChildList("2", false));
			printStringList("nnu3.getChildList(\"3\", false) return ", nnu3.getChildList("3", false));
			printStringList("nnu3.getChildList(\"3.1\", false) return ", nnu3.getChildList("3.1", false));
			printStringList("nnu3.getChildList(\"3.2\", false) return ", nnu3.getChildList("3.2", false));
			printStringList("nnu3.getChildList(\"3.3\", false) return ", nnu3.getChildList("3.3", false));
			printStringList("nnu3.getParentList(\"3.2.1\", false) return ", nnu3.getParentList("3.2.1", false));
			System.out.println("nnu3.isChild(\"1\", \"3.2.1\") return " + nnu3.isChild("1", "3.2.1"));//return true
			System.out.println("nnu3.isChild(\"1\", \"3.1\") return " + nnu3.isChild("1", "3.1"));//return false
			System.out.println("nnu3.isParent(\"3.2.1\", \"1\") return " + nnu3.isParent("3.2.1", "1"));//return true
			System.out.println("nnu3.isParent(\"3.2.1\", \"2\") return " + nnu3.isParent("3.2.1", "2"));//return true
			System.out.println("nnu3.isParent(\"3.2.1\", \"3\") return " + nnu3.isParent("3.2.1", "3"));//return true
			System.out.println("nnu3.isParent(\"3.1\", \"2\") return " + nnu3.isParent("3.1", "2"));//return false
			System.out.println("nnu3.getNode('3.2.1') return " + nnu3.getNode("3.2.1"));//return 3.2.1
			System.out.println("nnu3.getNode('3.3') return " + nnu3.getNode("3.3"));//return 3.3
			System.out.println("nnu3.getNode('3.2') return " + nnu3.getNode("3.2"));//return true
		} catch (JSONException ex) {
			System.out.println("readJson error: " + ex.toString());
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

		/*
		 * node4 - node4.1 - node4.1.1
		 *       - node4.2 - node4.2.1 - node4.2.2
		 */
		try {
			JSONObject jsonObj = new JSONObject("{'root':[" + 
			  		"{'key':'4','value':'n4','children':[" +
			  		"	{'key':'4.1','value':'n4.1','children':[{'key':'4.1.1','value':'n4.1.1'}]}," +
			  		"	{'key':'4.2','value':'n4.2','children':[{'key':'4.2.1','value':'n4.2.1','children':[{'key':'4.2.2','value':'n4.2.2'}]}]}]}" +
			  		"]" +
			  		"}");
			NetworkNodeUtil nnu4 = NetworkNodeUtil.readJson(jsonObj);
			printStringList("nnu4.getChildList(\"4\", false) return ", nnu4.getChildList("4", false));
			System.out.println("nnu4.isChild(\"4\", \"4.2.1\") return " + nnu4.isChild("4", "4.2.1"));//return true
			System.out.println("nnu4.isParent(\"4.2.2\", \"4\") return " + nnu4.isParent("4.2.2", "4"));//return true
			System.out.println("nnu4.isParent(\"4.2.2\", \"4.1\") return " + nnu4.isParent("4.2.2", "4.1"));//return false
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NetworkNodeUtil nnu5 = new NetworkNodeUtil();
		nnu5.addNode("a", "na")
		   .addNode("b", "nb")
		   .addNode("c", "nc");
		try {
			nnu5.addChild("a", "b",false)
				.addChild("b", "c",false);
			System.out.println("nnu5 toJson " + nnu5.toJson().toString(5));
			nnu5.removeNode("b", true);
			System.out.println("nnu5 toJson " + nnu5.toJson().toString(5));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkNodeUtil nnu6 = new NetworkNodeUtil();
		nnu6.addNode("a", "na")
		   .addNode("b", "nb")
		   .addNode("c", "nc")
		   .addNode("d", "nd");
		try {
			nnu6.addChild("a", "b",false)
				.addChild("b", "c",false)
				.addChild("a", "d",false)
				.addChild("d", "c",false);
			System.out.println("nnu6 toJson " + nnu6.toJson().toString(5));
			nnu6.removeNode("b");
			System.out.println("nnu6 toJson " + nnu6.toJson().toString(5));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NetworkNodeUtil nnu7 = new NetworkNodeUtil();
		nnu7.addNode("a", "na")
		   .addNode("b", "nb")
		   .addNode("c", "nc")
		   .addNode("d", "nd")
		   .addNode("e", "ne")
		   .addNode("f", "nf");
		try {
			nnu7.addChild("a", "b",false)
				.addChild("b", "c",false)
				.addChild("a", "d",false)
				.addChild("c", "d",false)
				.addChild("d", "e",false)
				.addChild("d", "f",false);
			System.out.println("nnu7 toJson " + nnu7.toJson().toString(5));
			nnu7.removeNode("d", true);
			System.out.println("nnu7 toJson " + nnu7.toJson().toString(5));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void printStringList(String message, List<String> list) {
		String ss = "";
		for (String s : list)
			ss += s + "|";
		System.out.println(message + ss);
	}
	
	public void setAllowLoop(boolean p_sw) {
		allowLoop = p_sw;
	}

	public synchronized Object getLinkData(String p_nodeKey, String p_parentNodeKey) throws Exception{
		Node node = mNodeMap.get(p_nodeKey);
		if(node == null) return(null);
		LinkAttribute attr = node.parentKeys.get(p_parentNodeKey);
		if(attr == null) return(null);
		return(attr.data);
	}
	public synchronized void setLinkData(String p_nodeKey, String p_parentNodeKey,Object p_data) throws Exception{
		Node node = mNodeMap.get(p_nodeKey);
		if(node == null) return;
		LinkAttribute attr = node.parentKeys.get(p_parentNodeKey);
		if(attr == null) return;
		attr.data = p_data;
	}
}
