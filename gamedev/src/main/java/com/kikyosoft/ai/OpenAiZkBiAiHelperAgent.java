package com.kikyosoft.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.kikyosoft.ai.openai.OpenAIChatClient;
import com.kikyosoft.utils.LogUtil;
import com.uniinformation.zkbi.ZkBiAiHelperAgent;

/** OpenAI backend adapter for the provider-neutral ZK BI AI helper. */
public final class OpenAiZkBiAiHelperAgent implements ZkBiAiHelperAgent {
    private OpenAIChatClient client;
    private String apikey;

    public OpenAiZkBiAiHelperAgent(String p_key) {
    	apikey = p_key;
    }

    @Override
    public String chat(String systemPrompt,
                       String message,
                       List<? extends ZkBiAiHelperAgent.Tool> availableTools) throws Exception {
        if (client == null)
            client = new OpenAIChatClient(apikey);

        List<? extends ZkBiAiHelperAgent.Tool> sourceTools = availableTools == null
                ? Collections.<ZkBiAiHelperAgent.Tool>emptyList()
                : availableTools;
        List<OpenAIChatClient.ChatTool> openAiTools =
                new ArrayList<OpenAIChatClient.ChatTool>(sourceTools.size());
        for (final ZkBiAiHelperAgent.Tool tool : sourceTools) {
            openAiTools.add(new OpenAIChatClient.ChatTool() {
                @Override
                public String getName() {
                    return tool.getName();
                }

                @Override
                public String getDescription() {
                    return tool.getDescription();
                }

                @Override
                public JSONObject getParameters() {
                	try {
                		return tool.getParameters();
                	} catch (JSONException jex) {
                		LogUtil.log(jex);
                		return(null);
                	}
                }

                @Override
                public Object execute(JSONObject arguments) throws Exception {
                    return tool.execute(arguments);
                }
            });
        }
        return client.chat(systemPrompt, message, openAiTools);
    }
}
