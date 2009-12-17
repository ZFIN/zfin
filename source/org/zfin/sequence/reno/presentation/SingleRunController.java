package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.RenoService;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.repository.RenoRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Class SingleRunController.
 */

public class SingleRunController extends SimpleFormController {

    private RenoRepository renoRepository;  // set in spring configuration
    private PublicationRepository publicationRepository; // set in spring configuration
    private String candidateType; // set in spring configuration bean, either "inqueue" or "pending"
    private final Logger LOG = Logger.getLogger(SingleRunController.class);

    private final static String INQUEUE_CANDIDATES = "inqueue";
    private final static String PENDING_CANDIDATES = "pending";

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {

        RunBean form = (RunBean) command;

        String runZdbId = form.getZdbID();
        Run run = renoRepository.getRunByID(runZdbId);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, form);

        if (run != null) {
            setFormData(request, form, run);
            map.put(LookupStrings.DYNAMIC_TITLE, run.getName());
        } else {
            map.put(LookupStrings.DYNAMIC_TITLE, runZdbId);
        }


        return map;

    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        RunBean form = (RunBean) command;

        String runZdbId = form.getZdbID();
        Run run = renoRepository.getRunByID(runZdbId);

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;

        try {


            tx = session.beginTransaction();

            handleNomenclatureAttributionUpdate(form, run);

            if (run.isNomenclature()) {
                handleOrthologyAttributionUpdate(form, (NomenclatureRun) run);
            } else if (run.isRedundancy()) {
                handleRelationUpdate(form, (RedundancyRun) run);
            }


            tx.commit();
        }
        catch (RuntimeException e) {
            tx.rollback();
            LOG.error("Error in onSubmit method ", e);
            throw e;
        }

        setFormData(request, form, run);

        ModelAndView modelAndView = new ModelAndView(getSuccessView(), LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, run.getName());
        return modelAndView;
    }

    /**
     * Use the parameter in the http request to sort the run candidates and
     * save the ordered list to the form bean.
     *
     * @param request HttpRequest
     * @param runBean RunBean
     * @param run     Actual Run
     */
    private void setFormData(HttpServletRequest request, RunBean runBean, Run run) {

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


        if (candidateType.equals(INQUEUE_CANDIDATES)) {
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

            if (request.getParameter("comparator") != null) {
                runBean.setComparator(request.getParameter("comparator"));
            } else {
                runBean.setComparator("expectValue");
            }

            if (run.isRedundancy()) {
                runBean.setRunCandidates(renoRepository.getSortedRunCandidates(run, runBean.getComparator(), RunBean.MAX_NUM_OF_RECORDS));
            } else if (run.isNomenclature()) {
                runBean.setRunCandidates(renoRepository.getSortedNonZFRunCandidates(run, runBean.getComparator(), RunBean.MAX_NUM_OF_RECORDS));
            }
        } else if (candidateType.equals(PENDING_CANDIDATES)) {
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
        LOG.info("form: " + form);
        LOG.info("form.getRelationPublicationZdbID: " + form.getRelationPublicationZdbID());
        if (
                redundancyRun.getRelationPublication() == null
                        ||
                        false == form.getRelationPublicationZdbID().equals(redundancyRun.getRelationPublication().getZdbID())
                ) {
            Publication attribution = publicationRepository.getPublication(form.getRelationPublicationZdbID());
            redundancyRun.setRelationPublication(attribution);
        }
    }

    public void setCandidateType(String candidateType) {
        this.candidateType = candidateType;
    }

    public void setRenoRepository(RenoRepository renoRepository) {
        this.renoRepository = renoRepository;
    }

    public void setPublicationRepository(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }
}


