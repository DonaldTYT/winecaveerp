package com.uniinformation.zkbi;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.webcore.SessionHelper;

/**
 * Provider-neutral, read-only AI help context for {@link ZkBiComposerBase}.
 *
 * <p>The composer supplies a fresh {@link PageState} snapshot for each tool
 * call. This class describes the page and its supported operations but does
 * not expose tools that mutate UI state or record data.</p>
 */
public class ZkBiComposerAIHelperContext implements ZkBiAiAgentContext {
    private final ZkBiComposerBase composer;

    public ZkBiComposerAIHelperContext(ZkBiComposerBase composer) {
        if (composer == null)
            throw new IllegalArgumentException("composer is required");
        this.composer = composer;
    }

    @Override
    public SessionHelper getAiHelpSessionHelper() {
        return composer.getAiHelpSessionHelper();
    }

    @Override
    public Component getAiHelpParentComponent() {
        return composer.getAiHelpParentComponent();
    }

    @Override
    public JSONObject getAiHelpContext() throws JSONException {
        PageState state = state();
        SessionHelper sessionHelper = getAiHelpSessionHelper();
        JSONObject context = new JSONObject();
        context.put("invokerClass", state.invokerClass);
        context.put("invokerComponentId", state.invokerComponentId == null
                ? JSONObject.NULL : state.invokerComponentId);
        context.put("pageTitle", StringUtils.defaultIfBlank(state.pageTitle, state.viewId));
        context.put("pageId", state.pageId);
        context.put("viewId", state.viewId);
        context.put("helpId", state.helpId);
        context.put("pageAction", state.pageAction);
        context.put("listVisible", state.listVisible);
        context.put("detailOpen", state.detailOpen);
        context.put("multiSelect", state.multiSelect);
        context.put("selectedRowIndex", state.selectedRowIndex);
        context.put("selectedRowCount", state.selectedRowCount);
        context.put("renderedRowCount", state.renderedRowCount);

        if (state.detailForm != null) {
            context.put("detailVisible", state.detailForm.isFormVisible());
            context.put("detailMode", modeName(state.detailForm.getCurMode()));
        }
        else {
            context.put("detailVisible", false);
            context.put("detailMode", "none");
        }

        BiResult result = state.result;
        if (result == null) {
            context.put("biResultAvailable", false);
            composer.appendAiActionContexts(context);
            return context;
        }
        context.put("biResultAvailable", true);
        context.put("viewName", result.getView().getName());
        context.put("viewHeader", result.getView().getHeader());
        context.put("loadedRowCount", result.getRowCount());
        context.put("recordLimit", result.getRecLimit());
        context.put("pendingChanges", new JSONObject()
                .put("added", result.getInsertCount())
                .put("updated", result.getUpdateCount())
                .put("deleted", result.getDeleteCount()));
        context.put("permissions", new JSONObject()
                .put("viewDetail", result.allowDetail())
                .put("add", result.allowAdd())
                .put("update", result.allowUpdate())
                .put("delete", result.allowDelete()));

        JSONArray fields = new JSONArray();
        Vector columns = result.getColumns();
        int limit = Math.min(columns.size(), 200);
        for (int i = 0; i < limit; i++) {
            BiColumn column = (BiColumn)columns.elementAt(i);
            fields.put(new JSONObject()
                    .put("id", column.getLabel())
                    .put("name", column.getEngName())
                    .put("type", column.getColumnType())
                    .put("editableWhenAdding", !column.isNoEntry(sessionHelper))
                    .put("editableWhenUpdating", !column.isNoUpdate(sessionHelper)));
        }
        context.put("fields", fields);
        context.put("fieldCount", columns.size());
        context.put("fieldListTruncated", columns.size() > limit);
        context.put("recordValuesShared", false);
        composer.appendAiActionContexts(context);
        return context;
    }

    @Override
    public JSONObject getAiHelpOperationCatalog() throws JSONException {
        PageState state = state();
        JSONArray operations = new JSONArray();

        if (state.canFilter)
            operations.put(operation("filter_records", "Filter the currently loaded list records"));
        if (state.canAdvancedSearch)
            operations.put(operation("advanced_search", "Query records using field-specific conditions and operators"));
        if (state.canQueryViewPresets)
            operations.put(operation("query_view_presets", "Save and restore query conditions, sorting and list-column layout"));
        if (state.canSortColumns)
            operations.put(operation("sort_records", "Sort the list by one or more columns"));
        if (state.canDisplayColumns)
            operations.put(operation("display_columns", "Show or hide columns in the desktop list"));
        if (state.canBatchUpdate)
            operations.put(operation("batch_update_records", "Set one eligible column to a value across selected records"));
        if (state.canExportList)
            operations.put(operation("export_list_to_excel", "Download the current list view as an Excel workbook"));
        if (state.canOpenDetail)
            operations.put(operation("open_record_detail", "Open one listed record's detail form"));
        if (state.canAdd)
            operations.put(operation("add_record", "Open a blank detail form and add a new record"));
        if (state.canSaveNew)
            operations.put(operation("save_new_record", "Validate and save the new record in the open add form"));
        if (state.canUpdate) {
            operations.put(operation("update_record", "Open, modify and save an existing record"));
            operations.put(operation("save_record", "Save changes in an open record detail form"));
        }
        if (state.canDeleteRecords)
            operations.put(operation("delete_records", "Select and permanently delete one or more list records after confirmation"));
        if (state.canDeleteCurrent)
            operations.put(operation("delete_current_record", "Permanently delete the record open in the detail form after confirmation"));
        if (state.detailOpen || (state.detailForm != null && state.detailForm.isFormVisible()))
            operations.put(operation("return_to_list", "Close the detail form and return to the list"));

        JSONObject catalog = new JSONObject()
                .put("viewId", state.viewId)
                .put("currentPageState", currentPageState(state))
                .put("operations", operations)
                .put("readOnlyHelp", true)
                .put("recordValuesShared", false);
        if (state.result == null)
            catalog.put("note", "The BI result is not currently available, so only live UI operations are listed.");
        composer.appendAiActionOperationCatalog(operations);
        composer.customizeAiHelpOperationCatalog(operations);
        return catalog;
    }

