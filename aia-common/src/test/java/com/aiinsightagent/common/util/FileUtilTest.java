package com.aiinsightagent.common.util;

import com.aiinsightagent.common.exception.CommonException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUtilTest {

	@TempDir
	Path tempDir;

	@Nested
	@DisplayName("readFileContent 테스트")
	class ReadFileContentTest {

		@Test
		@DisplayName("파일 전체 내용을 문자열로 읽는다")
		void readFileContent_validFile_returnsContent() throws IOException {
			// given
			File testFile = tempDir.resolve("test.txt").toFile();
			try (FileWriter writer = new FileWriter(testFile)) {
				writer.write("Hello World\nThis is a test file.");
			}

			// when
			String result = FileUtil.readFileContent(testFile);

			// then
			assertThat(result).contains("Hello World");
			assertThat(result).contains("This is a test file.");
		}

		@Test
		@DisplayName("여러 줄의 파일을 읽는다")
		void readFileContent_multiLineFile_returnsAllLines() throws IOException {
			// given
			File testFile = tempDir.resolve("multiline.txt").toFile();
			try (FileWriter writer = new FileWriter(testFile)) {
				writer.write("Line 1\n");
				writer.write("Line 2\n");
				writer.write("Line 3");
			}

			// when
			String result = FileUtil.readFileContent(testFile);

			// then
			assertThat(result).contains("Line 1");
			assertThat(result).contains("Line 2");
			assertThat(result).contains("Line 3");
		}

		@Test
		@DisplayName("빈 파일을 읽으면 빈 문자열을 반환한다")
		void readFileContent_emptyFile_returnsEmptyString() throws IOException {
			// given
			File testFile = tempDir.resolve("empty.txt").toFile();
			testFile.createNewFile();

			// when
			String result = FileUtil.readFileContent(testFile);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("존재하지 않는 파일을 읽으면 예외가 발생한다")
		void readFileContent_nonExistentFile_throwsException() {
			// given
			File nonExistentFile = tempDir.resolve("non-existent.txt").toFile();

			// when & then
			assertThatThrownBy(() -> FileUtil.readFileContent(nonExistentFile))
					.isInstanceOf(CommonException.class);
		}

		@Test
		@DisplayName("UTF-8 인코딩 파일을 올바르게 읽는다")
		void readFileContent_utf8File_returnsCorrectContent() throws IOException {
			// given
			File testFile = tempDir.resolve("utf8.txt").toFile();
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(testFile), StandardCharsets.UTF_8)) {
				writer.write("Korean Test\nEnglish Test");
			}

			// when
			String result = FileUtil.readFileContent(testFile);

			// then
			assertThat(result).contains("Korean Test");
			assertThat(result).contains("English Test");
		}
	}

	@Nested
	@DisplayName("readFileLines 테스트")
	class ReadFileLinesTest {

		@Test
		@DisplayName("파일을 라인 단위로 읽는다")
		void readFileLines_validFile_returnsLines() throws IOException {
			// given
			File testFile = tempDir.resolve("lines.txt").toFile();
			try (FileWriter writer = new FileWriter(testFile)) {
				writer.write("Line 1\n");
				writer.write("Line 2\n");
				writer.write("Line 3");
			}

			// when
			List<String> result = FileUtil.readFileLines(testFile);

			// then
			assertThat(result).hasSize(3);
			assertThat(result.get(0)).isEqualTo("Line 1");
			assertThat(result.get(1)).isEqualTo("Line 2");
			assertThat(result.get(2)).isEqualTo("Line 3");
		}

		@Test
		@DisplayName("한 줄짜리 파일을 읽는다")
		void readFileLines_singleLine_returnsSingleElementList() throws IOException {
			// given
			File testFile = tempDir.resolve("single.txt").toFile();
			try (FileWriter writer = new FileWriter(testFile)) {
				writer.write("Single line content");
			}

			// when
			List<String> result = FileUtil.readFileLines(testFile);

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0)).isEqualTo("Single line content");
		}

		@Test
		@DisplayName("빈 파일을 읽으면 빈 리스트를 반환한다")
		void readFileLines_emptyFile_returnsEmptyList() throws IOException {
			// given
			File testFile = tempDir.resolve("empty.txt").toFile();
			testFile.createNewFile();

			// when
			List<String> result = FileUtil.readFileLines(testFile);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("존재하지 않는 파일을 읽으면 예외가 발생한다")
		void readFileLines_nonExistentFile_throwsException() {
			// given
			File nonExistentFile = tempDir.resolve("non-existent.txt").toFile();

			// when & then
			assertThatThrownBy(() -> FileUtil.readFileLines(nonExistentFile))
					.isInstanceOf(CommonException.class);
		}

		@Test
		@DisplayName("UTF-8 인코딩 파일을 라인 단위로 올바르게 읽는다")
		void readFileLines_utf8File_returnsCorrectLines() throws IOException {
			// given
			File testFile = tempDir.resolve("utf8-lines.txt").toFile();
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(testFile), StandardCharsets.UTF_8)) {
				writer.write("First line\n");
				writer.write("Second line\n");
				writer.write("Third line");
			}

			// when
			List<String> result = FileUtil.readFileLines(testFile);

			// then
			assertThat(result).hasSize(3);
			assertThat(result.get(0)).isEqualTo("First line");
			assertThat(result.get(1)).isEqualTo("Second line");
			assertThat(result.get(2)).isEqualTo("Third line");
		}

		@Test
		@DisplayName("빈 줄이 포함된 파일을 읽는다")
		void readFileLines_fileWithEmptyLines_returnsAllLines() throws IOException {
			// given
			File testFile = tempDir.resolve("with-empty-lines.txt").toFile();
			try (FileWriter writer = new FileWriter(testFile)) {
				writer.write("Line 1\n");
				writer.write("\n");
				writer.write("Line 3");
			}

			// when
			List<String> result = FileUtil.readFileLines(testFile);

			// then
			assertThat(result).hasSize(3);
			assertThat(result.get(0)).isEqualTo("Line 1");
			assertThat(result.get(1)).isEmpty();
			assertThat(result.get(2)).isEqualTo("Line 3");
		}
	}
}
