package org.zfin.uniprot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static org.zfin.uniprot.UniProtTools.isNonLoadPublication;

@Getter
@Setter
public class DBLinkSlimDTO {
    private String accession;
    private String dataZdbID;
    private String markerAbbreviation;
    private String dbName;
    private List<String> publicationIDs;

    public String debugString() {
        return "DBLinkSlimDTO{" +
                "accession='" + accession + '\'' +
                ", dataZdbID='" + dataZdbID + '\'' +
                ", markerAbbreviation='" + markerAbbreviation + '\'' +
                ", dbName='" + dbName + '\'' +
                ", publicationIDs='" + getPublicationIDsAsString() + '\'' +
                '}';
    }

    private String getPublicationIDsAsString() {
        if (publicationIDs == null) {
            return "";
        }
        return String.join(",", publicationIDs);
    }

    public boolean containsNonLoadPublication() {
        if (publicationIDs == null) {
            return false;
        }
        return publicationIDs.stream().anyMatch(id -> isNonLoadPublication(id));
    }
}
