package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionResult2;
import org.zfin.feature.Feature;
import org.zfin.feature.presentation.SimpleFeaturePresentation;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mapping.MappingService;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MergeService;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.orthology.presentation.OrthologySlimPresentation;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationLink;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.presentation.AccessionPresentation;
import org.zfin.sequence.presentation.STRsequencePresentation;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * Note that this is only for merging markers and does not handle genotypes or features.
 */
@Controller
@RequestMapping("/marker")
public class MergeMarkerController {

    private MergeMarkerValidator validator = new MergeMarkerValidator();
    private Logger logger = Logger.getLogger(MergeMarkerController.class);

    protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        MergeBean mergeBean = (MergeBean) command;
        Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(mergeBean.getZdbIDToDelete());
        mergeBean.setMarkerToDelete(markerToDelete);

        Marker markerToMergeInto = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(mergeBean.getMarkerToMergeIntoViewString());
        mergeBean.setMarkerToMergeInto(markerToMergeInto);

        if (markerToMergeInto == null) {
            Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByName(mergeBean.getMarkerToMergeIntoViewString());
            if (antibodyToMergeInto == null) {
                errors.rejectValue(null, "nocode", new String[]{mergeBean.getMarkerToMergeIntoViewString()}, "Bad antibody name [{0}]");
            }
        }
    }

    @RequestMapping( value = "/merge",method = RequestMethod.GET)
    protected String getView(
            Model model
            ,@RequestParam("zdbIDToDelete") String zdbIDToDelete
            ,@ModelAttribute("formBean") MergeBean formBean
            ,BindingResult result
    ) throws Exception {
        String type = zdbIDToDelete.substring(4, 8);

        Marker markerToDelete;

        if (type.startsWith("ATB") || type.startsWith("GEN") || type.startsWith("MRP") || type.startsWith("TAL") || type.startsWith("CRI"))  {

            markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean .getZdbIDToDelete());

            formBean.setMarkerToDelete(markerToDelete);
            //        model.addAttribute("markerToDeleteId", markerToDelete.getZdbID());
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());
        }
        return "marker/merge-marker.page";
    }

    @RequestMapping( value = "/merge",method = RequestMethod.POST)
    protected String mergeMarkers(
            Model model
            ,@ModelAttribute("formBean") MergeBean formBean
//            ,@RequestParam("getZdbIDToDelete") String zdbIDToDelete
//            ,@RequestParam("markerToMergeIntoViewString") String markerToMergeIntoViewString
            ,BindingResult result
    ) throws Exception {
        Marker markerTobeMerged = formBean.getMarkerToDelete();
        if (markerTobeMerged == null) {
            markerTobeMerged = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean .getZdbIDToDelete());
            formBean.setMarkerToDelete(markerTobeMerged);
        }
        if (markerTobeMerged.isInTypeGroup(Marker.TypeGroup.ATB) || markerTobeMerged.isInTypeGroup(Marker.TypeGroup.GENE) || markerTobeMerged.isInTypeGroup(Marker.TypeGroup.MRPHLNO)) {
            Marker markerToDelete = RepositoryFactory.getMarkerRepository().getMarkerByID(formBean.getZdbIDToDelete()) ;
            formBean.setMarkerToDelete(markerToDelete);
            // get abbrev
            Marker markerToMergeInto = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation(formBean.getMarkerToMergeIntoViewString());
            formBean.setMarkerToMergeInto(markerToMergeInto);

            if (markerToMergeInto == null && markerTobeMerged.isInTypeGroup(Marker.TypeGroup.ATB) ) {
                Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByName(formBean.getMarkerToMergeIntoViewString());
                if (antibodyToMergeInto == null) {
                    result.rejectValue(null, "nocode", new String[]{formBean.getMarkerToMergeIntoViewString()}, "Bad antibody name [{0}]");
                }
            }

            if (markerTobeMerged.isInTypeGroup(Marker.TypeGroup.ATB))
                validator.validate(formBean,result);

            if(result.hasErrors()){
                return getView(model,formBean.getZdbIDToDelete(),formBean,result);
            }


            try {
                HibernateUtil.createTransaction();
                MergeService.mergeMarker(markerToDelete, markerToMergeInto);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                logger.error("Error merging marker [" + markerToDelete + "] into [" + markerToMergeInto + "]", e);
                HibernateUtil.rollbackTransaction();
                result.reject("no lookup", "Error merging marker [" + markerToDelete + "] into [" + markerToMergeInto + "]:\n"+ e);
                return getView(model,formBean.getZdbIDToDelete(),formBean,result);
            }
//        finally {
//            HibernateUtil.rollbackTransaction();
//        }

            model.addAttribute(LookupStrings.FORM_BEAN, formBean );
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerToDelete.getAbbreviation());
        }

        return "marker/merge-marker-finish.page";
    }

    // looks up gene to be merged into
    @RequestMapping(value = "/find-gene-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<TargetGeneLookupEntry> lookupGeneToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        List<TargetGeneLookupEntry> genesFound = RepositoryFactory.getMarkerRepository().getTargetGenesWithNoTranscriptForString(lookupString);
        List<TargetGeneLookupEntry> processedFoundGeneList = new ArrayList<>();
        for (TargetGeneLookupEntry gene : genesFound) {
            if (!gene.getId().equals(zdbId)) {
                processedFoundGeneList.add(gene);
            }
        }
        return processedFoundGeneList;
    }

    // looks up MO to be merged into
    @RequestMapping(value = "/find-mo-to-merge-into", method = RequestMethod.GET)
    public
    @ResponseBody
    List<SequenceTargetingReagentLookupEntry> lookupMOToMergeInto(@RequestParam("term") String lookupString, @RequestParam("exclude") String zdbId) {
        List<SequenceTargetingReagentLookupEntry> foundMOlist = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagentForString(lookupString,"MRPHLNO");
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
        List<SequenceTargetingReagentLookupEntry> foundTALENlist = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagentForString(lookupString,"TALEN");
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
        List<SequenceTargetingReagentLookupEntry> foundCRISPRlist = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagentForString(lookupString,"CRISPR");
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
        Marker gene = RepositoryFactory.getMarkerRepository().getGeneByID(geneZdbId);
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

    @RequestMapping(value = "/get-orthology-for-geneId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<OrthologySlimPresentation> getOrthologyForGene(@RequestParam("geneZdbId") String geneZdbId) {
        Marker gene = RepositoryFactory.getMarkerRepository().getGeneByID(geneZdbId);
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
        } else if (db.equals("UniGene")) {
            return RepositoryFactory.getSequenceRepository().getAccessionPresentation(ForeignDB.AvailableName.UNIGENE, marker);
        } else if (db.equals("EnsemblGRCz10")) {
            return RepositoryFactory.getSequenceRepository().getAccessionPresentation(ForeignDB.AvailableName.ENSEMBL_GRCZ10_, marker);
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
        Set<RelatedMarker> sequenceTargetingReagents = MarkerService.getRelatedMarkers(gene, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
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
    String updateStrSequence(@RequestParam("source") String source, @RequestParam("target") String target) {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        SequenceTargetingReagent sourceStr = mutantRepository.getSequenceTargetingReagentByID(source);
        SequenceTargetingReagent targetStr = mutantRepository.getSequenceTargetingReagentByID(target);
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        markerRepository.copyStrSequence(sourceStr, targetStr);
        return "Done";
    }
}
