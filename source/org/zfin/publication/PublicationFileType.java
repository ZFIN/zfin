package org.zfin.publication;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "publication_file_type")
public class PublicationFileType {

    public enum Name {
        ORIGINAL_ARTICLE,
        ANNOTATED_ARTICLE,
        SUPPLEMENTAL_MATERIAL,
        CORRESPONDENCE_DETAILS,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pft_pk_id")
    private long id;

    @Column(name = "pft_type")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.publication.PublicationFileType$Name")
    })
    private Name name;

    @Column(name = "pft_type_order")
    private int order;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
