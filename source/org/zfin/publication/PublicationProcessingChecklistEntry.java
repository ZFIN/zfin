package org.zfin.publication;

import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.GregorianCalendar;

@Entity
@Table(name = "publication_processing_checklist")
public class PublicationProcessingChecklistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ppc_pk_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "ppc_pub_zdb_id")
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "ppc_person_zdb_id")
    private Person person;

    @Column(name = "ppc_date_completed")
    private GregorianCalendar date;

    @ManyToOne
    @JoinColumn(name = "ppc_task_id")
    private ProcessingChecklistTask task;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public ProcessingChecklistTask getTask() {
        return task;
    }

    public void setTask(ProcessingChecklistTask task) {
        this.task = task;
    }
}
