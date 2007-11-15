package org.zfin.sequence.reno.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerFamilyName;
import org.zfin.marker.MarkerService;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.Species;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.repository.RenoRepository;

import java.util.Set;

/**
 * Class CandidateBeanValidator.
 * ToDo: Put some of the validation into a central place so it can be reused in other
 * controllers.
 */
public class CandidateBeanValidator implements Validator {

    private static Logger LOG = Logger.getLogger(CandidateBeanValidator.class);
    private RenoRepository rr = RepositoryFactory.getRenoRepository();
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    public boolean supports(Class clazz) {
        return clazz.equals(CandidateBean.class);
    }

    public void validate(Object command, Errors errors) {

        CandidateBean candidateBean = (CandidateBean) command;
        candidateBean.setRunCandidate(rr.getRunCandidateByID(candidateBean.getRunCandidate().getZdbID()));

        //if there is an accession associated with a gene, it will go here.
        Marker candidateGene = null;

        LOG.info("starting validation");

        //if they didn't select ignore, we're going to do real work, and
        //we need to be sure that the RunCandidate is in a state that the controller
        //can work with.   (more documentation below)
        if (StringUtils.equals(candidateBean.getAssociatedGeneField(), CandidateBean.IGNORE)
                || (candidateBean.getAssociatedGeneField() == null))
            candidateGene = validateRunCandidate(candidateBean, errors);


        if (candidateBean.getRunCandidate().getRun().isRedundancy())
            validateRedundancy(candidateBean, candidateGene, errors);
        else
            validateNomenclature(candidateBean, candidateGene, errors);


    }


    protected void validateRedundancy(CandidateBean candidateBean, Marker candidateGene, Errors errors) {

        LOG.info("Validating redundancy pipeline submission");

        //check that the pubs aren't null
        RedundancyRun run = (RedundancyRun) candidateBean.getRunCandidate().getRun();
        validateRunPublications(run.getNomenclaturePublication(), run.getRelationPublication(), errors);

        //if novel is selected and no zdb_id was specified... (ie, they really mean novel)
        String suggestedName = candidateBean.getRunCandidate().getCandidate().getSuggestedName();
        if (StringUtils.equals(candidateBean.getAssociatedGeneField(), CandidateBean.NOVEL)
                && StringUtils.isEmpty(candidateBean.getGeneZdbID())) {

            //reject if a gene was associated directly to an accession
            if (candidateGene != null) {
                errors.rejectValue("associatedGeneField", "code", "The query accession has a direct link to "
                        + candidateGene.getAbbreviation() + ", so creating a novel gene would cause the accession to be " +
                        "linked to both genes.");
            }

            //make sure it isn't going to try to create an existing marker..
            Marker m = mr.getMarkerByAbbreviation(suggestedName);
            //if we got a marker back, that name is in use, and novel is a bad choice..
            if (m != null) {
                errors.rejectValue("associatedGeneField", "code", suggestedName + " already exists in ZFIN");
            }

            //if the suggested name is empty, complain that a novel gene can't be created without a name,
            // and suggest that they make a new gene elsewhere and associate it by zdb_id.


            if (StringUtils.isEmpty(suggestedName)) {
                errors.rejectValue("associatedGeneField", "code", "There is no suggested name to use for " +
                        "creating a novel gene.  Create a new gene record via the ZFIN home page, then " +
                        "paste that ZFIN gene ID in the 'ZDB' box below.");

            }

        }

        //if they picked a gene, either by zdb_id or pulldown, we'll put it here
        Marker existingGene = null;

        //check that the entered zdb_id maps to a marker and that the marker is a gene
        if (!StringUtils.isEmpty(candidateBean.getGeneZdbID())) {
            Marker m = mr.getMarkerByID(candidateBean.getGeneZdbID());
            if (m == null) {
                errors.rejectValue("geneZdbID", "code", candidateBean.getGeneZdbID() + " is not a valid marker id in ZFIN.");
            } else if (!m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                errors.rejectValue("geneZdbID", "code", "marker id matched a non-genedom marker");
            } else {
                existingGene = m;
            }
        } else {
            //if they picked a gene in the pulldown, get it..
            Marker geneFromPulldown = mr.getMarkerByID(candidateBean.getAssociatedGeneField());
            if (geneFromPulldown != null) {
                existingGene = geneFromPulldown;
            }
        }

        //if there is a gene associated with one of the query accessions, make sure it's the same
        //gene they chose - display the error down by the submit button..
        if (candidateGene != null
                && existingGene != null
                && !candidateGene.equals(existingGene)) {
            errors.rejectValue("action", "code", candidateGene.getAbbreviation() + "is already associated with the query accession.");
        }

        //complain about rename if novel is selected
        if (candidateBean.getAssociatedGeneField().equals(CandidateBean.NOVEL)
                && candidateBean.isRename()) {
            errors.rejectValue("geneAbbreviation", "code", "Can't rename a novel gene");
        }
        if (candidateBean.isRename()){
                if (candidateBean.getGeneAbbreviation().equals(suggestedName)) {
                 errors.rejectValue("geneAbbreviation", "code", "Cannot rename a gene to an exisiting gene name.");
            }
        }
    }

