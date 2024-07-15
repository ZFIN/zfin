package org.zfin.profile;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

/**
 * Lab domain model.
 */
@Entity
@Table(name = "lab")
public class Lab extends Organization implements Serializable  {

    public Set<Person> getMemberList() {
        return memberList;
    }

    public void setMemberList(Set<Person> memberList) {
        this.memberList = memberList;
    }

    @ManyToMany
    @JoinTable(
            name = "int_person_lab",
            joinColumns = @JoinColumn(name = "target_id"),
            inverseJoinColumns = @JoinColumn(name = "source_id")
    )
    private Set<Person> memberList;

    public int compareTo(Organization org) {
        if (org == null)
            return -1;
        return name.compareTo(org.getName());
    }

    public boolean getLab() {
        return true;
    }

    public boolean getCompany() {
        return false;
    }

    @Override
    public String getType() {
        return "lab";
    }


    @Override
    public String toString() {
        return "Lab{" + super.toString() + "}";
    }

}
