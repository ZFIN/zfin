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

    @Builder.Default
    private Set<UniProtLoadTag> tags = new TreeSet<>();

    public UniProtLoadAction() {
        links = new TreeSet<>();
        tags = new TreeSet<>();
    }

    public UniProtLoadAction(Type type, SubType subType, String accession, String geneZdbID, String details, int length, Set<UniProtLoadLink> links, Set<UniProtLoadTag> tags) {
        this.type = type;
        this.subType = subType;
        this.accession = accession;
        this.geneZdbID = geneZdbID;
        this.details = details;
        this.length = length;
        this.links = links;
        this.tags = tags;
    }

    public void addLink(UniProtLoadLink uniProtLoadLink) {
        links.add(uniProtLoadLink);
    }

    public void addLinks(Collection<UniProtLoadLink> links) {
        this.links.addAll(links);
    }

    public void addTag(CategoryTag tag) {
        this.tags.add( new UniProtLoadTag(tag.name(), tag.getValue()));
    }

    public void addDetails(String details) {
        if (this.details == null) {
            this.details = details;
            return;
        }
        this.details += "\n" + details;
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
        ADD_ATTRIBUTION("Add Attribution"),
        GENE_LOST_ALL_UNIPROTS("ZFIN Gene Lost All UniProt Accessions"),
        GENE_GAINS_FIRST_UNIPROT("ZFIN Gene Gains First UniProt Accession"),
        MANUALLY_CURATED_ACCESSION_WOULD_BE_LOST("Manually Curated UniProt Accession Would Be Lost"),
        ;

        private final String value;

        SubType(String s) {
            this.value = s;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    //Some different tags that we can attach to each action for different categories of actions. Useful for cases
    //where we want to attach multiple descriptors to the same action.
    public enum CategoryTag {
        //may eventually replace subtypes: REMOVE_ATTRIBUTION_DUE_TO_OBSOLETE, ADD_ATTRIBUTION, GENE_LOST_ALL_UNIPROTS,
        // GENE_GAINS_FIRST_UNIPROT, LOST_UNIPROT_DUE_TO_OBSOLETE

        REPLACED_REFSEQ("RefSeq Accession Has Replacement"),
        SUPPRESSED_REFSEQ("RefSeq Accession Suppressed"),
        NEW_GENE("ZFIN Gene Gains First UniProt Accession"),
        LOST_ALL_UNIPROTS("ZFIN Gene Losing All UniProt Accessions");

        private final String value;

        CategoryTag(String s) {this.value = s;}

        @JsonValue
        public String getValue() {return value;}
    }

    public String toString() {
        return "UniProtLoadAction: " +
                "accession: " + accession +
                " type: " + type +
                " subType: " + subType +
                " geneZdbID: " + geneZdbID +
                " details: " + details +
                " length: " + length +
                " tags: " + tags +
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
