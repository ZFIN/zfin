package org.zfin.sequence.reno.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.marker.*;
import org.zfin.people.Person;
import org.zfin.sequence.Accession;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.*;

import java.util.*;


/**
 * Class CandidateController.
 */
public class RedundancyCandidateController extends AbstractCandidateController{

    private static Logger LOG = Logger.getLogger(RedundancyCandidateController.class);

    /**
     * Does all the work for referenceData method, handles loading up the runCandidate for viewing,
     * locking, unlocking, note saving and toggling problem status
     *
     * @param candidateBean bean...with data
     */
    public void handleView(CandidateBean candidateBean) {
        //get the runcandidate from the bean and use the repository to populate it
        String runCandidateID = candidateBean.getRunCandidate().getZdbID();
        RunCandidate rc = rr.getRunCandidateByID(runCandidateID);

        if (rc == null) {
            //the id was bad, return now, the jsp will handle the error
            String errorMessage = "Could not find a RunCandidate object with id " + runCandidateID;
            NullPointerException exception = new NullPointerException(errorMessage);
            LOG.error(errorMessage, exception);
            throw exception;
        }
        RenoService.populateLinkageGroups(rc);

        candidateBean.setRunCandidate(rc);

        RedundancyRun run = (RedundancyRun) rc.getRun();

//        LOG.debug("instance of RedundancyRun: " + (run instanceof RedundancyRun));
//        LOG.debug("Run.isRedundancy: " + run.isRedundancy());
//        LOG.debug("Run.isNomenclature: " + run.isNomenclature());


        candidateBean.setGeneAbbreviation(rc.getCandidate().getSuggestedName());

        handleNote(candidateBean);

        Person currentUser = Person.getCurrentSecurityUser();

        if (rc.getLockPerson() != null && !currentUser.equals(rc.getLockPerson()))
            LOG.debug(" Person records are not equal.. ");

        LOG.debug("action: " + candidateBean.getAction());

        // check that this candidate is not already related to any of the
        // associated markers
        List<Marker> associatedMarkers = RenoService.checkForExistingRelationships(candidateBean, rc);
        candidateBean.setAllSingleAssociatedGenesFromQueries(associatedMarkers);

        //handle locking & unlocking
        handleLock(candidateBean, rc, currentUser);

        //handle problem toggling
        if (StringUtils.equals(candidateBean.getAction(), CandidateBean.SET_PROBLEM)) {
            LOG.debug("acqui, senior?");
            rc.getCandidate().setProblem(candidateBean.isCandidateProblem());
            LOG.info(currentUser.getZdbID()
                    + " set " + rc.getCandidate().getZdbID()
                    + " problem status to " + rc.getCandidate().isProblem());

        }
        //if problem was set or not, copy the value from the candidate to the field in the bean
        candidateBean.setCandidateProblem(rc.getCandidate().isProblem());
    }

    public void handleRunCandidate(CandidateBean candidateBean) {
        LOG.info("enter handleRedundancy");
        Marker existingGene = null;

        //first, check the text input box, if there's anything there, we
        //ignore the value in the pulldown
        if (!candidateBean.getGeneZdbID().equals("")) {
            existingGene = mr.getMarkerByID(candidateBean.getGeneZdbID());
        } else if (candidateBean.getAssociatedGeneField().startsWith("ZDB-")) {
            //if the text input box was null, see if an existing gene was chosen in the pulldown
            //(but don't go into this code for PROBLEM, NOVEL or IGNORE
            existingGene = mr.getMarkerByID(candidateBean.getAssociatedGeneField());
        }

        //If else blocks for performing actions depending on choice in associatedGene box

        if (candidateBean.getAssociatedGeneField().equals(CandidateBean.IGNORE)) {
            //setDone happens at the end of handleDone - so there's nothing to do here
        } else if (candidateBean.getAssociatedGeneField().equals(CandidateBean.NOVEL)
                && (existingGene == null)) {
            //if the input box is filled, assume existing gene
            handleRedundancyNovelGene(candidateBean);
        } else {
            handleRedundancyExistingGene(candidateBean, existingGene);
        }


    }

    protected void handleRedundancyNovelGene(CandidateBean candidateBean) {
        LOG.info("enter handleNovelGene");

        RunCandidate rc = candidateBean.getRunCandidate();

        if (!rc.getRun().isRedundancy()) {
            LOG.info("run should be redundancy");
            return;
        }

        //Create a Marker object
        Marker novelGene = new Marker();

        novelGene.setName(rc.getCandidate().getSuggestedName());
        novelGene.setOwner(rc.getLockPerson());
        LOG.info("novelGene is set");
        MarkerType mt = mr.getMarkerTypeByName(rc.getCandidate().getMarkerType());
        if (mt == null) {
            String newline = System.getProperty("line.separator");
            String message = "No Marker Type with name " + rc.getCandidate().getMarkerType() + " found for " +
                    " Candidate: " + newline + rc.getCandidate();
            throw new NullPointerException(message);
        }
        // if a new gene is created make sure the abbreviation is lower case according to
        // nomenclature conventions.
        String suggestedAbbreviation = rc.getCandidate().getSuggestedName();
        if (mt.getType() == Marker.Type.GENE) {
            suggestedAbbreviation = suggestedAbbreviation.toLowerCase();
        }
        novelGene.setAbbreviation(suggestedAbbreviation);
        novelGene.setMarkerType(mt);
        mr.createMarker(novelGene, ((RedundancyRun) rc.getRun()).getRelationPublication());
        LOG.info("novelGene zdb_id: " + novelGene.getZdbID());
        //update marker history reason
        MarkerHistory mhist = mr.getLastMarkerHistory(novelGene, MarkerHistory.Event.ASSIGNED);

        if (mhist == null) {
            String errorMessage = "No Marker History found. Trigger did not run! ";
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage);

        }
        // change the reason for creating the marker in the Marker History
        mhist.setReason(MarkerHistory.Reason.NOT_SPECIFIED);

