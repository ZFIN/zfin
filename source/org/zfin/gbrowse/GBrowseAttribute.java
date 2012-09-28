package org.zfin.gbrowse;

import java.io.Serializable;

/**
 * This class is a direct mapping of the Bio::Seqfeature::Store attribute table
 * <p/>
 * This comes from the 9th column of the gff3 file, and can be arbitrarily
 * defined with field="value"; statements.
 * <p/>
 * GBrowseAttributeType is a controlled vocabulary of the field values.
 * <p/>
 * GBrowse uses Alias as a field for alternate names to search, so we populate that.
 * zfin ids are stored as gene_id or zdb_id.
 */

public class GBrowseAttribute implements Serializable {
    private GBrowseFeature feature;
    private Integer featureId;
    private Integer attributeTypeId; //the type of attribute, right now we don't care
    private GBrowseAttributeType attributeType;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GBrowseFeature getFeature() {
        return feature;
    }

    public void setFeature(GBrowseFeature feature) {
        this.feature = feature;
    }

    public Integer getAttributeTypeId() {
        return attributeTypeId;
    }

    public void setAttributeTypeId(Integer attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public Integer getFeatureId() {
        return featureId;
    }

    public void setFeatureId(Integer featureId) {
        this.featureId = featureId;
    }

    public GBrowseAttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(GBrowseAttributeType attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GBrowseAttribute that = (GBrowseAttribute) o;

        if (attributeType != null ? !attributeType.equals(that.attributeType) : that.attributeType != null)
            return false;
        if (attributeTypeId != null ? !attributeTypeId.equals(that.attributeTypeId) : that.attributeTypeId != null)
            return false;
        if (feature != null ? !feature.equals(that.feature) : that.feature != null) return false;
        if (featureId != null ? !featureId.equals(that.featureId) : that.featureId != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = feature != null ? feature.hashCode() : 0;
        result = 31 * result + (featureId != null ? featureId.hashCode() : 0);
        result = 31 * result + (attributeTypeId != null ? attributeTypeId.hashCode() : 0);
        result = 31 * result + (attributeType != null ? attributeType.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
