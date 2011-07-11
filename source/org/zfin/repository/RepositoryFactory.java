package org.zfin.repository;

import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.repository.HibernateAnatomyRepository;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.antibody.repository.HibernateAntibodyRepository;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.audit.repository.HibernateAuditLogRepository;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.repository.ExpressionSummaryRepository;
import org.zfin.expression.repository.HibernateExpressionRepository;
import org.zfin.expression.repository.HibernateExpressionSummaryRepository;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.HibernateFeatureRepository;
import org.zfin.gbrowse.repository.GBrowseRepository;
import org.zfin.gbrowse.repository.HibernateGBrowseRepository;
import org.zfin.infrastructure.repository.HibernateInfrastructureRepository;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.repository.HibernateLinkageRepository;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.marker.repository.HibernateMarkerRepository;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.repository.HibernateMutantRepository;
import org.zfin.mutant.repository.HibernatePhenotypeRepository;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.ontology.repository.HibernateMarkerGoTermEvidenceRepository;
import org.zfin.ontology.repository.HibernateOntologyRepository;
import org.zfin.ontology.repository.MarkerGoTermEvidenceRepository;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.orthology.repository.HibernateOrthologyRepository;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.people.repository.HibernateProfileRepository;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.security.repository.HibernateUserRepository;
import org.zfin.security.repository.UserRepository;
import org.zfin.sequence.blast.repository.BlastRepository;
import org.zfin.sequence.blast.repository.HibernateBlastRepository;
import org.zfin.sequence.reno.repository.HibernateRenoRepository;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.repository.DisplayGroupRepository;
import org.zfin.sequence.repository.HibernateDisplayGroupRepository;
import org.zfin.sequence.repository.HibernateSequenceRepository;
import org.zfin.sequence.repository.SequenceRepository;

/**
 * Please provide JavaDoc info!!!
 */
public class RepositoryFactory {

    private static AnatomyRepository anatRep;
    private static AntibodyRepository antibodyRep;
    private static RenoRepository renoRep;
    private static PublicationRepository pubRep;
    private static UserRepository userRep;
    private static AuditLogRepository auditRep;
    private static ProfileRepository profileRep;
    private static MutantRepository mutRep;
    private static FeatureRepository featRep;
    private static ExpressionSummaryRepository xpatsumRep;
    private static MarkerRepository markerRep;
    private static MarkerGoTermEvidenceRepository markerGoTermEvidenceRepository;
    private static InfrastructureRepository infraRep;
    private static SequenceRepository seqRep;
    private static OrthologyRepository orthoRep;
    private static LinkageRepository linkageRep;
    private static BlastRepository blastRepository;
    private static DisplayGroupRepository displayGroupRepository;
    private static ExpressionRepository expressionRep;
    private static GBrowseRepository gbrowseRepository;
    private static PhenotypeRepository phenotypeRep = new HibernatePhenotypeRepository();
    private static OntologyRepository ontologyRepository = new HibernateOntologyRepository();


    public static ExpressionSummaryRepository getExpressionSummaryRepository() {
        if (xpatsumRep == null) {
            xpatsumRep = new HibernateExpressionSummaryRepository();
        }

        return xpatsumRep;
    }

    public static AnatomyRepository getAnatomyRepository() {
        if (anatRep == null) {
            anatRep = new HibernateAnatomyRepository();
        }
        return anatRep;
    }

    public static void setAnatomyRepository(AnatomyRepository ar) {
        anatRep = ar;
    }

    public static AntibodyRepository getAntibodyRepository() {
        if (antibodyRep == null) {
            antibodyRep = new HibernateAntibodyRepository();
        }
        return antibodyRep;
    }

    public static void setAntibodyRepository(AntibodyRepository ar) {
        antibodyRep = ar;
    }

    public static RenoRepository getRenoRepository() {
        if (renoRep == null) {
            renoRep = new HibernateRenoRepository();
        }
        return renoRep;
    }

    public static void setSequenceRepository(SequenceRepository sr) {
        seqRep = sr;
    }

    public static SequenceRepository getSequenceRepository() {
        if (seqRep == null) {
            seqRep = new HibernateSequenceRepository();
        }
        return seqRep;
    }

    public static void setRenoRepository(RenoRepository rr) {
        renoRep = rr;
    }

