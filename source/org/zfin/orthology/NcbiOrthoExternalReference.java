package org.zfin.orthology;

import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ReferenceDatabase;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ncbi_ortholog_external_reference")
public class NcbiOrthoExternalReference implements Comparable<NcbiOrthoExternalReference>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noer_pk_id")
    private long ID;

    @ManyToOne
    @JoinColumn(name = "noer_fdbcont_zdb_id")
    private ReferenceDatabase referenceDatabase;

    @ManyToOne
    @JoinColumn(name = "noer_other_species_ncbi_gene_id", nullable = false)
    private NcbiOtherSpeciesGene ncbiOtherSpeciesGene;

    @Column(name = "noer_other_species_accession_number")
    private String accessionNumber;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public NcbiOtherSpeciesGene getNcbiOtherSpeciesGene() {
        return ncbiOtherSpeciesGene;
    }

    public void setNcbiOtherSpeciesGene(NcbiOtherSpeciesGene ncbiOtherSpeciesGene) {
        this.ncbiOtherSpeciesGene = ncbiOtherSpeciesGene;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NcbiOrthoExternalReference that = (NcbiOrthoExternalReference) o;

        if (ID != that.ID) return false;
        if (referenceDatabase != null ? !referenceDatabase.equals(that.referenceDatabase) : that.referenceDatabase != null)
            return false;
        if (ncbiOtherSpeciesGene != null ? !ncbiOtherSpeciesGene.equals(that.ncbiOtherSpeciesGene) : that.ncbiOtherSpeciesGene != null)
            return false;
        return !(accessionNumber != null ? !accessionNumber.equals(that.accessionNumber) : that.accessionNumber != null);
    }

    @Override
    public int hashCode() {
        int result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + (referenceDatabase != null ? referenceDatabase.hashCode() : 0);
        result = 31 * result + (ncbiOtherSpeciesGene != null ? ncbiOtherSpeciesGene.hashCode() : 0);
        result = 31 * result + (accessionNumber != null ? accessionNumber.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(NcbiOrthoExternalReference o) {
        ForeignDB.AvailableName nameOne = getReferenceDatabase().getForeignDB().getDbName();
        ForeignDB.AvailableName nameTwo = o.getReferenceDatabase().getForeignDB().getDbName();
        ExternalDatabase externalDatabaseOne = ExternalDatabase.getExternalDatabase(nameOne);
        ExternalDatabase externalDatabaseTwo = ExternalDatabase.getExternalDatabase(nameTwo);
        if (externalDatabaseOne == null)
            return -1;
        if (externalDatabaseTwo == null)
            return 1;
        return externalDatabaseOne.getIndex() - externalDatabaseTwo.getIndex();
    }
}
