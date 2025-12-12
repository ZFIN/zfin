package org.zfin.sequence.load;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;

@Getter
@Setter
public class LoadAction implements Comparable<LoadAction> {
    private Type type;
    private SubType subType;
    private String accession;
    private String geneZdbID;
    private String details;
    private int length;
    private Map<String, String> relatedEntityFields;

    private Set<LoadLink> links = new TreeSet<>();

    public LoadAction() {
        links = new TreeSet<>();
    }

    public LoadAction(Type type, SubType subType, String accession, String geneZdbID, String details, int length, Map<String, String> links) {
        this.type = type;
        this.subType = subType;
        this.accession = accession;
        this.geneZdbID = geneZdbID;
        this.details = details;
        this.length = length;
        this.relatedEntityFields = links;
    }

    public void addLink(LoadLink loadLink) {
        links.add(loadLink);
    }

    public void addLinks(Collection<LoadLink> links) {
        this.links.addAll(links);
    }

    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES, UPDATE}

    public enum SubType {
        MULTIPLE_GENES_PER_ACCESSION("Multiple ENSDARG IDs per ZFIN Gene"),
        MULTIPLE_GENES_PER_ACCESSION_BUT_APPROVED("Multiple Genes per Accession: Contains Approved Accession"),
        UPDATE_LENGTH_NULL("Update length info on DB_LINK for transcripts that had no value"),
        UPDATE_LENGTH_NON_NULL("Update length info on DB_LINK for transcripts that had a value"),
        ENSDART_MISSING("Transcripts (ENSDARTs) Missing in ZFIN"),
        ENSDARG_MISSING("Genes (ENSDARGs) Missing in ZFIN"),
        ENSDART_LOADED("New Transcript (ENSDART) Records Loaded into ZFIN"),
        ENSDART_ADDED("ENSDART added to existing Transcript in ZFIN"),
        ENSDART_REMOVED("ENSDART record removed from Transcript in ZFIN"),
        TRANSCRIPT_EXISTS("A Transcript of a given name already exists"),
        NO_NAME_FOR_TRANSCRIPT_FOUND("No Name found for Transcript"),
        PRIORITY_1("Priority 1"),
        PRIORITY_2("Priority 2"),
        PRIORITY_3("Priority 3"),
        NO_PRIORITY_FOUND("No Priority Found"),
        UNSUPPTORTED_BIOTYPE("Unsupported Biotype"),
        ENSEMBL_TRANSCRIPTS_DUPLICATE_PER_NAME("Multiple Transcripts in Ensembl have the same name in ENSEMBL"),
        ZFIN_ENSDARG_NOT_TRANSCRIPT_FILE("ENSDARG ID in ZFIN but not in the Transcript fasta file"),
        ZFIN_ENSDARG_NOT_TRANSCRIPT_FILE_MULTIPLE("ENSDARG ID in ZFIN but not in the Transcript fasta file"),
        ZFIN_ENSDART_NOT_TRANSCRIPT_FILE("ENSDART ID in ZFIN but not in the Transcript fasta file"),
        ADD_ATTRIBUTION("Add Attribution");

        private final String value;

        SubType(String s) {
            this.value = s;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public String toString() {
        return "LoadAction: " +
               "accession: " + accession +
               " type: " + type +
               " subType: " + subType +
               " geneZdbID: " + geneZdbID +
               " details: " + details +
               " length: " + length +
               " links: " + links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoadAction that)) return false;
        return compareTo(that) == 0;
    }

    @Override
    public int compareTo(LoadAction o) {
        Comparator<LoadAction> comparator = Comparator.comparing(
                (LoadAction obj) -> obj.accession, ObjectUtils::compare)
            .thenComparing(obj -> obj.type, ObjectUtils::compare)
            .thenComparing(obj -> obj.subType, ObjectUtils::compare)
            .thenComparing(obj -> obj.geneZdbID, ObjectUtils::compare)
            .thenComparing(obj -> obj.details, ObjectUtils::compare);
        return comparator.compare(this, o);
    }

}
