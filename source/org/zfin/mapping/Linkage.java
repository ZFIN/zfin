package org.zfin.mapping;

import org.zfin.infrastructure.ActiveSource;
import org.zfin.infrastructure.ZdbID;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;


public class Linkage {
    protected String zdbID;
    protected long id;
    protected String chromosome;
    protected Person person;
    protected String comments;
    protected Set<LinkageMember> linkageMemberSet;
    private Publication publication;
    private Person personReference;
    private String referenceID;

    public String getZdbID() {
        return zdbID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getChromosome() {
        if (chromosome.equals("0"))
            return "unknown";
        return chromosome;
    }

    public void setChromosome(String lg) {
        this.chromosome = lg;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Person getPersonReference() {
        return personReference;
    }

    public void setPersonReference(Person personReference) {
        this.personReference = personReference;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }

    public ZdbID getReference() {
        if (ActiveSource.isValidActiveData(referenceID, ActiveSource.Type.PUB))
            return publication;
        else
            return personReference;

    }

    public Set<LinkageMember> getLinkageMemberSet() {
        return linkageMemberSet;
    }

    public void setLinkageMemberSet(Set<LinkageMember> linkageMemberSet) {
        this.linkageMemberSet = linkageMemberSet;
    }

    public void addLinkageMember(LinkageMember member) {
        if (linkageMemberSet == null)
            linkageMemberSet = new HashSet<>(4);
        linkageMemberSet.add(member);
        member.setLinkage(this);
        // add inverse linkage entity
        linkageMemberSet.add(member.getInverseMember());
    }

}
