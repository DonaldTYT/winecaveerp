package com.uniinformation.erpv4;


import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import com.uniinformation.accumulator.DateBalanceAccumulator;
import com.uniinformation.accumulator.CostCalculator;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config.LOCATION_TYPE;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;


public class CostCalculation extends CostCalculator {
	static public final long maxRemoteIdle=60000L;
	static public final long maxRemoteDelay=60000L;
	static public final int maxRemoteTimeout=5000;
	static Hashtable<String,Hashtable<String,CostCalculation>> agentHash = new Hashtable();
	static boolean disabled = false;
	static boolean debug = false;
	static String costAgent = null;
	static String costHost = null;
	static int costPort = 0;
	static RpcClient remoteRpc = null;
	static HashSet<Integer> needUpdIrgSet = null;
	static long lastConnect = 0L;
	static long lastCall = 0L;
	static Boolean useNewCosting = null;
	Hashtable <String,DateBalanceAccumulator> locBalanceHash;
	static HashMap<Integer,HashSet<String>> costLoc =  null;
	static Date costLocDate = null;
	
	static public synchronized void debug(boolean p_sw) {
		debug = p_sw;
	}

	
	public CostCalculation(Comparable p_maxValue,boolean isNewCosting,boolean isFifo) {
		super(p_maxValue,isNewCosting,isFifo);
		// TODO Auto-generated constructor stub
	}
	
	static void resetRemoteRpc() {
		if(remoteRpc != null) {
			remoteRpc.close();
			remoteRpc = null;
			lastConnect = 0L;
			lastCall = 0L;
			
		}
	}
	synchronized public static void clearCostCalculation() throws Exception {
		UniLog.log("Clear All Cost Calculation Cache");
		agentHash.clear();
		resetRemoteRpc();
		costAgent = null;
		costHost = null;
		costPort = 0;
	}
	public static void clearCostTable(SessionHelper p_sh,int p_irg,int p_org) throws Exception {
		clearCostTable(p_sh,getCostKey(p_irg,p_org));
	}

