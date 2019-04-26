package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.DisplayGroupRepository;

import java.util.Set;
import java.util.TreeSet;


@Controller
@RequestMapping(value = "/devtool")
public class DisplayGroupController {

    @RequestMapping("/display-groups")
    protected String showPanelDetail(@ModelAttribute("formBean") DisplayGroupBean formBean,
                                     Model model) throws Exception {
        if (formBean.getDisplayGroupToEditID() != null) {
            handleCommand(formBean);
        }

        DisplayGroupRepository dgRepository = RepositoryFactory.getDisplayGroupRepository();

        Set<DisplayGroup> displayGroups = new TreeSet<DisplayGroup>();

        for (DisplayGroup.GroupName dgName : DisplayGroup.GroupName.values()) {
            DisplayGroup dg = dgRepository.getDisplayGroupByName(dgName);
            displayGroups.add(dg);
        }

        for (DisplayGroup dg : displayGroups) {
            for (ReferenceDatabase refDB : dg.getReferenceDatabases())
                logger.debug(dg.getGroupName() + " has " + refDB.getForeignDB().getDbName());
        }

        formBean.setDisplayGroups(displayGroups);

        formBean.setReferenceDatabases(new TreeSet<ReferenceDatabase>(HibernateUtil.currentSession().createCriteria(ReferenceDatabase.class).list()));

        ModelAndView modelAndView = new ModelAndView("display-groups.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, formBean);

        formBean.clear();
        return "dev-tools/display-groups.page";
    }

    private Logger logger = LogManager.getLogger(DisplayGroupController.class);

    private void handleCommand(DisplayGroupBean formBean) {
        DisplayGroup displayGroup = (DisplayGroup) HibernateUtil.currentSession().get(DisplayGroup.class, formBean.getDisplayGroupToEditID());
        HibernateUtil.createTransaction();
        if (StringUtils.isNotEmpty(formBean.getReferenceDatabaseToAddZdbID())) {
            displayGroup.getReferenceDatabases().add((ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, formBean.getReferenceDatabaseToAddZdbID()));
        } else if (StringUtils.isNotEmpty(formBean.getReferenceDatabaseToRemoveZdbID())) {
            displayGroup.getReferenceDatabases().remove(HibernateUtil.currentSession().get(ReferenceDatabase.class, formBean.getReferenceDatabaseToRemoveZdbID()));
        }
        HibernateUtil.currentSession().update(displayGroup);
        HibernateUtil.flushAndCommitCurrentSession();
    }
}
