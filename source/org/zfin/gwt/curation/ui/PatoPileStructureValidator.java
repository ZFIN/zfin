package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.util.StringUtils;

import java.util.List;
import java.util.Map;

import static org.zfin.gwt.root.dto.OntologyDTO.*;

/**
 * Structure Validator for Pato Pile Construction Zone.
 */
public class PatoPileStructureValidator extends AbstractPileStructureValidator<PhenotypeStatementDTO> {


    public PatoPileStructureValidator(Map<EntityPart, List<OntologyDTO>> termEntryMap) {
        super(termEntryMap);
    }

    /**
     * If a subterm is provided check if it matches a valid quality term:
     * AO and GO-CC require 'Quality - Objects'
     * GO-MF and GO-BP require 'Quality - Processes'
     * If no subterm is provided check the ontology of the super term with the same
     * matching quality term.
     *
     * @param phenotypeTerm structure to be validated
     * @return true or false
     */
    @Override
    public boolean isValidNewPileStructure(PhenotypeStatementDTO phenotypeTerm) {
        if (!super.isValidNewPileStructure(phenotypeTerm))
            return false;
        TermDTO qualityTerm = phenotypeTerm.getQuality();
        if (qualityTerm == null || StringUtils.isEmpty(qualityTerm.getName())) {
            errorMessages.add("No Quality term provided.");
            return false;
        }
        // Check for valid quality
        if (!EntityQualityOntologyPair.isValidCombination(phenotypeTerm.getEntity(), qualityTerm.getOntology())) {
            errorMessages.add("Not a valid combination of post-composed entity term and quality.");
            return false;
        }

        // Check for valid post-compositions
        if (!PostComposedOntologyPair.isValidCombination(phenotypeTerm.getEntity())) {
            errorMessages.add("Not a valid combination of post-composed terms for the entity portion.");
            return false;
        }

        // Check for valid post-compositions
        if (phenotypeTerm.getRelatedEntity() != null && phenotypeTerm.getRelatedEntity().getSuperTerm() != null) {
            if (!PostComposedOntologyPair.isValidCombination(phenotypeTerm.getRelatedEntity())) {
                errorMessages.add("Not a valid combination of post-composed terms for the related entity portion.");
                return false;
            }
        }

        // Check valid combinations of ontologies
        if (qualityTerm.isSubsetOf(SubsetDTO.RELATIONAL_SLIM)) {
            if (!EntityRelatedEntityOntologyPair.isValidCombination(phenotypeTerm)) {
                errorMessages.add("Not a valid combination of ontologies for a relational quality term!");
                return false;
            }
        }

        return true;
    }

    public static boolean isEqualsNullSafe(OntologyDTO ontologyOne, OntologyDTO ontologyTwo) {
        if (ontologyOne == null && ontologyTwo == null)
            return true;
        if (ontologyOne == null || ontologyTwo == null)
            return false;
        return ontologyOne.equals(ontologyTwo);

    }

    public enum EntityQualityOntologyPair {

        AO_x_OB(ANATOMY, null, QUALITY_QUALITIES),
        AO_AO_OB(ANATOMY, ANATOMY, QUALITY_QUALITIES),
        AO_CC_OB(ANATOMY, GO_CC, QUALITY_QUALITIES),
        AO_SP_OB(ANATOMY, SPATIAL, QUALITY_QUALITIES),
        AO_MPATH_OB(ANATOMY, MPATH_NEOPLASM, QUALITY_QUALITIES),
        BP_x_PR(GO_BP, null, QUALITY_PROCESSES),
        MF_x_PR(GO_MF, null, QUALITY_PROCESSES),
        MFBP_x_PR(GO_BP_MF, null, QUALITY_PROCESSES),
        AO_MF_PR(ANATOMY, GO_MF, QUALITY_PROCESSES);

        private OntologyDTO entitySuper;
        private OntologyDTO entitySub;
        private OntologyDTO quality;

        EntityQualityOntologyPair(OntologyDTO entitySuper, OntologyDTO entitySub, OntologyDTO quality) {
            this.entitySuper = entitySuper;
            this.entitySub = entitySub;
            this.quality = quality;
        }

