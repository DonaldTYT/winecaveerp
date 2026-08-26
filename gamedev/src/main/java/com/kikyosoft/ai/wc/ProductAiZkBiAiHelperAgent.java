package com.kikyosoft.ai.wc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kikyosoft.ai.OpenAiZkBiAiHelperAgent;
import com.uniinformation.zkbi.ZkBiAiAgent;

/**
 * OpenAI ZK BI helper agent for the Wine Cave Product view
 * ({@code graphql.Product}).
 *
 * <p>The normal composer tools remain responsible for live fields,
 * permissions and available operations. This agent adds a read-only tool for
 * the Product detail form's source-defined tab and section layout.</p>
 */
public class ProductAiZkBiAiHelperAgent extends OpenAiZkBiAiHelperAgent {
    private static final String PRODUCT_PROMPT =
            " This agent is configured only for view graphql.Product. "
          + "For Product-specific questions about detail tabs, sections or where a field is "
          + "located, call get_product_page_guide and use its source-backed result. Continue to "
          + "use get_page_context and the standard operation tools for live state, permissions, "
          + "search, sorting, columns, presets, export and record editing. Do not infer any "
          + "Product-specific action that is not returned by those tools.";

    public ProductAiZkBiAiHelperAgent(String apiKey) {
        super(apiKey);
    }

    @Override
    public String chat(String systemPrompt, String message,
            List<? extends ZkBiAiAgent.Tool> availableTools) throws Exception {
        List<ZkBiAiAgent.Tool> tools = new ArrayList<ZkBiAiAgent.Tool>();
        if (availableTools != null)
            tools.addAll(availableTools);
        tools.add(new ProductPageGuideTool());
        return super.chat(systemPrompt + PRODUCT_PROMPT, message, tools);
    }

    private static final class ProductPageGuideTool implements ZkBiAiAgent.Tool {
        @Override
        public String getName() {
            return "get_product_page_guide";
        }

        @Override
        public String getDescription() {
            return "Returns exact, read-only source-backed layout guidance for the graphql.Product "
                    + "detail form. Use it for the Product Detail, Location and Owner, or "
                    + "Transaction History tabs and for locating Product fields.";
        }

        @Override
        public JSONObject getParameters() throws JSONException {
            JSONObject topic = new JSONObject()
                    .put("type", "string")
                    .put("description", "Product page area to explain")
                    .put("enum", new JSONArray()
                            .put("overview")
                            .put("product_detail")
                            .put("location_and_owner")
                            .put("transaction_history"));
            return new JSONObject()
                    .put("type", "object")
                    .put("properties", new JSONObject().put("topic", topic))
                    .put("required", new JSONArray().put("topic"))
                    .put("additionalProperties", false);
        }

        @Override
        public Object execute(JSONObject arguments) throws Exception {
            String topic = StringUtils.trimToEmpty(arguments.optString("topic", "overview"));
            if ("overview".equals(topic))
                return overview();
            if ("product_detail".equals(topic))
                return productDetail();
            if ("location_and_owner".equals(topic))
                return locationAndOwner();
            if ("transaction_history".equals(topic))
                return transactionHistory();
            return new JSONObject()
                    .put("known", false)
                    .put("available", false)
                    .put("message", "Unknown Product page guide topic.");
        }

        private JSONObject base(String topic) throws JSONException {
            return new JSONObject()
                    .put("known", true)
                    .put("available", true)
                    .put("readOnlyHelp", true)
                    .put("recordValuesShared", false)
                    .put("viewId", "graphql.Product")
                    .put("topic", topic)
                    .put("source", "viewforms/graphql/Product.zul");
        }

