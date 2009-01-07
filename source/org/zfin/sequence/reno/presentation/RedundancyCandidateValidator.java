package org.zfin.sequence.reno.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
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
public class RedundancyCandidateValidator extends AbstractRunCandidateValidator {

    private static Logger LOG = Logger.getLogger(RedundancyCandidateValidator.class);
    private RenoRepository rr = RepositoryFactory.getRenoRepository();
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    public void validate(Object command, Errors errors) {

        CandidateBean candidateBean = (CandidateBean) command;
        candidateBean.setRunCandidate(rr.getRunCandidateByID(candidateBean.getRunCandidate().getZdbID()));

        //if there is an accession associated with a gene, it will go here.
        Marker candidateGene = null;

        LOG.debug("starting validation");

        //if they didn't select ignore, we're going to do real work, and
        //we need to be sure that the RunCandidate is in a state that the controller
        //can work with.   (more documentation below)
        if (StringUtils.equals(candidateBean.getAssociatedGeneField(), CandidateBean.IGNORE)
                || (candidateBean.getAssociatedGeneField() == null)){
            candidateGene = validateRunCandidate(candidateBean, errors);
        }

        validateRedundancy(candidateBean, candidateGene, errors);


    }


    protected void validateRedundancy(CandidateBean candidateBean, Marker candidateGene, Errors errors) {

        LOG.info("Validating redundancy pipeline submission");

        RunCandidate runCandidate =  candidateBean.getRunCandidate();
        // If it is null, but should have associated markers, then we have a serious problem.
        if(runCandidate.getIdentifiedMarker()==null  && runCandidate.getRun().hasAssociatedMarkers()==true){
            Set<Query> queries = runCandidate.getCandidateQueries() ;
            // just grab the first query and report the error if there is one query
            Object[] args = new Object[1] ;
            args[0] = queries.iterator().next().getAccession().getNumber() ;
            errors.rejectValue("action","code note used",args,"Can not resolve because accession {0} has no corresponding zfin gene." +
                    "  Either manually fix or wait for GenBank to fix and to be subsequently loaded."); ;
        }



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
            //if we got a marker back, that name is in use, and novel is a bad choice..
            if (mr.getMarkerByAbbreviation(suggestedName) != null) {
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
    }




}