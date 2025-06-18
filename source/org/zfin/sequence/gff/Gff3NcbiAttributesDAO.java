package org.zfin.sequence.gff;

import org.hibernate.Session;
import org.zfin.framework.dao.BaseSQLDAO;

public class Gff3NcbiAttributesDAO extends BaseSQLDAO<Gff3NcbiAttributePair> {

    protected Session entityManager;

    public Gff3NcbiAttributesDAO(Session entityManager) {
        super(Gff3NcbiAttributePair.class);
        this.entityManager = entityManager;
    }

    public Gff3NcbiAttributesDAO() {
        super(Gff3NcbiAttributePair.class);
    }

}
