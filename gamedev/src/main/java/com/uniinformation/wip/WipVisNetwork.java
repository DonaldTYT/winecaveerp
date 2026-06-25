package com.uniinformation.wip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import com.kyoko.common.DateUtil;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;

public class WipVisNetwork {
	
	public static AtomicBoolean fDebug = new AtomicBoolean(false);
	static final public int WIPVIS_CBMODE_NONE_CLICKED = 0;
	static final public int WIPVIS_CBMODE_START_CLICKED = 1;
	static final public int WIPVIS_CBMODE_END_CLICKED = 2;
	static final public int WIPVIS_CBMODE_ARROW_CLICKED = 3;
	static final public int WIPVIS_CBMODE_NODE_CLICKED = 4;
	static final public int WIPVIS_CBMODE_ARROW_ADDED = 5;
	static final public int WIPVIS_CBMODE_ARROW_DELETED = 6;
	static final public int WIPVIS_CBMODE_ARROW_UPDATED = 7;
	static final Set<String> RESERVED_OPTIONS = new HashSet<String>(Arrays.asList("level", "x", "y","id","chosen","label","color"));	
	
	HashMap <Integer,HashMap<String,String>> nodeTypeOptionMap;
	
	
	WipFlow wj;
//	JxForm jxf;
	boolean compactSpacing = false;
	boolean drawStartEnd = true;
	JxField btAddLink;
	JxField btDelLink;
	JxField btUpdLink;
	JxField btRedraw;
	Component visEventDiv;
	String visContentDiv;
	String currentNodeOrEdge = null;
//	String startColor = null;
//	String endColor = null;
	public AtomicBoolean preserveSelectedNode = new AtomicBoolean(true); //for enable/disable preserve selected node
	List<String> selectedNodeList = Collections.synchronizedList(new ArrayList<String>());
	
	int convertToInt(Object o) {
		if(o instanceof String) {
			return((int) Double.parseDouble((String) o));
		} else if(o instanceof Double) {
			return((int) ((Double) o).doubleValue());
		} else if(o instanceof Integer) {
			return((int) ((Integer) o).intValue());
		} 
		return(0);
	}	
	
	WipVisCallbackInterface callback = null;
	
