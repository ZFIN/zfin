package org.zfin.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parsing and validation for ORCID identifiers.
 * <p>
 * An ORCID is 16 digits, written in four dash separated groups, where the last character may be an
 * X check digit: {@code 0000-0002-1825-0097}. This matches the format {@code Person.orcidID}
 * stores.
 */
public final class OrcidUtil {

    /**
     * The canonical form we store and display.
     */
    private static final Pattern CANONICAL = Pattern.compile("\\d{4}-\\d{4}-\\d{4}-\\d{3}[\\dX]");

    /**
     * 16 digits with all punctuation removed, before we re-insert the dashes.
     */
    private static final Pattern BARE_DIGITS = Pattern.compile("\\d{15}[\\dX]");

    /**
     * Submitters routinely paste the whole ORCID URL rather than just the identifier.
     */
    private static final Pattern URL_PREFIX = Pattern.compile("(?i)^(https?://)?(www\\.)?orcid\\.org/");

    /**
     * Values people type in place of leaving an optional field blank. Treated as "not provided"
     * rather than as a malformed identifier.
     */
    private static final Set<String> PLACEHOLDERS = Set.of(
            "n/a", "na", "n.a.", "none", "no", "nil", "null", "unknown", "not available",
            "not applicable", "tbd", "pending", "?", "-", "--", "x", "0");

    private OrcidUtil() {
    }

    /**
     * Removes a leading orcid.org URL, leaving just the identifier as typed.
     */
    public static String stripUrlPrefix(String value) {
        return URL_PREFIX.matcher(StringUtils.trimToEmpty(value)).replaceFirst("");
    }

    /**
     * True if the value is empty or one of the stand-ins people type for "I don't have one".
     */
    public static boolean isBlankOrPlaceholder(String value) {
        String trimmed = StringUtils.trimToEmpty(value);
        return trimmed.isEmpty() || PLACEHOLDERS.contains(trimmed.toLowerCase(Locale.ROOT));
    }

    /**
     * Rewrites a submitted ORCID into canonical {@code 0000-0002-1825-0097} form, tolerating a
     * pasted URL, missing dashes and surrounding whitespace.
     *
     * @return the canonical identifier, or an empty string if the value is not an ORCID at all
     */
    public static String normalize(String value) {
        String trimmed = StringUtils.trimToEmpty(value);
        if (trimmed.isEmpty()) {
            return "";
        }
        String bare = URL_PREFIX.matcher(trimmed)
                .replaceFirst("")
                .replaceAll("[\\s\\u2010-\\u2015_-]", "")
                .toUpperCase(Locale.ROOT);
        if (!BARE_DIGITS.matcher(bare).matches()) {
            return "";
        }
        return bare.substring(0, 4) + "-" + bare.substring(4, 8) + "-"
                + bare.substring(8, 12) + "-" + bare.substring(12, 16);
    }

    /**
     * True if the value can be read as an ORCID, in any of the forms people submit.
     */
    public static boolean isValid(String value) {
        return !normalize(value).isEmpty();
    }

    /**
     * True if the value is already in canonical form.
     */
    public static boolean isCanonical(String value) {
        return value != null && CANONICAL.matcher(value).matches();
    }
}
