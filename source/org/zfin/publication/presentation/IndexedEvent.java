package org.zfin.publication.presentation;

import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Calendar;

public class IndexedEvent implements PublicationEvent {

    private Person indexer;
    private Calendar date;

    IndexedEvent(Publication publication) {
        indexer = publication.getIndexedBy();
        date = publication.getIndexedDate();
    }

    @Override
    public Person getPerformedBy() {
        return indexer;
    }

    @Override
    public Calendar getDate() {
        return date;
    }

    @Override
    public String getDisplay() {
        return "Indexed paper";
    }
}