	synchronized public static void clearCostTable(SessionHelper p_sh,String p_costkey) throws Exception {
//		Hashtable<String,CostCalculation> costCalculationHash  = (Hashtable<String,CostCalculation>) p_sh.getSessionData("CostCalculationHash");
		CellCollection col = new CellCollection();
		col.addCell("costkey", new Cell(p_costkey));
		RecSync.updateOneRecord(p_sh.getAgent(), "StockBalancePush",col);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) return;
		synchronized(costCalculationHash) {
			costCalculationHash.remove( p_costkey);
			/*
			EventQueue que = EventQueues.lookup("wacost_"+p_sh.getAgent(), EventQueues.APPLICATION, false);
				if(que != null) {
				que.publish(new Event("onCostClear", null,p_costkey));
			}
			*/
			Object que = p_sh.lookupEventQueue("wacost_"+p_sh.getAgent(), SessionHelper.EVENT_TYPE.APPLICATION , false);
			if(que != null) {
				p_sh.publishEventQueue(que,"onCostClear", p_costkey);
			}
		}
	}
	
	public static String getCostKey(int p_irg,int p_org) {
		return("c_"+p_irg+"_"+p_org);
	}

	static public String getCostKeyIrgOnly(String p_costkey) {
		int idx0 = p_costkey.indexOf('_');
		if(idx0 < 0) return(p_costkey);
		int idx1 = p_costkey.substring(idx0+1).indexOf('_');
		if(idx0 < 0) return(p_costkey);
		return(p_costkey.substring(0,idx0+idx1+1)+"_0");
	}

	static public int costKeyToIrg(String p_costkey) {
		int sidx = p_costkey.indexOf("c_");
		if(sidx >= 0) {
			String ss = p_costkey.substring(sidx+2);
			sidx = ss.indexOf("_");
			if(sidx >= 0) {
				ss = ss.substring(0,sidx);
				return(Integer.parseInt(ss));
			}
		}
		return(0);
	}
	

	static CostCalculation loadCostCalculation( Hashtable<String,CostCalculation> costCalculationHash,int p_irg,int p_org,SessionHelper p_sh) throws Exception
	{
				CostCalculation cl = new CostCalculation(DateUtil.maxDate,useNewCosting(p_sh),useFifoCosting(p_sh));
				cl.setDebug(debug);
				costCalculationHash.put(getCostKey(p_irg,p_org),cl);
				java.util.Date balBeginDate;
				if(getCostAgent(p_sh,p_org).equals("specific") ) {
					SelectUtil su = null;
					try {
						su = new SelectUtil();
						su.init(p_sh.getBiSchema().getConn());
						TableRec tr;
						tr = su.getQueryResult("select * from stmovd where stmd_irg = ? and stmd_org = ? and stmd_tdtype in (" + Erpv4Config.STOCKIN_TDtypes + ") and stmd_qty > 0",
								new Wherecl().appendArgument(p_irg).appendArgument(p_org)
								);
						double tQty  = 1;
						double tCost = 0;
						if(tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							tQty = tr.getFieldDouble("stmd_qty");
							tCost = tr.getFieldDouble("stmd_exprice1");
						} else {
							UniLog.log("cost record not found");
						}

						balBeginDate = DateUtil.dateTimeStrToDate("2008/01/01");
						cl.updateBalanceWithCost(DateUtil.prevday(balBeginDate), tQty, 0, 1,tCost);
						cl.recalAverageBuyBeforeSell(new Date());
						UniLog.log("cache specific stock cost " + p_irg + " " + p_org + " " + tQty + " " + tCost + " av " + (tQty == 0 ? 0 : tCost/tQty ));
					}
					catch(Exception ex) {
						throw ex;
					}
					finally {
						su.close();
					}
				} else {
					SelectUtil su = null;
					try {
						su = new SelectUtil();
						su.init(p_sh.getBiSchema().getConn());
						TableRec tr;
						balBeginDate = Erpv4Config.getCostOpeningErpDate(p_sh);
						boolean requireLoc = Erpv4Config.requiredLoc(p_sh);
						if(requireLoc) {
							cl.locBalanceHash = new Hashtable<String,DateBalanceAccumulator>();
						}
						if(balBeginDate.after(DateUtil.zeroDate)) {
							StockOpening sto = Erpv4Config.getStockOpening(su, p_irg, balBeginDate,requireLoc);
							if(sto != null) { 
								cl.updateBalanceWithCost(balBeginDate, sto.balance, 0, 1,sto.unitcost * sto.balance);
								if(requireLoc) {
									for(String loc : sto.locBalance.keySet()) {
										DateBalanceAccumulator bac = cl.locBalanceHash.get(loc);
										if(bac == null) {
											bac = new DateBalanceAccumulator();
											cl.locBalanceHash.put(loc, bac);
										}
										bac.updateBalance(balBeginDate, sto.locBalance.get(loc), 0.0, 1,true);
									}
								}
							}
						}
					
						if(p_org > 0)
						tr = su.getQueryResult("select stmd_date,stmd_loc,stmd_mrg,stmd_tdtype,stmd_exprice1,stmd_qty,stmd_direction from stmovd,stmov where stm_mrg = stmd_mrg and stmd_direction <> 0 and stm_status = 'Confirmed' and stmd_irg = ? and stmd_org = ? and stmd_date > ? order by stmd_date,stmd_direction desc, stmd_mrg", 
								new Wherecl().appendArgument(p_irg).appendArgument(p_org).appendArgument(balBeginDate)
								);
						else
						tr = su.getQueryResult("select stmd_date,stmd_loc,stmd_mrg,stmd_tdtype,stmd_exprice1,stmd_qty,stmd_direction from stmovd,stmov where stm_mrg = stmd_mrg and stmd_direction <> 0 and stm_status = 'Confirmed' and stmd_irg = ? and stmd_date > ? order by stmd_date,stmd_direction desc, stmd_mrg", 
								new Wherecl().appendArgument(p_irg).appendArgument(balBeginDate)
								);

						for(int i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							double dir = tr.getFieldDouble("stmd_direction");
							java.util.Date d = tr.getFieldDate("stmd_date");
							double qty = tr.getFieldDouble("stmd_qty");
							double exprice1 = tr.getFieldDouble("stmd_exprice1");
							String tt =  tr.getFieldString("stmd_tdtype");
							String loc = tr.getFieldString("stmd_loc");
//							if(
//									tt.equals( "KO") ||
//									tt.equals( "KI")
//									) {
//									int cc;
//									cc = 0;
//							}
							if(!isCostLoc(p_sh,loc,d,p_org)) {
//								if(tt.equals( "JO")) {
//									int cc;
//									cc = 0;
//								}
								continue;
							}
							if(Erpv4BaseCellCollection.stkInQty.contains(tt)) {
								cl.updateBalanceWithCost(d, qty, 0, 1,exprice1);
								UniLog.log("cache weighted average stock in cost " + p_irg + " " + p_org + " " + DateUtil.toDateString(d, "yymmdd") + " " + qty + " " + exprice1);
							} else if(Erpv4BaseCellCollection.stkOutQty.contains(tt)) {
								cl.updateBalanceWithCost(d, 0,-qty, 1,0);
							}
							if(requireLoc) {
								DateBalanceAccumulator bac = cl.locBalanceHash.get(loc);
								if(bac == null) {
									bac = new DateBalanceAccumulator();
									cl.locBalanceHash.put(loc, bac);
								}
								if(dir > 0) {
									bac.updateBalance(d, qty , 0.0, 1,true);
								} else if(dir < 0) {
									bac.updateBalance(d, -qty ,0.0, 1,true);
								}
								/*
								if(dir > 0) {
									bac.updateBalance(d, qty , 0.0, 1,true);
								} else if(dir < 0) {
									bac.updateBalance(d, 0.0, -qty , 1,true);
								}
								*/
							}
						}
						//				cl.recalAverageBuyBeforeSell(DateUtil.maxDate);
						cl.recalAverageBuyBeforeSell(new Date());
						//				double d = cl.getAverageCost(DateUtil.dateTimeStrToDate("2021/01/01"));
//						UniLog.log("cache weighted average stock cost " + p_irg + " " + p_org + " " + DateUtil.toDateString(d, "yymmdd") + );
					}
					catch(Exception ex) {
						throw ex;
					}
					finally {
						if (su != null) su.close();
					}
				}
				return(cl);
	}
	
	synchronized public static double getCostOfGoodSold(SessionHelper p_sh, int p_irg, int p_org,java.util.Date p_date) throws Exception {
		String costAgent = getCostAgent(p_sh,p_org);
		if(disabled) return(0.0);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) {
			costCalculationHash = new Hashtable<String,CostCalculation>();
			agentHash.put(p_sh.getAgent(),costCalculationHash);
		}

		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) {
//				if(!getCostAgent(p_sh,p_org).equals("local") && !getCostAgent(p_sh,p_org).equals("specific") ) {
				if(!costAgent.equals("local") && !costAgent.equals("specific") ) {
					RpcClient rpc = cacheGetRpcClient(p_org);
					if(rpc == null) return(Double.NaN); 
					try {
						Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.erpv4GetStockAverageCost", 
									new VectorUtil()
									.addElement("clerptst")
									.addElement(p_irg)
									.addElement(p_org)
									.toVector()
								);
						if(v == null) {
							resetRemoteRpc();
							return(Double.NaN); 
						}
						String ss = v.toString();
						if(!ss.startsWith("OK")) return(Double.NaN);
						cl = new CostCalculation(DateUtil.maxDate,useNewCosting(p_sh),useFifoCosting(p_sh));
						cl.fromJson(ss.substring(4));
						cl.setDebug(debug);
						costCalculationHash.put(getCostKey(p_irg,p_org),cl);
					} catch (Exception ex) {
						resetRemoteRpc();
						UniLog.log(ex);
						return(Double.NaN); 
					}
				} else cl = loadCostCalculation(costCalculationHash,p_irg,p_org,p_sh);
			}
			Date dd = p_date;
			if(!DateUtil.maxDate.after(dd)) {
				dd = DateUtil.prevday(dd);
			}
			return( cl.getCostOfGoodSold(p_date));
		}
	}
	synchronized public static double getRealizedPL(SessionHelper p_sh, int p_irg, int p_org,java.util.Date p_date,double pAmount ) throws Exception {
		String costAgent = getCostAgent(p_sh,p_org);
		if(disabled) return(0.0);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) {
			costCalculationHash = new Hashtable<String,CostCalculation>();
			agentHash.put(p_sh.getAgent(),costCalculationHash);
		}

		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) {
//				if(!getCostAgent(p_sh,p_org).equals("local") && !getCostAgent(p_sh,p_org).equals("specific") ) {
				if(!costAgent.equals("local") && !costAgent.equals("specific") ) {
					RpcClient rpc = cacheGetRpcClient(p_org);
					if(rpc == null) return(Double.NaN); 
					try {
						Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.erpv4GetStockAverageCost", 
									new VectorUtil()
									.addElement("clerptst")
									.addElement(p_irg)
									.addElement(p_org)
									.toVector()
								);
						if(v == null) {
							resetRemoteRpc();
							return(Double.NaN); 
						}
						String ss = v.toString();
						if(!ss.startsWith("OK")) return(Double.NaN);
						cl = new CostCalculation(DateUtil.maxDate,useNewCosting(p_sh),useFifoCosting(p_sh));
						cl.fromJson(ss.substring(4));
						cl.setDebug(debug);
						costCalculationHash.put(getCostKey(p_irg,p_org),cl);
					} catch (Exception ex) {
						resetRemoteRpc();
						UniLog.log(ex);
						return(Double.NaN); 
					}
				} else cl = loadCostCalculation(costCalculationHash,p_irg,p_org,p_sh);
			}
			Date dd = p_date;
			if(!DateUtil.maxDate.after(dd)) {
				dd = DateUtil.prevday(dd);
			}
			return(cl.getRealizedPL(p_date, pAmount));
		}
	}

	synchronized public static String getWaCostCache(SessionHelper p_sh, int p_irg, int p_org) throws Exception {
		String costAgent = getCostAgent(p_sh,p_org);
		if(disabled) return(null);

		/*
		if(costAgent == null) {
			String ss = Erpv4Config.getString(p_sh, "CostingAgent");
			if(ss == null || ss.equals("")) {
				costAgent ="local";
			} else if(ss.equals("none")) {
				costAgent ="none";
			} else if(ss.equals("specific")) {
				costAgent ="specific";
			} else {
				int cc = ss.indexOf("@");
				if(cc >= 0) {
					costAgent = ss.substring(0,cc);
					int c2 = ss.indexOf(":");
					if( c2 >= 0) {
						costHost = ss.substring(cc+1,c2);
						costPort = Integer.parseInt(ss.substring(c2+1));
					}
				}
			}
		}
		*/
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) {
			costCalculationHash = new Hashtable<String,CostCalculation>();
			agentHash.put(p_sh.getAgent(),costCalculationHash);
		}
		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) {
//				if(!getCostAgent(p_sh,p_org).equals("local") && !getCostAgent(p_sh,p_org).equals("specific") ) return(null);
				if(!costAgent.equals("local") && !costAgent.equals("specific") ) return(null);
				cl = loadCostCalculation(costCalculationHash,p_irg,p_org,p_sh);
			}
