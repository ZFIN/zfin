package org.zfin.repository;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.Accession;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerService;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.orthology.Species;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.hibernate.Session;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.util.Set;
import java.util.HashSet;

/**
 * Utility class that can be used to run persistence layer classes
 * without using the Tomcat environment.
 * <p/>
 * Java environment parameters:
 * -DDBNAME=<mutant_db> -DCONFIGURATION_DIRECTORY=<directory of hbm-files>
 */
public class HibernateStandaloneUtil {


    public static boolean createSession(){
        return SessionCreator.createSession() ; 
    }

    public static void main(String[] arguments){
        HibernateStandaloneUtil standalone = new HibernateStandaloneUtil() ;
        standalone.createSession();
        standalone.callPersistence();
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
