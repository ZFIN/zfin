package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.zfin.database.presentation.Table;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.InformixLuceneIndexInspection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Controller that obtains the meta data for the database.
 */
public class DatabaseInfoController extends MultiActionController {

    public ModelAndView databaseInfoHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        DatabaseMetaData meta = getMetaData();
        ModelAndView mv = new ModelAndView("metadata");
        mv.addObject("metadata", meta);
        return mv;
    }

    public ModelAndView btsInfoHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        InformixLuceneIndexInspection inspection = new InformixLuceneIndexInspection(Table.WH_FISH, "fas_all");
        return new ModelAndView("bts-index-statistics", "statistics", inspection);
    }

    public ModelAndView jdbcDriverHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        DatabaseMetaData meta = getMetaData();
        return new ModelAndView("jdbc-metadata", "metadata", meta);
    }

    public ModelAndView javaPropertiesHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        Properties properties = System.getProperties();
        Enumeration propEnum = properties.propertyNames();
        Map<String, String> props = new TreeMap<String, String>();
        while (propEnum.hasMoreElements()) {
            String key = (String) propEnum.nextElement();
            props.put(key, properties.getProperty(key));

        }
        return new ModelAndView("java-properties", "properties", props);
    }

    public ModelAndView viewHibernateInfoHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        SessionFactory sessionfac = HibernateUtil.getSessionFactory();
/*
        Properties properties = sessionfac.getStatistics().;
*/
        Properties properties = new Properties();
        Enumeration propEnum = properties.propertyNames();
        Map<String, String> props = new TreeMap<String, String>();
        while (propEnum.hasMoreElements()) {
            String key = (String) propEnum.nextElement();
            props.put(key, properties.getProperty(key));

        }
        return new ModelAndView("java-properties", "properties", props);
    }

    public ModelAndView testBrowserHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        HashMap map = new HashMap();
        Enumeration en = request.getHeaderNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            map.put(key, (Object) request.getHeader(key));
        }
        return new ModelAndView("test-browser", "form", map);
    }

    public ModelAndView viewSessionInfoHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        ZfinSessionBean form = new ZfinSessionBean();
        form.setRequest(request);
        return new ModelAndView("view-session-info", LookupStrings.FORM_BEAN, form);
    }

    public ModelAndView svnVersionHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("svn-version");

        return modelAndView;
    }

    public ModelAndView phenotypeCurationHistoryHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        List<PhenotypeExperiment> phenotypeExperiments = RepositoryFactory.getPhenotypeRepository().getLatestPhenotypeExperiments(5);
        ModelAndView modelAndView = new ModelAndView("phenotype-curation-history.page", "phenotypeExperiments", phenotypeExperiments);
        return modelAndView;
    }

    public ModelAndView phenotypeCurationHistoryStatementsHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        String experimentIDString = request.getParameter("experimentID");
        int experimentID = 0;
        if (!StringUtils.isEmpty(experimentIDString))
            experimentID = Integer.parseInt(experimentIDString);
        List<PhenotypeStatement> phenotypeStatements = RepositoryFactory.getPhenotypeRepository().getLatestPhenotypeStatements(experimentID, 2);
        ModelAndView modelAndView = new ModelAndView("phenotype-curation-history-statements.page", "phenotypeStatements", phenotypeStatements);
        return modelAndView;
    }

    public ModelAndView singleThreadInfoHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        String threadId = request.getParameter("threadID");
        ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
        int threadID = Integer.parseInt(threadId);
        // stack trace depth: show all
        return new ModelAndView("single-thread-info", "thread", mxbean.getThreadInfo(threadID, Integer.MAX_VALUE));
    }

    public ModelAndView threadInfoHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
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
        List<ThreadGroup> threadGroups = new ArrayList<ThreadGroup>();
        threadGroups.add(threadGroup);

        while (parent.getParent() != null) {
            ThreadGroup parentTemp = parent.getParent();
            threadGroups.add(parentTemp);
            parent = parentTemp;
        }
        List<Thread> allThreads = getThreads(parent);
        ModelAndView threads = new ModelAndView("thread-info", "threads", allThreads);
        threads.addObject("threadMXBean", mxbean);
        ThreadInfo[] attributeValue = mxbean.dumpAllThreads(true, true);
        List<ThreadInfo> threadInfos = Arrays.asList(attributeValue);
        Collections.sort(threadInfos, new ThreadInfoSorting());
        threads.addObject("allThreads", threadInfos);
        threads.addObject("deadlockedThreads", mxbean.findDeadlockedThreads());
        threads.addObject("monitorDeadlockedThreads", mxbean.findMonitorDeadlockedThreads());
        return threads;
    }

    public List<Thread> getThreads(ThreadGroup group) {
        int activeThreads = group.activeCount();
        Thread[] threads = new Thread[activeThreads + 20];
        group.enumerate(threads);
        Arrays.sort(threads, new Comparator() {
            public int compare(Object a, Object b) {
                Thread threadOne = (Thread) a;
                Thread threadTwo = (Thread) b;
                if (threadOne == null || threadTwo == null) {
                    return -1;
                }
                return threadOne.getThreadGroup().getName().compareToIgnoreCase(threadTwo.getThreadGroup().getName());
            }
        });
        List<Thread> threadList = new ArrayList<Thread>();
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
        DatabaseMetaData meta = null;
        Session session = HibernateUtil.currentSession();
        try {
            meta = session.connection().getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meta;
    }

    class ThreadInfoSorting implements Comparator<ThreadInfo> {


        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2) {
            return o1.getThreadState().compareTo(o2.getThreadState());
        }
    }

}
