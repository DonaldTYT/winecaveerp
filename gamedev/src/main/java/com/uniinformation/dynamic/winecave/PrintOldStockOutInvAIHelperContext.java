package com.uniinformation.dynamic.winecave;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.zkbi.ZkBiActionAIHelperContext;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Exact read-only help for list and detail Stockout Charge invoice output. */
public class PrintOldStockOutInvAIHelperContext extends ZkBiActionAIHelperContext {
    private static final String OPERATION_ID = "print_stockout_invoices";
    private final PrintOldStockOutInv handler;

    public PrintOldStockOutInvAIHelperContext(ZkBiComposerBase composer,
            PrintOldStockOutInv handler) {
        super(composer);
        this.handler = handler;
    }

    @Override
    public JSONObject getAiHelpContext() throws JSONException {
        return new JSONObject()
                .put("actionName", "Print Invoice")
                .put("viewId", "graphql.StockoutCharge")
                .put("availableScopes", new JSONArray().put("list_batch").put("detail_form"))
                .put("customerEmailDeliveryEnabled", handler.isRealEmailEnabled())
                .put("emailMode", handler.isRealEmailEnabled() ? "production" : "test");
    }

    @Override
    public JSONObject getAiHelpOperationCatalog() throws JSONException {
        return new JSONObject().put("readOnlyHelp", true).put("operations",
                new JSONArray().put(new JSONObject()
                        .put("id", OPERATION_ID)
                        .put("description", "Print stock-out charge invoices from selected list records or from the open detail form")
                        .put("available", true)));
    }

    @Override
    public JSONObject getAiHelpOperationHelp(String operationId) throws JSONException {
        if (!StringUtils.equals(OPERATION_ID, StringUtils.trimToEmpty(operationId)))
            return new JSONObject().put("known", false).put("available", false);
        JSONObject help = new JSONObject()
                .put("known", true).put("available", true)
                .put("operationId", OPERATION_ID).put("readOnlyHelp", true)
                .put("purpose", "Generate stock-out charge invoice PDFs for all or selected invoice rows associated with Stockout Charge records.")
                .put("listBatchSteps", new JSONArray()
                        .put("Select one or more Stockout Charge records in the list.")
                        .put("Click the Print Invoice batch button.")
                        .put("The action generates every invoice row under each selected Stockout Charge record and combines the output."))
                .put("detailFormSteps", new JSONArray()
                        .put("Open the required Stockout Charge record in its detail form.")
                        .put("Optionally filter the embedded invoice list before using Print Selected.")
                        .put("Click Print Invoice.")
                        .put("Choose Print to PDF or Print to Email, and choose Print All or Print Selected.")
                        .put("For Print Selected, select at least one customer invoice in the selection dialog; this selection is populated from the currently filtered embedded invoice list.")
                        .put("Click Proceed to generate the output, or Cancel to stop."))
                .put("pdfBehavior", new JSONObject()
                        .put("combinedOutput", true)
                        .put("desktop", "The combined PDF is sent to the desktop print flow.")
                        .put("mobile", "The combined PDF opens inline for viewing or sharing."))
                .put("dataBehavior", "Printing PDF does not update Stockout Charge business values.")
                .put("safety", "AI Help only explains this workflow and never selects records, prints, emails, or saves data.");
        if (!handler.isRealEmailEnabled()) {
            help.put("emailWarning", "Print to Email is currently in TEST mode. It does not deliver invoices to customer email addresses. Do not use it for customer delivery until production email mode is enabled and tested.");
        } else {
            help.put("emailWarning", "Email output is a real external action. Confirm recipients and invoice selection before Proceed. Email status remarks make the detail form dirty and must be reviewed.");
        }
        return help;
    }
}
