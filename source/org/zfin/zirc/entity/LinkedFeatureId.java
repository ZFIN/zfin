package org.zfin.zirc.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite-PK class for {@link LinkedFeature}. Field names match the
 * @Id-annotated fields on the entity; Hibernate populates this from the
 * referenced entities' primary keys (LineSubmission.zdbID → String,
 * Mutation.id → Long for both A and B).
 */
public class LinkedFeatureId implements Serializable {

    private String lineSubmission;
    private Long mutationA;
    private Long mutationB;

    public LinkedFeatureId() {
    }

    public LinkedFeatureId(String lineSubmission, Long mutationA, Long mutationB) {
        this.lineSubmission = lineSubmission;
        this.mutationA = mutationA;
        this.mutationB = mutationB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkedFeatureId other)) return false;
        return Objects.equals(lineSubmission, other.lineSubmission)
                && Objects.equals(mutationA, other.mutationA)
                && Objects.equals(mutationB, other.mutationB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineSubmission, mutationA, mutationB);
    }
}
