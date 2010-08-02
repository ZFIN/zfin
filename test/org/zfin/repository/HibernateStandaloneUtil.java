package org.zfin.repository;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.sequence.repository.SequenceRepository;

/**
 * Utility class that can be used to run persistence layer classes
 * without using the Tomcat environment.
 * <p/>
 * Java environment parameters:
 * -DDBNAME=<mutant_db> -DCONFIGURATION_DIRECTORY=<directory of hbm-files>
 */
public class HibernateStandaloneUtil {

    public static void main(String[] arguments){
        new HibernateSessionCreator();
        callPersistence();
        HibernateUtil.closeSession();
    }

    /*
     * Please change this method to call the repository of your choice.
     */
    private static void callPersistence() {
        Logger log = LogManager.getLogger("org.zfin");
        log.setLevel(Level.toLevel("INFO"));

        Session session = HibernateUtil.currentSession();

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
        ProfileRepository personRepository = RepositoryFactory.getProfileRepository();

        session.beginTransaction();

        //do stuff here

        session.getTransaction().rollback();
    }



}
