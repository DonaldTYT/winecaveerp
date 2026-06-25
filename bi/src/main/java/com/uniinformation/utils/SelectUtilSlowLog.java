package com.uniinformation.utils;

import java.util.*;
import java.io.*;

public class SelectUtilSlowLog {
   static SelectUtilSlowLog instance = new SelectUtilSlowLog();
   private SelectUtilSlowLog() {
	}
   static SelectUtilSlowLog getInstance() {
	   return(instance);
	}
}
