package org.zfin.zirc.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;
import org.zfin.zirc.entity.LinkedFeature;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.presentation.LinkedFeatureDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Replace-all save for the linked-features section. Diffs the incoming
     * list against the current collection (keyed on feature name) so we
     * update existing rows in place, insert new ones, and remove the rest —
     * avoiding the PK-violation hazard of clear() + add() with cascade
     * orphan removal.
     *
     * <p>Empty / blank feature names are dropped; duplicate feature names
     * are deduped (last write wins for the row's distance fields).
     */
    public LineSubmission saveLinkedFeatures(String zdbID, List<LinkedFeatureDTO> incoming, Person currentUser) {
        LineSubmission submission = loadOrCreate(zdbID, currentUser);
        Map<String, LinkedFeature> existing = new HashMap<>();
        for (LinkedFeature lf : submission.getLinkedFeatures()) {
            existing.put(lf.getFeature(), lf);
        }
        Set<String> incomingFeatures = new HashSet<>();

        if (incoming != null) {
            for (LinkedFeatureDTO dto : incoming) {
                if (dto.getFeature() == null || dto.getFeature().isBlank()) {
                    continue;
                }
                String feature = dto.getFeature().trim();
                if (!incomingFeatures.add(feature)) {
                    continue;
                }
                LinkedFeature lf = existing.get(feature);
                if (lf == null) {
                    lf = new LinkedFeature();
                    lf.setLineSubmission(submission);
                    lf.setFeature(feature);
                    submission.getLinkedFeatures().add(lf);
                }
                lf.setDistanceKnown(dto.getDistanceKnown());
                lf.setDistanceCentimorgans(dto.getDistanceCentimorgans());
                lf.setDistanceMegabases(dto.getDistanceMegabases());
                String info = dto.getAdditionalInfo();
                lf.setAdditionalInfo(info != null && !info.isBlank() ? info.trim() : null);
            }
        }

        // Drop any existing rows that didn't appear in the incoming list.
        submission.getLinkedFeatures().removeIf(lf -> !incomingFeatures.contains(lf.getFeature()));
        return submission;
    }

    /**
     * Append a new mutation to the given submission. Creates the parent
     * submission too if {@code lsId} is null/blank — same flow as
     * {@link #saveField}, just initiated from the Mutations section's "Add"
     * button before the user has typed any scalar fields.
     *
     * @return the newly persisted mutation (with its IDENTITY-assigned id).
     */
    public Mutation addMutation(String lsId, Person currentUser) {
        LineSubmission submission = loadOrCreate(lsId, currentUser);
        Mutation m = new Mutation();
        m.setLineSubmission(submission);
        m.setSortOrder(nextMutationSortOrder(submission));
        HibernateUtil.currentSession().persist(m);
        submission.getMutations().add(m);
        return m;
    }

    private int nextMutationSortOrder(LineSubmission submission) {
        return submission.getMutations().stream()
                .map(Mutation::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    public void removeMutation(Long mutationId) {
        Mutation m = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        // Detach from parent collection so orphanRemoval fires.
        m.getLineSubmission().getMutations().remove(m);
        HibernateUtil.currentSession().remove(m);
    }

    /**
     * Per-field save for a mutation. Mirrors {@link #saveField} but on a
     * mutation by primary-key id (no create-on-first-save here — mutations
     * are created via {@link #addMutation}).
     */
    public Mutation saveMutationField(Long mutationId, String fieldName, String rawValue, Person currentUser) {
        Mutation m = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        String value = (rawValue != null && !rawValue.isBlank()) ? rawValue.trim() : null;
        applyMutationField(m, fieldName, value);
        HibernateUtil.currentSession().merge(m);
        return m;
    }

    /** Package-private for unit testing. */
    static void applyMutationField(Mutation m, String fieldName, String value) {
        switch (fieldName) {
            case "alleleDesignation":          m.setAlleleDesignation(value); break;
            case "mutagenesisProtocol":        m.setMutagenesisProtocol(value); break;
            case "molecularlyCharacterized":   m.setMolecularlyCharacterized(parseTriBool(value)); break;
            case "mutationType":               m.setMutationType(value); break;
            case "homozygousLethal":           m.setHomozygousLethal(parseTriBool(value)); break;
            case "lethalityStageTypical":      m.setLethalityStageTypical(value); break;
            case "lethalitySpecificTimepoint": m.setLethalitySpecificTimepoint(value); break;
            case "lethalityWindowStart":       m.setLethalityWindowStart(value); break;
            case "lethalityWindowEnd":         m.setLethalityWindowEnd(value); break;
            case "lethalityAdditionalInfo":    m.setLethalityAdditionalInfo(value); break;
            case "zfinRecordEstablished":      m.setZfinRecordEstablished(parseTriBool(value)); break;
            case "cellGenomicFeature":         m.setCellGenomicFeature(value); break;
            case "mutationDiscoverer":         m.setMutationDiscoverer(value); break;
            case "mutationInstitution":        m.setMutationInstitution(value); break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown mutation field: " + fieldName);
        }
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