//			return(cl.avCostsToJson());
			return(cl.toJson());
		}
	}

	synchronized public static List<Pair<Comparable,Double>> getBalanceFifo(SessionHelper p_sh, int p_irg, int p_org,java.util.Date p_date ,Double p_qty) throws Exception {
		if(disabled) return(null);
		getWaCost(p_sh, p_irg, p_org,p_date);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) return(null);
		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) return(null);
			return(cl.getBalanceEndFifo(p_date,p_qty));
		}
	}
	synchronized public static List<Pair<Comparable,Double>> getBalanceFifo(SessionHelper p_sh, int p_irg, int p_org,java.util.Date p_date ) throws Exception {
		if(disabled) return(null);
		getWaCost(p_sh, p_irg, p_org,p_date);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) return(null);
		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) return(null);
			return(cl.getBalanceEndFifo(p_date));
		}
	}
	synchronized public static List<Pair<Comparable,Double>> getLocBalanceFifo(SessionHelper p_sh, int p_irg, int p_org,String p_loc,java.util.Date p_date ) throws Exception {
		if(disabled) return(null);
		getWaCost(p_sh, p_irg, p_org,p_date);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) return(null);
		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) return(null);
			if(cl.locBalanceHash == null) return(null);
			DateBalanceAccumulator dac = cl.locBalanceHash.get(p_loc);
			if(dac == null) return(null);
			return(dac.getBalanceEndFifo(p_date));
		}
		
	}
	synchronized public static double getBalance(SessionHelper p_sh, int p_irg, int p_org,java.util.Date p_date ) throws Exception {
		if(disabled) return(0.0);
		getWaCost(p_sh, p_irg, p_org,p_date);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) return(0.0);
		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) return(0.0);
			return(cl.getBalanceEnd(p_date));
		}
	}
	synchronized public static double getLocBalance(SessionHelper p_sh, int p_irg, int p_org,String p_loc,java.util.Date p_date ) throws Exception {
		if(disabled) return(0.0);
		getWaCost(p_sh, p_irg, p_org,p_date);
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) return(0.0);
		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) return(0.0);
			if(cl.locBalanceHash == null) return(0.0);
			DateBalanceAccumulator dac = cl.locBalanceHash.get(p_loc);
			if(dac == null) return(0.0);
			return(dac.getBalanceEnd(p_date));
		}
	}
