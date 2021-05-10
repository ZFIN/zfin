package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.api.View;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class ExpressionRibbonDetail {

    @JsonView(View.API.class)
    private PostComposedEntity entity;
    @JsonView(View.API.class)
    private List<DevelopmentStage> stages;
    @JsonView(View.API.class)
    private Publication publication;
    @JsonView(View.API.class)
    private List<String>  ribbonPubs;

    private List<String> pubIDs;

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

    @Override
    public String toString() {
        return entity.toString();
    }
}
