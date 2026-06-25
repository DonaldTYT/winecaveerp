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



package com.kyoko.parser.whereclpar;



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
    0,    0,    0,    8,    9,    9,    2,    2,    2,    2,
    2,    2,    2,    2,    2,    2,    2,    2,    2,    2,
    2,    2,    2,    2,    2,    2,    3,   10,   10,   10,
   10,   10,   10,   10,   10,   10,   10,   10,   10,    1,
    1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
    1,    6,    6,    6,    6,    6,    7,    7,    5,    5,
    5,    5,    4,    4,    4,
};
final static short yylen[] = {                            2,
    1,    1,    0,    3,    5,    4,    1,    1,    3,    3,
    3,    3,    3,    3,    3,    3,    2,    2,    3,    3,
    3,    4,    5,    6,    5,    6,    4,    1,    1,    1,
    1,    1,    1,    1,    1,    1,    2,    2,    2,    1,
    1,    1,    3,    3,    3,    3,    2,    3,    3,    1,
    1,    1,    3,    4,    5,    5,    1,    3,    1,    1,
    1,    1,    0,    1,    3,
};
final static short yydefred[] = {                         0,
   40,   41,    0,   42,    7,    8,    0,    0,    0,    0,
    0,    0,    0,   50,   51,    0,    0,    0,    0,    0,
    0,   18,    0,    0,   28,   29,   30,   31,   32,   33,
   34,   35,    0,    0,    0,    0,   36,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   53,    0,    0,    0,    0,    0,   64,   62,   61,    0,
   49,   19,    0,   21,   38,   37,    0,    0,   39,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   14,
    0,    0,    0,    0,    9,   54,    0,    0,    0,    0,
    0,    0,   27,    0,   22,    0,    0,    0,    0,   56,
   55,    0,    0,    4,    0,   65,    0,    0,   23,    0,
    6,    0,   58,   24,    0,    5,
};
final static short yydgoto[] = {                         11,
   54,   55,   14,   56,   57,   15,   92,   58,   59,   45,
};
final static short yysindex[] = {                       -34,
    0,    0,   -1,    0,    0,    0,  -34,   -7,  -34,  -34,
    0,  403, -191,    0,    0,   98,  -40,  403, -191,   -7,
  -91,    0,  161,  -37,    0,    0,    0,    0,    0,    0,
    0,    0, -260, -103,   -9,   -7,    0,   -7,   -7,   -7,
   -7,   -7,  -34,  -34,   -7,  -34,  -34,  -68, -210, -206,
    0,    7, -193,  403, -191,   66,    0,    0,    0,  -15,
    0,    0, -188,    0,    0,    0,   53,   -7,    0,  -40,
  -35,  -23,  -23,  -91,  -91,  -91,  423, -164,  443,    0,
   87,  423, -164,  443,    0,    0,   38,   46,  118,   57,
   69,   -4,    0,  -40,    0,  -40,  -25,   68,   -7,    0,
    0,   58, -193,    0, -193,    0,   70,   -7,    0,   87,
    0,   59,    0,    0,   87,    0,
};
final static short yyrindex[] = {                       153,
    0,    0,    1,    0,    0,    0,    0,    0,    0,    0,
    0,  154,  155,    0,    0,    0,   72,    0,   29,    0,
   21,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  141,    0,   92,  106,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   72,
    0,  101,  121,   41,   61,   81,   74,   76,   14,    0,
   16,   94,   96,   34,    0,    0,    0,    0,    0,  -21,
    6,    0,    0,    0,    0,   72,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   36,
    0,    0,    0,    0,   56,    0,
};
final static short yygindex[] = {                         0,
  464,  149,    0,  -61,   63,  -51,    0,    0,    0,    0,
};
final static int YYTABLESIZE=723;
final static short yytable[] = {                         10,
   52,   91,   42,   62,    8,   10,   40,   39,   98,   38,
    8,   41,   63,   15,   64,   20,   40,   39,   40,   38,
   47,   41,   52,   41,   86,   61,   40,   39,   17,   38,
   70,   41,   20,   13,  107,   25,   52,    8,   17,  105,
   45,   52,   52,   52,   52,   52,   17,   52,   87,   57,
   53,  112,   88,  113,   15,   26,   20,   15,   42,   20,
   46,   47,   47,   47,   47,   47,   90,   47,   42,   17,
   42,   52,   17,   12,   13,   11,   25,   13,   42,   25,
   48,   45,   45,   45,   45,   45,   95,   45,  104,   16,
   46,   47,   96,   10,   52,   16,   26,   89,   57,   26,
   44,   46,   46,   46,   46,   46,   93,   46,  109,   94,
  114,   94,   63,   94,   12,   63,   11,   12,   47,   11,
   43,   48,   48,   48,   48,   48,  103,   48,   40,   39,
  100,   38,   59,   41,   10,   59,   16,   10,  101,   16,
   50,   44,   49,   44,   44,   44,   60,   16,   13,   60,
  111,  116,    3,    1,    2,   19,  106,   22,   24,  102,
   50,   43,   49,   43,   43,   43,   65,   66,    0,    0,
   67,    0,    0,    0,    0,   68,   69,    0,    0,    0,
   42,   52,   52,   52,   52,   52,    0,   52,    0,    0,
   51,   78,   80,    0,   83,   85,    0,    0,    0,    0,
    0,   61,   40,   39,    0,   38,    0,   41,    0,    0,
   51,    0,    0,    0,    0,    0,    0,    1,    2,   52,
    4,    5,    6,    1,    2,    3,    4,    5,    6,    0,
    0,    0,    7,    0,   52,    0,    0,    0,    7,    0,
    0,    0,    0,    9,   46,   47,    0,   99,    0,    9,
    1,    2,    3,    4,   42,    0,    0,  108,    0,    0,
    0,    0,    0,    0,   52,   52,   52,   52,   52,   52,
   52,   52,   52,   52,   52,    0,    0,    0,    0,   52,
   52,    0,   52,   52,   47,   47,   47,   47,   47,   47,
   47,   47,   47,   47,   47,   15,   15,   20,   20,   47,
   47,    0,   47,   47,   45,   45,   45,   45,   45,   45,
   45,   45,   45,   45,   45,   13,   13,   25,   25,   45,
   45,    0,   45,   45,   46,   46,   46,   46,   46,   46,
   46,   46,   46,   46,   46,    0,    0,   26,   26,   46,
   46,    0,   46,   46,   48,   48,   48,   48,   48,   48,
   48,   48,   48,   48,   48,   12,   48,   11,    0,   48,
   48,    0,   48,   48,   44,   44,   44,   44,   44,   44,
   44,   44,   44,   44,   44,   10,   48,   16,    0,   44,
   44,    0,   44,   44,   43,   43,   43,   43,   43,   43,
   43,   43,   43,   43,   43,    0,    0,    0,    0,   43,
   43,    0,   43,   43,   52,   52,   52,   52,   52,   52,
   52,   52,   52,   52,   52,    0,    0,    0,    0,   52,
   52,    0,   52,   52,   25,   26,   27,   28,   29,   30,
   31,   32,   33,   34,   35,    0,    0,    0,    0,   36,
   37,    0,   43,   44,   40,   39,    0,   38,    0,   41,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   12,   40,   39,    0,   38,    0,   41,
   18,   21,   18,   23,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   60,   40,   39,    0,   38,    0,   41,
    0,    0,    0,    0,    0,    0,   42,    0,    0,   71,
    0,   72,   73,   74,   75,   76,   77,   79,   81,   82,
   84,    0,    0,    0,    0,    0,   42,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   97,    0,    0,    0,    0,   42,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  110,    0,    0,    0,    0,    0,    0,    0,
    0,  115,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   25,   26,   27,   28,
   29,   30,   31,   32,   33,   34,   35,    0,    0,    0,
    0,   36,   37,    0,   43,   44,   25,   26,   27,   28,
   29,   30,   31,   32,   33,   34,   35,    0,    0,    0,
    0,   36,   37,    0,    0,   44,   25,   26,   27,   28,
   29,   30,   31,   32,   33,   34,   35,    0,    0,    0,
    0,   36,   37,
};
final static short yycheck[] = {                         40,
    0,   53,   94,   41,   45,   40,   42,   43,   70,   45,
   45,   47,  273,    0,  275,    0,   42,   43,   42,   45,
    0,   47,   44,   47,   93,   41,   42,   43,    0,   45,
   40,   47,   40,    0,   96,    0,   58,   45,   40,   44,
    0,   41,   42,   43,   44,   45,   40,   47,  259,   44,
   91,  103,  259,  105,   41,    0,   41,   44,   94,   44,
    0,   41,   42,   43,   44,   45,  260,   47,   94,   41,
   94,   93,   44,    0,   41,    0,   41,   44,   94,   44,
    0,   41,   42,   43,   44,   45,  275,   47,   93,   91,
  282,  283,   40,    0,   94,    0,   41,   91,   93,   44,
    0,   41,   42,   43,   44,   45,   41,   47,   41,   44,
   41,   44,   41,   44,   41,   44,   41,   44,  283,   44,
    0,   41,   42,   43,   44,   45,   58,   47,   42,   43,
   93,   45,   41,   47,   41,   44,   41,   44,   93,   44,
   43,   41,   45,   43,   44,   45,   41,   91,    0,   44,
   93,   93,    0,    0,    0,    7,   94,    9,   10,   42,
   43,   41,   45,   43,   44,   45,  270,  271,   -1,   -1,
  274,   -1,   -1,   -1,   -1,  279,  280,   -1,   -1,   -1,
   94,   41,   42,   43,   44,   45,   -1,   47,   -1,   -1,
   93,   43,   44,   -1,   46,   47,   -1,   -1,   -1,   -1,
   -1,   41,   42,   43,   -1,   45,   -1,   47,   -1,   -1,
   93,   -1,   -1,   -1,   -1,   -1,   -1,  258,  259,  260,
  261,  262,  263,  258,  259,  260,  261,  262,  263,   -1,
   -1,   -1,  273,   -1,   94,   -1,   -1,   -1,  273,   -1,
   -1,   -1,   -1,  284,  282,  283,   -1,  283,   -1,  284,
  258,  259,  260,  261,   94,   -1,   -1,  283,   -1,   -1,
   -1,   -1,   -1,   -1,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,  282,  283,  282,  283,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,  282,  283,  282,  283,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,  282,  283,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,  282,  259,  282,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,  282,  259,  282,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,  264,  265,  266,  267,  268,  269,
  270,  271,  272,  273,  274,   -1,   -1,   -1,   -1,  279,
  280,   -1,  282,  283,   42,   43,   -1,   45,   -1,   47,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,    0,   42,   43,   -1,   45,   -1,   47,
    7,    8,    9,   10,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   20,   42,   43,   -1,   45,   -1,   47,
   -1,   -1,   -1,   -1,   -1,   -1,   94,   -1,   -1,   36,
   -1,   38,   39,   40,   41,   42,   43,   44,   45,   46,
   47,   -1,   -1,   -1,   -1,   -1,   94,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   68,   -1,   -1,   -1,   -1,   94,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   99,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  108,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,  264,  265,  266,  267,
  268,  269,  270,  271,  272,  273,  274,   -1,   -1,   -1,
   -1,  279,  280,   -1,  282,  283,  264,  265,  266,  267,
  268,  269,  270,  271,  272,  273,  274,   -1,   -1,   -1,
   -1,  279,  280,   -1,   -1,  283,  264,  265,  266,  267,
  268,  269,  270,  271,  272,  273,  274,   -1,   -1,   -1,
   -1,  279,  280,
};
final static short YYFINAL=11;
final static short YYMAXTOKEN=284;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,"'('","')'","'*'","'+'","','",
"'-'",null,"'/'",null,null,null,null,null,null,null,null,null,null,"':'",null,
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
"varlist : '[' vars ']'",
"varrange : '[' variable ':' variable ']'",
"varrange : IDENT '[' '*' ']'",
"bexp : TRUE",
"bexp : FALSE",
"bexp : bexp AND bexp",
"bexp : bexp OR exp",
"bexp : exp OR bexp",
"bexp : exp OR exp",
"bexp : bexp AND exp",
"bexp : exp AND bexp",
"bexp : exp AND exp",
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
"exp : exp '+' exp",
"exp : exp '-' exp",
"exp : exp '*' exp",
"exp : exp '/' exp",
"exp : '-' exp",
"exp : exp '^' exp",
"exp : '(' exp ')'",
"exp : functexpr",
"exp : variable",
"variable : IDENT",
"variable : IDENT '[' ']'",
"variable : IDENT '[' INT ']'",
"variable : IDENT '[' '+' INT ']'",
"variable : IDENT '[' '-' INT ']'",
"vars : variable",
"vars : vars ',' variable",
"argelt : exp",
"argelt : bexp",
"argelt : varrange",
"argelt : varlist",
"arglist :",
"arglist : argelt",
"arglist : arglist ',' argelt",
};

