package org.zfin.sequence.reno.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.Marker;
import org.zfin.people.Person;
import org.zfin.sequence.reno.RunCandidate;

import java.util.List;


/**
 * Class CandidateController.
 */
@Controller
public class RedundancyCandidateController extends AbstractCandidateController {

    private static Logger logger = Logger.getLogger(RedundancyCandidateController.class);

    private Validator validator = new RedundancyCandidateValidator();

    @RequestMapping(value = "/redundancy-candidate-view/{zdbID}", method = RequestMethod.GET)
    public String referenceData(@PathVariable String zdbID, CandidateBean candidateBean, Model model) {
        candidateBean.createRunCandidateForZdbID(zdbID);
        return handleGet(candidateBean, model);
    }

    @RequestMapping(value = "/redundancy-candidate-view/{zdbID}", method = RequestMethod.POST)
    protected ModelAndView onSubmit(@PathVariable String zdbID, CandidateBean candidateBean, BindingResult errors) throws Exception {
        if(doLock(zdbID, candidateBean)){
            return new ModelAndView("redirect:/action/reno/redundancy-candidate-view/" + zdbID);
        }
        candidateBean.createRunCandidateForZdbID(zdbID);
        ModelAndView modelAndView = handleSubmit(candidateBean, errors);
        modelAndView.addObject("errors", errors);
        return modelAndView;
    }


    /**
     * Does all the work for referenceData method, handles loading up the runCandidate for viewing,
     * locking, unlocking, note saving and toggling problem status
     *
     * @param candidateBean bean...with data
     */
    public void handleView(CandidateBean candidateBean) {
        //get the runcandidate from the bean and use the repository to populate it

        String runCandidateID = candidateBean.getRunCandidate().getZdbID();
        RunCandidate rc = renoRepository.getRunCandidateByID(runCandidateID);


        if (rc == null) {
            //the id was bad, return now, the jsp will handle the error
            String errorMessage = "Could not find a RunCandidate object with id " + runCandidateID;
            NullPointerException exception = new NullPointerException(errorMessage);
            logger.error(errorMessage, exception);
            throw exception;
        }

        renoService.populateLinkageGroups(rc);

        candidateBean.setRunCandidate(rc);

        candidateBean.setGeneAbbreviation(rc.getCandidate().getSuggestedName());

        List<DataAlias> aliases = ir.getDataAliases(candidateBean.getGeneAbbreviation());
        boolean isGeneAlias = false;
        for (DataAlias alias : aliases) {
            if (alias.getDataZdbID().startsWith("ZDB-GENE")) {
                isGeneAlias = true;
            }
        }
        candidateBean.setGeneAlias(isGeneAlias);


        handleNote(candidateBean);

        Person currentUser = Person.getCurrentSecurityUser();

        if (rc.getLockPerson() != null && !currentUser.equals(rc.getLockPerson())) {
            logger.debug(" Person records are not equal.. ");
        }

        logger.debug("action: " + candidateBean.getAction());

        // check that this candidate is not already related to any of the
        // associated markers
        List<Marker> associatedMarkers = renoService.checkForExistingRelationships(candidateBean, rc);
        candidateBean.setAllSingleAssociatedGenesFromQueries(associatedMarkers);

        //handle locking & unlocking
        renoService.handleLock(candidateBean);

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

    public void handleRunCandidate(CandidateBean candidateBean, BindingResult errors) {
        logger.info("enter handleRedundancy");
        Marker existingGene = null;

        validator.validate(candidateBean, errors);

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
            String suggestedName = candidateBean.getRunCandidate().getCandidate().getSuggestedName();
            if (false == suggestedName.startsWith("si:")) {
                errors.rejectValue("geneAbbreviation", "", "Only si: genes may be novel.  You need to create the gene '" + suggestedName + "'.");
                return;
            }
            //if the input box is filled, assume existing gene
            renoService.handleRedundancyNovelGene(candidateBean.getRunCandidate());
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


        renoService.createRedundancyRelationships(rc, existingGene);

        if (candidateBean.isRename()) {
            String abbreviation = candidateBean.getGeneAbbreviation();
            existingGene.setAbbreviation(abbreviation);
            existingGene.setName(abbreviation);
            renoService.renameGene(existingGene, rc.getRun().getNomenclaturePublication().getZdbID());
        }

        //create data note, copy curator note to data note, set curator note to nu
        renoService.moveNoteToGene(rc, existingGene);
        logger.info("handling an existing gene - exit");
    }


}
