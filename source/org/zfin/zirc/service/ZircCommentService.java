package org.zfin.zirc.service;

import org.springframework.stereotype.Service;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.zirc.entity.LineSubmissionComment;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * INSERT-only comment store. Mirrors what the legacy detail page does
 * via raw SQL on {@code zirc.line_submission_comment}; we go through
 * Hibernate so the entity participates in normal session/flush.
 *
 * <p>Read API: enumerate by (recId, scope, fieldName|sectionName);
 * write API: persist one row with current user as author.
 */
@Service
public class ZircCommentService {

    public List<LineSubmissionComment> listField(String recId, String fieldName) {
        return HibernateUtil.currentSession()
                .createQuery(
                    "from ZircLineSubmissionComment "
                  + "where recId = :rid and scope = 'field' and fieldName = :fn "
                  + "order by createdAt asc",
                    LineSubmissionComment.class)
                .setParameter("rid", recId)
                .setParameter("fn", fieldName)
                .list();
    }

    public List<LineSubmissionComment> listSection(String recId, String sectionName) {
        return HibernateUtil.currentSession()
                .createQuery(
                    "from ZircLineSubmissionComment "
                  + "where recId = :rid and scope = 'section' and sectionName = :sn "
                  + "order by createdAt asc",
                    LineSubmissionComment.class)
                .setParameter("rid", recId)
                .setParameter("sn", sectionName)
                .list();
    }

    /** Separator for the (recId, fieldName) composite key — a char that
     *  can't appear in a ZDB-ID or a JSON-pointer leaf. */
    public static final char KEY_SEP = '|';

    public static String fieldKey(String recId, String fieldName) {
        return recId + KEY_SEP + fieldName;
    }

    /**
     * For each (recId, field-scoped fieldName) among {@code recIds}, decide
     * whether the *latest* comment on that field is still open
     * (closed=false), and return the set of {@link #fieldKey} composites for
     * those. Drives the "open comment ⇒ IN_PROGRESS" status overlay.
     *
     * <p>One query for all recIds; latest-wins by createdAt then id.
     */
    public Set<String> openFieldKeys(Collection<String> recIds) {
        if (recIds == null || recIds.isEmpty()) return Set.of();
        List<LineSubmissionComment> rows = HibernateUtil.currentSession()
                .createQuery(
                    "from ZircLineSubmissionComment "
                  + "where recId in :rids and scope = 'field' "
                  + "order by createdAt asc, id asc",
                    LineSubmissionComment.class)
                .setParameterList("rids", recIds)
                .list();
        // Ascending order means the last write per key is the latest comment.
        Map<String, Boolean> latestClosed = new LinkedHashMap<>();
        for (LineSubmissionComment c : rows) {
            if (c.getFieldName() == null) continue;
            latestClosed.put(fieldKey(c.getRecId(), c.getFieldName()), c.isClosed());
        }
        Set<String> open = new HashSet<>();
        latestClosed.forEach((k, closed) -> { if (!closed) open.add(k); });
        return open;
    }

    public LineSubmissionComment add(String recId, String scope,
                                     String fieldName, String sectionName,
                                     Person author, String body, boolean closed) {
        if (recId == null || recId.isBlank()) {
            throw new IllegalArgumentException("recId is required");
        }
        if (!"field".equals(scope) && !"section".equals(scope)) {
            throw new IllegalArgumentException("scope must be 'field' or 'section'");
        }
        if ("field".equals(scope) && (fieldName == null || fieldName.isBlank())) {
            throw new IllegalArgumentException("fieldName is required for scope='field'");
        }
        if ("section".equals(scope) && (sectionName == null || sectionName.isBlank())) {
            throw new IllegalArgumentException("sectionName is required for scope='section'");
        }
        boolean hasBody = body != null && !body.isBlank();
        // A bare "mark closed" (checkbox only, no text) is allowed; an empty
        // body with closed=false would be a no-op, so reject that.
        if (!hasBody && !closed) {
            throw new IllegalArgumentException("comment text or the closed flag is required");
        }
        if (author == null) {
            throw new IllegalArgumentException("authenticated user required");
        }

        LineSubmissionComment c = new LineSubmissionComment();
        c.setRecId(recId);
        c.setScope(scope);
        c.setFieldName("field".equals(scope) ? fieldName : null);
        c.setSectionName("section".equals(scope) ? sectionName : null);
        c.setAuthor(author);
        c.setComment(hasBody ? body.trim() : "");   // lsc_comment is NOT NULL
        c.setClosed(closed);

        HibernateUtil.createTransaction();
        try {
            HibernateUtil.currentSession().persist(c);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
        return c;
    }
}
