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



package com.uniinformation.utils.whereclpar;



//#line 2 "calc.y"
  import java.io.*;
  import java.util.*;

import com.kyoko.parser.FunctionInterface;
import com.kyoko.parser.VariableInterface;
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
public final static short NL=257;
public final static short NUM=258;
public final static short INT=259;
public final static short IDENT=260;
public final static short STRING=261;
public final static short TRUE=262;
public final static short FALSE=263;
public final static short GT=264;
public final static short GE=265;
public final static short LT=266;
public final static short LE=267;
public final static short EQ=268;
public final static short NE=269;
public final static short MATCHES=270;
public final static short LIKE=271;
public final static short T_IS=272;
public final static short T_NOT=273;
public final static short T_IN=274;
public final static short T_NULL=275;
public final static short NOT_MATCHES=276;
public final static short NOT_REGEXP=277;
public final static short NOT_LIKE=278;
public final static short T_BETWEEN=279;
public final static short T_REGEXP=280;
public final static short NEG=281;
public final static short OR=282;
public final static short AND=283;
public final static short NOT=284;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    0,    0,    2,    2,    2,    2,    2,    2,    2,
    2,    2,    2,    2,    2,    2,    2,    2,    3,    6,
    6,    6,    6,    6,    6,    6,    6,    6,    6,    6,
    6,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    1,    1,    1,    5,    5,    4,    4,    4,
};
final static short yylen[] = {                            2,
    1,    1,    0,    1,    1,    3,    3,    2,    2,    3,
    3,    3,    4,    5,    6,    5,    6,    1,    4,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    2,    2,
    2,    1,    1,    1,    1,    3,    3,    3,    3,    2,
    3,    3,    1,    4,    1,    1,    0,    1,    3,
};
final static short yydefred[] = {                         0,
   32,   33,    0,   34,    4,    5,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   43,    9,    0,    0,   20,   21,   22,   23,   24,   25,
   26,   27,    0,    0,    0,    0,   28,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   48,    0,
    0,   42,   10,    0,   12,   30,   29,    0,    0,   31,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    6,
   19,    0,   44,   13,    0,    0,    0,    0,   49,    0,
    0,   14,    0,   15,    0,
};
final static short yydgoto[] = {                         11,
   17,   47,   21,   48,   49,   43,
};
final static short yysindex[] = {                       -40,
    0,    0,  -38,    0,    0,    0,  -40,  -21,  -40,  -40,
    0,  182, -245,    0,  -40,  -21,  182, -245,  -21,  -91,
    0,    0,  161,  -37,    0,    0,    0,    0,    0,    0,
    0,    0, -262, -181,  -11,  -21,    0,  -21,  -21,  -21,
  -21,  -21,  -21,  -40,  -40,  182, -245,    8,    0,   67,
  -15,    0,    0, -260,    0,    0,    0,   -9,  -21,    0,
  -40,  -35,   -7,   -7,  -91,  -91,  -91,   93, -258,    0,
    0,  -40,    0,    0,  -40,  -25,   10,  -21,    0,   26,
  -21,    0,   93,    0,   93,
};
final static short yyrindex[] = {                        33,
    0,    0,    1,    0,    0,    0,    0,    0,    0,    0,
    0,   39,   72,   21,   30,    0,    0,    6,    0,   41,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   32,   56,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   30,    0,  121,  141,   61,   81,  101,   14,   36,    0,
    0,    0,    0,    0,   30,    0,    0,    0,    0,    0,
    0,    0,   16,    0,   34,
};
final static short yygindex[] = {                         0,
  457,  168,  459,  -52,  -16,    0,
};
final static int YYTABLESIZE=538;
final static short yytable[] = {                         10,
   35,   15,   42,   53,    8,    8,   40,   39,   77,   38,
   54,   41,   55,   11,   74,   16,   40,   39,   19,   38,
   18,   41,   80,    8,   45,   52,   40,   39,   61,   38,
   75,   41,    3,   17,   40,    7,   44,   45,    1,   41,
   40,   35,   35,   35,   35,   35,    8,   35,   71,    8,
   82,   72,   16,   72,   11,   79,   16,   11,   42,   16,
   38,   18,   43,   43,   18,   43,   84,   43,   42,   72,
   47,    2,   45,   47,   17,   45,    7,   17,   42,    7,
   39,   40,   40,   40,   40,   40,   42,   40,   56,   57,
    0,    0,   58,   35,   35,    0,   46,   59,   60,   46,
   41,   38,   38,   38,   38,   38,    0,   38,   40,   39,
    0,   38,    0,   41,   43,    0,    0,    0,    0,    0,
   37,   39,   39,   39,   39,   39,    0,   39,    0,    0,
    0,    0,    0,   40,   40,   39,    0,   38,    0,   41,
   36,   41,   41,   41,   41,   41,    0,   41,    0,    0,
    0,    0,    0,   38,    0,    0,    0,    0,    0,   73,
   42,   37,    0,   37,   37,   37,    0,   13,    0,    0,
    0,    0,    0,   39,   18,    0,   22,   24,    0,    0,
    0,   36,    0,   36,   36,   36,   42,    0,    0,    0,
    0,    0,    0,   41,    0,    0,    0,    0,    0,    0,
    0,   52,   40,   39,    0,   38,    0,   41,    0,    0,
    0,   69,   70,   37,    0,    0,    0,    1,    2,    3,
    4,    5,    6,   40,   39,    0,   38,    0,   41,    0,
    0,    0,    7,   36,    0,    0,    1,    2,    3,    4,
    0,    0,    0,    9,   44,   45,    0,   78,    0,    0,
    0,    0,    0,    0,   42,    0,    0,   81,    0,    0,
    0,    0,    0,    0,   35,   35,   35,   35,   35,   35,
   35,   35,   35,   35,   35,   42,    0,    0,    0,   35,
   35,    0,   35,   35,   43,   43,   43,   43,   43,   43,
   43,   43,   43,   43,   43,   11,   11,   16,   16,   43,
   43,    0,   18,   18,   40,   40,   40,   40,   40,   40,
   40,   40,   40,   40,   40,   17,   17,    7,    0,   40,
   40,    0,   40,   40,   38,   38,   38,   38,   38,   38,
   38,   38,   38,   38,   38,    0,    0,    0,    0,   38,
   38,    0,   38,   38,   39,   39,   39,   39,   39,   39,
   39,   39,   39,   39,   39,    0,    0,    0,    0,   39,
   39,    0,   39,   39,   41,   41,   41,   41,   41,   41,
   41,   41,   41,   41,   41,    0,    0,    0,    0,   41,
   41,    0,   41,   41,   37,   37,   37,   37,   37,   37,
   37,   37,   37,   37,   37,    0,    0,    0,    0,   37,
   37,    0,   37,   37,   36,   36,   36,   36,   36,   36,
   36,   36,   36,   36,   36,    0,    0,    0,    0,   36,
   36,    0,   36,   36,   25,   26,   27,   28,   29,   30,
   31,   32,   33,   34,   35,    0,    0,    0,    0,   36,
   37,    0,    0,    0,    0,   25,   26,   27,   28,   29,
   30,   31,   32,   33,   34,   35,   12,    0,   14,    0,
   36,   37,    0,    0,   20,   14,   23,   14,   14,    0,
    0,   46,   50,   14,    0,   51,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   62,    0,   63,   64,   65,   66,   67,   68,
    0,    0,   14,   14,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   76,    0,   46,    0,   14,
    0,    0,    0,    0,    0,    0,    0,    0,   46,    0,
   14,   46,    0,   14,   83,    0,    0,   85,
};
final static short yycheck[] = {                         40,
    0,   40,   94,   41,   45,    0,   42,   43,   61,   45,
  273,   47,  275,    0,  275,    0,   42,   43,   40,   45,
    0,   47,   75,   45,  283,   41,   42,   43,   40,   45,
   40,   47,    0,    0,   42,    0,  282,  283,    0,   47,
    0,   41,   42,   43,   44,   45,   41,   47,   41,   44,
   41,   44,   91,   44,   41,   72,   41,   44,   94,   44,
    0,   41,   42,   43,   44,   45,   41,   47,   94,   44,
   41,    0,   41,   44,   41,   44,   41,   44,   94,   44,
    0,   41,   42,   43,   44,   45,   94,   47,  270,  271,
   -1,   -1,  274,   93,   94,   -1,   41,  279,  280,   44,
    0,   41,   42,   43,   44,   45,   -1,   47,   42,   43,
   -1,   45,   -1,   47,   94,   -1,   -1,   -1,   -1,   -1,
    0,   41,   42,   43,   44,   45,   -1,   47,   -1,   -1,
   -1,   -1,   -1,   93,   42,   43,   -1,   45,   -1,   47,
    0,   41,   42,   43,   44,   45,   -1,   47,   -1,   -1,
   -1,   -1,   -1,   93,   -1,   -1,   -1,   -1,   -1,   93,
   94,   41,   -1,   43,   44,   45,   -1,    0,   -1,   -1,
   -1,   -1,   -1,   93,    7,   -1,    9,   10,   -1,   -1,
   -1,   41,   -1,   43,   44,   45,   94,   -1,   -1,   -1,
   -1,   -1,   -1,   93,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   41,   42,   43,   -1,   45,   -1,   47,   -1,   -1,
   -1,   44,   45,   93,   -1,   -1,   -1,  258,  259,  260,
  261,  262,  263,   42,   43,   -1,   45,   -1,   47,   -1,
   -1,   -1,  273,   93,   -1,   -1,  258,  259,  260,  261,
   -1,   -1,   -1,  284,  282,  283,   -1,  283,   -1,   -1,
   -1,   -1,   -1,   -1,   94,   -1,   -1,  283,   -1,   -1,
   -1,   -1,   -1,   -1,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   94,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,  282,  283,  282,  283,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,  282,  283,  282,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,   -1,   -1,   -1,  264,  265,  266,  267,  268,
  269,  270,  271,  272,  273,  274,    0,   -1,    0,   -1,
  279,  280,   -1,   -1,    8,    7,   10,    9,   10,   -1,
   -1,   15,   16,   15,   -1,   19,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   36,   -1,   38,   39,   40,   41,   42,   43,
   -1,   -1,   44,   45,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   59,   -1,   61,   -1,   61,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   72,   -1,
   72,   75,   -1,   75,   78,   -1,   -1,   81,
};
final static short YYFINAL=11;
final static short YYMAXTOKEN=284;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,"'('","')'","'*'","'+'","','",
"'-'",null,"'/'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
"'['",null,"']'","'^'",null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,"NL","NUM","INT","IDENT","STRING","TRUE",
"FALSE","GT","GE","LT","LE","EQ","NE","MATCHES","LIKE","T_IS","T_NOT","T_IN",
"T_NULL","NOT_MATCHES","NOT_REGEXP","NOT_LIKE","T_BETWEEN","T_REGEXP","NEG",
"OR","AND","NOT",
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
"bexp : T_NOT bexp",
"bexp : NOT bexp",
"bexp : '(' bexp ')'",
"bexp : exp bexpoper exp",
"bexp : exp T_IS T_NULL",
"bexp : exp T_IS T_NOT T_NULL",
"bexp : exp T_IN '(' arglist ')'",
"bexp : exp T_NOT T_IN '(' arglist ')'",
"bexp : exp T_BETWEEN exp AND exp",
"bexp : exp T_NOT T_BETWEEN exp AND exp",
"bexp : functexpr",
"functexpr : IDENT '(' arglist ')'",
"bexpoper : GT",
"bexpoper : GE",
"bexpoper : LT",
"bexpoper : LE",
"bexpoper : EQ",
"bexpoper : NE",
"bexpoper : MATCHES",
"bexpoper : LIKE",
"bexpoper : T_REGEXP",
"bexpoper : T_NOT LIKE",
"bexpoper : T_NOT MATCHES",
"bexpoper : T_NOT T_REGEXP",
"exp : NUM",
"exp : INT",
"exp : STRING",
"exp : IDENT",
"exp : exp '+' exp",
"exp : exp '-' exp",
"exp : exp '*' exp",
"exp : exp '/' exp",
"exp : '-' exp",
"exp : exp '^' exp",
"exp : '(' exp ')'",
"exp : functexpr",
"exp : IDENT '[' exp ']'",
"argelt : exp",
"argelt : bexp",
"arglist :",
"arglist : argelt",
"arglist : arglist ',' argelt",
};

