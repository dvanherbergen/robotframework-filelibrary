package io.github.dvanherbergen.filelibrary.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.dvanherbergen.filelibrary.FileLibraryException;

public class JsonUtil {

	public static String toJSON(Object obj) {

		try {
			return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new FileLibraryException(e);
		}
	}

	public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

		Iterator<String> fieldNames = updateNode.fieldNames();

		while (fieldNames.hasNext()) {
			String updatedFieldName = fieldNames.next();
			JsonNode valueToBeUpdated = mainNode.get(updatedFieldName);
			JsonNode updatedValue = updateNode.get(updatedFieldName);

			// If the node is an @ArrayNode
			if (valueToBeUpdated != null && updatedValue.isArray()) {
				// running a loop for all elements of the updated ArrayNode
				for (int i = 0; i < updatedValue.size(); i++) {
					JsonNode updatedChildNode = updatedValue.get(i);
					// Create a new Node in the node that should be updated, if
					// there was no corresponding node in it
					// Use-case - where the updateNode will have a new element
					// in its Array
					if (valueToBeUpdated.size() <= i) {
						((ArrayNode) valueToBeUpdated).add(updatedChildNode);
					}
					// getting reference for the node to be updated
					JsonNode childNodeToBeUpdated = valueToBeUpdated.get(i);
					merge(childNodeToBeUpdated, updatedChildNode);
				}
				// if the Node is an @ObjectNode
			} else if (valueToBeUpdated != null && valueToBeUpdated.isObject()) {
				merge(valueToBeUpdated, updatedValue);
			} else {
				if (mainNode instanceof ObjectNode) {
					((ObjectNode) mainNode).replace(updatedFieldName, updatedValue);
				}
			}
		}
		return mainNode;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String jsonString) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(jsonString);
			return mapper.convertValue(node, Map.class);
		} catch (IOException e) {
			throw new FileLibraryException(e);
		}

	}
}
