package org.zfin.wiki.service;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.wiki.*;

import javax.xml.rpc.ServiceException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Please see http://confluence.atlassian.com/display/DOC/Remote+API+Specification
 * for further documentation of the service class.
 */
public class WikiWebService {

    protected String token = null;
    protected ConfluenceSoapService service = null;
    protected final String PAGE_TYPE = "page";
    protected final String ENDPOINT_SUFFIX = "/rpc/soap-axis/confluenceservice-v2";
    protected final String WEBSERVICE_PROTOCOL = "https://";

    protected String wikiUserName = ZfinPropertiesEnum.WIKI_USER.value();
    protected String wikiPassword = ZfinPropertiesEnum.WIKI_PASS.value();
    protected String wikiHost = ZfinPropertiesEnum.WIKI_HOST.value();
    protected String domainName = ZfinPropertiesEnum.DOMAIN_NAME.value();

    public static final Logger logger = LogManager.getLogger(WikiWebService.class);

    private final static String SANDBOX_DEFAULT_CONTENT =
            "<p>This is the home of the SandBox space.</p>\n" +
                    "\n" +
                    "\n" +
                    "<p>To help you on your way, we've inserted some of our favourite macros on this home page. As you start creating pages, adding news items and commenting you'll see the macros below fill up with all the activity in your space.</p>\n" +
                    "\n" +
                    "<ac:macro ac:name=\"section\"><ac:rich-text-body>\n" +
                    "<ac:macro ac:name=\"column\"><ac:parameter ac:name=\"width\">60%</ac:parameter><ac:rich-text-body>\n" +
                    "<ac:macro ac:name=\"recently-updated\" />\n" +
                    "\n" +
                    "</ac:rich-text-body></ac:macro>\n" +
                    "\n" +
                    "<ac:macro ac:name=\"column\"><ac:parameter ac:name=\"width\">5%</ac:parameter></ac:macro>\n" +
                    "\n" +
                    "<ac:macro ac:name=\"column\"><ac:parameter ac:name=\"width\">35%</ac:parameter><ac:rich-text-body>\n" +
                    "\n" +
                    "<p>Navigate space</p>\n" +
                    "<ac:macro ac:name=\"pagetreesearch\" />\n" +
                    "\n" +
                    "<ac:macro ac:name=\"pagetree\" />\n" +
                    "</ac:rich-text-body></ac:macro>\n" +
                    "</ac:rich-text-body></ac:macro>";


    private static WikiWebService instance = null;

    protected WikiWebService() {
    }

    public static WikiWebService getInstance() throws WikiLoginException {
        return getInstance(null);
    }

    public static WikiWebService getInstance(String wikiHost) throws WikiLoginException {
        if (instance == null) {
            instance = new WikiWebService();
            if (wikiHost != null) {
                instance.wikiHost = wikiHost;
            }
        }
        if (instance.login()) {
            return instance;
        } else {
            throw new WikiLoginException("login result was false");
        }
    }

    public boolean login() throws WikiLoginException {
        try {
            if (StringUtils.isEmpty(wikiUserName)) {
                throw new WikiLoginException("userName undefined in ZfinProperties, not executing wiki service.  Please define WIKI_USER");
            }
            if (StringUtils.isEmpty(wikiPassword)) {
                throw new WikiLoginException("password undefined in ZfinProperties, not executing wiki service.  Please define WIKI_PASS");
            }
            if (StringUtils.isEmpty(wikiHost)) {
                throw new WikiLoginException("wiki host undefined in ZfinProperties, not executing wiki service.  Please define WIKI_HOST");
            }
            if (StringUtils.isEmpty(domainName)) {
                throw new WikiLoginException("domain undefined in ZfinProperties, not executing wiki service.  Please define DOMAIN_NAME");
            }
            if (token != null) {
                try {
//                    return ( service.hasUser(token,ZfinProperties.getWikiUserName()) ) ;
                    if (service.hasUser(token, ZfinPropertiesEnum.WIKI_USER.value())) {
                        return true;
                    } else {
                        throw new WikiLoginException("Failed to find webservice user: " + ZfinPropertiesEnum.WIKI_USER.value());
                    }
                } catch (InvalidSessionException e) {
                    return doLogin();
                }
            }
            // if there is no token than do the login
            else {
                return doLogin();
            }
        } catch (Exception e) {
            throw new WikiLoginException(e);
        }
    }

    private boolean doLogin() throws WikiLoginException, ServiceException {
        ConfluenceSoapServiceServiceLocator serviceLocator = new ConfluenceSoapServiceServiceLocator();
        try {
            String wikiServer = WEBSERVICE_PROTOCOL + wikiHost + ENDPOINT_SUFFIX;
            serviceLocator.setConfluenceserviceV2EndpointAddress(wikiServer);
            service = serviceLocator.getConfluenceserviceV2();
            token = service.login(wikiUserName, wikiPassword);
            logger.info("logging into " + wikiServer);

            if (service == null) {
                throw new WikiLoginException("service is null, failed to instantiate");
            }

            if (token == null) {
                throw new WikiLoginException("token is null, failed to set");
            }

            return true;
        } catch (Exception e) {
            throw new WikiLoginException(e);
        }

    }

    public boolean logout() throws Exception {
        boolean returnValue = service.logout(token);
        token = null;
        return returnValue;
    }


    public String getDomainName() {
        return domainName;
    }

    public String getWikiHost() {
        return wikiHost;
    }

