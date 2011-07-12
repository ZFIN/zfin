package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class serves the antibody search page.
 */
public class AntibodySearchController extends SimpleFormController {

    AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();

    /**
     * This sets the default filter values for the form
     *
     * @param request request object
     * @return form bean
     * @throws Exception
     */
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        AntibodySearchFormBean formBean = (AntibodySearchFormBean) super.formBackingObject(request);
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
        formBean.setAntibodyCriteria(antibodySearchCriteria);
        return formBean;
    }

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        AntibodySearchFormBean abFormBean = (AntibodySearchFormBean) command;

        //we will eventually return this map, basically just as a holder for the bean
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, abFormBean);
        return map;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        AntibodySearchFormBean antibodySearchFormBean = (AntibodySearchFormBean) command;
        AntibodySearchCriteria antibodyCriteria = antibodySearchFormBean.getAntibodyCriteria();
        antibodyCriteria.setPaginationBean(antibodySearchFormBean);
        antibodySearchFormBean.setQueryString(request.getQueryString());
        antibodySearchFormBean.setRequestUrl(request.getRequestURL());

        int numberOfRecords = antibodyRepository.getNumberOfAntibodies(antibodyCriteria);
        antibodySearchFormBean.setTotalRecords(numberOfRecords);
        List<Antibody> antibodies = antibodyRepository.getAntibodies(antibodyCriteria);
        if (numberOfRecords != 1) {
            antibodySearchFormBean.setAntibodies(antibodies);
            ModelAndView view = new ModelAndView(getSuccessView());
            view.addAllObjects(referenceData(request, command, errors));
            return view;
        } else { // equals 1
            ModelAndView view = new ModelAndView("/action/marker/view/"+antibodies.get(0).getZdbID());
            return  view ;
//            return AntibodyDetailController.getModelAndViewForSingleAntibody(antibodies.get(0), true);
        }
    }


    protected boolean isFormSubmission(HttpServletRequest request) {
        return request.getParameter(AntibodySearchFormBean.ACTION) != null &&
                StringUtils.equals(request.getParameter(AntibodySearchFormBean.ACTION), AntibodySearchFormBean.Type.SEARCH.toString());
    }
}