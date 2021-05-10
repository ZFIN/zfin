package org.zfin.publication.presentation;

import org.zfin.profile.Person;

import java.util.Calendar;

public interface PublicationEvent {

    Person getPerformedBy();
    Calendar getDate();
    String getDisplay();

}
