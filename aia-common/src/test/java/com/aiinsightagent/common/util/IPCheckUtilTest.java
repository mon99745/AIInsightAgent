package com.aiinsightagent.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class IPCheckUtilTest {

	@Nested
	@DisplayName("isIPv4 테스트")
	class IsIPv4Test {

		@Test
		@DisplayName("유효한 IPv4 주소를 검증한다")
		void isIPv4_validIPv4_returnsTrue() {
			assertThat(IPCheckUtil.isIPv4("192.168.1.1")).isTrue();
		}

		@Test
		@DisplayName("로컬호스트 주소를 검증한다")
		void isIPv4_localhost_returnsTrue() {
			assertThat(IPCheckUtil.isIPv4("127.0.0.1")).isTrue();
		}

		@Test
		@DisplayName("최소값 IPv4 주소를 검증한다")
		void isIPv4_minAddress_returnsTrue() {
			assertThat(IPCheckUtil.isIPv4("0.0.0.0")).isTrue();
		}

		@Test
		@DisplayName("최대값 IPv4 주소를 검증한다")
		void isIPv4_maxAddress_returnsTrue() {
			assertThat(IPCheckUtil.isIPv4("255.255.255.255")).isTrue();
		}

		@ParameterizedTest
		@ValueSource(strings = {"10.0.0.1", "172.16.0.1", "192.168.0.1", "8.8.8.8", "1.1.1.1"})
		@DisplayName("다양한 유효 IPv4 주소를 검증한다")
		void isIPv4_variousValidAddresses_returnsTrue(String ip) {
			assertThat(IPCheckUtil.isIPv4(ip)).isTrue();
		}

		@Test
		@DisplayName("범위를 초과한 IPv4 주소는 무효하다")
		void isIPv4_outOfRange_returnsFalse() {
			assertThat(IPCheckUtil.isIPv4("256.1.1.1")).isFalse();
		}

		@Test
		@DisplayName("옥텟이 부족한 주소는 무효하다")
		void isIPv4_missingOctet_returnsFalse() {
			assertThat(IPCheckUtil.isIPv4("192.168.1")).isFalse();
		}

		@Test
		@DisplayName("옥텟이 너무 많은 주소는 무효하다")
		void isIPv4_tooManyOctets_returnsFalse() {
			assertThat(IPCheckUtil.isIPv4("192.168.1.1.1")).isFalse();
		}

		@Test
		@DisplayName("문자가 포함된 주소는 무효하다")
		void isIPv4_withLetters_returnsFalse() {
			assertThat(IPCheckUtil.isIPv4("192.168.a.1")).isFalse();
		}

		@Test
		@DisplayName("null 입력 시 false를 반환한다")
		void isIPv4_nullInput_returnsFalse() {
			assertThat(IPCheckUtil.isIPv4(null)).isFalse();
		}

		@Test
		@DisplayName("빈 문자열 입력 시 false를 반환한다")
		void isIPv4_emptyString_returnsFalse() {
			assertThat(IPCheckUtil.isIPv4("")).isFalse();
		}

		@Test
		@DisplayName("IPv6 주소는 무효하다")
		void isIPv4_ipv6Address_returnsFalse() {
			assertThat(IPCheckUtil.isIPv4("::1")).isFalse();
		}
	}

	@Nested
	@DisplayName("isIPv6 테스트")
	class IsIPv6Test {

		@Test
		@DisplayName("전체 형식 IPv6 주소를 검증한다")
		void isIPv6_fullForm_returnsTrue() {
			assertThat(IPCheckUtil.isIPv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334")).isTrue();
		}

		@Test
		@DisplayName("축약된 IPv6 주소를 검증한다")
		void isIPv6_compressedForm_returnsTrue() {
			assertThat(IPCheckUtil.isIPv6("2001:db8:85a3::8a2e:370:7334")).isTrue();
		}

		@Test
		@DisplayName("로컬호스트 IPv6 주소를 검증한다")
		void isIPv6_localhost_returnsTrue() {
			assertThat(IPCheckUtil.isIPv6("::1")).isTrue();
		}

		@Test
		@DisplayName("모든 0 IPv6 주소를 검증한다")
		void isIPv6_allZeros_returnsTrue() {
			assertThat(IPCheckUtil.isIPv6("::")).isTrue();
		}

		@ParameterizedTest
		@ValueSource(strings = {
				"fe80::1",
				"fe80::",
				"2001:db8::",
				"2001:db8:85a3:8d3:1319:8a2e:370:7348"
		})
		@DisplayName("다양한 유효 IPv6 주소를 검증한다")
		void isIPv6_variousValidAddresses_returnsTrue(String ip) {
			assertThat(IPCheckUtil.isIPv6(ip)).isTrue();
		}

		@Test
		@DisplayName("잘못된 IPv6 형식은 무효하다")
		void isIPv6_invalidFormat_returnsFalse() {
			assertThat(IPCheckUtil.isIPv6("2001:db8:85a3:0000:0000:8a2e:0370:7334:extra")).isFalse();
		}

		@Test
		@DisplayName("null 입력 시 false를 반환한다")
		void isIPv6_nullInput_returnsFalse() {
			assertThat(IPCheckUtil.isIPv6(null)).isFalse();
		}

		@Test
		@DisplayName("빈 문자열 입력 시 false를 반환한다")
		void isIPv6_emptyString_returnsFalse() {
			assertThat(IPCheckUtil.isIPv6("")).isFalse();
		}

		@Test
		@DisplayName("IPv4 주소는 무효하다")
		void isIPv6_ipv4Address_returnsFalse() {
			assertThat(IPCheckUtil.isIPv6("192.168.1.1")).isFalse();
		}
	}

	@Nested
	@DisplayName("isIP 테스트")
	class IsIPTest {

		@Test
		@DisplayName("IPv4 주소를 유효한 IP로 검증한다")
		void isIP_validIPv4_returnsTrue() {
			assertThat(IPCheckUtil.isIP("192.168.1.1")).isTrue();
		}

		@Test
		@DisplayName("IPv6 주소를 유효한 IP로 검증한다")
		void isIP_validIPv6_returnsTrue() {
			assertThat(IPCheckUtil.isIP("::1")).isTrue();
		}

		@Test
		@DisplayName("전체 형식 IPv6 주소를 유효한 IP로 검증한다")
		void isIP_fullFormIPv6_returnsTrue() {
			assertThat(IPCheckUtil.isIP("2001:0db8:85a3:0000:0000:8a2e:0370:7334")).isTrue();
		}

		@Test
		@DisplayName("잘못된 주소는 무효하다")
		void isIP_invalidAddress_returnsFalse() {
			assertThat(IPCheckUtil.isIP("invalid-ip")).isFalse();
		}

		@Test
		@DisplayName("null 입력 시 false를 반환한다")
		void isIP_nullInput_returnsFalse() {
			assertThat(IPCheckUtil.isIP(null)).isFalse();
		}

		@Test
		@DisplayName("빈 문자열 입력 시 false를 반환한다")
		void isIP_emptyString_returnsFalse() {
			assertThat(IPCheckUtil.isIP("")).isFalse();
		}

		@Test
		@DisplayName("호스트명은 무효하다")
		void isIP_hostname_returnsFalse() {
			assertThat(IPCheckUtil.isIP("localhost")).isFalse();
		}

		@Test
		@DisplayName("도메인명은 무효하다")
		void isIP_domainName_returnsFalse() {
			assertThat(IPCheckUtil.isIP("example.com")).isFalse();
		}
	}
}
