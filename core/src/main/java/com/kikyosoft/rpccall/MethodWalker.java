package com.kikyosoft.rpccall;

import java.lang.reflect.*;
import java.util.*;

import com.kikyosoft.utils.LogUtil;
public class MethodWalker
{
	class Node
	{
		int type;
		Vector childlist = new Vector();
		String methodname;
		Method method;

		public Node(Method p_method)
		{
			method= p_method;
			type = METHOD_TERMINATION;
		}
		public Node(String p_methodname)
		{
			methodname = p_methodname;
		}
		public Node(int p_nodetype)
		{
			type = p_nodetype;
		}
		public void addChild(Node p_node)
		{
			childlist.addElement(p_node);
		}
	}

	static final int METHOD_UNKNOWN_TYPE = -1;
	static final int METHOD_STRING_TYPE = 0;
	static final int METHOD_NUMERIC_TYPE = 1;
	static final int METHOD_DATE_TYPE = 2;
	static final int METHOD_VECTOR_TYPE = 3;
	static final int METHOD_TERMINATION = 4;
	static final int METHOD_BYTEARRAY_TYPE = 5;
	static final int METHOD_BOOLEAN_TYPE = 6;

	//Method methods[];

	int getNodeType(String className)
	{
		if(className.equals("java.lang.String")) {
			return(METHOD_STRING_TYPE);
		} else if(className.equals("int") ||
				className.equals("java.lang.Integer") ||
				className.equals("double") ||
				className.equals("java.lang.Double")) {
			return(METHOD_NUMERIC_TYPE);
		} else if(className.equals("java.util.Vector")) {
			return(METHOD_VECTOR_TYPE);
		} else if(className.equals("java.util.Date")) {
			return(METHOD_DATE_TYPE);
		} else if(className.equals("[B")) { /* for byte[] */
				/* "[Ljava.lang.Byte;" for Byte[] */
			return(METHOD_BYTEARRAY_TYPE);
		} else if(className.equals("java.lang.Boolean") ||
			className.equals("boolean")) {
			LogUtil.log("getNodeTpye() Boolean return String type");
			return(METHOD_STRING_TYPE);
			// return(METHOD_BOOLEAN_TYPE);
		}
		LogUtil.log("getNodeType unknown "  + className);
		return(METHOD_UNKNOWN_TYPE);
	}

	void printType(Node p_node,int level)
	{
			for(int i=0;i<level;i++)
				System.out.print("  ");
			switch(p_node.type) {
			case METHOD_STRING_TYPE:
				System.out.print("String");
				break;
			case METHOD_NUMERIC_TYPE:
				System.out.print("Numeric");
				break;
			case METHOD_DATE_TYPE:
				System.out.print("Date");
				break;
			case METHOD_BYTEARRAY_TYPE:
				System.out.print("ByteArray");
				break;
			case METHOD_VECTOR_TYPE:
				System.out.print("Vector");
				break;
			case METHOD_TERMINATION:
				System.out.println("Void");
				break;
			case METHOD_UNKNOWN_TYPE:
				System.out.println("UnKnown");
				break;
			default:
				System.out.print("NodeType" + p_node.type);
				break;
			}
	}

	public void printMethodTree()
	{
		printMethodTree(methodNode,0);
	}
	void printMethodTree(Node p_node,int level)
	{
		if(p_node == null)
			return;
		if(p_node.methodname != null)
			System.out.println(p_node.methodname);
		else
			printType(p_node,level+1);
		for(int i=0;i<p_node.childlist.size();i++) {
			Node child = (Node) p_node.childlist.elementAt(i);
			printMethodTree(child,level+1);
		}
	}

	int searchMethodNode(Node p_node, int p_nodeType)
	{
		for(int i=0;i<p_node.childlist.size();i++) {
			Node node = (Node)p_node.childlist.elementAt(i);
			if(node.type == p_nodeType)
				return(i);
		}
		return(-1);
	}

