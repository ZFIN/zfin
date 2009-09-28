package org.zfin.repository;

import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.repository.HibernateAnatomyRepository;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.antibody.repository.HibernateAntibodyRepository;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.audit.repository.HibernateAuditLogRepository;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.repository.HibernateExpressionRepository;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.repository.HibernateExpressionRepository;
import org.zfin.infrastructure.repository.HibernateInfrastructureRepository;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.repository.HibernateMarkerRepository;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.repository.HibernateMutantRepository;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.orthology.repository.HibernateOrthologyRepository;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.people.repository.HibernateProfileRepository;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.security.repository.HibernateUserRepository;
import org.zfin.security.repository.UserRepository;
import org.zfin.sequence.reno.repository.HibernateRenoRepository;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.repository.HibernateSequenceRepository;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.repository.DisplayGroupRepository;
import org.zfin.sequence.repository.HibernateDisplayGroupRepository;
import org.zfin.sequence.blast.repository.BlastRepository;
import org.zfin.sequence.blast.repository.HibernateBlastRepository;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.mapping.repository.HibernateLinkageRepository;
import org.zfin.uniquery.repository.QuicksearchRepository;
import org.zfin.uniquery.repository.HibernateQuicksearchRepository;

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
    private static ExpressionRepository xpatsumRep;
    private static MarkerRepository markerRep;
    private static InfrastructureRepository infraRep;
    private static SequenceRepository seqRep;
    private static OrthologyRepository orthoRep;
    private static LinkageRepository linkageRep;
    private static QuicksearchRepository quicksearchRep;
    private static BlastRepository blastRepository;
    private static DisplayGroupRepository displayGroupRepository;
    private static ExpressionRepository expressionRep;

    public static ExpressionRepository getExpressionSummaryRepository() {
        if (xpatsumRep == null) {
            xpatsumRep = new HibernateExpressionRepository();
        }

        return xpatsumRep;
    }

    public static void setExpressionSummaryRepository(ExpressionRepository xpr) {
        xpatsumRep = xpr;
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
        return renoRep ;
    }

    public static void setSequenceRepository(SequenceRepository sr) {
        seqRep = sr;
    }
      public static SequenceRepository getSequenceRepository() {
        if (seqRep == null) {
            seqRep = new HibernateSequenceRepository();
        }
        return seqRep ;
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

    public static OrthologyRepository  getOrthologyRepository() {
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
        if (displayGroupRepository== null)
            displayGroupRepository = new HibernateDisplayGroupRepository();
        return displayGroupRepository;
    }

    public static QuicksearchRepository getQuicksearchRepository() {
        if (quicksearchRep == null)
            quicksearchRep = new HibernateQuicksearchRepository();
        return quicksearchRep;
    }

    public static void setQuicksearchRepository(QuicksearchRepository quicksearchRep) {
        RepositoryFactory.quicksearchRep = quicksearchRep;
    }

    public static ExpressionRepository getExpressionRepository() {
        if (expressionRep == null)
            expressionRep = new HibernateExpressionRepository();
        return expressionRep;
    }

    public static void setExpressionRepository(ExpressionRepository expressionRep) {
        RepositoryFactory.expressionRep = expressionRep;
    }
}
