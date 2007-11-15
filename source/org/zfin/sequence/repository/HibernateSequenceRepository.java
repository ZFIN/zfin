/**
 *  Class SequenceRepository.
 */
package org.zfin.sequence.repository ;

import org.zfin.sequence.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.orthology.Species;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

public class HibernateSequenceRepository implements SequenceRepository {

//    Logger logger = Logger.getLogger(HibernateSequenceRepository.class) ;

    public ReferenceDatabase getReferenceDatabaseByAlternateKey(ForeignDB foreignDB,
                                                             ReferenceDatabase.Type type,
                                                             ReferenceDatabase.SuperType superType,
                                                             Species organism) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ReferenceDatabase.class);
        criteria.add(Restrictions.eq("foreignDB", foreignDB));
        criteria.add(Restrictions.eq("type",type.toString()));
        criteria.add(Restrictions.eq("superType",superType.toString()));
        criteria.add(Restrictions.eq("organism",organism.toString()));
        return (ReferenceDatabase) criteria.uniqueResult();

    }

    public ForeignDB getForeignDBByName(String dbName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ForeignDB.class);
        criteria.add(Restrictions.eq("dbName", dbName));
        return (ForeignDB) criteria.uniqueResult();

    }

    public ReferenceDatabase getReferenceDatabase(String foreignDBName, ReferenceDatabase.Type type, ReferenceDatabase.SuperType superType, Species organism) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ReferenceDatabase.class);
        criteria.add(Restrictions.eq("foreignDB.dbName", foreignDBName));
        criteria.add(Restrictions.eq("type",type.toString()));
        criteria.add(Restrictions.eq("superType",superType.toString()));
        criteria.add(Restrictions.eq("organism",organism.toString()));
        return (ReferenceDatabase) criteria.uniqueResult();
    }

    public Accession getAccessionByAlternateKey(String number, ReferenceDatabase referenceDatabase) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Accession.class);
        criteria.add(Restrictions.eq("number",number));
        criteria.add(Restrictions.eq("referenceDatabase",referenceDatabase));
        return (Accession) criteria.uniqueResult();
    }

    public Accession getAccessionByPrimaryKey(Long id) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Accession.class);
        criteria.add(Restrictions.eq("id",id));
        return (Accession) criteria.uniqueResult();
    }


    public AccessionRelationship getAccessionRelationshipByPrimaryKey(String zdbID){
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AccessionRelationship.class);
        criteria.add(Restrictions.eq("zdbID",zdbID));
        return (AccessionRelationship) criteria.uniqueResult();

    }
}


