package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class JsonProcessor {
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String formatJson(String json) throws Exception {
        Object jsonObject = objectMapper.readValue(json, Object.class);
        return objectMapper.writeValueAsString(jsonObject);
    }

    public static String extractJsonElement(String json, String xpath) {
        try {
            Object result = JsonPath.read(json, xpath);
            return objectMapper.writeValueAsString(result);
        } catch (PathNotFoundException e) {
            return "未找到匹配的元素";
        } catch (Exception e) {
            return "处理过程中出错: " + e.getMessage();
        }
    }
}
