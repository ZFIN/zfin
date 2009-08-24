package org.zfin.infrastructure;

import org.zfin.people.Person;

import java.io.Serializable;


/**
 * This class is an attribution to a person rather than a publication, meaning
 * a person made a statement not recorded in a publication.
 */
public class PersonAttribution extends RecordAttribution implements Serializable {

    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
        setSourceZdbID(person.getZdbID());
    }

}