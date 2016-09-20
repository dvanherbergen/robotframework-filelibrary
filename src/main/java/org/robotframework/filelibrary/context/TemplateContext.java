package org.robotframework.filelibrary.context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.robotframework.filelibrary.util.JsonUtil;
import org.robotframework.filelibrary.util.TextUtil;

public class TemplateContext {

	private static TemplateContext instance;

	private Map<String, Object> values = new ConcurrentHashMap<String, Object>();

	public TemplateContext() {
		initDefaultValues();
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public static TemplateContext getInstance() {
		if (instance == null) {
			synchronized (TemplateContext.class) {
				if (instance == null) {
					instance = new TemplateContext();
				}
			}
		}
		return instance;
	}

	public void reset() {
		values.clear();
		initDefaultValues();
	}

	private void initDefaultValues() {
		values.put("now", new SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(new Date()));
		values.put("today", new SimpleDateFormat("dd/MM/YYYY").format(new Date()));
	}

	public void setValue(String attribute, String value) {
		setAttributeValue(attribute, expandJsonValue(value));
	}

	public void setValueFromTemplateData(String attribute, String value) {
		setAttributeValue(attribute, TemplateContext.getInstance().getValue(TextUtil.getVariableName(value)));
	}

	@SuppressWarnings("unchecked")
	private void setAttributeValue(String attribute, Object value) {

		attribute = cleanAttributePath(attribute);

		if (attribute.indexOf('.') == -1) {

			if (TextUtil.containsIndex(attribute)) {
				// TODO make more robust
				int index = TextUtil.getIndex(attribute);
				List<Map<String, Object>> list = (List<Map<String, Object>>) values.get(TextUtil.removeIndex(attribute));
				mergeValueInMap(list.get(index), attribute, value);
			} else {
				mergeValueInMap(values, attribute, value);
			}

		} else {

			// find correct child map where to add the value
			String attributes[] = attribute.split("\\.");
			Map<String, Object> targetMap = values;
			for (int i = 0; i < attributes.length - 1; i++) {

				String attributeName = TextUtil.removeIndex(attributes[i]);
				if (targetMap.containsKey(attributeName) && targetMap.get(attributeName) instanceof Map) {
					targetMap = (Map<String, Object>) targetMap.get(attributeName);
				} else if (targetMap.containsKey(attributeName) && targetMap.get(attributeName) instanceof List) {
					int index = TextUtil.getIndex(attributes[i]);
					List<Map<String, Object>> list = (List<Map<String, Object>>) targetMap.get(attributeName);
					targetMap = list.get(index);
				} else {
					Map<String, Object> childMap = new HashMap<String, Object>();
					targetMap.put(attributeName, childMap);
					targetMap = childMap;
				}
			}
			mergeValueInMap(targetMap, attributes[attributes.length - 1], value);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void mergeValueInMap(Map targetMap, String attribute, Object value) {

		if (value instanceof Map && targetMap.containsKey(attribute) && targetMap.get(attribute) instanceof Map) {
			Map mergeFromMap = (Map) value;
			Map mergeToMap = (Map) targetMap.get(attribute);
			Iterator<String> it = mergeFromMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				Object valueToMerge = mergeFromMap.get(key);
				mergeToMap.put(key, valueToMerge);
			}
		} else {
			targetMap.put(attribute, value);
		}

	}

	public Object expandJsonValue(String value) {
		try {
			return JsonUtil.toMap(value);
		} catch (Exception e) {
			return value;
		}
	}

	public String toJSON() {
		return JsonUtil.toJSON(getValues());
	}

	public void setValuesFromJSON(String value) {
		Map<String, Object> valueMap = JsonUtil.toMap(value);
		Iterator<String> it = valueMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Object content = valueMap.get(key);
			if (content != null) {
				values.put(key, content);
			}
		}
	}

	public void setValueList(String attribute, List<?> value) {
		setAttributeValue(attribute, value);
	}

	public void setValue(String attribute, Map<String, Object> value) {
		setAttributeValue(attribute, value);
	}

	@SuppressWarnings("unchecked")
	public Object getValue(String attribute) {

		Object value = null;

		if (attribute.indexOf('.') == -1) {
			value = values.get(attribute);
		} else {

			// find correct child map where to get the value from
			String attributes[] = attribute.split("\\.");
			Map<String, Object> sourceMap = values;
			for (int i = 0; i < attributes.length - 1; i++) {

				if (TextUtil.containsIndex(attributes[i])) {
					int index = TextUtil.getIndex(attributes[i]);
					String attributeName = TextUtil.removeIndex(attributes[i]);
					if (!(sourceMap.get(attributeName) instanceof List)) {
						return "";
					}
					List<Map<String, Object>> list = (List<Map<String, Object>>) sourceMap.get(attributeName);
					if (list.size() < index + 1) {
						return "";
					}
					sourceMap = list.get(index);

				} else {
					if (!(sourceMap.get(attributes[i]) instanceof Map)) {
						return "";
					}
					sourceMap = (Map<String, Object>) sourceMap.get(attributes[i]);
				}
			}
			value = sourceMap.get(attributes[attributes.length - 1]);
		}

		if (value == null) {
			value = "";
		}
		return value;
	}

	public static boolean isListTarget(String attributePath) {
		return attributePath.endsWith("[]");
	}

	private String cleanAttributePath(String path) {
		return path.replaceAll("\\[\\]", "");
	}

	public String[] resolveAttributes(String... attributePaths) {

		String[] results = new String[attributePaths.length];
		for (int i = 0; i < attributePaths.length; i++) {
			Object value = getValue(attributePaths[i]);
			if (value != null) {
				results[i] = (String) value.toString();
			}
		}

		return results;
	}

	public List<String> expandTargetAttributes(String attributePath) {

		List<String> paths = new ArrayList<>();

		if (attributePath.indexOf("[].") == -1) {
			paths.add(attributePath);
			return paths;
		}

		expandPathSegment("", values, attributePath, paths);
		return paths;
	}

	@SuppressWarnings("unchecked")
	private void expandPathSegment(String currentPath, Map<String, Object> sourceMap, String remainingPath, List<String> results) {

		String currentAttribute = TextUtil.getFirstSegment(remainingPath);

		remainingPath = TextUtil.getNextSegments(remainingPath);

		if (remainingPath == null) {
			// we cannot go deeper
			currentPath = TextUtil.addSegment(currentPath, currentAttribute);
			results.add(currentPath);
			return;
		} else {
			currentPath = TextUtil.addSegment(currentPath, TextUtil.removeIndex(currentAttribute));
		}

		if (TextUtil.containsIndex(currentAttribute)) {

			int index = TextUtil.getIndex(currentAttribute);
			currentAttribute = TextUtil.removeIndex(currentAttribute);

			Object o = sourceMap.get(currentAttribute);
			if (!(o instanceof List)) {
				// invalid path
				return;
			}
			List<Map<String, Object>> list = (List<Map<String, Object>>) sourceMap.get(currentAttribute);

			if (index == -1) {
				int listIndex = 0;
				for (Map<String, Object> nextSourceMap : list) {
					expandPathSegment(currentPath + "[" + listIndex++ + "]", nextSourceMap, remainingPath, results);
				}
			} else {
				if (list.size() > index) {
					Map<String, Object> nextSourceMap = list.get(index);
					expandPathSegment(currentPath + "[" + index + "]", nextSourceMap, remainingPath, results);
				}
			}

		} else {

			if (!(sourceMap.get(currentAttribute) instanceof Map)) {
				// path doesn't exist..
				return;
			}
			Map<String, Object> nextSourceMap = (Map<String, Object>) sourceMap.get(currentAttribute);
			expandPathSegment(currentPath, nextSourceMap, remainingPath, results);
		}

	}

}
