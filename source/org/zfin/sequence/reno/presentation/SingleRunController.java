package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.reno.service.RenoService;

/**
 * Class SingleRunController.
 */

@Controller
public class SingleRunController {

    private final Logger logger = Logger.getLogger(SingleRunController.class);

    private RenoRepository renoRepository = RepositoryFactory.getRenoRepository();  // set in spring configuration
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository(); // set in spring configuration
    private Validator validator = new RunBeanValidator();


    @RequestMapping(value = "/candidate/{candidateType}/{runZdbID}", method = RequestMethod.GET)
    public String referenceData(@PathVariable String runZdbID, @PathVariable String candidateType
            , RunBean form, Model model
            , @RequestParam(required = false, defaultValue = "expectValue") String comparator
            , @RequestParam(required = false) String action
    ) {
        Run run = renoRepository.getRunByID(runZdbID);
        RunPresentation.CandidateType candidateTypeEnum = RunPresentation.CandidateType.getType(candidateType);
        form.setAction(action);
        form.setComparator(comparator);
        form.setZdbID(runZdbID);

        model.addAttribute(LookupStrings.FORM_BEAN, form);

        if (run != null) {
            setFormData(form, run, candidateTypeEnum);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, run.getName());
        } else {
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, runZdbID);
        }

        return "reno/candidate-" + candidateType + ".page";
    }

    @RequestMapping(value = "/candidate/{candidateType}/{runZdbID}", method = RequestMethod.POST)
    public String onSubmit(@PathVariable String runZdbID, @PathVariable String candidateType
            , RunBean form
            , BindingResult errors
            , Model model
            , @RequestParam(required = false, defaultValue = "expectValue") String comparator
            , @RequestParam(required = false) String action

    ) throws Exception {

        RunPresentation.CandidateType candidateTypeEnum = RunPresentation.CandidateType.getType(candidateType);
        Run run = renoRepository.getRunByID(runZdbID);
        form.setAction(action);
        form.setComparator(comparator);
        form.setRun(run);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, run.getName());

        validator.validate(form, errors);

        if (errors.hasErrors()) {
            logger.error("Has Errors: " + errors.toString());
            model.addAttribute("errors", errors);
        } else {

            try {
                HibernateUtil.createTransaction();

                handleNomenclatureAttributionUpdate(form, run);

                if (run.isNomenclature()) {
                    handleOrthologyAttributionUpdate(form, (NomenclatureRun) run);
                } else if (run.isRedundancy()) {
                    handleRelationUpdate(form, (RedundancyRun) run);
                }

                HibernateUtil.flushAndCommitCurrentSession();
            } catch (RuntimeException e) {
                HibernateUtil.rollbackTransaction();
                logger.error("Error in onSubmit method ", e);
                throw e;
            }
        }

        setFormData(form, run, candidateTypeEnum);

        return "reno/candidate-" + candidateType + ".page";
    }

    /**
     * Use the parameter in the http request to sort the run candidates and
     * save the ordered list to the form bean.
     *
     * @param runBean RunBean
     * @param run     Actual Run
     */
    private void setFormData(RunBean runBean, Run run, RunPresentation.CandidateType candidateType) {

        runBean.setRun(run);
        runBean.setNomenclaturePublicationZdbID(run.getNomenclaturePublication().getZdbID());

        if (run.isNomenclature()) {
            // we've seen orthologyAttribution being null in db
            // thus check it to avoid null-pointer error
            NomenclatureRun nomenRun = (NomenclatureRun) run;
            if (nomenRun.getOrthologyPublication() == null) {
                runBean.setOrthologyPublicationZdbID(null);
            } else {
                runBean.setOrthologyPublicationZdbID(nomenRun.getOrthologyPublication().getZdbID());
            }
        } else if (run.isRedundancy()) {
            RedundancyRun redunRun = (RedundancyRun) run;

            if (redunRun.getRelationPublication() == null) {
                runBean.setRelationPublicationZdbID(null);
            } else {
                runBean.setRelationPublicationZdbID(redunRun.getRelationPublication().getZdbID());
            }

        }


        if (candidateType == RunPresentation.CandidateType.INQUEUE_CANDIDATES) {
            if (runBean.getAction() != null && runBean.getAction().equals(RunBean.FINISH_REMAINDER) && run.isRedundancy()) {
                HibernateUtil.createTransaction();
                try {
                    RenoService.finishRemainderRedundancy(runBean.getRun());
                    HibernateUtil.flushAndCommitCurrentSession();
                } catch (Exception e) {
                    logger.error("Problem finishing remainder of the reno jobs", e);
                    HibernateUtil.rollbackTransaction();
                }
            }

            if (run.isRedundancy()) {
                runBean.setRunCandidates(renoRepository.getSortedRunCandidates(run, runBean.getComparator(), RunBean.MAX_NUM_OF_RECORDS));
            } else if (run.isNomenclature()) {
                runBean.setRunCandidates(renoRepository.getSortedNonZFRunCandidates(run, runBean.getComparator(), RunBean.MAX_NUM_OF_RECORDS));
            }
        } else if (candidateType == RunPresentation.CandidateType.PENDING_CANDIDATES) {
            runBean.setComparator("name");
            runBean.setRunCandidates(renoRepository.getPendingCandidates(run));
        }
    }

    private void handleOrthologyAttributionUpdate(RunBean form, NomenclatureRun nomenRun) {
        if (nomenRun.getOrthologyPublication() == null
                ||
                false == form.getOrthologyPublicationZdbID().equals(nomenRun.getOrthologyPublication().getZdbID())
                ) {

            Publication attribution = publicationRepository.getPublication(form.getOrthologyPublicationZdbID());
            nomenRun.setOrthologyPublication(attribution);
        }
    }

    private void handleNomenclatureAttributionUpdate(RunBean form, Run run) {
        if (run.getNomenclaturePublication() == null
                || !form.getNomenclaturePublicationZdbID().equals(run.getNomenclaturePublication().getZdbID())
                ) {

            Publication attribution = publicationRepository.getPublication(form.getNomenclaturePublicationZdbID());
            run.setNomenclaturePublication(attribution);
        }
    }

    /**
     * @param form          RunBean that contains form data.
     * @param redundancyRun Run to manipuluate
     */
    private void handleRelationUpdate(RunBean form, RedundancyRun redundancyRun) {
        logger.info("form: " + form);
        logger.info("form.getRelationPublicationZdbID: " + form.getRelationPublicationZdbID());
        if (
                redundancyRun.getRelationPublication() == null
                        ||
                        false == form.getRelationPublicationZdbID().equals(redundancyRun.getRelationPublication().getZdbID())
                ) {
            Publication attribution = publicationRepository.getPublication(form.getRelationPublicationZdbID());
            redundancyRun.setRelationPublication(attribution);
        }
    }

}


