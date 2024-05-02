package org.zfin.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.profile.Person;

import jakarta.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "data_note")
@Setter
@Getter
public class DataNote implements Comparable<DataNote> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DataNote")
    @GenericGenerator(name = "DataNote",
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

    public String getDateString() {
	        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public int compareTo(DataNote anotherNote) {

        return 0 - getDate().compareTo(anotherNote.getDate());
    }
}

