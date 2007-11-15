package org.zfin.security;

import org.acegisecurity.*;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.vote.AccessDecisionVoter;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Validate somebodies has an allowed role.
 */
public class ZfinAuthenticatedVoter implements AccessDecisionVoter {

    public static final String ROOT = "root";
    public static final String SUBMIT = "submit";
    public static final String GUEST = "guest";

    public boolean supports(ConfigAttribute attribute) {
        return (attribute.getAttribute() != null)
                && (ZfinAuthenticatedVoter.ROOT.equals(attribute.getAttribute())
                || ZfinAuthenticatedVoter.SUBMIT.equals(attribute.getAttribute())
                || ZfinAuthenticatedVoter.GUEST.equals(attribute.getAttribute()));
    }

    /**
     * This implementation supports any type of class, because it does not query the presented secure object.
     *
     * @param clazz the secure object
     * @return always <code>true</code>
     */
    public boolean supports(Class clazz) {
        return true;
    }

    public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config) {
        int result = ACCESS_ABSTAIN;
        Iterator iter = config.getConfigAttributes();

        while (iter.hasNext()) {
            ConfigAttribute attribute = (ConfigAttribute) iter.next();

            if (this.supports(attribute)) {
                result = ACCESS_DENIED;

                final Collection granted = getPrincipalAuthorities();
                Set grantedCopy = retainAll(granted, parseAuthoritiesString(attribute.getAttribute()));

                if (grantedCopy.size() > 0) {
                    return ACCESS_GRANTED;
                }
            }
        }

        return result;
    }

    private Collection getPrincipalAuthorities() {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();

        if (null == currentUser) {
            return Collections.EMPTY_LIST;
        }

        if ((null == currentUser.getAuthorities()) || (currentUser.getAuthorities().length < 1)) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(currentUser.getAuthorities());
    }

    private Set parseAuthoritiesString(String authorizationsString) {
        final Set requiredAuthorities = new HashSet();
        final String[] authorities = StringUtils.commaDelimitedListToStringArray(authorizationsString);

        for (String authority : authorities) {
            // Remove the role's whitespace characters without depending on JDK 1.4+
            // Includes space, tab, new line, carriage return and form feed.
            String role = StringUtils.replace(authority, " ", "");
            role = StringUtils.replace(role, "\t", "");
            role = StringUtils.replace(role, "\r", "");
            role = StringUtils.replace(role, "\n", "");
            role = StringUtils.replace(role, "\f", "");

            requiredAuthorities.add(new GrantedAuthorityImpl(role));
        }

        return requiredAuthorities;
    }

    private Set retainAll(final Collection granted, final Set required) {
        Set grantedRoles = authoritiesToRoles(granted);
        Set requiredRoles = authoritiesToRoles(required);
        grantedRoles.retainAll(requiredRoles);

        return rolesToAuthorities(grantedRoles, granted);
    }


    private Set authoritiesToRoles(Collection c) {
        Set target = new HashSet();

        for (Object aC : c) {
            GrantedAuthority authority = (GrantedAuthority) aC;

            if (null == authority.getAuthority()) {
                throw new IllegalArgumentException(
                        "Cannot process GrantedAuthority objects which return null from getAuthority() - attempting to process "
                                + authority.toString());
            }

            target.add(authority.getAuthority());
        }

        return target;
    }

    private Set rolesToAuthorities(Set grantedRoles, Collection granted) {
        Set target = new HashSet();

        for (Object grantedRole : grantedRoles) {
            String role = (String) grantedRole;

            for (Object aGranted : granted) {
                GrantedAuthority authority = (GrantedAuthority) aGranted;

                if (authority.getAuthority().equals(role)) {
                    target.add(authority);

                    break;
                }
            }
        }

        return target;
    }
}
