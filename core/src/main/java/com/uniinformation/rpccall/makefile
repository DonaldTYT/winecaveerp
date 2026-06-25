#JAVAC=/usr/local/jdk/bin/javac
#RMIC=/usr/local/jdk/bin/rmic
#JAREXE=/usr/local/jdk/bin/jar

CLASSDIR=/yic/v/unidev/classes
PACKAGE=/com/uniinformation

JAVAC=javac -classpath . -d $(CLASSDIR)
OBJDIR=$(CLASSDIR)$(PACKAGE)/rpccall
#JAVAC=javac -classpath $(CLASSDIR):$(CLASSDIR)/com/uniinformation/rpccall \
#	-d $(CLASSDIR) \
# -sourcepath /yic/v/unidev/com/src/uniinformation/rpccall \
#	-verbose
#JAVAC=javac -classpath . -verbose
RMIC=rmic
JAREXE=jar
.SUFFIXES= .class .java .jar
%.class : %.java
	$(JAVAC) $<

OBJS=	\
	$(OBJDIR)/Valuable.class  \
	$(OBJDIR)/Value.class  \
	$(OBJDIR)/Dateval.class \
	$(OBJDIR)/Longval.class \
	$(OBJDIR)/Realval.class \
	$(OBJDIR)/Strval.class \
	$(OBJDIR)/RpcException.class \
	$(OBJDIR)/Rpccall.class \
	$(OBJDIR)/RpcConnServlet.class \
	$(OBJDIR)/RpcServletClass.class \
	$(OBJDIR)/RpcServlet.class \
	$(OBJDIR)/RpcRegistry.class \
	$(OBJDIR)/RpcServletProvider.class \
	$(OBJDIR)/RpcServerConnection.class \
	$(OBJDIR)/RpcConst.class \
	$(OBJDIR)/RpcClient.class \
	$(OBJDIR)/RpcServer.class 

JARFILE=rpccall.jar

all :	$(OBJS)
#all :	$(JARFILE)
	@echo "made"
test:  TestRpccall.class 
	@echo "Testing program made"

$(JARFILE) : $(OBJS)
	$(JAREXE) cvf $(JARFILE) $(OBJS)

TestRpccall.class: TestRpccall.java RpcLib.java Rpccall.java
TestRpcServer.class: TestRpcServer.java RpcServer.java RpcServerConnection.java

Dateval.class: Value.java
Longval.class: Value.java
Realval.class: Value.java
Strval.class: Value.java

Uname.class TestCallcount.class : RpcServlet.java
RpcRegistry.class : RpcServletProvider.java
