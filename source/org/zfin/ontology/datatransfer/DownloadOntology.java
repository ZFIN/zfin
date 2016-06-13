package org.zfin.ontology.datatransfer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.obo.dataadapter.OBOParseException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static org.zfin.ontology.datatransfer.OntologyCommandLineOptions.*;

/**
 * Main class to start the ontology loader. Need to pass in the name of the obo file.
 * Command line options:
 * -localOboFileName <obo file name when saved on the local file system>
 * -oboFileURL <URL from where the OBO file can be downloaded>
 * -log4jFilename <log4j.xml file>
 */
public class DownloadOntology extends AbstractScriptWrapper {

    private static final Logger LOG = RootLogger.getLogger(DownloadOntology.class);

    public static final Option oboFileName = OptionBuilder.withArgName("localOboFileName").hasArg().withDescription("the obo file name").create("localOboFileName");

    static {
        options.addOption(oboFileNameOption);
        options.addOption(log4jFileOption);
        options.addOption(oboFileURL);
    }

    private String path;
    private String downloadUrl;

    public DownloadOntology(String path, String downloadUrl) {
        this.path = path;
        this.downloadUrl = downloadUrl;
    }

    public static void main(String[] args) throws OBOParseException {
        //DOMConfigurator.configure("server_apps\\data_transfer\\LoadOntology\\log4j.xml");
        //DOMConfigurator.configure("log4j.xml");
        CommandLine commandLine = parseArguments(args, "load <>");
        String path = commandLine.getOptionValue(oboFileName.getOpt());
        String downloadUrl = commandLine.getOptionValue(oboFileURL.getOpt());
        LOG.info("Downloading obo file: " + downloadUrl);
        LOG.info("Loading obo file: " + path);

        DownloadOntology download = new DownloadOntology(path, downloadUrl);
        download.downloadOntology();
    }

    public void downloadOntology() {
        URLConnection uRLConnection;
        String urlString = downloadUrl;
        try {
            // loop needed to support redirects, i.e. if the url is being redirected to
            // another URL. by default, it does not do this for security purposes.
            while (true) {
                URL url = new URL(urlString);
                uRLConnection = url.openConnection();
                if (urlString.toLowerCase().startsWith("http")) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) uRLConnection;
                    switch (httpURLConnection.getResponseCode()) {
                        case HttpURLConnection.HTTP_MOVED_PERM:
                        case HttpURLConnection.HTTP_MOVED_TEMP:
                            urlString = httpURLConnection.getHeaderField("Location");
                            continue;
                    }
                }
                break;
            }
        } catch (IOException e) {
            LOG.error("Could not connect to URL: " + downloadUrl, e);
            return;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = uRLConnection.getInputStream();
            os = new FileOutputStream(path);
            byte[] temp = new byte[1024];
            int numOfBytesRead;
            while ((numOfBytesRead = is.read(temp)) > 0)
                os.write(temp, 0, numOfBytesRead);
        } catch (IOException e) {
            LOG.error("Error while reading the file", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                LOG.error("could not close resources");
            }
        }
    }

}
