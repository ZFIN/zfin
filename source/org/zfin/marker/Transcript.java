package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.api.View;
import org.zfin.mutant.Genotype;
import org.zfin.sequence.DisplayGroup;

import org.zfin.sequence.TranscriptDBLink;
import org.zfin.sequence.service.TranscriptService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "transcript")
@PrimaryKeyJoinColumn(name = "tscript_mrkr_zdb_id")
@Getter
@Setter
public class Transcript extends Marker {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tscript_type_id")
    @JsonView(View.TranscriptDetailsAPI.class)
    private TranscriptType transcriptType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tscript_status_id")
    @JsonView(View.TranscriptDetailsAPI.class)
    private TranscriptStatus status;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dblink_linked_recid")
    private Set<TranscriptDBLink> transcriptDBLinks;

    @Column(name = "tscript_ensdart_id")
    private String ensdartId;

    @Column(name = "tscript_load_id")
    private String loadId;

    @Transient
    private TranscriptSequence trSequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tscript_genotype_zdb_id")
    private Genotype strain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tscript_vocab_id")
    private VocabularyTerm annotationMethod;



    public Integer getLength() {
        return TranscriptService.getTranscriptLength(this, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
    }

    public boolean isWithdrawn(){
        if(status!=null){
            return status.getStatus()== TranscriptStatus.Status.WITHDRAWN_BY_SANGER;
        }
        return false ;
    }

    public List<TranscriptDBLink> getTranscriptDBLinksForDisplayGroup(DisplayGroup.GroupName displayGroup) {
        List<TranscriptDBLink> returnDBLinks = new ArrayList<TranscriptDBLink>();
        for (TranscriptDBLink transcriptDBLink : getTranscriptDBLinks()) {
            if (transcriptDBLink.getReferenceDatabase().isInDisplayGroup(displayGroup)) {
                returnDBLinks.add(transcriptDBLink);
            }
        }
        return returnDBLinks;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder("Transcript");
        sb.append("\n");
        sb.append("zdbID: ").append(getZdbID());
        sb.append("\n");
        sb.append("name: ").append(getName());
        sb.append("\n");
        sb.append("symbol: ").append(getAbbreviation());
        sb.append("\n");
        sb.append("type: ").append(getMarkerType());
        sb.append("\n");
        sb.append("transcript type: ").append(transcriptType);
        sb.append("\n");
        sb.append("transcript status: ").append(status);
        sb.append("\n");
        return sb.toString();
    }
}
