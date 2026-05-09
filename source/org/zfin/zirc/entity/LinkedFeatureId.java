package org.zfin.zirc.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite-PK class for {@link LinkedFeature}. Field names match the
 * @Id-annotated fields on the entity; Hibernate populates this from
 * the referenced LineSubmission.zdbID plus the feature string.
 */
public class LinkedFeatureId implements Serializable {

    private String lineSubmission;
    private String feature;

    public LinkedFeatureId() {
    }

    public LinkedFeatureId(String lineSubmission, String feature) {
        this.lineSubmission = lineSubmission;
        this.feature = feature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkedFeatureId other)) return false;
        return Objects.equals(lineSubmission, other.lineSubmission)
                && Objects.equals(feature, other.feature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineSubmission, feature);
    }
}
