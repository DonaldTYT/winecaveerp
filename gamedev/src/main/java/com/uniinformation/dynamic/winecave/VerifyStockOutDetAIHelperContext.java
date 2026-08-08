package com.uniinformation.dynamic.winecave;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.zkbi.ZkBiActionAIHelperContext;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Exact read-only help for batch verification of Stockout Charge details. */
public class VerifyStockOutDetAIHelperContext extends ZkBiActionAIHelperContext {
    private static final String OPERATION_ID = "verify_stockout_details";

    public VerifyStockOutDetAIHelperContext(ZkBiComposerBase composer) {
        super(composer);
    }

    @Override
    public JSONObject getAiHelpContext() throws JSONException {
        return new JSONObject()
                .put("actionName", "Verify Stock-out Details")
                .put("viewId", "graphql.StockoutCharge")
                .put("scope", "list_batch")
                .put("requiresSelectedRecords", true)
                .put("readOnlyOperation", true)
                .put("configuredButtonLabel", "Print Invoice")
                .put("configurationWarning", "The current ViewExtraBatchAction configuration labels this verification button as Print Invoice even though it runs VerifyStockOutDet.");
    }

    @Override
    public JSONObject getAiHelpOperationCatalog() throws JSONException {
        return new JSONObject().put("readOnlyHelp", true).put("operations",
                new JSONArray().put(new JSONObject()
                        .put("id", OPERATION_ID)
                        .put("description", "Recalculate the expected storage details and compare them with selected Stockout Charge records without saving changes")
                        .put("available", true)));
    }

    @Override
    public JSONObject getAiHelpOperationHelp(String operationId) throws JSONException {
        if (!StringUtils.equals(OPERATION_ID, StringUtils.trimToEmpty(operationId)))
            return new JSONObject().put("known", false).put("available", false);
        return new JSONObject()
                .put("known", true).put("available", true)
                .put("operationId", OPERATION_ID).put("readOnlyHelp", true)
                .put("purpose", "Check whether each selected Stockout Charge record's stored detail rows match a freshly calculated expected result.")
                .put("steps", new JSONArray()
                        .put("In the Stockout Charge list, select one or more records to verify.")
                        .put("Run the batch action backed by VerifyStockOutDet. In the current configuration it is the second button labeled Print Invoice.")
                        .put("Review any reported missing, unexpected, or different detail values.")
                        .put("Correct a failed record in its detail form, regenerate if appropriate, review, and save separately."))
                .put("behavior", new JSONObject()
                        .put("processingOrder", "Selected records are checked in their current list order.")
                        .put("comparison", "The action recalculates expected prior-month storage details and compares them with the saved detail rows.")
                        .put("writesBusinessData", false)
                        .put("transaction", "The verification transaction is rolled back after the check; it does not save generated values."))
                .put("important", "Do not confuse this configured button with the real Print Invoice action. Its current label is a configuration error.")
                .put("safety", "AI Help only explains verification and never selects or changes records.");
    }
}
