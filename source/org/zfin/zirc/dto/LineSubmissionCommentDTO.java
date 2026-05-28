package org.zfin.zirc.dto;

import org.zfin.profile.Person;
import org.zfin.zirc.entity.LineSubmissionComment;

import java.text.SimpleDateFormat;

/**
 * Wire shape for one comment row. Author is denormalized into {@code
 * authorName} so the React popup can render initials/last-name without a
 * second person fetch; the raw zdbID stays available for profile links.
 */
public record LineSubmissionCommentDTO(
        Long id,
        String recId,
        String scope,
        String fieldName,
        String sectionName,
        String authorZdbId,
        String authorName,
        String comment,
        boolean closed,
        String createdAt) {

    public static LineSubmissionCommentDTO of(LineSubmissionComment c, Person author) {
        String name;
        if (author == null) {
            name = c.getAuthor().getZdbID();
        } else {
            String initial = (author.getFirstName() == null || author.getFirstName().isBlank())
                    ? "" : author.getFirstName().charAt(0) + ". ";
            name = initial + (author.getLastName() == null ? c.getAuthor().getZdbID() : author.getLastName());
        }
        return new LineSubmissionCommentDTO(
                c.getId(),
                c.getRecId(),
                c.getScope(),
                c.getFieldName(),
                c.getSectionName(),
                c.getAuthor().getZdbID(),
                name,
                c.getComment(),
                c.isClosed(),
                c.getCreatedAt() == null
                        ? null
                        : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(c.getCreatedAt()));
    }
}
