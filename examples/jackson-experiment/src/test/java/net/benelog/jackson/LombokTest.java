package net.benelog.jackson;

import static org.assertj.core.api.BDDAssertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

class LombokTest {
	@Test
	void parse() throws JsonProcessingException {
		var json = """
			{
			"accessDateTime": "2019-10-10T11:14:16Z",
			"ip": "175.242.91.54",
			"username": "benelog"
			}
			""";

		var objectMapper = new ObjectMapper()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.registerModule(new JavaTimeModule());

		AccessLog accessLog = objectMapper.readValue(json, AccessLog.class);

		then(accessLog.getAccessDateTime()).isEqualTo("2019-10-10T11:14:16Z");
		then(accessLog.getIp()).isEqualTo("175.242.91.54");
		then(accessLog.getUsername()).isEqualTo("benelog");
	}

	// lombok.config에  lombok.anyConstructor.addConstructorProperties=true 설정이 있어서
	// 생성자에 @ConstructorProperties 가 자동으로 추가 된다.
	@Builder
	@Getter
	@ToString
	public static class AccessLog {
		private final Instant accessDateTime;
		private final String ip;
		private final String username;
	}
}
