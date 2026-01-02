package org.zfin.construct;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.construct.presentation.ConstructComponentService;
import org.zfin.framework.api.View;
import org.zfin.marker.MarkerType;
import org.zfin.profile.Person;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "construct")
@org.hibernate.annotations.DynamicUpdate
public class ConstructCuration {

    @Id
    @GeneratedValue(generator = "zdbIdGeneratorConstruct")
    @GenericGenerator(name = "zdbIdGeneratorConstruct", strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "isConstruct", value = "true"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "construct_zdb_id")
    @JsonView(View.API.class)
    private String zdbID;

    @Column(name = "construct_comments")
    private String publicComments;

    @ManyToOne
    @JoinColumn(name = "construct_owner_zdb_id", nullable = false)
    private Person owner;

    @OneToMany(mappedBy = "construct")
    private Set<ConstructRelationship> constructRelations;

    @Column(name = "construct_name", nullable = false)
    @JsonView(View.API.class)
    private String name;

    @ManyToOne
    @JoinColumn(name = "construct_type", nullable = false)
    private MarkerType constructType;

    @Column(name = "construct_date_modified", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date modDate;

    @Column(name = "construct_date_inserted", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createdDate;

    /**
     * Create a basic new construct curation object from the name.
     * It uses the name to set the construct type and initializes the dates.
     *
     * @param name construct name
     * @return new construct curation object
     */
    public static ConstructCuration create(String name) {
        ConstructCuration newConstruct = new ConstructCuration();

        Optional<MarkerType> constructMarkerType = ConstructComponentService.getConstructTypeByConstructName(name);
        constructMarkerType.ifPresent(newConstruct::setConstructType);

        newConstruct.setName(name);
        newConstruct.setCreatedDate(new Date());
        newConstruct.setModDate(new Date());
        return newConstruct;
    }

    public void setPublicCommentsIfNotEmpty(String publicComments) {
        if (StringUtils.isNotEmpty(publicComments)) {
            this.publicComments = publicComments;
        }
    }

}
