package org.zfin.sequence.reno.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RunCandidate;

/**
 * Class AlignmentsController.
 */
@Controller
@RequestMapping(value = "/reno")
public class AlignmentsController {

    private static Logger logger = LogManager.getLogger(AlignmentsController.class);

    public AlignmentsController() {
        // set here because test-classes choke without it
        // also has to be set in order to provide errors
//        setCommandClass(Object.class);
//        setCommandName("formBean");
    }

    // TODO: Fix this so that we have the appropriate model and error.
    @RequestMapping("/alignment-list/{runCandidateZdbID}")
    public String handle(@PathVariable String runCandidateZdbID
            ,CandidateBean candidateBean, BindingResult errors,Model model) throws Exception {
        RunCandidate runCandidate = RepositoryFactory.getRenoRepository().getRunCandidateByID(runCandidateZdbID);
        logger.debug("ZdbID[" + runCandidateZdbID + "] runCandidate[" + runCandidate + "]");

        if (runCandidate != null) {
            logger.debug("ZdbID[" + runCandidateZdbID + "] runCandidate queries.size()[" + runCandidate.getCandidateQueries() + "]");
            candidateBean.setRunCandidate(runCandidate);
        } else {
            logger.fatal("ZdbID[" + runCandidateZdbID + "] runCandidate is null generating an empty one");
            candidateBean.setRunCandidate(new RunCandidate());
            errors.reject("no message key", "Invalidate RunCandidate zdbID[" + runCandidateZdbID + "]");
        }
        model.addAttribute(LookupStrings.FORM_BEAN, candidateBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "ReNo Redundancy Candidate: "+runCandidateZdbID);

        return "reno/alignments-list";

    }

}


