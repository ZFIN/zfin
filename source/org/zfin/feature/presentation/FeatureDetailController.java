package org.zfin.feature.presentation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureNote;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.feature.service.MutationDetailsConversionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.mutant.GenotypeDisplay;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListAdapter;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/feature")
public class FeatureDetailController {
    private static final Logger LOG = LogManager.getLogger(FeatureDetailController.class);

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private LinkageRepository linkageRepository;

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    @Autowired
    private MutationDetailsConversionService mutationDetailsConversionService;

    @RequestMapping(value = "view/{zdbID}")
    protected String getFeatureDetail(@PathVariable String zdbID, Model model) {
        CanonicalLinkConfig.addCanonicalIfFound(model);

        LOG.info("Start Feature Detail Controller");
        Feature feature = featureRepository.getFeatureByID(zdbID);
        if (feature == null) {
            String repldFtr = infrastructureRepository.getReplacedZdbID(zdbID);
            if (repldFtr != null) {
                feature = featureRepository.getFeatureByID(repldFtr);
            } else {
                // check if there exists a feature_tracking record for the given ID and if found and the feature is
                // one of the two Burgess / Linn feature types redirect to pub otherwise display: feature not found.
                String ftr = featureRepository.getFeatureByIDInTrackingTable(zdbID);
                if (ftr != null) {
                    if (zdbID.startsWith("ZDB-ALT-120130") || (zdbID.startsWith("ZDB-ALT-120806"))) {
                        return "redirect:/ZDB-PUB-121121-2";
                    }
                }
            }
        }
        if (feature == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        FeatureBean form = new FeatureBean();
        form.setZdbID(zdbID);
        form.setFeature(feature);
        form.setGBrowseImage(FeatureService.getGbrowseImage(feature));
        form.setSortedConstructRelationships(FeatureService.getSortedConstructRelationships(feature));
        form.setCreatedByRelationship(FeatureService.getCreatedByRelationship(feature));
        form.setFeatureTypeAttributions(FeatureService.getFeatureTypeAttributions(feature));
        form.setFeatureMap(FeatureService.getFeatureMap(feature));
        form.setSummaryPageDbLinks(FeatureService.getSummaryDbLinks(feature));
        List<FeatureNote> sortedExternalNotes = FeatureService.getSortedExternalNotes(feature);
        if (CollectionUtils.isNotEmpty(sortedExternalNotes)) {
            form.setExternalNotes(sortedExternalNotes.stream().filter(featureNote -> !featureNote.isVariantNote()).collect(Collectors.toList()));
            form.setVariantNotes(sortedExternalNotes.stream().filter(FeatureNote::isVariantNote).collect(Collectors.toList()));
        }
        form.setMutationDetails(mutationDetailsConversionService.convert(feature, true));
        form.setFeatureLocations(FeatureService.getPhysicalLocations(feature));
        form.setGenbankDbLinks(FeatureService.getGenbankDbLinks(feature));
        form.setDnaChangeAttributions(FeatureService.getDnaChangeAttributions(feature));
        form.setTranscriptConsequenceAttributions(FeatureService.getTranscriptConsequenceAttributions(feature));
        form.setProteinConsequenceAttributions(FeatureService.getProteinConsequenceAttributions(feature));
        form.setVarSequence(RepositoryFactory.getFeatureRepository().getFeatureVariant(feature));
        form.setVarSeqAttributions(FeatureService.getFlankSeqAttr(feature));
        if (feature.getAbbreviation().startsWith("hi")) {
            form.setAaLink(FeatureService.getAALink(feature));
        }
        form.setFtrCommContr(zebrashareRepository.getLatestCommunityContribution(feature));
        form.setZircGenoLink(FeatureService.getZIRCGenoLink(feature));
        form.setZShareOrigPub(zebrashareRepository.getZebraSharePublicationForFeature(feature));
        form.setSingleAffectedGeneFeature(featureRepository.isSingleAffectedGeneAlleles(feature));
        form.setVarSequence(RepositoryFactory.getFeatureRepository().getFeatureVariant(feature));
        form.setVarType(FeatureService.getVarType(feature));
        retrieveSortedGenotypeData(feature, form);
        retrievePubData(feature, form);
        model.addAttribute("isDeficiency",feature.getType().equals(FeatureTypeEnum.DEFICIENCY));
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.FEATURE.getTitleString() + feature.getName());

        return "feature/feature-view";
    }

