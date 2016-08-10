package org.zfin.framework.presentation;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.database.presentation.Table;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.InformixLuceneIndexInspection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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

    @RequestMapping("/bts-index-info")
    protected String showBtsIndex(Model model) throws Exception {
        return "bts-index-statistics.page";
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

    @RequestMapping("/svn-version")
    public String viewSvnInfo() throws ServletException {
        return "svn-version";
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
        return session.doReturningWork(new ReturningWork<DatabaseMetaData>(){
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

    class ThreadInfoSorting implements Comparator<ThreadInfo> {


        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2) {
            return o1.getThreadState().compareTo(o2.getThreadState());
        }
    }

}
