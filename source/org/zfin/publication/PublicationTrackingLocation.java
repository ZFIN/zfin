package org.zfin.publication;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "pub_tracking_location")
public class PublicationTrackingLocation {

    public enum Role {
        CURATOR,
        INDEXER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ptl_pk_id")
    private long id;

    @Column(name = "ptl_location_display")
    private String name;

    @Column(name = "ptl_role")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingLocation$Role")})
    private Role role;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
