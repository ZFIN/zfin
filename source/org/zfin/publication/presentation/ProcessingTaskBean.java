package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.curation.presentation.PersonDTO;
import org.zfin.framework.api.View;
import org.zfin.publication.ProcessingChecklistTask;

import java.util.Calendar;

public class ProcessingTaskBean {

    @JsonView(View.API.class) private long id;
    @JsonView(View.API.class) private ProcessingChecklistTask.Task task;
    @JsonView(View.API.class) private PersonDTO person;
    @JsonView(View.API.class) private Calendar date;

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
