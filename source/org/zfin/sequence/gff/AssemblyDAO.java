package org.zfin.sequence.gff;

import org.hibernate.Session;
import org.zfin.framework.dao.BaseSQLDAO;

public class AssemblyDAO extends BaseSQLDAO<Assembly> {

//    protected Session entityManager;

    public AssemblyDAO(Session entityManager) {
        super(Assembly.class);
        this.entityManager = entityManager;
    }

    public AssemblyDAO() {
        super(Assembly.class);
    }

}
