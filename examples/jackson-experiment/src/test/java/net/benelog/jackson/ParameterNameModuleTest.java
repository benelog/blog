package net.benelog.jackson;

import static org.assertj.core.api.BDDAssertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

class ParameterNameModuleTest {
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
			.registerModule(new JavaTimeModule())
			.registerModule(new ParameterNamesModule());

		AccessLog accessLog = objectMapper.readValue(json, AccessLog.class);

		then(accessLog.getAccessDateTime()).isEqualTo("2019-10-10T11:14:16Z");
		then(accessLog.getIp()).isEqualTo("175.242.91.54");
		then(accessLog.getUsername()).isEqualTo("benelog");
	}

	public static class AccessLog {
		private final Instant accessDateTime;
		private final String ip;
		private final String username;

		public AccessLog(Instant accessDateTime, String ip, String username) {
			this.accessDateTime = accessDateTime;
			this.ip = ip;
			this.username = username;
		}

		public Instant getAccessDateTime() {
			return accessDateTime;
		}

		public String getIp() {
			return ip;
		}

		public String getUsername() {
			return username;
		}

		@Override
		public String toString() {
			return "AccessLog{" +
				"accessDateTime=" + accessDateTime +
				", ip='" + ip + '\'' +
				", username='" + username + '\'' +
				'}';
		}
	}
}
