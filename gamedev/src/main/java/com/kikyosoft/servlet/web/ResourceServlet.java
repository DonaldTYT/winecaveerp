package com.kikyosoft.servlet.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

@WebServlet(name = "ResourceServlet", urlPatterns = {"/api/getResourceOld/*"})
public class ResourceServlet extends HttpServlet {

    // Change this to wherever you keep public-serving binaries.
    // Example: /opt/app/resources or a mounted NAS path.
    private Path baseDir;

    @Override
    public void init() throws ServletException {
        // Option 1: hard-code
//        baseDir = Paths.get("/opt/app/resources").toAbsolutePath().normalize();
        baseDir = Paths.get("/images").toAbsolutePath().normalize();

//    	// Option 2: from web.xml <context-param> or env var
//        String configured = getServletContext().getInitParameter("resource.baseDir");
//        if (configured == null || configured.isBlank()) {
//            configured = System.getenv("RESOURCE_BASE_DIR");
//        }
//        if (configured == null || configured.isBlank()) {
//            // safe default for demo
//            configured = System.getProperty("user.home") + "/app-resources";
//        }
//        baseDir = Paths.get(configured).toAbsolutePath().normalize();
//        if (!Files.isDirectory(baseDir)) {
//            throw new ServletException("Base directory does not exist: " + baseDir);
//        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1) Extract the resource path from /api/getResource/<resource_path>
    	SessionHelper sp = ZkSessionHelper.getSessionHelper(req, resp);
		String url = req.getParameter("url");
		if(url != null) {
        		try {
        			InputStream is = sp.newErpFileInputStream(url);
        			if(is != null) {
        				String ext = req.getParameter("ext");
        				String mime = "application/octet-stream";
        				if(ext != null) {
        					if(ext.equals("jpg")) {
        						mime = "image/jpg";
        					} else if(ext.equals("png")) {
        						mime = "image/png";
        					} else if(ext.equals("pdf")) {
        						mime = "application/pdf";
        					} else if(ext.equals("xls")) {
        						mime = "application/vnd.ms-excel";
        					} else if(ext.equals("xlsx")) {
        						mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        					} else if(ext.equals("ppt")) {
        						mime = "application/vnd.ms-powerpoint";
        					} else if(ext.equals("zip")) {
        						mime = "application/zip";
        					} else if(ext.equals("doc")) {
        						mime = "application/msword";
        					} else if(ext.equals("docx")) {
        						mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
        					} else if(ext.equals("webp")) {
        						mime = "image/webp";
        					}
        				}
        				resp.setContentType(mime);
        				resp.setHeader("Cache-Control", "public, max-age=3600"); // 1 hour
        				OutputStream out = resp.getOutputStream();
        				is.transferTo(out);
        				out.flush();
        			} else {
        				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing resource path.");
        				return;
        			}
        		} catch (Exception ex) {
        			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found.");
        		} 
                return;
		}
    	
        String pathInfo = req.getPathInfo(); // includes leading slash, e.g. "/images/logo.png"
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing resource path.");
            return;
        }

        // Decode URL-encoded characters (spaces, unicode, etc.)
        String decoded = URLDecoder.decode(pathInfo, StandardCharsets.UTF_8.name()); // e.g. "/images/logo.png"

        // Strip the leading slash so resolve() treats it as relative
        String relative = decoded.startsWith("/") ? decoded.substring(1) : decoded;
        if(relative.startsWith("filing/")) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found.");
        	relative = relative.substring(7);
    		try {
    			FilingUtilObject fobj = FilingUtil.getFile(sp.getAgent(), null, relative, null);
    			if(fobj != null) {
    				String mime = getServletContext().getMimeType(relative+".jpg");
    				if (mime == null) mime = "application/octet-stream";
    				resp.setContentType(mime);
    				resp.setHeader("Cache-Control", "public, max-age=3600"); // 1 hour
    				OutputStream out = resp.getOutputStream();
    				FilingUtil.getFile(sp.getAgent(), null, relative, out);
    				out.flush();
    			}
    		} catch (Exception ex) {
    			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found.");
    		} 
            return;
        } else if(relative.startsWith("basedir/")) {
        	relative = relative.substring(8);

        // 2) Resolve against baseDir and prevent path traversal (../../etc/passwd)
        Path candidate = baseDir.resolve(relative).normalize();
        if (!candidate.startsWith(baseDir)) { // crucial security check
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid resource path.");
            return;
        }

        // 3) Open the resource (filesystem example)
        if (!Files.exists(candidate) || !Files.isReadable(candidate) || Files.isDirectory(candidate)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found.");
            return;
        }

        long length = Files.size(candidate);

        // 4) Content type (let container guess by filename; fall back to octet-stream)
        String mime = getServletContext().getMimeType(candidate.getFileName().toString());
        if (mime == null) mime = "application/octet-stream";
        resp.setContentType(mime);

        // Optional: caching headers (tune as you like)
        resp.setHeader("Cache-Control", "public, max-age=3600"); // 1 hour
        // If you maintain your own ETag/Last-Modified, set them here.

        // 5) Content length (ok to omit for very large/streamed responses, but nice to have)
        // Avoid setContentLength for >2GB; use setContentLengthLong.
        if (length <= Integer.MAX_VALUE) {
            resp.setContentLength((int) length);
        } else {
            resp.setContentLengthLong(length);
        }

        // 6) Stream the bytes
        try (InputStream in = Files.newInputStream(candidate, StandardOpenOption.READ);
             OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            out.flush();
        }
        } else{
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found.");
            return;
        }
    }
}

