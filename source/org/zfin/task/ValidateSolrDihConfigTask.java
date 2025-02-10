package org.zfin.task;

import org.hibernate.query.Query;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Runs all of the queries in the db-data-config.xml file and
 * provides information about how long each query takes to run.
 * Times out any queries that last longer than 30 seconds.
 * Reports any queries that fail.
 */
public class ValidateSolrDihConfigTask extends AbstractScriptWrapper {

    private String filePath;
    private List<String> nameFilters = new ArrayList<>();
    private static final int TIMEOUT_SECONDS = 30;

    public static void main(String[] args) throws IOException {
        ValidateSolrDihConfigTask task = new ValidateSolrDihConfigTask();
        task.runTask();
    }

    private void runTask() {
        initProperties();
        initArguments();
        parseAndExecuteQueries();
    }

    private void parseAndExecuteQueries() {
        boolean success = true;
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList entityNodes = doc.getElementsByTagName("entity");
            for (int i = 0; i < entityNodes.getLength(); i++) {
                Node node = entityNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String query = element.getAttribute("query");
                    boolean result = executeQueryCmdLine(name, query);
                    if (!result) {
                        success = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!success) {
            System.exit(1);
        }
    }

    private boolean executeQueryJava(String entityName, String queryStr) {
        if (!nameFilters.isEmpty()) {
            if (!nameFilters.contains(entityName)) {
                return true;
            }
        }

        queryStr = queryStr.trim();
        if (queryStr.endsWith(";")) {
            queryStr = queryStr.substring(0, queryStr.length() - 1);
        }
        queryStr += " LIMIT 1";
        System.out.print("Running query: " + entityName);
        try {
            this.initDatabase();
            Calendar timestampBefore = Calendar.getInstance();
            Query<?> query = currentSession().createNativeQuery(queryStr);
//            query.setMaxResults(1);  // Limit results for efficiency
            query.getResultList();
            Calendar timestampAfter = Calendar.getInstance();
            Duration timePassed = Duration.between(timestampBefore.toInstant(), timestampAfter.toInstant());
            System.out.println(" passed duration: " + timePassed.toMillis() + "ms");
            return true;
        } catch (Exception e) {
            System.out.println(" failed: " + e.getMessage());
        } finally {
            HibernateUtil.closeSession();
        }
        return false;
    }

    public boolean executeQueryCmdLine(String entityName, String queryStr) {
        if (!nameFilters.isEmpty() && !nameFilters.contains(entityName)) {
            return true;
        }

        queryStr = queryStr.trim();
        if (queryStr.endsWith(";")) {
            queryStr = queryStr.substring(0, queryStr.length() - 1);
        }
        queryStr += " LIMIT 1;";

        System.out.print("Running query: " + entityName);

        ProcessBuilder pb = new ProcessBuilder(
                "psql", "-h", ZfinPropertiesEnum.PGHOST.toString(), "-d", ZfinPropertiesEnum.DB_NAME.toString(), "-c", queryStr
        );

        try {
            long startTime = System.currentTimeMillis();
            Process process = pb.start();

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.out.println(" Query timed out after 30 seconds.");

                FileUtil.writeToFileOrZip(new File("/tmp",entityName + ".sql"), queryStr, "utf-8");
                System.out.println(" Query written to /tmp/" + entityName + ".sql");

                return false;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
            }
            int exitCode = process.waitFor();
            long endTime = System.currentTimeMillis();
            if (exitCode != 0) {
                throw new IOException("Error running query: " + entityName);
            }
            System.out.println(" passed duration: " + (endTime - startTime) + "ms");
            return true;
        } catch (IOException | InterruptedException e) {
            System.out.println(" failed: " + e.getMessage());
            try {
                FileUtil.writeToFileOrZip(new File("/tmp",entityName + ".sql"), queryStr, "utf-8");
                System.out.println(" Query written to /tmp/" + entityName + ".sql");
            } catch (IOException ex) {
            }
            return false;
        }
    }

    private void initArguments() {
        filePath = System.getenv("DB_DATA_CONFIG_FILE");
        if (filePath == null) {
            System.err.println("Usage: \n" +
                    " set environment variable DB_DATA_CONFIG_FILE to db-data-config.xml location first \n" +
                    " eg. DB_DATA_CONFIG_FILE=./db-data-config.xml gradle validateSolrDihConfigTask");
            System.exit(1);
        }
        String nameFiltersString = System.getenv("NAME_FILTERS");
        if (nameFiltersString != null) {
            nameFilters = Arrays.asList(nameFiltersString.split(","));
        }
    }

}
