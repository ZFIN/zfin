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
    public static final String SSH_STRING = "ssh";

    private static ApplicationProperties props;
    private static File propertyFile;
    private static String webRootDirectory;
    private static boolean secureServer;
    private static String indexDirectory;

    public static final String NON_SECURE_HTTP = "http://";
    public static final String SECURE_HTTP = "https://";


    private static final Logger logger = Logger.getLogger(ZfinProperties.class);
    public static final String ZFIN_DEFAULT_PROPERTIES_XML = "zfin-properties.xml";
    public static final String CATALINA_BASE = System.getProperty("catalina.base");
    public static final String COMMUNITY_WIKI_URL = System.getProperty("COMMUNITY_WIKI_URL");

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
     * @param dir      Directory where property file exists.
     * @param propFile The name of  the property file.
     */
    public static void init(String dir, String propFile) {
        propertyFile = FileUtil.createFileFromDirAndName(dir, propFile);
        if (!propertyFile.exists()) {
            logger.debug("Property file " + propertyFile.getAbsolutePath() + " not found. Use default file.");
            propertyFile = FileUtil.createFileFromDirAndName(dir, ZFIN_DEFAULT_PROPERTIES_XML);
            if (!propertyFile.exists()) {
                String message = "No default Property file " + propertyFile.getAbsolutePath() + " found!";
                logger.error(message);
                throw new RuntimeException(message);
            }
        }
        logger.debug("Property file being used: " + propertyFile.getAbsolutePath());

        try {
            JAXBContext jc = JAXBContext.newInstance(JAXB_PACKAGE);

            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();
            u.setValidating(true);
            if (props != null) {
                logger.debug("Called more than once");
                //ToDO: find out why on the server (embryonix this is called twice)
                //throw new RuntimeException("ZfinProperties class already initialized! This can only be done once ");
            } else {
                props = (ApplicationProperties) u.unmarshal(new FileInputStream(propertyFile));
            }
        } catch (Throwable e) {
            logger.error("Error in initializing the Property File", e);
            throw new RuntimeException(e);
        }

    }

    public static String getLN54ContactEmail() {
        checkValidProperties();
        return stripEmailBackslash(props.getEmail().getLn54Contact());
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

    public static String[] getAdminEmailAddresses() {
        return getAdminEmailAddressString().split(" ");
    }

    public static String getAdminEmailAddressString() {
        checkValidProperties();
        return stripEmailBackslash(props.getEmail().getAdminEmailAddress());
    }

    public static String[] getRequestNewAnatomyTermEmailAddresses() {
        return getRequestNewAnatomyTermEmail().split(" ");
    }

    public static String getRequestNewAnatomyTermEmail() {
        checkValidProperties();
        return stripEmailBackslash(props.getEmail().getRequestNewAnatomyTerm());
    }

    protected static String stripEmailBackslash(String inputString) {
        return inputString.replaceAll("\\\\@", "@");
    }

    public static String[] getValidationOtherEmailAddresses() {
        checkValidProperties();
        return getValidationEmailOtherString().split(" ");
    }

    public static String getValidationEmailOtherString() {
        checkValidProperties();
        return stripEmailBackslash(props.getEmail().getValidationEmailOther());
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

    // convenience method
    // todo: should be removed at some point as I think that both methods are deprecated

    public static String getBlastAllBinary() {
        return getWebHostBinaryPath() + "/" + "wu-blastall";
    }

    // START - local blast methods

    public static String getWebHostDatabasePath() {
        checkValidProperties();
        String blastPath = props.getBlast().getWebHost().getDatabasePath();
        logger.debug("local blast path: " + blastPath);
        return blastPath;
    }

    public static String getWebHostBlastGetBinary() {
        checkValidProperties();
        return getWebHostBinaryPath() + "/" + props.getBlast().getWebHost().getGetBinary();
    }

    public static String getWebHostBlastPutBinary() {
        checkValidProperties();
        return getWebHostBinaryPath() + "/" + props.getBlast().getWebHost().getPutBinary();
    }


    public static String getWebHostUserAtHost() {
        checkValidProperties();
        return props.getBlast().getWebHost().getUser()
                + "@"
                + props.getBlast().getWebHost().getHostName();
    }

    public static String getWebHostBinaryPath() {
        checkValidProperties();
        return props.getBlast().getWebHost().getBinaryPath();
    }

    // END - local blast methods

    // START - remote blast methods

    public static String getBlastServerDatabasePath() {
        checkValidProperties();
        String blastPath = props.getBlast().getBlastServer().getDatabasePath();
        logger.debug("remote blast path: " + blastPath);
        return blastPath;
    }

    public static String getBlastServerBinaryPath() {
        checkValidProperties();
        return props.getBlast().getBlastServer().getBinaryPath();
    }

    public static String getKeyPath() {
        checkValidProperties();
        String blastPath = props.getBlast().getKeyPath();
        logger.debug("remote blast path: " + blastPath);
        return blastPath;
    }

    public static String getBlastServerPutBinary() {
        checkValidProperties();
        return props.getBlast().getBlastServer().getPutBinary();
    }

    public static String getBlastServerGetBinary() {
        checkValidProperties();
        return props.getBlast().getBlastServer().getGetBinary();
    }

    public static String getBlastServerAccessBinary() {
        return SSH_STRING;
    }

    public static String getBlastServerUserAtHost() {
        checkValidProperties();
        return props.getBlast().getBlastServer().getUser()
                + "@"
                + props.getBlast().getBlastServer().getHostName();
    }

    public static String getBlastServerTarget() {
        checkValidProperties();
        return props.getBlast().getTarget();
    }

    public static String getDistributedQueryPath() {
        checkValidProperties();
        return props.getBlast().getDistributedQueryPath();
    }


    // END - remote blast methods

    public static String getSearchIndexDirectory() {
        checkValidProperties();
        String indexPath = props.getPath().getIndexPath();
        indexPath = FileUtil.createAbsolutePath(getWebRootDirectory(), indexPath);
        return indexPath;
    }

    public static String getDomain() {
        checkValidProperties();
        return props.getWeb().getDomain();
    }

    public static String getWikiUserName() {
        checkValidProperties();
        return props.getWiki().getUsername();
    }

    public static String getWikiPassword() {
        checkValidProperties();
        return props.getWiki().getPassword();
    }

    public static String getWikiHostname() {
        checkValidProperties();
        return props.getWiki().getInterfaceHostname();
    }

    public static String getIndexerWikiHostname() {
        checkValidProperties();
        return props.getWiki().getIndexerHostname();
    }

    public static boolean isPushToWiki() {
        checkValidProperties();
        if (props.getWiki().isPushToWiki()) {
            logger.debug("Pushing to wiki: " + props.getWiki().isPushToWiki());
            return true;
        } else {
            logger.warn("Not pushing to wiki: " + props.getWiki().isPushToWiki());
            return false;
        }
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
            throw new RuntimeException(
                    "Server name is null! Please correct the <Server> tag in the zfin-properties.xml file.");
        if (!url.startsWith(SECURE_HTTP) && !url.startsWith(NON_SECURE_HTTP))
            throw new RuntimeException("Server name '" + url +
                    "' is not valid! Please correct the <Server> tag in the zfin-properties.xml file.");
    }

    /**
     * Retrieve the poperty file.
     *
     * @return File handle
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

    public static String getIndexDirectory() {
        return indexDirectory;
    }

    public static void setIndexDirectory(String indexDirectory) {
        ZfinProperties.indexDirectory = indexDirectory;
    }

//    public static boolean isQuartzEnabled(){
//       checkValidProperties();
//        return  props.getWeb().isQuartzEnabled() ;
//    }
//
//    public static void setQuartzEnabled(boolean quartzEnabled){
//        props.getWeb().setQuartzEnabled(quartzEnabled) ;
//    }

}
