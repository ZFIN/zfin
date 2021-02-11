package org.zfin.feature.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.feature.FeatureNote;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.mapping.VariantSequence;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.PreviousNameLight;
import org.zfin.mutant.GenotypeDisplay;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.publication.Publication;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.zebrashare.FeatureCommunityContribution;
import org.zfin.ExternalNote;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class FeatureBean {
    private Feature feature;
    private Marker marker;
    private int numPubs;
    private List<GenoExpStatistics> genoexpStats;
    private MappedMarkerBean mappedMarkerBean;
    private Set<FeatureMarkerRelationship> sortedConstructRelationships;


    private Set<FeatureMarkerRelationship> createdByRelationship;
    private List<PublicationAttribution> featureTypeAttributions;
    private Set<String> featureMap;
    private Collection<FeatureGenomeLocation> featureLocations;
    private String zdbID;
    private Set<FeatureDBLink> summaryPageDbLinks;
    private Set<FeatureDBLink> genbankDbLinks;
    private GBrowseImage gBrowseImage;
    private List<GenotypeDisplay> genotypeDisplays;
    private MutationDetailsPresentation mutationDetails;
    private List<PublicationAttribution> dnaChangeAttributions;
    private List<PublicationAttribution> transcriptConsequenceAttributions;
    private List<PublicationAttribution> proteinConsequenceAttributions;
    private List<PublicationAttribution> varSeqAttributions;
    private List<FeatureNote> externalNotes;
    private List<ExternalNote> allNotes;
    private FeatureCommunityContribution ftrCommContr;
    private Publication ZShareOrigPub;
    private String aaLink;
    private VariantSequence varSequence;
    private FeatureDBLink zircGenoLink;
    private boolean isSingleAffectedGeneFeature;
    private String varType;

    public String getDeleteURL() {
        return "";
    }

    public String getEditURL() {
        return "";
    }

    public List<PreviousNameLight> getSynonyms() {
        return feature.getAliases().stream()
                .map(alias -> {
                    PreviousNameLight previousNameLight = new PreviousNameLight(feature.getName());
                    previousNameLight.setMarkerZdbID(feature.getZdbID());
                    previousNameLight.setAlias(alias.getAlias());
                    if (alias.getPublications() != null) {
                        previousNameLight.setPublicationCount(alias.getPublicationCount());
                        if (alias.getPublicationCount() == 1)
                            previousNameLight.setPublicationZdbID(alias.getSinglePublication().getZdbID());
                    }
                    return previousNameLight;

                }).collect(Collectors.toList());
    }
}

