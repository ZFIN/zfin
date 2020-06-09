package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

@Setter
@Getter
public class ConstructInfo {

    @JsonView(View.API.class)
    private Marker construct;
    @JsonView(View.API.class)
    private List<Marker> regulatoryRegions;
    @JsonView(View.API.class)
    private List<Marker> codingSequences;
    @JsonView(View.API.class)
    private int numberOfTransgeniclines;
    @JsonView(View.API.class)
    private List<ControlledVocab> species;
    @JsonView(View.API.class)
    private int numberOfPublications;
    @JsonView(View.API.class)
    public Publication getSinglePublication() {

        List<Publication>pub=RepositoryFactory.getPublicationRepository().getPubsForDisplay(construct.zdbID);
        if (pub.size()==1){
            return pub.iterator().next();
        } else {
            return null;
        }
    }

}
