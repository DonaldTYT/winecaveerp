package com.kikyosoft.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.webcore.SessionHelper;

/**
 * Session-scoped state used by the shell. Agent-specific login state must not
 * be kept in static mutable fields because one webapp can serve several agents.
 */
public final class ShellSessionUtil {
	private static final String LOGGED_IN_ORIGINS_KEY = ShellSessionUtil.class.getName() + ".loggedInOrigins";

	private ShellSessionUtil() {
	}

	/**
	 * Validate that a target URL belongs to the configured application base.
	 */
	public static boolean isUrlUnderBase(String p_targetUrl, String p_applicationBaseUrl) {
		try {
			URI target = new URI(p_targetUrl).normalize();
			URI base = normalizeApplicationBase(p_applicationBaseUrl);
			if (target.getUserInfo() != null || target.getFragment() != null) return false;
			if (!StringUtils.equalsIgnoreCase(base.getScheme(), target.getScheme())) return false;
			if (!StringUtils.equalsIgnoreCase(base.getHost(), target.getHost())) return false;
			if (effectivePort(base) != effectivePort(target)) return false;
			return StringUtils.defaultString(target.getPath()).startsWith(base.getPath());
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Record a remote application only after its passport has been issued.
	 */
	public static void registerLoggedInOrigin(SessionHelper p_sessionHelper, String p_agent,
			String p_applicationBaseUrl) {
		if (p_sessionHelper == null || StringUtils.isBlank(p_agent) || StringUtils.isBlank(p_applicationBaseUrl)) return;
		try {
			URI base = normalizeApplicationBase(p_applicationBaseUrl);
			String baseUrl = base.toString();
			// The remote request must perform its own logout directly. Without this
			// marker, a remote shell could recursively start another logout fan-out.
			String logoutUrl = base.resolve("logout.html?remoteLogoutDone=Y").toString();
			synchronized (p_sessionHelper) {
				Map<String, String> loggedInOrigins = getLoggedInOrigins(p_sessionHelper);
				if (loggedInOrigins == null) loggedInOrigins = new LinkedHashMap<String, String>();
				loggedInOrigins.put(baseUrl, logoutUrl);
				p_sessionHelper.putSessionData(LOGGED_IN_ORIGINS_KEY, loggedInOrigins);
			}
		} catch (Exception ex) {
			LogUtil.log("Ignore invalid shell application base URL for agent " + p_agent + ": " + p_applicationBaseUrl);
		}
	}

	public static List<String> getLoggedInOriginLogoutUrls(SessionHelper p_sessionHelper) {
		if (p_sessionHelper == null) return new ArrayList<String>();
		synchronized (p_sessionHelper) {
			Map<String, String> loggedInOrigins = getLoggedInOrigins(p_sessionHelper);
			return loggedInOrigins == null
					? new ArrayList<String>()
					: new ArrayList<String>(loggedInOrigins.values());
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> getLoggedInOrigins(SessionHelper p_sessionHelper) {
		Object data = p_sessionHelper.getSessionData(LOGGED_IN_ORIGINS_KEY);
		return data instanceof Map ? (Map<String, String>) data : null;
	}

	private static URI normalizeApplicationBase(String p_applicationBaseUrl) throws URISyntaxException {
		URI base = new URI(p_applicationBaseUrl).normalize();
		String scheme = base.getScheme();
		if (!(StringUtils.equalsIgnoreCase("http", scheme) || StringUtils.equalsIgnoreCase("https", scheme))
				|| StringUtils.isBlank(base.getHost()) || base.getUserInfo() != null
				|| base.getQuery() != null || base.getFragment() != null) {
			throw new URISyntaxException(p_applicationBaseUrl, "Invalid application base URL");
		}
		String path = StringUtils.defaultIfBlank(base.getPath(), "/");
		if (!path.endsWith("/")) path += "/";
		return new URI(base.getScheme(), null, base.getHost(), base.getPort(), path, null, null);
	}

	private static int effectivePort(URI p_uri) {
		if (p_uri.getPort() >= 0) return p_uri.getPort();
		return StringUtils.equalsIgnoreCase("https", p_uri.getScheme()) ? 443 : 80;
	}
}
