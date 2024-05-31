package org.zfin.sequence;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.hibernate.Session;
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

    private Logger logger = LogManager.getLogger(DisplayGroupRepositoryTest.class);

    @Test
    public void testDisplayGroupMapping() {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("from DisplayGroup dg where dg.groupName = :groupName ");
        query.setParameter("groupName", DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
        List<DisplayGroup> displayGroups = query.list();
        for (DisplayGroup displayGroup : displayGroups) {
            assertEquals(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE, displayGroup.getGroupName());
        }
    }

    @Test
    public void testDisplayGroupData() {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select dgm.referenceDatabase 
            from DisplayGroup dg 
            join dg.displayGroupMembers dgm
            where dg.groupName = :groupName
        """;

        Query query = session.createQuery(hql);
        query.setParameter("groupName", DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
        List<ReferenceDatabase> referenceDatabaseList = query.list();
        for (ReferenceDatabase referenceDatabase : referenceDatabaseList) {
            boolean hasDisplayGroup = false;
            Set<DisplayGroup> displayGroups = referenceDatabase.getDisplayGroups();
//            logger.info("# of display groups: "+ displayGroups.size());
            for (DisplayGroup displayGroup : displayGroups) {
                if (displayGroup.getGroupName().equals(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE)) {
                    hasDisplayGroup = true;
                }
            }
            assertTrue("has displayed group", hasDisplayGroup);
        }
    }


    @Test
    public void dblinkIsInDisplayGroup() {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select dbl 
            from DBLink dbl 
            join dbl.referenceDatabase rdb 
            join DisplayGroupMember dgm on rdb = dgm.referenceDatabase 
            join dgm.displayGroup dg 
            where dg.groupName = :groupName
        """;

        Query query = session.createQuery(hql);
        query.setParameter("groupName", DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
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
        String hql = """
            select rdb 
            from ReferenceDatabase rdb 
            join DisplayGroupMember dgm on rdb = dgm.referenceDatabase 
            join dgm.displayGroup dg 
            where dg.groupName = :groupName
        """;

        Query query = session.createQuery(hql);
        query.setParameter("groupName", DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
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

        String hql = """
            select dgm.referenceDatabase 
            from DisplayGroup dg 
            join dg.displayGroupMembers dgm
            where dgm.referenceDatabase.foreignDB.dbName = :dbName
        """;
        Query query = session.createQuery(hql);
        query.setParameter("dbName", ForeignDB.AvailableName.GENPEPT);
        // display groups from query
        List<ReferenceDatabase> referenceDatabasesFromQuery = query.list();

        String hql2 = "select dg from DisplayGroup dg where dg.groupName = :groupName ";
        Query query2 = session.createQuery(hql2);
        query2.setParameter("groupName", DisplayGroup.GroupName.DBLINK_ADDING_ON_TRANSCRIPT_EDIT);
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
