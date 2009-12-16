package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.blast.Database;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 */
public class BlastableDatabasesController extends AbstractCommandController {

    public BlastableDatabasesController() {
        setCommandClass(BlastableDatabasesBean.class);
    }

    protected ModelAndView handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, BindException e) throws Exception {
        BlastableDatabasesBean blastableDatabasesBean = (BlastableDatabasesBean) o;

        if (blastableDatabasesBean.getSelectedReferenceDatabaseZdbID() != null) {
            handleCommand(blastableDatabasesBean);
        }

        List<ReferenceDatabase> referenceDatabases = (List<ReferenceDatabase>) HibernateUtil.currentSession().createCriteria(ReferenceDatabase.class).list();
        blastableDatabasesBean.setReferenceDatabases(referenceDatabases);
        List<Database> databases = (List<Database>) HibernateUtil.currentSession().createCriteria(Database.class).list();
        blastableDatabasesBean.setDatabases(databases);
        ModelAndView modelAndView = new ModelAndView("blastable-databases.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, blastableDatabasesBean);

        blastableDatabasesBean.setDatabaseToAddZdbID(null);
        blastableDatabasesBean.setDatabaseToRemoveZdbID(null);
        blastableDatabasesBean.setDatabaseToSetAsPrimaryZdbID(null);
        blastableDatabasesBean.setSelectedReferenceDatabaseZdbID(null);


        return modelAndView;
    }

    private void handleCommand(BlastableDatabasesBean blastableDatabasesBean) {
        ReferenceDatabase selectecReferenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, blastableDatabasesBean.getSelectedReferenceDatabaseZdbID());

        HibernateUtil.createTransaction();
        // these just happen no matter what
        if (StringUtils.isNotEmpty(blastableDatabasesBean.getDatabaseToRemoveZdbID())) {
            selectecReferenceDatabase.getRelatedBlastDbs().remove(HibernateUtil.currentSession().get(Database.class, blastableDatabasesBean.getDatabaseToRemoveZdbID()));
        }

        // these just happen no matter what
        if (StringUtils.isNotEmpty(blastableDatabasesBean.getDatabaseToAddZdbID())) {
            selectecReferenceDatabase.getRelatedBlastDbs().add((Database) HibernateUtil.currentSession().get(Database.class, blastableDatabasesBean.getDatabaseToAddZdbID()));
        }

        if (selectecReferenceDatabase.getPrimaryBlastDatabase() == null
                &&
                StringUtils.isNotEmpty(blastableDatabasesBean.getDatabaseToSetAsPrimaryZdbID())
                ) {
            selectecReferenceDatabase.setPrimaryBlastDatabase((Database) HibernateUtil.currentSession().get(Database.class, blastableDatabasesBean.getDatabaseToSetAsPrimaryZdbID()));
        } else if (selectecReferenceDatabase.getPrimaryBlastDatabase() != null
                &&
                StringUtils.isEmpty(blastableDatabasesBean.getDatabaseToSetAsPrimaryZdbID())
                ) {
            selectecReferenceDatabase.setPrimaryBlastDatabase(null);
        }
        HibernateUtil.currentSession().update(selectecReferenceDatabase);
        HibernateUtil.flushAndCommitCurrentSession();


    }

}
