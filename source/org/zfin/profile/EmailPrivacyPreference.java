package org.zfin.profile;

import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.service.ProfileService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.function.Supplier;

@Getter
@Setter
@Entity
@Table(name = "email_privacy_preference")
public class EmailPrivacyPreference {

    @Id
    @Column(name = "epp_pk_id")
    private Long id;

    @Column(name = "epp_name")
    private String name;

    @Column(name = "epp_description")
    private String description;

    @Column(name = "epp_order")
    private Integer order;

    public String toString() {
        return name;
    }

    /**
     * Figure out if the email address should be visible to the current user.
     * @return
     */
    public boolean isVisible() {
        return isVisibleToUser(ProfileService::getCurrentSecurityUser);
    }

    /**
     * Figure out if the email address should be hidden from the current user.
     * @return
     */
    public boolean isHidden() {
        return !isVisible();
    }

    /**
     * Figure out if the email address should be visible to the given user.
     * Using a supplier to potentially avoid a database call if the privacy preference is public.
     *
     * @param personSupplier
     * @return
     */
    private boolean isVisibleToUser(Supplier<Person> personSupplier) {
        if (isPublic()) {
            return true;
        }
        Person currentUser = personSupplier.get();
        if (currentUser == null) {
            return false;
        }
        if (isRegisteredOnly() && currentUser.isLoginAccount()) {
            return true;
        }
        return currentUser.isRootAccount();
    }

    private boolean isPublic() {
        return getName().equals(EmailPrivacyPreference.Name.PUBLIC.toString());
    }

    private boolean isRegisteredOnly() {
        return getName().equals(EmailPrivacyPreference.Name.REGISTERED.toString());
    }

    public enum Name {
        PUBLIC("Visible to All"),
        REGISTERED("Visible to Registered Users"),
        HIDDEN("Not Visible");

        private final String value;

        Name(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}