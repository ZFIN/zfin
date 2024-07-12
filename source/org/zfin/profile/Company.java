package org.zfin.profile;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.Set;

/**
 * Company domain model.
 */
public class Company extends Organization {

    private String bio;


    public Set<Person> getMemberList() {
        return memberList;
    }

    public void setMemberList(Set<Person> memberList) {
        this.memberList = memberList;
    }

    @ManyToMany
    @JoinTable(
            name = "int_person_company",
            joinColumns = @JoinColumn(name = "target_id"),
            inverseJoinColumns = @JoinColumn(name = "source_id")
    )
    private Set<Person> memberList;

    public int compareTo(Organization org) {
        if (org == null)
            return -1;
        return name.compareTo(org.getName());
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean getLab() {
        return false;
    }

    public boolean getCompany() {
        return true;
    }

    @Override
    public String getType() {
        return "company";
    }

}