	Method searchMethodTree(Object parameterType[])
	{
		Node lastnode,node;
		if(methodNode == null)
			return(null);
		lastnode = methodNode;
		node = null;
		int nodeidx;
		Node []travel = new Node[maxlevel];
		for(int i=0;i<parameterType.length;i++) {
			Object parameter = parameterType[i];
			String className = parameter.getClass().getName();
			int nodeType = getNodeType(className);
			travel[i] = lastnode;
			nodeidx = searchMethodNode(lastnode, nodeType);
			if(nodeidx < 0) {
				for(;i>=0;i--) {
					nodeidx = searchMethodNode(travel[i],METHOD_VECTOR_TYPE);
					if(nodeidx >= 0) {
						lastnode = (Node)travel[i].childlist.elementAt(nodeidx);
						break;
					}
				}
				if(nodeidx < 0) {
					return null;
				}
				/*
				nodeidx = searchMethodNode(lastnode,METHOD_VECTOR_TYPE);
				if(nodeidx < 0) {
					return null;
				}
				lastnode = (Node)lastnode.childlist.elementAt(nodeidx);
				*/
				break;
			} else {
				lastnode = (Node)lastnode.childlist.elementAt(nodeidx);
			}
		}

		nodeidx = searchMethodNode(lastnode,METHOD_TERMINATION);
		if(nodeidx < 0) {
			/* vector size = 0 */
			nodeidx = searchMethodNode(lastnode,METHOD_VECTOR_TYPE);
			if(nodeidx < 0) {
				return null;
			}
			lastnode = (Node)lastnode.childlist.elementAt(nodeidx);
			nodeidx = searchMethodNode(lastnode,METHOD_TERMINATION);
			if(nodeidx < 0) {
				return null;
			}
		}
		node = (Node)lastnode.childlist.elementAt(nodeidx);
		return (node.method);
	}
	
	void buildMethodTree(String methodName,Method method)
		throws ClassNotFoundException
	{
		Node lastnode,node;
		lastnode = methodNode;
		node = null;
		int nodeidx;
		int dirty = 0;
		Class parameterType[] = method.getParameterTypes();
		if(maxlevel < parameterType.length)
			maxlevel = parameterType.length;
		for(int i=0;i<parameterType.length;i++) {
			Class parameter = parameterType[i];
			String className = parameter.getName();
			int nodeType = getNodeType(className);
			nodeidx = searchMethodNode(lastnode,nodeType);
			if(nodeidx < 0) {
				node = new Node(nodeType);
				dirty++;
				lastnode.addChild(node);
				lastnode = node;
			} else  {
				lastnode = (Node)lastnode.childlist.elementAt(nodeidx);
			}
		}
		if(dirty == 0 && parameterType.length >0) {
			LogUtil.log("Warning !!!: Ambigous method " + method + " rejected");
			return;
		}
		lastnode.addChild(new Node(method)); /* Termination */
	}

	Node methodNode = null;
	int maxlevel = 0;

	public MethodWalker(Method [] p_methods, String p_methodname)
		throws Exception
	{
		
		//Method[] methods = p_methods; //old logic
		Method[] methods = MethodWalker.sortMethods(p_methods); //TODO: not yet test. For fix jdk 8 method ambigous method problem
		for(int i=0;i<methods.length;i++) {
			Method method = methods[i];
			if(method.getName().equals(p_methodname)) {
				if(methodNode == null)
					methodNode = new Node(p_methodname);
				buildMethodTree(p_methodname,method);
			}
		}
		
	}

