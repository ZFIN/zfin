package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptType;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.TranscriptDBLink;
import org.zfin.sequence.blast.MountedWublastBlastService;
import org.zfin.sequence.service.TranscriptService;

import java.util.ArrayList;
import java.util.List;


/**
 */
@Controller
public class TranscriptViewController {

    private Logger logger = Logger.getLogger(TranscriptViewController.class);

    @RequestMapping(value = "/transcript-view")
    String getTranscriptView(Model model
            , @RequestParam("zdbID") String zdbID
            , @RequestHeader("User-Agent") String userAgent)
            throws Exception {

        // set base bean
        TranscriptBean transcriptBean = new TranscriptBean();
        transcriptBean.setZdbID(zdbID);
        Transcript transcript;

        logger.debug("zdbID: " + transcriptBean.getZdbID());
        transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(transcriptBean.getZdbID());

        if (transcript == null) {
            logger.debug("vegaID " + transcriptBean.getVegaID());
            transcript = RepositoryFactory.getMarkerRepository().getTranscriptByVegaID(transcriptBean.getVegaID());
        }

        // search in replaced data?
        if (transcript == null) {
            String replacedTranscriptZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(transcriptBean.getZdbID());
            logger.debug("trying to find a replaced zdbID for: " + transcriptBean.getZdbID());
            if (replacedTranscriptZdbID != null) {
                logger.debug("found a replaced zdbID for: " + transcriptBean.getZdbID() + "->" + replacedTranscriptZdbID);
                transcriptBean.setZdbID(replacedTranscriptZdbID);
                transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(transcriptBean.getZdbID());
            }
        }

        if (transcript == null) {
            model.addAttribute(LookupStrings.ZDB_ID, transcriptBean.getZdbID());
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        logger.debug("transcript: " + transcript);
        transcriptBean.setMarker(transcript);

        MarkerService.createDefaultViewForMarker(transcriptBean);


        // setting transcript relationships
        transcriptBean.setStrain(RepositoryFactory.getMarkerRepository().getStrainForTranscript(transcript.getZdbID()));

        RelatedMarkerDisplay transcriptRelationships = TranscriptService.getRelatedMarkerDisplay(transcript);
        transcriptBean.setMarkerRelationships(transcriptRelationships);

        transcriptBean.setRelatedGenes(TranscriptService.getRelatedGenes(transcript));


        //MicroRNA transcripts get a more simple data structure, because grouping by gene
        //leads to a very silly display.  The important thing is that we want to populate
        //either one of these data structures or the other.  The jsp will just dumbly
        //display whatever this smarty pants controller hands to it.
        // (ie, the brains are in the control layer, not the view layer)

        if (transcript.getTranscriptType().getType().equals(TranscriptType.Type.MIRNA)) {
            transcriptBean.setMicroRNARelatedTranscripts(TranscriptService.getRelatedTranscriptsForTranscript(transcript));
        } else {
            //build the collection of relatedTranscripts for each gene
            boolean showGBrowse = true;
            logger.debug("User-Agent: " + userAgent);
            if (StringUtils.contains(userAgent, "Java") || StringUtils.contains(userAgent, "Indexer")) {
                logger.debug("getting viewed by indexer, load less data");
                showGBrowse = false;
            }

            List<RelatedTranscriptDisplay> relatedTranscriptDisplayList = new ArrayList<RelatedTranscriptDisplay>();

            for (RelatedMarker relatedGene : transcriptBean.getRelatedGenes()) {
                Marker gene = relatedGene.getMarker();
                relatedTranscriptDisplayList.add(TranscriptService.getRelatedTranscriptsForGene(gene, transcript, showGBrowse));
            }
            transcriptBean.setRelatedTranscriptDisplayList(relatedTranscriptDisplayList);
        }


        // setting supporting sequences
        SequenceInfo sequenceInfo = TranscriptService.getSupportingSequenceInfo(transcript);
        transcriptBean.setSequenceInfo(sequenceInfo);

        //get the "other transcript.name pages" dblink set
        // should be handled by default "other" in MarkerService.createDefaultView
//        transcriptBean.setSummaryDBLinkDisplay(TranscriptService.getSummaryPages(transcript));

        //get the protein summary pages
        transcriptBean.setProteinProductDBLinkDisplay(TranscriptService.getProteinProductDBLinks(transcript));

        //the targets of the transcript, should only get filled for
        //microRNA - but should that restriction come from the DB or code?
//        if (transcript.getTranscriptType().getType().equals(TranscriptType.Type.MIRNA.toString())) {
        if (transcript.getTranscriptType().getType() == TranscriptType.Type.MIRNA){
            transcriptBean.setTranscriptTargets(TranscriptService.getTranscriptTargets(transcript));
        }

        //get the collection of non standard backgrounds (genotypes) for clones that contain this
        //transcript
        transcriptBean.setNonReferenceStrains(TranscriptService.getNonReferenceStrainsForTranscript(transcript));


        List<TranscriptDBLink> transcriptDBLinks = transcript.getTranscriptDBLinksForDisplayGroup(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
        List<Sequence> sequences = MountedWublastBlastService.getInstance().
                getSequencesForTranscript(transcript, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);

        // typically there won't be more than one sequence in either set
        if (transcriptDBLinks.size() > 0 && transcriptDBLinks.size() > sequences.size()) {
            List<DBLink> unableToFindDbLinks = new ArrayList<DBLink>();
            for (TranscriptDBLink transcriptDBLink : transcriptDBLinks) {

                boolean hasSequence = false;
                for (Sequence sequence : sequences) {
                    if (sequence.getDbLink() != null && transcriptDBLink.getAccessionNumber().equals(sequence.getDbLink().getAccessionNumber())) {
                        hasSequence = true;
                    }
                }

                if (false == hasSequence) {
                    unableToFindDbLinks.add(transcriptDBLink);
                }
            }
            transcriptBean.setUnableToFindDBLinks(unableToFindDbLinks);
        }


        logger.info("transcriptviewcontroller # of seq: " + sequences.size());
        transcriptBean.setNucleotideSequences(sequences);

        model.addAttribute(LookupStrings.FORM_BEAN, transcriptBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.TRANSCRIPT.getTitleString() + transcript.getAbbreviation());

        return "marker/transcript-view.page";
    }
}
