package org.zfin.sequence.blast.repository;

import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.DatabaseRelationship;
import org.zfin.sequence.blast.Origination;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class HibernateBlastRepository implements BlastRepository{

    public Database getDatabase(Database.AvailableAbbrev blastDatabaseAvailableAbbrev) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Database.class) ;
        criteria.add(Restrictions.eq("abbrev",blastDatabaseAvailableAbbrev));
        return (Database) criteria.uniqueResult() ;
    }

    public Origination getOrigination(Origination.Type type) {
        Session session = HibernateUtil.currentSession();
        String hql = " select o from Origination o where o.type = :type" ;
        Query query = session.createQuery(hql) ;
        query.setParameter("type",type.toString()) ;
        return (Origination) query.uniqueResult();
    }

    public List<Database> getDatabases(Database.Type type) {
        return getDatabases(type,false,false) ;
    }

    public List<Database> getDatabases(Database.Type type, boolean excludePrivate,boolean excludeExternal) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Database.class) ;
        if(type!=null){
            criteria.add(Restrictions.eq("type",type));
        }
        if(excludePrivate==true){
            criteria.add(Restrictions.eq("publicDatabase",true));
        }
        if(excludeExternal==true){
            criteria.add(Restrictions.not(Restrictions.eq("origination", getOrigination(Origination.Type.EXTERNAL))));
        }
        criteria.addOrder(Order.asc("name"));
        return criteria.list() ;
    }

    public List<Database> getDatabaseByOrigination(Origination.Type... originationType) {
        Session session = HibernateUtil.currentSession();
        String hql = " select d from Database d where d.origination.type in  (:type)" ;
        Query query = session.createQuery(hql) ;
        query.setParameterList("type",originationType) ;
        return query.list();
    }

    public List<DatabaseRelationship> getChildDatabaseRelationshipsByOrigination(Origination.Type originationType) {
        Session session = HibernateUtil.currentSession();
        String hql = " select dr from DatabaseRelationship dr  where dr.child.origination.type = :type" ;
        Query query = session.createQuery(hql) ;
        query.setParameter("type",originationType.toString()) ;
        return query.list();
    }

    public Set<String> getAllValidAccessionNumbers(Database database) {
        Set<String> returnAccessions = new HashSet<String>() ;

        String hql1 = "select dbl.accessionNumber " +
                " from DBLink dbl join dbl.referenceDatabase rd join rd.primaryBlastDatabase bd " +
                " where bd.zdbID = :databaseZdbID" ;
        Query query1= HibernateUtil.currentSession().createQuery(hql1);
        query1.setString("databaseZdbID",database.getZdbID()) ;
        returnAccessions.addAll(query1.list()) ;

        String hql2 = "select acc.number" +
                " from Accession  acc join acc.referenceDatabase rd join rd.primaryBlastDatabase bd " +
                " where bd.zdbID = :databaseZdbID" ;
        Query query2= HibernateUtil.currentSession().createQuery(hql2);
        query2.setString("databaseZdbID",database.getZdbID()) ;
        returnAccessions.addAll(query2.list()) ;

        return returnAccessions;
    }

    /**
     * Number of valid accessions.  The other method is too memory intensive.
     * Must implement in SQL to get a proper union.
     * @param database Database to retrive number of accessions for.
     * @return Number of combined unique accessions between accession bank and dblink.
     */
    public Integer getNumberValidAccessionNumbers(Database database) {
        String sql = "" +
                "select dbl.dblink_acc_num " +
                "from db_link dbl" +
                " join foreign_db_contains fdbc on dbl.dblink_fdbcont_zdb_id=fdbc.fdbcont_zdb_id "  +
                " join blast_database bdb on fdbc.fdbcont_primary_blastdb_zdb_id=bdb.blastdb_zdb_id " +
                " where bdb.blastdb_zdb_id = :databaseZdbID " +
                " union  " +
				" select ab.accbk_acc_num " +
				" from accession_bank ab " +
				" join foreign_db_contains fdbc on ab.accbk_fdbcont_zdb_id=fdbc.fdbcont_zdb_id " +
				" join blast_database bdb on fdbc.fdbcont_primary_blastdb_zdb_id=bdb.blastdb_zdb_id " +
				" where bdb.blastdb_zdb_id = :databaseZdbID " ;
        Query query2= HibernateUtil.currentSession().createSQLQuery(sql);
        query2.setString("databaseZdbID",database.getZdbID()) ;
        ScrollableResults results = query2.scroll() ;
        results.last() ;
        return results.getRowNumber()+1 ; 
    }

    public int setAllDatabaseLock(boolean isLocked) {
        Session session = HibernateUtil.currentSession() ;
        String hql = " update Database d set d.locked = :locked " ;
        Query query = session.createQuery(hql) ;
        query.setBoolean("locked",isLocked) ;
        return query.executeUpdate() ;
    }
}
