package org.zfin.sequence.reno.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.validation.Errors;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerFamilyName;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.repository.RenoRepository;

import java.util.Set;


/**
 * Class CandidateBeanValidator.
 * controllers.
 */
public class NomenclatureCandidateValidator extends AbstractRunCandidateValidator {

    private static final Logger LOG = LogManager.getLogger(NomenclatureCandidateValidator.class);
    private RenoRepository rr = RepositoryFactory.getRenoRepository();
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    public void validate(Object command, Errors errors) {

        CandidateBean candidateBean = (CandidateBean) command;
        candidateBean.setRunCandidate(rr.getRunCandidateByID(candidateBean.getRunCandidate().getZdbID()));

        LOG.debug("starting validation");

        //if they didn't select ignore, we're going to do real work, and
        //we need to be sure that the RunCandidate is in a state that the controller
        //can work with.   (more documentation below)
        if (StringUtils.equals(candidateBean.getAssociatedGeneField(), CandidateBean.IGNORE)
                || (candidateBean.getAssociatedGeneField() == null)) {
            validateRunCandidate(candidateBean, errors);
        }

        validateNomenclature(candidateBean, errors);

    }


    protected void validateNomenclature(CandidateBean candidateBean, Errors errors) {

        LOG.info("Validating nomenclature pipeline submission");

        // nomenclature publication
        PublicationValidator.validatePublicationID(
                candidateBean.getNomenclaturePublicationZdbID(), RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID, errors);
        // Orthology publication
        PublicationValidator.validatePublicationID(
                candidateBean.getOrthologyPublicationZdbID(), RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID, errors);

        if (!StringUtils.isEmpty(candidateBean.getMouseOrthologAbbrev().getEntrezAccession().getEntrezAccNum())) {
            Set<OrthologEvidence.Code> mouseEvidence = candidateBean.getMouseOrthologyEvidence();
            if (CollectionUtils.isEmpty(mouseEvidence)) {
                errors.rejectValue(CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE, "code", "At least one Mouse evidence code needs to be selected");
            }
        }

        if (!StringUtils.isEmpty(candidateBean.getHumanOrthologAbbrev().getEntrezAccession().getEntrezAccNum())) {
            Set<OrthologEvidence.Code> humanEvidence = candidateBean.getHumanOrthologyEvidence();
            if (CollectionUtils.isEmpty(humanEvidence)) {
                errors.rejectValue(CandidateBean.HUMAN_ORTHOLOGY_EVIDENCE, "code", "At least one Human evidence code needs to be selected");
            }
        }

        if (!candidateBean.getOrthologyPublicationZdbID().equals("")) {
            Publication p = pr.getPublication(candidateBean.getOrthologyPublicationZdbID());
            if (p == null) {
                errors.rejectValue(RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID, "code", "not a valid publication id");
            }
        }

        if (!candidateBean.getNomenclaturePublicationZdbID().equals("")) {
            Publication p = pr.getPublication(candidateBean.getNomenclaturePublicationZdbID());
            if (p == null) {
                errors.rejectValue(RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID, "code", "not a valid publication id");
            }
        }

        String newAbbreviation = candidateBean.getGeneAbbreviation();
        String newGeneName = candidateBean.getGeneName();
        // Both fields have to have entries or none
        if ((!StringUtils.isEmpty(newAbbreviation) && StringUtils.isEmpty(newGeneName))) {
            errors.rejectValue(CandidateBean.NEW_GENE_NAME, "code", "You cannot provide a new abbreviation " +
                    "but not a new gene name. Please add a new name.");
        }
        if ((StringUtils.isEmpty(newAbbreviation) && !StringUtils.isEmpty(newGeneName))) {
            errors.rejectValue(CandidateBean.NEW_ABBREVIATION, "code", "You cannot provide a new gene name " +
                    "but not an abbreviation. Please add a new abbreviation.");
        }

        // Check for nomenclature conventions
        if (!StringUtils.isEmpty(newAbbreviation)) {
            // use only lower case abbreviations
            if (!newAbbreviation.equals(newAbbreviation.toLowerCase()))
                errors.rejectValue(CandidateBean.NEW_ABBREVIATION, "code", "Abbreviations have to be" +
                        " lower case according to the nomenclature conventions.");
        }

        //marker family check
        if (!StringUtils.isEmpty(candidateBean.getGeneFamilyName())) {
            MarkerFamilyName familyname = mr.getMarkerFamilyName(candidateBean.getGeneFamilyName());
            if (familyname == null) {
                errors.rejectValue("geneFamilyName", "code", "Not a valid gene family");
            }
        }

        //if orthology is submitted and the gene already has an ortholog for that species
//if orthology is submitted and the gene already has an ortholog for that species
        if (!StringUtils.isEmpty(candidateBean.getHumanOrthologAbbrev().getEntrezAccession().getEntrezAccNum())
                || !StringUtils.isEmpty(candidateBean.getMouseOrthologAbbrev().getEntrezAccession().getEntrezAccNum())) {
            RunCandidate rc = candidateBean.getRunCandidate();

            Marker m = rc.getIdentifiedMarker();

            // if(true) throw new RuntimeException("method Candidate.getIdentifiedMarker not supported") ;
            for (Ortholog o : m.getOrthologs()) {
/* todo
                if (o.getNcbiGene().getOrganism() == Species.Type.HUMAN
                        && !StringUtils.isEmpty(candidateBean.getHumanOrthologAbbrev().getEntrezAccession().getEntrezAccNum()))
                    errors.rejectValue("humanOrthologAbbrev.entrezAccession.entrezAccNum", "code", m.getAbbreviation() + " already has a human ortholog.");
                if (o.getNcbiGene().getOrganism() == Species.Type.MOUSE
                        && !StringUtils.isEmpty(candidateBean.getMouseOrthologAbbrev().getEntrezAccession().getEntrezAccNum()))
                    errors.rejectValue("mouseOrthologAbbrev.entrezAccession.entrezAccNum", "code", m.getAbbreviation() + " already has a mouse ortholog.");
*/

            }

        }

        if (mr.getMarkerByAbbreviation(newAbbreviation) != null) {
            errors.rejectValue(CandidateBean.NEW_ABBREVIATION, "code", "Gene with the abbreviation " + newAbbreviation + " already exists in ZFIN");
        }

        if (mr.getMarkerByName(candidateBean.getGeneName()) != null) {
            errors.rejectValue(CandidateBean.NEW_GENE_NAME, "code", "Gene with the name " + newGeneName + " already exists in ZFIN");
        }

    }

}
