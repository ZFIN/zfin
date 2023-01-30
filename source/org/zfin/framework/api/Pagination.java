package org.zfin.framework.api;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Setter
@Getter
public class Pagination {

    private Integer page = 1;
    private Integer limit = 20;
    private String sortBy;
    //    private FieldFilter sortByField;
    private Boolean asc;
    private BaseFilter fieldFilterValueMap = new BaseFilter();
    private List<String> errorList = new ArrayList<>();
    private List<String> invalidFilterList = new ArrayList<>();

    private Map<String, String> filterMap = new HashMap<>();
    private boolean isCount = false;

    public Pagination(Integer page, Integer limit, String sortBy, String asc) {
        if (page != null)
            this.page = page;
        if (limit != null)
            this.limit = limit;
        this.sortBy = sortBy;
//        sortByField = FieldFilter.getFieldFilterByName(sortBy);
        if (this.page < 1)
            errorList.add("'page' request parameter invalid: Found [" + page + "]. It has to be an integer number greater than 0");
        if (this.limit < 0)
            errorList.add("'limit' request parameter invalid: Found [" + limit + "].  It has to be an integer number greater than 0");
        init(asc);
    }

    public Pagination() {
        isCount = true;
    }

    public boolean isCountPagination() {
        return isCount;
    }

    private void init(String asc) {
        if (asc == null) {
            this.asc = true;
        } else {
            if (!AscendingValues.isValidValue(asc)) {
                String message = "Invalid 'asc' value. Needs to have the following values: [";
                message = message + AscendingValues.getAllValues() + "]";
                errorList.add(message);
            }
            this.asc = AscendingValues.getValue(asc);
        }
    }

    public void addFieldFilter(FieldFilter fieldFilter, String value) {
        fieldFilterValueMap.put(fieldFilter, value);
    }

    public String getFieldFilter(FieldFilter fieldFilter) {
        return fieldFilterValueMap.get(fieldFilter);
    }

    public void makeSingleFieldFilter(FieldFilter fieldFilter, String value) {
        fieldFilterValueMap.clear();
        fieldFilterValueMap.put(fieldFilter, value);
    }

    public void removeFieldFilter(FieldFilter fieldFilter) {
        fieldFilterValueMap.remove(fieldFilter);
    }

    public boolean hasErrors() {
        return !errorList.isEmpty();
    }

    public List<String> getErrors() {
        return errorList;
    }

    public boolean sortByDefault() {
        if (StringUtils.isEmpty(sortBy))
            return true;
        if (sortBy.equalsIgnoreCase("default"))
            return true;
        return false;
    }

    public String getAscending() {
        return asc ? "ASC" : "DESC";
    }


    /**
     * Uses 1-based indexing due to the client-side expectations
     * @return
     */
    public int getStart() {
        if (page == null || page == 0)
            return 0;
        return (page - 1) * limit;
    }

    public int getEnd() {
        return page * limit;
    }

    public boolean hasInvalidElements() {
        return invalidFilterList == null || !invalidFilterList.isEmpty();
    }

    public void setLimitToAll() {
        limit = Integer.MAX_VALUE;
    }

    public Integer getNextPage() {
        return page + 1;
    }

    enum AscendingValues {
        TRUE(true), FALSE(false), YES(true), NO(false), UP(true), DOWN(false);

        private Boolean val;

        AscendingValues(Boolean val) {
            this.val = val;
        }

        public static boolean isValidValue(String name) {
            for (AscendingValues val : values()) {
                if (val.name().equalsIgnoreCase(name))
                    return true;
            }
            return false;
        }

        public static String getAllValues() {
            StringJoiner values = new StringJoiner(",");
            Arrays.asList(values()).forEach(sorting ->
                    values.add(sorting.name()));
            return values.toString();
        }

        public static Boolean getValue(String asc) {
            for (AscendingValues val : values()) {
                if (val.name().equalsIgnoreCase(asc))
                    return val.val;
            }
            return null;
        }
    }

    public void addToFilterMap(String key, String value){
        filterMap.put(key, value);
    }

    public Map<String, String> getFilterMap(){
        return filterMap;
    }

    public int getIndexOfFirstElement() {
        return (page - 1) * limit;
    }

    public static Pagination getDownloadPagination() {
        return new Pagination(1, Integer.MAX_VALUE, null, null);
    }
}
