package org.zfin.zirc.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;

/**
 * Single entry point for mutating a line submission. Centralized so future
 * cross-cutting concerns (versioning via {@code Updates}, validation, audit
 * logging) have one place to hook in. Callers manage the transaction.
 */
@Service
@Log4j2
public class LineSubmissionService {

    /**
     * Persist a single field change on a line submission. If {@code zdbID} is
     * null/blank, a new submission is created with {@code is_draft = true} (DB
     * default) and {@code currentUser} auto-linked as a "submitter" — matches
     * the previous {@code /new} behavior, just deferred until the curator
     * actually types something.
     *
     * @return the (possibly newly created) submission, with the field applied.
     */
    public LineSubmission saveField(String zdbID, String fieldName, String rawValue, Person currentUser) {
        String value = (rawValue != null && !rawValue.isBlank()) ? rawValue.trim() : null;
        LineSubmission submission = loadOrCreate(zdbID, currentUser);
        applyField(submission, fieldName, value);
        HibernateUtil.currentSession().merge(submission);
        return submission;
    }

    /**
     * Persist the acceptance-reasons section: an array of canonical snake_case
     * option values plus a single optional free-text "Other" entry. As with
     * {@link #saveField}, a new submission is created on the fly when
     * {@code zdbID} is null/blank.
     */
    public LineSubmission saveReasons(String zdbID, String[] reasons, String reasonsOther, Person currentUser) {
        LineSubmission submission = loadOrCreate(zdbID, currentUser);
        submission.setReasons(reasons != null ? reasons : new String[0]);
        String trimmed = (reasonsOther != null && !reasonsOther.isBlank()) ? reasonsOther.trim() : null;
        submission.setReasonsOther(trimmed);
        HibernateUtil.currentSession().merge(submission);
        return submission;
    }

    private LineSubmission loadOrCreate(String zdbID, Person currentUser) {
        if (zdbID == null || zdbID.isBlank()) {
            LineSubmission submission = new LineSubmission();
            HibernateUtil.currentSession().persist(submission);
            linkSubmitter(submission, currentUser);
            return submission;
        }
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        return submission;
    }

    private void linkSubmitter(LineSubmission submission, Person currentUser) {
        if (currentUser == null || currentUser.getZdbID() == null) {
            return;
        }
        // Re-attach: getCurrentSecurityUser() returns a Person from the security context
        // that's detached from this session, which would trip the @Id @ManyToOne cascade.
        Person attached = HibernateUtil.currentSession().getReference(Person.class, currentUser.getZdbID());
        LineSubmissionPerson lsp = new LineSubmissionPerson();
        lsp.setLineSubmission(submission);
        lsp.setPerson(attached);
        lsp.setRole("submitter");
        lsp.setSortOrder(1);
        HibernateUtil.currentSession().persist(lsp);
    }

    /** Package-private for unit testing. */
    static void applyField(LineSubmission s, String fieldName, String value) {
        switch (fieldName) {
            case "name":                       s.setName(value); break;
            case "abbreviation":               s.setAbbreviation(value); break;
            case "previousNames":              s.setPreviousNames(value); break;
            case "featuresLinked":             s.setFeaturesLinked(parseTriBool(value)); break;
            case "maternalBackground":         s.setMaternalBackground(value); break;
            case "paternalBackground":         s.setPaternalBackground(value); break;
            case "backgroundChangeable":       s.setBackgroundChangeable(parseTriBool(value)); break;
            case "backgroundChangeConcerns":   s.setBackgroundChangeConcerns(value); break;
            case "unreportedFeaturesDetails":  s.setUnreportedFeaturesDetails(value); break;
            case "additionalInfo":             s.setAdditionalInfo(value); break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown field: " + fieldName);
        }
    }

    /** Package-private for unit testing. */
    static Boolean parseTriBool(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
