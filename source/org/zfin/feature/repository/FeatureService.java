package org.zfin.feature.repository;

import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.FeatureDBLink;

import java.util.*;


public class FeatureService {

    public static Set<FeatureMarkerRelationship> getSortedMarkerRelationships(Feature feature) {
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


    public static Set<String> getFeatureLocations(Feature feature) {
        TreeSet<String> lg = RepositoryFactory.getFeatureRepository().getFeatureLG(feature);
        TreeSet<String> delmarklg = new TreeSet<String>();

        for (String lgchr : lg){
            delmarklg.add(lgchr);
        }

        return delmarklg;
    }

    public static Set<String> getFeatureMap(Feature feature) {
        MarkerRepository mkrRepository = RepositoryFactory.getMarkerRepository();
        List<Marker> mkr = RepositoryFactory.getFeatureRepository().getMarkersByFeature(feature);
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

    public static List<RecordAttribution> getFeatureTypeAttributions(Feature feature) {
        InfrastructureRepository infRep = RepositoryFactory.getInfrastructureRepository();
        List<RecordAttribution> recordAttributions = infRep.getRecAttribforFtrType(feature.getZdbID());
        List<RecordAttribution> attributions = new ArrayList<RecordAttribution>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution);
        }

        return attributions;

    }

    public static int getPublicationCount(Feature feature) {
        InfrastructureRepository infRep = RepositoryFactory.getInfrastructureRepository();
        List<RecordAttribution> recordAttributions = infRep.getRecAttribforFtrType(feature.getZdbID());
        List<RecordAttribution> attributions = new ArrayList<RecordAttribution>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution);
        }
        return attributions.size();
    }

    public static String getSinglePublication(Feature feature) {
        if (getPublicationCount(feature) == 1) {
            return getFeatureTypeAttributions(feature).get(0).getSourceZdbID() ;
        }
        return null;
    }


    public static Set<FeatureMarkerRelationship> getSortedConstructRelationships(Feature feature) {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<FeatureMarkerRelationship>();
        }
        SortedSet<FeatureMarkerRelationship> constructMarkers = new TreeSet<FeatureMarkerRelationship>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null){
                if(ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString())
                || ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())
                 )  {
                    constructMarkers.add(ftrmrkrRelation);
                }
            }
        }
        return constructMarkers;

    }

    public static List<String> getFeatureAliases(Feature feature) {
        Set<FeatureAlias> featureAliases = feature.getAliases();
        List<String> featureAliasList = new ArrayList<String>();
        for (FeatureAlias featureAlias : featureAliases) {
            featureAliasList.add(featureAlias.getAlias());
        }
        return featureAliasList;
    }
    public static List<String> getFeatureSequences(Feature feature) {
        Set<FeatureDBLink> featureSequences = feature.getDbLinks();
        List<String> featureDBLinkList = new ArrayList<String>();
        for (FeatureDBLink featureDBLink : featureSequences) {
            if (!featureDBLink.getReferenceDatabase().getForeignDB().isZfishbook()){
            featureDBLinkList.add(featureDBLink.getAccessionNumberDisplay());
            }
        }
        return featureDBLinkList;
    }
}