//#line 152 "calc.y"
  private boolean fCollect = true;
  private Yylex lexer = null;
  private String errorMessage = null;
  private Object result = new Double(0);
  private VariableInterface varInterface = null;
  private FunctionInterface functInterface = null;
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
		  UniLog.log(e);
     }
     return yyl_return;
  }
  public void yyerror(String error) {
     UniLog.logClass(this, error);
  }
  public Parser(Reader r) {
     lexer = new Yylex(r, this);
  }
  public Parser(String p_string) {
     lastInput = p_string;
     //lexer = new Yylex(new StringReader(p_string+"\n"), this);
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
  public Object evalVariable(String p_varname) throws Exception {
		return(new Expression(new Variable(p_varname,varInterface)));
  }
  public Object evalVariable(String p_varname, Expression p_expr) throws Exception {
	  return(new Expression(new Variable(p_varname,varInterface,p_expr)));
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
		     return(new Condition(p_x,Condition.COMPARE_OP_GT,p_y));
	     case GE:
//		     return(new Boolean(x.compareTo(y) >= 0));
		     return(new Condition(p_x,Condition.COMPARE_OP_GE,p_y));
	     case LT:
//		     return(new Boolean(x.compareTo(y) < 0));
		     return(new Condition(p_x,Condition.COMPARE_OP_LT,p_y));
	     case LE:
//		     return(new Boolean(x.compareTo(y) <= 0));
		     return(new Condition(p_x,Condition.COMPARE_OP_LE,p_y));
	     case EQ:
//		     return(new Boolean(x.compareTo(y) == 0));
		     return(new Condition(p_x,Condition.COMPARE_OP_EQ,p_y));
	     case NE:
//		     return(new Boolean(x.compareTo(y) != 0));
		     return(new Condition(p_x,Condition.COMPARE_OP_NE,p_y));
	     case MATCHES:
//		     return(new Boolean(compare_matches(x.toString(), y.toString())));
		     return(new Condition(p_x,Condition.COMPARE_OP_MA,p_y));
	     case LIKE:
//		     return(new Boolean(compare_like(x.toString(), y.toString())));
		     return(new Condition(p_x,Condition.COMPARE_OP_LK,p_y));
	     case T_REGEXP:
		     return(new Condition(p_x,Condition.COMPARE_OP_REGEXP,p_y));
	     case NOT_LIKE:
//		     return(new Boolean(compare_like(x.toString(), y.toString())));
		     return(new Condition(p_x,Condition.COMPARE_OP_NLK,p_y));
	     case NOT_MATCHES:
//		     return(new Boolean(compare_like(x.toString(), y.toString())));
		     return(new Condition(p_x,Condition.COMPARE_OP_NM,p_y));
	     case NOT_REGEXP:
		     return(new Condition(p_x,Condition.COMPARE_OP_NOT_REGEXP,p_y));
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
//#line 499 "Parser.java"
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
//#line 48 "calc.y"
{
		       setResult(val_peek(0).obj);
           }
break;
case 2:
//#line 51 "calc.y"
{
		       setResult(val_peek(0).obj);
           }
break;
case 4:
//#line 56 "calc.y"
{ /* $$ = new Boolean(true); */ yyval.obj = new Condition(true);}
break;
case 5:
//#line 57 "calc.y"
{ /* $$ = new Boolean(false); */ yyval.obj = new Condition(false);}
break;
case 6:
//#line 58 "calc.y"
{ 
/*		 		$$ = new Boolean((((Boolean) $1).booleanValue() && ((Boolean) $3).booleanValue()));*/
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_AND,(Condition) val_peek(0).obj);
				}
break;
case 7:
//#line 62 "calc.y"
{ 
/*		 		$$ = new Boolean((((Boolean) $1).booleanValue() || ((Boolean) $3).booleanValue())); */
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_OR,(Condition) val_peek(0).obj);
				}
