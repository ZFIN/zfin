package org.zfin.feature;

import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityNotes;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mapping.MappedDeletion;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.people.FeatureSource;
import org.zfin.people.FeatureSupplier;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**ture
 */
public class Feature implements EntityNotes{

    private String zdbID;
    private String name;
    private String publicComments;
//    private String ftrType;
    private String lineNumber;
    private FeaturePrefix featurePrefix;
    private String abbreviation;
    private String transgenicSuffix;
    private Set<DataNote> dataNotes;
    private Set<PublicationAttribution> publications;
    private Set<GenotypeFeature> genotypeFeatures;
    private String abbreviationOrder;
    private String nameOrder;
    private Boolean isKnownInsertionSite;
    private Boolean isDominantFeature;
    private Boolean isUnspecifiedFeature;
    private Set<MappedDeletion> mappedDeletions;
    private Set<FeatureMarkerRelationship> featureMarkerRelations;
//    public FeatureType featureType;
    private FeatureTypeEnum type;
    private Set<FeatureSupplier> suppliers;
    private Set<FeatureSource> sources;
    private Set<FeatureAlias> aliases;
    private FeatureAssay featureAssay;


    public String getTransgenicSuffix() {
        return transgenicSuffix;
    }

    public void setTransgenicSuffix(String transgenicSuffix) {
        this.transgenicSuffix = transgenicSuffix;
    }

    public Set<PublicationAttribution> getPublications() {
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public Set<DataNote> getDataNotes() {
        return dataNotes;
    }

    public void setDataNotes(Set<DataNote> dataNotes) {
        this.dataNotes = dataNotes;
    }

    public Boolean getKnownInsertionSite() {
        return isKnownInsertionSite;
    }

    public void setKnownInsertionSite(Boolean knownInsertionSite) {
        isKnownInsertionSite = knownInsertionSite;
    }

    public Boolean getDominantFeature() {
        return isDominantFeature;
    }

    public void setDominantFeature(Boolean dominantFeature) {
        isDominantFeature = dominantFeature;
    }

    public Boolean getUnspecifiedFeature() {
        return isUnspecifiedFeature;
    }

    public void setUnspecifiedFeature(Boolean unspecifiedFeature) {
        isUnspecifiedFeature = unspecifiedFeature;
    }


    public FeaturePrefix getFeaturePrefix() {
        return featurePrefix;
    }

    public void setFeaturePrefix(FeaturePrefix featurePrefix) {
        this.featurePrefix = featurePrefix;
    }

//    public String getFtrTypeString() {
//        return ftrType;
//    }
//
//    public void setFtrTypeString(String ftrType) {
//        this.ftrType = ftrType;
//    }


    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public Set<MappedDeletion> getMappedDeletions() {
        return mappedDeletions;
    }

    public void setMappedDeletions(Set<MappedDeletion> mappedDeletions) {
        this.mappedDeletions = mappedDeletions;
    }

    public FeatureTypeEnum getType() {
        return type;
    }

    public void setType(FeatureTypeEnum type) {
        this.type = type;
    }

    public Set<FeatureSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<FeatureSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public Set<FeatureSource> getSources() {
        return sources;
    }

    public void setSources(Set<FeatureSource> sources) {
        this.sources = sources;
    }


    public FeatureAssay getFeatureAssay() {
        return featureAssay;
    }

    public void setFeatureAssay(FeatureAssay featureAssay) {
        this.featureAssay = featureAssay;
    }

    public Set<FeatureAlias> getAliases() {
        return aliases;
    }

    public void setAliases(Set<FeatureAlias> aliases) {
        this.aliases = aliases;
    }

    public String getZdbID() {

        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getPublicComments() {
        return publicComments;
    }

    public void setPublicComments(String publicComments) {
        this.publicComments = publicComments;
    }

    public String getAbbreviationOrder() {
        return abbreviationOrder;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }


    public Set<FeatureMarkerRelationship> getFeatureMarkerRelations() {
        return featureMarkerRelations;
    }

    public void setFeatureMarkerRelations(Set<FeatureMarkerRelationship> featureMarkerRelations) {
        this.featureMarkerRelations = featureMarkerRelations;
    }


    public Set<GenotypeFeature> getGenotypeFeatures() {
        return genotypeFeatures;
    }

    public void setGenotypeFeatures(Set<GenotypeFeature> genotypeFeatures) {
        this.genotypeFeatures = genotypeFeatures;
    }

    public int compareTo(Object otherFeature) {
        return getAbbreviationOrder().compareTo(((Feature) otherFeature).getAbbreviationOrder());
    }

    public int getNumberOfRelatedGenotypes() {
        if (genotypeFeatures == null || genotypeFeatures.size() == 0) {
            return 0;
        }

        Set<Genotype> relatedGenotypes = new HashSet<Genotype>();
        for (GenotypeFeature genoFtr : genotypeFeatures) {
            if (genoFtr != null) {
                relatedGenotypes.add(genoFtr.getGenotype());
            }
        }
        return relatedGenotypes.size();
    }

    public Marker getSingleRelatedMarker() {
        if (featureMarkerRelations != null && featureMarkerRelations.size() == 1  ) {
            return featureMarkerRelations.iterator().next().getMarker();
        }
        return null;
    }


    public Genotype getSingleRelatedGenotype() {
        if (genotypeFeatures != null && genotypeFeatures.size() == 1) {
            return genotypeFeatures.iterator().next().getGenotype();
        }
        return null;
    }

    public Set<FeatureMarkerRelationship> getConstructs() {
        if(type!=FeatureTypeEnum.TRANSGENIC_INSERTION){
            return null ;
        }
        if (featureMarkerRelations == null) {
            return new TreeSet<FeatureMarkerRelationship>();
        }
        SortedSet<FeatureMarkerRelationship> constructs = new TreeSet<FeatureMarkerRelationship>();
        for (FeatureMarkerRelationship ftrmrkrRelation : featureMarkerRelations) {
            if (ftrmrkrRelation != null) {
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString())
                        || ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())) {
                    constructs.add(ftrmrkrRelation);
                }
            }
        }
        return constructs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Feature");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", lineNumber='").append(lineNumber).append('\'');
        sb.append(", labPrefix=").append(featurePrefix);
        sb.append(", abbreviation='").append(abbreviation).append('\'');
        sb.append(", transgenicSuffix='").append(transgenicSuffix).append('\'');
        sb.append(", isKnownInsertionSite=").append(isKnownInsertionSite);
        sb.append(", isDominantFeature=").append(isDominantFeature);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
