package org.zfin.uniprot;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;

@Getter
@Setter
@Builder
public class UniProtLoadAction implements Comparable<UniProtLoadAction> {
    private Type type;
    private SubType subType;
    private String accession;
    private String geneZdbID;
    private String details;
    private int length;

    @Builder.Default
    private Set<UniProtLoadLink> links = new TreeSet<>();

    public UniProtLoadAction() {
        links = new TreeSet<>();
    }

    public UniProtLoadAction(Type type, SubType subType, String accession, String geneZdbID, String details, int length, Set<UniProtLoadLink> links) {
        this.type = type;
        this.subType = subType;
        this.accession = accession;
        this.geneZdbID = geneZdbID;
        this.details = details;
        this.length = length;
        this.links = links;
    }

    public void addLink(UniProtLoadLink uniProtLoadLink) {
        links.add(uniProtLoadLink);
    }

    public void addLinks(Collection<UniProtLoadLink> links) {
        this.links.addAll(links);
    }

    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES}

    public enum SubType {
        MULTIPLE_GENES_PER_ACCESSION("Multiple Genes per Accession"),
        MULTIPLE_GENES_PER_ACCESSION_BUT_APPROVED("Multiple Genes per Accession: Contains Approved Accession"),
        MATCH_BY_REFSEQ("Matched via RefSeq: Single Gene per Accession"),
        LOST_UNIPROT("ZFIN Gene Losing UniProt Accession"),
        LOST_UNIPROT_PREV_MATCH_BY_GB("Previously Matched by GenBank: No RefSeq Match"),
        LOST_UNIPROT_PREV_MATCH_BY_GP("Previously Matched by GenPept: No RefSeq Match"),
        LEGACY_PROBLEM_FILE("Legacy Problem File"),
        LEGACY_PROBLEM_FILE_LOAD("Legacy Problem File - Load"),
        LEGACY_PROBLEM_FILE_DELETE("Legacy Problem File - Delete"),
        REMOVE_ATTRIBUTION("Remove Attribution"),
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
        return "UniProtLoadAction: " +
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
        if (!(o instanceof UniProtLoadAction that)) return false;
        return compareTo(that) == 0;
    }

    @Override
    public int compareTo(UniProtLoadAction o) {
        Comparator<UniProtLoadAction> comparator = Comparator.comparing(
                (UniProtLoadAction obj) -> obj.accession, ObjectUtils::compare)
                .thenComparing(obj -> obj.type, ObjectUtils::compare)
                .thenComparing(obj -> obj.subType, ObjectUtils::compare)
                .thenComparing(obj -> obj.geneZdbID, ObjectUtils::compare)
                .thenComparing(obj -> obj.details, ObjectUtils::compare)
                ;
        return comparator.compare(this, o);
    }

}
