package org.zfin.datatransfer.ctd;

import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.zfin.publication.Publication;

import jakarta.persistence.*;

@Entity
@Table(name = "publication_ctd")
@Data
public class PublicationCtd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pc_id")
    private long id;
    @Column(name = "pc_ctd_id")
    private String ctdID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pc_pub_zdb_id")
    private Publication publication;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PublicationCtd that = (PublicationCtd) o;

        return new EqualsBuilder().append(getCtdID(), that.getCtdID()).append(getPublication(), that.getPublication()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getCtdID()).append(getPublication()).toHashCode();
    }
}
