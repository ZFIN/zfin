/**
 * This class is an adaptation of the Spider class from the spindle library (www.bitmechanic.com)
 * The original author was James Cooper (pixel@bitmechanic.com) with modifications
 * made by Shad Stafford (staffors@cs.uoregon.edu)
 */
 
 
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
import java.util.HashSet;
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


public class Spider implements Runnable
    {
    private static final String lineSep = System.getProperty("line.separator");

    private String indexDir;
    private ArrayList urls;
    private ArrayList include;
    private ArrayList exclude;
    private ArrayList crawlOnly;
    private ArrayList threadList;
    private boolean incremental;

    private boolean groksHTTPS;

    private IndexWriter index;
    private HashSet indexedURLs;
    private HashMap mimeTypes;

    private int threads;

    private int bytes;
    
    private long timeSpentLoading = 0;
    private long timeSpentParsing = 0;
    private long timeSpentIndexing = 0;
    private long timeSpentUrlProcessing = 0;
    private long timeSpentOptimizing = 0;
    
    private boolean verbose = false;
    private String logDirectory = ".";
    private PrintWriter log = null;
    private PrintWriter indexedUrlLog = null;
    private PrintWriter crawledUrlLog = null;
    private PrintWriter ignoredUrlLog = null;
    private PrintWriter malformedUrlLog = null;
    
    private static int errorCount = 0;


    public static void main(String argv[]) throws Exception
        {
        Spider s = new Spider(argv);
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




    public Spider(String argv[])
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
            urls = new ArrayList();
            threadList = new ArrayList();
            indexedURLs = new HashSet();
            mimeTypes = new HashMap();
            parseArgs(argv);
            
            log = new PrintWriter(new FileWriter(logDirectory + "/spider.log"));
            indexedUrlLog = new PrintWriter(new FileWriter(logDirectory + "/indexedUrls.log"));
            crawledUrlLog = new PrintWriter(new FileWriter(logDirectory + "/crawledUrls.log"));
            ignoredUrlLog = new PrintWriter(new FileWriter(logDirectory + "/ignoredUrls.log"));
            malformedUrlLog = new PrintWriter(new FileWriter(logDirectory + "/malformedUrls.log"));
            }
        catch (Exception e)
            {
            e.printStackTrace(log);
            }
        }




    public void go() throws Exception
        {
        // create the index directory -- or append to existing
        log.println("Creating index in: " + indexDir);
        if (incremental)
            {
            log.println("    - using incremental mode");
            }
        index = new IndexWriter(new File(indexDir), new ZfinAnalyzer(), !incremental);

        // check if we can do https URLs
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

        // index each entry point URL
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

        // save the index
        log.println("Indexed " + indexedURLs.size() + " URLs (" + (bytes / 1024) + " KB) in " + (elapsed / 1000) + " seconds");
        log.println("Optimizing index");

        long optimizingStart = System.currentTimeMillis();
        index.optimize();
        index.close();
        timeSpentOptimizing += (System.currentTimeMillis() - optimizingStart);
        
        log.println("Time spent loading: " + timeSpentLoading/1000);
        log.println("Time spent parsing: " + timeSpentParsing/1000);
        log.println("Time spent indexing: " + timeSpentIndexing/1000);
        log.println("Time spent urlProcessing: " + timeSpentUrlProcessing/1000);
        log.println("Time spent optimizing: " + timeSpentOptimizing/1000);
        
        log.close();
        indexedUrlLog.close();
        crawledUrlLog.close();
        ignoredUrlLog.close();
        malformedUrlLog.close();
        }



    public void run()
        {
        String url;
        try
            {
            while ((url = dequeueURL()) != null)
                {
                indexURL(url);
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



    public synchronized String dequeueURL() throws Exception
        {
        while (true)
            {
            if (urls.size() > 0)
                {
                return (String) urls.remove(0);
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



    public synchronized void enqueueURL(String url)
        {
        if (url.indexOf("cgi-bin_") != -1)
            {
            String hostName = StringUtils.substringBetween(url, "cgi-bin_", "/");
            int index = url.indexOf("cgi-bin_" + hostName);
            // should assert that index != -1
            String firstPart = url.substring(0, index+7);
            String secondPart = url.substring(index+8+hostName.length());
            url = firstPart + secondPart;
            }

        if (!indexedURLs.contains(url))
            {
            urls.add(url);
            indexedURLs.add(url);
            notifyAll();
            }
        }



    private void indexURL(String url)
        {
        try
            {
            long loadingStartTime = System.currentTimeMillis();
            URLSummary summary = loadURL(url);
            timeSpentLoading += (System.currentTimeMillis() - loadingStartTime);

            if (summary != null && summary.body != null)
                {
                if (summary.body.startsWith("Dynamic Page Generation Error"))
                    {
                    throw new Exception("Encountered dynamic page generation error for url: " + url);
                    }
                long parsingStartTime = System.currentTimeMillis();
                String urls[] = parseURLs(summary);
                timeSpentParsing += (System.currentTimeMillis() - parsingStartTime);
                
                long indexingStartTime = System.currentTimeMillis();
                
                boolean indexThisPage = true;
                for (int x = 0; indexThisPage && x < crawlOnly.size(); x++)
                    {
                    String str = (String) crawlOnly.get(x);
                    indexThisPage = (url.indexOf(str) == -1);
                    }
                if (indexThisPage)
                    {
                    if (verbose) { indexedUrlLog.println(url); indexedUrlLog.flush(); }
                    Document doc = new Document();
                    
                    URL u = new URL(url);
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
                        
                    String docType = getDocType(url, summary);
                    doc.add(Field.Text("type", docType));
    
                    synchronized (this)
                        {
                        bytes += summary.body.length();
                        index.addDocument(doc);
                        }
                    }
                else
                    {
                    if (verbose) { crawledUrlLog.println(url); crawledUrlLog.flush(); }
                    }
                timeSpentIndexing += (System.currentTimeMillis() - indexingStartTime);
                
                long urlProcessingStartTime = System.currentTimeMillis();
                ArrayList malformedUrls = new ArrayList();
                for (int i = 0; i < urls.length; i++)
                    {
                    // check against the include/exclude list
                    boolean add = true;
                    for (int x = 0; add && x < include.size(); x++)
                        {
                        String inc = (String) include.get(x);
                        add = (urls[i].indexOf(inc) != -1);
                        }
                    for (int x = 0; add && x < exclude.size(); x++)
                        {
                        String ex = (String) exclude.get(x);
                        add = (urls[i].indexOf(ex) == -1);
                        }

                    if (add)
                        {
                        urls[i] = StringUtils.replace(urls[i], "\n", "");
                        urls[i] = StringUtils.replace(urls[i], "\r", "");
                        urls[i] = StringUtils.replace(urls[i], "\t", "");
                        urls[i] = StringUtils.replace(urls[i], " ", "");
                        urls[i] = StringUtils.replace(urls[i], "&amp;", "&");
                        
                        if (urls[i].indexOf('\'') != -1)
                            {
                            malformedUrls.add(urls[i]); 
                            }
                        enqueueURL(urls[i]);
                        }
                    else
                        {
                        if (verbose) { ignoredUrlLog.println(urls[i]); ignoredUrlLog.flush(); }
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
                    }
                    
                timeSpentUrlProcessing += (System.currentTimeMillis() - urlProcessingStartTime);
                }
            }
        catch (Throwable e)
            {
            errorCount++;
            log.println(Thread.currentThread().getName() + ": encountered error while parsing URL: " + url);
            e.printStackTrace(log);
            }
        }


        

    private String getDocType(String url, URLSummary summary)
        {
        if (url.indexOf("fishview.apg") != -1)
            {
            return SearchCategory.MUTANTS_FISHVIEW;
            }
        else if (url.indexOf("locusview.apg") != -1)
            {
            return SearchCategory.MUTANTS_LOCUSVIEW;
            }
        else if (url.indexOf("mappingdetail.apg") != -1 && (url.indexOf("FISH") != -1 || url.indexOf("LOCUS") != -1))
            {
            return SearchCategory.MUTANTS_MAPPINGDETAIL;
            }
        else if (url.indexOf("markerview.apg") != -1)
            {
            return SearchCategory.GENES_MARKERVIEW;
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
        else if (url.indexOf("mappingdetail.apg") != -1)
            {
            return SearchCategory.GENES_MAPPINGDETAIL;
            }
        else if (url.indexOf("xpatview.apg") != -1)
            {
            return SearchCategory.EXPRESSION_XPATVIEW;
            }
        else if (url.indexOf("xpatindexview.apg") != -1)
            {
            return SearchCategory.EXPRESSION_XPATINDEXVIEW;
            }
        else if (url.indexOf("anatomy_item.apg") != -1)
            {
            return SearchCategory.ANATOMY_ITEM;
            }
        else if (url.indexOf("zf_info/anatomy") != -1)
            {
            return SearchCategory.ANATOMY_ZFINFO;
            }
        else if (url.indexOf("pubview2.apg") != -1)
            {
            return SearchCategory.PUBLICATIONS;
            }
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
        
        
        
        
    private String[] parseURLs(URLSummary summary) throws Exception
        {
        boolean inScriptTag = false;
        StringBuffer strippedText = new StringBuffer();
        ArrayList urls = new ArrayList();
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
                    
                String url = null;
                if (("a").equals(tagName))
                    {
                    url = tag.getAttributes().get("href");
                    }
                else if ("frame".equals(tagName))
                    {
                    url = tag.getAttributes().get("src");
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
                    

                if (url != null)
                    {
                    if (url.startsWith("http://") || (url.startsWith("https://") && groksHTTPS))
                        {
                        // verify we're on the same host and port
                        URL u = new URL(url);
                        if (u.getHost().equals(summary.url.getHost()) && u.getPort() == summary.url.getPort())
                            {
                            url = chopOffNamedAnchor(url);
                            if (!indexedURLs.contains(url))
                                {
                                urls.add(url);
                                }
                            }
                        }
                    else if (url.indexOf("://") == -1 && !url.startsWith("mailto:") && !url.startsWith("#") && !url.startsWith("javascript:"))
                        {
                        // parse relative url
                        url = formURL(summary.url, url);
                        url = chopOffNamedAnchor(url);
                        if (!indexedURLs.contains(url))
                            {
                            urls.add(url);
                            }
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

        String list[] = new String[urls.size()];
        urls.toArray(list);
        return list;
        }




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




    private URLSummary loadURL(String url) throws Exception
        {
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
        
                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        
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
            
        return summary;
        }




    private void parseArgs(String argv[]) throws IOException
        {
        for (int i = 0; i < argv.length; i++)
            {
            if (argv[i].equals("-d"))
                {
                indexDir = argv[++i];
                }
            else if (argv[i].equals("-u"))
                {
                loadFromFile(argv[++i], urls);
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
            
        if (urls.size() == 0)
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
            }
        }
        
        
        
        
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


    }

class URLSummary
    {
    URL url;
    String body = null;
    String text = null;
    String title = null;
    }
