package org.zfin.stats;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import java.io.Serializable;

@Setter
@Getter
public class Column<Entity, SubEntity> implements Serializable {

    @JsonView(View.API.class)
    private ColumnStats<Entity, SubEntity> columnDefinition;
    @JsonView(View.API.class)
    private ColumnValues columnStat;

}

