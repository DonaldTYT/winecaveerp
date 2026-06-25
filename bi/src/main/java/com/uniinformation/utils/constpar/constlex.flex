package com.uniinformation.utils.constpar;
%%

%byaccj
%unicode

%{
  private Parser yyparser;

  public Yylex(java.io.Reader r, Parser yyparser) {
    this(r);
    this.yyparser = yyparser;
  }
public void echo() {
}
/*
main()
{
   int token;

   stmtchk = 1;
   for (;;) {
      switch (token = yylex()) {
         case COLON : fprintf(stderr, "<- COLON\n");
		      break;
         case LE    : fprintf(stderr, "<- LE\n");
		      break;
         case LT    : fprintf(stderr, "<- LT\n");
		      break;
         case GE    : fprintf(stderr, "<- GE\n");
		      break;
         case GT    : fprintf(stderr, "<- GT\n");
		      break;
         case EQ    : fprintf(stderr, "<- EQ\n");
		      break;
         case NE    : fprintf(stderr, "<- NE\n");
		      break;
         case OR    : fprintf(stderr, "<- OR\n");
		      break;
         case AND   : fprintf(stderr, "<- AND\n");
		      break;
         case STRING: fprintf(stderr, "<- STRING\n");
		      break;
         default    : if (token <= 0)
			 return;
		      fprintf(stderr, "token = %d <- CHAR\n", token);
		      break;
      }
   }
}
*/
%}

%%

":"             { echo(); return(Parser.COLON); }
"<"             { echo(); return(Parser.LT); }
"<="            { echo(); return(Parser.LE); }
">"             { echo(); return(Parser.GT); }
">="            { echo(); return(Parser.GE); }
"="             { echo(); return(Parser.EQ); }
"=="            { echo(); return(Parser.EQ); }
"<>"            { echo(); return(Parser.NE); }
"!="            { echo(); return(Parser.NE); }
"|"             { echo(); return(Parser.OR); }
"&"             { echo(); return(Parser.AND); }
[^:<>=!|&]+      
      { echo(); 
		  yyparser.yylval = new ParserVal(yytext().trim());
		  return(Parser.STRING); 
		}
.     { echo(); 
        return((int) yycharat(0));
		}
