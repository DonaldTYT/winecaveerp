//### This file created by BYACC 1.8(/Java extension  1.1)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//### Please send bug reports to rjamison@lincom-asg.com
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";



package com.uniinformation.utils.constpar;



//#line 2 "constpar.y"
  import java.io.*;
  import java.util.*;

import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.utils.*;
//#line 19 "Parser.java"




/**
 * Encapsulates yacc() parser functionality in a Java
 *        class for quick code development
 */
public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[],stateptr;           //state stack
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
void state_push(int state)
{
  if (stateptr>=YYSTACKSIZE)         //overflowed?
    return;
  statestk[++stateptr]=state;
  if (stateptr>statemax)
    {
    statemax=state;
    stateptrmax=stateptr;
    }
}
int state_pop()
{
  if (stateptr<0)                    //underflowed?
    return -1;
  return statestk[stateptr--];
}
void state_drop(int cnt)
{
int ptr;
  ptr=stateptr-cnt;
  if (ptr<0)
    return;
  stateptr = ptr;
}
int state_peek(int relative)
{
int ptr;
  ptr=stateptr-relative;
  if (ptr<0)
    return -1;
  return statestk[ptr];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
boolean init_stacks()
{
  statestk = new int[YYSTACKSIZE];
  stateptr = -1;
  statemax = -1;
  stateptrmax = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal(0);
  yylval=new ParserVal(0);
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal(-1);
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal(-1);
  return valstk[ptr];
}
//#### end semantic value section ####
public final static short STRING=257;
public final static short GT=258;
public final static short GE=259;
public final static short EQ=260;
public final static short NE=261;
public final static short LT=262;
public final static short LE=263;
public final static short COLON=264;
public final static short OR=265;
public final static short AND=266;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,
};
final static short yylen[] = {                            2,
    1,    1,    3,    2,    2,    2,    2,    2,    2,    3,
    3,
};
final static short yydefred[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    4,    5,    8,    9,    6,    7,    0,    0,    3,   10,
   11,
};
final static short yydgoto[] = {                          8,
    9,
};
final static short yysindex[] = {                      -255,
 -264, -244, -243, -242, -241, -240, -239,    0, -256, -238,
    0,    0,    0,    0,    0,    0, -255, -255,    0,    0,
    0,
};
final static short yyrindex[] = {                         0,
    1,    0,    0,    0,    0,    0,    0,    0,   20,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,
};
final static short yygindex[] = {                         0,
   -6,
};
final static int YYTABLESIZE=267;
final static short yytable[] = {                         10,
    2,    1,    2,    3,    4,    5,    6,    7,   17,   18,
   20,   21,   11,   12,   13,   14,   15,   16,   19,    1,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    2,    2,
};
final static short yycheck[] = {                        264,
    0,  257,  258,  259,  260,  261,  262,  263,  265,  266,
   17,   18,  257,  257,  257,  257,  257,  257,  257,    0,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  265,  266,
};
final static short YYFINAL=8;
final static short YYMAXTOKEN=266;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"STRING","GT","GE","EQ","NE","LT","LE","COLON","OR","AND",
};
final static String yyrule[] = {
"$accept : start",
"start : expr",
"expr : STRING",
"expr : STRING COLON STRING",
"expr : GT STRING",
"expr : GE STRING",
"expr : LT STRING",
"expr : LE STRING",
"expr : EQ STRING",
"expr : NE STRING",
"expr : expr OR expr",
"expr : expr AND expr",
};

//#line 209 "constpar.y"
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
//#line 415 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
/* lai : add Exception Fri Jul 18 06:43:44 HKG 2003 */
int yyparse() throws Exception
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    else 
      yyval = new ParserVal(0);
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 13 "constpar.y"
{ 
           wherecl = (Wherecl) val_peek(0).obj; 
        }
break;
case 2:
//#line 17 "constpar.y"
{ 
          yyval.obj = new Wherecl();
          if (val_peek(0).sval.trim().equals("")) {
          }
          else {
             if (fwild) {
					 if (flike) {
                   ((Wherecl) yyval.obj) 
						    .appendString(new StringBuffer()
                                        .append(fdname)
                                        .append(" like ? ")
                                        .toString())
					       .appendArgument(new StringBuffer()
							                    .append(val_peek(0).sval.startsWith("^") 
													          ? StringUtil.strpart(val_peek(0).sval, 1, -1)
																 : "%"+val_peek(0).sval)
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
                   ((Wherecl) yyval.obj) 
						    .appendString(new StringBuffer()
                                        .append(fdname)
                                        .append(" matches ")
                                        .append(StringUtil.encodeInQuote(
													            new StringBuffer() 
                                                       .append(val_peek(0).sval.startsWith("^") 
	                                                            ? StringUtil.strpart(val_peek(0).sval, 1, -1) 
				                                                   : ".*"+val_peek(0).sval) 
	                                                    .append(".*") 
									                            .toString()))
                                        .toString());
				    }
             }
             else if (fDateToTime) {
                ((Wherecl) yyval.obj).andRange(fdname, 
                   DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000,
                   DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000+86400-1);
             }
             else if (fTime) {
                if (checkHourString(val_peek(0).sval) == 0) {
                   ((Wherecl) yyval.obj).andRange(fdname, 
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600,
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600+3600-1);
				    }
             }
             else {
                yyval.obj = constructUniop("=", val_peek(0).sval);
             }
          }
       }
break;
case 3:
//#line 81 "constpar.y"
{ 
          yyval.obj = new Wherecl();
          try {
             if (fDateToTime) {
                ((Wherecl) yyval.obj).andRange(fdname, 
                   DateUtil.getDate(val_peek(2).sval.trim(), dateFormat).getTime()/1000,
                   DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000+86400-1);
             }
             else if (fTime) {
                 if (checkHourString(val_peek(2).sval) == 0 && checkHourString(val_peek(0).sval) == 0) {
                   ((Wherecl) yyval.obj).andRange(fdname, 
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(2).sval.trim())*3600,
                      DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600+3600-1);
				    }
             }
             else {
                switch (fdtype) {
                   case TableBrowser.SQLTYPE_INTEGER:
                      ((Wherecl) yyval.obj).andRange(fdname, Integer.parseInt(val_peek(2).sval.trim()), Integer.parseInt(val_peek(0).sval.trim()));
                      break;
                   case TableBrowser.SQLTYPE_CHAR:
                      ((Wherecl) yyval.obj).andRange(fdname, val_peek(2).sval, val_peek(0).sval);
                      break;
                   case TableBrowser.SQLTYPE_FLOAT:
                      ((Wherecl) yyval.obj).andRange(fdname, Double.parseDouble(val_peek(2).sval.trim()), Double.parseDouble(val_peek(0).sval.trim()));
                      break;
                   case TableBrowser.SQLTYPE_DATE:
							 if (dateFormat == null)
                         ((Wherecl) yyval.obj).andRange(fdname, DateUtil.getDate(val_peek(2).sval.trim(), dateFormat), DateUtil.getDate(val_peek(0).sval.trim()));
							 else
                         ((Wherecl) yyval.obj).andRange(fdname, DateUtil.getDate(val_peek(2).sval.trim(), dateFormat), DateUtil.getDate(val_peek(0).sval.trim(), dateFormat));
                      break;
                   default: 
                      throw(new Exception("Invalid field type "+fdtype));
                }
             }
          } catch (Exception ex) {
             UniLog.log(ex);
             yyval.obj = null;
          }
       }
