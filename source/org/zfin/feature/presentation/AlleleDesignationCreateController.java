package org.zfin.feature.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;

import javax.validation.Valid;

@Controller
@RequestMapping("/feature")
public class AlleleDesignationCreateController {

    private static Logger logger = LogManager.getLogger(AlleleDesignationCreateController.class);
    private static FeatureRepository fr = RepositoryFactory.getFeatureRepository();

    @ModelAttribute("formBean")
    private CreateAlleleDesignationFormBean getDefaultSearchForm() {
        return new CreateAlleleDesignationFormBean();
    }

    @RequestMapping("/alleleDesig-add-form")
    protected String showForm(Model model) throws Exception {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Line Designation");
        return "feature/alleleDesig-add-form.page";
    }

    @InitBinder("formBean")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new AlleleLineDesignationValidator());
    }

    @RequestMapping(value = "/alleleDesig-add-form", method = RequestMethod.POST)
    public String saveLabPrefix(@Valid @ModelAttribute("formBean") CreateAlleleDesignationFormBean formBean,
                                BindingResult result) throws Exception {

        if (result.hasErrors())
            return "feature/alleleDesig-add-form.page";
        String labPrefix = formBean.getLineDesig();

        try {
            HibernateUtil.createTransaction();
            fr.setNewLabPrefix(labPrefix, formBean.getLineLocation()).getPrefixString();
            HibernateUtil.flushAndCommitCurrentSession();

        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                logger.error("Error during roll back of transaction", he);
            }
            logger.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        return "redirect:/action/feature/line-designations";
    }


}



