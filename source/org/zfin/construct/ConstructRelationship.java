package org.zfin.construct;

/**
 * Created by prita on 3/2/2015.
 */

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Parameter;
import org.zfin.framework.StringEnumValueUserType;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.Set;


/**
 * Class MarkerRelationship.
 */
@Setter
@Getter
@Entity
@Table(name = "construct_marker_relationship")
public class ConstructRelationship implements EntityAttribution {

    public enum Type {


        CODING_SEQUENCE_OF("coding sequence of"),
        CONTAINS_REGION("contains region"),
        CONTAINS_POLYMORPHISM("contains polymorphism"),

        PROMOTER_OF("promoter of");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public String getValue() {
            return value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No ConstructRelationship type of string " + type + " found.");
        }
    }

    @Id
    @GeneratedValue(generator = "zdbIdGeneratorCMREL")
    @GenericGenerator(name = "zdbIdGeneratorCMREL", strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "CMREL"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "conmrkrrel_zdb_id")
    @JsonView(View.API.class)
    private String zdbID;

    @Column(name = "conmrkrrel_relationship_type")
    @org.hibernate.annotations.Type(value = StringEnumValueUserType.class,
            parameters = {@Parameter(name = "enumClassname", value = "org.zfin.construct.ConstructRelationship$Type")})
    @JsonView(View.API.class)
    private Type type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conmrkrrel_construct_zdb_id")
    @LazyToOne(LazyToOneOption.NO_PROXY)
    @JsonView(View.API.class)
    private ConstructCuration construct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conmrkrrel_mrkr_zdb_id")
    @LazyToOne(LazyToOneOption.NO_PROXY)
    @JsonView(View.API.class)
    private Marker marker;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "recattrib_data_zdb_id")
    private Set<PublicationAttribution> publications;

    public int getPublicationCount() {
        if (publications == null)
            return 0;
        else
            return publications.size();
    }

    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            return getPublications().iterator().next().getPublication();
        } else {
            return null;
        }
    }
}


