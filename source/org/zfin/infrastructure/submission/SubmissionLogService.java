package org.zfin.infrastructure.submission;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.spam.SpamAssessment;

import java.util.List;

/**
 * Records public form submissions for periodic review.
 * <p>
 * Every method here is best effort: logging a submission must never be the reason a legitimate
 * request fails, so persistence problems are logged and swallowed rather than propagated to the
 * submitter.
 */
@Log4j2
public class SubmissionLogService {

    /**
     * Defensive caps, since these values come from the client.
     */
    private static final int MAX_FIELD_LENGTH = 500;
    private static final int MAX_DETAILS_LENGTH = 8000;

    /**
     * Builds a log entry for a submission. The caller fills in the outcome and saves it with
     * {@link #save(SubmissionLog)}.
     */
    public static SubmissionLog build(SubmissionType type, HttpServletRequest request) {
        SubmissionLog entry = new SubmissionLog();
        entry.setType(type);
        entry.setIpAddress(truncate(getClientIpAddress(request), MAX_FIELD_LENGTH));
        entry.setUserAgent(truncate(request == null ? null : request.getHeader("User-Agent"), MAX_FIELD_LENGTH));
        return entry;
    }

    public static void setSubmitter(SubmissionLog entry, String name, String email, String orcid) {
        entry.setName(truncate(name, MAX_FIELD_LENGTH));
        entry.setEmail(truncate(email, MAX_FIELD_LENGTH));
        entry.setOrcid(truncate(orcid, MAX_FIELD_LENGTH));
    }

    public static void setDetails(SubmissionLog entry, String details) {
        entry.setDetails(truncate(details, MAX_DETAILS_LENGTH));
    }

    /**
     * Stores a person submission field by field, so the account creation form can be prefilled from
     * the request rather than from a hand copy of the notification email.
     */
    public static void setPersonFields(SubmissionLog entry, String firstName, String lastName, String address,
                                       String country, String phone, String lab, String role, String url,
                                       String comments) {
        entry.setFirstName(truncate(firstName, MAX_FIELD_LENGTH));
        entry.setLastName(truncate(lastName, MAX_FIELD_LENGTH));
        entry.setAddress(truncate(address, MAX_FIELD_LENGTH));
        entry.setCountry(truncate(country, MAX_FIELD_LENGTH));
        entry.setPhone(truncate(phone, MAX_FIELD_LENGTH));
        entry.setLab(truncate(lab, MAX_FIELD_LENGTH));
        entry.setRole(truncate(role, MAX_FIELD_LENGTH));
        entry.setUrl(truncate(url, MAX_FIELD_LENGTH));
        entry.setComments(truncate(comments, MAX_DETAILS_LENGTH));
    }

    /**
     * Looks up a logged submission, for prefilling the account creation form.
     *
     * @return the entry, or null if the id is unknown or unreadable
     */
    public static SubmissionLog getById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return HibernateUtil.currentSession().get(SubmissionLog.class, id);
        } catch (Exception e) {
            log.error("Failed to load submission_log entry " + id, e);
            return null;
        }
    }

    public static void setAssessment(SubmissionLog entry, SpamAssessment assessment) {
        entry.setSpamScore(assessment.score());
        entry.setSpamReasons(truncate(String.join("; ", assessment.reasons()), MAX_DETAILS_LENGTH));
    }

    public static void setValidationErrors(SubmissionLog entry, List<String> errors) {
        entry.setValidationErrors(truncate(String.join("; ", errors), MAX_DETAILS_LENGTH));
    }

    /**
     * Persists the entry, and records the outcome in the application log either way.
     */
    public static void save(SubmissionLog entry) {
        try {
            HibernateUtil.createTransaction();
            HibernateUtil.currentSession().persist(entry);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            log.error("Failed to record " + entry.getType() + " submission in submission_log", e);
            try {
                HibernateUtil.rollbackTransaction();
            } catch (Exception rollbackFailure) {
                log.error("Failed to roll back submission_log transaction", rollbackFailure);
            }
        }
    }

    /**
     * Convenience for the common case: stamp the outcome and save.
     */
    public static void save(SubmissionLog entry, SubmissionOutcome outcome) {
        entry.setOutcome(outcome);
        save(entry);
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isBlank(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
