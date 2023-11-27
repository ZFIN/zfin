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
    private Function<Entity, String> singleValueEntityFunction;
    private Function<SubEntity, String> singleValueSubEntityFunction;
    private Function<SubEntity, List<String>> multiValueSubEntityFunction;

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues) {
        this.name = name;
        this.superEntity = superEntity;
        this.rowEntity = rowEntity;
        this.multiValued = multiValued;
        this.limitedValues = limitedValues;
    }

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues, Function<Entity, List<SubEntity>> function, Function<Entity, String> sfunction) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.singleValueEntityFunction = sfunction;
        this.multiValueFunction = function;
    }

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues, Function<SubEntity, String> function) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.singleValueSubEntityFunction = function;
    }

    public ColumnStats(String name, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues, boolean isMulti, Function<SubEntity, List<String>> function) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.multiValueSubEntityFunction = function;
    }

    public ColumnStats(String name, Function<Entity, String> function, boolean superEntity, boolean rowEntity, boolean multiValued, boolean limitedValues) {
        this(name, superEntity, rowEntity, multiValued, limitedValues);
        this.singleValueEntityFunction = function;
    }

    public String getSingleValue(Entity a) {
        if (singleValueEntityFunction == null)
            return null;
        return singleValueEntityFunction.apply(a);
    }

    @JsonView(View.API.class)
    public String getFilterName() {
        return ZfinStringUtils.getCamelCase(name);
    }
}