        public static boolean isValidCombination(EntityDTO entity, OntologyDTO qualityO) {
            if (entity == null || qualityO == null)
                return false;
            OntologyDTO entitySuperO = null;
            OntologyDTO entitySubO = null;
            if (entity.getSuperTerm() != null)
                entitySuperO = entity.getSuperTerm().getOntology();
            if (entity.getSubTerm() != null)
                entitySubO = entity.getSubTerm().getOntology();
            for (EntityQualityOntologyPair pair : values()) {
                if (!isEqualsNullSafe(pair.getEntitySuper(), entitySuperO))
                    continue;
                if (!isEqualsNullSafe(pair.getEntitySub(), entitySubO))
                    continue;
                if (!isEqualsNullSafe(pair.getQuality(), qualityO))
                    continue;
                return true;
            }
            return false;
        }

        public OntologyDTO getEntitySuper() {
            return entitySuper;
        }

        public OntologyDTO getEntitySub() {
            return entitySub;
        }

        public OntologyDTO getQuality() {
            return quality;
        }
    }

    public enum PostComposedOntologyPair {
        AO_x(ANATOMY, null),
        AO_AO(ANATOMY, ANATOMY),
        AO_CC(ANATOMY, GO_CC),
        AO_SP(ANATOMY, SPATIAL),
        AO_MF(ANATOMY, GO_MF),
        AO_MPATH(ANATOMY, MPATH_NEOPLASM),
        BP_x(GO_BP, null),
        MF_x(GO_MF, null),
        BPMF_x(GO_BP_MF, null);

        private OntologyDTO entitySuper;
        private OntologyDTO entitySub;

        private PostComposedOntologyPair(OntologyDTO entitySuper, OntologyDTO entitySub) {
            this.entitySuper = entitySuper;
            this.entitySub = entitySub;
        }

        public static boolean isValidCombination(EntityDTO entity) {
            if (entity == null)
                return false;
            OntologyDTO entitySuperO = null;
            OntologyDTO entitySubO = null;
            if (entity.getSuperTerm() != null)
                entitySuperO = entity.getSuperTerm().getOntology();
            if (entity.getSubTerm() != null)
                entitySubO = entity.getSubTerm().getOntology();
            for (PostComposedOntologyPair pair : values()) {
                if (!isEqualsNullSafe(pair.getEntitySuper(), entitySuperO))
                    continue;
                if (!isEqualsNullSafe(pair.getEntitySub(), entitySubO))
                    continue;
                return true;
            }
            return false;
        }

        public OntologyDTO getEntitySuper() {
            return entitySuper;
        }

        public OntologyDTO getEntitySub() {
            return entitySub;
        }
    }

    // valid entity-related entity combinations
    public enum EntityRelatedEntityOntologyPair {
        AO_x_AO_x(ANATOMY, null, ANATOMY, null),
        AO_x_AO_AO(ANATOMY, null, ANATOMY, ANATOMY),
        AO_x_AO_CC(ANATOMY, null, ANATOMY, GO_CC),
        AO_x_AO_SP(ANATOMY, null, ANATOMY, SPATIAL),

        AO_AO_AO_x(ANATOMY, ANATOMY, ANATOMY, null),
        AO_AO_AO_AO(ANATOMY, ANATOMY, ANATOMY, ANATOMY),
        AO_AO_AO_CC(ANATOMY, ANATOMY, ANATOMY, GO_CC),
        AO_AO_AO_SP(ANATOMY, ANATOMY, ANATOMY, SPATIAL),
        AO_AO_AO_MPATH(ANATOMY, ANATOMY, ANATOMY, MPATH_NEOPLASM),

        AO_CC_AO_x(ANATOMY, GO_CC, ANATOMY, null),
        AO_CC_AO_AO(ANATOMY, GO_CC, ANATOMY, ANATOMY),
        AO_CC_AO_CC(ANATOMY, GO_CC, ANATOMY, GO_CC),
        AO_CC_AO_SP(ANATOMY, GO_CC, ANATOMY, SPATIAL),

