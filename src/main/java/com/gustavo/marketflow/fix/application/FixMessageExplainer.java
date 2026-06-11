package com.gustavo.marketflow.fix.application;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gustavo.marketflow.fix.domain.FixTag;
import com.gustavo.marketflow.fix.domain.FixTagExplanation;

/**
 * Converts parsed tags into interview-friendly names and descriptions.
 */
@Component
public class FixMessageExplainer {

    private static final String UNKNOWN_TAG_NAME = "Unknown";
    private static final String UNKNOWN_TAG_DESCRIPTION = "Tag is not part of the Phase 6 simulated FIX catalogue";

    private final FixMessageParser fixMessageParser;

    public FixMessageExplainer(FixMessageParser fixMessageParser) {
        this.fixMessageParser = fixMessageParser;
    }

    public List<FixTagExplanation> explain(String rawMessage) {
        return fixMessageParser.parse(rawMessage).entrySet().stream()
                .map(this::explainField)
                .toList();
    }

    private FixTagExplanation explainField(Map.Entry<String, String> field) {
        return FixTag.fromNumber(field.getKey())
                .map(tag -> new FixTagExplanation(
                        field.getKey(),
                        tag.fieldName(),
                        field.getValue(),
                        tag.description(),
                        true
                ))
                .orElseGet(() -> new FixTagExplanation(
                        field.getKey(),
                        UNKNOWN_TAG_NAME,
                        field.getValue(),
                        UNKNOWN_TAG_DESCRIPTION,
                        false
                ));
    }
}
