package com.kikyosoft.servlet.web;

import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@WebServlet(urlPatterns = "/api/getResource") // or configure in web.xml (see below)
public class GetResource extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Minimal extension → MIME map (extend as needed)
    private static final Map<String, String> EXT_TO_MIME = Map.ofEntries(
        Map.entry("jpg",  "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"),
        Map.entry("png",  "image/png"),
        Map.entry("webp", "image/webp"),
        Map.entry("gif",  "image/gif"),
        Map.entry("pdf",  "application/pdf"),
        Map.entry("xls",  "application/vnd.ms-excel"),
        Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        Map.entry("ppt",  "application/vnd.ms-powerpoint"),
        Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        Map.entry("doc",  "application/msword"),
        Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        Map.entry("zip",  "application/zip")
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = request.getParameter("url");
        String ext = request.getParameter("ext");

        if (url == null || url.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Missing resource path (url).");
            return;
        }

        // Access your ERP file stream via session helper
        SessionHelper sp;
        try {
            sp = ZkSessionHelper.getSessionHelper(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Failed to obtain session helper: " + e.getMessage());
            return;
        }

        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        try (InputStream in = sp.newErpFileInputStream(decodedUrl)) {
            if (in == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Resource not found.");
                return;
            }

            // Determine content type
            String contentType = resolveContentType(ext, decodedUrl);
            response.setContentType(contentType);

            // Cache for 1 hour (public)
            long seconds = TimeUnit.HOURS.toSeconds(1);
            response.setHeader("Cache-Control", "public, max-age=" + seconds);

            // Inline display by default
            response.setHeader("Content-Disposition", "inline");

            // Stream bytes without buffering the whole file
            // (Optionally set a larger buffer if your files are large and IO is fast)
            byte[] buffer = new byte[8192];
            int read;
            OutputStream out = response.getOutputStream();
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

        } catch (Exception ex) {
            // Don’t leak stack traces to clients
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Failed to load resource: " + ex.getMessage());
        }
    }

    private static String resolveContentType(String ext, String urlMaybeWithExt) {
        String guess = null;

        if (ext != null && !ext.trim().isEmpty()) {
            guess = EXT_TO_MIME.get(ext.toLowerCase(Locale.ROOT));
        }

        if (guess == null) {
            Optional<String> extFromUrl = extensionFromUrl(urlMaybeWithExt);
            if (extFromUrl.isPresent()) {
                guess = EXT_TO_MIME.get(extFromUrl.get().toLowerCase(Locale.ROOT));
            }
        }

        return (guess != null) ? guess : "application/octet-stream";
    }

    private static Optional<String> extensionFromUrl(String url) {
        if (url == null) return Optional.empty();
        int q = url.indexOf('?');
        String path = (q >= 0) ? url.substring(0, q) : url;
        int dot = path.lastIndexOf('.');
        if (dot >= 0 && dot < path.length() - 1) {
            return Optional.of(path.substring(dot + 1));
        }
        return Optional.empty();
    }
}
