package org.zfin.sequence.reno.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.marker.*;
import org.zfin.people.Person;
import org.zfin.sequence.reno.RenoService;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.framework.HibernateUtil;

import java.util.List;


/**
 * Class CandidateController.
 */
public class RedundancyCandidateController extends AbstractCandidateController {

    private static Logger logger = Logger.getLogger(RedundancyCandidateController.class);

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
            logger.error(errorMessage, exception);
            throw exception;
        }
        RenoService.populateLinkageGroups(rc);

        candidateBean.setRunCandidate(rc);

        candidateBean.setGeneAbbreviation(rc.getCandidate().getSuggestedName());

        handleNote(candidateBean);

        Person currentUser = Person.getCurrentSecurityUser();

        if (rc.getLockPerson() != null && !currentUser.equals(rc.getLockPerson())){
            logger.debug(" Person records are not equal.. ");
        }

        logger.debug("action: " + candidateBean.getAction());

        // check that this candidate is not already related to any of the
        // associated markers
        List<Marker> associatedMarkers = RenoService.checkForExistingRelationships(candidateBean, rc);
        candidateBean.setAllSingleAssociatedGenesFromQueries(associatedMarkers);

        //handle locking & unlocking
        RenoService.handleLock(candidateBean);

        //handle problem toggling
        if (StringUtils.equals(candidateBean.getAction(), CandidateBean.SET_PROBLEM)) {
            logger.debug("acqui, senior?");
            rc.getCandidate().setProblem(candidateBean.isCandidateProblem());
            logger.info(currentUser.getZdbID()
                    + " set " + rc.getCandidate().getZdbID()
                    + " problem status to " + rc.getCandidate().isProblem());

        }
        //if problem was set or not, copy the value from the candidate to the field in the bean
        candidateBean.setCandidateProblem(rc.getCandidate().isProblem());
    }

    public void handleRunCandidate(CandidateBean candidateBean) {
        logger.info("enter handleRedundancy");
        Marker existingGene = null;

        //first, check the text input box, if there's anything there, we
        //ignore the value in the pulldown
        if (!candidateBean.getGeneZdbID().trim().equals("")) {
            existingGene = mr.getMarkerByID(candidateBean.getGeneZdbID().trim().toUpperCase());
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
            RenoService.handleRedundancyNovelGene(candidateBean.getRunCandidate());
        } else {
            handleRedundancyExistingGene(candidateBean, existingGene);
        }


    }


    protected void handleRedundancyExistingGene(CandidateBean candidateBean, Marker existingGene) {
        logger.info("handling an existing gene - entry");

        RunCandidate rc = candidateBean.getRunCandidate();

        if (!rc.getRun().isRedundancy()) {
            logger.info("run should be redundancy");
            return;
        }


        RenoService.createRedundancyRelationships(rc, existingGene);

        if (candidateBean.isRename()) {
            String abbreviation = candidateBean.getGeneAbbreviation();
            existingGene.setAbbreviation(abbreviation);
            existingGene.setName(abbreviation);
            RenoService.renameGene(existingGene, rc.getRun().getNomenclaturePublication().getZdbID());
        }

        //create data note, copy curator note to data note, set curator note to nu
        RenoService.moveNoteToGene(rc, existingGene);
        logger.info("handling an existing gene - exit");
    }




}
