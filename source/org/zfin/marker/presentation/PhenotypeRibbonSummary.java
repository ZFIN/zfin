package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.api.View;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class PhenotypeRibbonSummary {

    @JsonView(View.API.class)
    private String id;
    @JsonView(View.API.class)
    private String phenotype;
    @JsonView(View.API.class)
    private List<DevelopmentStage> stages;
    @JsonView(View.API.class)
    private Publication publication;
    @JsonView(View.API.class)
    private List<String> ribbonPubs;
    @JsonView(View.API.class)
    private List<String> pubIDs;
    private Set<String> phenotypeIDs;

    @JsonView(View.API.class)
    private int getNumberOfPublications() {
        if (pubIDs == null) {
            return 0;
        }
        return pubIDs.size();
    }

    public void addStage(DevelopmentStage stage) {
        if (stages == null) {
            stages = new ArrayList<>();
        }
        stages.add(stage);
    }

    public void addPublications(List<String> pubIDValues) {
        if (pubIDs == null) {
            pubIDs = new ArrayList<>();
        }
        pubIDs.addAll(pubIDValues);
        pubIDs = pubIDs.stream().distinct().collect(Collectors.toList());
    }

    public void addPhenotypeIds(List<String> phenotypeID) {
        if (phenotypeIDs == null)
            phenotypeIDs = new HashSet<>();
        phenotypeIDs.addAll(phenotypeID);
    }

    @JsonView(View.API.class)
    public String getPhenotypeIDs() {
        return String.join(",", phenotypeIDs);
    }

    @Override
    public String toString() {
        return phenotype;
    }
}
