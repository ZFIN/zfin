package org.zfin.gwt.root.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

/**
 * External ortholog, i.e. the non-zebrafish ortholog found at NCBI
 */
@Getter
@Setter
public class NcbiOtherSpeciesGeneDTO {

    @JsonView(View.OrthologyAPI.class)
    private String ID;
    private String name;
    @JsonView(View.OrthologyAPI.class)
    private String abbreviation;
    private String chromosome;
    private String position;
    @JsonView(View.OrthologyAPI.class)
    private String organism;

}
