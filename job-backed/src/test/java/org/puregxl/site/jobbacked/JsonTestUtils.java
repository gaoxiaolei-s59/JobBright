package org.puregxl.site.jobbacked;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonTestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestUtils() {
    }

    public static String readJson(String json, String path) throws Exception {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        for (String key : path.split("\\.")) {
            node = node.get(key);
        }
        return node.asText();
    }
}
