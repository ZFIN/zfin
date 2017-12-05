package org.zfin.framework;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.InvalidMappingException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.FileUtil;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class HibernateSessionCreator.  Used to handle connections without going through Tomcat explicitly.
 * <p/>
 */
public class HibernateSessionCreator {

    public static final Logger LOG = RootLogger.getLogger(HibernateSessionCreator.class);

    private boolean showSql = false;
    private boolean autocommit = false;

    public HibernateSessionCreator() {
        this(false);
    }

    public HibernateSessionCreator(String dbName) {
        init(showSql, false, dbName);
    }

    public HibernateSessionCreator(boolean showSql) {
        String db = ZfinPropertiesEnum.DB_NAME.value();
        init(showSql, false, db);
    }

    public HibernateSessionCreator(boolean showSql, boolean autocommit) {
        String db = ZfinPropertiesEnum.DB_NAME.value();
        init(showSql, autocommit, db);
    }

    public void init(boolean showSql, boolean autocommit, String db) {
        this.autocommit = autocommit;
        LOG.info("Start Hibernate Session Creation");
        this.showSql = showSql;
        String configDirectory = ZfinPropertiesEnum.HIBERNATE_CONFIGURATION_DIRECTORY.value();
        String showSqlString = ZfinPropertiesEnum.SHOW_SQL.value();
        if (showSqlString != null && showSqlString.equals("true")) {
            this.showSql = true;
        }
        if (configDirectory == null) {
            throw new RuntimeException("Failed to instantiate configDirectory");
        }
        Configuration config = createConfiguration(db);
        File[] hbmFiles = getHibernateConfigurationFiles();
        if (hbmFiles == null)
            throw new RuntimeException("No Hibernate mapping files found!");

        LOG.debug("Hibernate Mapping files being used:");
        for (File file : hbmFiles) {
            LOG.debug(file.getAbsolutePath());
        }

        // first add filter.hbm.xml bug in Hibernate!!
        for (File configurationFile : hbmFiles) {
            if (configurationFile.getName().startsWith("filters.")) {
                config.addFile(configurationFile);
                break;
            }
        }
        // now add the others
        for (File configurationFile : hbmFiles) {
            if (!configurationFile.getName().startsWith("filters.")) {
                LOG.debug("Loading Hibernate mapping file: " + configurationFile.getAbsolutePath());
                try {
                    config.addFile(configurationFile);
                } catch (InvalidMappingException e) {
                    LOG.error("Error Loading Hibernate mapping file: " + configurationFile.getAbsolutePath());
                }
            }
        }
        if (!db.equals("sysmaster")) {

            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
            provider.setResourceLoader(new PathMatchingResourcePatternResolver(this.getClass().getClassLoader()));
            final Set<BeanDefinition> classes = provider.findCandidateComponents("org.zfin");
            for (BeanDefinition bbean : classes) {
                try {
                    ScannedGenericBeanDefinition bean = (ScannedGenericBeanDefinition) bbean;
                    Set<String> annotationSet = bean.getMetadata().getAnnotationTypes();
                    if (annotationSet.contains("javax.persistence.Entity")) {
                        Class<?> clazz = Class.forName(bean.getBeanClassName());
                        config.addAnnotatedClass(clazz);
                        LOG.info("Loaded Annotated Class: " + clazz.getName());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            HibernateUtil.init(config.buildSessionFactory());
        }
    }

    public static File[] getHibernateConfigurationFiles() {
        // first in the source directory
        File hibernateConfDir = FileUtil.createFileFromStrings("source", "org", "zfin");
        // if not found in the source (used for testing) then check in the classpath
        if (hibernateConfDir == null || !hibernateConfDir.exists()) {
            // a bit hacky...
            ClassLoader cl = HibernateSessionCreator.class.getClassLoader();
            String directory = cl.getResource("org/zfin/filters.hbm.xml").toString();
            directory = directory.substring(0, directory.lastIndexOf("/"));
            directory = directory.replace("file:/", "/");
            hibernateConfDir = FileUtil.createFileFromStrings(directory);
        }
        File[] hibernateConfigurationFiles = hibernateConfDir.listFiles(new HibernateFilenameFilter());
        if (hibernateConfigurationFiles == null) {
            // then search in the classpath
            URL resource = ClassLoader.getSystemResource(FileUtil.createFileFromStrings("org", "zfin").getPath());

            String hibernateConfDirString = URLDecoder.decode(resource.getFile());
            LOG.debug("hibernateConfDirString: " + hibernateConfDirString);
            hibernateConfigurationFiles = (new File(hibernateConfDirString)).listFiles(new HibernateFilenameFilter());
            if (hibernateConfigurationFiles == null)
                throw new NullPointerException("No configuration files found in directory" + hibernateConfDir.getAbsolutePath());
        }
        LOG.info("Hibernate Configuration files in directory: " + hibernateConfDir.getAbsolutePath());
        return hibernateConfigurationFiles;
    }

    static class HibernateFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.endsWith(".hbm.xml");
        }
    }

    private Configuration createConfiguration(String db) {
        Configuration config = new AnnotationConfiguration();
        config.setInterceptor(new StringCleanInterceptor());
        if (ZfinPropertiesEnum.USE_POSTGRES.value().equals("true")) {
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
            config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        } else {
            config.setProperty("hibernate.dialect", "org.zfin.database.ZfinInformixDialect");
            config.setProperty("hibernate.connection.driver_class", "com.informix.jdbc.IfxDriver");
        }

        config.setProperty("hibernate.connection.autocommit", String.valueOf(autocommit));
        config.setProperty("hibernate.connection.url", getJdbcUrl(db));

//        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
        config.setProperty("hibernate.show_sql", Boolean.toString(showSql));
        config.setProperty("hibernate.format_sql", "true");
        config.setProperty("hibernate.connection.pool_size", "20");
//        config.setProperty("hibernate.cglib.use_reflection_optimizer", "false");
//        config.setProperty("hibernate.cache.provider_class", "net.sf.ehcache.hibernate.EhCacheProvider");
        config.setProperty("hibernate.cache.provider_configuration_file_resource_path", "conf");
        config.setProperty("hibernate.cache.use_second_level_cache", "false");
        //config.setProperty("hibernate.cache.use_query_cache", "true");
//        config.setProperty("hibernate.use_sql_comments", "true");
        return config;
    }

    private void createJndi(String db) {
        if (db == null) {
            throw new RuntimeException("No DB Name provided ");
        }
        String jdbcUrl = getJdbcUrl(db);
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES,
                    "org.apache.naming");
            InitialContext ic = new InitialContext();
            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");

            // Construct DataSource
            ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass("com.informix.jdbc.IfxDriver"); //loads the jdbc driver
            cpds.setJdbcUrl(jdbcUrl);
            cpds.setUser("zfinner");
            cpds.setPassword("Rtwm4ts");
            cpds.setMaxPoolSize(4);
            cpds.setMinPoolSize(2);
            cpds.setIdleConnectionTestPeriod(1200);
            ic.bind(getJndiAccessName(db), cpds);
        } catch (NamingException | PropertyVetoException ex) {
            if (!(ex instanceof NameAlreadyBoundException))
                LOG.warn(ex);
            else
                LOG.error(ex);
        }

    }

    private String getJdbcUrl(String db) {
        String informixServer = ZfinPropertiesEnum.INFORMIX_SERVER.value();
        String informixPort = ZfinPropertiesEnum.INFORMIX_PORT.value();
        String sqlHostsHost = ZfinPropertiesEnum.SQLHOSTS_HOST.value();

        String jdbcUrl;
        if (ZfinPropertiesEnum.USE_POSTGRES.value().equals("true")) {
            jdbcUrl = "jdbc:postgresql://localhost:5432/" + db;
        } else {
            jdbcUrl = "jdbc:informix-sqli://" + sqlHostsHost + ":" + informixPort + "/" + db + ":INFORMIXSERVER=" + informixServer;
            jdbcUrl += ";IFX_LOCK_MODE_WAIT=6;PDQPRIORITY=20;IFX_ISOLATION_LEVEL=DR;IFX_DIRTY_WAIT=10;CLIENT_LOCALE=en_US.utf8;DB_LOCALE=en_US.utf8";
            jdbcUrl += ";user=zfinner;password=Rtwm4ts";
        }
        return jdbcUrl;
    }

    private String getJndiAccessName(String db) {
        return "java:/comp/env/jdbc/" + db;
    }


} 


