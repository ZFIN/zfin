package org.zfin.orthology;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.zfin.sequence.ReferenceDatabase;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ortholog_external_reference")
@Getter
@Setter
public class OrthologExternalReference implements Comparable<OrthologExternalReference>, Serializable {

    @EmbeddedId
    private OrthologExternalReferenceId id;

    @ManyToOne
    @MapsId("orthologId")
    @JoinColumn(name = "oef_ortho_zdb_id", nullable = false)
    private Ortholog ortholog;

    @Column(name = "oef_accession_number", insertable = false, updatable = false)
    private String accessionNumber;

    @ManyToOne
    @MapsId("referenceDatabaseId")
    @JoinColumn(name = "oef_fdbcont_zdb_id")
    private ReferenceDatabase referenceDatabase;

    @Override
    public int compareTo(OrthologExternalReference o) {
        return ObjectUtils.compare(
                this.getReferenceDatabase().getForeignDB().getDbName(),
                o.getReferenceDatabase().getForeignDB().getDbName()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrthologExternalReference reference = (OrthologExternalReference) o;
        return ObjectUtils.equals(ortholog, reference.ortholog) &&
                ObjectUtils.equals(accessionNumber, reference.accessionNumber) &&
                ObjectUtils.equals(referenceDatabase, reference.referenceDatabase);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCodeMulti(ortholog, accessionNumber, referenceDatabase);
    }

    /**
     * Static Inner Class to represent the composite ID of this table
     */
    @Embeddable
    @Getter
    @Setter
    public static class OrthologExternalReferenceId implements Serializable {
        @Column(name = "oef_ortho_zdb_id", nullable = false)
        private String orthologId;

        @Column(name = "oef_accession_number", nullable = false)
        private String accessionNumber;

        @Column(name = "oef_fdbcont_zdb_id", nullable = false)
        private String referenceDatabaseId;

        public OrthologExternalReferenceId() {}

        public OrthologExternalReferenceId(String orthologId, String accessionNumber, String referenceDatabaseId) {
            this.orthologId = orthologId;
            this.accessionNumber = accessionNumber;
            this.referenceDatabaseId = referenceDatabaseId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrthologExternalReferenceId that = (OrthologExternalReferenceId) o;
            return ObjectUtils.equals(orthologId, that.orthologId) &&
                    ObjectUtils.equals(accessionNumber, that.accessionNumber) &&
                    ObjectUtils.equals(referenceDatabaseId, that.referenceDatabaseId);
        }

        @Override
        public int hashCode() {
            return ObjectUtils.hashCodeMulti(orthologId, accessionNumber, referenceDatabaseId);
        }
    }
}
