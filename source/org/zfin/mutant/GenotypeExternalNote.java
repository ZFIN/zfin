package org.zfin.mutant;

import org.zfin.ExternalNote;
import org.zfin.infrastructure.PublicationAttribution;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Genotype specific external note.
 */
@Entity
@DiscriminatorValue("genotype")
public class GenotypeExternalNote extends ExternalNote implements Comparable<GenotypeExternalNote> {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private Genotype genotype;

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public int compareTo(GenotypeExternalNote note) {
        if (note.getSinglePubAttribution() == null)
            return -1;
        PublicationAttribution publicationAttribution = getSinglePubAttribution();
        if (publicationAttribution == null)
            return +1;

        int publicationComparison = publicationAttribution.compareTo(note.getSinglePubAttribution());
        if (publicationComparison != 0)
            return publicationComparison;

        // handle case the notes have the same publication
        // compare according to date it got created
        return getZdbID().compareTo(note.getZdbID());
    }

    /**
     * GenotypeExternalNotes have only a single publication.
     * Thus, we have to extract the single one from the collection.
     * ToDo: better db model and hibernate mapping that reflect this relationship.
     *
     * @return Publication Attribution
     */
    public PublicationAttribution getSinglePubAttribution() {
        if (pubAttributions == null || pubAttributions.size() == 0)
            return null;
        if (pubAttributions.size() > 1)
            throw new RuntimeException("More than one publication attribution found!");
        return pubAttributions.iterator().next();
    }
}