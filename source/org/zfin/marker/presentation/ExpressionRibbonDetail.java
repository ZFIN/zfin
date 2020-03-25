package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class ExpressionRibbonDetail {

    @JsonView(View.API.class)
    private TermDTO term;
    @JsonView(View.API.class)
    private List<StageDTO> stages;
    @JsonView(View.API.class)
    private List<String> stageHistogram;
    @JsonView(View.API.class)
    private List<Boolean> definedStages;
    @JsonView(View.API.class)
    private PublicationDTO publication;

    private List<String> pubIDs;

    @JsonView(View.API.class)
    private int getNumberOfPublications() {
        if (pubIDs == null)
            return 0;
        return pubIDs.size();
    }

    public void addStage(StageDTO stage) {
        if (stages == null)
            stages = new ArrayList<>();
        stages.add(stage);
    }

    public void addPublications(List<String> pubIDValues) {
        if (pubIDs == null)
            pubIDs = new ArrayList<>();
        pubIDs.addAll(pubIDValues);
        pubIDs = pubIDs.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return term.getTermName();
    }
}
