package com.uniinformation.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

//import com.uniinformation.prtdoc.PrtdocJson;

public class DynamicClassLoader extends ClassLoader{
	static String classPath = null;
	static boolean enabled = true;
	public DynamicClassLoader (ClassLoader parent) {
	        super(parent);
	}
    public Class loadClass(String name) throws ClassNotFoundException {
        if(!enabled || ! name.startsWith("com.uniinformation.dynamic")) return super.loadClass(name);
        try {
        	if(classPath == null) {
//        		classPath = ZkUtil.getClassRootPath();  //andrew230619 fix classpath contain classname
        		Class dummy = Class.forName("com.uniinformation.dynamic.dummy");
        		classPath = dummy.getProtectionDomain().getCodeSource().getLocation().getPath();
//        		classPath = DynamicClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        		
        		classPath = StringUtils.removeEnd(classPath , StringUtils.replaceChars(DynamicClassLoader.class.getName(),'.','/') +".class");
        		//append slash
        		if (!StringUtils.endsWith(classPath, "/")) {
        			classPath = classPath +"/";
        		}
        	}
        	/*
        	String url = "file:"+classPath+"/"+name.replace(".", "/")+".class";
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            */
        	File f = new File(classPath+"/"+name.replace(".", "/")+".class");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            InputStream input = new FileInputStream(f);
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass(name, classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    static public Object newInstance(String p_prtdocClass,Class[] paramTypes,Object ... params) throws Exception {
    	ClassLoader classLoader = DynamicClassLoader.class.getClassLoader();
    	DynamicClassLoader dl = new DynamicClassLoader(classLoader);
    	Class prtdocClass = dl.loadClass(p_prtdocClass);
//    	Class[]	paramTypes = new Class[]{BiResultErpv4.class,PrtdocJson.class,JSONObject.class};
//    	Class prtdocClass = Class.forName(p_prtdocClass);
    	Constructor constructor = prtdocClass.getConstructor(paramTypes);
    	if(constructor == null) throw new Exception("getConstructor Failed");
//    	return(constructor.newInstance(this,ppj,p_option)); 
    	return(constructor.newInstance(params)); 
    }
    
    static public Object loadClass2(String p_prtdocClass,Class[] paramTypes,Object ... params) throws Exception {
//    	Class[]	paramTypes = new Class[]{BiResultErpv4.class,PrtdocJson.class,JSONObject.class};
    	Class prtdocClass = Class.forName(p_prtdocClass);
    	Constructor constructor = prtdocClass.getConstructor(paramTypes);
    	if(constructor == null) throw new Exception("getConstructor Failed");
//    	return(constructor.newInstance(this,ppj,p_option)); 
    	return(constructor.newInstance(params)); 
    }
}