//#line 228 "calc.y"
  private boolean fCollect = true;
  private Yylex lexer = null;
  private String errorMessage = null;
  private Object result = new Double(0);
  private VariableInterface varInterface = null;
  private FunctionInterface functInterface = null;
  private String lastInput = null;
  private int ignoreCase = 0;

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
  public Parser(int p_ignoreCase,String p_string) {
     lastInput = p_string;
	  ignoreCase = p_ignoreCase;
     //lexer = new Yylex(new StringReader(p_string+"\n"), this);
  }
  public Parser(int p_ignoreCase,String p_string, VariableInterface p_varInterface, FunctionInterface p_functInterface) {
     this(p_ignoreCase,p_string);
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
		return(new Variable(p_varname,varInterface));
  }
  public Object evalVarRange(Object o1,Object o2) throws Exception {
		return(new VariableSet((Variable) o1,(Variable) o2));
  }
  public Object evalVarList(Object o2) throws Exception {
		return(new VariableSet((List<Variable>) o2));
  }
  public Object evalVariable(String p_varname, int p_idx, boolean p_idxAbsolute) throws Exception {
	  return(new Variable(p_varname,varInterface,p_idx,p_idxAbsolute));
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
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_GT,p_y));
	     case GE:
//		     return(new Boolean(x.compareTo(y) >= 0));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_GE,p_y));
	     case LT:
