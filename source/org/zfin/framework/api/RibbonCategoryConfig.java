package org.zfin.framework.api;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

public abstract class RibbonCategoryConfig {

    public static final String ANATOMY = "ZFA:0100000";
    public static final String STAGE = "ZFS:0100000";
    public static final String GO_MF = "GO:0003674";
    public static final String GO_BP = "GO:0008150";
    public static final String GO_CC = "GO:0005575";

    private static final OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static final OntologyService ontologyService = new OntologyService();

    protected GenericTerm categoryTerm;
    @Getter protected List<GenericTerm> slimTerms;

    protected String allLabel;
    protected String allDescription;
    @Getter protected boolean includeAll = true;

    protected String otherLabel;
    protected String otherDescription;
    @Getter protected boolean includeOther = true;

    public static RibbonCategoryConfig forTerm(RibbonType ribbon, String termId) {
        switch (termId) {
            case ANATOMY:
                return new Anatomy();
            case STAGE:
                return new Stage();
            case GO_BP:
                switch (ribbon) {
                    case GENE_ONTOLOGY:
                        return new GeneOntologyBiologicalProcess();
                    case PHENOTYPE:
                        return new PhenotypeBiologicalProcess();
                }
                break;
            case GO_MF:
                switch (ribbon) {
                    case GENE_ONTOLOGY:
                        return new GeneOntologyMolecularFunction();
                    case PHENOTYPE:
                        return new PhenotypeMolecularFunction();
                }
                break;
            case GO_CC:
                switch (ribbon) {
                    case EXPRESSION:
                        return new ExpressionCellularComponent();
                    case PHENOTYPE:
                        return new PhenotypeCellularComponent();
                    case GENE_ONTOLOGY:
                        return new GeneOntologyCellularComponent();
                }
        }
        return null;
    }

    public String getCategoryDefinition() {
        return categoryTerm.getDefinition();
    }

    public String getCategoryId() {
        return categoryTerm.getOboID();
    }

    public String getCategoryLabel() {
        return categoryTerm.getTermName().replace('_', ' ');
    }

    public String getAllLabel() {
        return StringUtils.defaultIfEmpty(allLabel, "All " + getCategoryLabel());
    }

    public String getAllDescription() {
        return StringUtils.defaultIfEmpty(allDescription, "Show all " + getCategoryLabel() + " annotations");
    }

    public String getOtherLabel() {
        return StringUtils.defaultIfEmpty(otherLabel, "Other " + getCategoryLabel());
    }

    public String getOtherDescription() {
        return StringUtils.defaultIfEmpty(otherDescription, "Show all " + getCategoryLabel() + " annotations not mapped to a specific term");
    }

    public String getSlimTermLabel(GenericTerm slimTerm) {
        return slimTerm.getTermName();
    }

    public static class Anatomy extends RibbonCategoryConfig {
        public Anatomy() {
            this.categoryTerm = ontologyRepository.getTermByOboID(ANATOMY);
            this.slimTerms = ontologyRepository.getZfaRibbonTerms();
            this.allLabel = "All anatomical structures";
            this.otherLabel = "Other structures";
        }
    }

    public static class Stage extends RibbonCategoryConfig {
        public Stage() {
            this.categoryTerm = ontologyRepository.getTermByOboID(STAGE);
            this.slimTerms = ontologyService.getRibbonStages();
            this.allLabel = "All stages";
            this.includeOther = false;
        }

        @Override
        public String getSlimTermLabel(GenericTerm slimTerm) {
            String label = super.getSlimTermLabel(slimTerm);
            DevelopmentStage stage = ontologyService.getFirstDevelopmentStageForTerm(slimTerm);
            if (stage != null) {
                String startTime;
                if (stage.getHoursStart() < 168) {
                    startTime = stage.getHoursStart() + " hpf";
                } else {
                    startTime = (stage.getHoursStart() / 24) + " dpf";
                }
                label += " - " + startTime;
            }
            return label;
        }
    }

    public static abstract class MolecularFunction extends RibbonCategoryConfig {
        public MolecularFunction() {
            this.categoryTerm = ontologyRepository.getTermByOboID(GO_MF);
            this.allLabel = "All molecular functions";
            this.otherLabel = "Other molecular functions";
        }
    }

    public static class PhenotypeMolecularFunction extends MolecularFunction {
        public PhenotypeMolecularFunction() {
            super();
            this.slimTerms = ontologyRepository.getPhenotypeRibbonMolecularFunctionTerms();
        }
    }

    public static class GeneOntologyMolecularFunction extends MolecularFunction {
        public GeneOntologyMolecularFunction() {
            super();
            this.slimTerms = ontologyRepository.getGORibbonMolecularFunctionTerms();
        }
    }

    public static abstract class BiologicalProcess extends RibbonCategoryConfig {
        public BiologicalProcess() {
            this.categoryTerm = ontologyRepository.getTermByOboID(GO_BP);
            this.allLabel = "All biological processes";
            this.otherLabel = "Other biological processes";
        }
    }

    public static class PhenotypeBiologicalProcess extends BiologicalProcess {
        public PhenotypeBiologicalProcess() {
            super();
            this.slimTerms = ontologyRepository.getPhenotypeRibbonBiologicalProcessTerms();
        }
    }

    public static class GeneOntologyBiologicalProcess extends BiologicalProcess {
        public GeneOntologyBiologicalProcess() {
            super();
            this.slimTerms = ontologyRepository.getGORibbonBiologicalProcessTerms();
        }
    }

    public static abstract class CellularComponent extends RibbonCategoryConfig {
        public CellularComponent() {
            this.categoryTerm = ontologyRepository.getTermByOboID(GO_CC);
            this.allLabel = "All cellular components";
            this.otherLabel = "Other cellular components";
        }
    }

    public static class ExpressionCellularComponent extends CellularComponent {
        public ExpressionCellularComponent() {
            super();
            this.slimTerms = ontologyRepository.getExpressionRibbonCellularComponentTerms();
        }
    }

    public static class PhenotypeCellularComponent extends CellularComponent {
        public PhenotypeCellularComponent() {
            super();
            this.slimTerms = ontologyRepository.getPhenotypeRibbonCellularComponentTerms();
        }
    }

    public static class GeneOntologyCellularComponent extends CellularComponent {
        public GeneOntologyCellularComponent() {
            super();
            this.slimTerms = ontologyRepository.getGORibbonCellularComponentTerms();
        }
    }

}
