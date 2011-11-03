package org.zfin.feature.presentation;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.zfin.feature.presentation.AlleleLineDesignationValidator;
import org.zfin.framework.HibernateUtil;

import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class AlleleDesigCreateController {

    private static Logger LOG = Logger.getLogger(AlleleDesigCreateController.class);
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

    @ModelAttribute("formBean")
    private CreateAlleleDesigFormBean getDefaultSearchForm() {
        return new CreateAlleleDesigFormBean();
    }

    @RequestMapping("/alleleDesig-add-form")
    protected String showForm() throws Exception {

        return "feature/alleleDesig-add-form.page";
    }

    private @Autowired
    HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new AlleleLineDesignationValidator());
    }

    @RequestMapping(value = "/alleleDesig-add-form", method = RequestMethod.POST)
    public String saveLabPrefix(Model model,
                              @Valid @ModelAttribute("formBean") CreateAlleleDesigFormBean formBean,
                              BindingResult result) throws Exception {

        if(result.hasErrors())
            return "feature/alleleDesig-add-form.page";
        String labPrefix = formBean.getLineDesig();

try{
            HibernateUtil.createTransaction();
             RepositoryFactory.getFeatureRepository().setNewLabPrefix(labPrefix, formBean.getLineLocation()).getPrefixString();
             HibernateUtil.flushAndCommitCurrentSession();

        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        return "redirect:/action/feature/line-designations";
        }





}



