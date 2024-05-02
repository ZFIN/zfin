package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.zfin.framework.api.View;

import jakarta.persistence.*;
import java.util.Arrays;

@Setter
@Getter
@Entity
@Table(name = "pub_tracking_location")
public class PublicationTrackingLocation {

    public enum Role {
        CURATOR,
        INDEXER,
        STUDENT,
    }

    public enum Name {
        X_1("Write 1x"),
        X_2("Write 2x"),
        LOTS("Write lots"),
        BIN_1("Bin 1"),
        BIN_2("Bin 2"),
        DRUG("Drug"),
        ENVIRONMENTAL_TOX("Environmental Tox"),
        NANOMATERIALS("Nanomaterials"),
        NATURAL_PRODUCT("Natural Product"),
        XENOGRAFT("Xenograft"),
        PHENOTYPE("New Phenotype"),
        EXPRESSION("New Expression"),
        ORTHOLOGY("Orthology"),
        BIN_3("Bin 3"),
        INDEXER_PRIORITY_1("1"),
        INDEXER_PRIORITY_2("2"),
        INDEXER_PRIORITY_3("3"),
        INDEXER_UNPRIORITIZED("Unprioritized"), //special value without DB entry that maps to instances without any location
        DISEASE("Disease"),
        ZEBRASHARE("ZebraShare");

        private String display;

        Name(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        @Override
        @JsonValue
        public String toString() {
            return display;
        }

        public static Name fromString(String value) throws Exception {
            return Arrays.stream(values()).filter(n -> n.display.equals(value)).findAny().orElse(null);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ptl_pk_id")
    @JsonView(View.API.class)
    private long id;

    @Column(name = "ptl_location_display")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingLocation$Name")})
    @JsonView(View.API.class)
    private Name name;

    @Column(name = "ptl_role")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingLocation$Role")})
    @JsonView(View.API.class)
    private Role role;

    @Column(name = "ptl_display_order")
    @JsonView(View.API.class)
    private int displayOrder;

}
