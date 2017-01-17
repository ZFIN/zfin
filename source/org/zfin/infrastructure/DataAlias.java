package org.zfin.infrastructure;

import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for alias records. You need to extend this class
 * to make use of the real function, e.g. MarkerAlias.
 */
@Entity
@Table(name = "data_alias")
@DiscriminatorFormula("CASE get_obj_type(dalias_data_zdb_id)" +
        "                                    WHEN 'ANAT' THEN 'Anatom'" +
        "                                    WHEN 'GENE' THEN 'Marker'" +
        "                                    WHEN 'ATB' THEN  'Marker'" +
        "                                    WHEN 'TERM' THEN 'Term  '" +
        "                                    WHEN 'GENO' THEN 'Genoty'" +
        "                                    WHEN 'ALT'  THEN 'Featur'" +
        "                                    ELSE             'Marker'" +
        "                                 END")
public class DataAlias implements Comparable, EntityAttribution, Serializable, EntityZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "DALIAS"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "dalias_zdb_id")
    protected String zdbID;
    @Column(name = "dalias_alias")
    protected String alias;
    @Column(name = "dalias_data_zdb_id", insertable = false, updatable = false)
    protected String dataZdbID;

    @Column(name = "dalias_alias_lower")
    protected String aliasLowerCase;
    @ManyToMany
    @JoinTable(name = "record_attribution", joinColumns = {
            @JoinColumn(name = "recattrib_data_zdb_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "recattrib_source_zdb_id",
                    nullable = false, updatable = false)})
    protected Set<ActiveSource> sources;


    // this property is only useful when not only publications are desired
    @ManyToOne
    @JoinColumn(name = "dalias_group_id")
    public DataAliasGroup aliasGroup;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "recattrib_data_zdb_id")
    protected Set<PublicationAttribution> publications;

    public DataAliasGroup getAliasGroup() {
        return aliasGroup;
    }

    public void setAliasGroup(DataAliasGroup aliasGroup) {
        this.aliasGroup = aliasGroup;
    }

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

    public Set<ActiveSource> getSources() {
        return sources;
    }

    public void setSources(Set<ActiveSource> sources) {
        this.sources = sources;
    }

    public int getSourceCount() {
        if (sources == null) {
            return 0;
        } else {
            return sources.size();
        }
    }

    public Set<PublicationAttribution> getPublications() {
        if (publications == null) {
            return new HashSet<>();
        }
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public void addPublication(PublicationAttribution publicationAttribution) {
        if (this.publications == null) {
            this.publications = new HashSet<>();
        }
        this.publications.add(publicationAttribution);
    }

    public int getPublicationCount() {
        if (publications == null) {
            return 0;
        } else {
            return publications.size();
        }
    }

    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            return getPublications().iterator().next().getPublication();
        }
        return null;
    }

    public int compareTo(Object otherAlias) {
        return aliasLowerCase.compareTo(((DataAlias) otherAlias).getAliasLowerCase());
    }


    public boolean hasPublication(PublicationAttribution publicationAttribution) {
        for (PublicationAttribution aPublicationAttribution : getPublications()) {
            if (publicationAttribution.getPublication().equals(aPublicationAttribution.getPublication())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAbbreviation() {
        return alias;
    }

    @Override
    public String getAbbreviationOrder() {
        return alias;
    }

    @Override
    public String getEntityType() {
        return "Data Alias";
    }

    @Override
    public String getEntityName() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataAlias dataAlias = (DataAlias) o;

        if (!alias.equals(dataAlias.alias)) return false;
        if (!dataZdbID.equals(dataAlias.dataZdbID)) return false;
        return aliasGroup.equals(dataAlias.aliasGroup);

    }

    @Override
    public int hashCode() {
        int result = alias.hashCode();
        result = 31 * result + dataZdbID.hashCode();
        result = 31 * result + aliasGroup.hashCode();
        return result;
    }
}
