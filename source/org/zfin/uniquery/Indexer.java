package org.zfin.uniquery;

import cvu.html.HTMLTokenizer;
import cvu.html.TagToken;
import cvu.html.TextToken;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniquery.categories.SiteSearchCategories;
import org.zfin.uniquery.presentation.SearchBean;
import org.zfin.util.DateUtil;
import org.zfin.util.FileUtil;
import org.zfin.wiki.WikiLoginException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.apache.commons.cli.OptionBuilder.withArgName;


/**
 * This class is an adaptation of the Spider class from the spindle library (www.bitmechanic.com)
 * The original author was James Cooper (pixel@bitmechanic.com) with modifications
 * made by Shad Stafford (staffors@cs.uoregon.edu)
 * <p/>
 * Originally called "Spider" we have changed the functionality of this code fundamentally to
 * avoid crawling data pages.  Too many redundant and unnecessary HTTP calls were being made
 * only to discover a page was already indexed.  As a result, "Spider" began to take 50+ hours
 * to Index ZFIN, and growing (starting from 18 hours initially two years ago).
 * <p/>
 * "Indexer" was developed with a new paradigm in mind:  use the database!  Crawling to discover
 * data pages is not necessary since we can determine all of the data pages using a (albeit complex)
 * SQL query.  This saves an enormous amount of wasted time spent crawling pages unnecessarily,
 * cross-referencing them against discovered or indexed pages, and so forth.
 * Indexer currently takes 15 hours to complete on 10 threads.
 * <p/>
 * Crawling is useful, however, for static pages so we allow crawling on non "webdriver" pages like
 * the Zebrafish Book, for example.
 * <p/>
 * Data page URLS are fed in via a command-line option "-q" (for quick), and this data is updated
 * regularly via the CRON process that generates the indexes (by running this program).
 * <p/>
 * Finally, this program uses a number of threads (passed as a command-line option "-t").
 * Tests have shown that:
 * 4 threads gives the most "bang for the buck."  At the time of that
 * testing, Embryonix only has 4 processors -- which probably has something to do with it.
 * 2-3 threads is adequate, takes about 20% more time to complete
 * 10 threads hogs system resources and saves only about 10% time over 4 threads
 * 1-thread barely affects system performance so can be used occasionally during working hours
 */
public class Indexer extends AbstractScriptWrapper implements Runnable {

    private static final String lineSep = System.getProperty("line.separator");
    private final Logger logger = Logger.getLogger(Indexer.class);

    private String indexDir;
    private String indexRootDir;
    private List<UrlLink> urlsToIndex = new ArrayList<UrlLink>();
    private List<String> include = new ArrayList<String>();
    private List<String> exclude = new ArrayList<String>();
    private List<String> crawlOnly = new ArrayList<String>();
    private List<Thread> threadList = new ArrayList<Thread>();
    private boolean incremental;
    private boolean createDetailPageList = true;

    private IndexWriter index;
    private Set<String> discoveredURLs = new TreeSet<String>();
    private Map<String, Boolean> mimeTypes = new HashMap<String, Boolean>();

    private int threads = 2;

    private int bytes;

    private long timeSpentLoading = 0;
    private long timeSpentParsing = 0;
    private long timeSpentIndexing = 0;
    private long timeSpentUrlProcessing = 0;
    private long timeSpentOptimizing = 0;
    private int filesIndexed = 0;

    // log each indexed file in log
    private boolean verbose = true;
    private String logDirectory = ".";
    private PrintWriter log = null;
    private PrintWriter indexedUrlLog = null;
    private PrintWriter crawledUrlLog = null;
    private PrintWriter ignoredUrlLog = null;
    private PrintWriter malformedUrlLog = null;
    private PrintWriter loadFileLog = null;

    private static int errorCount = 0;
    private static final String INDEXED_URLS_LOG = "/indexedUrls.log";
    private static final String CRAWLED_URLS_LOG = "/crawledUrls.log";
    private int numberOfDetailPages = 0;
    private CronJobReport cronJobReport;

