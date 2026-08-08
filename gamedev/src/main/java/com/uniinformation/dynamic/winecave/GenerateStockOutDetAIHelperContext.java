package com.uniinformation.dynamic.winecave;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.zkbi.ZkBiActionAIHelperContext;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Exact read-only help for the Stockout Charge Generate Det action. */
public class GenerateStockOutDetAIHelperContext extends ZkBiActionAIHelperContext {
    private static final String OPERATION_ID = "generate_stockout_details";

    public GenerateStockOutDetAIHelperContext(ZkBiComposerBase composer) {
        super(composer);
    }

    @Override
    public JSONObject getAiHelpContext() throws JSONException {
        return new JSONObject()
                .put("actionName", "Generate Det")
                .put("viewId", "graphql.StockoutCharge")
                .put("scope", "detail_form")
                .put("requiredAccessRight", "#storage")
                .put("writesPendingFormData", true)
                .put("savesAutomatically", false);
    }

    @Override
    public JSONObject getAiHelpOperationCatalog() throws JSONException {
        return new JSONObject().put("readOnlyHelp", true).put("operations",
                new JSONArray().put(new JSONObject()
                        .put("id", OPERATION_ID)
                        .put("description", "Regenerate storage details and calculate stock-out charge invoice rows in the open detail form")
                        .put("available", true)));
    }

    @Override
    public JSONObject getAiHelpOperationHelp(String operationId) throws JSONException {
        if (!StringUtils.equals(OPERATION_ID, StringUtils.trimToEmpty(operationId)))
            return new JSONObject().put("known", false).put("available", false);
        return new JSONObject()
                .put("known", true).put("available", true)
                .put("operationId", OPERATION_ID).put("readOnlyHelp", true)
                .put("purpose", "Build the monthly Storage Detail rows and recalculate the related stock-out charge invoice rows for the current Stockout Charge record.")
                .put("requirements", new JSONArray()
                        .put("Open the required Stockout Charge record in its detail form.")
                        .put("The Stock-out Charge Date is required and must be the first day of a month.")
                        .put("The logged-in user must have the #storage access right."))
                .put("steps", new JSONArray()
                        .put("Open the required row using its detail/edit control.")
                        .put("Check that the Stock-out Charge Date identifies the required month and is the first day of that month.")
                        .put("Click Generate Det in the detail form.")
                        .put("At 'Confirm Generate Storate Detail ?', click Yes to continue or No to leave the form unchanged.")
                        .put("Review the regenerated Storage Detail rows and the recalculated invoice/charge rows.")
                        .put("Click Save only after the generated values have been reviewed; close or discard the detail form if they must not be committed."))
                .put("dataBehavior", new JSONObject()
                        .put("sourcePeriod", "The stock movement period used by the calculation is the month before the Stock-out Charge Date.")
                        .put("regeneration", "Existing in-form Storage Detail rows are replaced/reused by the generated result, then stock-out charge invoice rows are recalculated.")
                        .put("commit", "Generate Det marks the detail form dirty but does not save the record. Save is the permanent commit point."))
                .put("safety", "AI Help only explains the action. It never clicks Generate Det, confirms the dialog, or saves the record.");
    }
}
