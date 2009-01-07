package org.zfin.sequence.reno.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.Updates;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.Species;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.reno.*;
import org.zfin.sequence.reno.repository.RenoRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * Class CandidateController.
 */
public abstract class AbstractCandidateController extends SimpleFormController {

    private static Logger LOG = Logger.getLogger(AbstractCandidateController.class);
    protected static RenoRepository rr = RepositoryFactory.getRenoRepository();
    protected static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    protected static OrthologyRepository or = RepositoryFactory.getOrthologyRepository();
    protected static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

    /**
     * Does all the work for referenceData method, handles loading up the runCandidate for viewing,
     * locking, unlocking, note saving and toggling problem status
     *
     * @param candidateBean bean...with data
     */
    public abstract void handleView(CandidateBean candidateBean) ;

    public abstract void handleRunCandidate(CandidateBean candidateBean) ;

    //override POST vs GET distinction and make it explicitly all about the "done" boolean
    protected boolean isFormSubmission(HttpServletRequest request) {
        if (request.getParameter("action") != null)
            return StringUtils.equals(request.getParameter("action"), CandidateBean.DONE);
        else
            return false;
    }


    /*
    * Populate candidateBean, handle note saving and locking
    */
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        CandidateBean candidateBean = (CandidateBean) command;

        //we will eventually return this map, basically just as a holder for the bean
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, candidateBean);
        map.put(LookupStrings.DYNAMIC_TITLE, candidateBean.getRunCandidate().getZdbID());


        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            handleView(candidateBean);

            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return map;
    }


    /**
     * Handle a lock or unlock request.
     * Obtain a lock if:
     * 1) action code is lock
     * 2) if runcandidate is not already locked
     * Unlock if action code is unlock
     *
     * @param candidateBean Candidate Bean
     * @param rc            RunCandidate
     * @param currentUser   Person
     */
    protected void handleLock(CandidateBean candidateBean, RunCandidate rc, Person currentUser) {
        if (StringUtils.equals(candidateBean.getAction(), CandidateBean.LOCK_RECORD)) {
            boolean success = rr.lock(currentUser, rc);
            if (success)
                LOG.info(currentUser.getZdbID() + " is locking " + rc.getZdbID());
            else
                LOG.error("couldn't get lock for " + currentUser.getUsername());

        } else if (StringUtils.equals(candidateBean.getAction(), CandidateBean.UNLOCK_RECORD)) {
            rr.unlock(currentUser, rc);
            LOG.info(currentUser.getZdbID() + " is unlocking " + rc.getZdbID());
        }
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        CandidateBean candidateBean = (CandidateBean) command;


        Session session = HibernateUtil.currentSession();

        String runCandidateID = candidateBean.getRunCandidate().getZdbID();
        RunCandidate rc = rr.getRunCandidateByID(runCandidateID);
        candidateBean.setRunCandidate(rc);

        // don't try to process it, just send it to the end.
        if (candidateBean.isCandidateProblem()) {
            return new ModelAndView(new RedirectView("candidate-inqueue?zdbID=" + rc.getRun().getZdbID()));
        }

        if (rc == null) {
            String message = "No RunCandidate with " + runCandidateID + " found.";
            throw new RuntimeException(message);
        }

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            handleDone(candidateBean);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error(e);
            throw e;
        }

        //redirect to candidate-inqueue
        String url = "/action/reno/candidate-inqueue?zdbID=" + rc.getRun().getZdbID();
        return new ModelAndView(new RedirectView(url), LookupStrings.DYNAMIC_TITLE, rc.getZdbID());
    }


    /**
     * This method works on the orthology information being submitted on the nomenclature page.
     * Each organism can have an accession object (abbreviation and number) and
     * multiple evidence codes.
     * After all orthologous genes are worked on the fast search table is updated accordingly.
     * <p/>
     * Note: Currently, this code is hard-coded to only handle Mouse and Human orthologies.
     *
     * @param candidateBean   Form bean object
     * @param zebrafishMarker Marker
     */
    protected void handleOrthology(CandidateBean candidateBean, Marker zebrafishMarker) {
        LOG.debug("enter handleOrthology");

        RunCandidate rc = candidateBean.getRunCandidate();
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        String zdbID = candidateBean.getOrthologyPublicationZdbID();
        Publication orthologyPub = pr.getPublication(zdbID);
        //first get the human ortholog from the from
        Set<Orthologue> orthologies = new HashSet<Orthologue>();
        String humanAccessionNumber = candidateBean.getHumanOrthologueAbbrev().getEntrezAccession().getEntrezAccNum();
        if (!StringUtils.isEmpty(humanAccessionNumber)) {
            LOG.info(" Working on Human Orthologs ...: ");
            Orthologue humanOrtholog = new Orthologue();
            humanOrtholog.setGene(zebrafishMarker);
            EntrezProtRelation targetHumanAccession = candidateBean.getTargetAccessionHuman(rc, humanAccessionNumber);
            LOG.debug("humanOrthology accession: " + targetHumanAccession);

            humanOrtholog.setAbbreviation(targetHumanAccession.getEntrezAccession().getAbbreviation());
            humanOrtholog.setName(targetHumanAccession.getEntrezAccession().getName());
            humanOrtholog.setAccession(targetHumanAccession);
            humanOrtholog.setOrganism(Species.HUMAN);

            Set<OrthoEvidence> orthoEvidences = RenoService.createEvidenceCollection(candidateBean.getHumanOrthologyEvidence(), orthologyPub);
            humanOrtholog.setEvidence(orthoEvidences);
            LOG.info("Orthology: " + humanOrtholog);
            Updates up = new Updates();
            Date date = new Date();
            Person currentUser = Person.getCurrentSecurityUser();
            up.setRecID(zebrafishMarker.getZdbID());
            up.setFieldName("orthologue");
            up.setNewValue("Human");
            up.setSubmitterID(currentUser.getZdbID());
            up.setSubmitterName(currentUser.getUsername());
            up.setComments("Created a new orthologue record for species=Human for this record.");
            up.setWhenUpdated(date);
            or.saveOrthology(humanOrtholog, orthologyPub, up);
            orthologies.add(humanOrtholog);
            //ir.insertUpdatesTable(zebrafishMarker.getZdbID(),"orthologue","Human","Created a new orthologue record for species=Human for this record.",rc.getLockPerson().getZdbID(),rc.getLockPerson().getName());
        }

        //get the mouse ortholog from the form
        String mouseAccessionNumber = candidateBean.getMouseOrthologueAbbrev().getEntrezAccession().getEntrezAccNum();
        if (!StringUtils.isEmpty(mouseAccessionNumber)) {
            LOG.info(" Working on Mouse Orthologs ...: " + candidateBean.getMouseOrthologueAbbrev());
            Orthologue mouseOrtholog = new Orthologue();
            mouseOrtholog.setGene(zebrafishMarker);
            EntrezProtRelation targetMouseAccession = candidateBean.getTargetAccessionMouse(rc, mouseAccessionNumber);
            mouseOrtholog.setAbbreviation(targetMouseAccession.getEntrezAccession().getAbbreviation());
            mouseOrtholog.setName(targetMouseAccession.getEntrezAccession().getName());
            mouseOrtholog.setAccession(targetMouseAccession);
            mouseOrtholog.setOrganism(Species.MOUSE);
            Set<OrthoEvidence> orthoEvidences = RenoService.createEvidenceCollection(candidateBean.getMouseOrthologyEvidence(), orthologyPub);
            mouseOrtholog.setEvidence(orthoEvidences);
            Updates up = new Updates();
            Date date = new Date();
            Person currentUser = Person.getCurrentSecurityUser();
            up.setRecID(zebrafishMarker.getZdbID());
            up.setFieldName("orthologue");
            up.setNewValue("Mouse");
            up.setSubmitterID(currentUser.getZdbID());
            up.setSubmitterName(currentUser.getUsername());
            up.setComments("Created a new orthologue record for species=Mouse for this record.");
            up.setWhenUpdated(date);
            or.saveOrthology(mouseOrtholog, orthologyPub, up);
            orthologies.add(mouseOrtholog);
            //    ir.insertUpdatesTable(zebrafishMarker.getZdbID(),"orthologue","Mouse","Created a new orthologue record for species=Mouse for this record.",rc.getLockPerson().getZdbID(),rc.getLockPerson().getName());
        }
        LOG.info("Update Fast Search Evidence Codes");
        or.updateFastSearchEvidenceCodes(orthologies);
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


    public void handleDone(CandidateBean candidateBean) {
        //this method will save the note if necessary.
        handleNote(candidateBean);

        handleRunCandidate(candidateBean);

        candidateBean.getRunCandidate().setDone(true);
        candidateBean.getRunCandidate().getCandidate().setLastFinishedDate(new Date());

        LOG.info("handleDone exit");

    }
}


