package org.zfin.datatransfer.microarray;

import org.apache.log4j.Logger;
import org.zfin.datatransfer.service.DownloadService;

/**
 * To parse the GEO 2715 file.
 */
public class DefaultGeoSoftParser extends AbstractGeoSoftProcessor {

    private final Logger logger = Logger.getLogger(DefaultGeoSoftParser.class);

    public DefaultGeoSoftParser() {
        downloadService = new DownloadService();
    }

    public String parseLine(String line) {
        String[] strings = line.split("\t");
        if (strings.length >= getAccessionColumn()) {
            String accessionNumber = strings[getAccessionColumn() - 1]; // Genbank Accession
            // need to check to see if upper-case
            if (accessionNumber.toUpperCase().equals(accessionNumber) && accessionNumber.length() > 4) {
                return fixAccession(accessionNumber);
            }
        }
        return null;
    }


}
