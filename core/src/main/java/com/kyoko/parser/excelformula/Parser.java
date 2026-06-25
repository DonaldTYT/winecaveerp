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



package com.kyoko.parser.excelformula;



//#line 2 "calc.y"
  import java.io.*;
  import java.util.*;
  import com.kyoko.common.*;
  import com.kyoko.parser.*;
//#line 20 "Parser.java"




/**
 * Encapsulates yacc() parser functionality in a Java
 *        class for quick code development
 */
public class Parser
             extends com.kyoko.parser.Parser
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
public final static short NL=257;
public final static short NUM=258;
public final static short INT=259;
public final static short FUNC=260;
public final static short CELL=261;
public final static short RANGE=262;
public final static short STRING=263;
public final static short TRUE=264;
public final static short FALSE=265;
public final static short GT=266;
public final static short GE=267;
public final static short LT=268;
public final static short LE=269;
public final static short EQ=270;
public final static short NE=271;
public final static short NEG=272;
public final static short OR=273;
public final static short AND=274;
public final static short NOT=275;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    0,    0,    2,    2,    2,    2,    2,    2,    2,
    2,    3,    6,    6,    6,    6,    6,    6,    1,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    5,    5,    5,    4,    4,    4,
};
final static short yylen[] = {                            2,
    1,    1,    0,    1,    1,    3,    3,    2,    3,    3,
    1,    4,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    3,    3,    3,    3,    3,    2,    3,    3,
    1,    1,    1,    1,    0,    1,    3,
};
final static short yydefred[] = {                         0,
   19,   20,    0,   22,   21,    4,    5,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   31,    0,    8,
    0,    0,   13,   14,   15,   16,   17,   18,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   34,    0,    0,
    0,   36,    0,   30,    9,    0,    0,    0,    0,    0,
    0,    0,    0,    6,   12,    0,   37,
};
final static short yydgoto[] = {                         11,
   19,   40,   18,   41,   42,   35,
};
final static short yysindex[] = {                       -32,
    0,    0,  -31,    0,    0,    0,    0,   95,  -32,  -32,
    0,   94, -257,    0,  -40,   95,  -92,    0,   94,    0,
  -15,  -37,    0,    0,    0,    0,    0,    0,   95,   95,
   95,   95,   95,   95,   95,  -32,  -32,    0,   94, -257,
  -38,    0,  -23,    0,    0,   81,   81,  -24,  -24,  -92,
  -92,  113, -262,    0,    0,  -40,    0,
};
final static short yyrindex[] = {                        25,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   29,   35,    1,  -34,    0,   11,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   -8,   -7,
    0,    0,    0,    0,    0,   67,   77,   40,   57,   21,
   31,   47,   86,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
  134,  161,  116,    0,  -18,    0,
};
final static int YYTABLESIZE=365;
final static short yytable[] = {                         10,
   11,   34,   55,   45,    8,   56,   35,   10,   15,   35,
   28,   37,    8,   33,   33,   36,   37,   44,   31,   30,
   27,   29,   33,   32,    3,   44,   31,   30,    1,   29,
   29,   32,   32,   33,    2,   32,   33,   57,   31,   25,
    0,   11,   31,   31,   11,   31,   10,   31,   28,    0,
    0,   28,   28,   28,   28,   28,   26,   28,   27,    0,
    0,   27,   27,   27,   27,   27,   24,   27,   29,   34,
   34,   29,   29,   29,   29,   29,   23,   29,   34,    0,
   25,   25,   25,   25,   25,    7,   25,   10,    0,    0,
   10,    0,    0,    0,   31,    0,    0,   26,   26,   26,
   26,   26,    0,   26,    0,    0,    0,   24,    0,   24,
   24,   24,    0,    0,    0,   14,    0,   23,   33,   23,
   23,   23,   31,    0,   14,   14,    7,   32,    0,    7,
   14,   33,    0,   12,   16,   31,   30,    0,   29,    8,
   32,   17,    0,   21,    0,    0,    0,    0,   39,   43,
   33,   14,   14,    0,   31,   30,    0,   29,    0,   32,
   13,    0,   46,   47,   48,   49,   50,   51,   52,   20,
   22,   14,    0,    0,   34,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   34,    0,   39,
    0,    0,    0,    0,    0,    0,   53,   54,    0,    0,
    0,    0,    0,    0,    0,    0,   34,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    1,    2,    3,
    4,   38,    5,    6,    7,    1,    2,    3,    4,    0,
    5,    6,    7,    0,    9,   36,   37,    0,    0,    0,
    0,    0,    9,    0,    0,    0,    0,    0,    0,    0,
   23,   24,   25,   26,   27,   28,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   31,   31,   31,   31,
   31,   31,    0,   11,   11,    0,   28,   28,   28,   28,
   28,   28,    0,   28,   28,    0,   27,   27,   27,   27,
   27,   27,    0,   27,   27,    0,   29,   29,   29,   29,
   29,   29,    0,   29,   29,   25,   25,   25,   25,   25,
   25,    0,   25,   25,    0,    0,    0,    0,    0,   10,
   10,    0,   26,   26,   26,   26,   26,   26,    0,   26,
   26,    0,   24,   24,   24,   24,   24,   24,    0,   24,
   24,    0,   23,   23,   23,   23,   23,   23,    0,   23,
   23,    0,    1,    2,    3,    4,    0,    5,    7,   23,
   24,   25,   26,   27,   28,
};
final static short yycheck[] = {                         40,
    0,   94,   41,   41,   45,   44,   41,   40,   40,   44,
    0,  274,   45,   38,   38,  273,  274,   41,   42,   43,
    0,   45,   38,   47,    0,   41,   42,   43,    0,   45,
    0,   47,   41,   41,    0,   44,   44,   56,   38,    0,
   -1,   41,   42,   43,   44,   45,    0,   47,   38,   -1,
   -1,   41,   42,   43,   44,   45,    0,   47,   38,   -1,
   -1,   41,   42,   43,   44,   45,    0,   47,   38,   94,
   94,   41,   42,   43,   44,   45,    0,   47,   94,   -1,
   41,   42,   43,   44,   45,    0,   47,   41,   -1,   -1,
   44,   -1,   -1,   -1,   94,   -1,   -1,   41,   42,   43,
   44,   45,   -1,   47,   -1,   -1,   -1,   41,   -1,   43,
   44,   45,   -1,   -1,   -1,    0,   -1,   41,   38,   43,
   44,   45,   42,   -1,    9,   10,   41,   47,   -1,   44,
   15,   38,   -1,    0,   40,   42,   43,   -1,   45,   45,
   47,    8,   -1,   10,   -1,   -1,   -1,   -1,   15,   16,
   38,   36,   37,   -1,   42,   43,   -1,   45,   -1,   47,
    0,   -1,   29,   30,   31,   32,   33,   34,   35,    9,
   10,   56,   -1,   -1,   94,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   94,   -1,   56,
   -1,   -1,   -1,   -1,   -1,   -1,   36,   37,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   94,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,  258,  259,  260,
  261,  262,  263,  264,  265,  258,  259,  260,  261,   -1,
  263,  264,  265,   -1,  275,  273,  274,   -1,   -1,   -1,
   -1,   -1,  275,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  266,  267,  268,  269,  270,  271,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  266,  267,  268,  269,
  270,  271,   -1,  273,  274,   -1,  266,  267,  268,  269,
  270,  271,   -1,  273,  274,   -1,  266,  267,  268,  269,
  270,  271,   -1,  273,  274,   -1,  266,  267,  268,  269,
  270,  271,   -1,  273,  274,  266,  267,  268,  269,  270,
  271,   -1,  273,  274,   -1,   -1,   -1,   -1,   -1,  273,
  274,   -1,  266,  267,  268,  269,  270,  271,   -1,  273,
  274,   -1,  266,  267,  268,  269,  270,  271,   -1,  273,
  274,   -1,  266,  267,  268,  269,  270,  271,   -1,  273,
  274,   -1,  258,  259,  260,  261,   -1,  263,  273,  266,
  267,  268,  269,  270,  271,
};
final static short YYFINAL=11;
final static short YYMAXTOKEN=275;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,"'&'",null,"'('","')'","'*'","'+'",
"','","'-'",null,"'/'",null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"'^'",null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,"NL","NUM","INT","FUNC","CELL","RANGE",
"STRING","TRUE","FALSE","GT","GE","LT","LE","EQ","NE","NEG","OR","AND","NOT",
};
final static String yyrule[] = {
"$accept : line",
"line : exp",
"line : bexp",
"line :",
"bexp : TRUE",
"bexp : FALSE",
"bexp : bexp AND bexp",
"bexp : bexp OR bexp",
"bexp : NOT bexp",
"bexp : '(' bexp ')'",
"bexp : exp bexpoper exp",
"bexp : functexpr",
"functexpr : FUNC '(' arglist ')'",
"bexpoper : GT",
"bexpoper : GE",
"bexpoper : LT",
"bexpoper : LE",
"bexpoper : EQ",
"bexpoper : NE",
"exp : NUM",
"exp : INT",
"exp : STRING",
"exp : CELL",
"exp : exp '+' exp",
"exp : exp '-' exp",
"exp : exp '*' exp",
"exp : exp '/' exp",
"exp : exp '&' exp",
"exp : '-' exp",
"exp : exp '^' exp",
"exp : '(' exp ')'",
"exp : functexpr",
"argelt : exp",
"argelt : bexp",
"argelt : RANGE",
"arglist :",
"arglist : argelt",
"arglist : arglist ',' argelt",
};

