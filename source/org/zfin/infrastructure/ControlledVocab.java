package org.zfin.infrastructure;

import javax.persistence.*;

@Entity
@Table(name = "controlled_vocabulary")
public class ControlledVocab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cv_zdb_id")
    private String zdbID;
    @Column(name = "cv_term_name")
    private String cvTermName;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getCvTermName() {
        return cvTermName;
    }

    public void setCvTermName(String cvTermName) {
        this.cvTermName = cvTermName;
    }
}
