package org.zfin.framework.api;

import org.apache.commons.lang3.StringUtils;
import org.zfin.antibody.Antibody;

import java.util.stream.Collectors;

public class AntibodyFiltering extends Filtering<Antibody> {


    public AntibodyFiltering() {
        filterFieldMap.put(FieldFilter.ANTIBODY_NAME, antibodyNameFilter);
        filterFieldMap.put(FieldFilter.ISOTYPE, isotypeFilter);
        filterFieldMap.put(FieldFilter.HOST, hostFilter);
        filterFieldMap.put(FieldFilter.CLONAL_TYPE, clonalTypeFilter);
        filterFieldMap.put(FieldFilter.ASSAY, assayFilter);
        //filterFieldMap.put(FieldFilter.GENE_ABBREVIATION, antibodyNameFilter);
    }

    public static FilterFunction<Antibody, String> antibodyNameFilter =
            (antibody, value) -> FilterFunction.contains(antibody.getName(), value);

    public static FilterFunction<Antibody, String> isotypeFilter =
            (antibody, value) -> {
                if (antibody.getHeavyChainIsotype() != null)
                    return FilterFunction.fullMatchMultiValueOR(antibody.getHeavyChainIsotype(), value);
                return false;
            };

    public static FilterFunction<Antibody, String> hostFilter =
            (antibody, value) -> {
                if (antibody.getHostSpecies() != null)
                    return FilterFunction.fullMatchMultiValueOR(antibody.getHostSpecies(), value);
                return false;
            };

    public static FilterFunction<Antibody, String> clonalTypeFilter =
            (antibody, value) -> {
                if (antibody.getClonalType() != null)
                    return FilterFunction.fullMatchMultiValueOR(antibody.getClonalType(), value);
                return false;
            };

    public static FilterFunction<Antibody, String> assayFilter =
            (antibody, value) -> {
                if (antibody.getDistinctAssayNames() == null)
                    return StringUtils.isEmpty(value);
                return FilterFunction.contains(String.join(",", antibody.getDistinctAssayNames()), value);
            };

    public static FilterFunction<Antibody, String> antigenGenesFilter =
            (antibody, value) -> {
                if (antibody.getAntigenGenes() == null)
                    return StringUtils.isEmpty(value);
                return FilterFunction.contains(antibody.getAntigenGenes().stream().map(marker -> marker.getName()).collect(Collectors.joining()), value);
            };
}
