package org.zfin.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            throw new UsernameNotFoundException("User not found: "+username);
        }

        return person;
    }

}
