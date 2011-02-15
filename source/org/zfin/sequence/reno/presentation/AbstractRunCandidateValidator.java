package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.RunCandidate;

import java.util.Set;


/**
 * Class CandidateBeanValidator.
 * controllers.
 */
public abstract class AbstractRunCandidateValidator implements Validator {

    private static Logger logger = Logger.getLogger(AbstractRunCandidateValidator.class);

    public boolean supports(Class clazz) {
        return clazz.equals(CandidateBean.class);
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
                //reject on any marker that isn't a small segment or genedom, at least until
                //we know how to handle them
                if (!m.isInTypeGroup(Marker.TypeGroup.SMALLSEG)
                        && !m.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                    errors.rejectValue("action", "code", "A query in this candidate is associated with a marker that is not a small segment, gene or pseudogene.");
                }

                if (m.isInTypeGroup(Marker.TypeGroup.SMALLSEG)) {
//                    accessionGene = MarkerService.getRelatedGeneFromClone(m);
                    Set<Marker> accessionGenes = MarkerService.getRelatedSmallSegmentGenesFromClone(m);
                    for(Marker accessionGene: accessionGenes){
                        validateAccessionGene(accessionGene,candidateGene, errors) ;
                    }

                } else {
//                    accessionGene = m;
                    validateAccessionGene(m,candidateGene,errors);
                }

            }
        }
        return candidateGene;
    }

    protected void validateAccessionGene(Marker accessionGene,Marker candidateGene,Errors errors){
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