        createRedundancyRelationships(rc, novelGene);

        //create data note, copy curator note to data note, set curator note to null
        RenoService.moveNoteToGene(rc, novelGene);
    }


    protected void handleRedundancyExistingGene(CandidateBean candidateBean, Marker existingGene) {
        LOG.info("handling an existing gene - entry");

        RunCandidate rc = candidateBean.getRunCandidate();

        if (!rc.getRun().isRedundancy()) {
            LOG.info("run should be redundancy");
            return;
        }


        createRedundancyRelationships(rc, existingGene);

        if (candidateBean.isRename()) {
            String abbreviation = candidateBean.getGeneAbbreviation();
            existingGene.setAbbreviation(abbreviation);
            existingGene.setName(abbreviation);
            RenoService.renameGene(existingGene, rc.getRun().getNomenclaturePublication().getZdbID());
        }

        //create data note, copy curator note to data note, set curator note to nu
        RenoService.moveNoteToGene(rc, existingGene);
        LOG.info("handling an existing gene - exit");
    }



    /**
     * Creates marker relationships (or DBLinks) to connect markers (or accessions)
     * <p/>
     * the basic case is, for one est, or more than one est, we just make a marker relationship.
     * <p/>
     * if there are genes and ests both, it's a cleanup issue.  that's not handled yet
     * <p/>
     * if there is only a gene, then there's no new relationships to add, because that gene
     * came in because it already had a link to the query accession
     * <p/>
     * if there are no markers at all associated with the query accessions, then we link
     * them to the gene that the curators chose.
     *
     * @param rc   the RunCandidate
     * @param gene the gene chosen by the curators (could be newly created)
     */
    protected void createRedundancyRelationships(RunCandidate rc, Marker gene) {
        LOG.info("createRelationsips gene: " + gene);
        LOG.info("createRelationsips runCanZdbID: " + rc.getZdbID());

        if (!rc.getRun().isRedundancy()) {
            LOG.info("run should be redundancy");
            return;
        }
        String attributionZdbID = ((RedundancyRun) rc.getRun()).getRelationPublication().getZdbID();

        // thing to associate with
        List<Marker> markers = rc.getIdentifiedMarkers();
        LOG.debug("createRelationships markers.size(): " + markers.size());
        List<Marker> segments = RenoService.getSmallSegments(markers);

        //pull the single gene from the collection, if there is one.
        Marker candidateMarker = null;
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                LOG.debug("createRelationships marker type: " + m.getMarkerType().getType());
                LOG.debug("createRelationships is in type group genedom");
                candidateMarker  = m;
                break;
            }
            else
            if (m.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
                LOG.debug("createRelationships marker type: " + m.getMarkerType().getType());
                LOG.debug("createRelationships is in type group transcript");
                candidateMarker  = m;
                break;
            }
            else {
                LOG.debug("createRelationships NOT in type group genedom or transcript");

            }
        }
        LOG.debug("createRelationships rc.getCandidateQueries().size(): " + rc.getCandidateQueries().size());
        //pull the query accessions from the runCandidate
        Set<Accession> accessions = new HashSet<Accession>();
        for (Query q : rc.getCandidateQueries()) {
            accessions.add(q.getAccession());
            LOG.debug("adding accessions");
        }

        //if there are segments, we associate them with the gene,
        //if there are not, we make dblinks connecting the query
        //accessions to the gene.
        //if there is a gene associated with the query accessions, ignore it,
        //because the association we would make already exists
        if (!segments.isEmpty()) {
            LOG.debug("createRelationships segments are not empty");
            for (Marker segment : segments) {
                LOG.info("adding small segment to gene: " + segment);
                MarkerRelationship mrel = new MarkerRelationship();
                if(segment.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)){
                    mrel.setType(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
                }
                else{
                    mrel.setType(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
                }
                mrel.setFirstMarker(gene);
                mrel.setSecondMarker(segment);
                mr.addMarkerRelationship(mrel,  attributionZdbID);
            }
            //our query accession(s) was(were) linked to one or more segments, we just made
            //relationships from those segments to the gene that the curator chose
            //now, if there is a dblink from the accession directly to that gene,
            //we delete it.  (unless it's directly attributed to a journal article)
            MarkerService.removeRedundantDBLinks(gene, accessions);

        } else if (candidateMarker  == null) {
            //no segments & no genes means that we have an accession that
            //has yet to be linked to any marker at all, so we link it to whatever
            //gene the curators chose.
            createRedundancyDBLinks(rc, gene);
        }
    }

    /**
     * Associate the query accessions directly to the gene, because we don't
     * have an EST to put in between them
     *
     * @param rc   Runcandidate
     * @param gene Gene object
     */
    private void createRedundancyDBLinks(RunCandidate rc, Marker gene) {
        LOG.info("creating DBLinks");

        if (!rc.getRun().isRedundancy()) {
            LOG.info("run should be redundancy");
            return;
        }
        //create DBLinks for all queries
        String attributionZdbID = ((RedundancyRun) rc.getRun()).getRelationPublication().getZdbID();
        for (Query q : rc.getCandidateQueries()) {
            mr.addDBLink(gene, q.getAccession().getNumber(),
                    q.getAccession().getReferenceDatabase(), attributionZdbID);
        }
    }

}