break;
case 4:
//#line 122 "constpar.y"
{ 
          if (fDateToTime)
             yyval.obj = constructUniop(">", ""+(DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000+86400-1));
          else if (fTime) {
             if (checkHourString(val_peek(0).sval) == 0)
                yyval.obj = constructUniop(">", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600+3600-1));
			 }
          else
             yyval.obj = constructUniop(">", val_peek(0).sval);
       }
break;
case 5:
//#line 132 "constpar.y"
{ 
          if (fDateToTime)
             yyval.obj = constructUniop(">=", ""+(DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000));
          else if (fTime) {
             if (checkHourString(val_peek(0).sval) == 0)
                yyval.obj = constructUniop(">=", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600));
			 }
          else
             yyval.obj = constructUniop(">=", val_peek(0).sval);
       }
break;
case 6:
//#line 142 "constpar.y"
{
          if (fDateToTime)
             yyval.obj = constructUniop("<", ""+(DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000));
          else if (fTime) {
             if (checkHourString(val_peek(0).sval) == 0)
                yyval.obj = constructUniop("<", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600));
			 }
          else
             yyval.obj = constructUniop("<", val_peek(0).sval);
       }
break;
case 7:
//#line 152 "constpar.y"
{
          if (fDateToTime)
             yyval.obj = constructUniop("<=", ""+(DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000+86400-1));
          else if (fTime) {
             if (checkHourString(val_peek(0).sval) == 0)
                yyval.obj = constructUniop("<=", ""+(DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600+3600-1));
			 }
          else
             yyval.obj = constructUniop("<=", val_peek(0).sval);
       }
break;
case 8:
//#line 162 "constpar.y"
{
          if (fDateToTime) {
             yyval.obj = new Wherecl();
             ((Wherecl) yyval.obj).andRange(fdname, 
                   DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000,
                   DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000+86400-1);
          }
          else if (fTime) {
             if (checkHourString(val_peek(0).sval) == 0) {
                yyval.obj = new Wherecl();
                ((Wherecl) yyval.obj).andRange(fdname, 
                      DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000,
                      DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000+86400-1);
			    }
          }
          else
             yyval.obj = constructUniop("=", val_peek(0).sval);
       }
break;
case 9:
//#line 180 "constpar.y"
{
          if (fDateToTime) {
             yyval.obj = new Wherecl();
             ((Wherecl) yyval.obj).andUniop(fdname, "<", DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000);
             Wherecl wc = new Wherecl();
             wc.andUniop(fdname, ">", DateUtil.getDate(val_peek(0).sval.trim(), dateFormat).getTime()/1000+86400-1);
             ((Wherecl) yyval.obj).orWherecl(wc);
          }
          else if (fTime) {
             if (checkHourString(val_peek(0).sval) == 0) {
                yyval.obj = new Wherecl();
                ((Wherecl) yyval.obj).andUniop(fdname, "<", DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600);
                Wherecl wc = new Wherecl();
                wc.andUniop(fdname, ">", DateUtil.dayBeginning(qdate).getTime()/1000+Integer.parseInt(val_peek(0).sval.trim())*3600+3600-1);
                ((Wherecl) yyval.obj).orWherecl(wc);
				 }
          }
          else
             yyval.obj = constructUniop("<>", val_peek(0).sval);
       }
break;
case 10:
//#line 200 "constpar.y"
{ 
          yyval.obj = ((Wherecl) val_peek(2).obj).orWherecl((Wherecl) val_peek(0).obj);
       }
break;
case 11:
//#line 203 "constpar.y"
{ 
          yyval.obj = ((Wherecl) val_peek(2).obj).andWherecl((Wherecl) val_peek(0).obj);
       }
break;
//#line 790 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
 /* lai : add Exception Fri Jul 18 06:43:44 HKG 2003 */
public void run() throws Exception
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
