package com.uniinformation.bicore;
import java.io.*;
import java.util.*;

import com.uniinformation.utils.*;
import com.uniinformation.cell.*;

public class BiChain extends BiBase {
//	BiTable table;
//	BiTable master;
//	BiChains subChains;
//	BiColumns columns;
	boolean optional = false;
	boolean reverseJoin = false;
	public BiChain(CellCollection parent,BiTable p_table)
	{
		super(parent);
		addCollection("table",p_table);
//		addCollection("columns",new CellCollection(this));
		addCollection("subChains",new CellCollection(this));
		/*
		for(CellCollection c = parent;c!=null;c=c.getParent()) {
			if(c instanceof BiChain) {
				UniLog.log("set master of " + p_table.getName() + " to " + ((BiChain) c).getTable().getName());
				addCollection("master",((BiChain) c).getTable());
				break;
			}
		}
		*/
	}
	/*
	public BiColumn addColumn(String p_label,int p_idx,BiField p_field)
	{
		UniLog.log("BiChain.addColumn " + p_label);
		BiColumn column = new BiColumn(getCollection("columns"),p_idx,p_field);
		getCollection("columns").addCollection(p_label,column);
		return(column);
	}
	*/
	public BiChain getParentChain()
	{
//		return((BiTable) getCollection_SingleLevel("master"));
		CellCollection c;
		for(c = getParent();c != null;c = c.getParent()){
			if(c instanceof BiChain) return(((BiChain) c));
			
		}
		return(null);
	}
	public BiTable getMaster()
	{
//		return((BiTable) getCollection_SingleLevel("master"));
		CellCollection c;
		for(c = getParent();c != null;c = c.getParent()){
			if(c instanceof BiChain) return(((BiChain) c).getTable());
		}
		return(null);
	}
	public BiTable getTable()
	{
		return((BiTable) getCollection_SingleLevel("table"));
	}
	protected BiChain addChain(BiTable p_subTable)
	{
		BiChain chain = (BiChain) (getCollection("subChains").getCollection(p_subTable.getName()));
		if(chain == null) {
			//UniLog.log("BiChain add new subtable "+p_subTable.getName());
			chain = new BiChain(getCollection("subChains"),p_subTable);
			getCollection("subChains").addCollection(p_subTable.getName(),chain);
		}
		return(chain);
	}
	