//		     return(new Boolean(x.compareTo(y) < 0));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_LT,p_y));
	     case LE:
//		     return(new Boolean(x.compareTo(y) <= 0));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_LE,p_y));
	     case EQ:
//		     return(new Boolean(x.compareTo(y) == 0));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_EQ,p_y));
	     case NE:
//		     return(new Boolean(x.compareTo(y) != 0));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_NE,p_y));
	     case MATCHES:
//		     return(new Boolean(compare_matches(x.toString(), y.toString())));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_MA,p_y));
	     case LIKE:
//		     return(new Boolean(compare_like(x.toString(), y.toString())));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_LK,p_y));
	     case T_REGEXP:
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_REGEXP,p_y));
	     case NOT_LIKE:
//		     return(new Boolean(compare_like(x.toString(), y.toString())));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_NLK,p_y));
	     case NOT_MATCHES:
//		     return(new Boolean(compare_like(x.toString(), y.toString())));
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_NM,p_y));
	     case NOT_REGEXP:
		     return(new Condition(ignoreCase,p_x,Condition.COMPARE_OP_NOT_REGEXP,p_y));
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
//#line 575 "Parser.java"
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
//#line 53 "calc.y"
{
		       setResult(val_peek(0).obj);
           }
break;
case 2:
//#line 56 "calc.y"
{
		       setResult(val_peek(0).obj);
           }
