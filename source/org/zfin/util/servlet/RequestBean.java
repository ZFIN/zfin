package org.zfin.util.servlet;

import org.zfin.database.TableLock;
import org.zfin.profile.Person;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RequestBean {

    private Person person;
    private String name;
    private String request;
    private Map queryParameter;
    private Cookie[] cookies;
    private Cookie tomcatJSessioncookie;
    private String queryRequestString;
    private HttpServletRequest httRequest;
    private List<TableLock> locks;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Map getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(Map queryParameter) {
        this.queryParameter = queryParameter;
    }

    public Cookie[] getCookies() {
        return cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    public Cookie getTomcatJSessioncookie() {
        return tomcatJSessioncookie;
    }

    public void setTomcatJSessioncookie(Cookie tomcatJSessioncookie) {
        this.tomcatJSessioncookie = tomcatJSessioncookie;
    }

    public String getQueryRequestString() {
        return queryRequestString;
    }

    public void setQueryRequestString(String queryRequestString) {
        this.queryRequestString = queryRequestString;
    }

    public HttpServletRequest getHttRequest() {
        return httRequest;
    }

    public void setHttRequest(HttpServletRequest httRequest) {
        this.httRequest = httRequest;
    }

    public Map<String, String> getRequestParameterMap() {
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> set = queryParameter.keySet().iterator();
        while (set.hasNext()) {
            String key = set.next();
            String[] vals = (String[]) queryParameter.get(key);
            if (vals.length == 1)
                map.put(key, vals[0]);
            else {
                int index = 0;
                for (String value : vals)
                    map.put(key + "[" + index++ + "]", value);
            }
        }
        return map;
    }

    public String getReferrer() {
        return httRequest.getHeader("referer");
    }

    public String getUserAgent() {
        return httRequest.getHeader("user-agent");
    }

    public Date getRequestDate() {
        return new Date();
    }

    public void setLocks(List<TableLock> locks) {
        this.locks = locks;
    }

    public List<TableLock> getLocks() {
        return locks;
    }
}
