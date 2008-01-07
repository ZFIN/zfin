package org.zfin.uniquery.index;

import org.zfin.uniquery.ZfinAnalyzer;
import org.zfin.uniquery.SearchCategory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.commons.lang.StringUtils;
import cvu.html.HTMLTokenizer;
import cvu.html.TagToken;
import cvu.html.TextToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.TreeSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;


/**
 * Indexer
 * 
 * This class is an adaptation of the Spider class from the spindle library (www.bitmechanic.com)
 * The original author was James Cooper (pixel@bitmechanic.com) with modifications
 * made by Shad Stafford (staffors@cs.uoregon.edu)
 *
 * Originally called "Spider" we have changed the functionality of this code fundamentally to
 * avoid crawling data pages.  Too many redundant and unnecessary HTTP calls were being made
 * only to discover a page was already indexed.  As a result, "Spider" began to take 50+ hours
 * to Index ZFIN, and growing (starting from 18 hours initially two years ago).
 *
 * "Indexer" was developed with a new paradigm in mind:  use the database!  Crawling to discover
 * data pages is not necessary since we can determine all of the data pages using a (albeit complex) 
 * SQL query.  This saves an enormous amount of wasted time spent crawling pages unnecessarily, 
 * cross-referencing them against discovered or indexed pages, and so forth.  
 * Indexer currently takes 15 hours to complete on 10 threads.
 *
 * Crawling is useful, however, for static pages so we allow crawling on non "webdriver" pages like
 * the Zebrafish Book, for example.
 *
 * Data page URLS are fed in via a command-line option "-q" (for quick), and this data is updated
 * regularly via the CRON process that generates the indexes (by running this program).
 * 
 * Finally, this program uses a number of threads (passed as a command-line option "-t").  
 * Tests have shown that:
 *   4 threads gives the most "bang for the buck."  At the time of that
 *     testing, Embryonix only has 4 processors -- which probably has something to do with it.
 *   2-3 threads is adequate, takes about 20% more time to complete
 *   10 threads hogs system resources and saves only about 10% time over 4 threads
 *   1-thread barely affects system performance so can be used occasionally during working hours
 */
