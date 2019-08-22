package org.zfin.datatransfer.go;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.zfin.repository.RepositoryFactory;
import org.zfin.ontology.GenericTerm;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class GpadParser extends FpInferenceGafParser {

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
        return gafEntries;
    }

    public GafEntry getGafEntry(int lineNumber, CSVRecord record) throws IOException {
        GafEntry gafEntry = new GafEntry();
        gafEntry.setCreatedBy(record.get(Header.ASSIGNED_BY));
        gafEntry.setEntryId(record.get(Header.ENTITY_ID));
        gafEntry.setGoTermId(record.get(Header.GO_TERM_ID));
        gafEntry.setQualifier(record.get(Header.QUALIFIER));
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

    private String parseAnnotationProperties(String properties) throws IOException {
        Reader in = new StringReader(properties);

        CSVParser records = CSVFormat.RFC4180
                .withHeader(AnnotationPropertiesHeader.class)
                .withDelimiter('|')
                .parse(in);
        // awkward
        Iterator<CSVRecord> iterator = records.iterator();
        if (!iterator.hasNext())
            throw new RuntimeException("Should have only one line");
        CSVRecord record = iterator.next();
        String noctuaModel = record.get(AnnotationPropertiesHeader.NOCTUA_MODEL_ID);
        if (iterator.hasNext())
            throw new RuntimeException("Should have only one line");
        return getValueByKey(noctuaModel);
    }

    private String getValueByKey(String noctuaModel) {
        if (!noctuaModel.contains("="))
            return noctuaModel;
        return noctuaModel.substring(noctuaModel.indexOf("=") + 1);
    }

    // turn evidence EDO code into three-digit character
    public void postProcessing(List<GafEntry> gafEntries) {
        if (gafEntries == null)
            return;

        gafEntries.forEach(gafEntry -> {
            // replace ECO ID by GO Evidence Code (3-letter codes)
            GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID(gafEntry.getEvidenceCode());
            EcoGoEvidenceCodeMapping ecoCodeMap = RepositoryFactory.getOntologyRepository().getEcoEvidenceCode(term);
            String evCode = ecoCodeMap.getEvidenceCode();

          if (evCode == null) {
              logger.error("invalid eco code" + gafEntry.getEvidenceCode());
          }
          else {
              gafEntry.setEvidenceCode(evCode);
          }

            // fixed set of qualifiers
            // if thw incoming one is not one of them discard the value
            if (!Qualifier.exists(gafEntry.getQualifier()))
                gafEntry.setQualifier("");
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
        SOURCE(1),
        ENTITY_ID(2),
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
        private int column;

        Header(int column) {
            this.column = column;
        }

        public int getColumn() {
            return column;
        }
    }

    enum AnnotationPropertiesHeader {
        CONTRIBUTOR("contributor"),
        NOCTUA_MODEL_ID("noctua-model-id"),
        NOCTUA_MODEL_STATE("noctua-model-state");

        private String key;

        AnnotationPropertiesHeader(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