break;
case 8:
//#line 66 "calc.y"
{ /* $$ = new Boolean(!(((Boolean) $2).booleanValue())); */ yyval.obj = new Condition(Condition.LOGIC_OP_NOT,(Condition) val_peek(0).obj);}
break;
case 9:
//#line 67 "calc.y"
{ /* $$ = new Boolean(!(((Boolean) $2).booleanValue())); */ yyval.obj = new Condition(Condition.LOGIC_OP_NOT,(Condition) val_peek(0).obj);}
break;
case 10:
//#line 68 "calc.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 11:
//#line 69 "calc.y"
{ yyval.obj = comparison((Expression) val_peek(2).obj, val_peek(1).ival, (Expression) val_peek(0).obj); }
break;
case 12:
//#line 70 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(2).obj,Condition.COMPARE_OP_IS_NULL);}
break;
case 13:
//#line 71 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(3).obj,Condition.COMPARE_OP_IS_NOT_NULL);}
break;
case 14:
//#line 72 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(4).obj,Condition.COMPARE_OP_IN_ITEMLIST,(List) val_peek(1).obj);}
break;
case 15:
//#line 73 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(5).obj,Condition.COMPARE_OP_NOTIN_ITEMLIST,(List) val_peek(1).obj);}
break;
case 16:
//#line 74 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(4).obj,Condition.COMPARE_OP_BETWEEN,(Expression) val_peek(2).obj,(Expression) val_peek(0).obj);}
break;
case 17:
//#line 75 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(5).obj,Condition.COMPARE_OP_NOT_BETWEEN,(Expression) val_peek(2).obj,(Expression) val_peek(0).obj);}
break;
case 18:
//#line 77 "calc.y"
{
		      yyval.obj = val_peek(0).obj;
		   }