break;
case 4:
//#line 62 "calc.y"
{
  					yyval.obj = evalVarList(val_peek(1).obj);
			  }
break;
case 5:
//#line 66 "calc.y"
{
  					yyval.obj = evalVarRange(val_peek(3).obj,val_peek(1).obj);
			  }
break;
case 6:
//#line 69 "calc.y"
{
		 		{
					try {
						Object v0 = evalVariable(((LexIdent) val_peek(3).obj).getIdname(), 0, true); 
						Object v1 = evalVariable(((LexIdent) val_peek(3).obj).getIdname(), Integer.MAX_VALUE, true); 
  						yyval.obj = evalVarRange(v0,v1);
					} catch (Exception ex) {
						errorMessage = ex.toString();
						throw(ex);
					}
				}
		 }
break;
case 7:
//#line 82 "calc.y"
{ /* $$ = new Boolean(true); */ yyval.obj = new Condition(true);}
break;
case 8:
//#line 83 "calc.y"
{ /* $$ = new Boolean(false); */ yyval.obj = new Condition(false);}
break;
case 9:
//#line 84 "calc.y"
{ 
/*		 		$$ = new Boolean((((Boolean) $1).booleanValue() && ((Boolean) $3).booleanValue()));*/
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_AND,(Condition) val_peek(0).obj);
				}
break;
case 10:
//#line 88 "calc.y"
{ 
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_OR,new Condition((Expression) val_peek(0).obj));
		 }
break;
case 11:
//#line 91 "calc.y"
{ 
				yyval.obj = new Condition(new Condition((Expression) val_peek(2).obj),Condition.LOGIC_OP_OR,(Condition) val_peek(0).obj);
		 }
