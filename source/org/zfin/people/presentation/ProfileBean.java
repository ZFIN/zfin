package org.zfin.people.presentation;

import org.zfin.people.Person;
import org.zfin.people.AccountInfo;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Bean that holds info about people, publication and other profile-related data.
 */
public class ProfileBean {

    public static final String ACTION_DELETE = "delete-user";
    public static final String ACTION_EDIT = "edit-user";
    public static final String ACTION_CREATE = "create-user";

    private Person person;
    private AccountInfo accountInfo;
    private String passwordOne;
    private String passwordTwo;
    private String action;
    private boolean newUser;

    public Person getPerson() {
        if (person == null)
            person = new Person();
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public boolean isOwnerOrRoot() {
        Person securityUser = Person.getCurrentSecurityUser();
        if (AccountInfo.Role.ROOT.toString().equals(securityUser.getAccountInfo().getRole()))
            return true;
        return securityUser.getAccountInfo().equals(accountInfo);
    }

    public String getPasswordOne() {
        return passwordOne;
    }

    public void setPasswordOne(String passwordOne) {
        this.passwordOne = passwordOne;
    }

    public String getPasswordTwo() {
        return passwordTwo;
    }

    public void setPasswordTwo(String passwordTwo) {
        this.passwordTwo = passwordTwo;
    }//ToDo; This is a recurring method for many pages.

    // Make it reusable: jsp tag definition or
    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(person.getZdbID());
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean deleteRecord() {
        return (action != null && action.equals(ACTION_DELETE));
    }

    public boolean createRecord() {
        return (action != null && action.equals(ACTION_CREATE));
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public AccountInfo getAccountInfo() {
        if (accountInfo == null)
            accountInfo = new AccountInfo();
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }
}
