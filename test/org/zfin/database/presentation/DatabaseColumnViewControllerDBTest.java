package org.zfin.database.presentation;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.util.DatabaseJdbcStatement;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class DatabaseColumnViewControllerDBTest extends AbstractDatabaseTest {

    @Test
    public void createFKResultList() {
        DatabaseColumnViewController controller = new DatabaseColumnViewController();
/*
        List<ForeignKeyResult> resultList = controller.createFKResultList(Table.GENOTYPE_EXPERIMENT, "ZDB-GENOX-090731-5");
        assertNotNull(resultList);

        List<ForeignKeyResultFlattened> flattenedList = controller.getFlattenedView(resultList);
        assertNotNull(flattenedList);
*/

    }

    static {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

}
