# Documentation Validation Tests

This package contains comprehensive tests for validating the `README.md` documentation file.

## 📋 Overview

While README.md is a documentation file (not executable code), these tests ensure that:
- ✅ Documentation is accurate and matches the actual implementation
- ✅ API examples in README are valid and work correctly
- ✅ Configuration examples are correct and follow best practices
- ✅ Code blocks are properly formatted
- ✅ Links and references are valid
- ✅ JSON examples are well-formed and match API contracts

## 📁 Test Files

### 1. `ReadmeDocumentationValidationTest.java` (757 lines, 46 tests)
**Purpose:** Validates the overall structure and content of README.md

**Test Categories:**
- README Structure (10 tests)
- API Endpoint Documentation (3 tests)
- JSON Example Validation (6 tests)
- Configuration Validation (4 tests)
- Architecture Documentation (4 tests)
- Testing Section (4 tests)
- HTTP Status Codes (2 tests)
- Project Metadata (3 tests)
- Code Block Formatting (3 tests)
- Field Naming Conventions (2 tests)
- Example Data Consistency (3 tests)
- Link Validation (2 tests)

### 2. `ApiContractValidationTest.java` (678 lines, 23 tests)
**Purpose:** Ensures API implementations match README documentation

**Test Categories:**
- POST /api/v1/analysis Contract (5 tests)
- GET /api/v1/analysis/history Contract (4 tests)
- Context API Contract (6 tests)
- API Versioning (2 tests)
- Response Status Codes (3 tests)
- Field Structure Validation (3 tests)

### 3. `ConfigurationExampleValidationTest.java` (586 lines, 30 tests)
**Purpose:** Validates YAML configuration examples

**Test Categories:**
- Gemini API Configuration (7 tests)
- Queue/Worker Configuration (4 tests)
- Database Configuration (5 tests)
- YAML Formatting (4 tests)
- Configuration Value Ranges (4 tests)
- Configuration Consistency (3 tests)
- Environment Variables (3 tests)

### 4. `ReadmeEdgeCasesTest.java` (562 lines, 28 tests)
**Purpose:** Edge cases, regression, boundary, and negative scenarios

**Test Categories:**
- Regression Tests (5 tests)
- API Documentation Edge Cases (5 tests)
- Configuration Edge Cases (4 tests)
- Text Content Edge Cases (4 tests)
- Numeric Boundary Cases (4 tests)
- Negative Cases (4 tests)
- Bilingual Consistency (2 tests)

## 📊 Coverage Summary

| Metric | Count |
|--------|-------|
| **Test Files** | 4 |
| **Nested Test Classes** | 31 |
| **Test Methods** | 127 |
| **Lines of Code** | 2,583 |

## 🚀 Running Tests

### Run all documentation tests
```bash
./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.*"
```

### Run specific test class
```bash
# Structure and content validation
./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.ReadmeDocumentationValidationTest"

# API contract validation
./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.ApiContractValidationTest"

# Configuration validation
./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.ConfigurationExampleValidationTest"

# Edge cases and regression
./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.ReadmeEdgeCasesTest"
```

### Run specific nested test class
```bash
# Example: Run only JSON validation tests
./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.ReadmeDocumentationValidationTest\$JsonExampleValidationTest"
```

## 📈 View Test Reports

After running tests, view detailed reports at:
```
aia-app/build/reports/tests/test/index.html
```

## 🔧 Technologies Used

- **JUnit 5**: Test framework with `@Nested` test organization
- **AssertJ**: Fluent assertions for better readability
- **Spring Boot Test**: Integration testing support
- **MockMvc**: For API endpoint testing
- **Mockito**: For mocking external dependencies
- **Jackson ObjectMapper**: JSON parsing and validation

## 📝 Test Structure Example

```java
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("README Documentation Validation")
class ReadmeDocumentationValidationTest {

    @Nested
    @DisplayName("JSON Example Validation")
    class JsonExampleValidationTest {

        @Test
        @DisplayName("README의 InsightRequest JSON 예제가 유효한지 확인")
        void readme_InsightRequestJsonIsValid() {
            // Test implementation
        }
    }
}
```

## ✨ Key Benefits

1. **Documentation Accuracy**: Ensures README stays in sync with implementation
2. **Regression Prevention**: Catches documentation errors early in CI/CD
3. **Quality Assurance**: Validates all examples are correct and working
4. **Maintainability**: Easy to verify changes don't break documentation
5. **Developer Confidence**: New developers can trust the documentation
6. **Automated Validation**: No manual documentation review needed

## 🎯 What These Tests Validate

### Documentation Structure
- All required sections exist
- Table of contents is complete
- Headings are properly formatted
- Code blocks are closed properly

### API Examples
- All API endpoints are documented
- Request/response examples are valid JSON
- HTTP methods and paths are correct
- Query parameters follow proper format

### Configuration Examples
- YAML syntax is correct
- Indentation is consistent
- Configuration values are in valid ranges
- Environment variables follow naming conventions

### Content Quality
- No broken links
- No TODO/FIXME comments left
- No placeholder text
- Consistent terminology usage
- Bilingual content (Korean/English) present

### Edge Cases
- No trailing whitespace
- No excessive blank lines
- Proper JSON null handling
- Valid HTTP status codes
- Correct character encoding

## 🔄 Continuous Integration

These tests run automatically on:
- ✅ Pull requests to `main` or `dev` branches
- ✅ Pushes to `main` or `dev` branches
- ✅ Manual workflow triggers

See `.github/workflows/ci.yml` for CI configuration.

## 📚 Related Documentation

- Main project README: `../../../../../../README.md`
- Test summary: `../../../../../../TEST_SUMMARY.md`
- Project structure: See README "Project Structure" section

## 🤝 Contributing

When updating README.md:
1. Run these tests to verify your changes
2. Fix any failing tests
3. Add new tests if you add new sections or examples
4. Ensure tests pass before submitting PR

## 📞 Support

If tests fail:
1. Check the test output for specific error messages
2. Review the failing test method's purpose
3. Update either the README.md or the implementation to match
4. Re-run tests to verify fix

---

**Last Updated:** 2026-02-06
**Test Coverage:** 127 tests across 4 test classes
**Status:** ✅ All tests designed and ready to run