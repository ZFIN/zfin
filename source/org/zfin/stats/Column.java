package org.zfin.stats;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import java.io.Serializable;

@Setter
@Getter
public class Column implements Serializable {

    @JsonView(View.API.class)
    private ColumnStats columnDefinition;
    @JsonView(View.API.class)
    private ColumnValues columnStat;

}

