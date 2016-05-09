package org.zfin.infrastructure;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "controlled_vocabulary")
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class ControlledVocab {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "CV"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "cv_zdb_id")
    private String zdbID;
    @Column(name = "cv_term_name")
    private String cvTermName;
    @Column(name = "cv_foreign_species")
    private String cvForeignSpecies;
    @Column(name = "cv_name_definition")
    private String cvNameDefinition;

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

    public String getCvForeignSpecies() {
        return cvForeignSpecies;
    }

    public void setCvForeignSpecies(String cvForeignSpecies) {
        this.cvForeignSpecies = cvForeignSpecies;
    }

    public String getCvNameDefinition() {
        return cvNameDefinition;
    }

    public void setCvNameDefinition(String cvNameDefinition) {
        this.cvNameDefinition = cvNameDefinition;
    }
}
