package com.gustavo.marketflow.fix.application;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.gustavo.marketflow.shared.exception.InvalidFixMessageException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixMessageParserTest {

    private final FixMessageParser parser = new FixMessageParser();

    @Test
    void parse_validMessage_returnsOrderedTagMap() {
        String rawMessage = "8=FIX.4.4|35=D|55=AAPL|54=1";

        Map<String, String> result = parser.parse(rawMessage);

        assertThat(result).containsExactly(
                Map.entry("8", "FIX.4.4"),
                Map.entry("35", "D"),
                Map.entry("55", "AAPL"),
                Map.entry("54", "1")
        );
    }

    @Test
    void parse_duplicateTag_throwsInvalidFixMessage() {
        assertThatThrownBy(() -> parser.parse("8=FIX.4.4|35=D|55=AAPL|55=MSFT"))
                .isInstanceOf(InvalidFixMessageException.class)
                .hasMessageContaining("Duplicate FIX tag: 55");
    }

    @Test
    void parse_malformedField_throwsInvalidFixMessage() {
        assertThatThrownBy(() -> parser.parse("8=FIX.4.4|35=D|55"))
                .isInstanceOf(InvalidFixMessageException.class)
                .hasMessageContaining("numericTag=value");
    }

    @Test
    void parse_missingMessageType_throwsInvalidFixMessage() {
        assertThatThrownBy(() -> parser.parse("8=FIX.4.4|55=AAPL"))
                .isInstanceOf(InvalidFixMessageException.class)
                .hasMessageContaining("35");
    }

    @Test
    void parse_wrongBeginString_throwsInvalidFixMessage() {
        assertThatThrownBy(() -> parser.parse("8=FIX.4.2|35=D"))
                .isInstanceOf(InvalidFixMessageException.class)
                .hasMessageContaining("FIX.4.4");
    }
}
