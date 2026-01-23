package com.aiinsightagent.app.util;


import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.exception.InsightAppError;
import com.aiinsightagent.app.exception.InsightAppException;
import com.aiinsightagent.core.model.Context;
import com.aiinsightagent.core.model.InsightRequest;
import com.aiinsightagent.core.model.prompt.UserPrompt;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.CollectionUtils;

import java.util.Map;

public class InsightRequestValidator {
	public static void validate(InsightRequest request) {
		if (!hasUserPrompt(request)) {
			throw new InsightAppException(InsightAppError.MISSING_USER_PROMPT_REQUEST);
		}
		validateUserId(request.getUserId());
		validatePurpose(request.getPurpose());
		validateDataKey(request);
		validateDataFields(request);
	}

	public static void validate(Actor actor, Context context) {
		validateUserId(actor.getActorKey());
		validateContext(context);
	}

	public static void validateUserId(String userId) {
		if(userId == null || userId.isEmpty()) {
			throw new InsightAppException(InsightAppError.EMPTY_USER_ID);
		}
	}

	public static void validatePurpose(String purpose) {
		if(purpose == null || purpose.isEmpty()) {
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

	public static void validateContext(Context context) {
		if (context != null) {
			String category = context.getCategory();
			if (category == null || category.isEmpty()) {
				throw new InsightAppException(InsightAppError.EMPTY_CONTEXT_CATEGORY);
			}

			Map<String, String> contextData = context.getData();
			if (contextData == null || contextData.isEmpty()) {
				throw new InsightAppException(InsightAppError.EMPTY_CONTEXT_DATA);
			}
		} else {
			throw new InsightAppException(InsightAppError.EMPTY_CONTEXT);
		}
	}
}