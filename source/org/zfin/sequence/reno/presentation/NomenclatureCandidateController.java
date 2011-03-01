package org.zfin.sequence.reno.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerFamilyName;
import org.zfin.orthology.Species;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.service.RenoService;
import org.zfin.sequence.reno.RunCandidate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Class CandidateController.
 */
public class NomenclatureCandidateController extends AbstractCandidateController {

    private static final Logger LOG = Logger.getLogger(NomenclatureCandidateController.class);

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
        RenoService.populateLinkageGroups(rc);

        candidateBean.setRunCandidate(rc);

        NomenclatureRun nomenclatureRun = (NomenclatureRun) rc.getRun();

//        LOG.debug("instance of NomenclatureRun: " + (nomenclatureRun instanceof NomenclatureRun));
//        LOG.debug("Run.isNomenclature: " + nomenclatureRun.isNomenclature());

        //populate fields as necessary..
        candidateBean.setHumanReferenceDatabase(RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.ENTREZ_GENE,
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.HUMAN));
        candidateBean.setMouseReferenceDatabase(RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.ENTREZ_GENE,
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.MOUSE));

        if (nomenclatureRun.getOrthologyPublication() != null) {
            candidateBean.setOrthologyPublicationZdbID(((NomenclatureRun) rc.getRun()).getOrthologyPublication().getZdbID());
        }
        if (nomenclatureRun.getNomenclaturePublication() != null) {
            candidateBean.setNomenclaturePublicationZdbID(rc.getRun().getNomenclaturePublication().getZdbID());
        }

        handleNote(candidateBean);

        Person currentUser = Person.getCurrentSecurityUser();

        if (rc.getLockPerson() != null && !currentUser.equals(rc.getLockPerson()))
            LOG.debug(" Person records are not equal.. ");

        LOG.debug("action: " + candidateBean.getAction());

        // check that this candidate is not already related to any of the
        // associated markers
        List<Marker> associatedMarkers = RenoService.checkForExistingRelationships(candidateBean, rc);
        candidateBean.setAllSingleAssociatedGenesFromQueries(associatedMarkers);

        //handle locking & unlocking
        RenoService.handleLock(candidateBean);

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


    public void handleRunCandidate(CandidateBean candidateBean) {
        LOG.info("handleRunCandidate - entry");

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
            RenoService.renameGene(geneToRename, candidateBean.getNomenclaturePublicationZdbID());
        }

        //handle gene families
        if (!StringUtils.isEmpty(candidateBean.getGeneFamilyName())) {
            Marker gene = rc.getIdentifiedMarker();
            MarkerFamilyName mf = new MarkerFamilyName();
            mf.setMarkerFamilyName(candidateBean.getGeneFamilyName());
            Set<MarkerFamilyName> families = new HashSet<MarkerFamilyName>();
            families.add(mf);
            gene.setFamilyName(families);
        }
        handleOrthology(candidateBean, geneToRename);
        RenoService.moveNoteToGene(rc, geneToRename);

        LOG.info("handleRunCandidate - exit");
    }

}
