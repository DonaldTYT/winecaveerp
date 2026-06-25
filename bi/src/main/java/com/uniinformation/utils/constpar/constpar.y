%{
  import java.io.*;
  import java.util.*;
  import com.uniinformation.utils.*;
%}
      
%token <sval> STRING 
%type  <obj> expr
%token GT GE EQ NE LT LE COLON 
%left  OR AND

%%
start : expr { 
           wherecl = (Wherecl) $1; 
        }
      ;
expr : STRING { 
          $$ = new Wherecl();
          if ($1.trim().equals("")) {
          }
          else {
             if (fwild) {
					 if (flike) {
                   ((Wherecl) $$) 
						    .appendString(new StringBuffer()
                                        .append(fdname)
                                        .append(" like ? ")
                                        .toString())
					       .appendArgument(new StringBuffer()
							                    .append($1.startsWith("^") 
													          ? StringUtil.strpart($1, 1, -1)
																 : "%"+$1)
													  .append("%")
													  .toString());
					 }
					 else {
						 /*
                   ((Wherecl) $$) 
						    .appendString(new StringBuffer()
                                        .append(fdname)
                                        .append(" matches ? ")
                                        .toString())
					       .appendArgument(new StringBuffer()
							                    .append($1.startsWith("^") 
													          ? StringUtil.strpart($1, 1, -1)
																 : ".*"+$1)
													  .append(".*")
													  .toString());
					    */
                   ((Wherecl) $$) 
						    .appendString(new StringBuffer()
                                        .append(fdname)
                                        .append(" matches ")
                                        .append(StringUtil.encodeInQuote(
													            new StringBuffer() 
                                                       .append($1.startsWith("^") 
	                                                            ? StringUtil.strpart($1, 1, -1) 
				                                                   : ".*"+$1) 
	                                                    .append(".*") 
									                            .toString()))
                                        .toString());
				    }
             }
             else if (fDateToTime) {
                ((Wherecl) $$).andRange(fdname, 
                   DateUtil.getDate($1.trim(), dateFormat).getTime()/1000,
                   DateUtil.getDate($1.trim(), dateFormat).getTime()/1000+86400-1);
             }
             else if (fTime) {
                if (checkHourString($1) == 0) {
                   ((Wherecl) $$).andRange(fdname, 
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($1.trim())*3600,
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($1.trim())*3600+3600-1);
				    }
             }
             else {
                $$ = constructUniop("=", $1);
             }
          }
       }
     | STRING COLON STRING { 
          $$ = new Wherecl();
          try {
             if (fDateToTime) {
                ((Wherecl) $$).andRange(fdname, 
                   DateUtil.getDate($1.trim(), dateFormat).getTime()/1000,
                   DateUtil.getDate($3.trim(), dateFormat).getTime()/1000+86400-1);
             }
             else if (fTime) {
                 if (checkHourString($1) == 0 && checkHourString($3) == 0) {
                   ((Wherecl) $$).andRange(fdname, 
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($1.trim())*3600,
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($3.trim())*3600+3600-1);
				    }
             }
             else {
                switch (fdtype) {
                   case TableBrowser.SQLTYPE_INTEGER:
                      ((Wherecl) $$).andRange(fdname, Integer.parseInt($1.trim()), Integer.parseInt($3.trim()));
                      break;
                   case TableBrowser.SQLTYPE_CHAR:
                      ((Wherecl) $$).andRange(fdname, $1, $3);
                      break;
                   case TableBrowser.SQLTYPE_FLOAT:
                      ((Wherecl) $$).andRange(fdname, Double.parseDouble($1.trim()), Double.parseDouble($3.trim()));
                      break;
                   case TableBrowser.SQLTYPE_DATE:
							 if (dateFormat == null)
                         ((Wherecl) $$).andRange(fdname, DateUtil.getDate($1.trim(), dateFormat), DateUtil.getDate($3.trim()));
							 else
                         ((Wherecl) $$).andRange(fdname, DateUtil.getDate($1.trim(), dateFormat), DateUtil.getDate($3.trim(), dateFormat));
                      break;
                   default: 
                      throw(new Exception("Invalid field type "+fdtype));
                }
             }
          } catch (Exception ex) {
             UniLog.log(ex);
             $$ = null;
          }
       }
     | GT STRING { 
          if (fDateToTime)
             $$ = constructUniop(">", ""+(DateUtil.getDate($2.trim(), dateFormat).getTime()/1000+86400-1));
          else if (fTime) {
             if (checkHourString($2) == 0)
                $$ = constructUniop(">", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($2.trim())*3600+3600-1));
			 }
          else
             $$ = constructUniop(">", $2);
       }
     | GE STRING { 
          if (fDateToTime)
             $$ = constructUniop(">=", ""+(DateUtil.getDate($2.trim(), dateFormat).getTime()/1000));
          else if (fTime) {
             if (checkHourString($2) == 0)
                $$ = constructUniop(">=", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($2.trim())*3600));
			 }
          else
             $$ = constructUniop(">=", $2);
       }
     | LT STRING {
          if (fDateToTime)
             $$ = constructUniop("<", ""+(DateUtil.getDate($2.trim(), dateFormat).getTime()/1000));
          else if (fTime) {
             if (checkHourString($2) == 0)
                $$ = constructUniop("<", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($2.trim())*3600));
			 }
          else
             $$ = constructUniop("<", $2);
       }
     | LE STRING {
          if (fDateToTime)
             $$ = constructUniop("<=", ""+(DateUtil.getDate($2.trim(), dateFormat).getTime()/1000+86400-1));
          else if (fTime) {
             if (checkHourString($2) == 0)
                $$ = constructUniop("<=", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($2.trim())*3600+3600-1));
			 }
          else
             $$ = constructUniop("<=", $2);
       }
     | EQ STRING {
          if (fDateToTime) {
             $$ = new Wherecl();
             ((Wherecl) $$).andRange(fdname, 
                   DateUtil.getDate($2.trim(), dateFormat).getTime()/1000,
                   DateUtil.getDate($2.trim(), dateFormat).getTime()/1000+86400-1);
          }
          else if (fTime) {
             if (checkHourString($2) == 0) {
                $$ = new Wherecl();
                ((Wherecl) $$).andRange(fdname, 
                      DateUtil.getDate($2.trim(), dateFormat).getTime()/1000,
                      DateUtil.getDate($2.trim(), dateFormat).getTime()/1000+86400-1);
			    }
          }
          else
             $$ = constructUniop("=", $2);
       }
     | NE STRING {
          if (fDateToTime) {
             $$ = new Wherecl();
             ((Wherecl) $$).andUniop(fdname, "<", DateUtil.getDate($2.trim(), dateFormat).getTime()/1000);
             Wherecl wc = new Wherecl();
             wc.andUniop(fdname, ">", DateUtil.getDate($2.trim(), dateFormat).getTime()/1000+86400-1);
             ((Wherecl) $$).orWherecl(wc);
          }
          else if (fTime) {
             if (checkHourString($2) == 0) {
                $$ = new Wherecl();
                ((Wherecl) $$).andUniop(fdname, "<", DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($2.trim())*3600);
                Wherecl wc = new Wherecl();
                wc.andUniop(fdname, ">", DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt($2.trim())*3600+3600-1);
                ((Wherecl) $$).orWherecl(wc);
				 }
          }
          else
             $$ = constructUniop("<>", $2);
       }
     | expr OR expr { 
          $$ = ((Wherecl) $1).orWherecl((Wherecl) $3);
       }
     | expr AND expr { 
          $$ = ((Wherecl) $1).andWherecl((Wherecl) $3);
       }
     ;

