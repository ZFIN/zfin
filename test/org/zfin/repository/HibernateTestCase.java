package org.zfin.repository;

import junit.framework.AssertionFailedError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.*;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Iterator;
import java.util.Properties;

/**
 * Please provide JavaDoc info!!!
 */
public abstract class HibernateTestCase extends junit.framework.TestCase {
    private static SessionFactory sessions;
    private static Configuration cfg;
    private static Dialect dialect;
    private static Class lastTestClass;
    private org.hibernate.classic.Session session;

    protected boolean recreateSchema() {
        return true;
    }

    public HibernateTestCase(String x) {
        super(x);
    }

    protected void configure(Configuration cfg) {
    }

    private void buildSessionFactory(String[] files) throws Exception {

        if (getSessions() != null) getSessions().close();

        setDialect(Dialect.getDialect());

        try {
            setCfg(new Configuration());
            cfg.addProperties(getExtraProperties());

/*
            if( recreateSchema() ) {
                cfg.setBeanName(Environment.HBM2DDL_AUTO, "create-drop");
            }

*/
            for (int i = 0; i < files.length; i++) {
                files[i] = getBaseForMappings() + files[i];
                getCfg().addResource(files[i], HibernateTestCase.class.getClassLoader());
            }
            configure(cfg);

            if (getCacheConcurrencyStrategy() != null) {
                Iterator iter = cfg.getClassMappings();
                while (iter.hasNext()) {
                    PersistentClass clazz = (PersistentClass) iter.next();
                    Iterator props = clazz.getPropertyClosureIterator();
                    boolean hasLob = false;
                    while (props.hasNext()) {
                        Property prop = (Property) props.next();
                        if (prop.getValue().isSimpleValue()) {
                            String type = ((SimpleValue) prop.getValue()).getTypeName();
                            if ("blob".equals(type) || "clob".equals(type)) hasLob = true;
                            if (Blob.class.getName().equals(type) || Clob.class.getName().equals(type)) hasLob = true;
                        }
                    }
                    if (!hasLob && !clazz.isInherited() && overrideCacheStrategy()) {
                        cfg.setCacheConcurrencyStrategy(
                                clazz.getEntityName(),
                                getCacheConcurrencyStrategy()
                        );
                    }
                }

                iter = cfg.getCollectionMappings();
                while (iter.hasNext()) {
                    Collection coll = (Collection) iter.next();
                    cfg.setCollectionCacheConcurrencyStrategy(
                            coll.getRole(),
                            getCacheConcurrencyStrategy()
                    );
                }

            }

            setSessions(getCfg().buildSessionFactory());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected boolean overrideCacheStrategy() {
        return true;
    }

    protected String getBaseForMappings() {
        return "org/zfin/";
    }

    public String getCacheConcurrencyStrategy() {
        return "nonstrict-read-write";
    }

    protected void setUp() throws Exception {
        if (getSessions() == null || lastTestClass != getClass()) {
            buildSessionFactory(getMappings());
            lastTestClass = getClass();
        }
    }

    protected void runTest() throws Throwable {
        final boolean stats = (sessions).getStatistics().isStatisticsEnabled();
        try {
            if (stats) sessions.getStatistics().clear();

            super.runTest();

            if (stats) sessions.getStatistics().logSummary();

            if (session != null && session.isOpen()) {
                if (session.isConnected()) session.connection().rollback();
                session.close();
                session = null;
                fail("unclosed session");
            } else {
                session = null;
            }
        }
        catch (Throwable e) {
            try {
                if (session != null && session.isOpen()) {
                    if (session.isConnected()) session.connection().rollback();
                    session.close();
                }
            }
            catch (Exception ignore) {
            }
            try {
                if (dropAfterFailure() && sessions != null) {
                    sessions.close();
                    sessions = null;
                }
            }
            catch (Exception ignore) {
            }
            throw e;
        }
    }

    protected boolean dropAfterFailure() {
        return true;
    }

    public org.hibernate.classic.Session openSession() throws HibernateException {
        session = getSessions().openSession();
        return session;
    }

    public org.hibernate.classic.Session openSession(Interceptor interceptor)
            throws HibernateException {
        session = getSessions().openSession(interceptor);
        return session;
    }

    protected abstract String[] getMappings();

    private void setSessions(SessionFactory sessions) {
        HibernateTestCase.sessions = sessions;
    }

    protected SessionFactory getSessions() {
        return sessions;
    }

    private void setDialect(Dialect dialect) {
        HibernateTestCase.dialect = dialect;
    }

    protected Dialect getDialect() {
        return dialect;
    }

    protected static void setCfg(Configuration cfg) {
        HibernateTestCase.cfg = cfg;
    }

    protected static Configuration getCfg() {
        return cfg;
    }

    /**
     * @deprecated
     */
    public Properties getExtraProperties() {
        return new Properties();
    }

    public static void assertClassAssignability(Class source, Class target) throws AssertionFailedError {
        if (!target.isAssignableFrom(source)) {
            throw new AssertionFailedError(
                    "Classes were not assignment-compatible : source<" + source.getName() +
                            "> target<" + target.getName() + ">"
            );
        }
    }

    protected static final Log SKIP_LOG = LogFactory.getLog("org.hibernate.test.SKIPPED");

    protected boolean dialectSupportsEmptyInList(String testdescription) {
        return reportSkip("Dialect does not support SQL: \'x in ()\'.", testdescription, dialectIsNot(new Class[]{
                Oracle9Dialect.class,
                MySQLDialect.class,
                DB2Dialect.class,
                HSQLDialect.class,
                InformixDialect.class
        }));
    }

    protected boolean dialectIsCaseSensitive(String testdescription) {
        //	MySQL and SQLServer is case insensitive on strings (at least in default installation)
        return reportSkip("Dialect is case sensitive. ", testdescription, dialectIsNot(new Class[]{MySQLDialect.class, SQLServerDialect.class}));
    }

    protected boolean reportSkip(String reason, String testDescription, boolean canDoIt) {
        if (!canDoIt) {
            reportSkip(reason, testDescription);
        }
        return canDoIt;
    }

    protected void reportSkip(String reason, String testDescription) {
        SKIP_LOG.warn("*** skipping [" + fullTestName() + "] - " + testDescription + " : " + reason, new Exception());
    }

    private boolean dialectIsNot(Class[] dialectClasses) {
        for (Class dialectClass : dialectClasses) {
            if (dialectClass.isInstance(getDialect())) {
                return false;
            }
        }
        return true;
    }

    public String fullTestName() {
        return this.getName() + " (" + this.getClass().getName() + ")";
    }
}
