package org.zfin.repository;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.zfin.anatomy.*;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.people.Lab;
import org.zfin.people.Person;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.util.FileUtil;
import org.zfin.webdriver.repository.HibernateWebExplodeRepository;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Please provide JavaDoc info!!!
 */
public class RunHibernateConfigurationChecks extends HibernateTestCase {

    private static String db = "fstdb";
    public static final String HIBERNATE_PROP = "hibernate.properties";
    public static final String HIBERNATE_RENAMED = "hibernate.props";
    private static String FILE_SEP = System.getProperty("file.separator");
    public static final String CLASSES_DIR = "home"+FILE_SEP+"WEB-INF"+FILE_SEP+"classes"+FILE_SEP;

    public RunHibernateConfigurationChecks(String x) {
        super(x);
        setup();
    }

    private void setup() {
        renameHibernatePropteriesFile();
        try {
//            setUp();
            File propertyFile = new File("test", "spring-test-properties.xml");
            BeanFactory factory = new XmlBeanFactory(new FileSystemResource(propertyFile));
/*
            Hashtable ht = new Hashtable();
            ht.put(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getNumber());
            InitialContext context = new InitialContext(ht);
*/
            initHibernate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renameHibernatePropteriesFile() {
        File file = new File(CLASSES_DIR +HIBERNATE_PROP);
        File renamedFile = new File(CLASSES_DIR +HIBERNATE_RENAMED);
        boolean success = file.renameTo(renamedFile);
        if (!success)
            System.out.println("Unable to rename Hibernate Property file");
    }

    private static void unnameHibernatepropFile() {
        File file = new File(CLASSES_DIR +HIBERNATE_RENAMED);
        File renamedFile = new File(CLASSES_DIR +HIBERNATE_PROP);
        boolean success = file.renameTo(renamedFile);
        if (!success)
            throw new RuntimeException("Unable to rename Hibernate Property file");
    }

    public static void main(String[] arguments) {
        RunHibernateConfigurationChecks check = new RunHibernateConfigurationChecks("Hibernate Check");
        Session s = HibernateUtil.currentSession();
        String zdbID = "ZDB-ANAT-051116-41";
        zdbID = "ZDB-ANAT-010921-544";
        zdbID = "ZDB-ANAT-050915-161";
        // two synonyms
        zdbID = "ZDB-ANAT-010921-433";
        zdbID = "ZDB-ANAT-010921-455";
        AnatomyItem anatomyItem = (AnatomyItem) s.load(AnatomyItem.class, zdbID);
        System.out.println(anatomyItem.getName());

        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
//        List<AnatomyRelationshipType> types = ar.getAllAnatomyRelationshipTypes();
//        System.out.println(types);

        List<AnatomyRelationship> rels = ar.getAnatomyRelationships(anatomyItem);
        System.out.println(rels);

//        List<AnatomyStatistics> stats = ar.getAnatomyItemStatistics("neural rod");
        DevelopmentStage stage = new DevelopmentStage();
        // Zygote stage
//        getStatsByStage(stage, ar);
        //List<String> stats = ar.getAllAnatomyNamesAndSynonyms();
        //System.out.println("NUmber of Pubs: ");
        //highQualityProbe();
        //auditLog();
        //createZdbId();
        //createPerson();
        //recommendedMarkers();
        getFigures();
        unnameHibernatepropFile();
        getZdbId();
        
    }

    private static void getZdbId() {
        HibernateWebExplodeRepository impl = new HibernateWebExplodeRepository();
        String contents = null;
        try {
            contents = impl.getWebExplodeContents("aa-htmlpageheader.apg", null);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println(contents);
    }

    private static void getFigures() {
        String est = "eu815";
        String gene = "fgf8";
        String probeID = "ZDB-EST-060130-371";
        String geneID = "ZDB-EST-060130-371";
        String pubID = "ZDB-PUB-051025-1";
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        //List<Figure> figures = pr.getFiguresByGeneID(geneID, pubID);
        List<Figure> figures = pr.getFiguresByProbeAndPublication(geneID, pubID);
        System.out.println(figures);
    }

    private static void getStatsByStage(DevelopmentStage stage, AnatomyRepository ar) {
        stage.setZdbID("ZDB-STAGE-010723-4");
        List<AnatomyStatistics> stats = ar.getAnatomyItemStatisticsByStage(stage);
    }

    private static void recommendedMarkers() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        String zdbID = "ZDB-ANAT-010921-591";
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
/*
        List<CanonicalMarker> list = ar.getCanonicalMarker(zdbID);
        System.out.println("list");
*/

        CanonicalMarker canon = new CanonicalMarker();
        AnatomyItem item = new AnatomyItem();
        item.setZdbID("ZDB-ANAT-010921-591");
        canon.setItem(item);
        Publication pub = new Publication();
        pub.setZdbID("ZDB-PUB-000103-6");
        Set pubs = new HashSet();
        pubs.add(pub);
        canon.setPublications(pubs);
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-980526-561");
        canon.setGene(marker);
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        pr.insertCanonicalMarker(canon);
        session.getTransaction().commit();
    }

    private static void createPerson() {
        Person person = new Person();
        person.setFullName("Marga Manitius");
        person.setName("M. Man");
        person.setAddress("address");

        Lab lab = new Lab();
        lab.setName("TUM");
        HashSet set = new HashSet();
        set.add(lab);

        person.setLabs(set);
        ProfileRepository pr = RepositoryFactory.getProfileRepository();
        //pr.insertPerson(person);
        pr.insertLab(lab);
    }

    private static void createZdbId() {
        Session session = HibernateUtil.currentSession();
        Connection conn = session.connection();
        String name = "GENE";
        CallableStatement cs = null;
        String sql = "execute function get_id('GENE') ";
        SQLQuery query = session.createSQLQuery(sql);
        String id = (String) query.uniqueResult();

        System.out.println("New Unique Id: " + id);
    }

    private static void auditLog() {
        AuditLogRepository ar = RepositoryFactory.getAuditLogRepository();
        String aoZdbID = "ZDB-ANAT-010921-561";
        List logItems = ar.getAuditLogItems(aoZdbID);
        System.out.println("Audit Log Items: " + logItems.size());

    }

    private static void highQualityProbe() {
        PublicationRepository ar = RepositoryFactory.getPublicationRepository();
        String publictationID = "ZDB-PUB-040907-1";
        String expID = "ZDB-XPAT-011001-1";
        String anatomId = "ZDB-ANAT-010921-561";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(anatomId);

/*
        List<Publication> list = ar.getHighQualityProbePublications(anatomId);
        if (list != null && list.size() < 20)
            System.out.println(list);
        else if (list != null)
            System.out.println("Number of publication: " + list.size());
*/

//        List markers = ar.getHighQualityProbeNames(item).getResults();
        System.out.println("Probes Number : " + ar.getHighQualityProbeNames(item).getPopulatedResults().size()); // this is only the # populated
    }

    protected String[] getMappings() {
        return new String[]{"anatomy.hbm.xml", "people.hbm.xml"};
    }

    private void initHibernate() {
        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", "org.hibernate.dialect.InformixDialect");
        config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
        config.setProperty("hibernate.connection.url", "jdbc:informix-sqli://embryonix.cs.uoregon.edu:2002/" + db + ":INFORMIXSERVER=wanda");
        config.setProperty("hibernate.connection.username", "zfinner");
        config.setProperty("hibernate.connection.password", "Rtwm4ts");
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.connection.autocommit", "true");
        config.setProperty("hibernate.connection.isolation", "1");
        config.setProperty("hibernate.show_sql", "true");

        String absolutePathAnat = FileUtil.createAbsolutePath(ZfinProperties.CONFIGURATION_DIRECTORY, "anatomy.hbm.xml");
        String absolutePathPeople = FileUtil.createAbsolutePath(ZfinProperties.CONFIGURATION_DIRECTORY, "people.hbm.xml");
//        String absolutePathDb = FileUtil.createAbsolutePath(ZfinProperties.CONFIGURATION_DIRECTORY, "schema-info.hbm.xml");
        config.addFile(absolutePathAnat);
        config.addFile(absolutePathPeople);
//        config.addFile(absolutePathDb);
        HibernateUtil.init(config.buildSessionFactory());

    }

}
