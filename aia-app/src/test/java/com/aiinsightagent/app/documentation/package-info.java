/**
 * Documentation Validation Test Package
 *
 * <p>This package contains comprehensive tests for validating the README.md documentation file.
 * While README.md is a documentation file (not executable code), these tests ensure that:
 * <ul>
 *   <li>Documentation is accurate and matches implementation</li>
 *   <li>API examples are valid and working</li>
 *   <li>Configuration examples are correct</li>
 *   <li>Code blocks are properly formatted</li>
 *   <li>Links and references are valid</li>
 * </ul>
 *
 * <h2>Test Classes:</h2>
 * <ul>
 *   <li><b>ReadmeDocumentationValidationTest</b> - Validates README structure, content, and examples (46 tests)</li>
 *   <li><b>ApiContractValidationTest</b> - Ensures API implementations match README docs (23 tests)</li>
 *   <li><b>ConfigurationExampleValidationTest</b> - Validates YAML configuration examples (30 tests)</li>
 *   <li><b>ReadmeEdgeCasesTest</b> - Edge cases, regression, and boundary tests (28 tests)</li>
 * </ul>
 *
 * <h2>Running Tests:</h2>
 * <pre>
 * # All documentation tests
 * ./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.*"
 *
 * # Specific test class
 * ./gradlew :aia-app:test --tests "com.aiinsightagent.app.documentation.ReadmeDocumentationValidationTest"
 * </pre>
 *
 * <h2>Total Coverage:</h2>
 * <ul>
 *   <li>127 test methods across 31 nested test classes</li>
 *   <li>Validates documentation accuracy, API contracts, configuration, and edge cases</li>
 * </ul>
 *
 * @see com.aiinsightagent.app.documentation.ReadmeDocumentationValidationTest
 * @see com.aiinsightagent.app.documentation.ApiContractValidationTest
 * @see com.aiinsightagent.app.documentation.ConfigurationExampleValidationTest
 * @see com.aiinsightagent.app.documentation.ReadmeEdgeCasesTest
 */
package com.aiinsightagent.app.documentation;