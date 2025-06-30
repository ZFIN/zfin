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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oef_pk_id")
    private long oefId;

    @ManyToOne
    @JoinColumn(name = "oef_ortho_zdb_id", nullable = false)
    private Ortholog ortholog;

    @Column(name = "oef_accession_number")
    private String accessionNumber;

    @ManyToOne
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

}
