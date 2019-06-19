package org.zfin.framework.presentation;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
@RequestMapping(value = "/devtool")
public class DatabaseInfoController {


    @RequestMapping("/database-info")
    protected String showDatabaseInfo(Model model) throws Exception {
        model.addAttribute("metadata", getMetaData());
        model.addAttribute("unloadDate", getInfrastructureRepository().getUnloadInfo());
        return "dev-tools/view-database-info.page";
    }

    @RequestMapping("/jdbc-driver-info")
    public String showJdbcDriverInfo(Model model) throws ServletException {
        model.addAttribute("metadata", getMetaData());
        return "jdbc-metadata.page";
    }

    @RequestMapping("/java-properties")
    public String showJavaProperties(Model model) throws ServletException {
        Properties properties = System.getProperties();
        Enumeration propEnum = properties.propertyNames();
        Map<String, String> props = new TreeMap<>();
        while (propEnum.hasMoreElements()) {
            String key = (String) propEnum.nextElement();
            props.put(key, properties.getProperty(key));

        }
        model.addAttribute("properties", props);
        return "java-properties.page";
    }

    @RequestMapping("/view-hibernate-info")
    public String viewHibernateInfo(Model model) throws ServletException {
        Properties properties = new Properties();
        Enumeration propEnum = properties.propertyNames();
        Map<String, String> props = new TreeMap<>();
        while (propEnum.hasMoreElements()) {
            String key = (String) propEnum.nextElement();
            props.put(key, properties.getProperty(key));

        }
        return "view-hibernate-info";
    }

    @RequestMapping("/test-browser")
    protected String showBrowserInfo() throws Exception {
        return "dev-tools/test-browser.page";
    }

    @RequestMapping("/view-session-info")
    public String viewSessionInfoHandler(HttpServletRequest request, Model model)
            throws ServletException {
        ZfinSessionBean form = new ZfinSessionBean();
        form.setRequest(request);
        model.addAttribute("formBean", form);
        return "view-session-info.page";
    }

