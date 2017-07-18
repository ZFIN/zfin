package org.zfin.infrastructure;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.publication.Journal;

import javax.persistence.*;

@Entity
@Table(name = "source_alias")
public class SourceAlias {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "SALIAS"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveSource", value = "true")
            })
    @Column(name = "salias_zdb_id")
    protected String zdbID;


    @Column(name = "salias_alias")
    protected String alias;

    @Column(name = "salias_source_zdb_id")
    protected String dataZdbID;

    @Column(name = "salias_alias_lower")
    protected String aliasLowerCase;

    // if we use aliases for another source type, this will need a discriminator and subclasses
    // for now, there doesn't seem to be any reason to add that level complication
    /*@ManyToOne
    @JoinColumn(name = "salias_source_zdb_id")
    Journal journal;*/

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public String getAliasLowerCase() {
        return aliasLowerCase;
    }

    public void setAliasLowerCase(String aliasLowerCase) {
        this.aliasLowerCase = aliasLowerCase;
    }

    public String toString() {
        return alias;
    }
}
