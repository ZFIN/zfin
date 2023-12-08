package org.zfin.uniprot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.zfin.sequence.DBLinkExternalNote;

@Builder
@Getter
@Setter
public class DBLinkExternalNoteSlimDTO {
    private String zdbID;
    private String accession;
    private String geneZdbID;
    private String dblinkZdbID;
    private String note;

    public static DBLinkExternalNoteSlimDTO from(DBLinkExternalNote dbLinkExternalNote) {
        return DBLinkExternalNoteSlimDTO.builder()
                .geneZdbID(dbLinkExternalNote.getDblink().getDataZdbID())
                .dblinkZdbID(dbLinkExternalNote.getDblink().getZdbID())
                .accession(dbLinkExternalNote.getDblink().getAccessionNumber())
                .note(dbLinkExternalNote.getNote())
                .zdbID(dbLinkExternalNote.getZdbID())
                .build();
    }
}