//	synchronized public static double getLastWaCost(SessionHelper p_sh, int p_irg, int p_org) throws Exception {
//		if(disabled) return(0.0);
//		/*
//		if(costAgent == null) {
//			String ss = Erpv4Config.getString(p_sh, "CostingAgent");
//			if(ss == null || ss.equals("")) {
//				costAgent ="local";
//			} else if(ss.equals("none")) {
//				costAgent ="none";
//			} else if(ss.equals("specific")) {
//				costAgent ="specific";
//			} else {
//				int cc = ss.indexOf("@");
//				if(cc >= 0) {
//					costAgent = ss.substring(0,cc);
//					int c2 = ss.indexOf(":");
//					if( c2 >= 0) {
//						costHost = ss.substring(cc+1,c2);
//						costPort = Integer.parseInt(ss.substring(c2+1));
//					}
//				}
//			}
//		}
//		*/
//		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
//		if(costCalculationHash == null) {
//			costCalculationHash = new Hashtable<String,CostCalculation>();
//			agentHash.put(p_sh.getAgent(),costCalculationHash);
//		}
//
//		synchronized(costCalculationHash) {
//			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
//			if(cl == null) {
//				if(!getCostAgent(p_sh,p_org).equals("local") && !getCostAgent(p_sh,p_org).equals("specific") ) {
//					/*
//					RpcClient rpc = rpc = new RpcClient(costHost,costPort);
//					rpc.open();
//					Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.erpv4GetStockAverageCost", 
//									new VectorUtil()
//									.addElement("clerptst")
//									.addElement(p_irg)
//									.addElement(p_org)
//									.toVector()
//								);
//					rpc.close();
//					*/
//					RpcClient rpc = cacheGetRpcClient(p_org);
//					if(rpc == null) return(Double.NaN); 
//					try {
//						Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.erpv4GetStockAverageCost", 
//									new VectorUtil()
//									.addElement("clerptst")
//									.addElement(p_irg)
//									.addElement(p_org)
//									.toVector()
//								);
//						if(v == null) {
//							resetRemoteRpc();
//							return(Double.NaN); 
//						}
//						String ss = v.toString();
//						if(!ss.startsWith("OK")) return(Double.NaN);
//						cl = new CostCalculation(DateUtil.maxDate);
//						cl.fromJson(ss.substring(4));
//						cl.setDebug(debug);
//						costCalculationHash.put(getCostKey(p_irg,p_org),cl);
//					} catch (Exception ex) {
//						resetRemoteRpc();
//						UniLog.log(ex);
//						return(Double.NaN); 
//					}
//				} else cl = loadCostCalculation(costCalculationHash,p_irg,p_org,p_sh);
//			}
//			return(cl.getLastValidCost());
//		}
//	}
	synchronized public static double getWaCost(SessionHelper p_sh, int p_irg, int p_org,java.util.Date p_date ) throws Exception {
		String costAgent = getCostAgent(p_sh,p_org);
		if(disabled) return(0.0);
		/*
		if(costAgent == null) {
			String ss = Erpv4Config.getString(p_sh, "CostingAgent");
			if(ss == null || ss.equals("")) {
				costAgent ="local";
			} else if(ss.equals("none")) {
				costAgent ="none";
			} else if(ss.equals("specific")) {
				costAgent ="specific";
			} else {
				int cc = ss.indexOf("@");
				if(cc >= 0) {
					costAgent = ss.substring(0,cc);
					int c2 = ss.indexOf(":");
					if( c2 >= 0) {
						costHost = ss.substring(cc+1,c2);
						costPort = Integer.parseInt(ss.substring(c2+1));
					}
				}
			}
		}
		*/
		Hashtable<String,CostCalculation> costCalculationHash  = agentHash.get(p_sh.getAgent());
		if(costCalculationHash == null) {
			costCalculationHash = new Hashtable<String,CostCalculation>();
			agentHash.put(p_sh.getAgent(),costCalculationHash);
		}

		synchronized(costCalculationHash) {
			CostCalculation cl = costCalculationHash.get(getCostKey(p_irg,p_org));
			if(cl == null) {
//				if(!getCostAgent(p_sh,p_org).equals("local") && !getCostAgent(p_sh,p_org).equals("specific") ) {
				if(!costAgent.equals("local") && !costAgent.equals("specific") ) {
					/*
					RpcClient rpc = rpc = new RpcClient(costHost,costPort);
					rpc.open();
					Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.erpv4GetStockAverageCost", 
									new VectorUtil()
									.addElement("clerptst")
									.addElement(p_irg)
									.addElement(p_org)
									.toVector()
								);
					rpc.close();
					*/
					RpcClient rpc = cacheGetRpcClient(p_org);
					if(rpc == null) return(Double.NaN); 
					try {
						Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.erpv4GetStockAverageCost", 
									new VectorUtil()
									.addElement("clerptst")
									.addElement(p_irg)
									.addElement(p_org)
									.toVector()
								);
						if(v == null) {
							resetRemoteRpc();
							return(Double.NaN); 
						}
						String ss = v.toString();
						if(!ss.startsWith("OK")) return(Double.NaN);
						cl = new CostCalculation(DateUtil.maxDate,useNewCosting(p_sh),useFifoCosting(p_sh));
						cl.fromJson(ss.substring(4));
						cl.setDebug(debug);
						costCalculationHash.put(getCostKey(p_irg,p_org),cl);
					} catch (Exception ex) {
						resetRemoteRpc();
						UniLog.log(ex);
						return(Double.NaN); 
					}
				} else cl = loadCostCalculation(costCalculationHash,p_irg,p_org,p_sh);
			}
			Date dd = p_date;
			if(!DateUtil.maxDate.after(dd)) {
				dd = DateUtil.prevday(dd);
			}
			return(cl.getAverageCost(dd));
		}
	}
	
	/*
	String avCostsToJson() throws Exception {
		JSONObject costCache = new JSONObject();
		JSONArray costList = new JSONArray();
		JSONArray dateList = new JSONArray();
		costCache.put("dates", dateList);
		costCache.put("costs", costList);
		int i=0;
		for(;i<dataSet.size();i++) {
			DatedValue dv = dataSet.get(i);
			Date dd = (Date) dv.valueDate;
			if(!dd.before(DateUtil.maxDate)) break;
			double avCost = getAverageCost(dd);
			if(Double.isNaN(avCost)) break;
			costList.put(avCost);
			dateList.put(DateUtil.toDateString(dd, "yyyy/mm/dd"));
		}
		costCache.put("count", i);
		return(costCache.toString());
	}
	*/
	static RpcClient cacheGetRpcClient(int p_org) {
		long now = new Date().getTime();
		if(remoteRpc != null) {
			if(now - lastCall > maxRemoteIdle) {
				remoteRpc.close();
				remoteRpc=null;
			}
		}
		if(remoteRpc == null) {
			if(now - lastConnect < maxRemoteDelay) {
				return(null);
			}
			lastConnect = now;
			try {
				//remoteRpc = new RpcClient(costHost,costPort);
//				remoteRpc = new RpcClient(costHost,costPort,2000); //andrew210601 add connectTimeout
				remoteRpc = new RpcClient(getCostHost(p_org),getCostPort(p_org),2000); //andrew210601 add connectTimeout
				remoteRpc.open();
				if(!remoteRpc.isConnected()) {
					remoteRpc = null;
					return(null);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				remoteRpc = null;
				lastConnect = now;
			}
		}
		lastCall = now;
		return(remoteRpc);
	}
	
	static public String getErpStockStatus(SessionHelper p_sh,int p_irg,int p_org) throws Exception {
		SelectUtil su = new SelectUtil();
		su.init(p_sh.getBiSchema().getConn());
		TableRec tr;
		tr = su.getQueryResult("select * from stock_gen where stg_irg = " + p_irg);
		su.close();
		JSONObject jo = new JSONObject();
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			jo.put("freestock", tr.getFieldDouble("stg_freestock"));
			jo.put("onorder", tr.getFieldDouble("stg_onorder") - tr.getFieldDouble("stg_allocated"));
			return(jo.toString());
		} else {
			jo.put("freestock", 0.0);
			jo.put("onorder", 0.0);
			return(jo.toString());
		}
	}
	
	static String getCostAgent(SessionHelper p_sh,int p_org) {
		if(costAgent == null) {
			String ss = Erpv4Config.getString(p_sh, "CostingAgent");
			if(ss == null || ss.equals("")) {
				costAgent ="local";
			} else if(ss.equals("none")) {
				costAgent ="none";
			} else if(ss.equals("specific")) {
				costAgent ="specific";
			} else {
				int cc = ss.indexOf("@");
				if(cc >= 0) {
					costAgent = ss.substring(0,cc);
					int c2 = ss.indexOf(":");
					if( c2 >= 0) {
						costHost = ss.substring(cc+1,c2);
						costPort = Integer.parseInt(ss.substring(c2+1));
					}
				}
			}
		} 
		if(costAgent.equals("none")) disabled = true;
		return(costAgent);
	}
	static String getCostHost(int p_org) {
		return(costHost);
	}
	static int getCostPort(int p_org) {
		return(costPort);
	}
	
	static public boolean useNewCosting(SessionHelper p_sh) {
		if(useNewCosting == null)  {
			String s = Erpv4Config.getString(p_sh, "USEOLDCOSTING");
			useNewCosting = !("Y".equals(s));
		}
		return(useNewCosting);
	}
	static public boolean useFifoCosting(SessionHelper p_sh) {
		return(false);
	}
	
	static public boolean isCostLoc(SessionHelper p_sh,String p_loc,Date p_date,int p_org) {
		if(costLocDate == null) {
			String ss = Erpv4Config.getString(p_sh, "CostLocDate");
			if(ss == null) costLocDate = DateUtil.maxDate; else {
				costLocDate = DateUtil.getDate(ss);
				costLoc =  new HashMap<Integer,HashSet<String>>();
			}
		}
		if(!costLocDate.equals(DateUtil.maxDate)) {
			if(p_date.before(costLocDate)) return(true);
			HashSet<String> locs;
			if(!costLoc.containsKey(p_org)) {
				locs = null;
				try { 
					SelectUtil su = new SelectUtil();
					su.init(p_sh.getBiSchema().getConn());
					TableRec tr;
					tr = su.getQueryResult("select * from cocode where co_wtavorg = " + p_org);
					su.close();
					if(tr.getRecordCount() == 1) {
						tr.setRecPointer(0);
						Set<String> lss = Erpv4Config.getLocationListByCompany(p_sh,tr.getFieldString("co_cocode"),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_COMPANY_EXCLUDE_TRANSIT);
						locs = new HashSet<String>();
						for(String loc : lss) {
							locs.add(loc);
						}
					}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				costLoc.put(p_org, locs);
			}
			locs = costLoc.get(p_org);
			if(locs == null) return(true);
			return(locs.contains(p_loc));
		}
		return(true);
	}
}

