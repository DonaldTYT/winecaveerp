package com.uniinformation.jxapp.hw;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
//import java.util.StringTokenizer;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.LabelCell;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.ActionButtonHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zul.Button;


public class HwItemOption extends JxZkBase{
	CellCollection cCol = null;
	SelectUtil su = null;
	LinkedHashMap <String,Vector <LabelCell>> matOptions = null;
	LinkedHashMap <String,Vector <LabelCell>> mhcOptions = null;
	LinkedHashMap <String,Vector <LabelCell>> catOptions = null;
	@Override
	public void afterBind() {

		/*
		JxField fd = jxAdd("hw_optionlist");
		if(fd != null) {
			fd.gridSetCol(1);
			Vector v = new Vector();
			HashMap<String,Vector<LabelCell>>map = new HashMap <String,Vector<LabelCell>>();
			map.put("Option_1", new VectorUtil()
					.addElement(new LabelCell("Qty",new Cell(0)))
					.addElement(new LabelCell("Mat1",new Cell(0),LabelCell.CTYPE_COMBOBOX))
					.addElement(new LabelCell("Extra",new Cell("",Cell.VMODE_NORMAL,
									new VectorUtil()
										.addElement("Single")
										.addElement("Double")
										.toVector()
								),LabelCell.CTYPE_RADIOGROUP))
					.toVector());
			v.add("Option_1");
			v.add("Option_2");
			v.add("Option_3");
			v.add("Option_4");
			v.add("Option_5");
			v.add("Option_6");
			v.add("Option_7");
			v.add("Option_8");
			v.add("Option_9");
			fd.setItemList(v);
			fd.setItemSubOptions(map);
		}
		*/
		new JxFieldAction("itemOptionClear") {
			public void actionPerformed(JxField fd) {
				try {
//					cCol.getCell("ind_options").set("");
//					closeForm();
					setCellCollection(cCol,su,"",true) ;
//					clearAllFields();
				} catch (Exception jex) {
					UniLog.log(jex);
				}
			}
		};
		new JxFieldAction("itemOptionAccept") {
			public void actionPerformed(JxField fd) {
				try {
					JSONObject jo = new JSONObject();
					if(!jxAdd("hwopt_remark").getText().equals("")) {
						jo.put("remark", jxAdd("hwopt_remark").getText() );
					}
					if(!jxAdd("hwopt_mc").getText().equals("")) {
						jo.put("machine", jxAdd("hwopt_mc").getText() );
						if(mhcOptions != null) {
							JSONArray ja = new JSONArray();
							Vector<String> sl = jxAdd("hw_mhcoptionlist").getSelectList();
							for(String slstr : sl) {
								JSONObject oo = new JSONObject();
								oo.put("optname", slstr);
								Vector<LabelCell> vl = mhcOptions.get(slstr);
								if(vl != null && vl.size() > 0) {
									for(LabelCell lc : vl) {
										oo.put(lc.getLabel(), lc.getCell().getObject());
									}
								}
								ja.put(oo);
							}
							jo.put("mhcoptions", ja);
						}
					}
					if(!jxAdd("hwopt_mt").getText().equals("")) {
						jo.put("material", jxAdd("hwopt_mt").getText() );
						if(matOptions != null) {
							JSONArray ja = new JSONArray();
							Vector<String> sl = jxAdd("hw_matoptionlist").getSelectList();
							for(String slstr : sl) {
								JSONObject oo = new JSONObject();
								oo.put("optname", slstr);
								Vector<LabelCell> vl = matOptions.get(slstr);
								if(vl != null && vl.size() > 0) {
									for(LabelCell lc : vl) {
										oo.put(lc.getLabel(), lc.getCell().getObject());
									}
								}
								ja.put(oo);
							}
							jo.put("matoptions", ja);
						}
					}
					Vector<String> sl = jxAdd("hw_optionlist").getSelectList();
					if(catOptions != null && sl.size() > 0) {
						JSONArray ja = new JSONArray();
						for(String slstr : sl) {
							JSONObject oo = new JSONObject();
							oo.put("optname", slstr);
							Vector<LabelCell> vl = catOptions.get(slstr);
							if(vl != null && vl.size() > 0) {
								for(LabelCell lc : vl) {
									oo.put(lc.getLabel(), lc.getCell().getObject());
								}
							}
							ja.put(oo);
						}
						jo.put("catoptions", ja);
					}
					UniLog.log("HAHA jsonstr = " + jo.toString());
					if(jo.length() <= 0)
						cCol.getCell("ind_options").set("");
					else
						cCol.getCell("ind_options").set(jo.toString());
					closeForm();
				} catch (Exception jex) {
					UniLog.log(jex);
				}
			}
		};
		ActionButtonHelper.setDelayClickEventOne((Button) jxAdd("itemOptionClear").getNativeObject(), sessionHelper);
		ActionButtonHelper.setDelayClickEventOne((Button) jxAdd("itemOptionAccept").getNativeObject(), sessionHelper);
		new JxFieldChange("hwopt_mt") {
			public boolean valueChanged(JxField fd,String p_orgtext) {
				setupMaterialOption();
				jxAdd("hw_matoptionlist").setItemSubOptions(matOptions);
				return(true);
			}
		};
		new JxFieldChange("hwopt_mc") {
			public boolean valueChanged(JxField fd,String p_orgtext) {
				setupMachineOption();
				jxAdd("hw_mhcoptionlist").setItemSubOptions(mhcOptions);
				return(true);
			}
		};
	}
	
//		LinkedHashMap <String,Vector <LabelCell>> tmpOptions = new LinkedHashMap <String,Vector <LabelCell>>();
	void setupCategoryOption_0(TableRec tr,Vector v, LinkedHashMap <String,Vector <LabelCell>> tmpOptions , Vector sv
				) throws Exception{
				for(int i = 0; i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					String mo = (String) tr.getField("odf_name");
					v.add(mo);
					if(mo.startsWith(".")) sv.add(mo);
					if(catOptions != null && catOptions.get(mo) != null) {
						tmpOptions.put(mo,catOptions.get(mo));
					} else {
						Vector<LabelCell> so = new Vector<LabelCell>();
						if(!tr.getField("odf_selopt1").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt1"),",");
							String stok[] = tr.getFieldString("odf_selopt1").split(",");
							Vector<String> vs = new Vector();
							for(String ss : stok) {
								vs.add(ss);
							}
							if(vs.size() > 0) {
								so.add(new LabelCell("selopt1",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_COMBOBOX));
							}
						}
						if(!tr.getField("odf_selopt2").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt2"),",");
//							Vector<String> vs = new Vector();
//							for(;;) {
//								if(!stok.hasMoreTokens()) break;
//								vs.add(stok.nextToken());
//							}
							String stok[] = tr.getFieldString("odf_selopt2").split(",");
							Vector<String> vs = new Vector();
							for(String ss : stok) {
								vs.add(ss);
							}
							if(vs.size() > 0) {
								so.add(new LabelCell("selopt2",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_RADIOGROUP));
							}
						}
						if(so.size() > 0) tmpOptions.put(mo, so); else tmpOptions.put(mo, null);
					}
				}
		
	}
	