	public void setWorkFlow(WipFlow p_wj) {
		wj = p_wj;
	}
	public WipVisNetwork(Component p_visEventDiv,String p_visContentDiv,JxField p_btAddLink, JxField p_btDelLink,JxField p_btRedraw, JxField p_btUpdLink,final WipVisCallbackInterface p_callback) throws WipException {
//		jxf = p_jxf;
		btAddLink=p_btAddLink;
		btDelLink=p_btDelLink;
		btUpdLink=p_btUpdLink;
		btRedraw =p_btRedraw;
		visEventDiv = p_visEventDiv;
		visContentDiv = p_visContentDiv;
		callback = p_callback;

		nodeTypeOptionMap = new HashMap <Integer,HashMap<String,String>> ();
		
		visEventDiv.addEventListener("onVisselect",  //vis network
				new EventListener() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("Event Received:" + event.getName() +","+event.getData());
						selectedNodeList.clear();
						org.zkoss.json.JSONObject jo = (org.zkoss.json.JSONObject) event.getData();
							org.zkoss.json.JSONArray nodes = (org.zkoss.json.JSONArray) jo.get("nodes");
							org.zkoss.json.JSONArray edges = (org.zkoss.json.JSONArray) jo.get("edges");
							for (int i=0; i<nodes.size(); i++){
								UniLog.log(String.format("nodes :%d:%s", i, nodes.get(i)));
								String s = ""+nodes.get(i);
								selectedNodeList.add(s);
								/*
								if(s.equals("Start")) {
									selectedNodeList.add("End");
								}
								if(s.equals("End")) { 
									selectedNodeList.add("Start");
								}
								*/
							}
							for (int i=0; i<edges.size(); i++){
								UniLog.log(String.format("edges :%d:%s", i, edges.get(i)));
							}
							if(nodes.size() > 0) {
								if(btAddLink != null) {
									if(!nodes.get(0).toString().startsWith("Start") &&
										   !nodes.get(0).toString().endsWith("End")
												) {
									if(btAddLink != null && btAddLink.getText() == "Y") {
										UniLog.log("HAHA add one link");
//										wj.addPreq(wj.getStep(currentNodeOrEdge),wj.getStep(nodes.get(0).toString()));
										wj.addPreq(
												wj.getStep(nodes.get(0).toString()),
												wj.getStep(currentNodeOrEdge)
												);
//										callback.visActionCallBack(WIPVIS_CBMODE_ARROW_ADDED,wj.getStep(currentNodeOrEdge),wj.getStep(nodes.get(0).toString()));
										if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_ARROW_ADDED,
												wj.getStep(nodes.get(0).toString()),
												wj.getStep(currentNodeOrEdge)
												);
//										drawNetwork();
									} else {
										currentNodeOrEdge = nodes.get(0).toString();

										WipStep step = wj.getStep(nodes.get(0).toString());
										if(step != null && step instanceof WipTask) {
											if(((WipTask) step).getState() != WipJob.JOB_STATE_WAITING &&
													((WipTask) step).getState() != WipJob.JOB_STATE_AWAKED) {
												UniLog.log("cannot addlink to started task");
											} else {
												if(btAddLink != null) btAddLink.setEnable(true);
											}
										} else {
											if(btAddLink != null) btAddLink.setEnable(true);
										}
										if(btDelLink != null) btDelLink.setEnable(false);
										if(btUpdLink != null) btUpdLink.setEnable(false);
										if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_NODE_CLICKED,wj.getStep(currentNodeOrEdge),null);
									}
									} else {
										if(btAddLink != null) btAddLink.setEnable(false);
										if(btAddLink != null) btAddLink.setText("N");
										if(btDelLink != null) btDelLink.setEnable(false);
										if(btUpdLink != null) btUpdLink.setEnable(false);
//										drawNetwork(null);
									}
									if(nodes.get(0).toString().startsWith("Start")) {
										if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_START_CLICKED,null,null);
									}
									if(nodes.get(0).toString().startsWith("End")) {
										if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_END_CLICKED,null,null);
									}
								} else {
									currentNodeOrEdge = nodes.get(0).toString();
//									if(btAddLink != null) btAddLink.setEnable(true);
									if(btDelLink != null) btDelLink.setEnable(false);
									if(btUpdLink != null) btUpdLink.setEnable(false);
									if(nodes.get(0).toString().startsWith("Start")) {
										if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_START_CLICKED,null,null);
									} else if(nodes.get(0).toString().startsWith("End")) {
										if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_END_CLICKED,null,null);
									} else {
										if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_NODE_CLICKED,wj.getStep(currentNodeOrEdge),null);
									}
								}
							} else {
								if(edges.size() > 0 && !edges.get(0).toString().startsWith("Start")
									&& !edges.get(0).toString().endsWith("End")) {
									currentNodeOrEdge = edges.get(0).toString();
									boolean canDelLink = true;
									{
										int idx = currentNodeOrEdge.indexOf(':');
										if(idx > 0) {
											String s1 = currentNodeOrEdge.substring(idx+1);
											WipStep step = wj.getStep(s1);
											if(step != null && step instanceof WipTask) {
												if(((WipTask) step).getState() != WipJob.JOB_STATE_WAITING &&
													((WipTask) step).getState() != WipJob.JOB_STATE_AWAKED) {
													UniLog.log("cannot dellink to started task");
													canDelLink = false;
												}
											}
										}
									}
								
									if(btAddLink != null) btAddLink.setEnable(false);
									if(btAddLink != null) btAddLink.setText("N");
									if(btUpdLink != null) btUpdLink.setEnable(true);
									if(canDelLink) {
										if(btDelLink != null) btDelLink.setEnable(true);
									}
									if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_ARROW_CLICKED,null,null);
								} else {
									currentNodeOrEdge = null;
									if(btAddLink != null) btAddLink.setEnable(false);
									if(btAddLink != null) btAddLink.setText("N");
									if(btDelLink != null) btDelLink.setEnable(false);
									if(btUpdLink != null) btUpdLink.setEnable(false);
									if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_NONE_CLICKED,null,null);
								}
							}
							UniLog.log("currentNodeOrEdge = " + currentNodeOrEdge);
							
