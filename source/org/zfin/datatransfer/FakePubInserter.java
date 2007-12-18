package org.zfin.datatransfer;

import org.hibernate.Session;
import org.hibernate.Query;
import org.zfin.framework.HibernateUtil;
import org.zfin.publication.Publication;
import org.zfin.publication.Journal;
import org.zfin.repository.RepositoryFactory;
import org.apache.log4j.Logger;

/**
 * User: nathandunn
 * Date: Nov 28, 2007
 * Time: 2:24:52 PM
 */
public class FakePubInserter {

    private static Logger logger = Logger.getLogger(FakePubInserter.class) ;


    /**
     *  todo:  this will go away when we have a real curated pub reference
     */
    public static String insertFakePub(){

        Session session = HibernateUtil.currentSession() ;

        // can't add a pub without a transaction
//        if(session.getTransaction()==null){
          session.beginTransaction() ;
//        }
        try{
            Query query = session.createQuery("delete from Publication p where p.title='Curated microarray data'");
            int deletedRecords = query.executeUpdate() ;
            logger.info("deleted old record: "+deletedRecords) ;
            logger.info("inserting fake pub");
            Publication fakePub = new Publication() ;
            fakePub.setAuthors("Dunn, N");
            fakePub.setTitle("Curated microarray data");
            fakePub.setShortAuthorList("Dunn, N");
            fakePub.setType("Curation");
            Journal journal = RepositoryFactory.getPublicationRepository().getJournalByTitle("ZFIN Direct Data Submission");
            logger.info("journal zdbID: "+journal.getZdbID());
            fakePub.setJournal(journal);
            logger.info("A: "+journal.getZdbID());
            session.save(fakePub);
            logger.info("B: "+journal.getZdbID());
//            session.flush();
//            session.getTransaction().commit();
            String referencePubZdbID = fakePub.getZdbID() ;
            logger.info("C: "+journal.getZdbID());
            logger.info("flushed okay: "+referencePubZdbID);
            return referencePubZdbID ;
        }
        catch(Exception e){
            logger.fatal(e) ;
            session.getTransaction().rollback();
        }
        return null ;
    }
}
