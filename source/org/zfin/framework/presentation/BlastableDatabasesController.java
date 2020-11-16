package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.blast.Database;

import java.util.List;

@Controller
@RequestMapping(value = "/devtool")
public class BlastableDatabasesController {


    @RequestMapping("/blastable-databases")
    protected String showBlastableDb(@ModelAttribute("formBean") BlastableDatabasesBean blastableDatabasesBean,
                                     Model model) throws Exception {

        if (blastableDatabasesBean.getSelectedReferenceDatabaseZdbID() != null) {
            handleCommand(blastableDatabasesBean);
        }

        List<ReferenceDatabase> referenceDatabases = (List<ReferenceDatabase>) HibernateUtil.currentSession().createCriteria(ReferenceDatabase.class).list();
        blastableDatabasesBean.setReferenceDatabases(referenceDatabases);
        List<Database> databases = (List<Database>) HibernateUtil.currentSession().createCriteria(Database.class).list();
        blastableDatabasesBean.setDatabases(databases);
        blastableDatabasesBean.setDatabaseToAddZdbID(null);
        blastableDatabasesBean.setDatabaseToRemoveZdbID(null);
        blastableDatabasesBean.setDatabaseToSetAsPrimaryZdbID(null);
        blastableDatabasesBean.setSelectedReferenceDatabaseZdbID(null);

        return "dev-tools/blastable-databases";
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
