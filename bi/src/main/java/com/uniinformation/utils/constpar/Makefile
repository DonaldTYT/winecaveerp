JFLEX  = jflex 
BYACCJ = byacc -J
JAVAC  = javac

# targets:

all: Parser.java ParserVal.java Yylex.java ParserVal.java

Yylex.java: constlex.flex 
	$(JFLEX) constlex.flex

Parser.java: constpar.y
	$(BYACCJ) -Jpackage=com.uniinformation.utils.constpar constpar.y
