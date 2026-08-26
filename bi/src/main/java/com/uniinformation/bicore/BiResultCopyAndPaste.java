package com.uniinformation.bicore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;

/**
 * Copies and pastes a BiResult record tree without depending on UI components.
 */
public final class BiResultCopyAndPaste {

	private BiResultCopyAndPaste() {
	}

	public static JSONObject copyCurrentCollection(BiResult result) throws Exception {
		if(result == null || result.getCurrentCollection() == null) {
			throw new IllegalArgumentException("Current BiResult collection is missing");
		}
		JSONObject json = BiCellCollectionToJsonInterface
				.BiCellCollectionToJSON(result.getCurrentCollection());
		result.beforeCopyCollection(result.getCurrentCollection(), json);
		return json;
	}

	/**
	 * Copies the current record tree, initializes the BiResult as a new record,
	 * and pastes the copied values into that new collection tree.
	 */
	public static ReturnMsg copyAndPasteCurrentCollection(BiResult result) throws Exception {
		ReturnMsg validation = result.beforeCopyToNew();
		if(validation != null && !validation.getStatus()) {
			return validation;
		}
		JSONObject json = copyCurrentCollection(result);
		result.clearCurrentRec();
		return pasteCurrentCollection(result, json, true);
	}

	public static ReturnMsg pasteCurrentCollection(BiResult result, JSONObject source) throws Exception {
		return pasteCurrentCollection(result, source, false);
	}

	private static ReturnMsg pasteCurrentCollection(BiResult result, JSONObject source,
			boolean copyToNew) throws Exception {
		if(result == null || result.getCurrentCollection() == null) {
			return new ReturnMsg(false, "Current BiResult collection is missing");
		}
		if(source == null) {
			return new ReturnMsg(false, "Copied collection JSON is missing");
		}

		// Work on a private copy: paste filtering must not modify clipboard data.
		JSONObject json = new JSONObject(source.toString());
		filterPasteValues(result, json);

		ReturnMsg rtn = result.beforePasteCollection(result.getCurrentCollection(), json);
		if(rtn != null && !rtn.getStatus()) {
			return rtn;
		}

		Map<BiResult, Boolean> actionStates = new LinkedHashMap<BiResult, Boolean>();
		setActionsEnabled(result, false, actionStates);
		try {
			rtn = JsonToBiCellCollectionInterface.JsonToBiCellCollection(
					result.getCurrentCollection(), json,
					new JsonToBiCellCollectionInterface() {
						@Override
						public ReturnMsg onAddSubRecord(BiResult parentBr,
								BiResult sublinkBr, int idx) {
							return result.beforePasteSubRecord(parentBr, sublinkBr, idx);
						}
					});
		} finally {
			restoreActionStates(actionStates);
		}

		if(rtn != null && !rtn.getStatus()) {
			return rtn;
		}
		if(copyToNew) {
			result.resetToNew();
		}
		result.recalculateAggregateCellValues();
		return result.afterPasteCollection(result.getCurrentCollection(), json);
	}

	private static void filterPasteValues(BiResult result, JSONObject json) {
		for(BiColumn column : (Vector<BiColumn>) result.getColumns()) {
			if(!result.allowPasteColumn(column)) {
				json.remove(column.getLabel());
			}
		}
		Vector<BiResult> sublinks = result.getSubLinks();
		if(sublinks == null) return;
		for(BiResult sublink : sublinks) {
			JSONArray rows = json.optJSONArray(sublink.getView().getName());
			if(rows == null) continue;
			for(int i=0;i<rows.length();i++) {
				JSONObject row = rows.optJSONObject(i);
				if(row != null) filterPasteValues(sublink, row);
			}
		}
	}

	private static void setActionsEnabled(BiResult result, boolean enabled,
			Map<BiResult, Boolean> oldStates) {
		oldStates.put(result, result.isActionEnabled());
		result.setActionEnabled(enabled);
		Vector<BiResult> sublinks = result.getSubLinks();
		if(sublinks == null) return;
		for(BiResult sublink : sublinks) {
			setActionsEnabled(sublink, enabled, oldStates);
		}
	}

	private static void restoreActionStates(Map<BiResult, Boolean> oldStates) {
		for(Map.Entry<BiResult, Boolean> entry : oldStates.entrySet()) {
			entry.getKey().setActionEnabled(entry.getValue());
		}
	}
}