	public BiChain findChain(final BiTable p_table)
	{
		final Vector res = new Vector();
		try {
		traverseCollection(true,
				new TraverseInterface() {
					public boolean traverseOne(CellCollection p_col) throws CellException
					{
						if(p_col instanceof BiChain) {		
							BiChain chain = (BiChain ) p_col;
							if(chain.getTable() == p_table) {
								res.add(chain);
							}
						}
						return(true);
					}
				}
		);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
		if(res.size() > 0 ) return((BiChain) res.get(0)); else return(null);
	}	
	
	boolean contains(BiTable bt) {
		if(bt == getTable()) return(true);
		Object subChains[] = getCollection("subChains").getCollections();
		if(subChains != null) {
			for(int i=0;i<subChains.length;i++) {
				if(subChains[i] instanceof BiChain) {
					BiChain bichain = (BiChain) subChains[i];
					if(bichain.contains(bt)) return(true);
				}
			}
		}
		return(false);
	}
	boolean intersetTableSet(HashSet<BiTable> p_set) {
		if(p_set.contains(getTable())) return(true);
		Object subChains[] = getCollection("subChains").getCollections();
		if(subChains != null) {
			for(int i=0;i<subChains.length;i++) {
				if(subChains[i] instanceof BiChain) {
					BiChain bichain = (BiChain) subChains[i];
					if(bichain.intersetTableSet(p_set)) return(true);
				}
			}
		}
		return(false);
	}
	public String makeJoinTableStrSql20(HashSet<BiTable> innerList,boolean includeNoDetail) {
		Object subChains[] = getCollection("subChains").getCollections();
		String ss = "";
		if(subChains != null) {
			for(int i=0;i<subChains.length;i++) {
				if(subChains[i] instanceof BiChain) {
					BiChain bichain = (BiChain) subChains[i];
					BiTable tm,td;
					BiJoin jn = null;
					td = bichain.getMaster();
					tm = bichain.getTable();
					if(bichain.reverseJoin) {
//						kdjskjsksjkds
						jn = td.getJoin(tm);
					} else {
						jn = tm.getJoin(td);
					}
					if(jn != null) {
						if(bichain.optional) ss += " left outer "; else ss += " ";
						ss += " join ";
						Object subChains2[] = bichain.getCollection("subChains").getCollections();
						if(subChains2 != null && subChains2.length > 0) {
							ss += " ( ";
							ss += bichain.getTable().getSelectFromName();
							ss += bichain.makeJoinTableStrSql20(innerList,includeNoDetail);
							ss += " ) ";
							
						} else {
							ss += bichain.getTable().getSelectFromName();
						}
						ss += " on ";
						for(int j = 0;j<jn.getJoinCount();j++) {
								if(j > 0) ss += " and ";
								ss += String.format("%s = %s"
										,jn.getFromField(j).getFullName()
										,jn.getToField(j).getFullName()
										);
						}
					}
				}
			}
		}
		return(ss);
	}
	public String makeSelectTableStr(HashSet<BiTable> primaryTabList,HashSet<BiTable> innerList,boolean includeNoDetail,BiResult p_parent) {
		String s = "";
		Object subChains[] = getCollection("subChains").getCollections();
		if(subChains != null) {
			for(int i=0;i<subChains.length;i++) {
				if(subChains[i] instanceof BiChain) {
					BiChain bichain = (BiChain) subChains[i];
//					UniLog.log("240314 makeSelectTableStr chain " + bichain.getMaster().getName() + " -> " + bichain.getTable().getName() + " " + bichain.reverseJoin);
					if(p_parent != null && bichain.getTable() == p_parent.getView().getTable()) continue;
					boolean notInMaster  = false;
					if(bichain.reverseJoin && includeNoDetail) {
						for (Iterator<BiTable> it = primaryTabList.iterator(); it.hasNext();) {
						    BiTable tn = it.next();
						    if(bichain.contains(tn))  {
						    	it.remove();
						    }
						}
						for (Iterator<BiTable> it = innerList.iterator(); it.hasNext();) {
						    BiTable tn = it.next();
						    if(bichain.getTable() == tn) {
						    	it.remove();
						    }
						    /*
						    if(bichain.contains(tn))  {
						    	it.remove();
						    }
						    */
						}
					}
					if(!primaryTabList.contains(bichain.getTable())) {
						notInMaster  = true;
//						isOuter = !innerList.contains(bichain.getTable());
						if(!(bichain.reverseJoin && includeNoDetail)) {
						for(BiTable primaryTable : primaryTabList){
							BiChain ptc =  bichain.findChain(primaryTable);
							if(ptc != null) {
								notInMaster  = false;
								primaryTabList.add(bichain.getTable());
								break;
							}
						}
						}
					} else {
						if(bichain.reverseJoin && includeNoDetail) {
							primaryTabList.remove(bichain.getTable());
							notInMaster = true;
						}
					}
					boolean isOuter;
					if(notInMaster) {
						isOuter = true;
						if(!(bichain.reverseJoin && includeNoDetail)) {
							if(!bichain.optional || bichain.intersetTableSet(innerList) ) {
								isOuter = false;
							}
						}
					} else isOuter = false;
					if(notInMaster ) {
						if(isOuter) s += ", outer( "; else s += " , ";
						s += bichain.getTable().getSelectFromName();
					}
					s += (bichain.makeSelectTableStr(primaryTabList,innerList,includeNoDetail,p_parent));
					if(notInMaster ) {
						if(isOuter) s += " ) ";
					}
					

//					if(notInMaster ) {
//						if(bichain.optional && 
//									(!bichain.intersetTableSet(innerList) || (bichain.reverseJoin && includeNoDetail))
//									) s += ", outer( "; else s += " , ";
//						s += bichain.getTable().getSelectFromName();
//					}
//					s += (bichain.makeSelectTableStr(primaryTabList,innerList,includeNoDetail));
//					if(notInMaster ) {
//						if(bichain.optional && 
//									(!bichain.intersetTableSet(innerList) || (bichain.reverseJoin && includeNoDetail))
//									) s += " ) ";
//					}
				}
			}
		}
		return(s);
	}
	public boolean isOptional() {
		return(optional);
	}
	/*
	public  BiChain xxgetOrAddChain(BiTable p_parentTable,BiTable p_subTable)
	{
		BiTable me = (BiTable) getCollection("table");
		UniLog.log("HAHA getOrAddChain start " + p_subTable.getName()+ " parent " + p_parentTable.getName() + " this " +  me.getName()				); 
		if(p_parentTable == getCollection("table")) {
			UniLog.log("HAHA getOrAddChain master table matched " + p_parentTable.getName());
			BiChain chain = (BiChain) (getCollection("subChains").getCollection(p_subTable.getName()));
			if(chain == null) {
				UniLog.log("BiChain add new subtable "+p_subTable.getName());
				chain = new BiChain(getCollection("subChains"),p_subTable);
				getCollection("subChains").addCollection(p_subTable.getName(),chain);
			}
			return(chain);
		} else {
			UniLog.log("HAHA getOrAddChain down one level");
			Enumeration e = getCollection("subChains").getCollectionKeys();
			if(e != null) {
				for(;e.hasMoreElements();) {
					BiChain chain = (BiChain) getCollection("subChains").getCollection((String) e.nextElement());
					UniLog.log("HAHA getOrAddChain go one master table : "+chain.getTable().getName());
//					chain = chain.getOrAddChain(chain.getTable(),p_subTable);
					chain = chain.xxgetOrAddChain(p_parentTable,p_subTable);
					if(chain != null) return(chain);
				}
			}
			return(null);
		}
	}
	*/
	protected boolean isAllOneOne() {
		BiTable master = getMaster();
		BiTable lookup = getTable();
		if(master == null) return(true);
		BiJoin join = lookup.getJoin(master.getName());								
		if(join == null) return(false);
		if(!join.isOneToOne()) return(false);
		return(getParentChain().isAllOneOne());
	}
}
