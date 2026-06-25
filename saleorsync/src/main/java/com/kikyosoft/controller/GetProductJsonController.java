package com.kikyosoft.controller;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.kikyosoft.config.SaleorConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/oadmin")
public class GetProductJsonController {

	@Autowired
    private SaleorConfig config; // must expose getApiUrl(), getAuthHeader(), getMapper()
    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private JsonNode call(String query, Map<String, Object> vars) {
    	  ObjectNode body = mapper.createObjectNode();
    	  body.put("query", query);
    	  // IMPORTANT: allow nulls in variables
    	  if (vars == null) {
    	    body.set("variables", mapper.createObjectNode());
    	  } else {
    	    body.set("variables", mapper.valueToTree(vars)); // HashMap can hold nulls
    	  }
    	  HttpHeaders h = new HttpHeaders();
    	  h.setContentType(MediaType.APPLICATION_JSON);
    	  h.set("Authorization", config.getAuthHeader());
    	  return restTemplate.exchange(config.getApiUrl(), HttpMethod.POST, new HttpEntity<>(body, h), JsonNode.class).getBody();
    	}

    	private static Map<String, Object> vars(Object... kv) {
    	  Map<String, Object> m = new HashMap<>();
    	  for (int i = 0; i + 1 < kv.length; i += 2) {
    	    m.put((String) kv[i], kv[i + 1]); // may be null: OK
    	  }
    	  return m;
    	}



    private static final String PRODUCTS_PAGE = " query($after:String){ products(first: 100, after:$after){ pageInfo{ hasNextPage endCursor } edges{ node{ id name slug description productType{ slug } channelListings{ channel{ slug } isPublished visibleInListings } attributes{ attribute{ id slug inputType } values{ id name slug } } } } } } ";

    @GetMapping("/GetProductJson")
    public String exportProducts() throws Exception {
        ArrayNode out = config.getMapper().createArrayNode();

        String after = null;
        while (true) {
        	JsonNode data = call(PRODUCTS_PAGE, vars("after", after));
//            JsonNode data = call(config.getApiUrl(), config.getAuthHeader(), PRODUCTS_PAGE, Map.of("after", after));
            JsonNode conn = data.path("data").path("products");
            for (JsonNode e : conn.path("edges")) {
                JsonNode p = e.path("node");
                ObjectNode item = config.getMapper().createObjectNode();
                item.put("name", p.path("name").asText());
                item.put("slug", p.path("slug").asText());
                item.put("description", p.path("description").asText(null));
                item.put("productType", p.path("productType").path("slug").asText());

                // channel publication flags
                ArrayNode ch = config.getMapper().createArrayNode();
                for (JsonNode cl : p.path("channelListings")) {
                    ObjectNode c = config.getMapper().createObjectNode();
                    c.put("channel", cl.path("channel").path("slug").asText());
                    c.put("isPublished", cl.path("isPublished").asBoolean(false));
                    c.put("visibleInListings", cl.path("visibleInListings").asBoolean(false));
                    ch.add(c);
                }
                item.set("channels", ch);

                // attributes (flatten to attributeSlug -> array of strings OR array of valueSlugs)
                ArrayNode attrs = config.getMapper().createArrayNode();
                for (JsonNode a : p.path("attributes")) {
                    String aSlug = a.path("attribute").path("slug").asText();
                    String inputType = a.path("attribute").path("inputType").asText();
                    ArrayNode values = config.getMapper().createArrayNode();
                    for (JsonNode v : a.path("values")) {
                        // For DROPDOWN, we record choice slug (stable across envs)
                        if ("DROPDOWN".equalsIgnoreCase(inputType)) {
                            values.add(v.path("slug").asText());
                        } else {
                            // For text/numeric/rich, export the display value
                            String val = v.path("name").asText(null);
                            if (val == null || val.isBlank()) {
                                val = v.path("slug").asText(null); // fallback
                            }
                            if (val != null) values.add(val);
                        }
                    }
                    ObjectNode an = config.getMapper().createObjectNode();
                    an.put("attribute", aSlug);
                    an.put("inputType", inputType);
                    an.set("values", values);
                    attrs.add(an);
                }
                item.set("attributes", attrs);

                out.add(item);
            }
            boolean hasNext = conn.path("pageInfo").path("hasNextPage").asBoolean(false);
            if (!hasNext) break;
            after = conn.path("pageInfo").path("endCursor").asText(null);
        }

        File f = new File("/tmp/products.json");
        config.getMapper().writerWithDefaultPrettyPrinter().writeValue(f, out);
        return "Exported " + out.size() + " products -> " + f.getAbsolutePath();
    }
    
    
}
