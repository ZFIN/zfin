package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
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
 * Controller that serves meta information about the ontologies.
 */
@Controller
public class ObsoleteTermReportController {

    @Autowired
    private ExpressionService expressionService ;

    private static final Logger log = Logger.getLogger(ObsoleteTermReportController.class);

    @RequestMapping("/obsolete-term-report")
    private String createObsoleteTermReport(Model model) {
        OntologyBean form = new OntologyBean();
        model.addAttribute("formBean", form);
        List<PhenotypeStatement> phenotypesWithObsoletes = RepositoryFactory.getMutantRepository().getPhenotypesOnObsoletedTerms();
        if (phenotypesWithObsoletes != null) {
            List<PhenotypeObsoleteTermReport> phenotypeObsoleteTermReports = new ArrayList<PhenotypeObsoleteTermReport>(phenotypesWithObsoletes.size());
            for (PhenotypeStatement phenotypeStatement : phenotypesWithObsoletes) {
                PhenotypeObsoleteTermReport report = new PhenotypeObsoleteTermReport(phenotypeStatement);
                Set<GenericTerm> obsoletedTermSet = PhenotypeService.getObsoleteTerm(phenotypeStatement);
                report.setObsoletedTermList(obsoletedTermSet);
                if (obsoletedTermSet != null) {
                    for (GenericTerm obsoletedTerm : obsoletedTermSet) {
                        report.setReplacementTermList(RepositoryFactory.getOntologyRepository().getReplacedByTerms(obsoletedTerm));
                        report.setConsiderTermList(RepositoryFactory.getOntologyRepository().getConsiderTerms(obsoletedTerm));
                    }
                }
                phenotypeObsoleteTermReports.add(report);
            }
            model.addAttribute("phenotypeObsoleteTermReports", phenotypeObsoleteTermReports);
            model.addAttribute("numberOfObsoletedTermsPhenotype", phenotypesWithObsoletes.size());
        }
        List<ExpressionResult> expressionsWithObsoletes = RepositoryFactory.getExpressionRepository().getExpressionOnObsoletedTerms();
        if (expressionsWithObsoletes != null) {
            List<ExpressionObsoleteTermReport> expressionObsoleteTermReports = new ArrayList<ExpressionObsoleteTermReport>(expressionsWithObsoletes.size());
            for (ExpressionResult phenotypeStatement : expressionsWithObsoletes) {
                ExpressionObsoleteTermReport report = new ExpressionObsoleteTermReport(phenotypeStatement);
                Set<GenericTerm> obsoletedTermSet = expressionService.getObsoleteTerm(phenotypeStatement);
                report.setObsoletedTermList(obsoletedTermSet);
                if (obsoletedTermSet != null) {
                    for (GenericTerm obsoletedTerm : obsoletedTermSet) {
                        report.setReplacementTermList(RepositoryFactory.getOntologyRepository().getReplacedByTerms(obsoletedTerm));
                        report.setConsiderTermList(RepositoryFactory.getOntologyRepository().getConsiderTerms(obsoletedTerm));
                    }
                }
                expressionObsoleteTermReports.add(report);
            }
            model.addAttribute("expressionObsoleteTermReports", expressionObsoleteTermReports);
            model.addAttribute("numberOfObsoletedTermsExpression", expressionObsoleteTermReports.size());
        }
        List<MarkerGoTermEvidence> goEvidenceWithObsoletes = RepositoryFactory.getMutantRepository().getGoEvidenceOnObsoletedTerms();
        if (goEvidenceWithObsoletes != null) {
            List<GoEvidenceObsoleteTermReport> expressionObsoleteTermReports = new ArrayList<GoEvidenceObsoleteTermReport>(goEvidenceWithObsoletes.size());
            for (MarkerGoTermEvidence goEvidence : goEvidenceWithObsoletes) {
                GoEvidenceObsoleteTermReport report = new GoEvidenceObsoleteTermReport(goEvidence);
                report.setReplacementTermList(RepositoryFactory.getOntologyRepository().getReplacedByTerms(goEvidence.getGoTerm()));
                report.setConsiderTermList(RepositoryFactory.getOntologyRepository().getConsiderTerms(goEvidence.getGoTerm()));
                report.addObsoletedTerm(goEvidence.getGoTerm());
                expressionObsoleteTermReports.add(report);
            }
            model.addAttribute("goEvidenceObsoleteTermReport", expressionObsoleteTermReports);
            model.addAttribute("numberOfObsoletedTermsGoEvidences", expressionObsoleteTermReports.size());
        }

        return "ontology/obsolete-term-report.page";
    }


}