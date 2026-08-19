package org.zfin.infrastructure.submission;

/**
 * What we did with a submission, so a curator reviewing the log can tell an approved request from
 * one we threw away.
 */
public enum SubmissionOutcome {

    /**
     * Forwarded to the coordinator for account creation.
     */
    ACCEPTED,

    /**
     * Silently discarded: the honeypot field was filled in.
     */
    REJECTED_HONEYPOT,

    /**
     * Discarded: no valid captcha.
     */
    REJECTED_CAPTCHA,

    /**
     * Discarded: the spam score reached the threshold.
     */
    REJECTED_SPAM,

    /**
     * Sent back to the submitter with a visible error so they can correct it.
     */
    RETURNED_INVALID,

    /**
     * Passed our checks but the notification email failed to send.
     */
    ERROR_SENDING;

    public boolean isAccepted() {
        return this == ACCEPTED;
    }
}