    //todo: do we need to deal with genes directly associated with accessions for nomenclature?  or is that how it always happens?
    protected void validateNomenclature(CandidateBean candidateBean, Marker candidateGene, Errors errors) {

        LOG.info("Validating nomenclature pipeline submission");

        // nomenclature publication
        PublicationValidator.validatePublicationID(
                candidateBean.getNomenclaturePublicationZdbID(), RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID, errors);
        // Orthology publication
        PublicationValidator.validatePublicationID(
                candidateBean.getOrthologyPublicationZdbID(), RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID, errors);

        if (!StringUtils.isEmpty(candidateBean.getMouseOrthologueAbbrev().getEntrezAccession().getEntrezAccNum())) {
            Set<OrthoEvidence.Code> mouseEvidence = candidateBean.getMouseOrthologyEvidence();
            if (CollectionUtils.isEmpty(mouseEvidence)) {
                errors.rejectValue(CandidateBean.MOUSE_ORTHOLOGY_EVIDENCE, "code", "At least one Mouse evidence code needs to be selected");
            }
        }

        if (!StringUtils.isEmpty(candidateBean.getHumanOrthologueAbbrev().getEntrezAccession().getEntrezAccNum())) {
            Set<OrthoEvidence.Code> humanEvidence = candidateBean.getHumanOrthologyEvidence();
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
            // toDo: need to add more validation
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

        //if orthology is submitted and the gene already has an orthologue for that species
//if orthology is submitted and the gene already has an orthologue for that species
        if (!StringUtils.isEmpty(candidateBean.getHumanOrthologueAbbrev().getEntrezAccession().getEntrezAccNum())
                || !StringUtils.isEmpty(candidateBean.getMouseOrthologueAbbrev().getEntrezAccession().getEntrezAccNum())) {
            RunCandidate rc = candidateBean.getRunCandidate();

            Marker m = rc.getIdentifiedMarker();

            // if(true) throw new RuntimeException("method Candidate.getIdentifiedMarker not supported") ;
            for (Orthologue o : m.getOrthologues()) {
                if (o.getOrganism() == Species.HUMAN
                        && !StringUtils.isEmpty(candidateBean.getHumanOrthologueAbbrev().getEntrezAccession().getEntrezAccNum()))
                    errors.rejectValue("humanOrthologueAbbrev.proteinAccNum", "code", m.getAbbreviation() + " already has a human orthologue.");
                if (o.getOrganism() == Species.MOUSE
                        && !StringUtils.isEmpty(candidateBean.getMouseOrthologueAbbrev().getEntrezAccession().getEntrezAccNum()))
                    errors.rejectValue("mouseOrthologueAbbrev.proteinAccNum", "code", m.getAbbreviation() + " already has a mouse orthologue.");

            }

        }

    }

    protected void validateRunPublications(Publication nomenclaturePub, Publication relationshipPublication, Errors errors) {
        if (nomenclaturePub == null || relationshipPublication == null)
            errors.rejectValue("action", "code", "Both run publications (nomenclature & link) need to be set before you can close a candidate. " +
                    "You can open the run page in a new window, set the values, and then hit 'done' here.");
    }

    /**
     * A RunCandidate could come into CandidateController in a state relative to the rest
     * of the database that the controller isn't equipped to handle.  These situations
     * aren't based on user input, but the curator will either be able to resolve them,
     * or select ignore.
     * <p/>
     * (that means that if they selected ignore, this function shouldn't even
     * get called)
     * <p/>
     * So far, this section is really all about the query accessions.  If, going through
     * dblink (or dblink + marker relationship) two different genes come in, there's
     * nothing the controller can do about it.
     *
     * @param candidateBean bean to validate
     * @param errors        Errors object
     * @return Marker
     */
    protected Marker validateRunCandidate(CandidateBean candidateBean, Errors errors) {

        RunCandidate rc = candidateBean.getRunCandidate();

        Marker candidateGene = null;

        for (Query q : rc.getCandidateQueries()) {
            for (Marker m : q.getAccession().getBlastableMarkers()) {
                Marker accessionGene;
                //reject on any marker that isn't a small segment or genedom, at least until
                //we know how to handle them
                if (!m.isInTypeGroup(Marker.TypeGroup.SMALLSEG)
                        && !m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                    errors.rejectValue("action", "code", "A query in this candidate is associated with a marker that is not a small segment, gene or pseudogene.");
                }

                if (m.isInTypeGroup(Marker.TypeGroup.SMALLSEG)) {
                    accessionGene = MarkerService.getRelatedGeneFromClone(m);
                } else {
                    accessionGene = m;
                }

                //if this accession hit a gene
                if (accessionGene != null) {
                    //it's the first gene we've hit, hopefully it'll be the only one
                    if (candidateGene == null) {
                        candidateGene = accessionGene;
                    }
                    //uh oh, it's not the first gene, and it's a different gene than the last one
                    else if (!candidateGene.equals(accessionGene)) {
                        errors.rejectValue("action", "code", "The query accessions are associated with more than one gene.  " +
                                "Either merge the genes and come back, or select ignore.");
                    }
                }

            }
        }

        return candidateGene;


    }
}


