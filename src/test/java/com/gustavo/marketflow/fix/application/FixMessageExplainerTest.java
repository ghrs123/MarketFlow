package com.gustavo.marketflow.fix.application;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.gustavo.marketflow.fix.domain.FixTagExplanation;
import com.gustavo.marketflow.shared.exception.InvalidFixMessageException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixMessageExplainerTest {

    private final FixMessageExplainer explainer = new FixMessageExplainer(new FixMessageParser());

    @Test
    void explain_knownAndUnknownTags_returnsDescriptionsWithoutDroppingFields() {
        List<FixTagExplanation> result = explainer.explain("8=FIX.4.4|35=D|999=custom");

        assertThat(result).hasSize(3);
        assertThat(result.getFirst())
                .extracting(
                        FixTagExplanation::tag,
                        FixTagExplanation::name,
                        FixTagExplanation::value,
                        FixTagExplanation::known
                )
                .containsExactly("8", "BeginString", "FIX.4.4", true);
        assertThat(result.getLast())
                .extracting(
                        FixTagExplanation::tag,
                        FixTagExplanation::name,
                        FixTagExplanation::value,
                        FixTagExplanation::known
                )
                .containsExactly("999", "Unknown", "custom", false);
    }

    @Test
    void explain_invalidMessage_propagatesParserFailure() {
        assertThatThrownBy(() -> explainer.explain("invalid"))
                .isInstanceOf(InvalidFixMessageException.class)
                .hasMessageContaining("numericTag=value");
    }
}
