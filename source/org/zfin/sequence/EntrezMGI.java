package org.zfin.sequence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.zfin.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.Serializable;


@Entity
@Table(name = "entrez_to_xref")
@Setter
@Getter
public class EntrezMGI implements Serializable {

    @EmbeddedId
    private EntrezMGIId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ex_entrez_acc_num", insertable = false, updatable = false, nullable = false)
    private Entrez entrezAccession;

    @Embeddable
    @Getter
    @Setter
    public static class EntrezMGIId implements Serializable {

        @Column(name = "ex_entrez_acc_num")
        private String entrezAccessionNum;

        @Column(name = "ex_xref")
        private String mgiAccession;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            EntrezMGIId that = (EntrezMGIId) o;

            return new EqualsBuilder().append(getEntrezAccessionNum(), that.getEntrezAccessionNum()).append(getMgiAccession(), that.getMgiAccession()).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getEntrezAccessionNum()).append(getMgiAccession()).toHashCode();
        }
    }

    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    public ReferenceDatabase getRefDB() {
        return sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.MGI,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.MOUSE);
    }

    public int hashCode() {
        int num = 39;
        if (id.entrezAccessionNum != null)
            num += id.entrezAccessionNum.hashCode();
        if (id.mgiAccession != null)
            num += id.mgiAccession.hashCode();
        return num;
    }

    /**
     * This method assumes that mgiAccession, entrezAccessionNum are not null.
     * Otherwise this method throws an exception.
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof EntrezMGI mgi))
            return false;

        if (id.entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (mgi.id.entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (id.mgiAccession == null)
            throw new RuntimeException("mgiAccession is null but should not!");
        if (mgi.id.mgiAccession == null)
            throw new RuntimeException("mgiAccession is null but should not!");

        return (id.entrezAccessionNum.equals(mgi.id.entrezAccessionNum)) &&
                (entrezAccession.equals(mgi.entrezAccession));
    }
}
