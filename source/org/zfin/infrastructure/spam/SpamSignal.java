package org.zfin.infrastructure.spam;

/**
 * An individual indication that a form submission was machine generated.
 * <p>
 * Weights are additive; a submission is rejected once the total reaches
 * {@link SpamDetector#SPAM_SCORE_THRESHOLD}. A weight equal to the threshold means the signal is
 * conclusive on its own; lower weights need corroboration from another signal.
 */
public enum SpamSignal {

    /**
     * Interior capital letters in a long word, eg. "zbYbqnZkPWsfthidngV". This is the historical
     * check that the spam filter started with, so it stays conclusive on its own.
     */
    UPPERCASE_HEAVY(3, "unlikely capitalization"),

    /**
     * Repeated switching between upper and lower case within a single word, which random string
     * generators produce and typists essentially never do.
     */
    RANDOM_CASE(2, "alternating case"),

    /**
     * No vowel at all in a word long enough to need one, eg. "Rrztyds".
     */
    NO_VOWELS(1, "no vowels"),

    /**
     * A long stretch of consonants with no vowel to break it up, eg. "Fljqfedtoj".
     */
    CONSONANT_RUN(1, "long consonant run"),

    /**
     * An ORCID that is not a 16 digit identifier. Deliberately weak: now that ORCID is required,
     * a submitter who simply mistypes theirs should get a visible error and a chance to fix it,
     * not a silent discard. Bot ORCIDs like "rPlgedlRSUqMgwEAx" trip the case heuristics as well.
     */
    MALFORMED_ORCID(1, "malformed ORCID"),

    /**
     * A bare link pasted into a free text field.
     */
    LINK_IN_TEXT(1, "link in free text"),

    /**
     * Anchor or BBCode markup in a plain text field, which is only ever link spam.
     */
    LINK_MARKUP_IN_TEXT(3, "link markup in free text");

    private final int weight;
    private final String description;

    SpamSignal(int weight, String description) {
        this.weight = weight;
        this.description = description;
    }

    public int getWeight() {
        return weight;
    }

    public String getDescription() {
        return description;
    }
}
