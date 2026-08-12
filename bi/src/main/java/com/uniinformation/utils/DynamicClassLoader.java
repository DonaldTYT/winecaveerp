package com.uniinformation.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLConnection;

public class DynamicClassLoader extends ClassLoader{
	static boolean enabled = true;
	public DynamicClassLoader (ClassLoader parent) {
	        super(parent);
	}
    public Class loadClass(String name) throws ClassNotFoundException {
        if(!enabled || ! name.startsWith("com.uniinformation.dynamic")) return super.loadClass(name);
        try {
			Class<?> dummy = Class.forName("com.uniinformation.dynamic.dummy");
			String resourceName = name.replace('.', '/') + ".class";
			ClassLoader sourceClassLoader = dummy.getClassLoader();
			URL classUrl = sourceClassLoader != null
					? sourceClassLoader.getResource(resourceName)
					: ClassLoader.getSystemResource(resourceName);
			if (classUrl == null)
				throw new ClassNotFoundException("Class resource not found: " + resourceName);

			URLConnection connection = classUrl.openConnection();
			connection.setUseCaches(false);
			try (InputStream input = connection.getInputStream();
					ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
				byte[] data = new byte[8192];
				int length;
				while ((length = input.read(data)) != -1)
					buffer.write(data, 0, length);

				byte[] classData = buffer.toByteArray();
				return defineClass(name, classData, 0, classData.length);
			}
		} catch (IOException e) {
			throw new ClassNotFoundException("Unable to read class " + name, e);
		}
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
