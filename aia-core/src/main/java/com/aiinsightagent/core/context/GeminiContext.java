package com.aiinsightagent.core.context;

/**
 * Gemini API 호출 컨텍스트 (ThreadLocal)
 */
public class GeminiContext {
    private static final ThreadLocal<String> MODEL_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> MODEL_NAME = new ThreadLocal<>();

    private GeminiContext() {
    }

    public static void setModelInfo(String modelId, String modelName) {
        MODEL_ID.set(modelId);
        MODEL_NAME.set(modelName);
    }

    public static String getModelId() {
        return MODEL_ID.get();
    }

    public static String getModelName() {
        return MODEL_NAME.get();
    }

    public static void clear() {
        MODEL_ID.remove();
        MODEL_NAME.remove();
    }

    /**
     * 분석 버전 문자열 생성 (예: "gemini-2.5-flash[m08]")
     */
    public static String getAnalysisVersion() {
        String modelId = MODEL_ID.get();
        String modelName = MODEL_NAME.get();

        if (modelName == null) {
            return "unknown";
        }

        return String.format("%s[%s]", modelName, modelId);
    }
}