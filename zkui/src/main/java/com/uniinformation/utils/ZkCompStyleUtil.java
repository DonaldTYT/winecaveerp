package com.uniinformation.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.HtmlBasedComponent;

public class ZkCompStyleUtil {

    public static void setProperty(HtmlBasedComponent component, String... propOrValue) {
        Map<String, String> styleMap = getPropertyMap(component.getStyle());
        for (int i = 0; i < propOrValue.length; i+=2)
        	styleMap.put(propOrValue[i], propOrValue[i + 1]);
        component.setStyle(buildString(styleMap));
    }
    
    public static void removeProperty(HtmlBasedComponent component, String property) {
        Map<String, String> styleMap = getPropertyMap(component.getStyle());
        styleMap.remove(property);
        component.setStyle(buildString(styleMap));
    }
    
    public static String getProperty(HtmlBasedComponent component, String property) {
        return getPropertyMap(component).get(property);
    }

    public static Map<String, String> getPropertyMap(HtmlBasedComponent component) {
        return getPropertyMap(component.getStyle());
    }
    
    public static Map<String, String> getPropertyMap(String style) {
        if (StringUtils.isBlank(style))
            return new LinkedHashMap<>();
        return Arrays.stream(style.split(";"))
        			.map(d -> d.split(":", 2))
        			.filter(p -> p.length == 2)
        			.collect(Collectors.toMap(
        		            p -> p[0].trim(),
        		            p -> p[1].trim(),
        		            (o, n) -> n,
        		            LinkedHashMap::new
        		        ));
    }
    
    public static String buildString(Map<String, String> styleMap) {
        return styleMap.entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(";"));
    }
}