        AO_SP_AO_x(ANATOMY, SPATIAL, ANATOMY, null),
        AO_SP_AO_AO(ANATOMY, SPATIAL, ANATOMY, ANATOMY),
        AO_SP_AO_CC(ANATOMY, SPATIAL, ANATOMY, GO_CC),
        AO_SP_AO_SP(ANATOMY, SPATIAL, ANATOMY, SPATIAL),

        BP_x_BP_x(GO_BP, null, GO_BP, null),
        BP_x_MF_x(GO_BP, null, GO_MF, null),
        BPMF_x_BP_x(GO_BP_MF, null, GO_BP, null),
        BPMF_x_MF_x(GO_BP_MF, null, GO_MF, null),
        BP_x_AO_MF(GO_BP, null, ANATOMY, GO_MF);

        private OntologyDTO entitySuper;
        private OntologyDTO entitySub;
        private OntologyDTO relatedEntitySuper;
        private OntologyDTO relatedEntitySub;

        EntityRelatedEntityOntologyPair(OntologyDTO entitySuper, OntologyDTO entitySub, OntologyDTO relatedEntitySuper, OntologyDTO relatedEntitySub) {
            this.entitySuper = entitySuper;
            this.entitySub = entitySub;
            this.relatedEntitySuper = relatedEntitySuper;
            this.relatedEntitySub = relatedEntitySub;
        }

        public static boolean isValidCombination(OntologyDTO entitySuper, OntologyDTO entitySub, OntologyDTO relatedEntitySuper, OntologyDTO relatedEntitySub) {
            PhenotypeStatementDTO phenotypeStatementDTO = new PhenotypeStatementDTO();
            EntityDTO entity = new EntityDTO();
            TermDTO superTerm = new TermDTO();
            superTerm.setOntology(entitySuper);
            TermDTO subTerm = new TermDTO();
            superTerm.setOntology(entitySub);
            entity.setSuperTerm(superTerm);
            entity.setSuperTerm(subTerm);
            phenotypeStatementDTO.setEntity(entity);
            // related entity
            EntityDTO relateEntity = new EntityDTO();
            TermDTO superTermRelated = new TermDTO();
            superTermRelated.setOntology(relatedEntitySuper);
            TermDTO subTermRelated = new TermDTO();
            subTermRelated.setOntology(relatedEntitySub);
            relateEntity.setSuperTerm(superTermRelated);
            relateEntity.setSuperTerm(subTermRelated);
            phenotypeStatementDTO.setRelatedEntity(relateEntity);
            return isValidCombination(phenotypeStatementDTO);
        }

        public static boolean isValidCombination(PhenotypeStatementDTO phenotype) {
            OntologyDTO entitySuperO = null;
            OntologyDTO entitySubO = null;
            OntologyDTO relEntitySuperO = null;
            OntologyDTO relEntitySubO = null;
            EntityDTO entity = phenotype.getEntity();
            if (entity != null) {
                if (entity.getSuperTerm() != null)
                    entitySuperO = entity.getSuperTerm().getOntology();
                if (entity.getSubTerm() != null)
                    entitySubO = entity.getSubTerm().getOntology();
            }
            EntityDTO relEntity = phenotype.getRelatedEntity();
            if (relEntity != null) {
                if (relEntity.getSuperTerm() != null)
                    relEntitySuperO = relEntity.getSuperTerm().getOntology();
                if (relEntity.getSubTerm() != null)
                    relEntitySubO = relEntity.getSubTerm().getOntology();
            }
            for (EntityRelatedEntityOntologyPair pair : values()) {
                if (!isEqualsNullSafe(pair.getEntitySuper(), entitySuperO))
                    continue;
                if (!isEqualsNullSafe(pair.getEntitySub(), entitySubO))
                    continue;
                if (!isEqualsNullSafe(pair.getRelatedEntitySuper(), relEntitySuperO))
                    continue;
                if (!isEqualsNullSafe(pair.getRelatedEntitySub(), relEntitySubO))
                    continue;
                return true;
            }
            return false;
        }

        public OntologyDTO getEntitySuper() {
            return entitySuper;
        }

        public OntologyDTO getEntitySub() {
            return entitySub;
        }

        public OntologyDTO getRelatedEntitySuper() {
            return relatedEntitySuper;
        }

        public OntologyDTO getRelatedEntitySub() {
            return relatedEntitySub;
        }
    }
}