	void setupCategoryOption(int p_srg)
	{
		Vector v = new Vector();
		Vector sv = new Vector();
		LinkedHashMap <String,Vector <LabelCell>> tmpOptions = new LinkedHashMap <String,Vector <LabelCell>>();
			try {
				TableRec tr = su.getQueryResult("select * from optiondef where odf_type = 'C' and odf_disabled <> 'Y' order by odf_seq", null);
				setupCategoryOption_0(tr,v, tmpOptions , sv);
				tr = su.getQueryResult("select * from optiondef where odf_type = 'J' and odf_disabled <> 'Y' and odf_mrg = "+p_srg + " order by odf_seq", null);
				setupCategoryOption_0(tr,v, tmpOptions , sv);
//				for(int i = 0; i<tr.getRecordCount();i++) {
//					tr.setRecPointer(i);
//					String mo = (String) tr.getField("odf_name");
//					v.add(mo);
//					if(mo.startsWith(".")) sv.add(mo);
//					if(catOptions != null && catOptions.get(mo) != null) {
//						tmpOptions.put(mo,catOptions.get(mo));
//					} else {
//						Vector<LabelCell> so = new Vector<LabelCell>();
//						if(!tr.getField("odf_selopt1").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt1"),",");
//							Vector<String> vs = new Vector();
//							for(;;) {
//								if(!stok.hasMoreTokens()) break;
//								vs.add(stok.nextToken());
//							}
//							if(vs.size() > 0) {
//								so.add(new LabelCell("selopt1",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_COMBOBOX));
//							}
//						}
//						if(!tr.getField("odf_selopt2").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt2"),",");
//							Vector<String> vs = new Vector();
//							for(;;) {
//								if(!stok.hasMoreTokens()) break;
//								vs.add(stok.nextToken());
//							}
//							if(vs.size() > 0) {
//								so.add(new LabelCell("selopt2",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_RADIOGROUP));
//							}
//						}
//						if(so.size() > 0) tmpOptions.put(mo, so); else tmpOptions.put(mo, null);
//					}
//				}
				jxAdd("hw_optionlist").setItemList(v);
				catOptions = tmpOptions;
			} catch (Exception ex ){
				UniLog.log(ex);
			}
		jxAdd("hw_optionlist").setSelectList(sv);
	}
	void setupMaterialOption()
	{
		Vector v = new Vector();
		Vector sv = new Vector();
		LinkedHashMap <String,Vector <LabelCell>> tmpOptions = new LinkedHashMap <String,Vector <LabelCell>>();
		if(!jxGetText("hwopt_mt").equals(""))  {
			try {
				TableRec tr = su.getQueryResult("select * from material,optiondef where mt_name = '" + jxGetText("hwopt_mt") + "' and odf_type = 'M' and odf_disabled <> 'Y' and odf_mrg = mt_rg order by odf_seq", null);
				for(int i = 0; i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					String mo = (String) tr.getField("odf_name");
					v.add(mo);
					if(mo.startsWith(".")) sv.add(mo);
					if(matOptions != null && matOptions.get(mo) != null) {
						tmpOptions.put(mo,matOptions.get(mo));
					} else {
						Vector<LabelCell> so = new Vector<LabelCell>();
						if(!tr.getField("odf_selopt1").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt1"),",");
//							Vector<String> vs = new Vector();
//							for(;;) {
//								if(!stok.hasMoreTokens()) break;
//								vs.add(stok.nextToken());
//							}
							String stok[] = tr.getFieldString("odf_selopt1").split(",");
							Vector<String> vs = new Vector();
							for(String ss : stok) {
								vs.add(ss);
							}	
							if(vs.size() > 0) {
								so.add(new LabelCell("selopt1",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_COMBOBOX));
							}
						}
						if(!tr.getField("odf_selopt2").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt2"),",");
//							Vector<String> vs = new Vector();
//							for(;;) {
//								if(!stok.hasMoreTokens()) break;
//								vs.add(stok.nextToken());
//							}
							String stok[] = tr.getFieldString("odf_selopt2").split(",");
							Vector<String> vs = new Vector();
							for(String ss : stok) {
								vs.add(ss);
							}	
							if(vs.size() > 0) {
								so.add(new LabelCell("selopt2",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_RADIOGROUP));
							}
						}
						if(so.size() > 0) tmpOptions.put(mo, so); else tmpOptions.put(mo, null);
					}
				}
				jxAdd("hw_matoptionlist").setItemList(v);
				matOptions = tmpOptions;
			} catch (Exception ex ){
				UniLog.log(ex);
			}
		} else {
			jxAdd("hw_matoptionlist").setItemList(v);
		}
		jxAdd("hw_matoptionlist").setSelectList(sv);
	}

