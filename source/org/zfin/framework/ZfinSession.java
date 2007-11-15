package org.zfin.framework;

import org.zfin.people.Person;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Sep 11, 2006
 * Time: 1:52:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ZfinSession {

    private long zdbID;
    private String sessionID;
    private String userID;
    private String userName;
    private Person person;
    // ToDo; Make this an enumeration
    private String status;
    private Date dateCreated;
    private Date dateModified;

    public long getZdbID() {
        return zdbID;
    }

    public void setZdbID(long zdbID) {
        this.zdbID = zdbID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
