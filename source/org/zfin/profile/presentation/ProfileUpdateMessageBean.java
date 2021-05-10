package org.zfin.profile.presentation;

import org.zfin.profile.service.BeanFieldUpdate;

import java.io.Serializable;
import java.util.List;

/**
 */
public class ProfileUpdateMessageBean implements Serializable {

    //    private Person person ;
    private List<BeanFieldUpdate> fields;
    private String securityPersonZdbID;
    private String zdbIdToEdit;

    private ProfileType profileType = ProfileType.PERSON ;

    public static enum ProfileType {
        ACCOUNT_INFO,
        PERSON,
        ;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public List<BeanFieldUpdate> getFields() {
        return fields;
    }

    public void setFields(List<BeanFieldUpdate> fields) {
        this.fields = fields;
    }

    public String getSecurityPersonZdbID() {
        return securityPersonZdbID;
    }

    public void setSecurityPersonZdbID(String securityPersonZdbID) {
        this.securityPersonZdbID = securityPersonZdbID;
    }

    public String getZdbIdToEdit() {
        return zdbIdToEdit;
    }

    public void setZdbIdToEdit(String personZdbID) {
        this.zdbIdToEdit = personZdbID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ProfileUpdateMessageBean");
        sb.append("{fields=").append(fields);
        sb.append(", securityPersonZdbID='").append(securityPersonZdbID).append('\'');
        sb.append(", zdbIdToEdit='").append(zdbIdToEdit).append('\'');
        sb.append(", profileType=").append(profileType);
        sb.append('}');
        return sb.toString();
    }
}
