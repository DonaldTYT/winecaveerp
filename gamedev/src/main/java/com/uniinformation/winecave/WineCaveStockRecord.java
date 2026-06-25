package com.uniinformation.winecave;

import java.util.List;

import com.uniinformation.utils.UniLog;

public class WineCaveStockRecord {
	int irg = 0;
	int org = 0;
	String region = null;
	String color = null;
	int vint = 0;
	String name = null;
	String packing = null;
	int csQty = 0;
	int btlQty = 0;
	double price = 0.0;
	String score = null;
	double score0, score1;
	String remark = null;
	String cname = null;
	String wclass = null;
	String mtype = null;
	String country = null;
	public void setMtype(String s) {
		mtype = s;
	}
	public String getMtype() {
		return(mtype);
	}
	public void setCountry (String s) {
		country = s;
	}
	public String getCountry() {
		return(country);
	}
	public void setIrg(int p_irg) {
		irg = p_irg;
	}
	public int getIrg() {
		return(irg);
	}
	public void setOrg(int p_org) {
		org = p_org;
	}
	public int getOrg() {
		return(org);
	}
	public void setRegion(String p_region) {
		region = p_region;
	}
	public String getRegion() {
		return(region);
	}
	public void setColor(String p_color) {
		color = p_color;
	}
	public String getColor() {
		return(color); 
	}
	public void setVint(int p_vint) {
		vint = p_vint;
	}
	public int getVint() {
		return(vint);
	}
	public void setName(String p_name) {
		name = p_name;
	}
	public String getName() {
		return(name);
	}
	public void setPacking( String p_packing) {
		packing = p_packing;
	}
	public String getPacking() {
		return(packing);
	}
	public void setCsQty(int p_csQty) {
		csQty = p_csQty;
	}
	public int getCsQty() {
		return(csQty);
	}
	public void setBtlQty(int p_btlQty) {
		btlQty = p_btlQty;
	}
	public int getBtlQty() {
		return(btlQty);
	}
	public void setPrice(double p_price) {
		price = p_price;
	}
	public double getPrice() {
		return(price);
	}
	public void setScore(String p_score) {
		score = p_score;
	}
	public void setScore0(double p_score) {
		score0 = p_score;
	}
	public void setScore1(double p_score) {
		score1 = p_score;
	}
	public String getScore() {
		return(score);
	}
	public double getScore0() {
		return(score0);
	}
	public double getScore1() {
		return(score1);
	}
	public void setRemark(String p_remark) {
		remark = p_remark;
	}
	public String getRemark() {
		return(remark);
	}
	public void setCname(String p_cname) {
		cname = p_cname;
	}
	public String getCname() {
		return(cname);
	}
	public void setWineClass(String p_class) {
		wclass = p_class;
	}
	public String getWineClass() {
		return(wclass);
	}
	static public boolean checkIsEqual(WineCaveStockRecord p_r1,WineCaveStockRecord p_r2) {
		if(p_r1.irg != p_r2.irg) return(false);
		if(p_r1.org != p_r2.org) return(false);
		if(!p_r1.region.equals(p_r2.region)) return(false);
		if(!p_r1.color.equals(p_r2.color)) return(false);
		if(p_r1.vint != p_r2.vint) return(false);
		if(!p_r1.name.equals(p_r2.name)) return(false);
		if(!p_r1.packing.equals(p_r2.packing)) return(false);
		if(p_r1.csQty != p_r2.csQty) return(false);
		if(p_r1.btlQty != p_r2.btlQty) return(false);
		if(p_r1.price != p_r2.price) return(false);
		if(!p_r1.score.equals(p_r2.score)) return(false);
		if(p_r1.score0 != p_r2.score0) return(false);
		if(p_r1.score1 != p_r2.score1) return(false);
		if(!p_r1.remark.equals(p_r2.remark)) return(false);
		if (p_r1.cname == null || p_r2.cname == null) {
			UniLog.log("HAHA cname got null");
		}
		if(!p_r1.cname.equals(p_r2.cname)) return(false);
		return(true);
	}
	static public boolean checkIsListEqual(List<WineCaveStockRecord> p_l1,List<WineCaveStockRecord> p_l2) {
		if(p_l1.size() != p_l2.size()) return(false);
		for(int i=0;i<p_l1.size();i++) {
			WineCaveStockRecord r1 = p_l1.get(i);
			WineCaveStockRecord r2 = p_l2.get(i);
			if(!checkIsEqual(r1,r2)) return(false);
		}
		return(true);
	}
	static public void dumpStockRecord(List<WineCaveStockRecord> p_l) {
		for(int i = 0;i<p_l.size();i++)  {
			WineCaveStockRecord sr  = p_l.get(i);
			UniLog.log(String.format("%6d %6d %6d %6d %f", sr.irg,sr.org,sr.csQty,sr.btlQty,sr.price));
		}
	}
}


