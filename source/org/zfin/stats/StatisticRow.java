package org.zfin.stats;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.zfin.framework.api.View;

import java.io.Serializable;
import java.util.LinkedHashMap;

@Setter
@Getter
public class StatisticRow<Entity, SubEntity> implements Serializable {

    @JsonView(View.API.class)
    private LinkedHashMap<String, Column<Entity, SubEntity>> columns;

    public void put(ColumnStats<Entity, SubEntity> geneStat, ColumnValues columnValues) {
        Column<Entity, SubEntity> col = new Column<>();
        col.setColumnDefinition(geneStat);
        col.setColumnStat(columnValues);
        if (MapUtils.isEmpty(columns))
            columns = new LinkedHashMap<>();
        columns.put(col.getColumnDefinition().getName(), col);
    }


}