%%
   private Wherecl wherecl;
   private boolean fwild;
   private String fdname;
   private int fdtype;
   private String errmsg;
   private boolean fDateToTime;
   private boolean fTime;
   private java.util.Date qdate;
   private boolean flike;
   private Yylex lexer;
	private String dateFormat;

   public String getErrmsg() {
      return(errmsg);
   }
   public Wherecl getWherecl() {
      return(wherecl);
   }
   private int checkHourString(String p_hourString) {
      try {
         int hour = Integer.parseInt(p_hourString);
         if (hour < 0 || hour > 23) {
            errmsg = "Please enter 00-23 for hour search";
            return(-1);
         }
      } catch (Exception ex) {
         errmsg = "Please enter 00-23 for hour search";
         return(-1);
      }
      return(0);
   }
   private void init(boolean p_fwild, 
                     String p_fdname, 
                     int p_fdtype, 
                     boolean p_fDateToTime,
                     boolean p_fTime,
                     java.util.Date p_date,
							boolean p_flike, 
							String p_dateFormat) {
      wherecl = null;
      fwild = p_fwild;
      fdname = p_fdname;
      fdtype = p_fdtype;
      errmsg = null;
      fDateToTime = p_fDateToTime;
      fTime = p_fTime;
      qdate = p_date;
      flike = p_flike;
		dateFormat = p_dateFormat;
   }
   private int yylex() {
      int yyl_return = -1;
      try {
        yylval = new ParserVal(0);
        yyl_return = lexer.yylex();
      }
      catch (IOException e) {
        UniLog.log(e);
      }
      return yyl_return;
   }
   public void yyerror(String error) {
      UniLog.logClass(this, "Error:"+error);
   }
   public Parser(Reader r) {
      lexer = new Yylex(r, this);
   }
   private static Wherecl cwhere_gen(
                             String p_str, 
                             String p_fdname, 
                             int p_fdtype, 
                             boolean p_fwild, 
                             boolean p_fDateToTime, 
                             boolean p_fTime,
                             java.util.Date p_date,
									  boolean p_flike,
									  String p_dateFormat) throws Exception {
      Parser yyparser;
      yyparser = new Parser(new StringReader(p_str));
      yyparser.init(p_fwild, 
                    p_fdname, 
                    p_fdtype, 
                    p_fDateToTime, 
                    p_fTime, 
                    p_date,
						  p_flike,
						  p_dateFormat);
      yyparser.yyparse();
      if (yyparser.getErrmsg() == null)
         return(yyparser.getWherecl());
      else
         throw(new Exception(yyparser.getErrmsg()));
   }
   public static Wherecl cwhereDateToTime(String p_str, String p_fdname) throws Exception {
      return(cwhere_gen(p_str, p_fdname, TableBrowser.SQLTYPE_INTEGER, false, true, false, null, false, null));
   }
   public static Wherecl cwhereTime(String p_str, String p_fdname, java.util.Date p_date) throws Exception {
      return(cwhere_gen(p_str, p_fdname, TableBrowser.SQLTYPE_INTEGER, false, false, true, p_date, false, null));
   }
   public static Wherecl cwhere(String p_str, String p_fdname, int p_fdtype, String p_dateFormat) throws Exception {
      return(cwhere_gen(p_str, p_fdname, p_fdtype, false, false, false, null, false, p_dateFormat));
   }
   public static Wherecl cwhere(String p_str, String p_fdname, int p_fdtype) throws Exception {
      return(cwhere_gen(p_str, p_fdname, p_fdtype, false, false, false, null, false, null));
   }
   public static Wherecl cwherewild(String p_str, String p_fdname, int p_fdtype) throws Exception {
      return(cwhere_gen(p_str, p_fdname, p_fdtype, true, false, false, null, false, null));
   }
   public static Wherecl cwherewildlike(String p_str, String p_fdname, int p_fdtype) throws Exception {
      return(cwhere_gen(p_str, p_fdname, p_fdtype, true, false, false, null, true, null));
   }
   private Wherecl constructUniop(String p_operator, String p_string) {
      Wherecl tmpwhere = new Wherecl();
      try {
         if (fDateToTime) {
            tmpwhere.andUniop(fdname, p_operator, Integer.parseInt(p_string.trim()));
         }
         else {
            switch (fdtype) {
               case TableBrowser.SQLTYPE_INTEGER:
                  tmpwhere.andUniop(fdname, p_operator, Integer.parseInt(p_string.trim()));
                  break;
               case TableBrowser.SQLTYPE_CHAR:
                  tmpwhere.andUniop(fdname, p_operator, p_string);
                  break;
               case TableBrowser.SQLTYPE_FLOAT:
                  tmpwhere.andUniop(fdname, p_operator, Double.parseDouble(p_string.trim()));
                  break;
               case TableBrowser.SQLTYPE_DATE:
                  tmpwhere.andUniop(fdname, p_operator, DateUtil.getDate(p_string.trim(), dateFormat));
                  break;
               default: 
                  throw(new Exception("Invalid field type "+fdtype));
            }
         }
      } catch (Exception ex) {
         UniLog.log(ex);
         tmpwhere = null;
      }
      return(tmpwhere);
   }
   public static void main(String args[]) throws Exception {
      UniLog.log(""+cwhere(args[1], args[0], 0));
   }

  /*
  static boolean interactive;
  public static void main(String args[]) throws IOException {
    System.out.println("BYACC/Java with JFlex Calculator Demo");

    Parser yyparser;
    if ( args.length > 0 ) {
      // parse a file
      yyparser = new Parser(new FileReader(args[0]));
    }
    else {
      // interactive mode
      System.out.println("[Quit with CTRL-D]");
      System.out.print("Expression: ");
      interactive = true;
       yyparser = new Parser(new InputStreamReader(System.in));
    }

    yyparser.yyparse();
    
    if (interactive) {
      System.out.println();
      System.out.println("Have a nice day");
    }
  }
  */
