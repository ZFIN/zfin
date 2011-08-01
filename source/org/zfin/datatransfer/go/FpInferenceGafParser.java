package org.zfin.datatransfer.go;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 */
public class FpInferenceGafParser {

    private Logger logger = Logger.getLogger(FpInferenceGafParser.class);

    public static final String GOREF_PREFIX = "GO_REF:";
    protected static final String ZFIN_CREATED_BY = "ZFIN";
    protected static final String GOC_CREATED_BY = "GOC";
    protected static final String ZEBRAFISH_TAXID = "taxon:7955";
    protected static Set<String> goRefExcludePubMap = new HashSet<String>();

    public FpInferenceGafParser() {
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
        if (    goEvidenceCodeEnum == GoEvidenceCodeEnum.ND
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
        gafEntry.setEntryId(entries[1]); // uniprot ID for GOA, ZDB-GENE for ZFIN
        gafEntry.setQualifier(entries[3]);
        gafEntry.setGoTermId(entries[4]);
        gafEntry.setPubmedId(entries[5]);
        if(entries[6]==null){
            logger.error("bad gaf file: "+line);
        }
        gafEntry.setEvidenceCode(entries[6]);
        gafEntry.setInferences(entries[7]
                .replaceAll("EMBL:", "GenBank:")
                .replaceAll("protein_id:", "GenPept:")
        );
        gafEntry.setTaxonId(entries[12]);
        gafEntry.setCreatedDate(entries[13]);
        gafEntry.setCreatedBy(entries[14]);
        return gafEntry;
    }

}
