package org.zfin.sequence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "entrez_to_xref")
@Setter
@Getter
public class EntrezOMIM implements Serializable {

    @EmbeddedId
    private EntrezOMIMId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ex_entrez_acc_num", insertable = false, updatable = false, nullable = false)
    private Entrez entrezAccession;

    @Embeddable
    @Getter
    @Setter
    public static class EntrezOMIMId implements Serializable {

        @Column(name = "ex_entrez_acc_num")
        private String entrezAccessionNum;

        @Column(name = "ex_xref")
        private String omimAccession;
    }

    public int hashCode() {
        int num = 39;
        if (id.omimAccession != null)
            num += id.omimAccession.hashCode();
        if (id.entrezAccessionNum != null)
            num += id.entrezAccessionNum.hashCode();
        if (entrezAccession != null)
            num += entrezAccession.hashCode();
        return num;
    }

    /**
     * This method assumes that omimAccession, omimAccessionNum and entrezAccession are not null.
     * Otherwise this method throws an exception.
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof EntrezOMIM omim))
            return false;

        if (id.omimAccession == null)
            throw new RuntimeException("omimAccession is null but should not!");
        if (omim.id.omimAccession == null)
            throw new RuntimeException("omimAccession is null but should not!");
        if (id.entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (omim.id.entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (entrezAccession == null)
            throw new RuntimeException("entrezAccession is null but should not!");
        if (omim.entrezAccession == null)
            throw new RuntimeException("entrezAccession is null but should not!");

        return id.omimAccession.equals(omim.id.omimAccession) &&
                (id.entrezAccessionNum.equals(omim.id.entrezAccessionNum)) &&
                (entrezAccession.equals(omim.entrezAccession));
    }
}
