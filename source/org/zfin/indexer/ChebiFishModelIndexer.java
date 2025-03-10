package org.zfin.indexer;

import jakarta.persistence.Table;
import lombok.extern.log4j.Log4j2;
import org.zfin.expression.ExperimentCondition;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.ontology.GenericTerm;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.zfin.repository.RepositoryFactory.getDiseasePageRepository;

@Log4j2
public class ChebiFishModelIndexer extends UiIndexer<ChebiFishModelDisplay> {

    public ChebiFishModelIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected List<ChebiFishModelDisplay> inputOutput() {
        List<FishModelDisplay> fishModelList = getDiseasePageRepository().getAllFishDiseaseModels();

        List<ChebiFishModelDisplay> resultList = new ArrayList<>();
        fishModelList.forEach(fishModelDisplay -> {
            fishModelDisplay.setEvidenceSearch(fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream()
                .map(model -> DTOConversionService.evidenceCodeIdToAbbreviation(model.getDiseaseAnnotation().getEvidenceCode().getZdbID()))
                .distinct()
                .collect(Collectors.joining(",")));
            fishModelDisplay.setEvidenceCodes(new HashSet<>(fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream().map(model -> model.getDiseaseAnnotation().getEvidenceCode()).collect(Collectors.toList())));
            fishModelDisplay.setFishSearch(fishModelDisplay.getFish().getName().replaceAll("<[^>]*>", ""));
            fishModelDisplay.setConditionSearch(fishModelDisplay.getFishModel().getExperiment().getDisplayAllConditions());

            Set<GenericTerm> chebiTerms = fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream()
                .map(diseaseAnnotationModel -> diseaseAnnotationModel.getFishExperiment().getExperiment().getExperimentConditions().stream()
                    .map(ExperimentCondition::getChebiTerm).toList()).toList()
                .stream().flatMap(Collection::stream).collect(toSet());

            chebiTerms.stream().filter(Objects::nonNull)
                .forEach(chebiTerm -> {
                    ChebiFishModelDisplay display = new ChebiFishModelDisplay();
                    display.setFishModelDisplay(fishModelDisplay);
                    display.setChebi(chebiTerm);
                    resultList.add(display);
                });
        });
        return resultList;
    }

    @Override
    protected void cleanUiTables() {
        String schema = ChebiFishModelDisplay.class.getAnnotation(Table.class).schema();
        String fishModelTable = ChebiFishModelDisplay.class.getAnnotation(Table.class).name();
        cleanoutTable(schema, fishModelTable);
    }

}