	void setupMachineOption()
	{
		Vector v = new Vector();
		Vector sv = new Vector();
		LinkedHashMap <String,Vector <LabelCell>> tmpOptions = new LinkedHashMap <String,Vector <LabelCell>>();
		if(!jxGetText("hwopt_mc").equals(""))  {
			try {
				TableRec tr = su.getQueryResult("select * from stock,optiondef where st_iname = '" + jxGetText("hwopt_mc") + "' and odf_type = 'H' and odf_disabled <> 'Y' and odf_mrg = st_irg order by odf_seq", null);
				for(int i = 0; i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					String mo = (String) tr.getField("odf_name");
					v.add(mo);
					if(mo.startsWith(".")) sv.add(mo);
					if(mhcOptions != null && mhcOptions.get(mo) != null) {
						tmpOptions.put(mo,mhcOptions.get(mo));
					} else {
						Vector<LabelCell> so = new Vector<LabelCell>();
						if(!tr.getField("odf_selopt1").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt1"),",");
//							Vector<String> vs = new Vector();
//							for(;;) {
//								if(!stok.hasMoreTokens()) break;
//								vs.add(stok.nextToken());
//							}
							String stok[] = tr.getFieldString("odf_selopt1").split(",");
							Vector<String> vs = new Vector();
							for(String ss : stok) {
								vs.add(ss);
							}	
							if(vs.size() > 0) {
								so.add(new LabelCell("selopt1",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_COMBOBOX));
							}
						}
						if(!tr.getField("odf_selopt2").equals("")) {
//							StringTokenizer stok = new StringTokenizer((String) tr.getField("odf_selopt2"),",");
//							Vector<String> vs = new Vector();
//							for(;;) {
//								if(!stok.hasMoreTokens()) break;
//								vs.add(stok.nextToken());
//							}
							String stok[] = tr.getFieldString("odf_selopt2").split(",");
							Vector<String> vs = new Vector();
							for(String ss : stok) {
								vs.add(ss);
							}	
							if(vs.size() > 0) {
								so.add(new LabelCell("selopt2",new Cell("",Cell.VMODE_NORMAL,vs),LabelCell.CTYPE_RADIOGROUP));
							}
						}
						if(so.size() > 0) tmpOptions.put(mo, so); else tmpOptions.put(mo, null);
					}
				}
				jxAdd("hw_mhcoptionlist").setItemList(v);
				mhcOptions = tmpOptions;
			} catch (Exception ex ){
				UniLog.log(ex);
			}
		} else {
			jxAdd("hw_mhcoptionlist").setItemList(v);
		}
		jxAdd("hw_mhcoptionlist").setSelectList(sv);
	}
	
