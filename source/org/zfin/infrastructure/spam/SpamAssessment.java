package org.zfin.infrastructure.spam;

import java.util.List;

/**
 * The outcome of running a submission through {@link SpamDetector}.
 *
 * @param score   total weight of every signal that fired
 * @param reasons human readable "field: description" entries, for the log line a curator will read
 *                when a legitimate submission goes missing
 */
public record SpamAssessment(int score, List<String> reasons) {

    public boolean isSpam() {
        return score >= SpamDetector.SPAM_SCORE_THRESHOLD;
    }

    /**
     * Summary suitable for appending to a log message.
     */
    public String describe() {
        if (reasons.isEmpty()) {
            return "no spam signals";
        }
        return "score " + score + " [" + String.join(", ", reasons) + "]";
    }
}
