package com.aiinsightagent.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static java.lang.Math.round;

@Slf4j
@Tag(name = SimulationController.TAG, description = "외부 유입 데이터 시뮬레이션 API")
@RequiredArgsConstructor
@RestController
@RequestMapping(SimulationController.PATH)
public class SimulationController {
    public static final String TAG = "Simulation API";
    public static final String PATH = "/api/v1/simulation";

    @Operation(summary = "[테스트용] UUID(userId) 생성")
    @GetMapping("createUuid")
    public String createUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    @Operation(summary = "[테스트용] 분석 요청문 생성")
    @GetMapping("createInsightReqMsg")
    public String createInsightRequestMsg(@RequestParam String uuid) throws JsonProcessingException {
        Random random = new Random();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("userId", uuid);
        root.put("purpose", "running_style_analysis");

        List<Map<String, Object>> userPromptList = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Map<String, Object> prompt = new LinkedHashMap<>();
            prompt.put("dataKey", "running_session_" + i);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("duration", 900 + random.nextInt(2701));
            data.put("heartRate", round(160 + random.nextDouble() * 35));
            data.put("distance", round(3 + random.nextDouble() * 7.5));
            data.put("stepCount", 3000 + random.nextInt(8001));

            prompt.put("data", data);
            userPromptList.add(prompt);
        }

        root.put("userPrompt", userPromptList);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper.writeValueAsString(root);
    }

    @Operation(summary = "[테스트용] 전처리 데이터 저장 요청문 생성")
    @GetMapping("createContextReqMsg")
    public String createContext(@RequestParam String uuid) throws JsonProcessingException {
        Random random = new Random();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("userId", uuid);
        root.put("category", "user_profile");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("age", 20 + random.nextInt(31));        // 20 ~ 50
        data.put("gender", random.nextBoolean() ? "MALE" : "FEMALE");
        data.put("height", 160 + random.nextInt(21));   // 160 ~ 180
        data.put("weight", 55 + random.nextInt(26));    // 55 ~ 80

        root.put("data", data);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper.writeValueAsString(root);
    }
}
