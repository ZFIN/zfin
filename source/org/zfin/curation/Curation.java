package org.zfin.curation;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "curation")
public class Curation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "CUR"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "cur_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "cur_pub_zdb_id")
    private Publication publication;
    @ManyToOne
    @JoinColumn(name = "cur_curator_zdb_id")
    private Person curator;
    @Column(name = "cur_topic")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.curation.Curation$Topic")})
    private Topic topic;
    @Column(name = "cur_data_found")
    private boolean dataFound;
    @Column(name = "cur_entry_date")
    private Date entryDate;
    @Column(name = "cur_closed_date")
    private Date closedDate;
    @Column(name = "cur_opened_date")
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
