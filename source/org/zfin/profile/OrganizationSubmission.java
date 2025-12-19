package org.zfin.profile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationSubmission {

    private String name;
    private String type;
    private String contactPerson;
    private String email;
    private String email2;
    private String address;
    private String phone;
    private String fax;
    private String url;
    private String comments;

    public String toText() {
        return "Organization Name: " + name + "\n" +
                "Type: " + type + "\n" +
                "Contact Person: " + contactPerson + "\n" +
                "Email: " + email + "\n" +
                "Address: " + address + "\n" +
                "Phone: " + phone + "\n" +
                "Fax: " + fax + "\n" +
                "URL: " + url + "\n" +
                "Comments: " + comments + "\n\n";
    }
}
