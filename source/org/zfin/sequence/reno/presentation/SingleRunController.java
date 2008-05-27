package org.zfin.sequence.reno.presentation;

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
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * Class SingleRunController.
 */

public class SingleRunController extends SimpleFormController {

    private RenoRepository renoRepository;  // set in spring configuration
    private PublicationRepository publicationRepository; // set in spring configuration
    private String candidateType; // set in spring configuration bean, either "inqueue" or "pending"
    private final Logger LOG = Logger.getLogger(SingleRunController.class);

    public final static String INQUEUE_CANDIDATES = "inqueue";
    public final static String PENDING_CANDIDATES = "pending";

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {

        RunBean form = (RunBean) command;

        String runZdbId = form.getZdbID();
        Run run = renoRepository.getRunByID(runZdbId);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, form);

        if(run!=null){
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

            handleNomenclatureAttributionUpdate(form,run) ;

            if (run.isNomenclature()) {
                handleOrthologyAttributionUpdate(form, run);
            }
            else
            if (run.isRedundancy()) {
                handleRelationUpdate(form, run);
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
     */
    private void setFormData(HttpServletRequest request, RunBean form, Run run) {

        form.setRun(run);
        form.setNomenclaturePublicationZdbID(run.getNomenclaturePublication().getZdbID());

        if (run.isNomenclature()) {
            // we've seen orthologyAttribution being null in db
            // thus check it to avoid null-pointer error
            NomenclatureRun nomenRun = (NomenclatureRun) run ;
            if ( nomenRun.getOrthologyPublication() == null)   {
                form.setOrthologyPublicationZdbID(null);
            }
            else{
                form.setOrthologyPublicationZdbID(nomenRun.getOrthologyPublication().getZdbID());
            }
        }
        else
        if (run.isRedundancy()) {
            RedundancyRun redunRun = (RedundancyRun) run ;

            if (redunRun.getRelationPublication() == null)   {
                form.setRelationPublicationZdbID(null);
            }
            else{
                form.setRelationPublicationZdbID(redunRun.getRelationPublication().getZdbID());
            }

        }


        if (candidateType.equals(INQUEUE_CANDIDATES)) {

            if (request.getParameter("comparator") != null){
                form.setComparator(request.getParameter("comparator"));
            }
            else{
                form.setComparator("expectValue");
            }

            if (run.isRedundancy()) {
                form.setRunCandidates(renoRepository.getSortedRunCandidates(run.getZdbID(), form.getComparator(), RunBean.MAX_NUM_OF_RECORDS));
            }
            else
            if (run.isNomenclature()) {
                form.setRunCandidates(renoRepository.getSortedNonZFRunCandidates(run.getZdbID(), form.getComparator(), RunBean.MAX_NUM_OF_RECORDS));
            }
        }
        else
        if (candidateType.equals(PENDING_CANDIDATES)) {
            form.setComparator("name");
            form.setRunCandidates(renoRepository.getPendingCandidates(run));
        }
    }

    private void handleOrthologyAttributionUpdate(RunBean form, Run run) {
        if(!run.isNomenclature()){
            logger.warn("not a nomenclature run: "+run.getZdbID());
            return ;
        }
        NomenclatureRun nomenRun = (NomenclatureRun) run ;
        if (nomenRun.getOrthologyPublication() == null
                ||
                false==form.getOrthologyPublicationZdbID().equals(nomenRun.getOrthologyPublication().getZdbID())
                ) {

            Publication attribution = publicationRepository.getPublication(form.getOrthologyPublicationZdbID());
            nomenRun.setOrthologyPublication(attribution);
        }
    }

    private void handleNomenclatureAttributionUpdate(RunBean form,Run run){
        if (run.getNomenclaturePublication() == null
                || !form.getNomenclaturePublicationZdbID().equals(run.getNomenclaturePublication().getZdbID())
                ) {

            Publication attribution = publicationRepository.getPublication(form.getNomenclaturePublicationZdbID());
            run.setNomenclaturePublication(attribution);
        }
    }

    /**  
     *
     */
    private void handleRelationUpdate(RunBean form, Run run) {
        RedundancyRun redundancyRun = (RedundancyRun) run ;
        LOG.info("form: " + form) ; 
        LOG.info("form.getRelationPublicationZdbID: " + form.getRelationPublicationZdbID()) ; 
        if( 
            redundancyRun.getRelationPublication()==null
             ||
          false==form.getRelationPublicationZdbID().equals(redundancyRun.getRelationPublication().getZdbID())
                ){
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


