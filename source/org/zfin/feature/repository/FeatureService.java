package org.zfin.feature.repository;

import org.zfin.mutant.Feature;
import org.zfin.mutant.FeatureMarkerRelationship;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.sequence.LinkageGroup;
import org.zfin.repository.RepositoryFactory;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.mapping.MappedDeletion;
import org.apache.commons.collections.CollectionUtils;

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

   public  List<DataNote> getFeatureNote(){
           Set<DataNote> datanotes=feature.getDataNote();
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

    public  SortedSet<FeatureMarkerRelationship> getSortedMarkerRelationships() {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<FeatureMarkerRelationship>();
        }
        SortedSet<FeatureMarkerRelationship> affectedGenes = new TreeSet<FeatureMarkerRelationship>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null)
                if(ftrmrkrRelation.getType().equals(FeatureMarkerRelationship.IS_ALLELE_OF)){
                            affectedGenes.add(ftrmrkrRelation);
                }
        }
        

                return affectedGenes;
    }



    
    public List<Marker> getMappedDeletions(){
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        List<Marker> mkr=mutantRepository.getDeletedMarker(feature);
        List<Marker> delmark = new ArrayList<Marker>();

        for (Marker mark : mkr){
               delmark.add(mark);
    }

        return delmark;

    }


    public List<String> getFtrLocations(){
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        List<String> lg=mutantRepository.getDeletedMarkerLG(feature);
        List<String> delmarklg = new ArrayList<String>();

        for (String lgchr : lg)

                       delmarklg.add(lgchr);


        return delmarklg;

    }
    public List<String> getFtrMap(){
           MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
           List<String> lg=mutantRepository.getMappedFeatureLG(feature);
           List<String> delmarklg = new ArrayList<String>();

           for (String lgchr : lg)

                          delmarklg.add(lgchr);


           return delmarklg;

       }



public List<RecordAttribution> getFtrTypeAttr(){
        InfrastructureRepository infRep=RepositoryFactory.getInfrastructureRepository();
        List<RecordAttribution> recordAttributions = infRep.getRecAttribforFtrType(feature.getZdbID()) ;
        List<RecordAttribution> attributions =new ArrayList<RecordAttribution>();
         for(RecordAttribution recordAttribution: recordAttributions){
            attributions.add(recordAttribution);
        }

        return attributions;

    }

    public int getPublicationCount() {
      InfrastructureRepository infRep=RepositoryFactory.getInfrastructureRepository();
        List<RecordAttribution> recordAttributions = infRep.getRecAttribforFtrType(feature.getZdbID()) ;
        List<RecordAttribution> attributions =new ArrayList<RecordAttribution>();
         for(RecordAttribution recordAttribution: recordAttributions){
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
              if (ftrmrkrRelation.getType().equals(FeatureMarkerRelationship.CONTAINS_SEQUENCE_FEATURE)){
                   constructMarkers.add(ftrmrkrRelation);
              }
             if (ftrmrkrRelation.getType().equals(FeatureMarkerRelationship.CONTAINS_INNOCSEQUENCE_FEATURE)){
                   constructMarkers.add(ftrmrkrRelation);
              }
        }
        return constructMarkers;

}

     }