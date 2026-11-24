package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Answer to a yes/no question that also accepts "unknown" (ZFIN-10617).
 * A Java {@code null} means the question has not been answered yet.
 *
 * <p>On the wire the answer is {@code true}, {@code false} or the string
 * {@value #UNKNOWN_WIRE_VALUE}, so a question that gains the third option
 * keeps the JSON shape its plain-boolean siblings use. In the database it is
 * the existing nullable boolean column plus an {@code _unknown} flag column;
 * the entity accessors that combine the two are the only place that pairing
 * is spelled out.
 */
public enum YesNoUnknown {
    YES,
    NO,
    UNKNOWN;

    /** The JSON value for {@link #UNKNOWN}. */
    public static final String UNKNOWN_WIRE_VALUE = "unknown";

    /** The wire value: {@code true}, {@code false} or {@value #UNKNOWN_WIRE_VALUE}. */
    @JsonValue
    public Object wireValue() {
        return switch (this) {
            case YES -> Boolean.TRUE;
            case NO -> Boolean.FALSE;
            case UNKNOWN -> UNKNOWN_WIRE_VALUE;
        };
    }

    /** The yes/no part of the answer; {@code null} for {@link #UNKNOWN}. */
    public Boolean asBoolean() {
        return switch (this) {
            case YES -> Boolean.TRUE;
            case NO -> Boolean.FALSE;
            case UNKNOWN -> null;
        };
    }

    /** Combine the two stored columns; {@code null} when neither is set. */
    public static YesNoUnknown fromColumns(Boolean answer, boolean unknown) {
        if (unknown) {return UNKNOWN;}
        if (answer == null) {return null;}
        return answer ? YES : NO;
    }

    /**
     * Parse a PATCH value strictly. Anything other than {@code null},
     * {@code true}, {@code false} or {@value #UNKNOWN_WIRE_VALUE} is rejected
     * rather than coerced, so a typo cannot land in the database as "No".
     *
     * @throws IllegalArgumentException for any other value (a 400 via
     *         {@code ZircApiExceptionHandler})
     */
    public static YesNoUnknown fromJson(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        if (v.isBoolean()) {return v.booleanValue() ? YES : NO;}
        if (v.isTextual() && UNKNOWN_WIRE_VALUE.equals(v.textValue())) {return UNKNOWN;}
        throw new IllegalArgumentException(
                "Expected true, false, \"" + UNKNOWN_WIRE_VALUE + "\" or null, got: " + v);
    }
}
