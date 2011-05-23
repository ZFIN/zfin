package org.zfin.wiki.service;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.zfin.TestConfiguration;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.WikiSynchronizationReport;
import org.zfin.wiki.jobs.AntibodyWikiSynchronizationJob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to run wiki code once (as opposed to out of a cron job.
 * Note that the parameters for execution come from "zfin-properties-test.xml".
 * There is no ant target for this and this is generally run from an IDE with the parameters:
 **/
public class WikiScriptRunner {

    private final static Logger logger = Logger.getLogger(WikiScriptRunner.class);


    static  {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    /**
     * Adds a web page for a single antibody.
     * @param antibodyName antibody name
     */
    public void synchronizeWebPage(String antibodyName) {
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByName(antibodyName);
        if (antibody != null) {
//            AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
            try {
                AntibodyWikiWebService.ReturnStatus returnStatus = AntibodyWikiWebService.getInstance().synchronizeAntibodyWithWiki(antibody);
                logger.info("returned: " + returnStatus);
            } catch (Exception e) {
                e.fillInStackTrace() ;
                e.printStackTrace();
            }
        } else {
            logger.warn("no antibody found for[" + antibodyName + "]");
        }
    }

    /**
     * Adds a web page for a single antibody.
     * @param antibodyName antibody name
     */
    public void replaceWebPage(String antibodyName) {
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByName(antibodyName);
        if (antibody != null) {
//            AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
            try {
                AntibodyWikiWebService.getInstance().dropPageIndividually(antibody.getName());
                AntibodyWikiWebService.getInstance().createPageForAntibody(antibody);
            } catch (Exception e) {
                e.fillInStackTrace() ;
                e.printStackTrace();
            }
        } else {
            logger.warn("no antibody found for[" + antibodyName + "]");
        }
    }


    public static void main(String args[]) {
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue("home");
        try {

            List<Antibody> antibodies = RepositoryFactory.getAntibodyRepository().getAllAntibodies();
            Map<String, Antibody> zfinAntibodyHashMap = new HashMap<String, Antibody>();
            String pageTitle ;
            for(Antibody antibody : antibodies){
                pageTitle = AntibodyWikiWebService.getInstance().getWikiTitleFromAntibody(antibody);
                zfinAntibodyHashMap.put(pageTitle, antibody);
            }
            WikiSynchronizationReport wikiSynchronizationReport = new WikiSynchronizationReport(true);
            AntibodyWikiWebService.getInstance().validateAntibodiesOnWikiWithZFIN(zfinAntibodyHashMap,wikiSynchronizationReport);
//            WikiScriptRunner wikiScriptRunner = new WikiScriptRunner();
//            wikiScriptRunner.addWebPage("zn-5");
//            wikiScriptRunner.addWebPage("Ab-10E4");
//            wikiScriptRunner.addWebPage("Ab-3A10");
//            wikiScriptRunner.replaceWebPage("Ab1-tuba");
//            wikiScriptRunner.addWebPage("Ab1-tuba");
//            AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
//            AntibodyWikiWebService.getInstance().replaceAntibodiesOnWikiWithZFIN();
//            wikiScriptRunner.synchronizeWebPage("Ab1-tuba");
//            wikiScriptRunner.synchronizeWebPage("Ab1-tuba");
//            wikiScriptRunner.synchronizeWebPage("anti-Tbx16");

//            Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByName("zn-1");
//            Antibody antibodyToDelete  = RepositoryFactory.getAntibodyRepository().getAntibodyByName("zn-13");
//            AntibodyWikiWebService.getInstance().mergeAntibody(antibodyToMergeInto,antibodyToDelete) ;

//            AntibodyWikiWebService.login();
//            wikiScriptRunner.addWebPage("Ab-2F11");
//        AntibodyWikiWebService.addWebPage("zn-5");

//            AntibodyWikiWebService.logout();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