    @RequestMapping("/flank-seq")
    public String getFlankingSequenceNote() {
        return "feature/flank-seq-note";
    }

    @RequestMapping("/note/citations")
    public String getCitationsNote() {
        return "feature/citations-note";
    }

    @RequestMapping("/note/genomebrowser")
    public String getGBrowseNote() {
        return "feature/gbrowse-note";
    }

    @RequestMapping(value = "/feature/view/{zdbID}")
    public String retrieveFeatureDetail(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        return getFeatureDetail(zdbID, model);
    }

    @RequestMapping(value = "{zdbID}/mutation-detail-citations")
    public String showMutationDetailCitationList(Model model, @PathVariable String zdbID, @RequestParam(required = false) String orderBy,
                                                 @RequestParam(required = true) String type) {
        Feature feature = featureRepository.getFeatureByID(zdbID);
        MutationDetailAttributionList.Type detailType = MutationDetailAttributionList.Type.fromString(type);
        MutationDetailAttributionList bean = new MutationDetailAttributionList(feature, detailType);
        List<PublicationAttribution> attributions = null;
        if (detailType != null) {
            switch (detailType) {
                case DNA:
                    attributions = FeatureService.getDnaChangeAttributions(feature);
                    break;
                case TRANSCRIPT:
                    attributions = FeatureService.getTranscriptConsequenceAttributions(feature);
                    break;
                case PROTEIN:
                    attributions = FeatureService.getProteinConsequenceAttributions(feature);
                    break;
            }
        }
        if (attributions != null) {
            Set<Publication> publications = new HashSet<>(attributions.size());
            for (PublicationAttribution attribution : attributions) {
                publications.add(attribution.getPublication());
            }
            bean.setPublications(publications);
        }
        bean.setOrderBy(orderBy);
        model.addAttribute(LookupStrings.FORM_BEAN, bean);
        return "feature/mutation-detail-citation-list";
    }

    private void retrieveSortedGenotypeData(Feature feature, FeatureBean form) {
        Set<GenotypeFeature> genotypeFeatures = feature.getGenotypeFeatures();
        List<GenotypeDisplay> genotypeDisplays = new ArrayList<>(genotypeFeatures.size());
        GenotypeDisplay genotypeDisplay;
        for (GenotypeFeature genotypeFeature : genotypeFeatures) {
            genotypeDisplay = new GenotypeDisplay();
            genotypeDisplay.setGenotype(genotypeFeature.getGenotype());
            genotypeDisplay.setDadZygosity(genotypeFeature.getDadZygosity());
            genotypeDisplay.setMomZygosity(genotypeFeature.getMomZygosity());
            genotypeDisplays.add(genotypeDisplay);
        }
        Collections.sort(genotypeDisplays);
        form.setGenotypeDisplays(genotypeDisplays);
    }

    private void retrievePubData(Feature fr, FeatureBean form) {
        form.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(fr.getZdbID()));
    }

    @RequestMapping("/type-citation-list/{zdbID}")
    public String getFeatureTypePublicationList(@PathVariable String zdbID, Model model) {
        Feature feature = featureRepository.getFeatureByID(zdbID);
        model.addAttribute("feature", feature);
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        List<String> publicationIDs = publicationRepository.getPublicationIdsForFeatureType(zdbID);
        List<Publication> publications = new ArrayList<>();
        for (String pubID : publicationIDs) {
            publications.add(publicationRepository.getPublication(pubID));
        }
        model.addAttribute("pubCount", publications.size());
        PublicationListBean citationBean = new PublicationListAdapter(publications);
        citationBean.setOrderBy("author");
        model.addAttribute("citationList", citationBean);

        return "feature/type-citation-list";
    }

}

