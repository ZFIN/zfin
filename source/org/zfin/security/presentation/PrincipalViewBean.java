package org.zfin.security.presentation;

import org.springframework.security.core.session.SessionInformation;

import java.util.List;

/**
 * For viewing principals with a session.
 */
public class PrincipalViewBean {

    private Object principal ;
    private List<SessionInformation> sessionList ;

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public List<SessionInformation> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<SessionInformation> sessionList) {
        this.sessionList = sessionList;
    }
}
