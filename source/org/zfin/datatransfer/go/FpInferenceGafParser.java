package org.zfin.datatransfer.go;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
@Component
@Primary
public class FpInferenceGafParser {

    protected Logger logger = LogManager.getLogger(FpInferenceGafParser.class);

    public static final String GOREF_PREFIX = "GO_REF:";
    protected static final String ZFIN_CREATED_BY = "ZFIN";
    protected static final String GOC_CREATED_BY = "GOC";
    protected static final String ZEBRAFISH_TAXID = "taxon:7955";
    protected static Set<String> goRefExcludePubMap = new HashSet<>();
    public int countPipes=0;
    public int countCommas=0;
    public int countBoth=0;

    static {
        goRefExcludePubMap.add(GOREF_PREFIX + "0000002");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000003");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000004");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000015");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000037");
        goRefExcludePubMap.add(GOREF_PREFIX + "0000038");
    }

    public List<GafEntry> parseGafFile(File downloadedFile) throws Exception {
        List<GafEntry> gafEntries = new ArrayList<>();
        LineIterator it = FileUtils.lineIterator(downloadedFile);
        try {
            while (it.hasNext()) {
                String line = it.nextLine();

                if (line.startsWith("!") || StringUtils.isBlank(line)) {
                    continue;
                }
                GafEntry gafEntry = parseGafEntry(line);
                if (isValidGafEntry(gafEntry)) {
                    gafEntries.add(gafEntry);



                } else {
                    logger.debug("not a valid gaf entry, ignoring: " + gafEntry);
                }
            }
        } finally {
            it.close();
        }

        return gafEntries;
    }

    public void postProcessing(List<GafEntry> gafEntries) {

    }

    protected boolean isValidGafEntry(GafEntry gafEntry) {
        if (!isValidEvidenceCode(gafEntry.getEvidenceCode())) {
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
        if (!gafEntry.getTaxonId().equals(ZEBRAFISH_TAXID)) {
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
        return !(goEvidenceCodeEnum == GoEvidenceCodeEnum.ND
                || goEvidenceCodeEnum == GoEvidenceCodeEnum.NAS
                || goEvidenceCodeEnum == GoEvidenceCodeEnum.TAS);
    }


    protected GafEntry parseGafEntry(String line) {
        GafEntry gafEntry = new GafEntry();
        String[] entries = line.split("\\t", -1);

        gafEntry.setEntryId(entries[1]); // uniprot ID for GOA, ZDB-GENE for ZFIN
        gafEntry.setQualifier(entries[3]);
        gafEntry.setRelation("");
        gafEntry.setGoTermId(entries[4]);
        gafEntry.setPubmedId(entries[5]);
        if (entries[6] == null) {
            logger.error("bad gaf file: " + line);
        }
        gafEntry.setEvidenceCode(entries[6]);
        if(entries[7].contains("|")){
            countPipes++;
        }
        if(entries[7].contains(",")){
            countCommas++;
        }
        if((entries[7].contains(","))&&(entries[7].contains("|"))){
            countBoth++;
        }
        gafEntry.setInferences(entries[7]
                .replaceAll("EMBL:", "GenBank:")
                .replaceAll("protein_id:", "GenPept:")
        );

        gafEntry.setTaxonId(entries[12]);
        gafEntry.setCreatedDate(entries[13]);

        // for case 10868
        gafEntry.setCreatedBy(entries[14]
                .replaceAll("Ensembl:", "ENSEMBL:")
                .replaceAll("\\bUniProt:\\b", "UniProt:")

        );
        if (entries.length > 14) {
            gafEntry.setAnnotExtn(entries[15]);
        }
        gafEntry.setGeneProductFormID(entries[16]);
        gafEntry.setCol8pipes(countPipes);
        gafEntry.setCol8commas(countCommas);
        gafEntry.setCol8both(countBoth);
        return gafEntry;
    }

    public List<GafAnnotationGroup> parseAnnotationExtension(String line) {
        if (StringUtils.isEmpty(line))
            return null;

        List<String> groups = new ArrayList<>(Arrays.asList(line.split("\\|")));
        List<GafAnnotationGroup> annotationGroups = new ArrayList<>(groups.size());
        groups.forEach(group -> {
            GafAnnotationGroup annotationGroup = new GafAnnotationGroup();
            List<String> components = new ArrayList<>(Arrays.asList(group.split("\\,")));
            components.forEach(component -> {
                String pattern = "(?<" + GafAnnotationExtension.RELATIONSHIP_TERM +
                        ">.*)\\((?<" + GafAnnotationExtension.ENTITY_TERM + ">.*)\\)";
                Pattern componentPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = componentPattern.matcher(component.trim());
                if (matcher.matches()) {
                    GafAnnotationExtension extension = new GafAnnotationExtension(matcher.group(GafAnnotationExtension.RELATIONSHIP_TERM),
                            matcher.group(GafAnnotationExtension.ENTITY_TERM));
                    annotationGroup.addAnnotationExtendsion(extension);
                }
            });
            annotationGroups.add(annotationGroup);
        });
        return annotationGroups;
    }

}
