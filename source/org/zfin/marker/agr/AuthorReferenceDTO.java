package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class AuthorReferenceDTO {

    private String firstName;
    private String lastName;
    private List<String> middleNames;
    private String name;
    private List<CrossReferenceDTO> crossReferences;
    private String referenceId;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getMiddleNames() {
        return middleNames;
    }

    public void setMiddleNames(List<String> middleNames) {
        this.middleNames = middleNames;
    }

    public List<CrossReferenceDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}