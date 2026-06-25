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



package com.kyoko.parser.graphql;



//#line 2 "GraphQL.y"
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
public final static short QUERY=258;
public final static short MUTATION=259;
public final static short SUBSCRIPTION=260;
public final static short TYPE=261;
public final static short INPUT=262;
public final static short SCHEMA=263;
public final static short IDENT=264;
public final static short FLOAT=265;
public final static short INT=266;
public final static short STRING=267;
public final static short SCALER=268;
public final static short TRUE=269;
public final static short FALSE=270;
public final static short DOLLAR=271;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    0,    0,    1,    1,    1,    2,    2,    2,    2,
    3,    3,    3,    4,    4,    6,    6,    6,    7,    7,
    7,    7,    5,    5,    5,    5,    5,
};
final static short yylen[] = {                            2,
    4,    5,    8,    0,    1,    2,    1,    4,    4,    7,
    0,    1,    2,    3,    4,    0,    1,    2,    4,    5,
    6,    7,    1,    1,    1,    1,    1,
};
final static short yydefred[] = {                         0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    5,
    0,    0,    0,   17,    0,    0,    1,    6,    2,    0,
    0,   18,    0,    0,    0,   12,    0,    0,    9,    0,
    0,   13,    0,    0,   25,   24,   23,   26,   27,    0,
   14,    0,    0,    0,    3,   15,    0,    0,   21,   10,
   22,
};
final static short yydgoto[] = {                          3,
    9,   10,   25,   26,   41,   13,   14,
};
final static short yysindex[] = {                      -242,
 -247,  -91,    0,  -31, -229, -229,    1,  -30, -123,    0,
 -122, -226,  -21,    0, -229, -225,    0,    0,    0,  -14,
  -83,    0, -121,  -13,  -40,    0, -227, -229,    0,  -36,
  -80,    0,  -19, -120,    0,    0,    0,    0,    0, -218,
    0, -229,  -11, -239,    0,    0, -119, -239,    0,    0,
    0,
};
final static short yyrindex[] = {                         0,
    0,    0,    0,    0,  -78,  -78,   10, -118,    0,    0,
    0,    0,    0,    0,  -78,   11,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,  -78,    0,    0,
 -117,    0,  -18,    0,    0,    0,    0,    0,    0,    0,
    0,  -78,  -17,    0,    0,    0,    0,    0,    0,    0,
    0,
};
final static short yygindex[] = {                         0,
    6,    2,    0,   28,  -15,    0,   41,
};
final static int YYTABLESIZE=234;
final static short yytable[] = {                         40,
   31,   17,   19,   29,   45,   50,    7,    8,    7,   16,
   18,   11,   18,   43,   12,    1,    4,   19,   20,   21,
   23,    2,   19,   20,   18,   35,   36,   37,   49,   38,
   39,    5,   51,   34,    8,   18,   12,   20,   24,   28,
   33,   44,   42,   27,   30,   46,    4,   47,   18,   48,
   16,   11,   32,   22,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    6,   15,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    8,    8,    8,    8,    8,    7,    8,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   24,    0,    0,    0,    0,   35,   36,
   37,    0,   38,   39,
};
final static short yycheck[] = {                         36,
   41,  125,  125,  125,  125,  125,  125,  125,   40,   40,
    9,    6,   11,   33,   36,  258,  264,   36,   36,   41,
   15,  264,   41,   41,   23,  265,  266,  267,   44,  269,
  270,  123,   48,   28,  264,   34,   36,  264,  264,  123,
  268,   61,  123,   58,   58,  264,  125,   42,   47,   61,
   41,   41,   25,   13,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,  123,  123,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  264,  264,  264,  264,  264,  264,  264,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  264,   -1,   -1,   -1,   -1,  265,  266,
  267,   -1,  269,  270,
};
final static short YYFINAL=3;
final static short YYMAXTOKEN=271;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"'!'",null,null,"'$'",null,null,null,"'('","')'",null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,"':'",null,
null,"'='",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
"'{'",null,"'}'",null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,"NL","QUERY","MUTATION","SUBSCRIPTION",
"TYPE","INPUT","SCHEMA","IDENT","FLOAT","INT","STRING","SCALER","TRUE","FALSE",
"DOLLAR",
};
final static String yyrule[] = {
"$accept : line",
"line : IDENT '{' selection_set '}'",
"line : QUERY IDENT '{' selection_set '}'",
"line : QUERY IDENT '(' variable_list ')' '{' selection_set '}'",
"selection_set :",
"selection_set : selection",
"selection_set : selection_set selection",
"selection : IDENT",
"selection : IDENT '(' argument_list ')'",
"selection : IDENT '{' selection_set '}'",
"selection : IDENT '(' argument_list ')' '{' selection_set '}'",
"argument_list :",
"argument_list : argument",
"argument_list : argument_list argument",
"argument : IDENT ':' value",
"argument : IDENT ':' '$' IDENT",
"variable_list :",
"variable_list : variable",
"variable_list : variable_list variable",
"variable : '$' IDENT ':' SCALER",
"variable : '$' IDENT ':' SCALER '!'",
"variable : '$' IDENT ':' SCALER '=' value",
"variable : '$' IDENT ':' SCALER '!' '=' value",
"value : STRING",
"value : INT",
"value : FLOAT",
"value : TRUE",
"value : FALSE",
};

