package com.aiinsightagent.app.controller;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.service.ActorService;
import com.aiinsightagent.app.service.PreparedContextService;
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
@Tag(name = ContextController.TAG, description = "데이터 분석 사전 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping(ContextController.PATH)
public class ContextController {
	public static final String TAG = "Prepared Context Manager API";
	public static final String PATH = "/api/v1/context";

	public final ActorService actorService;
	public final PreparedContextService contextService;

	@Operation(summary = "전처리 데이터 저장" )
	@PostMapping("save")
	public PreparedContext saveContext(@RequestBody Context context) {
		Actor actor = actorService.getOrCreate(context.getUserId());

		return contextService.save(actor, context);
	}

	@Operation(summary = "전처리 데이터 추출" )
	@PostMapping("get")
	public PreparedContext getContext(@RequestParam String userId) {
		Actor actor = actorService.get(userId);

		return contextService.get(actor);
	}

	@Operation(summary = "전처리 데이터 수정" )
	@PostMapping("update")
	public PreparedContext updateContext(@RequestBody Context context) {
		Actor actor = actorService.get(context.getUserId());

		return contextService.update(actor, context);
	}

	@Operation(summary = "전처리 데이터 삭제" )
	@PostMapping("delete")
	public void deleteContext(@RequestParam String userId) {
		Actor actor = actorService.get(userId);
		contextService.delete(actor);
	}
}