package com.aiinsightagent.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Context {
	private String userId;
	private List<String> info;
}