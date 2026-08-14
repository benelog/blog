package net.benelog.jackson;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

class ZeroStartingNumberTest {
	@Test
	void parse() throws JsonProcessingException {
		var objectMapper = new ObjectMapper()
			.enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature());
		// JSON 스펙에 어긋나서 권장하지 않는 옵션

		var json = """
			{"code" : 011}
			""";

		Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
		assertThat(parsed.get("code")).isEqualTo(11);
	}
}
