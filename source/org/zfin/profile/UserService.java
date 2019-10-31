package org.zfin.profile;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.PrimaryKey;
import org.zfin.framework.mail.AbstractZfinMailSender;
import org.zfin.framework.mail.MailSender;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Business class that contains business logic for user-related data.
 */
public class UserService {

    /**
     * Check if the current security user has Root privileges.
     *
     * @return boolean
     */
    public static boolean isRootUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object o = authentication.getPrincipal();
        if (o instanceof Person) {
            Person person = (Person) o;
            AccountInfo accountInfo = person.getAccountInfo();
            String role = accountInfo.getRole();
            if (role != null && role.equals("root"))
                return true;
        }
        return false;
    }


    /**
     * Check if the current security user is the owner of the record defined
     * by the Object mapped to the clazz and the record id.
     *
     * @param zdbID primary key
     * @param clazz not used
     * @return boolean
     */
    public static boolean isOwner(String zdbID, Class clazz) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object o = authentication.getPrincipal();
        if (!(o instanceof Person))
            return false;

        Person user = (Person) o;
        String personID = user.getZdbID();

        Session session = HibernateUtil.currentSession();
        PrimaryKey entity = (PrimaryKey) session.load(clazz, zdbID);
        String ownerID = entity.getOwnerID();
        // If no owner string available: return false
        if (ownerID == null)
            return false;

        return ownerID.equals(personID);

    }


    public static boolean hasRole(String roleName) {
        if (StringUtils.isEmpty(roleName))
            return false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object o = authentication.getPrincipal();
        if (o instanceof AccountInfo) {
            AccountInfo accountInfo = (AccountInfo) o;
            String role = accountInfo.getRole();
            if (role != null && role.equals(roleName))
                return true;
        }
        return false;
    }

    public static void setPasswordResetKey(Person person) {
        String passwordResetKey = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        Date passwordResetDate = Calendar.getInstance().getTime();

        AccountInfo accountInfo = person.getAccountInfo();

        accountInfo.setPasswordResetKey(passwordResetKey);
        accountInfo.setPasswordResetDate(passwordResetDate);

//        HibernateUtil.currentSession().save(accountInfo);
        HibernateUtil.currentSession().flush();

    }

    public static boolean passwordResetKeyIsValid(AccountInfo accountInfo, String key) {

        if (accountInfo == null
                || accountInfo.getPasswordResetKey() == null
                || accountInfo.getPasswordResetDate() == null) { return false; }

        //if the keys don't match, return false
        if (!StringUtils.equals(accountInfo.getPasswordResetKey(),key)) {
            return false;
        }

        Date issueDate = accountInfo.getPasswordResetDate();
        Calendar expirationCalendar = Calendar.getInstance();
        expirationCalendar.setTime(issueDate);
        expirationCalendar.add(Calendar.HOUR, 24);
        Date expirationDate = expirationCalendar.getTime();
        Date today = Calendar.getInstance().getTime();


        //return true only if today is before the expirationDate and after the issueDate
        if (today.compareTo(expirationDate) < 0 && today.compareTo(issueDate) > 0) {
            return true;
        }

        return false;

    }
}
