package org.zfin.sequence.blast;

import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;

/**
 */
public abstract class BlastStressTest {

    protected Database blastDatabase;
    protected XMLBlastBean xmlBlastBean ;

    private Database.AvailableAbbrev abbrev ;
    private File fastaFile;

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }


    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.initApplicationProperties();

        xmlBlastBean = new XMLBlastBean() ;
        xmlBlastBean.setProgram("blastn");
//        File file = new File("test/pax6a-004.fa") ;
        File file = getFastaFile();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file)) ;
            StringBuilder sb = new StringBuilder() ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                sb.append(line).append("\n") ;
            }
            xmlBlastBean.setQuerySequence(sb.toString());

            List<Database> actualDatabases = new ArrayList<Database>() ;
            blastDatabase = RepositoryFactory.getBlastRepository().getDatabase(abbrev) ;
            WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(abbrev) ;
            actualDatabases.add(blastDatabase) ;
            xmlBlastBean.setActualDatabaseTargets(actualDatabases);
            xmlBlastBean.setExpectValue(1.0E-25);


            // set filter
            xmlBlastBean.setSeg(true);
            xmlBlastBean.setXnu(true);

            // set word length
            xmlBlastBean.setWordLength(12);
        }
        catch(Exception e){
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();

        File currentDirectory = new File(".") ;
        File[] fileList = currentDirectory.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return (name.startsWith("blast")&&name.endsWith(".xml"));
            }
        });
        for(File file: fileList){
            System.out.println("deleting file: "+ file.getAbsolutePath()) ;
            assertTrue(file.delete());
        }

    }


    public Database.AvailableAbbrev getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(Database.AvailableAbbrev abbrev) {
        this.abbrev = abbrev;
    }

    public File getFastaFile() {
        return fastaFile;
    }

    public void setFastaFile(File fastaFile) {
        this.fastaFile = fastaFile;
    }
}