    static {
        options.addOption(withArgName("indexerDir").hasArg().withDescription("site search directory").create("indexerDir"));
        options.addOption(withArgName("u").hasArg().withDescription("search urls").create("u"));
        options.addOption(withArgName("e").hasArg().withDescription("exclude urls").create("e"));
        options.addOption(withArgName("c").hasArg().withDescription("crawl urls").create("c"));
        options.addOption(withArgName("q").hasArg().withDescription("all view pages").create("t"));
        options.addOption(withArgName("t").hasArg().withDescription("number of threads").create("t"));
        options.addOption(withArgName("numberOfDetailPages").hasArg().withDescription("number of pages per entity").create("numberOfDetailPages"));
        options.addOption(withArgName("categoryDir").hasArg().withDescription("search urls").create("categoryDir"));
        options.addOption(withArgName("zfinPropertiesDir").hasArg().withDescription("zfin.properties path").create("zfinPropertiesDir"));
        options.addOption(withArgName("createDetailPageList").hasArg().withDescription("Create the list of view pages").create("createDetailPageList"));
    }


    /**
     * Main entry point.
     *
     * @param arguments arguments
     * @throws Exception exception
     */
    public static void main(String arguments[]) throws Exception {
        //DOMConfigurator.configure("server_apps/quicksearch/logs/log4j.xml");
        //initializeLog4j();
        CommandLine commandLine = parseArguments(arguments, "index ");
        //initializeLogger(commandLine.getOptionValue(log4jFileOption.getOpt()));
        Indexer indexer = new Indexer(commandLine);

        IntegratedJavaMailSender smtp = new IntegratedJavaMailSender();
        if (errorCount > 100) {
            smtp.sendMail("Error while indexing " + ZfinPropertiesEnum.DOMAIN_NAME.value(),
                    "Too many errors [" + errorCount + "]", ZfinProperties.getIndexerReportEmailAddresses());
            System.exit(-1);
        }
    }

