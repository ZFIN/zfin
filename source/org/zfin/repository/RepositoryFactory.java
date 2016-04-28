package org.zfin.repository;

import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.repository.HibernateAnatomyRepository;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.antibody.repository.HibernateAntibodyRepository;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.audit.repository.HibernateAuditLogRepository;
import org.zfin.construct.repository.ConstructRepository;
import org.zfin.construct.repository.HibernateConstructRepository;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.repository.ExpressionSummaryRepository;
import org.zfin.expression.repository.HibernateExpressionRepository;
import org.zfin.expression.repository.HibernateExpressionSummaryRepository;
import org.zfin.feature.*;
import org.zfin.feature.repository.ControlledVocabularyRepository;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.HibernateControlledVocabularyRepository;
import org.zfin.feature.repository.HibernateFeatureRepository;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.repository.HibernateFigureRepository;
import org.zfin.fish.repository.FishRepository;
import org.zfin.fish.repository.HibernateFishRepository;
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
import org.zfin.profile.repository.HibernateProfileRepository;
import org.zfin.profile.repository.ProfileRepository;
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
    private static FigureRepository figureRepository;
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
    private static PhenotypeRepository phenotypeRep = new HibernatePhenotypeRepository();
    private static OntologyRepository ontologyRepository = new HibernateOntologyRepository();
    private static FishRepository fishRepository = new HibernateFishRepository();
    private static ConstructRepository constructRepository = new HibernateConstructRepository();
    private static ControlledVocabularyRepository<DnaMutationTerm> dnaMutationTermRepository;
    private static ControlledVocabularyRepository<GeneLocalizationTerm> geneLocalizationTermRepository;
    private static ControlledVocabularyRepository<ProteinConsequence> proteinConsequenceTermRepository;
    private static ControlledVocabularyRepository<AminoAcidTerm> aminoAcidTermRepository;
    private static ControlledVocabularyRepository<TranscriptConsequence> transcriptTermRepository;


    public static ControlledVocabularyRepository<TranscriptConsequence> getTranscriptTermRepository() {
        if (transcriptTermRepository == null) {
            transcriptTermRepository = new HibernateControlledVocabularyRepository<>(TranscriptConsequence.class);
        }
        return transcriptTermRepository;
    }

    public static ControlledVocabularyRepository<AminoAcidTerm> getAminoAcidTermRepository() {
        if (aminoAcidTermRepository == null) {
            aminoAcidTermRepository = new HibernateControlledVocabularyRepository<>(AminoAcidTerm.class);
        }
        return aminoAcidTermRepository;
    }

    public static ControlledVocabularyRepository<ProteinConsequence> getProteinConsequenceTermRepository() {
        if (proteinConsequenceTermRepository == null) {
            proteinConsequenceTermRepository = new HibernateControlledVocabularyRepository<>(ProteinConsequence.class);
        }
        return proteinConsequenceTermRepository;
    }

    public static ControlledVocabularyRepository<DnaMutationTerm> getDnaMutationTermRepository() {
        if (dnaMutationTermRepository == null) {
            dnaMutationTermRepository = new HibernateControlledVocabularyRepository<>(DnaMutationTerm.class);
        }
        return dnaMutationTermRepository;
    }

    public static ControlledVocabularyRepository<GeneLocalizationTerm> getGeneLocalizationTermRepository() {
        if (geneLocalizationTermRepository == null) {
            geneLocalizationTermRepository = new HibernateControlledVocabularyRepository<>(GeneLocalizationTerm.class);
        }
        return geneLocalizationTermRepository;
    }

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

    public static FigureRepository getFigureRepository() {
        if (figureRepository == null)
            figureRepository = new HibernateFigureRepository();
        return figureRepository;
    }

    public static ExpressionRepository getExpressionRepository() {
        if (expressionRep == null)
            expressionRep = new HibernateExpressionRepository();
        return expressionRep;
    }

    public static void setExpressionRepository(ExpressionRepository expressionRepository) {
        expressionRep = expressionRepository;
    }

    public static PhenotypeRepository getPhenotypeRepository() {
        if (phenotypeRep == null) {
            phenotypeRep = new HibernatePhenotypeRepository();
        }
        return phenotypeRep;
    }

    public static void setPhenotypeRepository(PhenotypeRepository phenotypeRepository) {
        phenotypeRep = phenotypeRepository;
    }

    public static OntologyRepository getOntologyRepository() {
        if (ontologyRepository == null) {
            ontologyRepository = new HibernateOntologyRepository();
        }
        return ontologyRepository;
    }

    public static void setOntologyRepository(OntologyRepository repository) {
        ontologyRepository = repository;
    }

    public static FishRepository getFishRepository() {
        if (fishRepository == null) {
            fishRepository = new HibernateFishRepository();
        }
        return fishRepository;
    }

    public static void setFishRepository(FishRepository repository) {
        fishRepository = repository;
    }

    public static ConstructRepository getConstructRepository() {
        if (constructRepository == null) {
            constructRepository = new HibernateConstructRepository();
        }
        return constructRepository;
    }

    public static void setConstructRepository(ConstructRepository repository) {
        constructRepository = repository;
    }
}
