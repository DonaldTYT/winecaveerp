package com.uniinformation.erpv4.wip;

import com.uniinformation.wip.WipJob;
import com.uniinformation.wip.WipStep;

public class WfmStep implements WipStep {
		static public final int WFMTYPE_PROGRESS = 1;
		static public final int WFMTYPE_CHOICES = 2;
		static public final int WFMTYPE_ACTION = 3;
		String id;
		String description;
		int rg;
		int level = 0;
		int order = 0;
		int type;
		public WfmStep(int p_rg,String p_description,int p_order) {
			id = makeNodeId(p_rg);
			rg = p_rg;
			order = p_order;
			description = p_description;
		}
		public int getStepRg() {
			return(rg);
		}
		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return id;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return description;
		}

		public void setDescription(String p_desc) {
			// TODO Auto-generated method stub
			description = p_desc;
		}

		@Override
		public int getLevel() {
			// TODO Auto-generated method stub
			return level;
		}

		@Override
		public void setLevel(int p_level) {
			// TODO Auto-generated method stub
			level = p_level;
		}
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			if(o instanceof WfmStep) {
				WfmStep ws = (WfmStep) o;
				if(ws.level > level) return(-1); else {
					if(ws.level < level) return(1); else {
						if(ws.order > order) return(-1); else {
							if(ws.order < order ) return(1);
						}
					}
				}
			}
			return 0;
		}
		public String getColor() {
//			return(null);
			return(WipJob.Color_Locked);
		}
		
		static public String makeNodeId(int p_rg) {
			return(String.format("%08d", p_rg));
		}
		static public int getRgFromNodeId(String p_nodeid) {
			return(Integer.parseInt(p_nodeid));
		}
		@Override
		public String getFontColor() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public int getOrder() {
			// TODO Auto-generated method stub
			return order;
		}
		@Override
		public int getType() {
			// TODO Auto-generated method stub
			return (type);
		}
		
		public void setType(int p_type) {
			type = p_type;
		}
}
