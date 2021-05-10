package org.zfin.infrastructure;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.profile.Person;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "data_note")
public class DataNote implements Comparable<DataNote> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "DNOTE"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "dnote_zdb_id")
    private String zdbID;
    @Column(name = "dnote_data_zdb_id")
    private String dataZdbID;
    @ManyToOne
    @JoinColumn(name = "dnote_curator_zdb_id")
    private Person curator;
    @Column(name = "dnote_text")
    private String note;
    @Column(name = "dnote_date")
    private Date date;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public Person getCurator() {
        return curator;
    }

    public void setCurator(Person curator) {
        this.curator = curator;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateString() {
	        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public int compareTo(DataNote anotherNote) {

        return 0 - getDate().compareTo(anotherNote.getDate());
    }
}

