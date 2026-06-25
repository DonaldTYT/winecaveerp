package com.uniinformation.birt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.birt.report.engine.api.script.IUpdatableDataSetRow;
import org.eclipse.birt.report.engine.api.script.ScriptException;
import org.eclipse.birt.report.engine.api.script.eventadapter.ScriptedDataSetEventAdapter;
import org.eclipse.birt.report.engine.api.script.instance.IDataSetInstance;

import com.uniinformation.utils.UniLog;

public class ScriptedDataSetEventHander extends ScriptedDataSetEventAdapter {
	private static Map<String, List<Map<String, Object>>> recordListMap = new ConcurrentHashMap<String, List<Map<String, Object>>>();
	private int recordNum;
	public static void clearRecordList() {
		recordListMap.clear();
	}
	public static void addRecordList(String dataSetName, List<Map<String, Object>> list) {
		recordListMap.put(dataSetName, list);
	}
	@Override
	public void open(IDataSetInstance dataSet) throws ScriptException {
		super.open(dataSet);
		UniLog.log1("open dataset %s, totalMemory:%dMB", dataSet.getName(), Runtime.getRuntime().totalMemory() / 1024 / 1024);
	}
	@Override
	public boolean fetch(IDataSetInstance dataSet, IUpdatableDataSetRow row) throws ScriptException {

		List<Map<String, Object>> recordList = recordListMap.get(dataSet.getName());
		if (recordList != null) {
			if (recordNum < recordList.size()) {
				Map<String, Object> map = recordList.get(recordNum);
				for (Map.Entry<String, Object> entry : map.entrySet())
					row.setColumnValue(entry.getKey(), entry.getValue());
				recordNum++;
				if (recordNum % 2500 == 0) {
					System.gc();
					UniLog.log1("recordNum:%d System.gc, totalMemory:%dMB", recordNum, Runtime.getRuntime().totalMemory() / 1024 / 1024);
				}
				return true;
			}
		}
		return false;
	}
	@Override
	public void close(IDataSetInstance dataSet) throws ScriptException {
		super.close(dataSet);
		UniLog.log1("close dataset %s, totalMemory:%dMB", dataSet.getName(), Runtime.getRuntime().totalMemory() / 1024 / 1024);
	}
}
