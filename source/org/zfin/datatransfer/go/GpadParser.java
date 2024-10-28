package org.zfin.datatransfer.go;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class GpadParser extends FpInferenceGafParser {

    @Getter
    @Setter
    private boolean validateInferences = false;

    @Override
    public List<GafEntry> parseGafFile(File downloadedFile) throws Exception {
        List<GafEntry> gafEntries = new ArrayList<>();
        Reader in = new FileReader(downloadedFile);
        Iterable<CSVRecord> records = CSVFormat.TDF
            .withHeader(Header.class)
            .parse(in);
        int lineNumber = 0;
        for (CSVRecord record : records) {
            lineNumber++;
            // ignore header info
            if (record.get(0).startsWith("!"))
                continue;
            GafEntry gafEntry = getGafEntry(lineNumber, record);
            gafEntries.add(gafEntry);
        }
        handleValidation(gafEntries);
        return gafEntries;
    }

    private void handleValidation(List<GafEntry> gafEntries) {
        if (validateInferences) {
            try {
                GafEntriesValidator.raiseExceptionForAnyInvalidIDs(gafEntries);
            } catch (GafEntryValidationException geve) {
                //log the error (should we throw the exception and stop the job?)
                System.err.println(geve.getMessage());
                logger.error(geve.getMessage());
                setErrorEncountered(true);
                setErrorMessage(geve.getMessage());
            }
        }
    }

    public GafEntry getGafEntry(int lineNumber, CSVRecord record) {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setCreatedBy(record.get(Header.ASSIGNED_BY));
        gafEntry.setEntryId(record.get(Header.ENTITY_ID));
        gafEntry.setGoTermId(record.get(Header.GO_TERM_ID));
        gafEntry.setQualifier(record.get(Header.QUALIFIER));
        gafEntry.setNot(record.get(Header.NOT));
        gafEntry.setPubmedId(record.get(Header.REFERENCE));
        gafEntry.setCreatedDate(record.get(Header.DATE_CREATED));
        gafEntry.setAnnotExtn(record.get(Header.ANNOTATION_EXTENSION));
        gafEntry.setAnnotationProperties(record.get(Header.ANNOTATION_PROPERTIES));
        gafEntry.setModelID(parseAnnotationProperties(record.get(Header.ANNOTATION_PROPERTIES)));
        String evidenceCode = record.get(Header.EVIDENCE);
        gafEntry.setEvidenceCode(evidenceCode);
        if (evidenceCode == null)
            logger.error("bad gaf file: empty evidence code in line: " + lineNumber);
        gafEntry.setInferences(record.get(Header.WITH_OR_FROM)
            .replaceAll("EMBL:", "GenBank:")
            .replaceAll("protein_id:", "GenPept:")
        );
        return gafEntry;
    }

    private String parseAnnotationProperties(String properties) {
        return Arrays.stream(properties.split("\\|"))
            .filter(s -> s.startsWith(AnnotationPropertiesHeader.NOCTUA_MODEL_ID.key))
            .map(s -> (s.split("="))[1])
            .findFirst().orElseGet(String::new);
    }

    // turn evidence EDO code into three-digit character
    public void postProcessing(List<GafEntry> gafEntries) {
        if (gafEntries == null)
            return;

        StringBuilder errorMessages = new StringBuilder();
        if (isErrorEncountered()) {
            errorMessages.append(getErrorMessage());
        }

        gafEntries.forEach(gafEntry -> {
            // replace ECO ID by GO Evidence Code (3-letter codes)
            GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID(gafEntry.getEvidenceCode());
            EcoGoEvidenceCodeMapping ecoCodeMap = RepositoryFactory.getOntologyRepository().getEcoEvidenceCode(term);
            if (ecoCodeMap == null) {
                String message = "invalid eco code: " + gafEntry.getEvidenceCode();
                logger.error(message);
                System.out.println(message);
                setErrorEncountered(true);
                errorMessages.append(message + "\n");
                setErrorMessage(errorMessages.toString());
            } else {
                String evCode = ecoCodeMap.getEvidenceCode();
                gafEntry.setEvidenceCode(evCode);
            }
        });
    }

    enum Qualifier {
        NOT("not"),
        COLOCALIZES("colocalizes with"),
        CONTRIBUTES("contributes to");

        private String name;

        Qualifier(String name) {
            this.name = name;
        }

        public static boolean exists(String name) {
            for (Qualifier qualifier : values()) {
                if (qualifier.name.equalsIgnoreCase(name))
                    return true;
            }
            return false;
        }
    }

    enum Header {
        ENTITY_ID(1),
        NOT(2),
        QUALIFIER(3),
        GO_TERM_ID(4),
        REFERENCE(5),
        EVIDENCE(6),
        WITH_OR_FROM(7),
        TAXON_ID(8),
        DATE_CREATED(9),
        ASSIGNED_BY(10),
        ANNOTATION_EXTENSION(11),
        ANNOTATION_PROPERTIES(12);

        // only used to make the column comparison with the official column spec easier to compare
        // http://www.geneontology.org/page/gene-product-association-data-gpad-format
        private final int column;

        Header(int column) {
            this.column = column;
        }

        public int getColumn() {
            return column;
        }
    }

    enum AnnotationPropertiesHeader {
        CONTRIBUTOR("contributor"),
        NOCTUA_MODEL_STATE("model-state"),
        NOCTUA_MODEL_ID("noctua-model-id");

        private final String key;

        AnnotationPropertiesHeader(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
