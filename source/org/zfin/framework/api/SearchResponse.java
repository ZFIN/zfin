package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
public class SearchResponse<E> extends ApiResponse {

    @JsonView({View.Default.class})
    private List<E> results = new ArrayList<E>();

    @JsonView({View.Default.class})
    private Long totalResults;

    @JsonView({View.Default.class})
    private Integer returnedRecords;

    @JsonView({View.Default.class})
    private Map<String, Map<String, Long>> aggregations;

    @JsonView({View.Default.class})
    private String debug;

    public SearchResponse() {
    }

    public SearchResponse(List<E> results) {
        setResults(results);
    }

    public void setResults(List<E> results) {
        this.results = results;
        if (results != null) {
            returnedRecords = results.size();
        } else {
            this.results = new ArrayList<E>();
        }
    }

    public E getSingleResult() {
        return (results == null || CollectionUtils.isEmpty(results)) ? null : results.get(0);
    }
}
