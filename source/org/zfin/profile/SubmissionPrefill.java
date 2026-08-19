package org.zfin.profile;

import org.apache.commons.lang3.StringUtils;
import org.zfin.infrastructure.submission.SubmissionLog;
import org.zfin.infrastructure.submission.SubmissionType;

/**
 * Copies a logged account request onto the person creation form, so a coordinator following the
 * "create the account" link from the notification email does not have to retype the request.
 */
public final class SubmissionPrefill {

    private SubmissionPrefill() {
    }

    /**
     * Fills in any field the person has left empty from the submission. Values already typed by the
     * coordinator win, so this is safe to apply to a partly filled form.
     *
     * @return true if the submission was usable and anything was applied
     */
    public static boolean apply(SubmissionLog submission, Person person) {
        if (submission == null || person == null || submission.getType() != SubmissionType.PERSON) {
            return false;
        }
        boolean applied = false;
        applied |= copyIfBlank(person.getFirstName(), submission.getFirstName(), person::setFirstName);
        applied |= copyIfBlank(person.getLastName(), submission.getLastName(), person::setLastName);
        applied |= copyIfBlank(person.getEmail(), submission.getEmail(), person::setEmail);
        applied |= copyIfBlank(person.getOrcidID(), submission.getOrcid(), person::setOrcidID);
        applied |= copyIfBlank(person.getPhone(), submission.getPhone(), person::setPhone);
        applied |= copyIfBlank(person.getAddress(), submission.getAddress(), person::setAddress);
        applied |= copyIfBlank(person.getCountry(), submission.getCountry(), person::setCountry);
        applied |= copyIfBlank(person.getUrl(), submission.getUrl(), person::setUrl);
        return applied;
    }

    private interface Setter {
        void set(String value);
    }

    private static boolean copyIfBlank(String existing, String submitted, Setter setter) {
        if (StringUtils.isNotBlank(existing) || StringUtils.isBlank(submitted)) {
            return false;
        }
        setter.set(submitted.trim());
        return true;
    }
}
