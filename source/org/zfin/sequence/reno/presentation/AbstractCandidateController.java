package org.zfin.sequence.reno.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.orthology.service.OrthologService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.reno.service.RenoService;
import org.zfin.sequence.repository.SequenceRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getOrthologyRepository;


/**
 * Class CandidateController.
 */
public abstract class AbstractCandidateController {

    private static Logger LOG = LogManager.getLogger(AbstractCandidateController.class);
    protected MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    protected OrthologyRepository or = getOrthologyRepository();
    protected InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    protected SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();


    @Autowired
    protected RenoService renoService;

    @Autowired
    protected RenoRepository renoRepository;

    @Autowired
    protected OrthologService orthologService;

    /**
     * Does all the work for referenceData method, handles loading up the runCandidate for viewing,
     * locking, unlocking, note saving and toggling problem status
     *
     * @param candidateBean bean...with data
     */
    public abstract void handleView(CandidateBean candidateBean);

    public abstract void handleRunCandidate(CandidateBean candidateBean, BindingResult errors);

    //override POST vs GET distinction and make it explicitly all about the "done" boolean

    protected boolean isFormSubmission(HttpServletRequest request) {
        return request.getParameter("action") != null &&
                StringUtils.equals(request.getParameter("action"), CandidateBean.DONE);
    }


    /*
    * Populate candidateBean, handle note saving and locking
    */
    public String handleGet(CandidateBean candidateBean, Model model) {
        //we will eventually return this map, basically just as a holder for the bean
        model.addAttribute(LookupStrings.FORM_BEAN, candidateBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, candidateBean.getRunCandidate().getZdbID());

        // transaction is necessary to handle locks placed from within the view
        HibernateUtil.createTransaction();
        handleView(candidateBean);
        HibernateUtil.flushAndCommitCurrentSession();
        return "reno/candidate-view.page";
    }


    protected ModelAndView handleSubmit(CandidateBean candidateBean, BindingResult errors) throws Exception {

        String runCandidateID = candidateBean.getRunCandidate().getZdbID();
        RunCandidate rc = renoRepository.getRunCandidateByID(runCandidateID);
        candidateBean.setRunCandidate(rc);

        // don't try to process it, just send it to the end.
        if (candidateBean.isCandidateProblem()) {
            return new ModelAndView(new RedirectView("/action/reno/candidate/inqueue/" + rc.getRun().getZdbID()));
        }

        if (rc == null) {
            String message = "No RunCandidate with " + runCandidateID + " found.";
            throw new RuntimeException(message);
        }

        try {
            HibernateUtil.createTransaction();
            handleDone(candidateBean, errors);
            if (errors.hasErrors()) {
                HibernateUtil.rollbackTransaction();
                ModelAndView modelAndView = new ModelAndView("reno/candidate-view.page");
                modelAndView.addObject(LookupStrings.FORM_BEAN, candidateBean);
                modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, candidateBean.getRunCandidate().getZdbID());
                return modelAndView;
            } else {
                HibernateUtil.flushAndCommitCurrentSession();
            }
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error(e);
            throw e;
        }

