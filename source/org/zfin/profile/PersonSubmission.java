package org.zfin.profile;

import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.service.ProfileService;

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
    private String country;
    private String phone;
    private String lab;
    private String role;
    private String url;
    private String orcid;
    private String comments;

    public String toText() {
        return "First Name: " + firstName + "\n" +
                "Last Name: " + lastName + "\n" +
                "Email: " + email + "\n" +
                "Address: " + address + "\n" +
                "Country: " + country + "\n" +
                "Phone: " + phone + "\n" +
                "Lab: " + lab + "\n" +
                "URL: " + url + "\n" +
                "ORCID: " + orcid + "\n" +
                "Comments: " + comments + "\n\n";
    }
}
