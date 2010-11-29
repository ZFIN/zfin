package org.zfin.people;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.PrimaryKey;

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
}
