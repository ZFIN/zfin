package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.framework.api.Pagination;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mutant.Zygosity;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.ontology.service.OntologyService;

import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
        diseaseModelsWithFishModelsGrouped.forEach(fishModel -> {
            int affectedGeneCount = (int) fishModel.getFish().getFishFunctionalAffectedGeneCount();
            boolean singleGenotypeFeature = fishModel.getFish().getGenotype().getGenotypeFeatures().size() == 1;
            Zygosity.Type zygosity = null;
            if (singleGenotypeFeature) {
                Zygosity zygo = fishModel.getFish().getGenotype().getGenotypeFeatures().iterator().next().getZygosity();
                switch (zygo.getType()) {
                    case HOMOZYGOUS -> zygosity = Zygosity.Type.HOMOZYGOUS;
                    case HETEROZYGOUS -> zygosity = Zygosity.Type.HETEROZYGOUS;
                    case COMPLEX -> zygosity = Zygosity.Type.COMPLEX;
                }
            }
            boolean singleSTR = fishModel.getFish().getStrList().size() == 1;
            boolean isSTR = CollectionUtils.isNotEmpty(fishModel.getFish().getStrList());
            long singleTargetGene = 10;
            if (singleSTR) {
                singleTargetGene = fishModel.getFish().getStrList().get(0).getTargetGenes().size() == 1 ? 1 : 5;
            }
            fishModel.getExperiment().isStandard();
            boolean isWildType = fishModel.getFish().getGenotype().isWildtype();
            int order = 0;
            int increment = 100;
            if (zygosity != null && zygosity.equals(Zygosity.Type.HOMOZYGOUS) && affectedGeneCount == 1 && !isSTR) {
                order = increment;
            }
            if (isWildType && singleSTR && affectedGeneCount == 1) {
                order = 2 * increment;
            }
            if (!isWildType && singleSTR && affectedGeneCount == 1) {
                order = 3 * increment;
            }
            if (zygosity != null && zygosity.equals(Zygosity.Type.HETEROZYGOUS) && affectedGeneCount == 1) {
                order = 4 * increment;
            }
            if (zygosity == null && affectedGeneCount == 1) {
                order = 5 * increment;
            }
            if (isWildType && !singleSTR && isSTR) {
                order = 6 * increment;
            }
            if (!isWildType && !singleSTR && isSTR) {
                order = 7 * increment;
            }
            if (zygosity != null && zygosity.equals(Zygosity.Type.COMPLEX)) {
                order = 8 * increment + affectedGeneCount;
            }
            if (isWildType && !fishModel.getExperiment().isStandard()) {
                order = 9 * increment;
            }
            if (!isWildType && !fishModel.getExperiment().isStandard()) {
                order = 10 * increment;
            }
            fishModel.setOrder(order);
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
