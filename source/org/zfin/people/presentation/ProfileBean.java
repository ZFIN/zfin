package org.zfin.people.presentation;

import org.zfin.people.Person;
import org.zfin.people.User;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Bean that holds info about people, publication and other profile-related data.
 */
public class ProfileBean {

    private Person person;
    private User user;

    public Person getPerson() {
        if (person == null)
            person = new Person();
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    //ToDo; This is a recurring method for many pages.
    // Make it reusable: jsp tag definition or 
    public AuditLogItem getLatestUpdate(){
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        AuditLogItem latestLogItem = alr.getLatestAuditLogItem(person.getZdbID());
        return latestLogItem;
    }


}
