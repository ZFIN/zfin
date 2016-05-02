package org.zfin.infrastructure;

import org.zfin.profile.Person;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
@DiscriminatorValue("Person ")
public class PersonAttribution extends RecordAttribution implements Serializable, Comparable<PersonAttribution> {

    @ManyToOne
    @JoinColumn(name = "recattrib_source_zdb_id", insertable = false, updatable = false)
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
    @Override
    public int compareTo(PersonAttribution personAttribution) {
        if (personAttribution == null)
            return -1;
        return person.compareTo(personAttribution.getPerson());
    }
}
