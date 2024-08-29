package org.zfin.sequence.load;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.zfin.uniprot.UniProtLoadLink;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@Builder
public class LoadAction implements Comparable<LoadAction> {
    private Type type;
    private SubType subType;
    private String accession;
    private String geneZdbID;
    private String details;
    private int length;

    @Builder.Default
    private Set<LoadLink> links = new TreeSet<>();

    public LoadAction() {
        links = new TreeSet<>();
    }

    public LoadAction(Type type, SubType subType, String accession, String geneZdbID, String details, int length, Set<LoadLink> links) {
        this.type = type;
        this.subType = subType;
        this.accession = accession;
        this.geneZdbID = geneZdbID;
        this.details = details;
        this.length = length;
        this.links = links;
    }

    public void addLink(LoadLink loadLink) {
        links.add(loadLink);
    }

    public void addLinks(Collection<LoadLink> links) {
        this.links.addAll(links);
    }

    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES, UPDATE}

    public enum SubType {
        MULTIPLE_GENES_PER_ACCESSION("Multiple Genes per Accession"),
        MULTIPLE_GENES_PER_ACCESSION_BUT_APPROVED("Multiple Genes per Accession: Contains Approved Accession"),
        UPDATE_LENGTH_NULL("Update length info on DB_LINK for transcripts that had no value"),
        UPDATE_LENGTH_NON_NULL("Update length info on DB_LINK for transcripts that had a value"),
        ENSDART_MISSING("Transcripts (ENSDARTs) Missing in ZFIN"),
        ENSDARG_MISSING("Genes (ENSDARGs) Missing in ZFIN"),
        ZFIN_OBSOLETE("OBSOLETED ENSDARG IDs in ZFIN"),
        ZFIN_OBSOLETE_MULTIPLE("OBSOLETED ENSDARG IDs in ZFIN on multiple ZDB IDs"),
        ZFIN_TRANSCRIPT_OBSOLETE("OBSOLETED ENSDART IDs in ZFIN"),
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
