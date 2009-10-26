package org.zfin.uniquery;

import org.swift.confluence.cli.ConfluenceClient;
import org.zfin.uniquery.categories.SiteSearchCategories;

import java.io.File;

/**
 * Indexing the Wiki site.
 */
public class WikiIndexerTest extends ConfluenceClient {

    public static void main(String[] arguments) {

        File categoryFile = new File("home", "WEB-INF");
        File file = new File(categoryFile, "conf");

        String[] args = {"-d", "server_apps/quicksearch/indexes", "-u",
                "server_apps/quicksearch/etc/searchurls-test.txt", "-v", "-t", "1",
                "-l", "server_apps/quicksearch/logs",
                "-q", "server_apps/quicksearch/etc/allStaticPages.txt",
                "-categoryDir", file.getAbsolutePath(),
                "-e", "server_apps/quicksearch/etc/excludeurls.txt"};
        try {
            Indexer.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}