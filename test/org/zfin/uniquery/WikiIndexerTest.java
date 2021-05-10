package org.zfin.uniquery;

import org.zfin.properties.ZfinProperties;
import org.zfin.wiki.WikiLoginException;

import java.io.File;

/**
 * Indexing the Wiki site.
 */
public class WikiIndexerTest {

    public static void main(String[] arguments) throws WikiLoginException {

        File webInf = new File("home", "WEB-INF");
        File confDirectory = new File(webInf, "conf");
        File zfinProperties = new File(webInf, "zfin.properties");

        ZfinProperties.init(zfinProperties.getAbsolutePath());
        WikiIndexer indexer = new WikiIndexer();
        indexer.getUrlSummary();
    }

}