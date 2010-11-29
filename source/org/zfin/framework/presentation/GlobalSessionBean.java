package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.zfin.security.presentation.PrincipalViewBean;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 */
public class GlobalSessionBean {

    private static Logger logger = Logger.getLogger(GlobalSessionBean.class);

    private SessionRegistry sessionRegistry;
    private HttpSession currentSession;

    public List<PrincipalViewBean> getPrincipals(){
        List<Object> principals = sessionRegistry.getAllPrincipals() ;

        List<PrincipalViewBean> principalViewBeans = new ArrayList<PrincipalViewBean>() ;
        for(Object principal: principals){
            PrincipalViewBean principalViewBean = new PrincipalViewBean() ;
            principalViewBean.setPrincipal(principal);
            List<SessionInformation> sessionInformations = sessionRegistry.getAllSessions(principal,true) ;
            principalViewBean.setSessionList(sessionInformations);
            principalViewBeans.add(principalViewBean) ;
        }

        return principalViewBeans ;
    }

    public int getNumberOfGlobalSessions(){
        int count = 0 ;
        List<Object> principals = sessionRegistry.getAllPrincipals() ;

        for(Object principal: principals){
            count+= sessionRegistry.getAllSessions(principal,true).size() ;
        }
        return count;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public HttpSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(HttpSession currentSession) {
        this.currentSession = currentSession;
    }

    public Map<String,Object> getSessionAttributes(){
        Map<String,Object> sessionAttributeMap = new HashMap<String,Object>() ;
        if(currentSession!=null){
            Enumeration attributeNamesEnumeration = currentSession.getAttributeNames() ;
            while(attributeNamesEnumeration.hasMoreElements()){
                String name = attributeNamesEnumeration.nextElement().toString() ;
                Object value = currentSession.getAttribute(name) ;
                sessionAttributeMap.put(name,value) ;
            }
        }
        return sessionAttributeMap;
    }
}
