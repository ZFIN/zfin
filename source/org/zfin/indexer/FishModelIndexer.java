package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.service.OntologyService;

import javax.persistence.JoinTable;
import javax.persistence.Table;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Log4j2
public class FishModelIndexer extends UiIndexer<FishModelDisplay> {

    public FishModelIndexer(UiIndexerConfig config) {
        super(config);
    }

    @Override
    protected List<FishModelDisplay> inputOutput() {
        List<FishModelDisplay> diseaseModelsWithFishModelsGrouped = OntologyService.getDiseaseModelsWithFishModelsGrouped(null, false, new Pagination());

        diseaseModelsWithFishModelsGrouped.forEach(fishModelDisplay -> {
            fishModelDisplay.setEvidenceSearch(fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream()
                .map(model -> DTOConversionService.evidenceCodeIdToAbbreviation(model.getDiseaseAnnotation().getEvidenceCode().getZdbID()))
                .distinct()
                .collect(Collectors.joining(",")));
            fishModelDisplay.setEvidenceCodes(new HashSet<>(fishModelDisplay.getFishModel().getDiseaseAnnotationModels().stream().map(model -> model.getDiseaseAnnotation().getEvidenceCode()).collect(Collectors.toList())));
            fishModelDisplay.setFishSearch(fishModelDisplay.getFish().getName().replaceAll("<[^>]*>", ""));
            fishModelDisplay.setConditionSearch(fishModelDisplay.getFishModel().getExperiment().getDisplayAllConditions());
        });
        return diseaseModelsWithFishModelsGrouped;
    }

    @Override
    protected void cleanUiTables() {
        String associationTable;
        try {
            associationTable = FishModelDisplay.class.getDeclaredField("evidenceCodes").getAnnotation(JoinTable.class).name();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        String fishModelTable = FishModelDisplay.class.getAnnotation(Table.class).name();
        cleanoutTable(associationTable, fishModelTable);
    }

}
