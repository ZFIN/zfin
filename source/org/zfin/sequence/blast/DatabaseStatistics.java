package org.zfin.sequence.blast;

import lombok.Getter;
import lombok.Setter;

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
    private Date creationDate;
    private Date modifiedDate;

   public boolean isSet() {
        return
                creationDate != null
                        &&
                        modifiedDate != null
                        &&
                        numAccessions != BAD_DATABASE;
    }
}
