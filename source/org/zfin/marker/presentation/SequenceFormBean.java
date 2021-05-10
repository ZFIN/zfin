package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.sequence.Sequence;
import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.stream.Collectors;

@Setter
@Getter
public class SequenceFormBean {


    @JsonView(View.SequenceAPI.class)
    private String data;

    @JsonView(View.SequenceAPI.class)
    private String referenceDatabaseZdbID;

    @JsonView(View.SequenceAPI.class)
    private Collection<Publication> references;

    public static SequenceFormBean convert(Sequence relationship) {
        SequenceFormBean bean = new SequenceFormBean();
        bean.setData(relationship.getData());
        bean.setReferences(relationship.getDbLink().getPublications().stream()
                .map(PublicationAttribution::getPublication)
                .collect(Collectors.toList()));
        return bean;
    }
}
