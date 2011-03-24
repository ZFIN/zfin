package org.zfin.uniquery;

import java.io.File;

/**
 * Indexing the Wiki site.
 */
public class WikiIndexerTest {

    public static void main(String[] arguments) {

        File categoryFile = new File("home", "WEB-INF");
        File file = new File(categoryFile, "conf");

        String[] args = { "-u",
                "server_apps/quicksearch/etc/searchurls-test.txt", "-t", "1",
                "-categoryDir", file.getAbsolutePath(),
                "-e", "server_apps/quicksearch/etc/excludeurls.txt",
                "-indexerDir", "server_apps/quicksearch/",
                "-numberOfDetailPages", "1",
                "-zfinPropertiesDir", "home/WEB-INF/zfin.properties"};
        try {
            Indexer.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}