# only works with the Java extension of yacc: 
# byacc/j from http://troi.lincom-asg.com/~rjamison/byacc/

JFLEX  = /usr/app/jflex/bin/jflex
BYACCJ = /usr/local/bin/byacc -Jpackage=com.uniinformation.utils.excelformula
JAVAC  = javac -d /yic/v/unidev/classes-jdk1.6

# targets:

all: Parser.class

run: Parser.class
	java Parser

build: clean Parser.class

clean:
	rm -f *~ *.class *.java

Parser.class: Yylex.java Parser.java
	$(JAVAC) Yylex.java Parser.java

Yylex.java: calc.flex
	$(JFLEX) calc.flex

Parser.java: calc.y
	$(BYACCJ) calc.y
