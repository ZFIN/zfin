package org.zfin.uniquery;

import com.dolby.atlassian.confluence.soap.model.confluence.*;
import org.apache.log4j.Logger;
import org.swift.confluence.cli.ConfluenceClient;
import org.zfin.uniquery.categories.SiteSearchCategories;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Indexing the Wiki site.
 */
public class WikiIndexer extends ConfluenceClient {

    private ConfluenceSoapService service;
    private static String wikiAddress = System.getenv("COMMUNITY_WIKI_URL");
    private List<String> wikiSpaces = new ArrayList<String>();
    private String user = "webservice";
    private String password = "dan1orer1o";
    public static final String WIKI_CATEGORY_ID = "WIKI";

    public WikiIndexer() {
        // obtain wiki spaces to be indexed
        List<SearchCategory> wikiCategories = SiteSearchCategories.getWikiCategories();
        if (wikiCategories == null)
            return;

        for (SearchCategory wikiCategory : wikiCategories) {
            if (wikiCategory != null) {
                List<UrlPattern> patterns = wikiCategory.getUrlPatterns();
                for (UrlPattern pattern : patterns) {
                    int indexOfSlash = pattern.getPattern().lastIndexOf("/");
                    String space = pattern.getPattern().substring(indexOfSlash + 1);
                    wikiSpaces.add(space);
                }
            }
        }
    }

    public List<WebPageSummary> getUrlSummary() {
        List<WebPageSummary> summaries = new ArrayList<WebPageSummary>();
        for (String wikispace : wikiSpaces) {
            doWork(getArguments(wikispace));
            RemotePageSummary[] pages = null;
            String message = "Error while retrieving all web pages of the wiki";
            try {
                pages = getAllPages();
            } catch (ClientException e) {
                LOG.error(message, e);
            } catch (InvalidSessionException e) {
                LOG.error(message, e);
            } catch (RemoteException e) {
                LOG.error(message, e);
            }
            if (pages == null)
                return null;

            WebPageSummary[] summaryList = new WebPageSummary[pages.length];
            int index = 0;
            for (RemotePageSummary remoteSummary : pages) {
                WebPageSummary summary = new WebPageSummary();
                String title = remoteSummary.getTitle();
                summary.setTitle(title);
                try {
                    String url = remoteSummary.getUrl();
                    summary.setUrl(new URL(url));
                    summary.setUrlName(url);
                } catch (MalformedURLException e) {
                    String errorMessage = "Invalid URL.";
                    LOG.error(errorMessage, e);
                }
                addPageContents(remoteSummary, summary, wikispace);
                summaryList[index++] = summary;
            }
            summaries.addAll(Arrays.asList(summaryList));
        }
        return summaries;
    }

    private void addPageContents(RemotePageSummary remoteSummary, WebPageSummary summary, String wikiSpace) {
        String message = "Error while retrieving contents for specific wiki page.";
        try {
            summary.setText(getPage(remoteSummary.getTitle(), wikiSpace).getContent());
            summary.setBody(getPage(remoteSummary.getTitle(), wikiSpace).getContent());
        } catch (ClientException e) {
            LOG.error(message, e);
        } catch (InvalidSessionException e) {
            LOG.error(message, e);
        }
    }

    private String[] getArguments(String wikiSpace) {
        List<String> arguments = new ArrayList<String>();
        arguments.add("--server");
        arguments.add(wikiAddress);
        arguments.add("--user");
        arguments.add(user);
        arguments.add("--password");
        arguments.add(password);
        arguments.add("--action");
        arguments.add("login");
        arguments.add("--space");
        arguments.add(wikiSpace);
        String[] args = new String[arguments.size()];
        arguments.toArray(args);
        return args;
    }

    protected void serviceLogin(String user, String password) throws ClientException, RemoteException {
        token = service.login(user, password);
        if (verbose) {
            out.println("Successful login to: " + address + " by user: " + user);
        }
    }

    /*
     * Get page list - list of pages in a space, perhaps with subsetting
     */
    public RemotePageSummary[] getAllPages() throws java.rmi.RemoteException, ClientException {

        String space = getRequiredString("space");
        RemotePageSummary list[] = null;

        if (jsapResult.userSpecified("title")) {
            String title = getString("title");
            long id = getContentId(title, space);
            if (jsapResult.userSpecified("ancestors")) {
                list = service.getAncestors(token, id);
            } else if (jsapResult.userSpecified("descendents")) {
                list = service.getDescendents(token, id);
            } else if (jsapResult.userSpecified("children")) {
                list = service.getChildren(token, id);
            }
        }
        if (list == null) { // list of pages in a space
            list = service.getPages(token, space);
        }

        return list;
    }


    protected String getClientName() {
        return "";
    }

    /**
     * Setup for remote service
     * - this must be overridden by subclass
     * - Example for Confluence:
     * ConfluenceSoapServiceServiceLocator serviceLocator = new ConfluenceSoapServiceServiceLocator();
     * serviceLocator.setConfluenceserviceV1EndpointAddress(address);
     * service = serviceLocator.getConfluenceserviceV1();
     */
    protected void serviceSetup(String address) throws ClientException, RemoteException {

        try {
            ConfluenceSoapServiceServiceLocator serviceLocator = new ConfluenceSoapServiceServiceLocator();
            serviceLocator.setConfluenceserviceV1EndpointAddress(address);
            // serviceLocator.setMaintainSession(true); // not sure this applies
            service = serviceLocator.getConfluenceserviceV1();
        } catch (ServiceException exception) {
            throw new RemoteServiceException(exception.getMessage());
        }
    }

    protected void serviceLogin() throws ClientException, RemoteException {
        String user = getString("user");
        String password = getString("password");
        token = service.login(user, password);
        if (verbose) {
            out.println("Successful login to: " + address + " by user: " + user);
        }
    }

    /*
     * Get page by name
     * - return page or throw exception
     */
    public RemotePage getPage(String pageTitle, String spaceKey) throws ClientException, InvalidSessionException {
        try {
            return service.getPage(token, spaceKey, pageTitle);
        }
        catch (InvalidSessionException exception) {
            if (verbose) {
                out.println("Session expired.");
            }
            throw exception;
        }
        catch (java.rmi.RemoteException exception) {
            String message = "Page '" + pageTitle + "' not found in space '" + spaceKey + "'";
            if (verbose) {
                exception.toString();
            }
            throw new ClientException(message);
        }
    }


    private static final Logger LOG = Logger.getLogger(WikiIndexer.class);
}
