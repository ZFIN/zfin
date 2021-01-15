package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.TranscriptStatus;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptType;
import org.zfin.profile.Organization;
import org.zfin.profile.presentation.SupplierBean;
import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.stream.Collectors;

@Setter
@Getter
public class TranscriptAttributeBean {
    @JsonView(View.TranscriptDetailsAPI.class)
    String transcriptType;
    @JsonView(View.TranscriptDetailsAPI.class)
    String transcriptStatus;
    @JsonView(View.TranscriptDetailsAPI.class)
    private Collection<Publication> references;


    public static TranscriptAttributeBean convert(Transcript transcript) {
        TranscriptAttributeBean newBean = new TranscriptAttributeBean();
        newBean.setTranscriptType(transcript.getTranscriptType().getDisplay());
        if (transcript.getStatus().getStatus().toString()!=null) {
            newBean.setTranscriptStatus(transcript.getStatus().getDisplay());
        }

        newBean.setReferences(transcript.getPublications().stream()
                .map(PublicationAttribution::getPublication)
                .collect(Collectors.toList()));
        return newBean;
    }


}