	void clearAllFields()
	
	{
		jxAdd("hwopt_mc").setText("");
		jxAdd("hwopt_mt").setText("");
		jxAdd("hw_matoptionlist").setItemList(null);
		jxAdd("hw_matoptionlist").setSelectList(null);
		jxAdd("hw_mhcoptionlist").setItemList(null);
		jxAdd("hw_mhcoptionlist").setSelectList(null);
		matOptions = null;
		jxAdd("hw_optionlist").setItemList(null);
		jxAdd("hw_optionlist").setSelectList(null);
		catOptions = null;
	}
	
	void setOrAddLabelCell(Vector <LabelCell> vl,String label,Object val,int labelType) throws Exception {
		if(val == null) return;
		LabelCell lc = null;
		for(LabelCell llc : vl) {
			if(llc.getLabel().equals(label)) {
				lc = llc;
				break;
			}
		}
		if(lc == null) {
			Cell cc=null;
			if(val instanceof Integer) cc = new Cell(((Integer) val).intValue());
			if(val instanceof String) cc = new Cell();
			if(cc == null) throw new Exception("Option value unsupported");
			lc = new LabelCell(label,cc,labelType);
		} else lc.getCell().set(val);
	}
	public boolean setCellCollection(CellCollection p_col,SelectUtil p_su,String p_default)  {
		return(setCellCollection(p_col,p_su,p_default,false));
	}
	public boolean setCellCollection(CellCollection p_col,SelectUtil p_su,String p_default,boolean p_clear) 
	{
		clearAllFields();
		cCol = p_col;
		su = p_su;
		int srg = p_col.getCell("ind_srg").getInt();
		if(srg > 0) {
			try {
				TableRec tr = p_su.getQueryResult("select st_irg,st_iname from mcfitmodel,stock"
					+ " where mcfm_modelrg = " + srg + " and st_irg = mcfm_mrg"
					,null);
				Vector il = new Vector();
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					UniLog.log(tr.getField("st_iname").toString());
					il.add(tr.getField("st_iname"));
				}
				jxAdd("hwopt_mc").setItemList(il);
				
				tr = p_su.getQueryResult("select mt_rg,mt_name from material"
						,null);
					il = new Vector();
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						il.add(tr.getField("mt_name"));
					}
				jxAdd("hwopt_mt").setItemList(il);
//				il = new Vector();
//				il.add("Gross Lamination");
//				il.add("Matt Lamination");
//				jxAdd("hwopt_fw").setItemList(il);

				setupCategoryOption(srg);
				
				JSONObject jo = null;
				if(!p_clear && !p_col.getCell("ind_options").getString().equals("")) {
					jo = new JSONObject( p_col.getCell("ind_options").getString());
				}
				if(jo == null && p_default != null && !p_default.trim().equals("")) {
					jo = new JSONObject( p_default);
				}
				if(jo != null) {
					String s;
					if((s = jo.optString("remark")) != null) {
						jxAdd("hwopt_remark").setText(s);
					} else jxAdd("hwopt_remark").setText("");
					if((s = jo.optString("machine")) != null) {
						jxAdd("hwopt_mc").setText(s);
						LinkedHashSet<String> sl = new LinkedHashSet<String>();
						setupMachineOption();
						Vector <String> slv = jxAdd("hw_mhcoptionlist").getSelectList();
						for(String ss : slv) {
							if(ss.startsWith(".")) {
								sl.add(ss);
							}
						}
						JSONArray mo = jo.optJSONArray("mhcoptions");
						if(mo != null) {
							for(int i=0;i<mo.length();i++) {
								JSONObject o = (JSONObject) mo.get(i);
								sl.add(o.getString("optname"));
								Vector <LabelCell> vl = mhcOptions.get(o.getString("optname"));
								if(vl == null) {
									vl = new Vector<LabelCell>();
									mhcOptions.put(o.getString("optname"), vl);
								}
								setOrAddLabelCell(vl, "selopt1", o.opt("selopt1"), LabelCell.CTYPE_COMBOBOX);
								setOrAddLabelCell(vl, "selopt2", o.opt("selopt2"), LabelCell.CTYPE_RADIOGROUP);
							}
						}
						slv = new Vector();
						for(String ss:sl) {
							slv.add(ss);
						}
						if(slv.size() > 0) jxAdd("hw_mhcoptionlist").setSelectList(slv);
					}
					if((s = jo.optString("material")) != null) {
						jxAdd("hwopt_mt").setText(s);
						LinkedHashSet<String> sl = new LinkedHashSet<String>();
						setupMaterialOption();
						Vector <String> slv = jxAdd("hw_matoptionlist").getSelectList();
						for(String ss : slv) {
							if(ss.startsWith(".")) {
								sl.add(ss);
							}
						}
						JSONArray mo = jo.optJSONArray("matoptions");
						if(mo != null) {
							for(int i=0;i<mo.length();i++) {
								JSONObject o = (JSONObject) mo.get(i);
								sl.add(o.getString("optname"));
								Vector <LabelCell> vl = matOptions.get(o.getString("optname"));
								if(vl == null) {
									vl = new Vector<LabelCell>();
									matOptions.put(o.getString("optname"), vl);
								}
//								if(o.optString("selopt1") != null) setOrAddLabelCell(vl, "selopt1", o.getString("selopt1"));
//								if(o.optString("selopt2") != null) setOrAddLabelCell(vl, "selopt2", o.getString("selopt2"));
								setOrAddLabelCell(vl, "selopt1", o.opt("selopt1"), LabelCell.CTYPE_COMBOBOX);
								setOrAddLabelCell(vl, "selopt2", o.opt("selopt2"), LabelCell.CTYPE_RADIOGROUP);
							}
						}
						slv = new Vector();
						for(String ss:sl) {
							slv.add(ss);
						}
						if(slv.size() > 0) jxAdd("hw_matoptionlist").setSelectList(slv);
					}
					JSONArray co = jo.optJSONArray("catoptions");
					if(co != null) {
						LinkedHashSet<String> sl = new LinkedHashSet<String>();
						Vector <String> slv = jxAdd("hw_optionlist").getSelectList();
						for(String ss : slv) {
							if(ss.startsWith(".")) {
								sl.add(ss);
							}
						}
						for(int i=0;i<co.length();i++) {
							JSONObject o = (JSONObject) co.get(i);
							sl.add(o.getString("optname"));
							Vector <LabelCell> vl = catOptions.get(o.getString("optname"));
							if(vl == null) {
								vl = new Vector<LabelCell>();
								catOptions.put(o.getString("optname"), vl);
							}
							setOrAddLabelCell(vl, "selopt1", o.opt("selopt1"), LabelCell.CTYPE_COMBOBOX);
							setOrAddLabelCell(vl, "selopt2", o.opt("selopt2"), LabelCell.CTYPE_RADIOGROUP);
						}
						slv = new Vector();
						for(String ss:sl) {
							slv.add(ss);
						}
						if(slv.size() > 0) jxAdd("hw_optionlist").setSelectList(slv);
					}
				} else {
					jxSetText("hwopt_remark","");
				}
				jxAdd("hw_mhcoptionlist").setItemSubOptions(mhcOptions);
				jxAdd("hw_matoptionlist").setItemSubOptions(matOptions);
				jxAdd("hw_optionlist").setItemSubOptions(catOptions);
				return(true);
			} catch (Exception ex) {
				UniLog.log(ex);
				messageBox("Error setting up option screen");
				return(false);
			}
		}  else {
				messageBox("Error : Depertment not set");
				return(false);
		}
	}
}
