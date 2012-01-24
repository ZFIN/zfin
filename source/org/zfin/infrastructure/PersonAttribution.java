package org.zfin.infrastructure;

import org.zfin.people.Person;

import java.io.Serializable;

public class PersonAttribution extends RecordAttribution implements Serializable, Comparable<PersonAttribution> {

    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
        setSourceZdbID(person.getZdbID());
    }

    /**
     * Compare by person object.
     *
     * @param personAttribution PersonAttribution
     * @return comparison integer
     */
    public int compareTo(PersonAttribution personAttribution) {
        if (personAttribution == null)
            return -1;
        return person.compareTo(personAttribution.getPerson());
    }
}
