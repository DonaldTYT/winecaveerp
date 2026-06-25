// src/main/java/com/kikyosoft/config/IniLoader.java
package com.kikyosoft.config;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.kyoko.common.CoreLog;
import com.uniinformation.utils.IniHelper;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
public class IniLoader {
  private final ResourceLoader resourceLoader;
  private final Configurations configurations = new Configurations();

  public IniLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
    CoreLog.log("IniLoarder Instantiated");
    IniHelper.setIniLoader(this);
  }

  /**
   * Load an INI from either:
   *  - "classpath:config/erpsetup.ini" (recommended for bundled files), or
   *  - "/etc/saleorsync/erpsetup.ini" (absolute filesystem path), or
   *  - "file:/etc/saleorsync/erpsetup.ini" (explicit file: URL), or
   *  - "http(s)://..." (URL).
   */
  public INIConfiguration load(String location,INIConfiguration ini) throws Exception {
    String normalized = normalize(location);
    Resource r = resourceLoader.getResource(normalized);
    if (!r.exists()) {
      throw new IllegalArgumentException("INI not found: " + normalized);
    }
    URL url = r.getURL();                 // JAR/WAR-safe (no File needed)
    if(ini == null) {
    	return configurations.ini(url);
    } else {
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            ini.read(reader);
        }
        return ini;
    }
  }

  /** Overload for filesystem Paths. */
  public INIConfiguration load(Path path,INIConfiguration ini) throws Exception {
    URL url = path.toUri().toURL();
    if(ini == null) {
    	return configurations.ini(url);
    } else {
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            ini.read(reader);
        }
        return ini;
    }
  }

  private static String normalize(String location) {
    // If already has a scheme, leave it
    if (location.startsWith("classpath:") ||
        location.startsWith("file:") ||
        location.startsWith("http:") ||
        location.startsWith("https:")) {
      return location;
    }
    // Absolute filesystem path? treat as file:
    Path p = Path.of(location);
    if (p.isAbsolute()) return "file:" + p.toString();
    // Otherwise, treat as classpath-relative
    return "classpath:" + location;
  }
  
  public Properties loadProperty(String location) throws Exception {
	    String loc = normalize(location);
	    Resource res = resourceLoader.getResource(loc);
	    if (!res.exists()) throw new IllegalArgumentException("Properties not found: " + loc);

	    Properties p = new Properties();
	    // Use a Reader so you control encoding (UTF-8 here)
	    try (Reader reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
	      p.load(reader);
	    }
	    return p; 
  }
  
  public InputStream getResourceAsStream(String p_url) throws Exception {
	    Resource res = resourceLoader.getResource("classpath:"+p_url);
	    return(res.getInputStream());
  }
}
