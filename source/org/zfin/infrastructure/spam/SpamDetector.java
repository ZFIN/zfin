package org.zfin.infrastructure.spam;

import org.apache.commons.lang3.StringUtils;

import org.zfin.util.OrcidUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Heuristics for recognizing machine generated account and comment submissions.
 * <p>
 * Bots filling in our public forms produce random strings in every field, eg.
 * <pre>
 * First Name: Uryaehw          Lab: zbYbqnZkPWsfthidngV
 * Last Name: Rrztyds           ORCID: rPlgedlRSUqMgwEAx
 * Address: Fljqfedtoj          Comments: fwCkHqJmDxhvjWRxrkfaP
 * </pre>
 * Each field is scored independently and the weights are summed, so a submission is only rejected
 * on a single conclusive signal (see {@link SpamSignal#UPPERCASE_HEAVY}) or on several weaker ones
 * agreeing. That matters because our submitters are scientists worldwide: any one heuristic tuned
 * tightly enough to catch short gibberish will also catch somebody's real name, and silently
 * discarding a real account request is worse than forwarding a spam one.
 * <p>
 * Usage:
 * <pre>
 * SpamAssessment assessment = SpamDetector.examine()
 *         .name("firstName", submission.getFirstName())
 *         .freeText("comments", submission.getComments())
 *         .orcid("orcid", submission.getOrcid())
 *         .assess();
 * </pre>
 */
public final class SpamDetector {

    /**
     * Total signal weight at which a submission is treated as spam.
     */
    public static final int SPAM_SCORE_THRESHOLD = 3;

    /**
     * Words shorter than this carry no reliable signal ("Ng", "Xu", "Kim" are all real names).
     */
    private static final int MIN_WORD_LENGTH = 5;

    private static final int UPPERCASE_HEAVY_MIN_LENGTH = 10;
    private static final int UPPERCASE_HEAVY_MIN_COUNT = 3;
    private static final int RANDOM_CASE_MIN_LENGTH = 8;
    private static final int RANDOM_CASE_MIN_TRANSITIONS = 3;
    private static final int NO_VOWEL_MIN_LENGTH = 6;

    /**
     * Six rather than five: five spares real words that pile up consonants across a compound
     * ("Angstrom", "Handschrift") and the "https" left behind by a pasted link.
     */
    private static final int CONSONANT_RUN_MIN_LENGTH = 6;

    /**
     * Split on anything that is not a letter or digit, so "Marie-Claire", "O'Brien" and
     * "sub.domain.example.org" are each examined a word at a time.
     */
    private static final Pattern WORD_SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");

    private static final Pattern URL_SCHEME = Pattern.compile("(?i)^[a-z][a-z0-9+.-]*://");

    private static final String VOWELS = "aeiou";

    private SpamDetector() {
    }

    public static Builder examine() {
        return new Builder();
    }

    /**
     * Collects signals across the fields of one submission.
     */
    public static class Builder {

        private int score;
        private final List<String> reasons = new ArrayList<>();

        /**
         * A person, lab or institution name: examined for gibberish.
         */
        public Builder name(String fieldName, String value) {
            return text(fieldName, value);
        }

        /**
         * Any short text field: examined for gibberish.
         */
        public Builder text(String fieldName, String value) {
            for (String word : words(value)) {
                examineWord(fieldName, word);
            }
            return this;
        }

        /**
         * A comment or message box: examined for gibberish and for link spam.
         */
        public Builder freeText(String fieldName, String value) {
            text(fieldName, value);
            examineForLinks(fieldName, value);
            return this;
        }

        /**
         * A URL field: the host and path are examined for gibberish, but a link here is expected.
         */
        public Builder url(String fieldName, String value) {
            return text(fieldName, URL_SCHEME.matcher(StringUtils.defaultString(value)).replaceFirst(""));
        }

        /**
         * An ORCID field, which has a strict format we can check outright. A real ORCID is all
         * digits, so the gibberish heuristics apply to whatever a bot put here instead.
         */
        public Builder orcid(String fieldName, String value) {
            if (OrcidUtil.isBlankOrPlaceholder(value)) {
                // A missing ORCID is a validation matter, not evidence of a bot.
                return this;
            }
            // Strip any orcid.org prefix first, so a correctly pasted URL is not examined as words.
            text(fieldName, OrcidUtil.stripUrlPrefix(value));
            if (!OrcidUtil.isValid(value)) {
                add(fieldName, SpamSignal.MALFORMED_ORCID);
            }
            return this;
        }

        public SpamAssessment assess() {
            return new SpamAssessment(score, List.copyOf(reasons));
        }

        private void examineWord(String fieldName, String word) {
            if (word.length() < MIN_WORD_LENGTH || containsDigit(word)) {
                // street numbers, phone numbers and postcodes are not gibberish
                return;
            }

            // Some people type their name in all caps, and acronyms are everywhere in an
            // institution name, so all-uppercase words get a pass on the case heuristics.
            if (!StringUtils.isAllUpperCase(word)) {
                if (word.length() >= UPPERCASE_HEAVY_MIN_LENGTH
                        && countUpperCase(word) >= UPPERCASE_HEAVY_MIN_COUNT) {
                    add(fieldName, SpamSignal.UPPERCASE_HEAVY, word);
                }
                if (word.length() >= RANDOM_CASE_MIN_LENGTH
                        && countCaseTransitions(word) >= RANDOM_CASE_MIN_TRANSITIONS) {
                    add(fieldName, SpamSignal.RANDOM_CASE, word);
                }
            }

            // Vowel heuristics only make sense for the Latin alphabet. Skipping anything else
            // avoids penalizing names written with accents or in a non-Latin script.
            if (!isAsciiAlpha(word)) {
                return;
            }
            String lower = word.toLowerCase(Locale.ROOT);
            if (lower.length() >= NO_VOWEL_MIN_LENGTH && !containsVowel(lower)) {
                add(fieldName, SpamSignal.NO_VOWELS, word);
            }
            if (longestConsonantRun(lower) >= CONSONANT_RUN_MIN_LENGTH) {
                add(fieldName, SpamSignal.CONSONANT_RUN, word);
            }
        }

        private void examineForLinks(String fieldName, String value) {
            String lower = StringUtils.defaultString(value).toLowerCase(Locale.ROOT);
            if (containsAnyOf(lower, "<a ", "<a\t", "<a\n", "href=", "[url", "[/url]", "[link")) {
                add(fieldName, SpamSignal.LINK_MARKUP_IN_TEXT);
            } else if (containsAnyOf(lower, "http://", "https://", "www.")) {
                add(fieldName, SpamSignal.LINK_IN_TEXT);
            }
        }

        private void add(String fieldName, SpamSignal signal) {
            add(fieldName, signal, null);
        }

        private void add(String fieldName, SpamSignal signal, String word) {
            score += signal.getWeight();
            String reason = fieldName + ": " + signal.getDescription();
            reasons.add(word == null ? reason : reason + " ('" + word + "')");
        }
    }

    private static List<String> words(String value) {
        if (StringUtils.isBlank(value)) {
            return List.of();
        }
        List<String> words = new ArrayList<>();
        for (String word : WORD_SEPARATOR.split(value.trim())) {
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }

    private static boolean containsDigit(String word) {
        return word.chars().anyMatch(Character::isDigit);
    }

    private static boolean isAsciiAlpha(String word) {
        return word.chars().allMatch(c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }

    private static boolean containsVowel(String lowerCaseWord) {
        return lowerCaseWord.chars().anyMatch(c -> VOWELS.indexOf(c) >= 0);
    }

    private static boolean containsAnyOf(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static int countUpperCase(String word) {
        return (int) word.chars().filter(Character::isUpperCase).count();
    }

    /**
     * Number of places where the word switches between upper and lower case. "McDonald" has 2,
     * "zbYbqnZkPWsfthidngV" has 8.
     */
    private static int countCaseTransitions(String word) {
        int transitions = 0;
        for (int i = 1; i < word.length(); i++) {
            char previous = word.charAt(i - 1);
            char current = word.charAt(i);
            if (!Character.isLetter(previous) || !Character.isLetter(current)) {
                continue;
            }
            if (Character.isUpperCase(previous) != Character.isUpperCase(current)) {
                transitions++;
            }
        }
        return transitions;
    }

    /**
     * Longest stretch of consonants, treating 'y' as a vowel so that names like "Krzysztof" are
     * not mistaken for gibberish.
     */
    private static int longestConsonantRun(String lowerCaseWord) {
        int longest = 0;
        int current = 0;
        for (char c : lowerCaseWord.toCharArray()) {
            if (VOWELS.indexOf(c) >= 0 || c == 'y') {
                current = 0;
            } else {
                current++;
                longest = Math.max(longest, current);
            }
        }
        return longest;
    }
}