//#line 150 "GraphQL.y"
  private boolean fCollect = true;
  private Yylex lexer = null;
  private String errorMessage = null;
  private String lastInput = null;
  private Object result = null;

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
  public int getErrCnt()
  {
  		return(yynerrs);
  }
  public int getErrPos() {
		return(lexer.getCurrentPos());
  }

  public class GraphQlSelection {
  		String name;
		Vector arguments;
		Vector selections;
		public GraphQlSelection(String p_name) {
			name = p_name;
		}
		public GraphQlSelection(String p_name,Vector p_arguments,Vector p_selections) {
			name = p_name;
			arguments = p_arguments;
			selections = p_selections;
		}
  }
  public class GraphQlArgument {
  		String name;
		Object value;
		String varname;
  		public GraphQlArgument(String p_name,Object p_value,String p_varname) {
			name = p_name;
			value = p_value;
			varname = p_varname;
		}
  }
  public class GraphQlVariable{
  		String name;
		String type;
		boolean nonull;
		Object defaultValue;
  		public GraphQlVariable(String p_name,String p_type,boolean p_nonull,Object p_default) {
			name = p_name;
			type = p_type;
			nonull = p_nonull;
			defaultValue = p_default;
		}
  }
  public class GraphQlQuery{
  		int otype;
		String alias;
		Vector selections;
		Vector variables;
  		public GraphQlQuery(int p_type,String p_alias,Vector p_selections, Vector p_variables) {
			otype = p_type;	
			alias = p_alias;
			selections = p_selections;
			variables = p_variables;
		}
		public String toString() {
			return("GraphQlOp:"+otype);
		}
  }
//#line 382 "Parser.java"
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
//#line 35 "GraphQL.y"
{
			CoreLog.log("Default query operation parsed");
			setResult(new GraphQlQuery(0,((LexIdent) val_peek(3).obj).getIdname(),(Vector) val_peek(1).obj,null));
		}
break;
case 2:
//#line 39 "GraphQL.y"
{
			CoreLog.log("Query operation parsed");
			setResult(new GraphQlQuery(0,((LexIdent) val_peek(3).obj).getIdname(),(Vector) val_peek(1).obj,null));
		}
break;
case 3:
//#line 43 "GraphQL.y"
{
			CoreLog.log("Query operation parsed");
			setResult(new GraphQlQuery(0,((LexIdent) val_peek(6).obj).getIdname(),(Vector) val_peek(1).obj,(Vector) val_peek(4).obj));
		}
break;
case 4:
//#line 49 "GraphQL.y"
{
	  		yyval.obj = null;
	  	}
break;
case 5:
//#line 52 "GraphQL.y"
{
			Vector v = new Vector();
			v.addElement(val_peek(0).obj);
			yyval.obj = v;
		}
