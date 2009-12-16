package org.zfin.security;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;

/**
 * Retrieves user details from the database.
 */
public class UserDetailServiceImpl implements UserDetailsService {

    public UserDetails loadUserByUsername(String username) {
        UserRepository userRep = RepositoryFactory.getUserRepository();
        Person person = userRep.getPersonByLoginName(username);

        if (person == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return person;
    }

}
