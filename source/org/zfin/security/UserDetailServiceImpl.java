package org.zfin.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;

/**
 * Retrieves user details from the database.
 */
public class UserDetailServiceImpl implements UserDetailsService {

    public UserDetails loadUserByUsername(String username) {
        Person person;
        try {
            UserRepository userRep = RepositoryFactory.getUserRepository();
            person = userRep.getPersonByLoginName(username);

            if (person == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
        } finally {
            HibernateUtil.closeSession();
        }
        return person;
    }

}
