package org.zfin.infrastructure;

import org.zfin.people.Person;

import java.io.Serializable;

public class PersonAttribution extends RecordAttribution implements Serializable, Comparable<PersonAttribution>{

    private Person person ;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
		setSourceZdbID(person.getZdbID());
    }

    /**
     * Implemented this
     * @param personAttribution
     * @return
     */
    public int compareTo(PersonAttribution personAttribution) {
        if (personAttribution == null)
            return -1;
        if (personAttribution == null)
            return +1;
        return person.compareTo(personAttribution.getPerson());
    }
}
