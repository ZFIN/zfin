package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonValue;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Table(name = "pub_tracking_location")
public class PublicationTrackingLocation {

    public enum Role {
        CURATOR,
        INDEXER
    }

    public enum Name {
        BIN_1("Bin 1"),
        BIN_2("Bin 2"),
        PHENOTYPE("New Phenotype"),
        EXPRESSION("New Expression"),
        ORTHOLOGY("Orthology"),
        BIN_3("Bin 3"),
        INDEXER_PRIORITY_1("1"),
        INDEXER_PRIORITY_2("2"),
        INDEXER_PRIORITY_3("3"),
        DISEASE("Disease"),
        ZEBRASHARE("ZebraShare");

        private String display;

        Name(String display) {
            this.display = display;
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
    private long id;

    @Column(name = "ptl_location_display")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingLocation$Name")})
    private Name name;

    @Column(name = "ptl_role")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingLocation$Role")})
    private Role role;

    @Column(name = "ptl_display_order")
    private int displayOrder;

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
