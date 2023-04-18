package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HabrCareerDateTimeParserTest {

    @Test
    void whenDateIsCorrectThenNotException() {
        HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
        assertThat(habrCareerDateTimeParser.parse("2023-04-18T19:01:32"))
                .isEqualTo(LocalDateTime.of(2023, 04, 18, 19, 01, 32));
    }

    @Test
    void whenDateIsNotCorrectThenException() {
        HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
        DateTimeParseException exception = assertThrows(
                DateTimeParseException.class,
                () -> habrCareerDateTimeParser.parse("2023-04-18T19:01:32+05:00")
        );

        assertThat(exception.getMessage()).contains("could not be parsed, unparsed text found at index 19");
    }
}