package org.zfin.antibody;

import org.zfin.ExternalNote;
import org.zfin.infrastructure.PublicationAttribution;

/**
 * Antibody specific external note.
 */
public class AntibodyExternalNote extends ExternalNote implements Comparable<AntibodyExternalNote> {

    private Antibody antibody;

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public int compareTo(AntibodyExternalNote note) {
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
     * AntibodyExternalNotes have only a single publication.
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
