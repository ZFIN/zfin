package org.zfin.wiki;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;

import java.util.GregorianCalendar;

/**
 * Please see http://confluence.atlassian.com/display/DOC/Remote+API+Specification
 * for further documentation of the service class.
 */
public class WikiWebService {

    protected String token = null;
    protected ConfluenceSoapService service = null;
    protected final String PAGE_TYPE = "page";
    protected final String ENDPOINT_SUFFIX = "/rpc/soap-axis/confluenceservice-v1";
    protected final String WEBSERVICE_PROTOCOL = "https://";

    protected String wikiUserName = ZfinProperties.getWikiUserName();
    protected String wikiPassword = ZfinProperties.getWikiPassword();
    protected String wikiHost = ZfinProperties.getWikiHostname();
    protected String domainName = ZfinProperties.getDomain();

    private final Logger logger = Logger.getLogger(WikiWebService.class);


    private static WikiWebService instance = null;

    protected WikiWebService() { }

    public static WikiWebService getInstance() throws WikiLoginException {
        if (instance == null) {
            instance = new WikiWebService();
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
                    if (service.hasUser(token, ZfinProperties.getWikiUserName())) {
                        return true;
                    } else {
                        throw new WikiLoginException("Failed to find webservice user: "+ZfinProperties.getWikiUserName());
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

    private boolean doLogin() throws WikiLoginException {
        ConfluenceSoapServiceServiceLocator serviceLocator = new ConfluenceSoapServiceServiceLocator();
        serviceLocator.setConfluenceserviceV1EndpointAddress(WEBSERVICE_PROTOCOL + wikiHost + ENDPOINT_SUFFIX);
        try {
            service = serviceLocator.getConfluenceserviceV1();
            token = service.login(wikiUserName, wikiPassword);

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

    public void setOwnerForLabel(String label) throws Exception {

        if (false == ZfinProperties.isPushToWiki()) {
            return;
        }

        try {
            RemoteSearchResult[] pages = service.getLabelContentByName(token, label);
            logger.debug("pages to process: "+ pages.length + " for label: "+label);

            // for each page:
            // if there is is editing or view restriction, then add an editing one to the creator
            for (RemoteSearchResult page : pages) {
                logger.debug("processing page[" + page.getTitle() + "] type[" + page.getType() + "]") ;
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
                    permissions[1] = permissionForZfinGroup ;

                    service.setContentPermissions(token, page.getId(), Permission.EDIT.getValue(), permissions);
                }
            }  // end of for loop
        } catch (Exception e) {
            e.fillInStackTrace();
            logger.error("Unable to set owner for label[" + label + "]", e);
            throw e;
        }

    }

    /**
     * Code to clean the sandbox space.
     * Removes all pages individually and then creates a new home page.
     * Was previously dropping the entire space, but makes the space look like it is new, not cleaned.
     * see fogbugz 5179 for more details.
     * @throws Exception Thrown if problems during the process.
     */
    public void cleanSandbox() throws Exception{

        if (false == ZfinProperties.isPushToWiki()) {
            return;
        }

        RemotePageSummary[] pages = service.getPages(token,Space.SANDBOX.getValue()) ;
        if(pages!=null && pages.length>0){
            for(RemotePageSummary page: pages){
                logger.debug("removing page: "+ page.getTitle());
                service.removePage(token,page.getId()) ;
            }
        }

        RemotePage homePage = new RemotePage() ;
        homePage.setContent("This is the home of the SandBox space.\n\n" +
                "To help you on your way, we've inserted some of our favourite macros on this home page. As you start creating pages, adding news items and commenting you'll see the macros below fill up with all the activity in your space.\n\n" +
                "{section}\n" +
                "{column:width=60%}\n" +
                "{recently-updated}\n\n\n" +
                "{column}\n\n" +
                "{column:width=5%}\n" +
                "{column}\n\n" +
                "{column:width=35%}\n" +
                "Navigate space\n" +
                "{pagetreesearch}\n\n" +
                "{pagetree}\n\n" +
                "{column}\n\n" +
                "{section}");
        homePage.setCreated(GregorianCalendar.getInstance());
        homePage.setCreator(wikiUserName);
        homePage.setHomePage(true);
        homePage.setCurrent(true);
        homePage.setSpace(Space.SANDBOX.getValue());
        homePage.setTitle("Home");
        service.storePage(token,homePage) ;

    }

    public RemoteSearchResult[] getLabelContent(String label) throws Exception{
        return service.getLabelContentByName(token,label) ;
    }

    public RemoteContentPermission[] getRemoteContentPermissions(long id,String type) throws Exception{
        RemoteContentPermissionSet remoteContentPermissionSet = service.getContentPermissionSet(token,id,type) ;
        RemoteContentPermission[] remoteContentPermissions = remoteContentPermissionSet.getContentPermissions() ;
        return remoteContentPermissions ;
    }

}