        private JSONObject overview() throws JSONException {
            return base("overview")
                    .put("purpose", "Browse Product records and open a record detail form containing three tabs.")
                    .put("detailTabs", new JSONArray()
                            .put(new JSONObject().put("order", 1).put("label", "Product Detail"))
                            .put(new JSONObject().put("order", 2).put("label", "Location and Owner"))
                            .put(new JSONObject().put("order", 3).put("label", "Transaction History")))
                    .put("instructions", new JSONArray()
                            .put("Use the standard list controls to find the required Product record.")
                            .put("Open that record's detail form using the standard detail/edit control documented by get_operation_help.")
                            .put("Select the required tab by its visible tab label."))
                    .put("customActions", new JSONArray())
                    .put("note", "No Product-specific ViewExtraBatchAction or ViewExtraJxFormAction is configured. Use list_page_operations for the exact standard operations currently available to this user.");
        }

        private JSONObject productDetail() throws JSONException {
            return base("product_detail")
                    .put("tabLabel", "Product Detail")
                    .put("purpose", "View or edit the Product master information, subject to the live user's field and record permissions.")
                    .put("sections", new JSONArray()
                            .put(section("Product summary", new String[] {
                                    "mt_tpname", "mt_tpcname", "storg_name", "stbd_name",
                                    "st_msize3", "stbd_appellation", "st_maturity",
                                    "stbd_cappellation", "st_mszrange", "st_iname", "st_einame",
                                    "st_score0", "st_score1", "st_obsolete", "st_issalable",
                                    "st_remark", "pdpi_hashtag", "st_remark2" }))
                            .put(section("Codes and Units", new String[] {
                                    "st_origin", "st_oicode", "st_icode", "st_irg",
                                    "st_unitcode", "st_unit", "st_msize1", "st_msize2",
                                    "st_barcode" }))
                            .put(section("Description", new String[] {
                                    "stnd_note", "pdpi_comments" }))
                            .put(section("Cost, Price and Web", new String[] {
                                    "st_standardcost", "st_standardcostcur", "st_standardprice",
                                    "st_standardcur", "st_retailprice", "st_onsellqty",
                                    "st_lastsprice", "st_lastpcost" }))
                            .put(section("Photo", new String[] { "pdpi_photoimg" }))
                            .put(section("System", new String[] {
                                    "st_slug", "st_cdate", "st_cuser", "st_udate", "st_uuser" })))
                    .put("fieldGuidance", "The ids above identify where fields are placed. Use get_page_context for their current display names, editability and the logged-in user's permissions.")
                    .put("saveGuidance", "Editing and saving use the standard update_record and save_record operations returned by list_page_operations; this Product guide never changes data.");
        }

        private JSONObject locationAndOwner() throws JSONException {
            return base("location_and_owner")
                    .put("tabLabel", "Location and Owner")
                    .put("tabOrder", 2)
                    .put("componentId", "list_graphql_locbinstatus")
                    .put("linkedView", "graphql.locbinstatus")
                    .put("instructions", new JSONArray()
                            .put("Open the required Product record's detail form.")
                            .put("Select the Location and Owner tab.")
                            .put("Review the embedded location/owner list for that Product."))
                    .put("limitation", "This guide documents navigation and layout only. It does not expose row values or claim that embedded rows can be changed.");
        }

        private JSONObject transactionHistory() throws JSONException {
            return base("transaction_history")
                    .put("tabLabel", "Transaction History")
                    .put("tabOrder", 3)
                    .put("componentId", "list_graphql_Stmovd")
                    .put("linkedView", "graphql.Stmovd")
                    .put("instructions", new JSONArray()
                            .put("Open the required Product record's detail form.")
                            .put("Select the Transaction History tab.")
                            .put("Review the embedded transaction-history list for that Product."))
                    .put("limitation", "This Helper does not inspect transaction values or provide an unsupported transaction operation.");
        }

        private JSONObject section(String label, String[] fieldIds) throws JSONException {
            JSONArray fields = new JSONArray();
            for (String fieldId : fieldIds)
                fields.put(fieldId);
            return new JSONObject().put("label", label).put("fieldIds", fields);
        }
    }
}
