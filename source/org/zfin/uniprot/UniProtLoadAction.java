package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.ExternalNote;

import java.util.*;

import static java.lang.CharSequence.compare;

@Getter
@Setter
public class UniProtLoadAction implements Comparable<UniProtLoadAction> {
    private String title;
    private String accession;
    private String geneZdbID;
    private String details;
    private Type type;

    private Set<UniProtLoadLink> links = new TreeSet<>();

    public UniProtLoadAction() {
    }

    public void addLink(UniProtLoadLink uniProtLoadLink) {
        links.add(uniProtLoadLink);
    }

    public void addLinks(Collection<UniProtLoadLink> links) {
        this.links.addAll(links);
    }

    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE}

    public enum MatchTitle {
        MULTIPLE_GENES_PER_ACCESSION("Multiple Genes per Accession"),
        MULTIPLE_GENES_PER_ACCESSION_BUT_APPROVED("Multiple Genes per Accession: Contains Approved Accession"),
        MATCH_BY_REFSEQ("Matched via RefSeq: Single Gene per Accession"),
        LOST_UNIPROT("ZFIN Gene Losing UniProt Accession"),
        LOST_UNIPROT_PREV_MATCH_BY_GB("Previously Matched by GenBank: No RefSeq Match"),
        LOST_UNIPROT_PREV_MATCH_BY_GP("Previously Matched by GenPept: No RefSeq Match");

        private String value;

        MatchTitle(String s) {
            this.value = s;
        }

        public String getValue() {
            return value;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UniProtLoadAction: ");
        sb.append("accession: ").append(accession);
        sb.append(" title: ").append(title);
        sb.append(" geneZdbID: ").append(geneZdbID);
        sb.append(" details: ").append(details);
        sb.append(" type: ").append(type);
        sb.append(" links: ").append(links);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniProtLoadAction)) return false;
        UniProtLoadAction that = (UniProtLoadAction) o;
        return compareTo(that) == 0;
    }

    @Override
    public int compareTo(UniProtLoadAction o) {
        Comparator<UniProtLoadAction> comparator = Comparator.comparing(
                (UniProtLoadAction obj) -> obj.accession, ObjectUtils::compare)
                .thenComparing(obj -> obj.type, ObjectUtils::compare)
                .thenComparing(obj -> obj.title, ObjectUtils::compare)
                .thenComparing(obj -> obj.geneZdbID, ObjectUtils::compare)
                .thenComparing(obj -> obj.details, ObjectUtils::compare)
                ;
        return comparator.compare(this, o);
    }


}
