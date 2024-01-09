package org.zfin.framework.api;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterService<T> {

    private Filtering<T> filtering;

    public FilterService(Filtering<T> filtering) {
        super();
        this.filtering = filtering;
    }

    public List<T> filterAnnotations(List<T> annotationList, BaseFilter fieldFilterValueMap) {
        if (annotationList == null)
            return null;
        if (fieldFilterValueMap == null)
            return annotationList;
        return annotationList.stream()
                .filter(annotation -> containsFilterValue(annotation, fieldFilterValueMap))
                .collect(Collectors.toList());
    }

    public boolean containsFilterValue(T annotation, BaseFilter fieldFilterValueMap) {
        // remove entries with null values.
        fieldFilterValueMap.values().removeIf(Objects::isNull);

        Set<Boolean> filterResults = fieldFilterValueMap.entrySet().stream()
                .map((entry) -> {
                    FilterFunction<T, String> filterFunction = filtering.filterFieldMap.get(entry.getKey());
                    if (filterFunction == null)
                        return null;
                    return filterFunction.containsFilterValue(annotation, entry.getValue());
                })
                .collect(Collectors.toSet());

        return !filterResults.contains(false);
    }

/*
    public List<T> filterIsNull(List<T> list, Set<String> nullFields) {
        if (list == null)
            return null;
        if (nullFields == null)
            return list;

        return list.stream()
            .filter(annotation -> containsNull(annotation, nullFields))
            .collect(Collectors.toList());
    }

    public boolean containsNull(T list,  Set<String> nullFields) {
        Set<Boolean> filterResults = nullFields.stream()
            .map((entry) -> {
                FilterFunction<T, String> filterFunction = filtering.filterFieldMap.get(entry);
                if (filterFunction == null)
                    return null;
                return filterFunction.valueIsNull(list);
            })
            .collect(Collectors.toSet());
        return !filterResults.contains(false);
    }
*/
/*
    public List<T> getSortedResults(Pagination pagination, List<T> list, Sorting<T> sorting) {
        // sorting
        SortingField sortingField = null;
        String sortBy = pagination.getSortBy();
        if (sortBy != null && !sortBy.isEmpty())
            sortingField = SortingField.getSortingField(sortBy.toUpperCase());

        list.sort(sorting.getComparator(sortingField, pagination.getAsc()));

        // paginating
        return list.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());
    }
*/
}