    public static PublicationRepository getPublicationRepository() {
        if (pubRep == null) {
            pubRep = new HibernatePublicationRepository();
        }
        return pubRep;
    }

    public static void setPublicationRepository(UserRepository ur) {
        userRep = ur;
    }

    public static UserRepository getUserRepository() {
        if (userRep == null) {
            userRep = new HibernateUserRepository();
        }
        return userRep;
    }

    public static void setPublicationRepository(PublicationRepository pr) {
        pubRep = pr;
    }

    public static AuditLogRepository getAuditLogRepository() {
        if (auditRep == null) {
            auditRep = new HibernateAuditLogRepository();
        }
        return auditRep;
    }

    public static void setAuditRep(AuditLogRepository ar) {
        auditRep = ar;
    }

    public static MarkerRepository getMarkerRepository() {
        if (markerRep == null)
            markerRep = new HibernateMarkerRepository();
        return markerRep;
    }

    public static InfrastructureRepository getInfrastructureRepository() {
        if (infraRep == null)
            infraRep = new HibernateInfrastructureRepository();
        return infraRep;
    }

    public static MarkerGoTermEvidenceRepository getMarkerGoTermEvidenceRepository() {
        if (markerGoTermEvidenceRepository == null)
            markerGoTermEvidenceRepository = new HibernateMarkerGoTermEvidenceRepository();
        return markerGoTermEvidenceRepository;
    }

    public static void setMarkerRepository(MarkerRepository markerRep) {
        RepositoryFactory.markerRep = markerRep;
    }

    public static ProfileRepository getProfileRepository() {
        if (profileRep == null) {
            profileRep = new HibernateProfileRepository();
        }
        return profileRep;
    }

    public static void setProfileRepository(ProfileRepository proRep) {
        profileRep = proRep;
    }

    public static MutantRepository getMutantRepository() {
        if (mutRep == null) {
            mutRep = new HibernateMutantRepository();
        }
        return mutRep;
    }

    public static void setMutantRepository(MutantRepository db) {
        mutRep = db;
    }

    public static FeatureRepository getFeatureRepository() {
        if (featRep == null) {
            featRep = new HibernateFeatureRepository();
        }
        return featRep;
    }

    public static void setFeatureRepository(FeatureRepository db) {
        featRep = db;
    }

    public static OrthologyRepository getOrthologyRepository() {
        if (orthoRep == null)
            orthoRep = new HibernateOrthologyRepository();
        return orthoRep;
    }


    public static LinkageRepository getLinkageRepository() {
        if (linkageRep == null)
            linkageRep = new HibernateLinkageRepository();
        return linkageRep;
    }

    public static void setOrthologyRepository(OrthologyRepository orthoRep) {
        RepositoryFactory.orthoRep = orthoRep;
    }

    public static BlastRepository getBlastRepository() {
        if (blastRepository == null)
            blastRepository = new HibernateBlastRepository();
        return blastRepository;
    }


    public static DisplayGroupRepository getDisplayGroupRepository() {
        if (displayGroupRepository == null)
            displayGroupRepository = new HibernateDisplayGroupRepository();
        return displayGroupRepository;
    }

    public static ExpressionRepository getExpressionRepository() {
        if (expressionRep == null)
            expressionRep = new HibernateExpressionRepository();
        return expressionRep;
    }

    public static void setExpressionRepository(ExpressionRepository expressionRepository) {
        expressionRep = expressionRepository;
    }

    public static GBrowseRepository getGBrowseRepository() {
        if (gbrowseRepository == null)
            gbrowseRepository = new HibernateGBrowseRepository();
        return gbrowseRepository;
    }

    public static void setGBrowseRepository(GBrowseRepository gbrowseRepository) {
        RepositoryFactory.gbrowseRepository = gbrowseRepository;
    }

    public static PhenotypeRepository getPhenotypeRepository() {
        if(phenotypeRep==null){
            phenotypeRep = new HibernatePhenotypeRepository();
        }
        return phenotypeRep;
    }

    public static void setPhenotypeRepository(PhenotypeRepository phenotypeRepository) {
        phenotypeRep = phenotypeRepository;
    }

    public static OntologyRepository getOntologyRepository() {
        if(ontologyRepository==null){
            ontologyRepository = new HibernateOntologyRepository();
        }
        return ontologyRepository;
    }

    public static void setOntologyRepository(OntologyRepository repository) {
        ontologyRepository = repository;
    }
}
