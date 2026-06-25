package com.uniinformation.jx.zk;

import java.util.HashMap;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiSearchHelper;

public class ZkJxPickInput extends Bandbox {
	
	Bandpopup bp = null;
//	JxForm jxf = null;
	String jxfInstance;
//	Listbox lb;
//	EventListener onClickListener;
//	ListitemRenderer xlistitemRenderer ;
//	
//	HashMap<Integer,Integer> searchIdxHm = new HashMap<Integer,Integer>();
//	Vector recList = null;
	JxFormCloseListener fclr = null;
	public ZkJxPickInput()
	{
		super();
		UniLog.log("ZkJxPickInput Initialized");
		bp = new Bandpopup();
//		bp.setHeight("500px");
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		if(sessionHelper.isMobileDevice()){
			int cc = sessionHelper.getScreenWidth();
			if( cc <= 400) {
				setPopupWidth("100%");
			} else {
				setPopupWidth(""+(cc - 20)+"px");
			}
		}
//		lb = new Listbox();
//		lb.setWidth("500px");
//		lb.setHeight("300px");
//		lb.setAutopaging(true);
//    	lb.setMold("paging");
//		bp.appendChild(lb);
		appendChild(bp);
//		addEventListener("onOpen",new EventListener() {
//			public void onEvent(Event event) throws Exception {	
//				UniLog.log("ZkJxPickInput isOpened = " + isOpen());
//			}
//		});
//		xlistitemRenderer = new ListitemRenderer() {
//			 public void render(Listitem item, Object p_data, int p_idx) throws Exception {
//				 	if(p_data instanceof Object[]) {
//				 		Object items[] = (Object[]) p_data;
//				 		for(int i=0;i<items.length;i++) {
//				 			Listcell lc = new Listcell(items[i].toString());
//				 			item.appendChild(lc);
//				 		}
//				 	}
//				 	if(p_data instanceof String) {
//				 		Listcell lc = new Listcell(p_data.toString());
//				 		item.appendChild(lc);
//				 	}
//				 	if(onClickListener != null) item.addEventListener(Events.ON_CLICK,onClickListener);
//		 		};
//		};
//		
//		setInstant(true);
//		setAutodrop(true);
//		addEventListener("onChange", new EventListener() {
//    		public void onEvent(Event event) throws Exception {
//    			UniLog.logm(this, "onChange: %s", getValue());
//    			if(lb.getListModel() == null) return;
//    			Vector searchResult = new Vector();
//				searchIdxHm.clear();
//				if (recList != null){
//					for (int i=0; i<recList.size(); i++){
//						Object recObj = recList.elementAt(i);
//						//UniLog.logm(null, "HAHA:%s",  lb.getListModel().getElementAt(i).toString());
//						if (recObj instanceof Object[]){
//							Object[] rec = (Object[])recObj;
//							StringBuffer recSb = new StringBuffer();
//							for (int j=0; j<rec.length; j++){
//								recSb.append(rec[j].toString());
//							}
//							if (ZkBiSearchHelper.match(recSb.toString(), getValue())){
//								searchIdxHm.put(searchResult.size(), i); //save db idx
//								searchResult.add(recObj);
//							}
//							
//						}
//					}
//				}
//				((ListModelList)lb.getListModel()).clear();
//				((ListModelList)lb.getListModel()).addAll(searchResult);
//    		}
//    	});
	}
//	
//	public int getTrIdx(int p_listIdx){
//		if (searchIdxHm.get(p_listIdx) != null){  //obtain db idx by list idx
//			return(searchIdxHm.get(p_listIdx));
//		}
//		return(p_listIdx);
//	}
//	
//	public void setListModel(Vector p_recList) {
//		recList = p_recList;
//		ListModelList listModel = new ListModelList(p_recList);
//		lb.setModel(listModel);
//		lb.setItemRenderer(xlistitemRenderer);
//		searchIdxHm.clear();
//	}
//	
//	public Listbox getListbox()
//	{
//		return(lb);
//	}
//	
//	public int getSelectedIndex() //return dbidx
//	{
//		int idx = lb.getSelectedIndex();
//		if (searchIdxHm.get(idx) != null){  //obtain db idx by list idx
//			return(searchIdxHm.get(idx));
//		}
//		return(idx);
//		//return(lb.getSelectedIndex());
//	}
//	
//	public Object getSelectedItem() //return dbidx
//	{
//		int idx = lb.getSelectedIndex();
//		if(idx < 0) return(null);
//		if (searchIdxHm.get(idx) != null){  //obtain db idx by list idx
//			return(lb.getItems().get(searchIdxHm.get(idx)));
//		}
//		return(lb.getItems().get(idx));
//		//return(lb.getSelectedIndex());
//	}	
//	public void setOnClickListener(EventListener evlistener) {
//		onClickListener = evlistener;
//	}
//	public void closePopup()
//	{
//		close();
//		setFocus(true);
//	}
	
	public void setJxZkForm(JxForm p_jxf) {
		JxForm jxf = null;
		if(jxfInstance != null) {
			SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
			jxf = pvdr.jxGetForm(jxfInstance);
			Component c = (Component) jxf.getNativeComponent();
//			if(bp.hasFellow(c.getId())) {
			if(c.getParent() == bp) {
				if(jxf != p_jxf) {
					jxf.addFormCloseListener(null);
					bp.removeChild(c);
					jxfInstance = null;
					jxf = null;
				} else return;
			} else {
				jxfInstance = null;
				jxf = null;
			}
		}
		jxf = p_jxf;
		if(jxf != null) {
			Component c = (Component) jxf.getNativeComponent();
			/*
			if (c instanceof HtmlBasedComponent) {
				bp.setWidth(((HtmlBasedComponent) c).getWidth());
				bp.setHeight(((HtmlBasedComponent) c).getHeight());
			} else {
				bp.setWidth("300px");
				bp.setHeight("200px");
			}
//				bp.setWidth("300px");
//				bp.setHeight("200px");
			 */
			if(fclr == null) {
				fclr = new JxFormCloseListener( ) {
					public int formClose(JxForm thisjxf) {
						Component c = (Component) thisjxf.getNativeComponent();
						if(bp.hasFellow(c.getId())) {
//							close();	//andrew 181206: why comments out close? without this line prevent popup close
							close();
							setFocus(false);
							return(JxFormCloseListener.caNone);
						} else {
							return(JxFormCloseListener.caDefault);
						}
					}
				};
			}
			jxf.addFormCloseListener(fclr);
			jxfInstance = jxf.getSkin().getInstanceName();
			bp.appendChild(c);
		}
	}
	public JxForm getJxZkForm() {
		JxForm jxf = null;
		if(jxfInstance != null) {
			SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
			jxf = pvdr.jxGetForm(jxfInstance);
			Component c = (Component) jxf.getNativeComponent();
//			if(!bp.hasFellow(c.getId())) jxf = null;
			if(c.getParent() != bp) jxf = null;
		}
		return (jxf);
	}
	
	public void setPopupHeight(String p_height) {
		bp.setHeight(p_height);
		bp.invalidate(); //andrew 180919: fix popup display at wrong location
	}

	@Override
	public void setPopupWidth(String p_width) {
		super.setPopupWidth(p_width);
		bp.invalidate(); //andrew 180919: fix popup display at wrong location
	}
}
