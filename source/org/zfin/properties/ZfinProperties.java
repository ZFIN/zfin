package org.zfin.properties;

import org.apache.log4j.Logger;
import org.zfin.util.FileUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;

/**
 * Class that contains global properties for the application, such as
 * email addresses, colors, etc.
 */
public final class ZfinProperties {

    public static final String JAXB_PACKAGE = "org.zfin.properties";
    public static final String CONFIGURATION_DIRECTORY = "conf";

    private static ApplicationProperties props;
    private static File propertyFile;
    private static String webRootDirectory;
    private static boolean secureServer;

    private static final String NON_SECURE_HTTP = "http://";
    private static final String SECURE_HTTP = "https://";

    private static final Logger LOG = Logger.getLogger(ZfinProperties.class);
    public static final String ZFIN_DEFAULT_PROPERTIES_XML = "zfin-properties.xml";
    public static final String CATALINA_BASE = System.getProperty("catalina.base");

    public  static final String FULL_UPDATE_DOI = "org.zfin.datatransfer.DOI" ; 
    public  static final String LIGHT_UPDATE_DOI = "org.zfin.datatransfer.DOI.light" ; 
    public static final String MICROARRAY_NOT_FOUND = "MICROARRAY_NOT_FOUND";
    public static final String MICROARRAY_ERROR = "MICROARRAY_ERROR";
    public static final String MICROARRAY_INFO = "MICROARRAY_INFO";

    /**
     * Initialize properties via property file import.
     *
     * @param properties onfiguration data from the zfin-properties.xml file
     */
    public static void init(ApplicationProperties properties) {
        props = properties;
    }

    /**
     * Pass in the path to the property file.
     *
     * @param dir
     * @param propFile
     */
    public static void init(String dir, String propFile) {
        propertyFile = FileUtil.createFileFromDirAndName(dir, propFile);
        if(!propertyFile.exists()){
            LOG.info("Property file " + propertyFile.getAbsolutePath() + " not found. Use default file.");
            propertyFile = FileUtil.createFileFromDirAndName(dir, ZFIN_DEFAULT_PROPERTIES_XML);
            if(!propertyFile.exists()){
                String message = "No default Property file " + propertyFile.getAbsolutePath() + " found!";
                LOG.error(message);
                throw new RuntimeException(message);
            }
        }
        LOG.info("Property file being used: " + propertyFile.getAbsolutePath());

        try {
            JAXBContext jc = JAXBContext.newInstance(JAXB_PACKAGE);

            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();
            u.setValidating(true);
            if (props != null) {
                LOG.info("Called more than once");
                //ToDO: find out why on the server (embryonix this is called twice)
                //throw new RuntimeException("ZfinProperties class already initialized! This can only be done once ");
            } else {
                props = (ApplicationProperties) u.unmarshal(new FileInputStream(propertyFile));
            }
        } catch (Throwable e) {
            LOG.error("Error in initializing the Property File", e);
            throw new RuntimeException(e);
        }

    }

    public static String getBackgroundColor() {
        checkValidProperties();
        return props.getWeb().getBackgroundColor();
    }

    public static String getHeaderColor() {
        checkValidProperties();
        return props.getWeb().getHeaderColor();
    }

    public static String getHighlightColor() {
        checkValidProperties();
        return props.getWeb().getHighlightColor();
    }

    public static String getAdminEmailAddress() {
        checkValidProperties();
        return props.getEmail().getAdminEmailAddress();
    }

    public static String stripEmailBackslash(String inputString){
        return inputString.replaceAll("\\\\@","@")  ;
    }
    

    public static String getValidationEmailOther(){
        checkValidProperties();
        String returnString = props.getEmail().getValidationEmailOther();
        return returnString ;
    }

    public static String getHighlighterColor() {
        checkValidProperties();
        return props.getWeb().getHighlighterColor();
    }

    public static String getLinkbarColor() {
        checkValidProperties();
        return props.getWeb().getLinkbarColor();
    }

    public static String getFtpPath() {
        checkValidProperties();
        return props.getPath().getFtpRootPath();
    }

    public static String getImageLoadPath() {
        checkValidProperties();
        return props.getPath().getImageLoadPath();
    }

