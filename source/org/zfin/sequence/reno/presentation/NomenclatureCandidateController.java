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
import org.zfin.Species;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerFamilyName;
import org.zfin.marker.MarkerRelationship;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RunCandidate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Controller
@RequestMapping(value = "/reno")
public class NomenclatureCandidateController extends AbstractCandidateController {

    private static final Logger logger = Logger.getLogger(NomenclatureCandidateController.class);
    private Validator validator = new NomenclatureCandidateValidator();

    @RequestMapping(value = "/nomenclature-candidate-view/{zdbID}", method = RequestMethod.GET)
    public String referenceData(@PathVariable String zdbID, CandidateBean candidateBean, Model model) {
        candidateBean.createRunCandidateForZdbID(zdbID);
        return handleGet(candidateBean, model);
    }

    @RequestMapping(value = "/nomenclature-candidate-view/{zdbID}", method = RequestMethod.POST)
    public ModelAndView onSubmit(@PathVariable String zdbID, CandidateBean candidateBean, BindingResult errors) throws Exception {
        if (doLock(zdbID, candidateBean)) {
            return new ModelAndView("redirect:/action/reno/nomenclature-candidate-view/" + zdbID);
        }
        if (candidateBean.getAction().equals(CandidateBean.LOCK_RECORD.toString())) {
            if (candidateBean.getRunCandidate() == null) {
                RunCandidate runCandidate = renoRepository.getRunCandidateByID(zdbID);
                candidateBean.setRunCandidate(runCandidate);
            }
            HibernateUtil.createTransaction();
            renoService.handleLock(candidateBean);
            HibernateUtil.flushAndCommitCurrentSession();
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

        //get the run candidate from the bean and use the repository to populate it
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

        NomenclatureRun nomenclatureRun = (NomenclatureRun) rc.getRun();

        //populate fields as necessary..
        candidateBean.setHumanReferenceDatabase(sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENE,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                org.zfin.Species.Type.HUMAN));
        candidateBean.setMouseReferenceDatabase(sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENE,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.MOUSE));

        if (nomenclatureRun.getOrthologyPublication() != null) {
            candidateBean.setOrthologyPublicationZdbID(((NomenclatureRun) rc.getRun()).getOrthologyPublication().getZdbID());
        }
        if (nomenclatureRun.getNomenclaturePublication() != null) {
            candidateBean.setNomenclaturePublicationZdbID(rc.getRun().getNomenclaturePublication().getZdbID());
        }

        handleNote(candidateBean);

        Person currentUser = ProfileService.getCurrentSecurityUser();

        if (rc.getLockPerson() != null && !currentUser.equals(rc.getLockPerson()))
            logger.debug(" Person records are not equal.. ");

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
        logger.info("handleRunCandidate - entry");
        validator.validate(candidateBean, errors);

        RunCandidate rc = candidateBean.getRunCandidate();

        Marker geneToRename = rc.getIdentifiedMarker();
        //kludge to get nomenclature pipeline to work with OTTDARPs which are strangely on Transcripts instead of genes -- blerg.
        logger.info("geneToRename - entry: " + geneToRename.getAbbreviation());
        // Only rename gene if a name and an abbreviation is provided
        String newAbbreviation = candidateBean.getGeneAbbreviation();
        String newGeneName = candidateBean.getGeneName();
        boolean renameGene = !StringUtils.isEmpty(newAbbreviation) && !StringUtils.isEmpty(newGeneName);

        Marker renamedGene = new Marker();
        // The validator ensures that both values are present or none.
        if (geneToRename.getType().equals(Marker.Type.TSCRIPT)) {
            //logger.info("into gene Type: " + gene.getType().toString());
            List<MarkerRelationship> mrelGroup = new ArrayList<>();

            //logger.info("ready for for loop, mrkrType:  " + gene.getType().toString());
            if (!geneToRename.getSecondMarkerRelationships().isEmpty()) {
                for (MarkerRelationship mrel : geneToRename.getSecondMarkerRelationships()) {

                    if (mrel.getType().equals(MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT)) {
                        //logger.debug("get ottdarpGene" + mrel.getFirstMarker().getAbbreviation().toString());
                        mrelGroup.add(mrel);
                    }
                }
                if (mrelGroup.size() > 1) {
                    //  logger.debug("trying to get a gene from a transcript but can't figure out " +
                    //      "which one to grab because there is more than 1");
                    throw new RuntimeException("more than one gene associated with a ottdarp " +
                            "transcript.");
                } else {
                    if (!mrelGroup.isEmpty()) {
                        // logger.debug("mrel group is not empty" + mrelGroup.size());
                        renamedGene = mrelGroup.iterator().next().getFirstMarker();
                    }
                  //  throw new RuntimeException("No related gene found for transcript: "+geneToRename.getZdbID());
                }
            }
        } else {
            renamedGene = geneToRename;
        }
        if (renameGene) {
            String oldGeneSymbol = renamedGene.getAbbreviation();
            renamedGene.setAbbreviation(newAbbreviation);
            String oldGeneName = renamedGene.getName();
            renamedGene.setName(newGeneName);
            renoService.renameGene(renamedGene, candidateBean.getNomenclaturePublicationZdbID(), oldGeneSymbol, oldGeneName);
        }

        //handle gene families
        if (!StringUtils.isEmpty(candidateBean.getGeneFamilyName())) {
            Marker gene = rc.getIdentifiedMarker();
            MarkerFamilyName mf = new MarkerFamilyName();
            mf.setMarkerFamilyName(candidateBean.getGeneFamilyName());
            Set<MarkerFamilyName> families = new HashSet<>();
            families.add(mf);
            gene.setFamilyName(families);
        }
        handleOrthology(candidateBean, renamedGene);
        renoService.moveNoteToGene(rc, renamedGene);

        logger.info("handleRunCandidate - exit");
    }

}
