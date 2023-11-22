package org.zfin.uniprot.secondary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.UniProtLoadLink;

import java.util.*;

@Getter
@Builder(toBuilder = true)
public class SecondaryTermLoadAction implements Comparable<SecondaryTermLoadAction> {
    public enum Type {LOAD, INFO, WARNING, ERROR, DELETE, IGNORE, DUPES}

    public enum SubType {
        MARKER_GO_TERM_EVIDENCE("MarkerGoTermEvidence", 1),
        EXTERNAL_NOTE("ExternalNote", 2),
        DB_LINK("DBLink", 3),
        PROTEIN_DOMAIN("ProteinDomain", 4),
        PROTEIN("Protein", 5),
        INTERPRO_MARKER_TO_PROTEIN("InterproMarkerToProtein", 6),
        PROTEIN_TO_INTERPRO("ProteinToInterpro", 7),
        PDB("PDB", 8),
        ;

        private final String value;
        private final int processActionOrder;

        SubType(String s, int o) {
            this.value = s;
            this.processActionOrder = o;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public int getProcessActionOrder() {
            return processActionOrder;
        }
    }
    private final Type type;
    private final SubType subType;
    private final ForeignDB.AvailableName dbName;
    private final String accession;
    private final String goID;
    private final String goTermZdbID;
    private final String geneZdbID;
    private final String relatedEntityID;
    private final String details;
    private final int length;
    private final String handlerClass;
    private final Map<String, String> relatedEntityFields;
    private final Set<UniProtLoadLink> links;

    @JsonIgnore
    public String getPrefixedAccession() {
        return switch(dbName) {
            case INTERPRO ->  "InterPro:" + accession;
            case UNIPROTKB ->  "UniProtKB-KW:" + accession;
            case EC -> "EC:" + accession;
            default ->  dbName.toString() + ":" + accession;
        };
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
                " relatedEntityFields=" + relatedEntityFieldsToString() +
                " links=" + links;
    }

    public String markerGoTermEvidenceRepresentation() {
        return geneZdbID + "," + goTermZdbID + "," + goID + "," + dbName + ":" + this.accession;
    }

    public String getMd5() {
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        return encoder.encodePassword(toString(), null);
    }

    @JsonIgnore
    public String relatedEntityFieldsToString() {
        if (relatedEntityFields == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, String> entry : relatedEntityFields.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        sb.append("}");
        return sb.toString();
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