    @RequestMapping("/deployed-version")
    public String viewDeployedVersion(Model model) throws IOException {
        Class clazz = DatabaseInfoController.class;
        InputStream inputStream = clazz.getResourceAsStream("/git-info.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        model.addAttribute("commit", reader.readLine());
        model.addAttribute("branch", reader.readLine());
        model.addAttribute("domain", ZfinPropertiesEnum.DOMAIN_NAME.value());
        return "deployed-version";
    }

    @RequestMapping("/phenotype-curation-history")
    public String viewPhenotypeHistory(Model model) {
        List<PhenotypeExperiment> phenotypeExperiments = RepositoryFactory.getPhenotypeRepository().getLatestPhenotypeExperiments(5);
        model.addAttribute("phenotypeExperiments", phenotypeExperiments);
        return "phenotype-curation-history.page";
    }

    @RequestMapping("/phenotype-curation-history-statements/{ID}")
    public String viewPhenotypeHistoryStatementsByID(@PathVariable(value = "ID") String experimentIDString,
                                                     Model model) {
        int experimentID = Integer.parseInt(experimentIDString);
        List<PhenotypeStatement> phenotypeStatements = RepositoryFactory.getPhenotypeRepository().getLatestPhenotypeStatements(experimentID, 2);
        model.addAttribute("phenotypeStatements", phenotypeStatements);
        return "phenotype-curation-history-statements.page";
    }

    @RequestMapping("/phenotype-curation-history-statements")
    public String viewPhenotypeHistoryStatements(Model model) {
        List<PhenotypeStatement> phenotypeStatements = RepositoryFactory.getPhenotypeRepository().getLatestPhenotypeStatements(0, 2);
        model.addAttribute("phenotypeStatements", phenotypeStatements);
        return "phenotype-curation-history-statements.page";
    }

    @RequestMapping("/single-thread-info/{id}")
    public String showSingleThreadInfo(@PathVariable("id") String threadId,
                                       Model model) throws ServletException {

        ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
        int threadID = Integer.parseInt(threadId);
        // stack trace depth: show all
        model.addAttribute("thread", mxbean.getThreadInfo(threadID, Integer.MAX_VALUE));
        return "single-thread-info.page";
    }

    @RequestMapping("/thread-info")
    public String showThreadInfo(Model model) throws ServletException {
        Thread currentThread = Thread.currentThread();

        Map all = Thread.getAllStackTraces();
        Iterator iter = all.keySet().iterator();
        while (iter.hasNext()) {
            Thread t = (Thread) iter.next();
            StackTraceElement[] stack = (StackTraceElement[]) all.get(t);
        }
        ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
        ThreadGroup threadGroup = currentThread.getThreadGroup();
        ThreadGroup parent = threadGroup;
        List<ThreadGroup> threadGroups = new ArrayList<>();
        threadGroups.add(threadGroup);

        while (parent.getParent() != null) {
            ThreadGroup parentTemp = parent.getParent();
            threadGroups.add(parentTemp);
            parent = parentTemp;
        }
        List<Thread> allThreads = getThreads(parent);
        model.addAttribute("threads", allThreads);
        model.addAttribute("threadMXBean", mxbean);
        ThreadInfo[] attributeValue = mxbean.dumpAllThreads(true, true);
        List<ThreadInfo> threadInfos = Arrays.asList(attributeValue);
        Collections.sort(threadInfos, new ThreadInfoSorting());
        model.addAttribute("allThreads", threadInfos);
        model.addAttribute("deadlockedThreads", mxbean.findDeadlockedThreads());
        model.addAttribute("monitorDeadlockedThreads", mxbean.findMonitorDeadlockedThreads());
        return "thread-info.page";
    }

    public List<Thread> getThreads(ThreadGroup group) {
        int activeThreads = group.activeCount();
        Thread[] threads = new Thread[activeThreads + 20];
        group.enumerate(threads);
        Arrays.sort(threads, new Comparator() {
            public int compare(Object a, Object b) {
                Thread threadOne = (Thread) a;
                Thread threadTwo = (Thread) b;
                if (threadOne == null && threadTwo != null)
                    return 1;
                if (threadOne != null && threadTwo == null)
                    return -1;
                if (threadOne == null && threadTwo == null)
                    return 0;
                return threadOne.getThreadGroup().getName().compareToIgnoreCase(threadTwo.getThreadGroup().getName());
            }
        });
        List<Thread> threadList = new ArrayList<>();
        if (threads != null) {
            for (int i = 0; i < threads.length; i++) {
                Thread thread = threads[i];
                if (thread != null) {
                    threadList.add(thread);
                }
            }
        }
        return threadList;
    }

    private DatabaseMetaData getMetaData() {
        final Session session = HibernateUtil.currentSession();
        return session.doReturningWork(new ReturningWork<DatabaseMetaData>() {
            @Override
            public DatabaseMetaData execute(Connection connection) throws SQLException {
                DatabaseMetaData meta = null;
                try {
                    meta = connection.getMetaData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return meta;
            }
        });
    }


    @ResponseBody
    @RequestMapping("/db-connections")
    public DBConnectionPoolBean viewPhenotypeHistoryStatementsByID() throws NamingException, SQLException {

        InitialContext ictx = new InitialContext();
        ComboPooledDataSource pds = (ComboPooledDataSource) ictx.lookup("java:comp/env/jdbc/zfin");

        DBConnectionPoolBean bean = new DBConnectionPoolBean(pds.getNumConnectionsDefaultUser(),
                pds.getNumBusyConnectionsDefaultUser(),
                pds.getNumIdleConnectionsDefaultUser());

        return bean;
    }


    class ThreadInfoSorting implements Comparator<ThreadInfo> {


        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2) {
            return o1.getThreadState().compareTo(o2.getThreadState());
        }
    }

    private class DBConnectionPoolBean {
        private int totalNumber;
        private int busyNumber;
        private int idleNumber;


        public DBConnectionPoolBean(int totalNumber, int busyNumber, int idleNumber) {
            this.totalNumber = totalNumber;
            this.busyNumber = busyNumber;
            this.idleNumber = idleNumber;
        }

        public int getTotalNumber() {
            return totalNumber;
        }

        public int getBusyNumber() {
            return busyNumber;
        }

        public int getIdleNumber() {
            return idleNumber;
        }
    }
}
