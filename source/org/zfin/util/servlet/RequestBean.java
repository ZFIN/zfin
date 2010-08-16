package org.zfin.util.servlet;

import org.zfin.people.Person;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

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
}
