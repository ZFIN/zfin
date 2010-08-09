package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.antibody.presentation.AntibodySearchFormBean;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.OntologyService;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ExpressionPhenotypeReportController extends SimpleFormController {

    private ExpressionRepository rep = RepositoryFactory.getExpressionRepository();
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private String reportType;

    /**
     * This sets the default filter values for the form
     *
     * @param request request object
     * @return form bean
     * @throws Exception
     */
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        ExpressionPhenotypeReportBean formBean = (ExpressionPhenotypeReportBean) super.formBackingObject(request);
        return formBean;
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        ExpressionPhenotypeReportBean abFormBean = (ExpressionPhenotypeReportBean) command;

        //we will eventually return this map, basically just as a holder for the bean
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, abFormBean);
        return map;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {
        ExpressionPhenotypeReportBean expressionReportBean = (ExpressionPhenotypeReportBean) command;
        List<Term> terms = getAllTermsToBeIncluded(expressionReportBean);
        if (reportType != null && reportType.equals(ReportType.EXPRESSION.toString()))
            expressionReportBean.setAllExpressions(rep.getExpressionsWithEntity(terms));
        if (reportType != null && reportType.equals(ReportType.PHENOTYPE.toString()))
            expressionReportBean.setAllPhenotype(mutantRepository.getPhenotypeWithEntity(terms));
        if (reportType != null && reportType.equals(ReportType.GO_EVIDENCE.toString()))
            expressionReportBean.setAllGoEvidences(mutantRepository.getMarkerGoEvidence(terms));
        ModelAndView view = new ModelAndView(getSuccessView());
        view.addAllObjects(referenceData(request, command, errors));
        return view;
    }

    private List<Term> getAllTermsToBeIncluded(ExpressionPhenotypeReportBean expressionReportBean) {
        List<Term> allTerms = new ArrayList<Term>(50);
        for (String id : expressionReportBean.getTermIDs()) {
            Term term = OntologyManager.getInstance().getTermByID(id);
            if (term != null) {
                allTerms.add(term);
                if (expressionReportBean.isIncludeSubstructures() && term.getOntology().equals(Ontology.ANATOMY))
                    allTerms.addAll(OntologyService.getAllChildren(term));
                if (expressionReportBean.isIncludeSubstructuresGo() && term.getOntology().equals(Ontology.GO_BP))
                    allTerms.addAll(OntologyService.getAllChildren(term));
            }
        }
        return allTerms;
    }

    protected boolean isFormSubmission(HttpServletRequest request) {
        return request.getParameter(AntibodySearchFormBean.ACTION) != null &&
                StringUtils.equals(request.getParameter(AntibodySearchFormBean.ACTION), AntibodySearchFormBean.Type.SEARCH.toString());
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