    public static String getLoadUpFull() {
        checkValidProperties();
        return props.getPath().getLoadUpFull();
    }

    public static String getPdfPath() {
        checkValidProperties();
        return props.getPath().getPdfPath();
    }

    public static String getDatabaseInstanceName() {
        checkValidProperties();
        return props.getPath().getWebdriver();
    }

    public static String getWebDriver() {
        checkValidProperties();
        return props.getPath().getWebdriver();
    }

    public static String getCookiePath() {
        checkValidProperties();
        return props.getPath().getCookiePath();
    }

    public static int getInsecureServerPort() {
        checkValidProperties();
        return props.getPath().getServer().getPort();
    }

    public static int getSecureServerPort() {
        checkValidProperties();
        return props.getPath().getServer().getSecurePort();
    }

    public static String getLogFileName() {
        checkValidProperties();
        return props.getLogging().getLogFileName();
    }

    //ToDo: include in zfin-properties file
    public static String getSessionLogName() {
/*
        checkValidProperties();
        return props.getLogging().getLogFileName();
*/
        return "zfin-session.log";
    }

    public static String getSearchIndexDirectory() {
        checkValidProperties();
        String indexPath = props.getPath().getIndexPath();
        indexPath = FileUtil.createAbsolutePath(getWebRootDirectory(), indexPath);
        return indexPath;
    }

    public static String getTomcatLogFileName() {
        String catalinaDir = CATALINA_BASE;
        String absoluteFilePath = FileUtil.createAbsolutePath(catalinaDir, "logs", props.getLogging().getLogFileName());
        return absoluteFilePath;
    }


    public static long getLogFileSize() {
        checkValidProperties();
        String size = props.getLogging().getLogFileSize();
        float sizeMega = Float.parseFloat(size);
        sizeMega *= 1024 * 1024;
        return (long) sizeMega;
    }

    public static int getMaxlogFiles() {
        checkValidProperties();
        return props.getLogging().getMaxLogFiles();
    }

    public static String getLogFilePattern() {
        checkValidProperties();
        return props.getLogging().getLogPattern();
    }

    public static String getServer() {
        checkValidProperties();
        StringBuilder baseUrl = new StringBuilder();
        int port = props.getPath().getServer().getPort();
        int securePort = props.getPath().getServer().getSecurePort();
        if (secureServer) {
            baseUrl.append(SECURE_HTTP);
            baseUrl.append(props.getPath().getServer().getName());
            // 443 is the default secure server port
            if (port != 443)
                baseUrl.append(":" + securePort);
        } else {
            baseUrl.append(NON_SECURE_HTTP);
            baseUrl.append(props.getPath().getServer().getName());
            // 80 is the default port
            if (port != 80)
                baseUrl.append(":" + port);
        }
        if (baseUrl.charAt(baseUrl.length() - 1) != '/')
            baseUrl.append("/");
        checkValidServerName(baseUrl.toString());
        return baseUrl.toString();
    }

    /**
     * Check that the String is a fully qualified http URL, i.e.
     * Either starting with http:// or https://
     *
     * @param url URL
     */
    private static void checkValidServerName(String url) {
        if (url == null)
            throw new RuntimeException("Server name is null! Please correct the <Server> tag in the zfin-properties.xml file.");
        if (!url.startsWith(SECURE_HTTP) && !url.startsWith(NON_SECURE_HTTP))
            throw new RuntimeException("Server name '" + url + "' is not valid! Please correct the <Server> tag in the zfin-properties.xml file.");
    }

    /**
     * Retrieve the poperty file.
     */
    public static File getZfinPropertyFile() {
        return propertyFile;
    }

    // if initialization happens not via applicationProperties include
    // that logic in here.
    private static void checkValidProperties() {
        if (props == null)
            throw new RuntimeException("Properties are not yet initialized");
    }

    public static String getWebRootDirectory() {
        return webRootDirectory;
    }

    public static void setWebRootDirectory(String webRootDir) {
        webRootDirectory = webRootDir;
    }

    public static void setSecureServer(boolean secureServer) {
        ZfinProperties.secureServer = secureServer;
    }

}
