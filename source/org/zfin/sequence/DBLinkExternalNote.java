package org.zfin.sequence;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ExternalNote;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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
