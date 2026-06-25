package com.uniinformation.jx.zk;

import java.util.Hashtable;
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Template;

import com.uniinformation.jx.JxSkinElement;
import com.uniinformation.utils.UniLog;

public class JxZkLayout extends JxZkElement {
	class TemplateRec {
		Template template;
		String layoutid;
		String suffix;
		int realizedCount;
		int currentCount;
	}
	Hashtable templateHash;
	public JxZkLayout(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		templateHash = new Hashtable();
	}
	
	public int realizeTemplate(String p_template,int p_count)
	{
		return(realizeTemplate(p_template,null,p_count));
	}
	public int realizeTemplate(String p_template,String p_suffix,int p_count)
	{
		UniLog.logm(this,"%s %s %d entered", p_template, p_suffix, p_count);
		TemplateRec trec = (TemplateRec) templateHash.get(p_template);
		if(trec == null) {
			Template tl  = comp.getTemplate(p_template);
			if(tl == null) {
				UniLog.logm(this,"template(%s) not exist", p_template);
				return(-1);
			}
			trec = new TemplateRec();
			trec.suffix = p_suffix;
			trec.template = tl;
			trec.realizedCount = 0;
			trec.currentCount = 0;
			templateHash.put(p_template, trec);
		}
		UniLog.logm(this,"step 2");
		for(;trec.realizedCount<p_count;trec.realizedCount++) {
			Component carr[];
			carr = trec.template.create(comp, null, null, null);
			if(trec.realizedCount == 0 && carr.length > 0) {
				if(p_suffix != null) 
					trec.layoutid = carr[0].getId()+p_suffix;
				else
					trec.layoutid = carr[0].getId();
					
			}
			for(int j = 0;j<carr.length;j++) {
//				carr[j].setId(carr[j].getId()+"_"+trec.realizedCount);
				indexTemplateComponent(carr[j], trec.realizedCount,trec.suffix);
			}
		}
		
		UniLog.logm(this,"step 3");
		if(trec.layoutid != null) {
			for(int i = 0;i<p_count;i++) {
				JxSkinElement se = getSkin().getField(trec.layoutid+"_"+i);
				if(se != null) se.setVisible(true);
			}
			for(int i = p_count;i<trec.realizedCount;i++) {
				JxSkinElement se = getSkin().getField(trec.layoutid+"_"+i);
				if(se != null) se.setVisible(false);
			}
		}
		return(trec.realizedCount);
	}
	
	public void indexTemplateComponent(Component c, int idx, String p_suffix)
	{
		if(c.getId() != null && !c.getId().equals("")) {
			if(p_suffix != null) {
				c.setId(c.getId()+p_suffix+"_"+idx);
			} else {
				c.setId(c.getId()+"_"+idx);
			}
		}
		((JxZkSkin) getSkin()).addOneElementToSkin(c);
		java.util.List <Component> clist = c.getChildren();
		Iterator itr = clist.iterator();
 		while(itr.hasNext()) {
 			Component cc = (Component) itr.next();
 			indexTemplateComponent(cc, idx,p_suffix);
 		}
	}
	
	@Override
	public Object getValue() {
		return(null);
	}
}
