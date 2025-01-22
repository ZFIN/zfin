package org.zfin.sequence.blast.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.blast.BlastRegenerationCache;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.DatabaseRelationship;
import org.zfin.sequence.blast.Origination;

import java.util.*;

/**
 *
 */
public class HibernateBlastRepository implements BlastRepository {

    public static Logger logger = LogManager.getLogger(HibernateBlastRepository.class);

    public Database getDatabase(Database.AvailableAbbrev blastDatabaseAvailableAbbrev) {
        Session session = HibernateUtil.currentSession();
        Query<Database> query = session.createQuery("from Database where abbrev = :abbrev", Database.class);
        query.setParameter("abbrev", blastDatabaseAvailableAbbrev);
        Database database = query.uniqueResult();
        session.refresh(database);
        return database;
    }

    public Origination getOrigination(Origination.Type type) {
        Session session = HibernateUtil.currentSession();
        String hql = " select o from Origination o where o.type = :type";
        Query<Origination> query = session.createQuery(hql, Origination.class);
        query.setParameter("type", type);
        return query.uniqueResult();
    }

    public List<Database> getDatabases(Database.Type type) {
        return getDatabases(type, false, false);
    }

    public List<Database> getDatabases(Database.Type type, boolean excludePrivate, boolean excludeExternal) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            from Database
            """;
        List<String> hqlClauses = new ArrayList<>();
        HashMap<String, Object> parameterMap = new HashMap<>();

        if (type != null) {
            hqlClauses.add("type = :type");
            parameterMap.put("type", type);
        }
        if (excludePrivate) {
            hqlClauses.add("publicDatabase = true");
        }
        if (excludeExternal) {
            hqlClauses.add("origination != :origination");
            parameterMap.put("origination", getOrigination(Origination.Type.EXTERNAL));
        }
        hql += " where " + String.join(" and ", hqlClauses);
        hql += " order by name";
        Query<Database> query = session.createQuery(hql, Database.class);
        parameterMap.forEach(query::setParameter);
        return query.list();
    }

    public List<Database> getDatabaseByOrigination(Origination.Type... originationType) {
        Session session = HibernateUtil.currentSession();
        String hql = " select d from Database d where d.origination.type in  (:type)";
        Query<Database> query = session.createQuery(hql, Database.class);
        query.setParameterList("type", originationType);
        return query.list();
    }

    public List<DatabaseRelationship> getChildDatabaseRelationshipsByOrigination(Origination.Type originationType) {
        Session session = HibernateUtil.currentSession();
        String hql = " select dr from DatabaseRelationship dr  where dr.child.origination.type = :type";
        Query<DatabaseRelationship> query = session.createQuery(hql, DatabaseRelationship.class);
        query.setParameter("type", originationType);
        return query.list();
    }

    public Set<String> getAllValidAccessionNumbers(Database database) {
        Set<String> returnAccessions = new HashSet<>();

        String hql1 = "select dbl.accessionNumber " +
                      " from DBLink dbl join dbl.referenceDatabase rd join rd.primaryBlastDatabase bd " +
                      " where bd.zdbID = :databaseZdbID";
        Query query1 = HibernateUtil.currentSession().createQuery(hql1);
        query1.setParameter("databaseZdbID", database.getZdbID());
        returnAccessions.addAll(query1.list());

        String hql2 = "select acc.number" +
                      " from Accession  acc join acc.referenceDatabase rd join rd.primaryBlastDatabase bd " +
                      " where bd.zdbID = :databaseZdbID";
        Query query2 = HibernateUtil.currentSession().createQuery(hql2);
        query2.setParameter("databaseZdbID", database.getZdbID());
        List accessionBankList = query2.list();
        Set<String> onlyAccessionBankList = new HashSet<>();
        onlyAccessionBankList.addAll((accessionBankList));
        for (String accFromDbLink : returnAccessions)
            onlyAccessionBankList.remove(accFromDbLink);
        if (onlyAccessionBankList.size() > 0)
            logger.warn("Accession numbers in accession_bank not found in DB_LINK: " + onlyAccessionBankList);

        returnAccessions.addAll(accessionBankList);
        return returnAccessions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getPreviousAccessionsForDatabase(Database database) {
        String hql = " select brc.accession from BlastRegenerationCache brc" +
                     " where brc.blastDatabase = :database " +
                     " ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("database", database);
        return query.list();
    }


    public Map<String, Integer> getValidAccessionCountsForAllBlastDatabases() {

        String sql = " select bdb.blastdb_abbrev as abbrev, count(distinct dbl.dblink_acc_num) as num " +
                     " from db_link dbl " +
                     " join foreign_db_contains fdbc " +
                     "    on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id  " +
                     " join blast_database bdb " +
                     "    on fdbc.fdbcont_primary_blastdb_zdb_id=bdb.blastdb_zdb_id " +
                     " join blast_database_origination_type orig " +
                     "    on bdb.blastdb_origination_id=orig.bdot_pk_id" +
                     " where orig.bdot_type != :originationType " +
                     "  group by bdb.blastdb_abbrev ";

        Query query = HibernateUtil.currentSession()
            .createNativeQuery(sql)
            .addScalar("abbrev", StandardBasicTypes.STRING)
            .addScalar("num", StandardBasicTypes.LONG);
        query.setParameter("originationType", Origination.Type.EXTERNAL.toString()); //NativeQuery so originationType is a String
        List<Object[]> blastDatabaseCounts = query.list();

        if (blastDatabaseCounts == null)
            return null;

        Map<String, Integer> accessionCountMap = new HashMap<>();

        for (Object[] o : blastDatabaseCounts) {
            String blastDatabaseAbbrev = (String) o[0];
            Integer accessionCount = ((Long) o[1]).intValue();
            accessionCountMap.put(blastDatabaseAbbrev, accessionCount);
        }


        // repeat the pattern above for the accession bank blast databases
/*

        sql =  " select bdb.blastdb_abbrev, count(ab.accbk_acc_num) " +
                " from accession_bank ab " +
                " join foreign_db_contains fdbc " +
                "    on ab.accbk_fdbcont_zdb_id=fdbc.fdbcont_zdb_id  " +
                " join blast_database bdb " +
                "    on fdbc.fdbcont_primary_blastdb_zdb_id=bdb.blastdb_zdb_id " +
                "  group by bdb.blastdb_abbrev ";
        query = HibernateUtil.currentSession().createNativeQuery(sql);
        blastDatabaseCounts = query.list();

        if (blastDatabaseCounts == null)
            return null;

        for (Object[] o : blastDatabaseCounts) {
            String blastDatabaseAbbrev = (String)o[0];
            Integer accessionCount = ((BigDecimal)o[1]).intValue();

            accessionCountMap.put(blastDatabaseAbbrev, accessionCount);
        }
*/


        //now handle databases that don't show up in either
        List<Database> databases = getDatabaseByOrigination(Origination.Type.CURATED, Origination.Type.LOADED, Origination.Type.MARKERSEQUENCE);
        for (Database database : databases) {
            if (!accessionCountMap.containsKey(database.getAbbrev().toString())) {
                //if they weren't in either query, they must have 0 dblinks.  
                accessionCountMap.put(database.getAbbrev().toString(), 0);
            }
        }


        return accessionCountMap;

    }

    /**
     * Number of valid accessions.  The other method is too memory intensive.
     * Must implement in SQL to get a proper union.
     *
     * @param database Database to retrieve number of accessions for.
     * @return Number of combined unique accessions between accession bank and dblink.
     */
    public Integer getNumberValidAccessionNumbers(Database database) {
        String sql = "" +
                     "select dbl.dblink_acc_num " +
                     "from db_link dbl" +
                     " join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
                     " join blast_database bdb on fdbc.fdbcont_primary_blastdb_zdb_id=bdb.blastdb_zdb_id " +
                     " where bdb.blastdb_zdb_id = :databaseZdbID ";
        Query query2 = HibernateUtil.currentSession().createNativeQuery(sql);
        query2.setParameter("databaseZdbID", database.getZdbID());
        ScrollableResults results = query2.scroll();


        logger.debug("running slow individual blast database dblink count query");

        results.last();

        return results.getRowNumber() + 1;
    }

    public int setAllDatabaseLock(boolean isLocked) {
        Session session = HibernateUtil.currentSession();
        String hql = " update Database d set d.locked = :locked ";
        Query query = session.createQuery(hql);
        query.setParameter("locked", isLocked);
        return query.executeUpdate();
    }

    @Override
    public void addPreviousAccessions(Database database, Collection<String> accessionsToAdd) {
        if (CollectionUtils.isEmpty(accessionsToAdd)) {
            return;
        }
        for (String accessionToAdd : accessionsToAdd) {
            BlastRegenerationCache blastRegenerationCache = new BlastRegenerationCache();
            blastRegenerationCache.setAccession(accessionToAdd);
            blastRegenerationCache.setBlastDatabase(database);
            HibernateUtil.currentSession().save(blastRegenerationCache);
        }
        HibernateUtil.currentSession().flush();
    }

    @Override
    public void removePreviousAccessions(Database database, Collection<String> accessionToRemove) {
        if (CollectionUtils.isEmpty(accessionToRemove)) {
            return;
        }
        String hql = "delete BlastRegenerationCache brc where brc.accession in (:accessions) ";
        HibernateUtil.currentSession().createQuery(hql)
            .setParameterList("accessions", accessionToRemove)
            .executeUpdate();
    }

}