//						if(nwPopup != null) {
						{
							UniLog.log("show popup");
							org.zkoss.json.JSONObject pointer = (org.zkoss.json.JSONObject) jo.get("pointer");
//							UniLog.log(" x = " + pointer.get("x") + "y = " + pointer.get("y"));
							UniLog.log("pointer " + pointer);
							org.zkoss.json.JSONObject dom = (org.zkoss.json.JSONObject) pointer.get("DOM");
							org.zkoss.json.JSONObject canvas = (org.zkoss.json.JSONObject) pointer.get("canvas");
							UniLog.log("DOM " + dom);
//							UniLog.log("x " + dom.get("x") + " y " + dom.get("y"));
							/*
							int x = (int) ((Double) dom.get("x")).doubleValue();
							int y = (int) ((Double) dom.get("y")).doubleValue();
							int dx =(int) ((Double) canvas.get("x")).doubleValue();
							int dy =(int) ((Double) canvas.get("y")).doubleValue();
							*/
							int x = convertToInt(dom.get("x"));
							int y = convertToInt(dom.get("y"));
							int dx =convertToInt(canvas.get("x"));
							int dy =convertToInt(canvas.get("y"));
							UniLog.log("x " + x + " y " + y + " dx " + dx  + " dy " + dy);
							
//							nwPopup.open(x,y);
						}
					}		
				});
		
//		JxField f = jxf.jxAdd(btAddLink);
		if(btAddLink != null) {
			btAddLink.setAttribute("mode", "toggle");
			Button c = (Button) btAddLink.getNativeObject();
			c.setTooltiptext("Click <Add Link>,then click the child Node to establish a link");	
			btAddLink.addActionListener(
					new JxActionListener() {
						public void actionPerformed(JxField jf) {
							String s;
							s = jf.getText();
							UniLog.log("clicked:" + jf.getName() + " value " + s);	
						}
					}
			);
		}
//		f = jxf.jxAdd(btDelLink);
		if(btDelLink != null) {
			btDelLink.addActionListener(
					new JxActionListener() {
						public void actionPerformed(JxField jf) {
//								String =currentNodeOrEdge  null;
								if(currentNodeOrEdge == null || !currentNodeOrEdge.startsWith("E")) return;
								int idx;
								idx = currentNodeOrEdge.indexOf(':');
								if(idx <= 0) return;
								String s0 = currentNodeOrEdge.substring(1,idx);
								String s1 = currentNodeOrEdge.substring(idx+1);
								UniLog.log("Delete Link " + s0 + "->" + s1);
								try {
									wj.deletePreq(wj.getStep(s0),wj.getStep(s1));
									if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_ARROW_DELETED,wj.getStep(s0),wj.getStep(s1));
//									drawNetwork();
								} catch (Exception ex) {
									UniLog.log(ex);
								}
						}
					}
			);	
		}
		if(btUpdLink != null) {
			btUpdLink.addActionListener(
					new JxActionListener() {
						public void actionPerformed(JxField jf) {
//								String =currentNodeOrEdge  null;
								if(currentNodeOrEdge == null || !currentNodeOrEdge.startsWith("E")) return;
								int idx;
								idx = currentNodeOrEdge.indexOf(':');
								if(idx <= 0) return;
								String s0 = currentNodeOrEdge.substring(1,idx);
								String s1 = currentNodeOrEdge.substring(idx+1);
								UniLog.log("Update Link " + s0 + "->" + s1);
								try {
									if(callback != null) callback.visActionCallBack(WIPVIS_CBMODE_ARROW_UPDATED,wj.getStep(s0),wj.getStep(s1));
								} catch (Exception ex) {
									UniLog.log(ex);
								}
						}
					}
			);	
		}