//#line 128 "calc.y"
  private boolean fCollect = true;
  private Yylex lexer = null;
  private String errorMessage = null;
  private Object result = new Double(0);
  private VariableInterface varInterface = null;
  private FunctionInterface functInterface = null;
  private CellPositionInterface cellPositionInterface = null;
  private String lastInput = null;

  public boolean hasError() {
     return(errorMessage != null);
  }
  public String getErrorMessage() {
     return(errorMessage);
  }
  private int yylex() {
     int yyl_return = -1;
     try {
        yylval = new ParserVal(0);
        yyl_return = lexer.yylex();
     } catch (IOException e) {
		  CoreLog.log(e);
     }
     return yyl_return;
  }
  public void yyerror(String error) {
     CoreLog.logClass(this, error);
  }
  public Parser(Reader r) {
     lexer = new Yylex(r, this);
  }
  public Parser(String p_string) {
     lastInput = p_string;
     //lexer = new Yylex(new StringReader(p_string+"\n"), this);
  }
  public Parser(CellPositionInterface p_cellPositionInterface) {
  	  cellPositionInterface = p_cellPositionInterface;
  }
  public Parser(String p_string, VariableInterface p_varInterface, FunctionInterface p_functInterface) {
     this(p_string);
     varInterface = p_varInterface;
     functInterface = p_functInterface;
  }
  public void setVarInterface(VariableInterface p_varInterface) {
     varInterface = p_varInterface;
  }
  public void setFunctInterface(FunctionInterface p_functInterface) {
     functInterface = p_functInterface;
  }
  public void setCellPositionInterface(CellPositionInterface p_cellPositionInterface) {
  	  cellPositionInterface = p_cellPositionInterface;
  }
  public Object evalCellRef(String p_varname) throws Exception {
		return(new Expression(ExcelCellRef.newExcelCell(p_varname,varInterface,cellPositionInterface)
		));
  }
  public Object evalRange(String p_varname) throws Exception {
		return(new ExcelRange(p_varname,varInterface,cellPositionInterface));
  }
  public void setResult(Object p_result) {
     result = p_result;
  }
  public Object getResult() {
     return(result);
  }
  public Object parse() throws Exception {
     lexer = new Yylex(new StringReader(lastInput+"\n"), this);
	  errorMessage = null;
     yyparse();
	  if (errorMessage != null)
	     throw(new Exception(errorMessage));
     return(getResult());
  }
  public Object parse(String p_input) throws Exception {
     lastInput = p_input;
     lexer = new Yylex(new StringReader(p_input+"\n"), this);
     return(parse());
  }
  public Object comparison(Expression p_x, int p_operand, Expression p_y) throws Exception {
	  switch (p_operand) {
	     case GT:
//		     return(new Boolean(x.compareTo(y) > 0));
		     return(new Condition(Condition.COMPARE_MODE_IGNORECASE,p_x,Condition.COMPARE_OP_GT,p_y));
	     case GE:
//		     return(new Boolean(x.compareTo(y) >= 0));
		     return(new Condition(Condition.COMPARE_MODE_IGNORECASE,p_x,Condition.COMPARE_OP_GE,p_y));
	     case LT:
//		     return(new Boolean(x.compareTo(y) < 0));
		     return(new Condition(Condition.COMPARE_MODE_IGNORECASE,p_x,Condition.COMPARE_OP_LT,p_y));
	     case LE:
//		     return(new Boolean(x.compareTo(y) <= 0));
		     return(new Condition(Condition.COMPARE_MODE_IGNORECASE,p_x,Condition.COMPARE_OP_LE,p_y));
	     case EQ:
//		     return(new Boolean(x.compareTo(y) == 0));
		     return(new Condition(Condition.COMPARE_MODE_IGNORECASE,p_x,Condition.COMPARE_OP_EQ,p_y));
	     case NE:
//		     return(new Boolean(x.compareTo(y) != 0));
		     return(new Condition(Condition.COMPARE_MODE_IGNORECASE,p_x,Condition.COMPARE_OP_NE,p_y));
	  }
//	  return(new Boolean(false));
	  return(null);
  }

  public int getErrCnt()
  {
  		return(yynerrs);
  }
  public int getErrPos() {
		return(lexer.getCurrentPos());
  }
