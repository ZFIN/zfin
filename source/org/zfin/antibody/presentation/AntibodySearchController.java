package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.antibody.presentation.AntibodySearchFormBean;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class serves the antibody search page.
 */
@Controller
public class AntibodySearchController {

    AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();

    @ModelAttribute("formBean")
    private AntibodySearchFormBean getDefaultSearchForm() {
        AntibodySearchFormBean formBean = new AntibodySearchFormBean();
        AntibodySearchCriteria antibodySearchCriteria = new AntibodySearchCriteria();
        antibodySearchCriteria.setClonalType(AntibodyType.ANY.getValue());
        antibodySearchCriteria.setIncludeSubstructures(true);
        antibodySearchCriteria.setAnatomyEveryTerm(true);
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID(DevelopmentStage.ZYGOTE_STAGE_ZDB_ID);
        start.setName(DevelopmentStage.ZYGOTE_STAGE);
        antibodySearchCriteria.setStartStage(start);
        DevelopmentStage end = new DevelopmentStage();
        end.setZdbID(DevelopmentStage.ADULT_STAGE_ZDB_ID);
        end.setName(DevelopmentStage.ADULT_STAGE);
        antibodySearchCriteria.setEndStage(end);
        antibodySearchCriteria.setAntigenNameFilterType(FilterType.CONTAINS);
        antibodySearchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);
        formBean.setAntibodyCriteria(antibodySearchCriteria);
        return formBean;
    }

    @RequestMapping("/antibody-search")
    protected String showSearchForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Antibody Search");
        return "antibody/antibody-search-form.page";
    }

    private @Autowired
    HttpServletRequest request;

    @RequestMapping(value = "/antibody-do-search", method = RequestMethod.GET)
    public String doSearch(Model model,
                              @ModelAttribute("formBean") AntibodySearchFormBean antibodySearchFormBean,
                              BindingResult result
    ) throws Exception {
        AntibodySearchCriteria antibodySearchCriteria = antibodySearchFormBean.getAntibodyCriteria();
        antibodySearchCriteria.setPaginationBean(antibodySearchFormBean);
        model.addAttribute(LookupStrings.FORM_BEAN, antibodySearchFormBean);
        antibodySearchFormBean.setQueryString(request.getQueryString());
        antibodySearchFormBean.setRequestUrl(request.getRequestURL());
        int numberOfRecords = antibodyRepository.getNumberOfAntibodies(antibodySearchCriteria);
        antibodySearchFormBean.setTotalRecords(numberOfRecords);
        List<Antibody> antibodies = antibodyRepository.getAntibodies(antibodySearchCriteria);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Antibody Search");
        if (numberOfRecords != 1) {
            antibodySearchFormBean.setAntibodies(antibodies);
            return "antibody/antibody-search-result.page";
        } else {
            return  "redirect:/action/marker/view/"+antibodies.get(0).getZdbID() ;
        }
    }

}