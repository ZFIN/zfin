package org.zfin.repository;

import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.repository.HibernateAnatomyRepository;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.reno.repository.HibernateRenoRepository ;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.repository.HibernateSequenceRepository;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.audit.repository.HibernateAuditLogRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.security.repository.UserRepository;
import org.zfin.security.repository.HibernateUserRepository;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.people.repository.HibernateProfileRepository;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.HibernateMutantRepository;
import org.zfin.expression.repository.ExpressionSummaryRepository;
import org.zfin.expression.repository.HibernateExpressionSummaryRepository;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.repository.HibernateMarkerRepository;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.infrastructure.repository.HibernateInfrastructureRepository;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.orthology.repository.HibernateOrthologyRepository;

/**
 * Please provide JavaDoc info!!!
 */
public class RepositoryFactory {

    private static AnatomyRepository anatRep;
    private static RenoRepository renoRep;
    private static PublicationRepository pubRep;
    private static UserRepository userRep;
    private static AuditLogRepository auditRep;
    private static ProfileRepository profileRep;
    private static MutantRepository mutRep;
    private static ExpressionSummaryRepository xpatsumRep;
    private static MarkerRepository markerRep;
    private static InfrastructureRepository infraRep;
    private static SequenceRepository seqRep;
    private static OrthologyRepository orthoRep;

    public static ExpressionSummaryRepository getExpressionSummaryRepository() {
        if (xpatsumRep == null) {
            xpatsumRep = new HibernateExpressionSummaryRepository();
        }

        return xpatsumRep;
    }

    public static void setExpressionSummaryRepository(ExpressionSummaryRepository xpr) {
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

    public static void setOrthologyRepository(OrthologyRepository orthoRep) {
        RepositoryFactory.orthoRep = orthoRep; 
    }


}
