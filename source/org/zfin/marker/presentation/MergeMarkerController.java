package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionResult2;
import org.zfin.feature.Feature;
import org.zfin.feature.presentation.SimpleFeaturePresentation;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mapping.MappingService;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerMergeService;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationLink;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.presentation.AccessionPresentation;
import org.zfin.sequence.presentation.STRsequencePresentation;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;

/**
 * Note that this is only for merging markers and does not handle genotypes or features.
 */
@Controller
@RequestMapping("/marker")
public class MergeMarkerController {

    private Logger logger = LogManager.getLogger(MergeMarkerController.class);

    @RequestMapping(value = "/merge", method = RequestMethod.GET)
    protected String getView(
            Model model
            , @RequestParam("zdbIDToDelete") String zdbIDToDelete
            , @ModelAttribute("formBean") MergeBean formBean
            , BindingResult result
    ) throws Exception {
        String type = zdbIDToDelete.substring(4, 8);

        Marker markerToDelete;

        markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean.getZdbIDToDelete());

        formBean.setMarkerToDelete(markerToDelete);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());

        if (type.startsWith("ATB")) {
            return "marker/merge-antibody";
        } else {
            return "marker/merge-marker";
        }
    }

    /**
     * Perform the merge and redirect to the surviving marker. Both the gene and antibody merge forms
     * (merge-marker.jsp / merge-antibody.jsp) post here with the two ZDB ids. The actual merge is the
     * shared, equivalence-tested {@link MarkerMergeService} (same logic as {@code zfin-util
     * merge-markers}); on failure we redirect back to the merge form with an error message.
     */
    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    protected String mergeMarkers(
            @RequestParam("zdbIDToDelete") String zdbIDToDelete,
            @RequestParam("zdbIDToMergeInto") String zdbIDToMergeInto,
            RedirectAttributes redirectAttributes
    ) {
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        Marker markerToDelete = markerRepository.getMarkerByID(zdbIDToDelete);
        Marker markerToMergeInto = markerRepository.getMarkerByID(zdbIDToMergeInto);
        if (markerToDelete == null || markerToMergeInto == null) {
            redirectAttributes.addFlashAttribute("mergeError",
                    "Could not resolve both markers to merge (delete=" + zdbIDToDelete
                            + ", into=" + zdbIDToMergeInto + ").");
            return "redirect:/action/marker/merge?zdbIDToDelete=" + zdbIDToDelete;
        }

        try {
            HibernateUtil.createTransaction();
            // Shared, equivalence-tested merge (same logic as `zfin-util merge-markers`).
            new MarkerMergeService(zdbIDToDelete, zdbIDToMergeInto, false).merge();
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error("Error merging marker [" + zdbIDToDelete + "] into [" + zdbIDToMergeInto + "]", e);
            HibernateUtil.rollbackTransaction();
            redirectAttributes.addFlashAttribute("mergeError",
                    "Error merging " + markerToDelete.getAbbreviation() + " into "
                            + markerToMergeInto.getAbbreviation() + ": " + e.getMessage());
            return "redirect:/action/marker/merge?zdbIDToDelete=" + zdbIDToDelete;
        }

        // Merge succeeded: send the curator to the surviving marker (matches the Perl's redirect).
        return "redirect:/" + zdbIDToMergeInto;
    }

    // looks up gene to be merged into
    @RequestMapping(value = "/find-gene-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<LookupEntry> lookupGeneToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        List<LookupEntry> genesFound = RepositoryFactory.getMarkerRepository().getGeneSuggestionList(lookupString);
        List<LookupEntry> processedFoundGeneList = new ArrayList<>();
        for (LookupEntry gene : genesFound) {
            if (!gene.getId().equals(zdbId)) {
                processedFoundGeneList.add(gene);
            }
        }
        return processedFoundGeneList;
    }

    // looks up region to be merged into
    @RequestMapping(value = "/find-region-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<LookupEntry> lookupRegionToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        String type = MarkerService.getTypeForZdbID(zdbId);
        List<LookupEntry> foundRegionlist = RepositoryFactory.getMarkerRepository().getRegionListForString(lookupString, type);
        List<LookupEntry> processedFoundRegionlist = new ArrayList<>();
        for (LookupEntry region : foundRegionlist) {
            if (!region.getId().equals(zdbId)) {
                processedFoundRegionlist.add(region);
            }
        }
        return processedFoundRegionlist;
    }

    // looks up MO to be merged into
    @RequestMapping(value = "/find-mo-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<SequenceTargetingReagentLookupEntry> lookupMOToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        List<SequenceTargetingReagentLookupEntry> foundMOlist = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagentForString(lookupString, "MRPHLNO");
        List<SequenceTargetingReagentLookupEntry> processedFoundMOlist = new ArrayList<>();
        for (SequenceTargetingReagentLookupEntry mo : foundMOlist) {
            if (!mo.getId().equals(zdbId)) {
                processedFoundMOlist.add(mo);
            }
        }
        return processedFoundMOlist;
    }

    // looks up TALEN to be merged into
    @RequestMapping(value = "/find-talen-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<SequenceTargetingReagentLookupEntry> lookupTalenToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        List<SequenceTargetingReagentLookupEntry> foundTALENlist = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagentForString(lookupString, "TALEN");
        List<SequenceTargetingReagentLookupEntry> processedFoundTALENlist = new ArrayList<>();
        for (SequenceTargetingReagentLookupEntry talen : foundTALENlist) {
            if (!talen.getId().equals(zdbId)) {
                processedFoundTALENlist.add(talen);
            }
        }
        return processedFoundTALENlist;
    }

    // looks up CRISPR to be merged into
    @RequestMapping(value = "/find-crispr-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<SequenceTargetingReagentLookupEntry> lookupCrisprToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        List<SequenceTargetingReagentLookupEntry> foundCRISPRlist = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagentForString(lookupString, "CRISPR");
        List<SequenceTargetingReagentLookupEntry> processedFoundCRISPRlist = new ArrayList<>();
        for (SequenceTargetingReagentLookupEntry crispr : foundCRISPRlist) {
            if (!crispr.getId().equals(zdbId)) {
                processedFoundCRISPRlist.add(crispr);
            }
        }
        return processedFoundCRISPRlist;
    }

    @RequestMapping(value = "/get-transcripts-for-geneId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<TranscriptPresentation> getTranscriptsForGene(@RequestParam("geneZdbId") String geneZdbId) {
        return RepositoryFactory.getMarkerRepository().getTranscriptsForGeneId(geneZdbId);
    }

    @RequestMapping(value = "/get-eap-publication-for-geneId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<PublicationLink> getEapPublicationForGene(@RequestParam("geneZdbId") String geneZdbId) {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbId);
        if (gene == null)
            return null;
        List<ExpressionResult2> eapExpressionResults = RepositoryFactory.getExpressionRepository().getExpressionResultList(gene);
        List<PublicationLink> eapPublications = new ArrayList<>();
        if (eapExpressionResults != null && eapExpressionResults.size() > 0) {
            for (ExpressionResult2 eapExpressionResult : eapExpressionResults) {
                Publication eapPublication = eapExpressionResult.getExpressionFigureStage().getFigure().getPublication();
                PublicationLink publicationLink = new PublicationLink();
                publicationLink.setPublicationZdbId(eapPublication.getZdbID());
                publicationLink.setLinkContent(eapPublication.getShortAuthorList());
                eapPublications.add(publicationLink);
            }
        }
        return eapPublications;
    }

    @RequestMapping(value = "/get-non-eap-publication-for-geneId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<PublicationLink> getNonEapPublicationForGene(@RequestParam("geneZdbId") String geneZdbId) {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbId);
        if (gene == null)
            return null;
        List<ExpressionResult2> nonEapExpressionResults = RepositoryFactory.getExpressionRepository().getNonEapExpressionResultList(gene);
        List<PublicationLink> nonEapPublications = new ArrayList<>();
        if (nonEapExpressionResults != null && nonEapExpressionResults.size() > 0) {
            for (ExpressionResult2 nonEapExpressionResult : nonEapExpressionResults) {
                Publication nonEapPublication = nonEapExpressionResult.getExpressionFigureStage().getFigure().getPublication();
                PublicationLink publicationLink = new PublicationLink();
                publicationLink.setPublicationZdbId(nonEapPublication.getZdbID());
                publicationLink.setLinkContent(nonEapPublication.getShortAuthorList());
                nonEapPublications.add(publicationLink);
            }
        }
        return nonEapPublications;
    }

    @RequestMapping(value = "/get-orthology-for-geneId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<OrthologySlimPresentation> getOrthologyForGene(@RequestParam("geneZdbId") String geneZdbId) {
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(geneZdbId);
        if (gene == null)
            return null;
        return RepositoryFactory.getOrthologyRepository().getOrthologySlimForGeneId(geneZdbId);
    }

    @RequestMapping(value = "/get-targetGenes-for-sequenceTargetingReagentZdbId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<TargetGenePresentation> getTargetGenesForSequenceTargetingReagent(@RequestParam("sequenceTargetingReagentZdbId") String sequenceTargetingReagentZdbId) {
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        SequenceTargetingReagent sequenceTargetingReagent = mr.getSequenceTargetingReagent(sequenceTargetingReagentZdbId);
        if (sequenceTargetingReagent == null)
            return null;
        return mr.getTargetGenesForSequenceTargetingReagent(sequenceTargetingReagent);
    }

    @RequestMapping(value = "/get-sequence-for-sequenceTargetingReagent", method = RequestMethod.GET)
    public
    @ResponseBody
    STRsequencePresentation getSequenceForSTR(@RequestParam("sequenceTargetingReagentZdbId") String sequenceTargetingReagentZdbId) {
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        SequenceTargetingReagent sequenceTargetingReagent = mr.getSequenceTargetingReagent(sequenceTargetingReagentZdbId);
        if (sequenceTargetingReagent == null)
            return null;
        STRsequencePresentation sequencePresentation = new STRsequencePresentation();
        sequencePresentation.setSequence(sequenceTargetingReagent.getSequence().getSequence());
        if (sequenceTargetingReagentZdbId.startsWith("ZDB-TALEN")) {
            sequencePresentation.setSecondSequence(sequenceTargetingReagent.getSequence().getSecondSequence());
        } else {
            sequencePresentation.setSecondSequence("");
        }
        return sequencePresentation;
    }

    @RequestMapping(value = "/get-mapping-info", method = RequestMethod.GET)
    public
    @ResponseBody
    String getMappingInfoForMarkerId(@RequestParam("markerZdbId") String markerZdbId) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbId);
        return MappingService.getChromosomeLocationDisplay(marker);
    }

    @RequestMapping(value = "/get-accession", method = RequestMethod.GET)
    public
    @ResponseBody
    List<AccessionPresentation> getAccessionsWithLink(@RequestParam("markerZdbId") String markerZdbId, @RequestParam("db") String db) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbId);
        if (marker == null)
            return null;

        if (db.equals("NcbiGene")) {
            return RepositoryFactory.getSequenceRepository().getAccessionPresentation(ForeignDB.AvailableName.GENE, marker);
        } else if (db.equals("EnsemblGRCz11")) {
            return RepositoryFactory.getSequenceRepository().getAccessionPresentation(ForeignDB.AvailableName.ENSEMBL_GRCZ11_, marker);
        } else if (db.equals("Vega")) {
            List<Marker> transcripts = RepositoryFactory.getMarkerRepository().getSecondMarkersByFirstMarkerAndMarkerRelationshipType(marker, MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);

            List<AccessionPresentation> uniqueVegaIds = new ArrayList<>();
            Set<String> uniqueAccessionNumbers = new HashSet<>();
            for (Marker transcript : transcripts) {
                List<AccessionPresentation> vegaIdsWithDuplicate = RepositoryFactory.getSequenceRepository().getAccessionPresentation(ForeignDB.AvailableName.VEGA, transcript);
                for (AccessionPresentation vegaId : vegaIdsWithDuplicate) {
                    if (!uniqueAccessionNumbers.contains(vegaId.getAccessionNumber())) {
                        uniqueVegaIds.add(vegaId);
                        uniqueAccessionNumbers.add(vegaId.getAccessionNumber());
                    }
                }
            }

            return uniqueVegaIds;
        }

        return null;
    }

    @RequestMapping(value = "/get-unspecified-allele", method = RequestMethod.GET)
    public
    @ResponseBody
    SimpleFeaturePresentation getUnspecifiedFeature(@RequestParam("markerZdbId") String markerZdbId) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbId);
        if (marker == null)
            return null;

        SimpleFeaturePresentation unspecifiedAllele = new SimpleFeaturePresentation();
        Set<SimpleFeaturePresentation> unspecifiedFeatures = new HashSet<>();
        List<Feature> features = RepositoryFactory.getFeatureRepository().getFeaturesByMarker(marker);
        for (Feature feature : features) {
            if (feature.getUnspecifiedFeature()) {
                SimpleFeaturePresentation simpleFeature = new SimpleFeaturePresentation();
                simpleFeature.setName(feature.getName());
                simpleFeature.setZdbID(feature.getZdbID());
                unspecifiedFeatures.add(simpleFeature);
            }
        }

        // assuming only one (1) unspecified allele per gene
        for (SimpleFeaturePresentation allele : unspecifiedFeatures) {
            unspecifiedAllele = allele;
        }
        return unspecifiedAllele;
    }

    @RequestMapping(value = "/get-STR-for-gene", method = RequestMethod.GET)
    public
    @ResponseBody
    List<SequenceTargetingReagentLookupEntry> getSTRforGene(@RequestParam("geneZdbID") String geneZdbID) {
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        Marker gene = mr.getMarkerByID(geneZdbID);
        Set<RelatedMarker> sequenceTargetingReagents = new HashSet<>();
        if (gene.isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)) {
            Set<RelatedMarker> crisprs = MarkerService.getRelatedMarkers(gene, MarkerRelationship.Type.CRISPR_TARGETS_REGION);
            if (crisprs != null) {
                sequenceTargetingReagents.addAll(crisprs);
            }
            Set<RelatedMarker> talens = MarkerService.getRelatedMarkers(gene, MarkerRelationship.Type.TALEN_TARGETS_REGION);
            if (talens != null) {
                sequenceTargetingReagents.addAll(talens);
            }
        } else {
            sequenceTargetingReagents = MarkerService.getRelatedMarkers(gene, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        }
        List<SequenceTargetingReagentLookupEntry> sequenceTargetingReagentEntries = new ArrayList<>();
        for (RelatedMarker str : sequenceTargetingReagents) {
            SequenceTargetingReagentLookupEntry sequenceTargetingReagentEntry = new SequenceTargetingReagentLookupEntry();
            sequenceTargetingReagentEntry.setId(str.getMarker().getZdbID());
            sequenceTargetingReagentEntry.setLabel(str.getMarker().getAbbreviation());
            sequenceTargetingReagentEntry.setValue(str.getMarker().getAbbreviation());
            sequenceTargetingReagentEntries.add(sequenceTargetingReagentEntry);
        }
        Collections.sort(sequenceTargetingReagentEntries);
        return sequenceTargetingReagentEntries;
    }

    @RequestMapping(value = "/get-antibody-for-gene", method = RequestMethod.GET)
    public
    @ResponseBody
    List<AntibodyLookupEntry> getAntibodyforGene(@RequestParam("geneZdbID") String geneZdbID) {
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        Marker gene = mr.getMarkerByID(geneZdbID);
        List<MarkerRelationshipPresentation> relatedAntibodies = mr.getRelatedMarkerDisplayForTypes(gene, true, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        List<AntibodyLookupEntry> antibodyLookupEntries = new ArrayList<>();
        for (MarkerRelationshipPresentation antibody : relatedAntibodies) {
            AntibodyLookupEntry antibodyLookupEntry = new AntibodyLookupEntry();
            antibodyLookupEntry.setId(antibody.getZdbId());
            antibodyLookupEntry.setLabel(antibody.getAbbreviation());
            antibodyLookupEntry.setValue(antibody.getAbbreviation());
            antibodyLookupEntries.add(antibodyLookupEntry);
        }
        Collections.sort(antibodyLookupEntries);
        return antibodyLookupEntries;
    }

    @RequestMapping(value = "/sequenceTargetingReagent-used-in-fish", method = RequestMethod.GET)
    public
    @ResponseBody
    String usedInFish(@RequestParam("sequenceTargetingReagentZdbId") String sequenceTargetingReagentZdbId) {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        SequenceTargetingReagent str = mutantRepository.getSequenceTargetingReagentByID(sequenceTargetingReagentZdbId);
        if (str == null) {
            return "No";
        }
        List<Fish> fishList = mutantRepository.getFishListBySequenceTargetingReagent(str);
        if (fishList == null || fishList.size() == 0)
            return "No";
        else
            return "Yes";
    }

    @RequestMapping(value = "/get-fish-for-sequenceTargetingReagentZdbId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<LookupEntry> getFishList(@RequestParam("sequenceTargetingReagentZdbId") String sequenceTargetingReagentZdbId) {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        SequenceTargetingReagent str = mutantRepository.getSequenceTargetingReagentByID(sequenceTargetingReagentZdbId);
        if (str == null) {
            return null;
        }
        List<Fish> fishList = mutantRepository.getFishListBySequenceTargetingReagent(str);
        List<LookupEntry> fishes = new ArrayList<>(fishList.size());
        for (Fish fish : fishList) {
            LookupEntry fishEntry = new LookupEntry();
            fishEntry.setId(fish.getZdbID());
            fishEntry.setName(fish.getName());
            fishes.add(fishEntry);
        }
        return fishes;
    }

    @RequestMapping(value = "/update-str-sequence", method = RequestMethod.POST)
    public
    @ResponseBody
    void updateStrSequence(@RequestParam("source") String source, @RequestParam("target") String target) {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        SequenceTargetingReagent sourceStr = mutantRepository.getSequenceTargetingReagentByID(source);
        SequenceTargetingReagent targetStr = mutantRepository.getSequenceTargetingReagentByID(target);
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        markerRepository.copyStrSequence(sourceStr, targetStr);
        return;
    }

    // looks up anitibody to be merged into
    @RequestMapping(value = "/find-antibody-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<AntibodyLookupEntry> lookupAntibodyToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        List<AntibodyLookupEntry> foundList = RepositoryFactory.getMarkerRepository().getAntibodyForString(lookupString, "ATB");
        List<AntibodyLookupEntry> processedFoundList = new ArrayList<>();
        for (AntibodyLookupEntry ab : foundList) {
            if (!ab.getId().equals(zdbId)) {
                processedFoundList.add(ab);
            }
        }
        return processedFoundList;
    }

    @RequestMapping(value = "/get-antibody-clonal-type", method = RequestMethod.GET)
    public
    @ResponseBody
    String getClonalTypeForAntibodyId(@RequestParam("antibodyZdbId") String antibodyZdbId) {
        Antibody antibody = getAntibodyRepository().getAntibodyByID(antibodyZdbId);
        return antibody.getClonalType();
    }

    @RequestMapping(value = "/get-antibody-heavy-chain-isotype", method = RequestMethod.GET)
    public
    @ResponseBody
    String getHeavyChainIsotypeForAntibodyId(@RequestParam("antibodyZdbId") String antibodyZdbId) {
        Antibody antibody = getAntibodyRepository().getAntibodyByID(antibodyZdbId);
        return antibody.getHeavyChainIsotype();
    }

    @RequestMapping(value = "/get-antibody-light-chain-isotype", method = RequestMethod.GET)
    public
    @ResponseBody
    String getLightChainIsotypeForAntibodyId(@RequestParam("antibodyZdbId") String antibodyZdbId) {
        Antibody antibody = getAntibodyRepository().getAntibodyByID(antibodyZdbId);
        return antibody.getLightChainIsotype();
    }

    @RequestMapping(value = "/get-antibody-host-species", method = RequestMethod.GET)
    public
    @ResponseBody
    String getHostSpeciesForAntibodyId(@RequestParam("antibodyZdbId") String antibodyZdbId) {
        Antibody antibody = getAntibodyRepository().getAntibodyByID(antibodyZdbId);
        return antibody.getHostSpecies();
    }

    @RequestMapping(value = "/get-antibody-immunogen-species", method = RequestMethod.GET)
    public
    @ResponseBody
    String getImmunogenSpeciesForAntibodyId(@RequestParam("antibodyZdbId") String antibodyZdbId) {
        Antibody antibody = getAntibodyRepository().getAntibodyByID(antibodyZdbId);
        return antibody.getImmunogenSpecies();
    }
}
