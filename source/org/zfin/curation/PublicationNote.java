package org.zfin.curation;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "publication_note")
public class PublicationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "PNOTE"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "pnote_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "pnote_pub_zdb_id")
    private Publication publication;
    @ManyToOne
    @JoinColumn(name = "pnote_curator_zdb_id")
    private Person curator;
    @Column(name = "pnote_date")
    private Date date;
    @Column(name = "pnote_text")
    private String text;

    public Person getCurator() {
        return curator;
    }

    public void setCurator(Person curator) {
        this.curator = curator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
