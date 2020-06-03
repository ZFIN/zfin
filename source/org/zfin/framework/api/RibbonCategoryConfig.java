package org.zfin.framework.api;

import lombok.Builder;
import lombok.Getter;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

@Builder
@Getter
public class RibbonCategoryConfig {

    private static final OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static final OntologyService ontologyService = new OntologyService();
    private static List<GenericTerm> agrGoSlimTerms;

    private final GenericTerm categoryTerm;
    private final List<GenericTerm> slimTerms;

    @Builder.Default private final boolean includeAll = true;
    private final String allLabel;
    private final String allDescription;

    @Builder.Default private final boolean includeOther = true;
    private final String otherLabel;
    private final String otherDescription;

    public static final String ANATOMY = "ZFA:0100000";
    public static final String STAGE = "ZFS:0100000";
    public static final String GO_MF = "GO:0003674";
    public static final String GO_BP = "GO:0008150";
    public static final String GO_CC = "GO:0005575";

    public static RibbonCategoryConfig forTerm(RibbonType ribbon, String termOboID) {
        GenericTerm term = ontologyRepository.getTermByOboID(termOboID);
        RibbonCategoryConfigBuilder builder = RibbonCategoryConfig.builder().categoryTerm(term);
        switch (termOboID) {
            case ANATOMY:
                builder.slimTerms(ontologyRepository.getZfaRibbonTerms())
                        .allLabel("All anatomical structures")
                        .otherLabel("Other structures");
                break;

            case STAGE:
                builder.slimTerms(ontologyService.getRibbonStages())
                        .allLabel("All stages")
                        .includeOther(false);
                break;

            case GO_MF:
                switch (ribbon) {
                    case PHENOTYPE:
                        builder.slimTerms(ontologyRepository.getPhenotypeRibbonMolecularFunctionTerms());
                        break;
                    case GENE_ONTOLOGY:
                        builder.slimTerms(ontologyRepository.getGORibbonMolecularFunctionTerms());
                        break;
                }
                break;

            case GO_BP:
                switch (ribbon) {
                    case PHENOTYPE:
                        builder.slimTerms(ontologyRepository.getPhenotypeRibbonBiologicalProcessTerms());
                        break;
                    case GENE_ONTOLOGY:
                        builder.slimTerms(ontologyRepository.getGORibbonBiologicalProcessTerms());
                        break;
                }
                break;

            case GO_CC:
                switch (ribbon) {
                    case EXPRESSION:
                        builder.slimTerms(ontologyRepository.getExpressionRibbonCellularComponentTerms());
                        break;
                    case PHENOTYPE:
                        builder.slimTerms(ontologyRepository.getPhenotypeRibbonCellularComponentTerms());
                        break;
                    case GENE_ONTOLOGY:
                        builder.slimTerms(ontologyRepository.getGORibbonCellularComponentTerms());
                        break;
                }
                break;
        }
        return builder.build();
    }

}
