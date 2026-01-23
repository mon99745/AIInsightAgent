package com.aiinsightagent.app.service;

import com.aiinsightagent.app.entity.Actor;
import com.aiinsightagent.app.entity.PreparedContext;
import com.aiinsightagent.app.repository.PreparedContextRepository;
import com.aiinsightagent.app.util.InsightRequestValidator;
import com.aiinsightagent.app.util.ParserUtils;
import com.aiinsightagent.core.exception.InsightError;
import com.aiinsightagent.core.exception.InsightException;
import com.aiinsightagent.core.model.Context;
import com.aiinsightagent.core.model.ContextResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreparedContextService {
    private final PreparedContextRepository contextRepository;
	private final ParserUtils parserUtils;

	private PreparedContext getEntity(Actor actor) {
		return contextRepository.findByActor(actor)
				.orElseThrow(() -> new InsightException(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT, ":" + actor.getActorKey()));
	}

    public Optional<PreparedContext> findByActorKey(Actor actor) {
        return contextRepository.findByActor(actor);
    }

    public ContextResponse create(Actor actor, Context context) {
        // 1. 요청 데이터 검증
        InsightRequestValidator.validate(actor, context);

        // 2. context 조회 및 중복 검증
        contextRepository.findByActor(actor)
                .ifPresent(existingContext -> {
                    throw new InsightException(InsightError.EXIST_ACTOR_PREPARED_CONTEXT, ":" + actor.getActorKey());
                });

        // 3. PreparedContext 생성 및 저장
		PreparedContext preparedContext =
				new PreparedContext(actor, context.getCategory(), parserUtils.toJson(context.getData()));

		contextRepository.save(preparedContext);

        return ContextResponse.builder()
                .resultCode(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .context(context)
				.build();
    }

    public ContextResponse get(Actor actor) {
        // 1. 요청 데이터 점증
        InsightRequestValidator.validateUserId(actor.getActorKey());

        // 2. PreparedContext 조회 및 존재 여부 확인
        PreparedContext preparedContext = contextRepository.findByActor(actor)
                .orElseThrow(() -> new InsightException(InsightError.EMPTY_ACTOR_PREPARED_CONTEXT, ":" + actor.getActorKey()));

        // 3. ContextResponse 생성
        Context context = Context.builder()
                .userId(actor.getActorKey())
                .category(preparedContext.getContextType())
                .data(parserUtils.parsePayload(preparedContext.getContextPayload()))
                .build();

        return ContextResponse.builder()
                .resultCode(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .context(context)
                .build();
    }

    public ContextResponse update(Actor actor, Context context) {
        // 1. 요청 데이터 검증
        InsightRequestValidator.validate(actor, context);

        // 2. PreparedContext 조회 및 존재 여부 확인
        PreparedContext preparedContext = getEntity(actor);

        // 3. PreparedContext 업데이트
        preparedContext.update(context.getCategory(), parserUtils.toJson(context.getData()));

        contextRepository.save(preparedContext);

        return ContextResponse.builder()
                .resultCode(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .context(context)
                .build();
    }

    public ContextResponse delete(Actor actor) {
        // 1. 요청 데이터 검증
        InsightRequestValidator.validateUserId(actor.getActorKey());

        // 2. PreparedContext 조회 및 존재 여부 확인
        PreparedContext preparedContext = getEntity(actor);

        // 3. PreparedContext 삭제
        contextRepository.deleteById(preparedContext.getContextId());

        return ContextResponse.builder()
                .resultCode(HttpStatus.OK.value())
                .resultMsg(HttpStatus.OK.getReasonPhrase())
                .build();
    }
}