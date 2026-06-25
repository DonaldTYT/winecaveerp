package com.kyoko.common;

import java.io.*;
import java.util.*;
import java.text.*;

public class TextUtil {
   public static String intToHEX(int p_i) {
	   String str = intToHex(p_i);
		return(str.toUpperCase());
	}
   public static String intToHex(int p_i) {
	   StringBuffer sb = new StringBuffer();
		int i = p_i;
		if (p_i < 0)
		   i = -i;
	   for (;;) {
		   int d = i & 0xf;
		   switch (d) {
			   case 0: sb.insert(0, "0"); break;
			   case 1: sb.insert(0, "1"); break;
			   case 2: sb.insert(0, "2"); break;
			   case 3: sb.insert(0, "3"); break;
			   case 4: sb.insert(0, "4"); break;
			   case 5: sb.insert(0, "5"); break;
			   case 6: sb.insert(0, "6"); break;
			   case 7: sb.insert(0, "7"); break;
			   case 8: sb.insert(0, "8"); break;
			   case 9: sb.insert(0, "9"); break;
			   case 10: sb.insert(0, "a"); break;
			   case 11: sb.insert(0, "b"); break;
			   case 12: sb.insert(0, "c"); break;
			   case 13: sb.insert(0, "d"); break;
			   case 14: sb.insert(0, "e"); break;
			   case 15: sb.insert(0, "f"); break;
			}
		   i = i>>4;
			if (i == 0)
			   break;
		}
		if (p_i < 0)
			sb.insert(0, "-");
	   return(sb.toString());
	}
	public static void main(String[] args) {
	   System.out.println(intToHex(Integer.parseInt(args[0])));
	}
}