	Object [] transformParameters(Method m,Object parameters[])
	{
		String paraClassName;
		// CoreLog.log("start transfromParameters getParameterTypes()");
		Class ptype[] = m.getParameterTypes();
		// CoreLog.log("end transfromParameters getParameterTypes()");
		Object ret[] = new Object[ptype.length];
		for(int i=0;i<ptype.length;i++) {
			Class cl = ptype[i];
			if(parameters.length > i)
				ret[i] = parameters[i];
			/* else will be a vector */
			String funcClassName = cl.getName();
			if(funcClassName.equals("int") || funcClassName.equals("java.lang.Integer")) {
				Object parameter = parameters[i];
				paraClassName = parameter.getClass().getName();
				if(paraClassName.equals("java.lang.Integer") == false) {
					Double d = (Double)parameter;
					ret[i] = new Integer(d.intValue());
				}
			} else if(funcClassName.equals("double") || funcClassName.equals("java.lang.Double")) {
				Object parameter = parameters[i];
				paraClassName = parameter.getClass().getName();
				if(paraClassName.equals("java.lang.Double") == false) {
					Integer d = (Integer)parameter;
					ret[i] = new Double(d.doubleValue());
				}
			} else if(funcClassName.equals("boolean") || funcClassName.equals("java.lang.Boolean")) {
				Object parameter = parameters[i];
				paraClassName = parameter.getClass().getName();
				if(paraClassName.equals("java.lang.String")) {
					ret[i] = new Boolean(parameter.equals("Y"));
				}
			} else if(funcClassName.equals("java.util.Vector")) {
				Vector v = new Vector(parameters.length-i);
				ret[i] = v;
				for(;i<parameters.length;i++) {
					v.addElement(parameters[i]);
				}
			}
		}
		return(ret);
	}
	
	/***
	* refine sort order by parameter length
	* it fix jdk 8 method ambigous method problem
	*
	***/
	public static Method[] sortMethods(Method[] p_methods){
		Method[] resultArray =  p_methods.clone();
		for (int i=0; i<resultArray.length; i++){
			for (int j=1; j< (resultArray.length-i); j++){
				if(resultArray[j-1].getName().equals(resultArray[j].getName()) && resultArray[j-1].getParameterTypes().length > resultArray[j].getParameterTypes().length){
					Method tmpMethod = resultArray[j-1];
					resultArray[j-1] = resultArray[j];
					resultArray[j] = tmpMethod;
				}
			}
		}
		return(resultArray);
	}

	public static void main(String []args)
		throws Exception
	{

		Hashtable hash = new Hashtable();
        Class myclass = Class.forName(args[0]);
        Constructor constructor = myclass.getConstructor((Class []) null);
		Object o = constructor.newInstance((Object[]) null);
		LogUtil.log("start getMethods");
		Method []methods = o.getClass().getMethods();
		LogUtil.log("end getMethods");

		int sz = (args.length -2)/2;
		// System.out.println("sz " + sz);
		Object []parameter = new Object[sz];
		int cc =0;
		for(int i=0;i<sz;i++) {
			int offset = i*2 + 2;
			if(args[offset].equals("S")) {
				parameter[cc] = new String(args[offset+1]);
			} else if(args[offset].equals("I")) {
				parameter[cc] = new Integer(Integer.parseInt(args[offset+1]));
			} else if(args[offset].equals("F")) {
				parameter[cc] = new Double(Double.parseDouble(args[offset+1]));
			} else if(args[offset].equals("B")) {
				parameter[cc] = new byte[Integer.parseInt(args[offset+1])];
			} else if(args[offset].equals("b")) {
				parameter[cc] = new String(args[offset+1]);
				// parameter[cc] = new Boolean(args[offset+1].equals("Y"));
			} else {
				throw new Exception("HAHA" + args[offset]);
			}
			// System.out.println(cc + " " + parameter[cc]);
			cc++;
		}


		String methodname = args[0] + "." + args[1];
		for(int i=0;i<10;i++) {
			MethodWalker w = (MethodWalker) hash.get(methodname);
			if(w == null) {
				LogUtil.log("start build");
				w = new MethodWalker(methods,args[1]);
				hash.put(methodname,w);
				LogUtil.log("end build");
			}
			LogUtil.log("start search");
			Method m = w.searchMethodTree(parameter);
			LogUtil.log("end search");
			if(m != null)
				m.invoke(o, w.transformParameters(m,parameter));
			else 
				System.out.println("no method match");
		}
	}
}
