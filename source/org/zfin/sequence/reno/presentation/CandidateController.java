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
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.infrastructure.Updates;
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
import org.zfin.sequence.Accession;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.repository.RenoRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * Class CandidateController.
 */
public class CandidateController extends SimpleFormController {

    private static Logger LOG = Logger.getLogger(CandidateController.class);
    private static RenoRepository rr = RepositoryFactory.getRenoRepository();
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static OrthologyRepository or = RepositoryFactory.getOrthologyRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();


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
        }

        return map;
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
        RunCandidate rc = rr.getRunCandidateByID(runCandidateID);

        if (rc == null) {
            //the id was bad, return now, the jsp will handle the error
            String errorMessage = "Could not find a RunCandidate object with id " + runCandidateID;
            NullPointerException exception = new NullPointerException(errorMessage);
            LOG.error(errorMessage, exception);
            throw exception;
        }
        populateLinkageGroups(rc);

        candidateBean.setRunCandidate(rc);

        Run run = rc.getRun();

        LOG.debug("instance of RedundancyRun: " + (run instanceof RedundancyRun));
        LOG.debug("instance of NomenclatureRun: " + (run instanceof NomenclatureRun));
        LOG.debug("Run.isRedundancy: " + run.isRedundancy());
        LOG.debug("Run.isNomenclature: " + run.isNomenclature());

        //populate fields as necessary..

        if (run.isNomenclature()) {
            if (((NomenclatureRun) rc.getRun()).getOrthologyPublication() != null)
                candidateBean.setOrthologyPublicationZdbID(((NomenclatureRun) rc.getRun()).getOrthologyPublication().getZdbID());
            if (rc.getRun().getNomenclaturePublication() != null)
                candidateBean.setNomenclaturePublicationZdbID(rc.getRun().getNomenclaturePublication().getZdbID());
        }

        if (run.isRedundancy()) {
            candidateBean.setGeneAbbreviation(rc.getCandidate().getSuggestedName());
        }

        handleNote(candidateBean);

        Person currentUser = Person.getCurrentSecurityUser();

        if (rc.getLockPerson() != null && !currentUser.equals(rc.getLockPerson()))
            LOG.debug(" Person records are not equal.. ");

        LOG.debug("action: " + candidateBean.getAction());

        // check that this candidate is not already related to any of the
        // associated markers 
        List<Marker> associatedMarkers = checkForExistingRelationships(candidateBean, rc);
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

    private List<Marker> checkForExistingRelationships(CandidateBean candidateBean, RunCandidate rc) {
        List<Marker> associatedMarkers = rc.getAllSingleAssociatedGenesFromQueries();
        List<Marker> identifiedMarkers = rc.getIdentifiedMarkers();
        List<Marker> smallSegments = getSmallSegementClones(identifiedMarkers);
        candidateBean.setSmallSegments(smallSegments);

        if (associatedMarkers != null) {
            for (Marker associatedMarker : associatedMarkers) {
                MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
                for (Marker smallSegment : smallSegments) {
                    boolean hasRelationship = markerRepository.hasSmallSegmentRelationship(associatedMarker, smallSegment);
                    if(hasRelationship){
                        candidateBean.addMessage("This candidate already has a small-segment relationship to " +
                        associatedMarker.getAbbreviation());
                    }
                }
            }
        }
        return associatedMarkers;
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
    private void handleLock(CandidateBean candidateBean, RunCandidate rc, Person currentUser) {
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

    public void handleDone(CandidateBean candidateBean) {

        //this method will save the note if necessary.
        handleNote(candidateBean);

        if (candidateBean.getRunCandidate().getRun().isNomenclature()) {
            handleNomen(candidateBean);
        } else if (candidateBean.getRunCandidate().getRun().isRedundancy()) {
            LOG.info("Next step is handleRedundancy, so this is a redunCandidate");
            handleRedundancy(candidateBean);

        }

        candidateBean.getRunCandidate().setDone(true);
        candidateBean.getRunCandidate().getCandidate().setLastFinishedDate(new Date());

        LOG.info("handleDone exit");

    }

    public void handleRedundancy(CandidateBean candidateBean) {
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

    public void handleRedundancyNovelGene(CandidateBean candidateBean) {
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
        moveNoteToGene(rc, novelGene);
    }


    public void handleRedundancyExistingGene(CandidateBean candidateBean, Marker existingGene) {
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
            renameGene(existingGene, rc.getRun().getNomenclaturePublication().getZdbID());
        }

        //create data note, copy curator note to data note, set curator note to nu
        moveNoteToGene(rc, existingGene);
        LOG.info("handling an existing gene - exit");
    }

    public void handleNomen(CandidateBean candidateBean) {
        LOG.info("handleNomen - entry");

        RunCandidate rc = candidateBean.getRunCandidate();

        Marker geneToRename = rc.getIdentifiedMarker();

        // Only rename gene if a name and an abbrevation is provided
        String newAbbreviation = candidateBean.getGeneAbbreviation();
        String newGeneName = candidateBean.getGeneName();

        // The validator ensures that both values are present or none.
        if (!StringUtils.isEmpty(newAbbreviation) && !StringUtils.isEmpty(newGeneName)) {
            geneToRename.setAbbreviation(newAbbreviation);
            geneToRename.setName(newGeneName);
//            renameGene(geneToRename, candidateBean.getOrthologyPublicationZdbID());
            renameGene(geneToRename, candidateBean.getNomenclaturePublicationZdbID());
        }

        //handle gene families
        if (!StringUtils.isEmpty(candidateBean.getGeneFamilyName())) {
            Marker gene = rc.getIdentifiedMarker();
            MarkerFamilyName mf = new MarkerFamilyName();
            mf.setMarkerFamilyName(candidateBean.getGeneFamilyName());
            gene.setGeneFamilyName(mf);
        }
        handleOrthology(candidateBean, geneToRename);
        moveNoteToGene(rc, geneToRename);

        LOG.info("handleNomen - exit");
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
    private void handleOrthology(CandidateBean candidateBean, Marker zebrafishMarker) {
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

            Set<OrthoEvidence> orthoEvidences = createEvidenceCollection(candidateBean.getHumanOrthologyEvidence(), orthologyPub);
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
            Set<OrthoEvidence> orthoEvidences = createEvidenceCollection(candidateBean.getMouseOrthologyEvidence(), orthologyPub);
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

    /**
     * Creates a set of Evidence Codes from a set of evidence code strings coming from the submission form.
     *
     * @param formEvidenceCodes Set of evidences
     * @param orthologyPub      publication
     * @return set of OrthoEvidence codes
     */
    private Set<OrthoEvidence> createEvidenceCollection(Set<OrthoEvidence.Code> formEvidenceCodes, Publication orthologyPub) {
        HashSet<OrthoEvidence> OrthoEvidences = new HashSet<OrthoEvidence>();
        for (OrthoEvidence.Code orthoevidence : formEvidenceCodes) {
            OrthoEvidence oe = new OrthoEvidence();
            oe.setOrthologueEvidenceCode(orthoevidence);
            oe.setPublication(orthologyPub);
            OrthoEvidences.add(oe);
        }
        return OrthoEvidences;

    }

    /**
     * Retrieve the linkage groups for all hit-related marker or clones.
     * For hit get the ZFIN marker object and check for the linkage groups.
     * If there are none found and the marker is a clone check the associated gene
     * and its linkage groups.
     *
     * @param rc Runcandidate
     */
    private void populateLinkageGroups(RunCandidate rc) {
        if (rc == null)
            return;

        List<Query> queries = rc.getCandidateQueryList();
        LOG.info("popularLinkageGroups rc: " + rc.getZdbID());
        LOG.info("popularLinkageGroups queries.size: " + queries.size());
        for (Query query : queries) {
            Set<Hit> hits = query.getBlastHits();
            LOG.info("popularLinkageGroups hits.size: " + hits.size());
            for (Hit hit : hits) {
                LOG.info("popularLinkageGroups hit: " + hit.getZdbID());
                LOG.info("popularLinkageGroups hit.getTargetAccession: " + hit.getTargetAccession().getID());
                Set<MarkerDBLink> markerDBLinks = hit.getTargetAccession().getBlastableMarkerDBLinks();
                LOG.info("popularLinkageGroups markerDBLinks.size: " + markerDBLinks.size());
                for (MarkerDBLink markerDBLink : markerDBLinks) {
                    LOG.info("popularLinkageGroups markerDBLink: " + markerDBLink.getZdbID());
                    hit.getTargetAccession().setLinkageGroups(MarkerService.getLinkageGroups(markerDBLink.getMarker()));
                }
            }
        }
    }

    /**
     * Creates marker relationships (or DBLinks) to connect markers (or accessions)
     * to the gene that the curator chose.
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
    public void createRedundancyRelationships(RunCandidate rc, Marker gene) {
        LOG.info("createRelationsips gene: " + gene);
        LOG.info("createRelationsips runCanZdbID: " + rc.getZdbID());

        if (!rc.getRun().isRedundancy()) {
            LOG.info("run should be redundancy");
            return;
        }
        String attributionZdbID = ((RedundancyRun) rc.getRun()).getRelationPublication().getZdbID();

        List<Marker> markers = rc.getIdentifiedMarkers();
        LOG.debug("createRelationships markers.size(): " + markers.size());
        List<Marker> segments = getSmallSegementClones(markers);

        //pull the single gene from the collection, if there is one.
        Marker candidateGene = null;
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                LOG.debug("createRelationships marker type: " + m.getType());
                LOG.debug("createRelationships is in type group genedom");
                candidateGene = m;
                break;
            } else {
                LOG.debug("createRelationships NOT in type group genedom");

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
                mr.addSmallSegmentToGene(segment, gene, attributionZdbID);
            }
            //our query accession(s) was(were) linked to one or more segments, we just made
            //relationships from those segments to the gene that the curator chose
            //now, if there is a dblink from the accession directly to that gene,
            //we delete it.  (unless it's directly attributed to a journal article)
            MarkerService.removeRedundantDBLinks(gene, accessions);

        } else if (candidateGene == null) {
            //no segments & no genes means that we have an accession that
            //has yet to be linked to any marker at all, so we link it to whatever
            //gene the curators chose.
            createRedundancyDBLinks(rc, gene);
        }
    }

    private List<Marker> getSmallSegementClones(List<Marker> markers) {
        List<Marker> segments = new ArrayList<Marker>();

        //pull the ESTs from the candidate
        for (Marker m : markers) {
            if (m.isInTypeGroup(Marker.TypeGroup.SMALLSEG)
                    && !segments.contains(m)) {
                segments.add(m);
            }
        }
        LOG.debug("createRelationships segments.size(): " + segments.size());
        return segments;
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

    private void renameGene(Marker gene, String attributionZdbID) {
        Person currentUser = Person.getCurrentSecurityUser();
        Publication pub = new Publication();
        pub.setZdbID(attributionZdbID);
        mr.renameMarker(gene, pub, MarkerHistory.Reason.RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES);
        ir.insertUpdatesTable(gene, "data_alias", "", currentUser);
//        ir.insertUpdatesTable(geneToRename.getZdbID(),"dalias_alias",geneToRename.getAbbreviation(),"",rc.getLockPerson().getZdbID(),rc.getLockPerson().getName());

    }

    private void handleNote(CandidateBean candidateBean) {
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

    /**
     * Remove note from Candidate, make it a new datanote on a gene.
     *
     * @param rc   a RunCandidate that will lose it's note
     * @param gene a Marker that will gain a note.
     */
    private void moveNoteToGene(RunCandidate rc, Marker gene) {

        LOG.info("enter moveNoteToGene");
        LOG.info("existingGene abbrev: " + gene.getAbbreviation());
        if (!StringUtils.isEmpty(rc.getCandidate().getNote())) {
            LOG.debug("attach a data note to the gene");
            mr.addMarkerDataNote(gene, rc.getCandidate().getNote(), rc.getLockPerson());
            rc.getCandidate().setNote(null);
        }
        LOG.info("exit moveNoteToGene");
    }

}


