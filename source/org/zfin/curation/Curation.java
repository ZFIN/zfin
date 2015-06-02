package org.zfin.curation;

import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Calendar;

public class Curation {

    private String zdbID;
    private Publication publication;
    private Person curator;
    private Topic topic;
    private boolean dataFound;
    private Calendar entryDate;
    private Calendar closedDate;

    public Calendar getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Calendar closedDate) {
        this.closedDate = closedDate;
    }

    public Person getCurator() {
        return curator;
    }

    public void setCurator(Person curator) {
        this.curator = curator;
    }

    public boolean isDataFound() {
        return dataFound;
    }

    public void setDataFound(boolean dataFound) {
        this.dataFound = dataFound;
    }

    public Calendar getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Calendar entryDate) {
        this.entryDate = entryDate;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public enum Topic {
        ANTIBODIES("Antibodies"),
        DISEASE("Disease"),
        EXPRESSION("Expression"),
        FEATURES("Features (Mutant)"),
        FISH_ORTHOLOGY("Fish Orthology"),
        GO("GO"),
        GENES("Genes"),
        GENOTYPE("Genotype"),
        LINKED_AUTHORS("Linked Authors"),
        MAPPING("Mapping"),
        NOMENCLATURE("Nomenclature"),
        ORTHOLOGY("Orthology"),
        PHENOTYPE("Phenotype"),
        SEQUENCE("Sequence"),
        TOXICOLOGY("Toxicology"),
        TRANSCRIPTS("Transcripts"),
        TRANSGENIC_CONSTRUCT("Transgenic Construct");

        private String display;

        Topic(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

}
