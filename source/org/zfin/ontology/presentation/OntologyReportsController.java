package org.zfin.ontology.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.service.ExpressionService;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Controller that serves the various reports.
 */
@Controller
public class OntologyReportsController {

    @Autowired
    private ExpressionService expressionService ;

    @RequestMapping("/reports")
    private String createTermUsageReport(Model model) {
        return "ontology/reports.page";
    }

    @RequestMapping("/secondary-term-report")
    private String createObsoleteTermReport(Model model) {
        OntologyBean form = new OntologyBean();
        model.addAttribute("formBean", form);
        List<PhenotypeStatement> phenotypesWithSecondaryTerms = RepositoryFactory.getOntologyRepository().getPhenotypesOnSecondaryTerms();
        if (phenotypesWithSecondaryTerms != null) {
            List<PhenotypeObsoleteTermReport> phenotypeSecondaryTermReports = new ArrayList<PhenotypeObsoleteTermReport>(phenotypesWithSecondaryTerms.size());
            for (PhenotypeStatement phenotypeStatement : phenotypesWithSecondaryTerms) {
                PhenotypeObsoleteTermReport report = new PhenotypeObsoleteTermReport(phenotypeStatement);
                Set<GenericTerm> obsoletedTermSet = PhenotypeService.getSecondaryTerm(phenotypeStatement);
                report.setObsoletedTermList(obsoletedTermSet);
                if (obsoletedTermSet != null) {
                    for (GenericTerm obsoletedTerm : obsoletedTermSet) {
                        report.setReplacementTermList(RepositoryFactory.getOntologyRepository().getReplacedByTerms(obsoletedTerm));
                        report.setConsiderTermList(RepositoryFactory.getOntologyRepository().getConsiderTerms(obsoletedTerm));
                    }
                }
                phenotypeSecondaryTermReports.add(report);
            }
            model.addAttribute("phenotypeSecondaryTermReports", phenotypeSecondaryTermReports);
            model.addAttribute("numberOfPhenotypesOnSecondaryTerms", phenotypesWithSecondaryTerms.size());
        }
        List<ExpressionResult> expressionsWithSecondaryTerms = RepositoryFactory.getOntologyRepository().getExpressionsOnSecondaryTerms();
        if (expressionsWithSecondaryTerms != null) {
            List<ExpressionObsoleteTermReport> expressionSecondaryTermReports = new ArrayList<ExpressionObsoleteTermReport>(expressionsWithSecondaryTerms.size());
            for (ExpressionResult phenotypeStatement : expressionsWithSecondaryTerms) {
                ExpressionObsoleteTermReport report = new ExpressionObsoleteTermReport(phenotypeStatement);
                Set<GenericTerm> obsoletedTermSet = expressionService.getSecondaryTerm(phenotypeStatement);
                report.setObsoletedTermList(obsoletedTermSet);
                if (obsoletedTermSet != null) {
                    for (GenericTerm obsoletedTerm : obsoletedTermSet) {
                        report.setReplacementTermList(RepositoryFactory.getOntologyRepository().getReplacedByTerms(obsoletedTerm));
                        report.setConsiderTermList(RepositoryFactory.getOntologyRepository().getConsiderTerms(obsoletedTerm));
                    }
                }
                expressionSecondaryTermReports.add(report);
            }
            model.addAttribute("expressionObsoleteTermReports", expressionSecondaryTermReports);
            model.addAttribute("numberOfExpressionsOnSecondaryTerms", expressionSecondaryTermReports.size());
        }
        List<MarkerGoTermEvidence> goEvidenceWithSecondaryTerms = RepositoryFactory.getOntologyRepository().getGoEvidenceOnSecondaryTerms();
        if (goEvidenceWithSecondaryTerms != null) {
            List<GoEvidenceObsoleteTermReport> goEvidenceSecondaryTermReports = new ArrayList<GoEvidenceObsoleteTermReport>(goEvidenceWithSecondaryTerms.size());
            for (MarkerGoTermEvidence goEvidence : goEvidenceWithSecondaryTerms) {
                GoEvidenceObsoleteTermReport report = new GoEvidenceObsoleteTermReport(goEvidence);
                report.setReplacementTermList(RepositoryFactory.getOntologyRepository().getReplacedByTerms(goEvidence.getGoTerm()));
                report.setConsiderTermList(RepositoryFactory.getOntologyRepository().getConsiderTerms(goEvidence.getGoTerm()));
                report.addObsoletedTerm(goEvidence.getGoTerm());
                goEvidenceSecondaryTermReports.add(report);
            }
            model.addAttribute("goEvidenceSecondaryTermReports", goEvidenceSecondaryTermReports);
            model.addAttribute("numberOfGoEvidenceOnSecondaryTerms", goEvidenceSecondaryTermReports.size());
        }

        return "ontology/secondary-term-report.page";
    }


}