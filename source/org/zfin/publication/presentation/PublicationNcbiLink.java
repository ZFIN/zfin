package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

/**
 * One NCBI Gene or RefSeq db_link attributed to a publication: the ZFIN marker it belongs to, the
 * symbol and accession, the kind of link ({@code type}), the assemblies it belongs to, the date encoded in the
 * db_link's own ZDB ID ({@code dblinkDate}) and the date the attribution was created.
 * <p>
 * Assemblies are per gene for NCBI Gene links (marker_assembly, which the NCBI load keeps in sync
 * with them) and per link for RefSeq accessions (db_link_assembly), since model RefSeqs (XM_/XP_)
 * only exist within one assembly's annotation.
 */
public record PublicationNcbiLink(
        @JsonView(View.API.class) String dblinkID,
        @JsonView(View.API.class) String zdbID,
        @JsonView(View.API.class) String symbol,
        @JsonView(View.API.class) String accession,
        @JsonView(View.API.class) String type,
        @JsonView(View.API.class) String url,
        @JsonView(View.API.class) String assemblies,
        @JsonView(View.API.class) String dblinkDate,
        @JsonView(View.API.class) String attributionDate) {
}