//#line 424 "Parser.java"
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
//#line 41 "calc.y"
{
		       setResult(val_peek(0).obj);
           }
break;
case 2:
//#line 44 "calc.y"
{
		       setResult(val_peek(0).obj);
           }
break;
case 4:
//#line 49 "calc.y"
{ /* $$ = new Boolean(true); */ yyval.obj = new Condition(true);}
break;
case 5:
//#line 50 "calc.y"
{ /* $$ = new Boolean(false); */ yyval.obj = new Condition(false);}
break;
case 6:
//#line 51 "calc.y"
{ 
/*		 		$$ = new Boolean((((Boolean) $1).booleanValue() && ((Boolean) $3).booleanValue()));*/
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_AND,(Condition) val_peek(0).obj);
				}
break;
case 7:
//#line 55 "calc.y"
{ 
/*		 		$$ = new Boolean((((Boolean) $1).booleanValue() || ((Boolean) $3).booleanValue())); */
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_OR,(Condition) val_peek(0).obj);
				}
break;
case 8:
//#line 59 "calc.y"
{ /* $$ = new Boolean(!(((Boolean) $2).booleanValue())); */ yyval.obj = new Condition(Condition.LOGIC_OP_NOT,(Condition) val_peek(0).obj);}
break;
case 9:
//#line 60 "calc.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 10:
//#line 61 "calc.y"
{ yyval.obj = comparison((Expression) val_peek(2).obj, val_peek(1).ival, (Expression) val_peek(0).obj); }
break;
case 11:
//#line 62 "calc.y"
{
		      yyval.obj = val_peek(0).obj;
		   }