break;
case 12:
//#line 94 "calc.y"
{ 
				yyval.obj = new Condition(new Condition((Expression) val_peek(2).obj),Condition.LOGIC_OP_OR,new Condition((Expression) val_peek(0).obj));
		 }
break;
case 13:
//#line 97 "calc.y"
{ 
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_AND,new Condition((Expression) val_peek(0).obj));
		 }
break;
case 14:
//#line 100 "calc.y"
{ 
				yyval.obj = new Condition(new Condition((Expression) val_peek(2).obj),Condition.LOGIC_OP_AND,(Condition) val_peek(0).obj);
		 }
break;
case 15:
//#line 103 "calc.y"
{ 
				yyval.obj = new Condition(new Condition((Expression) val_peek(2).obj),Condition.LOGIC_OP_AND,new Condition((Expression) val_peek(0).obj));
		 }
break;
case 16:
//#line 106 "calc.y"
{ 
/*		 		$$ = new Boolean((((Boolean) $1).booleanValue() || ((Boolean) $3).booleanValue())); */
				yyval.obj = new Condition((Condition) val_peek(2).obj,Condition.LOGIC_OP_OR,(Condition) val_peek(0).obj);
				}
break;
case 17:
//#line 110 "calc.y"
{ /* $$ = new Boolean(!(((Boolean) $2).booleanValue())); */ yyval.obj = new Condition(Condition.LOGIC_OP_NOT,(Condition) val_peek(0).obj);}
break;
case 18:
//#line 111 "calc.y"
{ /* $$ = new Boolean(!(((Boolean) $2).booleanValue())); */ yyval.obj = new Condition(Condition.LOGIC_OP_NOT,(Condition) val_peek(0).obj);}
break;
case 19:
//#line 112 "calc.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 20:
//#line 113 "calc.y"
{ yyval.obj = comparison((Expression) val_peek(2).obj, val_peek(1).ival, (Expression) val_peek(0).obj); }
break;
case 21:
//#line 114 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(2).obj,Condition.COMPARE_OP_IS_NULL);}
break;
case 22:
//#line 115 "calc.y"
{ yyval.obj = new Condition((Expression) val_peek(3).obj,Condition.COMPARE_OP_IS_NOT_NULL);}
break;
case 23:
//#line 116 "calc.y"
{ yyval.obj = new Condition(ignoreCase,(Expression) val_peek(4).obj,Condition.COMPARE_OP_IN_ITEMLIST,(List) val_peek(1).obj);}
break;
case 24:
//#line 117 "calc.y"
{ yyval.obj = new Condition(ignoreCase,(Expression) val_peek(5).obj,Condition.COMPARE_OP_NOTIN_ITEMLIST,(List) val_peek(1).obj);}
break;
case 25:
//#line 118 "calc.y"
{ yyval.obj = new Condition(ignoreCase,(Expression) val_peek(4).obj,Condition.COMPARE_OP_BETWEEN,(Expression) val_peek(2).obj,(Expression) val_peek(0).obj);}
break;
case 26:
//#line 119 "calc.y"
{ yyval.obj = new Condition(ignoreCase,(Expression) val_peek(5).obj,Condition.COMPARE_OP_NOT_BETWEEN,(Expression) val_peek(2).obj,(Expression) val_peek(0).obj);}
break;
case 27:
//#line 121 "calc.y"
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
case 28:
//#line 133 "calc.y"
{ yyval.ival = GT; }
break;
case 29:
//#line 134 "calc.y"
{ yyval.ival = GE; }
break;
case 30:
//#line 135 "calc.y"
{ yyval.ival = LT; }
break;
case 31:
//#line 136 "calc.y"
{ yyval.ival = LE; }
break;
case 32:
//#line 137 "calc.y"
{ yyval.ival = EQ; }
break;
case 33:
//#line 138 "calc.y"
{ yyval.ival = NE; }
break;
case 34:
//#line 139 "calc.y"
{ yyval.ival = MATCHES; }
break;
case 35:
//#line 140 "calc.y"
{ yyval.ival = LIKE; }
break;
case 36:
//#line 141 "calc.y"
{ yyval.ival = T_REGEXP; }
break;
case 37:
//#line 142 "calc.y"
{yyval.ival = NOT_LIKE;}
break;
case 38:
//#line 143 "calc.y"
{yyval.ival = NOT_MATCHES;}
break;
case 39:
//#line 144 "calc.y"
{yyval.ival = NOT_REGEXP;}
break;
case 40:
//#line 147 "calc.y"
{ /* $$ = new Double($1); */ yyval.obj = new Expression(val_peek(0).dval); }
break;
case 41:
//#line 148 "calc.y"
{ /* $$ = new Integer($1); */ yyval.obj = new Expression(val_peek(0).ival); }
break;
case 42:
//#line 149 "calc.y"
{ /* $$ = $1; */ yyval.obj = new Expression(val_peek(0).sval);}
break;
case 43:
//#line 150 "calc.y"
{ /* $$ = operation($1, '+', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_PLUS,(Expression) val_peek(0).obj);}
break;
case 44:
//#line 151 "calc.y"
{ /* $$ = operation($1, '-', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_MINUS,(Expression) val_peek(0).obj);}
break;
case 45:
//#line 152 "calc.y"
{ /* $$ = operation($1, '*', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_MULTIPLY,(Expression) val_peek(0).obj);}
break;
case 46:
//#line 153 "calc.y"
{ /* $$ = operation($1, '/', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_DIVIDE,(Expression) val_peek(0).obj);}
break;
case 47:
//#line 154 "calc.y"
{ /* $$ = operation(null, '-', $2) */ yyval.obj = new Expression(null,Expression.OPERATOR_MINUS,(Expression) val_peek(0).obj);}
break;
case 48:
//#line 155 "calc.y"
{ /* $$ = operation($1, '^', $3); */ yyval.obj = new Expression((Expression) val_peek(2).obj,Expression.OPERATOR_XOR,(Expression) val_peek(0).obj);}
break;
case 49:
//#line 156 "calc.y"
{ yyval.obj = val_peek(1).obj; }
break;
case 50:
//#line 157 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 51:
//#line 158 "calc.y"
{ yyval.obj = new Expression ((Variable) val_peek(0).obj); }
break;
case 52:
//#line 160 "calc.y"
{ 
					   try {
		               yyval.obj = evalVariable(((LexIdent) val_peek(0).obj).getIdname()); 
						} catch (Exception ex) {
							errorMessage = ex.toString();
							throw(ex);
						}
					 }
