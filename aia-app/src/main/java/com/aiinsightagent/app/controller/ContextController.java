package com.aiinsightagent.app.controller;

import com.aiinsightagent.core.model.Context;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = ContextController.TAG, description = "분석 전처리 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping(ContextController.PATH)
public class ContextController {
	public static final String TAG = "Prepared Context Manager";
	public static final String PATH = "/api/v1/context";

	@Operation(summary = "분석 전처리 데이터 저장" )
	@PostMapping("save")
	public void saveBaseinfo(@RequestBody Context context) {
	}

	@Operation(summary = "분석 전처리 데이터 추출" )
	@PostMapping("get")
	public void getBaseinfo(@RequestParam String userId) {
	}

	@Operation(summary = "분석 전처리 데이터 수정" )
	@PostMapping("update")
	public void updateBaseinfo(@RequestBody Context context) {
	}

	@Operation(summary = "분석 전처리 데이터 삭제" )
	@PostMapping("delete")
	public void deleteBaseinfo(@RequestParam String userId) {
	}
}