package org.zfin.properties;

import freemarker.template.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.gwt.root.ui.HandlesErrorCallBack;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Class that contains global properties for the application, such as
 * email addresses, colors, etc.
 */
public final class ZfinProperties {

    private static final Logger logger = LogManager.getLogger(ZfinProperties.class);
    private static String currentPropertyFile;
    private static Configuration freeMarkerConfiguration;


    public static String[] splitValues(ZfinPropertiesEnum zfinPropertiesEnum) {
        return splitValues(" ", zfinPropertiesEnum);
    }

    public static String[] splitValues(String s, ZfinPropertiesEnum zfinPropertiesEnum) {
        if (zfinPropertiesEnum != null) {
            return zfinPropertiesEnum.value().split(s);
        } else {
            return null;
        }
    }

    public static String[] getAdminEmailAddresses() {
        return splitValues(ZfinPropertiesEnum.ZFIN_ADMIN);
    }

    public static String[] getIndexerReportEmailAddresses() {
        return splitValues(ZfinPropertiesEnum.INDEXER_REPORT_EMAIL);
    }

    public static String getInstance() {
        return ZfinPropertiesEnum.INSTANCE.value();
    }

    public static String[] getValidationOtherEmailAddresses() {
        return splitValues(ZfinPropertiesEnum.VALIDATION_EMAIL_OTHER);
    }

    public static String getCookiePath() {
        return "/";
    }

    public static boolean isPushToWiki() {
        return Boolean.valueOf(ZfinPropertiesEnum.WIKI_PUSH_TO_WIKI.value());
    }

    public static String getSessionLogName() {
//        return "zfin-session.log";
        return ZfinPropertiesEnum.LOG_FILE_SESSION.value();
    }


    public static long getLogFileSize() {
        float sizeMega = Float.parseFloat(ZfinPropertiesEnum.LOG_FILE_SIZE.value());
        sizeMega *= 1024 * 1024;
        return (long) sizeMega;
    }

    public static int getMaxlogFiles() {
        return Integer.parseInt(ZfinPropertiesEnum.LOG_FILE_MAX.value());
    }

    public static String getServer() {
        StringBuilder baseUrl = new StringBuilder();
        int port = Integer.parseInt(ZfinPropertiesEnum.APACHE_PORT.value());
        int securePort = Integer.parseInt(ZfinPropertiesEnum.APACHE_PORT_SECURE.value());
        if (isTrue(ZfinPropertiesEnum.SECURE_SERVER)) {
            baseUrl.append(ZfinPropertiesEnum.SECURE_HTTP);
            baseUrl.append(ZfinPropertiesEnum.APACHE_SERVER_NAME.value());
            // 443 is the default secure server port
            if (port != 443)
                baseUrl.append(":" + securePort);
        } else {
            baseUrl.append(ZfinPropertiesEnum.NON_SECURE_HTTP.value());
            baseUrl.append(ZfinPropertiesEnum.APACHE_SERVER_NAME.value());
            // 80 is the default port
            if (port != 80)
                baseUrl.append(":" + port);
        }
        if (baseUrl.charAt(baseUrl.length() - 1) != '/')
            baseUrl.append("/");
        checkValidServerName(baseUrl.toString());
        return baseUrl.toString();
    }

    private static boolean isTrue(ZfinPropertiesEnum zfinPropertiesEnum) {
        return Boolean.valueOf(zfinPropertiesEnum.value());
    }

    /**
     * Check that the String is a fully qualified http URL, i.e.
     * Either starting with http:// or https://
     *
     * @param url URL
     */
    private static void checkValidServerName(String url) {
        if (url == null)
            throw new RuntimeException(
                    "Server name is null! Please correct the <Server> tag in the zfin-properties.xml file.");
        if (!url.startsWith(ZfinPropertiesEnum.SECURE_HTTP.value()) && !url.startsWith(ZfinPropertiesEnum.NON_SECURE_HTTP.value()))
            throw new RuntimeException("Server name '" + url +
                    "' is not valid! Please correct the <Server> tag in the zfin-properties.xml file.");
    }

    public static void setValues(Map<String, String> results) {
        for (String key : results.keySet()) {
            ZfinPropertiesEnum.valueOf(key).setValue(results.put(key, results.get(key)));
        }
    }


    public static void loadFromRPCCall() {
        LookupRPCService.App.getInstance().getAllZfinProperties(new HandlesErrorCallBack<Map<String, String>>("Failed to load") {

            @Override
            public void onSuccess(Map<String, String> result) {
                ZfinProperties.setValues(result);
            }
        });
    }

    public static String getWebHostBlastGetBinary() {
        return ZfinPropertiesEnum.WEBHOST_BINARY_PATH + "/" + ZfinPropertiesEnum.WEBHOST_XDGET;
    }

    public static String getWebHostBlastPutBinary() {
        return ZfinPropertiesEnum.WEBHOST_BINARY_PATH + "/" + ZfinPropertiesEnum.WEBHOST_XDFORMAT;
    }