    @Override
    public JSONObject getAiHelpOperationHelp(String operationId) throws JSONException {
        PageState state = state();
        String id = StringUtils.trimToEmpty(operationId);
        JSONObject help = new JSONObject()
                .put("operationId", id)
                .put("viewId", state.viewId)
                .put("pageTitle", StringUtils.defaultIfBlank(state.pageTitle, state.viewId))
                .put("currentPageState", currentPageState(state))
                .put("readOnlyHelp", true)
                .put("recordValuesShared", false);

        if ("filter_records".equals(id))
            buildFilterRecords(state, help);
        else if ("advanced_search".equals(id))
            buildAdvancedSearch(state, help);
        else if ("query_view_presets".equals(id))
            buildQueryViewPresets(state, help);
        else if ("sort_records".equals(id))
            buildSortRecords(state, help);
        else if ("display_columns".equals(id))
            buildDisplayColumns(state, help);
        else if ("batch_update_records".equals(id))
            buildBatchUpdateRecords(state, help);
        else if ("export_list_to_excel".equals(id))
            buildExportListToExcel(state, help);
        else if ("open_record_detail".equals(id))
            buildOpenDetail(state, help);
        else if ("add_record".equals(id))
            buildAddRecord(state, help);
        else if ("save_new_record".equals(id))
            buildSaveNewRecord(state, help);
        else if ("update_record".equals(id))
            buildUpdateRecord(state, help);
        else if ("save_record".equals(id))
            buildSaveRecord(state, help);
        else if ("delete_records".equals(id))
            buildDeleteRecords(state, help);
        else if ("delete_current_record".equals(id))
            buildDeleteCurrentRecord(state, help);
        else if ("return_to_list".equals(id))
            buildReturnToList(state, help);
        else if (composer.buildAiActionOperationHelp(id, help)) {
            // The linked action context populated the operation help.
        }
        else {
            help.put("known", false);
            help.put("available", false);
            help.put("message", "No exact operating guide is registered for this operation.");
        }

        composer.customizeAiHelpOperationHelp(id, help);
        return help;
    }

    private PageState state() {
        return composer.captureAiHelpPageState();
    }

    private JSONObject operation(String id, String description) throws JSONException {
        return new JSONObject().put("id", id).put("description", description);
    }

    private String currentPageState(PageState state) {
        if (state.detailForm != null && state.detailForm.isFormVisible())
            return "detail_" + modeName(state.detailForm.getCurMode());
        return "list";
    }

    private void availability(JSONObject help, boolean available, String unavailableReason)
            throws JSONException {
        help.put("known", true).put("available", available);
        if (!available)
            help.put("unavailableReason", unavailableReason);
    }

