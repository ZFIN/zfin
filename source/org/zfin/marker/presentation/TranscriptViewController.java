package org.zfin.marker.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptType;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.MountedWublastBlastService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class TranscriptViewController extends AbstractCommandController {


    public TranscriptViewController() {
        setCommandClass(TranscriptBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        // set base bean
        TranscriptBean transcriptBean = (TranscriptBean) command;
        Transcript transcript;

        if (transcriptBean.getZdbID() != null) {
            logger.debug("zdbID: " + transcriptBean.getZdbID());
            transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(transcriptBean.getZdbID()) ;
        } else  {
            logger.debug("vegaID " + transcriptBean.getVegaID());
            transcript = RepositoryFactory.getMarkerRepository().getTranscriptByVegaID(transcriptBean.getVegaID()) ;
        }

        if(transcript==null){
            ModelAndView errorModelAndView = new ModelAndView("record-not-found.page") ;
            errorModelAndView.addObject(LookupStrings.ZDB_ID,transcriptBean.getZdbID()) ;
            return errorModelAndView ;
        }

        logger.debug("transcript: " + transcript);
        transcriptBean.setMarker(transcript);


        // setting transcript relationships

        RelatedMarkerDisplay transcriptRelationships = TranscriptService.getRelatedMarkerDisplay(transcript)  ;
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
            List<RelatedTranscriptDisplay> relatedTranscriptDisplayList = new ArrayList<RelatedTranscriptDisplay>();

            for (RelatedMarker relatedGene : transcriptBean.getRelatedGenes()) {
                Marker gene = relatedGene.getMarker();
                relatedTranscriptDisplayList.add(TranscriptService.getRelatedTranscriptsForGene(gene));
            }
            transcriptBean.setRelatedTranscriptDisplayList(relatedTranscriptDisplayList);
        }



        // setting supporting sequences
        SequenceInfo sequenceInfo = TranscriptService.getSupportingSequenceInfo(transcript)  ;
        transcriptBean.setSequenceInfo(sequenceInfo);

        //get the "other transcript.name pages" dblink set
        transcriptBean.setSummaryDBLinkDisplay(TranscriptService.getSummaryPages(transcript));

        //get the protein summary pages
        transcriptBean.setProteinProductDBLinkDisplay(TranscriptService.getProteinProductDBLinks(transcript));

        //the targets of the transcript, should only get filled for
        //microRNA - but should that restriction come from the DB or code?
        transcriptBean.setTranscriptTargets(TranscriptService.getTranscriptTargets(transcript));

        //get the collection of non standard backgrounds (genotypes) for clones that contain this
        //transcript
        transcriptBean.setNonReferenceStrains(TranscriptService.getNonReferenceStrainsForTranscript(transcript));

        // setting publications
        transcriptBean.setNumPubs(RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForMarker(
                transcript,0).getTotalCount());

        List<TranscriptDBLink> transcriptDBLinks = transcript.getTranscriptDBLinksForDisplayGroup(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE) ;
        List<Sequence> sequences = MountedWublastBlastService.getInstance().
                getSequencesForTranscript(transcript, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);

        // typically there won't be more than one sequence in either set
        if(transcriptDBLinks.size()>0 && transcriptDBLinks.size()>sequences.size()){
            List<DBLink> unableToFindDbLinks = new ArrayList<DBLink>() ;
            for(TranscriptDBLink transcriptDBLink: transcriptDBLinks){

                boolean hasSequence = false ;
                for(Sequence sequence : sequences){
                    if(sequence.getDbLink()!=null && transcriptDBLink.getAccessionNumber().equals(sequence.getDbLink().getAccessionNumber())){
                        hasSequence = true ;
                    }
                }

                if(false==hasSequence){
                    unableToFindDbLinks.add(transcriptDBLink) ;
                }
            }
            transcriptBean.setUnableToFindDBLinks(unableToFindDbLinks);
        }



        logger.info("transcriptviewcontroller # of seq: " + sequences.size());
        transcriptBean.setNucleotideSequences(sequences);


        ModelAndView modelAndView = new ModelAndView("transcript-view.page") ;
        modelAndView.addObject(LookupStrings.FORM_BEAN,transcriptBean) ;
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, transcript.getAbbreviation()) ;

        return modelAndView ;
    }
}
