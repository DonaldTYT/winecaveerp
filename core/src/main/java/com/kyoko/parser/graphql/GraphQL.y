%{
  import java.io.*;
  import java.util.*;
  import com.uniinformation.utils.*;
  import com.kyoko.parser.*;
%}
      
%token NL          /* newline  */
%token QUERY
%token MUTATION
%token SUBSCRIPTION
%token TYPE
%token INPUT
%token SCHEMA
%token <obj> IDENT
%token <dval> FLOAT /* a float */
%token <ival> INT   /* a integer */
%token <sval> STRING
%token <obj> SCALER
%type  <obj> line
%type  <obj> selection_set
%type  <obj> selection
%type  <obj> argument_list
%type  <obj> argument
%type  <obj> value
%type  <obj> variable_list
%type  <obj> variable
%token TRUE        
%token FALSE       
%token DOLLAR


      
%%
line: IDENT '{' selection_set '}' {
			UniLog.log("Default query operation parsed");
			setResult(new GraphQlQuery(0,((LexIdent) $1).getIdname(),(Vector) $3,null));
		} 
		| QUERY IDENT '{' selection_set '}' {
			UniLog.log("Query operation parsed");
			setResult(new GraphQlQuery(0,((LexIdent) $2).getIdname(),(Vector) $4,null));
		}
		| QUERY IDENT '(' variable_list ')' '{' selection_set '}' {
			UniLog.log("Query operation parsed");
			setResult(new GraphQlQuery(0,((LexIdent) $2).getIdname(),(Vector) $7,(Vector) $4));
		}
		;
selection_set:
	  	{
	  		$$ = null;
	  	}
	| selection {
			Vector v = new Vector();
			v.addElement($1);
			$$ = v;
		}
	| selection_set selection {
			((Vector) $1).addElement($2);
			$$ = $1;
		}
	;

selection:
		IDENT {
			$$ = new GraphQlSelection(((LexIdent) $1).getIdname(),null,null);
		}
		| IDENT '(' argument_list ')' {
			$$ = new GraphQlSelection(((LexIdent) $1).getIdname(),(Vector) $3,null);
		}
		| IDENT '{' selection_set '}' {
			$$ = new GraphQlSelection(((LexIdent) $1).getIdname(),null,(Vector) $3);
		}
		| IDENT '(' argument_list ')' '{' selection_set '}' {
			$$ = new GraphQlSelection(((LexIdent) $1).getIdname(),(Vector) $3,(Vector) $6);
		}
	;

argument_list:
	  	{
	  		$$ = null;
	  	}
	| argument {
			Vector v = new Vector();
			v.addElement($1);
			$$ = v;
		}
	| argument_list argument {
			((Vector) $1).addElement($2);
			$$ = $1;
		}
	;

argument:
		IDENT ':' value {
			$$ = new GraphQlArgument(((LexIdent) $1).getIdname(),$3,null);
		}
		| IDENT ':' '$' IDENT {
			$$ = new GraphQlArgument(((LexIdent) $1).getIdname(),null,((LexIdent) $4).getIdname());
		}
	;

variable_list:
	  	{
	  		$$ = null;
	  	}
	| variable {
			Vector v = new Vector();
			v.addElement($1);
			$$ = v;
		}
	| variable_list variable {
			((Vector) $1).addElement($2);
			$$ = $1;
		}
	;

variable:
		'$' IDENT ':' SCALER {
			$$ = new GraphQlVariable(((LexIdent) $2).getIdname(),((LexIdent) $4).getIdname(),false,null);
		}
		| '$' IDENT ':' SCALER '!' {
			$$ = new GraphQlVariable(((LexIdent) $2).getIdname(),((LexIdent) $4).getIdname(),true,null);
		}
		| '$' IDENT ':' SCALER '=' value {
			$$ = new GraphQlVariable(((LexIdent) $2).getIdname(),((LexIdent) $4).getIdname(),false,$6);
		}
		| '$' IDENT ':' SCALER '!' '=' value {
			$$ = new GraphQlVariable(((LexIdent) $2).getIdname(),((LexIdent) $4).getIdname(),false,$7);
		}
	;

value:
	STRING {
		$$ = $1;
	}
	| INT {
		$$ = new Integer($1);
	}
	| FLOAT {
		$$ = new Float($1);
	}
	| TRUE {
		$$ = new Boolean(true);
	}
	| FALSE {
		$$ = new Boolean(false);
	}
	;
%%
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
