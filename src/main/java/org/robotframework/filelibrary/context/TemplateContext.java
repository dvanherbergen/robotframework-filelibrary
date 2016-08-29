package org.robotframework.filelibrary.context;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.robotframework.filelibrary.util.JsonUtil;

public class TemplateContext {

	private static final ThreadLocal<TemplateContext> context = new ThreadLocal<TemplateContext>() {
		@Override
		protected TemplateContext initialValue() {
			return new TemplateContext();
		}
	};

	private Map<String, Object> values = new HashMap<String, Object>();

	public TemplateContext() {
		initDefaultValues();
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public static TemplateContext getCurrentContext() {
		return context.get();
	}

	public void reset() {
		values.clear();
		initDefaultValues();
	}

	private void initDefaultValues() {
		values.put("now", new Date());
	}

	public void setValue(String attribute, String value) {
		setAttributeValue(attribute, expandJsonValue(value));
	}

	@SuppressWarnings("unchecked")
	private void setAttributeValue(String attribute, Object value) {

		if (attribute.indexOf('.') == -1) {
			values.put(attribute, value);
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
			targetMap.put(attributes[attributes.length - 1], value);

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
			values.put(key, valueMap.get(key));
		}
	}

	public void setValues(String attribute, List<?> value) {
		setAttributeValue(attribute, value);
	}
}
