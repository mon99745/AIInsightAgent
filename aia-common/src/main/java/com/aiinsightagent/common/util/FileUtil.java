package com.aiinsightagent.common.util;


import com.aiinsightagent.common.exception.CommonException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.aiinsightagent.common.exception.CommonError.FAILED_READ_FILE;

/**
 * 파일 관련 공통 유틸리티 클래스
 * - 파일 유효성 검증
 * - 파일 내용 읽기 (전체 문자열 또는 라인 단위)
 */
public class FileUtil {
	private FileUtil() {}

	/**
	 * 파일 전체 내용을 문자열로 읽어오기
	 *
	 * @param file 읽을 파일
	 * @return 파일 전체 내용
	 * @throws CommonException 파일 읽기 실패 시 발생
	 */
	public static String readFileContent(File file) {
		StringBuilder content = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append(System.lineSeparator());
			}
		} catch (IOException e) {
			throw new CommonException(FAILED_READ_FILE, file.getAbsolutePath());
		}

		return content.toString();
	}

	/**
	 * 파일 내용을 라인 단위로 읽어오기
	 *
	 * @param file 읽을 파일
	 * @return 파일 내용 라인 리스트
	 * @throws CommonException 파일 읽기 실패 시 발생
	 */
	public static List<String> readFileLines(File file) {
		List<String> lines = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			throw new CommonException(FAILED_READ_FILE, file.getAbsolutePath());
		}

		return lines;
	}
}
