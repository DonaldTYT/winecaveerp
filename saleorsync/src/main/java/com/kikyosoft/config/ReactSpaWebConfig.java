package com.kikyosoft.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.util.UrlPathHelper;

@Configuration
public class ReactSpaWebConfig implements WebMvcConfigurer {
//	@Override
//	public void addViewControllers(ViewControllerRegistry registry) {
//	  registry.addRedirectViewController("/react/hr", "/react/hr/");
//	}
	
	 @Override
	  public void addCorsMappings(CorsRegistry registry) {
	    registry.addMapping("/erp/**")
	        .allowedOrigins(
	            "http://localhost:5173"   // Vite dev
	            // add prod origin later if needed
	        )
	        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
	        .allowedHeaders("*")
	        .allowCredentials(true)
	        .maxAge(3600);
	  }	
	

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/react/**")
        .addResourceLocations("classpath:/static/react/")
        .resourceChain(true)
        .addResolver(new MultiReactSpaResolver());
  }

  static class MultiReactSpaResolver extends PathResourceResolver {
    private static final UrlPathHelper PATH_HELPER = new UrlPathHelper();

//    @Override
//    protected Resource getResource(String resourcePath, Resource location) throws IOException {
//      // 1) Serve real static files if they exist
//      Resource requested = location.createRelative(resourcePath);
//      if (requested.exists() && requested.isReadable()) {
//        return requested;
//      }
//
//      // 2) SPA fallback for non-file routes (no dot in last segment)
//      if (looksLikeSpaRoute(resourcePath)) {
//        String app = firstSegment(resourcePath);
//        if (app != null && !app.isBlank()) {
//          Resource index = location.createRelative(app + "/index.html");
//          if (index.exists() && index.isReadable()) {
//            return index;
//          }
//        }
//      }
//
//      return null;
//    }
    
    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
      // 1) Serve real static files if they exist
      Resource requested = location.createRelative(resourcePath);
      if (requested.exists() && requested.isReadable()) {
        return requested;
      }

      // 2) If request is exactly "/react/<app>" (resourcePath like "hr"),
      // serve "/react/<app>/index.html"
      // This makes "/sync/react/hr" show Home without needing a redirect controller.
      if (resourcePath != null && !resourcePath.isBlank() && !resourcePath.contains("/")) {
        Resource index = location.createRelative(resourcePath + "/index.html");
        if (index.exists() && index.isReadable()) {
          return index;
        }
      }

      // 3) SPA fallback for routes (no dot in last segment)
      if (looksLikeSpaRoute(resourcePath)) {
        String app = firstSegment(resourcePath);
        if (app != null && !app.isBlank()) {
          Resource index = location.createRelative(app + "/index.html");
          if (index.exists() && index.isReadable()) {
            return index;
          }
        }
      }

      return null;
    }
 
    

    private boolean looksLikeSpaRoute(String resourcePath) {
      if (resourcePath == null || resourcePath.isBlank()) return false;

      // folder url: /react/hr/  -> resourcePath "hr/"
      if (resourcePath.endsWith("/")) return true;

      String last = resourcePath;
      int slash = resourcePath.lastIndexOf('/');
      if (slash >= 0) last = resourcePath.substring(slash + 1);

      // if it looks like a filename (has .js .css .png etc), don't fallback
      return !last.contains(".");
    }

    private String firstSegment(String resourcePath) {
      int slash = resourcePath.indexOf('/');
      return slash < 0 ? resourcePath : resourcePath.substring(0, slash);
    }
  }
}