public class Indexer implements Runnable
    {
    private static final String lineSep = System.getProperty("line.separator");
    
    //private long memoryFree = Runtime.getRuntime().freeMemory();
    //private long memoryTotal = Runtime.getRuntime().totalMemory();
    //private long memoryUsed = memoryTotal - memoryFree;

    private String indexDir;
    private ArrayList URLsToIndex;
    private ArrayList staticIndex;
    private ArrayList include;
    private ArrayList exclude;
    private ArrayList crawlOnly;
    private ArrayList threadList;
    private boolean incremental;

    private boolean groksHTTPS;

    private IndexWriter index;
    private TreeSet discoveredURLs;
    private HashMap mimeTypes;

    private int threads;

    private int bytes;
    
    private long timeSpentLoading = 0;
    private long timeSpentParsing = 0;
    private long timeSpentIndexing = 0;
    private long timeSpentUrlProcessing = 0;
    private long timeSpentOptimizing = 0;
    private int filesIndexed = 0;
    
    private boolean verbose = false;
    private String logDirectory = ".";
    //private PrintWriter memUsage = null;
    private PrintWriter log = null;
    private PrintWriter indexedUrlLog = null;
    private PrintWriter crawledUrlLog = null;
    private PrintWriter ignoredUrlLog = null;
    private PrintWriter malformedUrlLog = null;
    private PrintWriter loadFileLog = null;
    
    private static int errorCount = 0;


    /**
     *  MAIN
     */
    public static void main(String argv[]) throws Exception
        {
        Indexer s = new Indexer(argv);
        s.go();
        if (errorCount > 100)
            {
            System.exit(-1);
            }
        else
            {
            System.exit(0);
            }
        }



    /**
     *  Indexer
     *
     *  Initialize variables and open log writer streams.
     */
    public Indexer(String argv[])
        {
        try
            {
            groksHTTPS = true;
            incremental = false;
            threads = 2;
            bytes = 0;
            include = new ArrayList();
            exclude = new ArrayList();
            crawlOnly = new ArrayList();
            URLsToIndex = new ArrayList();
            staticIndex = new ArrayList();
            threadList = new ArrayList();
            discoveredURLs = new TreeSet();
            mimeTypes = new HashMap();
            parseArgs(argv);
            
            /* 
             * The "staticIndex" is what the main comments above refer to as 
             * the list of "data page URLs."  This has sped up performace dramatically
             * by reducing wasted time spent crawling data pages we can pre-determine
             * from the database.
             */
            URLsToIndex.addAll(staticIndex);
            
            //memUsage = new PrintWriter(new FileWriter(logDirectory + "/memusage.log"));
            log = new PrintWriter(new FileWriter(logDirectory + "/spider.log"));
            indexedUrlLog = new PrintWriter(new FileWriter(logDirectory + "/indexedUrls.log"));
            crawledUrlLog = new PrintWriter(new FileWriter(logDirectory + "/crawledUrls.log"));
            ignoredUrlLog = new PrintWriter(new FileWriter(logDirectory + "/ignoredUrls.log"));
            malformedUrlLog = new PrintWriter(new FileWriter(logDirectory + "/malformedUrls.log"));
            loadFileLog = new PrintWriter(new FileWriter(logDirectory + "/loadFileLog.log"));
            }
        catch (Exception e)
            {
            e.printStackTrace(log);
            }
        }




    /**
     *  go
     *
     *  This is the main method for threading processes.
     */
    public void go() throws Exception
        {
        // create the index directory -- or append to existing
        //log.println("Starting indexer at: " + Calendar.getInstance().getTime());
        log.println("Creating index in: " + indexDir);
        log.flush();
        if (incremental)
            {
            log.println("    - using incremental mode");
            }
        index = new IndexWriter(new File(indexDir), new ZfinAnalyzer(), !incremental);

        // check if we can do https URLs.
        // to date, this does not actually work, HTTPS calls require appropriate
        // certificates and handling which we are not doing here; more work is needed
        // for this to work
        try
            {
            System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            new URL("https://www.bitmechanic.com/");
            }
        catch (Exception e)
            {
            groksHTTPS = false;
            log.println("Disabling support for https URLs");
            }


        // generate the specified number of threads and begin indexing
        // from "run()" method below
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++)
            {
            Thread t = new Thread(this, "Thread-" + (i + 1));
            t.start();
            threadList.add(t);
            }
        while (threadList.size() > 0)
            {
            Thread child = (Thread) threadList.remove(0);
            child.join();
            }
        long elapsed = System.currentTimeMillis() - start;


        // after all threads have completed, close the index and write appropriate logs
        log.println("Indexed " + filesIndexed + " URLs (" + (bytes / 1024) + " KB) in " + (elapsed / 1000) + " seconds");
        log.println("Optimizing index");

        long optimizingStart = System.currentTimeMillis();
        index.optimize();
        index.close();
        timeSpentOptimizing += (System.currentTimeMillis() - optimizingStart);
        
        log.println("Time spent loading: " + timeSpentLoading/1000 + " seconds");
        log.println("Time spent parsing: " + timeSpentParsing/1000 + " seconds");
        log.println("Time spent indexing: " + timeSpentIndexing/1000 + " seconds");
        log.println("Time spent urlProcessing: " + timeSpentUrlProcessing/1000 + " seconds");
        log.println("Time spent optimizing: " + timeSpentOptimizing/1000 + " seconds");
        
        log.close();
        indexedUrlLog.close();
        crawledUrlLog.close();
        ignoredUrlLog.close();
        malformedUrlLog.close();
        
        //exit
        }



    /**
     *  run
     *
     *  The threading environment equivalent of MAIN.
     */
    public void run()
        {
        String url;
        try
            {
                
            // get the next URL in our list
            while ((url = dequeueURL()) != null)
                {
            	// Test Indexer on https for hoover security testing
            	//url = StringUtils.replaceOnce(url,"http://quark.zfin.org","https://hoover.zfin.org");
            	
            	// index the url
                indexURL(url);
                
                
//                String memoryStats = getMemoryUsage();
//                if (discoveredURLs.size()%10 == 0) {
//                    System.out.println(memoryStats);
//                    memUsage.println(memoryStats);
//                    memUsage.flush();
//                }
                }
            }
        catch (Exception e)
            {
            log.println(Thread.currentThread().getName() + ": aborting with error");
            e.printStackTrace(log);
            }
        if (verbose) log.println(Thread.currentThread().getName() + ": finished");
        threads--;
        }



    /**
     *  dequeueURL
     *
     *  We maintain a list of urls to index.  As we index and crawl, this list
     *  dynamically grows and shrinks.  To avoid infinite loops, this means
     *  we must also keep a list of "where we've been" to avoid retracing steps (discoveredURLs).
     *
     *  This function retrieves and removes the next URL from our list left to index (URLsToIndex).
     */
    public synchronized String dequeueURL() throws Exception
        {
        while (true)
            {
            if (URLsToIndex.size() > 0)
            	{
                return (String) URLsToIndex.remove(0);
                }
            else
                {
                threads--;
                if (threads > 0)
                    {
                    wait();
                    threads++;
                    }
                else
                    {
                    notifyAll();
                    return null;
                    }
                }
            }
        }



    /**
     *  enqueueURL
     *
     *  In the process of indexing, we also discover new URLS on a page.
     *  This is the aspect of "crawling" where, for new URLS, we add it to
     *  our list of urls to index.
     *
     *  We must becareful to not add a url we have previously added (discoveredURLs).
     *  Also, once we add the new url, we must keep track of the fact that
     *  we have done so so that we don't do it again.
     */
    public synchronized void enqueueURL(String url)
        {
        if (!discoveredURLs.contains(url))  // careful not to add something we already saw
            {
            URLsToIndex.add(url);  // adds the new url
            discoveredURLs.add(url);  // keeps track that we have now "seen" it so we don't do it again next time
            notifyAll();
            }
        }


    /* no longer needed, memory issue being debugged was solved by increasing JVM allocated memory
     *
     * This was done by adding the command-line option:  -XX:NewSize=128m -XX:MaxNewSize=128m -XX:SurvivorRatio=8 -Xms256M -Xmx256M
     * when we run the java machine
     *
    private String getMemoryUsage () {
        memoryFree = Runtime.getRuntime().freeMemory();
        memoryTotal = Runtime.getRuntime().totalMemory();
        memoryUsed = memoryTotal - memoryFree;
        String memUsage = memoryUsed + " (used) \t " + memoryFree + " (free) \t " + discoveredURLs.size() + " (discovered) \t " + URLsToIndex.size() + " (to do) ";
        return memUsage;
    }
    */

    
    /**
     *  indexURL
     *  
     *  The main workhorse of this code.  For a given URL, it loads the HTML page by doing a HTTP GET request.
     *  Then it parses the HTML text, records any newly discovered URLs, and then strips off the HTML formatting tags
     *  so that only the text body is left.
     *
     *  Finally, it indexes that data for the given url.  That means, it uses the Lucene engine
     *  to create some kind of ranking and proprietary database entry in the index files which
     *  can later be searched using some Lucene APIs.
     *
     */
    private void indexURL(String url)
    {
        // Note that we store various performance times for analysis.        
    	long loadingStartTime = System.currentTimeMillis();
    	URLSummary summary = null;
    	
    	try 
    	{
    	    /*
    	     * download and parse the HTML page from the given url
    	     */
    		summary = loadURL(url);  
    	} 
    	catch (IOException e) 
    	{
    		errorCount++;
    		log.println(Thread.currentThread().getName() + ": encountered error while loading URL: " + url);
    		e.printStackTrace(log);
    		e.printStackTrace();
    		log.flush();
    	}
    	
    	
    	timeSpentLoading += (System.currentTimeMillis() - loadingStartTime);
    	
    	// begin work
    	if (summary != null && summary.body != null)
    	{
    		if (summary.body.startsWith("Dynamic Page Generation Error"))
    		{
    			try 
    			{
    				throw new Exception("Encountered dynamic page generation error for url: " + url);
    			} 
    			catch (Exception e) 
    			{
    				errorCount++;
    				log.println(Thread.currentThread().getName() + ": encountered dynamic page generation error: " + url);
    				e.printStackTrace(log);
    				e.printStackTrace();
    				log.flush();
    			}
    		}
    		
    		long parsingStartTime = System.currentTimeMillis();
    		
    		
    		/*
    		 *  We could have updated the summary while we load the url, however, this
    		 *  would have wasted time if the URL was a 404 error or other bad page.  So we wait until
    		 *  after we have checked for errors before updating the summary
    		 *
    		 */
    		try 
    		{
    			updateSummary(summary);
    		} 
    		catch (IOException e) 
    		{
    			errorCount++;
    			log.println(Thread.currentThread().getName() + ": encountered error while updating summary: " + url);
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
    		for (int x = 0; indexThisPage && x < crawlOnly.size(); x++)
    		{
    			String str = (String) crawlOnly.get(x);
    			indexThisPage = (url.indexOf(str) == -1);
    		}
    		
    		
    		/*
    		 * Prepare the Lucene document for indexing.
    		 */
    		if (indexThisPage)
    		{
    			Document doc = new Document();
    			
    			URL u = null;
    			try {
    				u = new URL(url);
    			} catch (MalformedURLException e) {
    				errorCount++;
    				log.println(Thread.currentThread().getName() + ": encountered error in malformed URL: " + url);
    				e.printStackTrace(log);
    				e.printStackTrace();
    				log.flush();
    			}
    			
    			if (u != null) {
    				if (u.getFile() != null && u.getFile().length() > 0)
    				{
    					doc.add(Field.Text("url", u.getFile())); // store relative URLs
    				}
    				else
    				{
    					doc.add(Field.Text("url", u.toString()));
    				}
    				
    				if (summary.text != null && summary.text.length() > 0)
    				{
    					doc.add(Field.Text("body", summary.text));
    				}
    				else
    				{
    					doc.add(Field.Text("body", ""));
    					log.println(Thread.currentThread().getName() + ": no body text for URL: " + url);
    				}
    				
    				if (summary.title != null && summary.title.length() > 0)
    				{
    					String str = summary.title.trim();
    					if (str.startsWith("ZFIN View"))
    					{
    						str = str.substring(10).trim();
    					}
    					if (str.startsWith("ZFIN"))
    					{
    						str = str.substring(4).trim();
    					}
    					if (str.startsWith(":"))
    					{
    						str = str.substring(1);
    					}
    					doc.add(Field.Text("title", str));
    				}
    				else
    				{
    					doc.add(Field.Text("title", "Untitled"));
    				}
    			}
    			
    			String docType = getDocType(url, summary);
    			doc.add(Field.Text("type", docType));
    			
    			synchronized (this)
    			{
    				bytes += summary.body.length();
    				try 
    				{
    				    // index the document (the results of parsing URL)
    					index.addDocument(doc);
    					filesIndexed++;
    				} 
    				catch (IOException e) 
    				{
    					errorCount++;
    					log.println(Thread.currentThread().getName() + ": encountered error while indexing URL: " + url);
    					e.printStackTrace(log);
    					e.printStackTrace();
    					log.flush();
    				}
    			}
    			
    			if (verbose) { indexedUrlLog.println(url); indexedUrlLog.flush(); }
    		}
    		else
    		{
    			if (verbose) { crawledUrlLog.println(url); crawledUrlLog.flush(); }
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
    		ArrayList malformedUrls = new ArrayList();
    		for (int i = 0; i < summary.urls.length; i++)
    		{
    			// check against the include/exclude list
    			boolean add = true;
    			for (int x = 0; add && x < include.size(); x++)
    			{
    				String inc = (String) include.get(x);
    				add = (summary.urls[i].indexOf(inc) != -1);
    			}
    			for (int x = 0; add && x < exclude.size(); x++)
    			{
    				String ex = (String) exclude.get(x);
    				add = (summary.urls[i].indexOf(ex) == -1);
    			}
    			
    			if (add)
    			{                        
    				if (summary.urls[i].indexOf('\'') != -1)
    				{
    					// Why is this (single quote in URL) malformed?  I do not agree, commenting out. -- Paea
    					// malformedUrls.add(summary.urls[i]); 
    				}
    				else
    				{
    				    // enqueue MUST check if we already enqueued it!  That is the assumption here.
    				    // no duplicates should be enqueued.  Checking in both places would be inefficient.
    					enqueueURL(summary.urls[i]);  
    				}
    			}
    			else
    			{
    				if (verbose) { ignoredUrlLog.println(summary.urls[i]); ignoredUrlLog.flush(); }
    			}
    		}
    		if (malformedUrls.size() > 0)
    		{
    			malformedUrlLog.println(url);
    			malformedUrlLog.println("    contains the following malformed urls");
    			for (int i=0; i<malformedUrls.size(); i++)
    			{
    				errorCount++;
    				malformedUrlLog.println("    " + malformedUrls.get(i));
    			}
    			malformedUrlLog.println("\n\n");
    			malformedUrlLog.flush();
    		}
    		
    		timeSpentUrlProcessing += (System.currentTimeMillis() - urlProcessingStartTime);
    	}
    }
        

    /*
     *  getDocType
     *
     *  Based on the URL and data page naming conventions, 
     *  we assign the SearchCategory (or DocType) accordingly.
     */
    private String getDocType(String url, URLSummary summary)
        {
        if (url.indexOf("genotypeview.apg") != -1)
            {
            return SearchCategory.MUTANTS_GENOVIEW;
            }
        else if ((url.indexOf("markerview.apg") != -1 ) && (url.indexOf("ZDB-GENE") != -1))
            {
            return SearchCategory.GENES_MARKERVIEW;
            }
	else if (url.indexOf("markerview.apg") != -1 )
            {
            return SearchCategory.MARKERS_MARKERVIEW;
            }
        else if (url.indexOf("markergoview.apg") != -1)
            {
            return SearchCategory.GENES_MARKERGOVIEW;
            }
        else if (url.indexOf("sequence.apg") != -1)
            {
            return SearchCategory.GENES_SEQUENCE;
            }
		 else if (url.indexOf("geneprddescription.apg") != -1)
            {
            return SearchCategory.GENES_GENEPRDDESCRIPTION;
            }
        //else if (url.indexOf("mappingdetail.apg") != -1)
        //    {
        //    return SearchCategory.GENES_MAPPINGDETAIL;
        //    }
        else if (url.indexOf("fxfigureview.apg") != -1)
            {
            return SearchCategory.EXPRESSION_FXFIGVIEW;
            }
        else if (url.indexOf("anatomy/term-detail") != -1)
            {
            return SearchCategory.ANATOMY_ITEM;
            }
        else if (url.indexOf("zf_info/anatomy") != -1)
            {
            return SearchCategory.ANATOMY_ZFINFO;
            }
        //else if (url.indexOf("imageview.apg") != -1)
        //    {
        //    return SearchCategory.IMAGES;
	//   }
	// else if (url.indexOf("pubview2.apg") != -1)
        //    { 
        //    return SearchCategory.PUBLICATIONS;
	//     }
        else if (url.indexOf("persview.apg") != -1)
            {
            return SearchCategory.PEOPLE_PERSVIEW;
            }
        else if (url.indexOf("labview.apg") != -1)
            {
            return SearchCategory.PEOPLE_LABVIEW;
            }
        else if (url.indexOf("zf_info/zfbook/lab_desig.htm") != -1)
            {
            return SearchCategory.NOMENCLATURE_LAB;
            }
        else if (url.indexOf("zf_info/nomen.htm") != -1)
            {
            return SearchCategory.NOMENCLATURE_NOMEN;
            }
        else if (url.indexOf("zf_info/zfbook") != -1)
            {
            return SearchCategory.ZEBRAFISH_BOOK;
            }
        else if (url.indexOf("news/mtgs.htm") != -1)
            {
            return SearchCategory.MEETINGS;
            }
        else if (url.indexOf("news/jobs.htm") != -1)
            {
            return SearchCategory.JOBS;
            }
        else
            {
            return SearchCategory.OTHERS;
            }
        }
        
        
        
    /**
     * This procedure does two things: 
     *   (1) returns a list of all URLs in a webpage summary
     *   (2) updates the summary text (by removing markup tags) and title 
     * @param summary
     * @return
     * @throws IOException 
     * @throws Exception
     */
    private void updateSummary(URLSummary summary) throws IOException 
        {
        boolean inScriptTag = false;
        StringBuffer strippedText = new StringBuffer();
        ArrayList tmp_urls = new ArrayList();
        HTMLTokenizer ht = new HTMLTokenizer(new StringReader(summary.body));
        for (Enumeration e = ht.getTokens(); e.hasMoreElements();)
            {
            Object obj = e.nextElement();
            if (obj instanceof TagToken)
                {
                TagToken tag = (TagToken) obj;
                String tagName = tag.getName();
                if (tagName != null)
                    {
                    tagName = tagName.toLowerCase();
                    }
                    
                String new_url = null;
                if (("a").equals(tagName))
                    {
                    new_url = tag.getAttributes().get("href");
                    }
                else if ("frame".equals(tagName))
                    {
                    new_url = tag.getAttributes().get("src");
                    }
                else if ("title".equals(tagName) && e.hasMoreElements() && !tag.isEndTag())
                    {
                    obj = e.nextElement();
                    if (obj instanceof TextToken)
                        {
                        TextToken title = (TextToken) obj;
                        summary.title = title.getText();
                        }
                    }
                else if ("script".equals(tagName) && !tag.isEndTag())
                    {
                    inScriptTag = true;
                    }
                else if ("script".equals(tagName) && tag.isEndTag())
                    {
                    inScriptTag = false;
                    }
                    

                if (new_url != null)
                    {
                	// clean up special characters
                	new_url = StringUtils.replace(new_url,"\t","");
                	new_url = StringUtils.replace(new_url,"\n","");
                	new_url = StringUtils.replace(new_url,"\r","");
                	new_url = StringUtils.replace(new_url,"&amp;", "&");
                  
                    // remove the hostname (e.g., _quark) from cgi-bin_quark
                    if (new_url.indexOf("cgi-bin_") != -1)
	                    {
	                    String hostName = StringUtils.substringBetween(new_url, "cgi-bin_", "/");
	                    int index = new_url.indexOf("cgi-bin_" + hostName);
	                    // should assert that index != -1
	                    String firstPart = new_url.substring(0, index+7);
	                    String secondPart = new_url.substring(index+8+hostName.length());
	                    new_url = firstPart + secondPart;
	                    }                    

                    if (new_url.startsWith("http://") || (new_url.startsWith("https://") && groksHTTPS))
                        {
                        // verify we're on the same host and port
                        URL u = new URL(new_url);
                        if (u.getHost().equals(summary.url.getHost()) && u.getPort() == summary.url.getPort())
                            {
                            new_url = chopOffNamedAnchor(new_url);
                            tmp_urls.add(new_url);
                            }
                        }
                    else if (new_url.indexOf("://") == -1 && !new_url.startsWith("mailto:") && !new_url.startsWith("#") && !new_url.startsWith("javascript:"))
                        {
                        // parse relative new_url
                        new_url = formURL(summary.url, new_url);
                        new_url = chopOffNamedAnchor(new_url);
                        tmp_urls.add(new_url);
                        }
                    }
                }
            else if ((obj instanceof TextToken) && inScriptTag == false )
                {
                TextToken t = (TextToken) obj;
                String tokenText = t.getText();
                if (tokenText != null && tokenText.trim().length() > 0)
                    {
                    strippedText.append(tokenText.trim()).append(" ");
                    }
                }
            }

        summary.text = strippedText.toString();

        summary.urls = new String[tmp_urls.size()];
        tmp_urls.toArray(summary.urls);
        }



    // chopOffNamedAnchor
    private String chopOffNamedAnchor(String url)
        {
        int pos = url.indexOf("#");
        if (pos == -1)
            return url;
        else
            return url.substring(0, pos);
        }




    // converts relative URL to absolute URL
    private String formURL(URL origURL, String newURL)
        {
        StringBuffer base = new StringBuffer(origURL.getProtocol());
        base.append("://").append(origURL.getHost());
        if (origURL.getPort() != -1)
            {
            base.append(":").append(origURL.getPort());
            }


        // strip off single quotes because parser seems to leave them on
        if (newURL.startsWith("'"))
            {
            newURL = newURL.substring(1);
            }
        if (newURL.endsWith("'"))
            {
            newURL = newURL.substring(0, newURL.length() - 1);
            }
            
        if (newURL.startsWith("/"))
            {
            base.append(newURL);
            }
        else if (newURL.startsWith(".."))
            {
            origURL.getFile();
            }
        else
            {
            String file = origURL.getFile();
            int pos = file.lastIndexOf("/");
            if (pos != -1)
                file = file.substring(0, pos);

            while (newURL.startsWith("../"))
                {
                pos = file.lastIndexOf("/");
                file = file.substring(0, pos);
                newURL = newURL.substring(3);
                }

            base.append(file).append("/").append(newURL);
            }

        return base.toString();
        }




    /**
     *  loadURL
     *
     *  Given a URL, open the HTML page using an HTTP Get Request.  Then parse the
     *  results and store various parts of the HTML page in a URLSummary object.
     */
    private URLSummary loadURL(String url) throws IOException
        {
		long loadFileStartTime = System.currentTimeMillis();	
        URL u = new URL(url);
        URLSummary summary = null;
        HttpURLConnection uc;
        String ct = "";
        try
            {
            uc = (HttpURLConnection) u.openConnection();
            uc.setAllowUserInteraction(false);
            if (uc.getResponseCode() == 200)
                {
                ct = uc.getContentType();
        

                if (mimeTypes.get(ct) != null)
                    {
                    summary = new URLSummary();
                    summary.url = u;
        
                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "ISO-8859-1"));
        
                    StringBuffer body = new StringBuffer(2048);
                    String line;
        
                    while ((line = in.readLine()) != null)
                        {
                        body.append(line);
                        body.append(lineSep);
                        }
                    in.close();
        
                    summary.body = body.toString();
                    }
                else
                    {
                    errorCount++;
                    log.println("Unsupported MIME type (" + ct + ") type so ignoring: " + url);
                    }
                }
            else
                {
                errorCount++;
                log.println("Unexpected response code: " + uc.getResponseCode() + " for URL: " + url);
                }
            }
        catch (FileNotFoundException e)
            {
            // 404
            errorCount++;
            log.println("No content found for URL: " + url);
            }

		long timeSpentLoading = (System.currentTimeMillis() - loadFileStartTime);
        loadFileLog.println(url + "\t" + timeSpentLoading + " milliseconds");
        loadFileLog.flush();
        
        return summary;
        }



    /**
     *  parseArgs
     *
     *  Parse the command line arguments.
     */
    private void parseArgs(String argv[]) throws IOException
        {
        for (int i = 0; i < argv.length; i++)
            {
            if (argv[i].equals("-d"))
                {
                indexDir = argv[++i];
                }
            else if (argv[i].equals("-q")) // "q" stands for quick since static index is very quick
	            {
	            loadFromFile(argv[++i], staticIndex);
	            }
            else if (argv[i].equals("-u"))
                {
                loadFromFile(argv[++i], URLsToIndex);
                }
            else if (argv[i].equals("-i"))
                {
                loadFromFile(argv[++i], include);
                }
            else if (argv[i].equals("-e"))
                {
                loadFromFile(argv[++i], exclude);
                }
            else if (argv[i].equals("-c"))
                {
                loadFromFile(argv[++i], crawlOnly);
                }
            else if (argv[i].equals("-a"))
                {
                incremental = true;
                }
            else if (argv[i].equals("-m"))
                {
                mimeTypes.put(argv[++i], Boolean.TRUE);
                }
            else if (argv[i].equals("-t"))
                {
                threads = Integer.parseInt(argv[++i]);
                }
            else if (argv[i].equals("-l"))
                {
                StringBuffer buf = new StringBuffer(argv[++i]);
                if (buf.charAt(buf.length()-1) == '/')
                    {
                    buf.deleteCharAt(buf.length() - 1);
                    }
                logDirectory = buf.toString();
                }
            else if (argv[i].equals("-v"))
                {
                verbose = true;
                }
            else
                {
                log.println("Ignoring unknown argument: " + argv[i]);
                }
            }
            
        if (URLsToIndex.size() == 0)
            {
            throw new IllegalArgumentException("Missing required argument: -u [url file]");
            }
        if (indexDir == null)
            {
            throw new IllegalArgumentException("Missing required argument: -d [index dir]");
            }

        if (threads < 1)
            {
            throw new IllegalArgumentException("Invalid number of threads: " + threads);
            }

        if (mimeTypes.size() == 0)
            {
            // add default MIME types
            mimeTypes.put("text/html", Boolean.TRUE);
            mimeTypes.put("text/plain", Boolean.TRUE);
	    mimeTypes.put("text/html;charset=ISO-8859-1", Boolean.TRUE);
            }
        }
        
        
        
    /** 
     *  loadFromFile
     *
     *  Opens a file and puts it into an ArrayList, line-by-line.
     *  Ignores lines starting with a "#" (comments).
     */
    private void loadFromFile(String filename, ArrayList list) throws IOException
        {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String currentLine = null;
        while ((currentLine = in.readLine()) != null)
            {
            if (currentLine.startsWith("#"))
                {
                // ignore comment lines
                }
            else if (currentLine.trim().length() > 0)
                {
                list.add(currentLine);
                }
            }
        }

    /**
     *  URLSummary
     *
     *  A class that stores various aspects of an HTML page after it has been parsed.
     */
    public class URLSummary
	{
	URL url;
	String body = null;
	String text = null;
	String title = null;
	String[] urls = null;
	}
    
    }
