package com.aiinsightagent.app.util;


import com.aiinsightagent.app.exception.InsightAppError;
import com.aiinsightagent.app.exception.InsightAppException;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import org.springframework.util.CollectionUtils;

public class InsightRequestValidator {
	public static void validate(InsightRequest request) {
		if (!hasUserPrompt(request)) {
			throw new InsightAppException(InsightAppError.MISSING_USER_PROMPT_REQUEST);
		}
		validateUserId(request);
		validatePurpose(request);
		validateDataKey(request);
		validateDataFields(request);
	}

	public static void validateUserId(InsightRequest request) {
		if(request.getUserId() == null || request.getUserId().isEmpty()) {
			throw new InsightAppException(InsightAppError.EMPTY_USER_ID);
		}
	}

	public static void validatePurpose(InsightRequest request) {
		if(request.getPurpose() == null || request.getPurpose().isEmpty()) {
			throw new InsightAppException(InsightAppError.EMPTY_PURPOSE);
		}
	}

	public static boolean hasUserPrompt(InsightRequest request) {
		return !CollectionUtils.isEmpty(request.getUserPrompt());
	}

	public static void validateDataKey(InsightRequest request) {
		if (!hasUserPrompt(request)) return;

		for (UserPrompt prompt : request.getUserPrompt()) {
			if (prompt.getDataKey() == null || prompt.getDataKey().isEmpty()) {
				throw new InsightAppException(InsightAppError.EMPTY_DATA_KEY);
			}
		}
	}

	public static void validateDataFields(InsightRequest request) {
		if (!hasUserPrompt(request)) return;

		for (UserPrompt prompt : request.getUserPrompt()) {
			if (prompt.getData() == null || prompt.getData().isEmpty()) {
				throw new InsightAppException(InsightAppError.EMPTY_DATA_OBJECT);
			}
		}
	}
}