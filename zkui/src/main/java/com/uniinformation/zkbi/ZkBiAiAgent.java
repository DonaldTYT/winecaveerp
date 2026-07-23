package com.uniinformation.zkbi;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * AI-backend contract used by the ZK BI agent dialog.
 *
 * <p>Implementations translate these backend-neutral tools to their AI
 * provider. They must not create or depend on ZK UI components.</p>
 */
public interface ZkBiAiAgent {
    String chat(String systemPrompt,
                String message,
                List<? extends Tool> availableTools) throws Exception;

    interface Tool {
        String getName();

        String getDescription();

        JSONObject getParameters() throws JSONException;

        Object execute(JSONObject arguments) throws Exception;
    }
}
