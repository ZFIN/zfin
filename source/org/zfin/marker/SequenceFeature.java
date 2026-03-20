package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.framework.api.View;

/**
 *  @MappedSuperclass tells Hibernate: "this class isn't an entity itself, but its mapped fields should be inherited by entity subclasses."
 *
 *   SequenceFeature has no table of its own. When Hibernate processes Marker (which extends SequenceFeature), it pulls in the field mappings — zdbID → mrkr_zdb_id and name →
 *   mrkr_name — and treats them as if they were declared directly on Marker. The same happens for Antibody, Clone, Transcript, etc., but since those use @Inheritance(JOINED), they
 *   get their primary key from Marker's table via @PrimaryKeyJoinColumn.
 *
 *   The chain looks like:
 *
 *   SequenceFeature (@MappedSuperclass)     ← defines @Id on zdbID, @Column on name
 *       └── Marker (@Entity, table="marker")     ← inherits those columns into the marker table
 *           ├── Antibody (@Entity, table="antibody")     ← joins via atb_zdb_id
 *           ├── Clone (@Entity, table="clone")           ← joins via clone_mrkr_zdb_id
 *           └── ...
 *
 *   The @GenericGenerator for ZdbIdGenerator is also on SequenceFeature, which is slightly awkward since it's Marker-specific. But since Marker is the only entity extending
 *   SequenceFeature, it works fine. If another entity ever extended SequenceFeature with a different table/generator, you'd need to move the @Id and generator down to Marker and use
 *    @AttributeOverride — but that's not the case today.
 */
@MappedSuperclass
@Setter
@Getter
public class SequenceFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Marker")
    @GenericGenerator(name = "Marker",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "isMarker", value = "true"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "mrkr_zdb_id")
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    public String zdbID;

    @Transient
    public String nameOrder;

    @Column(name = "mrkr_name", nullable = false)
    @JsonView({View.API.class, View.ExpressedGeneAPI.class})
    public String name;
}
