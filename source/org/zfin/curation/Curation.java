package org.zfin.curation;

import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Date;

public class Curation {

    private String zdbID;
    private Publication publication;
    private Person curator;
    private Topic topic;
    private boolean dataFound;
    private Date entryDate;
    private Date closedDate;
    private Date openedDate;

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Date closedDate) {
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

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
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

    public Date getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(Date openedDate) {
        this.openedDate = openedDate;
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

        public static Topic fromString(String name) {
            for (Topic topic : values()) {
                if (topic.toString().equals(name)) {
                    return topic;
                }
            }
            return null;
        }
    }

}
