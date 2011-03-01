package org.zfin.datatransfer.go;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class GafParser {

    private Logger logger = Logger.getLogger(GafParser.class);

    public static final String GOREF_PREFIX = "GO_REF:";
    private static final String ZFIN_CREATED_BY = "ZFIN";
    private static final String GOC_CREATED_BY = "GOC";
    private static final String ZEBRAFISH_TAXID = "taxon:7955";
    private static Set<String> goRefExcludePubMap = new HashSet<String>();

    public GafParser() {
        goRefExcludePubMap.add(GOREF_PREFIX + "0000002");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000003");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000004");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000015");
    }

    public List<GafEntry> parseGafFile(File downloadedFile) throws Exception {
        List<GafEntry> gafEntries = new ArrayList<GafEntry>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(downloadedFile));
            String buffer;

            while ((buffer = bufferedReader.readLine()) != null) {
                if (!buffer.startsWith("!") && StringUtils.isNotEmpty(buffer)) {
                    GafEntry gafEntry = parseGafEntry(buffer);
                    if (isValidGafEntry(gafEntry)) {
                        gafEntries.add(gafEntry);
                    } else {
                        logger.debug("not a valid gaf entry, ignoring: " + gafEntry);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw e;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedReader = null;
                }
            } catch (Exception e) {
            }
        }

        return gafEntries;
    }

    protected boolean isValidGafEntry(GafEntry gafEntry) {
        if (false == isValidEvidenceCode(gafEntry.getEvidenceCode())) {
            logger.debug("invalid evidence code[" + gafEntry.getEvidenceCode() + " throwing out: " + gafEntry);
            return false; // just ignore
        }
        // will get a null-pointer if I don't have this in anyway
        if (StringUtils.isEmpty(gafEntry.getCreatedBy())) {
            logger.error("createdby field may not be empty throwing out: " + gafEntry);
            return false; // just ignore
        }
        if (gafEntry.getCreatedBy().equals(ZFIN_CREATED_BY)) {
            logger.debug("created by is zfin[" + gafEntry.getCreatedBy() + " throwing out: " + gafEntry);
            return false; // just ignore
        }
        if (gafEntry.getCreatedBy().equals(GOC_CREATED_BY)) {
            logger.debug("created by is the gene ontology consortium (ChrisFP) [" + gafEntry.getCreatedBy() + " throwing out and bringing in in other load: " + gafEntry);
            return false; // just ignore
        }
        if (false == gafEntry.getTaxonId().equals(ZEBRAFISH_TAXID)) {
            logger.debug("taxon id is not zebrafish [" + gafEntry.getTaxonId() + " throwing out: " + gafEntry);
            return false; // just ignore
        }
        if (goRefExcludePubMap.contains(gafEntry.getPubmedId())) {
            logger.debug("excluding ref [" + gafEntry.getTaxonId() + " throwing out: " + gafEntry);
            return false; // just ignore
        }
        return true;
    }

    private boolean isValidEvidenceCode(String evidenceCode) {
        GoEvidenceCodeEnum goEvidenceCodeEnum = GoEvidenceCodeEnum.getType(evidenceCode);
        if (goEvidenceCodeEnum == GoEvidenceCodeEnum.ND
                || goEvidenceCodeEnum == GoEvidenceCodeEnum.NAS
                || goEvidenceCodeEnum == GoEvidenceCodeEnum.TAS
                ) {
            return false;
        }
        return true;
    }


    protected GafEntry parseGafEntry(String line) {
        GafEntry gafEntry = new GafEntry();
        String[] entries = line.split("\t");
        gafEntry.setUniprotId(entries[1]);
        gafEntry.setQualifier(entries[3]);
        gafEntry.setGoTermId(entries[4]);
        gafEntry.setPubmedId(entries[5]);
        gafEntry.setEvidenceCode(entries[6]);
        gafEntry.setInferences(entries[7]);
        gafEntry.setTaxonId(entries[12]);
        gafEntry.setCreatedDate(entries[13]);
        gafEntry.setCreatedBy(entries[14]);
        return gafEntry;
    }

}
