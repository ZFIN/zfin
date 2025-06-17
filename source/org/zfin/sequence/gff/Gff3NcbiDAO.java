package org.zfin.sequence.gff;

import org.hibernate.Session;
import org.zfin.framework.dao.BaseSQLDAO;

public class Gff3NcbiDAO extends BaseSQLDAO<Gff3Ncbi> {

    protected Session entityManager;

    public Gff3NcbiDAO(Session entityManager) {
        super(Gff3Ncbi.class);
        this.entityManager = entityManager;
    }

    public Gff3NcbiDAO() {
        super(Gff3Ncbi.class);
    }

}
