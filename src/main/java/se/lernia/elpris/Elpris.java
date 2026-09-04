package se.lernia.elpris;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Elpris(
        @JsonProperty("SEK_per_kWh") double sekPerKwh,
        @JsonProperty("time_start") String timeStart,
        @JsonProperty("time_end") String timeEnd
) {
}