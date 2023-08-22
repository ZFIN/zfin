package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.ExternalNote;

import java.util.*;

@Getter
@Setter
public class UniProtLoadAction {
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

    public enum Type {LOAD, INFO, WARNING, ERROR}

    public enum MatchTitle {
        MULTIPLE_GENES_PER_ACCESSION("Multiple Genes per Accession"),
        MULTIPLE_GENES_PER_ACCESSION_BUT_APPROVED("Multiple Genes per Accession: Contains Approved Accession"),
        MATCH_BY_REFSEQ("Matched via RefSeq: Single Gene per Accession"),
        LOST_UNIPROT("ZFIN Gene Losing UniProt Accession");

        private String value;

        MatchTitle(String s) {
            this.value = s;
        }

        public String getValue() {
            return value;
        }
    }
}