    private void buildFilterRecords(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canFilter,
                "Quick Filter is not enabled for this view or user session.");
        if (!state.canFilter)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        help.put("control", new JSONObject()
                .put("label", sessionHelper.getLabel("Quick Filter"))
                .put("componentId", "tbSearchBox")
                .put("trigger", "automatic_after_idle")
                .put("idleMilliseconds", 500)
                .put("pressEnterRequired", false)
                .put("searchButtonExists", false)
                .put("filterIconPurpose", "Optionally commit the current text as a persistent search tag"));
        help.put("liveFiltering", new JSONObject()
                .put("enabled", true)
                .put("scope", "currently loaded BI result records")
                .put("fields", "list-column display values")
                .put("alsoChecksCurrentTextAgainstAggregateOrPivotValues", true)
                .put("caseSensitive", false)
                .put("trimSurroundingWhitespace", true));
        help.put("matchingSyntax", new JSONObject()
                .put("plainText", "Case-insensitive contains match")
                .put("exactMatch", "Prefix the text with =, for example =B45")
                .put("wildcard", "Use * to match any sequence of characters, for example Chateau*Palmer"));
        help.put("searchTags", new JSONObject()
                .put("create", "Press Enter or click/open the filter icon to convert non-blank current text into a persistent tag")
                .put("effect", "The input box is cleared but filtering by the new tag continues")
                .put("remove", "Click the tag's x control")
                .put("popup", "Hover/open a tag to see matched columns and the match count for each column")
                .put("perColumnSwitches", "Use the tag popup switches to include or exclude matching columns for that tag"));
        help.put("tagCombination", new JSONObject()
                .put("controlLabel", sessionHelper.getBtLabel("Match All"))
                .put("checkedMeaning", "Every committed tag and any current text must match the record")
                .put("uncheckedMeaning", "At least one committed tag or the current text must match the record")
                .put("visibleOnMobile", false)
                .put("mobileBehavior", "The configured/default match mode remains active even though Match All is hidden"));
        help.put("urlPersistence", new JSONObject()
                .put("configurationDependent", true)
                .put("tagParameter", "qf")
                .put("matchModeParameter", "qfm")
                .put("behavior", "When enabled, committed tags and AND/OR mode are written to the page URL and restored when that URL is opened"));
        help.put("steps", new JSONArray()
                .put("Locate the Quick Filter box above the record list.")
                .put("Type the desired name, code or other keyword into the box.")
                .put("Stop typing and wait about 0.5 second; the currently loaded list filters automatically.")
                .put("Optionally press Enter or click/open the filter icon to keep that text as a search tag, then enter additional tags.")
                .put("Use Match All on desktop to choose whether all tags or any tag must match."));
        help.put("notes", new JSONArray()
                .put("Do not tell the user that Enter or an icon click is required for ordinary filtering; those actions only commit the text as a tag.")
                .put("Records already marked as updated or deleted remain visible even when they do not match the filter.")
                .put("This operation filters the currently loaded records; it does not by itself count matching records for the AI."));
    }

    private void buildDisplayColumns(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canDisplayColumns,
                "Display Column is available only in the desktop tabular list, not the mobile card layout.");
        if (!state.canDisplayColumns)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        help.put("control", new JSONObject()
                .put("menuLabel", sessionHelper.getLabel("Display Column"))
                .put("openMenu", "Right-click any visible column header in the list")
                .put("dialogTitle", sessionHelper.getLabel("Display Column"))
                .put("selectionControl", "One checkbox for each displayable list column")
                .put("selectAllControl", sessionHelper.getLabel("Select All / Select None"))
                .put("applyButton", "OK")
                .put("cancelButton", "Cancel"));
        help.put("hideColumnSteps", new JSONArray()
                .put("Right-click any visible column header to open the column-header popup menu.")
                .put("Choose Display Column.")
                .put("In the Display Column dialog, clear the checkbox beside the column to hide.")
                .put("Click OK to apply the column visibility selection."));
        help.put("showColumnSteps", new JSONArray()
                .put("Right-click any remaining visible column header and choose Display Column.")
                .put("Check the checkbox beside the hidden column to show it again.")
                .put("Click OK to apply the column visibility selection."));
        help.put("notes", new JSONArray()
                .put("The header that is right-clicked only opens the shared menu; the dialog can show or hide other columns too.")
                .put("Checked means displayed; unchecked means hidden.")
                .put("Select All / Select None changes all available column checkboxes at once.")
                .put("The visibility changes are applied only after OK; Cancel discards the dialog selection.")
                .put("Showing or hiding a column changes only the list layout. It does not filter, update or delete record data."));
    }

    private void buildAdvancedSearch(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canAdvancedSearch,
                "Advanced Search is not enabled or visible for this page and device.");
        if (!state.canAdvancedSearch)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        JSONObject variants = new JSONObject()
                .put("g2DialogAvailable", state.advancedSearchG2Available)
                .put("legacyColumnFooterAvailable", state.advancedSearchG1Available)
                .put("embeddedFormVisible", state.embeddedAdvancedSearchVisible)
                .put("embeddedAutoSearch", state.embeddedAdvancedSearchAuto)
                .put("mobileLayout", state.mobileLayout);
        help.put("variants", variants);
        help.put("currentState", new JSONObject()
                .put("customConditionActive", state.advancedSearchConditionActive)
                .put("differsFromSelectedPreset", state.advancedSearchModified)
                .put("selectedPreset", state.selectedPreset == null ? JSONObject.NULL : state.selectedPreset)
                .put("recordLimit", state.advancedSearchRecordLimit));
        help.put("scope", new JSONObject()
                .put("type", "server_query")
                .put("behavior", "Search reloads records using the constructed conditions and record limit")
                .put("differenceFromQuickFilter", "Quick Filter only filters the currently loaded BI result; Advanced Search changes the query used to load records"));
        help.put("operators", new JSONArray()
                .put("equal").put("not equal")
                .put("greater").put("less")
                .put("greater or equal").put("less or equal")
                .put("in").put("not in")
                .put("between").put("not between")
                .put("like").put("not like")
                .put("is blank").put("is not blank")
                .put("contains").put("not contains"));
        help.put("operatorNotes", new JSONArray()
                .put("The operator dropdown is field-type aware; not every operator is shown for every field.")
                .put("Between and Not Between request two values.")
                .put("In and Not In accept multiple selected or entered values.")
                .put("Is Blank and Is Not Blank do not require a value.")
                .put("Use Contains for a normal substring condition; do not invent SQL percent-pattern syntax unless the UI explicitly requests Like."));
        putAdvancedSearchFields(state, help);

        if (state.advancedSearchG2Available) {
            help.put("g2Controls", new JSONObject()
                    .put("openButtonId", "btAdvancedSearchG2")
                    .put("openButtonLabel", sessionHelper.getTtLabel("Advanced Search"))
                    .put("applyButton", sessionHelper.getBtLabel("Search"))
                    .put("closeButton", sessionHelper.getBtLabel("Close"))
                    .put("addBlockButton", sessionHelper.getBtLabel("Add Block"))
                    .put("recordLimit", sessionHelper.getLabel("Record Limit")));
            help.put("g2Steps", new JSONArray()
                    .put("Click the Advanced Search button to open the G2 Advanced Search dialog.")
                    .put("On desktop, add a field to the active Condition Block by clicking its plus icon or dragging the field into the block.")
                    .put("For each condition row, choose the operator and enter or select the required value or values.")
                    .put("Choose All Field Mode or Any Field Mode according to the required field/block combination.")
                    .put("Optionally select an existing preset and adjust the positive Record Limit.")
                    .put("Click Search to apply the conditions and reload the record list. Click Close to leave without applying the dialog changes."));
            help.put("g2CombinationModes", new JSONObject()
                    .put("allFieldMode", "Conditions inside each block are joined with AND; separate condition blocks are joined with OR")
                    .put("anyFieldMode", "Conditions inside each block are joined with OR; separate condition blocks are joined with AND")
                    .put("addBlockPermission", state.adminUser
                            ? "Add Block is enabled for this administrator session"
                            : "Add Block is disabled for non-administrator sessions"));
            help.put("g2Editing", new JSONArray()
                    .put("Desktop condition rows can be moved up, moved down or deleted.")
                    .put("The active condition block is the block most recently clicked; newly added fields go into that block.")
                    .put("The red dot on the Advanced Search button means the active custom condition differs from the selected preset.")
                    .put("On mobile, the field list, mode selector and Add Block control are hidden; only the reduced mobile condition UI is available."));
            help.put("presets", new JSONObject()
                    .put("choose", "Select a saved preset to restore its search conditions and record limit")
                    .put("save", "Use Save Preset when the control is enabled and the user is permitted to save it")
                    .put("delete", "Use Delete Preset only when enabled for the selected preset")
                    .put("default", "The default checkbox can make an eligible non-public preset the user's default"));
        }

        if (state.advancedSearchG1Available) {
            help.put("legacyFooter", new JSONObject()
                    .put("openButtonId", "btAdvancedSearch")
                    .put("open", "Click Advanced Search to show or hide condition inputs aligned with the list columns")
                    .put("apply", "Choose an operator, enter the value and press Enter; the condition is added and the records reload")
                    .put("sameFieldCombination", "Multiple conditions entered for the same field are joined with OR")
                    .put("differentFieldCombination", "Conditions for different fields are applied together")
                    .put("clear", "Use Clear Conditions to remove the footer conditions and reload")
                    .put("presetControlsVisible", state.legacyAdvancedSearchPresetControls));
        }

        if (state.embeddedAdvancedSearchVisible) {
            help.put("embeddedSearch", new JSONObject()
                    .put("location", "Advanced Search condition panel above the list")
                    .put("apply", state.embeddedAdvancedSearchAuto
                            ? "AUTO mode reapplies the query when a condition changes"
                            : "Edit the displayed conditions and click Search")
                    .put("reset", "Click Reset to restore the selected preset's default conditions and reload"));
        }

        help.put("notes", new JSONArray()
                .put("Advanced Search is read-only with respect to business records, but Search reloads the list from the data source.")
                .put("The record limit caps how many matching records are loaded; it is not itself a filter condition.")
                .put("This helper never constructs raw custom predicates or SQL for the user and never clicks Search."));
    }

    private void putAdvancedSearchFields(PageState state, JSONObject help) throws JSONException {
        JSONArray fields = new JSONArray();
        Set<String> seenLabels = new LinkedHashSet<String>();
        int total = 0;
        Vector<BiResult> results = new Vector<BiResult>();
        results.add(state.result);
        if (state.result.getSubLinks() != null)
            results.addAll(state.result.getSubLinks());
        for (BiResult result : results) {
            for (BiColumn column : result.getColumns()) {
                if (column.isNoQuery()
                        || (column.isInvisible(result.getSessionHelper()) && !column.isInList(result.getSessionHelper()))
                        || (result != state.result && column.getField() == null)
                        || !seenLabels.add(column.getLabel()))
                    continue;
                total++;
                if (fields.length() < 200) {
                    fields.put(new JSONObject()
                            .put("id", column.getLabel())
                            .put("name", column.getEngName())
                            .put("type", column.getColumnType())
                            .put("sourceView", result.getView().getName()));
                }
            }
        }
        help.put("queryFields", fields);
        help.put("queryFieldCount", total);
        help.put("queryFieldListTruncated", total > fields.length());
    }

    private void buildQueryViewPresets(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canQueryViewPresets,
                "Query and View Presets are not available for this page or device.");
        if (!state.canQueryViewPresets)
            return;

        help.put("currentState", new JSONObject()
                .put("presetCount", state.presetCount)
                .put("selectedPreset", state.selectedPreset == null ? JSONObject.NULL : state.selectedPreset)
                .put("selectedPresetLabel", state.selectedPresetLabel == null
                        ? JSONObject.NULL : state.selectedPresetLabel)
                .put("selectedPresetIsUserPreset", state.selectedPresetCustom)
                .put("selectedPresetIsDefault", state.selectedPresetDefault)
                .put("defaultPreset", state.defaultPreset == null ? JSONObject.NULL : state.defaultPreset)
                .put("selectorVisibleInQueryBar", state.presetSelectorVisible)
                .put("managementEnabled", state.presetManagementEnabled));
        help.put("purpose", "A Query and View Preset restores a saved server query and its matching list presentation as one named setup.");
        help.put("savedSettings", new JSONArray()
                .put("Advanced Search field conditions and condition-block logic")
                .put("Custom query condition, when one already belongs to the preset")
                .put("Record limit")
                .put("Displayed and hidden columns")
                .put("Column display order")
                .put("Single-column or multi-column sort order, direction and priority")
                .put("Frozen-column count")
                .put("Where supported by the view: pivot columns, hidden aggregates and ad-hoc/generated columns"));
        help.put("selectPreset", new JSONObject()
                .put("location", state.presetSelectorVisible
                        ? "Use the preset dropdown in the query bar; the same selector is also available in the G2 Advanced Search dialog when that dialog is enabled"
                        : "Open the G2 Advanced Search dialog and use its preset dropdown")
                .put("steps", new JSONArray()
                        .put("Open the preset dropdown and choose the required named preset.")
                        .put("The page restores the preset's search conditions, record limit, column layout and sorting.")
                        .put("The list query/view is reapplied; wait for the records and headers to refresh before continuing."))
                .put("defaultIndicator", "A default preset is displayed in bold in the preset list."));

        JSONObject save = new JSONObject()
                .put("available", state.presetManagementEnabled)
                .put("steps", new JSONArray()
                        .put("Set the required Advanced Search conditions and Record Limit.")
                        .put("Arrange the list: show or hide columns, put columns in the required order, and apply the required single or multi-column sorting.")
                        .put(state.advancedSearchG2Available
                                ? "Open Advanced Search and click the plus / Save Preset control."
                                : "Click Save Preset in the query controls.")
                        .put("Enter a non-blank user-defined preset name.")
                        .put("Optionally select Set as default, then click OK to save the complete query and view setup."))
                .put("updateExisting", "Saving with the selected existing preset name updates that eligible preset; choose a new name to keep the existing preset unchanged.")
                .put("userPreset", "A normal user saves a private/custom preset belonging to that login profile.")
                .put("publicPreset", state.adminUser
                        ? "This administrator may use Public mode; an access key may also be requested when that feature is configured."
                        : "Public mode is administrator-controlled; this user cannot create or update a public preset.");
        if (!state.presetManagementEnabled)
            save.put("unavailableReason", "Preset save/delete controls are hidden because this preset configuration was supplied from the page URL.");
        help.put("savePreset", save);

        help.put("defaultPreset", new JSONObject()
                .put("behavior", "The applicable default preset is selected and applied automatically when this BI page is first opened, before its initial query and list layout are completed.")
                .put("setWhileSaving", "Select Set as default in the Save Preset dialog.")
                .put("setFromSelector", "For an eligible private/custom preset, select it and use the default checkbox beside the preset selector.")
                .put("publicRestriction", "The inline default checkbox is not enabled for public presets; public-preset administration is restricted to administrators."));
        help.put("deletePreset", new JSONObject()
                .put("available", state.presetManagementEnabled)
                .put("steps", new JSONArray()
                        .put("Select the preset to remove.")
                        .put("Click Delete Preset / the minus control and confirm the prompt."))
                .put("permissions", "Users may delete their own private/custom presets. Only an administrator may delete a public preset."));
        help.put("notes", new JSONArray()
                .put("Selecting a preset changes the query and page presentation together; it does not update, add or delete business records.")
                .put("Quick Filter text is not part of the Query and View Preset; the saved query is the Advanced Search query.")
                .put("Make all desired query, column and sort changes before saving, because the preset captures the current setup.")
                .put("This Helper only explains the feature. It never selects, saves, overwrites, defaults or deletes a preset."));
    }

    private void buildSortRecords(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canSortColumns,
                "Column sorting is available only when the tabular list and its column headers are displayed.");
        if (!state.canSortColumns)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        help.put("controls", new JSONObject()
                .put("openMenu", "Right-click the column header to be sorted")
                .put("ascending", sessionHelper.getLabel("Sort Ascending"))
                .put("descending", sessionHelper.getLabel("Sort Descending"))
                .put("multiColumn", sessionHelper.getLabel("Sort Multicolumn"))
                .put("clear", sessionHelper.getLabel("Clear")));
        help.put("singleColumnSteps", new JSONArray()
                .put("Locate the desired column header in the tabular record list.")
                .put("Right-click that column header to open its popup menu.")
                .put("Choose Sort Ascending for ascending order or Sort Descending for descending order."));
        help.put("singleColumnBehavior", new JSONObject()
                .put("replacesPreviousSort", true)
                .put("headerClickAlternative", "Clicking the column header also sorts and toggles its direction; use the popup menu when a specific direction is required"));
        help.put("multiColumnSteps", new JSONArray()
                .put("Right-click the first-priority column header.")
                .put("Open Sort Multicolumn and choose Ascending or Descending.")
                .put("Repeat on each additional column in the desired priority order.")
                .put("Numbered sort indicators show the active multi-column priority."));
        help.put("clearSteps", new JSONArray()
                .put("Right-click any visible column header.")
                .put("Open Sort Multicolumn and choose Clear to remove the active column sorting."));
        help.put("notes", new JSONArray()
                .put("Sorting reorders the currently loaded records and preserves any active Quick Filter.")
                .put("Sorting does not update, save or delete record data.")
                .put("Do not claim a particular text collation or case-ordering rule because it is not exposed by this help tool."));
    }

    private void buildBatchUpdateRecords(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canBatchUpdate,
                "Batch Update requires the desktop tabular list, G2 form support, view permission and at least one eligible update column.");
        if (!state.canBatchUpdate)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        JSONArray columns = new JSONArray();
        Vector<BiColumn> listColumns = state.result.getListColumns();
        for (BiColumn column : listColumns) {
            if (!column.isNoUpdate(sessionHelper) && column.allowBatchUpdate()) {
                columns.put(new JSONObject()
                        .put("id", column.getLabel())
                        .put("name", column.getEngName())
                        .put("type", column.getColumnType()));
            }
        }

        help.put("eligibleColumns", columns);
        help.put("controls", new JSONObject()
                .put("openMenu", "Right-click the header of the column to update")
                .put("menuLabel", sessionHelper.getLabel("Batch Update"))
                .put("valueActionLabel", sessionHelper.getBtLabel("Proceed"))
                .put("leaveBatchModeLabel", sessionHelper.getBtLabel("Back"))
                .put("finalConfirmTitle", sessionHelper.getLabel("Confirm Save Changes?"))
                .put("commitButton", "OK")
                .put("rollbackButton", "Cancel"));
        help.put("steps", new JSONArray()
                .put("Optionally use Quick Filter first to narrow the currently loaded list.")
                .put("Right-click the header of the eligible column whose value should be changed.")
                .put("Choose Batch Update. The page enters multi-select mode and preselects that column in the Batch Update bar.")
                .put("Select the records to update using the row selection checkboxes.")
                .put("Enter or choose the new value in the input created for that column's data type.")
                .put("Review the selected records and new value, then click Proceed.")
                .put("At Confirm Save Changes, verify the displayed updated-record count. Click OK to commit or Cancel to roll back the batch.")
                .put("Click Back when finished to leave the Batch Update panel."));
        help.put("selectionLimit", new JSONObject()
                .put("applies", !state.adminUser)
                .put("maximumRows", state.adminUser ? JSONObject.NULL : state.batchUpdateMaxRows)
                .put("source", state.adminUser
                        ? "Administrators are not restricted by the #maxupd check"
                        : "Default 100, optionally overridden by the login profile's #maxupd access-right value"));
        help.put("transactionBehavior", new JSONObject()
                .put("proceedPreparesUpdates", true)
                .put("finalApprovalRequired", true)
                .put("ok", "Commits the batch transaction")
                .put("cancel", "Rolls back the batch transaction")
                .put("rowFailure", "Stops processing, rolls back the transaction and displays Update Failed"));
        help.put("notes", new JSONArray()
                .put("Batch Update assigns one chosen value to the same selected column in every selected record.")
                .put("The Batch Update menu is disabled when the right-clicked column is not eligible.")
                .put("Only the columns returned in eligibleColumns should be described as batch-updateable.")
                .put("The input control may be a text, number, date, list, radio or other field-appropriate selector.")
                .put("This help tool only explains the workflow; it never selects rows, changes values, clicks Proceed or confirms the transaction."));
        help.put("continuousBatchMode", state.continuousBatchUpdate);
    }

    private void buildExportListToExcel(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canExportList,
                state.mobileLayout
                        ? "Excel export is disabled in the mobile layout. Open this page in the desktop layout to export."
                        : "Export is disabled for this BI view or the list is not currently displayed.");
        if (!state.canExportList)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        help.put("control", new JSONObject()
                .put("buttonId", "btExport")
                .put("buttonLabel", sessionHelper.getBtLabel("Export"))
                .put("location", "Open the Import/Export button group on the list action bar, then choose Export")
                .put("hotkey", "E, when the page hotkey is active"));
        help.put("currentScope", new JSONObject()
                .put("rowCount", state.exportRowCount)
                .put("scope", "all rows in the current list model")
                .put("selectedRowsOnly", false)
                .put("includesRowsHiddenByQuickFilter", false)
                .put("includesRecordsBeyondRecordLimit", false)
                .put("columns", "currently displayed list columns, in the current list-view order")
                .put("hiddenColumnsIncluded", false)
                .put("sortOrder", "the current filtered and sorted list order"));
        help.put("steps", new JSONArray()
                .put("Finish the required Advanced Search, Quick Filter, column display/order and sorting setup first.")
                .put("Open Import/Export on the list action bar and click Export.")
                .put("In Confirm export to excel, enter a file name using letters, numbers, hyphen or underscore only.")
                .put("Choose any optional export settings, then click OK.")
                .put("Wait for the Processing progress window to finish and for the browser download to start."));
        help.put("options", new JSONArray()
                .put(new JSONObject()
                        .put("name", "Password protect")
                        .put("behavior", "Prompts for a password and downloads a password-protected ZIP containing the XLSX workbook"))
                .put(new JSONObject()
                        .put("name", "Merge common field")
                        .put("behavior", "Merges repeated common master-record cells where the export supports it"))
                .put(new JSONObject()
                        .put("name", "As Import Template")
                        .put("behavior", "Exports the view's importable columns and input aids instead of the normal currently displayed-column layout"))
                .put(new JSONObject()
                        .put("name", "Export All Detail")
                        .put("behavior", "Includes configured export-link/detail record data in addition to the master list")));
        help.put("notes", new JSONArray()
                .put("Normal export includes every row remaining in the current list after Quick Filter, not only the highlighted or checked rows.")
                .put("Only records already loaded by the current server query are available to export; increase/refine the Advanced Search Record Limit and rerun the query first if necessary.")
                .put("Hidden columns are omitted from normal export. Show a required column before exporting, or use As Import Template when that is the intended format.")
                .put("Exporting creates a download and does not add, update, delete or commit ERP records.")
                .put("This Helper explains the controls but never starts an export or downloads a file."));
    }

    private void buildOpenDetail(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canOpenDetail,
                "Record detail is unavailable for this view, selection mode or logged-in user.");
        if (!state.canOpenDetail)
            return;

        if (state.mobileRecordCards) {
            help.put("control", new JSONObject()
                    .put("label", "desired record card")
                    .put("location", "record list")
                    .put("trigger", "single_tap"));
            help.put("steps", new JSONArray()
                    .put("Find the desired record in the list, using Quick Filter first when helpful.")
                    .put("Tap the desired record card once.")
                    .put("Wait for the record detail form to open."));
            help.put("notes", new JSONArray()
                    .put("In the mobile card layout, tap the record itself; no separate Record Detail icon is required."));
        }
        else {
            help.put("control", new JSONObject()
                    .put("label", getAiHelpSessionHelper().getTtLabel("Record Detail"))
                    .put("location", "left side of the desired record row")
                    .put("appearance", "small record-detail/pencil icon")
                    .put("trigger", "single_click"));
            help.put("steps", new JSONArray()
                    .put("Find the desired record in the list, using Quick Filter first when helpful.")
                    .put("Click the small Record Detail icon at the far left of that record's row.")
                    .put("Wait for the record detail form to open."));
            help.put("notes", new JSONArray()
                    .put("Click the row's Record Detail icon; do not instruct the user to double-click the row."));
        }
    }

    private void buildUpdateRecord(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canUpdate,
                "Updating records is unavailable for this view or logged-in user.");
        if (!state.canUpdate)
            return;

        JxZkBiBase form = state.detailForm;
        boolean detailVisible = form != null && form.isFormVisible();
        help.put("detailMode", detailVisible ? modeName(form.getCurMode()) : "not_open");
        JSONArray steps = new JSONArray();
        if (!detailVisible) {
            steps.put("Find the desired record, optionally by typing into Quick Filter and waiting about 0.5 second for automatic filtering.");
            steps.put(state.mobileRecordCards
                    ? "Tap the desired record card once."
                    : "Click the small Record Detail icon at the far left of the desired row.");
        }
        if (!detailVisible || form.getCurMode() == JxZkBiBase.MODE_DISPLAY)
            steps.put("If the detail form is read-only, click Edit to enter update mode.");
        steps.put("Modify the permitted fields in the detail form.");
        steps.put("After making a change, click Save.");
        steps.put("If validation reports an error, correct the indicated field and click Save again.");
        help.put("steps", steps);
        SessionHelper sessionHelper = getAiHelpSessionHelper();
        help.put("controls", new JSONObject()
                .put("openDetail", state.mobileRecordCards
                        ? "tap desired record card"
                        : sessionHelper.getTtLabel("Record Detail"))
                .put("edit", sessionHelper.getBtLabel("Edit"))
                .put("save", sessionHelper.getBtLabel("Save")));
        help.put("notes", new JSONArray()
                .put("The detail form may open directly in update mode; use Edit only when it opens read-only.")
                .put("Save can remain disabled until a field has actually changed."));
    }

    private void buildAddRecord(PageState state, JSONObject help) throws JSONException {
        boolean addFormOpen = state.detailForm != null && state.detailForm.isFormVisible()
                && state.detailForm.getCurMode() == JxZkBiBase.MODE_ADD;
        availability(help, state.canAdd || addFormOpen,
                "Adding records is unavailable for this view, page state or logged-in user.");
        if (!state.canAdd && !addFormOpen)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        help.put("controls", new JSONObject()
                .put("startAdd", sessionHelper.getBtLabel("Add"))
                .put("startAddComponentId", "btAdd")
                .put("saveNew", sessionHelper.getTtLabel("Save New Record"))
                .put("saveNewComponentId", "btAdd"));
        JSONArray steps = new JSONArray();
		if (!addFormOpen && state.detailForm != null && state.detailForm.isFormVisible())
			steps.put("Close the current detail form and return to the record list. Resolve any unsaved-change warning before continuing.");
        if (!addFormOpen)
            steps.put("Click Add in the list page action bar to open a blank record detail form in add mode.");
        steps.put("Enter the required values and any permitted optional values in the new-record form.");
        steps.put("Click Save New Record in the detail form.");
        steps.put("If validation reports an error, correct the indicated field and click Save New Record again.");
        steps.put("After a successful save, choose one of the follow-up choices offered by the form, such as adding another record, updating the new record, or closing, when those choices are available.");
        help.put("steps", steps);
        help.put("fieldGuidance", new JSONObject()
                .put("editableFieldsSource", "get_page_context fields[].editableWhenAdding")
                .put("note", "Required values and validation rules are view-specific. Do not guess them when they are not supplied by the page."));
        help.put("commitPoint", "Clicking Save New Record starts validation and, when validation succeeds, permanently adds the record.");
        help.put("notes", new JSONArray()
                .put("The Add button on the list starts a new record; the Save New Record button inside the add form commits it. Both controls use component id btAdd in their respective ZK id spaces.")
                .put("Closing the add form before a successful save does not add the new record."));
    }

    private void buildSaveNewRecord(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canSaveNew,
                "No enabled new-record form is currently open.");
        if (!state.canSaveNew)
            return;

        help.put("control", new JSONObject()
                .put("label", getAiHelpSessionHelper().getTtLabel("Save New Record"))
                .put("componentId", "btAdd")
                .put("location", "open record detail form")
                .put("trigger", "single_click"));
        help.put("steps", new JSONArray()
                .put("Complete the required fields and review the permitted optional fields in the open add form.")
                .put("Click Save New Record.")
                .put("If validation reports an error, correct the indicated value and click Save New Record again.")
                .put("After a successful save, choose the desired follow-up option offered by the form."));
        help.put("commitPoint", "A successful Save New Record permanently creates the record.");
    }

    private void buildDeleteRecords(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canDeleteRecords,
                "Deleting records from the list is unavailable for this view, device or logged-in user.");
        if (!state.canDeleteRecords)
            return;

        SessionHelper sessionHelper = getAiHelpSessionHelper();
        help.put("controls", new JSONObject()
                .put("delete", sessionHelper.getBtLabel("Delete"))
                .put("deleteComponentId", "btDelete")
                .put("proceed", sessionHelper.getBtLabel("Proceed"))
                .put("back", sessionHelper.getBtLabel("Back"))
                .put("finalConfirmation", sessionHelper.getLabel("Confirm Save Changes?")));
        JSONArray steps = new JSONArray();
		if (state.detailForm != null && state.detailForm.isFormVisible())
			steps.put("Close the current detail form and return to the record list. Resolve any unsaved-change warning before continuing.");
        if (!state.deleteSelectionActive)
            steps.put("Click Delete in the list page action bar. This enters the Delete batch-selection workflow; it does not immediately delete a record.");
        steps.put("Select every record that should be deleted. Review the selection carefully.");
        steps.put("Click Proceed to mark the selected records for deletion, or Back to leave the Delete workflow without proceeding.");
        steps.put("Review the added/updated/deleted count in the Confirm Save Changes dialog.");
        steps.put("Click OK to commit the deletion, or Cancel to cancel and refresh instead of committing it.");
        help.put("steps", steps);
        help.put("selection", new JSONObject()
                .put("currentSelectedRowCount", state.selectedRowCount)
                .put("multipleRecordsSupported", true)
                .put("desktopOnly", true));
        help.put("commitPoint", "The selected records are permanently deleted only after OK is chosen in Confirm Save Changes.");
        help.put("warning", "Deletion is destructive. Confirm the selected rows and the deleted count before clicking OK.");
    }

    private void buildDeleteCurrentRecord(PageState state, JSONObject help) throws JSONException {
        availability(help, state.canDeleteCurrent,
                "The current record cannot be deleted in the present detail mode or by this logged-in user.");
        if (!state.canDeleteCurrent)
            return;

        help.put("control", new JSONObject()
                .put("label", getAiHelpSessionHelper().getBtLabel("Delete"))
                .put("componentId", "btDelCurrent")
                .put("location", "open record detail form")
                .put("trigger", "single_click"));
        help.put("steps", new JSONArray()
                .put("Verify that the open detail form is the record that should be deleted.")
                .put("Click Delete in the detail form.")
                .put("At Confirm Delete ?, click Yes to permanently delete the current record, or No to keep it.")
                .put("After a successful deletion, the detail form closes and the list is marked for refresh."));
        help.put("commitPoint", "Clicking Yes in Confirm Delete ? permanently deletes the current record.");
        help.put("warning", "This operation is destructive and acts on the single record currently open in the detail form.");
    }

    private void buildSaveRecord(PageState state, JSONObject help) throws JSONException {
        JxZkBiBase form = state.detailForm;
        boolean visible = form != null && form.isFormVisible();
        boolean available = state.canUpdate && visible && form.getCurMode() == JxZkBiBase.MODE_UPDATE;
        availability(help, available, !visible
                ? "Open a record detail form before saving."
                : "The open detail form is not currently in update mode.");
        if (!available)
            return;

        help.put("control", new JSONObject()
                .put("label", getAiHelpSessionHelper().getBtLabel("Save"))
                .put("componentId", "btUpdate")
                .put("trigger", "single_click"));
        help.put("steps", new JSONArray()
                .put("Make the required changes in the open detail form.")
                .put("Click Save after it becomes enabled.")
                .put("Correct any validation error shown by the form, then click Save again."));
        help.put("notes", new JSONArray()
                .put("Save can remain disabled until a field has actually changed."));
    }

    private void buildReturnToList(PageState state, JSONObject help) throws JSONException {
        JxZkBiBase form = state.detailForm;
        boolean available = form != null && form.isFormVisible();
        availability(help, available, "No record detail form is currently open.");
        if (!available)
            return;

        help.put("control", new JSONObject()
                .put("label", getAiHelpSessionHelper().getBtLabel("Close"))
                .put("componentId", "btClose")
                .put("trigger", "single_click"));
        help.put("steps", new JSONArray()
                .put("Click Close on the record detail form.")
                .put("If the page warns about unsaved changes, choose whether to keep editing or discard them."));
    }

    private String modeName(int mode) {
        switch (mode) {
        case JxZkBiBase.MODE_ADD:
            return "add";
        case JxZkBiBase.MODE_UPDATE:
            return "update";
        case JxZkBiBase.MODE_DISPLAY:
            return "display";
        default:
            return "unknown";
        }
    }

    /** Package-private immutable-by-convention snapshot populated by the composer. */
    static final class PageState {
        String invokerClass;
        String invokerComponentId;
        String pageTitle;
        String pageId;
        String viewId;
        String helpId;
        String pageAction;
        boolean listVisible;
        boolean detailOpen;
        boolean multiSelect;
        int selectedRowIndex;
        int selectedRowCount;
        int renderedRowCount;
        BiResult result;
        JxZkBiBase detailForm;
        boolean canFilter;
        boolean canAdvancedSearch;
        boolean advancedSearchG2Available;
        boolean advancedSearchG1Available;
        boolean embeddedAdvancedSearchVisible;
        boolean embeddedAdvancedSearchAuto;
        boolean advancedSearchConditionActive;
        boolean advancedSearchModified;
        boolean legacyAdvancedSearchPresetControls;
        boolean mobileLayout;
        int advancedSearchRecordLimit;
        String selectedPreset;
        boolean canQueryViewPresets;
        boolean presetSelectorVisible;
        boolean presetManagementEnabled;
        boolean selectedPresetCustom;
        boolean selectedPresetDefault;
        int presetCount;
        String selectedPresetLabel;
        String defaultPreset;
        boolean canSortColumns;
        boolean canDisplayColumns;
        boolean canBatchUpdate;
        boolean canExportList;
        int exportRowCount;
        boolean canOpenDetail;
        boolean canAdd;
        boolean canSaveNew;
        boolean canUpdate;
        boolean canDeleteRecords;
        boolean deleteSelectionActive;
        boolean canDeleteCurrent;
        boolean mobileRecordCards;
        boolean adminUser;
        int batchUpdateMaxRows;
        boolean continuousBatchUpdate;
    }
}
