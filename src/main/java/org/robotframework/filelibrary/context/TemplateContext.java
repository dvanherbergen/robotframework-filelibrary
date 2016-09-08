package org.robotframework.filelibrary.context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.robotframework.filelibrary.util.JsonUtil;

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

	@SuppressWarnings("unchecked")
	private void setAttributeValue(String attribute, Object value) {

		attribute = cleanAttributePath(attribute);

		if (attribute.indexOf('.') == -1) {
			mergeValueInMap(values, attribute, value);
		} else {

			// find correct child map where to add the value
			String attributes[] = attribute.split("\\.");
			Map<String, Object> targetMap = values;
			for (int i = 0; i < attributes.length - 1; i++) {

				if (targetMap.containsKey(attributes[i]) && targetMap.get(attributes[i]) instanceof Map) {
					targetMap = (Map<String, Object>) targetMap.get(attributes[i]);
				} else {
					Map<String, Object> childMap = new HashMap<String, Object>();
					targetMap.put(attributes[i], childMap);
					targetMap = childMap;
				}
			}
			mergeValueInMap(targetMap, attributes[attributes.length - 1], value);
		}
	}

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

	public Object getValue(String attribute) {

		Object value = null;

		if (attribute.indexOf('.') == -1) {
			value = values.get(attribute);
		} else {

			// find correct child map where to get the value from
			String attributes[] = attribute.split("\\.");
			Map<String, Object> sourceMap = values;
			for (int i = 0; i < attributes.length - 1; i++) {
				if (!(sourceMap.get(attributes[i]) instanceof Map)) {
					return "";
				}
				sourceMap = (Map<String, Object>) sourceMap.get(attributes[i]);
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
			if (value instanceof String) {
				results[i] = (String) value;
			}
		}

		return results;
	}
}
