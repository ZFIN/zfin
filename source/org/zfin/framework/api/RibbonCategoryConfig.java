package org.zfin.framework.api;

import lombok.Builder;
import lombok.Getter;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.ontology.service.OntologyService;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

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

    public static RibbonCategoryConfig forTerm(String termOboID) {
        GenericTerm term = ontologyRepository.getTermByOboID(termOboID);
        if (term == null) {
            return null;
        }
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
                builder.slimTerms(filterTermsByOntology(
                        ontologyRepository.getTermsInSubset("goslim_agr"),
                        Ontology.GO_MF
                ));
                break;
            case GO_BP:
                builder.slimTerms(filterTermsByOntology(
                        ontologyRepository.getTermsInSubset("goslim_agr"),
                        Ontology.GO_BP
                ));
                break;
            case GO_CC:
                builder.slimTerms(filterTermsByOntology(
                        ontologyRepository.getTermsInSubset("goslim_agr"),
                        Ontology.GO_CC
                ));
                break;
        }
        return builder.build();
    }

    public static RibbonCategoryConfig anatomy() {
        return forTerm(ANATOMY);
    }

    public static RibbonCategoryConfig stage() {
        return forTerm(STAGE);
    }

    public static RibbonCategoryConfig molecularFunction() {
        return forTerm(GO_MF);
    }

    public static RibbonCategoryConfig biologicalProcess() {
        return forTerm(GO_BP);
    }

    public static RibbonCategoryConfig cellularComponent() {
        return forTerm(GO_CC);
    }

    private static List<GenericTerm> filterTermsByOntology(List<GenericTerm> terms, Ontology ontology) {
        return terms.stream().filter(term -> term.getOntology() == ontology).collect(toList());
    }

}
