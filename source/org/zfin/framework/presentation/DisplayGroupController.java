package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.DisplayGroupRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.TreeSet;


public class DisplayGroupController extends AbstractCommandController {

    public DisplayGroupController() {
        setCommandClass(DisplayGroupBean.class);
    }

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        DisplayGroupBean formBean = (DisplayGroupBean) o;

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

        return modelAndView;
    }

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
