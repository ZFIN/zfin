package org.zfin.anatomy.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
@Controller
public class ExpressionPhenotypeReportController {

    private ExpressionRepository rep = RepositoryFactory.getExpressionRepository();
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private String reportType;

    @ModelAttribute("formBean")
    protected ExpressionPhenotypeReportBean getDefaultBean() {
        return new ExpressionPhenotypeReportBean();
    }


    @RequestMapping("/anatomy-expression-search")
    protected String showSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Anatomy Expression Search");
        return "anatomy/expression-report-form.page";
    }

    @RequestMapping("/anatomy-phenotype-search")
    protected String showPhenotypeSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Anatomy Phenotype Search");
        return "anatomy/phenotype-report-form.page";
    }

    @RequestMapping("/anatomy-go-evidence-search")
    protected String showGoEvidenceSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Anatomy Go Evidence Search");
        return "anatomy/go-evidence-report-form.page";
    }


    @RequestMapping(value = "/anatomy-expression-report-do-search")
    public String doExpressionSearch(Model model,
                                     @ModelAttribute("formBean") ExpressionPhenotypeReportBean expressionReportBean,
                                     BindingResult result
    ) throws Exception {
        List<GenericTerm> terms = getAllTermsToBeIncluded(expressionReportBean);
        expressionReportBean.setAllExpressions(rep.getExpressionsWithEntity(terms));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Anatomy Expression Search");
        return "anatomy/expression-report.page";
    }

    @RequestMapping(value = "/anatomy-phenotype-report-do-search")
    public String doPhenotypeSearch(Model model,
                                    @ModelAttribute("formBean") ExpressionPhenotypeReportBean expressionReportBean,
                                    BindingResult result
    ) throws Exception {
        List<GenericTerm> terms = getAllTermsToBeIncluded(expressionReportBean);
        expressionReportBean.setAllPhenotype(mutantRepository.getPhenotypeWithEntity(terms));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Anatomy Phenotype Search");
        return "anatomy/phenotype-report.page";
    }

    @RequestMapping(value = "/anatomy-go-evidence-report-do-search")
    public String doGoEvidenceSearch(Model model,
                                     @ModelAttribute("formBean") ExpressionPhenotypeReportBean expressionReportBean,
                                     BindingResult result
    ) throws Exception {
        List<GenericTerm> terms = getAllTermsToBeIncluded(expressionReportBean);
        expressionReportBean.setAllGoEvidences(mutantRepository.getMarkerGoEvidence(terms));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Anatomy Go Evidence Search");
        return "anatomy/go-evidence-report.page";
    }

    private List<GenericTerm> getAllTermsToBeIncluded(ExpressionPhenotypeReportBean expressionReportBean) {
        List<GenericTerm> allTerms = new ArrayList<GenericTerm>();
        for (String id : expressionReportBean.getTermIDs()) {
            GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByZdbID(id);
            if (term != null) {
                allTerms.add(term);
                if ((expressionReportBean.isIncludeSubstructures() && term.getOntology().equals(Ontology.ANATOMY))
                        || (expressionReportBean.isIncludeSubstructuresGo() && term.getOntology().equals(Ontology.GO_BP))
                        ) {
                    for (GenericTerm childTerm : RepositoryFactory.getOntologyRepository().getAllChildTerms(term)) {
                        allTerms.add(childTerm);
                    }
                }
            }
        }
        return allTerms;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    enum ReportType {
        EXPRESSION, PHENOTYPE, GO_EVIDENCE
    }
}