break;
case 53:
//#line 169 "calc.y"
{
					              try {
	                            	yyval.obj = evalVariable(((LexIdent) val_peek(2).obj).getIdname(), Integer.MAX_VALUE, true); 
						             } catch (Exception ex) {
							             errorMessage = ex.toString();
										    throw(ex);
						             }
		                      }
break;
case 54:
//#line 177 "calc.y"
{ 
					                try {
	                            	yyval.obj = evalVariable(((LexIdent) val_peek(3).obj).getIdname(), val_peek(1).ival, true); 
						             } catch (Exception ex) {
							             errorMessage = ex.toString();
										    throw(ex);
						             }
		                      }
break;
case 55:
//#line 185 "calc.y"
{ 
					                try {
	                            	yyval.obj = evalVariable(((LexIdent) val_peek(4).obj).getIdname(), val_peek(1).ival, false); 
						             } catch (Exception ex) {
							             errorMessage = ex.toString();
										    throw(ex);
						             }
		                      }
break;
case 56:
//#line 193 "calc.y"
{ 
					                try {
	                            	yyval.obj = evalVariable(((LexIdent) val_peek(4).obj).getIdname(), -val_peek(1).ival, false); 
						             } catch (Exception ex) {
							             errorMessage = ex.toString();
										    throw(ex);
						             }
		                      }
break;
case 57:
//#line 203 "calc.y"
{
						Vector v = new Vector(); 
		            v.addElement(val_peek(0).obj);
						yyval.obj = v;
					}
break;
case 58:
//#line 208 "calc.y"
{ 
										((Vector) val_peek(2).obj).addElement(val_peek(0).obj);
		                        yyval.obj = val_peek(2).obj;
		                       }
break;
case 59:
//#line 213 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 60:
//#line 214 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 61:
//#line 215 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 62:
//#line 216 "calc.y"
{ yyval.obj = val_peek(0).obj; }
break;
case 63:
//#line 218 "calc.y"
{ yyval.obj = null; }
break;
case 64:
//#line 219 "calc.y"
{ Vector v = new Vector(); 
		                        v.addElement(val_peek(0).obj);
									   yyval.obj = v;
		                      }
break;
case 65:
//#line 223 "calc.y"
{ ((Vector) val_peek(2).obj).addElement(val_peek(0).obj);
		                        yyval.obj = val_peek(2).obj;
		                      }
break;
//#line 1075 "Parser.java"
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
