package org.zfin.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Has to sit here, because we use it in the SessionCreator for instantiateDB.  Should go away once properly configured.
*/
public class MockAuthenticationManager implements AuthenticationManager {
     public MockAuthenticationManager(boolean b) {
     }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return new TestingAuthenticationToken(authentication.getPrincipal(),authentication.getCredentials()){

        };
    }
}