//		f = jxf.jxAdd(btRedraw);
		if(btRedraw != null) {
			btRedraw.addActionListener(
					new JxActionListener() {
						public void actionPerformed(JxField jf) {
							drawNetwork(null);
						}
					}
			);
		};
	}

	public void drawNetwork() {
		drawNetwork(new Vector());
	}
	/***
	 * 
	 * @param p_init false - do not clear selected node
	 */
	public void drawNetwork(List<String> p_selected) {
		if(p_selected != null) {
			selectedNodeList.clear();
			for(String s : p_selected) {
				selectedNodeList.add(s);
			}
		}
		if(btAddLink != null) btAddLink.setEnable(false);
		if(btDelLink != null) btDelLink.setEnable(false);
		if(btAddLink != null) btAddLink.setText("N");
		List <WipStep> taskList = wj.getStepList(null);
		int maxLevel = wj.getMaxLevel();
		try{
		    final JSONObject jo = new JSONObject();
		    //define node and edge 
		    JSONArray jaNodes = new JSONArray();
		    JSONArray jaEdges = new JSONArray();
		    

		    if(drawStartEnd) {
	    	jaNodes.put(
		    			(new JSONObject()).put("id", "Start")
	    								  .put("level", 0)
	    								  .put("font", (new JSONObject()) .put("color", (wj.getStartFontColor() == null ? "black" : wj.getStartFontColor())))
	    								  .put("color", wj.getStartColor() == null ? "grey" : wj.getStartColor())
	    								  .put("label", "Start")
	    								  .put("shape", "circle")
   										  .put("borderWidthSelected",4)
	    			);
		    }
		    for(int i = 0;i<taskList.size();i++) {
		    	WipStep tn  = taskList.get(i);
		    	int eLevel = tn.getLevel();
		    	if(wj.getChildList(tn, true).isEmpty()) {
		    		eLevel = maxLevel;
		    	}
		    	JSONObject no = 
		    			(new JSONObject()).put("id", tn.getId())
	    								  .put("level", eLevel)
	    								  .put("label", tn.getDescription())
	    								  .put("font", (new JSONObject()) .put("color", (tn.getFontColor() == null ? "black" : tn.getFontColor())))
	    								  .put("color", tn.getColor() == null ? "#00c1ff" : tn.getColor())
   										  .put("heightConstraint.minimum","25px")
   										  .put("widthConstraint.minimum","80px")
   										  .put("borderWidthSelected",4)
	    								  ;
		    	HashMap<String,String> hp = nodeTypeOptionMap.get(new Integer(tn.getType()));
		    	if(hp != null) {
		    		for(String option : hp.keySet()) {
		    			String value = hp.get(option);
		    			no.put(option, value);
		    		}
		    	}
		    	if(tn.getLevel() == 1) {
		    		if(drawStartEnd) {
		    		jaEdges.put((new JSONObject()).put("id","Start:"+tn.getId()).put("from", "Start").put("to", tn.getId())/* .put("label", "HAHA skdsjkdsdjksdjksdjk")*/);	
		    		}
		    	}
		    	jaNodes.put(no);
		    }
		    if(drawStartEnd) {
	    	jaNodes.put(
		    			(new JSONObject()).put("id", "End")
	    								  .put("level", wj.getMaxLevel()+1)
	    								  .put("label", "End")
//	    								  .put("label", "End\nDelayed")
	    								  .put("font", (new JSONObject()) .put("color", (wj.getEndFontColor() == null ? "black" : wj.getEndFontColor())))
	    								  .put("color", wj.getEndColor() == null ? "grey" : wj.getEndColor())
	    								  .put("shape", "circle")
//	    								  .put("shape", "image")
//	    								  .put("image", "images/database-icon.png")
   										  .put("borderWidthSelected",4)
	    			);
		    }
	    	if(taskList.size() == 0) {
	    		if(drawStartEnd) {
	    			jaEdges.put((new JSONObject()).put("id","Start:End").put("from", "Start").put("to", "End")/*.put("color", "blue")*//* .put("label", "HAHA skdsjkdsdjksdjksdjk")*/);	
	    		}
	    	}
		    for(int i = 0;i<taskList.size();i++) {
		    	WipStep t0  = taskList.get(i);
		    	List <WipStep> cl = wj.getChildList(t0,true);
		    	for(int j = 0;j<cl.size();j++) {
		    		jaEdges.put((new JSONObject()).put("id","E"+t0.getId()+":"+cl.get(j).getId()).put("from", t0.getId()).put("to", cl.get(j).getId())/* .put("label", "HAHA skdsjkdsdjksdjksdjk")*/);	
		    	}
		    	if(cl.size() <= 0) {
		    		if(drawStartEnd) {
		    		jaEdges.put((new JSONObject()).put("id","E"+t0.getId()+":End").put("from", t0.getId()).put("to", "End").put("color",wj.getEndColor()  == null ? "grey" : wj.getEndColor())/* .put("label", "HAHA skdsjkdsdjksdjksdjk")*/);	
		    		}
		    	}
		    }

		    /*
	    	jaNodes.put((new JSONObject()).put("id", 0)
	    								  .put("level", 0)
	    								  .put("shape", "box")
	    								  .put("label", "item0"));
	    	jaNodes.put((new JSONObject()).put("id", 1)
	    								  .put("level", 0)
	    								  .put("shape", "box")
	    								  .put("label", "item1"));
	    	jaNodes.put((new JSONObject()).put("id", 2)
	    								  .put("level", 1)
	    								  .put("shape", "box")
	    								  .put("label", "item2"));
	    	
	    	jaEdges.put((new JSONObject()).put("id",1001).put("from", 0).put("to", 2));	
	    	jaEdges.put((new JSONObject()).put("id",1002).put("from", 1).put("to", 2));	
	    	*/
		    
		    
	    	//define option
		    JSONObject joOptions = new JSONObject();
	    	joOptions.put("edges", (new JSONObject()).put("smooth", (new JSONObject())
	    			                                      .put("type", "cubicBezier")
//	    			                                      .put("forceDirection", "vertical")
	    			                                      .put("forceDirection", true)
	    			                                      .put("roundness", 0.5)).put("arrows", "to"))
                     .put("layout", (new JSONObject())
//                          .put("improvedLayout", true)
                          .put("hierarchical", (new JSONObject())
                        		  .put("direction", "LR")
                        		  //.put("levelSeparation", compactSpacing ? 80 : 150)
                        		  //.put("nodeSpacing", compactSpacing ? 50 : 100)
                        		  //.put("treeSpacing", compactSpacing ? 50 : 100)
                        		  .put("levelSeparation", compactSpacing ? 90 : 150)
                        		  .put("nodeSpacing", compactSpacing ? 60 : 100)
                        		  .put("treeSpacing", compactSpacing ? 60 : 100)
//                        		  .put("parentCentralization", true)
//                        		  .put("edgeMinimization", true)
//                        		  .put("blockShifting", true)
   			              ))
                     .put("nodes", (new JSONObject())
   			              .put("shape", "box")
   			              .put("shapeProperties", 
   			            		  new JSONObject().put("useBorderWithImage", true)
   			            		  )
   			              )
                     .put("physics", false);
	    	joOptions.put("interaction", 
	    				(new JSONObject())
	    					.put("dragNodes", false)
	    					.put("dragView", true)  //andrew230911 better allow user to move the chart
	    					.put("zoomView", false)
	    			);
	    	
	    	
		    jo.put("containerId", visContentDiv);
		    jo.put("options", joOptions);
		    jo.put("items", ((new JSONObject()).put("nodes", jaNodes).put("edges", jaEdges)));
		    if (preserveSelectedNode.get() && selectedNodeList.size() > 0) {
		    	jo.put("selection", (new JSONObject()).put("nodes", new JSONArray(selectedNodeList)));
		    }
            if (fDebug.get()) System.out.print("result"+jo.toString(5));
            //Clients.evalJavaScript(String.format("visUtilProcessNetwork(%s,'visNwEventDiv')", jo.toString(5)));
//            ZkUtil.delayJs(visEventDiv, null, "visUtilProcessNetwork(%s,'visNwEventDiv')", jo.toString());
            //ZkUtil.delayJs(visEventDiv, null, "visUtilProcessNetwork(%s,'"+visEventDiv.getId()+"')", jo.toString());
			Iterator<EventListener<?>> it = visEventDiv.getEventListeners("onProcessNetwork").iterator();
			while (it.hasNext())
				visEventDiv.removeEventListener("onProcessNetwork", it.next());
			visEventDiv.addEventListener("onProcessNetwork", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					ZkUtil.js("visUtilProcessNetwork(%s,'"+visEventDiv.getId()+"')", jo.toString());
				}
			});
			Events.echoEvent("onProcessNetwork", visEventDiv, null);
            
            if(visEventDiv != null) {
            	visEventDiv.invalidate();
            }
            currentNodeOrEdge = null;
		}
		catch(Exception ex){
			ex.printStackTrace();
		}	
	}

	public void addNodeTypeOption(int p_type,String p_option,String p_value)
	{
		HashMap<String,String> hp = nodeTypeOptionMap.get(new Integer(p_type));
		if(hp == null) {
			hp = new HashMap<String,String>();
			nodeTypeOptionMap.put(new Integer(p_type), hp);
		}
		hp.put(p_option, p_value);
	}
	public void delNodeTypeOption(int p_type,String p_option,String p_value)
	{
		HashMap<String,String> hp = nodeTypeOptionMap.get(new Integer(p_type));
		if(hp == null) {
			hp = new HashMap<String,String>();
			nodeTypeOptionMap.put(new Integer(p_type), hp);
		}
		hp.remove(p_option);
	}
	
	public void setCallBack( WipVisCallbackInterface p_callback) {
		callback = p_callback;
		
	}
	public void setDrawStartEnd(boolean p_sw) {
		drawStartEnd = p_sw;
	}
	public void setCompactSpacing(boolean p_sw) {
		compactSpacing = p_sw;
	}
	public static String getCbModeLabel(int p_mode) {
		switch(p_mode) {
			case WIPVIS_CBMODE_NONE_CLICKED: return("WIPVIS_CBMODE_NONE_CLICKED") ;
			case WIPVIS_CBMODE_START_CLICKED: return("WIPVIS_CBMODE_START_CLICKED") ;
			case WIPVIS_CBMODE_END_CLICKED: return("WIPVIS_CBMODE_END_CLICKED") ;
			case WIPVIS_CBMODE_ARROW_CLICKED: return("WIPVIS_CBMODE_ARROW_CLICKED") ;
			case WIPVIS_CBMODE_NODE_CLICKED: return("WIPVIS_CBMODE_NODE_CLICKED") ;
			case WIPVIS_CBMODE_ARROW_ADDED: return("WIPVIS_CBMODE_ARROW_ADDED") ;
			case WIPVIS_CBMODE_ARROW_DELETED: return("WIPVIS_CBMODE_ARROW_DELETED") ;
			case WIPVIS_CBMODE_ARROW_UPDATED: return("WIPVIS_CBMODE_ARROW_UPDATED") ;
			default: return ("UNKNOWN");
		}
	}
}
