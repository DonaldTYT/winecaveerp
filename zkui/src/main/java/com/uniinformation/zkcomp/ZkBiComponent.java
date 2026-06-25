package com.uniinformation.zkcomp;

import java.util.concurrent.atomic.AtomicInteger;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.NoDOM;
import org.zkoss.zul.Radiogroup;

import com.uniinformation.utils.ZkCompStyleUtil;

public class ZkBiComponent extends NoDOM implements /* IdSpace ,*/ AfterCompose {
	static AtomicInteger compIdx = new AtomicInteger(0);
	String tmplName=null;
	@Wire
	private HtmlBasedComponent comp;
	@Wire
	private Label lb;
	@Wire
	Div lbDiv;
	@Wire
	Div compDiv;
	
	private String compStyle=null;

	protected void wireComponent(String p_zul) {
		//1. Render the template
		Executions.createComponents(p_zul, this, null);
		//2. Wire variables, components and event listeners (optional)
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		String id;
		if(lb != null && (id = lb.getId()) != null) {
			lb.setId(id+"_"+compIdx);
			lb.setValue(null);
		}
		if(lbDiv != null && (id = lbDiv.getId()) != null) {
			lbDiv.setId(id+"_"+compIdx);
		}
		if(compDiv != null && (id = compDiv.getId()) != null) {
			
			compDiv.setId(id+"_"+compIdx);
		}
		if(comp != null && (id = comp.getId()) != null) {
			comp.setId(id+"_"+compIdx);
		}
		if(comp != null && compStyle != null) {
			comp.setStyle(compStyle);
			compStyle = null;
		}
		compIdx.incrementAndGet();
	}
	public ZkBiComponent(String p_zul) {
		//1. Render the template
		if(p_zul != null ) {
		Executions.createComponents(p_zul, this, null);
		//2. Wire variables, components and event listeners (optional)
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		String id;
		if(lb != null && (id = lb.getId()) != null) {
			lb.setId(id+"_"+compIdx);
			lb.setValue(null);
		}
		if(lbDiv != null && (id = lbDiv.getId()) != null) {
			lbDiv.setId(id+"_"+compIdx);
		}
		if(compDiv != null && (id = compDiv.getId()) != null) {
			compDiv.setId(id+"_"+compIdx);
		}
		if(comp != null && (id = comp.getId()) != null) {
			comp.setId(id+"_"+compIdx);
		}
		compIdx.incrementAndGet();
		}
	}
	
	public Label getLabel() {
		return lb;
	}
	public void setLabel(String p_value) {
		if (lb != null) {
			lb.setValue(p_value);
		}
	}
	
	public HtmlBasedComponent getComp() {
		return comp;
	}
	
	/***
	 * set both label and comp id
	 * @param p_id
	 */
	public void setJxId(String p_id) {
		if (comp != null) {
			comp.setId(p_id);
			/* move radiogroup tlkey update logic to JxZkBiBase in order to have a better control
			if (comp instanceof Radiogroup) {
				comp.setAttribute("tlkey",p_id); //andrew230602 allow translate radio button option
			}
			*/
		}
		if (lb != null) {
			lb.setId("lb_"+p_id);
			//lb.setAttribute("tlkey","lb_"+ p_id);
			lb.setAttribute("tlkey",p_id); //anderw220705 unify tlkey without lb_ prefix. ref redmine #869
		}
		if (lbDiv != null) {
			lbDiv.setId("lbdiv_"+p_id);
		}
	}
	
	/***
	 * set comp id for nolabl component
	 */
	public void setId(String p_id) {
		if (comp != null) {
			comp.setId(p_id);
		}
	}
	/***
	 * set comp width for nolabel component
	 * @param p_width
	 */
	public void setWidth(String p_width) {
		if (comp != null) {
			comp.setWidth(p_width);
		}
	}

	public String getWidth() {
		return comp != null ? comp.getWidth() : null;
	}
	
	/***
	 * set comp style
	 * @param p_style
	 */
	public void setStyle(String p_style) {
		if (comp != null) {
			comp.setStyle(p_style);
		} else {
			compStyle = p_style;
		}
	}
	
	/***
	 * set comp hflex
	 * remark: does not work as expected. got js error Cannot read property 'position' of undefined
	 * @param p_hflex
	 */
	public void setHflex(String p_hflex) {
		if (comp != null) {
			comp.setHflex(p_hflex);
		}
	}
	
	public void setLabelWidth(String p_width) {
		if (lbDiv != null) {
			lbDiv.setWidth(p_width);
		}
	}
	public void setFieldWidth(String p_width) {
		if (comp != null) {
			comp.setWidth(p_width);
		}
	}
	public void setCompValue(String p_value) {
		if (comp != null) {
		}
	}
	public void setCompWidth(String p_width) {
		if (compDiv != null) {
			compDiv.setWidth(p_width);
		}
	}
	public void setTemplate(String p_template) {
		tmplName = p_template;
	}
	
	@Override
	public void afterCompose() {
		if(tmplName != null) {
		Template colTemplate = this.getTemplate(tmplName);
		if(colTemplate != null) {
			colTemplate.create(compDiv, null, null, null);
		}
		}
	}

	public void setCompStyle(String p_style) {
		if (compDiv != null) {
			compDiv.setStyle(p_style);
		}
	}

	public void setCompSclass(String p_sclass) {
		if (compDiv != null)
			compDiv.setSclass(p_sclass);
	}

	public void setCompMaxWidth(String p_width) {
		if (compDiv != null)
			ZkCompStyleUtil.setProperty(compDiv, "max-width", p_width);
	}

	public String getCompMaxWidth() {
		return compDiv != null ? ZkCompStyleUtil.getProperty(compDiv, "max-width") : null;
	}
}
