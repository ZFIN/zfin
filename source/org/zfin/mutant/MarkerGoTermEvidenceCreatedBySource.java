package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "marker_go_term_evidence_annotation_created_by_source")
public class MarkerGoTermEvidenceCreatedBySource {

    @Id
    @Column(name = "mrkrgoevcb_pk_id")
    private String id;
    @Column(name = "mrkrgoevcb_name", nullable = false)
    private String name;
    @Column(name = "mrkrgoevcb_url")
    private String url;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MarkerGoTermEvidenceCreatedBySource");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
