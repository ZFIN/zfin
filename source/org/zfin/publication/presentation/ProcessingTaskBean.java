package org.zfin.publication.presentation;

import org.zfin.curation.presentation.PersonDTO;
import org.zfin.publication.ProcessingChecklistTask;

import java.util.Calendar;

public class ProcessingTaskBean {

    private long id;
    private ProcessingChecklistTask.Task task;
    private PersonDTO person;
    private Calendar date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProcessingChecklistTask.Task getTask() {
        return task;
    }

    public void setTask(ProcessingChecklistTask.Task task) {
        this.task = task;
    }

    public PersonDTO getPerson() {
        return person;
    }

    public void setPerson(PersonDTO person) {
        this.person = person;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
