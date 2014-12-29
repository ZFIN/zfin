package org.zfin.orthology.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.orthology.Orthologs;
import org.zfin.orthology.OrthologyCriteriaService;
import org.zfin.orthology.SpeciesCriteria;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Controller class that takes the orthology search parameters and retrieves all orthologies.
 */
@Controller
@RequestMapping("/orthology")
public class OrthologySearchController {


    private OrthologyRepository orthologyRepository = RepositoryFactory.getOrthologyRepository();

    @Autowired
    private OrthologyWebSearchFormValidator validator;

    @ModelAttribute("formBean")
    private OrthologySearchBean getDefaultBean() {
        return new OrthologySearchBean();
    }

    // method that is called on a POST request (from submission).

    @RequestMapping("/search")
    protected String showOrthologyForm(@ModelAttribute("formBean") OrthologySearchBean formBean) throws Exception {
        return "orthology-search.page";
    }

    @RequestMapping("/do-search")
    protected String doOrthologySearch(@ModelAttribute("formBean") OrthologySearchBean formBean,
                                       BindingResult bindingResult) throws Exception {

        validator.validate(formBean, bindingResult);
        if (bindingResult.hasErrors())
            return "orthology-search.page";
        List<SpeciesCriteriaBean> criteriaBeans = formBean.getCriteria();
        List<SpeciesCriteria> speciesCriteria = OrthologyCriteriaService.getSpeciesCriteria(criteriaBeans);
        Object[] results = orthologyRepository.getOrthologies(speciesCriteria, formBean.getBasicCriteria());
        if (formBean.getFirstRecord() == 1) {
            formBean.setTotalRecords(Integer.parseInt(results[1].toString()));
        }
        formBean.setOrthologies((List<Orthologs>) results[0]);
        return "orthology-searchresult.page";
    }

    /*
     * Add the list of gene symbol filter types to the ModelAndView.
     */
    @ModelAttribute("geneSymbolValues")
    protected List<String> populateGeneSymbols() {
        return getDefaultBean().getGeneSymbolValues();
    }

    @ModelAttribute("chromosomeFilterValues")
    protected List<String> referenceData() {
        return (List<String>) getDefaultBean().getChromosomeFilterValues();
    }

}
