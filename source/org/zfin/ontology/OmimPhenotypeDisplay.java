package org.zfin.ontology;

import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.orthology.Ortholog;
import org.zfin.sequence.DBLink;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * OMIM Phenotype
 */
@Setter
@Getter
public class OmimPhenotypeDisplay {

    @JsonView(View.API.class)
    private String name;
    @JsonView(View.API.class)
    private String omimNum;
    @JsonView(View.API.class)
    private ArrayList<String> humanGene;
    @JsonView(View.API.class)
    private String symbol;

    @JsonView(View.API.class)
    private HumanGeneDetail humanGeneDetail;

    public DBLink getHumanAccession() {
        return humanAccession;
    }
    @JsonView(View.API.class)
    public String omimAccession;

    @JsonView(View.API.class)
    private Ortholog orthology;
    private DBLink humanAccession;
    @JsonView(View.API.class)
    private List<Marker> zfinGene;
    @JsonView(View.API.class)
    private GenericTerm term;

}
