package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.example.demo.constants.StringFormats.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Person extends AuditablePojo{

    @JsonProperty("date_of_birth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FMT_TEST_DATE_TIME, timezone = LOCAL_TIME_ZONE)
    @Schema(description = "Date of birth in a UTC time format", format = FMT_TEST_DATE_TIME, example = FMT_TEST_DATE_TIME_EG)
    private Instant dateOfBirth;
}