        //redirect to candidate-inqueue
        String url = "/action/reno/candidate/inqueue/" + rc.getRun().getZdbID();
        return new ModelAndView(new RedirectView(url), LookupStrings.DYNAMIC_TITLE, rc.getZdbID());
    }


    /**
     * This method works on the orthology information being submitted on the nomenclature page.
     * Each organism can have an accession object (abbreviation and number) and
     * multiple evidence codes.
     * After all orthologs are worked on the fast search table is updated accordingly.
     * <p/>
     * Note: Currently, this code is hard-coded to only handle Mouse and Human orthologies.
     *
     * @param candidateBean   Form bean object
     * @param zebrafishMarker Marker
     */
    protected void handleOrthology(CandidateBean candidateBean, Marker zebrafishMarker) {
        LOG.debug("enter handleOrthology");

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        String zdbID = candidateBean.getOrthologyPublicationZdbID();
        Publication orthologyPub = pr.getPublication(zdbID);
        //first get the human ortholog from the from
        String humanAccessionNumber = candidateBean.getHumanOrthologAbbrev().getEntrezAccession().getEntrezAccNum();
        if (!StringUtils.isEmpty(humanAccessionNumber)) {
            LOG.info(" Working on Human Orthologs ...: ");
            NcbiOtherSpeciesGene ncbiGene = getOrthologyRepository().getNcbiGene(humanAccessionNumber);
            if (ncbiGene == null)
                throw new NullPointerException("Could not find an NCBI Gene record with accession number " + humanAccessionNumber);

            Ortholog humanOrtholog = orthologService.createOrthologEntity(zebrafishMarker, ncbiGene);
            Set<OrthologEvidence> orthoEvidences = renoService.createEvidenceCollection(candidateBean.getHumanOrthologyEvidence(), orthologyPub, humanOrtholog);
            humanOrtholog.setEvidenceSet(orthoEvidences);

            LOG.info("Orthology: " + humanOrtholog);
            or.saveOrthology(humanOrtholog, null);
            //orthologService.createReferences(humanOrtholog, ncbiGene);

        }

        //get the mouse ortholog from the form
        String mouseAccessionNumber = candidateBean.getMouseOrthologAbbrev().getEntrezAccession().getEntrezAccNum();
        if (!StringUtils.isEmpty(mouseAccessionNumber)) {
            LOG.info(" Working on Mouse Orthologs ...: " + candidateBean.getMouseOrthologAbbrev());
            NcbiOtherSpeciesGene ncbiGene = getOrthologyRepository().getNcbiGene(mouseAccessionNumber);
            if (ncbiGene == null)
                throw new NullPointerException("Could not find an NCBI Gene record with accession number " + mouseAccessionNumber);

            Ortholog mouseOrtholog = orthologService.createOrthologEntity(zebrafishMarker, ncbiGene);
            Set<OrthologEvidence> orthoEvidences = renoService.createEvidenceCollection(candidateBean.getMouseOrthologyEvidence(), orthologyPub, mouseOrtholog);
            mouseOrtholog.setEvidenceSet(orthoEvidences);
            or.saveOrthology(mouseOrtholog, orthologyPub);
        }
        LOG.debug("exit handleOrthology");
    }


    protected void handleNote(CandidateBean candidateBean) {
        LOG.debug("enter handleNote");

        RunCandidate rc = candidateBean.getRunCandidate();

        if (StringUtils.equals(candidateBean.getAction(), CandidateBean.SAVE_NOTE)
                || StringUtils.equals(candidateBean.getAction(), CandidateBean.DONE)
                || StringUtils.equals(candidateBean.getAction(), CandidateBean.SET_PROBLEM)
                ) {

            LOG.debug("candidatebean action saves note or is done");

            //LOG.debug(getCurrentUser().getZdbID()
            //        + " is saving a note on "
            //       + candidateBean.getRunCandidate().getZdbID());

            //Don't save empty strings, save them as null instead.
            if (StringUtils.isEmpty(candidateBean.getCandidateNote())) {
                LOG.debug("candidatebean action is done or save_note but no note");
                rc.getCandidate().setNote(null);
            } else {
                LOG.debug("the candidateBean.getCandidateNote is not null");
                rc.getCandidate().setNote(candidateBean.getCandidateNote());
            }
        }
        //whether the note came in or not, copy the note from the runCandidate
        //to candidateBean.candidateNote, since that's what the textarea is bound to
        LOG.debug("candidatebean sets candidateNote to rc.candidate.note");
        candidateBean.setCandidateNote(rc.getCandidate().getNote());
        LOG.debug("candidateNote is: " + rc.getCandidate().getNote());
        LOG.debug("exit handleNote");
    }


    public void handleDone(CandidateBean candidateBean, BindingResult errors) {
        //this method will save the note if necessary.

        if (candidateBean.getAction().equals(CandidateBean.UNLOCK_RECORD)) {
            renoService.handleLock(candidateBean);
            return;
        }

        handleNote(candidateBean);
        handleRunCandidate(candidateBean, errors);

        candidateBean.getRunCandidate().setDone(true);
        candidateBean.getRunCandidate().getCandidate().setLastFinishedDate(new Date());

        LOG.info("handleDone exit");

    }

    protected boolean doLock(String zdbID, CandidateBean candidateBean) {
        if (candidateBean.getAction().equals(CandidateBean.LOCK_RECORD)) {
            if (candidateBean.getRunCandidate() == null) {
                RunCandidate runCandidate = RepositoryFactory.getRenoRepository().getRunCandidateByID(zdbID);
                candidateBean.setRunCandidate(runCandidate);
            }
            HibernateUtil.createTransaction();
            renoService.handleLock(candidateBean);
            HibernateUtil.flushAndCommitCurrentSession();
            return true;
        }
        return false;
    }

    public void setRenoService(RenoService renoService) {
        this.renoService = renoService;
    }

    public void setRenoRepository(RenoRepository renoRepository) {
        this.renoRepository = renoRepository;
    }
}