break;
case 19:
//#line 81 "calc.y"
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
case 20:
//#line 93 "calc.y"
{ yyval.ival = GT; }
break;
case 21:
//#line 94 "calc.y"
{ yyval.ival = GE; }
break;
case 22:
//#line 95 "calc.y"
{ yyval.ival = LT; }
break;
case 23:
//#line 96 "calc.y"
{ yyval.ival = LE; }
break;
case 24:
//#line 97 "calc.y"
{ yyval.ival = EQ; }
break;
case 25:
//#line 98 "calc.y"
{ yyval.ival = NE; }
break;
case 26:
//#line 99 "calc.y"
{ yyval.ival = MATCHES; }
break;
case 27:
//#line 100 "calc.y"
{ yyval.ival = LIKE; }
break;
case 28:
//#line 101 "calc.y"
{ yyval.ival = T_REGEXP; }
break;
case 29:
//#line 102 "calc.y"
{yyval.ival = NOT_LIKE;}
break;
case 30:
//#line 103 "calc.y"
{yyval.ival = NOT_MATCHES;}
break;
case 31:
//#line 104 "calc.y"
{yyval.ival = NOT_REGEXP;}
break;
case 32:
//#line 107 "calc.y"
{ /* $$ = new Double($1); */ yyval.obj = new Expression(val_peek(0).dval); }
break;
case 33:
//#line 108 "calc.y"
{ /* $$ = new Integer($1); */ yyval.obj = new Expression(val_peek(0).ival); }
break;
case 34:
//#line 109 "calc.y"
{ /* $$ = $1; */ yyval.obj = new Expression(val_peek(0).sval);}
break;
case 35:
//#line 110 "calc.y"
{ 
					   try {
		               yyval.obj = evalVariable(((LexIdent) val_peek(0).obj).getIdname()); 
						} catch (Exception ex) {
							errorMessage = ex.toString();
							throw(ex);
						}
					 }
