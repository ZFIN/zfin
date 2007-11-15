package org.zfin.orthology.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.ui.ModelMap;
import org.zfin.orthology.Orthologs;
import org.zfin.orthology.SpeciesCriteria;
import org.zfin.orthology.OrthologyCriteriaService;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Controller class that takes the orthology search parameters and retrieves all orthologies.
 */

public class OrthologySearchController extends SimpleFormController {

    // This object is set through IoC of Spring
    private OrthologyRepository or;
    private OrthologySearchBean searchBean = new OrthologySearchBean();

    public OrthologySearchController() {
        setCommandClass(OrthologySearchBean.class);

    }

    // method that is called on a POST request (from submission).
    protected ModelAndView onSubmit(HttpServletRequest request,	HttpServletResponse response, Object command,
                                    BindException errors) throws Exception{
        OrthologySearchBean formBean = (OrthologySearchBean) command;

        List<SpeciesCriteriaBean> criteriaBeans = formBean.getCriteria();
        List<SpeciesCriteria> speciesCriteria = OrthologyCriteriaService.getSpeciesCriteria(criteriaBeans);
        Object[] results = or.getOrthologies(speciesCriteria, formBean.getBasicCriteria());
        if (formBean.getFirstRecord() == 1) {
            formBean.setTotalRecords((Integer) results[1]);
        }
        formBean.setOrthologies((List<Orthologs>) results[0]);
        String sessionBeanName = getFormSessionAttributeName();
        // ToDo: Spring removes the form bean from the session and does not put it back into it.
        // Should not be necessary.
/*
        if(isSessionForm()){
           request.getSession().setAttribute(sessionBeanName, formBean);
        }
*/
        showForm(request, response, errors);
        return new ModelAndView(getSuccessView(), LookupStrings.FORM_BEAN, formBean);
    }

    /*
     * Add the list of gene symbol filter types to the ModelAndView.
     */
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        ModelMap modelMap = new ModelMap("geneSymbolValues", searchBean.getGeneSymbolValues());
        modelMap.addObject("chromosomeFilterValues", searchBean.getChromosomeFilterValues());
        return modelMap;
    }

    public void setOrthologyRepository(OrthologyRepository or) {
        this.or = or;
    }

    public OrthologyRepository getOrthologyRepository() {
        return or;
    }
}
