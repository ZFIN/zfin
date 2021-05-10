package org.zfin.curation.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.ObjectUtils;
import org.zfin.framework.api.View;

public class PersonDTO implements Comparable<PersonDTO> {

    @JsonView(View.API.class) private String zdbID;
    @JsonView(View.API.class) private String firstName;
    @JsonView(View.API.class) private String lastName;
    @JsonView(View.API.class) private String name;
    @JsonView(View.API.class) private String email;
    @JsonView(View.API.class) private String imageURL;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public int compareTo(PersonDTO o) {
        return ObjectUtils.compare(name, o.getName());
    }
}
