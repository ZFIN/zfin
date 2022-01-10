package org.zfin.stats;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.util.ZfinStringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

@Setter
@Getter
public class ColumnStats<Entity, SubEntity> implements Serializable {

    @JsonView(View.API.class)
    private String name;
    @JsonView(View.API.class)
    private boolean superEntity;
    @JsonView(View.API.class)
    private boolean rowEntity;
    @JsonView(View.API.class)
    private boolean multiValued;
    @JsonView(View.API.class)
    private boolean limitedValues;

    private Function<Entity, List<SubEntity>> multiValueFunction;
    private Function<Entity, String> singleValueFunction;

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues) {
        this.name = name;
        this.superEntity = superEntity;
        this.rowEntity = rowEntity;
        this.multiValued = multiValued;
        this.limitedValues = limitedValues;
    }

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues, Function<Entity, List<SubEntity>> function, Function<Entity, String> sfunction) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.singleValueFunction = sfunction;
        this.multiValueFunction = function;
    }

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues, Function<Entity, String> function) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.singleValueFunction = function;
    }

    public String getSingleValue(Entity a) {
        if (singleValueFunction == null)
            return null;
        return singleValueFunction.apply(a);
    }

    @JsonView(View.API.class)
    public String getFilterName() {
        return ZfinStringUtils.getCamelCase(name);
    }
}
