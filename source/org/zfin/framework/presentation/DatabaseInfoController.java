package org.zfin.framework.presentation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.util.FileWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
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

    public ModelAndView viewGlobalSessionInfoHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        ZfinSessionBean form = new ZfinSessionBean();
        form.setRequest(request);
        return new ModelAndView("view-global-session-info", LookupStrings.FORM_BEAN, form);
    }

    public ModelAndView svnVersionHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("svn-version");

        return modelAndView;
    }

    public ModelAndView zfinPropertyHandler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        File zfinPropertyFile = ZfinProperties.getZfinPropertyFile();
        FileWrapper wrapper = new FileWrapper(zfinPropertyFile, "ZFIN Properties");
        return new ModelAndView("file-content", "fileWrapper", wrapper);
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
        return new ModelAndView("thread-info", "threads", allThreads);
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


}
