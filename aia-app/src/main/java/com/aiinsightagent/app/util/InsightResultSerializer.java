package com.aiinsightagent.app.util;

import com.aiinsightagent.app.exception.InsightAppError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.InsightDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class InsightResultSerializer {

	private final ObjectMapper objectMapper;

	public InsightResultSerializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String serialize(InsightDetail detail) {
		try {
			return objectMapper.writeValueAsString(detail);
		} catch (Exception e) {
			throw new InsightException(InsightAppError.FAIL_SERIALIZE_INSIGHT_DETAIL);
		}
	}
}