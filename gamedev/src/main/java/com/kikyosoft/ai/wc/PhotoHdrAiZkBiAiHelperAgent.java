package com.kikyosoft.ai.wc;

import com.kikyosoft.ai.OpenAiZkBiAiHelperAgent;

/**
 * OpenAI ZK BI agent for the Wine Cave Product Photo view
 * ({@code graphql.photohdr}).
 *
 * <p>Page-specific Product Photo knowledge and tools can be added here
 * without coupling the reusable OpenAI client to ZK or Wine Cave code.</p>
 */
public class PhotoHdrAiZkBiAiHelperAgent extends OpenAiZkBiAiHelperAgent {

    public PhotoHdrAiZkBiAiHelperAgent(String apiKey) {
        super(apiKey);
    }
}