break;
case 36:
//#line 118 "calc.y"
{ /* $$ = operation($1, '+', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_PLUS,(Expression) val_peek(0).obj);}
break;
case 37:
//#line 119 "calc.y"
{ /* $$ = operation($1, '-', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_MINUS,(Expression) val_peek(0).obj);}
break;
case 38:
//#line 120 "calc.y"
{ /* $$ = operation($1, '*', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_MULTIPLY,(Expression) val_peek(0).obj);}
break;
case 39:
//#line 121 "calc.y"
{ /* $$ = operation($1, '/', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_DIVIDE,(Expression) val_peek(0).obj);}
break;
case 40:
//#line 122 "calc.y"
{ /* $$ = operation(null, '-', $2) */ yyval.obj = new Expression(null,Expression.OPERATOR_MINUS,(Expression) val_peek(0).obj);}
break;
case 41:
//#line 123 "calc.y"
{ /* $$ = operation($1, '^', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_XOR,(Expression) val_peek(0).obj);}
break;
case 42:
//#line 124 "calc.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 43:
//#line 125 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 44:
//#line 126 "calc.y"
{ 
					                try {
										 	/*
											 int idx = ((Double) $3).intValue();
		                            $$ = evalVariable(((LexIdent) $1).getIdname(), idx); 
										 	*/
	                            	yyval.obj = evalVariable(((LexIdent) val_peek(3).obj).getIdname(), (Expression) val_peek(1).obj); 
						             } catch (Exception ex) {
							             errorMessage = ex.toString();
										    throw(ex);
						             }
		                      }
break;
case 45:
//#line 139 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 46:
//#line 140 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 47:
//#line 142 "calc.y"
{ yyval.obj = null; }
break;
case 48:
//#line 143 "calc.y"
{ Vector v = new Vector(); 
		                        v.addElement(val_peek(0).obj);
									   yyval.obj = v;
		                      }
break;
case 49:
//#line 147 "calc.y"
{ ((Vector) val_peek(2).obj).addElement(val_peek(0).obj);
		                        yyval.obj = val_peek(2).obj;
		                      }
break;
//#line 886 "Parser.java"
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
