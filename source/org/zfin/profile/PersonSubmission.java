package org.zfin.profile;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain business object that describes a request to create a new account for a single person.
 */
@Getter
@Setter
public class PersonSubmission {

    private String firstName;
    private String lastName;
    private String email;
    private String email2;
    private String address;
    private String phone;
    private String lab;
    private String role;
    private String url;
    private String orcid;
    private String comments;

}
