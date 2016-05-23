package org.zfin.infrastructure.presentation;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Controller
@RequestMapping("/infrastructure")
public class ControlledVocabularyAddController {

    private static Logger LOG = Logger.getLogger(ControlledVocabularyAddController.class);

    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

    @ModelAttribute("formBean")
    private ControlledVocabularyAddBean getDefaultSearchForm() {
        ControlledVocabularyAddBean controlledVocabularyAddBean = new ControlledVocabularyAddBean();

        return controlledVocabularyAddBean;
    }

    @Autowired
    private HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new ControlledVocabularyAddBeanValidator());
    }

    @RequestMapping(value = "/controlled-vocabulary-add", method = RequestMethod.GET)
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Controlled Vocabulary");
        return "infrastructure/controlled-vocabulary-add.page";
    }

    @RequestMapping(value = "/controlled-vocabulary-add", method = RequestMethod.POST)
    public String addControlledVocabulary(Model model,
                                          @Valid @ModelAttribute("formBean") ControlledVocabularyAddBean formBean,
                                          BindingResult result) throws Exception {

        if (result.hasErrors()) {
            return showForm(model);
        }

        ControlledVocab newControlledVocab = new ControlledVocab();

        // capitalze the first letter for term name
        formBean.setTermName(WordUtils.capitalize(formBean.getTermName()));
        newControlledVocab.setCvTermName(formBean.getTermName());
        newControlledVocab.setCvForeignSpecies(formBean.getForeignSpecies());
        newControlledVocab.setCvNameDefinition(formBean.getNameDefinition());

        try {
            HibernateUtil.createTransaction();

            if (!ProfileService.getCurrentSecurityUser().getAccountInfo().getRoot()) {
                throw new RuntimeException("Non-root user cannot create a controlled_vocabulary");
            }

            if (newControlledVocab == null) {
                throw new RuntimeException("No marker object provided.");
            }

            currentSession().save(newControlledVocab);
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

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Controlled Vocabulary record added");

        ControlledVocab newlyCreatedControlledVocab = getInfrastructureRepository().getControlledVocabByNameAndSpecies(formBean.getTermName(), formBean.getForeignSpecies());

        model.addAttribute("newlyCreatedControlledVocab", newlyCreatedControlledVocab);

        return "infrastructure/controlled-vocabulary-added.page";
    }
}
