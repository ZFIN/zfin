package org.zfin.feature.repository;

import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Feature;
import org.zfin.mutant.FeatureMarkerRelationship;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;


public class FeatureService {
    private Feature feature;

    public FeatureService(Feature feature) {
        if (feature == null)
            throw new RuntimeException("No feature object provided");
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public List<DataNote> getFeatureNote() {
        Set<DataNote> datanotes = feature.getDataNote();
        if (datanotes == null) {
            return new ArrayList<DataNote>();
        }
        List<DataNote> privateNote = new ArrayList<DataNote>();
        for (DataNote prnotes : datanotes) {
            if (prnotes != null)
                privateNote.add(prnotes);
        }
        Collections.sort(privateNote);
        return privateNote;
    }

    public Set<FeatureMarkerRelationship> getSortedMarkerRelationships() {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<FeatureMarkerRelationship>();
        }
        SortedSet<FeatureMarkerRelationship> affectedGenes = new TreeSet<FeatureMarkerRelationship>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null)
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                    if (ftrmrkrRelation.getMarker().getMarkerType().getType().toString() == Marker.Type.GENE.toString())
                        affectedGenes.add(ftrmrkrRelation);
                }
        }
    
    return affectedGenes;
}


    public TreeSet<String> getFtrLocations() {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        TreeSet<String> lg = mutantRepository.getFeatureLG(feature);
        TreeSet<String> delmarklg = new TreeSet<String>();

        for (String lgchr : lg)

            delmarklg.add(lgchr);


        return delmarklg;

    }

    public Set<String> getFtrMap() {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        LinkageRepository linkageRepository = RepositoryFactory.getLinkageRepository();
        MarkerRepository mkrRepository = RepositoryFactory.getMarkerRepository();
        List<Marker> mkr = mutantRepository.getMarkerbyFeature(feature);
        Set<String> delmarklg = new TreeSet<String>();
        if (mkr != null)
            for (Marker mark : mkr) {
                Set<String> lg = mkrRepository.getLG(mark);
                for (String lgchr : lg) {
                    delmarklg.add(lgchr);
                }
            }
        return delmarklg;
    }

    public List<RecordAttribution> getFtrTypeAttr() {
        InfrastructureRepository infRep = RepositoryFactory.getInfrastructureRepository();
        List<RecordAttribution> recordAttributions = infRep.getRecAttribforFtrType(feature.getZdbID());
        List<RecordAttribution> attributions = new ArrayList<RecordAttribution>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution);
        }

        return attributions;

    }

    public int getPublicationCount() {
        InfrastructureRepository infRep = RepositoryFactory.getInfrastructureRepository();
        List<RecordAttribution> recordAttributions = infRep.getRecAttribforFtrType(feature.getZdbID());
        List<RecordAttribution> attributions = new ArrayList<RecordAttribution>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution);
        }
        return attributions.size();
    }

    public String getSinglePublication() {
        if (getPublicationCount() == 1) {
            for (RecordAttribution pubAttr : getFtrTypeAttr())
                return pubAttr.getSourceZdbID();
        }
        return null;
    }


    public Set<FeatureMarkerRelationship> getSortedConstructRelationships() {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<FeatureMarkerRelationship>();
        }
        SortedSet<FeatureMarkerRelationship> constructMarkers = new TreeSet<FeatureMarkerRelationship>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null)
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString())) {
                    constructMarkers.add(ftrmrkrRelation);
                }
            if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())) {
                //if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())) {
                constructMarkers.add(ftrmrkrRelation);
            }
        }
        return constructMarkers;

    }

    public Set<FeatureMarkerRelationship> getSortedLGRelationships() {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<FeatureMarkerRelationship>();
        }
        SortedSet<FeatureMarkerRelationship> lgMarkers = new TreeSet<FeatureMarkerRelationship>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null)
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.IS_ALLELE_OF)) {
                    lgMarkers.add(ftrmrkrRelation);
                }
            if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.MARKERS_PRESENT)) {
                lgMarkers.add(ftrmrkrRelation);
            }
            if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationship.Type.MARKERS_MISSING)) {
                lgMarkers.add(ftrmrkrRelation);
            }
        }
        return lgMarkers;

    }
}