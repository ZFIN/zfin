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
}
