package org.zfin.construct;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.zfin.construct.presentation.ConstructComponentService;
import org.zfin.marker.MarkerType;
import org.zfin.profile.Person;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Setter
@Getter
public class ConstructCuration {

    private String zdbID;
    private String publicComments;
    private Person owner;
    private Set<ConstructRelationship> constructRelations;

    private String name;
    private MarkerType constructType;

    private Date modDate;
    private Date createdDate;

    /**
     * Create a basic new construct curation object from the name.
     * It uses the name to set the construct type and initializes the dates.
     *
     * @param name
     * @return
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