break;
case 12:
//#line 66 "calc.y"
{
			   try {
/*		         $$ = evalfunct(((LexIdent) $1).getIdname(), (Vector) $3); */
		         yyval.obj = new Expression(
						 new Function (((LexIdent) val_peek(3).obj).getIdname(), functInterface,(Vector) val_peek(1).obj)
						); 
			   } catch (Exception ex) {
			      errorMessage = ex.toString();
			      throw(ex);
			   }
			}
break;
case 13:
//#line 78 "calc.y"
{ yyval.ival = GT; }
break;
case 14:
//#line 79 "calc.y"
{ yyval.ival = GE; }
break;
case 15:
//#line 80 "calc.y"
{ yyval.ival = LT; }
break;
case 16:
//#line 81 "calc.y"
{ yyval.ival = LE; }
break;
case 17:
//#line 82 "calc.y"
{ yyval.ival = EQ; }
break;
case 18:
//#line 83 "calc.y"
{ yyval.ival = NE; }
break;
case 19:
//#line 86 "calc.y"
{ /* $$ = new Double($1); */ yyval.obj = new Expression(val_peek(0).dval); }
break;
case 20:
//#line 87 "calc.y"
{ /* $$ = new Integer($1); */ yyval.obj = new Expression(val_peek(0).ival); }
break;
case 21:
//#line 88 "calc.y"
{ /* $$ = $1; */ yyval.obj = new Expression(val_peek(0).sval);}
break;
case 22:
//#line 89 "calc.y"
{ 
					   try {
		               yyval.obj = evalCellRef(((LexIdent) val_peek(0).obj).getIdname()); 
						} catch (Exception ex) {
							errorMessage = ex.toString();
							throw(ex);
						}
					 }
