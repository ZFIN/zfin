package org.zfin.zirc.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite-PK class for {@link LineSubmissionPerson}. Field names match the
 * @Id-annotated fields on the entity; Hibernate populates this from the
 * referenced entities' primary keys (LineSubmission.zdbID → String, Person.zdbID → String).
 */
public class LineSubmissionPersonId implements Serializable {

    private String lineSubmission;
    private String person;
    private String role;

    public LineSubmissionPersonId() {
    }

    public LineSubmissionPersonId(String lineSubmission, String person, String role) {
        this.lineSubmission = lineSubmission;
        this.person = person;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineSubmissionPersonId other)) return false;
        return Objects.equals(lineSubmission, other.lineSubmission)
                && Objects.equals(person, other.person)
                && Objects.equals(role, other.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineSubmission, person, role);
    }
}
