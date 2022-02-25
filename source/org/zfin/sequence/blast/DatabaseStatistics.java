package org.zfin.sequence.blast;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.util.JsonDateSerializer;
import org.zfin.util.JsonSimpleDateSerializer;

import java.util.Date;

/**
 * Holds blast database statistics.
 */
@Setter
@Getter
public class DatabaseStatistics {

    public static final int BAD_DATABASE = -1;

    private int numSequences = BAD_DATABASE;
    private int numAccessions = BAD_DATABASE;
    @JsonView(View.API.class)
    @JsonSerialize(using = JsonSimpleDateSerializer.class)
    private Date creationDate;
    @JsonView(View.API.class)
    @JsonSerialize(using = JsonSimpleDateSerializer.class)
    private Date modifiedDate;
    @JsonView(View.API.class)
    private Database database;

   public boolean isSet() {
        return
                creationDate != null
                        &&
                        modifiedDate != null
                        &&
                        numAccessions != BAD_DATABASE;
    }
}