break;
case 23:
//#line 97 "calc.y"
{ /* $$ = operation($1, '+', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_PLUS,(Expression) val_peek(0).obj);}
break;
case 24:
//#line 98 "calc.y"
{ /* $$ = operation($1, '-', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_MINUS,(Expression) val_peek(0).obj);}
break;
case 25:
//#line 99 "calc.y"
{ /* $$ = operation($1, '*', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_MULTIPLY,(Expression) val_peek(0).obj);}
break;
case 26:
//#line 100 "calc.y"
{ /* $$ = operation($1, '/', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_DIVIDE,(Expression) val_peek(0).obj);}
break;
case 27:
//#line 101 "calc.y"
{ /* $$ = operation($1, '&', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_AND,(Expression) val_peek(0).obj);}
break;
case 28:
//#line 102 "calc.y"
{ /* $$ = operation(null, '-', $2) */ yyval.obj = new Expression(null,Expression.OPERATOR_MINUS,(Expression) val_peek(0).obj);}
break;
case 29:
//#line 103 "calc.y"
{ /* $$ = operation($1, '^', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_XOR,(Expression) val_peek(0).obj);}
break;
case 30:
//#line 104 "calc.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 31:
//#line 105 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 32:
//#line 107 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 33:
//#line 108 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 34:
//#line 109 "calc.y"
{ 
					   try {
		               yyval.obj = evalRange(((LexIdent) val_peek(0).obj).getIdname()); 
						} catch (Exception ex) {
							errorMessage = ex.toString();
							throw(ex);
						}
					 }
break;
case 35:
//#line 118 "calc.y"
{ yyval.obj = null; }
break;
case 36:
//#line 119 "calc.y"
{ Vector v = new Vector(); 
		                        v.addElement(val_peek(0).obj);
									   yyval.obj = v;
		                      }
break;
case 37:
//#line 123 "calc.y"
{ ((Vector) val_peek(2).obj).addElement(val_peek(0).obj);
		                        yyval.obj = val_peek(2).obj;
		                      }
break;
//#line 759 "Parser.java"
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