    /**
     * @param spaceToken The token for the space.
     * @return All page summaries for a given space.
     */
    public RemotePageSummary[] getAllPagesForSpace(String spaceToken) {
        try {
            return service.getPages(token, spaceToken);
        } catch (java.rmi.RemoteException e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * @param spaceToken The token for the space.
     * @return All page summaries for a given space.
     */
    public RemoteBlogEntrySummary[] getAllBLogPagesForSpace(String spaceToken) {
        try {
            return service.getBlogEntries(token, spaceToken);
        } catch (java.rmi.RemoteException e) {
            logger.error(e);
            return null;
        }
    }


    /**
     * @param title      Title of the page to find.
     * @param spaceToken Space of the page to find.
     * @return The remote page.
     */
    public RemotePage getPageForTitleAndSpace(String title, String spaceToken) {
        try {
            return service.getPage(token, spaceToken, title);
        } catch (java.rmi.RemoteException e) {
            logger.error(e);
            return null;
        }
    }

    public List<String> setOwnerForLabel(String label) throws Exception {

        List<String> modifiedPages = new ArrayList<>();

        if (!ZfinProperties.isPushToWiki()) {
            logger.debug("Not configured to update the wiki: WIKI_PUSH_TO_WIKI=false");
            return modifiedPages;
        }

        RemotePage pageSummary = null;
        try {
            RemoteSearchResult[] pages = service.getLabelContentByName(token, label);
            logger.debug("pages to process: " + pages.length + " for label: " + label);

            // for each page:
            // if there is is editing or view restriction, then add an editing one to the creator
            for (RemoteSearchResult page : pages) {
                logger.debug("processing page[" + page.getTitle() + "] type[" + page.getType() + "]");
                // don't no why but this page throws an exception: should have the id: 131090 but
                // has 131089 which does not exist. Must be a bug in Confluence
                if (page.getUrl().endsWith("wiki.zfin.org/display/AB")) {
                    continue;
                }
                pageSummary = service.getPage(token, page.getId());
                // do not work on deleted pages
                if (pageSummary.getContentStatus().equals("deleted")) {
                    continue;
                }
                logger.info(page.getTitle());
                if (page.getType().equals(PAGE_TYPE) && service.getContentPermissionSets(token, page.getId()).length == 0) {
                    logger.debug("processing page[" + page.getTitle() + "] type[" + page.getType() + "] # perm["
                            + service.getContentPermissionSets(token, page.getId()).length + "]");
                    RemoteContentPermission permissionForUser = new RemoteContentPermission();
                    RemotePage remotePage = service.getPage(token, page.getId());
                    permissionForUser.setUserName(remotePage.getCreator());
                    logger.info(" setting permission for: " + page.getTitle() + " to " + remotePage.getCreator());
                    permissionForUser.setType(Permission.EDIT.getValue());
                    RemoteContentPermission[] permissions = new RemoteContentPermission[2];
                    permissions[0] = permissionForUser;

                    RemoteContentPermission permissionForZfinGroup = new RemoteContentPermission();
                    permissionForZfinGroup.setGroupName(Group.ZFIN_USERS.getValue());
                    logger.info(" setting permission for: " + page.getTitle() + " to group " + Group.ZFIN_USERS);
                    permissionForZfinGroup.setType(Permission.EDIT.getValue());
                    permissions[1] = permissionForZfinGroup;

                    service.setContentPermissions(token, page.getId(), Permission.EDIT.getValue(), permissions);

                    modifiedPages.add(page.getTitle());
                }
            }  // end of for loop
        } catch (Exception e) {
            logger.error("Unable to set owner for label[" + label + "] and page title: " + pageSummary.getTitle(), e);
            throw e;
        }
        return modifiedPages;
    }

    /**
     * Code to clean the sandbox space.
     * Removes all pages individually and then creates a new home page.
     * Was previously dropping the entire space, but makes the space look like it is new, not cleaned.
     * see fogbugz 5179 for more details.
     *
     * @throws Exception Thrown if problems during the process.
     */
    public void cleanSandbox() throws Exception {
        if (!ZfinProperties.isPushToWiki()) {
            logger.info("Not configured to update the wiki: WIKI_PUSH_TO_WIKI=false");
            return;
        }

        RemotePageSummary[] pages = service.getPages(token, Space.SANDBOX.getValue());
        logger.info(pages.length + " pages in ZFIN community Wiki: Sandbox");
        if (pages.length == 1) {
            if (service.getPage(token, pages[0].getId()).getContent().equals(SANDBOX_DEFAULT_CONTENT)) {
                logger.info("Nothing changed in wiki sandbox homepage, doing nothing");
                return;
            }
        }
        logger.info("Wiki sandbox changed, dropping all pages...");

        if (pages != null && pages.length > 0) {
            for (RemotePageSummary page : pages) {
                logger.info("removing page: " + page.getTitle());
                service.removePage(token, page.getId());
            }
        }
        RemotePage homePage = new RemotePage();
        homePage.setContent(SANDBOX_DEFAULT_CONTENT);
        homePage.setCreated(GregorianCalendar.getInstance());
        homePage.setCreator(wikiUserName);
        homePage.setHomePage(true);
        homePage.setCurrent(true);
        homePage.setSpace(Space.SANDBOX.getValue());
        homePage.setTitle("Home");
        service.storePage(token, homePage);
        logger.info("Wiki sandbox created home page");
    }

    public RemoteSearchResult[] getLabelContent(String label) throws Exception {
        return service.getLabelContentByName(token, label);
    }

    public RemoteContentPermission[] getRemoteContentPermissions(long id, String type) throws Exception {
        RemoteContentPermissionSet remoteContentPermissionSet = service.getContentPermissionSet(token, id, type);
        return remoteContentPermissionSet.getContentPermissions();
    }

    public RemotePage getPage(long id) throws Exception {
        return service.getPage(token, id);
    }

    public RemoteBlogEntry getBlogPage(long id) throws Exception {
        return service.getBlogEntry(token, id);
    }
}