    public Indexer(CommandLine commandLine) throws IOException {
        cronJobReport = new CronJobReport("Site Search Indexer: ");
        cronJobReport.start();
        parseCommandlineOptions(commandLine);
        try {
            initLogFiles();
            File configurationFile = new File(indexRootDir, "indexer.xml");
            FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext("file:" + configurationFile.getAbsolutePath());
            GenerateEntityDetailPageUrls generateUrls = (GenerateEntityDetailPageUrls) context.getBean("detailPageGenerator");
            generateUrls.setDetailEntityProperties(indexRootDir, propertiesFileName);
            generateUrls.setNumberOfRecordsPerEntities(numberOfDetailPages);
            if (createDetailPageList)
                generateUrls.generateAllUrls();
            List<String> staticIndex = new ArrayList<String>();
            loadFromFile(generateUrls.getDetailPageFilePath(), staticIndex);
            for (String viewPageLink : staticIndex) {
                UrlLink link = new UrlLink(viewPageLink, "viewPage");
                urlsToIndex.add(link);
            }
            go();
            // generate detail page url list
            CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.INDEXER_REPORT_EMAIL));
            cronJobUtil.emailReport("site-search-indexer-report.ftl", cronJobReport);

        } catch (Exception e) {
            e.printStackTrace(log);
            logger.error(e);
            cronJobReport.error(e.getMessage());
            IntegratedJavaMailSender smtp = new IntegratedJavaMailSender();
            smtp.sendMail("Error while indexing " + ZfinPropertiesEnum.DOMAIN_NAME.value(),
                    "Exception: " + e.getMessage(), ZfinProperties.getIndexerReportEmailAddresses());

        }
    }

    private void initLogFiles() throws IOException {
        log = new PrintWriter(new FileWriter(logDirectory + "/spider.log"));
        indexedUrlLog = new PrintWriter(new FileWriter(logDirectory + INDEXED_URLS_LOG));
        crawledUrlLog = new PrintWriter(new FileWriter(logDirectory + CRAWLED_URLS_LOG));
        ignoredUrlLog = new PrintWriter(new FileWriter(logDirectory + "/ignoredUrls.log"));
        malformedUrlLog = new PrintWriter(new FileWriter(logDirectory + "/malformedUrls.log"));
        loadFileLog = new PrintWriter(new FileWriter(logDirectory + "/loadFileLog.log"));
        mimeTypes.put("text/html", Boolean.TRUE);
        mimeTypes.put("text/plain", Boolean.TRUE);
        mimeTypes.put("text/html;charset=ISO-8859-1", Boolean.TRUE);
        mimeTypes.put("text/html;charset=UTF-8", Boolean.TRUE);
    }

    private void parseCommandlineOptions(CommandLine commandLine) throws IOException {
        indexRootDir = commandLine.getOptionValue("indexerDir");
        indexDir = FileUtil.createAbsolutePath(indexRootDir, "new_indexes");
        String createDetailPagesStr = commandLine.getOptionValue("createDetailPageList");
        if (createDetailPagesStr != null) {
            createDetailPageList = Boolean.valueOf(createDetailPagesStr);
        }
        for (String url : loadFromFile(commandLine.getOptionValue("u"), null)) {
            UrlLink link = new UrlLink(url, "given URL");
            urlsToIndex.add(link);
        }
        loadFromFile(commandLine.getOptionValue("e"), exclude);
        threads = Integer.parseInt(commandLine.getOptionValue("t"));
        try {
            numberOfDetailPages = Integer.parseInt(commandLine.getOptionValue("numberOfDetailPages"));
        } catch (NumberFormatException e) {
            // ignore as the variable is set to '0' by default.
        }
        initSiteSearchCategories(commandLine.getOptionValue("categoryDir"));
        propertiesFileName = commandLine.getOptionValue("zfinPropertiesDir");
        initAll(propertiesFileName);
        logDirectory = FileUtil.createAbsolutePath(indexRootDir, "logs");
    }

    /**
     * This method handles the threading.
     *
     * @throws Exception exception
     */
    public void go() throws Exception {
        // create the index directory -- or append to existing
        cronJobReport.start();
        reportMessage("Creating index in: " + indexDir);
        log.flush();
        if (incremental) {
            log.println("    - using incremental mode");
        }
        index = new IndexWriter(new File(indexDir), new ZfinAnalyzer(), !incremental);

        // generate the specified number of threads and begin indexing
        // from "run()" method below
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(this, "Thread-" + (i + 1));
            t.start();
            threadList.add(t);
        }
        while (threadList.size() > 0) {
            Thread child = threadList.remove(0);
            child.join();
        }
        long elapsed = System.currentTimeMillis() - start;
        // index the community wiki
        indexWiki();

        // after all threads have completed, close the index and write appropriate logs
        reportMessage("Indexed " + filesIndexed + " URLs (" + (bytes / 1024) + " KB) in " + DateUtil.getTimeDuration(0, elapsed));
        log.println("Optimizing index");

        long optimizingStart = System.currentTimeMillis();
        index.optimize();
        index.close();
        timeSpentOptimizing += (System.currentTimeMillis() - optimizingStart);

        reportMessage("Time spent loading: " + DateUtil.getTimeDuration(0, timeSpentLoading));
        reportMessage("Time spent parsing: " + DateUtil.getTimeDuration(0, timeSpentParsing));
        reportMessage("Time spent indexing: " + DateUtil.getTimeDuration(0, timeSpentIndexing));
        reportMessage("Time spent urlProcessing: " + DateUtil.getTimeDuration(0, timeSpentUrlProcessing));
        reportMessage("Time spent optimizing: " + DateUtil.getTimeDuration(0, timeSpentOptimizing));

        createStatistics();
        closeResources();
        if (errorCount > 100) {
            cronJobReport.error("Too many errors: " + errorCount);
        }
        cronJobReport.finish();
    }

    private void reportMessage(String message) {
        log.println(message);
        cronJobReport.addMessageToSection(message, "Main");
    }

    private void closeResources() {
        indexedUrlLog.close();
        crawledUrlLog.close();
        ignoredUrlLog.close();
        malformedUrlLog.close();
        log.close();
    }

    public void createStatistics() {
        String reportSubsectionTitle = "Statistics for indexed pages:";
        createStatistics(INDEXED_URLS_LOG, reportSubsectionTitle);
        reportSubsectionTitle = "Statistics for crawled pages:";
        createStatistics(CRAWLED_URLS_LOG, reportSubsectionTitle);
    }

    private void createStatistics(String fileName, String reportSubsectionTitle) {
        File indexedUrls;
        FileReader fileReader = null;
        LineNumberReader lineNumberReader = null;
        try {
            indexedUrls = new File(logDirectory, fileName);
            fileReader = new FileReader(indexedUrls);
            lineNumberReader = new LineNumberReader(fileReader);
            String url;
            IndexingStatistics stats = new IndexingStatistics();
            while ((url = lineNumberReader.readLine()) != null) {
                stats.addUrl(url);
            }
            for (Object o : stats.getStatisticsMap().keySet()) {
                String category = (String) o;
                String message = category + ": " + stats.getStatisticsMap().get(category);
                log.println(message);
                cronJobReport.addMessageToSection(message, reportSubsectionTitle);
            }
        } catch (IOException e) {
            log.write("Error: " + e.getMessage());
            cronJobReport.error(e);
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null)
                    fileReader.close();
                if (lineNumberReader != null)
                    lineNumberReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.write("Error: " + e.getMessage());
                cronJobReport.error(e);
            }
        }
    }

    /**
     * Include the community wiki in the indexer.
     * Note: This indexing is done in a single thread after
     * all other threads have finished. Once the community wiki gets big
     * we may have to allow for multiple threads to work on all urls.
     * However, neither page needs to be connected to as the contents is
     * already being retrieved via SOAP.
     */
    private void indexWiki() {
        WikiIndexer wikiIndexer = new WikiIndexer();
        List<WebPageSummary> urlSummaryList;
        try {
            urlSummaryList = wikiIndexer.getUrlSummary();
        } catch (WikiLoginException e) {
            logger.error("Failed to index the wiki", e);
            return;
        }

        int numberOfPages = 0;
        for (WebPageSummary summary : urlSummaryList) {
            if (numberOfDetailPages > 0) {
                if (numberOfPages++ > numberOfDetailPages)
                    break;
            }
            addToIndexer(summary);
            filesIndexed++;
            bytes += summary.getBody().length();
            if (verbose) {
                indexedUrlLog.println(summary.getUrlName());
                indexedUrlLog.flush();
            }
        }
    }


    /**
     * Method executed as part of the Runnable interface.
     */
    public void run() {
        try {
            // get the next URL in our list
            UrlLink link;
            while ((link = dequeueURL()) != null) {
                indexURL(link);
            }
        } catch (Exception e) {
            log.println(Thread.currentThread().getName() + ": aborting with error");
            e.printStackTrace(log);
        }
        if (verbose) log.println(Thread.currentThread().getName() + ": finished");
        threads--;
    }


    /**
     * dequeueURL
     * <p/>
     * We maintain a list of urls to index.  As we index and crawl, this list
     * dynamically grows and shrinks.  To avoid infinite loops, this means
     * we must also keep a list of "where we've been" to avoid retracing steps (discoveredURLs).
     * <p/>
     * This function retrieves and removes the next URL from our list left to index (urlsToIndex).
     *
     * @return URL
     * @throws InterruptedException exception from thread.wait()
     */
    public synchronized UrlLink dequeueURL() throws InterruptedException {
        while (true) {
            if (urlsToIndex.size() > 0) {
                return urlsToIndex.remove(0);
            } else {
                threads--;
                if (threads > 0) {
                    wait();
                    threads++;
                } else {
                    notifyAll();
                    return null;
                }
            }
        }
    }


    /**
     * In the process of indexing, we also discover new URLS on a page.
     * This is the aspect of "crawling" where, for new URLS, we add it to
     * our list of urls to index.
     * <p/>
     * We must be careful to not add a url we have previously added (discoveredURLs).
     * Also, once we add the new url, we must keep track of the fact that
     * we have done so so that we don't do it again.
     *
     * @param url      url to put into the queue
     * @param referrer referrer URL
     */
    public synchronized void enqueueURL(String url, String referrer) {
        if (!discoveredURLs.contains(url))  // careful not to add something we already saw
        {
            urlsToIndex.add(new UrlLink(url, referrer));  // adds the new url
            discoveredURLs.add(url);  // keeps track that we have now "seen" it so we don't do it again next time
            notifyAll();
        }
    }


    /**
     * The main workhorse of this code.  For a given URL, it loads the HTML page by doing a HTTP GET request.
     * Then it parses the HTML text, records any newly discovered URLs, and then strips off the HTML formatting tags
     * so that only the text body is left.
     * <p/>
     * Finally, it indexes that data for the given url.  That means, it uses the Lucene engine
     * to create some kind of ranking and proprietary database entry in the index files which
     * can later be searched using some Lucene APIs.
     *
     * @param link url
     */
    private void indexURL(UrlLink link) {
        // Note that we store various performance times for analysis.        
        long loadingStartTime = System.currentTimeMillis();
        WebPageSummary summary = null;

        try {
            // download and parse the HTML page from the given url
            summary = loadURL(link);
        } catch (MalformedURLException e) {
            errorCount++;
            log.println(Thread.currentThread().getName() + ": encountered error in malformed URL: " + link.getLinkUrl());
            e.printStackTrace(log);
            e.printStackTrace();
            log.flush();
            cronJobReport.error(e);
        } catch (Throwable e) {
            errorCount++;
            log.println(Thread.currentThread().getName() + ": encountered error while loading URL: " + link.getLinkUrl());
            e.printStackTrace(log);
            e.printStackTrace();
            log.flush();
            cronJobReport.error(e);
        }


        timeSpentLoading += (System.currentTimeMillis() - loadingStartTime);

        // begin work
        if (summary != null && summary.getBody() != null) {
            if (summary.getBody().startsWith("Dynamic Page Generation Error")) {
                try {
                    throw new Exception("Encountered dynamic page generation error for url: " + link.getLinkUrl());
                } catch (Exception e) {
                    errorCount++;
                    log.println(Thread.currentThread().getName() + ": encountered dynamic page generation error: " + link.getLinkUrl());
                    e.printStackTrace(log);
                    e.printStackTrace();
                    log.flush();
                    cronJobReport.error(e);
                }
            }

            long parsingStartTime = System.currentTimeMillis();
            /*
                *  We could have updated the summary while we load the url, however, this
                *  would have wasted time if the URL was a 404 error or other bad page.  So we wait until
                *  after we have checked for errors before updating the summary
                *
                */
            try {
                updateSummary(summary);
            } catch (IOException e) {
                errorCount++;
                log.println(Thread.currentThread().getName() + ": encountered error while updating summary: " + link.getLinkUrl());
                e.printStackTrace(log);
                e.printStackTrace();
                log.flush();
            }
            timeSpentParsing += (System.currentTimeMillis() - parsingStartTime);

            long indexingStartTime = System.currentTimeMillis();


            /*
                * determine whether to continue indexing this page based on the crawlonly
                * parameters set forth using command-line option configuration files --
                * "crawlonly" means to read and parse the page for new URLS, but don't index it
                */
            boolean indexThisPage = true;
            for (int x = 0; indexThisPage && x < crawlOnly.size(); x++) {
                String str = crawlOnly.get(x);
                indexThisPage = (!link.getLinkUrl().contains(str));
            }

            //Prepare the Lucene document for indexing.
            if (indexThisPage) {

                addToIndexer(summary);

                if (verbose) {
                    indexedUrlLog.println(link.getLinkUrl());
                    indexedUrlLog.flush();
                }
            } else {
                if (verbose) {
                    crawledUrlLog.println(link);
                    crawledUrlLog.flush();
                }
            }
            timeSpentIndexing += (System.currentTimeMillis() - indexingStartTime);

            /**
             * Now we parse the page we just crawled (and possibly indexed) and
             * process any URLS on that page.
             * This is a depth-first search "spidering" process.
             *
             * We do not index those files now, we simply decide whether to add them
             * to our list of files to index later depending on whether we've
             * already added it, or if it's on the include/exclude lists.
             */
            long urlProcessingStartTime = System.currentTimeMillis();
            for (int i = 0; i < summary.getUrls().length; i++) {
                // check against the include/exclude list
                boolean add = true;
                for (int x = 0; add && x < include.size(); x++) {
                    String inc = include.get(x);
                    add = (summary.getUrls()[i].contains(inc));
                }
                for (int x = 0; add && x < exclude.size(); x++) {
                    String ex = exclude.get(x);
                    add = (!summary.getUrls()[i].contains(ex));
                }

                if (add) {
                    enqueueURL(summary.getUrls()[i], link.getLinkUrl());
                } else {
                    if (verbose) {
                        ignoredUrlLog.println(summary.getUrls()[i]);
                        ignoredUrlLog.flush();
                    }
                }
            }
            timeSpentUrlProcessing += (System.currentTimeMillis() - urlProcessingStartTime);
        }
    }

    private void addToIndexer(WebPageSummary summary) {
        String url = summary.getUrl().toString();
        String uriName = summary.getUrlName();
        uriName = makeUrlGeneric(uriName);

        Document doc = new Document();
        if (uriName != null) {
            doc.add(new Field(SearchBean.URL, uriName, Field.Store.YES, Field.Index.TOKENIZED));

            if (summary.getText() != null && summary.getText().length() > 0) {
                doc.add(new Field(SearchBean.BODY, summary.getText(), Field.Store.YES, Field.Index.TOKENIZED));
            } else {
                doc.add(new Field(SearchBean.BODY, "", Field.Store.YES, Field.Index.TOKENIZED));
                log.println(Thread.currentThread().getName() + ": no body text for URL: " + summary.getUrlLink());
                cronJobReport.warning(Thread.currentThread().getName() + ": no body text for URL: " + summary.getUrlLink());
            }

            String title = summary.getAdjustedTitle();
            doc.add(new Field(SearchBean.TITLE, title, Field.Store.YES, Field.Index.TOKENIZED));
        }

        String docType = SiteSearchCategories.getDocType(url);
        doc.add(new Field(SearchBean.TYPE, docType, Field.Store.YES, Field.Index.TOKENIZED));

        synchronized (this) {
            bytes += summary.getBody().length();
            try {
                // index the document (the results of parsing URL)
                index.addDocument(doc);
                filesIndexed++;
            } catch (IOException e) {
                errorCount++;
                log.println(Thread.currentThread().getName() + ": encountered error while indexing URL: " + summary.getUrlLink());
                e.printStackTrace(log);
                e.printStackTrace();
                log.flush();
                cronJobReport.error(e);
            }
        }
    }

    public static String makeUrlGeneric(String uriName) {
        uriName = uriName.replace(ZfinPropertiesEnum.WEBDRIVER_LOC.value(), "WEBDRIVER_LOCATION");
        return uriName;
    }

    public static String makeUrlSpecific(String uriName) {
        uriName = uriName.replace("WEBDRIVER_LOCATION", ZfinPropertiesEnum.WEBDRIVER_LOC.value());
        return uriName;
    }

    /**
     * This procedure does two things:
     * (1) returns a list of all URLs in a web page summary
     * (2) updates the summary text (by removing markup tags) and title
     *
     * @param summary URL Summary
     * @throws IOException exception form HTMLTokenizer
     */
    private void updateSummary(WebPageSummary summary) throws IOException {
        boolean inScriptTag = false;
        StringBuilder strippedText = new StringBuilder();
        List<String> tmp_urls = new ArrayList<String>();
        summary.setBody(EntityPresentation.replaceSupTags(summary.getBody()));
        HTMLTokenizer ht = new HTMLTokenizer(new StringReader(summary.getBody()));
        for (Enumeration e = ht.getTokens(); e.hasMoreElements(); ) {
            Object obj = e.nextElement();
            if (obj instanceof TagToken) {
                TagToken tag = (TagToken) obj;
                String tagName = tag.getName();
                if (tagName != null) {
                    tagName = tagName.toLowerCase();
                }

                String new_url = null;
                if (("a").equals(tagName)) {
                    new_url = tag.getAttributes().get("href");
                } else if ("frame".equals(tagName)) {
                    new_url = tag.getAttributes().get("src");
                } else if ("title".equals(tagName) && e.hasMoreElements() && !tag.isEndTag()) {
                    // need to check all tokens until we hit a </title> tag
                    StringBuilder titleString = new StringBuilder();
                    while (e.hasMoreElements()) {
                        obj = e.nextElement();
                        if (obj instanceof TextToken) {
                            titleString.append(obj);
                        }
                        if (obj instanceof TagToken) {
                            TagToken internalTag = (TagToken) obj;
                            if (internalTag.getName().equals("title") && internalTag.isEndTag()) {
                                summary.setTitle(titleString.toString());
                                break;
                            }
                            if (internalTag.getName().equals("sup") && !internalTag.isEndTag()) {
                                titleString.append(" [");
                                obj = e.nextElement();
                                if (obj instanceof TextToken)
                                    titleString.append(((TextToken) obj).getText());
                            }
                            if (internalTag.getName().equals("sup") && internalTag.isEndTag()) {
                                titleString.append("]");
                            }
                        }
                    }

                } else if ("script".equals(tagName) && !tag.isEndTag()) {
                    inScriptTag = true;
                } else if ("script".equals(tagName) && tag.isEndTag()) {
                    inScriptTag = false;
                }


                if (new_url != null) {
                    // clean up special characters
                    new_url = StringUtils.replace(new_url, "\t", "");
                    new_url = StringUtils.replace(new_url, "\n", "");
                    new_url = StringUtils.replace(new_url, "\r", "");
                    new_url = StringUtils.replace(new_url, "&amp;", "&");

                    // remove the hostname (e.g., _quark) from cgi-bin_quark
                    if (new_url.contains("cgi-bin_")) {
                        String hostName = StringUtils.substringBetween(new_url, "cgi-bin_", "/");
                        int index = new_url.indexOf("cgi-bin_" + hostName);
                        // should assert that index != -1
                        String firstPart = new_url.substring(0, index + 7);
                        String secondPart = new_url.substring(index + 8 + hostName.length());
                        new_url = firstPart + secondPart;
                    }

                    if (new_url.startsWith("http://")) {
                        // verify we're on the same host and port
                        URL u = new URL(new_url);
                        if (u.getHost().equals(summary.getUrl().getHost()) && u.getPort() == summary.getUrl().getPort()) {
                            new_url = chopOffNamedAnchor(new_url);
                            tmp_urls.add(new_url);
                        }
                    } else if (!new_url.contains("://") && !new_url.startsWith("mailto:") && !new_url.startsWith("#") && !new_url.startsWith("javascript:")) {
                        // parse relative new_url
                        if (StringUtils.isNotEmpty(new_url)) {
                            new_url = formURL(summary.getUrl(), new_url);
                            new_url = chopOffNamedAnchor(new_url);
                            if (!tmp_urls.contains(new_url))
                                tmp_urls.add(new_url);
                        }
                    }
                }
            } else if ((obj instanceof TextToken) && !inScriptTag) {
                TextToken t = (TextToken) obj;
                String tokenText = t.getText();
                if (tokenText != null && tokenText.trim().length() > 0) {
                    strippedText.append(tokenText.trim()).append(" ");
                }
            }
        }

        summary.setText(stripSpecialCharacters(strippedText.toString()));
        summary.setUrls(new String[tmp_urls.size()]);
        tmp_urls.toArray(summary.getUrls());
    }

    public String stripSpecialCharacters(String text) {
        text = text.replaceAll("&nbsp;", "");
        return text;
    }


    private String chopOffNamedAnchor(String url) {
        int pos = url.indexOf("#");
        if (pos == -1)
            return url;
        else
            return url.substring(0, pos);
    }


    // converts relative URL to absolute URL

    private String formURL(URL origURL, String newURL) {
        StringBuilder base = new StringBuilder(origURL.getProtocol());
        base.append("://").append(origURL.getHost());
        if (origURL.getPort() != -1) {
            base.append(":").append(origURL.getPort());
        }


        // strip off single quotes because parser seems to leave them on
        if (newURL.startsWith("'")) {
            newURL = newURL.substring(1);
        }
        if (newURL.endsWith("'")) {
            newURL = newURL.substring(0, newURL.length() - 1);
        }

        if (newURL.startsWith("/")) {
            base.append(newURL);
        } else if (newURL.startsWith("..")) {
            origURL.getFile();
        } else {
            String file = origURL.getFile();
            int pos = file.lastIndexOf("/");
            if (pos != -1)
                file = file.substring(0, pos);

            while (newURL.startsWith("../")) {
                pos = file.lastIndexOf("/");
                file = file.substring(0, pos);
                newURL = newURL.substring(3);
            }

            base.append(file).append("/").append(newURL);
        }

        return base.toString();
    }


    /**
     * Given a URL, open the HTML page using an HTTP Get Request.  Then parse the
     * results and store various parts of the HTML page in a URLSummary object.
     *
     * @param link url
     * @return URLSummary
     * @throws java.io.IOException exception from opening a URL connection.
     */
    private WebPageSummary loadURL(UrlLink link) throws IOException {
        long loadFileStartTime = System.currentTimeMillis();
        URL u = new URL(link.getLinkUrl());
        WebPageSummary summary = null;
        HttpURLConnection uc;
        String ct;
        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setAllowUserInteraction(false);
            if (uc.getResponseCode() == 200) {
                ct = uc.getContentType();


                if (mimeTypes.get(ct) != null) {
                    summary = new WebPageSummary();
                    summary.setUrl(u);
                    summary.setUrlLink(link);

                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "ISO-8859-1"));

                    StringBuilder body = new StringBuilder(2048);
                    String line;

                    while ((line = in.readLine()) != null) {
                        body.append(line);
                        body.append(lineSep);
                    }
                    in.close();

                    summary.setBody(body.toString());
                } else {
                    errorCount++;
                    log.println("Unsupported MIME type (" + ct + ") type so ignoring: " + link);
                }
            } else {
                errorCount++;
                String message = "Unexpected response code: " + uc.getResponseCode() + " for URL: " + link;
                log.println(message);
                cronJobReport.warning(message);
            }
        } catch (FileNotFoundException e) {
            // 404
            errorCount++;
            log.println("No content found for URL: " + link);
        }

        long timeSpentLoading = (System.currentTimeMillis() - loadFileStartTime);
        loadFileLog.println(link.getLinkUrl() + "\t" + timeSpentLoading + " milliseconds");
        loadFileLog.flush();

        return summary;
    }


    private String propertiesFileName;

    private static void initializeLog4j() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("log4j.xml");
        String fileName = resource.getFile();
        DOMConfigurator.configure(fileName);
    }

    private void initSiteSearchCategories(String directory) {
        File categoryFile = new File(directory);
        SiteSearchCategories.init(categoryFile.getAbsolutePath(), "site-search-categories.xml");
        SiteSearchCategories.getAllSearchCategories();
    }

    /**
     * Opens a file and puts it into a List, line-by-line.
     * Ignores lines starting with a "#" (comments).
     *
     * @param filename file name
     * @param list     list into which all lines of the file are added into.
     * @return list of strings
     * @throws java.io.IOException exception
     */
    private List<String> loadFromFile(String filename, List<String> list) throws IOException {
        if (filename == null)
            return null;
        File file = new File(filename);
        if (!file.exists())
            file.createNewFile();
        BufferedReader in = new BufferedReader(new FileReader(filename));
        if (list == null)
            list = new ArrayList<String>();
        String currentLine;
        while ((currentLine = in.readLine()) != null) {
            if (currentLine.startsWith("#")) {
                // ignore comment lines
            } else if (currentLine.trim().length() > 0) {
                list.add(currentLine);
            }
        }
        return list;
    }

}
