package com.kikyosoft.utils;
import java.io.*;
import java.util.*;
import java.text.*;

public class ChineseConvert {
   public static boolean fDebug = false;
   public static String SIMPLIFIED_ENCODING = "GBK";
   // public static String SIMPLIFIED_ENCODING = "GB2312";
   // public static String TRADITIONAL_ENCODING = "EUC_TW";
   // public static String TRADITIONAL_ENCODING = "Big5";
//   public static String TRADITIONAL_ENCODING = "MS950";
   public static String TRADITIONAL_ENCODING = "MS950_HKSCS";
//  static final String G2BIDX_REF = "/usr/unc/font/g2bidx.ref";
//  static final String B2GIDX_REF = "/usr/unc/font/b2gidx.ref";
   static String DEFAULT_REF_PATH = "/usr/unc/font";
   static final String G2BIDX_REF = "g2bidx.ref";
   static final String B2GIDX_REF = "b2gidx.ref";
   static Object lockObj = new Object();
   static int[] g2bref = null;
   static int[] b2gref = null;
   static int g2bindex(int p_idx) {
	   if (g2bref == null) {
		   synchronized(lockObj) {
			   if (g2bref == null) {
		         Map env = System.getenv();
	            if (env.get("APP_BASEDIR") != null && !env.get("APP_BASEDIR").toString().trim().equals("")) {
	               String path = env.get("APP_BASEDIR").toString().trim()+"/config/"+G2BIDX_REF;
					   if (new File(path).exists()) {
				         g2bref = loadref(path, 7614);
					   }
		         }
			      if (g2bref == null) {
					   String path = DEFAULT_REF_PATH+"/"+G2BIDX_REF;
				      g2bref = loadref(path, 7614);
		 			}
				}
			}
		}
		if (g2bref == null)
		   return(-1);
	   if (p_idx < 0 || p_idx >= 7614) 
		   return(-1);
	   return(g2bref[p_idx]);
	}
   static int b2gindex(int p_idx) {
	   if (b2gref == null) {
		   synchronized(lockObj) {
			   if (b2gref == null) {
		         Map env = System.getenv();
	            if (env.get("APP_BASEDIR") != null && !env.get("APP_BASEDIR").toString().trim().equals("")) {
	               String path = env.get("APP_BASEDIR").toString().trim()+"/config/"+B2GIDX_REF;
					   if (new File(path).exists()) {
				         b2gref = loadref(path, 13565);
					   }
		         }
			      if (b2gref == null) {
					   String path = DEFAULT_REF_PATH+"/"+B2GIDX_REF;
				      b2gref = loadref(path, 13565);
					}
				}
			}
		}
		if (b2gref == null)
		   return(-1);
	   if (p_idx < 0 || p_idx >= 13565) 
		   return(-1);
	   return(b2gref[p_idx]);
	}
	static int[] loadref(String p_fname, int p_length) {
		try {
		   int[] bmptr = new int[p_length];

		   LineNumberReader input;
		   try {
		      input = new LineNumberReader(new FileReader(p_fname));
		   }
		   catch(java.io.FileNotFoundException fex) {
		      int cc = p_fname.lastIndexOf('/');
			  if(cc < 0) {
			  	throw(fex);
			  }
		      input = new LineNumberReader(new FileReader(p_fname.substring(cc+1)));
		   }
			for (int i=0; ; i++) {
				try {
			      String line = input.readLine();
			      if(line == null) break;
				   bmptr[i] = Integer.parseInt(line.trim());
				} catch (Exception ex) {
				   break;
				}
			}
			input.close();
		   return(bmptr);
		} catch (Exception ex) {
		   LogUtil.log(ex);
			return(null);
		}
	}
   static int gbtoidx(int ch1, int ch2) {
	   int idx;
      if (ch1 > 0xfe || ch1 < 0xa1) 
		    return(-1);
	   idx = (ch1 - 0xa1) * 94;
      if (ch2 >= 0xa1 && ch2 <= 0xff) {
		   idx += ch2 - 0xa1;
		   if (idx >= 1410) 
			   idx -= 564;
		   return(idx);
	   }
	   return(-1);
   }
   static int big5toidx(int ch1, int ch2) {
	   int idx;
      int hkscs_octet;
   
   	hkscs_octet = (ch1 << 8) + ch2;
   	//User-Defined Area 3 13565-16827
      if (hkscs_octet >= 0x8140 && hkscs_octet <= 0x8dfe){
         return(((ch1 << 8) + ch2) - 0x8140 + 13565);
   	}
   	//User-Defined Area 2 16828-21626
   	if (hkscs_octet >= 0x8e40 && hkscs_octet <= 0xa0fe){
         return(((ch1 << 8) + ch2) - 0x8e40 + 16828);
   	}
   	//Vendor-Defined Area 2 21627-22232
   	if (hkscs_octet >= 0xc6a1 && hkscs_octet <= 0xc8fe){
         return(((ch1 << 8) + ch2) - 0xc6a1 + 21627);
   	}
   	//User-Defined Area 1 22233-23447
   	if (hkscs_octet >= 0xfa40 && hkscs_octet <= 0xfefe){
         return(((ch1 << 8) + ch2) - 0xfa40 + 22233);
   	}

	   if (ch1 > 0xfe || ch1 < 0xa1) 
		   return(-1);
	   idx = (ch1 - 0xa1) * 157;
	   if(ch2 >= 0x40 && ch2 <= 0x7e) {
		   idx += ch2 - 0x40;
		   if (idx >= 5872 && idx < 6280) 
			   return(-1);
		   if (idx >= 6280) 
			   idx -= 408;
		   return(idx);
	   }
	   if (ch2 >= 0xa1 && ch2 <= 0xfe) {
		   idx += ch2 - 0xa1 + 63;
		   if (idx >= 5872 && idx < 6280) 
			   return(-1);
		   if (idx >= 6280) 
			   idx -= 408;
		   return(idx);
	   }
	   return(-1);
   }
   static int idxtobig5(int idx, byte[] ch) {
      int hkscs_octet;
   	if(idx >= 13565){
      	if(idx >= 13565 && idx <= 16827){
      	   hkscs_octet = idx - 13565 + 0x8140;
      	   ch[0] = (byte) (hkscs_octet >> 8);
      		ch[1] = (byte) (hkscs_octet - (hkscs_octet >> 8 << 8));
      		return(0);
      	}
      	if(idx >= 16828 && idx <= 21626){
      	   hkscs_octet = idx - 16828 + 0x8e40;
      	   ch[0] = (byte) (hkscs_octet >> 8);
      		ch[1] = (byte) (hkscs_octet - (hkscs_octet >> 8 << 8));
      		return(0);
      	}
      	if(idx >= 21627 && idx <= 22232){
      	   hkscs_octet = idx - 21627 + 0xc6a1;
      	   ch[0] = (byte) (hkscs_octet >> 8);
      		ch[1] = (byte) (hkscs_octet - (hkscs_octet >> 8 << 8));
      		return(0);
      	}
      	if(idx >= 22233 && idx <= 23447){
      	   hkscs_octet = idx - 22233 + 0xfa40;
      	   ch[0] = (byte) (hkscs_octet >> 8);
      		ch[1] = (byte) (hkscs_octet - (hkscs_octet >> 8 << 8));
      		return(0);
      	}
   	   return(-1);
   	}
	   if (idx >= 13565) 
		   return(-1);
	   if (idx >= 5872) 
		   idx += 408;
	   ch[0] = (byte) (idx / 157 + 0xa1);
	   idx %= 157;
	   if(idx >= 63) {
		   ch[1] = (byte) ((idx - 63) + 0xa1);
	   } else {
		   ch[1] = (byte) ((idx + 0x40));
	   }
	   return(0);
   }
   static int idxtogb(int idx, byte[] ch) {
	   if (idx >= 7614) 
		   return(-1);
	   if (idx >= 846) 
		   idx+= 564;
	   ch[0] = (byte) (idx / 94  + 0xa1);
	   ch[1] = (byte) ((idx % 94) + 0xa1);
	   return(0);
   }
	public static String convertAuto2BSquare(String p_inString) {
	   return(ChineseConvert.convertG2B(
		          ChineseConvert.convertB2G(
			          convertAuto2B(p_inString))));
	}
	public static String convertAuto2B(String p_inString) {
	   if (p_inString == null)
		   return("");
		StringBuffer sb = new StringBuffer();
		char[] ca = p_inString.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if (ca[i] < 256) {
			   sb.append(ca[i]);
			}
			else {
				try {
		         byte[] ba = new StringBuffer().append(ca[i]).toString().getBytes(TRADITIONAL_ENCODING);
			      if (ba.length == 1 && ba[0] == '?') {
		            ba = (""+ca[i]).getBytes(SIMPLIFIED_ENCODING);
			         if (ba.length == 1 && ba[0] == '?') {
					      // sb.append('?');
					      sb.append(ca[i]);
						} else {
	                  sb.append(ChineseConvert.convertG2B(""+ca[i]));
						}
		         }
				   else {
		            byte[] ba2 = (""+ca[i]).getBytes(SIMPLIFIED_ENCODING);
			         if (ba2.length == 1 && ba2[0] == '?') {
			            sb.append(ca[i]);
					   } else {
	                  String str0 = ChineseConvert.convertG2B(""+ca[i]);
							// is it a square??
							if (str0.charAt(0) == 9633 || str0.charAt(0) == 65533) {
							   sb.append(ca[i]);
							}
							else {
	                     sb.append(str0);
						   }
					   }
				   }
			   } catch (Exception ex) {
				   // sb.append('?');
				   sb.append(ca[i]);
				}
			}
		}
		return(sb.toString());
	}
	public static String convertAuto2G(String p_inString) {
	   if (p_inString == null)
		   return("");
		StringBuffer sb = new StringBuffer();
		char[] ca = p_inString.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if (ca[i] < 256) {
			   sb.append(ca[i]);
			}
			else {
				try {
		         byte[] ba = new StringBuffer().append(ca[i]).toString().getBytes(SIMPLIFIED_ENCODING);
			      if (ba.length == 1 && ba[0] == '?') {
		            ba = (""+ca[i]).getBytes(TRADITIONAL_ENCODING);
			         if (ba.length == 1 && ba[0] == '?') {
					      sb.append(ca[i]);
					   } else {
                     String str0 = ChineseConvert.convertB2G(""+ca[i]);
							// is it a square??
							if (str0.charAt(0) == 9633 || str0.charAt(0) == 65533) {
							   sb.append(ca[i]);
							}
							else {
	                     sb.append(str0);
						   }
						}
		         }
				   else {
		            byte[] ba2 = (""+ca[i]).getBytes(TRADITIONAL_ENCODING);
			         if (ba2.length == 1 && ba2[0] == '?') {
			            sb.append(ca[i]);
					 } else {
			            sb.append(ca[i]);
					 }
				   }
			   } catch (Exception ex) {
					sb.append(ca[i]);
				}
			}
		}
		return(sb.toString());
	}
	public static String convertB2G(String p_inString) {
	   if (p_inString == null)
		   return("");
		try {
	      int cc, c2, idx;
	      byte[] ch = new byte[2];
		   if (p_inString == null)
		      return(null);
	      byte[] bytes = p_inString.getBytes(TRADITIONAL_ENCODING);  /* microsoft compatiable */
		   for (int i=0; i<bytes.length; ) {
		      cc = bytes[i++];
			   if (cc < 0) {
			      cc += 256;
            }
//CoreLog.log("b2g:cc="+cc);
		      if (cc < 128) {
		      }
		      else {
		         c2 = bytes[i++];
			      if (c2 < 0)
			         c2 += 256;
			      int idx2 = big5toidx(cc, c2);
			      idx = b2gindex(idx2);
					if(idx == 84) {
			      	idxtogb(idx,ch);
					   bytes[i-2] = ch[0];
					   bytes[i-1] = ch[1];
					} else {
						int idx3 = g2bindex(idx);
						if(idx3 != idx2) {
							byte[] bch = new byte[2];
							bch[0] = (byte) cc;
							bch[1] = (byte) c2;
//							String s = new String(bch,"Big5");
//							String s = new String(bch,"MS950");
							String s = new String(bch,"MS950_HKSCS");
							byte[] tmpch = s.getBytes("GBK");
							if(tmpch .length == 1 && tmpch[0] == '?') {
				      		idxtogb(idx,ch);
							   bytes[i-2] = ch[0];
							   bytes[i-1] = ch[1];
							} else {
						   	bytes[i-2] = tmpch[0];
						   	bytes[i-1] = tmpch[1];
							}
						} else {
				      	idxtogb(idx,ch);
						   bytes[i-2] = ch[0];
						   bytes[i-1] = ch[1];
						}
					}
		      }
	      }
		   return(new String(bytes, SIMPLIFIED_ENCODING)); /* microsoft compatiable */
		} catch (Exception ex) {
		   LogUtil.log(ex);
			return(p_inString);
		}
	}
	public static String convertG2B(String p_inString) {
	   if (p_inString == null)
		   return("");
		try {
	      int cc, c2, idx;
	      byte[] ch = new byte[2];
		   if (p_inString == null)
		      return(null);
	      byte[] bytes = p_inString.getBytes(SIMPLIFIED_ENCODING); /* microsoft compatiable */
		   for (int i=0; i<bytes.length; ) {
		      cc = bytes[i++];
			   if (cc < 0)
			      cc += 256;
		      if (cc < 128) {
		      }
		      else {
		         c2 = bytes[i++];
			      if (c2 < 0)
			         c2 += 256;
			      idx = gbtoidx(cc, c2);
					if(idx >= 0) {
			      	idx = g2bindex(idx);
					} 
					if(idx >= 0) {
				      idxtobig5(idx,ch);
					   bytes[i-2] = ch[0];
					   bytes[i-1] = ch[1];
					} else {
						LogUtil.log("HAHA");
						byte[] bch = new byte[2];
						bch[0] = (byte) cc;
						bch[1] = (byte) c2;
						String s = new String(bch,"GBK");
//						byte[] tmpch = s.getBytes("Big5");
//						byte[] tmpch = s.getBytes("MS950");
						byte[] tmpch = s.getBytes("MS950_HKSCS");
						if(tmpch .length == 1 && tmpch[0] == '?') {
							LogUtil.log(new Exception("ConvertG2B failed"));
				      	idxtobig5(idx,ch);
						   bytes[i-2] = ch[0];
						   bytes[i-1] = ch[1];
						} else {
					   	bytes[i-2] = tmpch[0];
					   	bytes[i-1] = tmpch[1];
						}
					}
		      }
	      }
			/* microsoft compatiable */
		   return(new String(bytes, TRADITIONAL_ENCODING)); /* microsoft compatiable */
		} catch (Exception ex) {
		   LogUtil.log(ex);
			return(p_inString);
		}
	}
	public static void convertFileB2G(String p_infile, String p_outfile) throws Exception {
	   int cc, c2, idx;
	   byte[] ch = new byte[2];
		FileInputStream infile = new FileInputStream(p_infile);
		FileOutputStream outfile = new FileOutputStream(p_outfile);
		for (;;) {
		   cc = infile.read();
			if (cc < 0) 
			   break;
		   if (cc < 128) {
				outfile.write(cc);
		   }
		   else {
		      c2 = infile.read();
			   if (c2 < 0) 
			      break;
			   idx = big5toidx(cc, c2);
			   idx = b2gindex(idx);
			   idxtogb(idx,ch);
			   outfile.write(ch);
		   }
	   }
		infile.close();
		outfile.close();
	}
	public static void convertFileG2B(String p_infile, String p_outfile) throws Exception {
	   int cc, c2, idx;
	   byte[] ch = new byte[2];
		FileInputStream infile = new FileInputStream(p_infile);
		FileOutputStream outfile = new FileOutputStream(p_outfile);
		for (;;) {
		   cc = infile.read();
			if (cc < 0)
			   break;
		   if (cc < 128) {
				outfile.write(cc);
		   }
		   else {
		      c2 = infile.read();
				if (c2 < 0)
				   break;
			   idx = gbtoidx(cc, c2);
			   idx = g2bindex(idx);
			   idxtobig5(idx,ch);
			   outfile.write(ch);
		   }
	   }
		infile.close();
		outfile.close();
	}
	public static int splitChineseEnglish(String p_str, StringBuffer p_engSb, StringBuffer p_chnSb) throws Exception {
	   int cc = 0;
		StringBuffer sb0 = new StringBuffer();
	   int state = 0;
		char[] ca = p_str.trim().toCharArray();
	   for (int i=0; i<ca.length; i++) {
		   switch (state) {
			   case 0: // init state
					if ((ca[i] >= 'a' && ca[i] <= 'z')
					    || (ca[i] >= 'A' && ca[i] <= 'Z')
					    || (ca[i] >= '0' && ca[i] <= '9')) {
						p_engSb.append(sb0.toString());
				      p_engSb.append(ca[i]);
					   state = 1;
					}
				   else if (ca[i] > 256) {
						p_chnSb.append(sb0.toString());
				      p_chnSb.append(ca[i]);
					   state = 2;
				   }
					else 
		            sb0.append(ca[i]);
					break;
			   case 1: // english first state
					if ((ca[i] >= 'a' && ca[i] <= 'z')
					    || (ca[i] >= 'A' && ca[i] <= 'Z')
					    || (ca[i] >= '0' && ca[i] <= '9')) {
				      p_engSb.append(ca[i]);
					}
				   else if (ca[i] > 256) {
				      p_chnSb.append(ca[i]);
					   state = 11;
				   }
					else {
				      p_engSb.append(ca[i]);
					}
				   break;
			   case 11:  // english first and chinese started state
				   p_chnSb.append(ca[i]);
					if ((ca[i] >= 'a' && ca[i] <= 'z')
					    || (ca[i] >= 'A' && ca[i] <= 'Z')
					    || (ca[i] >= '0' && ca[i] <= '9'))
					   cc = -1;
				   break;
				case 2: // chinese first state
					if ((ca[i] >= 'a' && ca[i] <= 'z')
					    || (ca[i] >= 'A' && ca[i] <= 'Z')
					    || (ca[i] >= '0' && ca[i] <= '9')) {
				      p_engSb.append(ca[i]);
						state = 21;
					}
				   else if (ca[i] > 256) {
				      p_chnSb.append(ca[i]);
				   }
					else {
				      p_chnSb.append(ca[i]);
					}
				   break;
				case 21: // chinese first and english started state
				   p_engSb.append(ca[i]);
				   if (ca[i] >= 256)
					   cc = -1;
					break;
			}
		}
		return(cc);
	}
	public static void makeB2GIdx(int p_idx0, int p_idx1) throws Exception{
		for (int i=p_idx0; i<p_idx1; i++){
		   byte[] big5Bytes = new byte[2];
	      int cc = idxtobig5(i,big5Bytes);
			if (cc != 0) 
			   throw new Exception("sth wrong");
			String str = new String(big5Bytes, "Big5");
			byte[] gbBytes = str.getBytes("gb2312");

		   int gbInt = 0;
			if (gbBytes.length == 1){
			   gbInt = gbtoidx(gbBytes[0] & 0xFF, 0);
			}
			else if (gbBytes.length == 2){
			   gbInt = gbtoidx(gbBytes[0] & 0xFF, gbBytes[1] & 0xFF);
			}
			LogUtil.log("idx:" + i + " " + str + " gbInt:" +gbInt);
			//debug
			for (int j=0; j<big5Bytes.length; j++){ 
			   LogUtil.log("big5Bytes[" + j+"]:  " + String.format("(%x)",big5Bytes[j]));
			}
			for (int j=0; j<gbBytes.length; j++){ 
			   LogUtil.log("gbBytes[" + j+"]:  " + String.format("(%x)",gbBytes[j]));
			}

   	   

		}

	   
	}
	static public void main_xxx(String[] args) throws Exception {
		//ChineseConvert.makeB2GIdx(0,23447);
		ChineseConvert.makeB2GIdx(0,23447);
		/*
	   if (args[0].equals("b2g")) {
		   convertFileB2G(args[1], args[2]);
		}
	   else if (args[0].equals("g2b")) {
		   convertFileG2B(args[1], args[2]);
		}
		else {
		   CoreLog.log("Usage: java com.uniinformation.utils.ChineseConvert {b2g|g2b} <input file> <output file>");
		}
		*/
		/*
      char ca[] = {36774,20844,23460,26085,26412,35486,12398,12506,12540,12472,12434,26908,32034};
      // char ca[] = {12398};
		String str = new String(ca);
	   CoreLog.log("str     ="+StringUtil.cws(str));
	   CoreLog.log("auto2b()="+StringUtil.cws(ChineseConvert.convertAuto2B(str)));
	   CoreLog.log("auto2g()="+StringUtil.cws(ChineseConvert.convertAuto2G(str)));
		*/
		/*
		CoreLog.setEncoding("Big5");
		for(int i = 0;i<7614;i++) {
				int j = g2bindex(i);
				CoreLog.log("" + i + " " + j);
		}
		*/
/*
if(false) {
		for(int i = 0;i<13565;i++) {
				int j,k;
				byte[] ch = new byte[2];
				byte[] ch2= new byte[2];
				byte[] ch3= new byte[2];
				String s,s2;
				j = b2gindex(i);
				if(j != 84) {
				k = g2bindex(j);
				if(i != k) {
			   	idxtobig5(i,ch);
			   	idxtobig5(k,ch2);
					s = new String(ch,"Big5");
					s2= new String(ch2,"Big5");
//					byte[] tmpch = s.getBytes("gb2312");
					byte[] tmpch = s.getBytes("GBK");
					if(tmpch .length == 1 && tmpch[0] == '?') {
						ch3[0] = 0;
						ch3[1] = 0;
					} else {
						ch3[0] = tmpch[0];
						ch3[1] = tmpch[1];
					}
//			      CoreLog.log("" + i + " " + new String(ch, "Big5") + " " + j + " " + k);
					
					CoreLog.log(new Sprintf("%5d %s %5d %s %3d %3d").
									add(i).
									add(s).
									add(k).
									add(s2).
									add(ch3[0]).
									add(ch3[1]).
									toString());
				}
				}
		}
}
		{
			char[] ca = {0x4e00};
			String s = new String(ca);
			byte[] ba = s.getBytes("BIG5");
			CoreLog.log("BIG5 "+ba[0]);
			CoreLog.log("BIG5 "+ba[1]);
		}
		{
			char[] ca = {0x4e00};
			String s = new String(ca);
			byte[] ba = s.getBytes("MS950");
			CoreLog.log("MS950 "+ba[0]);
			CoreLog.log("MS950 "+ba[1]);
		}
*/
	}
	public static String convertAuto2Gnew(String p_inString) {
	   if (p_inString == null)
		   return("");
		StringBuffer sb = new StringBuffer();
		char[] ca = p_inString.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if (ca[i] < 256) {
			   sb.append(ca[i]);
			}
			else {
				try {
		         byte[] ba = new StringBuffer().append(ca[i]).toString().getBytes(TRADITIONAL_ENCODING);
			      if (ba.length == 1 && ba[0] == '?') { //not traditional chinese , no need to convert
			    	  	  sb.append(ca[i]);
			      } else {
					      String str0 = ChineseConvert.convertB2G(""+ca[i]);
					      if (str0.charAt(0) == 9633 || str0.charAt(0) == 65533) { // special char , no need to convert
							  sb.append(ca[i]);
					      } else {
					    	  sb.append(str0);
						  }
			      }
			   } catch (Exception ex) {
					sb.append(ca[i]);
				}
			}
		}
		return(sb.toString());
	}
	public static String convertAuto2Bnew(String p_inString) {
	   if (p_inString == null)
		   return("");
		StringBuffer sb = new StringBuffer();
		char[] ca = p_inString.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if (ca[i] < 256) {
			   sb.append(ca[i]);
			}
			else {
				try {
					byte[] ba = new StringBuffer().append(ca[i]).toString().getBytes(SIMPLIFIED_ENCODING);
			        if (ba.length == 1 && ba[0] == '?') { // not simplified chinese, no need to convert
					    sb.append(ca[i]);
			        } else {
			        	int cc0 = ba[0];
			        	int cc1 = ba[1];
			        	if(cc0 < 0) cc0 += 256;
			        	if(cc1 < 0) cc1 += 256;
			        	if( gbtoidx(cc0, cc1) >= 0) {
			        		String str0 = ChineseConvert.convertG2B(""+ca[i]);
			        		if (str0.charAt(0) == 9633 || str0.charAt(0) == 65533) { // specical charactor, no need to convert
			        			sb.append(ca[i]);
			        		} else {
			        			sb.append(str0);
			        		}
			        	} else { // this gbcode is not in our big5 <-> gb cross reference table, skipped
						   sb.append(ca[i]);
			        	}
			        }
			   } catch (Exception ex) {
				   // sb.append('?');
				   sb.append(ca[i]);
				}
			}
		}
		return(sb.toString());
	}
	
	public static void setFontPath(String p_path) {
		DEFAULT_REF_PATH = p_path;
	}
}
