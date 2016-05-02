package org.zfin.feature.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.zfin.feature.*;
import org.zfin.gbrowse.GBrowseTrack;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.FeatureDBLink;

import java.util.*;


public class FeatureService {

    public static Set<FeatureMarkerRelationship> getSortedMarkerRelationships(Feature feature) {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<>();
        }
        SortedSet<FeatureMarkerRelationship> affectedGenes = new TreeSet<>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null) {
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                    if (ftrmrkrRelation.getMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                        affectedGenes.add(ftrmrkrRelation);
                    }
                }
            }
        }

        return affectedGenes;
    }

    public static Set<FeatureDBLink> getSummaryDbLinks(Feature feature) {

        Set<FeatureDBLink> summaryLinks = new HashSet<>();
        for (FeatureDBLink featureDBLink : feature.getDbLinks()) {
            if (featureDBLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE)) {
                summaryLinks.add(featureDBLink);
            }
        }
        return summaryLinks;
    }

    public static Set<FeatureDBLink> getGenbankDbLinks(Feature feature) {
        Set<FeatureDBLink> genbankLinks = new HashSet<>();
        for (FeatureDBLink featureDBLink : feature.getDbLinks()) {
            if (!featureDBLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE)) {
                genbankLinks.add(featureDBLink);
            }
        }
        return genbankLinks;
    }

    public static FeatureMarkerRelationship getCreatedByRelationship(Feature feature) {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return null;
        }

        for (FeatureMarkerRelationship ftrmrkrRelationship : fmrelationships) {
            if (ftrmrkrRelationship != null) {
                if (ftrmrkrRelationship.getMarker().getMarkerType().getType() == Marker.Type.CRISPR
                        || ftrmrkrRelationship.getMarker().getMarkerType().getType() == Marker.Type.TALEN) {
                    return ftrmrkrRelationship;
                }
            }
        }

        return null;
    }

    public static Set<String> getFeatureLocations(Feature feature) {
        TreeSet<String> lg = RepositoryFactory.getFeatureRepository().getFeatureLG(feature);
        TreeSet<String> delmarklg = new TreeSet<>();

        for (String lgchr : lg) {
            delmarklg.add(lgchr);
        }

        return delmarklg;
    }

    public static List<FeatureGenomeLocation> getFeatureGenomeLocations(Feature feature) {
        List<FeatureGenomeLocation> locations = RepositoryFactory.getLinkageRepository().getGenomeLocation(feature);
        Collections.sort(locations);
        return locations;
    }

    public static List<FeatureGenomeLocation> getFeatureGenomeLocations(Feature feature, GenomeLocation.Source... sources) {
        List<FeatureGenomeLocation> locations = getFeatureGenomeLocations(feature);
        final Collection<GenomeLocation.Source> sourceList = Arrays.asList(sources);
        CollectionUtils.filter(locations, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return (o instanceof FeatureGenomeLocation) &&
                        sourceList.contains(((FeatureGenomeLocation) o).getSource());
            }
        });
        return locations;
    }

    public static Set<String> getFeatureMap(Feature feature) {
        MarkerRepository mkrRepository = RepositoryFactory.getMarkerRepository();
        List<Marker> mkr = RepositoryFactory.getFeatureRepository().getMarkersByFeature(feature);
        Set<String> delmarklg = new TreeSet<>();
        if (mkr != null) {
            for (Marker mark : mkr) {
                Set<String> lg = mkrRepository.getLG(mark);
                for (String lgchr : lg) {
                    delmarklg.add(lgchr);
                }
            }
        }
        return delmarklg;
    }

    public static Collection<PublicationAttribution> getMutationDetailAttributions(Feature feature) {
        InfrastructureRepository infRep = RepositoryFactory.getInfrastructureRepository();
        SortedSet<PublicationAttribution> attributions = new TreeSet<>();
        attributions.addAll(infRep.getPublicationAttributions(feature.getZdbID(), RecordAttribution.SourceType.FEATURE_TYPE));
        FeatureDnaMutationDetail dnaDetails = feature.getFeatureDnaMutationDetail();
        if (dnaDetails != null) {
            attributions.addAll(infRep.getPublicationAttributions(dnaDetails.getZdbID(), RecordAttribution.SourceType.FEATURE_TYPE));
        }
        Set<FeatureTranscriptMutationDetail> transcriptDetails = feature.getFeatureTranscriptMutationDetailSet();
        if (CollectionUtils.isNotEmpty(transcriptDetails)) {
            for (FeatureTranscriptMutationDetail detail : transcriptDetails) {
                attributions.addAll(infRep.getPublicationAttributions(detail.getZdbID(), RecordAttribution.SourceType.FEATURE_TYPE));
            }
        }
        FeatureProteinMutationDetail proteinDetail = feature.getFeatureProteinMutationDetail();
        if (proteinDetail != null) {
            attributions.addAll(infRep.getPublicationAttributions(proteinDetail.getZdbID(), RecordAttribution.SourceType.FEATURE_TYPE));
        }
        return attributions;
    }

    public static Set<FeatureMarkerRelationship> getSortedConstructRelationships(Feature feature) {
        Set<FeatureMarkerRelationship> fmrelationships = feature.getFeatureMarkerRelations();
        if (fmrelationships == null) {
            return new TreeSet<>();
        }
        SortedSet<FeatureMarkerRelationship> constructMarkers = new TreeSet<>();
        for (FeatureMarkerRelationship ftrmrkrRelation : fmrelationships) {
            if (ftrmrkrRelation != null) {
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString())
                        || ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())
                        ) {
                    constructMarkers.add(ftrmrkrRelation);
                }
            }
        }
        return constructMarkers;

    }

    public static List<String> getFeatureAliases(Feature feature) {
        Set<FeatureAlias> featureAliases = feature.getAliases();
        List<String> featureAliasList = new ArrayList<>();
        for (FeatureAlias featureAlias : featureAliases) {
            featureAliasList.add(featureAlias.getAlias());
        }
        return featureAliasList;
    }

    public static List<String> getFeatureSequences(Feature feature) {
        Set<FeatureDBLink> featureSequences = feature.getDbLinks();
        List<String> featureDBLinkList = new ArrayList<>();
        for (FeatureDBLink featureDBLink : featureSequences) {
            if (!featureDBLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE)) {
                featureDBLinkList.add(featureDBLink.getAccessionNumberDisplay());
            }
        }
        return featureDBLinkList;
    }

    public static List<Marker> getPresentMarkerList(Feature feature, FeatureMarkerRelationshipTypeEnum featureMarkerRelationship) {
        List<Marker> featureMarkerList = new ArrayList<>(feature.getFeatureMarkerRelations().size());
        for (FeatureMarkerRelationship rel : feature.getFeatureMarkerRelations()) {
            if (rel.getType().equals(featureMarkerRelationship)) {
                featureMarkerList.add(rel.getMarker());
            }
        }
        Collections.sort(featureMarkerList);
        return featureMarkerList;
    }

    public static List<FeatureNote> getSortedExternalNotes(Feature feature) {
        List<FeatureNote> notes = new ArrayList<FeatureNote>();
        notes.addAll(feature.getExternalNotes());
        Collections.sort(notes);

        return notes;
    }

    public static GBrowseImage getGbrowseImage(Feature feature) {
        Set<FeatureMarkerRelationship> featureMarkerRelationships = feature.getFeatureMarkerRelations();
        List<FeatureGenomeLocation> locations = getFeatureGenomeLocations(feature, GenomeLocation.Source.ZFIN_Zv9);
        if (CollectionUtils.isEmpty(locations)) {
            return null;
        }

        // gbrowse has a location for this feature. if there is a feature marker relationship AND we know where
        // that marker is, show the feature in the context of the marker. Otherwise just show the feature with
        // some appropriate amount of padding. We don't yet have GRCz10 coordinates for any features, so for
        // now, they're all Zv9 still
        GBrowseImage.GBrowseImageBuilder imageBuilder = GBrowseImage.builder()
                .genomeBuild(GBrowseImage.GenomeBuild.ZV9)
                .highlight(feature);

        FeatureGenomeLocation featureLocation = locations.get(0);
        if (featureMarkerRelationships.size() == 1) {
            Marker related = featureMarkerRelationships.iterator().next().getMarker();
            List<MarkerGenomeLocation> markerLocations = RepositoryFactory.getLinkageRepository().getGenomeLocation(related, GenomeLocation.Source.ZFIN_Zv9);
            if (CollectionUtils.isNotEmpty(markerLocations)) {
                imageBuilder.landmark(markerLocations.get(0)).withPadding(0.1);
            } else {
                imageBuilder.landmark(featureLocation).withPadding(10000);
            }
        } else {
            imageBuilder.landmark(featureLocation).withPadding(10000);
        }
        String subSource = featureLocation.getDetailedSource();
        if (subSource != null) {
            if (subSource.equals("BurgessLin")) {
                imageBuilder.tracks(GBrowseTrack.GENES, GBrowseTrack.INSERTION, GBrowseTrack.TRANSCRIPTS);
            } else if (subSource.equals("ZMP")) {
                imageBuilder.tracks(GBrowseTrack.GENES, GBrowseTrack.ZMP, GBrowseTrack.TRANSCRIPTS);
            }
        }
        return imageBuilder.build();
    }
}