package org.zfin.util;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class reads the access log files
 */
public class LogStats {

    private static final String LOG_FILE_DIRECTORY = "log-file-directory";
    private static final String ACTION_ANATOMY_TERM_DETAIL_ANATOMY_ITEM_ZDB_ID = "/action/anatomy/term-detail?anatomyItem.zdbID=";
    private static final String ANATOMY = "ANAT";
    private static final String RESOLVE_ZDB_IDS = "resolveZdbIDs";
    private static boolean resolveIDs;

    public static void main(String[] arguments) {
        String resolveIDsString = System.getProperty(RESOLVE_ZDB_IDS);
        if (resolveIDsString != null) {
            if (resolveIDsString.equals("true"))
                resolveIDs = true;
            if (resolveIDs)
                initDatabase();
        }
        System.out.println("Resolve ZDB IDs: " + resolveIDs);

        String logFileDirectory = System.getProperty(LOG_FILE_DIRECTORY);
        if (StringUtils.isEmpty(logFileDirectory)) {
            System.out.println("No log file directory provided");
            System.out.println("Please define the folder in the environment variable <log-file-directory>");
            System.exit(-1);
        }
        System.out.println("log file directory used: " + logFileDirectory);

        File file = new File(logFileDirectory);
        if (!file.exists()) {
            System.out.println("log file directory : " + logFileDirectory);
            System.exit(-1);
        }

        File[] logFiles = file.listFiles();
        if (logFiles == null) {
            System.out.println("No log files found in " + file.getAbsoluteFile());
            System.exit(-1);
        }

        System.out.println(logFiles.length + " log files found");

        for (File log : logFiles) {
            System.out.print("\t");
            System.out.println(log.getName());
        }

        for (File log : logFiles) {
            readFile(log);
        }


        System.out.println("Total ao terms  found: " + totalRequests);
        System.out.println("Number of different terms found: " + histogram.size());
        printHistogram();
        System.out.println("Number of invalid terms: " + failedIDs.size());
        System.out.println("Invalid terms: " + failedIDs);

    }

    private static void readFile(File log) {
        FileReader fr = null;
        try {
            fr = new FileReader(log);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                if (parseLine(line))
                    totalRequests++;
            }
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, Integer> histogram = new HashMap<String, Integer>();
    private static HashSet<String> failedIDs = new HashSet<String>();
    private static int totalRequests = 0;

    private static boolean parseLine(String line) {
        StringTokenizer st = new StringTokenizer(line, " ");

        int totalMatches = 0;
        while (st.hasMoreElements()) {
            String uri = (String) st.nextElement();
            if (uri != null && uri.startsWith(ACTION_ANATOMY_TERM_DETAIL_ANATOMY_ITEM_ZDB_ID)) {
                int indexOfID = uri.indexOf("=");
                String aoTermID = uri.substring(indexOfID + 1);
                //System.out.println(aoTermID);
                Integer number = histogram.get(aoTermID);
                if (number == null)
                    number = 0;
                histogram.put(aoTermID, number + 1);
                totalMatches++;
            }
        }
        //System.out.println("Total request found: " + totalMatches);
        return totalMatches > 0;
    }

    private static void printHistogram() {
        TreeSet<ZdbIDHistogram> set = new TreeSet<ZdbIDHistogram>(NUMBER_ORDER);
        for (String key : histogram.keySet()) {
            ZdbIDHistogram hist = new ZdbIDHistogram(key, histogram.get(key), ANATOMY);
            if (hist.validateID())
                set.add(hist);
            else
                failedIDs.add(hist.getAoID());
        }
        for (ZdbIDHistogram hist : set) {
            if (resolveIDs) {
                AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
                AnatomyItem term = ar.getAnatomyTermByID(hist.getAoID());
                if (term != null) {
                    String termName = term.getTermName();
                    System.out.print(termName);
                } else
                    System.out.print(hist.getAoID());
            } else
                System.out.print(hist.getAoID());


            System.out.print(" [");
            System.out.print(hist.getNumberOfOccurrences());
            System.out.println("]");
        }
    }

    public static final Comparator NUMBER_ORDER = new Comparator<ZdbIDHistogram>() {

        public int compare(ZdbIDHistogram first, ZdbIDHistogram second) {

            int ret = second.getNumberOfOccurrences().compareTo(first.getNumberOfOccurrences());

            if (ret == 0) {
                ret = first.getAoID().compareTo(second.getAoID());
            }

            return ret;
        }
    };

    public static void initDatabase() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
        TestConfiguration.configure();
    }
}


