package org.zfin.ontology;

import java.util.Set;

/**
 * This class holds the header info provided in the OBO file.
 */
public class OntologyMetadata {

    private long id;
    private String name;
    private int order;
    private String defaultNamespace;
    private String oboVersion;
    private String dataVersion;
    private String date;
    private String savedBy;
    // software used to generate the obo file.
    private String generatedBy;
    private String remark;
    private Set<Subset> subsets;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    public String getOboVersion() {
        return oboVersion;
    }

    public void setOboVersion(String oboVersion) {
        this.oboVersion = oboVersion;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(String savedBy) {
        this.savedBy = savedBy;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Set<Subset> getSubsets() {
        return subsets;
    }

    public void setSubsets(Set<Subset> subsets) {
        this.subsets = subsets;
    }

    @Override
    public String toString() {
        return "OntologyMetadata{" +
                "name='" + name + '\'' +
                ", order=" + order +
                ", defaultNamespace='" + defaultNamespace + '\'' +
                ", oboVersion='" + oboVersion + '\'' +
                ", dataVersion='" + dataVersion + '\'' +
                ", date='" + date + '\'' +
                ", savedBy='" + savedBy + '\'' +
                ", generatedBy='" + generatedBy + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OntologyMetadata that = (OntologyMetadata) o;

        if (dataVersion != null ? !dataVersion.equals(that.dataVersion) : that.dataVersion != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (defaultNamespace != null ? !defaultNamespace.equals(that.defaultNamespace) : that.defaultNamespace != null)
            return false;
        if (generatedBy != null ? !generatedBy.equals(that.generatedBy) : that.generatedBy != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (oboVersion != null ? !oboVersion.equals(that.oboVersion) : that.oboVersion != null) return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null) return false;
        if (savedBy != null ? !savedBy.equals(that.savedBy) : that.savedBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (defaultNamespace != null ? defaultNamespace.hashCode() : 0);
        result = 31 * result + (oboVersion != null ? oboVersion.hashCode() : 0);
        result = 31 * result + (dataVersion != null ? dataVersion.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (savedBy != null ? savedBy.hashCode() : 0);
        result = 31 * result + (generatedBy != null ? generatedBy.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        return result;
    }
}
