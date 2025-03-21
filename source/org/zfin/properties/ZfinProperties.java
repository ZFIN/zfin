package org.zfin.properties;

import freemarker.template.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.gwt.root.ui.HandlesErrorCallBack;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.properties.ant.LoadPropertiesTask;
import org.zfin.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Class that contains global properties for the application, such as
 * email addresses, colors, etc.
 */
public final class ZfinProperties {

    private static final Logger logger = LogManager.getLogger(ZfinProperties.class);
    private static LoadPropertiesTask loadPropertiesTask = new LoadPropertiesTask();
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
        // should load from a default file, or from a -DPROPERTY=<path/to/file>
        if (false == isInit()) {
            if (loadPropertiesTask.getFile() == null) {
                init("home/WEB-INF/zfin.properties");
            } else {
                init(loadPropertiesTask.getFile());
            }
        }
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


    public static void init(String propertiesFileName) {
        loadPropertiesTask.setFile(propertiesFileName);
        loadPropertiesTask.setEnumClass(ZfinPropertiesEnum.class.getCanonicalName());
        loadPropertiesTask.setOverrideSystemProperties(false);
        loadPropertiesTask.setProcessSystemProperties(false);
        loadPropertiesTask.execute();
    }

    public static String getWebHostUserAtHost() {
        return ZfinPropertiesEnum.WEBHOST_USER + "@" + ZfinPropertiesEnum.WEBHOST_HOSTNAME;
    }

    public static String getCurrentPropertyFile() {
        if (loadPropertiesTask != null) {
            return loadPropertiesTask.getFile();
        }
        return null;
    }


    public static Configuration getTemplateConfiguration() {
        if (freeMarkerConfiguration == null) {
            freeMarkerConfiguration = new Configuration();
            try {
                File templateFile = FileUtil.createFile(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value(), "WEB-INF", "templates");
                freeMarkerConfiguration.setDirectoryForTemplateLoading(templateFile);
            } catch (IOException e) {
                logger.error("Could not find template directory", e);
                logger.error("Directory: " + ZfinPropertiesEnum.WEBROOT_DIRECTORY.value() +"/" + "WEB-INF" + "/" + "templates");
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
