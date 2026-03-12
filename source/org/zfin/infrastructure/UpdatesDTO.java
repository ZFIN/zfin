package org.zfin.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.profile.Person;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.PublicationTrackingStatus;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public record UpdatesDTO(
        @JsonView(View.API.class)
        Long id,

        @JsonView(View.API.class)
        String submitterName,

        @JsonView(View.API.class)
        String submitterImage,

        @JsonView(View.API.class)
        String fieldName,

        @JsonView(View.API.class)
        String oldValue,

        @JsonView(View.API.class)
        String newValue,

        @JsonView(View.API.class)
        String comments,

        Date whenUpdated,

        @JsonView(View.API.class)
        @JsonProperty("whenUpdated")
        String whenUpdatedString,

        @JsonView(View.API.class)
        String submitterZdbID
) {

    @JsonView(View.API.class)
    @JsonProperty("uniqueKey")
    public String uniqueKey() {
        return id + fieldName + whenUpdatedString;
    }


    public static List<UpdatesDTO> fromUpdates(List<Updates> updates) {
        return updates.stream()
                .map(UpdatesDTO::updateToDTO)
                .collect(Collectors.toList());
    }

    private static UpdatesDTO updateToDTO(Updates update) {
        String name = update.getSubmitter() != null ? update.getSubmitter().getDisplay() : update.getSubmitterName();
        String image = update.getSubmitter() != null ? update.getSubmitter().getImage() : null;
        String zdbID = update.getSubmitter() != null ? update.getSubmitter().getZdbID() : null;
        return new UpdatesDTO(
                update.getID(),
                name,
                image,
                update.getFieldName(),
                update.getOldValue(),
                update.getNewValue(),
                update.getComments(),
                update.getWhenUpdated(),
                formatDate(update.getWhenUpdated()),
                zdbID
        );
    }

    public static List<UpdatesDTO> fromPublicationEvents(List<PublicationTrackingHistory> events) {
        List<UpdatesDTO> updates = new ArrayList<>();
        UpdatesDTO previous = null;

        List<PublicationTrackingHistory> orderedEvents = events.stream().sorted(Comparator.comparing(PublicationTrackingHistory::getDate)).toList();

        for(PublicationTrackingHistory event : orderedEvents) {
            UpdatesDTO tempEvent = publicationEventToDTO(event, previous);
            updates.add(tempEvent);
            previous = tempEvent;
        }
        updates.sort(Comparator.comparing(UpdatesDTO::whenUpdated));
        return updates;
    }

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static String formatDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    private static String formatCalendar(Calendar cal) {
        if (cal == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(cal.getTimeZone());
        return sdf.format(cal.getTime());
    }

    private static UpdatesDTO publicationEventToDTO(PublicationTrackingHistory publicationEvent, UpdatesDTO previous) {
        PublicationTrackingStatus.Name statusName = publicationEvent.getStatus().getName();
        Calendar date = publicationEvent.getDate();
        Person performedBy = publicationEvent.getPerformedBy();

        String submitterName = performedBy != null ? performedBy.getDisplay() : null;
        String image = performedBy != null ? performedBy.getImage() : null;
        String fieldName = "Status";
        String oldValue = previous != null ? previous.newValue() : null;
        String newValue = statusName.toString();
        String display = publicationEvent.getDisplay();
        String submitterZdbID = performedBy != null ? performedBy.getZdbID() : null;

        return new UpdatesDTO(
                publicationEvent.getId(),
                submitterName,
                image,
                fieldName,
                oldValue,
                newValue,
                display,
                date.getTime(),
                formatCalendar(date),
                submitterZdbID
        );
    }
}
