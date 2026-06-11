package com.gustavo.marketflow.fix.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.gustavo.marketflow.shared.exception.InvalidFixMessageException;

/**
 * Parses the pipe-delimited simulated FIX format while preserving tag order.
 */
@Component
public class FixMessageParser {

    private static final int MAX_MESSAGE_LENGTH = 4_096;

    public Map<String, String> parse(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new InvalidFixMessageException("FIX message must not be blank");
        }
        if (rawMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidFixMessageException("FIX message exceeds 4096 characters");
        }

        Map<String, String> fields = new LinkedHashMap<>();
        for (String field : rawMessage.split("\\|", -1)) {
            parseField(field, fields);
        }
        validateRequiredFields(fields);
        return Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }

    private void parseField(String field, Map<String, String> fields) {
        int separator = field.indexOf('=');
        if (separator <= 0 || separator == field.length() - 1 || separator != field.lastIndexOf('=')) {
            throw new InvalidFixMessageException("Each FIX field must use the format numericTag=value");
        }

        String tag = field.substring(0, separator);
        String value = field.substring(separator + 1);
        if (!tag.chars().allMatch(Character::isDigit)) {
            throw new InvalidFixMessageException("FIX tags must be numeric");
        }
        if (value.isBlank()) {
            throw new InvalidFixMessageException("FIX tag values must not be blank");
        }
        if (fields.putIfAbsent(tag, value) != null) {
            throw new InvalidFixMessageException("Duplicate FIX tag: " + tag);
        }
    }

    private void validateRequiredFields(Map<String, String> fields) {
        if (!"FIX.4.4".equals(fields.get("8"))) {
            throw new InvalidFixMessageException("Tag 8 must declare FIX.4.4");
        }
        if (!fields.containsKey("35")) {
            throw new InvalidFixMessageException("Required FIX tag is missing: 35");
        }
    }
}
