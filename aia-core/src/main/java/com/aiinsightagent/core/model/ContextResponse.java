package com.aiinsightagent.core.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContextResponse {
    private int resultCode;
    private String resultMsg;
    private Context context;
}