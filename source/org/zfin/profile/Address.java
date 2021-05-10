package org.zfin.profile;

import org.apache.commons.lang.StringUtils;

import javax.validation.constraints.Size;
import java.io.Serializable;

public class Address implements Serializable {

    //    @NotNull
    private Long id;

    @Size(min = 17, max = 25)
    private String ownerZdbID;

    private String institution;
    private String street1;
    private String street2;
    private String city;

    private String stateCode;
    private String countryCode;
    private String postalCode;

    private String composite;




    public String getOwnerZdbID() {
        return ownerZdbID;
    }

    public void setOwnerZdbID(String ownerZdbID) {
        this.ownerZdbID = ownerZdbID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getComposite() {
        return composite;
    }

    public void setComposite(String composite) {
        this.composite = composite;
    }

    // returns true if the address is not valid
    public Boolean isInvalidAddress() {
        return
                StringUtils.isEmpty(countryCode)
                        ||
                        StringUtils.isEmpty(street1)
                        ||
                        StringUtils.isEmpty(city)
                        ||
                        (StringUtils.isEmpty(stateCode) && countryCode.equals("USA"))
                        ||
                        StringUtils.isEmpty(postalCode)

                ;
    }
    // sadly necessary for jsp access
    public Boolean getInvalidAddress() {
        return isInvalidAddress();
    }


    // returns true if the address is not valid
    public Boolean isEmptyAddress() {
        return StringUtils.isEmpty(institution)
                && StringUtils.isEmpty(street1)
                && StringUtils.isEmpty(street2)
                && StringUtils.isEmpty(city)
                && StringUtils.isEmpty(stateCode)
                && StringUtils.isEmpty(countryCode)
                && StringUtils.isEmpty(postalCode)
                ;
    }
    public Boolean getEmptyAddress() {
        return isEmptyAddress();
    }

}
