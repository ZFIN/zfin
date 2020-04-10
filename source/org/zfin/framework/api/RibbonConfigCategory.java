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
public class RibbonConfigCategory {

    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static OntologyService ontologyService = new OntologyService();
    private static List<GenericTerm> agrGoSlimTerms;

    private GenericTerm categoryTerm;
    private List<GenericTerm> slimTerms;

    @Builder.Default private boolean includeAll = true;
    private String allLabel;
    private String allDescription;

    @Builder.Default private boolean includeOther = true;
    private String otherLabel;
    private String otherDescription;

    public static RibbonConfigCategory anatomy() {
        return RibbonConfigCategory.builder()
                .categoryTerm(ontologyRepository.getTermByOboID("ZFA:0100000"))
                .slimTerms(ontologyRepository.getZfaRibbonTerms())
                .allLabel("All anatomical structures")
                .otherLabel("Other structures")
                .build();
    }

    public static RibbonConfigCategory stage() {
        return RibbonConfigCategory.builder()
                .categoryTerm(ontologyRepository.getTermByOboID("ZFS:0100000"))
                .slimTerms(ontologyService.getRibbonStages())
                .allLabel("All stages")
                .otherLabel("Unknown stage")
                .build();
    }

    public static RibbonConfigCategory molecularFunction() {
        return RibbonConfigCategory.builder()
                .categoryTerm(ontologyRepository.getTermByOboID("GO:0003674"))
                .slimTerms(filterTermsByOntology(getAgrGoSlimTerms(), Ontology.GO_MF))
                .build();
    }

    public static RibbonConfigCategory biologicalProcess() {
        return RibbonConfigCategory.builder()
                .categoryTerm(ontologyRepository.getTermByOboID("GO:0008150"))
                .slimTerms(filterTermsByOntology(getAgrGoSlimTerms(), Ontology.GO_BP))
                .build();
    }

    public static RibbonConfigCategory cellularComponent() {
        return RibbonConfigCategory.builder()
                .categoryTerm(ontologyRepository.getTermByOboID("GO:0005575"))
                .slimTerms(filterTermsByOntology(getAgrGoSlimTerms(), Ontology.GO_CC))
                .build();
    }

    private static List<GenericTerm> getAgrGoSlimTerms() {
        if (agrGoSlimTerms == null) {
            agrGoSlimTerms = ontologyRepository.getTermsInSubset("goslim_agr");
        }
        return agrGoSlimTerms;
    }

    private static List<GenericTerm> filterTermsByOntology(List<GenericTerm> terms, Ontology ontology) {
        return terms.stream().filter(term -> term.getOntology() == ontology).collect(toList());
    }

}
