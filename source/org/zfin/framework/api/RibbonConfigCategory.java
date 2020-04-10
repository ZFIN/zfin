package org.zfin.framework.api;

import lombok.Builder;
import lombok.Getter;
import org.zfin.ontology.GenericTerm;

import java.util.List;

@Builder
@Getter
public class RibbonConfigCategory {

    private GenericTerm categoryTerm;
    private List<GenericTerm> slimTerms;

    @Builder.Default private boolean includeAll = true;
    private String allLabel;
    private String allDescription;

    @Builder.Default private boolean includeOther = true;
    private String otherLabel;
    private String otherDescription;

}
