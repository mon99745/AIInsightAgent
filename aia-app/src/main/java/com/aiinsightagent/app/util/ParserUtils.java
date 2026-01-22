package com.aiinsightagent.app.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ParserUtils {
    private final ObjectMapper objectMapper;

    public ParserUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse context payload: {}", e.getMessage());
            return Map.of();
        }
    }

    public String toJson(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize data to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
