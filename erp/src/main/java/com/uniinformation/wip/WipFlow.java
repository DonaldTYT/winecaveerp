package com.uniinformation.wip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.uniinformation.utils.NetworkNodeUtil;
import com.uniinformation.utils.NetworkNodeUtil.LinkAttribute;
import com.uniinformation.utils.UniLog;

public class WipFlow {
	protected NetworkNodeUtil nu;
	int startLevel = 1;
	public WipFlow() {
		nu = new NetworkNodeUtil();
		nu.setAllowLoop(false);
	}
	public void delStep(WipStep t,boolean p_joinFlag) {
		try {
            List<WipStep> cl = getChildList(t,true);
			nu.removeNode(t.getId(),p_joinFlag);
			for(WipStep ws : cl) {
				recursiveAssignStepLevel(ws);
			}
		} catch (Exception ex){
			UniLog.log(ex);
		}
	}
	public void addStep(WipStep t) {
		nu.addNode(t.getId(),t);
		t.setLevel(startLevel);
	}
	public void addPreq(WipStep p_parent,WipStep p_child) throws Exception {
		if(p_parent == p_child) {
			UniLog.log("Warning !!!! addPreq parent == child, skipped");
			return;
		}
		nu.addChild(p_parent.getId(),p_child.getId(),new LinkAttribute(false,null));
		recursiveAssignStepLevel(p_child);
	}
	/*
	public void addPreq(WipStep p_parent,WipStep p_child) throws Exception {
		addPreq(p_parent,p_child,null);
	}
	public void addPreq(WipStep p_parent,WipStep p_child,Comparable p_progressToProceed) throws Exception {
		if(p_parent == p_child) {
			UniLog.log("Warning !!!! addPreq parent == child, skipped");
			return;
		}
		nu.addChild(p_parent.getId(),p_child.getId(),new LinkAttribute(false,p_progressToProceed));
		recursiveAssignStepLevel(p_child);
	}
	*/
	public void deletePreq(WipStep p_parent,WipStep p_child) throws Exception {
		nu.removeLink(p_parent.getId(),p_child.getId());
		recursiveAssignStepLevel(p_child);
		/*
		List<WipStep> l = getChildList(p_child, true);
		for(int i=0;i<l.size();i++) {
			recursiveAssignTaskLevel(l.get(i));
		}
		*/
	}
	public void clearClearPreq()
	{
		nu.clearAllLinks();
		List<WipStep> nl = getStepList(null);
		for(WipStep t : nl) {
			t.setLevel(1);
		}
	}
	public void clear()
	{
		nu.clear();
	}
	public List<WipStep> getChildList(WipStep t, boolean p_immediate) {
		List<String> cl = nu.getChildList(t.getId(), p_immediate);
		List <WipStep> tl = new ArrayList<WipStep>();
		for(int i=0;i<cl.size();i++) {
			tl.add((WipStep) nu.getNode(cl.get(i)));
		}
		return(tl);
	}
	public List<WipStep> getParentList(WipStep t, boolean p_immediate) {
		List<String> cl = nu.getParentList(t.getId(), p_immediate);
		List <WipStep> tl = new ArrayList<WipStep>();
		for(int i=0;i<cl.size();i++) {
			tl.add((WipStep) nu.getNode(cl.get(i)));
		}
		return(tl);
	}
	
	public List<WipStep> getStepsWithoutChild() {
		List l = nu.getNodeValueList();
		ArrayList rl = new ArrayList();
		for(WipStep wt : (List<WipStep>) l) {
			List cl = getChildList(wt,true);
			if(cl.size() <= 0) rl.add(wt);
		}
		return(rl);
	}
	public List<WipStep> getStepsWithoutParent() {
		List l = nu.getNodeValueList();
		ArrayList rl = new ArrayList();
		for(WipStep wt : (List<WipStep>) l) {
			List cl = getParentList(wt,true);
			if(cl.size() <= 0) rl.add(wt);
		}
		return(rl);
	}
	
	
	public int getMaxLevel() {
		List l = nu.getNodeValueList();
		int maxLevel = 0;
		for(WipStep ws : (List <WipStep>) l) {
			if(ws.getLevel() > maxLevel) maxLevel = ws.getLevel();
		}
		return(maxLevel);
	}
	public List<WipStep> getStepList(Comparator <WipStep> c) {
		List l = nu.getNodeValueList();
		Collections.sort(l,c);
		return(l);
	}
	
	public WipStep getStep(String id) {
		return((WipStep) nu.getNode(id));
	}
	
	int recursiveGetStepLevel(WipStep t, int p_level) {
		int level = p_level;
		List<WipStep> l = getParentList(t, true);
		/*
		if(p_level > 10) {
			UniLog.log("too many level");
			return(p_level);
		}
		*/
		for(int i=0;i<l.size();i++) {
			int v = recursiveGetStepLevel(l.get(i), p_level+1);
			if(v > level) level = v;
		}
		return(level);
	}
	void	recursiveAssignStepLevel(WipStep t) {
		t.setLevel(recursiveGetStepLevel(t,startLevel));
		List<WipStep> l = getChildList(t, true);
		for(int i=0;i<l.size();i++) {
			recursiveAssignStepLevel(l.get(i));
		}
	}
	/*
	void assignStepLevel(WipStep t) {
		t.setLevel(recursiveGetStepLevel(t,startLevel));
		
	}
	
	public void assignAllStepLevel()
	{
		List l = nu.getNodeValueList();
		for(int i=0;i<l.size();i++) {
			assignStepLevel((WipStep) l.get(i));
			
		}
	}
	*/
	
	public String getStartFontColor() {
		return(null);
	}
	public String getEndFontColor() {
		return(null);
	}
	public String getStartColor() {
		return(null);
	}
	public String getEndColor() {
		return(null);
	}	
	/*
	List <WipStep> tl;
	HashMap <String,WipPreq> pm;
	public void delTask(WipStep t) {
			List <String> l = nu.getParentList(t.getId(), true);
			for(int i = 0;i<l.size();i++) {
				WipStep t0 = (WipStep) nu.getNode(l.get(i));
				pm.remove(t.getId()+":"+t0.getId());
			}
			l = nu.getChildList(t.getId(), true);
			for(int i = 0;i<l.size();i++) {
				WipStep t0 = (WipStep) nu.getNode(l.get(i));
				pm.remove(t0.getId()+":"+t.getId());
			}
			nu.
			tl.remove(t);
			
	}
	public void addTask(WipStep p_wt) throws WipException {
		WipStep t = (WipStep) nu.getNode(p_wt.getId());
		if(t != null) {
		}
	}
	*/
	public void setLinkData(WipStep p_parent,WipStep p_child,Object p_data) throws Exception {
		if(p_parent == p_child) {
			UniLog.log("Warning !!!! updLink parent == child, skipped");
			return;
		}
		nu.setLinkData(p_child.getId(),p_parent.getId(),p_data);
	}
	public Object getLinkData(WipStep p_parent,WipStep p_child) throws Exception {
		if(p_parent == p_child) {
			UniLog.log("Warning !!!! updLink parent == child, skipped");
			return(null);
		}
		return(nu.getLinkData(p_child.getId(),p_parent.getId()));
	}
}
