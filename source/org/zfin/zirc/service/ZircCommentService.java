package org.zfin.zirc.service;

import org.springframework.stereotype.Service;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.zirc.entity.LineSubmissionComment;

import java.util.List;

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

    public LineSubmissionComment add(String recId, String scope,
                                     String fieldName, String sectionName,
                                     Person author, String body) {
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
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("comment body is required");
        }
        if (author == null) {
            throw new IllegalArgumentException("authenticated user required");
        }

        LineSubmissionComment c = new LineSubmissionComment();
        c.setRecId(recId);
        c.setScope(scope);
        c.setFieldName("field".equals(scope) ? fieldName : null);
        c.setSectionName("section".equals(scope) ? sectionName : null);
        c.setAuthorZdbId(author.getZdbID());
        c.setComment(body.trim());

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
