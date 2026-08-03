package com.uniinformation.dynamic.winecave;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.zkbi.ZkBiActionAIHelperContext;
import com.uniinformation.zkbi.ZkBiComposerBase;

/**
 * Read-only operating knowledge for the Generate AI Comments action on
 * {@code graphql.photohdr}.
 */
public class GeneratePhotoAICommentsAIHelperContext
        extends ZkBiActionAIHelperContext {
    private static final String OPERATION_ID = "generate_ai_comments";

    private final GeneratePhotoAIComments handler;

    public GeneratePhotoAICommentsAIHelperContext(
            ZkBiComposerBase composer,
            GeneratePhotoAIComments handler) {
        super(composer);
        if (handler == null)
            throw new IllegalArgumentException("handler is required");
        this.handler = handler;
    }

    @Override
    public JSONObject getAiHelpContext() throws JSONException {
        return new JSONObject()
                .put("actionName", "Generate AI Comments")
                .put("viewId", "graphql.photohdr")
                .put("batchAction", true)
                .put("requiresSelectedRecords", true)
                .put("workflowAvailable", handler.isWorkflowAvailable())
                .put("writesBusinessData", true)
                .put("commitPoint", "Proceed in the Image description prompt dialog")
                .put("recordValuesShared", false);
    }

    @Override
    public JSONObject getAiHelpOperationCatalog() throws JSONException {
        JSONObject operation = new JSONObject()
                .put("id", OPERATION_ID)
                .put("description",
                        "Generate and save image descriptions in AI Comments for selected Product Photo records")
                .put("available", handler.isWorkflowAvailable());
        if (!handler.isWorkflowAvailable())
            operation.put("unavailableReason",
                    "The server-side image-to-text workflow file is not available.");
        return new JSONObject()
                .put("operations", new JSONArray().put(operation))
                .put("readOnlyHelp", true);
    }

    @Override
    public JSONObject getAiHelpOperationHelp(String operationId)
            throws JSONException {
        if (!StringUtils.equals(OPERATION_ID,
                StringUtils.trimToEmpty(operationId))) {
            return new JSONObject()
                    .put("known", false)
                    .put("available", false)
                    .put("message",
                            "No exact operating guide is registered for this action operation.");
        }

        JSONObject help = new JSONObject()
                .put("known", true)
                .put("available", handler.isWorkflowAvailable())
                .put("operationId", OPERATION_ID)
                .put("viewId", "graphql.photohdr")
                .put("readOnlyHelp", true)
                .put("purpose",
                        "Create an image description for each selected Product Photo record and save it into that record's AI Comments field.")
                .put("requirements", new JSONArray()
                        .put("Use the Product Photo list view.")
                        .put("Select one or more records using the page's row-selection controls.")
                        .put("Each selected record must have a valid Photo Id and a readable stored JPG image.")
                        .put("The server-side ComfyUI image-to-text workflow and service must be available."))
                .put("steps", new JSONArray()
                        .put("Select every Product Photo record whose AI Comments should be generated.")
                        .put("Click Generate AI Comments in the batch-action controls.")
                        .put("Review or edit the prefilled Image description prompt.")
                        .put("Click Proceed to start processing, or click Cancel/close the dialog to make no changes.")
                        .put("Wait for all selected records to be processed and for the Product Photo list to refresh.")
                        .put("Review the resulting AI Comments values after the refresh."))
                .put("selectionBehavior", new JSONObject()
                        .put("minimum", 1)
                        .put("multipleRecordsSupported", true)
                        .put("processingOrder", "selected records are processed in their current list order")
                        .put("unselectedRecordsAffected", false))
                .put("confirmation", new JSONObject()
                        .put("dialogTitle", "Generate AI Comment")
                        .put("promptField", "Image description prompt")
                        .put("proceedButton", "Proceed")
                        .put("cancelButton", "Cancel")
                        .put("commitWarning",
                                "Proceed starts a permanent update. The generated description replaces the selected record's current AI Comments value and is saved immediately for each successfully processed record."))
                .put("result", new JSONObject()
                        .put("source", "The stored JPG identified by the record's Photo Id")
                        .put("destinationField", "AI Comments")
                        .put("normalization",
                                "The returned description is normalized to English ASCII before saving")
                        .put("refresh", "The list refreshes after batch processing finishes"))
                .put("failureCases", new JSONArray()
                        .put("No records are selected.")
                        .put("The workflow file or ComfyUI service is unavailable.")
                        .put("A selected record has no valid Photo Id.")
                        .put("The stored photo image cannot be found.")
                        .put("The image-to-text service returns an empty description.")
                        .put("Saving the generated AI Comments value fails."))
                .put("notes", new JSONArray()
                        .put("This action is different from editing the record manually; it calls the configured image-to-text workflow for every selected record.")
                        .put("Canceling or closing the prompt dialog before Proceed makes no changes.")
                        .put("The AI Helper explains this action but never selects records, clicks Proceed, invokes ComfyUI or saves AI Comments."));
        if (!handler.isWorkflowAvailable())
            help.put("unavailableReason",
                    "The server-side image-to-text workflow file is not available. Ask the system administrator to check the configured workflow.");
        return help;
    }
}
