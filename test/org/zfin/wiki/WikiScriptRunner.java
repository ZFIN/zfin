package org.zfin.wiki;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.io.File;

/**
 * This class is used to run wiki code once (as opposed to out of a cron job.
 * Note that the parameters for execution come from "zfin-properties-test.xml".
 * There is no ant target for this and this is generally run from an IDE with the parameters:
 **/
public class WikiScriptRunner {

    private final static Logger logger = Logger.getLogger(WikiScriptRunner.class);

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
        new HibernateSessionCreator(false);
        File file = new File("test", "log4j.xml");
        DOMConfigurator.configure(file.getAbsolutePath());
        ZfinProperties.init();
//        LoadPropertiesTask.loadProperties();
//        ZfinProperties.init("test", "zfin-properties-test.xml");
//        ZfinProperties.setWebRootDirectory(new File(".").getAbsolutePath());
        ZfinPropertiesEnum.WEBROOT_DIRECTORY.setValue("home");
        try {
            WikiScriptRunner wikiScriptRunner = new WikiScriptRunner();
//            wikiScriptRunner.addWebPage("zn-5");
//            wikiScriptRunner.addWebPage("Ab-10E4");
//            wikiScriptRunner.addWebPage("Ab-3A10");
//            wikiScriptRunner.replaceWebPage("Ab1-tuba");
//            wikiScriptRunner.addWebPage("Ab1-tuba");
//            AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
//            AntibodyWikiWebService.getInstance().replaceAntibodiesOnWikiWithZFIN();
//            wikiScriptRunner.synchronizeWebPage("Ab1-tuba");
//            wikiScriptRunner.synchronizeWebPage("Ab1-tuba");
            wikiScriptRunner.synchronizeWebPage("anti-Tbx16");

//            AntibodyWikiWebService.login();
//            wikiScriptRunner.addWebPage("Ab-2F11");
//        AntibodyWikiWebService.addWebPage("zn-5");

//            AntibodyWikiWebService.logout();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
