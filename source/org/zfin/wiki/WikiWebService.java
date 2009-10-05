package org.zfin.wiki;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;

/**
 * Please see http://confluence.atlassian.com/display/DOC/Remote+API+Specification
 * for further docuementation of the service class.
 */
public class WikiWebService {

    protected String token = null;
    protected ConfluenceSoapService service = null;
    protected final String EDIT_PERMISSION = "Edit";
    protected final String VIEW_PERMISSION = "View";
    protected final String ENDPOINT_SUFFIX = "/rpc/soap-axis/confluenceservice-v1";
    protected final String WEBSERVICE_PROTOCOL = "https://";

    protected String wikiUserName = ZfinProperties.getWikiUserName();
    protected String wikiPassword = ZfinProperties.getWikiPassword();
    protected String wikiHost = ZfinProperties.getWikiHostname();
    protected String domainName = ZfinProperties.getDomain();

    private final Logger logger = Logger.getLogger(WikiWebService.class);


    private static WikiWebService instance = null;

    public static WikiWebService getInstance() throws WikiLoginException {
        if (instance == null) {
            instance = new WikiWebService();
            instance.login();
        }
        return instance;
    }

    public boolean login() throws WikiLoginException {
        try {
            if (StringUtils.isEmpty(wikiUserName)) {
                throw new WikiLoginException("userName undefined, not executing wiki service.  Please define WIKI_USER");
            }
            if (StringUtils.isEmpty(wikiPassword)) {
                throw new WikiLoginException("password undefined, not executing wiki service.  Please define WIKI_PASS");
            }
            if (StringUtils.isEmpty(wikiHost)) {
                throw new WikiLoginException("password undefined, not executing wiki service.  Please define WIKI_HOST");
            }
            if (StringUtils.isEmpty(domainName)) {
                throw new WikiLoginException("domain undefined, not executing wiki service.  Please define DOMAIN_NAME");
            }
            if (token != null) {
                try {
//                    return ( service.hasUser(token,ZfinProperties.getWikiUserName()) ) ;
                    if (service.hasUser(token, ZfinProperties.getWikiUserName())) {
                        return true;
                    } else {
                        throw new WikiLoginException("Failed to find webservice user.");
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

    public void setOwnerForLabel(String label) throws Exception{

        try {
            RemoteSearchResult[] pages = service.getLabelContentByName(token,label) ;

            // for each page:
            // if there is is editing or view restriction, then add an editing one to the creator
            for(RemoteSearchResult page: pages) {
                logger.debug("processing page: "+ page.getTitle());
                if(service.getContentPermissionSets(token,page.getId()).length==0){
                    RemoteContentPermission permission = new RemoteContentPermission();
                    RemotePage remotePage = service.getPage(token,page.getId()) ;
                    permission.setUserName(remotePage.getCreator())  ;
                    logger.info(" setting permission for: "+ page.getTitle()+" to "+remotePage.getCreator()) ;
                    permission.setType("Edit")  ;
                    RemoteContentPermission[] permissions = new RemoteContentPermission[1] ;
                    permissions[0] = permission ;
                    service.setContentPermissions(token,page.getId(),"Edit",permissions) ;
                }
            }  // end of for loop
        } catch (Exception e) {
            e.fillInStackTrace();
            logger.error("Unable to set owner for label["+label+"]",e);
            throw e ;
        }

    }
}
