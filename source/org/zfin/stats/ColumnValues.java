package org.zfin.stats;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Range;
import org.zfin.framework.api.View;

import java.util.Map;

@Setter
@Getter
public class ColumnValues {

    @JsonView(View.API.class)
    private String value;
    @JsonView(View.API.class)
    private long totalNumber;
    @JsonView(View.API.class)
    private long totalDistinctNumber;
    @JsonView(View.API.class)
    private Map<String, Integer> histogram;
    @JsonView(View.API.class)
    private Map<String, Integer> uberHistogram;
    private Range<Integer> cardinality;
    @JsonView(View.API.class)
    private Range<Integer> multiplicity;

    @JsonView(View.API.class)
    public String getCardinality() {
        return cardinality == null ? "" : cardinality.toString();
    }

    @JsonView(View.API.class)
    public String getMultiplicity() {
        return multiplicity == null ? "" : multiplicity.toString();
    }
}
