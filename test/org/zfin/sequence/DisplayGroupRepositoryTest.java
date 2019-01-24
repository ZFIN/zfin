package org.zfin.sequence;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * Class SequenceRepositoryTest.
 */

public class DisplayGroupRepositoryTest extends AbstractDatabaseTest {

    @Test
    public void testDisplayGroupMapping() {
        Session session = HibernateUtil.currentSession();
        Query<DisplayGroup> query = session.createQuery("from DisplayGroup dg where dg.groupName = :groupName ", DisplayGroup.class);
        query.setParameter("groupName", DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE.toString());
        List<DisplayGroup> displayGroups = query.list();
        for (DisplayGroup displayGroup : displayGroups) {
            assertEquals(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE, displayGroup.getGroupName());
        }
    }

    @Test
    public void dblinkIsInDisplayGroup() {
        Session session = HibernateUtil.currentSession();
        String hql = "" +
                "select dbl from DBLink dbl join dbl.referenceDatabase rdb " +
                "join rdb.displayGroups dg where dg.groupName = :groupName " +
                "";
        Query query = session.createQuery(hql);
        query.setParameter("groupName", DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE.toString());
        query.setMaxResults(3); // test 3
        List<DBLink> dbLinks = query.list();
        for (DBLink dbLink : dbLinks) {
            assertTrue("is in display group", dbLink.isInDisplayGroup(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE));
            assertFalse("is NOT in display group", dbLink.isInDisplayGroup(DisplayGroup.GroupName.MICRORNA_TARGETS));
        }
    }


    @Test
    public void referenceDatabaseIsInDisplayGroup() {
        Session session = HibernateUtil.currentSession();
        String hql = "" +
                "select rdb from ReferenceDatabase rdb " +
                "join rdb.displayGroups dg where dg.groupName = :groupName " +
                "";
        Query query = session.createQuery(hql);
        query.setParameter("groupName", DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE.toString());
        List<ReferenceDatabase> referenceDatabases = query.list();
        for (ReferenceDatabase referenceDatabase : referenceDatabases) {
            assertTrue("is in display group", referenceDatabase.isInDisplayGroup(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE));
            assertFalse("is NOT in display group", referenceDatabase.isInDisplayGroup(DisplayGroup.GroupName.MICRORNA_TARGETS));
        }
    }


    /**
     * Verify that the proper reference databases are returned for the display group.
     */
    @Test
    public void getDisplayGroupsForReferenceDatabase() {
        Session session = HibernateUtil.currentSession();

        String hql = "select rd from DisplayGroup dg join dg.referenceDatabases rd where rd.foreignDB.dbName = :dbName ";
        Query query = session.createQuery(hql);
        query.setParameter("dbName", ForeignDB.AvailableName.GENPEPT.toString());
        // display groups from query
        List<ReferenceDatabase> referenceDatabasesFromQuery = query.list();

        String hql2 = "select dg from DisplayGroup dg where dg.groupName = :groupName ";
        Query query2 = session.createQuery(hql2);
        query2.setParameter("groupName", DisplayGroup.GroupName.DBLINK_ADDING_ON_TRANSCRIPT_EDIT.toString());
        DisplayGroup displayGroup = (DisplayGroup) query2.uniqueResult();
        Set<ReferenceDatabase> referenceDatabasesFromDisplayGroup = displayGroup.getReferenceDatabases();

        DisplayGroup stemLoopDisplayGroup = RepositoryFactory.getDisplayGroupRepository().getDisplayGroupByName(DisplayGroup.GroupName.DBLINK_ADDING_ON_TRANSCRIPT_EDIT);
        assertEquals(displayGroup.getId(), stemLoopDisplayGroup.getId());

        // assumes that MIRBASE is in the STEM_LOOP display group, which should be correct
        for (ReferenceDatabase referenceDatabase : referenceDatabasesFromQuery) {
            assertTrue("Every reference database should be contained", referenceDatabasesFromDisplayGroup.contains(referenceDatabase));
        }
    }

    @Test
    public void getReferenceDatabasesForDisplayGroup() {

        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName.MICRORNA_TARGETS);
        assertTrue("Should have at least one database", referenceDatabases.size() > 0);
        for (ReferenceDatabase referenceDatabase : referenceDatabases) {
            assertTrue("Reference database should be in same reference", referenceDatabase.isInDisplayGroup(DisplayGroup.GroupName.MICRORNA_TARGETS));
        }

    }

}
