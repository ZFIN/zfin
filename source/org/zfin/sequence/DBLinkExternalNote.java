package org.zfin.sequence;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ExternalNote;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * DBLink specific external note.
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("dblink")
public class DBLinkExternalNote extends ExternalNote {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private DBLink dblink;

}
