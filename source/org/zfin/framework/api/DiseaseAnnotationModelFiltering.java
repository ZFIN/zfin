package org.zfin.framework.api;

import org.zfin.mutant.DiseaseAnnotationModel;

public class DiseaseAnnotationModelFiltering extends Filtering<DiseaseAnnotationModel> {


    public DiseaseAnnotationModelFiltering() {
        filterFieldMap.put(FieldFilter.PUBLICATION_ID, publicationFilter);
        filterFieldMap.put(FieldFilter.DISEASE_NAME, diseaseFilter);
        filterFieldMap.put(FieldFilter.FISH_NAME, fishFilter);
        filterFieldMap.put(FieldFilter.FILTER_EVIDENCE, evidenceFilter);
        filterFieldMap.put(FieldFilter.EXPERIMENT, experimentFilter);
    }

    public static FilterFunction<DiseaseAnnotationModel, String> publicationFilter =
        (model, value) -> FilterFunction.contains(model.getDiseaseAnnotation().getPublication().getZdbID(), value);

    public static FilterFunction<DiseaseAnnotationModel, String> diseaseFilter =
        (model, value) -> FilterFunction.contains(model.getDiseaseAnnotation().getDisease().getTermName(), value);

    public static FilterFunction<DiseaseAnnotationModel, String> fishFilter =
        (model, value) -> {
            if (model.getFishExperiment() != null && model.getFishExperiment().getFish() != null)
                return FilterFunction.fullMatchMultiValueOR(model.getFishExperiment().getFish().getName(), value);
            return false;
        };

    public static FilterFunction<DiseaseAnnotationModel, String> evidenceFilter =
        (model, value) -> FilterFunction.fullMatchMultiValueOR(model.getDiseaseAnnotation().getEvidenceCodeString(), value);

    public static FilterFunction<DiseaseAnnotationModel, String> experimentFilter =
        (model, value) -> {
            if (model.getFishExperiment() != null && model.getFishExperiment().getExperiment() != null)
                return FilterFunction.fullMatchMultiValueOR(model.getFishExperiment().getExperiment().getDisplayAllConditions(), value);
            return false;
        };

}
