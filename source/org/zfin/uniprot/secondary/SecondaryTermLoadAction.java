package org.zfin.uniprot.secondary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.UniProtLoadLink;

import java.util.*;

@Getter
@Setter
@Builder
public class SecondaryTermLoadAction implements Comparable<SecondaryTermLoadAction> {
    private Type type;
    private SubType subType;

    private ForeignDB.AvailableName dbName;
    private String accession;
    private String goID;
    private String goTermZdbID;
    private String geneZdbID;
    private String relatedEntityID;
    private String details;
    private int length;
    private String handlerClass;

    @Builder.Default
    private Map<String, String> relatedEntityFields = new HashMap<>();

    @Builder.Default
    private Set<UniProtLoadLink> links = new TreeSet<>();

    public SecondaryTermLoadAction() {
        links = new TreeSet<>();
    }

    public SecondaryTermLoadAction(Type type, SubType subType, ForeignDB.AvailableName dbName, String accession, String goID, String goTermZdbID, String geneZdbID, String relatedEntityID, String details, int length, String handlerClass, Map<String, String> relatedEntityFields, Set<UniProtLoadLink> links)
    {
        this.type = type;
        this.subType = subType;
        this.accession = accession;
        this.goID = goID;
        this.goTermZdbID = goTermZdbID;
        this.geneZdbID = geneZdbID;
        this.relatedEntityID = relatedEntityID;
        this.details = details;
        this.length = length;
        this.handlerClass = handlerClass;
        this.links = links;
        this.dbName = dbName;
        this.relatedEntityFields = relatedEntityFields;
    }

    public void addLink(UniProtLoadLink uniProtLoadLink) {
        links.add(uniProtLoadLink);
    }

    public void addLinks(Collection<UniProtLoadLink> links) {
        this.links.addAll(links);
    }

    @JsonIgnore
    public String getPrefixedAccession() {
        String prefixedAccession = "";
        switch(dbName) {
            case INTERPRO -> prefixedAccession = "InterPro:" + accession;
            case UNIPROTKB -> prefixedAccession = "UniProtKB-KW:" + accession;
            case EC -> prefixedAccession = "EC:" + accession;
            default -> prefixedAccession = dbName.toString() + ":" + accession;
        }
        return prefixedAccession;
    }

    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES}

    public enum SubType {
        MARKER_GO_TERM_EVIDENCE("MarkerGoTermEvidence"),
        EXTERNAL_NOTE("ExternalNote"),
        DB_LINK("DBLink"),
        PROTEIN_DOMAIN("ProteinDomain"),
        PROTEIN("Protein"),
        INTERPRO_MARKER_TO_PROTEIN("InterproMarkerToProtein"),
        PROTEIN_TO_INTERPRO("ProteinToInterpro"),
        PDB("PDB"),
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecondaryTermLoadAction that)) return false;
        return compareTo(that) == 0;
    }

    @Override
    public int compareTo(SecondaryTermLoadAction o) {
        Comparator<SecondaryTermLoadAction> comparator = Comparator.comparing(
                (SecondaryTermLoadAction obj) -> obj.accession, ObjectUtils::compare)
                .thenComparing(obj -> obj.type, ObjectUtils::compare)
                .thenComparing(obj -> obj.subType, ObjectUtils::compare)
                .thenComparing(obj -> obj.geneZdbID, ObjectUtils::compare)
                .thenComparing(obj -> obj.details, ObjectUtils::compare)
                ;
        return comparator.compare(this, o);
    }

    public String toString() {
        return "InterproLoadAction: " + " action=" + type +
                " subtype=" + subType +
                " accession=" + accession +
                " goID=" + goID +
                " goTermZdbID=" + goTermZdbID +
                " geneZdbID=" + geneZdbID +
                " relatedEntityID=" + relatedEntityID +
                " details=" + details +
                " length=" + length +
                " handlerClass=" + handlerClass +
                " relatedEntityFields=" + relatedEntityFields +
                " links=" + links;
    }

    public String markerGoTermEvidenceRepresentation() {
        return geneZdbID + "," + goTermZdbID + "," + goID + "," + dbName + ":" + this.accession;
    }

    public List<UniProtLoadLink> getDynamicLinks() {
        List<UniProtLoadLink> dynamicLinks = new ArrayList<>();
        if ( accession != null ) {
            if (dbName != null && dbName.equals(ForeignDB.AvailableName.INTERPRO)) {
                dynamicLinks.add(new UniProtLoadLink(accession, "http://www.ebi.ac.uk/interpro/entry/" + accession));
            }
        }
        if ( goID != null ) {
            dynamicLinks.add(new UniProtLoadLink(goID, "https://zfin.org/GO:" + goID));
        }
        if ( goTermZdbID != null ) {
            dynamicLinks.add(new UniProtLoadLink(goTermZdbID, "https://zfin.org/" + goTermZdbID));
        }
        if ( geneZdbID != null ) {
            dynamicLinks.add(new UniProtLoadLink(geneZdbID, "https://zfin.org/" + geneZdbID));
        }
        return dynamicLinks;
    }

}
