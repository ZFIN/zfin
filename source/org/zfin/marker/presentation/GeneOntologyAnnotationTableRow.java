package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.Collection;

@Getter
@Setter
public class GeneOntologyAnnotationTableRow {

    @JsonView(View.API.class) private String qualifier;
    @JsonView(View.API.class) private GenericTerm term;
    @JsonView(View.API.class) private GoEvidenceCode evidenceCode;
    @JsonView(View.API.class) private Collection<String> inferenceLinks;
    @JsonView(View.API.class) private Collection<String> annotationExtensions;
    @JsonView(View.API.class) private Collection<Publication> publications;

}
