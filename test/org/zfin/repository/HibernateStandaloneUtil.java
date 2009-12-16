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
        String[] confFiles = {
            "filters.hbm.xml",
            "anatomy.hbm.xml",
            "mutant.hbm.xml",
            "orthology.hbm.xml",
            "people.hbm.xml",
            "sequence.hbm.xml",
            "blast.hbm.xml",
            "reno.hbm.xml",
            "publication.hbm.xml",
            "marker.hbm.xml",
            "mapping.hbm.xml",
            "infrastructure.hbm.xml",
            "expression.hbm.xml"
        };
        new HibernateSessionCreator(false, confFiles);
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