    public static void init() {
        if (!isInit()) {
            String propertiesPath = resolvePropertiesPath();
            init(propertiesPath);
        }
    }

    /**
     * Resolve the properties file path using the following precedence:
     * 1. System property: -Dzfin.properties.path=/path/to/file
     * 2. Environment variable: ZFIN_PROPERTIES_PATH
     * 3. Web context: webRoot + /WEB-INF/zfin.properties (if running in servlet container)
     * 4. Default: home/WEB-INF/zfin.properties (relative to working directory)
     */
    private static String resolvePropertiesPath() {
        // 1. Check system property
        String systemProp = System.getProperty("zfin.properties.path");
        if (systemProp != null && !systemProp.isEmpty()) {
            logger.info("Using properties path from system property: {}", systemProp);
            return systemProp;
        }

        // 2. Check environment variable
        String envVar = System.getenv("ZFIN_PROPERTIES_PATH");
        if (envVar != null && !envVar.isEmpty()) {
            logger.info("Using properties path from ZFIN_PROPERTIES_PATH: {}", envVar);
            return envVar;
        }

        // 3. Check web context
        String webRoot = ZfinPropertiesLoadListener.getWebRoot();
        if (webRoot != null) {
            String webPath = webRoot + "/WEB-INF/zfin.properties";
            logger.info("Using properties path from web context: {}", webPath);
            return webPath;
        }

        // 4. Default to relative path
        String defaultPath = "home/WEB-INF/zfin.properties";
        logger.info("Using default properties path: {}", defaultPath);
        return defaultPath;
    }

    public static boolean isInit() {
        return
                ZfinPropertiesEnum.INSTANCE.value() != null
                        &&
                        ZfinPropertiesEnum.SECURE_HTTP.value() != null
                ;
    }

    public static void validateProperties() {
        String instance = ZfinPropertiesEnum.INSTANCE.value();
        if (instance == null) {
            instance = System.getenv("INSTANCE");
            logger.error("Instance undefined in properties files, getting from environment[" + instance + "]");
        }
        for (ZfinPropertiesEnum zfinPropertiesEnum : ZfinPropertiesEnum.values()) {
            if (zfinPropertiesEnum.value() == null) {
                logger.error("Property[" + zfinPropertiesEnum.name() + " not defined for INSTANCE[" + ZfinPropertiesEnum.INSTANCE + "]");
            } else {
                logger.info("Property[" + zfinPropertiesEnum.name() + "=[" + zfinPropertiesEnum.value() + "]");
            }
        }
    }


    /**
     * Load properties from the specified file and populate ZfinPropertiesEnum values.
     */
    public static void init(String propertiesFileName) {
        currentPropertyFile = propertiesFileName;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFileName)) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file: " + propertiesFileName, e);
        }

        // Load each property into the corresponding enum constant
        for (String key : props.stringPropertyNames()) {
            try {
                ZfinPropertiesEnum enumConstant = ZfinPropertiesEnum.valueOf(key);
                enumConstant.setValue(props.getProperty(key));
            } catch (IllegalArgumentException e) {
                // Property exists in file but not in enum - log warning but don't fail
                logger.warn("Property '{}' found in {} but not defined in ZfinPropertiesEnum",
                           key, propertiesFileName);
            }
        }

        logger.info("Loaded {} properties from {}", props.size(), propertiesFileName);
    }

    public static String getCurrentPropertyFile() {
        return currentPropertyFile;
    }

    public static Configuration getTemplateConfiguration() {
        if (freeMarkerConfiguration == null) {
            freeMarkerConfiguration = new Configuration();
            try {
                File templateFile = FileUtil.createFileFromDirAndName(ZfinPropertiesEnum.WAR_WEB_INF_DIR.value(), "templates");
                freeMarkerConfiguration.setDirectoryForTemplateLoading(templateFile);
            } catch (IOException e) {
                logger.error("Could not find template directory", e);
                logger.error("Directory: " + ZfinPropertiesEnum.WAR_WEB_INF_DIR.value() + "/" + "templates");
            }
        }
        return freeMarkerConfiguration;
    }


    public static String getJavaRoot() {
        return getJavaRoot(false);
    }

    public static String getJavaRoot(boolean secure) {
        return (secure ? ZfinPropertiesEnum.SECURE_HTTP : ZfinPropertiesEnum.NON_SECURE_HTTP)
                + ZfinPropertiesEnum.DOMAIN_NAME.value() + "/action";
    }

    public static String getInspectletID() {
        return ZfinPropertiesEnum.INSPECTLET_ID.toString();
    }

    public static Path getOntologyReloadStatusDirectory() {
        return Paths.get(ZfinPropertiesEnum.TARGETROOT.toString() + "/server_apps/data_transfer/LoadOntology/reload-status");
    }

    public static Path getDownloadReloadStatusDirectory() {
        return Paths.get(ZfinPropertiesEnum.DOWNLOAD_DIRECTORY.toString() + "/reload-status");
    }


}
