package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.publication.Publication;

@Setter
@Getter
public class PublicationStat {

    @JsonView(View.API.class)
    private Publication publication;
    @JsonView(View.API.class)
    private Integer markerCount;
}
