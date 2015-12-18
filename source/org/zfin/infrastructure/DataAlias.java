package org.zfin.infrastructure;

import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for alias records. You need to extend this class
 * to make use of the real function, e.g. MarkerAlias.
 */
public class DataAlias implements Comparable, EntityAttribution, Serializable, EntityZdbID {

    protected String zdbID;
    protected String alias;
    protected DataAliasGroup.Group group;
    protected String dataZdbID;

    protected String aliasLowerCase;

    // this property is only useful when not only publications are desired
    public DataAliasGroup aliasGroup;

    public DataAliasGroup getAliasGroup() {
        return aliasGroup;
    }

    public void setAliasGroup(DataAliasGroup aliasGroup) {
        this.aliasGroup = aliasGroup;
    }

    protected Set<ActiveSource> sources;

    transient protected Set<PublicationAttribution> publications;

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

    public DataAliasGroup.Group getGroup() {
        return group;
    }

    public void setGroup(DataAliasGroup.Group group) {
        this.group = group;
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
}
