package org.zfin.framework.search;


import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An individual parameter in a search
 */
public class SearchCriterion {
    public static String WHITESPACE_SEPARATOR = "\\s+";

    private String value;
    // used to keep the term names associated to the term id in the value attribute
    private String name;
    private SearchCriterionType type;
    private boolean isMultiValued;
    // used for multiple entries in value
    private String separator;
    // used for multiple entries in name entity
    private String nameSeparator;


    public SearchCriterion(SearchCriterionType type, boolean isMultiValued) {
        this.type = type;
        this.isMultiValued = isMultiValued;
    }

    public SearchCriterion(SearchCriterionType type, String value, boolean isMultiValued) {
        this.type = type;
        this.value = StringUtils.trimToNull(value);
        this.isMultiValued = isMultiValued;
    }


    //todo: should eventually handle quoted phrases - maybe by using the lucene whitespace tokenizer?
    public List<String> getValues() {

        if (StringUtils.isEmpty(value))
            return null;

        List<String> queryTerms = new ArrayList<String>();

        //commas in the warehouse are replaced with spaces for now, we'd rather do that
        //in neither place, but for now it'll happen here to match the warehouse.
        // ... but only when we split on whitespace, NOT when we split on commas!
        if (separator.equals(WHITESPACE_SEPARATOR))
            value = value.replace(","," ");

        if (separator.equals(WHITESPACE_SEPARATOR)) {
            for (String term : value.split(WHITESPACE_SEPARATOR)) {
                if (StringUtils.isNotEmpty(term))
                    queryTerms.add(term);
            }
        } else {
            //copied and slightly modified from antibodybean - maybe should use the same syntax as above?
            String[] array;
            array = value.split(separator);

            for (int i = 0; i < array.length; i++) {
                array[i] = array[i].trim();
                queryTerms.add(array[i]);
            }
        }
        return queryTerms;
    }

    /*
        public List<String> getExactMatchValues() {

            List<String> queryTerms = new ArrayList<String>();

            if (separator.equals(WHITESPACE_SEPARATOR)) {
                for (String term : value.split(WHITESPACE_SEPARATOR)) {
                    queryTerms.add(term);
                }
            } else {
                //copied and slightly modified from antibodybean - maybe should use the same syntax as above?
                String[] array;
                array = value.split(separator);

                for (int i = 0; i < array.length; i++) {
                    array[i] = array[i].trim();
                    queryTerms.add(array[i]);
                }
            }
            return queryTerms;
        }



        public String getBtsValues() {
            StringBuilder sb = new StringBuilder();
            for (String value : getExactMatchValues()) {
                value = btsClean(value);
                if (!StringUtils.isEmpty(sb.toString()))
                    sb.append(" and ");
                sb.append(value);
            }
            return sb.toString();
        }



        public String getBtsExpandedQuery() {
            StringBuilder sb = new StringBuilder();

            for (String value : getValues()) {

                if (!StringUtils.isEmpty(sb.toString()))
                    sb.append(" and ");
                sb.append(" (");
                sb.append(value);
                sb.append("^1000 or ");
                sb.append(value);
                sb.append("*) ");
            }
            return sb.toString();
        }

        private String btsClean(String value) {
            if (value == null)
                return null;

            List<String> charactersToRemove = Arrays.asList("-",":","\\(","\\)","\\[","\\]","\\.",",");
            for (String removeMe : charactersToRemove)
              value = value.replaceAll(removeMe,"");

            return value;
        }
    */
    public boolean isTrue() {
        return StringUtils.equals(value, "true");
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SearchCriterionType getType() {
        return type;
    }

    public void setType(SearchCriterionType type) {
        this.type = type;
    }

    public boolean isMultiValued() {
        return isMultiValued;
    }

    public void setMultiValued(boolean multiValued) {
        isMultiValued = multiValued;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameSeparator() {
        return nameSeparator;
    }

    public void setNameSeparator(String nameSeparator) {
        this.nameSeparator = nameSeparator;
    }

    public List<String> getNames() {
        if (StringUtils.isEmpty(name))
            return null;
        String[] token = name.split(nameSeparator);
        if (token.length != getValues().size())
            throw new RuntimeException("Not the same number of entries.");
        List<String> names = new ArrayList<String>(token.length);
        for (String name : token)
            names.add(name);
        return names;
    }

    public boolean hasValues() {
        if (StringUtils.isEmpty(value))
            return false;
        return true;
    }
}