break;
case 6:
//#line 57 "GraphQL.y"
{
			((Vector) val_peek(1).obj).addElement(val_peek(0).obj);
			yyval.obj = val_peek(1).obj;
		}
break;
case 7:
//#line 64 "GraphQL.y"
{
			yyval.obj = new GraphQlSelection(((LexIdent) val_peek(0).obj).getIdname(),null,null);
		}
break;
case 8:
//#line 67 "GraphQL.y"
{
			yyval.obj = new GraphQlSelection(((LexIdent) val_peek(3).obj).getIdname(),(Vector) val_peek(1).obj,null);
		}
break;
case 9:
//#line 70 "GraphQL.y"
{
			yyval.obj = new GraphQlSelection(((LexIdent) val_peek(3).obj).getIdname(),null,(Vector) val_peek(1).obj);
		}
break;
case 10:
//#line 73 "GraphQL.y"
{
			yyval.obj = new GraphQlSelection(((LexIdent) val_peek(6).obj).getIdname(),(Vector) val_peek(4).obj,(Vector) val_peek(1).obj);
		}
break;
case 11:
//#line 79 "GraphQL.y"
{
	  		yyval.obj = null;
	  	}
break;
case 12:
//#line 82 "GraphQL.y"
{
			Vector v = new Vector();
			v.addElement(val_peek(0).obj);
			yyval.obj = v;
		}
break;
case 13:
//#line 87 "GraphQL.y"
{
			((Vector) val_peek(1).obj).addElement(val_peek(0).obj);
			yyval.obj = val_peek(1).obj;
		}
break;
case 14:
//#line 94 "GraphQL.y"
{
			yyval.obj = new GraphQlArgument(((LexIdent) val_peek(2).obj).getIdname(),val_peek(0).obj,null);
		}
break;
case 15:
//#line 97 "GraphQL.y"
{
			yyval.obj = new GraphQlArgument(((LexIdent) val_peek(3).obj).getIdname(),null,((LexIdent) val_peek(0).obj).getIdname());
		}
break;
case 16:
//#line 103 "GraphQL.y"
{
	  		yyval.obj = null;
	  	}
break;
case 17:
//#line 106 "GraphQL.y"
{
			Vector v = new Vector();
			v.addElement(val_peek(0).obj);
			yyval.obj = v;
		}
break;
case 18:
//#line 111 "GraphQL.y"
{
			((Vector) val_peek(1).obj).addElement(val_peek(0).obj);
			yyval.obj = val_peek(1).obj;
		}
break;
case 19:
//#line 118 "GraphQL.y"
{
			yyval.obj = new GraphQlVariable(((LexIdent) val_peek(2).obj).getIdname(),((LexIdent) val_peek(0).obj).getIdname(),false,null);
		}
break;
case 20:
//#line 121 "GraphQL.y"
{
			yyval.obj = new GraphQlVariable(((LexIdent) val_peek(3).obj).getIdname(),((LexIdent) val_peek(1).obj).getIdname(),true,null);
		}
break;
case 21:
//#line 124 "GraphQL.y"
{
			yyval.obj = new GraphQlVariable(((LexIdent) val_peek(4).obj).getIdname(),((LexIdent) val_peek(2).obj).getIdname(),false,val_peek(0).obj);
		}
break;
case 22:
//#line 127 "GraphQL.y"
{
			yyval.obj = new GraphQlVariable(((LexIdent) val_peek(5).obj).getIdname(),((LexIdent) val_peek(3).obj).getIdname(),false,val_peek(0).obj);
		}
break;
case 23:
//#line 133 "GraphQL.y"
{
		yyval.obj = val_peek(0).sval;
	}
break;
case 24:
//#line 136 "GraphQL.y"
{
		yyval.obj = new Integer(val_peek(0).ival);
	}
break;
case 25:
//#line 139 "GraphQL.y"
{
		yyval.obj = new Float(val_peek(0).dval);
	}
break;
case 26:
//#line 142 "GraphQL.y"
{
		yyval.obj = new Boolean(true);
	}
break;
case 27:
//#line 145 "GraphQL.y"
{
		yyval.obj = new Boolean(false);
	}
break;
//#line 706 "Parser.java"
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
